#!/bin/sh


DIR="$(dirname "$(readlink -f $0})")"
. ${DIR}/common.sh

run logback "$1" "$2" perftest.LogBackExample "-Dlogback.configurationFile=logback.xml"