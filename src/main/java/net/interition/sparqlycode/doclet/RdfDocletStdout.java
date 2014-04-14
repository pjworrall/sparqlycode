package net.interition.sparqlycode.doclet;

import java.util.Arrays;

import com.sun.javadoc.*;
import com.sun.tools.doclets.formats.html.ConfigurationImpl;
import com.sun.tools.doclets.internal.toolkit.Configuration;
import com.sun.tools.doclets.internal.toolkit.util.ClassTree;

public class RdfDocletStdout extends AbstractDoclet  {

	public ConfigurationImpl configuration;
	
	private static String stdUriPrefix = "http://www.interition.net/kb/sparqlycode/";
	
	private String codeBasePrefix = null;

	public Configuration configuration() {
		return ConfigurationImpl.getInstance();
	}

	public RdfDocletStdout() {
		configuration = (ConfigurationImpl) configuration();
		// hard coded for testing but need to pick up from options
		// deliberately did not use a PREFIX to handle this because there needs to be a namespace plan for that first
		// we cannot keep repeating the same prefix name for different uri domains
		setCodeBasePrefix("https://svn.apache.org/repos/asf/jena/trunk/jena-core/src/main/java/");
	}

	public static boolean start(RootDoc root) {
	
		try {
			RdfDocletStdout doclet = new RdfDocletStdout();
			return doclet.start(doclet, root);

		} finally {
			ConfigurationImpl.reset();
		}
		
	}


	@Override
	protected void generateClassFiles(ClassDoc[] arr, ClassTree classtree) {
		
		// produce prefixes ( this will head every rdf'sed java file )
		genPrefixRdf();

		Arrays.sort(arr);
        for(int i = 0; i < arr.length; i++) {
        	// skipping over things that are not wanted
            if (!(configuration.isGeneratedDoc(arr[i]) && arr[i].isIncluded())) {
                continue;
            }

            ClassDoc curr = arr[i];
            //make an enum for these but for now...
            String rdfTypeClass = "java:Class";
            String rdfTypeInterface = "java:Interface";
            
            String classOrIntUri = prefixWithURL(curr.qualifiedName());
            String name = curr.name();
            String packageName = curr.containingPackage().name();
             
            // check if class or interface. potential bug here is it is not either!!
            if(curr.isClass()) {
            	spoRdfOut(classOrIntUri,"rdfs:type",rdfTypeClass);
            }
            
            if(curr.isInterface()) {
            	spoRdfOut(classOrIntUri,"rdfs:type",rdfTypeInterface);
            }
            
            // create basic metadata
            splRdfOut(classOrIntUri,"java:name",name);
            splRdfOut(classOrIntUri,"java:label",name);
            splRdfOut(classOrIntUri,"java:package",packageName);
            
            // handle the constructors
            for (ConstructorDoc con : curr.constructors() ) {
            	String methodUri = prefixWithURL(con.qualifiedName()) ;
            	spoRdfOut(classOrIntUri,"java:constructor",methodUri);
                parametersToRdf(methodUri, con);
            }
            
            
            // handle the methods
            for ( MethodDoc method : curr.methods() ) {
            	String methodUri = prefixWithURL(method.qualifiedName());
            	spoRdfOut(classOrIntUri,"java:method",methodUri);
            	parametersToRdf(methodUri, method);
            	
            }
            
            // handle the interfaces
            for ( ClassDoc interfaceDoc : curr.interfaces()) {
            	String interfacedUri = prefixWithURL(interfaceDoc.qualifiedName()) ;
            	spoRdfOut(classOrIntUri,"java:implements",interfacedUri);
            }
            
            // handle super classes
            if(curr.superclass() != null) {
            	spoRdfOut(classOrIntUri,"java:extends", curr.superclass().qualifiedName());
            }
            
            // handle fields
            for ( FieldDoc field : curr.fields()) {
            	String fieldUri = this.prefixWithURL(field.qualifiedName()) ;
            	spoRdfOut(classOrIntUri,"java:field",fieldUri);
            	splRdfOut(fieldUri,"java:name",field.name());
                splRdfOut(fieldUri,"java:label",field.name());
            }
            
            // add some simple attributes
            if(curr.isAbstract() == true ) {
            	spbRdfOut(classOrIntUri,"java:isAbstract", true);
            }
            
        }
		
	}

