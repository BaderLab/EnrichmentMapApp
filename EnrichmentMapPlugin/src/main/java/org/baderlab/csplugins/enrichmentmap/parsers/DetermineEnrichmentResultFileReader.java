/**
 **                       EnrichmentMap Cytoscape Plugin
 **
 ** Copyright (c) 2008-2009 Bader Lab, Donnelly Centre for Cellular and Biomolecular 
 ** Research, University of Toronto
 **
 ** Contact: http://www.baderlab.org
 **
 ** Code written by: Ruth Isserlin
 ** Authors: Daniele Merico, Ruth Isserlin, Oliver Stueker, Gary D. Bader
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** (at your option) any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** University of Toronto
 ** has no obligations to provide maintenance, support, updates, 
 ** enhancements or modifications.  In no event shall the
 ** University of Toronto
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** University of Toronto
 ** has been advised of the possibility of such damage.  
 ** See the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **
 **/

// $Id$
// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$
// $HeadURL$

package org.baderlab.csplugins.enrichmentmap.parsers;

import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResult;
import org.baderlab.csplugins.enrichmentmap.model.GSEAResult;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.baderlab.csplugins.enrichmentmap.model.GenericResult;
import org.baderlab.csplugins.enrichmentmap.model.SetOfEnrichmentResults;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Created by
 * User: risserlin
 * Date: Jun 25, 2009
 * Time: 2:40:14 PM
 * <p>
 * Read a set of enrichment results.  Results can be specific to a Gene set enrichment analysis or to
 * a generic enrichment analysis.
 * <p>
 * The two different results files are distinguished based on the number of columns, a GSEA results file
 * has exactly eleven columns.  Any other number of is assumed to be a generic results file.  (It is possible
 * that a generic file also has exactly 11 column so if the file has 11 column the 5 and 6 column headers are
 * checked.  If columns 5 and 6 are specified as ES and NES the file is for sure a GSEA result file.)
 */
public class DetermineEnrichmentResultFileReader  {
	
	//default Score at Max value
    public static final Double DefaultScoreAtMax = -1000000.0;
	
    //private EnrichmentMapParameters params;
    private DataSet dataset;
    //enrichment results file name
    private String EnrichmentResultFileName1;
    private String EnrichmentResultFileName2;
    
    //Stores the enrichment results
    private SetOfEnrichmentResults enrichments;
    private HashMap<String, EnrichmentResult> results ;

    //phenotypes defined by user - used to classify phenotype specifications
    //in the generic enrichment results file
    private String upPhenotype;
    private String downPhenotype;

    // Keep track of progress for monitoring:
    private TaskMonitor taskMonitor = null;
    private boolean interrupted = false;
    private TaskIterator currentTasks;

    //services needed
    private StreamUtil streamUtil;
    
    /**
     * Class constructor
     *
     * @param dataset  - dataset enrichment results are from
     */
    public DetermineEnrichmentResultFileReader(DataSet dataset,StreamUtil streamUtil) {
        
    		this.dataset = dataset;
    		this.streamUtil = streamUtil;
    	
        this.EnrichmentResultFileName1 = dataset.getEnrichments().getFilename1();
        this.EnrichmentResultFileName2 = dataset.getEnrichments().getFilename2();
       
        //create a new enrichment results set
        enrichments = dataset.getEnrichments();
        results = enrichments.getEnrichments();
        upPhenotype = enrichments.getPhenotype1(); 
        downPhenotype = enrichments.getPhenotype2();
        
        this.currentTasks = currentTasks;
       
        
    }

    /**
     * Parse enrichment results file
     */

    public TaskIterator getParsers()  {
    		TaskIterator parserTasks = new TaskIterator();
    		AbstractTask current = null; 
    		try {
    			if(this.EnrichmentResultFileName1 != null && !this.EnrichmentResultFileName1.isEmpty()){
    				current = readFile(this.EnrichmentResultFileName1);
    				if(current instanceof ParseGREATEnrichmentResults )
    					parserTasks.append(new GREATWhichPvalueQuestionTask(dataset.getMap()));
					parserTasks.append(current);
    			}
    			if(this.EnrichmentResultFileName2 != null && !this.EnrichmentResultFileName2.isEmpty()){
    				parserTasks.append(readFile(this.EnrichmentResultFileName2));
    			}
    		} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		return parserTasks;
    }
    
    
    /*
     * Read file
     */

    public AbstractTask readFile(String EnrichmentResultFileName) throws IOException{
    		//check to see if the enrichment file is an edb file
    		if(EnrichmentResultFileName.endsWith(".edb"))
    			return new ParseEDBEnrichmentResults(dataset,streamUtil);   		    				
    	
    		else{
    			//open Enrichment Result file
    			InputStream reader = streamUtil.getInputStream(EnrichmentResultFileName);
    			
    			String fullText = new Scanner(reader,"UTF-8").useDelimiter("\\A").next();                        

    			
    	        String []lines = fullText.split("\r\n?|\n");


    			//figure out what type of enrichment results file.  Either it is a GSEA result
    			//file or it is a generic result file.
    			// Currently the headings in the GSEA Results file are:
    			// NAME <tab> GS<br> follow link to MSigDB <tab> GS DETAILS <tab> SIZE <tab> ES <tab> NES <tab> NOM p-val <tab> FDR q-val <tab> FWER p-val <tab> RANK AT MAX <tab> LEADING EDGE
    			// There are eleven headings.

    			//DAVID results have 13 columns
    			//Category <tab> Term <tab> Count <tab> % <tab> PValue <tab> Genes <tab> List <tab> Total <tab> Pop Hits <tab> Pop Total <tab> Fold Enrichment <tab> Bonferroni <tab> Benjamini <tab> FDR


    			//ES and NES columns are specific to the GSEA format
    			String header_line = lines[0];
    			String [] tokens = header_line.split("\t");

    			//check to see if there are exactly 11 columns - = GSEA results
    			if(tokens.length == 11){
    				//check to see if the ES is the 5th column and that NES is the 6th column
    				if((tokens[4].equalsIgnoreCase("ES")) && (tokens[5].equalsIgnoreCase("NES")))
    					return new ParseGSEAEnrichmentResults(dataset,streamUtil);
    				//it is possible that the file can have 11 columns but that it is still a generic file
    				//if it doesn't specify ES and NES in the 5 and 6th columns
    				else
    					return new ParseGenericEnrichmentResults(dataset,streamUtil);
    			}
    			//check to see if there are exactly 13 columns - = DAVID results
    			else if (tokens.length == 13){
    				//check to see that the 6th column is called Genes and that the 12th column is called "Benjamini"
    				if((tokens[5].equalsIgnoreCase("Genes")) && tokens[11].equalsIgnoreCase("Benjamini"))
    					return new ParseDavidEnrichmentResults(dataset,streamUtil);
    				else
    					return new ParseGenericEnrichmentResults(dataset,streamUtil);

    			}
    			//fix bug with new version of bingo plugin change the case of the header file.
    			else if (header_line.toLowerCase().contains("File created with BiNGO".toLowerCase())){
    				return new ParseBingoEnrichmentResults(dataset,streamUtil);
    			}
    			else if(header_line.contains("GREAT version")){
    				return new ParseGREATEnrichmentResults(dataset,streamUtil);
    			}
    			else{
    				return new ParseGenericEnrichmentResults(dataset,streamUtil);
    			}
    		} //end of else (not edb file)
    }//end of method
    
}
