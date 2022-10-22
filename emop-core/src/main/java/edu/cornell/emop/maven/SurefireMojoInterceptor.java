package edu.cornell.emop.maven;

/**
 * This class is adapted from STARTS's SurefireMojoInterceptor.
 **/

public final class SurefireMojoInterceptor extends AbstractMojoInterceptor {
    public static final String UNSUPPORTED_SUREFIRE_VERSION_EXCEPTION = "Unsupported surefire version. ";
    public static Object sfMojo;

    /**
     * Method that executes at the very beginning of SurefirePlugin's execute method.
     */
    public static void execute(Object mojo) throws Exception {
        sfMojo = mojo;
        System.out.println("SFMOJO!!!!" + sfMojo);
        String currentArgs = checkSurefireVersion(mojo); // check if the version of surefire is good
        manipulateArgs(mojo, currentArgs);
        System.out.println("2SFMOJO!!!!" + sfMojo);
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

    private static void manipulateArgs(Object mojo, String currentArgs) throws Exception {
        String argsToAppend = "";
        if (currentArgs != null) {
            // we want to preserve all preexisting arguments besides -javaagent:${previousJavamopAgent}
            String previousJavamopAgent = System.getProperty("previous-javamop-agent");
            String[] currentArgComponents = currentArgs.split("\\s+");
            for (String arg : currentArgComponents) {
                if (!arg.equals("-javaagent:" + previousJavamopAgent)) {
                    argsToAppend += arg + " ";
                }
            }
        }
        System.out.println("original args: " + currentArgs);
        String agentPathString = System.getProperty("rpp-agent");
        if (agentPathString != null) {
            String newArgLine = "-javaagent:" + agentPathString + " " + argsToAppend;
            setField("argLine", mojo, newArgLine);
            System.out.println("Running with argument: " + getField("argLine", mojo));
        }
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
