<%-- 
    Document   : visualizationCollection
    Created on : Nov 2, 2012, 3:39:14 PM
    Author     : Netty Gouw
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Visualize Collection</title>
        <% String contextPath = request.getContextPath();%>
        <link rel="stylesheet" type="text/css" href="http://serverapi.arcgisonline.com/jsapi/arcgis/2.0/js/dojo/dijit/themes/tundra/tundra.css">
        <link rel="stylesheet" type="text/css" href="<% out.print(contextPath);%>/catalog/skins/themes/blue/main.css"  />
        <link rel="stylesheet" type="text/css" href="<% out.print(contextPath);%>/catalog/skins/themes/blue/preview.css"  />
        <link rel="icon" type="image/x-icon"   href="<% out.print(contextPath);%>/catalog/images/favicon.ico" />
        <link rel="shortcut icon" type="image/x-icon" href="<% out.print(contextPath);%>/catalog/images/favicon.ico" />
        <style type="text/css">
            .columnsTable td {vertical-align: top;}

            .grid td, .grid th{
                line-height: 18px;
            }
        </style>
        <link rel="stylesheet" href="<% out.print(contextPath);%>/catalog/js/jquery-ui/css/ui-lightness/jquery-ui.css" />
        <link rel="stylesheet" href="http://serverapi.arcgisonline.com/jsapi/arcgis/2.0/js/esri/dijit/css/InfoWindow.css" />
        <link rel="stylesheet" href="http://serverapi.arcgisonline.com/jsapi/arcgis/2.0/css/jsapi.css" />

    </head>
    <body>
         <!--<div>TESTING</div>-->
        <div id="dialog-form-member-tree" title="Collection members">
            <div style="font-weight: bold;">Please pan around if you are unable to see all members of the collection.</div>
            <div style="clear:both;"></div>
            <div id="infovis"></div>
        </div>
        <!--<div>SOMETHING</div>-->
    </body>
</html>

<script>var contextPath = "<%=contextPath%>";</script>

<script type="text/javascript" src="../js/jquery-ui/js/jquery.js"></script>
<script type="text/javascript" src="../js/jquery-ui/js/jquery-ui.js"></script>
<script type="text/javascript" src="../js/jquery-ui/js/ui/jquery.ui.core.js"></script>
<script type="text/javascript" src="../js/jquery-ui/js/ui/jquery.ui.widget.js"></script>
<script type="text/javascript" src="../js/jquery-ui/js/ui/jquery.ui.mouse.js"></script>
<script type="text/javascript" src="../js/jquery-ui/js/ui/jquery.ui.button.js"></script>
<script type="text/javascript" src="../js/jquery-ui/js/ui/jquery.ui.draggable.js"></script>
<script type="text/javascript" src="../js/jquery-ui/js/ui/jquery.ui.position.js"></script>
<script type="text/javascript" src="../js/jquery-ui/js/ui/jquery.ui.resizable.js"></script>
<script type="text/javascript" src="../js/jquery-ui/js/ui/jquery.ui.dialog.js"></script>
<script type="text/javascript" src="../js/jquery-ui/js/ui/jquery.effects.core.js"></script>
<script type="text/javascript" src="../js/jquery-ui/js/external/jquery.bgiframe-2.1.2.js"></script>
<!--[if IE]><script language="javascript" type="text/javascript" src="../js/Jit/Extras/excanvas.js"></script><![endif]-->
<script type="text/javascript" src="../js/Jit/jit-yc.js"></script>
<script type="text/javascript" src="../../catalog/collection/js/manageBody.js"></script>

<!--<script type="text/javascript" src="../../catalog/collection/js/homeBody.js"></script>-->
<script type="text/javascript">
    function openVisualizeTree(){
        var url = window.location.toString();
        col_id=url.split('?')[1];
              
        openTreeChart(col_id);
    }
            
</script>

<script>openVisualizeTree();</script>





