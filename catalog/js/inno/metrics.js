/* 
 * Code to support metrics measurement
 */

function innoRecordClick(event) {
    var uuid = innoGetAnchorUuid(event.target);
    if (uuid.length==0) {
        // try to get uuid from page instead
        var uuidHidden = dojo.byId("innoUuid");
        //console.debug("uuidHidden: "+uuidHidden);
        if ((uuidHidden != null) && (uuidHidden != "undefined"))
            uuid = uuidHidden.value;
    }
    //alert("Click: "+event.target.href+" uuid: "+uuid);
    var href = event.target.href;
    //console.debug("href: "+href);
    //console.debug("event.target.innerHTML: "+event.target.innerHTML);
    if (event.target.innerHTML=="Preview") {
        // For preview, href contains url within it
        var urlPos = href.indexOf("&url=");
        //console.debug("urlPos: "+urlPos);
        if (urlPos >= 0) {
            var urlEnd = href.indexOf("&",urlPos+5);
            //console.debug("urlEnd: "+urlEnd);
            if (urlEnd < 0)
                urlEnd = href.length;
            href = decodeURIComponent(href.substring(urlPos+5,urlEnd));
            //console.debug("href: "+href);
        }
    }
    var contextPath = "/metadata";
    var ctxEnd = location.pathname.indexOf("/",1);
    if (ctxEnd >= 0)
        contextPath = location.pathname.substring(0,ctxEnd);
    
    var url = contextPath + "/RecordMetrics?href="+encodeURIComponent(href)+
        "&uuid="+uuid;
    //alert("url: "+url);
    dojo.xhrGet({
        url: url,
        preventCache: true,
        handleAs: "text",
        load: function() {
            //alert("returned from web service");
        },
        error: function(err) {
           //alert("error: "+error);
        }
    });
    return true;
}

function innoGetAnchorUuid(node) {
    var parent = node.parentNode;
    //console.debug("parentId: "+parent.id);
    var childNodes = parent.childNodes;
    for (var i=0; i<childNodes.length; i++) {
        //console.debug("parentId: "+parent.id+"   childHtml: "+childNodes[i].innerHTML+"   childHref: "+childNodes[i].href);
        if (childNodes[i].innerHTML=="Details") {
            var href = childNodes[i].href;
            var uuidPos = href.indexOf("uuid=");
            var uuid = "";
            if (uuidPos>=0)
                uuid = href.substring(uuidPos+5);
            return uuid;
        }
    }
    return "";
}

// list of anchors (innerHTML) that need to be measured
var innoMeasuredAnchors = ["Open","Preview","Website","ArcMap","Globe (.kml)","Globe (.nmf)"];

function innoHookUpAnchor(node) {
    for (var i=0; i<innoMeasuredAnchors.length; i++) {
        if (node.innerHTML==innoMeasuredAnchors[i]) {
            //console.debug("hooking up "+node.innerHTML);
            dojo.connect(node,'onclick',innoRecordClick);
        }
    }
}

function innoHookUpAnyAnchor(node) {
        //console.debug("hooking up "+node.innerHTML);
        dojo.connect(node,'onclick',innoRecordClick);
}

function innoHookupAnchors(doc) {
    // install onClick on all anchors that are to be measured
    //alert("in innoHookupAnchors");
    if ((doc=="undefined") || (doc==null))
        dojo.query("a").forEach(function(node) {innoHookUpAnchor(node);});
    else
        dojo.query("a",doc).forEach(function(node) {innoHookUpAnchor(node);});
}

function innoHookUpFrame(frameNode) {
    //alert("innoHookUpFrame");
    var doc = (frameNode.contentWindow || frameNode.contentDocument);
    if (doc.document)
        doc=doc.document;

    innoHookupAnchors(doc);
}

function innoHookupAllAnchors(doc) {
    // install onClick on all anchors that are to be measured
    //alert("in innoHookupAllAnchors");
    if ((doc=="undefined") || (doc==null)) {
        //console.debug("no doc");
        dojo.query("a").forEach(function(node) {innoHookUpAnyAnchor(node);});
    } else {
        //console.debug("with doc "+doc.nodeName);
        dojo.query("a",doc).forEach(function(node) {innoHookUpAnyAnchor(node);});
    }
}



