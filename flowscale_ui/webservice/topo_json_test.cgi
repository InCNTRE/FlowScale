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

     


	my $results = { data => {
                            nodes => [ { id => "1" },
                                       { id => "2" },
				       { id => "3", parent => "1" },
                                       { id => "4", parent => "2" },
                                       { id => "5", parent => "2" },
                            ],
                            edges => [ 
                                    { target => "3", source => "4" },
				    { target => "3", source => "5" },
                            ]
                        }
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


