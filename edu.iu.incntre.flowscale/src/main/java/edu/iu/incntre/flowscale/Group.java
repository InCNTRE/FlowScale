package edu.iu.incntre.flowscale;

import grnoc.util.net.ipaddress.IPv4Address;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.beaconcontroller.core.IOFSwitch;

import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFPhysicalPort;
import org.openflow.protocol.OFPort;
import org.openflow.protocol.OFPortStatus.OFPortReason;
import org.openflow.protocol.OFType;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionOutput;
import org.openflow.util.HexString;
import org.openflow.util.U16;

public class Group {

	private String[] values;
	private int groupId;
	private String groupName;
	private List<Short> inputPorts = new ArrayList<Short>();
	private List<Short> outputPorts = new ArrayList<Short>();
	private ArrayList<Short> outputPortsUp = new ArrayList<Short>();
	private short priority;
	private long inputSwitchDatapathId;
	private long outputSwitchDatapathId;
	public static final short ETHERTYPE_IP = 0X800;
	public static final byte NWTYPE_TCP = 6;
	public static final int MAXIMUM_FLOWS_TO_PUSH = 600;
	private int type;
	private int maximumFlowsAllowed;
	private boolean dropPortGroup = false;
	private FlowscaleController flowscaleController;

	private ArrayList<OFRule> groupRules = new ArrayList<OFRule>();

	public Group(FlowscaleController controller) {

		this.flowscaleController = controller;

	}

	public void switchUpAlert(IOFSwitch sw) {

		List<OFPhysicalPort> physicalPorts = sw.getFeaturesReply().getPorts();

		for (OFPhysicalPort ofp : physicalPorts) {

			if (ofp.getState() == 0
					&& this.outputPorts.contains(ofp.getPortNumber())) {
				FlowscaleController.logger.debug("added port {}",
						ofp.getPortNumber());
				outputPortsUp.add(ofp.getPortNumber());

			}

		}

		FlowscaleController.logger.debug("output ports up after addeing is {}",
				outputPortsUp.size());

		pushRules();
	}

	public void switchDownAlert(IOFSwitch sw) {

		this.groupRules.clear();

	}

	public void addGroupDetails(String groupIdString, String groupName,
			String inputSwitchDatapathIdString,
			String outputSwitchDatapathIdString, String inputPortListString,
			String outputPortListString, String typeString,
			String priorityString, String valuesString,
			String maximumFlowsAllowedString) {

		try {
			groupId = Integer.parseInt(groupIdString);
			FlowscaleController.logger.info("group ID added {}", groupId);
		} catch (NumberFormatException nfe) {
			FlowscaleController.logger.error("{}", nfe);
			return;
		}

		this.groupName = groupName;

		inputSwitchDatapathId = HexString.toLong(inputSwitchDatapathIdString);
		outputSwitchDatapathId = HexString.toLong(outputSwitchDatapathIdString);

		// for (String s : inputPortsString) {

		// inputPorts.add(Short.parseShort(s));

		// }

		if (outputPortListString.equals("")) {

			dropPortGroup = true;

		} else {

			String[] outputPortsString = outputPortListString.split(",");
			FlowscaleController.logger.debug(
					"output ports before adding group arre {}",
					outputPortListString);
			for (String s : outputPortsString) {
				try {
					outputPorts.add(Short.parseShort(s));
				} catch (NumberFormatException numbe) {
					FlowscaleController.logger.error("{}", numbe);
					continue;
				}
			}

		}

		type = Integer.parseInt(typeString);
		maximumFlowsAllowed = Integer.parseInt(maximumFlowsAllowedString);

		priority = Short.parseShort(priorityString);

		values = valuesString.split(",");

		// get ports that are up

		// populate ports

		List<OFPhysicalPort> physicals = flowscaleController.getSwitchDevices()
				.get(this.outputSwitchDatapathId).getPortStates();

		if (physicals != null) {

			for (OFPhysicalPort physical : physicals) {
				if (physical.getState() == 0
						&& outputPorts.contains(physical.getPortNumber()))
					outputPortsUp.add(physical.getPortNumber());

			}

			FlowscaleController.logger.debug("ports up in this group are {}",
					outputPortsUp);
		}

		FlowscaleController.logger.debug("ports up are {}", this.outputPortsUp);

	}

