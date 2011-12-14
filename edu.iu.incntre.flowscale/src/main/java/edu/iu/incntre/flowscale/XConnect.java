package edu.iu.incntre.flowscale;

public class XConnect {
    
    private long inputSwitch;
    private long outputSwitch;
    
    private int inputPortNum;
    private int outputPortNum;
    
    
    public XConnect(){
        
        
        
        
    }


    public long getInputSwitch() {
        return inputSwitch;
    }


    public void setInputSwitch(long inputSwitch) {
        this.inputSwitch = inputSwitch;
    }


    public long getOutputSwitch() {
        return outputSwitch;
    }


    public void setOutputSwitch(long outputSwitch) {
        this.outputSwitch = outputSwitch;
    }


    public int getInputPortNum() {
        return inputPortNum;
    }


    public void setInputPortNum(int inputPortNum) {
        this.inputPortNum = inputPortNum;
    }


    public int getOutputPortNum() {
        return outputPortNum;
    }


    public void setOutputPortNum(int outputPortNum) {
        this.outputPortNum = outputPortNum;
    }
    
    @Override
    public boolean equals(Object obj){
        
        XConnect compared = (XConnect)obj;
        
        if (compared.getInputSwitch() == this.inputSwitch && compared.getOutputSwitch() == this.outputSwitch  &&
                compared.getInputPortNum() == this.inputPortNum && compared.getOutputPortNum() == this.outputPortNum){
            
            
           return true;
            
        }
        
        return false;
        
    }
    

}
