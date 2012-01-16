#!/bin/sh

DIR=`dirname $0`
. ${DIR}/common.sh

run log4j log4j "$1" "$2" perftest.Log4JExample "-Dlog4j.configuration=log4j.xml"