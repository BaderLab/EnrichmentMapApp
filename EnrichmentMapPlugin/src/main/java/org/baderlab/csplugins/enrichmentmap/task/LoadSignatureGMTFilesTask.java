package org.baderlab.csplugins.enrichmentmap.task;

import org.baderlab.csplugins.enrichmentmap.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.parsers.GMTFileReaderTask;
import org.baderlab.csplugins.enrichmentmap.view.PostAnalysisInputPanel;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;


public class LoadSignatureGMTFilesTask implements TaskFactory{

        private PostAnalysisParameters paParams = null;
        private PostAnalysisInputPanel paPanel = null;
        private EnrichmentMap map = null;
        private StreamUtil streamUtil;
        private TaskIterator loadSigGMTTaskIterator;        
        
        /**
         * constructor w/o TaskMonitor
         * @param paParams
         */
        private LoadSignatureGMTFilesTask(EnrichmentMap map, PostAnalysisParameters paParams, StreamUtil streamUtil) {
            this.paParams = paParams;
            this.map = map;
            this.streamUtil = streamUtil;
        }
        
        /**
         * constructor w map, post-analysis parameters and post-analysis panel
         * @param paParams
         */
        public LoadSignatureGMTFilesTask(EnrichmentMap map, PostAnalysisParameters paParams, StreamUtil streamUtil, PostAnalysisInputPanel paPanel) {
            this(map, paParams, streamUtil);
            this.paPanel = paPanel;
        }
        
        /* (non-Javadoc)
         * @see cytoscape.task.Task#getTitle()
         */
        public String getTitle() {
            return new String("Loading Signature Geneset Files...");
        }

        /**
         * @see cytoscape.task.Task#run()
         */
        public void load() {
            //now a Cytoscape Task (LoadSignatureGenesetsTask)
            try {

                    //Load the GSEA geneset file
                    //GMTFileReaderTask gmtFile_1 = new GMTFileReaderTask(map,paParams, GMTFileReaderTask.ENRICHMENT_GMT);
                    //this.loadSigGMTTaskIterator.append(gmtFile_1);
    
                    //Load the Disease Signature geneset file
                    GMTFileReaderTask gmtFile_2 = new GMTFileReaderTask(map,paParams, GMTFileReaderTask.SIGNATURE_GMT,streamUtil);
                    this.loadSigGMTTaskIterator.append(gmtFile_2);
                    
                    //filter signature genesets
                    FilterSignatureGSTask filterSigGs = new FilterSignatureGSTask(map,paParams,paPanel);
                    this.loadSigGMTTaskIterator.append(filterSigGs);
                } catch (OutOfMemoryError e) {
                    //taskMonitor.setException(e,"Out of Memory. Please increase memory allotment for Cytoscape.");
                    return;
                }   catch(Exception e){
                    //taskMonitor.setException(e,"unable to load GMT files");
                    return;
                }            
        } 


		public TaskIterator createTaskIterator() {
			this.loadSigGMTTaskIterator = new TaskIterator();
			load();
			return this.loadSigGMTTaskIterator;
		}

		public boolean isReady() {
			
			return true;
		}

}
