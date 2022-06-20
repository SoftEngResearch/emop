git clone https://github.com/TestingResearchIllinois/starts
if [ $? -eq 0 ]; then
  (
    cd starts
    git checkout impacted-both-ways
    mvn install
  )
  rm -rf starts/
fi
