<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xpath-default-namespace="http://maven.apache.org/POM/4.0.0"
    version="2.0">
    
    <xsl:param name="BaseURI"
        select="'http://www.interition.net/sources/anonymous/'"/>
    
    <xsl:param name="MvnURI"
        select="'http://www.interition.net/sparqlycode/maven/vocabulary/'"/>
    
    <xsl:param name="IscURI"
        select="'http://www.interition.net/sparqlycode/vocabulary/'"/>
    
    <xsl:param name="RdfURI"
        select="'http://www.interition.net/sparqlycode/vocabulary/'"/>
    
    <xsl:strip-space elements="*"/>
    <xsl:output method="text"/>
    
    <xsl:template match="/project">
        <!-- Header declerations for Turtle -->
        @prefix rdf: <xsl:text>&lt;</xsl:text><xsl:value-of select="$RdfURI"/><xsl:text>&gt;</xsl:text> .
        @prefix mvn: <xsl:text>&lt;</xsl:text><xsl:value-of select="$MvnURI"/><xsl:text>&gt;</xsl:text> .
        @prefix isc: <xsl:text>&lt;</xsl:text><xsl:value-of select="$IscURI"/><xsl:text>&gt;</xsl:text> .
        @prefix : <xsl:text>&lt;</xsl:text><xsl:value-of select="$BaseURI"/><xsl:text>&gt;</xsl:text> .
        <!-- create the root project uri that will be passed to all templates -->
        <xsl:variable name="projectUri">
            <xsl:value-of select="concat(':',name())"/>
        </xsl:variable>       
        <xsl:value-of select="$projectUri"/> rdf:type isc:MavenProject .
        
        <xsl:value-of select="$projectUri"/> mvn:name "<xsl:value-of select="name"/>" .
        <xsl:value-of select="$projectUri"/> mvn:description "<xsl:value-of select="description"/>" .
        <xsl:value-of select="$projectUri"/> mvn:packaging "<xsl:value-of select="packaging"/>" .
        
        <xsl:value-of select="$projectUri"/> mvn:groupId "<xsl:value-of select="groupId"/>" .
        <xsl:value-of select="$projectUri"/> mvn:artifactId "<xsl:value-of select="artifactId"/>" .
        <xsl:value-of select="$projectUri"/> mvn:version "<xsl:value-of select="versio "/>" .

        <xsl:apply-templates select="parent">
            <xsl:with-param name="projectUri" select="$projectUri" />
        </xsl:apply-templates>
        <xsl:apply-templates select="dependencies/dependency">
            <xsl:with-param name="projectUri" select="$projectUri" />
        </xsl:apply-templates>
        
    </xsl:template>
    
    <xsl:template match="parent">
        <xsl:param name="projectUri"/>
        
        <xsl:variable name="parentUri">
            <xsl:value-of select="concat(':',name())"/>
        </xsl:variable>
        
        <xsl:value-of select="$projectUri"/> mvn:parent <xsl:value-of select="$parentUri"/>     .   
        <xsl:apply-templates select="*">
            <xsl:with-param name="subjectUri" select="$parentUri" />
        </xsl:apply-templates>
    </xsl:template>
    
    <xsl:template match="dependency"> 
        <xsl:param name="projectUri"/>
        
        <xsl:variable name="number">
            <xsl:number/>
        </xsl:variable>     
        <xsl:variable name="dependencyUri">
            <xsl:value-of select="concat(':',name(),'_',$number)"/>
        </xsl:variable>
        
        <xsl:value-of select="$projectUri"/> mvn:dependency <xsl:value-of select="$dependencyUri"/>     .  
        
        <xsl:apply-templates select="*">
            <xsl:with-param name="subjectUri" select="$dependencyUri" />
        </xsl:apply-templates>
    </xsl:template>
    
    <xsl:template match="*">
        <xsl:param name="subjectUri"/>
        <xsl:value-of select="$subjectUri"/> mvn:<xsl:value-of select="name()"/> "<xsl:value-of select="."/>" .     
    </xsl:template>

    <!-- these tidy output -->
    <xsl:template match="*/text()[normalize-space()]">
        <xsl:value-of select="normalize-space()"/>
    </xsl:template>
    
    <xsl:template match="*/text()[not(normalize-space())]" />
    
    
</xsl:stylesheet>