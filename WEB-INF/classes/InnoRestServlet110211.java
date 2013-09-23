import java.io.*;
import java.util.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Properties;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
    private static Log log = LogFactory.getLog(InnoRestServlet.class);
    protected static ServletContext ctx;
	private static Properties sso = new Properties();
	private static ArrayList<String> legacyXsl = new ArrayList<String>();

    @Override
    public void init() {
        BufferedReader legacyXslReader = null;
        try {
            sso.load(this.getServletContext().getResourceAsStream("/WEB-INF/classes/sso.properties"));
            legacyXslReader = new BufferedReader(new InputStreamReader(this.getServletContext().getResourceAsStream("/WEB-INF/classes/legacyXsl.txt")));
            String line = null;
            while ((line=legacyXslReader.readLine()) != null) {
                if ((line.trim().length() > 0) && (!line.substring(0,1).equals("#"))) {
                    legacyXsl.add(line.trim());
                }
            }
            legacyXslReader.close();
            //log.debug("InnoRestServlet legacyXsl: "+legacyXsl.toString());
            log.info("Initialization complete.");
        } catch (Exception e) {
            log.fatal("Initialization failed.");
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

		ctx = this.getServletContext();
        //response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            String id = request.getParameter("id");
            if (id.contains("/"))
                return;     // possible hack attempt
            String xmlUrl = request.getScheme() + "://" + request.getServerName();
	    if (request.getServerPort()>0)
		  xmlUrl += ":" + request.getServerPort();
	    //xmlUrl += this.getServletContext().getRealPath().toString();
	    //xmlUrl += request.getContextPath() + "/ESRIRestServlet?id="+URLEncoder.encode(id);
	    xmlUrl += request.getContextPath() + "/ESRIRestServlet?"+request.getQueryString();
            //String xmlUrl = "http://localhost:8080/GDG/test/Metadata.xml";
            log.debug("InnoRestServlet xmlUrl: "+xmlUrl);
            String xmlIn = null;
            try {
                xmlIn = getUrlContents(xmlUrl, request);
		String requestUrl = request.getRequestURL().toString();
		if(!requestUrl.contains("&redirected=true") & xmlIn.contains("Error getting metadata: No associated record was located for: ")) {
			String contextPath = request.getContextPath();
			String redirectUrl = requestUrl.substring(0, requestUrl.indexOf(contextPath) + contextPath.length() + 1);
			//out.println("<SCRIPT>var win = window; if(window.parent) win = window.parent.window; win.location.href = win.location.href.replace(\"https://geogateway.epa.gov/geoportal/\", \"http://134.67.221.181:8080/WAFr2/\"); </SCRIPT>");
			out.println("<SCRIPT>var win = window; if(window.parent) win = window.parent.window; win.location.href = win.location.href.replace(\"" + redirectUrl + "\", \"" + sso.getProperty("IntranetAdapterServletUrl") + "\"); </SCRIPT>");
			return;
		}
            } catch (Exception e) {
                log.error("InnoRestServlet  processRequest threw exception while trying to get metadata using xmlUrl "+xmlUrl,e);
                return;
            }
            String xslParm = request.getParameter("xsl");
            if (xslParm == null || xslParm.equals("")) {
		    String fParm = request.getParameter("f");
		    if (fParm == null || fParm.equals(""))
                	response.setContentType("text/xml");
		    else
                	response.setContentType("text/html");
                out.println(xmlIn);
                return;
            } else {
                if ((!xslParm.equals("metadata_to_html_full")) && (!legacyXsl.contains(xslParm))) {
                    out.println("Unsupported xsl parm supplied to InnoRestServlet");
                    return;
                }
            }
            String htmlOut = "";
            try {
                String xslUrl = "/catalog/search/metadata_to_html_full.xsl";
                if (legacyXsl.contains(xslParm)) {
                    String legacyUrl = request.getScheme() + "://" + request.getServerName();
                    if (request.getServerPort()>0)
                        legacyUrl += ":" + request.getServerPort();
                    legacyUrl += "/legacyXsl/legacyXsl.ashx?xsl=" + URLEncoder.encode(xslParm) +
                                "&xml=" + URLEncoder.encode(xmlUrl);
                    log.info("InnoRestServlet invoking legacyXsl web service with URL: "+legacyUrl);
                    htmlOut = getUrlContents(legacyUrl,request);
                } else
                    htmlOut = xform(xmlIn, xslUrl);
                response.setContentType("text/html;charset=UTF-8");
                out.println(htmlOut);
            } catch (Exception e2) {
                log.error("InnoRestServlet processRequest threw exception", e2);
                return;
            }
        } finally {
            out.close();
        }
    }

	public static String getUrlContents(String urlStr, HttpServletRequest request) throws Exception {
		URL u;
		URLConnection c = null;
		InputStream is = null;
		BufferedReader br;
		String content = "";
		String s;

		try {
			// Force use of http for now.
			//urlStr = urlStr.replace("https://", "http://");
			u = new URL(urlStr);
			c = u.openConnection();
            // get cookies from request and put them in thew req for the xml
            String theCookies = request.getHeader("Cookie");
            //log.info("Cookie: " + theCookies);
            c.setRequestProperty("Cookie", theCookies);

//			log.info("XYZ_1");
// Grab sso header from gpt config - when we have more time...
//			getIdentityConfiguration().getSingleSignOnMechanism().getActive()
//			String ssoHeader = "HTMLWGUSERUID".toLowerCase();
			String ssoHeader = sso.getProperty("ssoHeader").toLowerCase();
            if(request.getHeader(ssoHeader)!=null) {
            	c.setRequestProperty(ssoHeader, request.getHeader(ssoHeader));
            	//log.info(ssoHeader + ": " + request.getHeader(ssoHeader));
			}

 			c.connect();
			is = u.openStream();
			br = new BufferedReader(new InputStreamReader(c.getInputStream()));

			while ((s = br.readLine()) != null)
				content += s + "\n";

		} finally {
			try {
				is.close();
			} catch (Exception e) {
			}
		}
		return(content);
	}


 	public String xform(String xmlStr, String xslUrl)
	throws TransformerException, TransformerConfigurationException, FileNotFoundException, IOException {
        /* if externalXslUrl is present, use it to get the xsl from an external server, otherwise
         * use xslUrl to get it from this server
         */

		TransformerFactory tFactory = TransformerFactory.newInstance();
        InputStream xslStream = null;
        URL url = null;
        if (xslUrl == null)
        	return "";
        if (xslUrl.startsWith("http://")) {
            url = new URL(xslUrl);
            xslStream = url.openStream();
        } else
            xslStream = ctx.getResourceAsStream(xslUrl);
		Transformer transformer = tFactory.newTransformer(new StreamSource(xslStream));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
        transformer.transform(new StreamSource(new StringReader(xmlStr)), new StreamResult(baos));
		return(baos.toString());
	}

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
        return "Xforms xml via an xslt.";
    }// </editor-fold>

}

