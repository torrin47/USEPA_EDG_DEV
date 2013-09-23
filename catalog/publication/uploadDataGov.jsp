<%-- 
    Document   : uploadDataGov
    Created on : Jan 3, 2011, 3:42:01 PM
    Author     : John Sievel
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
        <script type="text/javascript">
            function checkCsv() {
                var fname = document.getElementById("uploadCsv").value.toUpperCase();
                var per = fname.lastIndexOf(".");
                if (per != (fname.length-4)) {
                    alert("You can only submit .csv files");
                    return false;
                }
                if (fname.substring(per)!=".CSV") {
                    alert("You can only submit .csv files");
                    return false;                    
                }
                return true;
            }
        </script>
    </head>
    <body>
        <h1>Upload Data.Gov .csv File</h1>
        <form id="upload" method="post" action="UploadDataGov/" accept="text/csv"
              enctype="multipart/form-data" accept="text/csv" onsubmit="return checkCsv();">

            <input type="file" name="uploadCsv" id="uploadCsv"  accept="text/csv" size="50"/><br/><br/>
            <input id="upload:submit" type="submit" name="submit" value="Upload" />
        </form>

    </body>
</html>
