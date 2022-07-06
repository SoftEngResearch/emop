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

    /**
     * The path that specify the Javamop Agent JAR file.
     */
    @Parameter(property = "javamopAgent", required = true)
    private String javamopAgent;

    public void execute() throws MojoExecutionException {
        super.execute();
        getLog().info("[eMOP] Invoking the Monitor Mojo...");
        // TODO: Should get the affected mojo to return a set (keep the map for debugging)
//        for (Map.Entry<String, Set<String>> entry : classToSpecs.entrySet()) {
//            monitoredSpecs.addAll(entry.getValue());
//        }
        generateNewMonitorFile();
        Util.replaceSpecSelectionWithFile(javamopAgent, getArtifactsDir() + File.separator + "new-aop-ajc.xml");
        deleteNewMonitorFile();
    }

    private void generateNewMonitorFile() throws MojoExecutionException {
        try (PrintWriter writer = new PrintWriter(getArtifactsDir() + File.separator + "new-aop-ajc.xml")) {
            // Write header
            writer.println("<aspectj>");
            writer.println("<aspects>");
            // Write body
            for (String spec : specs) {
                writer.println("<aspect name=\"mop." + spec + "\"/>");
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

    private void deleteNewMonitorFile() throws MojoExecutionException {
        File file = new File(getArtifactsDir() + File.separator + "new-aop-ajc.xml");
        if (!file.delete()) {
            throw new MojoExecutionException("new-aop-ajc.xml delete unsuccessful.");
        }
    }
}
