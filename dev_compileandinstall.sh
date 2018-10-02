#!/bin/bash

export ONOS_ROOT=~/work/onos
source $ONOS_ROOT/tools/dev/bash_profile

mvn clean install

onos-app 172.16.230.128 activate org.onosproject.sdnwise
onos-app 172.16.230.128 reinstall! target/onos-sdwsn-dynamic-routing-1.0-SNAPSHOT.oar

