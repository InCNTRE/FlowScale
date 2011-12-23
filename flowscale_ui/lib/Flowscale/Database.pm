#!/opt/local/bin/perl5.12

use strict;
use warnings;

package Flowscale::Database;

use DBI;
use XML::Simple;
use File::ShareDir;
use Data::Dumper qw(Dumper);

our $VERSION = '0.0.1';

our $ENABLE_DEVEL = 0;

sub new {
	my $that = shift;
	my $class = ref($that) || $that;

	my %args = (
		config =>
'/home/chsmall/git/FlowScale/flowscale_ui/conf/database.xml',
		@_,
	);

	my $self = \%args;
	bless $self, $class;

	my $config_filename = $args{'config'};

	my $config = XML::Simple::XMLin($config_filename);

	my $username      = $config->{'credentials'}->{'username'};
	my $password      = $config->{'credentials'}->{'password'};
	my $database_name = $config->{'credentials'}->{'database_name'};
	my $port          = $config->{'credentials'}->{'port'};
	my $hostname      = $config->{'credentials'}->{'hostname'};
	my $dbh           = DBI->connect(
		"DBI:mysql:database=$database_name;host=$hostname;port=$port",
		$username, $password, { AutoCommit => 0 } );

	if ( !$dbh ) {
		return undef;
	}

	$self->{'dbh'} = $dbh;

	return $self;
}



sub _set_error {
	my $self = shift;
	my $err  = shift;

	$self->{'error'} = $err;
}

sub get_error {
	my $self = shift;

	return $self->{'error'};
}

sub _set_tran_id {
	my $self = shift;
	my %args = @_;
	my $transaction_id = $args{'transaction_id'};
	$self->{'transaction_id'} = $transaction_id;
	
	
}

sub get_transaction_id {
	my $self = shift;
	return $self->{'transaction_id'};
}

sub _start_transaction {
	my $self = shift;

	my $dbh = $self->{'dbh'};

	$dbh->begin_work();
}

sub _commit {
	my $self = shift;

	my $dbh = $self->{'dbh'};

	$dbh->commit();
}
sub _disconnect {
		my $self = shift;

	my $dbh = $self->{'dbh'};

	$dbh->disconnect();
	
}

sub _rollback {

	my $self = shift;

	my $dbh = $self->{'dbh'};
	$dbh->rollback();
}

sub _prepare_query {
	my $self  = shift;
	my $query = shift;

	my $dbh = $self->{'dbh'};

	my $sth = $dbh->prepare($query);

	if ( !$sth ) {
		warn "Error in prepare query: $DBI::errstr";
		$self->_set_error("Unable to prepare query: $DBI::errstr");
		return undef;
	}

	return $sth;
}

sub delete_xconnect {
	my $self = shift;
	my %args = @_;

	my $switch_1_id = $args{'switch_1_id'};
	my $switch_2_id = $args{'switch_2_id'};

	$self->_start_transaction();

	my $sth = $self->_prepare_query(
		"delete from x_connect where switch_1_id = ? AND switch_2_id = ?");

	$sth->execute( $switch_1_id, $switch_2_id );

	if ( $sth->rows != 1 ) {
		return [
			{
				"error"   => "Error delete node.",
				"success" => 0
			}
		];
	}

	$self->_commit();

	return [ { 'success' => 1 } ];

}

sub add_xconnect {
	my $self = shift;
	my %args = @_;

	my $switch_1_id = $args{'switch_1_id'};
	my $switch_2_id = $args{'switch_2_id'};
	my $port1       = $args{'port1'};
	my $port2       = $args{'port2'};

	

	my $sth = $self->_prepare_query("insert into x_connect values (?,?,?,?)");

	$sth->execute( $switch_1_id, $switch_2_id, $port1, $port2 );

	if ( $sth->rows != 1 ) {
		return [
			{
				"error"   => "Error updating node.",
				"success" => 0
			}
		];
	}

	$self->_commit();

	return [ { 'success' => 1 } ];

}

