/* See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * Esri Inc. licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.gpt.framework.jsf; 

import javax.faces.component.UIComponent;   
import javax.faces.context.FacesContext;   
import javax.faces.convert.Converter;   
 
import com.esri.gpt.catalog.search.ResourceLink;
import com.esri.gpt.catalog.search.ResourceLinks;
import com.esri.gpt.framework.util.Val; 

/**
 * The Class ConverterLinksToHtml.  Converts resource links
 * to HTML
 */
public class ConverterLinksToHtml implements Converter {

// methods =====================================================================
/**
 * Gets the string as an object.
 *
 * @param arg0 the arg0
 * @param arg1 the arg1
 * @param arg2 the String to be converted to the list
 *
 * @return the as object
 */
public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2) {

  return null;
}

/**
 * Gets the List object as string.
 *
 * @param arg0 the arg0
 * @param arg1 the arg1
 * @param arg2 the List Object
 *
 * @return the as string
 */
public String getAsString(FacesContext arg0, UIComponent arg1, Object arg2) {

  if(!(arg2 instanceof ResourceLinks)) {
    return "";
  }
  ResourceLinks links = (ResourceLinks) arg2;

  StringBuffer sb = new StringBuffer();
  for (ResourceLink link: links) {
    String target = link.getTarget();
    String tag = link.getTag();
    //if (tag.equals(ResourceLink.TAG_DETAILS) || (tag.equals(ResourceLink.TAG_PREVIEW) && !link.isForExtenalRecord())) {
    //  target = "_top";
    //}
    appendLink(sb,link.getUrl(),link.getLabel(), target);
  }
  return sb.toString();
}


private void appendLink(StringBuffer sb, String url, String text,
    String target) {
  url = Val.chkStr(url);
  text = Val.chkStr(text);
  if (url.length() > 0) {
    sb.append("<a href=\"");
    // for epageoplatform link, remove final "/data" if it exists on "Open" link
    if (url.contains("epageoplatform") && "Open".equals(text) && url.endsWith("/data")) {
        url = url.substring(0, url.indexOf("/data"));
    }
    sb.append(Val.escapeXmlForBrowser(url));
    // for metadata link, add xsl parm, and generate invisible csv link
    if ("Metadata".equals(text)) {
        if (url.contains("?"))
            sb.append("&");
        else
            sb.append("?");
        sb.append("xsl=metadata_to_html_full");
        sb.append("\" class=\"resultsLink\" ");
        sb.append(" target=\""+ target + "\" ");
        sb.append(">");
        sb.append(Val.escapeXmlForBrowser(text));
        sb.append("</a>");
        // csv link
        sb.append("<a href=\"");
        String csvUrl = url.replace("rest/document?id", "DownloadMetadataAsCsv.csv?uuids");
        sb.append(Val.escapeXmlForBrowser(csvUrl));
        sb.append("\" class=\"resultsLink csvLink\" ");
        sb.append("\" style=\"display:none\"");  // invisible initially
        text = "CSV";
    } else
        sb.append("\" class=\"resultsLink\" ");
    sb.append(" target=\""+ target + "\" ");
    sb.append(">");
    sb.append(Val.escapeXmlForBrowser(text));
    sb.append("</a>");
  }
}

}
