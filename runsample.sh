#!/usr/bin/env bash

if [ $# -ne 1 ]; then
  echo "Usage: ./runsample.sh JAVASOURCEFILE"
  exit
fi

echo "Compiling and running the Java file $1..."
javac $1 && time java ${1%%.*} < sample.in | java Judge sample.out