sub delete_switch {
	my $self = shift;
	my %args = @_;

	my $switch_id = $args{'switch_id'};



	#check to see if the switch is involved in any group
	my $check_group_query =
	  "SELECT * FROM flow_group WHERE input_switch = ? OR  output_switch = ?";

	my $sth = $self->_prepare_query($check_group_query);
	$sth->execute( $switch_id, $switch_id );

	if ( $sth->rows != 0 ) {

		return [
			{
				"error" => "Switch already used by a group, delete group first",
				"success" => 0

			}
		];
	}

	#check to see if the switch is involved in any xconnect

	my $check_xconnect_query =
	  "SELECT * from x_connect where switch_1_id = ? OR switch_2_id = ? ";

	$sth = $self->_prepare_query($check_xconnect_query);
	$sth->execute( $switch_id, $switch_id );

	if ( $sth->rows != 0 ) {

		return [
			{
				"error" =>
				  "Switch already involved in xconnect, delete xconnect first",
				"success" => 0
			}

		];
	}

	#if switch is not involved in any you may delete

	$sth = $self->_prepare_query("delete from switch where datapath_id = ?");

	$sth->execute($switch_id);

	if ( $sth->rows != 1 ) {
		return [
			{
				"error"   => "Error deleting switch.",
				"success" => 0
			}
		];
	}

	$sth = $self->_prepare_query("delete from switch_port where switch_id = ?");

	$sth->execute($switch_id);

	if ( $sth->rows != 1 ) {
		return [
			{
				"error"   => "Error deleting switch.",
				"success" => 0
			}
		];
	}

	$self->_commit();

	return [ { 'success' => 1 } ];

}

sub add_new_switch {

	my $self = shift;
	my %args = @_;

	my $datapath_id = $args{'datapath_id'};
	my $switch_name = $args{'switch_name'};
	my $mac_address = $args{'mac_address'};
	my $ip_address  = $args{'ip_address'};

	my $update_query = "INSERT INTO switch VALUES (?,?,?,?,?)";

	my $sth = $self->_prepare_query($update_query) or return undef;
	$sth->execute( $datapath_id, $mac_address, $ip_address, $switch_name, ' ' );

	if ( $sth->rows != 1 ) {
		return [
			{
				"error"   => "Error adding  device.",
				"success" => 0

			}
		];

	}
	$self->_commit();


	return [ { "success" => 1 } ];

}

sub edit_switch {

	my $self = shift;
	my %args = @_;

	my $datapath_id = $args{'datapath_id'};
	my $switch_name = $args{'switch_name'};
	my $mac_address = $args{'mac_address'};
	my $ip_address  = $args{'ip_address'};

	my $update_query =
"update switch set switch_name = ? , mac_address = ?, ip_address = ? WHERE switch.datapath_id = ?";

	my $sth = $self->_prepare_query($update_query) or return undef;
	$sth->execute( $switch_name, $mac_address, $ip_address, $datapath_id );

	if ( $sth->rows != 1 ) {
		return [
			{
				"error"   => "Error updating switch.",
				"success" => 0
			}
		];

	}
	$self->_commit();

	return [ { 'success' => 1 } ];

}

sub edit_group {

	my $self = shift;
	my %args = @_;

#	my $edit_group_query =
#"update flow_group set input_switch = ?, output_switch = ? ,priority = ? , type= ? where group_id = ? ";

 #	my $add_group_port_query = "insert into  group_port values (?,?,?)";
 #	my $delete_group_port_query =	  "delete from group_port where group_id =?";
 #	my $add_group_value_query = "insert into group_values values (?,?)";
 #	my $delete_group_value_query = "delete from group_values where group_id = ?";

	my $delete_iport_query =
"Delete from group_port where group_id = ? and port_direction = 0 and port_id = ?";
	my $add_iport_query = "Insert into group_port values (?,0,?)";
	my $delete_oport_query =
"Delete from group_port where group_id = ? and port_direction = 1 and port_id = ?";
	my $add_oport_query = "Insert into group_port values (?,1,?)";

	my $delete_value_query =
	  "Delete from group_values wehere group_id = ? and value = ?";
	my $add_value_query = "Insert into group_values values (?,?)";

	my $used_query = "";

	my $group_id      = $args{'group_id'};
	my $input_switch  = $args{'input_switch'};
	my $output_switch = $args{'output_switch'};
	my $group_type    = $args{'group_type'};
	my $priority      = $args{'priority'};
	my @commands      = $args{'commands'};
	my $parse;

	foreach my $command (@commands) {

		my @parse = split( ',', $command );

		if ( $parse[0] eq "DELETE" ) {
			if ( $parse[1] eq "IPORT" ) {
				$used_query = $delete_iport_query;
			}
			elsif ( $parse[1] eq "OPORT" ) {
				$used_query = $delete_oport_query;

			}
			elsif ( $parse[1] eq "VALUE" ) {
				$used_query = $delete_value_query;
			}
		}
		elsif ( $parse[0] eq "ADD" ) {
			if ( $parse[1] eq "IPORT" ) {
				$used_query = $add_iport_query;
			}
			elsif ( $parse[1] eq "OPORT" ) {
				$used_query = $add_oport_query;

			}
			elsif ( $parse[1] eq "VALUE" ) {
				$used_query = $add_value_query;
			}
		}

		my $sth = $self->_prepare_query($used_query) or return undef;
		$sth->execute( $group_id, $parse[2] );

		if ( $sth->rows != 1 ) {
			$self->_rollback();
			return [
				{
					"error"   => "Error updating groups.",
					"success" => 0
				}
			];

		}

	}

	$self->_commit();

	return [ { 'success' => 1 } ];

}

