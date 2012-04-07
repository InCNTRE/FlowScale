package edu.iu.incntre.flowscale.exception;

public class NoSwitchException extends Exception{
	
	String switchDatapathId ="[undefined switch]";
	
	public NoSwitchException(){
		
	}
	
	public NoSwitchException(String datapathIdString){
		this.switchDatapathId = datapathIdString;
	}
	
	@Override 
	public String toString(){
		return "Switch " +  switchDatapathId + " does not exist";
	}
	

}
