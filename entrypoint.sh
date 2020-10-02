#!/bin/sh -l

export JAVA_HOME=/opt/java/openjdk
export PATH=$JAVA_HOME/bin:$PATH

echo "Executing java branch-release-drafter"
java -jar /app/branch-release-drafter.jar --repo=${GITHUB_REPOSITORY} --show-tags --show-branches --show-refs --show-releases @
