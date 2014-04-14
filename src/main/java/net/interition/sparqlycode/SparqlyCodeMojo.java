package net.interition.sparqlycode;

import java.util.List;

import net.interition.sparqlycode.ast.JavaSourceFiles;
import net.interition.sparqlycode.ast.RdfAbstractSyntaxTree;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;

@Mojo(name = "sparqlycode-maven-plugin")
public class SparqlyCodeMojo extends AbstractMojo {
	
	@Component
    private MavenProject project;

	public void execute() throws MojoExecutionException {
		try {
			@SuppressWarnings("rawtypes")
			List sourceLocations = project.getCompileSourceRoots();
			
			JavaSourceFiles files = new JavaSourceFiles();
			// don't know why it return Object types but the content are strings
			for(Object sourceRoot : sourceLocations ) {
				getLog().info(sourceRoot.toString());
				List<String> fileNames = files.getSourceFiles(sourceRoot.toString());
				for(String fileName :fileNames) {
					RdfAbstractSyntaxTree ra = new RdfAbstractSyntaxTree();
					ra.convert(fileName);
				}
			}
			
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
