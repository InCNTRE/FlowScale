#!/bin/bash


set -ex


BASEDIR=`pwd`

rm -rf temp

mkdir -p temp/plugins

mv plugins/* temp/plugins/

java -jar $ECLIPSE_HOME/plugins/org.eclipse.equinox.launcher_*.jar \
   -application org.eclipse.equinox.p2.publisher.FeaturesAndBundlesPublisher \
   -metadataRepository file://$BASEDIR \
   -artifactRepository file://$BASEDIR \
   -source $BASEDIR/temp \
   -publishArtifacts

rm -rf temp
