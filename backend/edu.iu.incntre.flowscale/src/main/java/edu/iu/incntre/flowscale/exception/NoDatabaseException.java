/** 
 * Copyright 2012 InCNTRE, This file is released under Apache 2.0 license except for component libraries under different licenses
http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.iu.incntre.flowscale.exception;
/**
 * 
 * @author Ali Khalfan (akhalfan@indiana.edu)
 *
 */
public class NoDatabaseException extends Exception {

	
	@Override 
	public String toString(){
		return "Database not loaded";
	}
}
