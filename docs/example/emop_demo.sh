#!/bin/bash
#
# Executing this script demonstrates how eMOP does RPS -- selecting a subset of properties to monitor
mvn clean
sed -i.bak "s|<configuration></configuration>|<configuration><argLine>-javaagent:${HOME}/.m2/repository/javamop-agent-emop/javamop-agent-emop/1.0/javamop-agent-emop-1.0.jar</argLine></configuration>|g" pom.xml && rm pom.xml.bak
mvn emop:rps -DclosureOption=TRANSITIVE -DjavamopAgent=${HOME}/.m2/repository/javamop-agent-emop/javamop-agent-emop/1.0/javamop-agent-emop-1.0.jar
sed -i.bak 's/i = a(l, " ");/i = a(Collections.synchronizedList(l), " ");/g' src/main/java/demo/B.java && rm src/main/java/demo/B.java.bak
mvn emop:rps -DclosureOption=TRANSITIVE -DjavamopAgent=${HOME}/.m2/repository/javamop-agent-emop/javamop-agent-emop/1.0/javamop-agent-emop-1.0.jar
sed -i.bak 's/i = a(Collections.synchronizedList(l), " ");/i = a(l, " ");/g' src/main/java/demo/B.java && rm src/main/java/demo/B.java.bak
sed -i.bak "s|<configuration><argLine>-javaagent:${HOME}/.m2/repository/javamop-agent-emop/javamop-agent-emop/1.0/javamop-agent-emop-1.0.jar</argLine></configuration>|<configuration></configuration>|g" pom.xml && rm pom.xml.bak
mvn emop:clean