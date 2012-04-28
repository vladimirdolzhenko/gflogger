#!/bin/sh

REPORT=report-`date +%y%m%d-%H%M`.txt

run(){
	SCRIPT=$1
	THREADS=$2
	MESSAGES=$3
	OPTS=$4
	
	for a in {1..10}; do
		$SCRIPT ${THREADS} ${MESSAGES} ${OPTS} 1>>${REPORT} 2>&1
		rm logs/*
	done
}

for t in 1 2 4 6 ;
do
	for c in 65536 131072 524288 1048576 2097152 4194304;
	do
		run scripts/log4j.sh $t $c 
		sleep 5
		run scripts/logback.sh $t $c
		sleep 5
		run scripts/gflog4j.sh $t $c
		sleep 5
		run scripts/logger.sh $t $c
		sleep 5
		run scripts/dlogger.sh $t $c
		sleep 5
		run scripts/jcl-logger.sh $t $c
		sleep 5
		run scripts/logger.sh $t $c false
		sleep 5
		run scripts/dlogger.sh $t $c false
		sleep 5
	done
done
