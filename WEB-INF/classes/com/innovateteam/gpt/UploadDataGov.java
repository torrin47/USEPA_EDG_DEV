/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.innovateteam.gpt; 

import com.esri.gpt.catalog.publication.UploadRequest;
import com.esri.gpt.catalog.publication.PublicationRequest; 
import com.esri.gpt.catalog.publication.ValidationRequest;
import java.util.*;
import java.io.*; 
import java.util.logging.Logger;
import javax.servlet.ServletException;    
import javax.servlet.http.HttpServlet;  
import javax.servlet.http.HttpServletRequest;      
import javax.servlet.http.HttpServletResponse; 
import com.esri.gpt.framework.context.RequestContext;  
import com.esri.gpt.framework.security.principal.Publisher;
import com.esri.gpt.framework.security.identity.NotAuthorizedException; 
import com.esri.gpt.catalog.schema.SchemaException;
import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.servlet.*;
import org.apache.commons.fileupload.disk.*;
/**
 *
 * @author John Sievel
 */
public class UploadDataGov extends HttpServlet {
    private static Logger log = Logger.getLogger("com.esri.gpt");
	private static Properties tagNames = new Properties();
    public static final int maxRepeatedRows = 1000;  // number of rows that can repeat
    public static final int maxCols = 1000;  // max number of cells in a row

	class ColDesc   {
        String elementNumber;
        String elementTitle;
        String occurrence;
        String tagName;

		ColDesc(String theElementNumber, String theElementTitle, String theOccurrence) {
            if (tagNames.containsKey(theElementNumber)) {
                elementNumber = theElementNumber;
                tagName = tagNames.getProperty(theElementNumber);
                elementTitle = theElementTitle;
                occurrence = theOccurrence;
            } else {
                elementNumber = null;
                tagName = null;
            }
		}
    }

    class Group {
        String elementNumber;
        String tagName;
        int startCol;
        int endCol;

        Group(String theElementNumber, String startElementNumber,
                String endElementNumber, ColDesc[] colDescs) {
            tagName = tagNames.getProperty(theElementNumber);
            startCol = 0;
            endCol = 0;
            for (int i=0; i<colDescs.length; i++) {
                if (colDescs[i].elementNumber != null) {
                    if (colDescs[i].elementNumber.equals(startElementNumber))
                        startCol = i;
                    else if (colDescs[i].elementNumber.equals(endElementNumber))
                        endCol = i;
                }
            }
        }

        boolean isEmpty(String[] cells) {
            if ((startCol==0) && (endCol==0))
                return true;        // the group is not even in the csv
            if (endCol > (cells.length-1))  // avoid non-existant cells at end
                endCol = cells.length-1;
            for (int i=startCol; i<=endCol; i++) {
                if (cells[i].trim().length() != 0) {
                    //log.fine("found non-empty at i "+i+"   cells[i] "+cells[i]);
                    return false;
                }
            }
            return true;
        }

        String buildXml(String[] cells, ColDesc[] colDescs) {
            //log.fine("in buildXml for group "+tagName+" startCol "+Integer.toString(startCol)+" endCol"+Integer.toString(endCol));
            if ((startCol==0) && (endCol==0))
                return "";  // group not in csv
            if (endCol > (cells.length-1))  // avoid non-existant cells at end
                endCol = cells.length-1;
            String xml = "\t<" + tagName + ">\n";
            for (int i=startCol; i<=endCol; i++) {
                xml += "\t\t<" + colDescs[i].tagName + ">" +
                        cells[i] +
                        "</" + colDescs[i].tagName + ">\n";
            }
            xml += "\t</" + tagName + ">\n";
            return xml;
        }
    }


