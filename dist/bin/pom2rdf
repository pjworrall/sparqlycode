#!/bin/sh

if [ -z "$SAXON_HOME" ] ; then
   echo You need to set the SAXON_HOME environment variable to the home of the SaxonHE 9 distribution
   exit 1;
fi

if [ -z "$SPARQLYCODE_HOME" ] ; then
   echo You need to set the SPARQLYCODE_HOME environment variable to the home of the Sparqlycode distribution
   exit 1;
fi

if [ ! "$1" ] ; then
   echo You need to provide a Base URI. We propose the best practice of making this the svn or git source code repository URL to the pom.xml.
   exit 1;
fi
export params="SPARQLYCODE_HOME=${SPARQLYCODE_HOME}  BaseURI=$1"

echo Command used...
echo $JAVA_HOME/bin/java -jar $SAXON_HOME/saxon9he.jar -xsl:${SPARQLYCODE_HOME}/bin/pom2rdf.xsl -s:pom.xml -o:pomrdf.ttl $params 
$JAVA_HOME/bin/java -jar $SAXON_HOME/saxon9he.jar -xsl:${SPARQLYCODE_HOME}/bin/pom2rdf.xsl -s:pom.xml -o:pomrdf.ttl $params 
