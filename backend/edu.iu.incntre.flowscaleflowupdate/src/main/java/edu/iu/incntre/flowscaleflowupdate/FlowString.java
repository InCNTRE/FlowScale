package edu.iu.incntre.flowscaleflowupdate;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.openflow.protocol.OFMatch;

import edu.iu.incntre.flowscale.OFRule;

public class FlowString {

    private long [] packetCount ;
    private OFMatch match;
    private double percentage; 
    private  double averagePacketCount;
  
    private short priority  = 0;
    public OFMatch getMatch(){
        return match;
    }
    
    public FlowString(int numberOfIterations){
        packetCount = new long[numberOfIterations];
    }
    public void setMatch(OFMatch match){
        this.match = match;
    }

    public void setPriority(short priority){
        this.priority = priority;
    }
    
    public short getPriority(){
        return priority;
    }
    
    @Override
    public boolean equals(Object o){
     
        FlowString comparedFlowStats = (FlowString)o;
        
        if(this.match.equals(comparedFlowStats.getMatch())){
            return true;
        }
        
        return false;
    }
    
    public void setPacketCount (long packetCount, int cycleIteration){
        if(cycleIteration == 0)
            this.packetCount[cycleIteration] = packetCount;
        else
            this.packetCount[cycleIteration] = packetCount ;
        
    
        
            
    }
    
    public double getAveragePacketCount(){
        long packetCountSum =0;
    for(int i=1 ; i < packetCount.length ; i++){
            
            packetCountSum = packetCountSum + (packetCount[i] - packetCount[i-1]);
            
        }
    
     
        
        averagePacketCount = packetCountSum/(packetCount.length -1);
        return averagePacketCount;
    }
    
    public long getPacketCount(){
        long packetCountSum =0;
   for(int i=1 ; i < packetCount.length ; i++){
            
            packetCountSum = packetCountSum + (packetCount[i] - packetCount[i-1]);
            
        }       
    
    return packetCountSum;
    }
    
    
  public void setPercentage(double totalAveragePacketCount){
        this.percentage = (double)(averagePacketCount/ totalAveragePacketCount* 100 );
       
        
    }
  
  public double getPercentage(){
      return this.percentage;
  }
  
  public String getPacketCountArray(){
      return this.packetCount[0] +","+this.packetCount[1] +","+this.packetCount[2] +","+this.packetCount[3]+","+this.packetCount[4];
  }
    
}
