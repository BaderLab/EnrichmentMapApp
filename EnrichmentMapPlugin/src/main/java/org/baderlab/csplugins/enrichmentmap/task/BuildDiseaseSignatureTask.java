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
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;


/**
 * Cytoscape-Task to perform  Disease-Signature Post-Analysis
 */
public class BuildDiseaseSignatureTask extends AbstractTask implements ObservableTask {
	private final CySwingApplication swingApplication;
	private final CyApplicationManager applicationManager;
    private final CyEventHelper eventHelper;
    
    private PostAnalysisParameters paParams;
    private EnrichmentMap map;
    
    private Map<String,GeneSet> EnrichmentGenesets;
    private Map<String,GeneSet> SignatureGenesets;
    private Map<String,GeneSet> SelectedSignatureGenesets;
    
    // Gene Populations:
    private Set<Integer> EnrichmentGenes;
    private Set<Integer> SignatureGenes;
    
    // Ranks
    private Ranking ranks;
    private Map<String,GenesetSimilarity> geneset_similarities;
    
    private BuildDiseaseSignatureTaskResult.Builder taskResult = new BuildDiseaseSignatureTaskResult.Builder();
    
    
    

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


    @SuppressWarnings("incomplete-switch")
	public void buildDiseaseSignature(TaskMonitor taskMonitor) {

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
            taskResult.setNetwork(current_network);
            taskResult.setNetworkView(current_view);
            
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
                    
                    if (cancelled) {
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
                        		case DIR_OVERLAP:
                        			coeffecient = (double)intersection.size() / (double)enrGenes.size();
                        			break;
                        		case JACCARD:
                        			coeffecient = (double)intersection.size() / (double)union.size();
                        			break;
                        		case OVERLAP:
                        			coeffecient = (double)intersection.size() / Math.min((double)sigGenes.size(), (double)enrGenes.size());
                        			break;
                        	}
                        	
	                        //create Geneset similarity object
	                        GenesetSimilarity comparison = new GenesetSimilarity(hub_name, geneset_name, coeffecient, PostAnalysisParameters.SIGNATURE_INTERACTION_TYPE, (HashSet<Integer>)intersection);
	                        
	                        int universeSize;
	                        switch(paParams.getSignature_rankTest()) {
		                        case MANN_WHIT:
		                        	mannWhitney(intersection, comparison);
		                        	universeSize = map.getNumberOfGenes(); // #70 calculate hypergeometric also
		                        	hypergeometric(universeSize, sigGenesInUniverse, enrGenes, intersection, comparison);
		                        	break;
		                        case HYPERGEOM:
		                        	universeSize = paParams.getUniverseSize();
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
                
                createHubNode(hub_name, current_network, current_view, currentNodeY_offset, 
                		      prefix, cyEdgeAttrs, cyNodeAttrs, geneUniverse, sigGeneSet);   
                
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
                if (cancelled)
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
                if ( (paParams.getSignature_rankTest() == PostAnalysisParameters.FilterMetric.MANN_WHIT) && 
                        (geneset_similarities.get(edge_name).getMann_Whit_pValue() <= paParams.getSignature_Mann_Whit_Cutoff() ) ||
                     (paParams.getSignature_rankTest() == PostAnalysisParameters.FilterMetric.MANN_WHIT) && 
                        (geneset_similarities.get(edge_name).getSensitivity() ) ||
                     (paParams.getSignature_rankTest() == PostAnalysisParameters.FilterMetric.HYPERGEOM) && 
                        (geneset_similarities.get(edge_name).getHypergeom_pvalue() <= paParams.getSignature_Hypergeom_Cutoff() )) {
                   	passed_cutoff = true;
                }

                createEdge(edge_name, current_network, current_view, prefix, cyEdgeAttrs, cyNodeAttrs, passed_cutoff);
                
            } //for
            
            taskResult.setWarnUserBypassStyle(shouldUseBypass(prefix, cyEdgeAttrs));
            
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
			                     String prefix, CyTable cyEdgeAttrs, CyTable cyNodeAttrs, Set<Integer> geneUniverse, GeneSet sigGeneSet) {
		
		// test for existing node first
		CyNode hub_node = NetworkUtil.getNodeWithValue(current_network, cyNodeAttrs, CyNetwork.NAME, hub_name);
		if(hub_node == null)
			hub_node = current_network.addNode();
		
		current_network.getRow(hub_node).set(CyNetwork.NAME, hub_name);
		//current_view.updateView();
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
		
		
		if(shouldUseBypass(prefix, cyEdgeAttrs)) {
			// Use old way of setting bypass
			hubNodeView.setLockedValue(BasicVisualLexicon.NODE_SHAPE, NodeShapeVisualProperty.TRIANGLE);               
			hubNodeView.setLockedValue(BasicVisualLexicon.NODE_FILL_COLOR, EnrichmentMapVisualStyle.yellow);               
			hubNodeView.setLockedValue(BasicVisualLexicon.NODE_BORDER_PAINT, EnrichmentMapVisualStyle.yellow);
		}
		else {
			// a value less than -1 will be interpreted by the visual style as yellow
			current_row.set(prefix + EnrichmentMapVisualStyle.COLOURING_DATASET1, -2.0);
			if(map.getDataset(EnrichmentMap.DATASET2) != null) {
				current_row.set(prefix + EnrichmentMapVisualStyle.COLOURING_DATASET2, -2.0);
			}
		}
		
		return hub_node;
	}
    
    
	/**
	 * Returns true iff the user should be warned about an existing edge that does not pass the new cutoff.
	 * If the edge already exists it will be returned, if the edge had to be created it will not be returned.
	 */
	private void createEdge(String edge_name, CyNetwork current_network, CyNetworkView current_view, 
			                String prefix, CyTable cyEdgeAttrs, CyTable cyNodeAttrs, boolean passed_cutoff) {
		
		CyEdge edge = NetworkUtil.getEdgeWithValue(current_network, cyEdgeAttrs, CyNetwork.NAME, edge_name);
		GenesetSimilarity genesetSimilarity = geneset_similarities.get(edge_name);
		
		if(edge == null) {
			if(passed_cutoff) {
				CyNode hub_node = NetworkUtil.getNodeWithValue(current_network, cyNodeAttrs, CyNetwork.NAME, genesetSimilarity.getGeneset1_Name());
				CyNode gene_set = NetworkUtil.getNodeWithValue(current_network, cyNodeAttrs, CyNetwork.NAME, genesetSimilarity.getGeneset2_Name());
				edge = current_network.addEdge(hub_node, gene_set, false);
				taskResult.incrementCreatedEdgeCount();
			} else {
				return; // edge does not exist and does not pass cutoff, do nothing
			}
		}
		else {
			if(!passed_cutoff) {
				taskResult.addExistingEdgeFailsCutoff(edge);
			}
		}
		
		if(passed_cutoff)
			taskResult.incrementPassedCutoffCount();
			
		//add update view because view is returning null when we try to get the edge view.
		current_view.updateView();
		CyRow current_edgerow = cyEdgeAttrs.getRow(edge.getSUID());
		current_edgerow.set(CyNetwork.NAME, edge_name);
		
		View<CyEdge> edgeView = current_view.getEdgeView(edge);
		//create an attribute that stores the genes that are associated with this edge as an attribute list
		//only create the list if the hashkey 2 genes is not null Otherwise it take too much time to populate the list
		if(map.getHashkey2gene() != null){
		    List<String> gene_list = new ArrayList<>();
		    Set<Integer> genes_hash = genesetSimilarity.getOverlapping_genes();
		    for(Integer current : genes_hash) {
		        String gene = map.getGeneFromHashKey(current);
		        if(gene_list != null && gene != null) {
		            gene_list.add(gene);
		        }
		    }
		    Collections.sort(gene_list);
		    
		    current_edgerow.set(prefix + EnrichmentMapVisualStyle.OVERLAP_GENES, gene_list);                        
		}
 
		current_edgerow.set(prefix + EnrichmentMapVisualStyle.OVERLAP_SIZE, genesetSimilarity.getSizeOfOverlap());
		current_edgerow.set(prefix + EnrichmentMapVisualStyle.SIMILARITY_COEFFICIENT, genesetSimilarity.getSimilarity_coeffecient());
		current_edgerow.set(prefix + EnrichmentMapVisualStyle.HYPERGEOM_PVALUE, genesetSimilarity.getHypergeom_pvalue());
		current_edgerow.set(prefix + EnrichmentMapVisualStyle.ENRICHMENT_SET, genesetSimilarity.getEnrichment_set());
		if(!shouldUseBypass(prefix, cyEdgeAttrs)) {
			current_edgerow.set(prefix + EnrichmentMapVisualStyle.COLOURING_EDGES, -1); // special value for PA edge color
		}
		
		// Attributes related to the Hypergeometric Test
		switch(paParams.getSignature_rankTest()) {
			case MANN_WHIT:
				current_edgerow.set(prefix + EnrichmentMapVisualStyle.MANN_WHIT_PVALUE, genesetSimilarity.getMann_Whit_pValue());
				// want to fall through to the HYERGEOM case
			case HYPERGEOM:
				current_edgerow.set(prefix + EnrichmentMapVisualStyle.HYPERGEOM_PVALUE, genesetSimilarity.getHypergeom_pvalue());
				current_edgerow.set(prefix + EnrichmentMapVisualStyle.HYPERGEOM_N, genesetSimilarity.getHypergeom_N());
				current_edgerow.set(prefix + EnrichmentMapVisualStyle.HYPERGEOM_n, genesetSimilarity.getHypergeom_n());
				current_edgerow.set(prefix + EnrichmentMapVisualStyle.HYPERGEOM_m, genesetSimilarity.getHypergeom_m());
				current_edgerow.set(prefix + EnrichmentMapVisualStyle.HYPERGEOM_k, genesetSimilarity.getHypergeom_k());
			default: break;
		}
		
		// Set edge width attribute
		if(edgeView != null) {
			if(shouldUseBypass(prefix, cyEdgeAttrs)) {
				// default back to old way of using a bypass
				edgeView.setLockedValue(BasicVisualLexicon.EDGE_UNSELECTED_PAINT, EnrichmentMapVisualStyle.pink);
				edgeView.setLockedValue(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT, EnrichmentMapVisualStyle.pink);
				
				if(paParams.getSignature_rankTest() == PostAnalysisParameters.FilterMetric.HYPERGEOM) {
					//change "edge.lineWidth" based on Hypergeometric Value 
					if (geneset_similarities.get(edge_name).getHypergeom_pvalue() <= (paParams.getSignature_Hypergeom_Cutoff()/100) )
						edgeView.setLockedValue(BasicVisualLexicon.EDGE_WIDTH,8.0);	
					else 
						if (geneset_similarities.get(edge_name).getHypergeom_pvalue() <= (paParams.getSignature_Hypergeom_Cutoff()/10) )
							edgeView.setLockedValue(BasicVisualLexicon.EDGE_WIDTH,4.5);	                   
						else
							edgeView.setLockedValue(BasicVisualLexicon.EDGE_WIDTH,1.0);
				}
				if(paParams.getSignature_rankTest() == PostAnalysisParameters.FilterMetric.MANN_WHIT) {
					//change "edge.lineWidth" based on Hypergeometric Value 
					if (geneset_similarities.get(edge_name).getMann_Whit_pValue() <= (paParams.getSignature_Mann_Whit_Cutoff()/100) )
						edgeView.setLockedValue(BasicVisualLexicon.EDGE_WIDTH,8.0);	
					else 
						if (geneset_similarities.get(edge_name).getMann_Whit_pValue() <= (paParams.getSignature_Mann_Whit_Cutoff()/10) )
							edgeView.setLockedValue(BasicVisualLexicon.EDGE_WIDTH,4.5);	                   
						else
							edgeView.setLockedValue(BasicVisualLexicon.EDGE_WIDTH,1.0);
				}
			}
			else {
				// New way uses a function based on the WIDTH_EDGES column
				if(paParams.getSignature_rankTest() == PostAnalysisParameters.FilterMetric.HYPERGEOM) {
					//change "edge.lineWidth" based on Hypergeometric Value 
					if (genesetSimilarity.getHypergeom_pvalue() <= (paParams.getSignature_Hypergeom_Cutoff()/100) )
						current_edgerow.set(prefix + EnrichmentMapVisualStyle.WIDTH_EDGES, 0.8);
					else 
						if (genesetSimilarity.getHypergeom_pvalue() <= (paParams.getSignature_Hypergeom_Cutoff()/10) )
							current_edgerow.set(prefix + EnrichmentMapVisualStyle.WIDTH_EDGES, 0.45);
						else
							current_edgerow.set(prefix + EnrichmentMapVisualStyle.WIDTH_EDGES, 0.1);
				}
				if(paParams.getSignature_rankTest() == PostAnalysisParameters.FilterMetric.MANN_WHIT) {
					//change "edge.lineWidth" based on Hypergeometric Value 
					if (genesetSimilarity.getMann_Whit_pValue() <= (paParams.getSignature_Mann_Whit_Cutoff()/100) )
						current_edgerow.set(prefix + EnrichmentMapVisualStyle.WIDTH_EDGES, 0.8);
					else 
						if (genesetSimilarity.getMann_Whit_pValue() <= (paParams.getSignature_Mann_Whit_Cutoff()/10) )
							current_edgerow.set(prefix + EnrichmentMapVisualStyle.WIDTH_EDGES, 0.45);
						else
							current_edgerow.set(prefix + EnrichmentMapVisualStyle.WIDTH_EDGES, 0.1);
				}
			}
		}
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


	private void mannWhitney(Set<Integer> intersection, GenesetSimilarity comparison) {
		Map<Integer, Double> gene2score = ranks.getGene2Score();
		if (gene2score == null || gene2score.isEmpty()) {
			comparison.setMann_Whit_pValue(1.5);
		} else {
			// Calculate Mann-Whitney U pValue for Overlap
            Integer[] overlap_gene_ids = intersection.toArray(new Integer[intersection.size()]);
            double[] overlap_gene_scores = new double[overlap_gene_ids.length];
            
            // Get the scores for the overlap
            for (int k = 0; k < overlap_gene_ids.length; k++) {
            	overlap_gene_scores[k] = gene2score.get(overlap_gene_ids[k]);
            }
            
            double[] scores = ranks.getScores();
            MannWhitneyUTest mann_whit = new MannWhitneyUTest();
			double mannPval = mann_whit.mannWhitneyUTest(overlap_gene_scores, scores);
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
    
    
    /**
	 * If the user has a session file that was created with an older version of EnrichmentMap then
	 * they might not have the table columns that are required to support the new visual style.
	 * In this case revert back to using bypass (better than not working at all).
	 * @param cyEdgeAttrs The edge table.
	 */
	private static boolean shouldUseBypass(String prefix, CyTable cyEdgeAttrs) {
		return cyEdgeAttrs.getColumn(prefix + EnrichmentMapVisualStyle.WIDTH_EDGES) == null;
	}

	
    /*
     * Create Node attribute table with post analysis parameters not in the main EM table
     */
    private CyTable createNodeAttributes(CyNetwork network, String name, String prefix){
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
    private CyTable createEdgeAttributes(CyNetwork network, String name, String prefix){
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
    
    
  
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Generating Signature Hubs");
		buildDiseaseSignature(taskMonitor);
	}

	@Override
	public <R> R getResults(Class<? extends R> type) {
		if(BuildDiseaseSignatureTaskResult.class.equals(type)) {
			taskResult.setCancelled(cancelled);
			return type.cast(taskResult.build());
		}
		return null;
	}

}
