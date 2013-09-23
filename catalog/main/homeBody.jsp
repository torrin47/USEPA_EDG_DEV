<%--
 See the NOTICE file distributed with
 this work for additional information regarding copyright ownership.
 Esri Inc. licenses this file to You under the Apache License, Version 2.0
 (the "License"); you may not use this file except in compliance with
 the License.  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
--%>
<% // homeBody.jsp - Home page (JSF body) %>
<%@taglib prefix="f" uri="http://java.sun.com/jsf/core"%>
<%@taglib prefix="h" uri="http://java.sun.com/jsf/html"%>
<f:verbatim>

<script type="text/javascript" src="../../catalog/js/jquery-ui/js/jquery.js"></script>
<script type="text/javascript" src="../../catalog/js/jquery-ui/js/jquery-ui.js"></script>
<script type="text/javascript">
/**
Submits from when on enter.
@param event The event variable
@param form The form to be submitted.
**/
function hpSubmitForm(event, form) {

  var e = event;
  if (!e) e = window.event;
  var tgt = (e.srcElement) ? e.srcElement : e.target; 
  if ((tgt != null) && tgt.id) {
    if (tgt.id == "frmSearchCriteria:mapInput-locate") return;
  }
  
  if(!GptUtils.exists(event)) {
    GptUtils.logl(GptUtils.log.Level.WARNING, 
         "fn submitform: could not get event so as to determine if to submit form ");
    return;
  }
  var code;
  
  if(GptUtils.exists(event.which)) {
    code = event.which;
  } else if (GptUtils.exists(event.keyCode)) {
    code = event.keyCode;
  } else {
    GptUtils.logl(GptUtils.log.Level.WARNING, 
         "fn submitForm: Could not determine key pressed");
    return;
  }
  
  if(code == 13) {
    
    // Getting main search button
    var searchButtonId = "hpFrmSearch:btnDoSearch";
    var searchButton = document.getElementById(searchButtonId);
    if(!GptUtils.exists(searchButton)){
      GptUtils.logl(GptUtils.log.Level.WARNING, 
         "Could not find button id = " + searchButtonId);
    } else if (!GptUtils.exists(searchButton.click)) {
      GptUtils.logl(GptUtils.log.Level.WARNING, 
         "Could not find click action on id = " + searchButtonId);
    } else {
      searchButton.click();
    }
  } else {
    return true;
  }
}
</script>
<style type="text/css" media="screen,projection">
#footer {
	background-repeat: no-repeat;
	background-position: 0 0;
	height: 154px;
	clear: both;
	position: relative;
	overflow: visible;
}
.ancillary {
	color: #000000;
	display: block;
	position: absolute;
	top: 20px;
	left: 30px;
	width: 464px;
	padding:0 5px;
	line-height: 24px;
	font-size: 0.85em;
	font-style:normal;

}
.ancillary ul {
	list-style:none;
	text-indent:-5px;
	font-style:normal;
}
.ancillary ul li {
	display:inline;
}
.ancillary li a {
	color: #435258;
	text-decoration:underline;
	padding: 0 2px 0 2px;
	border-right:  #8f989c 1px solid;
    border-bottom:none;
	font-style:normal;
}
.ancillary li a:hover {
	color: #47aa42;
}
li a.ancillaryLast {
	padding: 0 0 0 3px;
	border-right:  none;
}
#footerTools {
	width: 260px;
	height: 80px;
	background: url(../images/footer_tools.png);
	position: relative;
	top: 33px;
	left:539px;
}
#footerTools li {
	margin: 0;
	padding: 0;
	list-style: none;
	position: absolute;
}
#footerTools li, #footerTools a {
	height: 40px;
	display: block;
        border-bottom:none;
}
 
#footerTools li a span {
display: none;
}

#Widgets {
	top:0;
	left:0;
	width: 115px;
}
#RSS {
	top:0;
	left:115px;
	width: 145px;
}
#Podcasts {
	top:40px;
	left:0px;
	width: 115px;
}
#Mobile {
	top:40px;
	left:115px;
	width: 145px;
}
#Email {
	top:40px;
	left:115px;
	width: 145px;
}
#Widgets a:hover {
	background: url(../images/footer_tools.png) 0 -80px no-repeat;
}
#RSS a:hover {
	background: url(../images/footer_tools.png) -115px -80px no-repeat;
}
#Podcasts a:hover {
	background: url(../images/footer_tools.png) 0px -120px no-repeat;
}
#Mobile a:hover {
	background: url(../images/footer_tools.png) -115px -120px no-repeat;
}
#Email a:hover {
	background: url(../images/footer_tools.png) -115px -120px no-repeat;
}
h4.footer_LogoLink a {
	text-indent: -9999px;
	display: block;
	width: 82px;
	height: 82px;
	top: 33px;
	left: 846px;
	position:absolute;
}
.EpaLinks {
    font-family: "Lucida Grande",Geneva,Verdana,Arial,Helvetica,sans-serif;
    font-size: 1.0em;
    color: #435258;
    font-weight: bold;
    text-indent: -4px
}
</style>
</f:verbatim>
 


