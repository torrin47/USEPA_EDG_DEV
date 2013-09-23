/*
 * Servlet to provide the file "browse-catalog.xml, which is used by the browse page.
 * The existing browse-catalog.xml page will be output, but the comment that contains
 * the string "InsertOwnersHere" will be replaced by the xml to define the owners.
 * Note: Only users that contain the filterUserStr configed parm ("cn=gis" on prod and staging)
 * will be output.
 */

package com.innovateteam.gpt;

import java.io.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.esri.gpt.framework.context.RequestContext;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import com.esri.gpt.framework.sql.ManagedConnection;
import java.util.logging.Logger;

/**
 *
 * @author jsievel
 */
public class BrowseOwners extends HttpServlet {
    private static Logger log = Logger.getLogger("com.esri.gpt");
    public String filterUserStr = null;

    @Override
    public void init() {
        filterUserStr = this.getServletConfig().getInitParameter("filterUserStr");
        if ((filterUserStr != null) && (filterUserStr.length()>0))
            log.info("filterUserStr: "+filterUserStr);
        else
            log.severe("Init failed, no filterUserStr");
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
        response.setContentType("text/xml;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            // get exiting file, and output, looking for where to insert owners.
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    this.getServletContext().getResourceAsStream("/WEB-INF/classes/gpt/search/browse/browse-catalog.xml")));
            log.fine("in: "+in.toString());
            String rec = null;
            while ((rec=in.readLine())!=null) {
                if (rec.contains("InsertOwnersHere")) {
                    outputOwners(request,out);
                } else
                    out.println(rec);
            }
        } finally { 
            out.close();
        }
    }

    public void outputOwners(HttpServletRequest req, PrintWriter out) {
        Connection con = null;
        Statement st = null;
        ResultSet rs = null;
        String sql = null;
        try {
            ManagedConnection mc = RequestContext.extract(req).getConnectionBroker().returnConnection("");
            con = mc.getJdbcConnection();
            st = con.createStatement();
            sql = "Select userid, dn, username,"+
                    "(select count(*) from gpt_resource"+
                        " where owner=userid and approvalstatus='approved') cnt"+
                    " From gpt_user Order By username";
            log.info("Executing query: "+sql);
            rs = st.executeQuery(sql);
            out.println("<item>");
            out.println("<name resourceKey=\"catalog.search.filterOwners.title\"></name>");
            out.println("<query></query>");
            String cn = null;
            while (rs.next()) {
                cn = rs.getString("dn");
                if (cn.contains(filterUserStr) && (rs.getInt("cnt")>0)) {
                    // an office
                    out.println("<item>");
                    out.println("<name>"+rs.getString("username")+"</name>");
                    out.println("<query>owner="+rs.getString("userid")+"&amp;xsl=metadata_to_html_full</query>");
                    out.println("</item>");
                }
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            log.severe(sw.toString());
        } finally {
            out.println("</item>");
            if (con != null) {
                try {
                    con.close();
                } catch (Exception f) {}
            }
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
