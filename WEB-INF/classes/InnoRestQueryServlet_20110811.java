
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.net.URLConnection;
import java.util.*;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.net.ssl.TrustManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.util.logging.Logger;

/**
 *
 * @author John Sievel
 */
public class InnoRestQueryServlet extends HttpServlet {
    static final String linksString = "<div class=\"links\">";
    static final String endLinksString = "</div>";
    static final String categoryString = "<category>";
    private static Logger log = Logger.getLogger("com.esri.gpt");
    private static Properties props = new Properties();
	private static ArrayList<String> legacyXsl = new ArrayList<String>();
    public String defaultMaxRecords = null;
	//private String contentType = "";

    @Override
    public void init() {
        BufferedReader legacyXslReader = null;
        try {
            defaultMaxRecords = getInitParameter("defaultMaxRecords");
            props.load(this.getServletContext().getResourceAsStream("/WEB-INF/classes/sso.properties"));
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
            log.severe("Initialization failed.");
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
        PrintWriter out = null;
        try {
            /* If there is no xsl parm, simply return the output from the ESRI RestQueryServlet. Otherwise,
             * get the input, and search for '<div class="links">'. Output all text up to that,
             * and fix the links. For georss, also handle the link element.
             */
            String f = request.getParameter("f");
            if (f==null)
                f = "html";
            //response.setContentType(getMimeTypeForFormat(f));
            //response.setContentType("application/rss+xml;charset=UTF-8\r\n");
            //response.setContentType("text/html;charset=UTF-8\r\n");
            //System.out.println(f);

            String xslParm = request.getParameter("xsl");
            String queryStr = request.getQueryString();
            if(queryStr.contains("{")) {
                queryStr = queryStr.replace("{", "%7B");
                queryStr = queryStr.replace("}", "%7D");
            }
	    
            String inURL = request.getRequestURL().toString().replace("rest/find/document","RestQueryServlet")+ "?" + queryStr;
            inURL = inURL.replace("RestQueryServlet.kml","RestQueryServlet");
            //AE: It seems ESRI is limiting output to 10 recs by default which seems too few.
            //Override for now. TO DO: Try to sense and pass recordset paging params if ESRI servlet accepts.
            String max = request.getParameter("max");
            log.fine("parm max: "+max+"   defaultMaxRecords: "+defaultMaxRecords);
            if (request.getParameter("max")!=null)
                inURL += "&max="+max;
            else {
                // use servlet defaultMaxRecords if there
                if ((defaultMaxRecords != null) && (defaultMaxRecords.length()>0))
                    inURL += "&max="+defaultMaxRecords;
                else
                    inURL += "&max=50";
            }
            //inURL = "http://localhost:8080/GDG/test/"+request.getParameter("file");
            log.fine("inURL: "+inURL);

            if (xslParm != null) {
                // xsl, verify legit
                if ((!xslParm.equals("metadata_to_html_full")) && (!legacyXsl.contains(xslParm))) {
                    response.setContentType("text/html");
                    out = response.getWriter();
                    out.println("Unsupported xsl parameter supplied to InnoRestQueryServlet");
                    return;
                }
            }
            String xmlIn = "";
            URL url = new URL(inURL);
            URLConnection c = null;
            InputStream is = null;
            BufferedReader br;
            String s;
            c = url.openConnection();

            // get cookies from request and put them in thew req for the xml
            String theCookies = request.getHeader("Cookie");
            c.setRequestProperty("Cookie", theCookies);

            String ssoHeader = props.getProperty("ssoHeader").toLowerCase();
            if(request.getHeader(ssoHeader)!=null) {
                c.setRequestProperty(ssoHeader, request.getHeader(ssoHeader));
                log.info(ssoHeader + ": " + request.getHeader(ssoHeader));
            }

		//if(inURL.startsWith("https://"))
		//{
			//System.out.println("Pre-easy");
        		//TrustManager[] myTM = new TrustManager [] {new EasyX509TrustManager(null)};
        		//SSLContext ctx = SSLContext.getInstance("TLS");
        		//ctx.init(null, myTM, null);
			//SSLSocketFactory sslSocketFactory = ctx.getSocketFactory();
			//((HttpsURLConnection) c).setSSLSocketFactory(sslSocketFactory);
		//}
            c.connect();
	    System.out.println("ct: " + c.getContentType());
	    //contentType = c.getContentType();
	    response.setContentType(c.getContentType());
            is = url.openStream();
            br = new BufferedReader(new InputStreamReader(c.getInputStream()));

            while ((s = br.readLine()) != null)
                xmlIn += s+"\n";

	    out = response.getWriter();

            // if no xsl, return xmlIn and done
            if (xslParm == null) {
                out.println(xmlIn);
                return;
            }

            // there is an xsl parm, so process text and return that

            boolean rss = f.equals("georss")?true:false;
            int linksEnd = 0;
            int categoryPos = -1;
            int linkElementStart = -1;
            int linkElementEnd = -1;
            String category = "";
            int linksPos = xmlIn.indexOf(linksString);
            while (linksPos != -1) {
                int linksStart = linksPos+linksString.length();
                if (rss) {
                    // look for link element and handle
                    linkElementStart = xmlIn.lastIndexOf("<link>",linksPos);
                    if (linkElementStart >= 0) {
                        linkElementEnd = xmlIn.indexOf("</link>",linkElementStart);
                        if (linkElementEnd>=0) {
                            out.print(xmlIn.substring(linksEnd, linkElementStart+6));
                            out.print(fixLinkElement(xmlIn.substring(linkElementStart+6,linkElementEnd),request));
                            linksEnd = linkElementEnd;
                        }
                    }
                }
                out.print(xmlIn.substring(linksEnd, linksStart));
                linksEnd = xmlIn.indexOf(endLinksString, linksPos+linksString.length());
                if (linksEnd == -1)
                    linksEnd = xmlIn.length();
                categoryPos = xmlIn.indexOf(categoryString, linksEnd);
                if (categoryPos != -1) {
                    category = xmlIn.substring(categoryPos+categoryString.length(), xmlIn.indexOf("</", categoryPos+categoryString.length())).toLowerCase();
                }
                out.println(fixLinks(xmlIn.substring(linksStart,linksEnd),category,request));
                linksPos = xmlIn.indexOf(linksString,linksEnd);
            }
            // output anything left over
            out.print(xmlIn.substring(linksEnd));
        } catch (Throwable ex) {
            response.setContentType("text/html");
            System.out.println("Servlet threw exception:"+ex.getMessage());
        } finally {
		if(out != null) out.close();
        }
    }

    private String fixLinkElement(String thisLink, HttpServletRequest request) {
        if (thisLink != null) {
            // if thisLink ends in "]]>", link content is in a CDATA section, so put xsl parm inside it
            int cdataEnd = thisLink.lastIndexOf("]]>");
            if (cdataEnd==(thisLink.length()-3))
                return thisLink.substring(0, cdataEnd) + "&xsl=" + request.getParameter("xsl") + "]]>";
            else
                return thisLink+"&amp;xsl="+request.getParameter("xsl");
        }
        return "";
    }


    /*
     * Return the correct mime-type for the given format specification
     */
    public static String getMimeTypeForFormat(String f) {
        if (f == null) 
		f = "rss";

            if(f.equals("kml"))
                return("application/vnd.google-earth.kml+xml");
            else if(f.equals("rss") || f.equals("georss"))
                return("application/rss+xml");
            else if(f.startsWith("html"))
                return("text/html");

	return "";
    }

    private String fixLinks(String links, String category, HttpServletRequest request)
        throws UnsupportedEncodingException {
        String llnks = links.toLowerCase();
        HashMap<String,String> linkMap = new HashMap(10);
        int linkStart = llnks.indexOf("<a");
        if (linkStart == -1)
            return links;
        int linkEnd = 0;
        int aEnd = -1;
        while ((linkStart != -1) && (linkEnd != -1)) {
            // get link name, and save the link attrs for it in the map
            linkEnd = llnks.indexOf("</a>",linkStart);
            if (linkEnd != -1) {
                aEnd = llnks.lastIndexOf(">", linkEnd);
                if (aEnd != -1) {
                    String name = llnks.substring(aEnd+1, linkEnd).trim();
                    String attrs = links.substring(linkStart+3, aEnd);
                    // only save the link if it does not contain an href begining "server="
                    if (attrs.toLowerCase().indexOf("href=\"server=")<0)
                        linkMap.put(name, attrs);
                }
                linkStart = llnks.indexOf("<a", linkEnd);
            }
        }
        // now play back links in order, changing name if necessary
        //  "details","preview","open","website","metadata,"add to map","arcmap","globe (.kml)","globe (.nmf)"
        String fixedLinks = "";
        String thisLink = "";
        fixedLinks += outputLink("details",linkMap);
        if ((thisLink=linkMap.get("preview")) != null) {
            if ((category.length()==0) || (category.equals("livedata")))
                fixedLinks += "<A "+thisLink+">Preview</A> \n";
            linkMap.remove("preview");
        }
        if ((thisLink=linkMap.get("open")) != null) {
            if ((category.length()==0) || (!category.equals("livedata")))
                fixedLinks += "<A "+thisLink+">Download</A> \n";
            linkMap.remove("open");
        }
        fixedLinks += outputLink("website",linkMap);
        fixedLinks += outputMetadataLink(linkMap,request);  // special handling for metadata
        fixedLinks += outputLink("add to map",linkMap);
        fixedLinks += outputLink("arcmap",linkMap);
        fixedLinks += outputLink("globe (.kml)",linkMap);
        fixedLinks += outputLink("globe (.nmf)",linkMap);
        // now put out any remaining links
        Set<Map.Entry<String,String>> linksSet = linkMap.entrySet();
        for (Map.Entry thisEntry : linksSet) {
            fixedLinks += "<A "+thisEntry.getValue()+">"+thisEntry.getKey()+"</A> \n";
        }
        return fixedLinks;
    }

    private String outputMetadataLink(HashMap<String, String> map, HttpServletRequest request)
        throws UnsupportedEncodingException {
        // Change the href to add the xsl parm to control the xform
        String thisLink = map.get("metadata");
        String thisLinkLower = thisLink.toLowerCase();
        if (thisLink != null) {
            int hrefPos = thisLinkLower.indexOf("href=");
            if (hrefPos >= 0) {
                hrefPos += 6;
                int hrefEnd = thisLinkLower.indexOf("\"", hrefPos);
                if (hrefEnd > 0) {
                    String origHref = thisLink.substring(hrefPos, hrefEnd);
                    String newHref = origHref+"&xsl="+request.getParameter("xsl");
                    String newLink = thisLink.substring(0,thisLinkLower.indexOf("href=\"")+6) + newHref + thisLink.substring(hrefEnd);
                    String fixedLink = "<A "+newLink+">Metadata</A> \n";
                    map.remove("metadata");
                    return fixedLink;
                }
            }
        }
        // somethning went wrong, use orig
        return outputLink("metadata",map);
    }

    private String outputLink(String type, HashMap<String, String> map) {
        // upcase the first char of type, output link, and remove from map
        String thisLink = map.get(type);
        String fixedLink = "";
        if (thisLink != null) {
            String upcasedType = type.substring(0,1).toUpperCase() + type.substring(1);
            fixedLink += "<A "+thisLink+">"+upcasedType+"</A> \n";
            map.remove(type);
        }
        return fixedLink;
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

