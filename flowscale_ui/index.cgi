#!/usr/bin/perl

#---------
# This is the main workhorse service for serving up all the template toolkit
# pages that serve the OE-SS frontend. There is nothing particularly special in here,
# it just accepts what page the user wants to see and returns it templatized.
#---------

use strict;
use warnings;

use CGI;
use Template;
use Switch;
use JSON;

use lib "lib";

use Flowscale::Database;
use Flowscale::Controller;
use Flowscale::Graph;

my $db = new Flowscale::Database();
my $ctrl = new Flowscale::Controller();

my $graph = new Flowscale::Graph();

my $ADD_BREADCRUMBS = [
	{ title => "FlowScale",         url => "?action=index" },
	{ title => "Add Group",    url => "?action=add_group" },
	{ title => "Edit Group",   url => "?action=edit_group" },
	{ title => "Add Switch",   url => "?action=add_switch" },
	{ title => "Primary Path", url => "?action=primary_path" },
	{ title => "Backup Path",  url => "?action=backup_path" },
	{ title => "Scheduling",   url => "?action=scheduling" },
	{ title => "Provisioning", url => "?action=provisioning" },
];

my $REMOVE_BREADCRUMBS = [
	{ title => "FlowScale",         url => "?action=index" },
	{ title => "Scheduling",   url => "?action=remove_scheduling" },
	{ title => "Provisioning", url => "?action=provisioning" },
];

my $HOME_BREADCRUMBS = [ { title => "Home", url => "?action=index" } ];

my $DETAILS_BREADCRUMBS = [
	{ title => "Home",            url => "?action=index" },
	{ title => "Circuit Details", url => "?action=view_details" }
];

my $ADMIN_BREADCRUMBS = [
	{ title => "Home",  url => "?action=index" },
	{ title => "Admin", url => "?action=admin" }
];

sub main {

	my $cgi = new CGI;

	my $tt =
	  Template->new( INCLUDE_PATH =>
		  "./" )
	  || die $Template::ERROR;

	#-- What to pass to the TT and what http headers to send
	my ( $vars, $output, $filename, $title, $breadcrumbs, $current_breadcrumb,
		$content );

   #-- Figure out what we're trying to templatize here or default to index page.
	my $action = "index";

	if ( $cgi->param('action') =~ /^(\w+)$/ ) {
		$action = $1;

	}

	$content = "";

	switch ($action) {

		case "index" {

			$filename           = "html_pages/flowscale3.html";
			$title              = "FlowScale";
			$breadcrumbs        = $HOME_BREADCRUMBS;
			$current_breadcrumb = "FlowScale";
		}
		case "rest" {

			$filename = "html_pages/index.html";
			$content  = decode_json( $ctrl->get_gibberish() );

		}

		case "group" {
			$filename           = "html_pages/group.html";
			$title              = "Groups Panel";
			$breadcrumbs        = $HOME_BREADCRUMBS;
			$current_breadcrumb = "Groups Details";
			$content            = $db->get_groups();
		}

		case "x_connect" {
			$filename           = "html_pages/x_connect.html";
			$title              = "X-Connect Panel";
			$breadcrumbs        = $HOME_BREADCRUMBS;
			$current_breadcrumb = "Configure X-Connect";
			$content            = $db->get_switches();
		}
		case "switch_status" {
			$filename           = "html_pages/switches_status.html";
			$title              = "Switches Status";
			$breadcrumbs        = $HOME_BREADCRUMBS;
			$current_breadcrumb = "Switches Status";
			$content            = $db->get_switches();

		}
		case "switch_graph"{
			$filename = "html_pages/switch_graph.html";
#			$title ="Switch Graph";
			$title = $cgi->param('switch_selection');
			$breadcrumbs = $HOME_BREADCRUMBS;
			$current_breadcrumb = "Switch status";
		#	$content = $graph->get_switch_ports(device => $cgi->param('switch_selection'));
			$content = $graph->get_graph(datapath_id => $cgi->param('switch_selection') , time1 => $cgi->param('date_range_1') , time2 => $cgi->param('date_range_2')  ) ;
		}

		case "switch_graph_agg"{
                        $filename = "html_pages/switch_graph_out.html";
                        $title ="Switch Graph Output";
                        $breadcrumbs = $HOME_BREADCRUMBS;
                        $current_breadcrumb = "Switch status Out";
                     #   $content = $graph->get_switch_ports();
                }

		case "statistics_by_switch" {

			$filename           = "html_pages/switch_statistics_cyto.html";
			$title              = "Switches Statistics";
			$breadcrumbs        = $HOME_BREADCRUMBS;
			$current_breadcrumb = "Switches Statistics";
			$content 			= $db->get_switches();

		}

		case "help" {
			$filename           = "html_pages/help.html";
			$title              = "Help Page";
			$breadcrumbs        = $ADD_BREADCRUMBS;
			$current_breadcrumb = "Help Page";
		}

		case "switch" {
			$filename           = "html_pages/switch.html";
			$title              = "Switches";
			$breadcrumbs        = $ADD_BREADCRUMBS;
			$current_breadcrumb = "Switches";
			$content            = $db->get_switches();
		}

		case "full_stats" {
			$filename = "html_pages/full_stats.html";
			$title = "Switch Details";
			$breadcrumbs =   $ADD_BREADCRUMBS;
			$current_breadcrumb = "Switch Details";
			$content = $db->get_switches();	
		}
		case "capstats" {
			$filename = "html_pages/switch_capstats.html";
			$title = $cgi->param('switch_selection');
			$breadcrumbs = $HOME_BREADCRUMBS;
			$current_breadcrumb = "Switch status";
#			$content = $graph->get_sensors();

		}
		case "flow_test" {
			$filename = "html_pages/flow_test.html";
			$title ="Flow Test";
			$breadcrumbs = $HOME_BREADCRUMBS;
			$current_breadcrumb = "Flow Test";
			$content = $db->get_switches();

		}



		else {
			$filename = "html_pages/error.html";
			$title    = "Error";
		}

	}

	$vars->{'page'}               = $filename;
	$vars->{'title'}              = $title;
	$vars->{'breadcrumbs'}        = $breadcrumbs;
	$vars->{'current_breadcrumb'} = $current_breadcrumb;
	$vars->{'content'}            = $content;

	$tt->process( "html_pages/page_base.html", $vars, \$output )
	  or warn $tt->error();

	print "Content-type: text/html\n\n" . $output;

}

main();
