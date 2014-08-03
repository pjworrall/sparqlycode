package net.interition.sparqlycode.doclet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.jena.atlas.logging.Log;

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

		// obviously this needs to be resolved against build information
		System.out
				.println("Sparqlycode v 0.0.2a . (C) copyright 2014 Interition Limited. All Rights Reserved. ");

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
			typeUri.addProperty(RDFS.label, curr.name());
			typeUri.addProperty(JAVALANG.name, curr.name());
			typeUri.addProperty(JAVALANG._package, curr.containingPackage()
					.name());

			// add a line number reference
			typeUri.addProperty(JAVALANG.lineNumber,
					model.createTypedLiteral(curr.position().line()));

			// add some simple attributes
			if (curr.isAbstract()) {
				typeUri.addProperty(JAVALANG.isAbsract,
						model.createTypedLiteral(true));
			}

			if (curr.isSerializable()) {
				typeUri.addProperty(JAVALANG.isSerializable,
						model.createTypedLiteral(true));
			}

			if (curr.isFinal()) {
				typeUri.addProperty(JAVALANG.isFinal,
						model.createTypedLiteral(true));
			}

			// if inner class then create relationship
			if (curr.containingClass() != null) {
				Resource containingClazz = model.createResource(baseUri
						+ curr.containingClass().qualifiedName()
								.replace(".", "/"));
				typeUri.addProperty(JAVALANG.innerClassOf, containingClazz);
			}

			// handle imports
			createImportsRdf(typeUri, curr);

			// todo: might get closer to resolving the Uri minting problem for
			// dependencies
			// with curr.importedClasses()

			// constructors should be reusing the same stuff as methods. just
			// the resource type is different!
			createConstructorRdf(typeUri, curr);

			createMethodsRdf(typeUri, curr);

			// handle super classes
			if (curr.superclass() != null) {
				Resource superClazz = model.createResource(baseUri
						+ curr.superclass().qualifiedName().replace(".", "/"));
				typeUri.addProperty(JAVALANG._extends, superClazz);
			}

			// handle implemented interfaces
			for (Type implementedInterface : curr.interfaceTypes()) {
				Resource interfaceResource = model.createResource(baseUri
						+ implementedInterface.qualifiedTypeName().replace(".",
								"/"));
				typeUri.addProperty(JAVALANG._implements, interfaceResource);
			}

			// what can we do with annotations ?
			for (AnnotationDesc desc : curr.annotations()) {
				AnnotationTypeDoc a = desc.annotationType();
				Resource annotationTypeUri = model.createResource(baseUri
						+ a.qualifiedTypeName().replace(".", "/"));
				typeUri.addProperty(JAVALANG.hasAnnotation, annotationTypeUri);
			}

			// handle fields
			for (FieldDoc field : curr.fields()) {
				Resource fieldResource = model.createResource(baseUri
						+ field.qualifiedName().replace(".", "/"));

				fieldResource.addProperty(RDF.type, JAVALANG.AField);

				typeUri.addProperty(JAVALANG.field, fieldResource);

				// add a line number reference
				fieldResource.addProperty(JAVALANG.lineNumber,
						model.createTypedLiteral(field.position().line()));

				// assign a label
				fieldResource.addProperty(RDFS.label, field.name());

				if (field.isStatic()) {
					fieldResource.addProperty(JAVALANG.isStatic,
							model.createTypedLiteral(true));
				}

				if (field.isFinal()) {
					fieldResource.addProperty(JAVALANG.isFinal,
							model.createTypedLiteral(true));
				}

				if (field.isTransient()) {
					fieldResource.addProperty(JAVALANG.isTransient,
							model.createTypedLiteral(true));
				}

				if (field.isVolatile()) {
					fieldResource.addProperty(JAVALANG.isVolatile,
							model.createTypedLiteral(true));
				}

				// what can we do with annotations ?
				for (AnnotationDesc desc : field.annotations()) {
					AnnotationTypeDoc a = desc.annotationType();
					Resource annotationTypeUri = model.createResource(baseUri
							+ a.qualifiedTypeName().replace(".", "/"));
					fieldResource.addProperty(JAVALANG.hasAnnotation,
							annotationTypeUri);
				}

				Access access = Access.createAccessModifier(field);
				fieldResource.addProperty(JAVALANG.access, access.getLabel());

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

			classOrIntUri.addProperty(JAVALANG.javaImport, packageUri);
		}

	}

	@Override
	protected void generatePackageFiles(ClassTree arg0) throws Exception {
		// TODO Auto-generated method stub

	}

	private void createConstructorRdf(Resource classOrIntUri, ClassDoc curr) {
		// handle the constructors
		// duplication here as it is the same as methods generally
		for (ConstructorDoc con : curr.constructors()) {

			int line = con.position().line();

			Resource methodUri = model.createResource(baseUri
					+ con.qualifiedName().replace(".", "/") + "#" + line);
			classOrIntUri.addProperty(JAVALANG.constructor, methodUri);

			// add a line number reference
			methodUri.addProperty(JAVALANG.lineNumber,
					model.createTypedLiteral(line));

			// throws any types ?
			for (Type t : con.thrownExceptionTypes()) {
				Resource thrownTypeUri = model.createResource(baseUri
						+ t.qualifiedTypeName().replace(".", "/"));

				methodUri.addProperty(JAVALANG._throws, thrownTypeUri);

			}

			// what can we do with annotations ?
			for (AnnotationDesc desc : con.annotations()) {
				AnnotationTypeDoc a = desc.annotationType();
				Resource annotationTypeUri = model.createResource(baseUri
						+ a.qualifiedTypeName().replace(".", "/"));
				methodUri
						.addProperty(JAVALANG.hasAnnotation, annotationTypeUri);
			}

			// create a label for the method
			methodUri.addProperty(RDFS.label, con.name());

			parametersToRdf(con, methodUri);

		}

	}

	private void createMethodsRdf(Resource classOrIntUri, ClassDoc curr) {
		// handle the constructors
		for (MethodDoc method : curr.methods()) {

			// get the line number as it will be used a couple of times
			int line = method.position().line();

			Resource methodUri = model.createResource(baseUri
					+ method.qualifiedName().replace(".", "/") + "#" + line);

			// support for generic method types
			TypeVariable[] tv = method.typeParameters();

			for (TypeVariable t : tv) {
				methodUri.addProperty(JAVALANG.typeVariable,
						model.createTypedLiteral(t.typeName()));
			}

			// add a line number reference
			methodUri.addProperty(JAVALANG.lineNumber,
					model.createTypedLiteral(line));

			methodUri.addProperty(RDF.type, JAVALANG.AMethod);

			classOrIntUri.addProperty(JAVALANG.method, methodUri);

			// add access modifier
			Access access = Access.createAccessModifier(method);
			methodUri.addProperty(JAVALANG.access, access.getLabel());

			// is final
			if (method.isFinal()) {
				methodUri.addProperty(JAVALANG.isFinal,
						model.createTypedLiteral(true));
			}

			// is it static?
			if (method.isStatic()) {
				methodUri.addProperty(JAVALANG.isStatic,
						model.createTypedLiteral(true));
			}

			// is abstract ?
			if (method.isAbstract()) {
				methodUri.addProperty(JAVALANG.isAbsract,
						model.createTypedLiteral(true));
			}

			// is abstract ?
			if (method.isSynchronized()) {
				methodUri.addProperty(JAVALANG.isSynchronized,
						model.createTypedLiteral(true));
			}

			// throws any types ?
			for (Type t : method.thrownExceptionTypes()) {
				Resource thrownTypeUri = model.createResource(baseUri
						+ t.qualifiedTypeName().replace(".", "/"));

				methodUri.addProperty(JAVALANG._throws, thrownTypeUri);

			}

			// what can we do with annotations ?
			for (AnnotationDesc desc : method.annotations()) {
				AnnotationTypeDoc a = desc.annotationType();
				Resource annotationTypeUri = model.createResource(baseUri
						+ a.qualifiedTypeName().replace(".", "/"));
				methodUri
						.addProperty(JAVALANG.hasAnnotation, annotationTypeUri);
			}

			// create a label for the method
			methodUri.addProperty(RDFS.label, method.name());

			// process the method parameters
			parametersToRdf(method, methodUri);

			// process the method return type
			returnTypesToRdf(method, methodUri);

		}

	}

	private void returnTypesToRdf(MethodDoc method, Resource methodUri) {

		// generate the resource to the main java Class type
		Resource returnTypeUri = model.createResource(baseUri
				+ method.returnType().qualifiedTypeName().replace(".", "/"));

		// if it is a parameterised type ...
		ParameterizedType type = method.returnType().asParameterizedType();

		if (type instanceof ParameterizedType) {
			// debug
			System.out.println(methodUri.toString());
			
			// the generic type info needs to be appended here with addProperty
			// YOU JUST NEED TO TEST ID THE RETURN TYPES HAVE THE NEW CONCEPT ATTACHED ON WILDCARD PARAMTER TYPES
			methodUri.addProperty(
					JAVALANG.returns,
					model.createResource()
							.addProperty(JAVALANG.type, returnTypeUri)
							.addProperty(JAVALANG.typeParameter, parameterizedTypesToRdf(type)));

		} else {
			// simple non generic type
			methodUri.addProperty(JAVALANG.returns, model.createResource()
					.addProperty(JAVALANG.type, returnTypeUri));
		}

	}

	/*
	 * 
	 * Processes a methods arguments that have parameterised type
	 * 
	 * Parameterised types represent an invocation of a generic class or
	 * interface. For example, given the generic interface List<E>, possible
	 * invocations include: (1) List<String> (2) List<T extends Number> (3)
	 * List<?>
	 */
	private Resource parameterizedTypesToRdf(ParameterizedType type) {

		// so we have to handle ordinary parameter types (1), extends (2) and
		// wildcard (3)

		// create an anonymous node to return
		Resource parameterizedType = model.createResource();

		for (Type t : type.typeArguments()) {
			
			// add the type
			parameterizedType.addProperty(
					JAVALANG.type,
					model.createResource(baseUri
							+ t.qualifiedTypeName().replace(".", "/")));
			
			System.out.println("=== START ===");
			System.out.println("Properties of Type Argument: ");
			System.out.println("qualifiedTypeName: " + t.qualifiedTypeName());
			System.out.println("simpleTypeName: " + t.simpleTypeName());
			System.out.println("typeName: " + t.typeName());

			if (t.asTypeVariable() != null) {
				System.out.println("asTypeVariable: "
						+ t.asTypeVariable().toString());
				for (Type bound : t.asTypeVariable().bounds()) {
					System.out.println("---> bounds: " + bound.typeName());
				}

			}
			if (t.asWildcardType() != null) {
				System.out.println("asWildcardType: "
						+ t.asWildcardType().toString());

				for (Type bound : t.asWildcardType().extendsBounds()) {
					System.out.println("---> extendsBounds: "
							+ bound.typeName());
					
					parameterizedType.addProperty(
							JAVALANG._extends,
							model.createResource(baseUri
									+ bound.qualifiedTypeName().replace(".", "/")));
				}

				for (Type _super : t.asWildcardType().superBounds()) {
					System.out
							.println("---> superBounds: " + _super.typeName());
					
					parameterizedType.addProperty(
							JAVALANG._super,
							model.createResource(baseUri
									+ _super.qualifiedTypeName().replace(".", "/")));
				}
				


			}
			System.out.println("=== END === ");


		}

		return parameterizedType;

	}

	// this is being replaced
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
				methodUri.addProperty(
						JAVALANG.argument,
						model.createResource()
								.addProperty(JAVALANG.name, p.name())
								.addProperty(JAVALANG.type, g[0]));
				break;
			case 2:
				methodUri.addProperty(
						JAVALANG.argument,
						model.createResource()
								.addProperty(JAVALANG.name, p.name())
								.addProperty(JAVALANG.type, g[0])
								.addProperty(JAVALANG.typeParameter, g[1]));
				break;
			default:
				System.out
						.println("unexecpected problem handling parameterized method arguments - skipped");
				break;
			}

		}

	}

	// old method we are trying to replace
	private Resource[] getParameterizedType(Parameter p) {

		Resource[] r = null;

		// System.out.println("parameterizedType> " + p.type());
		// System.out.println("parameterizedTypeName> " + p.typeName());

		if (!p.type().toString().contains("<")) {
			Resource pType = model.createResource(baseUri
					+ p.type().qualifiedTypeName().replace(".", "/"));
			r = new Resource[1];
			r[0] = pType;
		} else {

			// Unpack using literal to create a java:typeParameter
			// argument type didn't have any methods to do this better
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

	private String getType(Type type) {

		// don't have to handle these at the moment
		// type.asAnnotationTypeDoc();
		// type.asClassDoc();

		/*
		 * 
		 * ParameterizedType: Represents an invocation of a generic class or
		 * interface. For example, given the generic interface List<E>, possible
		 * invocations include: List<String> List<T extends Number> List<?>
		 */
		// type.asParameterizedType();

		/*
		 * 
		 * Represents a type variable. For example, the generic interface
		 * List<E> has a single type variable E. A type variable may have
		 * explicit bounds, as in C<R extends Remote>.
		 */
		// type.asTypeVariable();

		/*
		 * 
		 * Represents a wildcard type argument. Examples include: <?> <? extends
		 * E> <? super T>
		 * 
		 * A wildcard type can have explicit extends bounds or explicit super
		 * bounds or neither, but not both.
		 */
		// type.asWildcardType();

		// type.isPrimitive() ;

		return "unimplemented";
	}

}