<f:verbatim>
<p>&nbsp;</p>
</f:verbatim>


<h:outputText escape="false" styleClass="prompt" value="#{gptMsg['catalog.main.home.prompt']}"/>

<f:verbatim>
    <a id="frmTertiaryNavigation:identityFeedback" style="font-size:1.1em;"
       onclick="mojarra.jsfcljs(document.getElementById('frmTertiaryNavigation'),{'frmTertiaryNavigation:identityFeedback':'frmTertiaryNavigation:identityFeedback'},'');return false"
       href="#">Contact the EDG Team</a>
</f:verbatim>
<h:panelGrid columns="4" summary="#{gptMsg['catalog.general.designOnly']}" width="100%" columnClasses="homeTableColLeft,homeTableColRight">

	<h:panelGrid columns="1" summary="#{gptMsg['catalog.general.designOnly']}" width="100%" columnClasses="homeTableLeft" footerClass="homeTableLeftFooter" headerClass="homeTableLeftHeader" cellpadding="0" cellspacing="0">
		<f:facet name="header">
			<h:column>
				<h:graphicImage id="homeTableLeftHeaderImageL" alt="" styleClass="homeTableLeftHeaderImageL" url="/catalog/images/blank.gif" width="15" height="24"></h:graphicImage>
				<h:graphicImage id="homeTableLeftHeaderImageR" alt="" styleClass="homeTableLeftHeaderImageR" url="/catalog/images/blank.gif" width="48" height="24"></h:graphicImage>
				<h:outputText escape="false" value="#{gptMsg['catalog.main.home.youCanSimply']}"/>
			</h:column>
		</f:facet>
		<h:column>
			<h:outputText escape="false" value="#{gptMsg['catalog.main.home.topic.findData']}"/>
			<f:verbatim><p>&nbsp;</p></f:verbatim>

			<h:panelGrid columns="1" summary="#{gptMsg['catalog.general.designOnly']}" width="90%" styleClass="homeTableCol">
				<h:panelGrid columns="2" id="_pnlKeyword" cellpadding="0" cellspacing="0">
				
					<h:form id="hpFrmSearch" onkeypress="javascript: hpSubmitForm(event, this);">
					<h:inputText id="itxFilterKeywordText" 
					  onkeypress="if (event.keyCode == 13) return false;"
					  value="#{SearchFilterKeyword.searchText}" maxlength="400" style="width: 240px" />
					
					<h:commandButton id="btnDoSearch"
					  value="#{gptMsg['catalog.search.search.btnSearch']}"
					  action="#{SearchController.getNavigationOutcome}"
					  actionListener="#{SearchController.processAction}"
					  onkeypress="if (event.keyCode == 13) return false;">
					  <f:attribute name="#{SearchController.searchEvent.event}"
					    value="#{SearchController.searchEvent.eventExecuteSearch}" />
                                          <!--Added by Netty-->
                                          <f:attribute name="f"
					    value="searchpageresults" />
					</h:commandButton>
					</h:form>
					
				</h:panelGrid>
				<f:verbatim>
				<p><br></p>
				</f:verbatim>
				<h:outputText escape="false" value="#{gptMsg['catalog.main.home.topic.findData.searchData']}"/>
				<h:outputText styleClass="contentStatement" escape="false" rendered="#{PageContext.roleMap['anonymous']}" value="#{gptMsg['catalog.main.home.topic.findData.browseExternal']}"/>
				<h:outputText styleClass="contentStatement" escape="false" rendered="#{not PageContext.roleMap['anonymous']}" value="#{gptMsg['catalog.main.home.topic.findData.browseInternal']}"/>
				
				<h:outputText value="#{gptMsg['catalog.main.home.topic.findData.downloadData']}"/>
				
			</h:panelGrid>
		</h:column>
		<f:facet name="footer">
			<h:column>
				<h:graphicImage id="homeTableLeftFooterImageL" alt="" styleClass="homeTableLeftFooterImageL" url="/catalog/images/blank.gif" width="23" height="16"></h:graphicImage>
				<h:graphicImage id="homeTableLeftFooterImageR" alt="" styleClass="homeTableLeftFooterImageR" url="/catalog/images/blank.gif" width="21" height="16"></h:graphicImage>
			</h:column>
		</f:facet>
	</h:panelGrid>
  

	<h:panelGrid columns="2" summary="#{gptMsg['catalog.general.designOnly']}" columnClasses="homeTableRight,homeTableRight" width="100%" footerClass="homeTableRightFooter" headerClass="homeTableRightHeader" cellpadding="0" cellspacing="0">
		<f:facet name="header">
			<h:column>
				<h:graphicImage id="homeTableRightHeaderImageL" alt="" styleClass="homeTableRightHeaderImageL" url="/catalog/images/blank.gif" width="15" height="24"></h:graphicImage>
				<h:graphicImage id="homeTableRightHeaderImageR" alt="" styleClass="homeTableRightHeaderImageR" url="/catalog/images/blank.gif" width="48" height="24"></h:graphicImage>
				<h:outputText value="#{gptMsg['catalog.main.home.youCanDoMore']}"/>
			</h:column>
		</f:facet>
		<h:column>
			<h:outputText escape="false" value="#{gptMsg['catalog.main.home.topic.DataAccess.External']}"/>
			<f:verbatim><p>&nbsp;</p></f:verbatim>
			<h:panelGrid columns="1" summary="#{gptMsg['catalog.general.designOnly']}" styleClass="homeTableCol" width="80%">
			<h:outputText escape="false" value="#{gptMsg['catalog.main.home.topic.DataAccess.External.Img']}"/>

			</h:panelGrid>
		</h:column>
		<h:column>
			<h:outputText escape="false" value="#{gptMsg['catalog.main.home.topic.shareData']}"/>
			<f:verbatim><p>&nbsp;</p></f:verbatim>
			<h:panelGrid columns="1" summary="#{gptMsg['catalog.general.designOnly']}" styleClass="homeTableCol" width="80%">
				<h:outputText escape="false" value="#{gptMsg['catalog.main.home.topic.shareData.createMetadata']}"/>

			</h:panelGrid>
		</h:column>
		<f:facet name="footer">
			<h:column>
				<h:graphicImage id="homeTableRightFooterImageL" alt="" styleClass="homeTableRightFooterImageL" url="/catalog/images/blank.gif" width="17" height="20"></h:graphicImage>
				<h:graphicImage id="homeTableRightFooterImageR" alt="" styleClass="homeTableRightFooterImageR" url="/catalog/images/blank.gif" width="23" height="20"></h:graphicImage>
			</h:column>
		</f:facet>
	</h:panelGrid>
              
