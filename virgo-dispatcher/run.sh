#!/bin/sh

BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd ${BASEDIR}

if [ ! -d "${BASEDIR}/applogs" ] ; then
  mkdir -p "${BASEDIR}/applogs"
fi

if [ "$1" = "dispatcher" ]; then
  echo "starting..."
  echo "output: ${BASEDIR}/applogs"
  exec java -Xms512m -Xmx2048m -XX:MaxPermSize=256m -cp "target/virgo-dispatcher-1.0.0-SNAPSHOT.jar:target/dependency/*" com.huifu.virgo.routing.DispatcherServer > "${BASEDIR}/applogs/dispatcher_log.out" 2>&1 &
  echo "Dispatcher server start successfully..."
else
  	echo "Usage: run.sh {dispatcher}" >&2
    echo "       Dispatcher:     start Dispatcher server"
	echo "unknown command" $1 
fi