sub add_new_group {

	my $self = shift;
	my %args = @_;
	my $results;
	my $add_group_port_query = "insert into  group_port values (?,?,?)";

	my $add_group_value_query = "insert into group_values values (?,?)";

	my $group_name    = $args{'group_name'};
	my $input_switch  = $args{'input_switch'};
	my $output_switch = $args{'output_switch'};
	my $group_type    = $args{'group_type'};
	my $priority      = $args{'priority'};
	my $group_values  = $args{'group_values'};
	my $input_ports   = $args{'input_ports'};
	my $output_ports  = $args{'output_ports'};
	my $maximum_allowed = $args{'maximum_allowed'};

	my @input_ports_array  = split( ',', $input_ports );
	my @output_ports_array = split( ',', $output_ports );
	my @group_values_array = split( ',', $group_values );

	my $query =
"insert into flow_group (input_switch, output_switch, comments, priority, type, maximum_flows) values (?,?,?,?,?,?)";

	my $sth = $self->_prepare_query($query) or return undef;
	$sth->execute( $input_switch, $output_switch, $group_name, $priority,
		$group_type, $maximum_allowed );

	if ( $sth->rows != 1 ) {
		return [
			{
				"error"   => "Error adding group .",
				"success" => 0
			}
		];

	}

	my $group_id = $self->{'dbh'}->last_insert_id( undef, undef, undef, undef );

	foreach my $port (@input_ports_array) {

		$sth = $self->_prepare_query($add_group_port_query)
		  or return undef;
		$sth->execute( $group_id, 0, $port );

		if ( $sth->rows != 1 ) {
			$self->_rollback();
			return [
				{
					"error"   => "Error adding group_values.",
					"success" => 0
				}
			];

		}

	}

	foreach my $port (@output_ports_array) {

		$sth = $self->_prepare_query($add_group_port_query)
		  or return undef;
		$sth->execute( $group_id, 1, $port );

		if ( $sth->rows != 1 ) {
			$self->_rollback();
			return [
				{
					"error"   => "Error adding group_values.",
					"success" => 0
				}
			];

		}

	}

	foreach my $value (@group_values_array) {

		$sth = $self->_prepare_query($add_group_value_query)
		  or return undef;
		$sth->execute( $group_id, $value );

		if ( $sth->rows != 1 ) {
			$self->_rollback();
			return [
				{
					"error"   => "Error updating group values.",
					"success" => 0
				}
			];

		}
	}

	$self->_commit();
	
	$self->_set_tran_id(transaction_id => $group_id);

	return [ { 'success' => 1, 'transaction_id' => $group_id } ];

}

sub delete_group {

	my $self  = shift;
	my %args  = @_;
	my $query = "delete from flow_group where group_id = ?";

	my $port_query = "delete from group_port where group_id = ?";

	my $group_value_query = "delete from group_values where group_id = ?";

	my $group_id = $args{'group_id'};

	my $sth = $self->_prepare_query($query) or return undef;
	$sth->execute($group_id);

	if ( $sth->rows != 1 ) {
		return [
			{
				"error"   => "Error deleting group." . $sth->rows,
				"success" => 0
			}
		];
	}

	$sth = $self->_prepare_query($port_query) or return undef;
	$sth->execute($group_id);

	#if ( $sth->rows != 1 ) {
	#	return [
	#		{
	#			"error"   => "Error deleting ports.",
	#			"success" => 0
	#		}
	#	];
#	}

	$sth = $self->_prepare_query($group_value_query) or return undef;
	$sth->execute($group_id);

#	if ( $sth->rows != 1 ) {
#		return [
#			{
#				"error"   => "Error group values .",
#				"success" => 0
#			}
#		];
#	}

	$self->_commit();

	return [ { 'success' => 1 } ];

}

