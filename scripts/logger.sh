#!/bin/sh

DIR=`dirname $0`
. ${DIR}/common.sh

DIRECT=true
NAME="gflogger+d"
if [ -n "$3" ]; then
	DIRECT="$3"
	NAME="gflogger-d"
fi

#run $NAME gflogger "$1" "$2" perftest.LoggerExample  "-Dgflogger.direct=$DIRECT -Dgflogger.filename=/dev/null -Dgflogger.service.count=1024 -Dgflogger.append=false -Dgflogger.internalDebugEnabled=false -Dgflogger.bufferedIOThreshold=1000"
run $NAME gflogger "$1" "$2" perftest.LoggerExample  "-Dgflogger.direct=$DIRECT -Dgflogger.filename=./logs/gflogger.log -Dgflogger.service.count=1024 -Dgflogger.append=false -Dgflogger.internalDebugEnabled=false"