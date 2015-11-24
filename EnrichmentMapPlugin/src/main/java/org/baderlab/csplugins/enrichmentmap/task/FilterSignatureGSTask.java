package org.baderlab.csplugins.enrichmentmap.task;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultListModel;

import org.baderlab.csplugins.enrichmentmap.FilterParameters.FilterType;
import org.baderlab.csplugins.enrichmentmap.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.baderlab.csplugins.enrichmentmap.model.Ranking;
import org.baderlab.csplugins.enrichmentmap.view.PostAnalysisInputPanel;
import org.baderlab.csplugins.mannwhit.MannWhitneyUTestSided;
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
	        
	        if(taskMonitor != null)
	        	taskMonitor.setStatusMessage("Analyzing "+ setNamesArray.length+ " genesets"  );
	        
	        
	        FilterMetric filterMetric = null;
        	
	        for (int i = 0; i < setNamesArray.length; i++) {  
	        	
	        	int percentComplete = (int) (((double) i / setNamesArray.length) * 100);
	            if(taskMonitor != null)
	            	taskMonitor.setProgress(percentComplete);
	            if (cancelled)
	                throw new InterruptedException();
	            String signatureGeneset = setNamesArray[i];
	            
	            if(!selectedSignatureSetNames.contains(signatureGeneset)) {
	            	boolean matchfound = false;
	            	
	                if(paParams.getFilterParameters().getType() != FilterType.NO_FILTER) {
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
		FilterType type = paParams.getFilterParameters().getType();
		switch(type) {
			case NUMBER:    
				return new NumberFilterMetric();
			case PERCENT:   
				return new PercentFilterMetric();
			case SPECIFIC:  
				return new SpecificFilterMetric();
			case HYPERGEOM: 
				return new HypergeomFilterMetric();
			case MANN_WHIT_TWO_SIDED: 
			case MANN_WHIT_GREATER:
			case MANN_WHIT_LESS:
				return new MannWhitFilterMetric(type);
			default:
				throw new RuntimeException("Unsupported FilterType: " + type);
		}
	}
	
	
	private abstract class FilterMetric {
		void init() {}
		/**
		 * @param mapGenesetSize number of genes associated with the node in the enrichment map
		 * @param intersection intersection of genes from enrichment map node and signature geneset
		 * @param signatureSet genes in the signature geneset (each hub node created by post-analysis)
		 */
		abstract boolean match(int mapGenesetSize, Set<Integer> intersection, Set<Integer> signatureSet);
	}
	
	
	private class PercentFilterMetric extends FilterMetric {
		public boolean match(int mapGenesetSize, Set<Integer> intersection, Set<Integer> signatureSet) {
			double relative_per =  (double)intersection.size() / (double)mapGenesetSize;
            return relative_per >= paParams.getFilterParameters().getValue(FilterType.PERCENT)/100.0;
		}
	}
	
	private class NumberFilterMetric extends FilterMetric {
		public boolean match(int mapGenesetSize, Set<Integer> intersection, Set<Integer> signatureSet) {
            return intersection.size() >= paParams.getFilterParameters().getValue(FilterType.NUMBER);
		}
	}
	
	private class SpecificFilterMetric extends FilterMetric {
		public boolean match(int mapGenesetSize, Set<Integer> intersection, Set<Integer> signatureSet) {
			double relative_per =  (double)intersection.size() / (double)signatureSet.size();
            return relative_per >= paParams.getFilterParameters().getValue(FilterType.SPECIFIC)/100.0;
		}
	}
	
	
	private class HypergeomFilterMetric extends FilterMetric {

        int N;
        
		public void init() {
			N = paParams.getUniverseSize();
		}

		public boolean match(int mapGenesetSize, Set<Integer> intersection, Set<Integer> signatureSet) {
			// Calculate Hypergeometric pValue for Overlap
            // N: number of total genes (size of population / total number of balls)
            int n = signatureSet.size();  //size of signature geneset (sample size / number of extracted balls)
            int m = mapGenesetSize; //size of enrichment geneset (success Items / number of white balls in population)
            int k = intersection.size(); //size of intersection (successes /number of extracted white balls)
            
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
            
            return hyperPval <= paParams.getFilterParameters().getValue(FilterType.HYPERGEOM);
		}
	}
	
	
	private class MannWhitFilterMetric extends FilterMetric {

		private Ranking ranks;
		private final FilterType type;
		
		public MannWhitFilterMetric(FilterType type) {
			this.type = type;
		}
        
		public void init() {
			Map<String, DataSet> data_sets = map.getDatasets();
	    	DataSet dataset = data_sets.get(paParams.getSignature_dataSet());
	    	ranks = new Ranking();
	    	if (dataset != null) {
	    		ranks = dataset.getExpressionSets().getRanks().get(paParams.getSignature_rankFile());
	    	}
		}

		public boolean match(int mapGenesetSize, Set<Integer> intersection, Set<Integer> signatureSet) {
			// Calculate Mann-Whitney U pValue for Overlap
			Map<Integer, Double> gene2score = ranks.getGene2Score();
			Object[] overlap_gene_ids = intersection.toArray();
            if (overlap_gene_ids.length > 0) {
            	double[]  overlap_gene_scores = new double[overlap_gene_ids.length];
                
                // Get the scores for the overlap
                for (int p = 0; p < overlap_gene_ids.length; p++) {
                	overlap_gene_scores[p] = gene2score.get(overlap_gene_ids[p]);
                }
                
                double[] scores = ranks.getScores();
                MannWhitneyUTestSided mann_whit = new MannWhitneyUTestSided();
				double mannPval = mann_whit.mannWhitneyUTest(overlap_gene_scores, scores, type.mannWhitneyTestType());
            	if (mannPval <= paParams.getFilterParameters().getValue(type)) {
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
