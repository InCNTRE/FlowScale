package edu.iu.incntre.flowscaleflowupdate;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFType;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionOutput;
import org.openflow.util.U16;

public class PortFlowStats implements Comparable<PortFlowStats>{
/*
    private ArrayList<FlowStats> flowStatsArray = new ArrayList<FlowStats>();
    private boolean overAverage;
    private double totalPercentage;
    
    private short port;
    
    
    public PortFlowStats(short port){
        this.port = port;
        
    }
    
    public short getPort(){
        return port;
    }
    
    public void addFlow(FlowStats flowStats){
        
        flowStatsArray.add(flowStats);
        
    }

    
   public void accumulatePercentageFromFlows(){
       totalPercentage = 0;
       
       for(FlowStats flowStatsInstance: flowStatsArray){
           totalPercentage += flowStatsInstance.getPercentage();
                          
           
           
       }
       
       
   }
   
   public double getTotalPercentage(){
       return totalPercentage;
   }
   public void setTotalPercentage(double totalPercentage){
       
       this.totalPercentage = totalPercentage;
   }
    
   
   public ArrayList<FlowStats> getFlows(){
       return this.flowStatsArray;
   }
   public ArrayList<OFFlowMod> balanceLoad(ArrayList<PortFlowStats> lowLoadPorts, double idealPercentage){
       
       ArrayList<OFFlowMod> updatedFlows = new ArrayList<OFFlowMod>();
       StatCollector.logger.info("starting loadbalancing for port {}", this.port);
      for(PortFlowStats lowPort : lowLoadPorts){ 
     
          ArrayList<FlowStats> flowStatsToRemove = new ArrayList<FlowStats>();
          
       for(FlowStats checkFlow : this.flowStatsArray){
        
         //  StatCollector.logger.info(" check flow percentage {}", checkFlow.getPercentage() );
         
           if(lowPort.getTotalPercentage() >= idealPercentage){
           //    StatCollector.info("ideal percentage of port low port {} reached exiting", lowPort.getPort());
               break;
           }
     //      StatCollector.logger.info("checking if flow {} can be moved with ideal percentage {}", checkFlow.getMatch(),idealPercentage);
       //    StatCollector.logger.info("percentage of flow is {} and lowPort total percentage is {}",checkFlow.getPercentage(),lowPort.getTotalPercentage());
           lowPort.accumulatePercentageFromFlows();
           if(this.getTotalPercentage() <= idealPercentage){
               break;
           }
           if( (checkFlow.getPercentage() <= ( idealPercentage - lowPort.getTotalPercentage() ) )&& (checkFlow.getPercentage() > 0)){
           
              OFFlowMod updateFlow = new OFFlowMod();
              
              updateFlow.setType(OFType.FLOW_MOD);
                updateFlow.setCommand(OFFlowMod.OFPFC_ADD);
                updateFlow.setHardTimeout((short) 0);
                updateFlow.setIdleTimeout((short) 0);
                updateFlow.setMatch(checkFlow.getMatch());
                updateFlow.setBufferId(-1);
                updateFlow.setPriority(checkFlow.getPriority());

                OFActionOutput ofActionOutput = new OFActionOutput();
                ofActionOutput.setPort(lowPort.getPort());
                ArrayList<OFAction> actionList = new ArrayList<OFAction>();
                actionList.add(ofActionOutput);
                updateFlow.setActions(actionList);
                updateFlow.setLength(U16.t(OFFlowMod.MINIMUM_LENGTH
                        + OFActionOutput.MINIMUM_LENGTH));
                
                updatedFlows.add(updateFlow);
                
                lowPort.addFlow(checkFlow);
                lowPort.accumulatePercentageFromFlows();
                flowStatsToRemove.add(checkFlow);
        
           }
           
       }
       for(FlowStats removedFlowStats : flowStatsToRemove ){
          this.flowStatsArray.remove(removedFlowStats);
          
       }
       this.accumulatePercentageFromFlows();
       
      }
       return updatedFlows;    
       
   }
    
    public long getTotalPacketCount(){
        long packetCountSum =0;
        for(FlowStats fs : this.flowStatsArray){
            packetCountSum += fs.getPacketCount();
        }
        return packetCountSum;
    }
    
    public double getTotalAveragePacketCount(){
        double  packetAverageCount =0;
        for(FlowStats fs: this.flowStatsArray){
            packetAverageCount = packetAverageCount += fs.getAveragePacketCount() ;
        }
    
        return packetAverageCount;
    }
    */
    public int compareTo(PortFlowStats portToCompare){
        /*
        if(this.getTotalAveragePacketCount()< portToCompare.getTotalAveragePacketCount()){
            return 1;
        }else if(this.getTotalAveragePacketCount() > portToCompare.getTotalAveragePacketCount()){
            return -1;
        }else{
            return 0;
        }
        */
        return 0;
        
    }
}
