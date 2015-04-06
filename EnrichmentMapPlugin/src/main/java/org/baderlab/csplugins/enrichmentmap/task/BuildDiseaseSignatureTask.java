/*
 *                       EnrichmentMap Cytoscape Plugin
 *
 * Copyright (c) 2008-2009 Bader Lab, Donnelly Centre for Cellular and Biomolecular 
 * Research, University of Toronto
 *
 * Contact: http://www.baderlab.org
 *
 * Code written by: Ruth Isserlin
 * Authors: Daniele Merico, Ruth Isserlin, Oliver Stueker, Gary D. Bader
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * University of Toronto
 * has no obligations to provide maintenance, support, updates, 
 * enhancements or modifications.  In no event shall the
 * University of Toronto
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * University of Toronto
 * has been advised of the possibility of such damage.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 *
 */

// $Id$
// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$
// $HeadURL$

package org.baderlab.csplugins.enrichmentmap.task;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.inference.MannWhitneyUTest;
import org.baderlab.csplugins.enrichmentmap.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.EnrichmentMapVisualStyle;
import org.baderlab.csplugins.enrichmentmap.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.baderlab.csplugins.enrichmentmap.model.GenesetSimilarity;
import org.baderlab.csplugins.enrichmentmap.model.Ranking;
import org.baderlab.csplugins.enrichmentmap.util.NetworkUtil;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;



/**
 * Cytoscape-Task to perform  Disease-Signature Post-Analysis
 * 
 * @author revilo
 * <p>
 * Date   July 10, 2009<br>
 * Time   3:58:24 PM<br>
 *
 */
public class BuildDiseaseSignatureTask extends AbstractTask implements ObservableTask {
	private final CySwingApplication swingApplication;
	private final CyApplicationManager applicationManager;
    private final CyEventHelper eventHelper;
    
    private PostAnalysisParameters paParams;
    private EnrichmentMap map;
    
    // Keep track of progress for monitoring:
    private TaskMonitor taskMonitor = null;
    private boolean interrupted = false;
    
    private Map<String,GeneSet> EnrichmentGenesets;
    private Map<String,GeneSet> SignatureGenesets;
    private Map<String,GeneSet> SelectedSignatureGenesets;
    
    // Gene Populations:
    private Set<Integer> EnrichmentGenes;
    private Set<Integer> SignatureGenes;
    
    // Ranks
    private Ranking ranks;
    private boolean warnUser = false;
        
    private HashMap<String,GenesetSimilarity> geneset_similarities;
    
    /**
     * default constructor
     * @param paParams
     */

    public BuildDiseaseSignatureTask(EnrichmentMap map, PostAnalysisParameters paParams,
    		CySessionManager manager, StreamUtil streamUtil,
    		CyApplicationManager applicationManager, CyEventHelper eventHelper, CySwingApplication swingApplication) {
    	this.map = map;
    	this.applicationManager = applicationManager;
    	this.eventHelper = eventHelper;
    	this.swingApplication = swingApplication;

    	HashMap<String, DataSet> data_sets = this.map.getDatasets();
    	DataSet dataset = data_sets.get(paParams.getSignature_dataSet());
    	ranks = new Ranking();
    	if (dataset != null) {
    		ranks = dataset.getExpressionSets().getRanks().get(paParams.getSignature_rankFile());
    	}    	

    	//create a new instance of the parameters and copy the version received from the input
        //window into this new instance.
    	this.paParams = new PostAnalysisParameters();
        this.paParams.copyFrom(paParams);
        
        this.EnrichmentGenesets = map.getEnrichmentGenesets();
        this.SignatureGenesets  = this.paParams.getSignatureGenesets().getGenesets();

        if (map.getGenesetSimilarity() == null)
            this.geneset_similarities = new HashMap<String, GenesetSimilarity>();
        else
            this.geneset_similarities = map.getGenesetSimilarity();
            
        this.SelectedSignatureGenesets = new HashMap<String,GeneSet>();
        for (int i = 0; i < paParams.getSelectedSignatureSetNames().getSize(); i++){
            Object geneset = paParams.getSelectedSignatureSetNames().get(i);
			this.SelectedSignatureGenesets.put(geneset.toString(), this.SignatureGenesets.get(geneset));
        }
        
        // EnrichmentGenes: pool of all genes in Enrichment Gene Sets
        //TODO: get enrichment map genes from enrichment map parameters now that they are computed there.
        EnrichmentGenes = new HashSet<>();
        for(GeneSet geneSet : EnrichmentGenesets.values()) {
            EnrichmentGenes.addAll(geneSet.getGenes());
        }
        // SignatureGenes: pool of all genes in Signature Gene Sets
        SignatureGenes = new HashSet<>();
        for(GeneSet geneSet :  SignatureGenesets.values()) {
            SignatureGenes.addAll(geneSet.getGenes());
        }
    }


