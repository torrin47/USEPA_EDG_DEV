<?xml version="1.0" encoding="UTF-8"?>

<!--
    Document   : data-gov.xsl
    Created on : September 21, 2010, 11:50 AM
    Author     : johnsievel
    Description:
        Purpose of transformation follows.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html"/>

    <xsl:template match="/">
        <html>
            <head>
                <title><xsl:value-of select="/dataGov/basic/title"/></title>
                <link href="../xsl/metadata.css" rel="stylesheet" type="text/css"/>
            </head>
            <body>
                <h1 class="toolbarTitle">
                    <xsl:value-of select="/dataGov/basic/title"/>
                </h1>
                <hr/>
                <h3 class="headTitle">
                  <xsl:text>Data.Gov Metadata</xsl:text>
                </h3>
                <dl>
                    <dt>
                        <em><xsl:text>Basic Information:</xsl:text></em>
                        <xsl:apply-templates select="/dataGov/basic"/>
                    </dt>
                    <dt>
                        <em><xsl:text>Downloadable File Information:</xsl:text></em>
                        <xsl:apply-templates select="/dataGov/downloadableFile"/>
                    </dt>
                    <xsl:if test="/dataGov/statisticalMethodology1">
                        <xsl:apply-templates select="/dataGov/statisticalMethodology1"/>
                    </xsl:if>
                    <xsl:if test="/dataGov/statisticalMethodology2">
                        <xsl:apply-templates select="/dataGov/statisticalMethodology2"/>
                    </xsl:if>
                    <xsl:if test="/dataGov/statisticalMethodology3">
                        <xsl:apply-templates select="/dataGov/statisticalMethodology3"/>
                    </xsl:if>
                    <xsl:if test="/dataGov/ogd/ogdSubmission='Yes'">
                        <xsl:apply-templates select="/dataGov/ogd"/>
                    </xsl:if>
                    <dt>
                        <em><xsl:text>Downloadable File Information:</xsl:text></em>
                        <xsl:apply-templates select="/dataGov/downloadableFile"/>
                    </dt>
                    <xsl:apply-templates select="/dataGov/ogd"/>
                    <xsl:apply-templates select="/dataGov/gdg"/>
                </dl>

           </body>
        </html>
    </xsl:template>

    <xsl:template match="/dataGov/basic">
        <dl>
        <dt><em><xsl:text>Unique ID: </xsl:text></em><xsl:value-of select="uniqueId"/></dt>
        <dt><em><xsl:text>User Generated ID: </xsl:text></em><xsl:value-of select="userGeneratedId"/></dt>
        <dt><em><xsl:text>Dataset Group Name: </xsl:text></em><xsl:value-of select="datasetGroupName"/></dt>
        <dt><em><xsl:text>Description: </xsl:text></em><xsl:value-of select="description"/></dt>
        <dt><em><xsl:text>Agency Name: </xsl:text></em><xsl:value-of select="agencyName"/></dt>
        <dt><em><xsl:text>Agency Short Name: </xsl:text></em><xsl:value-of select="agencyShortName"/></dt>
        <dt><em><xsl:text>Sub Agency Name: </xsl:text></em><xsl:value-of select="subAgencyName"/></dt>
        <dt><em><xsl:text>Sub Agency Short Name: </xsl:text></em><xsl:value-of select="subAgencyShortName"/></dt>
        <dt><em><xsl:text>Contact Name: </xsl:text></em><xsl:value-of select="contactName"/></dt>
        <dt><em><xsl:text>Contact Phone Number: </xsl:text></em><xsl:value-of select="contactPhoneNumber"/></dt>
        <dt><em><xsl:text>Contact Email Address: </xsl:text></em><xsl:value-of select="contactEmailAddress"/></dt>
        <dt><em><xsl:text>Agency Responsible For Quality: </xsl:text></em><xsl:value-of select="agencyResponsibleForQuality"/></dt>
        <dt><em><xsl:text>Compliance With Agency's Quality Guidelines: </xsl:text></em><xsl:value-of select="complianceWithAgencysQualityGuidelines"/></dt>
        <dt><em><xsl:text>Privacy And Confidential: </xsl:text></em><xsl:value-of select="privacyAndConfidential"/></dt>
        <dt><em><xsl:text>Data.Gov CatalogType: </xsl:text></em><xsl:value-of select="dataGovCatalogType"/></dt>
        <dt><em><xsl:text>Subject Area: </xsl:text></em><xsl:apply-templates select="subjectArea"/></dt>
        <dt><em><xsl:text>Specialized Data Category Designation: </xsl:text></em><xsl:value-of select="specializedDataCategoryDesignation"/></dt>
        <dt><em><xsl:text>Keywords: </xsl:text></em><xsl:value-of select="keywords"/></dt>
        <dt><em><xsl:text>Date Released: </xsl:text></em><xsl:value-of select="dateReleased"/></dt>
        <dt><em><xsl:text>Date Updated: </xsl:text></em><xsl:value-of select="dateUpdated"/></dt>
        <dt><em><xsl:text>Agency Program Url: </xsl:text></em><xsl:apply-templates select="agencyProgramUrl"/></dt>
        <dt><em><xsl:text>Agency Data Series Url: </xsl:text></em><xsl:apply-templates select="agencyDataSeriesUrl"/></dt>
        <xsl:apply-templates select="collectionMode"/>
        <dt><em><xsl:text>Frequency: </xsl:text></em><xsl:value-of select="frequency"/></dt>
        <xsl:apply-templates select="periodOfCoverage"/>
        <dt><em><xsl:text>Unit Of Analysis: </xsl:text></em><xsl:value-of select="unitOfAnalysis"/></dt>
        <dt><em><xsl:text>Geographic Scope: </xsl:text></em><xsl:value-of select="geographicScope"/></dt>
        <dt><em><xsl:text>Geographic Granularity: </xsl:text></em><xsl:value-of select="geographicGranularity"/></dt>
        <dt><em><xsl:text>Reference For Technical Documentation: </xsl:text></em><xsl:apply-templates select="referenceForTechnicalDocumentation"/></dt>
        <dt><em><xsl:text>Data Dictionary Variable List: </xsl:text></em><xsl:apply-templates select="dataDictionaryVariableList"/></dt>
        <xsl:apply-templates select="dataCollectionInstrument"/>
        <dt><em><xsl:text>Bibliographic Citation For Dataset: </xsl:text></em><xsl:apply-templates select="bibliographicCitationForDataset"/></dt>
        <dt><em><xsl:text>Number Datasets Represented By This Submission: </xsl:text></em><xsl:value-of select="numberDatasetsRepresentedByThisSubmission"/></dt>
        <dt><em><xsl:text>Additional Metadata: </xsl:text></em><xsl:value-of select="additionalMetadata"/></dt>
        <dt><em><xsl:text>Dataset Use Requires License: </xsl:text></em><xsl:value-of select="datasetUseRequiresLicense"/></dt>
        <xsl:if test="datasetUseRequiresLicense='Yes'">
            <dt><em><xsl:text>Dataset License Agreement Url: </xsl:text></em><xsl:apply-templates select="datasetLicenseAgreementUrl"/></dt>
        </xsl:if>
        </dl>
    </xsl:template>

    <xsl:template match="collectionMode">
        <dt><em><xsl:text>Collection Mode: </xsl:text></em><xsl:value-of select="."/></dt>
    </xsl:template>

    <xsl:template match="periodOfCoverage">
        <dt><em><xsl:text>Period Of Coverage: </xsl:text></em><xsl:value-of select="."/></dt>
    </xsl:template>

    <xsl:template match="dataCollectionInstrument">
        <dt><em><xsl:text>Data Collection Instrument: </xsl:text></em>
        <a>
            <xsl:attribute name="href">
                <xsl:value-of select="."/>
            </xsl:attribute>
            <xsl:value-of select="."/>
        </a>
        </dt>
    </xsl:template>

    <xsl:template match="*">
        <a>
            <xsl:attribute name="href">
                <xsl:value-of select="."/>
            </xsl:attribute>
            <xsl:value-of select="."/>
        </a>
    </xsl:template>

    <xsl:template match="subjectArea">
        <xsl:choose>
            <xsl:when test=".='1'"><xsl:text>Population</xsl:text></xsl:when>
            <xsl:when test=".='2'"><xsl:text>Births, Deaths, Marriages, and Divorces</xsl:text></xsl:when>
            <xsl:when test=".='3'"><xsl:text>Health and Nutrition</xsl:text></xsl:when>
            <xsl:when test=".='4'"><xsl:text>Education</xsl:text></xsl:when>
            <xsl:when test=".='5'"><xsl:text>Law Enforcement, Courts, and Prisons</xsl:text></xsl:when>
            <xsl:when test=".='6'"><xsl:text>Geography and Environment</xsl:text></xsl:when>
            <xsl:when test=".='7'"><xsl:text>Elections</xsl:text></xsl:when>
            <xsl:when test=".='8'"><xsl:text>State and Local Government Finances and Employment</xsl:text></xsl:when>
            <xsl:when test=".='9'"><xsl:text>Federal Government Finances and Employment</xsl:text></xsl:when>
            <xsl:when test=".='10'"><xsl:text>National Security and Veterans Affairs</xsl:text></xsl:when>
            <xsl:when test=".='11'"><xsl:text>Social Insurance and Human Services</xsl:text></xsl:when>
            <xsl:when test=".='12'"><xsl:text>Labor Force, Employment, and Earnings</xsl:text></xsl:when>
            <xsl:when test=".='13'"><xsl:text>Income, Expenditures, Poverty, and Wealth</xsl:text></xsl:when>
            <xsl:when test=".='14'"><xsl:text>Prices</xsl:text></xsl:when>
            <xsl:when test=".='15'"><xsl:text>Business Enterprise</xsl:text></xsl:when>
            <xsl:when test=".='16'"><xsl:text>Science and Technology</xsl:text></xsl:when>
            <xsl:when test=".='17'"><xsl:text>Agriculture</xsl:text></xsl:when>
            <xsl:when test=".='18'"><xsl:text>Natural Resources</xsl:text></xsl:when>
            <xsl:when test=".='19'"><xsl:text>Energy and Utilities</xsl:text></xsl:when>
            <xsl:when test=".='20'"><xsl:text>Construction and Housing</xsl:text></xsl:when>
            <xsl:when test=".='21'"><xsl:text>Manufactures</xsl:text></xsl:when>
            <xsl:when test=".='22'"><xsl:text>Wholesale and Retail Trade</xsl:text></xsl:when>
            <xsl:when test=".='23'"><xsl:text>Transportation</xsl:text></xsl:when>
            <xsl:when test=".='24'"><xsl:text>Information and Communications</xsl:text></xsl:when>
            <xsl:when test=".='25'"><xsl:text>Banking, Finance, and Insurance</xsl:text></xsl:when>
            <xsl:when test=".='26'"><xsl:text>Arts, Recreation, and Travel</xsl:text></xsl:when>
            <xsl:when test=".='27'"><xsl:text>Accommodation, Food Services, and Other Services</xsl:text></xsl:when>
            <xsl:when test=".='28'"><xsl:text>Foreign Commerce and Aid</xsl:text></xsl:when>
            <xsl:when test=".='29'"><xsl:text>Puerto Rico and the Island Areas</xsl:text></xsl:when>
            <xsl:when test=".='30'"><xsl:text>International Statistics</xsl:text></xsl:when>
            <xsl:when test=".='31'"><xsl:text>Other</xsl:text></xsl:when>
            <xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="/dataGov/downloadableFile">
        <dl>
        <dt><em><xsl:text>Access Point: </xsl:text></em><xsl:apply-templates select="accessPoint"/></dt>
        <dt><em><xsl:text>Media Format: </xsl:text></em><xsl:value-of select="mediaFormat"/></dt>
        <dt><em><xsl:text>File Size: </xsl:text></em><xsl:value-of select="fileSize"/></dt>
        <dt><em><xsl:text>File Format: </xsl:text></em><xsl:value-of select="fileFormat"/></dt>
        </dl>
    </xsl:template>

   <xsl:template match="/dataGov/statisticalMethodology1">
      <dt>
        <em><xsl:text>Statistical Methodology 1:</xsl:text></em>
      </dt>
      <xsl:call-template name="statisticalMethodology"></xsl:call-template>
   </xsl:template>

   <xsl:template match="/dataGov/statisticalMethodology2">
      <dt>
        <em><xsl:text>Statistical Methodology 2:</xsl:text></em>
      </dt>
      <xsl:call-template name="statisticalMethodology"></xsl:call-template>
   </xsl:template>

   <xsl:template match="/dataGov/statisticalMethodology3">
      <dt>
        <em><xsl:text>Statistical Methodology 3:</xsl:text></em>
      </dt>
      <xsl:call-template name="statisticalMethodology"></xsl:call-template>
   </xsl:template>

   <xsl:template name="statisticalMethodology">
        <dl>
            <dt><em><xsl:text>Sampling: </xsl:text></em><xsl:apply-templates select="sampling"/></dt>
            <dt><em><xsl:text>Estimation: </xsl:text></em><xsl:apply-templates select="estimation"/></dt>
            <dt><em><xsl:text>Weighting: </xsl:text></em><xsl:apply-templates select="weighting"/></dt>
            <dt><em><xsl:text>Disclosure Avoidance: </xsl:text></em><xsl:apply-templates select="disclosureAvoidance"/></dt>
            <dt><em><xsl:text>Questionnaire Design: </xsl:text></em><xsl:apply-templates select="questionnaireDesign"/></dt>
            <dt><em><xsl:text>Series Breaks: </xsl:text></em><xsl:apply-templates select="seriesBreaks"/></dt>
            <dt><em><xsl:text>Non Response Adjustment: </xsl:text></em><xsl:apply-templates select="nonResponseAdjustment"/></dt>
            <dt><em><xsl:text>Seasonal Adjustment: </xsl:text></em><xsl:apply-templates select="seasonalAdjustment"/></dt>
            <dt><em><xsl:text>Data Quality: </xsl:text></em><xsl:apply-templates select="dataQuality"/></dt>
        </dl>
   </xsl:template>

   <xsl:template match="/dataGov/ogd">
      <dt>
        <em><xsl:text>OGD Submission:</xsl:text></em>
        <dl>
            <dt><em><xsl:text>Listed In Open.Gov: </xsl:text></em><xsl:value-of select="listedInOpenGov"/></dt>
            <dt><em><xsl:text>High Value: </xsl:text></em><xsl:value-of select="highValue"/></dt>
            <dt><em><xsl:text>Why High Value: </xsl:text></em><xsl:apply-templates select="whyHighValue"/></dt>
            <dt><em><xsl:text>How Is This New: </xsl:text></em><xsl:value-of select="howNew"/></dt>
        </dl>
      </dt>
   </xsl:template>

   <xsl:template match="whyHighValue">
        <xsl:choose>
            <xsl:when test=".='1'"><xsl:text>Increases agency accountability and responsiveness</xsl:text></xsl:when>
            <xsl:when test=".='2'"><xsl:text>Improves public knowledge of the agency and its operations</xsl:text></xsl:when>
            <xsl:when test=".='3'"><xsl:text>Furthers the core mission of the agency</xsl:text></xsl:when>
            <xsl:when test=".='4'"><xsl:text>Creates or expands economic opportunity</xsl:text></xsl:when>
            <xsl:when test=".='5'"><xsl:text>Responds to need and demand as identified through public consultation</xsl:text></xsl:when>
            <xsl:when test=".='6'"><xsl:text>Other</xsl:text></xsl:when>
            <xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
        </xsl:choose>
   </xsl:template>

   <xsl:template match="/dataGov/gdg">
      <dt>
        <em><xsl:text>GeoDataGateway Information:</xsl:text></em>
        <dl>
            <dt><em><xsl:text>Id: </xsl:text></em><xsl:value-of select="id"/></dt>
            <dt><em><xsl:text>Content Type: </xsl:text></em><xsl:value-of select="contentType"/></dt>
        </dl>
      </dt>


   </xsl:template>

</xsl:stylesheet>
