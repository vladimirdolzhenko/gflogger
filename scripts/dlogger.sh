#!/bin/sh

DIR="$(dirname "$(readlink -f $0})")"
. ${DIR}/common.sh

run dgflogger "$1" "$2" gflogger.disruptor.DLoggerExample