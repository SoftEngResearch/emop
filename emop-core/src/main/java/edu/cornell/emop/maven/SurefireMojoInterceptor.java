package edu.cornell.emop.maven;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * This class is adapted from STARTS's SurefireMojoInterceptor.
 **/

public final class SurefireMojoInterceptor extends AbstractMojoInterceptor {
    public static final String UNSUPPORTED_SUREFIRE_VERSION_EXCEPTION = "Unsupported surefire version. ";
    public static Object sfMojo;
    public static boolean oldCmdLineSet = false;
    public static String oldCmdLine;

    /**
     * Method that executes at the very beginning of SurefirePlugin's execute method.
     */
    public static void execute(Object mojo) throws Exception {
        sfMojo = mojo;
        String currentArgs = checkSurefireVersion(mojo); // check if the version of surefire is good
        callMethodTracer(mojo, currentArgs);
    }

    private static boolean isSurefirePlugin(Object mojo) throws Exception {
        return mojo.getClass().getName().equals("org.apache.maven.plugin.surefire.SurefirePlugin");
    }

    private static boolean isAlreadyInvoked(Object mojo) throws Exception {
        String key = "DSI-COLLECT-TRACES" + System.identityHashCode(mojo);
        String value = System.getProperty(key);
        System.setProperty(key, "DSI-COLLECT-TRACES-invoked");
        return value != null;
    }

    private static String checkSurefireVersion(Object mojo) throws Exception {
        String argLineString = "";
        try {
            // modern versions of surefire have both of these fields. skip if we don't have these fields
            argLineString = (String) getField("argLine", mojo);
        } catch (NoSuchMethodException ex) {
            throwMojoExecutionException(mojo, UNSUPPORTED_SUREFIRE_VERSION_EXCEPTION, ex);
        }
        return argLineString;
    }

    private static void callMethodTracer(Object mojo, String currentArgs) throws Exception {
        System.out.println("in callMethodTracer");
//        // FIXME: series of hacks to get the
//        if (currentArgs != null) { // sad sad hack. Will fix later
//            if (currentArgs.equals("BASERUN")) {
//                setField("argLine", mojo, null); // no args for base run
//                return;
//            }
//            String dsiJarOriginal = System.getProperty("dsiJarOriginal");
//            if (dsiJarOriginal != null && currentArgs.contains(dsiJarOriginal)) {
//                // another cheap hack that I'm not proud of
//                return;
//            }
//        }
//        // new config skipping trace collection
//        if (Boolean.getBoolean("skipTraceCollection")) {
//            System.out.println("SKIPPING TRACE COLLECTION...");
//            return;
//        }
////        String jarPath = extractJarURLFromClass(MethodTracingAgent.class).toString().replace("file:", "");
//        // FIXME
//        String jarPath = "";
//        String testName = (String) getField("test", mojo);
//        String newArgLine = "-javaagent:" + jarPath + "=";
//        if (testName != null) {
//            newArgLine = newArgLine + testName + "@";
//        } else {
//            newArgLine = newArgLine + "all-tests@"; // we will have a all-tests.txt.gz traces file
//        }
//        // NOTE: somehow specifying excludes (of files that are not in the includes list)
//        // makes the trace file much larger than if we specified includes.
//        // Not quite sure why? but I'm going to comment out the excluding procedure for now.
//        // TODO: investigate the above
//        String traceExclude;
//        String instrumentExclude;
//        // uncomment below to use user-specified traceExcludes and instrumentExcludes
//        traceExclude = System.getProperty("traceExclude");
//        instrumentExclude = System.getProperty("instrumentExclude");
//        if (traceExclude != null) {
//            newArgLine = newArgLine + "trace.exclude=" + traceExclude + ";";
//        }
//        if (instrumentExclude != null) {
//            newArgLine = newArgLine + "instrument.exclude=" + instrumentExclude + ";";
//        }
//        if (Boolean.getBoolean("autoComputeIncludes")) {
//            String includes = "";
//            newArgLine = newArgLine + "trace.include=" + includes + ";" + "instrument.include=" + includes;
//        }
//        if (currentArgs != null) {
//            if (currentArgs.equals("EXTRACOLLECTION")) {
//                currentArgs = getOriginalArgs();
//            } else {
//                System.setProperty("originalArgs", currentArgs);
//            }
//            newArgLine = newArgLine + " " + currentArgs;
//        } else {
//            System.setProperty("originalArgs", "none"); // sad hack
//        }
//        setField("argLine", mojo, newArgLine);
    }

    private static String getOriginalArgs() {
        // get original arguments from system property
        String originalArgs = System.getProperty("originalArgs");
        if (!originalArgs.equals("none")) {
            return originalArgs;
        } else {
            return "";
        }
    }
}
