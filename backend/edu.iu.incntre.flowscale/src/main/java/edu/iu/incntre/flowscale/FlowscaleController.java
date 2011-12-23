package edu.iu.incntre.flowscale;

import org.openflow.protocol.OFFeaturesReply;
import org.openflow.protocol.OFFeaturesRequest;
import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFMessage;

import org.openflow.protocol.OFPhysicalPort;

import org.openflow.protocol.OFPortStatus;
import org.openflow.protocol.OFPortStatus.OFPortReason;
import org.openflow.protocol.OFType;

import org.openflow.protocol.action.OFAction;

import org.openflow.util.HexString;
import org.openflow.util.U16;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.beaconcontroller.core.IBeaconProvider;
import net.beaconcontroller.core.IOFMessageListener;
import net.beaconcontroller.core.IOFSwitch;
import net.beaconcontroller.core.IOFSwitchListener;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.HashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


/**
 * @author Ali Khalfan (akhalfan@indiana.edu)
 */

public class FlowscaleController implements IOFSwitchListener,
        IOFMessageListener {

    protected IBeaconProvider ibeaconProvider;

    private HashMap<Long, SwitchDevice> controllerSwitches = new HashMap<Long, SwitchDevice>();
    private HashMap<Integer, Group> groupList = new HashMap<Integer, Group>();

    private String username, password, host, port;

    private ArrayList<XConnect> xConnectList = new ArrayList<XConnect>();

    protected static Logger logger = LoggerFactory
            .getLogger(FlowscaleController.class);

    // implementation of the IOFMessage Listener

    @Override
    public Command receive(IOFSwitch sw, OFMessage msg) {
        // TODO Auto-generated method stub

        if (msg.getType() == OFType.PACKET_IN) {

            return Command.CONTINUE;

        }

        if (msg.getType() == OFType.PORT_STATUS) {

            logger.info("you got a port status message");

            OFPortStatus ps = (OFPortStatus) msg;

            logger.info("port {}, with h/w address {} is updated", ps.getDesc()
                    .getPortNumber(), ps.getDesc().getHardwareAddress());

            if (OFPortReason.values()[ps.getReason()] == OFPortReason.OFPPR_MODIFY) {

                updateGroupsWithPortStatus(sw, ps.getDesc().getPortNumber(),
                        ps.getDesc());

            }

            // update switch as well

            SwitchDevice switchDevice = controllerSwitches.get(sw.getId());

            if (switchDevice != null)
                switchDevice.updatePort(ps);

        }

        return null;
    }

    private void updateGroupsWithPortStatus(IOFSwitch sw, short portNum,
            OFPhysicalPort physicalPort) {

        for (Integer groupId : groupList.keySet()) {
            Group group = groupList.get(groupId);

            if ((sw.getId() == group.getInputSwitchDatapathId() || sw.getId() == group
                    .getOutputSwitchDatapathId())
                    && (group.getInputPorts().contains(portNum) || group
                            .getOutputPorts().contains(portNum))) {

                group.alert(sw, portNum, physicalPort, null);

            }
        }

    }

    // implementation of the IOFSwitchLisnter

    @Override
    public void addedSwitch(IOFSwitch sw) {
        // TODO Auto-generated method stub
        SwitchDevice switchDevice = controllerSwitches.get(sw.getId());
        if (switchDevice == null) {

            switchDevice = new SwitchDevice();
            switchDevice.setDatapathId(sw.getId());
          
        }

        try {

            OFFlowMod ofDeleteAll = new OFFlowMod();
            OFMatch ofMatchAll = new OFMatch();
            ofMatchAll.setWildcards(OFMatch.OFPFW_ALL);
            ofDeleteAll.setMatch(ofMatchAll);
            ofDeleteAll.setCommand(OFFlowMod.OFPFC_DELETE);

            OFFlowMod ofDefaultDropRule = new OFFlowMod();

            ofDefaultDropRule.setPriority((short) 5);
            ofDefaultDropRule.setMatch(ofMatchAll);
            ofDefaultDropRule.setIdleTimeout((short) 0);
            ofDefaultDropRule.setHardTimeout((short) 0);
            ArrayList<OFAction> emptyActions = new ArrayList<OFAction>();

            ofDefaultDropRule.setActions(emptyActions);
         

            sw.getOutputStream().write(ofDeleteAll);

            ofDefaultDropRule.setBufferId(-1);
            ofDefaultDropRule.setLength(U16.t(OFFlowMod.MINIMUM_LENGTH));
            sw.getOutputStream().write(ofDefaultDropRule);

            sw.getOutputStream().flush();

        } catch (Exception e) {

            FlowscaleController.logger.error("{}", e);
        }

        switchDevice.setOpenFlowSwitch(sw);
        logger.debug("ports on initiation {}" ,sw.getFeaturesReply().getPorts());
        
        switchDevice.setPhysicalPorts(sw.getFeaturesReply().getPorts());
        
        controllerSwitches.put(sw.getId(), switchDevice);

        for (Integer groupId : groupList.keySet()) {
            Group group = groupList.get(groupId);

            if (sw.getId() == group.getOutputSwitchDatapathId()) {

                group.switchUpAlert(sw);

            }

        }

        logger.info("switch {} added", sw.getId());

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
        logger.info("switch {} removed", sw.getId());
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

    public void setBeaconProvider(IBeaconProvider beaconProvider) {
        this.ibeaconProvider = beaconProvider;
    }

    public String getSwitchPorts(long datapathId) {
        JSONArray js = new JSONArray();

        logger.debug("all switches {}", controllerSwitches.toString());
        logger.debug("this datapath id {}", datapathId);
        SwitchDevice switchDevice = this.controllerSwitches.get(datapathId);

        logger.debug("ibeacon provider switches {}", ibeaconProvider.getSwitches());
                logger.debug("ports now {}", switchDevice.getOpenFlowSwitch().getFeaturesReply().getPorts());
                logger.debug("sw port states ", switchDevice.getPortStates());
                            
        if (switchDevice.getPortStates() == null) {
            logger.debug("switch is null");
            return null;
        }

        List<OFPhysicalPort> ss = switchDevice.getPortStates();

        for (OFPhysicalPort pp : ss) {

            JSONObject obj = new JSONObject();
            if (pp.getPortNumber() == -2) {
                continue;
            }
            obj.put("port_id", pp.getPortNumber());
            obj.put("port_address",
                    HexString.toHexString(pp.getHardwareAddress()));
            obj.put("config", pp.getConfig());
            obj.put("supported", pp.getSupportedFeatures());
            obj.put("current", pp.getCurrentFeatures());

            obj.put("state", pp.getState());

            logger.debug("port {}", pp.getPortNumber());
            logger.debug("h/w {}",
                    HexString.toHexString(pp.getHardwareAddress()));

            logger.debug("state {}", pp.getState());

            logger.debug("-------");

            js.add(obj);

        }

       // return js;
        
        return  js.toJSONString();
        
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setHost(String host) {
        this.host = host;

    }

    public void setPort(String port) {

        this.port = port;
    }

    public void startUp() {

        try {
            DatabaseUtility db = new DatabaseUtility();
            db.setConnection(username, password, host, port);

            logger.info("initiating controller");
            ibeaconProvider.addOFMessageListener(OFType.PACKET_IN, this);
            ibeaconProvider.addOFMessageListener(OFType.FEATURES_REPLY, this);
            ibeaconProvider.addOFMessageListener(OFType.ECHO_REQUEST, this);
            ibeaconProvider.addOFMessageListener(OFType.ERROR, this);
            ibeaconProvider.addOFMessageListener(OFType.PORT_MOD, this);
            ibeaconProvider.addOFMessageListener(OFType.PORT_STATUS, this);
            logger.info("adding switch listener");
            ibeaconProvider.addOFSwitchListener(this);

            this.controllerSwitches = db.populateSwitchesFromDatabase(this);
            this.groupList = db.populateGroupsFromDatabase(this);

            logger.debug("groupList has {}", groupList);

            for (Integer groupId : groupList.keySet()) {
                Group group = groupList.get(groupId);

            }

        } catch (Exception e) {
            logger.error("{}", e);
        }

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
            
            switchDevice.setPhysicalPorts(ofSwitch.getFeaturesReply().getPorts());

        }

        controllerSwitches.put(datapathId, switchDevice);

    }

    public void removeSwitchFromInterface(String datapathIdString) {

        long datapathId = HexString.toLong(datapathIdString);
        controllerSwitches.remove(datapathId);

    }
    
    
    public String addGroupFromInterface(String groupIdString,String groupName, String inputSwitchDatapathIdString, String outputSwitchDatapathIdString,
                String inputPortListString, String outputPortListString, String typeString,
           String priorityString, String valuesString, String maximumFlowsAllowedString){
        
        Group g = new Group(this);
        g.addGroupDetails(groupIdString, groupName,
                inputSwitchDatapathIdString, outputSwitchDatapathIdString,
                inputPortListString, outputPortListString, typeString,
                priorityString, valuesString, maximumFlowsAllowedString);

        g.pushRules();

        groupList.put(Integer.parseInt(groupIdString), g);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("result", "group added");
        
        
        return jsonObject.toJSONString();
       
        
    }
    
    public String editGroupFromInterface(String groupIdString, String editTypeString, String updateValueString){
        
        Group g = groupList.get(Integer.parseInt(groupIdString));
        g.editGroup(editTypeString, updateValueString);
        
        return null;
    }
    
    public String deleteGroupFromInterface(String groupIdString){
        
        logger.debug(groupList.toString());
        
        Group g = groupList.get(Integer.parseInt(groupIdString));

        g.removeGroup();
        groupList.remove(Integer.parseInt(groupIdString));
        
        return null;
        
    }
    
    
    public String getSwitchStatisticsFromInterface(String datapathIdString, String typeString){
        

        long datapathId = HexString.toLong(datapathIdString);

        SwitchDevice switchDevice = controllerSwitches.get(datapathId);
        JSONArray jsonArray = switchDevice.getStatistics(typeString);
        
        return jsonArray.toJSONString();

    }
   

}
