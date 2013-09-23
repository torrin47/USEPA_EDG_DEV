/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.innovateteam.gpt;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.params.HttpMethodParams;

/**
 *
 * @author Deewen
 */
public class batchUpdateAll {

    private final Connection con;
    private String restServiceURL;
    private static Logger logger;
    private PostMethod method;
    private HttpClient client;
    private String urlToLook, restServiceResponse;
    private Matcher matcher;
    private xmlParser objXmlParser;
    Pattern p_content_type;
    private PreparedStatement insertMetricsXpath = null, insertMetricsSummary = null, deleteMetricsSummary = null;
    private LuceneSearch luceneSearchObj;
    private HashMap allContentTypes = new HashMap();
    
    public batchUpdateAll(Logger loggerObj, Connection con) {
        this.allContentTypes.put("livedata", "Live Data and Maps");
        this.allContentTypes.put("downloadabledata", "Downloadable Data");
        this.allContentTypes.put("offlinedata", "Offline Data");
        this.allContentTypes.put("staticmapimage","Static Map Images");
        this.allContentTypes.put("other", "Other Documents");
        this.allContentTypes.put("application","Applications");
        this.allContentTypes.put("geographicservice","Geographic Services");
        this.allContentTypes.put("clearinghouse","Clearinghouse");
        this.allContentTypes.put("mapfiles","Map Files");
        this.allContentTypes.put("geographicactivities","Geographic Activities");
        this.allContentTypes.put("unknown","Resource");
        
        this.luceneSearchObj = null;
        this.restServiceURL = null;

        this.con = con;
        logger = loggerObj;
        this.p_content_type = Pattern.compile("<img src=\"((.)+)\" alt=\"((.)+)\" title=\"((.)+)\"/>");

        try {
            this.insertMetricsXpath = con.prepareStatement("INSERT INTO metrics_md_xpath (std,xpath) VALUES (?,?)");
            this.insertMetricsSummary = con.prepareStatement("INSERT INTO metrics_md_summary (uuid,xpath,val) VALUES (?,?,?)");
            this.deleteMetricsSummary = con.prepareStatement("DELETE FROM  metrics_md_summary WHERE uuid = ?");
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

    }

    /*
     * Class constructor
     * name                 updateContentType
     * @params
     *  restServiceURL      String                      URL of the rest service eg:https://edg.epa.gov/metadata/rest/document
     *  logger              java.util.logging.Logger    logger
     *  con                 sql.Connection              database connection resource
     *  frequency           String                      period compliant with geoportal sever scheduler setting eg: 2[day], 1[hour]
     */
    public batchUpdateAll(String restServiceURL, Logger loggerObj, Connection con) {
        this(loggerObj, con);
        this.restServiceURL = restServiceURL;
    }

    public batchUpdateAll(File luceneIndexDir, Logger loggerObj, Connection con) {
        this(loggerObj, con);
        this.luceneSearchObj = new LuceneSearch(luceneIndexDir);
    }

    public void executePreparedStatement(PreparedStatement ps, ArrayList params) {
        try {
            ps.setString(1, params.get(0) == null ? null : params.get(0).toString());
            ps.setString(2, params.get(1) == null ? null : params.get(1).toString());
            ps.executeUpdate();
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * 
     * @param   docuuid         String document id
     * @return  String          content type of the metadata as extracted from the rest service
     */
    public String getContentType(String docuuid) {
        if (this.restServiceURL != null) {
            this.urlToLook = this.restServiceURL + "?f=html&id=" + docuuid;
            this.method = new PostMethod();
            this.client = new HttpClient();
            try {
                this.method.setURI(new URI(this.urlToLook, false));
                this.method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));

                if (this.client.executeMethod(this.method) != HttpStatus.SC_OK) {
                    return null;
                } else {
                    this.restServiceResponse = new String(this.method.getResponseBody());
                    this.matcher = this.p_content_type.matcher(this.restServiceResponse);
                    if (this.matcher.find()) {
                        return matcher.group(3).toString();
                    }
                }

            } catch (URIException ex) {
                logger.log(Level.SEVERE, null, ex);
                return null;
            } catch (NullPointerException ex) {
                logger.log(Level.SEVERE, null, ex);
                return null;
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
                return null;
            }
        } else {
            //lucene index search
            String contentType = this.luceneSearchObj.search(docuuid);
            if(this.allContentTypes.containsKey(contentType)){
                return this.allContentTypes.get(contentType).toString();
            }else{
                return "Unknown";
            }
        }
        return null;
    }

    /*
     * This function reads the XML content for given docuuid and parses it 
     * @param 
     *      String      docuuid         Document id
     */
    public void parseResourceDataXML(String docuuid, String xml) {
        String md_std;
        try {
            this.objXmlParser = new xmlParser(xml);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        node root = objXmlParser.traverseNodes();

        if (root.getNode().getNodeName().equalsIgnoreCase("dataGov")) {
            md_std = "dataGov";
        } else {
            md_std = "csdgm";
        }

        try {
            this.deleteMetricsSummary.setString(1, docuuid);
            this.deleteMetricsSummary.execute();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        this.doUpdate(root, '/' + root.getName() + '[' + root.getIndex() + ']', md_std, docuuid);

    }

    /*
     * This function loops through the xml nodes and updates the metrics_md_* tables
     */
    private void doUpdate(node root, String xpath, String md_std, String docuuid) {

        ArrayList list = root.getChildren();

        Iterator it = list.iterator();
        String val;
        while (it.hasNext()) {
            node sub = (node) it.next();

            val = sub.getVal().trim();
            val = val.replaceAll("\\n|\\r", "");

            if (!(val.trim().equals("")) && val.length() != 0) {
                try {
                    this.insertMetricsXpath.setString(1, md_std);
                    this.insertMetricsXpath.setString(2, xpath + '/' + sub.getName() + '[' + sub.getIndex() + ']');
                    this.insertMetricsXpath.execute();
                } catch (Exception ex) {
                    if (ex.getMessage().indexOf("PK_METRICS_MD_XPATH") == -1) {
                        logger.log(Level.SEVERE, null, ex);
                    }
                }
                try {
                    this.insertMetricsSummary.setString(1, docuuid);
                    this.insertMetricsSummary.setString(2, xpath + '/' + sub.getName() + '[' + sub.getIndex() + ']');
                    this.insertMetricsSummary.setString(3, sub.getVal());
                    this.insertMetricsSummary.execute();
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            } else {
                this.doUpdate(sub, xpath + '/' + sub.getName() + '[' + sub.getIndex() + ']', md_std, docuuid);
            }
        }
    }

    /*
     * This function loops through all the records in the gpt_resouce that were modified since last run
     * and updates the content_type by reading the contents returned from the web service identified by 
     * restServiceURL
     */
    public final void doUpdate() {
        logger.log(Level.INFO, "Update procedure initiated.");
        int total = 0, hit = 0, miss = 0;
        String docuuid, contentType, logInfo;
        ArrayList params = new ArrayList();

        PreparedStatement psUpdate = null;
        try {
            psUpdate = this.con.prepareStatement("UPDATE gpt_resource SET content_type=? WHERE docuuid = ?");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        PreparedStatement ps = null;
        try {
            ps = this.con.prepareStatement("SELECT gptres.docuuid,grd.xml FROM gpt_resource gptres LEFT JOIN gpt_resource_data grd ON (gptres.docuuid=grd.docuuid)");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                if (Thread.interrupted()) {
                    logger.log(Level.SEVERE, "Thread was interreupted! All tasks aborted.");
                    return;
                }
                total++;

                docuuid = rs.getString(1);
                contentType = this.getContentType(docuuid);
                this.parseResourceDataXML(rs.getString(1), rs.getString(2));//PARSE XML AND UPDATE THE CONTENT IN THE metrics_md_* tables
                if (contentType != null) {
                    hit++;
                } else {
                    miss++;
                }
                logInfo = total + ") docuuid: " + docuuid + " Content Type :" + contentType;
                logger.log(Level.INFO, logInfo);
                params.clear();
                params.add(contentType);
                params.add(docuuid);
                this.executePreparedStatement(psUpdate, params);

            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        logger.log(Level.INFO, "Update procedure completed.");
        logInfo = hit + " records updated, " + miss + " records didnot return valid content type.";
        logger.log(Level.INFO, logInfo);

    }
}