    public void buildDiseaseSignature() {

        /* **************************************************
         * Calculate Similarity between Signature Gene Sets *
         * and Enrichment Genesets.                         *
         ****************************************************/
        int maxValue = SelectedSignatureGenesets.size() * EnrichmentGenesets.size();
        int currentProgress = 0;
        double currentNodeY_offset = paParams.getCurrentNodePlacementY_Offset();
        double currentNodeY_increment = 150.0;
              
        try {
            CyNetwork current_network  = applicationManager.getCurrentNetwork();
            CyNetworkView current_view = applicationManager.getCurrentNetworkView();
            
            //the signature geneset
            CyNode hub_node = null;
            
            String prefix = paParams.getAttributePrefix();
            if (prefix == null) {
                prefix = "EM1_";
                paParams.setAttributePrefix(prefix);
            }
            
            //get the node attribute and edge attribute tables
            CyTable cyEdgeAttrs = createEdgeAttributes(current_network,"",prefix);
            CyTable cyNodeAttrs = createNodeAttributes(current_network,"",prefix);
            
            // make a HashMap of all Nodes in the Network
            Map<String,CyNode> nodesMap = createNodeMap(current_network, cyNodeAttrs, prefix);
            
            // Common gene universe: Intersection of EnrichmentGenes and SignatureGenes
            Set<Integer> geneUniverse = new HashSet<>();
            geneUniverse.addAll(EnrichmentGenes);

            /* bug: #97: Post-analysis: thresholding not working with overlap 
             * Don't restrict Universe to Intersection of Enrichment- and Signature Genes 
             * but rather the Universe of all Enrichment Genes.  
             */
            // geneUniverse.retainAll(SignatureGenes); 
            int universeSize = paParams.getUniverseSize();
        	Map<Integer,Double> gene2score = new HashMap<>();
            if (this.ranks != null) {
            	gene2score = this.ranks.getGene2Score();
            }
            
            Map<String,String> duplicateGenesets = new HashMap<>();

            //iterate over selected Signature genesets
            for(String hub_name : SelectedSignatureGenesets.keySet()) {
                
                // get the Signature Genes, restrict them to the Gene-Universe and add them to the Parameters
                GeneSet sigGeneSet = SelectedSignatureGenesets.get(hub_name);

                // Check to see if the signature geneset shares the same name with an 
                // enrichment geneset. If it does, give the signature geneset a unique name
                if (EnrichmentGenesets.containsKey(hub_name)) {
                	duplicateGenesets.put(hub_name, "PA_" + hub_name);
                	hub_name = "PA_" + hub_name;
                }

                //the signature genes in this signature gene set 
                Set<Integer> sigGenes = sigGeneSet.getGenes();

                // the genes that are in this signature gene set as well as in the Universe of Enrichment-GMT Genes.    
                Set<Integer> sigGenesInUniverse = new HashSet<Integer>(sigGenes);
                sigGenesInUniverse.retainAll(geneUniverse);
                //sigGeneSet.setGenes(sigGenes);
                
                EnrichmentMapManager.getInstance().getMap(current_network.getSUID()).getSignatureGenesets().put(hub_name, sigGeneSet);
                
                // iterate over Enrichment Genesets
                for(String geneset_name : EnrichmentGenesets.keySet()) {
                  
                    // Calculate Percentage.  This must be a value between 0..100.
                    int percentComplete = (int) (((double) currentProgress / maxValue) * 100);
                    // Estimate Time Remaining
                    if (taskMonitor != null) {
                       taskMonitor.setProgress(percentComplete);
                       taskMonitor.setStatusMessage("Computing Geneset similarity " + currentProgress + " of " + maxValue);
                       taskMonitor.setTitle("Post Analysis");
                    }
                    currentProgress++;
                    
                    if (interrupted) {
                        throw new InterruptedException();
                    }
                    
                    // Calculate overlap:
                    
                    //Check to see if this comparison has been done
                    //The key for the set of geneset similarities is the combination of the two names.  Check for either variation name1_name2 or name2_name1
                    String similarity_key1 = hub_name     + " (" + PostAnalysisParameters.SIGNATURE_INTERACTION_TYPE + ") " + geneset_name;
                    String similarity_key2 = geneset_name + " (" + PostAnalysisParameters.SIGNATURE_INTERACTION_TYPE + ") " + hub_name;
                    
                    //first check to see if the terms are the same
                    if(hub_name.equalsIgnoreCase(geneset_name)) {
                       //don't compare two identical genesets
                    }
                    else if (! nodesMap.containsKey(geneset_name)) {
                        // skip if the Geneset is not in the Network
                    } 
                    else if (cyNodeAttrs.getRow(nodesMap.get(geneset_name).getSUID()).get(prefix + EnrichmentMapVisualStyle.GS_TYPE,String.class).equalsIgnoreCase(
                            EnrichmentMapVisualStyle.GS_TYPE_SIGNATURE)) {
                        // skip if the Geneset is a Signature Node from a previous analysis
                    }
                    /* else if(geneset_similarities.containsKey(similarity_key1) || geneset_similarities.containsKey(similarity_key2)){
                        //skip this geneset comparison.  It has already been done.
                    }*/
                    else{
                        //get the Enrichment geneset
                        GeneSet enrGeneset = EnrichmentGenesets.get(geneset_name);

                        // restrict to a common gene universe
                        Set<Integer> enrGenes = enrGeneset.getGenes();
                        enrGenes.retainAll(geneUniverse);
                        
                        //Get the union of the two sets
                        Set<Integer> union = new HashSet<Integer>(sigGenes);
                        union.addAll(enrGenes);

                        //Get the intersection
                        Set<Integer> intersection = new HashSet<Integer>(sigGenesInUniverse);
                        intersection.retainAll(enrGenes);
                        
                        // Only calculate Mann-Whitney pValue if there is overlap
                        if (intersection.size() > 0) {
                        	double coeffecient;
                        	
                        	// if  either Jaccard or Overlap similarity are requested:
                        	switch(paParams.getSignature_CutoffMetric()) {
                        		default: // use Directed Overlap
                        		case PostAnalysisParameters.DIR_OVERLAP:
                        			coeffecient = (double)intersection.size() / (double)enrGenes.size();
                        			break;
                        		case PostAnalysisParameters.JACCARD:
                        			coeffecient = (double)intersection.size() / (double)union.size();
                        			break;
                        		case PostAnalysisParameters.OVERLAP:
                        			coeffecient = (double)intersection.size() / Math.min((double)sigGenes.size(), (double)enrGenes.size());
                        			break;
                        	}
                        	
	                        //create Geneset similarity object
	                        GenesetSimilarity comparison = new GenesetSimilarity(hub_name, geneset_name, coeffecient, PostAnalysisParameters.SIGNATURE_INTERACTION_TYPE, (HashSet<Integer>)intersection);
	                        
	                        switch(paParams.getSignature_rankTest()) {
		                        case PostAnalysisParameters.MANN_WHIT:
		                        	mannWhitney(gene2score, intersection, comparison);
		                        	break;
		                        case PostAnalysisParameters.HYPERGEOM:
		                        	hypergeometric(universeSize, sigGenesInUniverse, enrGenes, intersection, comparison);
		                        	break;
	                        }
                            
	                        geneset_similarities.put(similarity_key1, comparison);

	                    }
                    }
                } // End: iterate over Enrichment Genesets
                
                
                /* ***************************
                 * Create Signature Hub Node *
                 *****************************/
                
                hub_node = createHubNode(hub_name, current_network, current_view, currentNodeY_offset, 
                		                 prefix, cyNodeAttrs, geneUniverse, sigGeneSet);   
                
                currentNodeY_offset += currentNodeY_increment;
                              
            }// End: iterate over Signature Genesets
            
            // Update signature geneset map with new names of all signature genesets that have duplicates
            for(String original_hub_name : duplicateGenesets.keySet()) {
            	GeneSet geneset = SelectedSignatureGenesets.remove(original_hub_name);
            	SelectedSignatureGenesets.put(duplicateGenesets.get(original_hub_name), geneset);
            }
            duplicateGenesets.clear();
            
            paParams.setCurrentNodePlacementY_Offset(currentNodeY_offset);
            
            /* ****************************
             * Create Signature Hub Edges *
             ******************************/
            
            for (String edge_name : geneset_similarities.keySet()) {
                if (interrupted)
                    throw new InterruptedException();
                
                if (!geneset_similarities.get(edge_name).getInteractionType().equals(PostAnalysisParameters.SIGNATURE_INTERACTION_TYPE))
                    // skip if it's not a signature edge
                    continue;
                if (!(this.SelectedSignatureGenesets.containsKey(geneset_similarities.get(edge_name).getGeneset1_Name()) 
                      || this.SelectedSignatureGenesets.containsKey(geneset_similarities.get(edge_name).getGeneset2_Name()) ) )   
                    // skip if not either of the adjacent nodes is a SelectedSignatureGenesets of the current analysis (fixes Bug #44)
                    continue;

                // check if combination passes Cut-Off:
                boolean passed_cutoff = false;
//                if( (paParams.getSignature_CutoffMetric() == PostAnalysisParameters.ABS_NUMBER) && 
//                     (geneset_similarities.get(edge_name).getSizeOfOverlap() >= paParams.getSignature_absNumber_Cutoff() ) )
//                    passed_cutoff = true;
//                else if ( (paParams.getSignature_CutoffMetric() == PostAnalysisParameters.JACCARD) && 
//                          (geneset_similarities.get(edge_name).getSimilarity_coeffecient() >= paParams.getSignature_Jaccard_Cutoff() ) )
//                    passed_cutoff = true;
//                else if ( (paParams.getSignature_CutoffMetric() == PostAnalysisParameters.OVERLAP) && 
//                        (geneset_similarities.get(edge_name).getSimilarity_coeffecient() >= paParams.getSignature_Overlap_Cutoff() ) )
//                    passed_cutoff = true;
//                else if ( (paParams.getSignature_CutoffMetric() == PostAnalysisParameters.DIR_OVERLAP) && 
//                        (geneset_similarities.get(edge_name).getSimilarity_coeffecient() >= paParams.getSignature_DirOverlap_Cutoff() ) )
//                    passed_cutoff = true;
                if ( (paParams.getSignature_rankTest() == PostAnalysisParameters.MANN_WHIT) && 
                        (geneset_similarities.get(edge_name).getMann_Whit_pValue() <= paParams.getSignature_Mann_Whit_Cutoff() ) ||
                     (paParams.getSignature_rankTest() == PostAnalysisParameters.MANN_WHIT) && 
                        (geneset_similarities.get(edge_name).getSensitivity() ) ||
                     (paParams.getSignature_rankTest() == PostAnalysisParameters.HYPERGEOM) && 
                        (geneset_similarities.get(edge_name).getHypergeom_pvalue() <= paParams.getSignature_Hypergeom_Cutoff() )) {
                   	passed_cutoff = true;
                }

                warnUser |= createEdge(edge_name, current_network, current_view, hub_node, prefix, cyEdgeAttrs, cyNodeAttrs, passed_cutoff);
                
            } //for
            
            //update the view 
            current_view.updateView();
            
            //TODO add network attribute
           // cyNetworkAttrs.setAttribute(currentNetworkView.getIdentifier(), EnrichmentMapVisualStyle.NUMBER_OF_ENRICHMENT_GENES, geneUniverse.size());
        } catch (InterruptedException e) {
        		//TODO cancel task
            //taskMonitor.setException(e, "Generation of Signature Hubs cancelled");
        }
    }
    

