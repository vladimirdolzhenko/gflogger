#!/bin/sh

DIR=`dirname $0`
. ${DIR}/common.sh

DIRECT=true
NAME="gflogger"
if [ -n "$3" ]; then
	DIRECT="$3"
	NAME="gflogger-d"
fi

run $NAME gflogger "$1" "$2" perftest.LoggerExample  "-Dgflogger.direct=$DIRECT"