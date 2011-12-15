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

my $ctrl = new Flowscale::Controller();

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

		case "delete_xconnect" {
			$output = &delete_xconnect();

		}
		case "add_xconnect" {
			$output = &add_xconnect();
		}
		case "submit_new_switch" {

			$output = &add_new_switch();
		}
		case "edit_switch" {
			$output = &edit_switch();
		}
		case "delete_switch" {
			$output = &delete_switch();
		}
		case "add_group" {
			$output = &submit_new_group();
		}
		case "edit_group" {
			$output = &edit_group();
		}
		case "delete_group" {
			$output = &delete_group();
		}

		else {
			$output = {
				error => "Unknown action - $action"
			};
		}

	}

	send_json($output);

}

sub add_new_switch() {
	my $results;
	my $switch_name = $cgi->param('switch_name');
	my $mac_address = $cgi->param('mac_address');
	my $ip_address  = $cgi->param('ip_address');
	my $datapath_id = $cgi->param('datapath_id');

	my $result = $db->add_new_switch(
		datapath_id => $datapath_id,
		switch_name => $switch_name,
		mac_address => $mac_address,
		ip_address  => $ip_address,
	);

	if ( !defined $result ) {
		$results->{'error'} = $db->get_error();

	}
	else {

		$results->{'results'} = $result;
		my $ctrl_results = $ctrl->add_switch(
		datapath_id => $datapath_id,
		switch_name => $switch_name,
		mac_address => $mac_address,
		ip_address  => $ip_address);
	}

	#use module to add ports
	#my $port_result = $sw->get_switch_ports(ip_address = $ip_address);

	#my $insert_port_result = $db->add_new_port(ports => $port_result);

	# $results->{'port_result'} = $port_result;

	return $results;

}

sub edit_switch() {
	my $results;
	my $switch_name = $cgi->param('switch_name');
	my $mac_address = $cgi->param('mac_address');
	my $ip_address  = $cgi->param('ip_address');
	my $datapath_id = $cgi->param('datapath_id');

	my $result = $db->edit_switch(
		datapath_id => $datapath_id,
		switch_name => $switch_name,
		mac_address => $mac_address,
		ip_address  => $ip_address,
	);

	if ( !defined $result ) {
		$results->{'error'} = $db->get_error();

	}
	else {

		$results->{'results'} = $result;

	}

	#    my $port_result = $sw->get_switch_ports(ip_address = $ip_address);

	# my $insert_port_result = $db->add_new_port(ports => $port_result);

	# $results->{'port_result'} = $port_result;

	return $results;

}

sub delete_switch() {

	my $results;

	my $switch_id = $cgi->param('switch_id');

	my $result = $db->delete_switch( switch_id => $switch_id );

	if ( !defined $result ) {
		$results->{'error'} = $db->get_error();

	}
	else {

		$results->{'results'} = $result;

	}
	return $results;

}

sub submit_new_group() {
	my $results;

	my $group_name    = $cgi->param('group_name');
	my $input_switch  = $cgi->param('input_switch');
	my $output_switch = $cgi->param('output_switch');
	my $group_type    = $cgi->param('group_type');
	my $priority      = $cgi->param('priority');
	my $group_values  = $cgi->param('group_values');
	my $input_ports   = $cgi->param('input_ports');
	my $output_ports  = $cgi->param('output_ports');
	my $maximum_allowed = $cgi->param('maximum_allowed');


	my $result = $db->add_new_group(
		group_name    => $group_name,
		input_switch  => $input_switch,
		output_switch => $output_switch,
		group_type    => $group_type,
		priority      => $priority,
		group_values  => $group_values,
		input_ports   => $input_ports,
		output_ports  => $output_ports,
		maximum_allowed => $maximum_allowed,
	);
	if ( !defined $result ) {
		$results->{'error'} = $db->get_error();

	}
	else {
		$results->{'results'} = $result;
	my $group_id = $db->get_transaction_id();
	
		my $ctrl_result = $ctrl->add_new_group(	
		group_name    => $group_name,
		input_switch  => $input_switch,
		output_switch => $output_switch,
		group_type    => $group_type,
		priority      => $priority,
		group_values  => $group_values,
		input_ports   => $input_ports,
		output_ports  => $output_ports,
		maximum_allowed => $maximum_allowed,
		group_id => $group_id,
		);
		
		$results->{'switch_result'} = $ctrl_result;

	}
	return $results;

}

