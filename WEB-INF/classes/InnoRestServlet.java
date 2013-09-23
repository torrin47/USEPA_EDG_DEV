
import java.io.*;
import java.util.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Properties;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.catalog.schema.MetadataDocument;
import com.esri.gpt.catalog.schema.Schema;
import com.esri.gpt.catalog.schema.SchemaException;
import java.util.logging.Logger;
//import com.esri.gpt.framework.security.identity.SingleSignOnMechanism;

/**
 *
 * @author john sievel
 */
public class InnoRestServlet extends HttpServlet {

    /*
     * If the xsl parm is present, return the xformed xml from the ESRI RestServlet.
     * Otherwise, just return the xml from the ESRI RestServlet.
     */
    private static Logger log = Logger.getLogger("com.esri.gpt");
    protected static ServletContext ctx;
    private static Properties sso = new Properties();
    private static Properties xslStyleSheets = new Properties();
    private static ArrayList<String> legacyXsl = new ArrayList<String>();
    public static String[] xslAnchors = {">Metadata<", ">Details<", ">Preview<"};  // anchors that need xsl parm

    @Override
    public void init() {
        BufferedReader legacyXslReader = null;
        try {
            log.fine("in initialize");
            sso.load(this.getServletContext().getResourceAsStream("/WEB-INF/classes/sso.properties"));
            log.fine("after sso");
            xslStyleSheets.load(this.getServletContext().getResourceAsStream("/WEB-INF/classes/xslStyleSheets.properties"));
            log.fine("after xsl");
            legacyXslReader = new BufferedReader(new InputStreamReader(this.getServletContext().getResourceAsStream("/WEB-INF/classes/legacyXsl.txt")));
            String line = null;
            while ((line = legacyXslReader.readLine()) != null) {
                if ((line.trim().length() > 0) && (!line.substring(0, 1).equals("#"))) {
                    legacyXsl.add(line.trim());
                }
            }
            legacyXslReader.close();
            //log.debug("InnoRestServlet legacyXsl: "+legacyXsl.toString());
            log.info("Initialization complete.");
        } catch (Exception e) {
            log.severe("Initialization failed.");
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        ctx = this.getServletContext();
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            String id = request.getParameter("id");
            String xml = request.getParameter("xml");
         
            if (id != null) {
                if (id.contains("/")) {
                    log.fine("returning because id contains /");
                    return;
                }
            }
//            String xmlUrl = request.getScheme() + "://" + request.getServerName();
            String xmlUrl = "https://edg.epa.gov";
//            if (request.getServerPort()>0)
//                xmlUrl += ":" + request.getServerPort();

                     
            //Added by Netty to accept the xml paramater and redirect to data/public or data/restricted
            if (xml != null) {
                boolean xmlValid = true;
                xml=xml.toLowerCase();
                if (xml.substring(0, 1).equalsIgnoreCase("/")) {
                    xml = xml.substring(1);
                }
                if (xml.startsWith("data/")) {
                    xml = xml.substring(5);
                }

                if (!(xml.contains("public") || (xml.contains("restricted")))) {
                    xmlValid = false;
                } else if (xml.contains("./") || !(xml.contains(".xml"))) {
                    xmlValid = false;
                }

                if (xmlValid) {
                    xmlUrl += "/data/" + xml;
                } else {
                    out.println("Invalid XML Paramater");
                }
            } else {
                xmlUrl += request.getContextPath() + "/ESRIRestServlet?" + request.getQueryString();
//                xmlUrl +=  "/metadata/ESRIRestServlet?" + request.getQueryString();
            }

            log.fine("InnoRestServlet xmlUrl: " + xmlUrl);
            String xmlIn = null;
            String contentType = null;  // will come from getUrlContents
            try {
                String[] result = getUrlContents(xmlUrl, request, contentType);
                contentType = result[0];
                xmlIn = result[1];
                log.fine("contentType: " + contentType);
                String requestUrl = request.getRequestURL().toString();
                String queryStr = request.getQueryString();
                if (!queryStr.contains("&redirected=true") & xmlIn.contains("Error getting metadata: No associated record was located for: ")) {
                    String contextPath = request.getContextPath();
                    String redirectUrl = requestUrl.substring(0, requestUrl.indexOf(contextPath) + contextPath.length() + 1);
                    out.println("<SCRIPT>var win = window; if(window.parent) win = window.parent.window; win.location.href = win.location.href.replace(\"" + redirectUrl + "\", \"" + sso.getProperty("IntranetAdapterServletUrl") + "\") + '&redirected=true'; </SCRIPT>");
                    return;
                }
            } catch (Exception e) {
                log.severe("InnoRestServlet  processRequest threw exception while trying to get metadata: "  + e);
                return;
            }
            String fParm = request.getParameter("f");
            String xslParm = request.getParameter("xsl");
            if (xslParm == null || xslParm.equals("")) {
                response.setContentType(contentType);
                out.println(xmlIn);
                return;
            } else {
                log.fine("xslStyleSheets: " + xslStyleSheets.toString());
                boolean inSS = xslStyleSheets.containsKey(xslParm);
                boolean inLeg = legacyXsl.contains(xslParm);
                log.fine("inSS " + inSS + "   inLeg " + inLeg);
                if (((!inSS)) && (!inLeg)) {
                    out.println("Unsupported xsl parm supplied to InnoRestServlet " + xslParm);
                    return;
                }
            }
            String htmlOut = "";
            try {
                String xslUrl = "/WEB-INF/classes/gpt/metadata/" + xslParm + ".xsl";
                if (legacyXsl.contains(xslParm)) {
                    String legacyUrl = request.getScheme() + "://" + request.getServerName();
                    if (request.getServerPort() > 0) {
                        legacyUrl += ":" + request.getServerPort();
                    }
                    legacyUrl += "/legacyXsl/legacyXsl.ashx?xsl=" + URLEncoder.encode(xslParm)
                            + "&xml=" + URLEncoder.encode(xmlUrl);
                    log.info("InnoRestServlet invoking legacyXsl web service with URL: " + legacyUrl);
                    String[] result = getUrlContents(legacyUrl, request, contentType);
                    htmlOut = result[1];
                    response.setContentType(result[0]);
                } else {
                    if (fParm == null || fParm.equals("")) {
                        xslParm = getCorrectSS(xslParm, request, xmlIn);
                        xslUrl = "/WEB-INF/classes/gpt/metadata/" + xslParm + ".xsl";
                        htmlOut = xform(xmlIn, xslUrl);
                        contentType = xslStyleSheets.getProperty(xslParm);
                    } else {
                        // fparm and xsl parm, so put xsl parm on anchors but don't xform
                        htmlOut = fixAnchors(xmlIn, xslParm);
                    }
                    if ((contentType != null) && contentType.length() > 0) {
                        response.setContentType(contentType);
                    } else {
                        response.setContentType("text/html");
                    }
                }
                out.println(htmlOut);
            } catch (Exception e2) {
                log.severe("InnoRestServlet processRequest threw exception" + e2);
                return;
            }
        } finally {
            out.close();
        }
    }

