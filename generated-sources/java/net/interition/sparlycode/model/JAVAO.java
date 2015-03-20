/* CVS $Id: $ */
package net.interition.sparlycode.model; 
import com.hp.hpl.jena.rdf.model.*;
 
/**
 * Vocabulary definitions from /Users/pjworrall/Documents/Java2RDF/sparqlycode/sparqlycode-maven-plugin/src/main/resources/JAVAO.ttl 
 * @author Auto-generated by schemagen on 12 Mar 2015 16:51 
 */
public class JAVAO {
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static Model m_model = ModelFactory.createDefaultModel();
    
    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://ontology.interition.net/java/ref/";
    
    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );
    
    public static final Property access = m_model.createProperty( "http://ontology.interition.net/java/ref/access" );
    
    public static final Property argument = m_model.createProperty( "http://ontology.interition.net/java/ref/argument" );
    
    public static final Property constructor = m_model.createProperty( "http://ontology.interition.net/java/ref/constructor" );
    
    public static final Property dimension = m_model.createProperty( "http://ontology.interition.net/java/ref/dimension" );
    
    public static final Property extends_ = m_model.createProperty( "http://ontology.interition.net/java/ref/extends" );
    
    public static final Property field = m_model.createProperty( "http://ontology.interition.net/java/ref/field" );
    
    public static final Property hasAnnotation = m_model.createProperty( "http://ontology.interition.net/java/ref/hasAnnotation" );
    
    public static final Property implements_ = m_model.createProperty( "http://ontology.interition.net/java/ref/implements" );
    
    public static final Property imports = m_model.createProperty( "http://ontology.interition.net/java/ref/imports" );
    
    public static final Property innerClassOf = m_model.createProperty( "http://ontology.interition.net/java/ref/innerClassOf" );
    
    public static final Property isAbstract = m_model.createProperty( "http://ontology.interition.net/java/ref/isAbstract" );
    
    public static final Property isFinal = m_model.createProperty( "http://ontology.interition.net/java/ref/isFinal" );
    
    public static final Property isNative = m_model.createProperty( "http://ontology.interition.net/java/ref/isNative" );
    
    public static final Property isSerializable = m_model.createProperty( "http://ontology.interition.net/java/ref/isSerializable" );
    
    public static final Property isStatic = m_model.createProperty( "http://ontology.interition.net/java/ref/isStatic" );
    
    public static final Property isSynchronized = m_model.createProperty( "http://ontology.interition.net/java/ref/isSynchronized" );
    
    public static final Property isTransient = m_model.createProperty( "http://ontology.interition.net/java/ref/isTransient" );
    
    public static final Property isVolatile = m_model.createProperty( "http://ontology.interition.net/java/ref/isVolatile" );
    
    public static final Property lineNumber = m_model.createProperty( "http://ontology.interition.net/java/ref/lineNumber" );
    
    public static final Property method = m_model.createProperty( "http://ontology.interition.net/java/ref/method" );
    
    public static final Property package_ = m_model.createProperty( "http://ontology.interition.net/java/ref/package" );
    
    public static final Property returnTypeParameter = m_model.createProperty( "http://ontology.interition.net/java/ref/returnTypeParameter" );
    
    public static final Property returns = m_model.createProperty( "http://ontology.interition.net/java/ref/returns" );
    
    public static final Property super_ = m_model.createProperty( "http://ontology.interition.net/java/ref/super" );
    
    public static final Property throws_ = m_model.createProperty( "http://ontology.interition.net/java/ref/throws" );
    
    public static final Property type = m_model.createProperty( "http://ontology.interition.net/java/ref/type" );
    
    public static final Property typeParameter = m_model.createProperty( "http://ontology.interition.net/java/ref/typeParameter" );
    
    public static final Property typeVariable = m_model.createProperty( "http://ontology.interition.net/java/ref/typeVariable" );
    
    public static final Resource Class = m_model.createResource( "http://ontology.interition.net/java/ref/Class" );
    
    public static final Resource Constructor = m_model.createResource( "http://ontology.interition.net/java/ref/Constructor" );
    
    public static final Resource Enum = m_model.createResource( "http://ontology.interition.net/java/ref/Enum" );
    
    public static final Resource Field = m_model.createResource( "http://ontology.interition.net/java/ref/Field" );
    
    public static final Resource Interface = m_model.createResource( "http://ontology.interition.net/java/ref/Interface" );
    
    public static final Resource Method = m_model.createResource( "http://ontology.interition.net/java/ref/Method" );
    
}
