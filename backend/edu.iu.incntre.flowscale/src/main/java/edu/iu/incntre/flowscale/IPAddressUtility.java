package edu.iu.incntre.flowscale;

import org.openflow.util.HexString;

public class IPAddressUtility{



    public static int incrementSubnet(int ip,int subnet){

            int newIP = 0;

            int raiseTo  = (int)Math.pow(2,32-subnet);

            newIP  = ip + raiseTo;

            return newIP;


    }

  /*  public static void main(String []args){

            int ip1 = Integer.parseInt(args[0]);
            int subnet1 = Integer.parseInt(args[1]);
            int ip2 = Integer.parseInt(args[2]);
            int subnet2 = Integer.parseInt(args[3]);

            int newIP = IPAddress.incrementSubnet(ip1,subnet1);
            System.out.println(newIP);
    }
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