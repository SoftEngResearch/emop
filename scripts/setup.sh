#!/bin/bash
#
# Run the script ONLY ONCE, it sets you up for running eMOP.

if [ $# -ne 2 ]; then
  echo "USAGE: bash $0 MODE CONFIG_FILE"
  echo "where MODE describes the level of detail included in runs"
  echo "where CONFIG_FILE is usually ~/.bashrc or something else, depending on the shell you use"
  echo "using -stats will include statistics"
  echo "using -nostats or anything other than -stats will not include statistics"
  exit
fi

STATS=$1
CONFIG_FILE=$2

EXPERIMENT_ROOT="$(cd $(dirname "$0") && pwd)"
ENV_DIR="$HOME"

#######################################
# Sets up the appropriate environment variables to run eMOP.
# Note that this will alter your configuration file.
# Globals:
#   ENV_DIR
#   CONFIG_FILE
#######################################
function setup_environment_variables {
  echo "export ASPECTJ_HOME=${ENV_DIR}/aspectj1.8" >> "${CONFIG_FILE}"
  echo "export CLASSPATH=\$ASPECTJ_HOME/lib/aspectjrt.jar:\$ASPECTJ_HOME/lib/aspectjtools.jar:\$ASPECTJ_HOME/lib/aspectjweaver.jar:${ENV_DIR}/rv-monitor/target/release/rv-monitor/lib/rv-monitor-rt.jar:\$CLASSPATH" >> "${CONFIG_FILE}"
  echo "export PATH=\$ASPECTJ_HOME/bin:${ENV_DIR}/rv-monitor/target/release/rv-monitor/bin:${ENV_DIR}/javamop/target/release/javamop/javamop/bin:\${PATH}" >> "${CONFIG_FILE}"
  source "${CONFIG_FILE}"
}

#######################################
# Download the specified version of AspectJ.
# Globals:
#   ENV_DIR
#######################################
function setup_aspectj() {
  if [ ! -d "${ENV_DIR}/aspectj1.8" ]; then
    (
      cd "${ENV_DIR}"
      wget https://www.cs.cornell.edu/courses/cs6156/2020fa/resources/aspectj1.8.tgz
      tar -xzf aspectj1.8.tgz && rm aspectj1.8.tgz
    )
  fi
}

#######################################
# Download and install the specified version of AspectJ.
# Globals:
#   STATS
#   ENV_DIR
#######################################
function setup_rvmonitor {
  if [ ! -d "${ENV_DIR}/rv-monitor" ]; then
    (
      cd "${ENV_DIR}"
      git clone https://github.com/owolabileg/rv-monitor.git
    )
  fi
  cd "${ENV_DIR}/rv-monitor"
  if [ "${STATS}" = 'stats' ]; then
      git checkout statistics
  fi
  mvn install -DskipTests -DskipDocs -fn
}

#######################################
# Download and install the specified version of JavaMOP.
# Globals:
#   STATS
#   ENV_DIR
#######################################
function setup_javamop {
  if [ ! -d ${ENV_DIR}/javamop ]; then
    (
      cd ${ENV_DIR}
      git clone https://github.com/owolabileg/javamop.git
    )
  fi
  cd ${ENV_DIR}/javamop
  if [ ${STATS} = 'stats' ]; then
      git checkout -f statistics
  else
      git checkout -f emop
  fi
  mvn install -DskipTests
}

#######################################
# Download and install the specified version of JavaMOP agent.
# Globals:
#   STATS
#   ENV_DIR
#######################################
function setup_javamop_agent {
  if [ ! -d ${ENV_DIR}/javamop-agent-bundle ]; then
    (
      cd ${ENV_DIR}
      git clone https://github.com/SoftEngResearch/javamop-agent-bundle.git
      (
        cd javamop-agent-bundle
        source "${CONFIG_FILE}"
        if [ ${STATS} = 'stats' ]; then
          sed -i 's/-emop ${spec}/-emop ${spec} -s/' make-agent.sh
          sed -i 's/*.rvm/*.rvm -s/' make-agent.sh
        fi
        bash make-agent.sh props agents quiet
        # The purpose of installing 2 versions is to compare the differences w/ & w.o/ eMOP.
        mvn install:install-file -Dfile=agents/JavaMOPAgent.jar -DgroupId="javamop-agent" -DartifactId="javamop-agent" -Dversion="1.0" -Dpackaging="jar"
        mvn install:install-file -Dfile=agents/JavaMOPAgent.jar -DgroupId="javamop-agent-emop" -DartifactId="javamop-agent-emop" -Dversion="1.0" -Dpackaging="jar"
      )
    )
  fi
}

#######################################
# Download and install the specified version of eMOP.
# Globals:
#   ENV_DIR
#######################################
function setup_emop {
  if [ ! -d ${ENV_DIR}/emop ]; then
    (
      cd ${ENV_DIR}
      git clone https://github.com/SoftEngResearch/emop
      (
	      cd emop
        bash scripts/install-starts.sh
        mvn clean install
      )
    )
  fi
}

setup_environment_variables
(
  cd ${ENV_DIR}
  setup_aspectj
  setup_rvmonitor
  setup_javamop
  setup_javamop_agent
  setup_emop
)
