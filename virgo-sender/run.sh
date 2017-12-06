#!/bin/sh

BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd ${BASEDIR}

if [ ! -d "${BASEDIR}/applogs" ] ; then
  mkdir -p "${BASEDIR}/applogs"
fi

if [ "$1" = "sender" ]; then
  echo "starting..."
  echo "output: ${BASEDIR}/applogs"
  exec java -Xms512m -Xmx4096m -Djsse.enableSNIExtension=false -XX:MaxPermSize=256m -cp "target/virgo-sender-1.0.0-SNAPSHOT.jar:target/dependency/*" worker.assign.SenderClient > "${BASEDIR}/applogs/sender_log.out" 2>&1 &
  echo "Sender start successfully..."
elif [ "$1" = "assign" ]; then
  echo "starting..."
  echo "output: ${BASEDIR}/applogs"
  exec java -Xms64m -Xmx512m -XX:MaxPermSize=256m -cp "target/virgo-sender-1.0.0-SNAPSHOT.jar:target/dependency/*" worker.assign.ZkManager > "${BASEDIR}/applogs/zkManager_log.out" 2>&1 &
  echo "ZkManager start successfully..."
else
  	echo "Usage: run.sh {sender|assign}" >&2
    echo "       sender:     start sender server"
    echo "       assign:     start ZkManager server"
	echo "unknown command" $1 
fi
