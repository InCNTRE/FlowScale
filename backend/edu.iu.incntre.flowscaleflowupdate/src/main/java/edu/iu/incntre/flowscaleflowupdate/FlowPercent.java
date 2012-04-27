package edu.iu.incntre.flowscaleflowupdate;

public class FlowPercent {

	
<<<<<<< HEAD
	private String flow;
=======
	private String flowString;
>>>>>>> 167d685550cf44c233c925cbdf65cc90592cc953
	private Double flowPercent;
	
	public FlowPercent(String flow, Double flowPercent){
		
		
<<<<<<< HEAD
		this.flow = flow;
=======
		this.flowString = flow;
>>>>>>> 167d685550cf44c233c925cbdf65cc90592cc953
		this.flowPercent = flowPercent;
		
		
		
		
	}
	
	
	public Double getFlowPercent(){
		
		
		return this.flowPercent;
		
	}
	
<<<<<<< HEAD
	public String getFlow(){
		
		return this.flow;
=======
	public String getFlowString(){
		
		return this.flowString;
>>>>>>> 167d685550cf44c233c925cbdf65cc90592cc953
		
		
	}
	
	@Override
	public boolean equals(Object o){
		
		String otherFlow = (String)o;
		
<<<<<<< HEAD
		if(otherFlow.equals(this.flow)){
=======
		if(otherFlow.equals(this.flowString)){
>>>>>>> 167d685550cf44c233c925cbdf65cc90592cc953
			
			return true;
		}
		
		return false;
		
		
		
	}
	
	
	
	
}
