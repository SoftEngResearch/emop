def firstRun = new File(basedir, "first-run.txt");
/*
def logContent = new File(basedir, 'build.log').text
def matcher = logContent =~ /AffectedSpecs: \[(.+?)\]/
def affectedSpecsMessage = matcher[0][1]
*/
def aspectFilepath = basedir.toString() +  "/.starts/new-aop-ajc.xml"

if (!firstRun.exists()) {
    firstRun.createNewFile();
    // No expectations for the first run
} else {
    def xmlFileContent = new File(aspectFilepath).text
    def xml = new XmlSlurper().parseText(xmlFileContent)
    def affectedSpecsMessage = xml.aspects.aspect*.@name.collect { it.toString().split("\\.")[1] }

    println("The affectedSpecs are: " + affectedSpecsMessage)
    assert affectedSpecsMessage == "StringBuilder_ThreadSafeMonitorAspect" : "Unexpected affected specs message for subsequent runs!"
    firstRun.delete()
}
