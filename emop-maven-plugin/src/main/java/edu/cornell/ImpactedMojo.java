package edu.cornell;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "impacted")
public class ImpactedMojo extends AbstractMojo {
  public void execute() throws MojoExecutionException {
    getLog().info( "Hello, world!");
  }
}