    private CyNode createHubNode(String hub_name, CyNetwork current_network, CyNetworkView current_view, double currentNodeY_offset,
			                     String prefix, CyTable cyNodeAttrs, Set<Integer> geneUniverse, GeneSet sigGeneSet) {
		
		// test for existing node first
		CyNode hub_node = NetworkUtil.getNodeWithValue(current_network, cyNodeAttrs, CyNetwork.NAME, hub_name);
		if(hub_node == null)
			hub_node = current_network.addNode();
		
		current_network.getRow(hub_node).set(CyNetwork.NAME, hub_name);
		current_view.updateView();
		//flush events to make sure view has been created.
		this.eventHelper.flushPayloadEvents();
		
		// add currentNodeY_offset to initial Y position of the Node
		// and increase currentNodeY_offset for the next Node
		View<CyNode> hubNodeView = current_view.getNodeView(hub_node);
		double hubNodeY = hubNodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
		hubNodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, hubNodeY + currentNodeY_offset);
		
		
		String formatted_label =  CreateEnrichmentMapNetworkTask.formatLabel(hub_name);
		CyRow current_row = cyNodeAttrs.getRow(hub_node.getSUID());
		current_row.set(prefix + EnrichmentMapVisualStyle.FORMATTED_NAME, formatted_label);
		
