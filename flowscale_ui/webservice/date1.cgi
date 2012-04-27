#!/usr/bin/perl
use strict;
use warnings;
use Date::Manip;

my $time = UnixDate('2007/07/17 13:21', "%s");

print "time=$time\n";   # 1184671260
