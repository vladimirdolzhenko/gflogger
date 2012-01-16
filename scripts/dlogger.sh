#!/bin/sh

DIR=`dirname $0`
. ${DIR}/common.sh

DIRECT=true
NAME="dgflogger"
if [ -n "$3" ]; then
	DIRECT="$3"
	NAME="dgflogger-d"
fi

run $NAME dgflogger "$1" "$2" perftest.DLoggerExample "-Dgflogger.direct=$DIRECT"