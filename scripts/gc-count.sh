#!/bin/sh

grep "Total time for which application threads were stopped" $1 | awk '{t+=$9;}END{print t}'
