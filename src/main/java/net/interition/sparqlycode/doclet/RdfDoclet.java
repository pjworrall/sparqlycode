package net.interition.sparqlycode.doclet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.interition.sparqlycode.vocabulary.Access;
import net.interition.sparqlycode.vocabulary.JAVALANG;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.sun.javadoc.*;
import com.sun.tools.doclets.formats.html.ConfigurationImpl;
import com.sun.tools.doclets.internal.toolkit.Configuration;
import com.sun.tools.doclets.internal.toolkit.util.ClassTree;

public class RdfDoclet extends AbstractDoclet {

	public ConfigurationImpl configuration;

	protected static final String sparqlyCodeDomainPrefix = "http://www.interition.net/java/ref/";

	private static String baseUriDefault = "http://www.sparqlycode.com/id/";
	private static String fileNameDefault = "sparqlycode.ttl";

	private String baseUri = null;
	private String fileName = null;

	// create an empty Model
	private Model model = ModelFactory.createDefaultModel();

	public Configuration configuration() {
		return ConfigurationImpl.getInstance();
	}

	public RdfDoclet() {
		configuration = (ConfigurationImpl) configuration();
	}

	public static boolean start(RootDoc root) {

		try {
			RdfDoclet doclet = new RdfDoclet();

			Map<String, String> options = readOptions(root.options());
			String fileName = (options.get("file") == null) ? RdfDoclet.fileNameDefault
					: options.get("file");
			doclet.setFileName(fileName);

			String baseUri = (options.get("baseuri") == null) ? RdfDoclet.baseUriDefault
					: options.get("baseuri");
			doclet.setBaseUri(baseUri);

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

			Resource typeUri = model.createResource(baseUri
					+ curr.qualifiedName().replace(".", "/"));

			// check if class or interface. potential bug here is it is not
			// either!!
			if (curr.isClass()) {
				typeUri.addProperty(RDF.type, JAVALANG.Class);

			}

			if (curr.isInterface()) {
				typeUri.addProperty(RDF.type, JAVALANG.Interface);
			}
			
			if (curr.isEnum()) {
				typeUri.addProperty(RDF.type, JAVALANG.Enum);
			}

			// create basic metadata
			typeUri.addProperty(RDFS.label, curr.name(), "en");
			typeUri.addProperty(JAVALANG.Name, curr.name());
			typeUri.addProperty(JAVALANG.Package, curr
					.containingPackage().name());

			// add a line number reference
			typeUri.addProperty(JAVALANG.LineNumber,
					model.createTypedLiteral(curr.position().line()));
			
			// add some simple attributes
			if (curr.isAbstract()) {
				typeUri.addProperty(JAVALANG.IsAbsract,
						model.createTypedLiteral(true));
			}
			
			if (curr.isSerializable()) {
				typeUri.addProperty(JAVALANG.IsSerializable,
						model.createTypedLiteral(true));
			}
			
			// if inner class then create relationship
			if(curr.containingClass() != null) {
				Resource containingClazz = model.createResource(baseUri
						+ curr.containingClass().qualifiedName().replace(".", "/"));
				typeUri.addProperty(JAVALANG.InnerClassOf, containingClazz);
			}
			

			// handle imports
			createImportsRdf(typeUri, curr);

			// todo: might get closer to resolving the Uri minting problem for
			// dependencies
			// with curr.importedClasses()

			createConstructorRdf(typeUri, curr);

			createMethodsRdf(typeUri, curr);

			// handle super classes
			if (curr.superclass() != null) {
				Resource superClazz = model.createResource(baseUri
						+ curr.superclass().qualifiedName().replace(".", "/"));
				typeUri.addProperty(JAVALANG.Extends, superClazz);
			}
			
			// handle implemented interfaces
			for(Type implementedInterface : curr.interfaceTypes() ) {
				Resource interfaceResource = model.createResource(baseUri
						+ implementedInterface.qualifiedTypeName().replace(".", "/"));
				typeUri.addProperty(JAVALANG.Implements,interfaceResource);
			}
				
			
			
			// handle fields
			for (FieldDoc field : curr.fields()) {
				Resource fieldResource = model.createResource(baseUri
						+ field.qualifiedName().replace(".", "/"));
				typeUri.addProperty(JAVALANG.Field, fieldResource);
				

				if (field.isStatic()) {
					fieldResource.addProperty(JAVALANG.IsStatic,
							model.createTypedLiteral(true));
				}
				
				if (field.isFinal()) {
					fieldResource.addProperty(JAVALANG.IsFinal,
							model.createTypedLiteral(true));
				}
				
				if (field.isTransient()) {
					fieldResource.addProperty(JAVALANG.IsTransient,
							model.createTypedLiteral(true));
				}
				
				if (field.isVolatile()) {
					fieldResource.addProperty(JAVALANG.IsVolatile,
							model.createTypedLiteral(true));
				}
				
				Access access = Access.createAccessModifier(field);
				fieldResource.addProperty(JAVALANG.Access, access.getLabel(),
						"en");

			}
			

			writeRdf(model);

		}

	}

	private void createImportsRdf(Resource classOrIntUri, ClassDoc curr) {
		// although the method is deprecated it might still works. It is removed
		// for a
		// design philosophy purpose
		// and not for implementation reasons. Sparqlycode is actually raising a
		// requirement for
		// processing package declarations for reasons more important than that
		// stated for ignoring imports.

		@SuppressWarnings("deprecation")
		PackageDoc[] packages = curr.importedPackages();

		for (PackageDoc p : packages) {
			Resource packageUri = model.createResource(baseUri
					+ p.name().replace(".", "/"));

			classOrIntUri.addProperty(JAVALANG.Import, packageUri);
		}

	}

