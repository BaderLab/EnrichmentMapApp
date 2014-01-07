package org.baderlab.csplugins.enrichmentmap.task;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.DefaultListModel;

import org.baderlab.csplugins.enrichmentmap.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class FilterSignatureGSTask extends AbstractTask{
	
	private EnrichmentMap map;
	private PostAnalysisParameters paParams = null;
	
	// Keep track of progress for monitoring:
    private int maxValue;
    private TaskMonitor taskMonitor = null;
    private boolean interrupted = false;
	
	public FilterSignatureGSTask(EnrichmentMap map,
			PostAnalysisParameters paParams) {
		super();
		this.map = map;
		this.paParams = paParams;
	}

	public void filterSignatureGS(){
		//Sort the Genesets:
        DefaultListModel signatureSetNames = paParams.getSignatureSetNames();
        DefaultListModel selectedSignatureSetNames = paParams.getSelectedSignatureSetNames();
        signatureSetNames.clear(); // clear, that we don't have duplicates afterwards - Bug #103 a

        //filter the signature genesets to only include genesets that overlap with the genesets
        //in our current map.
        HashMap<String, GeneSet> genesets_in_map = map.getAllGenesets();
        Object[] setsOfInterest = genesets_in_map.keySet().toArray();
        //get the value to be filtered by if there is a filter


        Object[] setNamesArray = paParams.getSignatureGenesets().getGenesets().keySet().toArray();
        Arrays.sort( setNamesArray );
        
        for (int i = 0; i < setNamesArray.length; i++) {
            if (! selectedSignatureSetNames.contains(setNamesArray[i])){

                if(paParams.isFilter()){
                    //only add the name if it overlaps with the sets in the map.
                    boolean matchfound = false;
                    for(int j = 0; j < setsOfInterest.length ; j++){
                        //check if this set overlaps with current geneset
                        HashSet <Integer> mapset = new HashSet<Integer>(genesets_in_map.get(setsOfInterest[j]).getGenes());
                        Integer original_size = mapset.size();
                        HashSet <Integer> paset = new HashSet<Integer>(paParams.getSignatureGenesets().getGenesets().get(setNamesArray[i]).getGenes());
                        mapset.retainAll(paset);

                        //if we are looking for percentage do:
                        if(paParams.getSignature_filterMetric() == paParams.PERCENT){
                            Double relative_per =  mapset.size()/original_size.doubleValue();
                            if(relative_per >= (Double)(paParams.getFilterValue()/100.0) ){
                                matchfound = true;
                                break;
                            }
                        }
                        //if we are looking for number in the overlap
                        else if(paParams.getSignature_filterMetric() == paParams.NUMBER){
                            if(mapset.size() >= paParams.getFilterValue()){
                                matchfound = true;
                                break;
                            }
                        }
                        else if(paParams.getSignature_filterMetric() == paParams.SPECIFIC){
                            Double relative_per =  mapset.size()/((Integer)(paset.size())).doubleValue();
                            if(relative_per >= (Double)(paParams.getFilterValue()/100.0) ){
                                matchfound = true;
                                break;
                            }
                        }
                    }
                    if(matchfound){
                        if (! signatureSetNames.contains(setNamesArray[i]))
                            signatureSetNames.addElement(setNamesArray[i] );
                    }
                }
                else{
                    signatureSetNames.addElement(setNamesArray[i] );
                }
            }
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
