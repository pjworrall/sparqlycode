<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    xpath-default-namespace="http://maven.apache.org/POM/4.0.0"
    xmlns="http://maven.apache.org/POM/4.0.0"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd"
    version="2.0">
    <xsl:output method="xml" indent="yes"/>
    
        <xsl:template match="@*|node()">
            <xsl:copy copy-namespaces="no">
                <xsl:apply-templates />
            </xsl:copy>
        </xsl:template>
    
    <xsl:template match="/">
        <xsl:element name="project" namespace="http://maven.apache.org/POM/4.0.0">
         
         <xsl:element name="properties">
             <xsl:element name="sparqlycode.lib">/Users/pjworrall/Documents/sparqlycode/lib</xsl:element>
         </xsl:element>   
            
            
        <xsl:copy-of copy-namespaces="no" select="project/modelVersion"/>
        <xsl:copy-of copy-namespaces="no" select="/project/parent"/>
        <xsl:copy-of copy-namespaces="no" select="/project/artifactId"/>
        <xsl:copy-of copy-namespaces="no" select="/project/description"/>
            
        <xsl:copy-of copy-namespaces="no" select="project/dependencies"/>
            
            <xsl:element name="build" >
             <xsl:element name="plugins">
                 <xsl:element name="plugin">
                     <xsl:element name="groupId">org.apache.maven.plugins</xsl:element>
                     <xsl:element name="artifactId">maven-javadoc-plugin</xsl:element>
                     <xsl:element name="version">2.9.1</xsl:element>
                     <xsl:element name="configuration">
                         <xsl:element name="doclet">net.interition.sparqlycode.doclet.RdfDoclet</xsl:element>
                         <xsl:element name="docletPath">${sparqlycode.lib}/sparqlycode-maven-plugin.jar:${sparqlycode.lib}/jena-core-2.11.1.jar:${sparqlycode.lib}/jena-arq-2.11.1.jar:${sparqlycode.lib}/jena-iri-1.0.1.jar:${sparqlycode.lib}/slf4j-log4j12-1.6.4.jar:${sparqlycode.lib}/jcl-over-slf4j-1.6.4.jar:${sparqlycode.lib}/slf4j-api-1.6.4.jar:${sparqlycode.lib}/log4j-1.2.16.jar:${sparqlycode.lib}/xml-apis-1.4.01.jar:${sparqlycode.lib}/xercesImpl-2.11.0.jar</xsl:element>
                         <xsl:element name="javadocVersion">1.7</xsl:element>
                         <xsl:element name="useStandardDocletOptions">false</xsl:element>
                         <xsl:element name="detail">true</xsl:element>
                         <xsl:element name="failOnError">true</xsl:element>
                         <xsl:element name="debug">true</xsl:element>
                         <xsl:element name="includeDependencySources">false</xsl:element>
                         <xsl:element name="includeTransitiveDependencySources">false</xsl:element>
                         <xsl:element name="show">private</xsl:element>
                         <xsl:element name="additionalparam">-file sparqlycode</xsl:element>
                         <xsl:element name="subpackages">org.apache.activemq</xsl:element>
                     </xsl:element>
                 </xsl:element>
             </xsl:element>        
         </xsl:element>             
        </xsl:element>
    </xsl:template>
        
</xsl:stylesheet>