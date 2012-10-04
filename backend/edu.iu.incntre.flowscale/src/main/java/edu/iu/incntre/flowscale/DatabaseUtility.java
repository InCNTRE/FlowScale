package edu.iu.incntre.flowscale;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.openflow.util.HexString;

import edu.iu.incntre.flowscale.exception.NoDatabaseException;

public class DatabaseUtility {

	static PreparedStatement updateTotal = null;
	static Connection conn;

	public void setDriver(String driver) {

	}

	public void setConnection(String username, String password,
			String connectionString, String dbDriver) {

		String DRIVER_CLASS_NAME = dbDriver;

		conn = null;
		try {
			Class.forName(DRIVER_CLASS_NAME).newInstance();
		} catch (Exception ex) {
			FlowscaleController.logger
					.error("Check classpath. Cannot load db driver: "
							+ DRIVER_CLASS_NAME);
		}

		try {
			conn = DriverManager.getConnection(connectionString, username,
					password);
		} catch (SQLException e) {
			FlowscaleController.logger
					.error("Driver loaded, but cannot connect to db: "
							+ connectionString);

		}

	}

	public HashMap<Integer, Group> populateGroupsFromDatabase (
			FlowscaleController controller) throws NoDatabaseException{

			if (conn == null){
				throw new NoDatabaseException();
			}
		
		FlowscaleController.logger
				.info("adding groups from database incase there are any records ");

		HashMap<Integer, Group> groupList = new HashMap<Integer, Group>();
		// do all db transaction here
		String flow_group_query = "SELECT group_id , input_switch ,output_switch , comments , priority , type, maximum_flows, "
				+ "network_protocol, transport_direction FROM flow_group";

		String group_port_query = "SELECT port_direction, port_id FROM group_port WHERE group_id = ?";

		String group_values_query = "SELECT value FROM group_values where group_id = ?";
		
		
		
		try {
		
			
			PreparedStatement groupPS = conn.prepareStatement(flow_group_query);
			PreparedStatement groupPortPS = conn
					.prepareStatement(group_port_query);
			PreparedStatement groupValuesPS = conn
					.prepareStatement(group_values_query);

			ResultSet groupRs = groupPS.executeQuery();
			String groupIdString = "";
			String groupName = "";
			String inputSwitchDatapathIdString = "";
			String outputSwitchDatapathIdString = "";
			String inputPortListString = "";
			String outputPortListString = "";
			String typeString = "";
			String priorityString = "";
			String valuesString = "";
			String maximumFlowsAllowedString = "";
			String networkProtocolString = "";
			String transportDirectionString = "";

			while (groupRs.next()) {

				FlowscaleController.logger.debug("fetching a  record");
				groupIdString = groupRs.getString(1);
				FlowscaleController.logger.debug("group id string is {}",
						groupIdString);

				groupName = groupRs.getString(4);
				inputSwitchDatapathIdString = groupRs.getString(2);
				outputSwitchDatapathIdString = groupRs.getString(3);
				priorityString = groupRs.getString(5);
				typeString = groupRs.getString(6);
				maximumFlowsAllowedString = groupRs.getString(7);
				networkProtocolString = groupRs.getString(8);
				transportDirectionString = groupRs.getString(9);

				groupPortPS.setInt(1, Integer.parseInt(groupIdString));
				ResultSet groupPortRS = groupPortPS.executeQuery();

				inputPortListString = "";
				outputPortListString = "";
				while (groupPortRS.next()) {

					if (groupPortRS.getInt(1) == 0) {
						inputPortListString += "," + groupPortRS.getString(2);
					} else if (groupPortRS.getInt(1) == 1) {
						outputPortListString += "," + groupPortRS.getString(2);
					}

				}
				if (!inputPortListString.equals("")) {
					inputPortListString = inputPortListString.substring(1);
				}
				if (!outputPortListString.equals("")) {

					outputPortListString = outputPortListString.substring(1);

				}

				groupValuesPS.setInt(1, Integer.parseInt(groupIdString));
				ResultSet groupValuesRS = groupValuesPS.executeQuery();
				valuesString = "";
				while (groupValuesRS.next()) {

					valuesString += "," + groupValuesRS.getString(1);
				}
				try{valuesString = valuesString.substring(1);
				
				}catch(StringIndexOutOfBoundsException stringOutOfBoundException){
					continue;
				}

				FlowscaleController.logger.debug("value {}", valuesString);
				FlowscaleController.logger.debug("input {}",
						inputPortListString);
				FlowscaleController.logger.debug("outputport list {}",
						outputPortListString);

				Group group = new Group(controller);
				FlowscaleController.logger
						.debug("The following group to add with details: ");
				FlowscaleController.logger.debug(
						"groupIDString and name {} {}", groupIdString,
						groupName);
				FlowscaleController.logger.debug(
						"outputSwitchDatapathIdString {}",
						outputSwitchDatapathIdString);
				FlowscaleController.logger.debug("typeString {}", typeString);
				FlowscaleController.logger.debug("valuesString {}",
						valuesString);

				group.addGroupDetails(groupIdString, groupName,
						inputSwitchDatapathIdString,
						outputSwitchDatapathIdString, inputPortListString,
						outputPortListString, typeString, priorityString,
						valuesString, maximumFlowsAllowedString,
						networkProtocolString, transportDirectionString);

				groupList.put(Integer.parseInt(groupIdString), group);
				FlowscaleController.logger.debug("group list in loop {}",
						groupList);
			}

		} catch (SQLException sqlE) {
			FlowscaleController.logger.info("{}", sqlE);

		}finally{
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				FlowscaleController.logger.info("{}", e);
			}
		}
		FlowscaleController.logger.debug("this is the group list in the db {}",
				groupList);

		return groupList;

	}

	public HashMap<Long, SwitchDevice> populateSwitchesFromDatabase(
			FlowscaleController controller) throws NoDatabaseException{

		HashMap<Long, SwitchDevice> switchList = new HashMap<Long, SwitchDevice>();
		String switch_query = "select datapath_id , switch_name from switch";
		
		if (conn == null){
			throw new NoDatabaseException();
		}
		
		try {
			PreparedStatement switchPS = conn.prepareStatement(switch_query);

			ResultSet switchRS = switchPS.executeQuery();

			long datapathId;
			String switchName;
			while (switchRS.next()) {

				datapathId = HexString.toLong(switchRS.getString(1));
				switchName =  switchRS.getString(2);
				SwitchDevice switchDevice = new SwitchDevice(datapathId,switchName);

				switchList.put(datapathId, switchDevice);

			}

			return switchList;

		} catch (SQLException sqlE) {

			FlowscaleController.logger.error("{}", sqlE);
			return null;
		}
	}

	public String getName() {
		// TODO Auto-generated method stub
		return "databaseUtility";
	}

}