	private void generateRules() throws ArithmeticException {
		FlowscaleController.logger.info("in generating rules with type {}",
				type);
		switch (type) {

		case 1:
			try{generateIPRules();
			
			}catch(ArithmeticException ae){
				throw ae;
			}

			break;

		case 2:
			generateTransportPortRules();
			break;

		case 3:
			generateEtherRules();
			break;

		}

		// generate rules from values given
	}

	private void generateIPRules() throws ArithmeticException {

		FlowscaleController.logger.debug(" up ports are {}", outputPortsUp);

		int flowForEachValue = (int) (this.maximumFlowsAllowed / values.length);
		ArrayList<IPAddress> ipAddressValues = null;

		for (String s : values) {

			String[] ipAndSubnet = s.split("/");

			ipAddressValues = generateIPandSubnets(ipAndSubnet,
					flowForEachValue / 2);

			int i = 0;

			int actionPort = 0;
			short rulePriority = this.priority;
			for (IPAddress ipAddress : ipAddressValues) {

				if (this.dropPortGroup) {
					actionPort = -1;
				} else {
					try{
					actionPort = this.outputPortsUp.get(i
							% outputPortsUp.size());
					}catch(ArithmeticException ae){
						throw new ArithmeticException();
					}
				}
				i++;

				// set source rule

				OFRule ofRuleSource = new OFRule();
				OFMatch ofMatchSource = new OFMatch();
				ofMatchSource.setDataLayerType((short) 0x0800);
				ofMatchSource.setNetworkSource(ipAddress.getIpAddressValue());
				ofMatchSource.setWildcards(OFMatch.OFPFW_ALL
						^ OFMatch.OFPFW_DL_TYPE ^ OFMatch.OFPFW_NW_SRC_MASK);

				short maskingBits = (short) (ipAddress.getSubnet() - 1);
				int wildCardSource = OFMatch.OFPFW_ALL ^ OFMatch.OFPFW_DL_TYPE
						^ OFMatch.OFPFW_NW_SRC_ALL
						^ (maskingBits << OFMatch.OFPFW_NW_SRC_SHIFT);

				ofMatchSource.setWildcards(wildCardSource);

				ofRuleSource.setMatch(ofMatchSource);
				ofRuleSource.setPriority(rulePriority);
				ofRuleSource.setPort(actionPort);

				FlowscaleController.logger.debug(
						"ip address match is {} and masking bit is {} ",
						ipAddress.getIpAddressValue(), ipAddress.getSubnet());

				FlowscaleController.logger.debug(
						"match is {} and wildcard is {}",
						ofMatchSource.toString(), wildCardSource);

				this.groupRules.add(ofRuleSource);

				// end set source rule

				// set destination rule:
				OFRule ofRuleDestination = new OFRule();
				OFMatch ofMatchDestination = new OFMatch();
				ofMatchDestination.setDataLayerType((short) 0x0800);
				ofMatchDestination.setNetworkDestination(ipAddress
						.getIpAddressValue());
				ofMatchDestination.setWildcards(OFMatch.OFPFW_ALL
						^ OFMatch.OFPFW_DL_TYPE ^ OFMatch.OFPFW_NW_DST_MASK);

				int wildCardDestination = OFMatch.OFPFW_ALL
						^ OFMatch.OFPFW_DL_TYPE ^ OFMatch.OFPFW_NW_DST_ALL
						^ (maskingBits << OFMatch.OFPFW_NW_DST_SHIFT);

				ofMatchDestination.setWildcards(wildCardDestination);

				ofRuleDestination.setMatch(ofMatchDestination);
				ofRuleDestination.setPriority(rulePriority);
				ofRuleDestination.setPort(actionPort);

				FlowscaleController.logger.debug(
						"ip address match is {} and masking bit is {} ",
						ipAddress.getIpAddressValue(), ipAddress.getSubnet());

				FlowscaleController.logger.debug(
						"match is {} and wildcard is {}",
						ofMatchDestination.toString(), wildCardDestination);

				this.groupRules.add(ofRuleDestination);

				// end set destination rules
				rulePriority++;
				
			}

		}

	}

