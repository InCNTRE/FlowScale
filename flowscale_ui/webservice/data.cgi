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

	my $action = $cgi->param('action');

	my $output;

	switch ($action) {

		case "get_current__circuits" {
			$output = &get_current_switches();
		}

		case "group_values" {

			$output = &get_group_values();

		}
		case "xconnect_values" {

			$output = &get_xconnect_values();

		}
case "get_switch_ports"{
	$output = &get_switch_ports();	
	
}
		case "group_input_ports" {
			$output = &get_group_input_ports();
		}
		case "group_output_ports" {
			$output = &get_group_output_ports();

		}

		case "get_switch_status" {
			$output = &get_switch_status();
		}
		case "get_switch_statistics" {

			$output = &get_switch_statistics();
		}

		else {
			$output = {
				error => "Unknown action - $action"
			}
		}
	}

	send_json($output);

}
sub get_current_switches() {

	my $results;
	my $switches = $db->get_switches();

	if ( !defined $switches ) {
		$results->{'error'} = $db->get_error();
	}
	else {
		$results->{'results'} = $switches;
	}

	return $results;

}

sub get_group_values {

	my $results;

	my $group_values =
	  $db->get_group_values( group_id => $cgi->param('group_id') );

	# something went wrong
	if ( !defined $group_values ) {

		$results->{'error'} = $db->get_error();
	}
	else {
		$results->{'results'} = $group_values;
	}

	return $results;
}


sub get_group_input_ports {
	
	my $results;

	my $group_values =
	  $db->get_group_input_ports( group_id => $cgi->param('group_id') );

	# something went wrong
	if ( !defined $group_values ) {

		$results->{'error'} = $db->get_error();
	}
	else {
		$results->{'results'} = $group_values;
	}

	return $results;
	
	
	
}


sub get_group_output_ports {
	
	my $results;

	my $group_values =
	  $db->get_group_output_ports( group_id => $cgi->param('group_id') );

	# something went wrong
	if ( !defined $group_values ) {

		$results->{'error'} = $db->get_error();
	}
	else {
		$results->{'results'} = $group_values;
	}

	return $results;
	
	
	
}



sub get_switch_ports {

	my $results;
	my $ports;

	my $switch_id = $cgi->param('switch_id');

	$ports = $ctrl->get_switch_ports( switch_id => $cgi->param('switch_id') );

	if ( !defined $ports ) {

		$results->{'error'} = $db->get_error();
	}
	else {
		$results->{'results'} = decode_json($ports);
	}

	return $results;

}

sub get_xconnect_values {

	my $results;
	my $xconnect_values;

	my $switch_id = $cgi->param('switch_id');

	if ( defined $switch_id ) {
		$xconnect_values =
		  $db->get_xconnect( switch_id => $cgi->param('switch_id') );

	}
	else {
		$xconnect_values = $db->get_all_xconnect();
	}

	if ( !defined $xconnect_values ) {

		$results->{'error'} = $db->get_error();
	}
	else {
		$results->{'results'} = $xconnect_values;
	}

	return $results;

}



sub get_switch_statistics {


	my $results;

	my $result = $ctrl->get_switch_statistics(switch_id => $cgi->param('switch_id') , type => $cgi->param('type'));

	if ( !defined $result ) {
		$results->{'error'} = $ctrl->get_error();
	}
	else {
		$results->{'results'} = decode_json($result);
	}

	return $results;

}

sub get_switch_status {

	my $results;
	my $ports = $ctrl->get_switch_status();
	if ( !defined $ports ) {
		$results->{'error'} = $ctrl->get_error();;
	}
	else {
		$results->{'results'} = decode_json($ports);

	}

	return $results;

}

sub send_json {
	my $output = shift;
	print "Content-type: text/plain\n\n" . encode_json($output);
}

main();