sub get_switches {
	my $self = shift;
	my %args = @_;
	my $query =
"select switch.datapath_id, switch.mac_address, switch.ip_address, switch.switch_name, switch.description from switch";

	my $sth = $self->_prepare_query($query) or return undef;
	$sth->execute();

	my $results = [];
	while ( my $row = $sth->fetchrow_hashref() ) {
		push(
			@$results,
			{
				"datapath_id" => $row->{'datapath_id'},
				"mac_address" => $row->{'mac_address'},
				"ip_address"  => $row->{'ip_address'},
				"switch_name" => $row->{'switch_name'},
				"description" => $row->{'description'}
			}

		);

	}
	return $results;
}


sub get_groups {
	my $self        = shift;
	my %args        = @_;
	my $switches    = [];
	my $group_types = [];

	my $query =
	    "select flow_group.group_id, "
	  . "flow_group.comments, s1.switch_name as input_switch_name,"
	  . "s2.switch_name as output_switch_name, flow_group.priority, group_type.group_type_desc, flow_group.maximum_flows  from"
	  . " flow_group inner join switch s1 on flow_group.input_switch = s1.datapath_id inner join switch s2 on "
	  . " flow_group.output_switch = s2.datapath_id inner join group_type on flow_group.type = group_type.group_type_id ";

	my $sth = $self->_prepare_query($query) or return undef;
	$sth->execute();

	my $results = [];

	while ( my $row = $sth->fetchrow_hashref() ) {

		my $input_ports  = [];
		my $output_ports = [];

		my $values = [];

		push( @$input_ports,  '0' );
		push( @$output_ports, '1' );

		my $sth2 = $self->_prepare_query(
			"select value from group_values where group_id = ?");
		$sth2->execute( $row->{'group_id'} );

		while ( my $group_row = $sth2->fetchrow_hashref() ) {

			push( @$values, $group_row->{'value'} );
		}
		push(
			@$results,
			{
				"group_id"           => $row->{'group_id'},
				"comments"           => $row->{'comments'},
				"input_switch_name"  => $row->{'input_switch_name'},
				"output_switch_name" => $row->{'output_switch_name'},
				"priority"           => $row->{'priority'},
				"group_type_desc"    => $row->{'group_type_desc'},
				"maximum_flows"     => $row->{'maximum_flows'},
				"input_ports"        => $input_ports,
				"output_ports"       => $output_ports,
				"value"              => $values
			}

		);

	}    #end while

	my $switch_query = "SELECT datapath_id, switch_name FROM switch";
	$sth = $self->_prepare_query($switch_query) or return undef;
	$sth->execute();

	while ( my $row_switches = $sth->fetchrow_hashref() ) {
		push(
			@$switches,
			{
				"datapath_id" => $row_switches->{'datapath_id'},
				"switch_name" => $row_switches->{'switch_name'}
			}
		);

	}

	my $group_type_query =
	  "SELECT group_type_id, group_type_desc FROM group_type;";
	$sth = $self->_prepare_query($group_type_query) or return undef;
	$sth->execute();

	while ( my $row_group_types = $sth->fetchrow_hashref() ) {
		push(
			@$group_types,
			{
				"group_type_id"   => $row_group_types->{'group_type_id'},
				"group_type_desc" => $row_group_types->{'group_type_desc'}
			}
		);

	}
	my $totalResults = [ $results, $switches, $group_types ];

	return $totalResults;
}

sub get_group_values {

	my $self = shift;
	my %args = @_;

	my $group_id = $args{'group_id'};

	my $values = [];

	my $sth = $self->_prepare_query(
		"select value from group_values where group_id = ?");
	$sth->execute($group_id);

	while ( my $group_row = $sth->fetchrow_hashref() ) {

		push( @$values, { "group_value", $group_row->{'value'} } );

	}

	return $values;

}

sub get_switch_input_ports {
	my $self = shift;
        my %args = @_;

        my $dpid = $args{'dpid'};
	
	my $ports = [];

	my $sth = $self->_prepare_query(
	"select group_port.port_id, flow_group.input_switch, port_address
	from switch_port, flow_group, group_port
	where switch_port.switch_id = ? and 
	flow_group.input_switch = switch_port.switch_id and
        group_port.port_id = switch_port.port_id and 
	group_port.port_direction = 0" 
        );
	
	$sth->execute( $dpid );

        while ( my $input_row = $sth->fetchrow_hashref() ) {
		push(
                        @$ports,
                        {
				"dpid",         $dpid,
                                "port_id",      $input_row->{'port_id'},
                                "port_address", $input_row->{'port_address'}
                        }
                );

        }

        return $ports;
}



