/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.innovateteam.gpt;

import com.esri.gpt.catalog.arcims.ImsMetadataAdminDao; 
import com.esri.gpt.catalog.arcims.ImsServiceException;
import com.esri.gpt.catalog.context.CatalogIndexException;
import com.esri.gpt.catalog.management.MmdActionRequest;
import com.esri.gpt.catalog.management.MmdCriteria;
import com.esri.gpt.catalog.publication.UploadRequest; 
import com.esri.gpt.catalog.schema.SchemaException; 
import com.esri.gpt.control.view.SelectablePublishers;    
import com.esri.gpt.framework.collection.StringSet;    
import com.esri.gpt.framework.context.RequestContext;   
import com.esri.gpt.framework.security.credentials.CredentialsDeniedException;
import com.esri.gpt.framework.security.identity.IdentityException; 
import com.esri.gpt.framework.security.identity.NotAuthorizedException;
import com.esri.gpt.framework.security.principal.Groups;
import com.esri.gpt.framework.security.principal.Publisher;   
import com.esri.gpt.framework.sql.BaseDao;
import java.sql.Connection;
import java.sql.DriverManager;  
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import com.esri.gpt.framework.util.LogUtil;
import java.sql.Statement;
import java.util.ArrayList;  
import java.util.Iterator; 
import java.util.LinkedHashMap;  
import java.util.Set;

/**
 *
 * @author Deewen
 */
public class innoCollection extends BaseDao {

    private Connection con = null;
    private static Logger log = Logger.getLogger("com.esri.gpt");

    /*
     public innoCollection(ServletContext dbCredentials) {
        
        
     String database = null;
     try {
     Class.forName("org.postgresql.Driver").newInstance();
     try {
     final Properties properties = new Properties();
     database = dbCredentials.getInitParameter("database");
     properties.setProperty("user", dbCredentials.getInitParameter("username"));
     properties.setProperty("password", dbCredentials.getInitParameter("password"));

     this.con = DriverManager.getConnection(database, properties);
     } catch (SQLException e) {
     log.info("Deewendra : Connection Failed! " + e.toString());
     }
     } catch (Exception e) {
     log.info("Deewendra : Could not find the class in given library path! " + e.toString());
     }
     }*/
    public innoCollection(Connection con) {
        this.con = con;
    }

    public innoCollection(RequestContext context) {
        try {
            this.con = context.getConnectionBroker().returnConnection("").getJdbcConnection();
        } catch (SQLException ex) {
            log.info("Deewendra : " + ex);
        }
    }

    public void closeConnection() {

        try {
            this.con.close();
        } catch (SQLException ex) {
            log.info("Deewendra : " + ex.getMessage());
        }
    }

    public Connection getConnection() {
        return this.con;
    }

