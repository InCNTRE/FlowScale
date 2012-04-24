package edu.iu.incntre.flowscaleflowupdate;

import java.util.LinkedHashSet;

public class LoadFlow {

	
	private String flowString;
	private Double flowPercent;
	private LinkedHashSet<Short> ports;
	public LoadFlow(String flowString){
		
		
		this.flowString = flowString;
		
		
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
	
	@Override
	public boolean equals(Object o){
	
		if(o instanceof String){
		
		String otherFlow = (String)o;
		
		if(otherFlow.equals(this.flowString)){
			
			return true;
		}
		
		return false;
		
		}else if ( o instanceof Short ){
			Short seekedPort =  (Short)o;
			return ports.contains(seekedPort);
						
			
		}
		
		return false;
	}
	
	
	
	
}
