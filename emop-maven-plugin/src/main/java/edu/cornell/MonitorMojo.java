package edu.cornell;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import edu.cornell.emop.util.Util;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "monitor", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.TEST)
public class MonitorMojo extends AffectedSpecsMojo {

    private static String MONITOR_FILE = "new-aop-ajc.xml";

    /**
     * The path that specify the Javamop Agent JAR file.
     */
    @Parameter(property = "javamopAgent", required = true)
    private String javamopAgent;

    public void execute() throws MojoExecutionException {
        super.execute();
        getLog().info("[eMOP] Invoking the Monitor Mojo...");
        generateNewMonitorFile();
        Util.replaceSpecSelectionWithFile(javamopAgent, getArtifactsDir() + File.separator + MONITOR_FILE);
    }

    private void generateNewMonitorFile() throws MojoExecutionException {
        try (PrintWriter writer = new PrintWriter(getArtifactsDir() + File.separator + MONITOR_FILE)) {
            // Write header
            writer.println("<aspectj>");
            writer.println("<aspects>");
            // Write body
            for (String affectedSpec : affectedSpecs) {
                writer.println("<aspect name=\"mop." + affectedSpec + "\"/>");
            }
            // Write footer
            writer.println("</aspects>");
            // TODO: Hard-coded for now, make optional later
            writer.println("<weaver options=\"-nowarn -Xlint:ignore -verbose -showWeaveInfo\"></weaver>");
            writer.println("</aspectj>");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
