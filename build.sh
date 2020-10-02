#!/bin/sh -l

BRD_VERSION=$(cat /app/build/version.txt)
BRD_URL="https://github.com/joakime/github-branch-release-drafter/releases/download/${BRD_VERSION}/github-branch-release-drafter-${BRD_VERSION}.jar"
BRD_JAR=/app/branch-release-drafter.jar

echo "Attempting to fetch (possibly) pre-built ${BRD_URL}"
wget -q -O ${BRD_JAR} ${BRD_URL} 2>&1 > /app/download.log

if [ ! -f /app/branch-release-drafter.jar ] ; then
  echo "Prebuilt branch-release-drafter.jar for version $BRD_VERSION not found, building version from source."
  export JAVA_HOME=/opt/java/openjdk
  export PATH=$JAVA_HOME/bin:$PATH

  cd /app/build/
  echo "Building ${BRD_JAR} ..."
  ./mvnw clean install -q -e -B -V -fae -DskipTests -Dmaven.javadoc.skip=true

  cp target/branch-release-drafter.jar /app/
fi