</h:panelGrid>



<f:verbatim>
  <div id="footer">
    <div class="ancillary">
        <p class="EpaLinks">
            EPA Web Links:
        </p>
     <ul>
        <li><a href="http://www.epa.gov/epahome/comments3.htm">Contact Us</a></li>
        <li><a href="http://www.epa.gov/epahome/aboutepa.htm">About EPA</a></li>
        <li><a href="http://www.epa.gov/careers/">Careers</a></li>
        <li><a href="http://www.epa.gov/epahome/faq.htm">FAQs</a></li>
        <li><a href="http://www.epa.gov/newsroom/">Newsroom</a></li>

        <li><a href="http://blog.epa.gov/blog/">Blog</a></li>
        <li><a href="http://www.epa.gov/epahome/usenotice2.htm" class="ancillaryLast">Privacy &amp; Security</a></li>
      </ul>
      <ul>
        <li><a href="http://www.epa.gov/oig/">Inspector General</a></li>
        <li><a href="http://www.epa.gov/performance/">Budget &amp; Performance</a></li>

        <li><a href="http://www.epa.gov/civilrights/nofearact.htm">No Fear Act</a></li>
        <li><a href="http://www.epa.gov/foia/">FOIA</a></li>
        <li><a href="http://www.epa.gov/epahome/accessibility2.htm" class="ancillaryLast">Accessibility</a></li>
      </ul>
      <ul>
        <li><a href="http://www.usa.gov/">USA.gov</a></li>
        <li><a href="http://www.whitehouse.gov/">White House</a></li>

        <li><a href="http://www.regulations.gov/">Regulations.gov</a></li>
        <li><a href="http://www.pandemicflu.gov/">Pandemicflu.gov</a></li>
        <li><a href="http://www.epa.gov/pandemicflu/" class="ancillaryLast">EPA &amp; Pandemic Flu</a></li>
      </ul>
    </div>
    <ul id="footerTools">

      <li id="Widgets"><a href="http://www.epa.gov/widgets/"><span>Widgets</span></a></li>
      <li id="RSS"><a href="http://www.epa.gov/newsroom/rssfeeds.htm"><span>RSS</span></a></li>
      <li id="Podcasts"><a href="http://www.epa.gov/epahome/podcasts.htm"><span>Podcasts</span></a></li>
      <li id="Mobile"><a href="http://m.epa.gov/"><span>EPA Mobile</span></a></li>
    </ul>
  </div>

    <!-- // END OF FOOTER//  -->
<!-- more content here -->

</f:verbatim>
