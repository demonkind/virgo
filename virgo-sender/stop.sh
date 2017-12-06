#!/bin/sh

#JAVA_HOME=/root/JDK/jdk1.6.0_24
echo "JAVA_HOME=$JAVA_HOME"

APP_MAINCLASS=worker.assign.SenderClient

psid=0

checkpid() {
javaps=`jps -l | grep $APP_MAINCLASS`
if [ -n "$javaps" ]; then
echo "jps=$javaps"
psid=`echo $javaps | awk '{print $1}'`
else
psid=0
fi
}

stop() {
checkpid
if [ $psid -ne 0 ]; then
echo -n "Stopping $APP_MAINCLASS ...(pid=$psid) "
kill -9 $psid
if [ $? -eq 0 ]; then
echo "[STOP OK $psid]"
else
echo "[STOP Failed $psid]"
fi
else
echo "not found $APP_MAINCLASS psid."
fi
}

if [ "$1" = "sender" ]; then
  echo "stop..."
  APP_MAINCLASS=worker.assign.SenderClient
  stop
elif [ "$1" = "assign" ]; then
  echo "stop..."
  APP_MAINCLASS=worker.assign.ZkManager
  stop
else
  	echo "Usage: stop.sh {sender|assign}" >&2
    echo "       sender:     stop sender server"
    echo "       assign:     stop ZkManager server"
	echo "unknown command" $1
fi