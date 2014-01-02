package org.baderlab.csplugins.enrichmentmap.task;


import java.util.HashMap;
import java.util.Iterator;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResult;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.baderlab.csplugins.enrichmentmap.model.GenericResult;
import org.baderlab.csplugins.enrichmentmap.model.GenesetSimilarity;
import org.baderlab.csplugins.enrichmentmap.model.SetOfEnrichmentResults;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.task.edit.MapTableToNetworkTablesTaskFactory;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

/**
 * Created by IntelliJ IDEA.
 * User: risserlin
 * Date: 11-02-16
 * Time: 4:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class BuildGMTEnrichmentMapTask implements TaskFactory{
    // Keep track of progress for monitoring:
    private int maxValue;
    private TaskMonitor taskMonitor = null;
    private boolean interrupted = false;

  //services required
    private StreamUtil streamUtil;
    private CyApplicationManager applicationManager;
    private CyNetworkManager networkManager;
    private CyNetworkViewManager networkViewManager;
    private CyNetworkViewFactory networkViewFactory;
    private CyNetworkFactory networkFactory;
    private CyTableFactory tableFactory;
    private CyTableManager tableManager;
    private  MapTableToNetworkTablesTaskFactory mapTableToNetworkTable;
    
      
    private EnrichmentMap map;
    TaskIterator buildEMGMTTaskIterator;

    public BuildGMTEnrichmentMapTask(EnrichmentMap map,
    		CyNetworkFactory networkFactory, CyApplicationManager applicationManager, 
    		CyNetworkManager networkManager, CyNetworkViewManager networkViewManager,
    		CyTableFactory tableFactory,CyTableManager tableManager, MapTableToNetworkTablesTaskFactory mapTableToNetworkTable) {
        
    		this.map = map;
        
        this.networkFactory = networkFactory;
        this.applicationManager = applicationManager;
        this.networkManager = networkManager;
        this.networkViewManager	= networkViewManager;
        this.tableFactory = tableFactory;
        this.tableManager = tableManager;
        this.mapTableToNetworkTable = mapTableToNetworkTable;
        

    }


    public void buildEnrichmentMap(){



        	   		//data is loaded into a dataset.
        	   		//Since we are building an enrichment map from only the gmt file default to put info into 
        	   		//dataset 1.
        	   		DataSet current_dataset = map.getDataset(EnrichmentMap.DATASET1);
        	   		
               //in this case all the genesets are of interest
               map.getParams().setMethod(EnrichmentMapParameters.method_generic);
               current_dataset.setGenesetsOfInterest(current_dataset.getSetofgenesets());
              
             //compute the geneset similarities
               ComputeSimilarityTask similarities = new ComputeSimilarityTask(map);
               buildEMGMTTaskIterator.append(similarities);

                HashMap<String, GenesetSimilarity> similarity_results = similarities.getGeneset_similarities();

                map.setGenesetSimilarity(similarity_results);
                
                HashMap<String, GeneSet> current_sets = current_dataset.getSetofgenesets().getGenesets();
                
                //create an new Set of Enrichment Results
                SetOfEnrichmentResults setofenrichments = new SetOfEnrichmentResults();
                
                HashMap<String,EnrichmentResult> currentEnrichments = setofenrichments.getEnrichments();
                
                //need also to put all genesets into enrichment results
                for(Iterator i = current_sets.keySet().iterator(); i.hasNext();){
                    String geneset1_name = i.next().toString();

                    GeneSet current = (GeneSet)current_sets.get(geneset1_name);

                    GenericResult temp_result = new GenericResult(current.getName(),current.getDescription(),0.01,current.getGenes().size());

                    currentEnrichments.put(current.getName(), temp_result);

                }
               current_dataset.setEnrichments(setofenrichments);               

             //build the resulting map
               CreateEnrichmentMapNetworkTask create_map = new CreateEnrichmentMapNetworkTask(map,networkFactory, applicationManager,networkManager,tableFactory,tableManager,mapTableToNetworkTable);
               buildEMGMTTaskIterator.append(create_map);
                             
       
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


	
	public void run(TaskMonitor taskMonitor) throws Exception {
		this.taskMonitor = taskMonitor;
		taskMonitor.setTitle("Building Enrichment Map based on GMT File");
		
		this.buildEnrichmentMap();
	}


	 public TaskIterator createTaskIterator() {
			this.buildEMGMTTaskIterator = new TaskIterator();
			this.buildEnrichmentMap();
			return buildEMGMTTaskIterator;
		}

		public boolean isReady() {
			
			return false;
		}

}
