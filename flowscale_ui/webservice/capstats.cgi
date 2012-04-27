#!/usr/bin/perl

use strict;
use warnings;

use CGI;
use DBI;
use JSON;
use Date::Manip;

my $cgi = new CGI;


my $db = DBI->connect("dbi:SQLite:/home/akhalfan/capstats_output/capstats.db", "", "",
{RaiseError => 1, AutoCommit => 1});

my $first_time = $cgi->param('first_time');
my $second_time = $cgi->param('second_time');

my $ports = $cgi->param('port_id');
my $time1 = UnixDate($first_time, "%s");
my  $time2 = UnixDate($second_time, "%s");
my $combination_type = $cgi->param('combination');                                          
$time1 = $time1 ;
$time2 = $time2 ;
my $total_packets = 0;

#$second_time =1328124008271 ;
 #$first_time =1328123708271;
 my @port_values = split(',', $ports);

my $output ="" ;
if ($combination_type eq "side_by_side"){
  foreach my $port_id (@port_values) {

my $all = $db->selectall_arrayref("SELECT * FROM capstats where timex >= $time2 and timex <= $time1 and swill = $port_id");
foreach my $row (@$all) {
my ($timex, $MBps, $swill) = @$row;
#push (@$output, {
#	"timex" => $timex,
#	"packet_received" => $packet_received,
#	"port_id" => $port_id
#	}
#	);
$output = $output . $timex.",".$MBps."-";



#$soutput =  $soutput . "$timex|$packet_received|$port_id";
#print   "$timex|$packet_received|$port_id";

}
$output = substr($output, 0, - 1);
$output = $output . "=";
}
$output = substr($output, 0, - 1);

}


elsif($combination_type eq "aggregate"){


my %intervals  =();
my $all = $db->selectall_arrayref("SELECT * FROM capstats where timex >= $time2 and timex <= $time1 ");
my %port_hash = map { $_ => 1 } @port_values;

foreach my $row (@$all){


my ($timex,$MBps, $swill) = @$row;
if(exists($port_hash{$swill})) { 

$intervals{$timex} = $intervals{$timex} + $MBps ;
}

}
foreach my $timex (sort keys %intervals){

$output = $output. $timex.",".$intervals{$timex} . "-";

}
$output = substr($output, 0, - 1);
}



print "Content-type: text/plain\n\n" . $output;
#print "\n<br />".$cgi->param('first_time') ."\n<br />" . $cgi->param('second_time');
#print "\n<br />".$time1 ."\n<br />" . $time2;