	private ArrayList<IPAddress> generateIPandSubnets(String[] ipAndSubnet,
			int flowForEachValue) {
		ArrayList<IPAddress> ipAddressValues = new ArrayList<IPAddress>();
		FlowscaleController.logger.debug("in method generate ips and subnets");
		String byteValue = Long.toBinaryString(flowForEachValue);
		FlowscaleController.logger.debug("IP and subnet details {}", ipAndSubnet);
		FlowscaleController.logger.debug("group type is {}", type);
		int newSubnetValue = Integer.parseInt(ipAndSubnet[1])
				+ byteValue.length() - 1;

		int numberOfValues = (int) Math.pow(2, (byteValue.length() - 1));
		if (newSubnetValue > 32) {
			newSubnetValue = 32;

			numberOfValues = 32 - Integer.parseInt(ipAndSubnet[1]);
		}

		int oldByteValue = 32 - Integer.parseInt(ipAndSubnet[1]);

		int originalNumberOfFlows = (int) Math.pow(2, oldByteValue);
		if (originalNumberOfFlows < numberOfValues) {
			numberOfValues = originalNumberOfFlows;

		}

		IPv4Address ipv4Address = new IPv4Address(ipAndSubnet[0]);
		ipv4Address.setSubnet(Integer.parseInt(ipAndSubnet[1]));
		int ipAddressInt = ipv4Address.getIPv4AddressInt();

		IPAddress ipAddress = new IPAddress();
		ipAddress.setIpAddressValue(ipAddressInt);
		ipAddress.setSubnet(newSubnetValue);
		ipAddressValues.add(ipAddress);
		for (int i = 0; i < numberOfValues - 1; i++) {

			ipAddressInt = IPAddressUtility.incrementSubnet(ipAddressInt,
					newSubnetValue);

			ipAddress = new IPAddress();
			ipAddress.setIpAddressValue(ipAddressInt);
			ipAddress.setSubnet(newSubnetValue);
			ipAddressValues.add(ipAddress);

		}

		return ipAddressValues;

	}

	private void generateTransportPortRules() {

		// loop round robin through output ports

		int i = 0;

		int actionPort = 0;

		for (String s : values) {

			int portMatch = Integer.parseInt(s);
			if (this.dropPortGroup) {
				actionPort = -1;
			} else {
				actionPort = this.outputPortsUp.get(i % outputPortsUp.size());
			}
			i++;

			OFRule rule = new OFRule();
			
			rule.setPort(actionPort);
			OFMatch match = new OFMatch();
			match.setDataLayerType((short) ETHERTYPE_IP);
			match.setNetworkProtocol(NWTYPE_TCP);
			rule.setPriority(this.priority);
			match.setTransportDestination((short) portMatch);
			match.setWildcards(OFMatch.OFPFW_ALL ^ OFMatch.OFPFW_DL_TYPE
					^ OFMatch.OFPFW_NW_PROTO ^ OFMatch.OFPFW_TP_DST);
			rule.setMatch(match);

			groupRules.add(rule);

		}

	}

	private void generateEtherRules() {

		int i = 0;

		int actionPort = 0;

		for (String s : values) {

			int etherTypeMatch = (int) HexString.toLong(s);
			FlowscaleController.logger.debug("etherTypematch is {}",
					etherTypeMatch);

			if (this.dropPortGroup) {
				actionPort = -1;
			} else {
				actionPort = outputPortsUp.get(i % outputPortsUp.size());
			}
			i++;

			OFRule rule = new OFRule();
			rule.setPriority(this.priority);
			rule.setPort(actionPort);
			OFMatch match = new OFMatch();
			match.setDataLayerType((short) etherTypeMatch);
			rule.setWildcards(OFMatch.OFPFW_ALL ^ OFMatch.OFPFW_DL_TYPE);
			match.setWildcards((short) rule.getWildcards());
			rule.setMatch(match);

			groupRules.add(rule);

		}

	}

