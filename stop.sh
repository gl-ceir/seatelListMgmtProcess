#!/bin/bash
process=seatelfileprocess.jar
instances=1
script_name="./stop.sh"
count=`ps -ef | grep $process | grep -v grep | grep -v "$script_name" | wc -l`
if [ $count != $instances ]
then
        if [ $count -eq 0 ]
        then
                echo "No Process Running"
        else
                echo "Instances Count Not Equal : "$count
                echo
                ps -ef | grep $process | grep -v grep | grep -v "./stop.sh"
        fi
elif [ $count -eq $instances ] && [ $count -ne 0 ]

then
        pid=`ps -ef | grep "$process" | grep -v grep | grep -v "$script_name" | awk '{print $2}'`
        kill -9 $(ps aux | grep "$process" | grep -v grep | grep -v "$script_name" | awk '{print $2}')
        echo "Process killed"
else
        echo "No Process Running"
fi
