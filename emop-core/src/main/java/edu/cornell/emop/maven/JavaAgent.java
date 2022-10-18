package edu.cornell.emop.maven;


import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

public class JavaAgent {

    /**
     * Invoked when agent is attached after the JVM has started. Rewrites the SurefirePlugin’s execute method
     * to add MethodTracer agent to the argLine
     */
    public static void agentmain(String args, Instrumentation instrumentation) {
        MavenCFT classFileTransformer = new MavenCFT();
        instrumentation.addTransformer(classFileTransformer, true);
        instrumentMaven(instrumentation);
    }

    private static void instrumentMaven(Instrumentation instrumentation) {
        try {
            for (Class<?> clz : instrumentation.getAllLoadedClasses()) {
                String name = clz.getName();
                String abstractSurefireMojo = "org.apache.maven.plugin.surefire.AbstractSurefireMojo";
                String surefirePlugin = "org.apache.maven.plugin.surefire.SurefirePlugin";
                if (name.equals(abstractSurefireMojo) || name.equals(surefirePlugin)) {
                    // goes through all transformers and applies to class -->
                    // calls transformer in MavenCFT
                    instrumentation.retransformClasses(clz);
                }
            }
        } catch (UnmodifiableClassException uce) {
            uce.printStackTrace();
        }
    }
}
