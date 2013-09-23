/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.innovateteam.gpt;

import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.PageContext;
import com.esri.gpt.framework.jsf.RoleMap;
import com.esri.gpt.framework.security.identity.IdentityConfiguration;
import com.esri.gpt.framework.security.principal.Group;
import com.esri.gpt.framework.security.principal.Groups;
import com.esri.gpt.framework.security.principal.Publisher;
import com.esri.gpt.framework.security.principal.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Deewen
 */
public class innoHelperFunctions {

    private static Logger log = Logger.getLogger("com.esri.gpt");
    private HashMap gptUsers = new HashMap();

    public String getValFromMap(HashMap map, String key) {
        if (map.containsKey(key)) {
            if (map.get(key).toString().trim().length() > 0) {
                return map.get(key).toString().trim();
            } else {
                return "";
            }
        }
        return "";
    }

    public String getHeadingClass(HashMap map, String field) {
        if (map.containsKey("frm:sortField") && map.get("frm:sortField").toString().equals(field)) {
            if (map.containsKey("frm:sortDirection") && map.get("frm:sortDirection").toString().equals("desc")) {
                return "class=\"descending\"";
            } else {
                return "class=\"ascending\"";
            }
        }
        return "";
    }

    public String getSortByField(HashMap map) {
        String ret = null;
        if (map.containsKey("frm:sortField")) {
            ret = map.get("frm:sortField").toString();
        }
        if (map.containsKey("frm:sortDirection")) {
            ret += " " + map.get("frm:sortDirection").toString();
        }
        return ret;
    }

    public String getCurrentUserID(HttpServletRequest request) {
        HttpSession session = request.getSession();
        User curUser = (User) session.getAttribute("com.esri.gpt.user");
        return curUser.getLocalID() + "";

        //return "2";
        /*
         HttpSession session = request.getSession();
         String sessionVal = session.getAttribute("com.esri.gpt.user") != null ? session.getAttribute("com.esri.gpt.user").toString() : "";
         //String sessionVal = "com.esri.gpt.framework.security.principal.User: key=\"cn=gptadmin,ou=users,ou=system\" distinguishedName=\"cn=gptadmin,ou=users,ou=system\" localID=\"1\" name=\"gptadmin\"";
        
         Pattern pattern = Pattern.compile("localID\\=\\\"(-?\\d+)\\\"");
         Matcher matcher = pattern.matcher(sessionVal);
         while (matcher.find()) {
         //System.out.print("Start index: " + matcher.start());
         //System.out.print(" End index: " + matcher.end() + " ");
         return matcher.group(1);
         }
         return null;
         */
    }

    public Boolean isAdminUser(HttpServletRequest request) {
        try {
            Publisher pub = new Publisher(RequestContext.extract(request));
            return pub.getIsAdministrator();
        } catch (Exception ex) {
            Logger.getLogger(innoHelperFunctions.class.getName()).info(ex.getMessage());
            return false;
        }
    }

    public Boolean isAdminUser() {
        PageContext pc = new PageContext();
        RoleMap roles = pc.getRoleMap();
        return roles.get("gptAdministrator");
    }

