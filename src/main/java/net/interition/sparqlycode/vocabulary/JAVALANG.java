package net.interition.sparqlycode.vocabulary;

import com.hp.hpl.jena.rdf.model.*;

public class JAVALANG {

	protected static final String uri = "http://www.interition.net/java/ref/";

	/**
	 * returns the URI for this schema
	 * 
	 * @return the URI for this schema
	 */
	public static String getURI() {
		return uri;
	}

	protected static final Resource resource(String local) {
		return ResourceFactory.createResource(uri + local);
	}

	protected static final Property property(String local) {
		return ResourceFactory.createProperty(uri, local);
	}

	public static final Resource Class = resource("Class");
	public static final Resource Interface = resource("Interface");
	public static final Resource Enum = resource("Enum");
	public static final Resource _Method = resource("Method");
	public static final Resource _Field = resource("Field");
	
	public static final Property name = property("name");
	public static final Property _package = property("package");
	public static final Property constructor = property("constructor");
	public static final Property method = property("method");
	public static final Property argument = property("argument");
	public static final Property type = property("type");
	public static final Property typeParameter = property("typeParameter");
	public static final Property _super = property("super");
	public static final Property _extends = property("extends");
	public static final Property _implements = property("implements");
	public static final Property returns = property("returns");
	public static final Property _throws = property("throws");
	public static final Property innerClassOf = property("innerClassOf");
	public static final Property field = property("field");
	public static final Property isAbsract = property("isAbstract");
	public static final Property isStatic = property("isStatic");
	public static final Property isFinal = property("isFinal");
	public static final Property isTransient = property("isTransient");
	public static final Property isVolatile = property("isVolatile");
	public static final Property isSerializable = property("isSerializable");
	public static final Property isNative = property("isNative");
	public static final Property lineNumber = property("lineNumber");
	public static final Property javaImport = property("imports");
	public static final Property access = property("access");
	public static final Property isSynchronized = property("isSynchronized");
	public static final Property hasAnnotation = property("hasAnnotation");
	public static final Property typeVariable = property("typeVariable");
	public static final Property returnTypeParameter = property("returnTypeParameter");

}
