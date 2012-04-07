package edu.iu.incntre.flowscaleflowupdate;

public class FlowPercent {

	
	private String flow;
	private Double flowPercent;
	
	public FlowPercent(String flow, Double flowPercent){
		
		
		this.flow = flow;
		this.flowPercent = flowPercent;
		
		
		
		
	}
	
	
	public Double getFlowPercent(){
		
		
		return this.flowPercent;
		
	}
	
	public String getFlow(){
		
		return this.flow;
		
		
	}
	
	@Override
	public boolean equals(Object o){
		
		String otherFlow = (String)o;
		
		if(otherFlow.equals(this.flow)){
			
			return true;
		}
		
		return false;
		
		
		
	}
	
	
	
	
}
