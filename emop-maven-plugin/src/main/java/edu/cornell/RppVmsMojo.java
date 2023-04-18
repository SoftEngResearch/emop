package edu.cornell;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "rpp-vms", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.TEST)
@Execute(phase = LifecyclePhase.TEST, lifecycle = "rpp-vms")
public class RppVmsMojo extends VmsMojo {

    @Parameter(property = "demoteCritical", defaultValue = "false", required = false)
    private boolean demoteCritical;

    public void execute() throws MojoExecutionException {
//        super.monitorFile = MonitorMojo.MONITOR_FILE;
//        super.execute();
        RppMojo rppMojo = new RppMojo();
        rppMojo.setDemoteCritical(demoteCritical);
        rppMojo.execute();
        super.execute();
    }
}
