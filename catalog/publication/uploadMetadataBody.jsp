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
<% // uploadMetadataBody.jsp - Upload metadata page (JSF body) %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>
<%@taglib uri="http://www.esri.com/tags-gpt" prefix="gpt" %>

<h:form id="upload" enctype="multipart/form-data" styleClass="fixedWidth"
        onsubmit="ckCsv();">
<h:inputHidden value="#{UploadMetadataController.prepareView}"/>
<f:verbatim>
    <iframe id="innoMsgsIframe" name="innoMsgsIframe" src="" style="display:none"></iframe>
    <div id="innoMsgs" style="display:none"></div>
</f:verbatim>

<script type="text/javascript">

function uploadOnSpecMethodClicked() {
  var i, bExplicit = false;;
  var oRadio = document.getElementsByName("upload:specificationMethod"); 
  for (i=0;i<oRadio.length;i++) {
    if (oRadio[i].checked) {
      bExplicit = (oRadio[i].value == "explicit");
      break;
    }
  }
  if (!bExplicit) {
    document.getElementById("upload:meth_explicit").style.display = "none";
    document.getElementById("upload:meth_browse").style.display = "block";
  } else {
    document.getElementById("upload:meth_explicit").style.display = "block";
    document.getElementById("upload:meth_browse").style.display = "none";
  }
}

function displayInnoMsgs() {
    //alert("display upload done");
    var msg = frames['innoMsgsIframe'].document.getElementsByTagName("body")[0].innerHTML;
    document.getElementById("innoMsgs").innerHTML = msg;
    document.getElementById("innoMsgs").style.display = "block";
    // if there are esri msgs, make them invisible
    var esriMsgs = document.getElementById("cmPlPgpGptMessages");
    // restore the submit buttons
    document.getElementById("innoButtons").style.display = "block";
    // change the curser to pointer
    document.getElementById("gptBody").style.cursor = "default";
    //alert("esriMsgs: "+esriMsgs);
    if (esriMsgs != null)
        esriMsgs.style.display = "none";
}

function ckCsv() {
    // if this is a .csv, post to uploadDataGov
    var fname = document.getElementById("upload:uploadXml").value.toUpperCase();
    //alert("fname: "+fname);
    if (fname.lastIndexOf(".CSV")==fname.length-4) {
       //alert("is csv");
       document.getElementById("upload").action = "UploadDataGov/";
       document.getElementById('upload').target = "innoMsgsIframe";
       // hide the submit buttons
       document.getElementById("innoButtons").style.display = "none";
       // change the curser to hourglass
       document.getElementById("gptBody").style.cursor = "wait";
    } else {
       document.getElementById("upload").action = "<%= request.getContextPath() %>/catalog/publication/uploadMetadata.page";
       document.getElementById('upload').target = null;
    }
}

</script>

<% // prompt %>
<h:outputText escape="false" styleClass="prompt"
  value="#{gptMsg['catalog.publication.uploadMetadata.prompt']}"/>
  
<% // input table %>
<h:panelGrid columns="1" summary="#{gptMsg['catalog.general.designOnly']}"
  styleClass="formTable" columnClasses="formInputColumn">
  
  <% // on behalf of %>
  <h:panelGroup>
    <h:outputLabel for="onBehalfOf" styleClass="requiredField"
      value="#{gptMsg['catalog.publication.uploadMetadata.label.onBehalfOf']}"/>
    <h:selectOneMenu id="onBehalfOf"
       value="#{UploadMetadataController.selectablePublishers.selectedKey}">
      <f:selectItems value="#{UploadMetadataController.selectablePublishers.items}"/>
    </h:selectOneMenu>
  </h:panelGroup>
  
  <% // specification method %>
  <f:verbatim><br/></f:verbatim>
  <%--
  <h:selectOneRadio id="specificationMethod"
    value="#{UploadMetadataController.specificationMethod}"
    onclick="javascript:uploadOnSpecMethodClicked(this);">
    <f:selectItem itemValue="browse"
      itemLabel="#{gptMsg['catalog.publication.uploadMetadata.method.browse']}" />
    <f:selectItem itemValue="explicit"
      itemLabel="#{gptMsg['catalog.publication.uploadMetadata.method.explicit']}" />
  </h:selectOneRadio>
  --%>
    
  <% // browse for local file %>
  <h:panelGroup id="meth_browse" style="#{UploadMetadataController.styleForBrowseMethod}">
	  <f:verbatim>
	    <label class="requiredField" id="upload:lbluploadXml" for="upload:uploadXml"><%=com.esri.gpt.framework.jsf.PageContext.extractMessageBroker().retrieveMessage("catalog.publication.uploadMetadata.label.file")%></label>
	    <input type="file" id="upload:uploadXml" name="upload:uploadXml" size="50"/>
	  </f:verbatim>
  </h:panelGroup>
  
  <% // explicit path (URL or UNC) %>
  <%--
  <h:panelGroup id="meth_explicit" style="#{UploadMetadataController.styleForExplicitMethod}">
    <h:outputLabel for="explicitPath" styleClass="requiredField"
      value="#{gptMsg['catalog.publication.uploadMetadata.label.url']}"/>
    <h:inputText id="explicitPath" size="80" value="#{UploadMetadataController.explicitPath}"/>
  </h:panelGroup>
  --%>

  <% // buttons %>
  <f:verbatim><br/></f:verbatim>
  <h:panelGroup>
    <f:verbatim><div id="innoButtons"></f:verbatim>
    <h:commandButton id="submit"
      value="#{gptMsg['catalog.publication.uploadMetadata.button.submit']}"
      action="#{UploadMetadataController.getNavigationOutcome}"
      actionListener="#{UploadMetadataController.processAction}" />
    <h:commandButton id="validate"
      value="#{gptMsg['catalog.publication.uploadMetadata.button.validate']}"
      action="#{ValidateMetadataController.getNavigationOutcome}"
      actionListener="#{UploadMetadataController.processAction}">
      <f:attribute name="command" value="validate" />
   </h:commandButton>
   <f:verbatim></div></f:verbatim>
  </h:panelGroup>
  
</h:panelGrid>

<% // required fields note %>
<h:outputText escape="false" styleClass="requiredFieldNote"
  value="#{gptMsg['catalog.general.requiredFieldNote']}"/>
  
</h:form>
