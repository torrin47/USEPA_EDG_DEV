import java.util.Properties;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.context.BaseServlet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.*;
import org.apache.commons.httpclient.methods.*;

public class WAFerAdapterServlet extends BaseServlet
{
	private Properties props = new Properties();
	private Log log = LogFactory.getLog(getClass());
	private String wafUser = null;
	private String wafPwd = null;
	private String WAFerUrlAuth = null;
	private String WAFerUrlNoAuth = null;

    @Override
    public void init() {
	String WAFerUrl = null;
	String subcontextAuth = null;
	String subcontextNoAuth = null;
        try {
		log.info("Initialization start.");
		props.load(this.getServletContext().getResourceAsStream("/WEB-INF/classes/WAFerAdapterServlet.properties"));
		wafUser = props.getProperty("subcontextAuthUser");
		wafPwd = props.getProperty("subcontextAuthPwd");
		WAFerUrl = props.getProperty("WAFerUrl");
		subcontextAuth = props.getProperty("subcontextAuth");
		subcontextNoAuth = props.getProperty("subcontextNoAuth");
		WAFerUrlAuth = WAFerUrl + subcontextAuth + "/";
		WAFerUrlNoAuth = WAFerUrl + subcontextNoAuth + "/";
		log.info("Initialization complete.");
        } catch (Exception e) {
            log.fatal("Initialization failed.");
        }
    }


  protected void execute(HttpServletRequest request, HttpServletResponse response, RequestContext context)
    throws Exception
    {

		String user = context.getUser().getName();
		log.info("User: " + user);
		String id = request.getParameter("id");

		String wafUrl;
		if(user == null || user.trim().equals("")) {
			wafUrl = WAFerUrlNoAuth;
		}
		else {
			wafUrl = WAFerUrlAuth;
		}

		// Map original query
		if(id != null && !id.trim().equals("")) {
			String queryStr = request.getQueryString();
			if(queryStr.contains("{")) {
				queryStr = queryStr.replace("{", "%7B");
				queryStr = queryStr.replace("}", "%7D");
			}
			wafUrl += "get.jsp?" + queryStr;
		}


		// Prevent URL engineering into private area
		if(user == null || user.trim().equals("")) {
			wafUrl = wafUrl.replace("GDG_ALL", "GDG");
			log.info("Attempt to access GDG_ALL WAFer context detected.");
		}

		// Process the request
		String wafResponse = getUrlContentsWithPost(wafUrl, null, wafUser, wafPwd);
//		getLogger().finer(wafResponse);

		if(id != null && !id.trim().equals(""))
			writeXmlResponse(response, wafResponse);
		else {
			wafResponse = wafResponse.replace("get.jsp?", "waf?");
			writeHtmlResponse(response, wafResponse);
		}
	}



	public String getUrlContentsWithPost(String targetUrl, String postData, String user, String pwd) throws Exception {
	HttpMethodBase method = null;
		try {

			Boolean doOutput = true;
			if(postData==null || postData.equals(""))
				doOutput = false;

	        	HttpClient client = new HttpClient();
		        client.setConnectionTimeout(3*1000);	// 3 seconds.

			if(user != null && !user.trim().equals(""))
			{
				log.info("Attempting basic auth");
				client.getParams().setAuthenticationPreemptive(true);
				Credentials creds = new UsernamePasswordCredentials(user, pwd);
				client.getState().setCredentials(AuthScope.ANY, creds);
			}
			else
				log.info("No auth");

			if(doOutput)
			{
				method = new PostMethod(targetUrl);
				((PostMethod)method).setRequestEntity(new StringRequestEntity(postData, "text/xml", null) );
			}
			else
				method = new GetMethod(targetUrl);

			int statusCode = client.executeMethod(method);
			if (statusCode == HttpStatus.SC_OK)
				return new String(method.getResponseBody());
			else
				System.out.println("Method failed: " + method.getStatusLine());

		} catch (Exception e) {
            getLogger().finer("Error::getUrlContentsWithPost::Exception while trying to communicate with URL "+targetUrl);
			getLogger().finer(e.getMessage());
		}
		finally
		{
			if(method != null)
				method.releaseConnection();
		}
		return "";
	}

}
