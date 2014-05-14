#!/bin/sh

if [ -z "$SAXON_HOME" ] ; then
   echo You need to set the SAXON_HONE environment variable to the home of the SaxonHE 9 distribution
   exit 1;
fi

if [ -z "$SPARQLYCODE_HOME" ] ; then
   echo You need to set the SPARQLYCODE_HOME environment variable to the home of the Sparqlycode distribution
   exit 1;
fi

if [ ! "$1" ] ; then
   echo You need to provide a list of java packages to process in the source code
   exit 1;
fi

export params="SPARQLYCODE_HOME=${SPARQLYCODE_HOME} SUBPACKAGE=$1"

echo Command used...
echo $JAVA_HOME/bin/java -jar $SAXON_HOME/saxon9he.jar -xsl:${SPARQLYCODE_HOME}/bin/pom4sparqly.xsl -s:pom.xml -o:scpom.xml $params 
$JAVA_HOME/bin/java -jar $SAXON_HOME/saxon9he.jar -xsl:${SPARQLYCODE_HOME}/bin/pom4sparqly.xsl -s:pom.xml -o:scpom.xml $params 