sub get_group_input_ports {

	my $self = shift;
	my %args = @_;

	my $group_id = $args{'group_id'};

	my $ports = [];

	my $sth = $self->_prepare_query(
"select group_port.port_id , port_address, flow_group.input_switch from switch_port, flow_group, group_port where flow_group.group_id = ? and input_switch = switch_port.switch_id "
		  . " and group_port.port_id = switch_port.port_id and port_direction = 0 and group_port.group_id = ?"
	);
	$sth->execute( $group_id, $group_id );

	while ( my $group_row = $sth->fetchrow_hashref() ) {

		push(
			@$ports,
			{
				"port_id",      $group_row->{'port_id'},
				"port_address", $group_row->{'port_address'}
			}
		);

	}

	return $ports;

}


sub get_switch_output_ports {
        my $self = shift;
        my %args = @_;

        my $dpid = $args{'dpid'};

        my $ports = [];

        my $sth = $self->_prepare_query(
        "select group_port.port_id, flow_group.output_switch, port_address
        from switch_port, flow_group, group_port
        where switch_port.switch_id = ? and 
        flow_group.output_switch = switch_port.switch_id and
        group_port.port_id = switch_port.port_id and 
        group_port.port_direction = 1"
        );

        $sth->execute( $dpid );

        while ( my $output_row = $sth->fetchrow_hashref() ) {
                push(
                        @$ports,
                        {
				"dpid",		$dpid,
                                "port_id",      $output_row->{'port_id'},
                                "port_address", $output_row->{'port_address'}
                        }
                );

        }

        return $ports;
}



sub get_group_output_ports {

	my $self = shift;
	my %args = @_;

	my $group_id = $args{'group_id'};

	my $ports = [];

	my $sth = $self->_prepare_query(
"select group_port.port_id , port_address, flow_group.output_switch from switch_port, flow_group, group_port where flow_group.group_id = ? and output_switch = switch_port.switch_id "
		  . " and group_port.port_id = switch_port.port_id and port_direction = 1 and group_port.group_id = ?"
	);
	$sth->execute( $group_id, $group_id );

	while ( my $group_row = $sth->fetchrow_hashref() ) {

		push(
			@$ports,
			{
				"port_id",      $group_row->{'port_id'},
				"port_address", $group_row->{'port_address'}
			}
		);

	}

	return $ports;

}

sub get_all_xconnect {

	my $self = shift;
	my %args = @_;

	my $xvalues = [];

	my $sth = $self->_prepare_query(
"select switch_1_id, s1.switch_name as switch1_name,  switch_2_id, s2.switch_name as switch2_name , port_1_id, port_2_id from x_connect INNER JOIN switch s1 ON switch_1_id = s1.datapath_id "
		  . "INNER JOIN   switch s2 ON switch_2_id = s2.datapath_id" );
	$sth->execute();

	while ( my $x_connect_row = $sth->fetchrow_hashref() ) {

		push(
			@$xvalues,
			{
				"switch1_name", $x_connect_row->{'switch1_name'},
				"switch2_name", $x_connect_row->{'switch2_name'},
				"switch_1_id",  $x_connect_row->{'switch_1_id'},
				"switch_2_id",  $x_connect_row->{'switch_2_id'},
				"port_1_id",    $x_connect_row->{'port_1_id'},
				"port_2_id",    $x_connect_row->{'port_2_id'},

			}
		);

	}

	return $xvalues;

}

sub get_xconnect {

	my $self = shift;
	my %args = @_;

	my $switch_id = $args{'switch_id'};

	my $xvalues = [];

	my $sth = $self->_prepare_query(
"select switch_1_id, s1.switch_name as switch1_name,  switch_2_id, s2.switch_name as switch2_name , port_1_id, port_2_id from x_connect INNER JOIN switch s1 ON switch_1_id = s1.datapath_id "
		  . "INNER JOIN   switch s2 ON switch_2_id = s2.datapath_id where x_connect.switch_1_id = ?"
	);
	$sth->execute($switch_id);

	while ( my $x_connect_row = $sth->fetchrow_hashref() ) {

		push(
			@$xvalues,
			{
				"switch1_name", $x_connect_row->{'switch1_name'},
				"switch2_name", $x_connect_row->{'switch2_name'},
				"switch_1_id",  $x_connect_row->{'switch_1_id'},
				"switch_2_id",  $x_connect_row->{'switch_2_id'},
				"port_1_id",    $x_connect_row->{'port_1_id'},
				"port_2_id",    $x_connect_row->{'port_2_id'},

			}
		);

	}

	return $xvalues;

}

