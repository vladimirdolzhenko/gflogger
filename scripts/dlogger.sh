#!/bin/sh

DIR=`dirname $0`
. ${DIR}/common.sh

DIRECT=true
NAME="dgflogger+d"
if [ -n "$3" ]; then
	DIRECT="$3"
	NAME="dgflogger-d"
fi

#run $NAME dgflogger "$1" "$2" org.gflogger.perftest.DLoggerExample "-Dgflogger.direct=$DIRECT -Dgflogger.filename=./logs/dgflogger.log -Dgflogger.service.count=1024 -Dgflogger.append=false -ea -Dgflogger.internalDebugEnabled=true -Dgflogger.bufferedIOThreshold=100000"
run $NAME dgflogger "$1" "$2" org.gflogger.perftest.DLoggerExample "-Dgflogger.direct=$DIRECT -Dgflogger.filename=./logs/dgflogger.log -Dgflogger.service.count=1024 -Dgflogger.append=false -Dgflogger.internalDebugEnabled=false"
