/** 
 * Copyright 2012 InCNTRE, This file is released under Apache 2.0 license except for component libraries under different licenses
http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.iu.incntre.flowscale;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFPhysicalPort;
import org.openflow.protocol.OFPort;
import org.openflow.protocol.OFPortStatus;
import org.openflow.protocol.OFStatisticsRequest;
import org.openflow.protocol.OFPortStatus.OFPortReason;
import org.openflow.protocol.statistics.OFAggregateStatisticsRequest;
import org.openflow.protocol.statistics.OFFlowStatisticsRequest;
import org.openflow.protocol.statistics.OFPortStatisticsRequest;
import org.openflow.protocol.statistics.OFStatistics;
import org.openflow.protocol.statistics.OFStatisticsType;
import org.openflow.util.HexString;

import edu.iu.incntre.flowscale.exception.NoSwitchException;

import net.beaconcontroller.core.IOFSwitch;

/** This class is an abstraction of an openflow switch for FlowScale. It provides functionality 
 * needed for FlowScale that may not exist in IDFSwitch (consider inheriting IOFSwitch in the future)
 * @author Ali Khalfan (akhlafan@indiana.edu)
 * 
 */
public class SwitchDevice {

	private String switchName;
	private String ipAddress;
	private String macAddress;
	private long datapathId;
	private ArrayList<OFRule> switchRules = new ArrayList<OFRule>();

	private IOFSwitch openFlowSwitch;
	private List<OFPhysicalPort> portList;
	private List<Short> outputPortsUp = new ArrayList<Short>();

	public SwitchDevice() {

	}

	public SwitchDevice(String switchName, String ipAddress, String macAddress,
			long datapathId) {
		this.switchName = switchName;
		this.ipAddress = ipAddress;
		this.macAddress = macAddress;
		this.datapathId = datapathId;

	}

	public SwitchDevice(long datapathId) {

		this.datapathId = datapathId;
	}

	public String getSwitchName() {
		return switchName;
	}

