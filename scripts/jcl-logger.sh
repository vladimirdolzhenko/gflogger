#!/bin/sh

DIR=`dirname $0`
. ${DIR}/common.sh

DIRECT=true
NAME="jcl-gflogger+d"
if [ -n "$3" ]; then
	DIRECT="$3"
	NAME="jcl-gflogger-d"
fi

run $NAME jcl-gflogger "$1" "$2" perftest.JCLGFLoggerExample  "-Dgflogger.direct=$DIRECT -Dorg.apache.commons.logging.Log=gflogger.jcl.LogImpl -Dgflogger.filename=./logs/jcl-gflogger.log -Dgflogger.service.count=1024 -Dgflogger.append=false"