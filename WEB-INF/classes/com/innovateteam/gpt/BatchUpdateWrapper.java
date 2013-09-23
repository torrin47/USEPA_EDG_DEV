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
package com.innovateteam.gpt;     
import com.esri.gpt.framework.collection.StringAttributeMap;  
import com.esri.gpt.framework.context.RequestContext;     
import com.esri.gpt.framework.scheduler.IScheduledTask;
import com.esri.gpt.framework.sql.ManagedConnection; 
import com.esri.gpt.catalog.context.CatalogConfiguration;    
import com.esri.gpt.catalog.lucene.LuceneConfig;  

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.Date;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Background thread to optimize the Lucene index.
 */
public class BatchUpdateWrapper implements Runnable, IScheduledTask {

  /** class variables ========================================================= */
    private static Logger LOGGER = Logger.getLogger(BatchUpdateWrapper.class.getName());

    /** instance variables ====================================================== */
    private StringAttributeMap parameters = null;
    private boolean            wasInterrupted = false;

    /** constructors  =========================================================== */

    /** Default constructor. */
    public BatchUpdateWrapper() {}

    /**
    * Sets the configuration paramaters for the task.
    * @param parameters the configuration paramaters
    */
    public void setParameters(StringAttributeMap parameters) {
        this.parameters = parameters;
    }

  /** methods ================================================================= */


    /**
     * Run the UpdateContentType process.
     */
    public void run() {
        LOGGER.info("Innovate UpdateContentTypeWrapper thread run started...");
        Connection con = null;
        try {
            ManagedConnection mc = RequestContext.extract(null).getConnectionBroker().returnConnection("");
            con = mc.getJdbcConnection();
            String restServiceURL = parameters.getValue("restServiceURL");
            String period = parameters.getValue("period");
            LOGGER.info("restServiceURL: "+restServiceURL);
            LOGGER.info("period: "+period);
            // if no restServiceURL configed, assume want to do lucene instead
            batchUpdate batchUpdate = null;
            if ((restServiceURL==null) || (restServiceURL.trim().length()==0)) {
                CatalogConfiguration catConf = RequestContext.extract(null).getCatalogConfiguration();
                String luceneIndexLoc = catConf.getLuceneConfig().getIndexLocation();
                if ((luceneIndexLoc==null) || (luceneIndexLoc.trim().length()==0)) {
                    LOGGER.severe("Could not get lucene index location, batch update not run");
                    return;
                }
                LOGGER.info("Using lucene index loc: "+luceneIndexLoc);
                File luceneIndexDir = new File(luceneIndexLoc);
                batchUpdate = new batchUpdate(luceneIndexDir,LOGGER,con,period);
            } else {
                batchUpdate = new batchUpdate(restServiceURL,LOGGER,con,period);
            }
            batchUpdate.doUpdate();
            LOGGER.info("batchUpdate instantiated");
            return;
         }  catch (SQLException se) {
            LOGGER.severe("SQLException: "+se.getMessage());
            return;
        }
        catch (Exception e) {
            LOGGER.severe("Exception: "+e.getMessage());
            return;
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (Exception f) {}
            }
        }
    }

}
