#!/bin/bash
#
# Executing this script demonstrates how JavaMOP deals with code evolution: simply rerun RV
mvn clean
sed -i.bak "s|<configuration></configuration>|<configuration><argLine>-javaagent:${HOME}/.m2/repository/javamop-agent/javamop-agent/1.0/javamop-agent-1.0.jar</argLine></configuration>|g" pom.xml && rm pom.xml.bak
mvn test
sed -i.bak 's/i = a(l, " ");/i = a(Collections.synchronizedList(l), " ");/g' src/main/java/demo/B.java && rm src/main/java/demo/B.java.bak
mvn test
git checkout -f src/main/java/demo/B.java
git checkout -f pom.xml
