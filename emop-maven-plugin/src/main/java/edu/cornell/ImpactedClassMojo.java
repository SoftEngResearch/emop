package edu.cornell;

import edu.illinois.starts.jdeps.ImpactedMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "impacted")
public class ImpactedClassMojo extends ImpactedMojo {
  public void execute() throws MojoExecutionException {
    getLog().info( "[eMOP] Invoking the ImpactedClasses Mojo...");
    super.execute();
  }
}
