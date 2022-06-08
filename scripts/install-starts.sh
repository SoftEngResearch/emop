git clone https://github.com/TestingResearchIllinois/starts
cd starts
git checkout impacted-both-ways
export JAVA_HOME=$(/usr/libexec/java_home -v1.8)
mvn install
rm -rf starts/
