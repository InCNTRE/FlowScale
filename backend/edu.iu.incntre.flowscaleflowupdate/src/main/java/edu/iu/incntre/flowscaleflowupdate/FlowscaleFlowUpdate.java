package edu.iu.incntre.flowscaleflowupdate;



import grnoc.net.util.ipaddress.IPv4Address;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFPhysicalPort;
import org.openflow.protocol.OFType;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionOutput;
import org.openflow.protocol.statistics.OFStatistics;
import org.openflow.util.HexString;

import net.beaconcontroller.core.IOFSwitch;

import edu.iu.incntre.flowscale.FlowscaleController;
import edu.iu.incntre.flowscale.Group;
import edu.iu.incntre.flowscale.IPAddress;
import edu.iu.incntre.flowscale.OFRule;
import edu.iu.incntre.flowscale.SwitchDevice;
import edu.iu.incntre.flowscale.exception.NoSwitchException;
import edu.iu.incntre.flowscale.util.JSONConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class will hot swap flows in order to remove flows from highly loaded
 * ports
 * 
 * 
 * @author Ali Khalfan (akhalfan@indiana.edu)
 * 
 * **/

public class FlowscaleFlowUpdate {

	protected static Logger logger = LoggerFactory
			.getLogger(FlowscaleFlowUpdate.class);
	private String databaseDriver;
	private String databaseClass;
	private Connection conn;
	private Statement sqlStatement;
	protected Thread hotSwappingThread;
	private FlowscaleController flowscaleController;
	private int intervalTime;

	private String dbUsername;
	private String dbPassword;
	private HashMap<Long, HashMap<Short, Short>> switchFlowMirrorPortsHashMap;

	private HashMap<Long, ArrayList<Short>> loadedPortsHashMap = new HashMap<Long, ArrayList<Short>>();
	double optimalPercentage;

	public void setDatabaseDriver(String databaseDriver) {
		this.databaseDriver = databaseDriver;
	}

	public void setDatabaseClass(String databaseClass) {
		this.databaseClass = databaseClass;
	}

	public void setFlowscaleController(FlowscaleController flowscaleController) {
		this.flowscaleController = flowscaleController;
	}

	public void setIntervalTime(int intervalTime) {

		this.intervalTime = intervalTime;

	}

	public void setLoadedPorts(String loadedPortsString) {

		String[] splitByDatapath = loadedPortsString.split("-");

		for (String switchAndLoadedPorts : splitByDatapath) {
			String datapathIdString = switchAndLoadedPorts.split(":")[0];
			String loadedPortsFromDatapathId = switchAndLoadedPorts.split(":")[1];

			String[] loadedPortStrings = loadedPortsFromDatapathId.split(",");

			ArrayList<Short> loadedPorts = new ArrayList<Short>();

			for (String loadedPortString : loadedPortStrings) {

				loadedPorts.add(Short.parseShort(loadedPortString));

			}

			loadedPortsHashMap.put(HexString.toLong(datapathIdString),
					loadedPorts);

		}

	}

	public void setDbUsername(String dbUsername) {
		this.dbUsername = dbUsername;
	}

	public void setDbPassword(String dbPassword) {
		this.dbPassword = dbPassword;

	}

