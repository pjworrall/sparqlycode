SPARQLYCODE (C) 2014- Paul Worrall, Interition Ltd, All Rights Reserved

SPARQLYCODE enables querying and linking of software artifacts, code, dependencies, build, configuration and source code management data using SPARQL.

It has the following dependencies:

Apache Jena CORE 2.11.1	(jena-core-2.11.1.jar)
Apache Jena ARQ 2.11.1	(jena-arq-2.11.1.jar)
Apache Jena IRI 1.0.1	(jena-iri-1.0.1.jar)
Apache SLF4J 1.6.4		(slf4j-log4j12-1.6.4.jar)
Apache JCL over slf4j	(jcl-over-slf4j-1.6.4.jar)
Apache SLF4J API		(slf4j-api-1.6.4.jar)
Apache Log4J			(log4j-1.2.16.jar)
Apache XML APIs			(xml-apis-1.4.01.jar)
Apache Xerces			(Impl-2.11.0.jar)

Put the above dependency jar libraries and the  Sparqlycode binary into a folder like $HOME/sparqlycode/lib . Remember this location because you need
to use it in your Maven configuration for your project.

To generate a Sparqlycode RDF Knowledge Base for a Maven based project add the following to the POM.XML. Tailor as necessary :

<!-- Add a property to point to the location of the Sparqlycode binary and it's dependencies -->
	<properties>
		<sparqlycode.lib>/Users/pjworrall/Documents/sparqlycode/lib</sparqlycode.lib>
	</properties>

<build>
		<plugins>
		        <!--- Declare a Javadoc plugin - note that his might upset any Javadoc plugin config you already have. Sorry. --->
		        <!-- see the Jvdoc plugin documentation to learn what the configuration means -->
				<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-javadoc-plugin</artifactId>
				<version>2.9.1</version>
				<configuration>
					<doclet>net.interition.sparqlycode.doclet.RdfDoclet</doclet>
					<docletPath>${sparqlycode.lib}/sparqlycode-maven-plugin.jar:${sparqlycode.lib}/jena-core-2.11.1.jar:${sparqlycode.lib}/jena-arq-2.11.1.jar:${sparqlycode.lib}/jena-iri-1.0.1.jar:${sparqlycode.lib}/slf4j-log4j12-1.6.4.jar:${sparqlycode.lib}/jcl-over-slf4j-1.6.4.jar:${sparqlycode.lib}/slf4j-api-1.6.4.jar:${sparqlycode.lib}/log4j-1.2.16.jar:${sparqlycode.lib}/xml-apis-1.4.01.jar:${sparqlycode.lib}/xercesImpl-2.11.0.jar</docletPath>
					<!-- this does not work for some reason... -->
					<!-- <docletPath>${sparqlycode.lib}/*</docletPath> -->
					<javadocVersion>1.7</javadocVersion>
					<useStandardDocletOptions>false</useStandardDocletOptions>
					<detail>true</detail>
					<failOnError>true</failOnError>
					<debug>true</debug>
					<includeDependencySources>false</includeDependencySources>
					<includeTransitiveDependencySources>false</includeTransitiveDependencySources>
					<show>private</show>
					<!-- Set the nme of the file you want the Sparclycode RDF to be written to -->
					<additionalparam>-file sparqlcode</additionalparam>
					<!-- Specify the packages in your code you want to be processed -->
					<subpackages>net.interition.test</subpackages>
				</configuration>
			</plugin>
		</plugins>
	</build>
	
	Usage FAQ
	
	Always build a source true with mvn install to check validity of build (skip tests - see below)
	
	To generate just the sparqlycode use javadoc:javadoc
	
	Best practice is to use a duplicate of the pom.xml named scpom.xml and use mvn -f to refer to it.
	
	Enable mvn debug option -X
	
	Disable tests with -Dmaven.test.skip=true
	
	Example command: mvn -X -Dmaven.test.skip=true -f scpom.xml javadoc:javadoc
	
	scpom.xml can have all configuration removed other than the dependencies for the project and the above javadoc plugin decleration.
	
	run jena riot on the resulting sparqlycode.n3 out file to check it is a valid format
	
	
	