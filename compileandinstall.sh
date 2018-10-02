#!/bin/bash

export ONOS_ROOT=~/work/onos
source $ONOS_ROOT/tools/dev/bash_profile

mvn clean install

onos-app onos.scitab.org activate org.onosproject.sdnwise
onos-app onos.scitab.org reinstall! target/onos-sdwsn-dynamic-routing-1.0-SNAPSHOT.oar 

