#!/bin/sh

#set -x

# Paul Worrall, Interition Limited, 2014

# this script creates a pom just for producing sparqlycode 

if [ -z "$SAXON_HOME" ] ; then
   echo You need to set the SAXON_HONE environment variable to the home of the SaxonHE 9 distribution
   exit 1;
fi

if [ -z "$SPARQLYCODE_HOME" ] ; then
   echo You need to set the SPARQLYCODE_HOME environment variable to the home of the Sparqlycode distribution
   exit 1;
fi

USAGE="Usage: `basename $0` package BaseUri"

if [ ! "$1" ] ; then
   echo You need to provide a java root package  . Eg net.interition.sparqlycode
   echo $USAGE >&2
   exit 1;
fi

if [ ! "$2" ] ; then
   echo You need to provide a BaseUri. Eg. the source code repository uri
   echo $USAGE >&2
   exit 1;
fi

export params="SPARQLYCODE_HOME=${SPARQLYCODE_HOME} SUBPACKAGE=$1 BASEURI=$2"
echo "\n"

$JAVA_HOME/bin/java -jar $SAXON_HOME/saxon9he.jar -xsl:${SPARQLYCODE_HOME}/bin/pom4sparqly.xsl -s:pom.xml -o:scpom.xml $params 
