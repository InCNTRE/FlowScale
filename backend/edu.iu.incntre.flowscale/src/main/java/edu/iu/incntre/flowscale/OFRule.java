/** 
 * Copyright 2012 InCNTRE, This file is released under Apache 2.0 license except for component libraries under different licenses
http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.iu.incntre.flowscale;

/**
 * This class is an abstraction for an openflow rule that will be inserted to the switch, this class is associated 
 * to group on a one-to-many basis
 * @author Ali Khalfan (akhalfan@indiana.edu)
 */

import java.util.ArrayList;
import java.util.List;

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
    /**
     * To facilitating mirroring, this method adds the specified mirroring port to the rule
     * (see documentation on how mirroring works)
     * @param port
     */
    public void setMirrorPort(int port){
    	OFActionOutput mirrorAction = new OFActionOutput();
    	mirrorAction.setPort((short)port);
    	actions.add(mirrorAction);
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

    public void setActions(List<OFAction> ofActions){
    	
    	actions = (ArrayList<OFAction>)ofActions;
    	
    }
    
    public short getPriority() {
        return priority;
    }

    public void setPriority(short priority) {
        this.priority = priority;
    }
    
    
    
}






