package edu.cornell;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import edu.cornell.emop.util.Util;
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

    public void execute() throws MojoExecutionException {
        super.execute();
        getLog().info("[eMOP] Invoking the Monitor Mojo...");
        long start = System.currentTimeMillis();
        generateNewMonitorFile();
        if (javamopAgent == null) {
            javamopAgent = getLocalRepository().getBasedir() + File.separator + "javamop-agent"
                    + File.separator + "javamop-agent"
                    + File.separator + "1.0"
                    + File.separator + "javamop-agent-1.0.jar";
        }
        Util.replaceFileInJar(javamopAgent, "/META-INF/aop-ajc.xml", getArtifactsDir() + File.separator + monitorFile);
        long end = System.currentTimeMillis();
        getLog().info("[eMOP Timer] Generating aop-ajc.xml and replace it takes " + (end - start) + " ms");
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
        // Replace compiled BaseAspect in javamop-agent's jar
        Util.replaceFileInJar(javamopAgent, "/mop/BaseAspect.class",
                getArtifactsDir() + File.separator + "mop" + File.separator + "BaseAspect.class");
        end = System.currentTimeMillis();
        getLog().info("[eMOP Timer] Generating BaseAspect and replace it takes " + (end - start) + " ms");
    }

    private void generateNewMonitorFile() throws MojoExecutionException {
        try (PrintWriter writer = new PrintWriter(getArtifactsDir() + File.separator + monitorFile)) {
            // Write header
            writer.println("<aspectj>");
            writer.println("<aspects>");
            // Write body
            for (String affectedSpec : affectedSpecs) {
                writer.println("<aspect name=\"mop." + affectedSpec + "\"/>");
            }
            // Write footer
            writer.println("</aspects>");
            // TODO: Hard-coded for now, make optional later (-verbose -showWeaveInfo)
            writer.println("<weaver options=\"-nowarn -Xlint:ignore\"></weaver>");
            writer.println("</aspectj>");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void generateNewBaseAspect() throws MojoExecutionException {
        try (PrintWriter writer = new PrintWriter(getArtifactsDir() + File.separator + baseAspectFile)) {
            writer.println("package mop;");
            writer.println("public aspect BaseAspect {");
            writer.println("    pointcut notwithin() :");
            for (String className : getNonAffected()) {
                writer.println("    !within(" + className + ") &&");
            }
            // TODO: Hard-coded for now, need to be changed to implement different variants for including libraries.
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
