#!/bin/sh

DIR=`dirname $0`
. ${DIR}/common.sh

run logback logback "$1" "$2" org.gflogger.perftest.LogBackExample "-Dlogback.configurationFile=logback.xml"