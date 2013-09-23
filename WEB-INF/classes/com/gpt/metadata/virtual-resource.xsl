<?xml version="1.0" encoding="UTF-8"?>

<!--
    Document   : virtual-resource.xsl
    Created on : November 13, 2012, 2:35 PM
    Author     : ngouw
    Description:
        Purpose of transformation follows.
-->

<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" 
    xmlns:dc="http://purl.org/dc/elements/1.1/" 
    xmlns:dcmiBox="http://dublincore.org/documents/2000/07/11/dcmi-box/" 
    xmlns:dct="http://purl.org/dc/terms/" 
    xmlns:ows="http://www.opengis.net/ows"
    version="1.0">
    <xsl:output method="html"/>
    <xsl:template match="/">
        <html>
            <head>
                <title>
                    <xsl:value-of select="rdf:RDF/rdf:Description/dc:title"/>
                </title>
                <link href="../xsl/metadata.css" rel="stylesheet" type="text/css"/>
            </head>
            <body>
                <h2 class="toolbarTitle"><xsl:value-of select="rdf:RDF/rdf:Description/dc:title"/></h2>
                <hr/>
                <p>
                    <xsl:text>This is a temporary placeholder record. This record will be replaced by a full metadata record when one becomes available.</xsl:text>
                </p>
               
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>
