#!/bin/sh

REPORT=report-`date +%y%m%d-%H%M`.txt

run(){
    SCRIPT=$1
    THREADS=$2
    MESSAGES=$3
    
    for a in 1 2 3; do
        $SCRIPT ${THREADS} ${MESSAGES} 1>>${REPORT} 2>&1
        rm logs/*
    done
}

for t in 1 2 4 8 ;
do
	for c in 1024 131072 1048576 4194304;
	do
		run scripts/log4j.sh $t $c 
		run scripts/logback.sh $t $c
		run scripts/logger.sh $t $c
		run scripts/dlogger.sh $t $c
	done
done