    @Override
    public void init() {
        try {
            log.fine("in init");
            InputStream inStream = this.getServletContext().getResourceAsStream(
                    "/WEB-INF/classes/gpt/metadata/data-gov/dataGovElement2Tag.properties");
            log.fine("inStream "+inStream);
            tagNames.load(inStream);
            log.info("Initialization complete.");
        } catch (Exception e) {
            log.severe("Initialization failed."+e.getMessage());
        }
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        // handle mutipart csv file upload
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        String innoMsgs = "";
        boolean errors = false;
        if (!isMultipart) {
            innoMsgs = "ERROR: File is not multipart.";
            returnMsgs(out,innoMsgs,true);
            return;
        }
        InputStream in = null;  // stream of csv file

        // Create a factory for disk-based file items
        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        String xml = "";
        boolean validateOnly = false;
        
        try {
            List items = upload.parseRequest(request);
            Iterator iter = items.iterator();
            String onBehalfOf = "";
            String filename = "";
            while (iter.hasNext()) {
                FileItem item = (FileItem) iter.next();
                if (item.isFormField()) {
                    //processFormField(item);
                    String name = item.getFieldName();
                    String value = item.getString();
                    //log.fine("form field "+name+" has value "+value);
                    if (name.equalsIgnoreCase("upload:validate") &&
                            value.equalsIgnoreCase("validate")) {
                        log.fine("setting validateOnly = true");
                        validateOnly = true;
                    }
                    if (name.equalsIgnoreCase("upload:onBehalfOf") && value != null) {
                        log.fine("setting onBehalfOf = "+value);
                        onBehalfOf = value;
                    }
                } else {
                    String contentType = item.getContentType();
                    log.fine("contentType "+contentType);
                    in = item.getInputStream();
                    filename = item.getName();
                }
            }
            //InputStream in = this.getServletContext().getResourceAsStream(
            //       "/WEB-INF/classes/gpt/metadata/TRI Test Data.csv");

            BufferedReader csv = new BufferedReader(new InputStreamReader(in));

            ColDesc[] colDescs = null;
            Group[] groups = null;
            try {
                colDescs = parseHeaders(csv);
                groups = new Group[5];
                groups[0] = new Group("BSC","1","29",colDescs);
                groups[1] = new Group("DD","D1","D4",colDescs);
                groups[2] = new Group("SM","SM.01","SM.09",colDescs);
                groups[3] = new Group("OGDG","OGD","OGD.04",colDescs);
                groups[4] = new Group("GDGG","GDG.ID","GDG.CT",colDescs);
            } catch (Exception e) {
                log.fine("Exception parsing header: "+e.getMessage());
                innoMsgs = "ERROR: Could not parse header.";
                returnMsgs(out,innoMsgs,true);
                return;
            }

            String[][] rows = new String[maxRepeatedRows][];
            int numRows = 0;
            String[] cells = getCells(csv);
            while (cells != null) {
                rows[numRows++] = cells;
                cells = getCells(csv);
                while ((cells != null) && (groups[0].isEmpty(cells))) {
                    // is a repeating row, since basic is empty
                    rows[numRows++] = cells;
                    //log.fine("numRows "+numRows);
                    cells = getCells(csv);
                }
                xml = buildXml(rows, numRows, colDescs, groups);
                log.finest("xml: "+xml);
                //out.println(xml);
                // the sourceFileName is the uuid, which is pointed to by groups[4].startCol
                String sourceFileName = "";
                int idCol = groups[4].startCol;
                if ((idCol>0) && (idCol <= rows[0].length))
                    sourceFileName = rows[0][idCol].trim()+".xml";
                log.fine("sourceFileName from csv"+sourceFileName);  // from spreadsheet col
                log.fine("filename "+filename);  // name from browser file input
                sourceFileName = getSourceFileName(sourceFileName,filename);
                log.fine("using sourceFileName "+sourceFileName);  // name used in upload
                String result = uploadXml(out, xml, request, response, sourceFileName,validateOnly,onBehalfOf);
                log.fine("result: "+result+" for xml of len: "+xml.length());
                if ((result.contains("Not Valid")) || (result.contains("ERROR")))
                    errors = true;
                //out.println(xml);
                innoMsgs += rows[0][groups[0].startCol+1]+" "+result+"<br/>";  // print id and result
                numRows = 0;
                rows[0] = cells;
            }
            returnMsgs(out,innoMsgs,errors);
        } catch (Exception e) {
            String stack = "";
            StackTraceElement[] els = e.getStackTrace();
            for (int i=0; i<els.length; i++) {
                stack += els[i].toString()+"\r\n";
            }
            log.severe("Exception "+e);
            log.severe(stack);
            innoMsgs += "ERROR: "+e.getMessage();
            returnMsgs(out,innoMsgs,true);
            return;
        } finally {
            out.close();
            try {in.close();} catch (Exception ee) {}
            in = null;
        }
    }

