package edu.iu.incntre.flowscale;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.openflow.util.HexString;

import net.beaconcontroller.core.IOFSwitch;

public class DatabaseUtility {

	static PreparedStatement updateTotal = null;
	static Connection conn;

	static String DRIVER_CLASS_NAME = "com.mysql.jdbc.Driver";

	public void setConnection(String username, String password, String host,
			String port) {

		// Provided by your driver documentation. In this case, a MySql driver

		String db_conn_string = "jdbc:mysql://" + host + ":" + port + "/webui";
		conn = null;
		try {
			Class.forName(DRIVER_CLASS_NAME).newInstance();
		} catch (Exception ex) {
			FlowscaleController.logger
					.error("Check classpath. Cannot load db driver: "
							+ DRIVER_CLASS_NAME);
		}

		try {
			conn = DriverManager.getConnection(db_conn_string, username,
					password);
		} catch (SQLException e) {
			FlowscaleController.logger
					.error("Driver loaded, but cannot connect to db: "
							+ db_conn_string);

		}

	}

	public HashMap<Integer, Group> populateGroupsFromDatabase(
			FlowscaleController controller) {

		FlowscaleController.logger
				.info("adding groups from database incase there are any records ");

		HashMap<Integer, Group> groupList = new HashMap<Integer, Group>();
		// do all db transaction here
		String flow_group_query = "SELECT group_id , input_switch ,output_switch , comments , priority , type, maximum_flows type FROM flow_group";

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
				valuesString = valuesString.substring(1);

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
						valuesString, maximumFlowsAllowedString);

				groupList.put(Integer.parseInt(groupIdString), group);
				FlowscaleController.logger.debug("group list in loop {}",
						groupList);
			}

		} catch (SQLException sqlE) {
			FlowscaleController.logger.info("{}", sqlE);

		}
		FlowscaleController.logger.debug("this is the group list in the db {}",
				groupList);

		return groupList;

	}

	public HashMap<Long, SwitchDevice> populateSwitchesFromDatabase(
			FlowscaleController controller) {

		HashMap<Long, SwitchDevice> switchList = new HashMap<Long, SwitchDevice>();
		String switch_query = "select datapath_id from switch";
		try {
			PreparedStatement switchPS = conn.prepareStatement(switch_query);

			ResultSet switchRS = switchPS.executeQuery();

			long datapathId;

			while (switchRS.next()) {

				datapathId = HexString.toLong(switchRS.getString(1));
				SwitchDevice switchDevice = new SwitchDevice(datapathId);

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
