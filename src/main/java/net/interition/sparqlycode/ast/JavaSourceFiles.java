package net.interition.sparqlycode.ast;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class JavaSourceFiles {

	private List<String> sourceFiles = new ArrayList<String>();
	
	/*
	 * 
	 *  Just here for testing. Should drop for unit test.
	 */
	public static void main(String[] args) throws IOException {
		
		JavaSourceFiles processor = new JavaSourceFiles();

		List<String> fileNames = 
				processor.getSourceFiles("/Users/pjworrall/Documents/workspace/jena-2.11.0/jena-arq/src/main/java");			
	
		for( String fileName : fileNames ) {
			System.out.println(fileName);
		}
	}
	
	
	// recursive!
	public List<String> getSourceFiles(String rootFolder) throws IOException {
			
		Path dir = FileSystems.getDefault().getPath(rootFolder);
	
		DirectoryStream<Path> stream = null;
		try {
			stream = Files.newDirectoryStream(dir,"*");
			for (Path entry : stream) {
				if(entry.toFile().isDirectory()) {
					System.out.println("jumping into: " + entry.toAbsolutePath().toString());	
					this.getSourceFiles(entry.toAbsolutePath().toString());
				} else {
					sourceFiles.add(entry.toAbsolutePath().toString());
					System.out.println("Collecting: " + entry.toAbsolutePath().toString());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			stream.close();
		}
		
		return sourceFiles;
		
	}

}
