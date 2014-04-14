package net.interition.sparqlycode.doclet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import net.interition.sparqlycode.vocabulary.JAVALANG;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.sun.javadoc.*;
import com.sun.tools.doclets.formats.html.ConfigurationImpl;
import com.sun.tools.doclets.internal.toolkit.Configuration;
import com.sun.tools.doclets.internal.toolkit.util.ClassTree;


public class CopyOfRdfDoclet extends AbstractDoclet {

	public ConfigurationImpl configuration;

	protected static final String sparqlyCodeDomainPrefix = "http://www.interition.net/java/ref/";

	private String codeBasePrefix = null;
	
	private String fileName = "rdfdoclet";

	// create an empty Model
	private Model model = ModelFactory.createDefaultModel();

	private Resource rdfTypeClass = model
			.createResource(sparqlyCodeDomainPrefix + "Class");
	private Resource rdfTypeInterface = model.createResource(sparqlyCodeDomainPrefix
			+ "Interface");
	private Property classNameProperty = model.createProperty(sparqlyCodeDomainPrefix, "name");
	private Property packageNameProperty = model
			.createProperty(sparqlyCodeDomainPrefix, "package");
	private Property constructorProperty = model.createProperty(sparqlyCodeDomainPrefix,
			"constructor");
	private Property methodProperty = model.createProperty(sparqlyCodeDomainPrefix, "method");
	private Property parameterProperty = model
			.createProperty(sparqlyCodeDomainPrefix, "parameter");
	private Property parameterTypeProperty = model.createProperty(sparqlyCodeDomainPrefix,
			"parameterType");
	private Property parameterBoundProperty = model.createProperty(sparqlyCodeDomainPrefix,
			"parameterBound");
	private Property nameProperty = model.createProperty(sparqlyCodeDomainPrefix, "name");
	private Property extendsProperty = model.createProperty(sparqlyCodeDomainPrefix, "extends");
	private Property returnsProperty = model.createProperty(sparqlyCodeDomainPrefix, "returns");
	private Property fieldProperty = model.createProperty(sparqlyCodeDomainPrefix, "field");
	private Property isAbstractProperty = model.createProperty(sparqlyCodeDomainPrefix, "isAbstract");

	public Configuration configuration() {
		return ConfigurationImpl.getInstance();
	}

	public CopyOfRdfDoclet() {
		configuration = (ConfigurationImpl) configuration();
		// hard coded for testing but need to pick up from options
		// deliberately did not use a PREFIX to handle this because there needs
		// to be a namespace plan for that first
		// we cannot keep repeating the same prefix name for different uri
		// domains
		setCodeBasePrefix("http://www.sparqlycode.com/id/");
	}

	public static boolean start(RootDoc root) {

		try {
			CopyOfRdfDoclet doclet = new CopyOfRdfDoclet();
			String fileName = readOptions(root.options());
			doclet.setFileName(fileName);
			return doclet.start(doclet, root);

		} finally {
			ConfigurationImpl.reset();
		}
		

	}

	@Override
	protected void generateClassFiles(ClassDoc[] arr, ClassTree classtree) {
	
		Arrays.sort(arr);
		for (int i = 0; i < arr.length; i++) {
			// skipping over things that are not wanted
			if (!(configuration.isGeneratedDoc(arr[i]) && arr[i].isIncluded())) {
				continue;
			}

			ClassDoc curr = arr[i];

			Resource classOrIntUri = model.createResource(codeBasePrefix
					+ curr.qualifiedName().replace(".", "/"));

			// check if class or interface. potential bug here is it is not
			// either!!
			if (curr.isClass()) {
				classOrIntUri.addProperty(RDF.type, rdfTypeClass);
			}

			if (curr.isInterface()) {
				classOrIntUri.addProperty(RDF.type, rdfTypeInterface);
			}
			 

			// create basic metadata
			classOrIntUri.addProperty(RDFS.label, curr.name());
			classOrIntUri.addProperty(JAVALANG.Name, curr.name());
			classOrIntUri.addProperty(packageNameProperty, curr.containingPackage()
					.name());

			createConstructorRdf(classOrIntUri, curr);

			createMethodsRdf(classOrIntUri, curr);
			
			// handle super classes
            if(curr.superclass() != null) {
            	Resource superClazz = model.createResource(codeBasePrefix
    					+ curr.superclass().qualifiedName().replace(".", "/"));
            	classOrIntUri.addProperty(extendsProperty,superClazz);
            }
            
            // handle fields
            for ( FieldDoc field : curr.fields()) {
            	Resource fieldResource = model.createResource(codeBasePrefix
    					+ field.qualifiedName().replace(".", "/"));
            	classOrIntUri.addProperty(fieldProperty,fieldResource);
            }
            
         // add some simple attributes
            if(curr.isAbstract() == true ) {            	
            	classOrIntUri.addProperty(isAbstractProperty,
            			model.createTypedLiteral(true));
            }


			writeRdf(model);

		}

	}

