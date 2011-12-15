package edu.iu.incntre.flowscale;

import java.util.ArrayList;

import org.openflow.protocol.OFMatch;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionOutput;

public class OFRule {

    OFMatch match ;
    short priority;
    ArrayList<OFAction> actions = new ArrayList<OFAction>();
    long wildcards;
    
    public long getWildcards() {
        return wildcards;
    }

    public void setWildcards(long wildcards) {
        this.wildcards = wildcards;
    }

    public OFRule(){
        
    }

    public OFMatch getMatch() {
        return match;
    }

    public void setMatch(OFMatch match) {
        this.match = match;
    }

    public ArrayList<OFAction> getActions() {
        return actions;
    }

    public void setPort(int port) {
        
        if (port == -1){
            actions = null;
            return;
        }
        
        OFActionOutput action = new OFActionOutput();
       action.setPort((short)port);
       actions.add(action);
      
    }

    public short getPriority() {
        return priority;
    }

    public void setPriority(short priority) {
        this.priority = priority;
    }
    
    
    
}






