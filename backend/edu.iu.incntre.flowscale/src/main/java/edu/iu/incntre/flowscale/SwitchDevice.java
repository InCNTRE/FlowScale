package edu.iu.incntre.flowscale;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFPhysicalPort;
import org.openflow.protocol.OFPort;
import org.openflow.protocol.OFPortStatus;
import org.openflow.protocol.OFStatisticsRequest;
import org.openflow.protocol.OFPortStatus.OFPortReason;
import org.openflow.protocol.statistics.OFAggregateStatisticsReply;
import org.openflow.protocol.statistics.OFAggregateStatisticsRequest;
import org.openflow.protocol.statistics.OFFlowStatisticsReply;
import org.openflow.protocol.statistics.OFFlowStatisticsRequest;
import org.openflow.protocol.statistics.OFPortStatisticsReply;
import org.openflow.protocol.statistics.OFPortStatisticsRequest;
import org.openflow.protocol.statistics.OFStatistics;
import org.openflow.protocol.statistics.OFStatisticsType;
import org.openflow.protocol.statistics.OFTableStatistics;
import org.openflow.util.HexString;

import net.beaconcontroller.core.IOFSwitch;

/**
 * @author Ali Khalfan (akhlafan@indiana.edu)
 * 
 */
public class SwitchDevice {

    private String switchName;
    private String ipAddress;
    private String macAddress;
    private long datapathId;
    private ArrayList<OFRule> switchRules = new ArrayList<OFRule>();
    private ArrayList<SwitchPort> ports = new ArrayList<SwitchPort>();
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
        // FlowscaleController.logger.info("setting up portlist {}", portList);

