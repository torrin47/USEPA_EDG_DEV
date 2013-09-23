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
<% // browseBody.jsp - view metadata details(JSF body) %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>
<%@ taglib prefix="gpt" uri="http://www.esri.com/tags-gpt"%>

<%	
  com.esri.gpt.framework.jsf.MessageBroker brwMsgBroker = com.esri.gpt.framework.jsf.PageContext.extractMessageBroker();
        String reqUrl = request.getRequestURL().toString();
        String brwContextPath = request.getContextPath();
        int slshPos = reqUrl.indexOf("/", 9);
        if (slshPos>=0)
            brwContextPath = reqUrl.substring(0,slshPos)+request.getContextPath();
        String brwKey = request.getParameter("key");
        String brwUuid = com.esri.gpt.framework.util.Val.chkStr(request.getParameter("uuid"));
        if(brwKey == null){
                brwKey = "browseCatalog";
        }
        String brwTocUrl = brwContextPath + "/browse/toc?key=" + java.net.URLEncoder.encode(brwKey,"UTF-8");
        if(brwUuid.length() > 0){
                brwUuid = java.net.URLEncoder.encode(brwUuid,"UTF-8");
                brwTocUrl += "&uuid=" + brwUuid;
        }
        brwTocUrl = com.esri.gpt.framework.util.Val.escapeXmlForBrowser(brwTocUrl);
        String brwPrompt = "";
         if(brwKey.equals("browseCatalog")){
                 brwPrompt = brwMsgBroker.retrieveMessage("catalog.browse.prompt");
         } else {
                 brwPrompt = brwMsgBroker.retrieveMessage("catalog.search.resource.relationships.prompt");
         } 
%>
<gpt:jscriptVariable
    id="innoRestFrom"
    quoted="true"
    value="#{gptMsg['catalog.search.searchResult.restFrom']}"
    variableName="innoRestFrom"/>
<gpt:jscriptVariable
    id="innoRestTo"
    quoted="true"
    value="#{gptMsg['catalog.search.searchResult.restTo']}"
    variableName="innoRestTo"/>

   <style type="text/css">
  .columnsTable td {vertical-align: top;}
  
  .grid td, .grid th{
        line-height: 18px;
    }
    
     #infovis {

        margin: auto;
        overflow: hidden;
        position: relative;

    }
    </style>

