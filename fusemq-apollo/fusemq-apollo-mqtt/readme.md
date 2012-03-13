# The MQTT Protocol for Apollo

## Overview

This plugin adds MQTT v3.1 protocol support to Apache Apollo message brokers.
All MQTT v3.1 feature are supported:

* QoS 0, 1, and 2
* Retained messages
* Clean and non-clean sessions
* Client authentication

## Prequisites

An installation of:

  * [Apollo 1.1](http://www.apache.org/dyn/closer.cgi?path=activemq/activemq-apollo/1.1)

## Installing

Download and copy the [fusemq-apollo-mqtt-1.0-uber.jar][release_jar] into 
the your Apollo's `lib` directory then restart your broker.

[release_jar]: http://repo.fusesource.com/nexus/content/repositories/public/org/fusesource/fuse-extra/fusemq-apollo-mqtt/1.0/fusemq-apollo-mqtt-1.0-uber.jar
[snapshot_jar]: http://repo.fusesource.com/nexus/service/local/artifact/maven/redirect?r=snapshots&g=org.fusesource.fuse-extra&a=fusemq-apollo-mqtt&v=99-master-SNAPSHOT&c=uber

## Validating the Installation

You can use the simple MQTT listener and publisher command line apps included 
in the mqtt-client library.  To use, download the 
[mqtt-client-1.0-uber.jar][client_release_jar] then in a command line 
window, run a MQTT message listener on the `test` topic on your local apollo broker
by running:

	java -cp mqtt-client-1.0-uber.jar org.fusesource.mqtt.cli.Listener -h tcp://localhost:61613 -u admin -p password  -t test

Then in a seperate command line window then run a publisher to send a `hello` message
to the `test` topic by running:

	java -cp mqtt-client-1.0-uber.jar org.fusesource.mqtt.cli.Publisher -h tcp://localhost:61613 -u admin -p password  -t test -m hello

Your listener's command line process should then print to the screen the `hello` message.

[client_release_jar]: http://repo.fusesource.com/nexus/content/repositories/public/org/fusesource/fuse-extra/fusemq-apollo-mqtt/1.0/fusemq-apollo-mqtt-1.0-uber.jar
