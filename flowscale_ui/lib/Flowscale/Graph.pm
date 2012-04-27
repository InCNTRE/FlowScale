##!/opt/local/bin/perl5.12
#!/usr/bin/perl

# -*- perl -*-

use strict;
use warnings;

use Date::Manip;
package Flowscale::Graph;
use CGI;
use RRDs;


sub new {
	my $that = shift;
	my $class = ref($that) || $that;


  my $self = bless { };
    return $self;
}

sub get_graph {

	my $self = shift;
	my %args = @_;

	#my $sw_ip = $args{'switch_ip'};
# 	my	$dev = $args{'device'};
	# get the root directory of the archive, its title, and its Web page
	my $query      = new CGI;
	my $switchname = "";
	my $safe_title = $switchname;
	$safe_title =~ s/\//_/g;


my $value=	$self->create_rrds(datapath_id => $args{'datapath_id'}, time1 => $args{'time1'}, 
		time2 => $args{'time2'} );

	
return $value;

}


sub create_graphs{

	my $self = shift;
	my %args = @_;
	#set the width and height for the graphs
	my $width  = 750;
	my $height = 345;



	my $time1 = $args{'time1'};
	my $time2 = $args{'time2'};
	my $datapath_id = $args{'datapath_id'};

	 my $port_ref  = $args{'port'};
	


	my $rrdtime =  $time1 ;

	my $rrdDir = "[path]/rrd";


	my $imgDir =
	  "/var/www/flowscale_ui/img";

	my $graphSuccess;

my $rrd_dir ="/[akhalfan]/rrd";
	my $image_names = [];
	#foreach my $sw_ip ( sort keys %switches ) {
		foreach my $pt_index ( sort { $a <=> $b } keys %{$port_ref}  )
		
		{

my $log;
open $log, ">> /home/akhalfan/somlogfile" or die $!;
#print $log "port $pt_index\n";
		}


my $log;

foreach my $pt_index ( sort { $a <=> $b } keys %{$port_ref}  )
		{
	
	
	if(1){			
		my $rrdfile  = $rrdDir . "/" . $datapath_id . "_" . $pt_index . ".rrd";

my $hash;
open $log, ">> /home/akhalfan/somlogfile" or die $!;
   $hash = RRDs::info  $rrdfile;
        foreach my $key (keys %$hash){
          #print $log  " some graph $key = $$hash{$key}\n";
        }


			my $filename = $imgDir . "/" . $datapath_id . ":" . $pt_index . ".png";

			my ( $averages, $xsize, $ysize ) = RRDs::graph(
				$filename,
				"-s $rrdtime",
				"-e $time2",
				"-h $height",
				"-w $width",
				"-t packets on  port $pt_index  for switch $datapath_id",
				"DEF:output=$rrdfile:out-port:AVERAGE",
				"DEF:input=$rrdfile:in-port:AVERAGE",
				"CDEF:outputCDEF=output,30,/",                                                                         
                                "CDEF:inputCDEF=input,30,/",                                                                           
                                "AREA:outputCDEF#00FF00:packets sent",                                                                 
                                "LINE:inputCDEF#0000FF:packets received",   
			);

			my  $rrderror = RRDs::error;
			#print $rrderror;
open $log, ">> /home/akhalfan/somlogfile" or die $!;
print $log "error  $rrderror";
			
			push(
				@$image_names,
				{ port_id => $pt_index , image_name => "$datapath_id:$pt_index.png", error => "$rrderror" }

			);
}
		}
	#}
	return $image_names;
}



sub create_rrds{



	my $self = shift;
	my %args = @_;


	my $time1 = $args{'time1'};
	my $time2 = $args{'time2'};
	my $datapath_id = $args{'datapath_id'};


	# get data from database 

	my 	%port = () ;

	%port =	$self->get_data_from_db(datapath_id => $datapath_id,
		time1 => $time1,
		time2 => $time2,
	);



my $timestamp1;
my $timestamp2;
if($time1 eq ""){


$timestamp2 = time;

$timestamp1 = $timestamp2 - 600;
}
else{
 $timestamp1 = Date::Manip::UnixDate($time1, "%s");
  $timestamp2 = Date::Manip::UnixDate($time2, "%s");
}

my $string_values ="start string ";
my $i =0;
my $log;
open $log, ">> /home/akhalfan/somlogfile" or die $!;
foreach my $pt (sort {$a <=> $b }  keys %port) {


		if((1))
		{



my $rrd_dir ="/home/akhalfan/rrd";
my $step = $timestamp2 - $timestamp1;
my $file = $rrd_dir . "/" . $datapath_id . "_" . $pt . ".rrd";
my $starttime = $timestamp1;

#print $log "start time is $starttime";

		RRDs::create($file,"--step",30,
"--start","$starttime",
"DS:out-port:GAUGE:90:U:U",
"DS:in-port:GAUGE:90:U:U",
"RRA:AVERAGE:0.5:1:1600",
"RRA:AVERAGE:0.5:1:1600");
my $rrderror = RRDs::error;

print $log "$rrderror";

my %port_details = ();

my $timer1 = time;
print $log "for port $pt";

print $log "timer1 = $timer1";
#print $log "inner loop\n";
foreach my $timestampval (sort   keys %{%port->{$pt}} ){
my $count = %port->{$pt}->{$timestampval}{transmit} ;
my $count_receive = %port->{$pt}->{$timestampval}{receive} ;
my $actualtime = $timestampval/1000;
 RRDs::update($file, "$actualtime:$count:$count_receive");
 $rrderror = RRDs::error;
print $log $rrderror;

print $log "inserting time ". $timestampval." and count  $count \n "; 

#print $log "file $file tiemstamp  $timestampval count $count\n";


#$string_values = $string_values ."<br />" . $pt ." timestamp ".  $timestampval ." count  ".%port->{$pt}->{$timestampval} ;


}



}

}


my	$images = [];

	$images = $self->create_graphs(
		time1 => $timestamp1,
		time2 => $timestamp2,
		datapath_id => $datapath_id,
		port => \%port,
	);




return $images;



}





