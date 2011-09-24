#!/bin/sh

function run(){
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
    
    grep "final" logs/${NAME}.log | awk '{t+=$5;c++}END{print "logger avg time:" t/c;}'
    
    WARMED_LINE=`sed = logs/${OUT} | sed 'N;s/\n/\t/' | grep "warmed up ---" | tail -1 | cut -f1`
    tail -n +${WARMED_LINE} logs/${OUT} | grep "Total time for which application threads were stopped" | awk '{t+=$9;}END{print t}'
    
    for f in ${NAME}.log ${OUT};
    do
    	if [ -f logs/$f ]; then	
    		mv logs/$f logs/${THREADS}-${MESSAGES}-$f
    	fi	
    done
}
