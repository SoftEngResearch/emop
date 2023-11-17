def firstRun = new File(basedir, "first-run.txt");

def logContent = new File(basedir, 'build.log').text
println("SSSSSSSS")
println(logContent)
println("SSSSSSS")
def matcher = logContent =~ /AffectedSpecs: \[(.+?)\]/

def affectedSpecsMessage = matcher[0][1]

if (!firstRun.exists()) {
    firstRun.createNewFile();
    // No expectations for the first run
} else {
    println("The AffectedSpecs are: " + affectedSpecsMessage)
    assert affectedSpecsMessage == "Collections_SynchronizedCollectionMonitorAspect" : "Unexpected affected specs message for subsequent runs!"
    firstRun.delete()
}
