#!/bin/sh -l

BRD_VERSION=$(cat /app/build/version.txt)

wget -q -O /app/branch-release-drafter.jar \
  https://github.com/joakime/github-branch-release-drafter/releases/download/${BRD_VERSION}/github-branch-release-drafter-${BRD_VERSION}.jar
  2 > &1 > /dev/null

if [ ! -f /app/branch-release-drafter.jar ] ; then
  echo "Prebuilt branch-release-drafter.jar for version $BRD_VERSION not found, building afresh."
  export JAVA_HOME=/opt/java/openjdk
  export PATH=$JAVA_HOME/bin:$PATH

  cd /app/build/
  ./mvnw clean install -e -B -V -fae -DskipTests -Dmaven.javadoc.skip=true

  cp target/branch-release-drafter.jar /app/
fi
