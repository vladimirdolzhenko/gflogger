#!/bin/sh

echo gflogger number of threads: "$1" messages: $2

JAVA_OPTS="
-Xss2m
-Xms512m
-Xmx512m

-XX:NewSize=256m
-XX:MaxNewSize=256m
-XX:PermSize=128m
-XX:MaxPermSize=128m

-Xloggc:""./logs/logger-gc.log""
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
-XX:+HeapDumpOnOutOfMemoryError

"

#echo java -cp out $JAVA_OPTS gflogger.LoggerExample $1 $2
java -cp libs/gflogger.jar $JAVA_OPTS gflogger.LoggerExample $1 $2 1>/dev/null 2>&1

grep "final" logs/logger.log | awk '{t+=$5;c++}END{print "logger avg time:" t/c;}'
scripts/gc-count.sh logs/logger-gc.log

for f in logger.log logger-gc.log;
do
	if [ -f logs/$f ]; then	
		mv logs/$f logs/$1-$2-$f	
	fi	
done
