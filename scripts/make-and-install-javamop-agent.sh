#!/bin/bash

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )

AGENT_DIR=${SCRIPT_DIR}/javamop-agent-bundle

cd ${SCRIPT_DIR}

if [ ! -d ${AGENT_DIR} ]; then
    git clone https://github.com/SoftEngResearch/javamop-agent-bundle.git
fi

bash ${AGENT_DIR}/make-and-install-agent.sh
