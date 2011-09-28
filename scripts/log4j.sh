#!/bin/sh


DIR="$(dirname "$(readlink -f $0})")"
. ${DIR}/common.sh

run log4j "$1" "$2" perftest.Log4JExample "-Dlog4j.configuration=log4j.xml"