        this.portList = portList;

    }

    public void updatePort(OFPortStatus ps) {

        // OFPhysicalPort portModified =
        // portList.get(ps.getDesc().getPortNumber());
        // FlowscaleController.logger.info("port list is {}", this.portList);
        FlowscaleController.logger.info("{}", ps.getReason());

        OFPortReason reason = OFPortReason.values()[ps.getReason()];

        if (reason == OFPortReason.OFPPR_ADD) {
            portList.add(ps.getDesc());
            return;
        }

        for (OFPhysicalPort ofp : this.portList) {

            if (HexString.toHexString(ofp.getHardwareAddress()).equals(
                    HexString.toHexString(ps.getDesc().getHardwareAddress()))) {

                if (reason == OFPortReason.OFPPR_DELETE) {
                    portList.remove(ofp);
                    return;
                } else if (reason == OFPortReason.OFPPR_MODIFY) {

                    portList.remove(ofp);
                    portList.add(ps.getDesc());

                    return;

                }

            }

        }

        return;

    }

    public List<OFPhysicalPort> getPortStates() {

        return this.portList;

    }

    public void setOpenFlowSwitch(IOFSwitch sw) {

        this.openFlowSwitch = sw;

    }

    public IOFSwitch getOpenFlowSwitch() {
        return this.openFlowSwitch;

    }

    public JSONObject getSwitchPorts() {

        return new JSONObject();

    }

    public void pushRuleToSwitch(OFFlowMod flowMod) {

    }

    public JSONArray getStatistics(String type) {

        JSONArray jsonArray = new JSONArray();

        if (type.equals("aggregate")) {
            jsonArray = getAggregateStatistics();
        } else if (type.equals("table")) {
            jsonArray = getTableStatistics();
        } else if (type.equals("flow")) {
            jsonArray = getFlowStatistics();
        } else if (type.equals("port")) {
            jsonArray = getPortStatistics();
        }

        return jsonArray;

    }

    private JSONArray getPortStatistics() {

        JSONArray jsonArray = new JSONArray();

        Future<List<OFStatistics>> future;
        IOFSwitch iofSwitch = this.openFlowSwitch;

        OFStatisticsRequest req = new OFStatisticsRequest();
        OFPortStatisticsRequest fsr = new OFPortStatisticsRequest();
        fsr.setPortNumber(OFPort.OFPP_NONE.getValue());
        req.setStatisticType(OFStatisticsType.PORT);
        req.setStatistics(Collections.singletonList((OFStatistics) fsr));
        req.setLengthU(fsr.getLength() + req.getLength());

        try {
            future = iofSwitch.getStatistics(req);
            List<OFStatistics> values = null;
            values = future.get(10, TimeUnit.SECONDS);

            for (OFStatistics ofst : values) {

                OFPortStatisticsReply st = (OFPortStatisticsReply) ofst;
                // st.getPortNumber() st.getReceiveBytes();

                JSONObject jsonObject = new JSONObject();
                if (st.getPortNumber() == -2 ){
                	continue;
                }
                jsonObject.put("port_id", st.getPortNumber());
                jsonObject.put("receive_packets", st.getreceivePackets());
                jsonObject.put("transmit_packets", st.getTransmitPackets());
                jsonObject.put("receive_bytes", st.getReceiveBytes());
                jsonObject.put("transmit_bytes",st.getTransmitBytes());
                
                jsonArray.add(jsonObject);

            }

            FlowscaleController.logger.debug("values are {}", values);
        } catch (Exception e) {
            FlowscaleController.logger.error("Failure retrieving {} ", e);
        }

        return jsonArray;
    }

    private JSONArray getTableStatistics() {

        JSONArray jsonArray = new JSONArray();

        Future<List<OFStatistics>> futureTable;
        IOFSwitch iofSwitch = this.openFlowSwitch;

        OFStatisticsRequest reqTable = new OFStatisticsRequest();

        reqTable.setStatisticType(OFStatisticsType.TABLE);
        reqTable.setLengthU(reqTable.getLengthU());

        try {
            futureTable = iofSwitch.getStatistics(reqTable);
            List<OFStatistics> values = null;
            values = futureTable.get(10, TimeUnit.SECONDS);

            for (OFStatistics ofst : values) {

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

                jsonArray.add(jsonObject);

            }

            FlowscaleController.logger.debug("values are {}", values);
        } catch (Exception e) {
            FlowscaleController.logger.error("Failure retrieving {} ", e);
        }

        return jsonArray;
    }

    private JSONArray getFlowStatistics() {

        JSONArray jsonArray = new JSONArray();

        Future<List<OFStatistics>> future;
        IOFSwitch iofSwitch = this.openFlowSwitch;

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

        try {
            future = iofSwitch.getStatistics(req);
            List<OFStatistics> futureValues = null;

            futureValues = future.get(10, TimeUnit.SECONDS);
            FlowscaleController.logger.debug("Future values are {}",
                    futureValues);
            for (OFStatistics ofst : futureValues) {

                OFFlowStatisticsReply st = (OFFlowStatisticsReply) ofst;
                // st.getPortNumber() st.getReceiveBytes();

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("actions", st.getActions().toString());
                jsonObject.put("hard_timeout", st.getHardTimeout());
                jsonObject.put("idle_timeout", st.getIdleTimeout());
                jsonObject.put("match", st.getMatch().toString());
                jsonObject.put("priority", st.getPriority());
                jsonObject.put("packet_count", st.getPacketCount());
                jsonObject.put("byte_count", st.getByteCount());
                jsonObject.put("table_id", st.getTableId());

                jsonArray.add(jsonObject);

            }

            FlowscaleController.logger.debug("values are {}", futureValues);
        } catch (Exception e) {
            FlowscaleController.logger.error("Failure retrieving {} ", e);
        }

        if (jsonArray.size() == 0) {
            JSONObject jso = new JSONObject();
            jso.put("data", "nono");
        }

        return jsonArray;
    }

    private JSONArray getAggregateStatistics() {

        JSONArray jsonArray = new JSONArray();

        Future<List<OFStatistics>> future;
        IOFSwitch iofSwitch = this.openFlowSwitch;

        OFStatisticsRequest req = new OFStatisticsRequest();
        OFAggregateStatisticsRequest fsr = new OFAggregateStatisticsRequest();

        req.setStatisticType(OFStatisticsType.AGGREGATE);
        req.setStatistics(Collections.singletonList((OFStatistics) fsr));
        req.setLengthU(fsr.getLength() + req.getLength());

        try {
            future = iofSwitch.getStatistics(req);
            List<OFStatistics> values = null;
            values = future.get(10, TimeUnit.SECONDS);

            for (OFStatistics ofst : values) {

                OFAggregateStatisticsReply st = (OFAggregateStatisticsReply) ofst;

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("packet_count", st.getPacketCount());
                jsonObject.put("flow_count", st.getFlowCount());

                jsonArray.add(jsonObject);

            }

            FlowscaleController.logger.debug("values are {}", values);
        } catch (Exception e) {
            FlowscaleController.logger.error("Failure retrieving {} ", e);
        }

        return jsonArray;
    }



}

