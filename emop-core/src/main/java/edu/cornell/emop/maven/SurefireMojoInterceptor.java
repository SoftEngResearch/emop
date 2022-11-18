package edu.cornell.emop.maven;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class is adapted from STARTS's SurefireMojoInterceptor
 * (https://github.com/TestingResearchIllinois/starts/blob/master/...
 * starts-core/src/main/java/edu/illinois/starts/maven/SurefireMojoInterceptor.java).
 * This class manipulates the argLine field of SurefirePlugin, where STARTS's SurefireMojoInterceptor manipulates the
 * excludes field of SurefirePlugin.
 **/

public final class SurefireMojoInterceptor extends AbstractMojoInterceptor {
    public static final String UNSUPPORTED_SUREFIRE_VERSION_EXCEPTION = "Unsupported surefire version. ";
    public static Object sfMojo;

    /**
     * Method that executes at the very beginning of SurefirePlugin's execute method.
     */
    public static void execute(Object mojo) throws Exception {
        String originalExcludes = System.getProperty("original-excludes");
        if (originalExcludes == null) {
            List<String> excludes = (List<String>) getField("excludes", mojo);
            System.setProperty("original-excludes", excludes == null ? "" : String.join("," ,excludes));
        } else {
            setField("excludes", mojo, Arrays.asList(originalExcludes));
            setField("includes", mojo, Arrays.asList("**/Test*,**/*Test,**/*Tests,**/*TestCase"));
            setField("excludesFile", mojo, new File("/home/ayaka/projects/tiny-emop-example/excludesFile2.txt"));
            setField("includesFile", mojo, new File("/home/ayaka/projects/tiny-emop-example/includesFile.txt"));
        }
        sfMojo = mojo;
        String currentArgs = checkSurefireVersion(mojo);
        System.out.println(System.getProperties());
        if (Boolean.getBoolean("skipping-execution")) {
            setField("excludesFile", mojo, new File("/home/ayaka/projects/tiny-emop-example/excludesFile.txt"));
//            skipTests(mojo);
        }
        if (Boolean.getBoolean("running-rpp")) {
            manipulateArgs(mojo, currentArgs, true);
        } else { // FIXME: this will break RPS maybe???
            manipulateArgs(mojo, currentArgs, false);
        }
        System.out.println("excludes: " + getField("excludes" , mojo));
        System.out.println("argLine: " + getField("argLine", mojo));
    }

    private static String checkSurefireVersion(Object mojo) throws NoSuchFieldException, IllegalAccessException {
        String argLineString = "";
        // Modern versions of surefire have both of these fields. Skip if we don't have these fields.
        argLineString = (String) getField("argLine", mojo);
        return argLineString;
    }

    private static void skipTests(Object mojo) throws NoSuchFieldException, IllegalAccessException {
        List<String> currentExcludes = (List<String>) getField("excludes", mojo);
        // always use forward-slash as separator for Surefire's excludes field
        List<String> newExcludes = new ArrayList<>(Arrays.asList("**/Test*,**/*Test,**/*Tests,**/*TestCase"
                .replace("[", "").replace("]", "").replace(File.separator, "/").split(",")));
        if (currentExcludes != null) {
            newExcludes.addAll(currentExcludes);
        } else {
            newExcludes.add("**/*$*");
        }
        setField("excludes", mojo, newExcludes);
    }

    private static void manipulateArgs(Object mojo, String currentArgs, boolean setNewAgent)
            throws NoSuchFieldException, IllegalAccessException {
        String newArgLine = "";
        if (currentArgs != null) {
            System.out.println("replacing previousjavamopagent. currentArgs: " + currentArgs);
            // we want to preserve all preexisting arguments besides -javaagent:${previousJavamopAgent}
            String previousJavamopAgent = System.getProperty("previous-javamop-agent");
            newArgLine = currentArgs.replace("-javaagent:" + previousJavamopAgent, "");
        }
        if (setNewAgent) {
            String agentPathString = System.getProperty("rpp-agent");
            if (agentPathString != null) {
                newArgLine = "-javaagent:" + agentPathString + " " + newArgLine;
            }
        }
        setField("argLine", mojo, newArgLine);
    }
}
