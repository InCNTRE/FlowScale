package grnoc.net.util.ipaddress;



public class IPv4Address {

    private int subnet;
    private int ipv4Address;
    private StringBuffer ipv4StringBuffer = new StringBuffer();
    public IPv4Address(int ipv4Address){

            this.ipv4Address = ipv4Address;
    }

    public IPv4Address(String stringAddress){
            //convert to ipv4 int address
            byte[] ipByteParts = new byte[4];
         
            String [] ipPart = stringAddress.split("\\.");
            int i =0;
            Short tempShort = 0;
            for(String ipString : ipPart){
                  
                    tempShort = Short.parseShort(ipString);
                    byte b = (byte) (0xFF & tempShort) ;
                    if (tempShort > 128)
                            b = (byte)(b | 0x80);
                    ipByteParts[i++] = b;
            }


            int intValue = 0;
            for ( i = 0; i < 4; i++) {

                    intValue = (intValue << 8);

                    intValue = (intValue | (0xFF & ipByteParts[i]));

            }
            this.ipv4Address = intValue;
  
    }

    public int getIPv4AddressInt(){
            return this.ipv4Address;
    }

    public void setSubnet(int subnet){
            this.subnet = subnet;
    }
    
    public int getSubnet(){
    	return this.subnet;
    }


    public boolean equals(Object object){
    		IPv4Address ipv4AddressObject =  (IPv4Address) object;
            int ipv4AddressValue = ipv4AddressObject.getIPv4AddressInt();
            int subnetValue = ipv4AddressObject.getSubnet();
            ipv4AddressValue = ipv4AddressValue >>> (32-subnetValue);

            int ipv4AddressShifted = this.ipv4Address >>> (32-subnetValue);
      
            if(ipv4AddressShifted == ipv4AddressValue){
                    return true;
            }
            else{
                    return false;
            }

    }
    public String getIPv4AddressValue(){
        String ipv4AddressValue ="";
        int tempValue =0 ;
        for(int i =0 ; i < 4; i++){

                 tempValue =  (this.ipv4Address >>> ((4-i-1)*8) ) & 0xFF;

                if(tempValue < 0)
                        tempValue = tempValue & 0xFF;
                ipv4AddressValue += tempValue +".";

        }
        return (ipv4AddressValue.substring(0,ipv4AddressValue.length() -1 ));

}


}
