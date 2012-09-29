#!/bin/sh

DIR=`dirname $0`
. ${DIR}/common.sh

DIRECT=true
NAME="gflogger+d"
if [ -n "$3" ]; then
	DIRECT="$3"
	NAME="gflogger-d"
fi

#run $NAME gflogger "$1" "$2" org.gflogger.perftest.LoggerExample  "-Dgflogger.direct=$DIRECT -Dgflogger.filename=/dev/null"
run $NAME gflogger "$1" "$2" org.gflogger.perftest.LoggerExample  "-Dgflogger.direct=$DIRECT -Dgflogger.filename=./logs/gflogger.log"