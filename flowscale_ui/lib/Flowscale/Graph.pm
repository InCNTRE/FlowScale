#!/opt/local/bin/perl5.12
# -*- perl -*-

use strict;
use warnings;

package Flowscale::Graph;
use CGI;
use RRDs;

sub new {
	my $that = shift;
	my $class = ref($that) || $that;

  my $self = bless { };
    return $self;
}

sub get_switch_ports {

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

	my $rrdtime = time() - 600;

	my $rrdDir = "/home/akhalfan/rrds";

	if ($dev eq "SC"){
		$rrdDir = "/home/akhalfan/rrdssc";
	}	

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
				"DEF:input=$rrdfile:in_bytes:AVERAGE",
				"DEF:output=$rrdfile:out_bytes:AVERAGE",
				"CDEF:inputCDEF=input,8,*",
				"CDEF:outputCDEF=output,8,*",
				"AREA:inputCDEF#00FF00:rx bits",
				"LINE1:outputCDEF#0000FF:tx bits"
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
