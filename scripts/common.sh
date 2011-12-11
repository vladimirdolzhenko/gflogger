#!/bin/sh

run(){
    NAME=$1
    THREADS=$2
    MESSAGES=$3
    MAINCLASS=$4
    JAVA_EXTRA_OPTS=$5
    
    echo ${NAME} number of threads: "${THREADS}" messages: ${MESSAGES}
    
    JAVA_OPTS="
    ${JAVA_EXTRA_OPTS}
    -Xss2m
    -Xms512m
    -Xmx512m

	-XX:CompileThreshold=5000
    
    -XX:-UseBiasedLocking

    -XX:NewSize=256m
    -XX:MaxNewSize=256m
    -XX:PermSize=128m
    -XX:MaxPermSize=128m
    
    -verbose:gc
    -XX:+PrintGCTimeStamps
    -XX:+PrintGC
    -XX:+PrintGCDetails
    -XX:+PrintGCApplicationStoppedTime
    -XX:+PrintGCApplicationConcurrentTime
    -XX:+PrintTenuringDistribution
    
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
    
    OUT=${NAME}.std
    if [ -f logs/${OUT} ]; then
        rm logs/${OUT}
    fi
    
    CLASSPATH="out"
    for j in libs/*jar;
    do
        CLASSPATH="$CLASSPATH:$j"
    done
    
    java -cp ${CLASSPATH} ${JAVA_OPTS} ${MAINCLASS} ${THREADS} ${MESSAGES} 1>logs/${OUT} 2>&1
   
    LOG_ENTRIES=`grep -c "test" logs/${NAME}.log`
	AVG_TIME=`grep "final" logs/${NAME}.log | awk '{t+=$5;c++}END{print t/c;}'`
 
    echo "${LOG_ENTRIES} avg time ${AVG_TIME}"     
    WARMED_LINE=`nl logs/${OUT} | grep "warmed up ---" | tail -1 | cut -f1 | sed "s/ //g"`
    STOPPING_LINE=`nl logs/${OUT} | grep "stopping" | head -1 | cut -f1 | sed "s/ //g"`

    cat logs/${OUT} | sed -n "${WARMED_LINE},${STOPPING_LINE}p" | grep "Total time for which application threads were stopped" | awk '{t+=$9;}END{print t}'
    
    for f in ${NAME}.log ${OUT};
    do
    	if [ -f logs/$f ]; then	
    		mv logs/$f logs/${THREADS}-${MESSAGES}-$f
    	fi	
    done
}