    void returnMsgs(PrintWriter out, String msgs, boolean errors) {
        String head = "<html>"+
            "<script type=\"text/javascript\">"+
            "function init() {"+
            "if(top.displayInnoMsgs) top.displayInnoMsgs();"+
            "}"+
            "window.onload=init;"+
            "</script>"+
            "<body>";
        String styleDiv = errors ? "<div class=\"errorMessage\">" : "<div class=\"successMessage\">";
        String trailer = "</div></body></html>";
        out.print(head+styleDiv+msgs+trailer);

    }

    String buildXml(String[][] rows, int numRows, ColDesc[] colDescs, Group[] groups) {
        String xml = "<dataGov>\n";
        //log.fine("numRows "+numRows);
        
        for (int group=0; group<4; group++) {
            //log.fine("group "+group);
            xml += groups[group].buildXml(rows[0], colDescs);
            //log.fine("xml" +xml);
            for (int row=1; row<numRows; row++) {
                if (!groups[group].isEmpty(rows[row]))
                    xml += groups[group].buildXml(rows[row], colDescs);
            }
        }
        xml += buildGDGXml(rows,numRows,colDescs,groups);

        xml += "</dataGov>\n";
        return xml;
    }

    /**
     * Build the xml for the gdg section.
     */
    String buildGDGXml(String[][] rows, int numRows, ColDesc[] colDescs, Group[] groups) {
        String xml = "\t<gdg>\n\t\t<id>";
        int idCol = groups[4].startCol;
        if ((idCol>0) && (idCol <= rows[0].length)) 
            xml += rows[0][idCol];  // output id if in csv, otherwise empty
        xml += "</id>\n\t\t<contentType>";
        int ctCol = groups[4].endCol;
        if ((ctCol>0) && (ctCol <= rows[0].length)) 
            xml += rows[0][ctCol];  // output contentType if in csv
        else
            xml += computeContentType(rows,numRows,colDescs,groups);  // otherwise compute it
        xml += "</contentType>\n\t</gdg>\n";
        //log.fine("gdg xml: "+xml);
        return xml;
    }
    
    /**
     * Map data.gov data to a gdg congtentType as follows:
     *  Applications - basic.dataGovCatalogType=='Tool Catalog'
     *  Offline Data - basic.dataGovCatalogType=='Raw Catalog' and there are no downloadableFiles
     *  Live Map Services - any downloadableFile.accessPoint contains "service="
     *  Downloadable Data - downloadableFiles that do not fall into any other category
     */
    String computeContentType(String[][] rows, int numRows, ColDesc[] colDescs, Group[] groups) {
        // get col of downloadableFile.accessPoint and basic.dataGovCatalogType
        int apCol = 0;
        int ctCol = 0;
        for (int i=0; i<colDescs.length; i++) {
            if (colDescs[i].elementNumber != null) {
                if (colDescs[i].elementNumber.equals("D1"))  // accessPoint
                    apCol = i;
                if (colDescs[i].elementNumber.equals("9"))  // dataGovCatalogType
                    ctCol = i;
            }
        }
        log.finer("apCol "+apCol+",   ctCol "+ctCol);
        if ((apCol == 0) || (ctCol == 0))
            return "";    // don't have enough to classify
        if (rows[0][ctCol].equalsIgnoreCase("Tool Catalog"))
            return "Applications";

        // look thru all accessPoint rows to see if they are all empty, or any contain "service="
        boolean empty = true;
        boolean service = false;
        for (int i=0; i<numRows; i++) {
            if (rows[i][apCol].trim().length()>0)
                empty = false;
            if (rows[i][apCol].toLowerCase().contains("service="))
                service = true;
        }
        if (empty)
            return "Offline Data";
        if (service)
            return "Live Data and Maps";
        // none of the above, assume Downloadable Data
        return "Downloadable Data";
    }

    ColDesc[] parseHeaders(BufferedReader csv) throws IOException {

        String[] elementNumbers = getCells(csv);
        String[] elementTitle = getCells(csv);
        String[] occurrence = getCells(csv);

        if ((elementNumbers==null) || (elementTitle==null) || (occurrence==null))
            return null;
        ColDesc[] colDescs = new ColDesc[elementNumbers.length];
        for (int i=0; i<elementNumbers.length; i++) {
            colDescs[i] = new ColDesc(elementNumbers[i],elementTitle[i],occurrence[i]);
        }
        return colDescs;
    }