	@Override
	protected void generatePackageFiles(ClassTree arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	private void parametersToRdf(String methodUri, ExecutableMemberDoc method) {
		// do the parameters as well
		
		// modify to cope with bounded types
		
    	for (Parameter p : method.parameters()) {
    		
    		// use a bnode to associate the name and parameter
    		System.out.println("<" + methodUri + ">" + "\t" + "java:parameter" + "\t" + "[");
    		System.out.println("\t\t" + "java:name" + "\t" + "\"" + p.name() + "\"" + "   ;");
    		
    		if(!p.type().toString().contains("<")) {
    			String classOrIntUri = prefixWithURL(p.type().qualifiedTypeName());
    			System.out.println("\t\t" + "java:type" + "\t" + "<" + classOrIntUri+ ">" );
    		} else {
    			// Unpack using literal to create a java:boundType
    			// Parameter type didn't have any methods to do this better
    			int open = p.typeName().indexOf('<');
    			int close = p.typeName().indexOf('>');
    			String bound = prefixWithURL(p.typeName().substring(open + 1, close));
    			String type = prefixWithURL(p.typeName().substring(0,open));
    			
    			System.out.println("\t\t" + "java:boundType" + "\t" + "[" );
    				System.out.println("\t\t\t" + "java:type" + "\t" + "<" + type + ">" + "  ;" );
    				System.out.println("\t\t\t" + "java:bound" + "\t" + "<" +  bound + ">");
    			System.out.println("\t\t\t" + "]");
    						
    		}
    		
    		System.out.println("\t" + "] .");
    	}
	}
	
	private void spoRdfOut(String uri, String predicate, String object) {
		System.out.println("<" + uri + ">" + "\t" + predicate + "\t" + "<" +  object + ">"  + "   .");
	}
	
	private void splRdfOut(String uri, String predicate, String literal) {
		System.out.println("<" + uri + ">" + "\t" + predicate + "\t" + "\"" + literal + "\"" + "   .");
	}
	
	private void spbRdfOut(String uri, String predicate, boolean boo) {
		System.out.println("<" + uri + ">" + "\t" + predicate + "\t" + "\"" + boo + "\"" + "^^xsd:boolean" + "  .");
	}
	
	public void genPrefixRdf() {
	
		// print java prefix
		System.out.println("@prefix java: " + "<"
				+ "http://www.interition.net/java/ref/" + ">  .");

		// print sparqlcode prefix (the new model adding knowledge to the AST)
		System.out.println("@prefix sc: " + "<"
				+ "http://www.interition.net/sparqlingcode/ref/" + ">  .");

		// rdf and rdfs
		System.out.println("@prefix rdfs: " + "<"
				+ "http://www.w3.org/2000/01/rdf-schema#" + ">  .");
		System.out.println("@prefix rdf: " + "<"
				+ "http://www.w3.org/1999/02/22-rdf-syntax-ns#" + ">  .");
		
		// xsd
		System.out.println("@prefix xsd: " + "<"
				+ "http://www.w3.org/2001/XMLSchema#" + ">  .");

	}
	
	public String prefixWithURL(String uri) {
		
		
		String url = RdfDocletStdout.stdUriPrefix + uri;
		
		
		return url;
	}

	public String getCodeBasePrefix() {
		return codeBasePrefix;
	}

	public void setCodeBasePrefix(String codeBasePrefix) {
		this.codeBasePrefix = codeBasePrefix;
	}
	
}
