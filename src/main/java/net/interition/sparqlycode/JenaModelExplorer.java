package net.interition.sparqlycode;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;

public class JenaModelExplorer {

	public static void main(String[] args) {
		
		String sparqlyCodeDomainPrefix = "http://www.interition.net/java/ref/";
		String codeBasePrefix = "http://www.sparqlycode.com/id/";
		
		// create an empty Model
		Model model = ModelFactory.createDefaultModel();

		Resource rdfTypeClass = model
				.createResource(sparqlyCodeDomainPrefix + "Class");

		Property classNameProperty = model.createProperty(sparqlyCodeDomainPrefix, "name");
		Property packageNameProperty = model
				.createProperty(sparqlyCodeDomainPrefix, "package");
		
		
		
		Resource classOrIntUri = model.createResource(codeBasePrefix
				+ "woolywooly");
		
		classOrIntUri.addProperty(RDFS.label, "tits");
		classOrIntUri.addProperty(packageNameProperty,classOrIntUri);

		model.write(System.out, "N3");

	}

}
