/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.innovateteam.gpt;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Deewen
 */
public class innoPaging {

    private HashMap settings;

    /*
     * current_page
     * records_per_page 
     * pages_per_page 
     * total_records
     */
    public innoPaging() {
        this.settings = new HashMap();
        try {
            this.settings.put("current_page", 1);
            this.settings.put("records_per_page", 50);
            this.settings.put("links_per_page", 10);
        } catch (Exception e) {
            Logger.getLogger(innoPaging.class.getName()).log(Level.SEVERE, null, e);
            Logger.getLogger(innoPaging.class.getName()).log(Level.SEVERE, null, " Deewendra - "+e.toString());
        }
    }

    public innoPaging(HashMap settings) {
        this();
        String indx;
        Set s = settings.keySet();
        Iterator it = s.iterator();
        while (it.hasNext()) {
            indx = (String) it.next();
            this.settings.put(indx, settings.get(indx));
        }
    }

    public int getRecordsPerPage() {
        return Integer.parseInt(this.settings.get("records_per_page").toString());
    }

    public int getCurrentPage() {
        return Integer.parseInt(this.settings.get("current_page").toString());
    }

    public void setCurrentPage(int page) {
        this.settings.put("current_page", page);
    }

    public void setCurrentPage(String page) {
        if (page != null) {
            this.settings.put("current_page", Integer.parseInt(page));
        }
    }

    public void setTotalRecords(int count) {
        this.settings.put("total_records", count);
    }

    public void setTotalRecords(String count) {
        if (count != null) {
            this.settings.put("total_records", Integer.parseInt(count));
        }
    }

    public void setTotalRecords(ResultSet rs) {
        try {
            this.settings.put("total_records", rs.getInt(1));
        } catch (Exception e) {
            this.settings.put("total_records", 0);
        }

    }

    public int getOffset() {
        return (this.getCurrentPage() == 1) ? 0 : (this.getCurrentPage() - 1) * this.getRecordsPerPage();
    }

    public int getLimit() {
        return this.getRecordsPerPage();
    }

    public String generateLinks() {
        int startRecord = 0, endRecord = 0, records_per_page = Integer.parseInt(this.settings.get("records_per_page").toString()), total_records = Integer.parseInt(this.settings.get("total_records").toString()), startPage = 1, endPage = Integer.parseInt(this.settings.get("links_per_page").toString()), current_page = Integer.parseInt(this.settings.get("current_page").toString()), totalPages = (int) (Math.ceil(total_records / ((double) records_per_page))), links_per_page = Integer.parseInt(this.settings.get("links_per_page").toString());

        if (Integer.parseInt(this.settings.get("total_records").toString()) <= records_per_page) {
            return "";
        }
        //for calculating record numbers
        if (current_page == 1) {
            startRecord = 1;
        } else {
            startRecord = this.getOffset() + 1;
        }
        endRecord = startRecord + records_per_page - 1;
        if (endRecord > total_records) {
            endRecord = total_records;
        }

        //for calculating page numbers
        if (current_page != 1) {
            if (current_page - Math.floor(links_per_page / 2) < 1) {
                startPage = 1;
            } else {
                startPage = current_page - (links_per_page / 2);
            }
        } else {
            startPage = 1;
        }
        endPage = startPage + links_per_page;

        if (endPage > totalPages) {
            endPage = totalPages;
        }

        String html = "<div class=\"nav\"><span class=\"result\">Results " + startRecord + "-" + endRecord + " of " + total_records + " record(s)</span>";

        if (current_page != 1) {
            html += "<a href=\"#\" onClick=\"goToPage(1);\" >First</a>";
            html += "<a href=\"#\" onClick=\"goToPage(" + (current_page - 1) + ");\" >&lt;</a>";
        }

        while (startPage <= endPage) {
            if (startPage == current_page) {

                html += "<a href=\"#\" class=\"current\">" + startPage + "</a>";
            } else {
                html += "<a href=\"#\" onClick=\"goToPage(" + startPage + ");\">" + startPage + "</a>";
            }



            startPage++;
        }

        if (current_page != endPage) {
            html += "<a href=\"#\" onClick=\"goToPage(" + (current_page + 1) + ");\" >&gt;</a>";
            html += "<a href=\"#\" onClick=\"goToPage(" + totalPages + ");\" >Last</a>";
        }

        html += "</div>";
        return html;
    }
}