#!/bin/bash
source ~/.bash_profile
set -x
VAR=""
build_path="${APP_HOME}/seatelfileprocess/"
build="seatelfileprocess.jar"
cd $build_path
status=`ps -ef | grep $build | grep application.properties | grep java`
if [ "$status" != "$VAR" ]
  then
    echo "Process Already Running"
  else
    echo "Starting Process"
    java -Dlog4j.configurationFile=file:./log4j2xml -jar $build --spring.config.location=file:/u01/eirsapp/configuration/configuration.properties,file:./application.properties 1>/dev/null 2>/dev/null &
    echo "Process Started"
fi