    String[] getCells(BufferedReader csv) throws IOException {

        String rec = csv.readLine();
        if (rec==null)
            return null;
        log.finer("rec: "+rec);
        //log.fine("len: "+rec.length()/*+"  0: "+Integer.toString(rec.codePointAt(0))+"  1: "+Integer.toString(rec.codePointAt(1))+
        //        "  2: "+Integer.toString(rec.codePointAt(2))*/+rec.substring(0,75));
        //if (rec.trim().length()<3)
        //    return null;
        //log.fine("rec: "+rec);
        String[] cells = new String[maxCols];
        // cells separated by "," but can be surrounded by """
        int cnt=0;
        int start=0;
        int end=0;

        while ((cnt<(maxCols-1)) && (start < rec.length())) {
            if (rec.charAt(start)=='"') {
                start++;
                end = locateQuote(rec, start);
                while (end<0) {
                    // could not find ", so keep reading until find closing "
                    String newRec = csv.readLine();
                    if (newRec != null) {
                        log.finer("newRec: "+newRec);
                        end = locateQuote(newRec,0);
                        if (end >= 0)
                            end += rec.length();
                        rec += newRec;
                    } else
                        end = rec.length();
                }
            } else {
                end = rec.indexOf(',', start);
            }
            if (end < 0)
                end = rec.length();
            log.finer("cell "+cnt+"   "+rec.substring(start, end));
            cells[cnt++] = rec.substring(start, end);
            // new start is after comma if end is "
            if (end < (rec.length()-1)) {
                if (rec.charAt(end)=='"')
                    start = end + 2;
                else
                    start = end + 1;
            } else
                start = rec.length();
            log.finer("new start: "+start);
        }
        //cells[cnt++] = "";
        String[] retCells = new String[cnt];
        for (int i=0; i<cnt; i++)
            retCells[i] = escape(cells[i]);
        log.fine("retCells.length: "+retCells.length);
        return retCells;
    }

    private int locateQuote(String rec, int start) {
        // skip "", which is how excel escapes quotes, and find the first  "
        int end = rec.indexOf("\"", start);
        while (end >= 0) {
            log.finer("start "+start+"   end "+end+"   rec "+rec);
            if ((end+1) < rec.length()) {
                if (rec.charAt(end+1)=='"')
                    end = rec.indexOf("\"", end+2);
                else
                    return end;
            } else
                return end;
        }
        return end;
    }

    public String escape(String in) {
        String out = in.replaceAll("\"\"", "\"").replaceAll("&", "&amp;").replaceAll("\"", "&quot;").replaceAll("'", "&apos;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
        //log.fine("escaped cell: "+out);
        return out;
    }
    
    /* Determine what sourceFileName to use in the upload. If there is
     * a name in the csv, use it. Otherwise, if the name from the
     * browser looks like a docuuid, use it. Otherwise return empty.
     */
    private String getSourceFileName(String fromCsv, String fromBrowser) {
        if (fromCsv.length()>0)
            return fromCsv;
        int lbPos = fromBrowser.indexOf("{");
        int rbPos = fromBrowser.indexOf("}");
        if ((lbPos>=0) && (rbPos>=0)) {
            String fromBrowserFiltered = fromBrowser.substring(lbPos,rbPos);
            String[] parts = fromBrowserFiltered.split("-");
            if (parts.length==5)
                return fromBrowser;
        }
        return "";
    }

    protected String uploadXml(PrintWriter out, String xml, HttpServletRequest request, HttpServletResponse response,
            String sourceFileName, boolean validateOnly, String onBehalfOf) throws Exception {

        try {
            RequestContext requestContext = RequestContext.extract(request);
            Publisher pub = null;
            if (onBehalfOf.length()>0)
                pub = new Publisher(requestContext,onBehalfOf);
            else
                pub = new Publisher(requestContext);
            if (validateOnly) {
                ValidationRequest validate = new ValidationRequest(requestContext,sourceFileName,xml);
                try {
                    validate.verify();
                    return "Valid";
                } catch (SchemaException se) {
                    return "Not Valid";
                }
            } else {
                UploadRequest upload = new UploadRequest(requestContext,pub,sourceFileName,xml);
                upload.publish();
                if (upload.getPublicationRecord().getWasDocumentReplaced())
                    return "Replaced "+sourceFileName;
                return("Uploaded");
            }
        } catch (NotAuthorizedException ex) {
            String innoMsg = "ERROR: "+ex.getMessage();
            return innoMsg;
        }
    }


    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