	public void pushRules() {
		if (this.groupRules == null || this.groupRules.size() == 0)
			try{generateRules();}
			catch(ArithmeticException ae){
				FlowscaleController.logger.error("no output ports are up , no rules injected");
				return;
			}

		// handle two switches later
		FlowscaleController.logger.info("in ruleset pushing rules now ");

		FlowscaleController.logger.debug("group rules are {} ", groupRules);
		// handle output switch first

		IOFSwitch outputSwitch = flowscaleController.getIBeaconProvider()
				.getSwitches().get(outputSwitchDatapathId);

		OFFlowMod flowModRule = new OFFlowMod();
		flowModRule.setType(OFType.FLOW_MOD);
		flowModRule.setCommand(OFFlowMod.OFPFC_ADD);
		flowModRule.setHardTimeout((short) 0);
		flowModRule.setIdleTimeout((short) 0);
		flowModRule.setBufferId(-1);

		int count = 0;

		int actionOutputLength;

		if (this.dropPortGroup) {
			actionOutputLength = 0;
		} else {
			actionOutputLength = OFActionOutput.MINIMUM_LENGTH;
		}

		for (OFRule rule : this.groupRules) {
			count++;

			flowModRule.setMatch(rule.getMatch());
			if (!this.dropPortGroup) {
				flowModRule.setActions(rule.getActions());
			}
			flowModRule.setLength(U16.t(OFFlowMod.MINIMUM_LENGTH
					+ actionOutputLength));
			flowModRule.setPriority(rule.getPriority());

			try {
				FlowscaleController.logger.debug("{}", flowModRule);
				outputSwitch.getOutputStream().write(flowModRule);
				if (count == MAXIMUM_FLOWS_TO_PUSH) {
					count = 0;

					outputSwitch.getOutputStream().flush();
					Thread.sleep(10);

				}
			} catch (InterruptedException interruptedException) {
				FlowscaleController.logger.error("{}", interruptedException);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				FlowscaleController.logger.error("{}", e);
			}

		}

		try {

			outputSwitch.getOutputStream().flush();

		} catch (Exception e) {
			FlowscaleController.logger.error("{}", e);
		}

	}

	public void editGroup(String updateType, String updateValue) {

		String[] updateValues = updateValue.split(",");
		IOFSwitch sw = flowscaleController.getIBeaconProvider().getSwitches()
				.get(this.inputSwitchDatapathId);
		int i = 0;

		if (updateType.equals("values")) {

			for (String s : updateValues) {

				String[] command = s.split(" ");

				if (command[0].equals("remove")) {

					removeRules(command[1], command[2]);

				} else if (command[0].equals("add")) {

					addRules(command[1], command[2]);
				}
			}

		} else if (updateType.equals("ports")) {

			for (String s : updateValues) {

				String[] command = s.split(" ");
				OFMatch ofMatch;

				if (command[0].equals("remove")) {

					alert(sw, Short.parseShort(command[1]), null,
							OFPortReason.OFPPR_DELETE);

				} else if (command[0].equals("add")) {
					alert(sw, Short.parseShort(command[1]), null,
							OFPortReason.OFPPR_ADD);

				}
			}

		}

	}

	public void removeGroup() {

		deleteAllRules();

	}

	private void deleteAllRules() {
		// algorithm to nuke all rules from all switches
		IOFSwitch sw = flowscaleController.ibeaconProvider.getSwitches().get(
				this.outputSwitchDatapathId);
		OFFlowMod flowToDelete = new OFFlowMod();
		flowToDelete.setCommand(OFFlowMod.OFPFC_DELETE_STRICT);
		flowToDelete.setOutPort(OFPort.OFPP_NONE);
		for (OFRule ofRule : this.groupRules) {

			flowToDelete.setMatch(ofRule.getMatch());
			flowToDelete.setPriority(priority);
			flowToDelete.setLength(U16.t(OFFlowMod.MINIMUM_LENGTH));

			try {

				FlowscaleController.logger
						.info("delete flow  {}", flowToDelete);
				sw.getOutputStream().write(flowToDelete);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				FlowscaleController.logger.error("{}", e);
			}

		}

		try {
			sw.getOutputStream().flush();
		} catch (IOException ioe) {
			FlowscaleController.logger.error("{}", ioe);
		}

	}

