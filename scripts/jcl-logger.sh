#!/bin/sh

DIR=`dirname $0`
. ${DIR}/common.sh

DIRECT=true
NAME="jcl-gflogger"
if [ -n "$3" ]; then
	DIRECT="$3"
	NAME="jcl-gflogger-d"
fi

run $NAME jcl-gflogger "$1" "$2" perftest.JCLGFLoggerExample  "-Dgflogger.direct=$DIRECT -Dorg.apache.commons.logging.Log=gflogger.jcl.LogImpl"