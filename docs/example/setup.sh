#!/bin/bash
#
# Sets up the environment for demo
git clone https://github.com/SoftEngResearch/javamop-agent-bundle.git
(
  cd javamop-agent-bundle
  bash make-agent.sh props agents quiet
  # The purpose of installing 2 versions is to compare the differences w/ & w.o/ eMOP.
  mvn install:install-file -Dfile=agents/JavaMOPAgent.jar -DgroupId="javamop-agent" -DartifactId="javamop-agent" -Dversion="1.0" -Dpackaging="jar"
  mvn install:install-file -Dfile=agents/JavaMOPAgent.jar -DgroupId="javamop-agent-emop" -DartifactId="javamop-agent-emop" -Dversion="1.0" -Dpackaging="jar"
)
rm -rf javamop-agent-bundle
