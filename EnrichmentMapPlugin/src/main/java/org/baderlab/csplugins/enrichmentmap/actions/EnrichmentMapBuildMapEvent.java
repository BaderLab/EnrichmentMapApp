package org.baderlab.csplugins.enrichmentmap.actions;

import java.util.HashMap;
import java.util.Iterator;

import javax.swing.SwingUtilities;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.Ranking;
import org.baderlab.csplugins.enrichmentmap.parsers.EnrichmentResultFileReaderTask;
import org.baderlab.csplugins.enrichmentmap.parsers.ExpressionFileReaderTask;
import org.baderlab.csplugins.enrichmentmap.parsers.GMTFileReaderTask;
import org.baderlab.csplugins.enrichmentmap.parsers.RanksFileReaderTask;
import org.baderlab.csplugins.enrichmentmap.task.BuildEnrichmentMapTask;
import org.baderlab.csplugins.enrichmentmap.task.BuildGMTEnrichmentMapTask;
import org.baderlab.csplugins.enrichmentmap.task.ComputeSimilarityTask;
import org.baderlab.csplugins.enrichmentmap.task.CreateDummyExpressionTask;
import org.baderlab.csplugins.enrichmentmap.task.CreateEnrichmentMapNetworkTask;
import org.baderlab.csplugins.enrichmentmap.task.FilterGenesetsByDatasetGenes;
import org.baderlab.csplugins.enrichmentmap.task.InitializeGenesetsOfInterestTask;
import org.baderlab.csplugins.enrichmentmap.task.LoadInputDataTaskFactory;
import org.baderlab.csplugins.enrichmentmap.task.VisualizeEnrichmentMapTask;
import org.baderlab.csplugins.enrichmentmap.view.EnrichmentMapInputPanel;
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
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.swing.DialogTaskManager;
public class EnrichmentMapBuildMapEvent {

    private EnrichmentMap map;
    
	//services required
	    private CyApplicationManager applicationManager;
	    private CyNetworkManager networkManager;
	    private CyNetworkViewManager networkViewManager;
	    private CyNetworkViewFactory networkViewFactory;
	    private CyNetworkFactory networkFactory;
	    private CyTableFactory tableFactory;
	    private CyTableManager tableManager;
	    
	    private VisualMappingManager visualMappingManager;
	    private VisualStyleFactory visualStyleFactory;
	    
	    //we will need all three mappers
	    private VisualMappingFunctionFactory vmfFactoryContinuous;
	    private VisualMappingFunctionFactory vmfFactoryDiscrete;
	    private VisualMappingFunctionFactory vmfFactoryPassthrough;
	    
	    private StreamUtil streamUtil;
	    private DialogTaskManager dialog;
	    private CyLayoutAlgorithmManager layoutManager;
	    private MapTableToNetworkTablesTaskFactory mapTableToNetworkTable;
		

	    
	    
	    
	public EnrichmentMapBuildMapEvent(EnrichmentMap map,
				CyApplicationManager applicationManager,
				CyNetworkManager networkManager,
				CyNetworkViewManager networkViewManager,
				CyNetworkViewFactory networkViewFactory,
				CyNetworkFactory networkFactory, CyTableFactory tableFactory,
				CyTableManager tableManager,
				VisualMappingManager visualMappingManager,
				VisualStyleFactory visualStyleFactory,
				VisualMappingFunctionFactory vmfFactoryContinuous,
				VisualMappingFunctionFactory vmfFactoryDiscrete,
				VisualMappingFunctionFactory vmfFactoryPassthrough,
				DialogTaskManager dialog, StreamUtil streamUtil,CyLayoutAlgorithmManager layoutManager, MapTableToNetworkTablesTaskFactory mapTableToNetworkTable) {
			super();
			this.map = map;
			this.applicationManager = applicationManager;
			this.networkManager = networkManager;
			this.networkViewManager = networkViewManager;
			this.networkViewFactory = networkViewFactory;
			this.networkFactory = networkFactory;
			this.tableFactory = tableFactory;
			this.tableManager = tableManager;
			this.visualMappingManager = visualMappingManager;
			this.visualStyleFactory = visualStyleFactory;
			this.vmfFactoryContinuous = vmfFactoryContinuous;
			this.vmfFactoryDiscrete = vmfFactoryDiscrete;
			this.vmfFactoryPassthrough = vmfFactoryPassthrough;
			this.dialog = dialog;
			this.streamUtil = streamUtil;
			this.layoutManager = layoutManager;
			this.mapTableToNetworkTable = mapTableToNetworkTable;
		}





