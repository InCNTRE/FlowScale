#!/usr/bin/perl

use strict;
use warnings;

use CGI;
use JSON;
use Switch;
use Data::Dumper;
use RRDs;

use lib "../lib";

my $cgi = new CGI;

$| = 1;

sub main {



	my $output;

    my $date = time() - 3600;   
  
    my $start      = $cgi->param("start") || $date; 
    my $end        = $cgi->param("end"); 
    my $filename = $cgi->param("file") || "156.56.5.43_25.rrd:156.56.5.43_26.rrd:156.56.5.43_27.rrd";
    


	$output = &get_rrd_file_data($filename,
                                     $start,
                                     $end);


	send_json($output);

}

sub get_rrd_file_data{
    my ($filename,$st,$end) = @_;


    if(!defined($filename)){
        print "No file specified";
        return undef;
    }

    if(!defined($st)){
        return undef;
    }

    if(!defined($end)){
        $end = "NOW";
    }

    my $h_size = 300;

    my @rrds = split(/:/,$filename);
    my ($start,$step,$names,$data);
    my @data;
    my $agg_data;
    my $file_num = 0;
    foreach my $rrd (@rrds) {

    my $rrdfile = "/home/akhalfan/rrds/" . $rrd;
    if ($rrd =~ /140.221.223.201/) {$rrdfile = "/home/akhalfan/rrdssc/" . $rrd;}
    #print " Filename $rrd\n";


    ($start,$step,$names,$data) = RRDs::fetch($rrdfile,"AVERAGE","-s " . $st,"-e " . $end);
    @data = @$data;
    #print Dumper $data;

    for(my $y=0; $y<$#data; $y++){
	my @data_foo = @$data[$y];
	#print Dumper @data_foo;
	for (my $z=0; $z<4; $z++) {
          #print "data $y $z\n";
          #print $data->[$y][$z] . "\n";
          
          if ($file_num == 0) {
	     $agg_data->[$y][$z] = $data->[$y][$z];
	  } else {
	     $agg_data->[$y][$z] += $data->[$y][$z] 
	   }
	  }
      }
    $file_num ++;
   }
   $agg_data->[$#data] = $data->[$#data]; 
   #print "Agg \n =========\n";
   #print Dumper $agg_data;

    if (! defined $data){
        return undef;
    }

    
    my @results;
    my $output;
    my $input;
    my @names = @$names;
    my @agg_data = @$agg_data;


    for(my $i=0;$i<$#names+1;$i++){

        if($names[$i] eq 'out_bytes'){
            $output = $i;
        }
        if($names[$i] eq 'in_bytes'){
            $input = $i;
        }
    }
    my $time = $st;
    my @outputs;
    my @inputs;

    my $spacing = int(@$agg_data / $h_size);
    $spacing = 1 if($spacing == 0);

    for(my $i=0;$i<$#agg_data;$i+=$spacing){
        my ($bucket,$j);

        for($j = 0;$j<$spacing && $j+$i < @$agg_data; $j++){
            my $row = @$agg_data[$i+$j];
            my $divisor = ($j + $i == @$agg_data - 1? $j+1 : $spacing);

            if(defined(@$row[$input])){
                $bucket->{'in_bytes'} += @$row[$input] / $divisor;
            }

            if(defined(@$row[$output])){
                $bucket->{'out_bytes'} += @$row[$output] / $divisor;
            }
        }

        if($spacing > 1){
            my $timeStart = $start;
            my $timeEnd = ($start + ($step*$spacing));
            $bucket->{'time'} = ($timeStart + $timeEnd) / 2;
        }else{
            $bucket->{'time'} = $start;
        }

        $start += ($step * $spacing);
        if(defined($bucket->{'in_bytes'})){
            $bucket->{'in_bytes'} *= 8;
        }
        push(@inputs,[$bucket->{'time'}, $bucket->{'in_bytes'}]);
        if(defined($bucket->{'out_bytes'})){
            $bucket->{'out_bytes'} *= 8;
        }
        push(@outputs,[$bucket->{'time'}, $bucket->{'out_bytes'}]);

    }

    push(@results,{"name" => "Input (bps)",
                   "data" => \@inputs});
    push(@results,{"name" => "Output (bps)",
                   "data" => \@outputs});
#    push(@results,{"name" => "filename",
#		   "data" => $filename}); 

    my $res;
    $res->{'results'} = \@results;

    return $res;

    
}
 

   



sub send_json {
	my $output = shift;
	print "Content-type: text/plain\n\n" . encode_json($output);
}

main();

