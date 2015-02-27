package net.interition.sparqlycode.doclet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.interition.sparlycode.model.ISCO;
import net.interition.sparlycode.model.JAVAO;
import net.interition.sparqlycode.vocabulary.Access;

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

	private final Log log = LogFactory.getLog(RdfDoclet.class);

	public ConfigurationImpl configuration;

	protected static final String sparqlyCodeDomainPrefix = "http://www.interition.net/java/ref/";

	private static String baseUriDefault = "http://www.sparqlycode.com/id/";
	private static String fileNameDefault = "sparqlycode.ttl";

	private String baseUri = null;
	private String fileUri = null;
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
				.println("Sparqlycode v 0.0.4-SNAPSHOT . (C) copyright 2014 Interition Limited. All Rights Reserved. ");

		try {
			RdfDoclet doclet = new RdfDoclet();

			Map<String, String> options = readOptions(root.options());
			String fileName = (options.get("file") == null) ? RdfDoclet.fileNameDefault
					: options.get("file");

			doclet.setFileName(fileName);

			String baseUri = (options.get("baseuri") == null) ? RdfDoclet.baseUriDefault
					: options.get("baseuri");

			doclet.setBaseUri(baseUri);

			// base file uri on baseUri if http schema else make the same as
			// baseUri
			if (baseUri.startsWith("http:")) {
				doclet.setFileUri(baseUri.replaceFirst("http:", "file:"));
			} else {
				doclet.setFileUri(baseUri);
			}

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

			Resource typeUri = createHttpResource(curr.qualifiedName());

			// if there is no containing class create a file reference
			if (curr.containingClass() == null) {
				Resource fileResource = createFileResource(curr.qualifiedName());
				typeUri.addProperty(ISCO.file, fileResource);
			}

			// check if class or interface. potential bug here is it is not
			// either!!
			if (curr.isEnum()) {
				typeUri.addProperty(RDF.type, JAVAO.Enum);
			} else if (curr.isInterface()) {
				typeUri.addProperty(RDF.type, JAVAO.Interface);
			} else if (curr.isClass()) {
				typeUri.addProperty(RDF.type, JAVAO.Class);
			}

			// create basic metadata
			typeUri.addProperty(RDFS.label, curr.name());
			typeUri.addProperty(JAVAO.package_, curr.containingPackage().name());

			// add a line number reference
			typeUri.addProperty(JAVAO.lineNumber,
					model.createTypedLiteral(curr.position().line()));

			// add access modifier
			Access classAccess = Access.createAccessModifier(curr);
			typeUri.addProperty(JAVAO.access, classAccess.getLabel());

			// add some simple attributes
			if (curr.isAbstract()) {
				typeUri.addProperty(JAVAO.isAbstract,
						model.createTypedLiteral(true));
			}

			if (curr.isSerializable()) {
				typeUri.addProperty(JAVAO.isSerializable,
						model.createTypedLiteral(true));
			}

			if (curr.isFinal()) {
				typeUri.addProperty(JAVAO.isFinal,
						model.createTypedLiteral(true));
			}

			// if inner class then create relationship
			if (curr.containingClass() != null) {
				Resource containingClazz = createHttpResource(curr
						.containingClass().qualifiedName());
				typeUri.addProperty(JAVAO.innerClassOf, containingClazz);
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
				Resource superClazz = createHttpResource(curr.superclass()
						.qualifiedName());
				typeUri.addProperty(JAVAO.extends_, superClazz);
			}

			// handle implemented interfaces
			for (Type implementedInterface : curr.interfaceTypes()) {
				Resource interfaceResource = model
						.createResource(implementedInterface
								.qualifiedTypeName());
				typeUri.addProperty(JAVAO.implements_, interfaceResource);
			}

			// what can we do with annotations ?
			for (AnnotationDesc desc : curr.annotations()) {
				AnnotationTypeDoc a = desc.annotationType();
				Resource annotationTypeUri = createHttpResource(a
						.qualifiedTypeName());
				typeUri.addProperty(JAVAO.hasAnnotation, annotationTypeUri);
			}

			generateFieldsRDF(typeUri, curr);

			writeRdf(model);

		}

	}

	private void generateFieldsRDF(Resource typeUri, ClassDoc _class) {

		// handle fields
		for (FieldDoc field : _class.fields()) {

			// the field type
			Resource fieldTypeResource = createHttpResource(field.type()
					.qualifiedTypeName());

			// the line it is on .. used subsequently
			int line = field.position().line();

			Resource fieldResource = createHttpResource(field.qualifiedName()
					+ "#" + line);

			fieldResource.addProperty(RDF.type, JAVAO.Field);

			fieldResource.addProperty(JAVAO.type, fieldTypeResource);

			typeUri.addProperty(JAVAO.field, fieldResource);

			// add a line number reference
			fieldResource.addProperty(JAVAO.lineNumber,
					model.createTypedLiteral(line));

			// assign a label
			fieldResource.addProperty(RDFS.label, field.name());

			// is field an array, add dimensions ?
			int dimension = arrayDimension(field.type().dimension());

			// System.out.println(_class.name() + " : " + field.name() + " : " +
			// field.type().dimension() + " : " + dimension );

			if (dimension > 0) {
				fieldResource.addProperty(JAVAO.dimension,
						model.createTypedLiteral(dimension));
			}

			if (field.isStatic()) {
				fieldResource.addProperty(JAVAO.isStatic,
						model.createTypedLiteral(true));
			}

			if (field.isFinal()) {
				fieldResource.addProperty(JAVAO.isFinal,
						model.createTypedLiteral(true));
			}

			if (field.isTransient()) {
				fieldResource.addProperty(JAVAO.isTransient,
						model.createTypedLiteral(true));
			}

			if (field.isVolatile()) {
				fieldResource.addProperty(JAVAO.isVolatile,
						model.createTypedLiteral(true));
			}

			// what can we do with annotations ?
			for (AnnotationDesc desc : field.annotations()) {
				AnnotationTypeDoc a = desc.annotationType();
				Resource annotationTypeUri = createHttpResource(a
						.qualifiedTypeName());
				fieldResource.addProperty(JAVAO.hasAnnotation,
						annotationTypeUri);
			}

			Access fieldAccess = Access.createAccessModifier(field);
			fieldResource.addProperty(JAVAO.access, fieldAccess.getLabel());

			// generic type handling
			ParameterizedType type = field.type().asParameterizedType();

			if (type instanceof ParameterizedType) {
				fieldResource.addProperty(JAVAO.typeParameter,
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
			Resource packageUri = createHttpResource(p.name());

			classOrIntUri.addProperty(JAVAO.imports, packageUri);
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

			Resource methodUri = createHttpResource(method.qualifiedName()
					+ "#" + line);

			// moved typing and class relationship to here so we can handle
			// constructors as well
			if (method.isConstructor()) {
				methodUri.addProperty(RDF.type, JAVAO.Constructor);
				classOrIntUri.addProperty(JAVAO.constructor, methodUri);
			} else {
				methodUri.addProperty(RDF.type, JAVAO.Method);
				classOrIntUri.addProperty(JAVAO.method, methodUri);
			}

			// support for generic method types
			TypeVariable[] tv = method.typeParameters();

			for (TypeVariable t : tv) {
				methodUri.addProperty(JAVAO.typeVariable,
						model.createTypedLiteral(t.typeName()));
			}

			// add a line number reference
			methodUri.addProperty(JAVAO.lineNumber,
					model.createTypedLiteral(line));

			// add access modifier
			Access access = Access.createAccessModifier(method);
			methodUri.addProperty(JAVAO.access, access.getLabel());

			// is final
			if (method.isFinal()) {
				methodUri.addProperty(JAVAO.isFinal,
						model.createTypedLiteral(true));
			}

			// is it static?
			if (method.isStatic()) {
				methodUri.addProperty(JAVAO.isStatic,
						model.createTypedLiteral(true));
			}

			// is synchronised ?
			if (method.isSynchronized()) {
				methodUri.addProperty(JAVAO.isSynchronized,
						model.createTypedLiteral(true));
			}

			// is Native
			if (method.isNative()) {
				methodUri.addProperty(JAVAO.isNative,
						model.createTypedLiteral(true));
			}

			// throws any types ?
			for (Type t : method.thrownExceptionTypes()) {
				Resource thrownTypeUri = createHttpResource(t
						.qualifiedTypeName());

				methodUri.addProperty(JAVAO.throws_, thrownTypeUri);

			}

			// what can we do with annotations ?
			for (AnnotationDesc desc : method.annotations()) {
				AnnotationTypeDoc a = desc.annotationType();
				Resource annotationTypeUri = createHttpResource(a
						.qualifiedTypeName());
				methodUri.addProperty(JAVAO.hasAnnotation, annotationTypeUri);
			}

			// create a label for the method
			methodUri.addProperty(RDFS.label, method.name());

			// process the method parameters
			argumentsToRdf(method, methodUri);

			// things specific to a method
			if (method instanceof MethodDoc) {

				// is abstract ?
				if (((MethodDoc) method).isAbstract()) {
					methodUri.addProperty(JAVAO.isAbstract,
							model.createTypedLiteral(true));
				}

				// process the method return type
				returnTypesToRdf((MethodDoc) method, methodUri);

			}

		}

	}

	private void argumentsToRdf(ExecutableMemberDoc method, Resource methodUri) {
		/**
		 * The javadoc API is a bit confusing, calling arguments parameters and
		 * stuff so watch out.
		 */

		for (Parameter argument : method.parameters()) {

			Resource argumentTypeUri = createHttpResource(argument.type()
					.qualifiedTypeName());

			Resource node = model.createResource();

			// warning that type will be null if it is not a Parameterised Type
			ParameterizedType type = argument.type().asParameterizedType();
			if (type instanceof ParameterizedType) {

				node.addProperty(JAVAO.type, argumentTypeUri).addProperty(
						JAVAO.typeParameter, parameterizedTypesToRdf(type));

				methodUri.addProperty(JAVAO.argument, node);

			} else {
				// an ordinary type I hope

				node.addProperty(JAVAO.type, argumentTypeUri).addProperty(
						RDFS.label, argument.name());

				methodUri.addProperty(JAVAO.argument, node);
			}

			// if an array (not sure what this will mean to a parameterised type
			int dimension = arrayDimension(argument.type().dimension());

			if (dimension > 0) {
				node.addProperty(JAVAO.dimension,
						model.createTypedLiteral(dimension));
			}

		}
	}

	private void returnTypesToRdf(MethodDoc method, Resource methodUri) {

		Resource returnTypeUri = null;

		// if it is a parameterised type ...
		ParameterizedType type = method.returnType().asParameterizedType();

		Resource resource = model.createResource();

		if (type instanceof ParameterizedType) {
			// the generic type info needs to be appended here with addProperty

			returnTypeUri = createHttpResource(method.returnType()
					.qualifiedTypeName());

			log.debug("method parameterized return type: "
					+ returnTypeUri.toString());

			Resource parameterizedTypeForRdf = parameterizedTypesToRdf(type);

			resource.addProperty(JAVAO.type, returnTypeUri).addProperty(
					JAVAO.typeParameter, parameterizedTypeForRdf);

			methodUri.addProperty(JAVAO.returns, resource);

		} else {
			// simple non generic type

			// COD-353 remove < and > from to address occurrence of <any>
			// todo: we need to understand the generics better and improve the
			// whole process
			returnTypeUri = createHttpResource(method.returnType()
					.qualifiedTypeName());

			log.debug("method non parameterized return type: "
					+ returnTypeUri.toString());

			resource.addProperty(JAVAO.type, returnTypeUri);
		}

		// if it is an array add dimension property
		int dimension = arrayDimension(method.returnType().dimension());

		if (dimension > 0) {
			resource.addProperty(JAVAO.dimension,
					model.createTypedLiteral(dimension));
		}

		methodUri.addProperty(JAVAO.returns, resource);

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

				log.debug("parameterized type t.asTypeVariable(): "
						+ t.qualifiedTypeName());

				Resource typeArgument = model.createResource();
				typeArgument.addProperty(RDFS.label, t.typeName());
				typeArguments.addProperty(JAVAO.argument, typeArgument);

				// looks like typeArgument property assignment was overlooked
				typeArgument.addProperty(JAVAO.argument,
						createHttpResource(t.qualifiedTypeName()));

				// don't understand what bounds means at the moment so just
				// binding the name of the type variable eg T
				// TODO: looked like a duplicate property so I took out
				// typeArgument.addProperty(RDFS.label, t.simpleTypeName());

			} else if (t.asWildcardType() != null) {

				log.debug("parameterized type t.asWildcardType() "
						+ t.qualifiedTypeName());

				Resource typeArgument = model.createResource();
				typeArgument.addProperty(RDFS.label, t.typeName());
				typeArguments.addProperty(JAVAO.argument, typeArgument);

				for (Type _bound : t.asWildcardType().extendsBounds()) {
					// bound could be processed recursively
					typeArgument.addProperty(JAVAO.extends_,
							createHttpResource(_bound.qualifiedTypeName()));
				}

				for (Type _super : t.asWildcardType().superBounds()) {
					// _super can be processed recursively
					typeArgument.addProperty(JAVAO.super_,
							createHttpResource(_super.qualifiedTypeName()));
				}

			} else if (t.asParameterizedType() != null) {

				log.debug("parameterized type t.asParameterizedType() "
						+ t.qualifiedTypeName());
				// this is a recursive bit because types can also be
				// parameterised
				// ignore for now
				// t.asParameterizedType()
			} else {

				log.debug("parameterized type not recognised, defaulting "
						+ t.qualifiedTypeName());

				Resource typeArgument = model.createResource();
				typeArgument.addProperty(RDFS.label, t.typeName());
				typeArguments.addProperty(JAVAO.argument, typeArgument);

				typeArgument.addProperty(JAVAO.argument,
						createHttpResource(t.qualifiedTypeName()));
			}

		}

		return typeArguments;

	}

	private Resource createHttpResource(String name) {

		return model.createResource(baseUri
				+ name.replace(".", "/").replaceAll("<|>", ""));
	}

	private Resource createFileResource(String name) {

		return model.createResource(fileUri + name.replace(".", "/") + ".java");

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

	/*
	 * Convert string dimension like [][] into an int dimension
	 */
	private int arrayDimension(String stringArrayDimension) {
		int dimension = StringUtils.countMatches(stringArrayDimension, "[]");
		return dimension;
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
		model.setNsPrefix("sparqlycode", baseUri);
		this.baseUri = baseUri;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileUri() {
		return fileUri;
	}

	public void setFileUri(String fileUri) {
		this.fileUri = fileUri;
	}

}