<script type="text/javascript">
    /**
     * gpt-browse.js
     * Browse support
     */

    dojo.declare("BrowseMainInno", null, {
        contextPath: "",
        filterText: "",
        nItemsPerPage: 10,
        persist: true,
        resultsBodyId: "browse-results-body",
        resultsHeaderId: "browse-results-header",
        query: "",
        resetFilterOnTreeItemClicked: false,
        treeId: "browse-toc",
        resources: {
            noItemsSelected: "Select one of the items in the tree to view a specific list of resources.",
            noResults: "Search returned no results",
            filter: "Filter",
            clear: "Clear",
            first: "First",
            last: "Last",
            summaryPattern: "{0} results",
            summaryPatternFiltered: "{0} filtered results",
            pageSummaryPattern: "Showing {0}-{1}"
        },

        buildHeader: function(nStartIndex,nTotalResults,wasFiltered) {
            var elHdr = dojo.byId(this.resultsHeaderId);

            var elFilterControl = document.createElement("div");
            elFilterControl.className = "nav";

            var sSummaryText = this.resources.summaryPattern;
            if (wasFiltered) sSummaryText = this.resources.summaryPatternFiltered;
            sSummaryText = sSummaryText.replace("{0}",nTotalResults);
            var elSummaryText = document.createElement("span");
            elSummaryText.id = this.resultsHeaderId+"-summaryText";
            elSummaryText.className = "result";
            elSummaryText.appendChild(document.createTextNode(sSummaryText));
            elFilterControl.appendChild(elSummaryText);

            var elFilterText = document.createElement("input");
            elFilterText.id = this.resultsHeaderId+"-filterText";
            elFilterText.type = "text";
            elFilterText.size = 20;
            elFilterText.maxlength = 1024;
            elFilterText.value = this.filterText;
            elFilterControl.appendChild(elFilterText);
            //dojo.connect(elText,"onchange",this,"onFilterTextChange");
            dojo.connect(elFilterText,"onkeypress",this,"onFilterTextKeyPress");

            var elFilterBtn = document.createElement("input");
            elFilterBtn.id = this.resultsHeaderId+"-filter";
            elFilterBtn.type = "submit";
            elFilterBtn.value = this.resources.filter;
            elFilterControl.appendChild(elFilterBtn);
            dojo.connect(elFilterBtn,"onclick",this,"onFilterClicked");

            var elClearBtn = document.createElement("input");
            elClearBtn.id = this.resultsHeaderId+"-clearFilter";
            elClearBtn.type = "submit";
            elClearBtn.value = this.resources.clear;
            elFilterControl.appendChild(elClearBtn);
            dojo.connect(elClearBtn,"onclick",this,"onClearFilterClicked");

            elHdr.innerHTML = "";
            elHdr.appendChild(elFilterControl);

            if (nTotalResults == 0) {
                return;
            }

            var currentPageNumber = Math.ceil(nStartIndex / this.nItemsPerPage);
            var nOfPages = Math.ceil(nTotalResults / this.nItemsPerPage);
            var iFrom = (currentPageNumber - 2) > 1 ? (currentPageNumber - 2) : 1;
            var iTo = (currentPageNumber + 2) > nOfPages ? nOfPages : (currentPageNumber + 2);
            if (iTo < 6) {
                iTo = nOfPages >= 5 ? 5 : nOfPages;
                iFrom = 1;
            }else if (iTo == nOfPages){
                iFrom = nOfPages - 4;
            }
            var nEndIndex = nStartIndex + this.nItemsPerPage - 1;
            if (nEndIndex > nTotalResults) nEndIndex = nTotalResults;

            var elPageControl = document.createElement("div");
            elPageControl.id = this.resultsHeaderId+"-pageControl";
            elPageControl.className = "nav";

            var sPageSummary = this.resources.pageSummaryPattern;
            sPageSummary = sPageSummary.replace("{0}",nStartIndex);
            sPageSummary = sPageSummary.replace("{1}",nEndIndex);
            var elPageSummary = document.createElement("span");
            elPageSummary.className = "result";
            elPageSummary.appendChild(document.createTextNode(sPageSummary));
            elPageControl.appendChild(elPageSummary);

            var elPageNumbers = document.createElement("span");
            elPageControl.appendChild(elPageNumbers);
            if (iFrom > 1) {
                var elPage = document.createElement("a");
                elPage.setAttribute("href","javascript:void(0);");
                elPage.pageNumber = 1;
                elPage.appendChild(document.createTextNode(this.resources.first));
                elPageNumbers.appendChild(elPage);
                dojo.connect(elPage,"onclick",this,"onPageClicked");

                var elPage = document.createElement("a");
                elPage.setAttribute("href","javascript:void(0);");
                elPage.pageNumber = iFrom;
                elPage.appendChild(document.createTextNode("<"));
                elPageNumbers.appendChild(elPage);
                dojo.connect(elPage,"onclick",this,"onPageClicked");
            }
            if (iTo > 1) {
                for (var i=iFrom; i<=iTo; i++) {
                    var elPage = document.createElement("a");
                    elPage.setAttribute("href","javascript:void(0);");
                    elPage.pageNumber = i;
                    elPage.appendChild(document.createTextNode(""+i));
                    if (i == currentPageNumber) {
                        elPage.className = "current";
                    }
                    elPageNumbers.appendChild(elPage);
                    dojo.connect(elPage,"onclick",this,"onPageClicked");

                }
            }

            if (iTo < nOfPages) {
                var elPage = document.createElement("a");
                elPage.setAttribute("href","javascript:void(0);");
                elPage.pageNumber = iTo;
                elPage.appendChild(document.createTextNode(">"));
                elPageNumbers.appendChild(elPage);
                dojo.connect(elPage,"onclick",this,"onPageClicked");
                var elPage = document.createElement("a");
                elPage.setAttribute("href","javascript:void(0);");
                elPage.pageNumber = nOfPages;
                elPage.appendChild(document.createTextNode(this.resources.last));
                elPageNumbers.appendChild(elPage);
                dojo.connect(elPage,"onclick",this,"onPageClicked");
            }

            elHdr.appendChild(elPageControl);

        },

        executeSearch: function(e,nStartIndex) {
            var wasFiltered = false;
            if (nStartIndex == null) nStartIndex = 1;
            if ((this.query == undefined) || (this.query == null)) {
                dojo.byId(this.resultsHeaderId).innerHTML = "";
                this.showMessage(this.resources.noItemsSelected,"prompt");
            } else {
                var sQueryString = ""+this.query;
                var sRequestBase = this.contextPath+"/rest/find/document";
                if (sQueryString.length > 0) {
                    if (sQueryString.charAt(0) == "?") {
                        sQueryString = dojo.trim(sQueryString.substring(1));
                    }
                    if (sQueryString.charAt(0) == "&") {
                        sQueryString = dojo.trim(sQueryString.substring(1));
                    }
                }

                if (this.filterText != null) {
                    var sFilter = dojo.trim(this.filterText);
                    if (sFilter.length > 0) {
                        wasFiltered = true;
                        if (sQueryString.indexOf("searchText=") != -1) {
                            var iIndex = sQueryString.indexOf("searchText=");
                            var sLeft = sQueryString.substring(0,iIndex);
                            var sRight = sQueryString.substring(iIndex+11);
                            var sMiddle = "searchText=" + escape(sFilter + " AND ");
                            sQueryString = sLeft + sMiddle + sRight;
                        } else {
                            if (sQueryString.length > 0) sQueryString += "&";
                            sQueryString += "searchText=" + escape(sFilter);
                        }
                    }
                }
                if (sQueryString.length > 0) {
                    sRequestBase += "?"+sQueryString;
                }
                if (sQueryString.toLowerCase().indexOf("http://") > -1 || sQueryString.toLowerCase().indexOf("https://") > -1) {
                    sRequestBase = sQueryString;
                }
                var sUrl = sRequestBase;
                if (sUrl.indexOf("?") == -1) sUrl += "?";
                else sUrl += "&";
                sUrl += "f=htmlfragment&start="+nStartIndex+"&max="+this.nItemsPerPage;
                //console.debug("sUrl: "+sUrl);
                dojo.byId("innoFragment").href = sUrl;
                dojo.byId("innoHtml").href = sUrl.replace("htmlfragment","html");
                dojo.byId("innoGeorss").href = sUrl.replace("htmlfragment","georss");
                dojo.byId("innoKml").href = sUrl.replace("htmlfragment","kml");
                dojo.byId("innoFragmentIntra").href = dojo.byId("innoFragment").href.replace(innoRestFrom,innoRestTo);
                dojo.byId("innoHtmlIntra").href = dojo.byId("innoHtml").href.replace(innoRestFrom,innoRestTo);
                dojo.byId("innoGeorssIntra").href = dojo.byId("innoGeorss").href.replace(innoRestFrom,innoRestTo);
                dojo.byId("innoKmlIntra").href = dojo.byId("innoKml").href.replace(innoRestFrom,innoRestTo);

                dojo.xhrGet({
                    url: sUrl,
                    handleAs: "text",
                    load: dojo.hitch(this,function(response, ioArgs) {

                        var nTotalResults = this.parseHiddenInt(response,"<input type=\"hidden\" id=\"totalResults\" value=\"", 0);

                        if (nTotalResults > 0) {
                            // make rest links visible
                            dojo.byId("innoRestLinks").style.display = "block";
                            nStartIndex = this.parseHiddenInt(response,"<input type=\"hidden\" id=\"startIndex\" value=\"", nStartIndex);
                            this.buildHeader(nStartIndex,nTotalResults,wasFiltered);
                            dojo.byId(this.resultsBodyId).innerHTML = response;
                            dojo.query("div.title",this.resultsBodyId).forEach(dojo.hitch(this, function(item) {
                                dojo.connect(item,"onclick",this,"onTitleClicked");
                            }));
                        }else {
                            this.buildHeader(nStartIndex,nTotalResults,wasFiltered);
                            dojo.byId(this.resultsBodyId).innerHTML = "";
                            dojo.byId("innoRestLinks").style.display = "none";
                        }
                        return response;
                    }),

                    error: dojo.hitch(this,function(response, ioArgs) {
                        this.showMessage(sRequest+" "+response,"errorMessage");
                        return response;
                    })
                });

            }
        },

        init: function() {
            var tree = dijit.byId(this.treeId);
            if (tree != null) dojo.connect(tree,"onLoad",this,"onDijitTreeLoaded");
        },

        onDijitTreeLoaded: function() {
            if (this.persist) {
                var tree = dijit.byId(this.treeId);
                if (tree != null) {
                    var lastClickedId = dojo.cookie(tree.cookieName+"_lastClickedId");
                    if (lastClickedId != null) {
                        var aryNodes = tree.getNodesByItem(lastClickedId);
                        if ((aryNodes != null) && (aryNodes.length > 0)) {
                            var treeNode = aryNodes[0];
                            if ((treeNode != null) && (treeNode.item != null)) {
                                tree.focusNode(treeNode);
                                this.onTreeItemClicked(treeNode.item);
                            }
                        }
                    }
                }
            }
        },

        onClearFilterClicked: function() {
            var elText = dojo.byId(this.resultsHeaderId+"-filterText");
            if (elText != null) {
                elText.value = "";
            }
            this.filterText = "";
            this.executeSearch();
        },

        onFilterClicked: function(e) {
            var elText = dojo.byId(this.resultsHeaderId+"-filterText");
            if (elText != null) {
                this.filterText = dojo.trim(elText.value);
                this.executeSearch(e,1);
            }
        },

        onFilterTextChange: function(e) {
            var el = this;
            if ((el != null) && (el.value != null)) {
                this.filterText = dojo.trim(dojo.byId(el.value));
            }
        },

        onFilterTextKeyPress: function(e) {
            if (!e) e = window.event;
            var target = (window.event) ? e.srcElement : e.target;
            if (e) {
                var nKey = (e.keyCode) ? e.keyCode : e.which;
                if ((target != null) && (nKey == 13)) {
                    this.filterText = dojo.trim(target.value);
                    this.executeSearch();
                }
            }
        },

        onPageClicked: function(e) {
            if (!e) e = window.event;
            var el = (window.event) ? e.srcElement : e.target;
            if ((el != null) && (el.pageNumber != null)) {
                var nStartIndex = ((el.pageNumber - 1) * this.nItemsPerPage) + 1;
                this.executeSearch(e,nStartIndex);
            }
        },

        onTitleClicked: function(e) {
            if (!e) e = window.event;
            var target = (window.event) ? e.srcElement : e.target;
            if (target != null) {
                var qScope = target.parentNode;
                if (target.tagName.toLowerCase() == "img") qScope = qScope.parentNode;
                dojo.query("div.abstract",qScope).forEach(function(item) {
                    if ((typeof(item.style.display) == 'undefined') ||
                        (item.style.display == null) || (item.style.display == "")) {
                        item.style.display = "none";
                    } else if (item.style.display == "block") {
                        item.style.display = "none";
                    } else {
                        item.style.display = "block";
                    }
                });
                dojo.query("div.links",qScope).forEach(function(item) {
                    if ((typeof(item.style.display) == 'undefined') ||
                        (item.style.display == null) || (item.style.display == "")) {
                        item.style.display = "none";
                    } else if (item.style.display == "block") {
                        item.style.display = "none";
                    } else {
                        item.style.display = "block";
                    }
                });
            }
        },

        onTreeItemClicked: function(item) {
            this.query = item.query;
            if (this.resetFilterOnTreeItemClicked) {
                var elText = dojo.byId(this.resultsHeaderId+"-filterText");
                if (elText != null) {
                    elText.value = "";
                }
                this.filterText = "";
            }
            if (this.persist) {
                var tree = dijit.byId(this.treeId);
                var lastClickedId = tree.model.getIdentity(item);
                dojo.cookie(tree.cookieName+"_lastClickedId",lastClickedId,{expires:365});
            }
            this.executeSearch();
        },

        parseHiddenInt: function(sResponse,sPrefix,nDefault) {
            var sTemp = sPrefix;
            var iIndex = sResponse.indexOf(sTemp);
            if(iIndex != -1){
                sTemp = sResponse.substring(iIndex+sTemp.length);
                iIndex = sTemp.indexOf("\"");
                if(iIndex != -1){
                    var nTemp = parseInt(sTemp.substring(0, iIndex));
                    if(!isNaN(nTemp)){
                        return nTemp;
                    }
                }
            }
            return nDefault;
        },

        showMessage: function(msg,className) {
            var elMsg = document.createElement("div");
            elMsg.id = this.resultsHeaderId+"-messageText";
            if (className != null) {
                elMsg.className = className;
            }
            elMsg.appendChild(document.createTextNode(msg));
            dojo.byId(this.resultsBodyId).innerHTML = "";
            dojo.byId(this.resultsBodyId).appendChild(elMsg);
        }

    });

