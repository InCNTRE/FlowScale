package edu.iu.incntre.flowscale;

import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFMessage;

import org.openflow.protocol.OFPhysicalPort;

import org.openflow.protocol.OFPortStatus;
import org.openflow.protocol.OFPortStatus.OFPortReason;
import org.openflow.protocol.OFType;

import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionOutput;
import org.openflow.protocol.statistics.OFStatistics;

import org.openflow.util.HexString;
import org.openflow.util.U16;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.beaconcontroller.core.IBeaconProvider;
import net.beaconcontroller.core.IOFMessageListener;
import net.beaconcontroller.core.IOFSwitch;
import net.beaconcontroller.core.IOFSwitchListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import edu.iu.incntre.flowscale.exception.NoDatabaseException;
import edu.iu.incntre.flowscale.exception.NoSwitchException;

/**
 * @author Ali Khalfan (akhalfan@indiana.edu)
 */

public class FlowscaleController implements IOFSwitchListener,
		IOFMessageListener {

	protected IBeaconProvider ibeaconProvider;

	private HashMap<Long, SwitchDevice> controllerSwitches = new HashMap<Long, SwitchDevice>();
	private HashMap<Integer, Group> groupList = new HashMap<Integer, Group>();

	private String username, password, connectionString, dbDriverString;

	private ArrayList<XConnect> xConnectList = new ArrayList<XConnect>();
	private ArrayList<SwitchDevice> connectedSwitches = new ArrayList<SwitchDevice>();
	private String mirroringRules;
	private int defaultRulePriority;
	private short mirrorPriority;
	private String flowMirrorPorts;
	private HashMap<Long,Integer> maximumFlowsToPushHashMap = new HashMap<Long,Integer>();
	HashMap<Long, HashMap<Short,Short>> switchFlowMirrorPortsHashMap ;

	private int maximumFlowsToPush;
	
	

	public static Logger logger = LoggerFactory
			.getLogger(FlowscaleController.class);

	// implementation of the IOFMessage Listener

	@Override
	public Command receive(IOFSwitch sw, OFMessage msg) {
		// TODO Auto-generated method stub

		if (msg.getType() == OFType.PACKET_IN) {

			return Command.CONTINUE;

		}

		if (msg.getType() == OFType.PORT_STATUS) {

			logger.info(
					"controller received a port status message from switch {}",
					HexString.toHexString(sw.getId()));

			OFPortStatus ps = (OFPortStatus) msg;

			logger.info("port {}, with h/w address {} sent a port update message", ps.getDesc()
					.getPortNumber(), HexString.toHexString(ps.getDesc()
					.getHardwareAddress()));

			logger.info("Status of port is {}", (ps.getDesc().getState() %2 ==0 ) ? "up" : "down");
			
			if (OFPortReason.values()[ps.getReason()] == OFPortReason.OFPPR_MODIFY) {

				updateGroupsWithPortStatus(sw, ps.getDesc().getPortNumber(),
						ps.getDesc());

			}

			// update switch as well

			SwitchDevice switchDevice = controllerSwitches.get(sw.getId());

			if (switchDevice != null){
				switchDevice.updatePort(ps);

			}
		}

		return null;
	}

	private void updateGroupsWithPortStatus(IOFSwitch sw, short portNum,
			OFPhysicalPort physicalPort) {
		logger.trace("updating groups with port number {} on switch {} ",
				portNum, HexString.toHexString(sw.getId()));
		for (Integer groupId : groupList.keySet()) {
			Group group = groupList.get(groupId);

			if ((sw.getId() == group.getInputSwitchDatapathId() || sw.getId() == group
					.getOutputSwitchDatapathId())
					&& (group.getInputPorts().contains(portNum) || group
							.getOutputPorts().contains(portNum))) {
				logger.trace(
						"group with value {} contains this port ,updating ...",
						group.getValues());
				group.alert(sw, portNum, physicalPort, null);

			}
		}

	}

	// implementation of the IOFSwitchLisnter

	@Override
	public void addedSwitch(IOFSwitch sw) {
		
		logger.info("in added switch method");
		
		// TODO Auto-generated method stub
		try {
			
		SwitchDevice switchDevice = controllerSwitches.get(sw.getId());
		if (switchDevice == null) {
			logger.info("switch device is not in list exiting...");
			return;
			// switchDevice = new SwitchDevice();
			// switchDevice.setDatapathId(sw.getId());

		}

		if (connectedSwitches.contains(switchDevice)) {
			logger.info("switch is already connected exiting ");
			return;
		}

		// initiate switch

		initiateSwitch(sw);

		switchDevice.setOpenFlowSwitch(sw);
		logger.debug("ports on initiation {}", sw.getFeaturesReply().getPorts());

		switchDevice.setPhysicalPorts(sw.getFeaturesReply().getPorts());

		controllerSwitches.put(sw.getId(), switchDevice);

		for (Integer groupId : groupList.keySet()) {
			Group group = groupList.get(groupId);

			if (sw.getId() == group.getOutputSwitchDatapathId()) {

			
				group.switchUpAlert(sw);

			}

		}

		logger.info("switch {} added", HexString.toHexString(sw.getId()));
		connectedSwitches.add(switchDevice);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.info("adding group exception {}",e);
		}
		
		
	}

	public void initiateSwitch(IOFSwitch sw) {

		
		
		try {

			// 1) delete all flows

			OFFlowMod ofDeleteAll = new OFFlowMod();
			OFMatch ofMatchAll = new OFMatch();
			ofMatchAll.setWildcards(OFMatch.OFPFW_ALL);
			ofDeleteAll.setMatch(ofMatchAll);
			ofDeleteAll.setCommand(OFFlowMod.OFPFC_DELETE);

			// 2) inset default drop rule to avoid sending any packet to the
			// controller
			OFFlowMod ofDefaultDropRule = new OFFlowMod();

			ofDefaultDropRule.setPriority((short) defaultRulePriority);
			ofDefaultDropRule.setMatch(ofMatchAll);
			ofDefaultDropRule.setIdleTimeout((short) 0);
			ofDefaultDropRule.setHardTimeout((short) 0);
			ArrayList<OFAction> emptyActions = new ArrayList<OFAction>();

			ofDefaultDropRule.setActions(emptyActions);
			ArrayList<OFFlowMod> mirroringOFFlowMods = new ArrayList<OFFlowMod>();
			// 3) insert mirroring rules for UISO flowscale
			if (mirroringRules == null){
				logger.info("no mirroring rules configured");
			}else{
			String[] mirrorValues = mirroringRules.split(";");

			// loop over comma separated value

			

			OFFlowMod mirrorFlowMod;
			OFMatch mirrorMatch;
			ArrayList<OFAction> mirrorOutput;
			OFActionOutput mirrorAction;
			for (String mirrorValue : mirrorValues) {

				String[] mirrorIndex = mirrorValue.split("-");

				short inputPort = Short.parseShort(mirrorIndex[0]);

				String[] mirrorPortValues = mirrorIndex[1].split(",");

				// add the flows

				mirrorFlowMod = new OFFlowMod();
				mirrorMatch = new OFMatch();
				mirrorMatch.setWildcards(OFMatch.OFPFW_ALL
						^ OFMatch.OFPFW_IN_PORT);
				mirrorMatch.setInputPort(inputPort);
				mirrorFlowMod.setMatch(mirrorMatch);
				
				mirrorFlowMod.setIdleTimeout((short) 0);
				mirrorFlowMod.setHardTimeout((short) 0);
				mirrorFlowMod.setPriority(mirrorPriority);
				
				mirrorOutput = new ArrayList<OFAction>();

				for (String mirrorPortValue : mirrorPortValues) {

					mirrorAction = new OFActionOutput();
					mirrorAction.setPort(Short.parseShort(mirrorPortValue));

					mirrorOutput.add(mirrorAction);

				}
				
				mirrorFlowMod.setActions(mirrorOutput);
				mirrorFlowMod.setBufferId(-1);

				mirroringOFFlowMods.add(mirrorFlowMod);

			}
			}

			// finally, insert above flows to the switch
			sw.getOutputStream().write(ofDeleteAll);
			logger.debug("deleting all flows...");
			ofDefaultDropRule.setBufferId(-1);
			ofDefaultDropRule.setLength(U16.t(OFFlowMod.MINIMUM_LENGTH));
			logger.debug("adding default rule {}", ofDefaultDropRule.toString());
			sw.getOutputStream().write(ofDefaultDropRule);
			
			
			for(OFFlowMod mirrorFlowModValue : mirroringOFFlowMods){
				
				logger.debug("adding mirror rules {}", mirrorFlowModValue.toString());
				sw.getOutputStream().write(mirrorFlowModValue);
				
				
			}
			

			sw.getOutputStream().flush();

		} catch (Exception e) {

			FlowscaleController.logger.error("{}", e);
		}

	}

	@Override
	public void removedSwitch(IOFSwitch sw) {
		// TODO Auto-generated method stub
		for (Integer groupId : groupList.keySet()) {
			Group group = groupList.get(groupId);

			if (sw.getId() == group.getOutputSwitchDatapathId()) {

				group.switchDownAlert(sw);

			}

		}
		connectedSwitches.remove(controllerSwitches.get(sw.getId()));

		try {
			sw.getSocketChannel().close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("{}", e);
		}
		logger.info("switch {} removed", HexString.toHexString(sw.getId()));
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "flowscaleController";
	}

	// controller listeners

	public IBeaconProvider getIBeaconProvider() {

		return this.ibeaconProvider;

	}

	public void setMaximumFlowsToPush(int maximumFlowsToPush){
		
		if (maximumFlowsToPush == 0){
			
			this.maximumFlowsToPush = Integer.MAX_VALUE;
			
		}else{
			this.maximumFlowsToPush = maximumFlowsToPush;
		}
		
		
	}
	
	public int getMaximumFlowsToPush(){
		return this.maximumFlowsToPush;
	}
	
	public void setBeaconProvider(IBeaconProvider beaconProvider) {
		this.ibeaconProvider = beaconProvider;
	}

	public void setDefaultRulePriority(short defaultRulePriority) {

		this.defaultRulePriority = defaultRulePriority;

	}

	public void setMirrorPriority(short mirrorPriority) {

		this.mirrorPriority = mirrorPriority;
	}

	public void setFlowMirrorPorts(String flowMirrorPorts){
		this.flowMirrorPorts = flowMirrorPorts;
		
		
		 switchFlowMirrorPortsHashMap = new HashMap<Long, HashMap<Short,Short>>();
		
		String [] switchMirrorConfig =  flowMirrorPorts.split("-");
		
		for(String switchMirrorConfigValue : switchMirrorConfig){
			
			String [] switchSplitter =  switchMirrorConfigValue.split(":");
			
			
			long switchDatapathId = HexString.toLong(switchSplitter[0]);
			
			HashMap<Short,Short> mirrors = new HashMap<Short,Short>();
			
			String[] mirrorPorts = switchSplitter[1].split(";");
			
			for (String mirrorPortsValue : mirrorPorts){
				
				String [] flowMirrors = mirrorPortsValue.split(",");
				mirrors.put(Short.parseShort(flowMirrors[0]), Short.parseShort(flowMirrors[1]));
				
			}
			
			switchFlowMirrorPortsHashMap.put(switchDatapathId, mirrors);
		}
		
		
				
	}
	

	
	public void setMirroringRules(String mirroringRules) {
		this.mirroringRules = mirroringRules;
	}

	
	public HashMap<Long, HashMap<Short,Short>> getSwitchFlowMirrorPortsHashMap(){
		
		return this.switchFlowMirrorPortsHashMap;
		
	}
	


	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setConnectionString(String connectionString) {
		this.connectionString = connectionString;

	}

	public void setDbDriverString(String dbDriverString) {

		this.dbDriverString = dbDriverString;
	}

	public void startUp() {

		logger.info("starting controller ");
			DatabaseUtility db = new DatabaseUtility();
			db.setConnection(username, password, connectionString,
					dbDriverString);

			logger.info("initiating controller");
			//adding listeners 
			ibeaconProvider.addOFMessageListener(OFType.PACKET_IN, this);
			ibeaconProvider.addOFMessageListener(OFType.FEATURES_REPLY, this);
			ibeaconProvider.addOFMessageListener(OFType.ECHO_REQUEST, this);
			ibeaconProvider.addOFMessageListener(OFType.ERROR, this);
			ibeaconProvider.addOFMessageListener(OFType.PORT_MOD, this);
			ibeaconProvider.addOFMessageListener(OFType.PORT_STATUS, this);
			logger.info("adding switch listener");
			ibeaconProvider.addOFSwitchListener(this);

			try {
				this.controllerSwitches = db.populateSwitchesFromDatabase(this);
				this.groupList = db.populateGroupsFromDatabase(this);
				logger.debug("controller switches are {}", controllerSwitches);
			} catch (NoDatabaseException e) {
				
				// TODO Auto-generated catch block
				logger.error("No Database loaded, please update your configuration and rerun the FlowScale");
				ibeaconProvider.notify();
			}
			

			logger.debug("groupList has {}", groupList);

			for (Integer groupId : groupList.keySet()) {
				Group group = groupList.get(groupId);

			}
			
			
			logger.info("controller instance is {}", this.toString());
	

	}

	public void shutDown() {
		logger.info("controller is shutting down");
		ibeaconProvider.removeOFMessageListener(OFType.PACKET_IN, this);
		ibeaconProvider.removeOFMessageListener(OFType.ECHO_REQUEST, this);
		ibeaconProvider.removeOFMessageListener(OFType.ERROR, this);

		ibeaconProvider.removeOFSwitchListener(this);

		try {

		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("{}", e

			);
		}

	}

	public HashMap<Long, SwitchDevice> getSwitchDevices() {

		return this.controllerSwitches;

	}

	public void addSwitchFromInterface(String datapathIdString) {

		long datapathId = HexString.toLong(datapathIdString);

		SwitchDevice switchDevice = new SwitchDevice(datapathId);

		IOFSwitch ofSwitch = ibeaconProvider.getSwitches().get(datapathId);

		if (ofSwitch != null) {

			switchDevice.setOpenFlowSwitch(ofSwitch);

			switchDevice.setPhysicalPorts(ofSwitch.getFeaturesReply()
					.getPorts());

		}

		controllerSwitches.put(datapathId, switchDevice);

	}

	public void removeSwitchFromInterface(String datapathIdString) {

		long datapathId = HexString.toLong(datapathIdString);
		controllerSwitches.remove(datapathId);

	}

	public String addGroupFromInterface(String groupIdString, String groupName,
			String inputSwitchDatapathIdString,
			String outputSwitchDatapathIdString, String inputPortListString,
			String outputPortListString, String typeString,
			String priorityString, String valuesString,
			String maximumFlowsAllowedString, String networkProtocolString,
			String transportDirectionString) {

		Group g = new Group(this);
		g.addGroupDetails(groupIdString, groupName,
				inputSwitchDatapathIdString, outputSwitchDatapathIdString,
				inputPortListString, outputPortListString, typeString,
				priorityString, valuesString, maximumFlowsAllowedString,
				networkProtocolString, transportDirectionString);

		g.pushRules();

		groupList.put(Integer.parseInt(groupIdString), g);
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("result", "group added");

		return jsonObject.toJSONString();

	}

	public String editGroupFromInterface(String groupIdString,
			String editTypeString, String updateValueString) {

		Group g = groupList.get(Integer.parseInt(groupIdString));
		g.editGroup(editTypeString, updateValueString);

		return null;
	}

	public String deleteGroupFromInterface(String groupIdString) {

		logger.debug(groupList.toString());

		Group g = groupList.get(Integer.parseInt(groupIdString));

		g.removeGroup();
		groupList.remove(Integer.parseInt(groupIdString));

		return null;

	}

	public List <OFStatistics> getSwitchStatisticsFromInterface(String datapathIdString,
			String typeString) throws NoSwitchException, IOException, InterruptedException, ExecutionException, TimeoutException {

		long datapathId = HexString.toLong(datapathIdString);

		SwitchDevice switchDevice = controllerSwitches.get(datapathId);
		if (switchDevice == null){
			throw new NoSwitchException(datapathIdString);
		}
		List <OFStatistics> ofst = switchDevice.getStatistics(typeString);

		return  ofst;

	}

	
	
	public void injectFlows(ArrayList<OFFlowMod> ofFlowMods, long datapathId){
		logger.info("injecting flows in controller");
		IOFSwitch sw = this.ibeaconProvider.getSwitches().get(datapathId);
		if (sw == null){
			logger.error("no switch {} exists", datapathId);
			return;
		}
		for(OFFlowMod ofFlowMod : ofFlowMods){
			
			
			logger.info("injecting flow {}", ofFlowMod);
			try {
				sw.getOutputStream().write(ofFlowMod);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				logger.error("{}",e);
			}
			
			
		
			
		}
		
		try {
			sw.getOutputStream().flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("{}",e);
		}
		
		
	}
	
	
	
}