		//create an attribute that stores the genes that are associated with this node as an attribute list
		//only create the list if the hashkey 2 genes is not null Otherwise it takes too much time to populate the list
//                GeneSet sigGeneSet = SelectedSignatureGenesets.get(hub_name);
		if(map.getHashkey2gene() != null){
		    // HashSet to List:
		    List<String> gene_list = new ArrayList<>();
		    for(Integer current : sigGeneSet.getGenes()) {
		        String gene = map.getGeneFromHashKey(current);
		        if(gene_list != null && gene != null)
		            gene_list.add(gene);
		    }
		    Collections.sort(gene_list);

		    List<String> enr_gene_list = new ArrayList<>();
		    Set<Integer> enr_genes_hash = sigGeneSet.getGenes();
		    enr_genes_hash.retainAll(geneUniverse);
		    for(Integer current : enr_genes_hash) {
		        String gene = map.getGeneFromHashKey(current);
		        if(enr_gene_list != null && gene != null)
		            enr_gene_list.add(gene);
		    }
		    Collections.sort(enr_gene_list);
		    
		    current_row.set(prefix + EnrichmentMapVisualStyle.GENES, gene_list);
		    current_row.set(prefix + EnrichmentMapVisualStyle.ENR_GENES, enr_gene_list);
		    current_row.set(prefix + EnrichmentMapVisualStyle.GS_DESCR, sigGeneSet.getDescription());
		    current_row.set(prefix + EnrichmentMapVisualStyle.GS_TYPE, EnrichmentMapVisualStyle.GS_TYPE_SIGNATURE);
		    current_row.set(prefix + EnrichmentMapVisualStyle.NAME, sigGeneSet.getName() );
		    current_row.set(prefix + EnrichmentMapVisualStyle.GS_SIZE_SIGNATURE , sigGeneSet.getGenes().size() );
		}

