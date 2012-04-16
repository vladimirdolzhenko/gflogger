#!/bin/sh

DIR=`dirname $0`
. ${DIR}/common.sh

run logback logback "$1" "$2" perftest.LogBackExample "-Dlogback.configurationFile=logback.xml"