	public void setSwitchName(String switchName) {
		this.switchName = switchName;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	public long getDatapathId() {
		return datapathId;
	}

	public void setDatapathId(long datapathId) {
		this.datapathId = datapathId;
	}

	public void setPhysicalPorts(List<OFPhysicalPort> portList) {
	

		this.portList = portList;

	}

	/**
	 * When a switch reports an update within a port FlowscaleController calls this method, charged with
	 * updating the status of the ports in the port list to convey the new status of the port 
	 * @param ps
	 */
	public void updatePort(OFPortStatus ps) {

		

		OFPortReason reason = OFPortReason.values()[ps.getReason()];

		if (reason == OFPortReason.OFPPR_ADD) {
			portList.add(ps.getDesc());
			return;
		}

		

				if (reason == OFPortReason.OFPPR_DELETE) {
					
					for(OFPhysicalPort ofp : portList){
						if(ofp.getPortNumber() == ps.getDesc().getPortNumber() && HexString.toHexString(ofp.getHardwareAddress()).equals(HexString.toHexString(ps.getDesc().getHardwareAddress())) ) {
						portList.remove(ofp);
						return;
						}
						
					}
					
					
				} else if (reason == OFPortReason.OFPPR_MODIFY) {

					
					for(OFPhysicalPort ofp : portList){
						if(ofp.getPortNumber() == ps.getDesc().getPortNumber() && HexString.toHexString(ofp.getHardwareAddress()).equals(HexString.toHexString(ps.getDesc().getHardwareAddress())) )   {
						portList.remove(ofp);
						break;
						}
						
					}
						
				
					portList.add(ps.getDesc());
					
					return;

				}

		return;

	}

	public List<OFPhysicalPort> getPortStates() {

		return this.portList;

	}
/** 
 * Set the IOFSwitch object for easy retrieval since SwitchDevice will be associated with other objects in the FlowScale package
 * 
 * @param sw IOFSwitch object
 */
	public void setOpenFlowSwitch(IOFSwitch sw) {

		this.openFlowSwitch = sw;

	}

	public IOFSwitch getOpenFlowSwitch() {
		return this.openFlowSwitch;

	}




/**
 *  This method will be used to retrieve different statisitcs from the switch as returned by OFPT_STATS_REPLY,
 *  this method encapsulates the more specific private methods 
 * @param type
 * @return List<OFStatistics> returns whatever OFStatistics are requests (aggregate, table, Flow or Port , 
 * @throws IOException
 * @throws InterruptedException
 * @throws ExecutionException
 * @throws TimeoutException
 * @throws NoSwitchException
 */
	  List<OFStatistics> getStatistics(String type) throws IOException, InterruptedException, ExecutionException, TimeoutException, NoSwitchException {

		

		if (type.equals("aggregate")) {
			return getAggregateStatistics();
		} else if (type.equals("table")) {
			return getTableStatistics();
		} else if (type.equals("flow")) {
			return getFlowStatistics();
		} else if (type.equals("port")) {
			return getPortStatistics();
		}

		return null;

	}

	
	


/**
 * Similar to getStatistics , but to be callsed from the FloscaleController (instead of the HttpListener), specifically to retreive flows
 * the reason for this method is for objects that need non-json data (e.g. FlowscaleStatCollector) - consider integrating this method 
 * with the one above and entirely removing this method 
 * 
 * @return
 * @throws NoSwitchException
 * @throws IOException
 * @throws InterruptedException
 * @throws ExecutionException
 * @throws TimeoutException
 */
	public List<OFStatistics> getFlowStatisticsForLoader()
			throws NoSwitchException, IOException, InterruptedException,
			ExecutionException, TimeoutException {
		IOFSwitch iofSwitch = this.openFlowSwitch;
		Future<List<OFStatistics>> future;
		OFStatisticsRequest req = new OFStatisticsRequest();
		OFFlowStatisticsRequest fsr = new OFFlowStatisticsRequest();
		OFMatch match = new OFMatch();
		match.setWildcards(0xffffffff);
		fsr.setMatch(match);
		fsr.setOutPort(OFPort.OFPP_NONE.getValue());
		fsr.setTableId((byte) 0xff);
		req.setStatisticType(OFStatisticsType.FLOW);
		req.setStatistics(Collections.singletonList((OFStatistics) fsr));
		req.setLengthU(req.getLengthU() + fsr.getLength());

		if (iofSwitch == null) {
			throw new NoSwitchException();

		}

		future = iofSwitch.getStatistics(req);
		List<OFStatistics> futureValues = null;

		futureValues = future.get(10, TimeUnit.SECONDS);
		return futureValues;

	}

	
	
	
	
	/** 
	 * getPortStatistics is a private method only called by getStatistics
	 * @return  List<OFStatistics>
	 * @throws NoSwitchException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	private  List<OFStatistics> getPortStatistics() throws NoSwitchException, IOException, InterruptedException, ExecutionException, TimeoutException {

		

		Future<List<OFStatistics>> future;
		IOFSwitch iofSwitch = this.openFlowSwitch;
		if(iofSwitch == null){
			throw new NoSwitchException(HexString.toHexString(this.datapathId));
		}
		OFStatisticsRequest req = new OFStatisticsRequest();
		OFPortStatisticsRequest fsr = new OFPortStatisticsRequest();
		fsr.setPortNumber(OFPort.OFPP_NONE.getValue());
		req.setStatisticType(OFStatisticsType.PORT);
		req.setStatistics(Collections.singletonList((OFStatistics) fsr));
		req.setLengthU(fsr.getLength() + req.getLength());
		List<OFStatistics> values = null;
	

				future = iofSwitch.getStatistics(req);
				
				 values = future.get(10, TimeUnit.SECONDS);
		
			return values;
			 

				
				

	}
/**
 * a private method to retrieve the table statistics from the switch , this method is only called by getStatistics
 * @return List<OFStatistics>
 * @throws IOException
 * @throws InterruptedException
 * @throws ExecutionException
 * @throws TimeoutException
 * @throws NoSwitchException
 */
	private List<OFStatistics> getTableStatistics() throws IOException, InterruptedException, ExecutionException, TimeoutException, NoSwitchException {

		

		Future<List<OFStatistics>> futureTable;
		IOFSwitch iofSwitch = this.openFlowSwitch;
		if(iofSwitch == null){
			throw new NoSwitchException(HexString.toHexString(this.datapathId));
		}
		OFStatisticsRequest reqTable = new OFStatisticsRequest();

		reqTable.setStatisticType(OFStatisticsType.TABLE);
		reqTable.setLengthU(reqTable.getLengthU());

	
		
				futureTable = iofSwitch.getStatistics(reqTable);
				List<OFStatistics> values = null;
				
				values = futureTable.get(10, TimeUnit.SECONDS);
				return values;

	}
	
	
	
	
	/**
	 * This method is used to retrieve to Flows from the switch this method is only called getStatistics 
	 * 
	 * @return List<OFStatistics>
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 * @throws NoSwitchException
	 */
	
	private List<OFStatistics>getFlowStatistics() throws IOException, InterruptedException, ExecutionException, TimeoutException, NoSwitchException {

		

		Future<List<OFStatistics>> future;
		IOFSwitch iofSwitch = this.openFlowSwitch;
		if(iofSwitch == null){
			throw new NoSwitchException(HexString.toHexString(this.datapathId));
		}
		OFStatisticsRequest req = new OFStatisticsRequest();
		OFFlowStatisticsRequest fsr = new OFFlowStatisticsRequest();
		OFMatch match = new OFMatch();
		match.setWildcards(0xffffffff);
		fsr.setMatch(match);
		fsr.setOutPort(OFPort.OFPP_NONE.getValue());
		fsr.setTableId((byte) 0xff);
		req.setStatisticType(OFStatisticsType.FLOW);
		req.setStatistics(Collections.singletonList((OFStatistics) fsr));
		req.setLengthU(req.getLengthU() + fsr.getLength());

	
			future = iofSwitch.getStatistics(req);
			List<OFStatistics> futureValues = null;

			futureValues = future.get(10, TimeUnit.SECONDS);
			
			return futureValues;

	}

	/**
	 * 
	 * @return List<OFStatistics>
	 * @throws NoSwitchException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	private List<OFStatistics> getAggregateStatistics() throws NoSwitchException , IOException, InterruptedException, ExecutionException, TimeoutException {

	

		Future<List<OFStatistics>> future;
		IOFSwitch iofSwitch = this.openFlowSwitch;
		if(iofSwitch == null){
			
		}

		OFStatisticsRequest req = new OFStatisticsRequest();
		OFAggregateStatisticsRequest fsr = new OFAggregateStatisticsRequest();

		req.setStatisticType(OFStatisticsType.AGGREGATE);
		req.setStatistics(Collections.singletonList((OFStatistics) fsr));
		req.setLengthU(fsr.getLength() + req.getLength());

		
			future = iofSwitch.getStatistics(req);
			List<OFStatistics> values = null;
			values = future.get(10, TimeUnit.SECONDS);
			return values;
			
	}

}