</script>

<script type="text/javascript">      
    var brwMain = null;
    function brwInit() {
        brwMain = new BrowseMainInno();
        brwMain.contextPath = "<%=brwContextPath %>";
        contextPath=brwMain.contextPath;
        brwMain.resources.filter = "<%=brwMsgBroker.retrieveMessage("catalog.browse.filter")%>";  
        brwMain.resources.clear = "<%=brwMsgBroker.retrieveMessage("catalog.browse.filter.clear")%>"; 
        brwMain.resources.summaryPattern = "<%=brwMsgBroker.retrieveMessage("catalog.browse.summaryPattern")%>";
        brwMain.resources.summaryPatternFiltered = "<%=brwMsgBroker.retrieveMessage("catalog.browse.summaryPattern.filtered")%>";
        brwMain.resources.pageSummaryPattern = "<%=brwMsgBroker.retrieveMessage("catalog.browse.pageSummaryPattern")%>";
        brwMain.resources.first = "<%=brwMsgBroker.retrieveMessage("catalog.browse.page.first")%>";
        brwMain.resources.last = "<%=brwMsgBroker.retrieveMessage("catalog.browse.page.last")%>";          
        brwMain.resources.noItemsSelected = "<%=brwMsgBroker.retrieveMessage("catalog.browse.noItemSelected")%>";
        brwMain.resources.noResults = "<%=brwMsgBroker.retrieveMessage("catalog.browse.noResults")%>";
        brwMain.init();
    }
  	
    function treeItemClicked(item){
        if (brwMain != null) {	  
            brwMain.onTreeItemClicked(item);                  
        }
    } 

    
    if (typeof(dojo) != 'undefined') {
        dojo.require("dojo.data.ItemFileReadStore");
        dojo.require("dijit.Tree");
        dojo.require("dijit.layout.SplitContainer");
        dojo.require("dijit.layout.ContentPane");    
        dojo.addOnLoad(brwInit);
    }   
