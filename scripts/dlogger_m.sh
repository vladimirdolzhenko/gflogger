#!/bin/sh

DIR=`dirname $0`
. ${DIR}/common.sh

DIRECT=true
NAME="dgflogger_m+d"
if [ -n "$3" ]; then
	DIRECT="$3"
	NAME="dgflogger_m-d"
fi

run $NAME dgflogger_m "$1" "$2" org.gflogger.perftest.DLoggerExample "-Dgflogger.direct=$DIRECT -Dgflogger.filename=./logs/dgflogger_m.log -Dgflogger.multibyte=true -Dgflogger.service.count=1024 -Dgflogger.append=false -Dgflogger.internalDebugEnabled=false"
