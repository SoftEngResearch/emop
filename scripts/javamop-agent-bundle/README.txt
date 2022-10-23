cd ${HOME}/javamop-agent-bundle/

# Make the agent
bash make-agent.sh props agents quiet

# install the agent
mvn install:install-file -Dfile=agents/JavaMOPAgent.jar -DgroupId="javamop-agent" -DartifactId="javamop-agent" -Dversion="1.0" -Dpackaging="jar"

# Read instructions for adding JavaMOP to your project:
# https://github.com/runtimeverification/javamop/blob/emop/docs/JavaMOPAgentUsage.md#using-a-java-agent

# For Maven, if you have installed the agent as instructed above, you should replace '-javaagent:JavaMOPAgent.jar' with '-javaagent:${settings.localRepository}/javamop-agent/javamop-agent/1.0/javamop-agent-1.0.jar' when you follow the instructions in the GitHUb link above
