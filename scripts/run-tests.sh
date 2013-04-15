#!/bin/sh

SCRIPTS_DIR=`dirname $0`
REPORT=${SCRIPTS_DIR}/../report-`date +%y%m%d-%H%M`.txt

run(){
	SCRIPT=$1
	THREADS=$2
	MESSAGES=$3
	OPTS=$4
	
	for a in {1..1}; do
	#for a in {1..10}; do
		echo $SCRIPT ${THREADS} ${MESSAGES} ${OPTS}
		$SCRIPT ${THREADS} ${MESSAGES} ${OPTS} 1>>${REPORT} 2>&1

		for f in $SCRIPTS_DIR/../logs/*; 
		do
			test -f $f && rm $f
		done
	done
}

for t in 1;
do
	for c in 1024
	#for c in 65536 131072 524288 1048576 2097152 4194304
	do
		for app in log4j.sh gflog4j.sh logger.sh dlogger.sh jcl-logger.sh
		do	
			run ${SCRIPTS_DIR}/$app $t $c && sleep 5
		done	

		for app in logger.sh dlogger.sh
		do		
			run ${SCRIPTS_DIR}/$app $t $c false && sleep 5
		done	
	done
done
