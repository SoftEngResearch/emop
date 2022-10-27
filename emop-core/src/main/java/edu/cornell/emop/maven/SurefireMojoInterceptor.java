package edu.cornell.emop.maven;

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
        sfMojo = mojo;
        String currentArgs = checkSurefireVersion(mojo);
        manipulateArgs(mojo, currentArgs);
    }

    private static String checkSurefireVersion(Object mojo) throws NoSuchFieldException, IllegalAccessException {
        String argLineString = "";
        // Modern versions of surefire have both of these fields. Skip if we don't have these fields.
        argLineString = (String) getField("argLine", mojo);
        return argLineString;
    }

    private static void manipulateArgs(Object mojo, String currentArgs)
            throws NoSuchFieldException, IllegalAccessException {
        String argsToAppend = "";
        if (currentArgs != null) {
            // we want to preserve all preexisting arguments besides -javaagent:${previousJavamopAgent}
            String previousJavamopAgent = System.getProperty("previous-javamop-agent");
            argsToAppend = currentArgs.replace("-javaagent:" + previousJavamopAgent, "");
        }
        String agentPathString = System.getProperty("rpp-agent");
        if (agentPathString != null) {
            String newArgLine = "-javaagent:" + agentPathString + " " + argsToAppend;
            setField("argLine", mojo, newArgLine);
        }
    }
}
