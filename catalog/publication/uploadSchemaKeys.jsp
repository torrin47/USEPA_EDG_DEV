<%-- 
    Document   : uploadSchemaKeys
    Created on : Jun 16, 2011, 5:16:10 PM
    Author     : jsievel

    This is a one time utility to upload the schema_key
    into the resources table
--%>

<% // downloadMetadata.jsp - Download metadata page (JSP) %>
<%@page import="com.esri.gpt.catalog.schema.MetadataDocument"%>
<%@page import="com.esri.gpt.framework.context.RequestContext"%>
<%@page import="com.esri.gpt.framework.security.principal.Publisher"%>
<%@page import="com.esri.gpt.catalog.schema.Schema"%>
<%@page import="com.esri.gpt.catalog.schema.SchemaException"%>
<%@page import="com.esri.gpt.framework.util.LogUtil"%>
<%@page import="java.util.logging.Level"%>
<%@page import="com.esri.gpt.framework.sql.ManagedConnection"%>
<%@page import="com.esri.gpt.framework.context.RequestContext"%>"
<%@page import="com.esri.gpt.framework.sql.BaseDao"%>
<%@page import="java.sql.Connection"%>
<%@page import="java.sql.PreparedStatement"%>
<%@page import="java.sql.Statement"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="java.util.ArrayList"%>

<%
class UpdateSchemaKey extends BaseDao {
    UpdateSchemaKey(HttpServletRequest req) {
        super(RequestContext.extract(req));
    }
    
public String updateSchemaKey(String uuid, String schemaKey) {
    Connection con = null;
    boolean autoCommit = true;
    PreparedStatement st = null;
    ResultSet rs = null;
    String result = uuid + ": "+schemaKey;

    try {
      ManagedConnection mc = returnConnection();
      con = mc.getJdbcConnection();
      autoCommit = con.getAutoCommit();
      con.setAutoCommit(true);
        StringBuffer sql = new StringBuffer();
        sql.append("UPDATE ").append("gpt_resource");
        sql.append(" SET ");
        sql.append("SCHEMA_KEY=?");   // JSS 20110325 from schema def file
        sql.append(" WHERE DOCUUID=?");
        st = con.prepareStatement(sql.toString());
        int n = 1;
        st.setString(n++,schemaKey);
        st.setString(n++,uuid);
        int nRows = st.executeUpdate();
        if (nRows != 1) {
            result += "Error, nRows: "+nRows;
        }
    } catch (Exception e) {
        result += "Exception: "+e.toString()+e.getMessage();
        e.printStackTrace();
    } finally {
        closeStatement(st);
        return result;
    }
}

// returns null if problem
public ArrayList getAllUuids() {
    Connection con = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    ArrayList<String> result = new ArrayList<String>();

    try {
        ManagedConnection mc = returnConnection();
        con = mc.getJdbcConnection();
        st = con.prepareStatement("Select docuuid, schema_key from gpt_resource");
        rs = st.executeQuery();
        while (rs.next()) {
            result.add(rs.getString("docuuid"));
        }
    } catch (Exception e) {
        result = null;
        LogUtil.getLogger().log(Level.WARNING,"Exception getting uuids",e);
    } finally {
        closeStatement(st);
        return result;
    }
}

// read the XML string associated with the UUID
public String readXml(HttpServletRequest request, String uuid) throws Exception {
  String sXml = "";
  RequestContext context = null;
  try {
    context = RequestContext.extract(request);
    Publisher publisher = new Publisher(context);
    MetadataDocument mdDoc = new MetadataDocument();
    sXml = mdDoc.prepareForDownload(context,publisher,uuid);
  } finally {
    if (context != null) {
      context.onExecutionPhaseCompleted();
    }
  }
  return sXml;
}

private String getSchemaKey(HttpServletRequest request, String xml) {
        String schemaKey = "";
        RequestContext reqContext = RequestContext.extract(request);
        MetadataDocument md = new MetadataDocument();
        Schema schema = null;
        try {
            schema = md.prepareForView(reqContext, xml);
        } catch (SchemaException se) {
            LogUtil.getLogger().log(Level.WARNING,"Could not find schema for xml",se);
            return "";
        }
        String schemaLabelResourceKey = schema.getLabel().getResourceKey();
        if (schemaLabelResourceKey!=null) {
            int lastPer = schemaLabelResourceKey.lastIndexOf(".");
            if (lastPer >= 0)
                schemaKey = schemaLabelResourceKey.substring(lastPer+1);
        }
        return schemaKey;
}
}

  UpdateSchemaKey update = new UpdateSchemaKey(request);
  String sUuid = request.getParameter("uuid");
  String sXml = "";
  String sErr = "";

  // if uuid is 'ALL', process all metadata
  try {
      String xml = null;
      String schemaKey = null;
      String result = null;
      if (sUuid.equals("ALL")) {
          ArrayList<String> uuids =null;
          out.println("getting getAllUuids<br>");
          uuids = update.getAllUuids();
          if (uuids==null) {
              out.println("getAllUuids returned null. Check log.");
          } else {
              int cnt = 0;
              for (String uuid : uuids) {
                  sUuid = uuid;
                  cnt++;
                  xml = update.readXml(request, uuid);
                  schemaKey = update.getSchemaKey(request, xml);
                  result = update.updateSchemaKey(uuid, schemaKey);
                  out.println(result+"<br>");
                  //out.println(sUuid+"<br>");
              }
              out.println("cnt: "+cnt);
          }
      } else {
          xml = update.readXml(request, sUuid);
          schemaKey = update.getSchemaKey(request, xml);
          result = update.updateSchemaKey(sUuid, schemaKey);
          out.println(result+"<br>");
      }
  } catch (Exception e) {
      out.print("Exception while processing uuid: "+sUuid+"   "+e.getMessage()+"<br>");
  } finally {
    //out.close();
  }


%>
