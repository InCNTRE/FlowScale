package edu.iu.incntre.flowscale;

public class SwitchPort {

    String macAddress;
    int status;
    public String getMacAddress() {
        return macAddress;
    }
    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }
    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }
    public SwitchPort(String macAddress, int status) {
        super();
        this.macAddress = macAddress;
        this.status = status;
    }
    
    
    
    
}
