#!/usr/bin/perl

use strict;
use warnings;

use CGI;
use DBI;
use JSON;
use Date::Manip;

my $cgi = new CGI;

my $loader =0;

my $db = DBI->connect("dbi:SQLite:/var/flowscale/linux.gtk.x86_64/flowscale.db", "", "",
{RaiseError => 1, AutoCommit => 1});

my $first_time = $cgi->param('first_time');
my $second_time = $cgi->param('second_time');

my $ports = $cgi->param('port_id');
my $time1 = UnixDate($first_time, "%s");
my  $time2 = UnixDate($second_time, "%s");
my $combination_type = $cgi->param('combination');                                          
$time1 = $time1 *1000;
$time2 = $time2 *1000;
my $total_packets = 0;
my $theout;
my $total_packet_transmitted =0;
my $total_packet_received =0;
#$second_time =1328124008271 ;
 #$first_time =1328123708271;
 my @port_values = split(',', $ports);

my $output ="" ;
if ($combination_type eq "side_by_side"){
  foreach my $port_id (@port_values) {

my $all = $db->selectall_arrayref("SELECT * FROM port_stats where timex >= $time2 and timex <= $time1 and port_id = $port_id");
foreach my $row (@$all) {
my ($timex, $packet_transmitted, $packet_received, $port_id) = @$row;
#push (@$output, {
#	"timex" => $timex,
#	"packet_received" => $packet_received,
#	"port_id" => $port_id
#	}
#	);
$output = $output . $timex.",".$packet_transmitted.",".$packet_received."-";



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

my $all = $db->selectall_arrayref("SELECT * FROM port_stats where timex >= $time2 and timex <= $time1 ");
my %port_hash = map { $_ => 1 } @port_values;
$loader =0;
foreach my $row (@$all){


my ($timex,$packet_transmitted, $packet_received, $port_id) = @$row;
#if(exists($port_hash{$port_id})) { 

$intervals{$timex}{packet_received} = $intervals{$timex}{packet_received} + $packet_received ;
$intervals{$timex}{packet_transmitted} = $intervals{$timex} {packet_transmitted}+ $packet_transmitted ;
$loader = $loader + $packet_received;
$loader = $loader - $packet_transmitted;
$theout = $theout ."\n".$timex.",".$packet_transmitted.",".$packet_received.",".$port_id
#}

}
foreach my $timex (sort keys %intervals){

$output = $output. $timex.",".$intervals{$timex}{packet_transmitted}.",".$intervals{$timex}{packet_received} . "-";
$total_packet_received = $total_packet_received + $intervals{$timex}{packet_received};
$total_packet_transmitted = $total_packet_transmitted + $intervals{$timex}{packet_transmitted}
}
$output = substr($output, 0, - 1);
}



print "Content-type: text/plain\n\n" . $output ."\n";

print $total_packet_transmitted."\n";
print $total_packet_received;
print $theout;
#print "\n<br />".$cgi->param('first_time') ."\n<br />" . $cgi->param('second_time');
#print "\n<br />".$time1 ."\n<br />" . $time2;
