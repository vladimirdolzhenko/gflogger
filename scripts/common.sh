#!/bin/sh

SCRIPTS_DIR=`dirname $0`
LOG_DIR=${SCRIPTS_DIR}/../logs/
run(){
	NAME=$1
	LOG_NAME=$2
	THREADS=$3
	MESSAGES=$4
	MAINCLASS=$5
	JAVA_EXTRA_OPTS=$6
	
	echo "${NAME}, number of threads: "${THREADS}", messages: ${MESSAGES}"

	JAVA_ASSEMBLY=""
	
	#JAVA_ASSEMBLY="-XX:+UnlockDiagnosticVMOptions -XX:+PrintAssembly"
	
	GFLOGGER_OPTS="
		-Dgflogger.service.count=1024 
		-Dgflogger.append=false 
		-Dgflogger.internalDebugEnabled=false
		-Dgflogger.bytebuffer=true
		"
	
	JAVA_OPTS="
	${JAVA_OPTS}
	
	${JAVA_EXTRA_OPTS}
	
	${GFLOGGER_OPTS}

	-Dlog.dir=${LOG_DIR}
	
	-Xss2m
	-Xms512m
	-Xmx512m

	-XX:CompileThreshold=5000
	
	${JAVA_ASSEMBLY}
	
	-XX:-UseBiasedLocking

	-XX:NewSize=256m
	-XX:MaxNewSize=256m
	-XX:PermSize=128m
	-XX:MaxPermSize=128m
	
	-verbose:gc
	-XX:+PrintGCDateStamps
	-XX:+PrintGC
	-XX:+PrintGCDetails
	-XX:+PrintGCApplicationStoppedTime
	-XX:+PrintGCApplicationConcurrentTime
	-XX:+PrintTenuringDistribution
	-XX:+PrintSafepointStatistics
	-XX:+PrintHeapAtGC
	
	-XX:+UseParNewGC
	-XX:ParallelGCThreads=2
	-XX:MaxTenuringThreshold=1
	-XX:SurvivorRatio=8
	-XX:+UseConcMarkSweepGC
	-XX:+CMSParallelRemarkEnabled
	-XX:+CMSClassUnloadingEnabled
	-XX:+CMSPermGenSweepingEnabled
	-XX:CMSInitiatingOccupancyFraction=60
	-XX:+UseCMSInitiatingOccupancyOnly
	-Dsun.rmi.dgc.server.gcInterval=0x7FFFFFFFFFFFFFFE
	-Dsun.rmi.dgc.client.gcInterval=0x7FFFFFFFFFFFFFFE
	-XX:+HeapDumpOnOutOfMemoryError
	"
	
	OUT=${LOG_NAME}.std
	test -f ${LOG_DIR}/$OUT && rm ${LOG_DIR}/${OUT}
	
	CLASSPATH="$SCRIPTS_DIR/../out"
	for j in $SCRIPTS_DIR/../libs/*jar;
	do
		CLASSPATH="$CLASSPATH:$j"
	done

	STD_LOG=${LOG_DIR}/${OUT}
	
	${JAVA_HOME}/bin/java -cp ${CLASSPATH} ${JAVA_OPTS} ${MAINCLASS} ${THREADS} ${MESSAGES} 1> ${STD_LOG} 2>&1

	#echo ${STD_LOG}
	#cat ${STD_LOG}

	AVG_TIME=`grep "final" ${STD_LOG} | awk '{t+=$5;c++}END{print t/c;}'`
	LOG_ENTRIES=`grep "final" ${STD_LOG} | awk '{n+=$3;c++}END{print n/c;}'`
	MPS=`echo ${LOG_ENTRIES} ${AVG_TIME} | awk '{OFMT = "%.0f";print 1000*$1/$2;}'`
 
	echo "${LOG_ENTRIES} avg time: ${AVG_TIME} mps: ${MPS}"	 
	WARMED_LINE=`nl ${STD_LOG} | grep "warmed up ---" | tail -1 | cut -f1 | sed "s/ //g"`
	STOPPING_LINE=`nl ${STD_LOG} | grep "stopping" | head -1 | cut -f1 | sed "s/ //g"`

	cat ${STD_LOG} | sed -n "${WARMED_LINE},${STOPPING_LINE}p" | grep "Total time for which application threads were stopped" | awk '{t+=$9;}END{print t}'
	
	for f in ${LOG_NAME}.log ${OUT};
	do
		fn=${LOG_DIR}/$f	
		if [ -f $fn ]; then	
			mv $fn ${LOG_DIR}/${THREADS}-${MESSAGES}-$f
		fi	
	done
}
