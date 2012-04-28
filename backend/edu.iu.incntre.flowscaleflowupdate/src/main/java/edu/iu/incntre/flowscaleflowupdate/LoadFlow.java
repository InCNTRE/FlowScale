package edu.iu.incntre.flowscaleflowupdate;

import java.util.HashSet;

public class LoadFlow {

	
	private String flowString;
	private double flowPercent = 0f;
	private short loadedPort;
	private long packetCount = 0;

	public LoadFlow(String flowString){
		
		
		this.flowString = flowString;
		
		
	}
	public LoadFlow(String flowString, short loadedPort){
		this.flowString = flowString;
		this.loadedPort = loadedPort;
	}
	
	
	public Double getFlowPercent(){
		
		
		return this.flowPercent;
		
	}
	
	public void setFlowPercent(double flowPercent){
		this.flowPercent = flowPercent;
	}
	
	public String getFlowString(){
		
		return this.flowString;
		
		
	}
	public short getLoadedPort(){
		return this.loadedPort;
	}
	
	public void setLoadedPort(short loadedPort){
		this.loadedPort = loadedPort;
	}
	
	public void setPacketCount(long packetCount){
		
		this.packetCount = packetCount;
		
	}
	
	public long getPacketCount(){
		return this.packetCount;
	}
	
	
	@Override
	public boolean equals(Object otherObject){
		
		if (!(otherObject instanceof LoadFlow)){
			return false;
		}
		
		LoadFlow otherObjectLoadFlow =  (LoadFlow)otherObject;
		
		if(otherObjectLoadFlow.getFlowString().equals(this.flowString)  && this.loadedPort == otherObjectLoadFlow.getLoadedPort()){
			return true;
		}else {
			return false;
		}
		
		
		
	}
	@Override
	public String toString(){
		
		return this.flowString +" "+this.loadedPort;
	}
	
}