    public int fnListCollectionCount(HashMap filter) {
        int count = 0;
        ResultSet rs = this.fnListCollection(filter, true, null, null);
        try {
            while (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (Exception e) {
            //write to log
        }
        return count;
    }

    public ResultSet fnListCollection(HashMap filter, Integer offset, Integer limit) {
        return this.fnListCollection(filter, false, offset, limit);
    }

    public ResultSet fnListCollection(HashMap filter, Boolean returnCount, Integer offset, Integer limit) {
        String sql = "";
        if (returnCount) {
            sql = "SELECT count(*) FROM inno_collections col";
        } else {
            sql = "SELECT col.col_id, col.name, col.owner, usr.username, col.approved FROM inno_collections col LEFT JOIN gpt_user usr ON (col.owner=usr.userid) ";
        }
        if (filter.size() > 0) {
            sql += " WHERE 1=1 ";
            if (filter.containsKey("col_id") && filter.get("col_id").toString().trim().length() > 0) {
                sql += " AND col.col_id=?";
            }
            if (filter.containsKey("name") && filter.get("name").toString().trim().length() > 0) {
                sql += " AND col.name=?";
            }
            if (filter.containsKey("name_like") && filter.get("name_like").toString().trim().length() > 0) {
                sql += " AND lower(col.name) LIKE ?";
            }
            if (filter.containsKey("owner") && filter.get("owner").toString().trim().length() > 0) {
                sql += " AND col.owner = ?";
            }
        }
        if (filter.containsKey("sortByField") && !returnCount) {
            sql += " ORDER BY  " + filter.get("sortByField").toString();
        }
        if (offset != null) {
            sql += " offset " + offset;
        }
        if (limit != null) {
            sql += " limit " + limit;
        }
        int indx = 0;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            ps = this.con.prepareStatement(sql);
            if (filter.size() > 0) {
                if (filter.containsKey("col_id") && filter.get("col_id").toString().trim().length() > 0) {
                    ps.setString(++indx, filter.get("col_id").toString());
                }
                if (filter.containsKey("name") && filter.get("name").toString().trim().length() > 0) {
                    ps.setString(++indx, filter.get("name").toString());
                }
                if (filter.containsKey("name_like") && filter.get("name_like").toString().trim().length() > 0) {
                    ps.setString(++indx, "%" + filter.get("name_like").toString().toLowerCase() + "%");
                }
                if (filter.containsKey("owner") && filter.get("owner").toString().trim().length() > 0) {
                    ps.setInt(++indx, Integer.parseInt(filter.get("owner").toString()));
                }
            }
            rs = ps.executeQuery();
        } catch (SQLException ex) {
            log.info("Deewendra there was problem in innoCollection.fnListCollection: SQL :" + sql + " ERROR : " + ex.toString());
        }
        return rs;
    }

    public ResultSet fnListCollectionMember(String col_id) {

        String sql = "SELECT colm.col_id, colm.docuuid,pres.title as parent_title, colm.role, colm.child_docuuid,cres.title as child_title "
                + " FROM inno_collection_member colm "
                + " LEFT JOIN gpt_resource pres ON (pres.docuuid = colm.docuuid) "
                + " LEFT JOIN gpt_resource cres ON (cres.docuuid = colm.child_docuuid) WHERE colm.col_id=? ORDER BY cres.title";

        int indx = 0;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            ps = this.con.prepareStatement(sql);
            ps.setString(1, col_id);
            rs = ps.executeQuery();
        } catch (SQLException ex) {
            log.info("Deewendra : " + ex.toString());
        }
        return rs;
    }
    
     public ResultSet fnCollection(String uuid) {

        String sql = "SELECT colm.col_id"
                + " FROM inno_collection_member colm "
                + " LEFT JOIN gpt_resource pres ON (pres.docuuid = colm.docuuid) "
                + " LEFT JOIN gpt_resource cres ON (cres.docuuid = colm.child_docuuid) WHERE colm.child_docuuid=? "
                + " OR colm.docuuid=? ORDER BY cres.title";

        int indx = 0;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            ps = this.con.prepareStatement(sql);
            ps.setString(1, uuid);
            ps.setString(2, uuid);
            rs = ps.executeQuery();
           
        } catch (SQLException ex) {
            log.info("Deewendra : " + ex.toString());
        }
        