</script>

<f:verbatim>

    <span id="browse-prompt" class="prompt"><%=brwPrompt %></span> 
    <div dojoType="dojo.data.ItemFileReadStore" url="<%=brwTocUrl %>" jsid="popStore" requestMethod="get"/>
    <div class="section" id="browse-splitter" dojoType="dijit.layout.SplitContainer"
         orientation="horizontal" sizerWidth="7" activeSizing="true" style="width: 100%; height: 500px;">	               	
        <div id="browse-toc" class="tundra" dojotype="dijit.Tree" store="popStore" labelattr="name" 
             sizeMin="30" sizeShare="30">			        			        	
            <script type="dojo/method" event="onClick" args="item">treeItemClicked(item);</script>			
        </div>
        <div id="browse-results" class="browse-results" dojoType="dijit.layout.ContentPane" sizeMin="30" sizeShare="70">
            <div id="browse-results-header">
                <%=brwMsgBroker.retrieveMessage("catalog.browse.noItemSelected")%>
            </div>
            <div id="browse-results-body"></div>
            <div id="innoRestLinks" style="display:none">
                Internet REST Links:
                <a id="innoGeorss" target="_blank">GEORSS</a>&nbsp;
                <a id="innoHtml" target="_blank">HTML</a>&nbsp;
                <a id="innoFragment" target="_blank">FRAGMENT</a>&nbsp;
                <a id="innoKml" target="_blank">KML</a><br/>
                Intranet REST Links:
                <a id="innoGeorssIntra" target="_blank">GEORSS</a>&nbsp;
                <a id="innoHtmlIntra" target="_blank">HTML</a>&nbsp;
                <a id="innoFragmentIntra" target="_blank">FRAGMENT</a>&nbsp;
                <a id="innoKmlIntra" target="_blank">KML</a>
            </div>
        </div>
    </div>
