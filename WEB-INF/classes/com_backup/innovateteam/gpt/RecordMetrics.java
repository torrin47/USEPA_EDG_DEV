/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.innovateteam.gpt;

import java.io.*;
import java.net.URL;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.logging.Logger;
import com.esri.gpt.framework.context.RequestContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import com.esri.gpt.framework.sql.ManagedConnection;

/**
 *
 * @author jsievel
 */
public class RecordMetrics extends HttpServlet {
    private static final Logger log = Logger.getLogger("com.esri.gpt");
   
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
        boolean errors = false;
        try {
            String href = request.getParameter("href");
            String uuid = request.getParameter("uuid");
            String uid = request.getHeader("uid");
            if ((uid == null) || uid.equalsIgnoreCase("OblixAnonymous"))
                    uid = "";
            String rip = "";
            if(request.getHeader("x-forwarded-for") != null)
                rip = request.getHeader("x-forwarded-for");
            else
                rip = request.getRemoteAddr();
            // report missing parms to log, but save hit anyway
            if ((href == null) || (href.length()==0)) {
                log.warning("Missing href parm");
                errors = true;
            }
            if ((uuid == null) || (uuid.length()==0)) {
                log.warning("Missing uuid parm");
                errors = true;
            }
            log.info("uuid: "+uuid+"   uid: "+uid+"   href: "+href+"   rip: "+rip);
            String[] geoInfoArr = getGeoInfo(rip);
            String result = writeMetrics(uuid,uid,href,rip,geoInfoArr,request);
            out.println(result);
        } finally { 
            out.close();
        }
    }

    String writeMetrics(String uuid, String uid, String href, String rip, String[] geoInfoArr,
            HttpServletRequest req) {
        
        Connection con = null;
        PreparedStatement s = null;
        try {
            ManagedConnection mc = RequestContext.extract(req).getConnectionBroker().returnConnection("");
            con = mc.getJdbcConnection();
            s = con.prepareStatement("insert into metrics_raw(ip_address, cntry_code,"+
                    "rgn_code, city, zip_code, latitude, longitude, metro_code, area_code,"+
                    "isp, organization, uuid, linkage,uid,access_date)"+
                    " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,current_timestamp)");
            s.setString(1, rip);
            if (geoInfoArr != null) {
                s.setString(2, geoInfoArr[0]);
                s.setString(3, geoInfoArr[1]);
                s.setString(4, geoInfoArr[2]);
                s.setString(5, geoInfoArr[3]);
                s.setObject(6, geoInfoArr[4],java.sql.Types.DOUBLE);
                s.setObject(7, geoInfoArr[5],java.sql.Types.DOUBLE);
                if (geoInfoArr[6].length()==0)
                    s.setNull(8, java.sql.Types.NUMERIC);
                else
                    s.setObject(8, geoInfoArr[6],java.sql.Types.NUMERIC);
                if (geoInfoArr[7].length()==0)
                    s.setNull(9, java.sql.Types.NUMERIC);
                else
                    s.setObject(9, geoInfoArr[7],java.sql.Types.NUMERIC);
                s.setString(10, geoInfoArr[8]);
                s.setString(11, geoInfoArr[9]);
            }
            s.setString(12, uuid);
            s.setString(13, href);
            s.setString(14, uid);
            s.execute();
            return "OK:";
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            log.severe(sw.toString());
            return "ERROR: Exception writing metrics";
        }
    }

    /**
     * Get the geo info array from the ip
     * @param rip - the raw ip
     * @return
     */
    String[] getGeoInfo(String rip) {
        String geoInfo = null;
        String[] geoInfoArr = null;
        if(rip!=null && rip.length() >= 7) {
            BufferedReader in = null;
            try {
                URL geolocator = new URL("http://geoip3.maxmind.com/f?l=uHb8LATVUFub&i=" + rip);
                in = new BufferedReader(new InputStreamReader(geolocator.openStream()));
                geoInfo = in.readLine();
            }
            catch(Exception e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                log.severe(sw.toString());
            }
            finally {
                try {in.close();} catch (Exception a) {}
            }
            if(geoInfo!=null) {
                log.info("geoInfo: "+geoInfo);
                geoInfoArr = geoInfo.split(",");
                if(geoInfoArr[8].startsWith("\""))
                    geoInfoArr[8] = geoInfoArr[8].substring(1, geoInfoArr[8].length()-1);
                if(geoInfoArr[9].startsWith("\""))
                    geoInfoArr[9] = geoInfoArr[9].substring(1, geoInfoArr[9].length()-1);
            }
        }
        return geoInfoArr;
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
