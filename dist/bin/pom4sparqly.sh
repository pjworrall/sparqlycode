#!/bin/sh

# Copyright 2014, Paul Worrall , Interition Ltd.

# The BaseUri option has been disabled while waiting on a more strategic solution
# It will default to a common Base Uri declared in the XSLT
#  - http://www.interition.net:3030/browse/COD-96

#set -x

# this script creates a pom just for producing sparqlycode 

if [ -z "$SAXON_HOME" ] ; then
   echo You need to set the SAXON_HONE environment variable to the home of the SaxonHE 9 distribution
   exit 1;
fi

if [ -z "$SPARQLYCODE_HOME" ] ; then
   echo You need to set the SPARQLYCODE_HOME environment variable to the home of the Sparqlycode distribution
   exit 1;
fi

#USAGE="Usage: `basename $0` package BaseUri"
USAGE="Usage: `basename $0` package [java_version]"

if [ ! "$1" ] ; then
   echo You need to provide a java root package  . Eg net.interition.sparqlycode
   echo $USAGE >&2
   exit 1;
fi

JAVA_VERSION=$2

if [ "$JAVA_VERSION" = "" ] ; then
   echo "No Java version provided, defaulting to 1.7"
   JAVA_VERSION=1.7
fi

# We are not providing baseuri
#if [ ! "$3" ] ; then
#   echo You need to provide a BaseUri. Eg. the source code repository uri
#   echo $USAGE >&2
#   exit 1;
#fi

#export params="SPARQLYCODE_HOME=${SPARQLYCODE_HOME} SUBPACKAGE=$1 JAVADOC_VERSION=$JAVA_VERSION BASEURI=$3"
export params="SPARQLYCODE_HOME=${SPARQLYCODE_HOME} SUBPACKAGE=$1 JAVADOC_VERSION=$JAVA_VERSION"
echo "\n"

$JAVA_HOME/bin/java -jar $SAXON_HOME/saxon9he.jar -xsl:${SPARQLYCODE_HOME}/bin/pom4sparqly.xsl -s:pom.xml -o:scpom.xml $params 
