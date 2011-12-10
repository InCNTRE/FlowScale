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

        my $results;
	my $ports;
	my @nodes;
	my %node;
	my @in_ports;
        my @out_ports;
	
        my $switches = $db->get_switches();

	# Add nodes for FlowScale switches
	$node{'id'} = "Loadbalancers";
	push (@nodes,%node);


        # Add Input and Output Ports         
	my $groups = $db->get_groups();
	print "groups\n==========\n";
	print Dumper $groups;

        if ( defined $groups ) { 
	  # Input ports
    	  $node{'id'} = "Input Ports";
          push (@nodes,%node);

	  $node{'id'} = "Output Ports";
          push (@nodes,%node);

          foreach my $gp ($groups->[0]) {
	   foreach my $gp2 (@$gp) {
	      @in_ports = ( @in_ports, $gp2->{'input_ports'} );
	      @out_ports = ( @out_ports, $gp2->{'output_ports'} );
	  }
         }
	}	

        foreach my $in_pt (@in_ports) {
	    print Dumper $in_pt;
	}

	foreach my $out_pt (@out_ports) {
            print Dumper $out_pt;
        }

        print "ports\n==========\n";	


	foreach my $sw (@$switches) {
	     $ports = $ctrl->get_switch_ports( switch_id => $sw->{'datapath_id'});
	     if ( $ports ) {
		my $ppp = decode_json($ports);
		foreach my $pt (@$ppp) {
		#	print "$sw->{'switch_name'} : $pt->{'port_id'} : $pt->{'state'}\n";
		}
	     }
	}	

	print "nodes\n==========\n";
	$results->{'nodes'} = @nodes;

        print Dumper @nodes;

	return $results;

}



sub send_json {
	my $output = shift;
	print "Content-type: text/plain\n\n" . encode_json($output);
}

main();


