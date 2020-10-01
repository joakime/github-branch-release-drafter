#!/bin/sh -l

export JAVA_HOME=/opt/java/openjdk
export PATH=$JAVA_HOME/bin:$PATH

echo "Executing java release-drafter"
java -cp /app/release-drafter.jar net.webtide.github.releasedrafter.Main $@

echo "Args $@"
time=$(date)
echo "::set-output name=time::$time"