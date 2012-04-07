package edu.iu.incntre.flowscale.exception;

public class NoDatabaseException extends Exception {

	
	@Override 
	public String toString(){
		return "Database not loaded";
	}
}
