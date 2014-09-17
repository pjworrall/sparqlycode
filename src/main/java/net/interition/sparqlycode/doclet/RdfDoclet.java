package net.interition.sparqlycode.doclet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

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
				.println("Sparqlycode v 0.0.2j . (C) copyright 2014 Interition Limited. All Rights Reserved. ");

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
			if (curr.isEnum()) {
				typeUri.addProperty(RDF.type, JAVALANG.Enum);
			} else if (curr.isInterface()) {
				typeUri.addProperty(RDF.type, JAVALANG.Interface);
			} else if (curr.isClass()) {
				typeUri.addProperty(RDF.type, JAVALANG.Class);
			}

			// create basic metadata
			typeUri.addProperty(RDFS.label, curr.name());
			typeUri.addProperty(JAVALANG._package, curr.containingPackage()
					.name());

			// add a line number reference
			typeUri.addProperty(JAVALANG.lineNumber,
					model.createTypedLiteral(curr.position().line()));

			// add access modifier
			Access classAccess = Access.createAccessModifier(curr);
			typeUri.addProperty(JAVALANG.access, classAccess.getLabel());

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
			// createConstructorRdf(typeUri, curr);

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

			generateFieldsRDF(typeUri, curr);

			writeRdf(model);

		}

	}

	private void generateFieldsRDF(Resource typeUri, ClassDoc _class) {

		// handle fields
		for (FieldDoc field : _class.fields()) {

			// the field type
			Resource fieldTypeResource = model.createResource(baseUri
					+ field.type().qualifiedTypeName().replace(".", "/"));

			// the line it is on .. used subsequently
			int line = field.position().line();

			Resource fieldResource = model.createResource(baseUri
					+ field.qualifiedName().replace(".", "/") + "#" + line);

			fieldResource.addProperty(RDF.type, JAVALANG._Field);

			fieldResource.addProperty(JAVALANG.type, fieldTypeResource);

			typeUri.addProperty(JAVALANG.field, fieldResource);

			// add a line number reference
			fieldResource.addProperty(JAVALANG.lineNumber,
					model.createTypedLiteral(line));

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

			Access fieldAccess = Access.createAccessModifier(field);
			fieldResource.addProperty(JAVALANG.access, fieldAccess.getLabel());

			// generic type handling
			ParameterizedType type = field.type().asParameterizedType();

			if (type instanceof ParameterizedType) {
				fieldResource.addProperty(JAVALANG.typeParameter,
						parameterizedTypesToRdf(type));
			}
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

	private void createMethodsRdf(Resource classOrIntUri, ClassDoc curr) {

		// we want to process both methods and constructors

		ExecutableMemberDoc[] members = new ExecutableMemberDoc[curr
				.constructors().length + curr.methods().length];
		System.arraycopy(curr.constructors(), 0, members, 0,
				curr.constructors().length);
		System.arraycopy(curr.methods(), 0, members,
				curr.constructors().length, curr.methods().length);

		for (ExecutableMemberDoc method : members) {

			// get the line number as it will be used a couple of times
			int line = method.position().line();

			Resource methodUri = model.createResource(baseUri
					+ method.qualifiedName().replace(".", "/") + "#" + line);

			// moved typing and class relationship to here so we can handle
			// constructors as well
			if (method.isConstructor()) {
				methodUri.addProperty(RDF.type, JAVALANG._Constructor);
				classOrIntUri.addProperty(JAVALANG.constructor, methodUri);
			} else {
				methodUri.addProperty(RDF.type, JAVALANG._Method);
				classOrIntUri.addProperty(JAVALANG.method, methodUri);
			}

			// support for generic method types
			TypeVariable[] tv = method.typeParameters();

			for (TypeVariable t : tv) {
				methodUri.addProperty(JAVALANG.typeVariable,
						model.createTypedLiteral(t.typeName()));
			}

			// add a line number reference
			methodUri.addProperty(JAVALANG.lineNumber,
					model.createTypedLiteral(line));

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

			// is synchronised ?
			if (method.isSynchronized()) {
				methodUri.addProperty(JAVALANG.isSynchronized,
						model.createTypedLiteral(true));
			}

			// is Native
			if (method.isNative()) {
				methodUri.addProperty(JAVALANG.isNative,
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
			argumentsToRdf(method, methodUri);

			// things specific to a method
			if (method instanceof MethodDoc) {

				// is abstract ?
				if (((MethodDoc) method).isAbstract()) {
					methodUri.addProperty(JAVALANG.isAbsract,
							model.createTypedLiteral(true));
				}

				// process the method return type
				returnTypesToRdf((MethodDoc) method, methodUri);

			}

		}

	}

	private void argumentsToRdf(ExecutableMemberDoc method, Resource methodUri) {
		// The javadoc API is a bit confusing, calling arguments parameters.
		// watch out.

		for (Parameter argument : method.parameters()) {

			Resource argumentTypeUri = model.createResource(baseUri
					+ argument.type().qualifiedTypeName().replace(".", "/"));

			// warning that type will be null if it is not a Parameterised Type
			ParameterizedType type = argument.type().asParameterizedType();
			if (type instanceof ParameterizedType) {
				methodUri.addProperty(
						JAVALANG.argument,
						model.createResource()
								.addProperty(JAVALANG.type, argumentTypeUri)
								.addProperty(JAVALANG.typeParameter,
										parameterizedTypesToRdf(type)));
			} else {
				// an ordinary type I hope

				// outstanding issue btw of how we know something is
				// representing an array!
				methodUri.addProperty(JAVALANG.argument, model.createResource()
						.addProperty(JAVALANG.type, argumentTypeUri)
						.addProperty(RDFS.label, argument.name()));
			}
		}
	}

	private void returnTypesToRdf(MethodDoc method, Resource methodUri) {

		Resource returnTypeUri = model.createResource(baseUri
				+ method.returnType().qualifiedTypeName().replace(".", "/"));

		// if it is a parameterised type ...
		ParameterizedType type = method.returnType().asParameterizedType();

		if (type instanceof ParameterizedType) {
			
			System.out.println(method.name() + " :return type: " + method.returnType().typeName() );

			// the generic type info needs to be appended here with addProperty
			methodUri.addProperty(
					JAVALANG.returns,
					model.createResource()
							.addProperty(JAVALANG.type, returnTypeUri)
							.addProperty(JAVALANG.typeParameter,
									parameterizedTypesToRdf(type)));

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
	private Resource parameterizedTypesToRdf(ParameterizedType parameterizedType) {

		// create an anonymous node to return
		Resource typeArguments = model.createResource();

		for (Type t : parameterizedType.typeArguments()) {
			
			if (t.asTypeVariable() != null) {
				Resource typeArgument = model.createResource();
				typeArgument.addProperty(RDFS.label, t.typeName());
				typeArguments.addProperty(JAVALANG.argument, typeArgument);

				// don't understand what bounds means at the moment so just
				// binding the name of the type variable eg T
				// TODO: looked like a duplicate property so I took out 
				//typeArgument.addProperty(RDFS.label, t.simpleTypeName());

			} else if (t.asWildcardType() != null) {

				Resource typeArgument = model.createResource();
				typeArgument.addProperty(RDFS.label, t.typeName());
				typeArguments.addProperty(JAVALANG.argument, typeArgument);

				for (Type _bound : t.asWildcardType().extendsBounds()) {
					// bound could be processed recursively
					typeArgument.addProperty(
							JAVALANG._extends,
							model.createResource(baseUri
									+ _bound.qualifiedTypeName().replace(".",
											"/")));
				}

				for (Type _super : t.asWildcardType().superBounds()) {
					// _super can be processed recursively
					typeArgument.addProperty(
							JAVALANG._super,
							model.createResource(baseUri
									+ _super.qualifiedTypeName().replace(".",
											"/")));
				}

			} else if (t.asParameterizedType() != null) {
				// this is a recursive bit because types can also be
				// parameterised
				// ignore for now
				// t.asParameterizedType()
			} else {
				// property does not need intermediary bnode in this case - OH YES IT DOES!
				
				System.out.println( "parameterizedType.typeArguments(): " + t.qualifiedTypeName()  );
				
				Resource typeArgument = model.createResource();
				typeArgument.addProperty(RDFS.label, t.typeName());
				typeArguments.addProperty(JAVALANG.argument, typeArgument);
				
				typeArgument.addProperty(
						JAVALANG.argument,
						model.createResource(baseUri
								+ t.qualifiedTypeName().replace(".", "/")));
			}

		}

		return typeArguments;

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
