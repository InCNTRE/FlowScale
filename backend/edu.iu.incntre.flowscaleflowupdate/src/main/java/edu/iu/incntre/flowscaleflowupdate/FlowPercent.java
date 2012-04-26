package edu.iu.incntre.flowscaleflowupdate;

public class FlowPercent {

	
	private String flowString;
	private Double flowPercent;
	
	public FlowPercent(String flow, Double flowPercent){
		
		
		this.flowString = flow;
		this.flowPercent = flowPercent;
		
		
		
		
	}
	
	
	public Double getFlowPercent(){
		
		
		return this.flowPercent;
		
	}
	
	public String getFlowString(){
		
		return this.flowString;
		
		
	}
	
	@Override
	public boolean equals(Object o){
		
		String otherFlow = (String)o;
		
		if(otherFlow.equals(this.flowString)){
			
			return true;
		}
		
		return false;
		
		
		
	}
	
	
	
	
}
