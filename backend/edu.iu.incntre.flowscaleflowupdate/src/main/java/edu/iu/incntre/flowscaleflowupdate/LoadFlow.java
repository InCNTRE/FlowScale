package edu.iu.incntre.flowscaleflowupdate;

import java.util.HashSet;

public class LoadFlow implements Comparable{

	private String flowString;
	private double flowPercent = 0f;
	private short loadedPort;
	private long packetCount = 0;
	private short priroity= 0;

	public LoadFlow(String flowString) {

		this.flowString = flowString;

	}

	public LoadFlow(String flowString, short loadedPort, short priority) {
		this.flowString = flowString;
		this.loadedPort = loadedPort;
		this.priroity = priority;
	}

	public Double getFlowPercent() {

		return this.flowPercent;

	}
	public short getPriority(){
		return this.priroity;
	}

	public void setFlowPercent(double flowPercent) {
		this.flowPercent = flowPercent;
	}

	public String getFlowString() {

		return this.flowString;

	}

	public short getLoadedPort() {
		return this.loadedPort;
	}

	public void setLoadedPort(short loadedPort) {
		this.loadedPort = loadedPort;
	}

	public void setPacketCount(long packetCount) {

		this.packetCount = packetCount;

	}

	public long getPacketCount() {
		return this.packetCount;
	}

	@Override
	public boolean equals(Object otherObject) {

		if (!(otherObject instanceof LoadFlow)) {
			return false;
		}

		LoadFlow otherObjectLoadFlow = (LoadFlow) otherObject;

		if (otherObjectLoadFlow.getFlowString().equals(this.flowString)) {
			return true;
		} else {
			return false;
		}

	}

	@Override
	public String toString() {

		return this.flowString + " " + this.loadedPort;
	}

	@Override
	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		LoadFlow b = (LoadFlow)arg0;
		return this.getFlowString().compareTo(b.getFlowString());
	}

}
