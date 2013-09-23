/*
 * Servlet to provide the file "browse-catalog.xml, which is used by the browse page.
 * The existing browse-catalog.xml page will be output, but the comment that contains
 * the string "InsertOwnersHere" will be replaced by the xml to define the owners.
 * Note: Only users that contain the filterUserStr configed parm ("cn=gis" on prod and staging)
 * will be output.
 */
package com.innovateteam.gpt;

import com.esri.gpt.framework.context.RequestContext;
import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.dom.DOMResult;
import org.w3c.dom.*;
import java.util.logging.Logger;

/**
 *
 * @author jsievel
 */
public class BrowseOwners extends HttpServlet {

    private static Logger log = Logger.getLogger("com.esri.gpt");
    private DOMResult dom;
    public String filterUserStr = null;
    private innoCollection innoCol = null;

    @Override
    public void init() {

        buildHierarchyDOM();
        filterUserStr = this.getServletConfig().getInitParameter("filterUserStr");
        if ((filterUserStr != null) && (filterUserStr.length() > 0)) {
            log.info("filterUserStr: " + filterUserStr);
        } else {
            log.severe("Init failed, no filterUserStr");
        }


    }

    private void buildHierarchyDOM() {
        TransformerFactory factory = TransformerFactory.newInstance();
        StreamSource src = new StreamSource(this.getServletContext().getResourceAsStream("/WEB-INF/classes/gpt/search/browse/ownerHierarchy.xml"));
        log.info("initializing src from stream " + src);
        try {
            Transformer t = factory.newTransformer();
            dom = new DOMResult();
            t.transform(src, dom);
            // now go thru tree, setting up the query attribute for each node
            Node tree = dom.getNode();
            NodeList children = tree.getChildNodes();
            log.info("dom tree contains " + children.getLength() + " nodes");
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) child;
                    String query = computeQuery(e);
                    e.setAttribute("query", query);
                }
            }
        } catch (Exception e) {
            log.severe("Could not init ownerHierarchy because exception thrown:");
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            log.severe(sw.toString());
        }
    }

    /*
     * If the element contains an attribute 'makeOnly' with a value of 'true',
     * create a child element before any existing children which will display a node
     * which will display only the elements records.
     * Compute the query attribute, which is the appended result of all of the children
     * appended to this element name. If noQuery=true. then do not generate a query term
     * for this element.
     */
    String computeQuery(Element e) {
        String query = "";
        if (!(e.getAttribute("noQuery").equalsIgnoreCase("true"))) {
            query = e.getAttribute("name");
        }
        String makeOnly = e.getAttribute("makeOnly");
        boolean madeOnly = false;
        NodeList children = e.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                if (makeOnly.equalsIgnoreCase("true") && !madeOnly) {
                    // need to make an ...-Only node and populate it
                    String onlyTagName = e.getTagName() + "-Only";
                    Element only = ((Document) dom.getNode()).createElement(onlyTagName);
                    only.setAttribute("name", e.getAttribute("name") + "-Only");
                    only.setAttribute("query", e.getAttribute("name"));
                    e.insertBefore(only, child);
                    i++;
                    madeOnly = true;
                }
                if (query.length() > 0) {
                    query += ",";
                }
                query += computeQuery((Element) child);
            }
        }
        log.info("setting query for " + e.getNodeName() + "   " + query);
        e.setAttribute("query", query);
        return query;
    }

    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/xml;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            // get exiting file, and output, looking for where to insert owners.
            BufferedReader in = new BufferedReader(new InputStreamReader(this.getServletContext().getResourceAsStream("/WEB-INF/classes/gpt/search/browse/browse-catalog.xml")));
            log.fine("in: " + in.toString());
            String rec = null;
            while ((rec = in.readLine()) != null) {
                if (rec.contains("InsertOwnersHere")) {
                    outputOwners(out);
                } else {
                    out.println(rec);
                }
            }
        } finally {
            out.close();
        }
    }

    public void outputOwners(PrintWriter out) {
        // traverse doc, outputing nodes

        log.info("inserting owners ");
        try {
            Node tree = dom.getNode();
            NodeList children = tree.getChildNodes();
            log.info("dom tree contains " + children.getLength() + " nodes");
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    //check to see if there are collections under owner
                    Element e = (Element) child;
                    outputItem(e, out);
                }
            }
        } catch (Exception e) {
            log.severe("Could not init ownerHierarchy because exception thrown:");
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            log.severe(sw.toString());
        }
    }

    /*
     * Output this item. Then, if the element has child elements, output each.
     */
    private void outputItem(Element e, PrintWriter out) {
        int chk = 0;
        HashMap collection = new HashMap();
        String key = null;
        Element collectionElement = null;
        log.fine("outputing item for " + e.getAttribute("name") + ":   " + e.getAttribute("query"));
        out.println("<item>");
        out.println("<name>" + e.getAttribute("name") + "</name>");
        if (e.getTagName().equalsIgnoreCase("collection")) {
            
            //change made by NETTY (change collectionlist to collection)
            out.println("<query>collection=" + e.getAttribute("col_id") + "&amp;xsl=metadata_to_html_full</query>");
        } else {
            out.println("<query>owner=" + e.getAttribute("query") + "&amp;xsl=metadata_to_html_full</query>");

        }
        // now output children
        
        NodeList children = e.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                chk++;
                outputItem((Element) child, out);
            }
        }

        out.println("</item>");
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
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
     * Handles the HTTP
     * <code>POST</code> method.
     *
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
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
