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
	
	public static final Property Name = property("name");
	public static final Property Package = property("package");
	public static final Property Constructor = property("constructor");
	public static final Property Method = property("method");
	public static final Property Parameter = property("parameter");
	public static final Property ParameterType = property("parameterType");
	public static final Property ParameterBound = property("parameterBound");
	public static final Property Extends = property("extends");
	public static final Property Returns = property("returns");
	public static final Property Field = property("field");
	public static final Property IsAbsract = property("isAbstract");
	public static final Property LineNumber = property("lineNumber");
	public static final Property Import = property("imports");

}