	@Override
	protected void generatePackageFiles(ClassTree arg0) throws Exception {
		// TODO Auto-generated method stub

	}

	private void createConstructorRdf(Resource classOrIntUri, ClassDoc curr) {
		// handle the constructors
		for (ConstructorDoc con : curr.constructors()) {
			Resource methodUri = model.createResource(codeBasePrefix
					+ con.qualifiedName().replace(".", "/"));
			classOrIntUri.addProperty(this.constructorProperty, methodUri);

			parametersToRdf(con, methodUri);

		}

	}

	private void createMethodsRdf(Resource classOrIntUri, ClassDoc curr) {
		// handle the constructors
		for (MethodDoc m : curr.methods()) {
			Resource methodUri = model.createResource(codeBasePrefix
					+ m.qualifiedName().replace(".", "/"));
			classOrIntUri.addProperty(this.methodProperty, methodUri);

			parametersToRdf(m, methodUri);
			
			// parameterised return types need to be handled simularly to method parameters.
			// need to apply here to - opportunity to generalise both services in handling parameterise types
			Resource returnTypeUri = model.createResource(codeBasePrefix
					+ m.returnType().qualifiedTypeName().replace(".", "/"));
			methodUri.addProperty(returnsProperty, returnTypeUri);

		}

	}

	private void parametersToRdf(ExecutableMemberDoc method, Resource methodUri) {
		// there is a conceptual issue here because a method does not know if a
		// Parameters type is an Interface or a Class
		// we have taken the decision that methods consider everything to be an
		// Interface.
		// this has the consequence that all Classes will end up also being of
		// type Interface in RFD!!

		for (Parameter p : method.parameters()) {

			// Could not figure out how to use the API to determine types on
			// ParameterizedTypes so just parsed string with
			// a utility method.
			Resource[] g = getParameterizedType(p);

			switch (g.length) {
			case 1:
				methodUri.addProperty(parameterProperty,
						model.createResource().addProperty(nameProperty, p.name())
								.addProperty(parameterTypeProperty, g[0]));
				break;
			case 2:
				methodUri.addProperty(parameterProperty,
						model.createResource().addProperty(nameProperty, p.name())
								.addProperty(parameterTypeProperty, g[0])
								.addProperty(parameterBoundProperty, g[1]));
				break;
			default:
				System.out
						.println("unexecpected problem handling parameterized method arguments - skipped");
				break;
			}

		}

	}

	private Resource[] getParameterizedType(Parameter p) {

		Resource[] r = null;

		if (!p.type().toString().contains("<")) {
			Resource pType = model.createResource(codeBasePrefix
					+ p.type().qualifiedTypeName().replace(".", "/"));
			r = new Resource[1];
			r[0] = pType;
		} else {
			// Unpack using literal to create a java:parameterBound
			// Parameter type didn't have any methods to do this better
			int open = p.typeName().indexOf('<');
			int close = p.typeName().indexOf('>');

			r = new Resource[2];
			r[0] = model.createResource(codeBasePrefix
					+ p.typeName().substring(0, open).replace(".", "/"));
			r[1] = model.createResource(codeBasePrefix
					+ p.typeName().substring(open + 1, close).replace(".", "/"));
			
			System.out.println("p.type().qualifiedTypeName() = " + p.type().qualifiedTypeName());
			System.out.println("p.typeName() = " + p.typeName());
			
		}

		return r;

	}
	
	private void writeRdf(Model model) {
		try {
 
			File file = new File( getFileName() + ".n3");
 
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
 
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
 
			model.write(bw, "N3");
			bw.close();

		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error writing out RDF to file " + getFileName() );
		}
	}
	
    private static String readOptions(String[][] options) {
        String fileName = null;
        for (int i = 0; i < options.length; i++) {
            String[] opt = options[i];
            if (opt[0].equals("-file")) {
                fileName = opt[1];
            }
        }
        return fileName;
    }

    public static int optionLength(String option) {
        if(option.equals("-file")) {
            return 2;
        }
        return 0;
    }

    public static boolean validOptions(String options[][], 
                                       DocErrorReporter reporter) {
        boolean foundFileOption = false;
        for (int i = 0; i < options.length; i++) {
            String[] opt = options[i];
            if (opt[0].equals("-file")) {
                if (foundFileOption) {
                    reporter.printError("Only one -file option allowed.");
                    return false;
                } else { 
                    foundFileOption = true;
                }
            } 
        }
        if (!foundFileOption) {
            reporter.printError("Usage: javadoc -file myfilename -doclet RdfDoclet ...");
        }
        return foundFileOption;
    }

	public String getCodeBasePrefix() {
		return codeBasePrefix;
	}

	public void setCodeBasePrefix(String codeBasePrefix) {
        model.setNsPrefix( "", codeBasePrefix );
		this.codeBasePrefix = codeBasePrefix;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}
