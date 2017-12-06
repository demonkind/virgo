#!/bin/sh

BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd ${BASEDIR}

if [ ! -d "${BASEDIR}/applogs" ] ; then
  mkdir -p "${BASEDIR}/applogs"
fi

if [ "$1" = "console" ]; then
  echo "starting..."
  echo "output: ${BASEDIR}/applogs"
  exec java -Xms64m -Xmx512m -XX:MaxPermSize=256m -cp "target/virgo-console-1.0.0-SNAPSHOT.jar:target/dependency/*" com.huifu.virgo.console.VirgoConsoleServer > "${BASEDIR}/applogs/console_log.out" 2>&1 &
  echo "Dispatcher server start successfully..."
else
  	echo "Usage: run.sh {console}" >&2
    echo "       console:     start console server"
	echo "unknown command" $1 
fi