    public String fixAnchors(String in, String xslParm) {
        // put the xsl parm on xsl anchor hrefs
        for (int i = 0; i < xslAnchors.length; i++) {
            int nPos = in.indexOf(xslAnchors[i]);
            if (nPos >= 0) {
                int hrefPos = in.toUpperCase().lastIndexOf("HREF", nPos);
                if (hrefPos >= 0) {
                    int lastQuote = in.indexOf('"', hrefPos + 7);
                    if (lastQuote >= 0) {
                        in = in.substring(0, lastQuote) + "&xsl=metadata_to_html_full"
                                + in.substring(lastQuote);
                    }
                }
            }
        }
        return in;
    }

    /**
     * If the xslParm is "metadata_to_html_full", check to see if the xml is
     * actually data-gov, and if so return that ss, otherwise return
     * "metadata_to_html_full"
     */
    public String getCorrectSS(String xslParm, HttpServletRequest request, String xml) {
        if (!xslParm.equals("metadata_to_html_full")) {
            return xslParm;
        }
        // try to infer what xsl to use
        RequestContext reqContext = RequestContext.extract(request);
        MetadataDocument md = new MetadataDocument();
        Schema schema = null;
        try {
            schema = md.prepareForView(reqContext, xml);
        } catch (SchemaException se) {
            log.warning("Could not find schema for xml: " + xml);
            return "metadata_to_html_full";
        }
        String schemaLabelResourceKey = schema.getLabel().getResourceKey();

        //Modified by Netty to include the sytlesheet for virtual-resource (11/13/2012)
        if ((schemaLabelResourceKey != null) && schemaLabelResourceKey.equals("catalog.mdParam.schema.dataGov")) {
            return "data-gov";
        } else if ((schemaLabelResourceKey != null) && schemaLabelResourceKey.equals("catalog.mdParam.schema.virtual")) {
            return "virtual-resource";
        }

        return "metadata_to_html_full";
    }

    public static String[] getUrlContents(String urlStr, HttpServletRequest request, String contentType) throws Exception {
        // return contentType in 0, and the contents in 1
        URL u;
        URLConnection c = null;
        InputStream is = null;
        BufferedReader br;
        String content = "";
        String s;

        try {
            u = new URL(urlStr);
            c = u.openConnection();
            // get cookies from request and put them in thew req for the xml
            String theCookies = request.getHeader("Cookie");
            //log.info("Cookie: " + theCookies);
            c.setRequestProperty("Cookie", theCookies);

            String ssoHeader = sso.getProperty("ssoHeader").toLowerCase();
            if (request.getHeader(ssoHeader) != null) {
                c.setRequestProperty(ssoHeader, request.getHeader(ssoHeader));
                //log.info(ssoHeader + ": " + request.getHeader(ssoHeader));
            }
            c.connect();
            contentType = c.getContentType();
            log.finest("getContentType: " + contentType);
            is = u.openStream();
            br = new BufferedReader(new InputStreamReader(c.getInputStream()));

            while ((s = br.readLine()) != null) {
                content += s + "\n";
            }

        } finally {
            try {
                is.close();
            } catch (Exception e) {
            }
        }
        String[] result = new String[2];
        result[0] = contentType;
        result[1] = content;
        return (result);
    }

    public String xform(String xmlStr, String xslUrl)
            throws TransformerException, TransformerConfigurationException, FileNotFoundException, IOException {
        /* if externalXslUrl is present, use it to get the xsl from an external server, otherwise
         * use xslUrl to get it from this server
         */

        TransformerFactory tFactory = TransformerFactory.newInstance();
        InputStream xslStream = null;
        URL url = null;
        if (xslUrl == null) {
            return "";
        }
        if (xslUrl.startsWith("http://")) {
            url = new URL(xslUrl);
            xslStream = url.openStream();
        } else {
            xslStream = ctx.getResourceAsStream(xslUrl);
        }
        Transformer transformer = tFactory.newTransformer(new StreamSource(xslStream));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        transformer.transform(new StreamSource(new StringReader(xmlStr)), new StreamResult(baos));
        return (baos.toString());
    }

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
        return "Xforms xml via an xslt.";
    }// </editor-fold>
}
