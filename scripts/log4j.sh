#!/bin/sh


DIR="$(dirname "$(readlink -f $0})")"
. ${DIR}/common.sh

run log4j "$1" "$2" logger.Log4JLogger "-Dlog4j.configuration=test-log4j.xml"