package org.eclipse.equinox.console.telnet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.osgi.framework.console.CommandInterpreter;

import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFPhysicalPort;
import org.openflow.protocol.OFPortMod;
import org.openflow.protocol.OFType;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionOutput;
import org.openflow.protocol.action.OFActionType;
import org.openflow.util.HexString;

import edu.iu.incntre.flowscale.FlowscaleController;
import edu.iu.incntre.flowscale.SwitchDevice;

import grnoc.net.util.ipaddress.IPv4Address;
import net.beaconcontroller.core.IBeaconProvider;
import net.beaconcontroller.core.IOFSwitch;

public class StaticPusher {

	public static String insertFlow(HashMap<String, String> cliArguments,
			IBeaconProvider ibeaconProvider) {

		OFMatch ofMatch = new OFMatch();
		int wildCard = OFMatch.OFPFW_ALL;

		for (String argKey : cliArguments.keySet()) {

			if (argKey.equals("in_port")) {

				ofMatch.setInputPort(Short.parseShort(cliArguments.get(argKey)));
				wildCard = wildCard ^ OFMatch.OFPFW_IN_PORT;

			} else if (argKey.equals("dl_src")) {
				ofMatch.setDataLayerSource(cliArguments.get(argKey));
				wildCard = wildCard ^ OFMatch.OFPFW_DL_SRC;
			} else if (argKey.equals("dl_dst")) {
				ofMatch.setDataLayerDestination(cliArguments.get(argKey));
				wildCard = wildCard ^ OFMatch.OFPFW_DL_DST;
			} else if (argKey.equals("dl_vlan")) {
				ofMatch.setDataLayerVirtualLan(Short.parseShort(cliArguments
						.get(argKey)));
				wildCard = wildCard ^ OFMatch.OFPFW_DL_VLAN;
			} else if (argKey.equals("dl_vlan_pcp")) {
				ofMatch.setDataLayerVirtualLanPriorityCodePoint(Byte
						.parseByte(cliArguments.get(argKey)));
				wildCard = wildCard ^ OFMatch.OFPFW_DL_VLAN_PCP;
			} else if (argKey.equals("dl_type")) {
				ofMatch.setDataLayerType(Short.parseShort(cliArguments
						.get(argKey)));
				wildCard = wildCard ^ OFMatch.OFPFW_DL_TYPE;
			} else if (argKey.equals("nw_tos")) {
				ofMatch.setNetworkTypeOfService(Byte.parseByte(cliArguments
						.get(argKey)));
				wildCard = wildCard ^ OFMatch.OFPFW_NW_TOS;
			} else if (argKey.equals("nw_proto")) {
				ofMatch.setNetworkProtocol(Byte.parseByte(cliArguments
						.get(argKey)));
				wildCard = wildCard ^ OFMatch.OFPFW_NW_PROTO;
				
			} else if (argKey.equals("nw_src")) {
				IPv4Address ipv4Address = new IPv4Address(
						cliArguments.get(argKey));
				ofMatch.setNetworkSource(ipv4Address.getIPv4AddressInt());
				wildCard = wildCard ^ OFMatch.OFPFW_NW_SRC_ALL ^
				((31 << OFMatch.OFPFW_NW_SRC_SHIFT));
				
			} else if (argKey.equals("nw_dst")) {
				IPv4Address ipv4Address = new IPv4Address(
						cliArguments.get(argKey));
				ofMatch.setNetworkDestination(ipv4Address.getIPv4AddressInt());
				wildCard = wildCard ^ OFMatch.OFPFW_NW_DST_ALL ^
				((31 << OFMatch.OFPFW_NW_DST_SHIFT));
			} else if (argKey.equals("tp_src")) {
				ofMatch.setTransportSource(Short.parseShort(cliArguments
						.get(argKey)));
				wildCard = wildCard ^ OFMatch.OFPFW_TP_SRC;

			} else if (argKey.equals("tp_dst")) {
				ofMatch.setTransportDestination(Short.parseShort(cliArguments
						.get(argKey)));
				wildCard = wildCard ^ OFMatch.OFPFW_TP_DST;

			}
		}
			ofMatch.setWildcards(wildCard);
			OFFlowMod oFFlowMod = new OFFlowMod();
			oFFlowMod.setType(OFType.FLOW_MOD);
			oFFlowMod.setMatch(ofMatch);
			oFFlowMod.setCommand(OFFlowMod.OFPFC_ADD);
			oFFlowMod.setBufferId(-1);
			oFFlowMod
					.setPriority(Short.parseShort(cliArguments.get("priority")));
			oFFlowMod.setIdleTimeout((Short.parseShort( cliArguments.get("idle_timeout"))));
			oFFlowMod.setHardTimeout((Short.parseShort(cliArguments.get("hard_timeout"))));

			
			
			ArrayList<OFAction> flowActions = new ArrayList<OFAction>();
			
			if(cliArguments.get("actions") != null){
			Short  actions = Short.parseShort(cliArguments.get("actions"));
			OFActionOutput actionOutput = new OFActionOutput();
			actionOutput.setPort(actions);
			
			
			flowActions.add(actionOutput);
			oFFlowMod.setActions(flowActions);
			}
			
			IOFSwitch sw = ibeaconProvider.getSwitches().get(
					HexString.toLong(cliArguments.get("switch")));
			try {
				System.out.println(oFFlowMod);
				sw.getOutputStream().write(oFFlowMod);
				sw.getOutputStream().flush();
				return "flow added";
				
			} catch (IOException ioe) {
				ioe.printStackTrace();
				return "An error occured with the insertion , check logs";
			}

		

	}
	
	public static void downPort(String datapathIdString, 
			String portString, FlowscaleController flowscaleController, CommandInterpreter ci){
		
		
		long datapathId = HexString.toLong(datapathIdString);
		short port = Short.parseShort(portString);
		

		try {

			SwitchDevice switchDevice = flowscaleController
					.getSwitchDevices().get(
							HexString.toLong(datapathIdString));

			IOFSwitch sw = switchDevice.getOpenFlowSwitch();
			List<OFPhysicalPort> switchPorts = switchDevice.getPortStates();
			OFPhysicalPort updatedPort = null;

			for (OFPhysicalPort ofPort : switchPorts) {

				if (ofPort.getPortNumber() == port) {
					updatedPort = ofPort;
					break;

				}

			}

			updatedPort.setConfig(0);

			OFPortMod portMod = new OFPortMod();

			
				portMod.setConfig(OFPhysicalPort.OFPortConfig.OFPPC_PORT_DOWN
						.getValue());
		

			portMod.setMask(1);
			portMod.setPortNumber(updatedPort.getPortNumber());
			portMod.setHardwareAddress(updatedPort.getHardwareAddress());
			portMod.setType(OFType.PORT_MOD);

			sw.getOutputStream().write(portMod);
			sw.getOutputStream().flush();
			ci.print("done");

		} catch (Exception e) {
			ci.print(e.toString());
	
		}

		
		
		
		
		
		
		
		
		
		
		
	}

}