sub get_data_from_db{

	my $self = shift;
	my %args = @_;
	

	my $time1 =0 ;
	my $time2  = 0;
	my $datapath_id = "";

	my $string_date_1;
	my $string_date_2;

	 $datapath_id = hex($args{'datapath_id'});
	 $string_date_1 = $args{'time1'};
	 $string_date_2 = $args{'time2'};

if($string_date_1 eq ""){


$time2 = time;

$time1 = $time2 - 600;
}else{
	 $time1 = Date::Manip::UnixDate($string_date_1, "%s");
	 $time2 = Date::Manip::UnixDate($string_date_2, "%s");
}
	my %port;

	$time1 = $time1 * 1000;
	$time2 = $time2 * 1000;	


	my $database_name;
	my $hostname;
	my $port;
	my $username;
	my $password;



	 $database_name = "flowscale_db" ;
	 $hostname  = "[db-url]";
	 $port  ="[db-port]";
	 $username = "[db-usernmame]";
	 $password ="[db-password]";
	
	my $dbh ;
 $dbh           = DBI->connect(
		"DBI:mysql:database=$database_name;host=$hostname;port=$port",
		$username, $password, { AutoCommit => 0 } );

	#database query 
my $query = 	"SELECT datapath_id, port, packets_transmitted FROM port_stats where datapath_id = $datapath_id  AND  timestamp >= $time1 AND timestamp <= $time2";


my $log;
open $log, ">> /home/akhalfan/somlogfile" or die $!;

print $log "$query\n";

	my $db_result ;
	$db_result = $dbh->prepare("SELECT datapath_id, port, timestamp , packets_transmitted ,packets_received FROM port_stats where datapath_id =  $datapath_id   AND  timestamp >=  $time1  AND timestamp <=  $time2 ; ");
	my $port_value;
	$db_result->execute();

	while(my $port_stat_row = $db_result->fetchrow_hashref()){

	
		$port_value = $port_stat_row->{'port'};
		my $timestamp_value;
		$timestamp_value = $port_stat_row->{'timestamp'};
		$port{$port_value}{$timestamp_value}{transmit} = $port_stat_row->{'packets_transmitted'};
		$port{$port_value}{$timestamp_value}{receive} = $port_stat_row->{'packets_received'};




	}

	return %port;
}





sub get_sensors {

	my $self = shift;
	my %args = @_;

	#my $sw_ip = $args{'switch_ip'};
 	my	$dev = $args{'device'};
	# get the root directory of the archive, its title, and its Web page
	my $query      = new CGI;
	my $switchname = "";
	my $safe_title = $switchname;
	$safe_title =~ s/\//_/g;

	#set the width and height for the graphs
	my $width  = 450;
	my $height = 145;

	my $rrdtime = time() - 1200;

	my $rrdDir = "/home/akhalfan/rrdssensors";


	my $imgDir =
	  "/var/www/html/flowscale-dev/img";

	my $graphSuccess;

	my $rrd_db = $rrdDir . "/of.db";

	my %switches;

	open( DBFILE, $rrd_db ) or die "Can not open DB File $rrd_db";
	while (<DBFILE>) {
		chomp;
		my ( $sw, $filenm, $pt ) = split( /:/, $_ );
		$switches{$sw}{$pt} = $filenm;
	}

	my $image_names = [];
	foreach my $sw_ip ( sort keys %switches ) {
		foreach my $pt_index ( sort { $a <=> $b } keys %{ $switches{$sw_ip} } )
		{
			my $rrdfile  = $switches{$sw_ip}{$pt_index};
			my $filename = $imgDir . "/" . $sw_ip . ":" . $pt_index . ".png";

			my ( $averages, $xsize, $ysize ) = RRDs::graph(
				$filename,
				"-s $rrdtime",
				"-h $height",
				"-w $width",
     "DEF:input=$rrdfile:kpps:AVERAGE",
                                "DEF:output=$rrdfile:mbps:AVERAGE",
                                "CDEF:inputCDEF=input,1,*",
                                "CDEF:outputCDEF=output,1,/",
                                "AREA:inputCDEF#00FF00:kpps",
                                "LINE1:outputCDEF#0000FF:MBps"

			);

			my  $rrderror = RRDs::error;
			#print $rrderror;
			
			push(
				@$image_names,
				{ port_id => $pt_index , image_name => "$sw_ip:$pt_index.png", error => "$rrderror" }

			);

		}
	}
	return $image_names;
}





return 1;
