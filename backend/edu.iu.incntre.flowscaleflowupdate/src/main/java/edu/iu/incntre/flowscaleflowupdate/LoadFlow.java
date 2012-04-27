package edu.iu.incntre.flowscaleflowupdate;

import java.util.HashSet;

public class LoadFlow {

	
	private String flowString;
	private double flowPercent;
	private short loadedPort;
	private long packetCount;

	public LoadFlow(String flowString){
		
		
		this.flowString = flowString;
		
		
	}
	public LoadFlow(String flowString, short loadedPort){
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
		
		if (!(otherObject instanceof String)){
			return false;
		}
		
		String otherObjectString =  (String)otherObject;
		
		if(otherObjectString.equals(this.flowPercent)){
			return true;
		}else {
			return false;
		}
		
		
		
	}
	
	
}