</f:verbatim>
<f:verbatim>
    <div id="dialog-form-member-tree" title="Collection members">
        <div style="font-weight: bold;">Please pan around if you are unable to see all members of the collection.</div>
        <div style="clear:both;"></div>
        <div id="infovis"></div>
    </div>
</f:verbatim>
<link rel="stylesheet" href="../../../catalog/js/jquery-ui/css/ui-lightness/jquery-ui.css" />
<script type="text/javascript" src="../../../catalog/js/jquery-ui/js/jquery.js"></script>
<script type="text/javascript" src="../../../catalog/js/jquery-ui/js/jquery-ui.js"></script>
<script type="text/javascript" src="../../../catalog/js/jquery-ui/js/ui/jquery.ui.core.js"></script>
<script type="text/javascript" src="../../../catalog/js/jquery-ui/js/ui/jquery.ui.widget.js"></script>
<script type="text/javascript" src="../../../catalog/js/jquery-ui/js/ui/jquery.ui.mouse.js"></script>
<script type="text/javascript" src="../../../catalog/js/jquery-ui/js/ui/jquery.ui.button.js"></script>
<script type="text/javascript" src="../../../catalog/js/jquery-ui/js/ui/jquery.ui.draggable.js"></script>
<script type="text/javascript" src="../../../catalog/js/jquery-ui/js/ui/jquery.ui.position.js"></script>
<script type="text/javascript" src="../../../catalog/js/jquery-ui/js/ui/jquery.ui.resizable.js"></script>
<script type="text/javascript" src="../../../catalog/js/jquery-ui/js/ui/jquery.ui.dialog.js"></script>
<script type="text/javascript" src="../../../catalog/js/jquery-ui/js/ui/jquery.effects.core.js"></script>
<script type="text/javascript" src="../../../catalog/js/jquery-ui/js/external/jquery.bgiframe-2.1.2.js"></script>
<!--[if IE]><script language="javascript" type="text/javascript" src="../js/Jit/Extras/excanvas.js"></script><![endif]-->
<script type="text/javascript" src="../../../catalog/js/Jit/jit-yc.js"></script>
<script language="javascript" type="text/javascript" src="../../../catalog/js/Jit/Extras/excanvas.js"></script>
<script type="text/javascript" src="../../../catalog/collection/js/manageBody.js"></script>