        return rs;
    }
     

    public void updateModifiedDateForResources(String col_id) {
        String sql = "UPDATE gpt_resource SET updatedate=now() WHERE docuuid IN (SELECT docuuid FROM vw_collection_member WHERE col_id=?)";
        PreparedStatement ps = null;
        try {
            ps = this.con.prepareStatement(sql);
            ps.setString(1, col_id);
            ps.executeQuery();
        } catch (SQLException ex) {
            log.info("Deewendra : " + ex.toString());
        }
    }

    public int fnDeleteCollectionMember(String col_id) {
        this.updateModifiedDateForResources(col_id);
        int rs = 0;
        String sql = "DELETE FROM inno_collection_member WHERE col_id=?";
        PreparedStatement ps = null;
        try {
            ps = this.con.prepareStatement(sql);
            if (col_id.trim().length() > 0) {
                ps.setString(1, col_id);
            }
            rs = ps.executeUpdate();
        } catch (SQLException ex) {
            return -1;
        }
        return rs;

    }

    public int fnAddCollectionMember(HashMap data) {
        int ret = 0, i;
        Boolean approved = true;//spproval status of collection
        String sql = "", docuuid = data.containsKey("docuuid") ? data.get("docuuid").toString() : null;
        String child_docuuid = data.containsKey("child_docuuids") ? data.get("child_docuuids").toString() : null;
        String col_id = data.get("col_id").toString();
        //check if customn resource was added

        sql = "INSERT INTO inno_collection_member (col_id,docuuid,role,child_docuuid) VALUES (?,?,?,?)";
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            if (!((docuuid == null || docuuid.trim().equals("")) && (child_docuuid == null || child_docuuid.trim().equals("")))) {
                ps = this.con.prepareStatement(sql);
                ps.setString(1, col_id);
                if (docuuid == null || docuuid.trim().equals("")) {
                    ps.setNull(2, java.sql.Types.VARCHAR);
                } else {
                    ps.setString(2, docuuid);
                }
                ps.setString(3, data.get("role").toString());

                if (child_docuuid == null || child_docuuid.trim().equals("")) {
                    ps.setNull(4, java.sql.Types.VARCHAR);
                    ret += ps.executeUpdate();
                } else {
                    String[] child = data.get("child_docuuids").toString().split(",");
                    for (i = 0; i < child.length; i++) {
                        if (child[i].trim().length() > 0) {
                            ps.setString(4, child[i]);
                            ret += ps.executeUpdate();
                        }
                    }
                }
            }
        } catch (Exception ex) {
            log.info("Deewendra : " + ex.toString());
            return -1;
        }
        this.updateModifiedDateForResources(col_id);
        return ret;
    }

    public void fnAutoApproveCollection(String col_id, ArrayList currentUserGroups) {
        Boolean approved = true;
        String sql = "SELECT res.owner FROM (SELECT child_docuuid as docuuid,col_id FROM inno_collection_member UNION SELECT docuuid,col_id FROM inno_collection_member) colm "
                + " LEFT JOIN inno_collections col ON (colm.col_id=col.col_id) "
                + " LEFT JOIN gpt_resource res ON (colm.docuuid=res.docuuid) "
                + " WHERE res.owner!= col.owner AND colm.col_id=?";
        try {
            PreparedStatement ps = this.con.prepareStatement(sql);
            ps.setString(1, col_id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                if (!(currentUserGroups.contains(rs.getInt(1) + ""))) {
                    approved = false;
                    break;
                }
            }

            String approvedStatus = approved ? "Y" : "N";
            approvedStatus = "Y";
            sql = "UPDATE inno_collections SET approved = ? WHERE col_id=?";
            ps = this.con.prepareStatement(sql);
            ps.setString(1, approvedStatus);
            ps.setString(2, col_id);
            ps.executeUpdate();
        } catch (Exception ex) {
            log.info("Deewendra : " + ex.toString());
        }
    }

    public String fnAddInnoVirtualResource(String resourceName, RequestContext context, String docuuid, String owner) {
        SelectablePublishers selectablePublishers = new SelectablePublishers();
        selectablePublishers.build(context, false);
        Publisher publisher = null;

        try {
            publisher = selectablePublishers.selectedAsPublisher(context, false);
        } catch (Exception ex) {
            log.info("Deewendra : create publisher issue " + ex);
            return "0";
        }

        String xml = null;
        String fileName = null;

        if (docuuid == null) {
            //adding new resource
            try {
                String sql = "SELECT getnewguid()";
                Statement st = this.con.createStatement();
                ResultSet rs = st.executeQuery(sql);
                while (rs.next()) {
                    fileName = rs.getString(1);
                }

            } catch (Exception ex) {
                log.info("Deewendra : get guid issue " + ex);
                return "0";
            }
        } else {
            //updating new resource
            fileName = docuuid;
        }

        xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
                + "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:dcmiBox=\"http://dublincore.org/documents/2000/07/11/dcmi-box/\" xmlns:dct=\"http://purl.org/dc/terms/\" xmlns:ows=\"http://www.opengis.net/ows\">"
                + "<rdf:Description>"
                + "<dc:title>" + resourceName + "</dc:title>"
                + "<dc:type>EDG_VIRTUAL_RECORD</dc:type>"
                + "</rdf:Description>"
                + "</rdf:RDF>";

        UploadRequest request = new UploadRequest(context, publisher, fileName + ".xml", xml);
        try {
            request.publish();

            //update the approval status by default
            PreparedStatement ps = this.con.prepareStatement("UPDATE gpt_resource SET updatedate=now(),approvalstatus = ?,owner=?,acl=NULL WHERE docuuid=?");
            ps.setString(1, "approved");
            ps.setInt(2, Integer.parseInt(owner));
            ps.setString(3, fileName);
            ps.executeUpdate();

        } catch (Exception ex) {
            log.info("Deewendra : publish issue " + ex.toString());
            return "0";
        }
        return fileName;
    }

    public int fnDeleteInnoVirtualResource(String docuuid) {
        int ret = 0;
        ResultSet rs = null;
        PreparedStatement ps = null;
        ArrayList affectedCollections = new ArrayList();

        try {
            //have to update the last modification date on the resources
            ps = this.con.prepareStatement("SELECT DISTINCT col_id FROM inno_collection_member WHERE docuuid=? OR child_docuuid=?");
            ps.setString(1, docuuid);
            ps.setString(2, docuuid);
            rs = ps.executeQuery();
            while (rs.next()) {
                affectedCollections.add(rs.getString("col_id"));
            }

            ps = this.con.prepareStatement("DELETE FROM gpt_resource_data WHERE docuuid=?");
            ps.setString(1, docuuid);
            ret = ps.executeUpdate();

            ps = this.con.prepareStatement("DELETE FROM gpt_resource WHERE docuuid=?");
            ps.setString(1, docuuid);
            ret += ps.executeUpdate();

            //delete from member
            ps = this.con.prepareStatement("UPDATE inno_collection_member SET docuuid=null WHERE docuuid=?");
            ps.setString(1, docuuid);
            ret += ps.executeUpdate();

            ps = this.con.prepareStatement("DELETE FROM inno_collection_member WHERE child_docuuid=? OR (docuuid IS NULL AND child_docuuid IS NULL)");
            ps.setString(1, docuuid);
            ret += ps.executeUpdate();

            for (int i = 0, size = affectedCollections.size(); i < size; i++) {
                this.updateModifiedDateForResources(affectedCollections.get(i).toString());
            }


        } catch (SQLException ex) {
            log.info("Deewendra : class innoCollection - ERROR : " + ex.toString());
            return -1;
        }
        return ret;
    }

    public String fnAddCollection(HashMap data) {
        int indx = 0;
        String sql = "INSERT INTO inno_collections (name,owner,approved) VALUES (?,?,?) RETURNING col_id";
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            ps = this.con.prepareStatement(sql);
            ps.setString(++indx, data.get("name").toString());
            ps.setInt(++indx, Integer.parseInt(data.get("owner").toString()));
            ps.setString(++indx, data.get("approved").toString());

            rs = ps.executeQuery();
            while (rs.next()) {
                return rs.getString("col_id");
            }
            return null;
        } catch (SQLException ex) {
            Logger.getLogger(innoCollection.class.getName()).log(Level.SEVERE, null, "Deewendra -- SQL : " + sql + " -- parameters -- " + data.toString() + " -- error -- " + ex.toString());
            return null;
        }

    }

    public int fnDeleteCollection(String col_id, Boolean delete_orphaned_virtual_resources) {
        int ret = 0;
        ResultSet rs = null;
        PreparedStatement ps = null;
        ArrayList virtualResources = new ArrayList();

        String sql1 = "DELETE FROM inno_collection_member WHERE col_id=?";
        String sql2 = "DELETE FROM inno_collections WHERE col_id=?";
        String sql3 = "DELETE FROM gpt_resource_data WHERE docuuid=?";
        String sql4 = "DELETE FROM gpt_resource WHERE docuuid=?";

        if (delete_orphaned_virtual_resources) {
            try {
                String select = "SELECT cmem.docuuid,"
                        + " CASE WHEN EXISTS(SELECT NULL FROM vw_collection_member WHERE docuuid=cmem.docuuid AND col_id!=?) THEN '0' ELSE '1' END as can_delete "
                        + " FROM vw_collection_member cmem LEFT JOIN gpt_resource gptr ON (cmem.docuuid=gptr.docuuid) "
                        + " WHERE gptr.schema_key='virtual' AND cmem.col_id=?";

                ps = this.con.prepareStatement(select);
                ps.setString(1, col_id);
                ps.setString(2, col_id);
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (rs.getString("can_delete").toString().equals("1")) {
                        virtualResources.add(rs.getString("docuuid"));
                    }
                }
            } catch (SQLException ex) {
                //this.print(ex.toString());
                return ret;
            }
        }

        try {
            ps = this.con.prepareStatement(sql1);
            ps.setString(1, col_id);
            ret = ps.executeUpdate();

            ps = this.con.prepareStatement(sql2);
            ps.setString(1, col_id);
            ret = ps.executeUpdate();

            if (delete_orphaned_virtual_resources && virtualResources.size() > 0) {
                ps = this.con.prepareStatement(sql3);
                Iterator it = virtualResources.iterator();
                while (it.hasNext()) {
                    ps.setString(1, it.next().toString());
                    ps.executeUpdate();
                }

                ps = this.con.prepareStatement(sql4);
                it = virtualResources.iterator();
                while (it.hasNext()) {
                    ps.setString(1, it.next().toString());
                    ps.executeUpdate();
                }
            }

        } catch (SQLException ex) {
            //this.print(ex.toString());
            return ret;
        }
        return ret;
    }

    public int fnUpdateCollectionApprovalStatus(String col_id) {
        int ret = 0;
        String sql = "UPDATE inno_collections SET approved=(CASE WHEN approved='Y' THEN 'N' ELSE 'Y' END) WHERE col_id=?";
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            ps = this.con.prepareStatement(sql);
            ps.setString(1, col_id);
            ret = ps.executeUpdate();
        } catch (SQLException ex) {
            return ret;
        }
        return ret;
    }
    
    public int fnUpdateCollectionName(String collectionName , String col_id) {
        int ret = 0;
        String sql = "UPDATE inno_collections SET name=? WHERE col_id=?";
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            ps = this.con.prepareStatement(sql);
            ps.setString(1, collectionName);
            ps.setString(2, col_id);
            ret = ps.executeUpdate();
        } catch (SQLException ex) {
            return ret;
        }
        return ret;
    }

    public int fnListGptResourceCount(HashMap filter) {
        int count = 0;
        ResultSet rs = this.fnListGptResource(filter, true, null, null);


        try {
            while (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (Exception e) {
            return count;
        }
        return count;
    }

    public ResultSet fnListGptResource(HashMap filter, Integer offset, Integer limit) {
        return this.fnListGptResource(filter, false, offset, limit);
    }

    public ResultSet fnListGptResource(HashMap filter, Boolean returnCount, Integer offset, Integer limit) {
        int indx = 0;
        String sql = "SELECT count(*) as count ";
        LogUtil.getLogger().severe(" Baohong : ");
        if (!returnCount) {

            sql = " SELECT  gptres.docuuid, gptres.title, gptres.owner, gptres.inputdate,to_char(gptres.inputdate,'yyyy-mm-dd') as inputdatechr,gptres.updatedate,gptres.id,gptres.approvalstatus,gptres.pubmethod,gptres.siteuuid,gptres.sourceuri,"
                    + " gptres.fileidentifier,gptres.acl,gptres.host_url,gptres.protocol_type,gptres.protocol,gptres.frequency,gptres.send_notification,gptres.findable,gptres.searchable,gptres.lastsyncdate,"
                    + " gptres.schema_key,gptres.content_type,gptuser.username,CASE WHEN gptres.findable='true' THEN 'Unrestricted' ELSE 'Restricted' END as access,"
                    + " (CASE WHEN stat.approved='Y' THEN 'Approved' WHEN stat.approved='N' THEN 'Posted' ELSE 'Not a member' END) as status";

        }
        sql += " FROM gpt_resource gptres LEFT JOIN gpt_user gptuser ON (gptres.owner=gptuser.userid)"
                + " LEFT JOIN (SELECT mem.docuuid,mem.sort_index,col.approved FROM ("
                + " SELECT col_id,docuuid,0 as sort_index FROM inno_collection_member WHERE col_id=?"
                + " UNION SELECT col_id,child_docuuid,1 as sort_index FROM inno_collection_member  WHERE col_id=?) mem LEFT JOIN inno_collections col ON (mem.col_id=col.col_id)) stat ON (gptres.docuuid=stat.docuuid)"
                + " LEFT JOIN VW_COLLECTION_MEMBER vwcmem ON (gptres.docuuid=vwcmem.docuuid) ";

        if (filter.size() > 0) {

            sql += " WHERE 1=1 ";
            if (filter.containsKey("docuuid") && filter.get("docuuid").toString().trim().length() > 0) {
                sql += " AND gptres.docuuid=?";
            }
            if (filter.containsKey("title") && filter.get("title").toString().trim().length() > 0) {
                sql += " AND gptres.title = ?";
            }
            if (filter.containsKey("title_like") && filter.get("title_like").toString().trim().length() > 0) {
                sql += " AND lower(gptres.title) LIKE ?";
            }
            if (filter.containsKey("owner") && filter.get("owner").toString().trim().length() > 0) {
                sql += " AND gptres.owner = ?";
            }
            if (filter.containsKey("current_members_only") && filter.get("current_members_only").toString().trim().length() > 0) {
                sql += " AND vwcmem.col_id=?";
            }
            if (filter.containsKey("approvalstatus") && filter.get("approvalstatus").toString().trim().length() > 0) {
                sql += " AND gptres.approvalstatus = ?";
            }
            if (filter.containsKey("strictfilter") && filter.get("strictfilter").toString().trim().length() > 0) {
                sql += " AND (vwcmem.col_id=? OR vwcmem.col_id IS NULL)";
            }
            if (filter.containsKey("owner_in")) {
                sql += " AND gptres.owner IN (" + ((LinkedHashMap) filter.get("owner_in")).keySet().toString().replace("[", "").replace("]", "") + ")";
            }
        }

        if (!returnCount) {
            if (filter.containsKey("col_id") && filter.get("col_id").toString().trim().length() > 0) {
                sql += " ORDER BY  stat.sort_index ASC";
                if (filter.containsKey("sortByField")) {
                    sql += " ," + filter.get("sortByField").toString();
                }
            } else if (filter.containsKey("sortByField")) {
                sql += " ORDER BY  " + filter.get("sortByField").toString();
            }
            if (offset != null) {
                sql += " offset " + offset;
            }
            if (limit != null) {
                sql += " limit " + limit;

            }
        }
        LogUtil.getLogger().severe(" Deewendra - SQL : " + sql);
        ResultSet rs = null;
        PreparedStatement ps;

        try {
            ps = this.con.prepareStatement(sql);
            if (filter.size() > 0) {
                if (filter.containsKey("col_id") && filter.get("col_id").toString().trim().length() > 0) {
                    ps.setString(++indx, filter.get("col_id").toString());
                    ps.setString(++indx, filter.get("col_id").toString());
                }


                if (filter.containsKey("docuuid") && filter.get("docuuid").toString().trim().length() > 0) {
                    ps.setString(++indx, filter.get("docuuid").toString());
                }
                if (filter.containsKey("title") && filter.get("title").toString().trim().length() > 0) {
                    ps.setString(++indx, filter.get("title").toString());
                }
                if (filter.containsKey("title_like") && filter.get("title_like").toString().trim().length() > 0) {
                    ps.setString(++indx, "%" + filter.get("title_like").toString().toLowerCase() + "%");
                }
                if (filter.containsKey("owner") && filter.get("owner").toString().trim().length() > 0) {
                    ps.setInt(++indx, Integer.parseInt(filter.get("owner").toString()));
                }
                if (filter.containsKey("current_members_only") && filter.get("current_members_only").toString().trim().length() > 0) {
                    ps.setString(++indx, filter.get("col_id").toString());
                }
                if (filter.containsKey("approvalstatus") && filter.get("approvalstatus").toString().trim().length() > 0) {
                    ps.setString(++indx, filter.get("approvalstatus").toString());
                }
                if (filter.containsKey("strictfilter") && filter.get("strictfilter").toString().trim().length() > 0) {
                    ps.setString(++indx, filter.get("col_id").toString());
                }
            }
            rs = ps.executeQuery();
        } catch (SQLException ex) {
            LogUtil.getLogger().severe(" Deewendra - SQL : " + sql + " ERROR : " + ex.toString());
        }

        return rs;
    }

    public ResultSet fnListGptUser() {
        String sql = "SELECT userid,dn,username FROM gpt_user ORDER BY username";
        ResultSet rs = null;
        PreparedStatement ps;
        try {
            ps = this.con.prepareStatement(sql);
            rs = ps.executeQuery();
        } catch (SQLException ex) {
            Logger.getLogger(innoCollection.class.getName()).log(Level.SEVERE, null, " Deewendra - " + ex.toString());
        }
        return rs;
    }

    public String getParentResource(String docuuid) {
        String returnVal = "";
        String sql = "SELECT distinct docuuid FROM inno_collection_member WHERE child_docuuid = ?";
        PreparedStatement ps = null;

        try {
            ps = this.con.prepareStatement(sql);
            ps.setString(1, docuuid);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                returnVal += "," + rs.getString("docuuid");
            }
            if (returnVal.length() > 0) {
                return returnVal.substring(1);
            } else {
                return null;
            }
        } catch (SQLException ex) {
            Logger.getLogger(innoCollection.class.getName()).log(Level.SEVERE, null, " Deewendra - " + ex.toString());
            return null;
        }
    }

    public String getChildResource(String docuuid) {
        String returnVal = "";
        String sql = "SELECT distinct child_docuuid FROM inno_collection_member WHERE docuuid = ?";
        PreparedStatement ps = null;

        try {
            ps = this.con.prepareStatement(sql);
            ps.setString(1, docuuid);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                returnVal += "," + rs.getString("child_docuuid");
            }
            if (returnVal.length() > 0) {
                return returnVal.substring(1);
            } else {
                return null;
            }
        } catch (SQLException ex) {
            Logger.getLogger(innoCollection.class.getName()).log(Level.SEVERE, null, " Deewendra - " + ex.toString());
            return null;
        }
    }

    public int fnListVirtualResourceCount(HashMap filter) {
        int count = 0;
        ResultSet rs = this.fnListVirtualResource(filter, true, null, null);
        try {
            while (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (Exception e) {
            return count;
        }
        return count;
    }

    public ResultSet fnListVirtualResource(HashMap filter, Integer offset, Integer limit) {
        return this.fnListVirtualResource(filter, false, offset, limit);
    }

    public ResultSet fnListVirtualResource(HashMap filter, Boolean returnCount, Integer offset, Integer limit) {
        int indx = 0;
        String sql = "SELECT count(*) as count FROM gpt_resource vres ";
        if (!returnCount) {

            sql = "SELECT  vres.docuuid, vres.title, vres.owner, gptuser.username, COALESCE(ccnt.member_count,0) as isChild, COALESCE(pcnt.member_count,0) as isParent"
                    + ",CASE WHEN ccnt.member_count>0 AND pcnt.member_count>0 THEN 'Parent/Child' WHEN ccnt.member_count>0 THEN 'Child' WHEN pcnt.member_count>0 THEN 'Parent' ELSE 'Orphan' END as resource_role"
                    + " FROM gpt_resource vres LEFT JOIN gpt_user gptuser ON (vres.owner=gptuser.userid) "
                    + " LEFT JOIN (SELECT docuuid,count(*) as member_count FROM inno_collection_member GROUP BY docuuid) pcnt ON (vres.docuuid=pcnt.docuuid) "
                    + " LEFT JOIN (SELECT child_docuuid,count(*) as member_count FROM inno_collection_member GROUP BY child_docuuid) ccnt ON (vres.docuuid= ccnt.child_docuuid) ";

        }
        sql += " WHERE schema_key='virtual'";
        if (filter.size() > 0) {
            if (filter.containsKey("docuuid") && filter.get("docuuid").toString().trim().length() > 0) {
                sql += " AND vres.docuuid=?";
            }
            if (filter.containsKey("title") && filter.get("title").toString().trim().length() > 0) {
                sql += " AND lower(vres.title) = ?";
            }
            if (filter.containsKey("title_like") && filter.get("title_like").toString().trim().length() > 0) {
                sql += " AND lower(vres.title) LIKE ?";
            }
            if (filter.containsKey("owner") && filter.get("owner").toString().trim().length() > 0) {
                sql += " AND vres.owner = ?";
            }
        }
        if (filter.containsKey("sortByField") && !returnCount) {
            sql += " ORDER BY  " + filter.get("sortByField").toString();
        }
        if (offset != null) {
            sql += " offset " + offset;
        }
        if (limit != null) {
            sql += " limit " + limit;

        }

        ResultSet rs = null;
        PreparedStatement ps;
        //log("Deewendra - "+sql);
        //log("Deewendra - "+filter.get("title_like").toString().toLowerCase());

        try {
            ps = this.con.prepareStatement(sql);
            if (filter.size() > 0) {
                if (filter.containsKey("docuuid") && filter.get("docuuid").toString().trim().length() > 0) {
                    ps.setString(++indx, filter.get("docuuid").toString());
                }
                if (filter.containsKey("title") && filter.get("title").toString().trim().length() > 0) {
                    ps.setString(++indx, filter.get("title").toString().toLowerCase().trim());
                }
                if (filter.containsKey("title_like") && filter.get("title_like").toString().trim().length() > 0) {
                    ps.setString(++indx, "%" + filter.get("title_like").toString().toLowerCase() + "%");
                }
                if (filter.containsKey("owner") && filter.get("owner").toString().trim().length() > 0) {
                    ps.setInt(++indx, Integer.parseInt(filter.get("owner").toString()));
                }
            }
            rs = ps.executeQuery();
        } catch (SQLException ex) {
            Logger.getLogger(innoCollection.class.getName()).log(Level.SEVERE, null, " Deewendra - " + ex.toString());
        }

        return rs;
    }

    public HashMap getResourceParentChildChk() {
        HashMap ret = new HashMap();
        HashMap tmp = null;
        PreparedStatement ps = null;
        /*
         String sql = "SELECT res.docuuid"
         + " ,CASE WHEN EXISTS (SELECT NULL FROM inno_collection_member WHERE docuuid=res.docuuid) THEN 1 ELSE 0 END as haschild"
         + " ,CASE WHEN EXISTS (SELECT NULL FROM inno_collection_member WHERE child_docuuid=res.docuuid) THEN 1 ELSE 0 END as hasparent"
         + " FROM gpt_resource res "
         + " JOIN (select docuuid FROM inno_collection_member cm LEFT JOIN inno_collections col ON (cm.col_id = col.col_id) WHERE col.approved='Y' UNION"
         + " SELECT child_docuuid FROM inno_collection_member cm LEFT JOIN inno_collections col ON (cm.col_id = col.col_id) WHERE col.approved='Y') cm ON (res.docuuid=cm.docuuid)";
         */
        String sql = "SELECT res.docuuid,CASE WHEN parentCount.cnt IS NULL THEN 0 ELSE 1 END as haschild,CASE WHEN childCount.cnt IS NULL THEN 0 ELSE 1 END as hasparent"
                + " FROM gpt_resource res "
                + " JOIN (select docuuid FROM inno_collection_member cm LEFT JOIN inno_collections col ON (cm.col_id = col.col_id) WHERE col.approved='Y' UNION"
                + " SELECT child_docuuid FROM inno_collection_member cm LEFT JOIN inno_collections col ON (cm.col_id = col.col_id) WHERE col.approved='Y') cm ON (res.docuuid=cm.docuuid)"
                + " LEFT JOIN (SELECT count(*) as cnt, docuuid FROM inno_collection_member cm LEFT JOIN inno_collections inc ON (cm.col_id=inc.col_id) WHERE inc.approved='Y' GROUP BY docuuid) parentCount"
                + " ON (res.docuuid=parentCount.docuuid)"
                + " LEFT JOIN (SELECT count(*) as cnt, child_docuuid as docuuid FROM inno_collection_member cm LEFT JOIN inno_collections inc ON (cm.col_id=inc.col_id) WHERE inc.approved='Y' GROUP BY child_docuuid) childCount"
                + " ON (res.docuuid=childCount.docuuid)";



        try {
            ps = this.con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                tmp = new HashMap();
                tmp.put("haschild", rs.getInt("haschild"));
                tmp.put("hasparent", rs.getInt("hasparent"));

                ret.put(rs.getString("docuuid"), tmp);
            }
        } catch (SQLException ex) {
            Logger.getLogger(innoCollection.class.getName()).log(Level.SEVERE, null, " Deewendra - " + ex.toString());
        }
        return ret;
    }
}