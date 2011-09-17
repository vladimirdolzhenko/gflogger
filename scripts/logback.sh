#!/bin/sh

CLASSPATH="out"
for j in libs/*jar;
do
	CLASSPATH="$CLASSPATH:$j"	
done

JAVA_OPTS="
-Dlogback.configurationFile=logback.xml

-Xss2m
-Xms512m
-Xmx512m

-XX:NewSize=256m
-XX:MaxNewSize=256m
-XX:PermSize=128m
-XX:MaxPermSize=128m

-Xloggc:""logs/logback-gc.log""
-XX:+PrintGCTimeStamps
-XX:+PrintGCDetails
-XX:+PrintGCApplicationStoppedTime
-XX:+PrintTenuringDistribution

-XX:+DisableExplicitGC
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
-XX:+HeapDumpOnOutOfMemoryError"

echo logback number of threads: "$1" messages: $2
java -cp $CLASSPATH $JAVA_OPTS logger.LogBackLogger $1 $2 1>/dev/null 2>&1

grep "final" logs/logback.log | awk '{t+=$5;c++}END{print "logback avg time:" t/c;}'
scripts/gc-count.sh logs/logback-gc.log

for f in logback.log logback-gc.log;
do
	if [ -f logs/$f ]; then	
		mv logs/$f logs/$1-$2-$f	
	fi	
done
