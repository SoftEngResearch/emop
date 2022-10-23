#!/bin/bash

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )

AGENT_DIR=${SCRIPT_DIR}/javamop-agent-bundle

cd ${SCRIPT_DIR}

if [ ! -d ${AGENT_DIR} ]; then
    wget https://www.cs.cornell.edu/courses/cs6156/2020fa/resources/javamop-agent-bundle.tgz
    tar -xf javamop-agent-bundle.tgz && rm javamop-agent-bundle.tgz
fi

bash ${AGENT_DIR}/make-and-install-agent.sh
