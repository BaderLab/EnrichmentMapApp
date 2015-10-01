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

import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.baderlab.csplugins.enrichmentmap.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.baderlab.csplugins.enrichmentmap.model.SetOfGeneSets;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;


/**
 * Created by
 * User: risserlin
 * Date: Jan 8, 2009
 * Time: 11:59:17 AM
 * <p>
 *  This class parses a GMT (gene set) file and  creates a set of genesets
 */
public class GMTFileReaderTask extends AbstractTask {

    private EnrichmentMap map;

    //gene set file name
    private String GMTFileName;
    // gene hash (and inverse hash)
    private Map<String, Integer> genes;
    private Map<Integer, String> hashkey2gene;

    //gene sets
    private SetOfGeneSets setOfgenesets;

    
    public final static int ENRICHMENT_GMT = 1, SIGNATURE_GMT = 2; 

    private StreamUtil streamUtil;
    /**
     * Class Constructor - also given current task monitor
     * @param DataSet - a gmt file is associated with a dataset
     */
    public GMTFileReaderTask(DataSet dataset,StreamUtil streamUtil){
    	
    	this.GMTFileName = dataset.getSetofgenesets().getFilename();
    	this.genes = dataset.getMap().getGenes();
    	this.hashkey2gene = dataset.getMap().getHashkey2gene();
   
    	this.setOfgenesets = dataset.getSetofgenesets();
    	
    	this.map = dataset.getMap();
    		
    	this.streamUtil = streamUtil;
    }


    /**
     * for BuildDiseaseSignatureTask
     *
     * @param params
     * @param genesets_file
     */
    public GMTFileReaderTask(EnrichmentMap map, PostAnalysisParameters params, int genesets_file,StreamUtil streamUtil)   {
        
    	this.map = map;
    	this.streamUtil = streamUtil;
        this.genes = map.getGenes();
        this.hashkey2gene = map.getHashkey2gene();
        
        if (genesets_file == ENRICHMENT_GMT) {
            //open GMT file
            //this.GMTFileName = params.getGMTFileName();
            //this.genesets = params.getEM().getGenesets();
            //this.setOfgenesets = map.get
        }
        else if ( genesets_file == SIGNATURE_GMT) {
            //open signature-GMT file
            this.GMTFileName = params.getSignatureGMTFileName();
            this.setOfgenesets = params.getSignatureGenesets();
        }
        else 
            throw new IllegalArgumentException("argument not allowed:" + genesets_file);
    }

    private String deAccent(String str) {
        String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD); 
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("");
    }
    
    /**
     * parse GMT (gene set) file
     */
    public void parse(TaskMonitor taskMonitor) throws IOException {
    		
    	HashMap<String, GeneSet> genesets = setOfgenesets.getGenesets();
    	
        //open GMT file
    	InputStream reader = streamUtil.getInputStream(GMTFileName);
        String fullText = new Scanner(reader,"UTF-8").useDelimiter("\\A").next();
        
        String []lines = fullText.split("\r\n?|\n");

        int currentProgress = 0;
        int maxValue = lines.length;
        
        taskMonitor.setStatusMessage("Parsing GMT file - " + maxValue + " rows");   
        try {
            for (int i = 0; i < lines.length; i++) {
                if (cancelled)
                    throw new InterruptedException();

                String line = lines[i];

                String[] tokens = line.split("\t");

                //only go through the lines that have at least a gene set name and description.
                if(tokens.length >= 2){
                    //The first column of the file is the name of the geneset
                    //String Name = deAccent(tokens[0].toUpperCase().trim());
                    String Name = tokens[0].toUpperCase().trim();
                    
                    //issue with accents on some of the genesets - replace all the accents
                    

                    //The second column of the file is the description of the geneset
                    String description = tokens[1].trim();

                    //create an object of type Geneset with the above Name and description
                    GeneSet gs = new GeneSet(Name, description);

                    // Calculate Percentage.  This must be a value between 0..100.
                    int percentComplete = (int) (((double) currentProgress / maxValue) * 100);
                    taskMonitor.setProgress(percentComplete);                   
                    currentProgress++;

                    //All subsequent fields in the list are the geneset associated with this geneset.
                    for (int j = 2; j < tokens.length; j++) {

                        //Check to see if the gene is already in the hashmap of genes
                        //if it is already in the hash then get its associated key and put it
                        //into the set of genes
                        if (genes.containsKey(tokens[j].toUpperCase())) {
                            gs.addGene(genes.get(tokens[j].toUpperCase()));
                        }

                        //If the gene is not in the list then get the next value to be used and put it in the list
                        else{
                            //only add the gene if it isn't a blank
                            if(!tokens[j].equalsIgnoreCase("")){

                                //add the gene to the master list of genes
                                int value = map.getNumberOfGenes();
                                genes.put(tokens[j].toUpperCase(), value);
                                hashkey2gene.put(value, tokens[j].toUpperCase());
                                map.setNumberOfGenes(value+1);

                                //add the gene to the genelist
                                gs.addGene(genes.get(tokens[j].toUpperCase()));
                            }
                        }
                    }

                    //finished parsing that geneset
                    //add the current geneset to the hashmap of genesets
                    genesets.put(Name, gs);

                    //add the geneset type to the list of types
                    setOfgenesets.addGenesetType(gs.getSource());
                 }

            }
        } catch (InterruptedException e) {
            taskMonitor.setStatusMessage("Loading of GMT file cancelled");
        }
    }


        
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("parsing GMT file");
		parse(taskMonitor);
	}
}
