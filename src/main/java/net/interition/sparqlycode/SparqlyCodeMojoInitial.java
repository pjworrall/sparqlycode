package net.interition.sparqlycode;

import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;



@Mojo(name = "sparqlycode")
public class SparqlyCodeMojoInitial extends AbstractMojo {
	
	@Component
    private MavenProject project;

	public void execute() throws MojoExecutionException {
		try {
			@SuppressWarnings("rawtypes")
			List sourceLocations = project.getCompileSourceRoots();
			// get the first member
			Object dunno = sourceLocations.get(0);
			getLog().info("Sparqlycode - Hello, world.\n" + 
			    "Class of sourceLocation = " + dunno.getClass() + " , Value =" + dunno.toString());
		} catch (Exception e) {
			throw new MojoExecutionException("Error ", e);
		} finally {

			try {
				// close any resources
			} catch (Exception e) {
				// ignore
			}

		}
	}
}
