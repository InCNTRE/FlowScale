#!/usr/bin/perl

use strict;
use warnings;

use CGI;
use JSON;
use Switch;
use Data::Dumper;

use lib "../lib";

use Flowscale::Database;
use Flowscale::Controller;

my $db = new Flowscale::Database();

my $ctrl  = new Flowscale::Controller;
my $cgi = new CGI;

$| = 1;

sub main {


	if ( !$db ) {
		send_json( { "error" => "Unable to connect to database." } );
		exit(1);
	}

	my $action = $cgi->param('action') || "get_topo";


	my $output; 

	switch ($action) {
		case "get_topo" {
			$output = &get_topo();
		}
		else {
			$output = {
				error => "Unknown action - $action"
			}
		}
	}

	send_json($output);

}
sub get_topo() {

	my $ports;
	my @nodes = ();
	my @edges = ();;
	my $num_inports = 0;
	my $num_outports = 0;
	my $st;
	my $status;

	my ($sw_rrd_str,$out_rrd_str,$in_rrd_str,$lb_rrd_str) = "";
	
        my $switches = $db->get_switches();


        # Loop through each switch
	foreach my $switch (@$switches) {

	  # Get pp status 
	  $ports = $ctrl->get_switch_ports( switch_id => $switch->{'datapath_id'});
	  # By default switch is down
	  $status->{$switch->{"switch_name"}} = 0;
	  if ( $ports ) {
		$status->{$switch->{"switch_name"}} = 1;
		my $ppp = decode_json($ports);
		foreach my $pt (@$ppp) {
		   $status->{$pt->{'port_id'}} = $pt->{'state'};
		}
	     }
	  
	  my $in_ports = $db->get_switch_input_ports( dpid => $switch->{'datapath_id'});
	  if ($in_ports)  {
	     # Add input port box 
	     if ($num_inports == 0) {
		push ( @nodes, { "id" => "Input Ports" });
		$num_inports++;
	     }
	     foreach my $in_port (@$in_ports) {
		if ($status->{$in_port->{"port_id"}}) { $status = $status->{$in_port->{"port_id"}}} 
		else { $st = 2; }

		# Add in port node
		push ( @nodes, { "id" => $in_port->{"port_id"}, "parent" => "Input Ports", 
			          "status" => $st});
		# Add in port edge
		push ( @edges, { "target" => $switch->{"switch_name"}, "source" =>  $in_port->{"port_id"} });
	        # increment in ports
	        my $in_rrd = $switch->{"ip_address"} . "_" . $in_port->{"port_id"} . ".rrd";
	        $in_rrd_str .= $in_rrd; 
	        $num_inports++;
	     }
	}

	 my $out_ports = $db->get_switch_output_ports( dpid => $switch->{'datapath_id'});
          if ($out_ports)  {
             # Add output port box 
             if ($num_outports == 0) {
             	push ( @nodes, { "id" => "Output Ports" });
		$num_outports++;
             }
             foreach my $out_port (@$out_ports) {
		if ($status->{$out_port->{"port_id"}}) { $status = $status->{$out_port->{"port_id"}}} 
                else { $st = 2; }
                # Add out port node
                push ( @nodes, { "id" => $out_port->{"port_id"}, "parent" => "Output Ports", "status" => $st});
		# Add out port edge
		push ( @edges, { "source" => $switch->{"switch_name"}, "target" =>  $out_port->{"port_id"} });

		my $out_rrd = $switch->{"ip_address"} . "_" . $out_port->{"port_id"} . ".rrd:";
                $out_rrd_str .= $out_rrd;
		$num_outports++;
	     }
	  }
	
        $sw_rrd_str = $in_rrd_str . $out_rrd_str;
	$lb_rrd_str .= $sw_rrd_str;
	  
 	# Add switch to nodes
 	push ( @nodes, { "id" => $switch->{"switch_name"},
 	                 "parent" => "Loadbalancers",
 			 "rrd" => $sw_rrd_str, 
 			 "status" => $status->{$switch->{"switch_name"}}});	

	
	}




	    #if any switches exist add Loadbalancers node
	    if ($switches) {
	    	push ( @nodes, { "id" => "Loadbalancers",
				 "rrd" => $lb_rrd_str
				});
	    }



	my $results = { "dataSchema" => { nodes => [ { "name" => "status", "type" => "int" },
						     { "name" => "rrd", "type" => "string" } ] },
			"data" => { "nodes" => \@nodes, "edges" => \@edges}
		      };

        #print "results\n==========\n";
        #print Dumper $results;


	return $results;

}



sub send_json {
	my $output = shift;
	print "Content-type: text/plain\n\n" . encode_json($output);
}

main();


