/*
 * Given uuids as the req parm, transform the dataGov xml to csv.
 */

package com.innovateteam.gpt;

import java.net.URL;
import java.net.URLConnection; 
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.esri.gpt.catalog.schema.MetadataDocument;    
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.security.principal.Publisher;   
import com.esri.gpt.framework.util.LogUtil; 
import com.esri.gpt.framework.util.Val;  
import java.util.logging.Level;   
import javax.xml.transform.*; 
import javax.xml.transform.stream.*; 
import java.io.*;  
import java.util.*;

/**
 *
 * @author jsievel
 */
public class DownloadMetadataAsCsv extends HttpServlet {
   
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

        //response.setContentType("text/csv; charset=UTF-8");
        String sUuids = Val.chkStr(request.getParameter("uuids"));
        String uuid = "DataGov Bulk Download";
        int commaPos = sUuids.indexOf(",");
        if (commaPos < 0)
            uuid = sUuids;
        response.setContentType("application/vnd.ms-excel; charset=UTF-8");
        //response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition","attachment; filename=\""+uuid+".CSV\"");
        PrintWriter out = response.getWriter();
        String sXml = "";
        String sErr = "";

      // read the XML string associated with the UUID
      if (sUuids.length() > 0) {
        //outputHeader(out);  JSS done in style sheet now
        String[] uuids = sUuids.split(",");
        try {
          for (int i=0; i<uuids.length; i++) {
              LogUtil.getLogger().log(Level.INFO,"Reading xml: "+uuids[i]);
              //sXml = readXml(request,uuids[i]);
              String xmlUrl = request.getScheme() + "://" + request.getServerName();
              if (request.getServerPort()>0)
                  xmlUrl += ":" + request.getServerPort();
              xmlUrl += request.getContextPath() + "/rest/document?id="+uuids[i];
              sXml = getUrlContents(xmlUrl,request);
              LogUtil.getLogger().log(Level.FINER,"xml: "+sXml);
              if (sXml.length() == 0) {
                out.println("Download failed: "+uuids[i]);
              } else {
                 LogUtil.getLogger().log(Level.INFO,"Xforming xml: "+uuids[i]);
                 String csvOut = "";
                  try {
                        String xslUrl = "/WEB-INF/classes/gpt/metadata/dataGovCsv.xsl";
                        // do a csv header for the first rec only
                        boolean suppresHeader = (i!=0);
                        csvOut = xform(sXml, xslUrl,uuids[i],suppresHeader);
                        out.print(csvOut);
                  } catch (Exception e) {
                      out.println("Could not tranform "+uuids[i]+": "+e.getMessage());
                  }                  
              }              
          }
        } catch (Exception e) {
          out.println("Download Failed. Still logged in?");
          LogUtil.getLogger().log(Level.SEVERE,"Metadata download failed",e);
        }
      }
      out.flush();
      out.close();
    }

// read the XML string associated with the UUID
private String readXml(HttpServletRequest request, String uuid) throws Exception {
  String sXml = null;
  RequestContext context = null;
  try {
    context = RequestContext.extract(request);
    Publisher publisher = new Publisher(context);
    LogUtil.getLogger().log(Level.FINE,"Retrieving xml for uuid "+uuid);
    MetadataDocument mdDoc = new MetadataDocument();
    sXml = mdDoc.prepareForDownload(context,publisher,uuid);
  } finally {
    if (context != null) {
      context.onExecutionPhaseCompleted();
    }
  }
  return sXml;
}

public String getUrlContents(String urlStr, HttpServletRequest request) {
    URL u;
    URLConnection c = null;
    InputStream is = null;
    BufferedReader br;
    String content = "";
    String s;

    try {
        u = new URL(urlStr);
        c = u.openConnection();
        setHeaders(c,request);
        c.connect();
        is = u.openStream();
        br = new BufferedReader(new InputStreamReader(c.getInputStream()));

        while ((s = br.readLine()) != null)
                content += s + "\n";
    } catch (Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        LogUtil.getLogger().log(Level.SEVERE,sw.toString());
    } finally {
        try {
            is.close();
        } catch (Exception e) {}
    }
    return content;
}

