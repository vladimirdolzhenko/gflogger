#!/bin/sh

#  1048576
#  4194304
for t in 1 2 4;
do
	for c in 1024 131072 1048576;
	do
		scripts/log4j.sh $t $c
		scripts/logger.sh $t $c
	done		
done
