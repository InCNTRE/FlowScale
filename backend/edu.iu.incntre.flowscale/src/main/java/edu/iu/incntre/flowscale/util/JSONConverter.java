/** 
 * Copyright 2012 InCNTRE, This file is released under Apache 2.0 license except for component libraries under different licenses
http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.iu.incntre.flowscale.util;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openflow.protocol.OFPhysicalPort;
import org.openflow.protocol.statistics.OFAggregateStatisticsReply;
import org.openflow.protocol.statistics.OFFlowStatisticsReply;
import org.openflow.protocol.statistics.OFPortStatisticsReply;
import org.openflow.protocol.statistics.OFStatistics;
import org.openflow.protocol.statistics.OFTableStatistics;
import org.openflow.util.HexString;

import edu.iu.incntre.flowscale.FlowscaleController;

/**
 * Uility class to convert structure in openflowj to json 
 * @author Ali Khalfan
 *
 */
public class JSONConverter {

	
	/** 
	 * convert from status of OFPhysicalPort to JSONArray
	 * @param portList
	 * @return JSONArray of an ArrayList<OFPhysicalPort>
	 */
	
	public static JSONArray toPortStatus(List<OFPhysicalPort> portList){
		
		JSONArray jsonArray = new JSONArray();
		for (OFPhysicalPort pp : portList) {

			JSONObject obj = new JSONObject();
			if (pp.getPortNumber() <0) {
				continue;
			}
			obj.put("port_id", pp.getPortNumber());
			obj.put("port_address",
					HexString.toHexString(pp.getHardwareAddress()));
			obj.put("config", pp.getConfig());
			obj.put("supported", pp.getSupportedFeatures());
			obj.put("current", pp.getCurrentFeatures());

			obj.put("state", pp.getState());

			FlowscaleController.logger.debug("port {}", pp.getPortNumber());
			FlowscaleController.logger.debug("h/w {}",
					HexString.toHexString(pp.getHardwareAddress()));

			FlowscaleController.logger.debug("state {}", pp.getState());

			FlowscaleController.logger.debug("-------");

			jsonArray.add(obj);

		}
		
		return jsonArray;
		
		
	}
	
	/**
	 * convert List<OFStatistics> to JSONArray
	 * @param ofs
	 * @return JSONArray
	 */
	public static JSONArray toTableStat(List<OFStatistics> ofs){
		
		JSONArray jsonArray = new JSONArray();
		for (OFStatistics ofst : ofs) {

			OFTableStatistics st = (OFTableStatistics) ofst;
			// st.getPortNumber() st.getReceiveBytes();

			FlowscaleController.logger.debug(
					"Maximum Entries {} and and Table id {}",
					st.getMaximumEntries(), st.getTableId());
			FlowscaleController.logger.debug(
					"Name {} and and Table length {}", st.getName(),
					st.getLength());

			JSONObject jsonObject = new JSONObject();
			jsonObject.put("match_count", st.getMatchedCount());
			jsonObject.put("maximum_entries", st.getMaximumEntries());
			jsonObject.put("name", st.getName());
			jsonObject.put("table_id", st.getTableId());
			jsonObject.put("active_count", st.getActiveCount());

			jsonArray.add(jsonObject);

		}
		
		return jsonArray;
		
		
	}
		
/** 
 * convert from general List<OFStatistics> 
 * @param ofs
 * @param type
 * @return JSONArray of a List 
 */
public static JSONArray toStat(List<OFStatistics> ofs, String type){
	
	JSONArray jsonArray = new JSONArray();
	
	if (type.equals("aggregate")) {
		return toAggregateStat(ofs);
	} else if (type.equals("table")) {
		return toTableStat(ofs);
	} else if (type.equals("flow")) {
		return toFlowStat(ofs);
	} else if (type.equals("port")) {
		return toPortStat(ofs);
	}
	
	return jsonArray;
	
}
		
	public static JSONArray toPortStat(List<OFStatistics> ofs){
		JSONArray jsonArray = new JSONArray();
		
		for (OFStatistics ofst : ofs) {

			OFPortStatisticsReply st = (OFPortStatisticsReply) ofst;
			
			
			

			JSONObject jsonObject = new JSONObject();
			if (st.getPortNumber() < -2) {
				continue;
			}
			jsonObject.put("port_id", st.getPortNumber());
			jsonObject.put("receive_packets", st.getreceivePackets());
			jsonObject.put("transmit_packets", st.getTransmitPackets());
			jsonObject.put("receive_bytes", st.getReceiveBytes());
			jsonObject.put("transmit_bytes", st.getTransmitBytes());

			jsonArray.add(jsonObject);

		}

		
	

	return jsonArray;
			
		
		
		
	}
		
	/**
	 * convert a list of flows to JSONArray
	 * @param ofs
	 * @return JSONArray of flows 
	 */
	public static JSONArray toFlowStat(List<OFStatistics> ofs){
		JSONArray jsonArray = new JSONArray();
		
		
		
		for (OFStatistics ofst : ofs) {

			OFFlowStatisticsReply st = (OFFlowStatisticsReply) ofst;

			JSONObject jsonObject = new JSONObject();
			String[] a = st.getActions().toString().split("port=");
			String c;
			c = st.getActions().toString();
			if (a.length == 1) {
				c = st.getActions().toString();
			} else {

				String[] b = a[1].split(",");
				c = b[0];
			}

			// parse the output
			String outputString = "";
			try {
				Pattern pattern = Pattern.compile("port=\\w*");

				Matcher matcher = pattern.matcher(st.getActions()
						.toString());

				while (matcher.find()) {

					outputString += matcher.group().split("=")[1] + ",";
				}

			} catch (PatternSyntaxException pse) {
				outputString = "malformed";
			}

			if (outputString.length() == 0) {
				outputString = "DROP,";
			}
			jsonObject.put("actions",
					outputString.substring(0, outputString.length() - 1));
			jsonObject.put("hard_timeout", st.getHardTimeout());
			jsonObject.put("idle_timeout", st.getIdleTimeout());
			jsonObject.put("match", st.getMatch().toString());
			jsonObject.put("priority", st.getPriority()); 
			jsonObject.put("packet_count", st.getPacketCount());
			jsonObject.put("byte_count", st.getByteCount());
			jsonObject.put("table_id", st.getTableId());

			jsonArray.add(jsonObject);

		}



	if (jsonArray.size() == 0) {
		JSONObject jso = new JSONObject();
		jso.put("data", "nono");
	}
		

		return jsonArray;
		
	}
	
	/**
	 * convert from aggregate stats to JSONArray
	 * 
	 * @param ofs
	 * @return JSONArray of aggregate stats 
	 */
	public static JSONArray toAggregateStat(List<OFStatistics> ofs){
		JSONArray jsonArray = new JSONArray();
		
	
	for (OFStatistics ofst : ofs) {

		OFAggregateStatisticsReply st = (OFAggregateStatisticsReply) ofst;

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("packet_count", st.getPacketCount());
		jsonObject.put("flow_count", st.getFlowCount());

		jsonArray.add(jsonObject);

	}



return jsonArray;
	
	}
	
	
	
}
