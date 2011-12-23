#!/opt/local/bin/perl5.12

use strict;
use warnings;



package Flowscale::Controller;

use REST::Client;
use XML::Simple;
use RRDs;
use Data::Dumper qw(Dumper);

sub new {
	my $that = shift;
	my $class = ref($that) || $that;

	my %args = (
		config =>
'/home/chsmall/git/FlowScale/flowscale_ui/conf/controller.xml',
		@_,
	);

	my $self = \%args;
	bless $self, $class;

	my $config_filename = $args{'config'};

	my $config = XML::Simple::XMLin($config_filename);

	my $hostname        = $config->{'connection'}->{'hostname'};
	my $controller_port = $config->{'connection'}->{'port'};

	my $client = REST::Client->new();
	my $request_headers = { group_type_id => "test" };

	$client->setHost("$hostname:$controller_port");

	if ( !$client ) {
		return undef;
	}

	$self->{'client'} = $client;

	return $self;
}

sub add_new_group {

	my $self   = shift;
	my $client = $self->{'client'};
	my %args   = @_;


	$client->addHeader( 'action',              'addGroup' );
	$client->addHeader( 'groupId',            $args{'group_id'} );
	$client->addHeader( 'groupName',            $args{'group_name'} );
	$client->addHeader( 'inputSwitch',         $args{'input_switch'} );
	$client->addHeader( 'outputSwitch',         $args{'output_switch'} );
	$client->addHeader( 'inputPorts',          $args{'input_ports'} );
	$client->addHeader( 'outputPorts',         $args{'output_ports'} );
	$client->addHeader( 'type',                $args{'group_type'} );
	$client->addHeader( 'maximumFlowsAllowed', $args{'maximum_allowed'} );
	$client->addHeader( 'values',              $args{'group_values'} );
	$client->addHeader( 'priority',              $args{'priority'} );
	$client->GET('/');

	return $client->responseContent();

}

sub edit_group {
	

	my $self   = shift;
	my $client = $self->{'client'};
	my %args = @_;
	$client->addHeader( 'action',         'editGroup' );
	$client->addHeader( 'groupId',        $args{'group_id'} );
	$client->addHeader( 'updateCommands', $args{'commands'} );
	
	$client->GET('/');

	return $client->responseContent();

}

sub delete_group {

	
	my $self   = shift;
	my $client = $self->{'client'};
	my %args   = @_;

	$client->addHeader( 'action',  'deleteGroup' );
	$client->addHeader( 'groupId', $args{'group_id'} );

	$client->GET('/');

	return $client->responseContent();

}

sub get_gibberish {
	my $self   = shift;
	my $client = $self->{'client'};

	$client->addHeader( 'CustomHeader', 'Value' );

	$client->GET('/');

	return $client->responseContent();

}

sub get_statistics {

	my $self = shift;

	my $client = $self->{'client'};

	my $switch_id = args { 'switch_id' };

	$client->addHeader( 'action',     'getSwitchStatistics' );
	$client->addHeader( 'datapathId', $switch_id );

	return $client->responseContent();

}

sub get_switch_status {

	my $self = shift;
	my %args = @_;

	my $client = $self->{'client'};

	my $switch_id = $args{'switch_id'};

	$client->addHeader( 'action',     'getSwitchStatus' );
	$client->addHeader( 'datapathId', $switch_id );

	$client->GET('/');

	return $client->responseContent();

}


sub get_switch_statistics{
		my $self = shift;
	my %args =@_;
	my $client = $self->{'client'};
	my $datapath_id = $args{'switch_id'};
	my $type = $args{'type'};
	$client->addHeader( 'action',     'getSwitchStatistics' );
	$client->addHeader( 'datapathId', $datapath_id );
	$client->addHeader('type' , $type );
	$client->GET('/');

	return $client->responseContent();
	
	
}

sub get_switch_ports{
	my $self = shift;
	my %args =@_;
	my $client = $self->{'client'};
	my $datapath_id = $args{'switch_id'};
	$client->addHeader( 'action',     'getSwitchPorts' );
	$client->addHeader( 'datapathId', $datapath_id );

	$client->GET('/');

	return $client->responseContent();
	
	
}

sub add_switch{
	my $self = shift;
	my %args = @_;

	my $client = $self->{'client'};

	my $datapath_id = $args{'datapath_id'};

	$client->addHeader( 'action',     'addSwitch' );
	$client->addHeader( 'datapathId', $datapath_id );

	$client->GET('/');

	return $client->responseContent();
	
	
	
}

sub get_capstats{
    my $self = shift;
        my %args = @_;

        my $client = $self->{'client'};

  #      my $datapath_id = $args{'datapath_id'};
  #
                $client->addHeader( 'action',     'getCapstats' );
  #               #       $client->addHeader( 'datapathId', $datapath_id );
  #
                         $client->GET('/');
  #
                                 return $client->responseContent();
  #
  #
  #



}





return 1;