	public void startUp() {

		try {
			Class.forName(databaseClass);
			conn = DriverManager.getConnection(databaseDriver, dbUsername,
					dbPassword);
			sqlStatement = conn.createStatement();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error("{}", e);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			logger.error("{}", e);
		}

		logger.info("Start up of flow updater ");
		try{
		hotSwappingThread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
//get stats from database
					String flowStatQuery = "SELECT datapath_id, match_string, action, packet_count FROM flow_stats where datapath_id = ? AND  timestamp >= ?";
					switchFlowMirrorPortsHashMap = flowscaleController
							.getSwitchFlowMirrorPortsHashMap();
					while (hotSwappingThread != null) {

						logger.info("in flow updater thread ");
						HashMap<Long, SwitchDevice> switchDevices = flowscaleController
								.getSwitchDevices();
						long queryTime = Calendar.getInstance()
								.getTimeInMillis();

						for (long datapathId : loadedPortsHashMap.keySet()) {
							
							if(switchDevices.get(datapathId) == null || switchDevices.get(datapathId).getOpenFlowSwitch() ==null){
								
								logger.info("switch {} is not connected yet", HexString.toHexString(datapathId));
								continue;
							}
							
						
							
							HashMap<Short, Short> mirroredPorts = null;
							mirroredPorts = switchFlowMirrorPortsHashMap
									.get(datapathId);

							if (mirroredPorts == null) {
								mirroredPorts = new HashMap<Short, Short>();
							}

							ArrayList<Short> loadedPorts = new ArrayList<Short>();
							for(Short shortPort : loadedPortsHashMap.get(datapathId)){
								
								loadedPorts.add(shortPort);
								
								
							}
							
	//end get stats from database
							
						//copy loaded ports into another arraylist since some ports will be removed if they are down during this iteration 	
							Collections.copy(loadedPorts,loadedPortsHashMap.get(datapathId));
							
							
							logger.debug("port list {}", flowscaleController.getSwitchDevices().get(datapathId).getPortStates());
							
							if (switchDevices.get(datapathId).getPortStates() ==null){
								logger.info("switch {} not ready for flow update yet, port states data structure is null ", HexString.toHexString(datapathId));
								continue;
							}
							
							//make sure only load balance on ports that are up 
							removeDownPorts(flowscaleController, datapathId,loadedPorts);
					

						
							//obtain optimal percentage for each loaded ports
							optimalPercentage = (double) (100 / loadedPorts
									.size());

					//retreive flowstats from database since the load balancing will be donw by moving flows
							
							PreparedStatement flowStatPs = null;
							ResultSet flowStatRs = null;

							HashMap<Short, Long> portPacketCount = new HashMap<Short, Long>();

							HashMap<Short, ArrayList<String>> portToFlowHashMap = new HashMap<Short, ArrayList<String>>();

							HashMap<String, Short> flowToPortHashMap = new HashMap<String, Short>();
							HashMap<String, Double> flowPercentage = new HashMap<String, Double>();
							HashMap<String, Long> flowStat = new HashMap<String, Long>();
							HashMap<Short, Double> percentages = new HashMap<Short, Double>();
							ArrayList<Short> belowPercentPorts = new ArrayList<Short>();
							ArrayList<Short> abovePercentPorts = new ArrayList<Short>();
							ArrayList<OFFlowMod> flowMods = new ArrayList<OFFlowMod>();
							ArrayList<FlowPercent> flowPercentArrayList = new ArrayList<FlowPercent>();
							long totalPacketCount = 0;
				

							try {
								logger.info(
										"query : SELECT datapath_id, match_string, action, packet_count FROM flow_stats where datapath_id = {} AND  timestamp >= {} ",
										datapathId, queryTime
												- (intervalTime * 1000));

								flowStatPs = conn
										.prepareStatement(flowStatQuery);
								flowStatPs.setLong(1, datapathId);
								flowStatPs.setLong(2, queryTime
										- (intervalTime * 1000));

								flowStatRs = flowStatPs.executeQuery();

								for (Short loadedPort : loadedPorts) {

									portPacketCount.put(loadedPort, 0L);

								}
		
								//call db to populate flowstats structures here 
								

								logger.info("total packet count {}",
										totalPacketCount);

								for (Short port : portPacketCount.keySet()) {
									Double percentageValue = new Double(0);
									if (totalPacketCount == 0) {
										percentages.put(port, percentageValue);
									} else {

										percentageValue = (double) ((double) portPacketCount
												.get(port)
												/ (double) totalPacketCount * 100);
										logger.info(
												"port packet count for port {} is {}",
												port, portPacketCount.get(port));

										percentages.put(port, new Double(
												percentageValue));

									}
									if (percentageValue <= optimalPercentage) {
										belowPercentPorts.add(port);

									} else {

										abovePercentPorts.add(port);

									}

								}
								for (String matchValue : flowStat.keySet()) {

									Double percentageValue = new Double(0);

									if (totalPacketCount == 0) {
										flowPercentage.put(matchValue,
												percentageValue);
									} else {

										percentageValue = (double) ((double) flowStat
												.get(matchValue)
												/ (double) totalPacketCount * 100);

										flowPercentage.put(matchValue,
												percentageValue);
									}

									FlowPercent flowPercentInstance = new FlowPercent(
											matchValue, flowPercentage
													.get(matchValue));
									flowPercentArrayList
											.add(flowPercentInstance);
								}

								logger.info("port packet count list {}",
										portPacketCount.toString());
								logger.info("percentage list {}",
										percentages.toString());

								Collections.sort(flowPercentArrayList,
										new FlowPercentComparator());

								for (FlowPercent flowPercent : flowPercentArrayList) {

									logger.info("flow {} has percentage {}",
											flowPercent.getFlow(),
											flowPercent.getFlowPercent());
									logger.info("its port is {}",
											flowToPortHashMap.get(flowPercent
													.getFlow()));

								}

								// now get Percentage of each action

								for (Short port : belowPercentPorts) {

									logger.info(
											"port below balance {} and percent is {}",
											port, percentages.get(port));

								}
								for (Short port : abovePercentPorts) {

									logger.info(
											"port above balance {} and percent is {} ",
											port, percentages.get(port));

								}

								HashMap<Short, ArrayList<String>> newFlows = new HashMap<Short, ArrayList<String>>();

								Collections.sort(abovePercentPorts,
										new HighPortComparator(percentages));

								for (Short highPort : abovePercentPorts) {
									logger.info(
											"sorted port is {} and percentage is {}",
											highPort, percentages.get(highPort));
								}

								ArrayList<String> checkedFlows = new ArrayList<String>();

								for (Short highPort : abovePercentPorts) {

									for (FlowPercent flowPercent : flowPercentArrayList) {

										logger.info(
												"trying to move flows from high port {}",
												highPort);

										if (!(flowToPortHashMap.get(flowPercent
												.getFlow()).equals(highPort))) {
											logger.info(
													"flow {} does not belong to high port  {}",
													flowPercent.getFlow(),
													highPort);
											continue;
										}
										if (flowPercent.getFlowPercent()
												.doubleValue() < 0.05) {
											logger.info(
													"flow {} has percent {} which is  equal to 0 anyway",
													flowPercent.getFlow(),
													flowPercent
															.getFlowPercent());
											continue;
										}
										if (checkedFlows.contains(flowPercent
												.getFlow())) {
											logger.info(
													"flow {} is already in the checked list ",
													flowPercent.getFlow());
											continue;
										}
										Short port = flowToPortHashMap
												.get(flowPercent.getFlow());

										if (abovePercentPorts.contains(port)) {
											double flowPercentageValue = flowPercent
													.getFlowPercent();

											Short lowPort = getMinPort(
													belowPercentPorts,
													percentages);
											if (lowPort == null) {
												logger.info("no more loadbalancing allowed");
												break;
											}

											if (lowPort != null) {
												logger.info(
														"check if port {} with percentage {} can receive",
														lowPort, percentages
																.get(lowPort));

												String otherDirectFlow = "";

												if (flowPercent.getFlow()
														.indexOf("nw_src") != -1) {
													otherDirectFlow = flowPercent
															.getFlow().replace(
																	"nw_src",
																	"nw_dst");

												} else if (flowPercent
														.getFlow().indexOf(
																"nw_dst") != -1) {
													otherDirectFlow = flowPercent
															.getFlow().replace(
																	"nw_dst",
																	"nw_src");

												}

												double totalNeededPercentage = (double) flowPercentageValue
														+ (double) flowPercentage
																.get(otherDirectFlow);

												// logger.info("total Percentage needed is {}",
												// totalNeededPercentage);
												// logger.info("From flows {}",
												// flowPercent.getFlow());

												if (((totalNeededPercentage < (optimalPercentage - percentages
														.get(lowPort))) || ((percentages
														.get(highPort) - percentages
														.get(lowPort)) >= (double) (optimalPercentage / 2)))
														&& totalNeededPercentage < ((double) optimalPercentage / 2)) {

													// add to newFlows
													logger.info(
															"success in moving from high port {} with flow {}",
															port, flowPercent
																	.getFlow());
													if (newFlows.get(lowPort) == null) {

														ArrayList<String> updatedFlows = new ArrayList<String>();
														newFlows.put(lowPort,
																updatedFlows);

													}

													newFlows.get(lowPort).add(
															flowPercent
																	.getFlow());

													checkedFlows
															.add(otherDirectFlow);
													checkedFlows
															.add(flowPercent
																	.getFlow());

													// subtract flow percentage
													// from
													// high load port

													double portPercentage = percentages
															.get(port);
													percentages
															.put(port,
																	(double) ((double) portPercentage
																			- (double) flowPercentageValue - (double) flowPercentage
																			.get(otherDirectFlow)));

													// add flow percentage to
													// low load
													// port
													double lowPortPercentage = percentages
															.get(lowPort);
													percentages
															.put(lowPort,
																	(double) ((double) lowPortPercentage
																			+ (double) flowPercentageValue + (double) flowPercentage
																			.get(otherDirectFlow)));

													logger.info(
															"port {} new percentage is {}",
															lowPort,
															percentages
																	.get(lowPort));

													if (percentages.get(port) <= optimalPercentage) {

														break;
														// abovePercentPorts.remove(port);

													}

													if (percentages
															.get(lowPort) >= optimalPercentage) {
														belowPercentPorts
																.remove(lowPort);
													}

												}

											}

										}
									}
								}

								logger.info("percentage are {}", percentages);

								for (Short increasedPorts : newFlows.keySet()) {

									for (String changedFlow : newFlows
											.get(increasedPorts)) {

										logger.info("new flow {} in port {}",
												changedFlow, increasedPorts);
										logger.info("from port {}",
												flowToPortHashMap
														.get(changedFlow));

									}

								}

								for (Short increasedPorts : newFlows.keySet()) {

									for (String changedFlow : newFlows
											.get(increasedPorts)) {

										OFMatch ofMatch = null;

										Pattern pattern = Pattern
												.compile("nw_src=([0-9]+.[0-9]+.[0-9]+.[0-9]+)/([0-9]*)");

										Matcher matcher = pattern
												.matcher(changedFlow);

										while (matcher.find()) {
											String fullValue = matcher.group()
													.replaceAll("nw_src=", "");

											String[] ipAndSubnet = fullValue
													.split("/");

											try {
												logger.debug(
														"ip address is {} and subnet is {}",

														matcher.group(1),
														matcher.group(2));

											} catch (IndexOutOfBoundsException ioe) {
												break;
											}
											IPv4Address ipv4Address = new IPv4Address(
													ipAndSubnet[0]);
											ipv4Address.setSubnet(Integer
													.parseInt(ipAndSubnet[1]));
											int ipAddressInt = ipv4Address
													.getIPv4AddressInt();

											IPAddress ipAddress = new IPAddress();
											ipAddress
													.setIpAddressValue(ipAddressInt);
											ipAddress.setSubnet(Integer
													.parseInt(ipAndSubnet[1]));

											short maskingBits = (short) (ipAddress
													.getSubnet());
											int wildCardSource = OFMatch.OFPFW_ALL
													^ OFMatch.OFPFW_DL_TYPE
													^ OFMatch.OFPFW_NW_SRC_ALL
													^ (((maskingBits) - 1) << OFMatch.OFPFW_NW_SRC_SHIFT);

											OFMatch ofMatchSource = new OFMatch();
											ofMatchSource
													.setDataLayerType((short) 0x0800);

											ofMatchSource
													.setNetworkSource(ipAddress
															.getIpAddressValue());

											ofMatchSource
													.setWildcards(wildCardSource);
											ofMatch = ofMatchSource;
											OFFlowMod flowModRule = new OFFlowMod();

											flowModRule
													.setHardTimeout((short) 0);
											flowModRule
													.setIdleTimeout((short) 0);
											flowModRule.setBufferId(-1);
											flowModRule.setMatch(ofMatch);

											OFActionOutput ofAction = new OFActionOutput();
											ofAction.setPort(increasedPorts);
											ArrayList<OFAction> actions = new ArrayList<OFAction>();
											actions.add(ofAction);
											if (mirroredPorts
													.get(increasedPorts) != null) {
												OFActionOutput ofActionMirror = new OFActionOutput();
												ofActionMirror
														.setPort(mirroredPorts
																.get(increasedPorts));
												actions.add(ofActionMirror);
											}

											flowModRule.setActions(actions);

											flowModRule
													.setPriority((short) 100);

											flowMods.add(flowModRule);

											OFMatch ofMatchDest = new OFMatch();
											ofMatchDest
													.setDataLayerType((short) 0x0800);
											ofMatchDest
													.setNetworkDestination(ipAddress
															.getIpAddressValue());

											int wildCardDest = OFMatch.OFPFW_ALL
													^ OFMatch.OFPFW_DL_TYPE
													^ OFMatch.OFPFW_NW_DST_ALL
													^ (((maskingBits) - 1) << OFMatch.OFPFW_NW_DST_SHIFT);

											ofMatchDest
													.setWildcards(wildCardDest);

											OFFlowMod flowModRule1 = new OFFlowMod();

											flowModRule1
													.setHardTimeout((short) 0);
											flowModRule1
													.setIdleTimeout((short) 0);
											flowModRule1.setBufferId(-1);
											flowModRule1.setMatch(ofMatchDest);

											flowModRule1.setActions(actions);

											flowModRule1
													.setPriority((short) 100);

											flowMods.add(flowModRule1);

										}

										pattern = Pattern
												.compile("nw_dst=([0-9]+.[0-9]+.[0-9]+.[0-9]+)/([0-9]*)");

										matcher = pattern.matcher(changedFlow);

										while (matcher.find()) {

											try {
												logger.debug(
														"ip address is {} and subnet is {}",

														matcher.group(1),
														matcher.group(2));

											} catch (IndexOutOfBoundsException ioe) {
												break;
											}
											IPv4Address ipv4Address = new IPv4Address(
													matcher.group(1));
											ipv4Address
													.setSubnet(Integer
															.parseInt(matcher
																	.group(2)));
											int ipAddressInt = ipv4Address
													.getIPv4AddressInt();

											IPAddress ipAddress = new IPAddress();
											ipAddress
													.setIpAddressValue(ipAddressInt);
											ipAddress
													.setSubnet(Integer
															.parseInt(matcher
																	.group(2)));

											short maskingBits = (short) (ipAddress
													.getSubnet());
											int wildCardDestination = OFMatch.OFPFW_ALL
													^ OFMatch.OFPFW_DL_TYPE
													^ OFMatch.OFPFW_NW_DST_ALL
													^ (((maskingBits) - 1) << OFMatch.OFPFW_NW_DST_SHIFT);

											OFMatch ofMatchDestination = new OFMatch();
											ofMatchDestination
													.setDataLayerType((short) 0x0800);

											ofMatchDestination
													.setNetworkDestination(ipAddress
															.getIpAddressValue());

											ofMatchDestination
													.setWildcards(wildCardDestination);
											ofMatch = ofMatchDestination;
											OFFlowMod flowModRule = new OFFlowMod();

											flowModRule
													.setHardTimeout((short) 0);
											flowModRule
													.setIdleTimeout((short) 0);
											flowModRule.setBufferId(-1);
											flowModRule.setMatch(ofMatch);

											OFActionOutput ofAction = new OFActionOutput();
											ofAction.setPort(increasedPorts);
											ArrayList<OFAction> actions = new ArrayList<OFAction>();
											actions.add(ofAction);
											if (mirroredPorts
													.get(increasedPorts) != null) {
												OFActionOutput ofActionMirror = new OFActionOutput();
												ofActionMirror
														.setPort(mirroredPorts
																.get(increasedPorts));
												actions.add(ofActionMirror);
											}
											flowModRule.setActions(actions);

											flowModRule
													.setPriority((short) 100);

											flowMods.add(flowModRule);

											OFMatch ofMatchSrc = new OFMatch();
											ofMatchSrc
													.setDataLayerType((short) 0x0800);
											ofMatchSrc
													.setNetworkSource(ipAddress
															.getIpAddressValue());

											int wildCardSrc = OFMatch.OFPFW_ALL
													^ OFMatch.OFPFW_DL_TYPE
													^ OFMatch.OFPFW_NW_SRC_ALL
													^ (((maskingBits) - 1) << OFMatch.OFPFW_NW_SRC_SHIFT);

											ofMatchSrc
													.setWildcards(wildCardSrc);

											OFFlowMod flowModRule1 = new OFFlowMod();

											flowModRule1
													.setHardTimeout((short) 0);
											flowModRule1
													.setIdleTimeout((short) 0);
											flowModRule1.setBufferId(-1);
											flowModRule1.setMatch(ofMatchSrc);

											flowModRule1.setActions(actions);

											flowModRule1
													.setPriority((short) 100);

											flowMods.add(flowModRule1);

										}

									}

								}

								logger.info(
										"flowscale controller instance in thread is {} ",
										flowscaleController.toString());

							} catch (SQLException sqlE) {

								logger.error("{}", sqlE);

							}

							for (OFFlowMod flowMod : flowMods) {
								logger.info("prior to injection flow is {}",
										flowMod);
							}

							// set percentages for every port;
							logger.info(
									"I REACHED THIS LEVEL!! and flow mod size is {} ",
									flowMods.size());

							flowscaleController.injectFlows(flowMods,
									datapathId);

						}

						try {

							Thread.sleep(intervalTime * 1000);

						} catch (InterruptedException e) {

							logger.error("{}", e);

							break;
						}

					}

				} catch (Exception runException) {
					logger.error(" {}", runException);
				}
			}
		}, "Hot Swapping Thread");
		hotSwappingThread.start();
		}catch(Exception e){
			logger.error("{}",e);
		}

	}

	public void shutDown() {
	}

	/*
	 * @param interval Time in seconds to where the evaluation has to be done
	 * 
	 * @return Arraylist of newly created flowstrings
	 */
	public ArrayList<FlowString> getAllFlows(int interval) {

		ArrayList<FlowString> databaseFlows = new ArrayList<FlowString>();

		// query database

		return databaseFlows;

	}

	/*
	 * iterate over the arraylist, get the sum of packets, then in each flow
	 * divide the numOFPacket value with the sum
	 * 
	 * @param databaseFlows an arraylist of flowstrings whose elements will have
	 * their percentage value added
	 */
	public static void setPercentage(ArrayList<FlowString> databaseFlows) {

	}

	/*
	 * @param porthasmMap a HashMap with each port pointing to an oject that
	 * carries all the FlowString that direct to it databaseFlows an arraylist
	 * with all the FlowString elements retrieved from the database
	 */
	public static void setPorthashMap(
			HashMap<Short, ArrayList<FlowString>> portHashMap,
			ArrayList<FlowString> databaseFlows) {

	}

	public static void offloadFlows(short port,
			ArrayList<String> databaseFlows,
			HashMap<Short, ArrayList<FlowString>> portHashMap) {

	}

	public Short getMinPort(ArrayList<Short> lowPorts,
			HashMap<Short, Double> percentages) {

		if (lowPorts == null) {
			return null;
		}

		double minPercentage = Double.MAX_VALUE;

		Short minPort = null;
		for (Short lowPort : lowPorts) {

			if (percentages.get(lowPort) < this.optimalPercentage
					&& percentages.get(lowPort) < minPercentage) {

				minPercentage = percentages.get(lowPort);
				minPort = lowPort;

			}

		}

		return minPort;

	}

	public String getName() {

		return "flowUpdate";
	}

	private static void removeDownPorts(FlowscaleController flowscaleController, long datapathId, ArrayList<Short> loadedPorts){
		
		for (int i =0; i < flowscaleController.getSwitchDevices().get(datapathId).getPortStates().size(); i++){
			OFPhysicalPort checkedPorts = flowscaleController.getSwitchDevices().get(datapathId).getPortStates().get(i);	

		if (loadedPorts.contains(checkedPorts
				.getPortNumber())) {

			if (checkedPorts.getState() % 2 != 0) {

				loadedPorts.remove(new Short(checkedPorts
						.getPortNumber()));

			}

		}

	}
		
		
	}
	
	private static void getFlowStatsFromDB(ResultSet flowStatRs, ArrayList<Short> loadedPorts, HashMap<String,Short> flowToPortHashMap,
			HashMap<Short,ArrayList<String>>portToFlowHashMap, HashMap<String,Long> flowStat, HashMap<Short,Long> portPacketCount,  Long totalPacketCount){

		while (flowStatRs.next()) {

			String matchString = flowStatRs
					.getString(2);
			String action = flowStatRs.getString(3);
			long packetCount = flowStatRs.getLong(4);

			
			if(!(matchString.contains("nw_src") || matchString.contains("nw_dst"))){
				
				//skip saving of flow if not layer 3
				continue;
			}
			
			Short loadedPort = 0;

			String[] actions = action.split(",");
			try {
				loadedPort = Short
						.parseShort(actions[0]);

			} catch (NumberFormatException nfe) {

				continue;

			}

			if (loadedPorts.contains(loadedPort)) {
				flowToPortHashMap.put(matchString,
						loadedPort);

				// increment here
				ArrayList<String> portFlows = new ArrayList<String>();
				if (portToFlowHashMap.get(loadedPort) != null) {

					portFlows = portToFlowHashMap
							.get(loadedPort);

				} else {
					ArrayList<String> newFlowList = new ArrayList<String>();
					portToFlowHashMap.put(loadedPort,
							newFlowList);
					newFlowList.add(matchString);
				}

				portFlows.add(matchString);

				Long flowOriginalPacketCount;
				if ((flowOriginalPacketCount = flowStat
						.get(matchString)) != null) {
					flowStat.put(matchString,
							flowOriginalPacketCount
									+ packetCount);
				} else {
					flowStat.put(matchString,
							packetCount);
				}
				Long originalPortCount;
				if ((originalPortCount = portPacketCount
						.get(loadedPort)) != null) {

					portPacketCount
							.put(loadedPort,
									(originalPortCount + packetCount));

				} else {

					portPacketCount.put(loadedPort,
							packetCount);

				}

				totalPacketCount += packetCount;

			}

		}
		
		
	}
	
	
}

class FlowPercentComparator implements Comparator<Object> {

	@Override
	public int compare(Object flow1, Object flow2) {

		Double percent1 = ((FlowPercent) flow1).getFlowPercent();
		Double percent2 = ((FlowPercent) flow2).getFlowPercent();

		if (percent1 > percent2) {
			return 1;
		} else if (percent1 < percent2) {
			return -1;
		} else {
			return 0;
		}

	}
}

class HighPortComparator implements Comparator<Object> {

	HashMap<Short, Double> portPercentage;

	public HighPortComparator() {

	}

	public HighPortComparator(HashMap<Short, Double> portPercentage) {
		this.portPercentage = portPercentage;

	}

	@Override
	public int compare(Object port1, Object port2) {

		Double percentage1 = portPercentage.get(((Short) port1));
		Double percentage2 = portPercentage.get(((Short) port2));

		if (percentage1 < percentage2) {
			return 1;
		} else if (percentage1 > percentage2) {
			return -1;
		} else {
			return 0;
		}

	}

}
