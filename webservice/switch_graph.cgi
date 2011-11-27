#!/opt/local/bin/perl5.12
# -*- perl -*-

use strict;
use CGI;
use RRDs;

# get the root directory of the archive, its title, and its Web page
my $query = new CGI;
my $switchname = "testname";
my $safe_title = $switchname;
$safe_title =~ s/\//_/g;


#set the width and height for the graphs
my $width = 450; 
my $height = 145; 

my $rrdtime = time() - 600;

my $rrdDir="/Users/akhalfan/Documents/NOC/flowscale/rrds";
my $imgDir="/Users/akhalfan/Documents/workspace-beacon-1/flowscale-webui/img";

my $graphSuccess;

my $rrd_db=$rrdDir . "/of.db";

my %switches;


# print out the Web page
print << "END";
Expires: Fri, 30 Oct 1998 14:19:41 GMT
Content-type: text/html


END

print "<h2>$safe_title</h3>\n";
print << "END";
<table>
<tr bgcolor="#0000FF">
END


#print "### $rrdStr\n";
print "<th colspan=\"2\"><font color=\"#FFFFFF\">";
print "</tr>\n";

print <<"END";
<tr bgcolor="#0000FF">
    <th><font color="#FFFFFF">Port</font></th>
    <th><font color="#FFFFFF">Graph</font></th>
</tr>

END



open(DBFILE,$rrd_db) or die "Can not open DB File $rrd_db";
while (<DBFILE>) {
  chomp;
  my ($sw,$filenm,$pt) = split (/:/,$_);
  $switches{$sw}{$pt} = $filenm;
}


foreach my $sw_ip (sort keys %switches) {
    foreach my $pt_index (sort { $a <=> $b } keys %{$switches{$sw_ip}}) {
	my $rrdfile = $switches{$sw_ip}{$pt_index}; 
        my $filename = $imgDir . "/" . $sw_ip . ":" . $pt_index . ".png";

       my ($averages, $xsize, $ysize) = RRDs::graph($filename,
	"-s $rrdtime","-h $height","-w $width",
        "DEF:input=$rrdfile:in_bytes:AVERAGE",
        "DEF:output=$rrdfile:out_bytes:AVERAGE",
        "CDEF:inputCDEF=input,8,*",
        "CDEF:outputCDEF=output,8,*",
	"AREA:inputCDEF#00FF00:rx bits",
        "LINE1:outputCDEF#0000FF:tx bits");

        my $rrderror = RRDs::error;

        print $rrderror;
        print "<tr><td>$sw_ip -- $pt_index</td><td><img src=\"../img/$sw_ip:$pt_index.png\"></td></tr>";
      }
}


print <<"END";

</table>
<br><br>

END

