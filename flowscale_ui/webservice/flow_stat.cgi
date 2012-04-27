#!/usr/bin/perl

use strict;
use warnings;

use CGI;
use DBI;
use JSON;
use Date::Manip;

my $cgi = new CGI;


my $db = DBI->connect("dbi:SQLite:/var/flowscale/linux.gtk.x86_64/flowscale.db", "", "",
{RaiseError => 1, AutoCommit => 1});

my $first_time = $cgi->param('first_time');
my $second_time = $cgi->param('second_time');

my $ports = $cgi->param('port_id');
my $time1 = UnixDate($first_time, "%s");
my  $time2 = UnixDate($second_time, "%s");
$time1 = $time1 *1000 ;
$time2 = $time2 *1000 ;

my $totalPackets = 0;
my %port_packets ;
my $output =[] ;
my $all = $db->selectall_arrayref("SELECT * FROM flow_stats where timex >= $time2 and timex <= $time1 ");
foreach my $row (@$all) {
my ($timex, $packet_received, $match_string,$actions) = @$row;
push (@$output, {
	"timex" => $timex,
	"packet_count" => $packet_received,
	"match_string" => $match_string,
	"action" => $actions
	}
	);
$port_packets{$actions} = $port_packets{$actions} + $packet_received;
$totalPackets = $totalPackets + $packet_received;

}



my $results;
my $totalResult;
$results->{'results'} = $output;
$results->{'total'}  = $totalPackets;
$results->{'port_packets'} = %port_packets;
$totalResult->{'response'} = $results;
print "Content-type: text/plain\n\n" . encode_json($totalResult);
