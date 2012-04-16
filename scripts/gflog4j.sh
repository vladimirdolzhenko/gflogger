#!/bin/sh

DIR=`dirname $0`
. ${DIR}/common.sh

run gflog4j gflog4j "$1" "$2" perftest.Log4JLoggerFacadeExample "-Dlog4j.configuration=gflog4j.xml"
