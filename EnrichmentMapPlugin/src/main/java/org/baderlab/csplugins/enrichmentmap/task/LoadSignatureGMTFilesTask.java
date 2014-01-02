package org.baderlab.csplugins.enrichmentmap.task;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.DefaultListModel;

import org.baderlab.csplugins.enrichmentmap.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.baderlab.csplugins.enrichmentmap.parsers.GMTFileReaderTask;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;


public class LoadSignatureGMTFilesTask extends AbstractTask{

        private PostAnalysisParameters paParams = null;
        private EnrichmentMap map = null;
        private TaskMonitor taskMonitor = null;
        private boolean interrupted = false;
        /**
         * constructor w/ TaskMonitor
         * @param paParams
         * @param taskMonitor
         */
        @SuppressWarnings("unused")
        public LoadSignatureGMTFilesTask( EnrichmentMap map, PostAnalysisParameters paParams, TaskMonitor taskMonitor ){
            this(map, paParams );
            this.taskMonitor = taskMonitor;
        }
        
        /**
         * constructor w/o TaskMonitor
         * @param paParams
         */
        public LoadSignatureGMTFilesTask( EnrichmentMap map, PostAnalysisParameters paParams ){
            this.paParams = paParams;
            this.map = map;
        }

        /* (non-Javadoc)
         * @see cytoscape.task.Task#getTitle()
         */
        public String getTitle() {
            return new String("Loading Geneset Files...");
        }

        /* (non-Javadoc)
         * @see cytoscape.task.Task#halt()
         */
        public void halt() {
            this.interrupted = true;

        }

        /**
         * @see cytoscape.task.Task#run()
         */
        public void run() {
            //now a Cytoscape Task (LoadSignatureGenesetsTask)
            try {
/*                try{
                	//TODO:Add signature support
                    //Load the GSEA geneset file
                    //GMTFileReaderTask gmtFile_1 = new GMTFileReaderTask(map,paParams, taskMonitor, GMTFileReaderTask.ENRICHMENT_GMT);
                    //gmtFile_1.run();
    
                    //Load the Disease Signature geneset file
                    GMTFileReaderTask gmtFile_2 = new GMTFileReaderTask(map,paParams, taskMonitor, GMTFileReaderTask.SIGNATURE_GMT);
                    gmtFile_2.run();
    
                } catch (OutOfMemoryError e) {
                    taskMonitor.setException(e,"Out of Memory. Please increase memory allotment for Cytoscape.");
                    return;
                }   catch(Exception e){
                    taskMonitor.setException(e,"unable to load GMT files");
                    return;
                }
  */              
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
                    if (interrupted)
                        throw new InterruptedException();
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
            
            } catch (InterruptedException e) {
                //taskMonitor.setException(e, "loading of GMT files cancelled");
            }

        }
        /* (non-Javadoc)
         * @see cytoscape.task.Task#setTaskMonitor(cytoscape.task.TaskMonitor)
         */
        public void setTaskMonitor(TaskMonitor taskMonitor) {
            if (this.taskMonitor != null) {
                throw new IllegalStateException("Task Monitor is already set.");
            }
            this.taskMonitor = taskMonitor;
        }

		@Override
		public void run(TaskMonitor arg0) throws Exception {
			
			
		}

}
