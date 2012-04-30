/** 
 * Copyright 2012 InCNTRE, This file is released under Apache 2.0 license except for component libraries under different licenses
http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.iu.incntre.flowscale;
/** 
 * This class is a utility class fo IPv4 and subnets ,
 * has methods that maybe used on subnets , might consider merging this class with grnoc.net.util.IPv4Address
 * 
 * @author Ali Khalfan (akhalfan@indiana.edu)
 *
 */
public class IPAddressUtility{


/**
 * Increment subnet based on how subnets are to be divided
 * e.g. 192.168.0.0/24 will incremented will be 
 * 192.168.1.0/24  
 * @param ip
 * @param subnet
 * @return the incremented subnet in integer form 
 */
    public static int incrementSubnet(int ip,int subnet){

            int newIP = 0;

            int raiseTo  = (int)Math.pow(2,32-subnet);

            newIP  = ip + raiseTo;

            return newIP;


    }

/** 
 * check if one subnet is in range of the other, useful , for dividing subnet into chunks
 * @see generateIpRules
 * @param ip1
 * @param subnet1
 * @param ip2
 * @param subnet2
 * @return boolean of whether IP in range or not 
 */
    public static boolean checkIfInRange(int ip1,int subnet1,int ip2,int subnet2){

            if(subnet2 < subnet1)
                    return false;

            String ip1String = Integer.toHexString(ip1);
            String ip2String = Integer.toHexString(ip2);

            if(ip1String.substring(0,subnet1).equals(ip2String.substring(0,subnet2)))
                    return true;

            return false;
    }
    
    /**
     * convert int ip value to dotted ip address format 
     * @param ipValue
     * @return
     */
    public static String toIPString(int ipValue){
    	
    	FlowscaleController.logger.info("ip address is {}", ipValue);
    	
    	String a = Integer.toBinaryString(ipValue);
    	String zeroString ="";
    	for(int i = a.length(); i < 32; i++){
    		zeroString += "0";
    	}
    	a = zeroString +a;
    	
    	String ip = "";
    	for(int i = 0 ;i < 4; i++){
    
    		ip = ip +"." + Integer.parseInt(a.substring(i*8, 8*(i+1)),2);
    		
    		
    	}
    	
    	return ip.substring(1);
    	
    }

}