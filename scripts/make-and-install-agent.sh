#!/bin/bash

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )

cd ${SCRIPT_DIR}/javamop-agent-bundle

echo "Creating agent..."
bash make-agent.sh props agents quiet

res="$?"

if [ "${res}" -eq 0 ]; then
    echo
    echo "Installing agent..."
    mvn install:install-file -Dfile=agents/JavaMOPAgent.jar -DgroupId="javamop-agent" -DartifactId="javamop-agent" -Dversion="1.0" -Dpackaging="jar"
else
    echo "Agent build was unsuccessful, exiting..."
fi
