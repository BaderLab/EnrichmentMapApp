package org.baderlab.csplugins.enrichmentmap.task;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.DefaultListModel;

import org.apache.commons.math3.stat.inference.MannWhitneyUTest;
import org.baderlab.csplugins.enrichmentmap.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.baderlab.csplugins.enrichmentmap.model.Ranking;
import org.baderlab.csplugins.enrichmentmap.view.PostAnalysisInputPanel;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskMonitor.Level;

public class FilterSignatureGSTask extends AbstractTask{
	
	private EnrichmentMap map;
	private PostAnalysisParameters paParams = null;
	private PostAnalysisInputPanel paPanel = null;
	
	// Keep track of progress for monitoring:
    private TaskMonitor taskMonitor = null;
    private boolean interrupted = false;
	
	public FilterSignatureGSTask(EnrichmentMap map, PostAnalysisParameters paParams, PostAnalysisInputPanel paPanel) {
		super();
		this.map = map;
		this.paParams = paParams;
		this.paPanel = paPanel;
	}

	public void filterSignatureGS(){
		try {
			DefaultListModel signatureSetNames = paParams.getSignatureSetNames();
	        DefaultListModel selectedSignatureSetNames = paParams.getSelectedSignatureSetNames();
	        signatureSetNames.clear(); // clear, that we don't have duplicates afterwards - Bug #103 a
	        //filter the signature genesets to only include genesets that overlap with the genesets
	        //in our current map.
	        HashMap<String, GeneSet> genesets_in_map = map.getAllGenesets();
	        //get the value to be filtered by if there is a filter
	        HashSet<Integer> EnrichmentGenes = new HashSet<Integer>();
	        for (Iterator<String> i = genesets_in_map.keySet().iterator(); i.hasNext(); ){
	            String setName = i.next();
	            EnrichmentGenes.addAll(genesets_in_map.get(setName).getGenes());
	        }
	        // Initialize parameters for Hypergeometric Test
	        int N = EnrichmentGenes.size();
	        int n = 0;
	        int m = 0;
	        int k = 0;
	
	        double hyperPval;
	        Object signatureGeneset, mapGeneset;
	        int percentComplete;
	
	        Object[] setNamesArray = paParams.getSignatureGenesets().getGenesets().keySet().toArray();
	        Arrays.sort(setNamesArray);
	        
	    	HashMap<String, DataSet> data_sets = this.map.getDatasets();
	    	DataSet dataset = data_sets.get(paParams.getSignature_dataSet());
	    	Ranking ranks = new Ranking();
	    	if (dataset != null) {
	    		ranks = dataset.getExpressionSets().getRanks().get(paParams.getSignature_rankFile());
	    	}
	    	HashMap<Integer, Double> gene2score = ranks.getGene2Score();
	        Object[] overlap_gene_ids;
	        double[] overlap_gene_scores;
	    	double mannPval;
	        MannWhitneyUTest mann_whit;
	        
	        for (int i = 0; i < setNamesArray.length; i++) {  
	        	percentComplete = (int) (((double) i / setNamesArray.length) * 100);
	            taskMonitor.setStatusMessage("Analyzing geneset " + (i + 1) + " of " + setNamesArray.length);
	            taskMonitor.setProgress(percentComplete);
	            if (interrupted)
	                throw new InterruptedException();
	            signatureGeneset = setNamesArray[i];
	            if (! selectedSignatureSetNames.contains(signatureGeneset)) {
	                if(paParams.isFilter()) {
	                    //only add the name if it overlaps with the sets in the map.
	                    boolean matchfound = false;
	                    for (Iterator<String> j = genesets_in_map.keySet().iterator(); j.hasNext(); ){
	                        mapGeneset = j.next();
	                    	//check if this set overlaps with current geneset
	                        HashSet <Integer> mapset = new HashSet<Integer>(genesets_in_map.get(mapGeneset).getGenes());
	                        Integer original_size = mapset.size();
	                        HashSet <Integer> paset = new HashSet<Integer>(paParams.getSignatureGenesets().getGenesets().get(signatureGeneset).getGenes());
	                        mapset.retainAll(paset);
	                        //if we want to do the hypergeometric test do:
	                        if (paParams.getSignature_filterMetric() == paParams.HYPERGEOM) {
	                            // Calculate Hypergeometric pValue for Overlap
	                            //N: number of total genes (size of population / total number of balls)
	                            n = paset.size();  //size of signature geneset (sample size / number of extracted balls)
	                            m = original_size;   //size of enrichment geneset (success Items / number of white balls in population)
	                            k = mapset.size(); //size of intersection (successes /number of extracted white balls)
	                            if (k > 0) 
	                                hyperPval = BuildDiseaseSignatureTask.hyperGeomPvalue_sum(N, n, m, k, 0);
	                            else // Correct p-value of empty intersections to 1 (i.e. not significant)
	                                hyperPval = 1.0;
	                            if (hyperPval <= paParams.getSignature_Hypergeom_Cutoff()) {
				                	matchfound = true;    
				                	break;
	                            }
	                        //if we are looking for percentage do:
	                        } else if(paParams.getSignature_filterMetric() == paParams.PERCENT) {
	                            Double relative_per =  mapset.size()/original_size.doubleValue();
	                            if(relative_per >= (Double)(paParams.getFilterValue()/100.0) ){
	                                matchfound = true;
	                                break;
	                            }
	                        //if we are looking for number in the overlap
	                        } else if(paParams.getSignature_filterMetric() == paParams.NUMBER) {
	                            if(mapset.size() >= paParams.getFilterValue()){
	                                matchfound = true;
	                                break;
	                            }
	                        } else if(paParams.getSignature_filterMetric() == paParams.SPECIFIC) {
	                            Double relative_per =  mapset.size()/((Integer)(paset.size())).doubleValue();
	                            if(relative_per >= (Double)(paParams.getFilterValue()/100.0) ){
	                                matchfound = true;
	                                break;
	                            }
	                        } else if (paParams.getSignature_filterMetric() == paParams.MANN_WHIT) {
		                        // Calculate Mann-Whitney U pValue for Overlap
	                            overlap_gene_ids = mapset.toArray();
	                            if (overlap_gene_ids.length > 0) {
		                            overlap_gene_scores = new double[overlap_gene_ids.length];
		                            
		                            // Get the scores for the overlap
		                            for (int p = 0; p < overlap_gene_ids.length; p++) {
		                            	overlap_gene_scores[p] = gene2score.get(overlap_gene_ids[p]);
		                            }
		                            
			                        mann_whit = new MannWhitneyUTest();
				                	mannPval = mann_whit.mannWhitneyUTest(overlap_gene_scores, ranks.getScores());
				                	if (mannPval <= paParams.getSignature_Mann_Whit_Cutoff()) {
	                                    matchfound = true;
	                                    break;
				                	}
	                            }
	                        }
	                    }
	                    if(matchfound){
	                        if (! signatureSetNames.contains(signatureGeneset)) {
	                            signatureSetNames.addElement(signatureGeneset);
	                            if(paPanel != null) {
	                            	this.paPanel.setAvSigCount(signatureSetNames.size());
	                            }
	                        }
	                    }
	                } else {
	                	signatureSetNames.addElement(signatureGeneset);
	                	if(paPanel != null) {
	                		this.paPanel.setAvSigCount(signatureSetNames.size());
	                	}
	                }
	            }
	        }
		} catch (InterruptedException e) {
            taskMonitor.showMessage(Level.ERROR, "loading of GMT files cancelled");
        }

	}
	
	
	/**
     * Non-blocking call to interrupt the task.
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
		taskMonitor.setTitle("filtering Signature Gene set file");
		
		filterSignatureGS();
	}

}