	public void build(){
		
	    TaskIterator currentTasks = new TaskIterator(); 
		
	    HashMap<String, DataSet> datasets = map.getDatasets();
	    
	    for(Iterator<String> i = datasets.keySet().iterator(); i.hasNext();){
	    		DataSet dataset = datasets.get(i.next());
	    		//first step: load GMT file if a file is specified in this dataset    		
	    		if(dataset.getSetofgenesets().getFilename() != null && !dataset.getSetofgenesets().getFilename().isEmpty()){
    				//Load the geneset file
    				GMTFileReaderTask gmtFileTask = new GMTFileReaderTask(dataset,streamUtil);
    				currentTasks.append(gmtFileTask);
    			}
    		
    	
	    		//second step: load the enrichments 
	    		EnrichmentResultFileReaderTask enrichmentResultsFilesTask = new EnrichmentResultFileReaderTask(dataset,streamUtil);
	    		currentTasks.append(enrichmentResultsFilesTask);            
    		
             //third step: load expression file if specified in the dataset.
             //if there is no expression file then create a dummy file to associate with 
             //this dataset so we can still use the expression viewer
             if(dataset.getDatasetFiles().getExpressionFileName() == null || dataset.getDatasetFiles().getExpressionFileName().isEmpty()){
            	 	CreateDummyExpressionTask dummyExpressionTask = new CreateDummyExpressionTask(dataset);
            	 	currentTasks.append(dummyExpressionTask);
             }
             else{
            	 	ExpressionFileReaderTask expressionFileTask = new ExpressionFileReaderTask(dataset,streamUtil);
            	 	currentTasks.append(expressionFileTask);
             }    	
             //fourth step: Load ranks
             //check to see if we have ranking files
             if(dataset.getMap().getParams().getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_GSEA)){
            	 	if(dataset.getExpressionSets().getRanksByName(Ranking.GSEARanking) != null){
            	 		RanksFileReaderTask ranking1 = new RanksFileReaderTask(dataset.getExpressionSets().getRanksByName(Ranking.GSEARanking).getFilename(),dataset,Ranking.GSEARanking,false,streamUtil);
            	 		currentTasks.append(ranking1);
            	 	}
             }
             else{
            	 	if(dataset.getExpressionSets().getRanksByName(EnrichmentMap.DATASET1) != null){
                    RanksFileReaderTask ranking1 = new RanksFileReaderTask(dataset.getExpressionSets().getRanksByName(EnrichmentMap.DATASET1).getFilename(),dataset,EnrichmentMap.DATASET1,false,streamUtil);
                    currentTasks.append(ranking1);
                }
    			if(dataset.getExpressionSets().getRanksByName(EnrichmentMap.DATASET2) != null){
                    RanksFileReaderTask ranking1 = new RanksFileReaderTask(dataset.getExpressionSets().getRanksByName(EnrichmentMap.DATASET2).getFilename(),dataset,EnrichmentMap.DATASET2,false,streamUtil);
                    currentTasks.append(ranking1);
                }
             }
	    }
	  		

	    
	  		//trim the genesets to only contain the genes that are in the data file.
	        FilterGenesetsByDatasetGenes filter = new FilterGenesetsByDatasetGenes(map);
	        currentTasks.append(filter);
	        
	        //Initialize the set of genesets and GSEA results that we want to compute over
	        InitializeGenesetsOfInterestTask genesets_init = new InitializeGenesetsOfInterestTask(map);
	        currentTasks.append(genesets_init);
	     
	        //compute the geneset similarities
	        ComputeSimilarityTask similarities = new ComputeSimilarityTask(map);
	        currentTasks.append(similarities);
	       

	        //build the resulting map
	        CreateEnrichmentMapNetworkTask create_map = new CreateEnrichmentMapNetworkTask(map,networkFactory, applicationManager,networkManager,tableFactory,tableManager,mapTableToNetworkTable);
	        currentTasks.append(create_map);
	  		

	  		//String errors = map.getParams().checkMinimalRequirements();
	  		
		
       
	        //visualize Network
	  		VisualizeEnrichmentMapTask map_viz = new VisualizeEnrichmentMapTask(map,networkFactory,networkManager, networkViewManager,
	         		networkViewFactory,visualMappingManager,visualStyleFactory,
	         		vmfFactoryContinuous, vmfFactoryDiscrete,vmfFactoryPassthrough,layoutManager);
	        currentTasks.append(map_viz);

		    dialog.execute(currentTasks);
   
	}
	    
}
