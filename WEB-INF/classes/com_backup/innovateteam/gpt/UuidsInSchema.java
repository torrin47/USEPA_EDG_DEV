/*
 * Web service to return a list of uuids whose metadata
 * use a particular schema.
 */

package com.innovateteam.gpt;

import com.esri.gpt.framework.context.RequestContext;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import com.esri.gpt.framework.sql.ManagedConnection;
import java.util.logging.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author jsievel
 */
public class UuidsInSchema extends HttpServlet {
    private static Logger log = Logger.getLogger("com.esri.gpt");
   
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text;charset=UTF-8");
        PrintWriter out = response.getWriter();
        // return ERROR: or OK: followed by comma separated list of uuids in the schemaKey.
        try {
            String uuidsIn = request.getParameter("uuids");
            String schemaKey = request.getParameter("schemaKey");
            if ((uuidsIn == null) || (uuidsIn.length()==0))
                out.println("ERROR: No uuids found in request.");
            else if ((schemaKey == null) || (schemaKey.length()==0))
                out.println("ERROR: No schemaKey found in request.");
            else {
                String result = getUuidsInSchema(request,uuidsIn,schemaKey);
                out.print(result);
            }
        } finally { 
            out.close();
        }
    }

    private String getUuidsInSchema(HttpServletRequest req, String uuidsIn, String schemaKey) {
        StringBuffer result = new StringBuffer();
        Connection con = null;
        Statement st = null;
        ResultSet rs = null;
        String[] uuidsArray = uuidsIn.split(",");
        StringBuffer sqlBuf = new StringBuffer("SELECT docuuid FROM Gpt_Resource WHERE schema_key = '"+schemaKey+"'"+
                " AND docuuid IN (");
        for (int i=0; i<uuidsArray.length; i++) {
            if (i>0)
                sqlBuf.append(',');
            sqlBuf.append('\'').append(uuidsArray[i]).append('\'');
        }
        sqlBuf.append(")");
        String sql = sqlBuf.toString();

        try {
            ManagedConnection mc = RequestContext.extract(req).getConnectionBroker().returnConnection("");
            con = mc.getJdbcConnection();
            st = con.createStatement();
            log.info("Executing query: "+sql);
            rs = st.executeQuery(sql);
            while (rs.next()) {
                if (result.length()>0)
                    result.append(',');
                result.append(rs.getString("docuuid"));
            }
            return "OK:," + result.toString();
        } catch (Exception e) {
            return "ERROR: Exception: "+e.getMessage();
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