private void setHeaders(URLConnection c, HttpServletRequest req) {
    // copy all req headers to c
    Enumeration names = req.getHeaderNames();
    while (names.hasMoreElements()) {
        String name = (String) names.nextElement();
        Enumeration values = req.getHeaders(name);
        String allValues = "";
        while (values.hasMoreElements()) {
            if (allValues.length()>0)
                allValues += ",";
            allValues += (String) values.nextElement();
        }
        c.setRequestProperty(name, allValues);
    }
}

public String xform(String xmlStr, String xslUrl, String uuid, boolean suppressHeader)
    throws TransformerException, TransformerConfigurationException, FileNotFoundException, IOException {

    TransformerFactory tFactory = TransformerFactory.newInstance();
    InputStream xslStream = null;
    xslStream = this.getServletContext().getResourceAsStream(xslUrl);
    Transformer transformer = tFactory.newTransformer(new StreamSource(xslStream));
    transformer.setParameter("edgId", uuid);
    transformer.setParameter("suppressHeader", suppressHeader);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    transformer.transform(new StreamSource(new StringReader(xmlStr)), new StreamResult(baos));
    return(baos.toString());
}

void outputHeader(PrintWriter out) {
    out.println("Element Number,1,1.1,2,3,4,5,5.1,6,6.1,7,7.1,7.2,8,8.1,8.2,9,10,11,12,13,14,15,16,17,18,19,20,21,21.1,22,23,24,25,26,27,28,29,Downloadable file metadata (repeat this section for multiple download formats using multiple rows underneath initial submission.,D1,D2,D3,D4,Additional Statistical Metadata ,SM,SM.01,SM.02,SM.03,SM.04,SM.05,SM.06,SM.07,SM.08,SM.09,OGD Response specific metadata ,OGD,OGD.01,OGD.02,OGD.03,OGD.04,GeoDataGateway Specific Information (optional),GDG.ID,GDG.CT");
    out.println("Element,Unique ID,User Generated  ID, Title,Dataset Group Name,Description,Agency Name,Agency Short Name,Sub-Agency Name,Sub-Agency   Short Name,Contact Name,Contact Phone Number,Contact Email Address,Agency responsible for Information Quality,Compliance with Agency's Information Quality Guidelines,Privacy and Confidentiality ,Data.gov data category type,Subject area (Taxonomy),Specialized data category designation,Keywords ,Date released,Date updated,Agency Program URL,Agency Data Series URL,Collection mode,Frequency,Period of Coverage,Unit of analysis,Geographic scope,Geographic Granularity,Reference for Technical Documentation,Data dictionary/variable list,Data collection instrument ,Bibliographic citation for dataset,Number of Datasets Represented by this Submission,Additional Metadata,Dataset use requires a license agreement,Dataset license agreement URL,,Access point,Media Format,File size,File format ,,Statistical methodology ,Sampling,Estimation,Weighting  ,Disclosure avoidance,Questionnaire design,Series breaks,Non-response adjustment,Seasonal adjustment,\"Data quality (variances, CVs, CIs, etc)\",,Is this record a part of OGD submissions,Is this record listed in your agency open government plan,Is this a high value dataset,What makes this a high value dataset,How is this new,,GDG Metadata Id,GDG ContentType");
    out.println("\"Occurrence [min,max]\",\"[1,1]\",\"[1,1]\",\"[1,1]\",\"[0,1]\",\"[1,1]\",\"[1,1]\",\"[1,1]\",\"[1,1]\",\"[1,1]\",\"[1,1]\",\"[1,1]\",\"[1,1]\",\"[1,1]\",\"[1,1]\",\"[1,1]\",\"[1,1]\",\"[1,1]\",\"[1,1]\",\"[1,n]\",\"[1,1]\",\"[1,1]\",\"[1,1]\",\"[1,1]\",\"[1,n]\",\"[1,1]\",\"[1,n]\",\"[1,1]\",\"[0,n]\",\"[0,1]\",\"[1,n]\",\"[1,1]\",\"[1,n]\",\"[1,1]\",\"[0,1]\",\"[0,1]\",\"[1,1]\",\"[1,1]\",,\"[1,1]\",\"[1,1]\",\"[1,1]\",\"[1,1]\",,\"[0,n] \",\"[0,1] \",\"[0,1] \",\"[0,1] \",\"[0,1] \",\"[0,1] \",\"[0,1] \",\"[0,1] \",\"[0,1] \",\"[0,1] \",,\"[1,1]\",\"[1,1]\",\"[1,1]\",\"[1,1]\",\"[1,1]\",,\"[0,1]\",\"[0,1]\"");
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
