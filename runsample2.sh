#!/usr/bin/env bash

if [ $# -ne 1 ]; then
  echo "Usage: ./runsample.sh JAVASOURCEFILE"
  exit
fi

echo "Compiling and running the Java file $1..."
javac $1 && time java ${1%%.*} < sample2.in | java Judge sample2.out
echo "If user time + system time < 10 seconds, you should be fine for 
the real test."
