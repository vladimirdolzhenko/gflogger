#!/bin/sh

DIR="$(dirname "$(readlink -f $0})")"
. ${DIR}/common.sh

run gflogger "$1" "$2" perftest.LoggerExample