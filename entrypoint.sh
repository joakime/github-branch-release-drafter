#!/bin/sh -l

echo "Did app get populated?"
ls -la /
ls -la /app

echo "Java on path?"
which java

java --version

echo "Args $@"
time=$(date)
echo "::set-output name=time::$time"