sub get_switch_ports {

	my $self = shift;
	my %args = @_;

	my $switch_id = $args{'switch_id'};

	my $ports = [];

	my $sth = $self->_prepare_query(
		"select port_id, port_address from switch_port where switch_id = ?");
	$sth->execute($switch_id);

	while ( my $port_row = $sth->fetchrow_hashref() ) {
		push(
			@$ports,
			{
				"port_id",      $port_row->{'port_id'},
				"port_address", $port_row->{'port_address'}
			}
		);
	}

	return $ports;
}

####################now we put the shared/test methods
#
sub print_db_schema_file {
	my $self = shift;

	my $module_dir = File::ShareDir::dist_dir('OESS-Database');

	warn "module_dir=$module_dir\n";

	my $file_path = File::ShareDir::dist_file( 'OESS-Database', 'nddi.sql' );

	warn "file_path=$file_path\n";
}

sub reset_database {

	my $self = shift;

	if ( !$ENABLE_DEVEL ) {
		$self->_set_error(
			"will not reset the db unless devel has been enabled");
		return undef;
	}

	my $dbh = $self->{'dbh'};

	my $xml = XML::Simple::XMLin( $self->{'config'} );

	my $username = $xml->{'credentials'}->{'username'};
	my $password = $xml->{'credentials'}->{'password'};
	my $database = $xml->{'credentials'}->{'database'};

	my $import_filename =
	  File::ShareDir::dist_file( 'OESS-Database', 'nddi.sql' );

	$dbh->do("drop database $database");
	$dbh->do("create database $database");

	# satisfy taint mode
	$ENV{PATH} = "/usr/bin";

	my $success = (
		!system(
"/usr/local/mysql-5.5.16-osx10.6-x86_64/bin/mysql -u$username -p$password $database < $import_filename"
		)
	);

	# reconnect to the database
	$self->{'dbh'} =
	  DBI->connect( "DBI:mysql:$database", $username, $password );

	return $success;
}

