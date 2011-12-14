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

import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.Handler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * @author Ali Khalfan (akhalfan@indiana.edu)
 */

public class FlowscaleController implements IOFSwitchListener,
		IOFMessageListener, Handler {

	protected IBeaconProvider ibeaconProvider;
	protected int jettyListenerPort;
	protected String capstatsFilePath;
	private HashMap<Long, SwitchDevice> controllerSwitches = new HashMap<Long, SwitchDevice>();
	private HashMap<Integer, Group> groupList = new HashMap<Integer, Group>();

	private String username, password, host, port;

	private ArrayList<XConnect> xConnectList = new ArrayList<XConnect>();
	Server server;

	protected static Logger logger = LoggerFactory
			.getLogger(FlowscaleController.class);

	@Override
	public void handle(String arg0, HttpServletRequest request,
			HttpServletResponse response, int arg3) throws IOException,
			ServletException {
		// TODO Auto-generated method stub

		logger.debug("http request received");
		logger.debug("request header type is {}", request.getMethod());

		Request base_request = (request instanceof Request) ? (Request) request
				: HttpConnection.getCurrentConnection().getRequest();
		base_request.setHandled(true);
		response.setContentType("text/html;charset=utf-8");

		String output = "";

		if (request.getMethod() != "GET") {

			response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);

			JSONObject obj = new JSONObject();
			obj.put("success", 0);
			obj.put("error", "method not allowed");
			response.getWriter().print(obj);

			return;
		}

		String requestAction = request.getHeader("action");

		logger.debug("action is {}", requestAction);
		if (requestAction.equals("getSwitchStatus")) {

			String switchId = request.getHeader("datapathId");

			// getSwitchStatus(switchId);

		} else if (requestAction.equals("getSwitchPorts")) {

			long datapathId = HexString.toLong(request.getHeader("datapathId"));

			JSONArray objArray = new JSONArray();
			objArray = this.getSwitchPorts(datapathId);

			output = objArray.toJSONString();

		} else if (requestAction.equals("addXonnect")) {

			XConnect xConnect = new XConnect();
			xConnect.setInputSwitch(HexString.toLong(request
					.getHeader("inputSwitch")));
			xConnect.setOutputSwitch(HexString.toLong(request
					.getHeader("outputSwitch")));
			xConnect.setInputPortNum(Integer.parseInt(request
					.getHeader("inputPort")));
			xConnect.setOutputPortNum(Integer.parseInt(request
					.getHeader("outputPort")));
			xConnectList.add(xConnect);

		} else if (requestAction.equals("removeXconnect")) {

			XConnect xConnect = new XConnect();
			xConnect.setInputSwitch(HexString.toLong(request
					.getHeader("inputSwitch")));
			xConnect.setOutputSwitch(HexString.toLong(request
					.getHeader("outputSwitch")));
			xConnect.setInputPortNum(Integer.parseInt(request
					.getHeader("inputPort")));
			xConnect.setOutputPortNum(Integer.parseInt(request
					.getHeader("outputPort")));

			xConnectList.remove(xConnect);

		}else if (requestAction.equals("getCapstats")){
			
			FlowscaleController.logger.debug("action is getCapstats");
			
	
			
			try{
				  // Open the file that is the first 
				  // command line parameter
				  FileInputStream fstream = new FileInputStream(capstatsFilePath);
				  // Get the object of DataInputStream
				  DataInputStream in = new DataInputStream(fstream);
				  BufferedReader br = new BufferedReader(new InputStreamReader(in));
				  String strLine;
				  //Read File Line By Line
				  JSONArray jsonArray = new JSONArray();
				  for(int i=0 ; i < 10; i++)   {
				  // Print the content on the console
					  strLine = br.readLine();
					  
					  String [] values = strLine.split("\\s+");
					  logger.debug("{}",values[0]);
					  JSONObject jsonObject = new JSONObject();
						jsonObject.put("sensor", values[0]);
						jsonObject.put("kpps", values[1]);
						jsonObject.put("mbps",values[2]);
						jsonArray.add(jsonObject);
						
					  
				//  System.out.println (strLine);
				  }
				  output = jsonArray.toJSONString();
				  
				  //Close the input stream
				  in.close();
				    }catch (Exception e){//Catch exception if any
				  FlowscaleController.logger.error("Error {}: " + e);
				  }
				  
		}	
		
		
			else if (requestAction.equals("addSwitch")) {
		

			FlowscaleController.logger.debug(("action is to add Switch"));
			long datapathId = HexString.toLong(request.getHeader("datapathId"));

			SwitchDevice switchDevice = new SwitchDevice(datapathId);

			IOFSwitch ofSwitch = ibeaconProvider.getSwitches().get(datapathId);

			if (ofSwitch != null) {

				switchDevice.setOpenFlowSwitch(ofSwitch);

			}

			controllerSwitches.put(datapathId, switchDevice);

		} else if (requestAction.equals("removeSwitch")) {
			long datapathId = HexString.toLong(request.getHeader("datapathId"));
			controllerSwitches.remove(datapathId);

		} else if (requestAction.equals("addGroup")) {
			Group g = new Group(this);

			// get all values for the grouop

			String groupIdString = request.getHeader("groupId");
			String groupName = request.getHeader("groupName");
			String inputSwitchDatapathIdString = request
					.getHeader("inputSwitch");
			String outputSwitchDatapathIdString = request
					.getHeader("outputSwitch");
			String inputPortListString = request.getHeader("inputPorts");
			String outputPortListString = request.getHeader("outputPorts");
			String typeString = request.getHeader("type");
			String priorityString = request.getHeader("priority");
			String valuesString = request.getHeader("values");
			String maximumFlowsAllowedString = request
					.getHeader("maximumFlowsAllowed");

			// end get all values for the gorup

			g.addGroupDetails(groupIdString, groupName,
					inputSwitchDatapathIdString, outputSwitchDatapathIdString,
					inputPortListString, outputPortListString, typeString,
					priorityString, valuesString, maximumFlowsAllowedString);

			g.pushRules();

			groupList.put(Integer.parseInt(request.getHeader("groupId")), g);
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("result", "group added");
			output = jsonObject.toJSONString();

		}

		else if (requestAction.equals("editGroup")) {

			Group g = groupList.get(Integer.parseInt(request
					.getHeader("groupId")));
			g.editGroup(request.getHeader("editType"),
					request.getHeader("updateValue"));

			// parse command ,
			// the right methods

		} else if (requestAction.equals("deleteGroup")) {

			logger.debug(groupList.toString());
			logger.debug("group id is {}", request.getHeader("groupId"));

			Group g = groupList.get(Integer.parseInt(request
					.getHeader("groupId")));

			g.removeGroup();
			groupList.remove(Integer.parseInt(request.getHeader("groupId")));

		} else if (requestAction.equals("getSwitchStatistics")) {

			long datapathId = HexString.toLong(request.getHeader("datapathId"));

			SwitchDevice switchDevice = controllerSwitches.get(datapathId);
			JSONArray jsonArray = switchDevice.getStatistics((request
					.getHeader("type")));

			output = jsonArray.toJSONString();

		}

		response.setStatus(HttpServletResponse.SC_OK);

		logger.debug("{}", output);

		response.getWriter().print(output);

	}

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
			controllerSwitches.put(sw.getId(), switchDevice);
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
			// hard coding SC switch to not delete all flows since switch is
			// shared

			sw.getOutputStream().write(ofDeleteAll);

			ofDefaultDropRule.setBufferId(-1);
			ofDefaultDropRule.setLength(U16.t(OFFlowMod.MINIMUM_LENGTH));
			sw.getOutputStream().write(ofDefaultDropRule);

			sw.getOutputStream().flush();

		} catch (Exception e) {

			FlowscaleController.logger.error("{}", e);
		}

		switchDevice.setOpenFlowSwitch(sw);
		switchDevice.setPhysicalPorts(sw.getFeaturesReply().getPorts());

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

	public void setJettyListenerPort(int port) {

		this.jettyListenerPort = port;
	}
	public void setCapstatsFilePath(String filePath){
		this.capstatsFilePath = filePath;
	}

	private JSONArray getSwitchPorts(long datapathId) {
		JSONArray js = new JSONArray();

		logger.debug("all switches {}", controllerSwitches.toString());
		logger.debug("this datapath id {}", datapathId);
		SwitchDevice switchDevice = this.controllerSwitches.get(datapathId);

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

		return js;
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
			Handler handler = this;
			logger.info("starting http server at port *:{}", jettyListenerPort);
			server = new Server(jettyListenerPort);
			server.setHandler(handler);
			server.start();

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
			server.stop();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("{}", e

			);
		}

	}

	public HashMap<Long, SwitchDevice> getSwitchDevices() {

		return this.controllerSwitches;

	}

	@Override
	public void addLifeCycleListener(Listener arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setServer(Server arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isFailed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRunning() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isStarted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isStarting() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isStopped() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isStopping() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeLifeCycleListener(Listener arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void start() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void stop() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public Server getServer() {
		// TODO Auto-generated method stub
		return null;
	}

}
