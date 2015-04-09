package org.baderlab.csplugins.enrichmentmap.task;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
	
	private final EnrichmentMap map;
	private final PostAnalysisParameters paParams;
	private final PostAnalysisInputPanel paPanel;
	
	
	public FilterSignatureGSTask(EnrichmentMap map, PostAnalysisParameters paParams, PostAnalysisInputPanel paPanel) {
		this.map = map;
		this.paParams = paParams;
		this.paPanel = paPanel;
	}
	

	private void filterSignatureGS(TaskMonitor taskMonitor) {
		try {
			DefaultListModel<String> signatureSetNames = paParams.getSignatureSetNames();
	        DefaultListModel<String> selectedSignatureSetNames = paParams.getSelectedSignatureSetNames();
	        signatureSetNames.clear(); // clear, that we don't have duplicates afterwards - Bug #103 a
	        
	        //filter the signature genesets to only include genesets that overlap with the genesets in our current map.
	        
	        // Use the same genesets that are saved to the session file (bug #66)
//	        HashMap<String, GeneSet> genesets_in_map = map.getAllGenesets();
	        HashMap<String, GeneSet> genesets_in_map = map.getAllGenesetsOfInterest();
	
	        String[] setNamesArray = paParams.getSignatureGenesets().getGenesets().keySet().toArray(new String[0]);
	        Arrays.sort(setNamesArray);
	        
	        FilterMetric filterMetric = null;
        	
	        for (int i = 0; i < setNamesArray.length; i++) {  
	        	
	        	int percentComplete = (int) (((double) i / setNamesArray.length) * 100);
	            taskMonitor.setStatusMessage("Analyzing geneset " + (i + 1) + " of " + setNamesArray.length);
	            taskMonitor.setProgress(percentComplete);
	            if (cancelled)
	                throw new InterruptedException();
	            String signatureGeneset = setNamesArray[i];
	            
	            if(!selectedSignatureSetNames.contains(signatureGeneset)) {
	            	boolean matchfound = false;
	            	
	                if(paParams.isFilter()) {
	                	if(filterMetric == null) {
		            		filterMetric = createFilterMetric(paParams);
		                 	filterMetric.init();
		            	}
	                	
	                    //only add the name if it overlaps with the sets in the map.
	                	for(String mapGeneset : genesets_in_map.keySet()) {
	                    	//check if this set overlaps with current geneset
	                        Set<Integer> mapset = new HashSet<>(genesets_in_map.get(mapGeneset).getGenes());
	                        int original_size = mapset.size();
	                        Set<Integer> paset = new HashSet<>(paParams.getSignatureGenesets().getGenesets().get(signatureGeneset).getGenes());
	                        mapset.retainAll(paset);
	                        
	                        matchfound = filterMetric.match(original_size, mapset, paset);
	                        if(matchfound)
	                        	break;
	                    }
	                } 
	                else {
	                	matchfound = true;
	                }
	                
                    if(matchfound) { 
                        if(!signatureSetNames.contains(signatureGeneset)) {
                            signatureSetNames.addElement(signatureGeneset);
                            if(paPanel != null) {
                            	paPanel.setAvSigCount(signatureSetNames.size());
                            }
                        }
                    }
                    
	            }
	        }
	        
		} catch (InterruptedException e) {
            taskMonitor.showMessage(Level.ERROR, "loading of GMT files cancelled");
        }
	}
	
	
	
	private FilterMetric createFilterMetric(PostAnalysisParameters paParams) {
		switch(paParams.getSignature_filterMetric()) {
			default:
			case PostAnalysisParameters.NUMBER:    return new NumberFilterMetric();
			case PostAnalysisParameters.PERCENT:   return new PercentFilterMetric();
			case PostAnalysisParameters.SPECIFIC:  return new SpecificFilterMetric();
			case PostAnalysisParameters.HYPERGEOM: return new HypergeomFilterMetric();
			case PostAnalysisParameters.MANN_WHIT: return new MannWhitFilterMetric();
		}
	}
	
	
	private interface FilterMetric {
		void init();
		boolean match(int original_size, Set<Integer> mapset, Set<Integer> paset);
	}
	
	
	private class PercentFilterMetric implements FilterMetric {
		public void init() { }
		
		public boolean match(int original_size, Set<Integer> mapset, Set<Integer> paset) {
			Double relative_per =  mapset.size()/(double)original_size;
            return relative_per >= (Double)(paParams.getFilterValue()/100.0);
		}
	}
	
	
	private class NumberFilterMetric implements FilterMetric {
		public void init() { }

		public boolean match(int original_size, Set<Integer> mapset, Set<Integer> paset) {
			Double relative_per =  mapset.size()/((Integer)(paset.size())).doubleValue();
            return relative_per >= (Double)(paParams.getFilterValue()/100.0);
		}
	}
	
	
	private class SpecificFilterMetric implements FilterMetric {
		public void init() { }
		
		public boolean match(int original_size, Set<Integer> mapset, Set<Integer> paset) {
			Double relative_per =  mapset.size()/((Integer)(paset.size())).doubleValue();
            return relative_per >= (Double)(paParams.getFilterValue()/100.0);
		}
	}
	
	
	private class HypergeomFilterMetric implements FilterMetric {

        int N;
        
		public void init() {
			N = paParams.getUniverseSize();
		}

		public boolean match(int original_size, Set<Integer> mapset, Set<Integer> paset) {
			// Calculate Hypergeometric pValue for Overlap
            // N: number of total genes (size of population / total number of balls)
            int n = paset.size();  //size of signature geneset (sample size / number of extracted balls)
            int m = original_size; //size of enrichment geneset (success Items / number of white balls in population)
            int k = mapset.size(); //size of intersection (successes /number of extracted white balls)
            
            double hyperPval;
            try {
	            if (k > 0) 
	                hyperPval = Hypergeometric.hyperGeomPvalue_sum(N, n, m, k, 0);
	            else // Correct p-value of empty intersections to 1 (i.e. not significant)
	                hyperPval = 1.0;
            } catch(ArithmeticException e) {
            	e.printStackTrace();
            	return false;
            }
            
            return hyperPval <= paParams.getSignature_Hypergeom_Cutoff();
		}
	}
	
	
	private class MannWhitFilterMetric implements FilterMetric {

		private Ranking ranks;
        
		public void init() {
			Map<String, DataSet> data_sets = map.getDatasets();
	    	DataSet dataset = data_sets.get(paParams.getSignature_dataSet());
	    	ranks = new Ranking();
	    	if (dataset != null) {
	    		ranks = dataset.getExpressionSets().getRanks().get(paParams.getSignature_rankFile());
	    	}
		}

		public boolean match(int original_size, Set<Integer> mapset, Set<Integer> paset) {
			// Calculate Mann-Whitney U pValue for Overlap
			Map<Integer, Double> gene2score = ranks.getGene2Score();
			Object[] overlap_gene_ids = mapset.toArray();
            if (overlap_gene_ids.length > 0) {
            	double[]  overlap_gene_scores = new double[overlap_gene_ids.length];
                
                // Get the scores for the overlap
                for (int p = 0; p < overlap_gene_ids.length; p++) {
                	overlap_gene_scores[p] = gene2score.get(overlap_gene_ids[p]);
                }
                
                double[] scores = ranks.getScores();
                MannWhitneyUTest mann_whit = new MannWhitneyUTest();
				double mannPval = mann_whit.mannWhitneyUTest(overlap_gene_scores, scores);
            	if (mannPval <= paParams.getSignature_Mann_Whit_Cutoff()) {
                    return true;
            	}
            }
			return false;
		}
	}
	
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("filtering Signature Gene set file");
		filterSignatureGS(taskMonitor);
	}

}
