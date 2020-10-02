#!/bin/sh -l

BRD_VERSION=$(cat /app/build/version.txt)

wget -O /app/branch-release-drafter.jar \
  https://github.com/joakime/github-branch-release-drafter/releases/download/${BRD_VERSION}/github-branch-release-drafter-${BRD_VERSION}.jar

if [ ! -f /app/branch-release-drafter.jar ] ; then
  export JAVA_HOME=/opt/java/openjdk
  export PATH=$JAVA_HOME/bin:$PATH

  cd /app/build/
  ./mvnw clean install -e -B -V -fae -DskipTests -Dmaven.javadoc.skip=true

  cp target/branch-release-drafter.jar /app/
fi