		// add the geneset of the signature node to the GenesetsOfInterest,
		// as the Heatmap will grep it's data from there.
		//TODO: Currently only supports one dataset
		//TODO:Enable signature dataset with multiple dataset
		
		sigGeneSet.getGenes().retainAll(map.getDataset(EnrichmentMap.DATASET1).getDatasetGenes());
		map.getDataset(EnrichmentMap.DATASET1).getGenesetsOfInterest().getGenesets().put(hub_name, sigGeneSet);
		
		// set Visual Style bypass
		hubNodeView.setLockedValue(BasicVisualLexicon.NODE_SHAPE, paParams.getSignatureHub_nodeShape());               
		hubNodeView.setLockedValue(BasicVisualLexicon.NODE_FILL_COLOR, paParams.getSignatureHub_nodeColor());               
		hubNodeView.setLockedValue(BasicVisualLexicon.NODE_BORDER_PAINT, paParams.getSignatureHub_borderColor());
		return hub_node;
	}
    
    
	/**
	 * Returns true iff the user should be warned about an existing edge that does not pass the new cutoff.
	 */
	private boolean createEdge(String edge_name, CyNetwork current_network, CyNetworkView current_view, 
			                   CyNode hub_node, String prefix, CyTable cyEdgeAttrs, CyTable cyNodeAttrs, boolean passed_cutoff) {
		
		CyEdge edge = NetworkUtil.getEdgeWithValue(current_network, cyEdgeAttrs, CyNetwork.NAME, edge_name);
		if(passed_cutoff) {
			if(edge == null) { // edge does not exist, create it
				CyNode gene_set = NetworkUtil.getNodeWithValue(current_network, cyNodeAttrs, CyNetwork.NAME, geneset_similarities.get(edge_name).getGeneset2_Name());
				edge = current_network.addEdge(hub_node, gene_set, false);
			} 
			// if the edge already exists then just update existing one
		} else {
			if(edge == null) { // edge does not exist, so do nothing
				return false;
			} 
			// if the edge already exists but does not pass the cutoff we will sill update it
		}
		
		//add update view because view is returning null when we try to get the edge view.
		current_view.updateView();
		CyRow current_edgerow = cyEdgeAttrs.getRow(edge.getSUID());
		current_edgerow.set(CyNetwork.NAME, edge_name);
		
		View<CyEdge> edgeView = current_view.getEdgeView(edge);
		//create an attribute that stores the genes that are associated with this edge as an attribute list
		//only create the list if the hashkey 2 genes is not null Otherwise it take too much time to populate the list
		if(map.getHashkey2gene() != null){
		    List<String> gene_list = new ArrayList<>();
		    Set<Integer> genes_hash = geneset_similarities.get(edge_name).getOverlapping_genes();
		    for(Integer current : genes_hash) {
		        String gene = map.getGeneFromHashKey(current);
		        if(gene_list != null && gene != null) {
		            gene_list.add(gene);
		        }
		    }
		    Collections.sort(gene_list);
		    
		    current_edgerow.set(prefix + EnrichmentMapVisualStyle.OVERLAP_GENES, gene_list);                        
		}
 
		current_edgerow.set(prefix + EnrichmentMapVisualStyle.OVERLAP_SIZE, geneset_similarities.get(edge_name).getSizeOfOverlap());
		current_edgerow.set(prefix + EnrichmentMapVisualStyle.SIMILARITY_COEFFICIENT, geneset_similarities.get(edge_name).getSimilarity_coeffecient());
		current_edgerow.set(prefix + EnrichmentMapVisualStyle.HYPERGEOM_PVALUE, geneset_similarities.get(edge_name).getHypergeom_pvalue());
		current_edgerow.set(prefix + EnrichmentMapVisualStyle.ENRICHMENT_SET, geneset_similarities.get(edge_name).getEnrichment_set());
		
		// Attributes related to the Hypergeometric Test
		if (paParams.getSignature_rankTest() == PostAnalysisParameters.HYPERGEOM) {
			current_edgerow.set(prefix + EnrichmentMapVisualStyle.HYPERGEOM_PVALUE, geneset_similarities.get(edge_name).getHypergeom_pvalue());
			current_edgerow.set(prefix + EnrichmentMapVisualStyle.HYPERGEOM_N, geneset_similarities.get(edge_name).getHypergeom_N());
			current_edgerow.set(prefix + EnrichmentMapVisualStyle.HYPERGEOM_n, geneset_similarities.get(edge_name).getHypergeom_n());
			current_edgerow.set(prefix + EnrichmentMapVisualStyle.HYPERGEOM_m, geneset_similarities.get(edge_name).getHypergeom_m());
			current_edgerow.set(prefix + EnrichmentMapVisualStyle.HYPERGEOM_k, geneset_similarities.get(edge_name).getHypergeom_k());
		}
		
		// Attributes related to the Mann-Whitney Test
		if (paParams.getSignature_rankTest() == PostAnalysisParameters.MANN_WHIT) {
			current_edgerow.set(prefix + EnrichmentMapVisualStyle.MANN_WHIT_PVALUE, geneset_similarities.get(edge_name).getMann_Whit_pValue());
		}
		
		if(edgeView != null) {
			edgeView.setLockedValue(BasicVisualLexicon.EDGE_UNSELECTED_PAINT, paParams.getSignatureHub_edgeColor());
			edgeView.setLockedValue(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT, paParams.getSignatureHub_edgeColor());
		
			//change "edge.lineWidth" based on Hypergeometric Value 
			if (geneset_similarities.get(edge_name).getHypergeom_pvalue() <= (paParams.getSignature_Hypergeom_Cutoff()/100) )
				edgeView.setLockedValue(BasicVisualLexicon.EDGE_WIDTH,8.0);	
			else 
				if (geneset_similarities.get(edge_name).getHypergeom_pvalue() <= (paParams.getSignature_Hypergeom_Cutoff()/10) )
					edgeView.setLockedValue(BasicVisualLexicon.EDGE_WIDTH,4.5);	                   
				else
					edgeView.setLockedValue(BasicVisualLexicon.EDGE_WIDTH,1.0);	
		}
		
		return !passed_cutoff;
	}
	


	private void hypergeometric(int universeSize,
			Set<Integer> sigGenesInUniverse, Set<Integer> enrGenes,
			Set<Integer> intersection, GenesetSimilarity comparison) {
		
		// Calculate Hypergeometric pValue for Overlap
		int N = universeSize; //number of total genes (size of population / total number of balls)
		int n = sigGenesInUniverse.size(); //size of signature geneset (sample size / number of extracted balls)
		int m = enrGenes.size(); //size of enrichment geneset (success Items / number of white balls in population)
		int k = intersection.size(); //size of intersection (successes /number of extracted white balls)
		double hyperPval;
		
		if (k > 0)
		    hyperPval = Hypergeometric.hyperGeomPvalue_sum(N, n, m, k, 0);
		else // Correct p-value of empty intersections to 1 (i.e. not significant)
		    hyperPval = 1.0;
		
		comparison.setHypergeom_pvalue(hyperPval);
		comparison.setHypergeom_N(N);
		comparison.setHypergeom_n(n);
		comparison.setHypergeom_m(m);
		comparison.setHypergeom_k(k);
	}


	private void mannWhitney(Map<Integer, Double> gene2score, Set<Integer> intersection, GenesetSimilarity comparison) {
		if (gene2score.size() == 0) {
			comparison.setMann_Whit_pValue(1.5);
		} else {
			// Calculate Mann-Whitney U pValue for Overlap
            Integer[] overlap_gene_ids = intersection.toArray(new Integer[intersection.size()]);
            double[] overlap_gene_scores = new double[overlap_gene_ids.length];
            
            // Get the scores for the overlap
            for (int k = 0; k < overlap_gene_ids.length; k++) {
            	overlap_gene_scores[k] = gene2score.get(overlap_gene_ids[k]);
            }
            
            MannWhitneyUTest mann_whit = new MannWhitneyUTest();
        	double mannPval = mann_whit.mannWhitneyUTest(overlap_gene_scores, this.ranks.getScores());
    		
    		// Set Mann-Whitney U Parameters
    		comparison.setMann_Whit_pValue(mannPval);
		}
	}

    
    private Map<String,CyNode> createNodeMap(CyNetwork current_network, CyTable cyNodeAttrs, String prefix) {
    	HashMap<String,CyNode> nodesMap = new HashMap<String, CyNode>();
        for(CyNode aNode : current_network.getNodeList()) {
            nodesMap.put(cyNodeAttrs.getRow(aNode.getSUID()).get(prefix + EnrichmentMapVisualStyle.NAME, String.class), aNode);
        }
        return nodesMap;
    }
    
    
   
    /*
     * Create Node attribute table with post analysis parameters not in the main EM table
     */
    public CyTable createNodeAttributes(CyNetwork network, String name, String prefix){
		//TODO:change back to creating our own table.  Currently can only map to a string column.
	    //in mean time use the default node table
		//CyTable nodeTable = tableFactory.createTable(/*name*/ prefix + "_" + node_table_suffix, CyNetwork.SUID, Long.class, true, true);
    
		CyTable nodeTable = network.getDefaultNodeTable();
		//check to see if column exists.  If it doesn't then create it
		if(nodeTable.getColumn(prefix + EnrichmentMapVisualStyle.ENR_GENES) == null)
			nodeTable.createListColumn(prefix + EnrichmentMapVisualStyle.ENR_GENES, String.class, false); 
		if(nodeTable.getColumn(prefix + EnrichmentMapVisualStyle.GS_SIZE_SIGNATURE) == null)
			nodeTable.createColumn(prefix + EnrichmentMapVisualStyle.GS_SIZE_SIGNATURE , Integer.class, false);
		
		return nodeTable;
    }
    
    //create the edge attribue table
    public CyTable createEdgeAttributes(CyNetwork network, String name, String prefix){
    	//TODO:change back to creating our own table.  Currently can only map to a string column.
	    //in mean time use the default edge table
    	//CyTable edgeTable = tableFactory.createTable(/*name*/ prefix + "_" + edge_table_suffix, CyNetwork.SUID,Long.class, true, true);
		CyTable edgeTable = network.getDefaultEdgeTable();
		
		//check to see if column exists.  If it doesn't then create it
		if(edgeTable.getColumn(prefix + EnrichmentMapVisualStyle.HYPERGEOM_PVALUE) == null)		
    		edgeTable.createColumn(prefix + EnrichmentMapVisualStyle.HYPERGEOM_PVALUE , Double.class, false);
    	if(edgeTable.getColumn(prefix + EnrichmentMapVisualStyle.HYPERGEOM_N) == null)	
    		edgeTable.createColumn(prefix + EnrichmentMapVisualStyle.HYPERGEOM_N , Integer.class, false);
    	if(edgeTable.getColumn(prefix + EnrichmentMapVisualStyle.HYPERGEOM_n) == null)	
    		edgeTable.createColumn(prefix + EnrichmentMapVisualStyle.HYPERGEOM_n , Integer.class, false);
    	if(edgeTable.getColumn(prefix + EnrichmentMapVisualStyle.HYPERGEOM_m) == null)	
    		edgeTable.createColumn(prefix + EnrichmentMapVisualStyle.HYPERGEOM_m , Integer.class, false);
    	if(edgeTable.getColumn(prefix + EnrichmentMapVisualStyle.HYPERGEOM_k) == null)	
    		edgeTable.createColumn(prefix + EnrichmentMapVisualStyle.HYPERGEOM_k , Integer.class, false);
    	
    	if(edgeTable.getColumn(prefix + EnrichmentMapVisualStyle.MANN_WHIT_PVALUE) == null)		
    		edgeTable.createColumn(prefix + EnrichmentMapVisualStyle.MANN_WHIT_PVALUE , Double.class, false);
    	
    	return edgeTable;
    }
    
    
  
    /**
     * @see cytoscape.task.Task#getTitle()
     */
    public String getTitle() {
        return new String("Generating Signature Hubs");
    }

    /**
     * @see cytoscape.task.Task#halt()
     */
    public void halt() {
        this.interrupted = true;

    }

    /**
     * Sets the Task Monitor.
     *
     * @param taskMonitor TaskMonitor Object.
     */
    public void setTaskMonitor(TaskMonitor taskMonitor) {
        if (this.taskMonitor != null) {
            throw new IllegalStateException("Task Monitor is already set.");
        }
        this.taskMonitor = taskMonitor;
    }

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		this.taskMonitor = taskMonitor;
		taskMonitor.setTitle("Generating Signature Hubs");
		
		buildDiseaseSignature();
		
	}

	@Override
	public <R> R getResults(Class<? extends R> type) {
		if(Boolean.class.equals(type)) {
			return type.cast(warnUser);
		}
		return null;
	}

}
