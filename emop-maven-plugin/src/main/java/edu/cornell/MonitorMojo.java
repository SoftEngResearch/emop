package edu.cornell;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.cornell.emop.util.Util;
import edu.illinois.starts.helpers.Writer;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.MessageHandler;
import org.aspectj.tools.ajc.Main;

@Mojo(name = "monitor", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.TEST)
public class MonitorMojo extends AffectedSpecsMojo {

    private String monitorFile = "new-aop-ajc.xml";

    private String baseAspectFile = "BaseAspect.aj";

    /**
     * The path that specify the Javamop Agent JAR file.
     */
    @Parameter(property = "javamopAgent")
    private String javamopAgent;

    /**
     * The path that specify the Javamop Agent JAR file.
     */
    @Parameter(property = "includeNonAffected", required = false, defaultValue = "true")
    private boolean includeNonAffected;

    /**
     * Whether to instrument third-party libraries.
     * Setting this option to false triggers the ^l weak RPS variants.
     */
    @Parameter(property = "includeLibraries", required = false, defaultValue = "true")
    private boolean includeLibraries;

    public void execute() throws MojoExecutionException {
        super.execute();
        getLog().info("[eMOP] Invoking the Monitor Mojo...");
        long start = System.currentTimeMillis();
        Util.generateNewMonitorFile(getArtifactsDir() + File.separator + monitorFile, affectedSpecs);
        if (javamopAgent == null) {
            javamopAgent = getLocalRepository().getBasedir() + File.separator + "javamop-agent"
                    + File.separator + "javamop-agent"
                    + File.separator + "1.0"
                    + File.separator + "javamop-agent-1.0.jar";
        }
        Util.replaceFileInJar(javamopAgent, "/META-INF/aop-ajc.xml", getArtifactsDir() + File.separator + monitorFile);
        long end = System.currentTimeMillis();
        getLog().info("[eMOP Timer] Generating aop-ajc.xml and replace it takes " + (end - start) + " ms");
        if (!includeNonAffected || !includeLibraries) {
            start = System.currentTimeMillis();
            // Rewrite BaseAspect.aj to ignore non-affected classes
            generateNewBaseAspect();
            // Compile BaseAspect.aj with ajc
            Main compiler = new Main();
            MessageHandler mh = new MessageHandler();
            String classpath = getClassPath() + File.pathSeparator + getRuntimeJars();
            String[] ajcArgs = {"-d", getArtifactsDir(), "-classpath", classpath,
                    getArtifactsDir() + File.separator + baseAspectFile};
            compiler.run(ajcArgs, mh);
            IMessage[] ms = mh.getMessages(null, true);
            for (IMessage i : ms) {
                if (i.isError()) {
                    getLog().error("AspectJ compilation of BaseAspect.aj failed! Exiting...");
                    System.exit(1);
                }
            }
            // Replace compiled BaseAspect in javamop-agent's jar
            Util.replaceFileInJar(javamopAgent, "/mop/BaseAspect.class",
                    getArtifactsDir() + File.separator + "mop" + File.separator + "BaseAspect.class");
            end = System.currentTimeMillis();
            getLog().info("[eMOP Timer] Generating BaseAspect and replace it takes " + (end - start) + " ms");
        }
    }

    /**
     * Generates a String containing within() pointcuts so that instrumentation is only performed within the
     * packages in the maven project in question, effectively disabling instrumentation in third-party libraries.
     * If the includeLibraries parameter is set to true, then this method would return an empty string.
     */
    private String generateThirdPartyExclusion() throws MojoExecutionException {
        StringBuilder exclusions = new StringBuilder();
        if (!includeLibraries) {
            // TODO: Remove hard-coded parts
            String sureFireClassPath = Writer.pathToString(getSureFireClassPath().getClassPath());
            Set<String> dependencies = new HashSet<>();
            for (String entry : sureFireClassPath.split(":")) {
                if (entry.contains(".jar")) {
                    entry = entry.replace('-', '.');
                    String[] segments = entry.split("/");
                    StringBuilder sb = new StringBuilder();
                    for (int i = 5; i < segments.length - 3; i++) {
                        sb.append(segments[i]);
                        sb.append(".");
                    }
                    dependencies.add(sb.toString());
                }
            }
            for (String dependency : dependencies) {
                exclusions.append("    !within(" + dependency + ".*) &&\n");
            }
        }
        return exclusions.toString();
    }

    /**
     * Generates a String containing !within() pointcuts so that instrumentation is only performed within the
     * affected classes, effectively disabling instrumentation in non-affected classes.
     * If the includeNonAffected parameter is set to true, then this method would return an empty string.
     */
    private String generateNonAffectedExclusion() {
        StringBuilder stringBuilder = new StringBuilder();
        if (!includeNonAffected) {
            for (String className : getNonAffected()) {
                if (!className.contains("package-info")) { // not excluding package-info results in compile error
                    stringBuilder.append("    !within(" + className + ") &&\n");
                }
            }
        }
        return stringBuilder.toString();
    }

    private void generateNewBaseAspect() throws MojoExecutionException {
        try (PrintWriter writer = new PrintWriter(getArtifactsDir() + File.separator + baseAspectFile)) {
            writer.println("package mop;");
            writer.println("public aspect BaseAspect {");
            writer.println("    pointcut notwithin() :");
            writer.println(generateThirdPartyExclusion());
            writer.println(generateNonAffectedExclusion());
            // hard-coding the essential exclusions.
            writer.println("    !within(sun..*) &&");
            writer.println("    !within(java..*) &&");
            writer.println("    !within(javax..*) &&");
            writer.println("    !within(javafx..*) &&");
            writer.println("    !within(com.sun..*) &&");
            writer.println("    !within(org.dacapo.harness..*) &&");
            writer.println("    !within(net.sf.cglib..*) &&");
            writer.println("    !within(mop..*) &&");
            writer.println("    !within(javamoprt..*) &&");
            writer.println("    !within(rvmonitorrt..*) &&");
            writer.println("    !within(org.junit..*) &&");
            writer.println("    !within(junit..*) &&");
            writer.println("    !within(java.lang.Object) &&");
            writer.println("    !within(com.runtimeverification..*) &&");
            writer.println("    !within(org.apache.maven.surefire..*) &&");
            writer.println("    !within(org.mockito..*) &&");
            writer.println("    !within(org.powermock..*) &&");
            writer.println("    !within(org.easymock..*) &&");
            writer.println("    !within(com.mockrunner..*) &&");
            writer.println("    !within(org.jmock..*);");
            writer.println("}");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
