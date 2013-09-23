<% // dataBody.jsp -  (JSF body) %>
<%@taglib prefix="f" uri="http://java.sun.com/jsf/core"%>
<%@taglib prefix="h" uri="http://java.sun.com/jsf/html"%>


<h:outputText escape="false" styleClass="prompt" value="#{gptMsg['catalog.data.home.prompt']}"/>

<f:verbatim>
    <br/><br/>
    <table width="100%">
        <tbody>
            <tr>
                <td width="8%">
                    <a href="/data" style="border-bottom:none;background-color: transparent;">
                        <img src="../images/data_download.png" alt="Data" title="Data"/>
                    </a>
                </td>
                <td valign="top">
                    <div class="prompt">
                         <a href="/data"><b>EDG Data Download Locations</b></a><br/>
                            Download data posted by EPA's Regions, Programs, or Labs. This site allows you to browse
                            through the folders set up for each group and download files individually.
                    </div>
                </td>
            </tr>
            <tr>
                <td>
                    <a href="https://edg.epa.gov/clipship/" style="border-bottom:none;background-color: transparent;">
                        <img src="../images/clip.png" alt="Clip" title="Clip"/>
                    </a>
                </td>
                <td valign="top">
                    <div class="prompt">
                        <a href="https://edg.epa.gov/clipship/"><b>EPA's Clip N Ship Site</b></a><br/>
                        Select and download data for your area of interest. This interactive site presents EPA data in a map viewer
                        and allows you to view the data, select your area of interest, and choose an
                        output format to save the content locally.
                    </div>
                </td>
            </tr>

        </tbody>
    </table>
</f:verbatim>