    public void setGptUsers(HttpServletRequest req) {
        innoCollection obj = new innoCollection(RequestContext.extract(req));
        ResultSet rs = obj.fnListGptUser();
        try {
            while (rs.next()) {
                this.gptUsers.put(rs.getString("dn"), rs.getString("userid"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(innoHelperFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ArrayList<String> getCurrentUserGroups(HttpServletRequest request) {
        ArrayList grps = new ArrayList();
        HttpSession session = request.getSession();
        User curUser = (User) session.getAttribute("com.esri.gpt.user");
        Groups grp = curUser.getGroups();
        for (Group mgmtGroup : grp.values()) {
            if (this.gptUsers.containsKey(mgmtGroup.getDistinguishedName())) {
                grps.add(this.gptUsers.get(mgmtGroup.getDistinguishedName()));
            }
        }
        return grps;
    }
    
    //Modified for the sorting
    public LinkedHashMap listAllMetadataManagementGroups(HttpServletRequest req) {
        LinkedHashMap groups = new LinkedHashMap();
        HashMap unsortedGroups = new HashMap();
        ArrayList sortedGroupNames = new ArrayList();
        IdentityConfiguration idConfig = RequestContext.extract(req).getIdentityConfiguration();
        Groups mgmtGroups = idConfig.getMetadataManagementGroups();
        for (Group mgmtGroup : mgmtGroups.values()) {
            if (this.gptUsers.containsKey(mgmtGroup.getDistinguishedName())) {
                unsortedGroups.put(mgmtGroup.getName(),this.gptUsers.get(mgmtGroup.getDistinguishedName()));
                sortedGroupNames.add(mgmtGroup.getName());
            }
        }
        Collections.sort(sortedGroupNames);
        for(int i=0;i<sortedGroupNames.size();i++){
            groups.put(unsortedGroups.get(sortedGroupNames.get(i)), sortedGroupNames.get(i));
        } 
        return groups;
    }

    public String getCurrentUserPublisherGroupKey(HttpServletRequest request) {
        ArrayList curUserGrps = this.getCurrentUserGroups(request);
        LinkedHashMap publisherGroups = this.listAllMetadataManagementGroups(request);
        Set s = publisherGroups.keySet();
        Iterator it = s.iterator();
        String pgKey = null;
        while (it.hasNext()) {
            pgKey = it.next().toString();
            if (curUserGrps.contains(pgKey)) {
                return pgKey;
            }
        }
        return null;
    }

    public void updateBrowseTreeOwner(JSONObject obj, HashMap owner_groups) {
        if (obj.has("children")) {

            try {
                JSONArray ja = obj.getJSONArray("children");
                for (int i = 0, len = ja.length(); i < len; i++) {
                    this.updateBrowseTreeOwner(ja.getJSONObject(i),owner_groups);
                }
            } catch (JSONException ex) {
                Logger.getLogger(innoHelperFunctions.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else {


            try {
                String grpname = obj.getString("name");
                if (owner_groups.containsKey(grpname)) {
                    
                    ArrayList children = new ArrayList();
                    children = (ArrayList) owner_groups.get(grpname);
                    HashMap child = new HashMap();
                    JSONArray tmp = new JSONArray();
                    
                    for (int j = 0, slen = children.size(); j < slen; j++) {
                        child = (HashMap) children.get(j);
                        tmp.put(new JSONObject("{\"query\": \"collection=" + child.get("id").toString() + "&xsl=metadata_to_html_full\",\"name\": \"" + child.get("name").toString() + "\"}"));
                    }

                    obj.put("children", tmp);


                } else {
                    obj.remove("children");
                }
            } catch (JSONException ex) {
                Logger.getLogger(innoHelperFunctions.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    public String addCollectionDataToTocTreeResponse(String json, HttpServletRequest req) {
        String owner_to_update = "Owner";
        HashMap owner_groups = new HashMap();
        ArrayList collections = new ArrayList();
        HashMap collection = new HashMap();

        innoCollection innocol = new innoCollection(RequestContext.extract(req));
        ResultSet rs = innocol.fnListCollection(new HashMap(), null, null);
        try {
            while (rs.next()) {
                collection = new HashMap();
                collections = new ArrayList();

                String username = rs.getString("username");
                collection.put("id", rs.getString("col_id"));
                collection.put("name", rs.getString("name"));

                if (owner_groups.containsKey(username)) {
                    collections = (ArrayList) owner_groups.get(username);
                }
                collections.add(collection);
                owner_groups.put(username, collections);
            }
        } catch (SQLException ex) {
            Logger.getLogger(innoHelperFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }


        Boolean found = false;

        try {
            JSONObject obj = new JSONObject(json);
            //get the items
            JSONArray jarr = obj.getJSONArray("items");
            JSONObject jobj = jarr.getJSONObject(0);//is items object
            jarr = jobj.getJSONArray("children");//get the chilren array
            //iterate through the children to get the "owner" object and set that object to jobj
            for (int i = 0, len = jarr.length(); i < len; i++) {
                jobj = jarr.getJSONObject(i);
                if (jobj.getString("name").equalsIgnoreCase("owner")) {
                    found = true;
                    break;
                }
            }
            if (found) {
                found = false;
                //get "owner" object's children
                jarr = jobj.getJSONArray("children");
                //iterate through the children to get the "owner_to_update" object and set that object to jobj
                for (int i = 0, len = jarr.length(); i < len; i++) {
                    jobj = jarr.getJSONObject(i);
                    this.updateBrowseTreeOwner(jobj,owner_groups);
                    
                    /*
                    if (jobj.getString("name").equalsIgnoreCase(owner_to_update)) {
                        found = true;
                        break;
                    }*/
                }
                /*
                if (found) {
                    //get "dev_owner" object's children
                    jarr = jobj.getJSONArray("children");
                    //iterate through all the children and add/update therir children(collection)
                    for (int i = 0, len = jarr.length(); i < len; i++) {
                        jobj = jarr.getJSONObject(i);
                        String grpname = jobj.getString("name");
                        if (owner_groups.containsKey(grpname)) {
                            jobj.remove("children");

                            ArrayList children = new ArrayList();
                            children = (ArrayList) owner_groups.get(grpname);
                            HashMap child = new HashMap();
                            JSONArray tmp = new JSONArray();
                            for (int j = 0, slen = children.size(); j < slen; j++) {
                                child = (HashMap) children.get(j);
                                tmp.put(new JSONObject("{\"query\": \"collectionlist=" + child.get("id").toString() + "&xsl=metadata_to_html_full\",\"name\": \"" + child.get("name").toString() + "\"}"));
                            }

                            jobj.put("children", tmp);


                        } else {
                            jobj.remove("children");
                        }
                    }
                }
                */
            }




            return obj.toString(5);
        } catch (JSONException ex) {
            log.finer("Deewendra :Issue in innoHelperFunctions.java-> addCollectionDataToTocTreeResponse " + ex.getMessage());
            return json;
        }

    }
}
