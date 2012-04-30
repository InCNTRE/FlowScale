/** 
 * Copyright 2012 InCNTRE, This file is released under Apache 2.0 license except for component libraries under different licenses
http://www.apache.org/licenses/LICENSE-2.0
 */
package edu.iu.incntre.flowscale;

/**
 * This class is an abstraction for IPAddress and subnet 
 * might need to integrate grnoc.net.util.IPv4Address
 * @author Ali Khalfan (akhalfan@indiana.edu)
 *
 */
public class IPAddress {

    
    int ipAddressValue;
    int subnet;
    /** 
     * 
     * @return int value of IpAddress
     */
    public int getIpAddressValue() {
        return ipAddressValue;
    }
    public void setIpAddressValue(int ipAddressValue) {
        this.ipAddressValue = ipAddressValue;
    }
    public int getSubnet() {
        return subnet;
    }
    public void setSubnet(int subnet) {
        this.subnet = subnet;
    }
    
    
    
    
}