sub edit_group() {
	my $results;
	my $group_id      = $cgi->param('group_id');
	my $input_switch  = $cgi->param('input_switch_id');
	my $output_switch = $cgi->param('output_switch_id');
	my $group_type    = $cgi->param('group_type');
	my $priority      = $cgi->param('priority');
	my $group_values  = $cgi->param('group_values');
	my $input_ports   = $cgi->param('input_ports');
	my $output_ports  = $cgi->param('output_ports');

	my @original_input_ports =
	  split( ',', $cgi->param('original_input_ports') );
	my @original_output_ports =
	  split( ',', $cgi->param('original_output_ports') );
	my @original_group_values =
	  split( ',', $cgi->param('original_group_values') );

	my @input_ports_array  = split( ',', $input_ports );
	my @output_ports_array = split( ',', $output_ports );
	my @group_values_array = split( ',', $group_values );

	my $values_changed       = 0;
	my $input_ports_changed  = 0;
	my $output_ports_changed = 0;

	my $commands = [];

	foreach my $val (@original_input_ports) {
		if ( !grep( /\b$val\b/, @input_ports_array ) ) {
			$input_ports_changed = 1;

			push( @$commands, "DELETE IPORT " . $val );

		}

	}
	foreach my $val (@input_ports_array) {
		if ( !grep( /\b$val\b/, @original_input_ports ) ) {

			#an input port is added
			$input_ports_changed = 1;
			push( @$commands, "ADD IPORT " . $val );
		}

	}

	foreach my $val (@original_output_ports) {
		if ( !grep( /\b$val\b/, @output_ports_array ) ) {

			#an input port is deleted
			$output_ports_changed = 1;
			push( @$commands, "DELETE OPORT " . $val );
		}

	}

	foreach my $val (@output_ports_array) {
		if ( !grep( /\b$val\b/, @original_output_ports ) ) {

			$output_ports_changed = 1;
			push( @$commands, "ADD OPORT " . $val );
		}

	}

	foreach my $val (@original_group_values) {
		if ( !grep( /\b$val\b/, @group_values_array ) ) {

			$values_changed = 1;
			push( @$commands, "DELETE VALUE " . $val );

		}

	}

	foreach my $val (@group_values_array) {
		if ( !grep( /\b$val\b/, @original_group_values ) ) {
			$values_changed = 1;
			push( @$commands, "ADD VALUE " . $val );

		}

	}

	if ( $values_changed eq 1 && $output_ports_changed eq 1 ) {

		return $results->{'error'} = "can only update one thing at a time";
	}

	my $controller_results = $ctrl->edit_group(

		group_id => $group_id,
		commands => $commands
	);

	#check if priority was updated

	my $result = $db->edit_group(
		group_id      => $group_id,
		input_switch  => $input_switch,
		output_switch => $output_switch,
		group_type    => $group_type,
		priority      => $priority,
		commands      => $commands

	);

	if ( !defined $result ) {
		$results->{'error'} = $db->get_error();

	}
	else {

		$results->{'results'} = $result;

	}
	return $results;

}

sub delete_group() {

	my $results;

	my $group_id = $cgi->param('group_id');

	my $result = $db->delete_group( group_id => $group_id );

	$results->{'results'} = $result;

	if ( !defined $result ) {
		$results->{'error'} = $db->get_error();

	}
	else {

		$results->{'results'} = $result;

		my $sw_result = $ctrl->delete_group( group_id => $group_id );

	}
	return $results;
}

sub delete_xconnect {

	my $results;

	my $switch_1_id = $cgi->param('switch_1_id');
	my $switch_2_id = $cgi->param('switch_2_id');

	my $result = $db->delete_xconnect(
		switch_1_id => $switch_1_id,
		switch_2_id => $switch_2_id,
	);

	$results->{'results'} = $result;

	return $results;
}

sub add_xconnect {

	my $results;

	my $switch_1_id = $cgi->param('switch_1_id');
	my $switch_2_id = $cgi->param('switch_2_id');
	my $port1       = $cgi->param('port1');
	my $port2       = $cgi->param('port2');

	my $result = $db->add_xconnect(
		switch_1_id => $switch_1_id,
		switch_2_id => $switch_2_id,
		port1       => $port1,
		port2       => $port2
	);

	if ( !defined $results ) {
		$results->{'error'} = $db->get_error();

	}
	else {

		$results->{'results'} = $result;

	}

	return $results;

}

sub send_json {
	my $output = shift;

	print "Content-type: text/plain\n\n" . encode_json($output);
}

main();

