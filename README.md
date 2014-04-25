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
	
	