/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.innovateteam.gpt;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 *
 * @author Deewen
 */
public class LuceneSearch {

    private IndexSearcher searcher;
    private Analyzer analyzer;
    private QueryParser parser;
    private String searchField;

    public LuceneSearch(File indexDir) {
        
        this.searchField = "uuid";
        try {
            this.searcher = new IndexSearcher(FSDirectory.open(indexDir));
	    // Searches fail with StandardAnalyzer
            //this.analyzer = new StandardAnalyzer(Version.LUCENE_29);
            this.analyzer = new WhitespaceAnalyzer();
            this.parser = new QueryParser(Version.LUCENE_29,this.searchField, this.analyzer);
        } catch (CorruptIndexException ex) {
            System.out.println(ex.toString());
            Logger.getLogger(LuceneSearch.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            System.out.println(ex.toString());
            Logger.getLogger(LuceneSearch.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public String search(String searchValue) {
        searchValue = searchValue.replaceAll("\\{", "\\\\{");
        searchValue = searchValue.replaceAll("\\}", "\\\\}");
        
        try {
            Query query = this.parser.parse(searchValue);
            ScoreDoc[] hits = this.searcher.search(query, 1).scoreDocs;
            if (hits.length > 0) {
                Document hitDoc = this.searcher.doc(hits[0].doc);
                String ct = hitDoc.get("contentType");
            	//System.out.println(ct);
                return ct;
                //return hitDoc.get("contentType");
            }
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
        //System.out.println("No hit!");
        return null;
    }
    
}