	public void alert(IOFSwitch sw, short portNum, OFPhysicalPort physicalPort,
			OFPortReason reason) {
		OFFlowMod updateFlow = new OFFlowMod();
		updateFlow.setType(OFType.FLOW_MOD);
		updateFlow.setCommand(OFFlowMod.OFPFC_ADD);
		updateFlow.setHardTimeout((short) 0);
		updateFlow.setIdleTimeout((short) 0);
		int portStatus = 0;

		if (physicalPort == null) {

			if (reason == OFPortReason.OFPPR_ADD) {

				portStatus = 0;

			} else if (reason == OFPortReason.OFPPR_DELETE) {
				portStatus = 1;
			}

		} else if (reason == null) {

			portStatus = physicalPort.getState();

		}

		FlowscaleController.logger
				.info("updating flows since there is a port modification");

		switch (portStatus) {

		case 0:
			FlowscaleController.logger
					.info("a port belonging to the group output ports has been added");

			outputPortsUp.add(portNum);
			int ruleDistribution = this.outputPortsUp.size();

			int i = 1;

			for (OFRule ofRule : this.groupRules) {

				if (i % ruleDistribution == 0) {

					OFActionOutput ofActionOutput = new OFActionOutput();
					ofActionOutput.setPort(portNum);
					ArrayList<OFAction> actionList = ofRule.getActions();
					actionList.clear();
					actionList.add(ofActionOutput);
					updateFlow.setMatch(ofRule.getMatch());
					updateFlow.setBufferId(-1);
					updateFlow.setPriority(ofRule.getPriority());

					updateFlow.setActions(actionList);
					updateFlow.setLength(U16.t(OFFlowMod.MINIMUM_LENGTH
							+ OFActionOutput.MINIMUM_LENGTH));

					try {
						FlowscaleController.logger.info(
								"modifying flow here  {}", updateFlow);

						sw.getOutputStream().write(updateFlow);

					} catch (IOException e) {
						// TODO Auto-generated catch block
						FlowscaleController.logger.error("{}", e);
					}

				}

				i++;
			}

			break;
		case 1:

			i = 0;

			outputPortsUp.remove(Short.valueOf(portNum));
			FlowscaleController.logger
					.info("port removed so flows for this will be updated");

			for (OFRule ofRule : this.groupRules) {

				OFActionOutput ofActionOutput = (OFActionOutput) ofRule
						.getActions().get(0);

				if (ofActionOutput.getPort() == (short) portNum) {

					ofActionOutput.setPort((short) this.outputPortsUp.get(i++
							% outputPortsUp.size()));
					ArrayList<OFAction> actionList = ofRule.getActions();
					actionList.clear();
					actionList.add(ofActionOutput);
					updateFlow.setMatch(ofRule.getMatch());
					updateFlow.setBufferId(-1);
					updateFlow.setPriority(this.priority);

					updateFlow.setActions(actionList);
					updateFlow.setLength(U16.t(OFFlowMod.MINIMUM_LENGTH
							+ OFActionOutput.MINIMUM_LENGTH));

					try {
						FlowscaleController.logger.info("updating flow {}",
								updateFlow);

						sw.getOutputStream().write(updateFlow);

					} catch (IOException e) {
						// TODO Auto-generated catch block
						FlowscaleController.logger.error("{}", e);
					}

				}

			}

			break;

		}
		try {
			sw.getOutputStream().flush();
			FlowscaleController.logger.info("update complete");
		} catch (IOException ioe) {
			FlowscaleController.logger.error("{}", ioe);
		}

		// logger.info("with status of {}", ps.get )

	}

	private void removeRules(String value, String option) {

	}

	private void addRules(String value, String option) {

	}

	public int getMaximumFlowsAllowed() {
		return maximumFlowsAllowed;
	}

	public void setMaximumFlowsAllowed(int maximumFlowsAllowed) {
		this.maximumFlowsAllowed = maximumFlowsAllowed;
	}

	public int getGrouopId() {
		return groupId;
	}

	public void setGrouopId(int grouopId) {
		this.groupId = grouopId;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public List<Short> getInputPorts() {
		return inputPorts;
	}

	public void setInputPorts(List<Short> inputPorts) {
		this.inputPorts = inputPorts;
	}

	public List<Short> getOutputPorts() {
		return outputPorts;
	}

	public void setOutputPorts(List<Short> outputPorts) {
		this.outputPorts = outputPorts;
	}

	public long getInputSwitchDatapathId() {
		return inputSwitchDatapathId;
	}

	public void setInputSwitchDatapathId(long inputSwitchDatapathId) {
		this.inputSwitchDatapathId = inputSwitchDatapathId;
	}

	public long getOutputSwitchDatapathId() {
		return outputSwitchDatapathId;
	}

	public void setOutputSwitchDatapathId(long outputSwitchDatapathId) {
		this.outputSwitchDatapathId = outputSwitchDatapathId;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String[] getValues() {
		return values;
	}

	public void setValues(String[] values) {
		this.values = values;
	}

}
