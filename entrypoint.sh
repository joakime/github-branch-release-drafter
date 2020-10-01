#!/bin/sh -l

echo "Java on path?"
which java

echo "Args $@"
time=$(date)
echo "::set-output name=time::$time"