	@Override
	protected void generatePackageFiles(ClassTree arg0) throws Exception {
		// TODO Auto-generated method stub

	}

	private void createConstructorRdf(Resource classOrIntUri, ClassDoc curr) {
		// handle the constructors
		for (ConstructorDoc con : curr.constructors()) {
			Resource methodUri = model.createResource(baseUri
					+ con.qualifiedName().replace(".", "/"));
			classOrIntUri.addProperty(JAVALANG.Constructor, methodUri);

			// add a line number reference
			methodUri.addProperty(JAVALANG.LineNumber,
					model.createTypedLiteral(con.position().line()));

			parametersToRdf(con, methodUri);

		}

	}

	private void createMethodsRdf(Resource classOrIntUri, ClassDoc curr) {
		// handle the constructors
		for (MethodDoc m : curr.methods()) {
			Resource methodUri = model.createResource(baseUri
					+ m.qualifiedName().replace(".", "/"));
			classOrIntUri.addProperty(JAVALANG.Method, methodUri);

			// add a line number reference
			methodUri.addProperty(JAVALANG.LineNumber,
					model.createTypedLiteral(m.position().line()));

			// add access modifier
			Access access = Access.createAccessModifier(m);
			methodUri.addProperty(JAVALANG.Access, access.getLabel(), "en");
			
			// is it static?
			if (curr.isStatic()) {
				methodUri.addProperty(JAVALANG.IsStatic,
						model.createTypedLiteral(true));
			}

			// create a label for the method
			methodUri.addProperty(RDFS.label, m.name(), "en");
			parametersToRdf(m, methodUri);

			// parameterised return types need to be handled simularly to method
			// parameters.
			// need to apply here to - opportunity to generalise both services
			// in handling parameterise types
			Resource returnTypeUri = model.createResource(baseUri
					+ m.returnType().qualifiedTypeName().replace(".", "/"));
			methodUri.addProperty(JAVALANG.Returns, returnTypeUri);

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
			// This did not work very well and needs special attention - see
			// COD-41.
			// in particular had to have a special case to avoid processing
			// things like E,K,T...etc. in Generics
			if (p.typeName().length() == 1)
				break;

			// ..also avoid extends and super of generics
			if (p.typeName().matches("(^.* extends .*$)|(^.* super .*$)"))
				break;

			Resource[] g = getParameterizedType(p);

			switch (g.length) {
			case 1:
				methodUri.addProperty(JAVALANG.Parameter, model
						.createResource().addProperty(JAVALANG.Name, p.name())
						.addProperty(JAVALANG.ParameterType, g[0]));
				break;
			case 2:
				methodUri.addProperty(
						JAVALANG.Parameter,
						model.createResource()
								.addProperty(JAVALANG.Name, p.name())
								.addProperty(JAVALANG.ParameterType, g[0])
								.addProperty(JAVALANG.ParameterBound, g[1]));
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

		System.out.println("parameterizedType> " + p.type());
		System.out.println("parameterizedTypeName> " + p.typeName());

		if (!p.type().toString().contains("<")) {
			Resource pType = model.createResource(baseUri
					+ p.type().qualifiedTypeName().replace(".", "/"));
			r = new Resource[1];
			r[0] = pType;
		} else {

			// Unpack using literal to create a java:parameterBound
			// Parameter type didn't have any methods to do this better
			int open = p.typeName().indexOf('<');
			int close = p.typeName().indexOf('>');

			r = new Resource[2];

			String ptype = p.typeName().substring(0, open)
					.replaceAll("(\\s+)|(<|>)", "").replace(".", "/");
			r[0] = model.createResource(baseUri + ptype);

			String pbound = p.typeName().substring(open + 1, close)
					.replaceAll("(\\s+)|(<|>)", "").replace(".", "/");
			r[1] = model.createResource(baseUri + pbound);

		}

		return r;

	}

	private void writeRdf(Model model) {
		try {

			File file = new File(getFileName());

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			model.write(bw, "TURTLE");
			bw.close();

		} catch (IOException e) {
			e.printStackTrace();
			System.out
					.println("Error writing out RDF to file " + getFileName());
		}
	}

	private static Map<String, String> readOptions(String[][] options) {
		Map<String, String> optionValues = new HashMap<String, String>();

		for (int i = 0; i < options.length; i++) {
			String[] opt = options[i];
			if (opt[0].equals("-file")) {
				optionValues.put("file", opt[1]);
			}
			if (opt[0].equals("-baseuri")) {
				optionValues.put("baseuri", opt[1]);
			}
		}
		return optionValues;
	}

	public static int optionLength(String option) {
		if (option.equals("-file")) {
			return 2;
		}
		if (option.equals("-baseuri")) {
			return 2;
		}
		return 0;
	}

	public static boolean validOptions(String options[][],
			DocErrorReporter reporter) {
		// it doesn't really matter if the options aren't provided as we have
		// defaults but we keep the method
		// as a template for the future.

		boolean foundOptions = true;

		if (!foundOptions) {
			reporter.printError("Usage: javadoc -file myfilename -baseuri uri -doclet RdfDoclet ...");
		}

		return foundOptions;
	}

	public String getBaseUri() {
		return baseUri;
	}

	public void setBaseUri(String baseUri) {
		model.setNsPrefix("", baseUri);
		this.baseUri = baseUri;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}
