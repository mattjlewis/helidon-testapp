#!/bin/bash

java -Djavax.net.ssl.trustStore=/Users/matt/devel/HelidonTxTest/wso2carbon.jks -Djavax.net.ssl.trustStorePassword=wso2carbon -jar target/helidon-testapp-services.jar
