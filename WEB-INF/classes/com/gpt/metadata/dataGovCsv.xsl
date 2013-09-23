<?xml version="1.0" encoding="UTF-8"?>

<!--
    Document   : dataGovCsv.xsl
    Created on : January 4, 2011, 9:17 PM
    Author     : John Sievel
    Description:
        Transform data.gov xml to csv.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="text" encoding="UTF-8"/>
    <xsl:param name="edgId"/>
    <xsl:param name="suppressHeader"/>

    <xsl:template match="/">
        <xsl:if test="$suppressHeader != 'true'">
            <xsl:call-template name="header"/>
        </xsl:if>
        <xsl:apply-templates select="dataGov"/>
    </xsl:template>
    
    <xsl:template match="dataGov">
        <xsl:apply-templates select="basic"/>
        <xsl:apply-templates select="downloadableFile[1]"/>
        <xsl:apply-templates select="statisticalMethodology[1]"/>
        <xsl:apply-templates select="ogd"/>
        <xsl:apply-templates select="gdg"/>
        <xsl:text>&#10;</xsl:text>
        <xsl:apply-templates select="downloadableFile[position()>1]"/>
    </xsl:template>

    <xsl:template name="header">
        <xsl:text>Element Number,1,1.1,2,3,4,5,5.1,6,6.1,7,7.1,7.2,8,8.1,8.2,9,10,11,12,13,14,15,16,17,18,19,20,21,21.1,22,23,24,25,26,27,28,29,Downloadable file metadata (repeat this section for multiple download formats using multiple rows underneath initial submission.,D1,D2,D3,D4,Additional Statistical Metadata ,SM,SM.01,SM.02,SM.03,SM.04,SM.05,SM.06,SM.07,SM.08,SM.09,OGD Response specific metadata ,OGD,OGD.01,OGD.02,OGD.03,OGD.04,GeoDataGateway Specific Information (optional),GDG.ID,GDG.CT&#10;</xsl:text>
        <xsl:text>Element,Unique ID,User Generated  ID, Title,Dataset Group Name,Description,Agency Name,Agency Short Name,Sub-Agency Name,Sub-Agency   Short Name,Contact Name,Contact Phone Number,Contact Email Address,Agency responsible for Information Quality,Compliance with Agency's Information Quality Guidelines,Privacy and Confidentiality ,Data.gov data category type,Subject area (Taxonomy),Specialized data category designation,Keywords ,Date released,Date updated,Agency Program URL,Agency Data Series URL,Collection mode,Frequency,Period of Coverage,Unit of analysis,Geographic scope,Geographic Granularity,Reference for Technical Documentation,Data dictionary/variable list,Data collection instrument ,Bibliographic citation for dataset,Number of Datasets Represented by this Submission,Additional Metadata,Dataset use requires a license agreement,Dataset license agreement URL,,Access point,Media Format,File size,File format ,,Statistical methodology ,Sampling,Estimation,Weighting  ,Disclosure avoidance,Questionnaire design,Series breaks,Non-response adjustment,Seasonal adjustment,"Data quality (variances, CVs, CIs, etc)",,Is this record a part of OGD submissions,Is this record listed in your agency open government plan,Is this a high value dataset,What makes this a high value dataset,How is this new,,GDG Metadata Id,GDG ContentType&#10;</xsl:text>
        <xsl:text>"Occurrence [min,max]","[1,1]","[1,1]","[1,1]","[0,1]","[1,1]","[1,1]","[1,1]","[1,1]","[1,1]","[1,1]","[1,1]","[1,1]","[1,1]","[1,1]","[1,1]","[1,1]","[1,1]","[1,1]","[1,n]","[1,1]","[1,1]","[1,1]","[1,1]","[1,n]","[1,1]","[1,n]","[1,1]","[0,n]","[0,1]","[1,n]","[1,1]","[1,n]","[1,1]","[0,1]","[0,1]","[1,1]","[1,1]",,"[1,1]","[1,1]","[1,1]","[1,1]",,"[0,n] ","[0,1] ","[0,1] ","[0,1] ","[0,1] ","[0,1] ","[0,1] ","[0,1] ","[0,1] ","[0,1] ",,"[1,1]","[1,1]","[1,1]","[1,1]","[1,1]",,"[0,1]","[0,1]"&#10;</xsl:text>
    </xsl:template>

    <xsl:template match="basic">
        <xsl:text>,</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="uniqueId" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="userGeneratedId" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="title" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="datasetGroupName" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="description" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="agencyName" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="agencyShortName" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="subAgencyName" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="subAgencyShortName" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="contactName" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="contactPhoneNumber" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="contactEmailAddress" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="agencyResponsibleForQuality" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="complianceWithAgencysQualityGuidelines" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="privacyAndConfidential" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="dataGovCatalogType" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="subjectArea" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="specializedDataCategoryDesignation" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="keywords" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="dateReleased" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="dateUpdated" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="agencyProgramUrl" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="agencyDataSeriesUrl" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="collectionMode" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="frequency" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="periodOfCoverage" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="unitOfAnalysis" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="geographicScope" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="geographicGranularity" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="referenceForTechnicalDocumentation" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="dataDictionaryVariableList" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="dataCollectionInstrument" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="bibliographicCitationForDataset" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="numberDatasetsRepresentedByThisSubmission" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="additionalMetadata" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="datasetUseRequiresLicense" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="datasetLicenseAgreementUrl" /><xsl:text>",</xsl:text>
    </xsl:template>

    <xsl:template match="downloadableFile[1]">
        <xsl:text>,</xsl:text>
        <xsl:call-template name="download"/>
    </xsl:template>

    <xsl:template match="downloadableFile[position()>1]">
        <xsl:text>,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,</xsl:text>
        <xsl:call-template name="download"/>
        <xsl:text>,,,,,,,,,,,,,,,,,,,,&#10;</xsl:text>
    </xsl:template>

    <xsl:template name="download">
        <xsl:text>"</xsl:text><xsl:value-of select="accessPoint" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="mediaFormat" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="fileSize" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="fileFormat" /><xsl:text>",</xsl:text>
    </xsl:template>

    <xsl:template match="statisticalMethodology">
        <xsl:text>,</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="sampling" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="estimation" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="weighting" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="disclosureAvoidance" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="questionnaireDesign" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="seriesBreaks" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="nonResponseAdjustment" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="seasonalAdjustment" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="dataQuality" /><xsl:text>",</xsl:text>
    </xsl:template>

    <xsl:template match="ogd">
        <xsl:text>,,</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="ogdSubmission" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="listedInOpenGov" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="highValue" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="whyHighValue" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="howNew" /><xsl:text>",</xsl:text>
    </xsl:template>

    <xsl:template match="gdg">
        <xsl:text>,</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="$edgId" /><xsl:text>",</xsl:text>
        <xsl:text>"</xsl:text><xsl:value-of select="contentType" /><xsl:text>",</xsl:text>
    </xsl:template>

</xsl:stylesheet>