sub add_into {

	my $self = shift;
	my %args = @_;

	if ( !$ENABLE_DEVEL ) {
		$self->_set_error(
			"will not reset the db unless devel has been enabled");
		return undef;
	}

	my $filename = $args{'xml_dump'};
	my $dbh      = $self->{'dbh'};

	my $xs = XML::Simple->new(
		ForceArray => [
			'user', 'workgroup', 'user_member', 'network',
			'node', 'interface', 'link',        'circuit',
			'path', 'member_link'
		],
		KeyAttr => {
			user        => 'email',
			termination => 'interface',
			workgroup   => 'name',
			network     => 'name',
			node        => 'name',
			interface   => 'name',
			'link'      => 'name',
			circuit     => 'name',
			path        => 'type'
		}
	);

	my $db_dump = $xs->XMLin($filename) or die;

	#users->
	my $users = $db_dump->{'user'};
	my $user_query =
	  "insert ignore into user (email,given_names,family_name) VALUES (?,?,?)";
	my $user_sth = $self->_prepare_query($user_query) or return undef;

	foreach my $user_name ( keys %$users ) {
		$user_sth->execute(
			$user_name,
			$users->{$user_name}->{'given_names'},
			$users->{$user_name}->{'last_name'}
		) or return undef;
	}

	#do workgroups
	my $workgroups = $db_dump->{'workgroup'};

	my $workgroup_query =
	  "insert ignore into workgroup (name,description) VALUES (?,?)";
	my $workgroup_sth = $self->_prepare_query($workgroup_query)
	  or return undef;

	my $workgroup_select_query =
	  "select workgroup_id from workgroup where name=?";
	my $workgroup_select_sth = $self->_prepare_query($workgroup_select_query)
	  or return undef;

	my $user_workgroup_insert =
'insert into user_workgroup_membership (workgroup_id,user_id) VALUES (?,(select user_id from user where email=?))';
	my $user_workgroup_insert_sth =
	  $self->_prepare_query($user_workgroup_insert)
	  or return undef;

	foreach my $workgroup_name ( keys %$workgroups ) {

		$workgroup_sth->execute( $workgroup_name,
			$workgroups->{$workgroup_name}->{'description'} )
		  or return undef;

		$workgroup_select_sth->execute($workgroup_name) or return undef;

		my $workgroup_db_id;
		my $row;

		if ( $row = $workgroup_select_sth->fetchrow_hashref() ) {
			$workgroup_db_id = $row->{'workgroup_id'};
		}

		return undef unless $workgroup_db_id;

		my $users_in_workgroup =
		  $workgroups->{$workgroup_name}->{'user_member'};

		foreach my $user (@$users_in_workgroup) {
			$user_workgroup_insert_sth->execute( $workgroup_db_id, $user )
			  or return undef;
		}
	}

	#now networks
	my $network_insert_query =
	  "insert into network (name,longitude, latitude) VALUES (?,?,?)";
	my $network_insert_sth = $self->_prepare_query($network_insert_query)
	  or return undef;

	my $insert_node_query =
"insert into node (name,longitude,latitude, network_id) VALUES (?,?,?,(select network_id from network where name=?))";
	my $insert_node_sth = $self->_prepare_query($insert_node_query)
	  or return undef;

	my $insert_node_instantiaiton_query =
"insert into node_instantiation (node_id,end_epoch,start_epoch,management_addr_ipv4,dpid) VALUES ((select node_id from node where name=?),-1,unix_timestamp(now()),inet_aton(?),?)";
	my $insert_node_instantiaiton_sth =
	  $self->_prepare_query($insert_node_instantiaiton_query)
	  or return undef;

	my $insert_interface_query =
"insert into interface (name,description,node_id) VALUES(?,?,(select node_id from node where name=?)) ";
	my $insert_interface_sth = $self->_prepare_query($insert_interface_query);

	my $select_interface_query =
"select interface_id from interface where name=? and node_id=(select node_id from node where name=?)";
	my $select_interface_sth = $self->_prepare_query($select_interface_query)
	  or return undef;

	my $insert_interface_instantiaiton_query =
"insert into interface_instantiation (interface_id,end_epoch,start_epoch,capacity_mbps,mtu_bytes) VALUES (?,-1,unix_timestamp(now()),10000,9000)";
	my $insert_interface_instantiaiton_sth =
	  $self->_prepare_query($insert_interface_instantiaiton_query)
	  or return undef;

	my $networks = $db_dump->{'network'};

	foreach my $network_name ( keys %$networks ) {
		$network_insert_sth->execute(
			$network_name,
			$networks->{$network_name}->{'longitude'},
			$networks->{$network_name}->{'latitude'}
		) or return undef;

		my $nodes = $networks->{$network_name}->{'node'};

		foreach my $node_name ( keys %$nodes ) {
			my $node = $nodes->{$node_name};

			$insert_node_sth->execute( $node_name, $node->{'longitude'},
				$node->{'latitude'}, $network_name );

			$insert_node_instantiaiton_sth->execute( $node_name,
				$node->{'managemnt_addr'},
				$node->{'dpid'} );

			my $interfaces = $node->{'interface'};

			foreach my $interface_name ( keys %$interfaces ) {
				my $interface = $interfaces->{$interface_name};

				my $interface_db_id;

				$insert_interface_sth->execute( $interface_name,
					$interface->{'description'}, $node_name );

				$select_interface_sth->execute( $interface_name, $node_name );

				if ( my $row = $select_interface_sth->fetchrow_hashref() ) {
					$interface_db_id = $row->{'interface_id'};
				}

				return undef unless $interface_db_id;

				$insert_interface_instantiaiton_sth->execute($interface_db_id)
				  or return undef;

			}
		}

		#now links
		my $links = $networks->{$network_name}->{'link'};

		my $insert_new_link_query = "insert into link (name) VALUES (?)";
		my $insert_new_link_sth = $self->_prepare_query($insert_new_link_query)
		  or return undef;

		my $insert_new_link_instantiation =
"insert into link_instantiation (link_id,end_epoch,start_epoch,interface_a_id,interface_b_id) VALUES ( (select link_id from link where name=?),-1,unix_timestamp(now()),?,?)";
		my $insert_new_link_instantiation_sth =
		  $self->_prepare_query($insert_new_link_instantiation)
		  or return undef;

		foreach my $link_name ( keys %$links ) {
			my $link = $links->{$link_name};

			$insert_new_link_sth->execute($link_name) or return undef;

			my ( $node_a_name, $node_a_interface_name ) =
			  split( /:/, $link->{'interface_a'} );
			my ( $node_b_name, $node_b_interface_name ) =
			  split( /:/, $link->{'interface_b'} );

			my $interface_a_db_id = $self->get_interface_id_by_names(
				node      => $node_a_name,
				interface => $node_a_interface_name
			);

			my $interface_b_db_id = $self->get_interface_id_by_names(
				node      => $node_b_name,
				interface => $node_b_interface_name
			);

			$insert_new_link_instantiation_sth->execute( $link_name,
				$interface_a_db_id, $interface_b_db_id );
		}
	}

	#now circuits!
	my $insert_circuit_query =
	  "insert into circuit (name,description) VALUES (?,?)";
	my $insert_circuit_sth = $self->_prepare_query($insert_circuit_query)
	  or return undef;

	my $insert_circuit_instantiation_query =
"insert into circuit_instantiation (circuit_id,end_epoch,start_epoch,reserved_bandwidth_mbps,circuit_state,modified_by_user_id) VALUES ((select circuit_id from circuit where name=?),-1,unix_timestamp(now()),?,?,?)";
	my $insert_circuit_instantiation_sth =
	  $self->_prepare_query($insert_circuit_instantiation_query)
	  or return undef;

	my $insert_path_query =
"insert into path (path_type,circuit_id) VALUES (?,(select circuit_id from circuit where name=?))";
	my $insert_path_sth = $self->_prepare_query($insert_path_query)
	  or return undef;

	my $insert_path_inst_query =
"insert into path_instantiation ( path_id,end_epoch,start_epoch,internal_vlan_id,path_state) VALUES ((select path_id from path where path_type=? and circuit_id=(select circuit_id from circuit where name=?)),-1,unix_timestamp(now()),?,?)";
	my $insert_path_inst_sth = $self->_prepare_query($insert_path_inst_query)
	  or return undef;

	my $insert_link_path_membership_query =
"insert into link_path_membership (path_id,link_id,end_epoch,start_epoch) VALUES ((select path_id from path where path_type=? and circuit_id=(select circuit_id from circuit where name=?)),?,-1, unix_timestamp(now()) )";
	my $insert_link_path_membership_sth =
	  $self->_prepare_query($insert_link_path_membership_query)
	  or return undef;

	my $insert_circuit_edge_interface_membership_query =
"insert into circuit_edge_interface_membership (circuit_id,interface_id,extern_vlan_id,end_epoch,start_epoch) VALUES ((select circuit_id from circuit where name=?),?,?,-1,unix_timestamp(now()))";
	my $insert_circuit_edge_interface_membership_sth =
	  $self->_prepare_query($insert_circuit_edge_interface_membership_query)
	  or return undef;

	my $circuits = $db_dump->{'circuit'};
	my $i        = 100;
	foreach my $circuit_name ( keys %$circuits ) {

		my $circuit = $circuits->{$circuit_name};

		$insert_circuit_sth->execute( $circuit_name, $circuit->{'description'} )
		  or return undef;

		$insert_circuit_instantiation_sth->execute( $circuit_name,
			$circuit->{'reserved_bw'},
			'active', 1 )
		  or return undef;

		#now paths
		my $paths = $circuit->{'path'};
		foreach my $path_type ( keys %$paths ) {
			$insert_path_sth->execute( $path_type, $circuit_name )
			  or return undef;

			$insert_path_inst_sth->execute( $path_type, $circuit_name, $i,
				'active' );

			$i++;
			my $path_links = $paths->{$path_type}->{'member_link'};
			foreach my $link_name (@$path_links) {
				my $link_db_id =
				  $self->get_link_id_by_name( link => $link_name );

				$insert_link_path_membership_sth->execute( $path_type,
					$circuit_name, $link_db_id )
				  or return undef;
			}
		}

		#now terminations
		my $end_points = $circuit->{'termination'};
		foreach my $end_interface_long_name ( keys %$end_points ) {
			my $end_point = $end_points->{$end_interface_long_name};

			my ( $node_name, $interface_name ) =
			  split( /:/, $end_interface_long_name );

			my $interface_db_id = $self->get_interface_id_by_names(
				node      => $node_name,
				interface => $interface_name
			);

			$insert_circuit_edge_interface_membership_sth->execute(
				$circuit_name, $interface_db_id, $end_point->{'vlan'} )
			  or return undef;
		}

	}

	return 1;
}

return 1;
