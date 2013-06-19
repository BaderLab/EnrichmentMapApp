package org.baderlab.csplugins.enrichmentmap.task;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.Ranking;
import org.baderlab.csplugins.enrichmentmap.parsers.EnrichmentResultFileReaderTask;
import org.baderlab.csplugins.enrichmentmap.parsers.ExpressionFileReaderTask;
import org.baderlab.csplugins.enrichmentmap.parsers.GMTFileReaderTask;
import org.baderlab.csplugins.enrichmentmap.parsers.RanksFileReaderTask;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;



/*
 * A dataset is the unit that consists of genesets, enrichments and expressions
 * Not all data is required 
 */

public class LoadDataSetTask implements TaskFactory{
	
	private DataSet dataset;
	private TaskIterator loadDataTaskIterator;
	private StreamUtil streamUtil;
    
    /*
     * In order to load a dataset you need to supply an dataset in which to
     * put all loaded information
     */
    public LoadDataSetTask(DataSet dataset,StreamUtil streamUtil){
    		this.dataset = dataset;
    		this.streamUtil = streamUtil;
    }
    
    /**
     * parse Dataset Files consists of a set of Tasks
     */
    public void load() {
    	   	
    		//first step: load GMT file if a file is specified in this dataset    		
    		if(dataset.getSetofgenesets().getFilename() != null && !dataset.getSetofgenesets().getFilename().isEmpty()){
    				//Load the geneset file
    				GMTFileReaderTask gmtFileTask = new GMTFileReaderTask(dataset,streamUtil);
    				loadDataTaskIterator.append(gmtFileTask);
    				
    			}
    		
    	
    		//second step: load the enrichments 
             EnrichmentResultFileReaderTask enrichmentResultsFilesTask = new EnrichmentResultFileReaderTask(dataset,streamUtil);
             loadDataTaskIterator.append(enrichmentResultsFilesTask);            
    		
    		//third step: load expression file if specified in the dataset.
    		//if there is no expression file then create a dummy file to associate with 
    		//this dataset so we can still use the expression viewer
        if(dataset.getDatasetFiles().getExpressionFileName() == null || dataset.getDatasetFiles().getExpressionFileName().isEmpty()){
        		CreateDummyExpressionTask dummyExpressionTask = new CreateDummyExpressionTask(dataset);
        		loadDataTaskIterator.append(dummyExpressionTask);
        }
        else{
    			ExpressionFileReaderTask expressionFileTask = new ExpressionFileReaderTask(dataset,streamUtil);
    			loadDataTaskIterator.append(expressionFileTask);
         }    	
    		//fourth step: Load ranks
    		//check to see if we have ranking files
    		if(dataset.getMap().getParams().getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_GSEA)){
    			if(dataset.getExpressionSets().getRanksByName(Ranking.GSEARanking) != null){
                RanksFileReaderTask ranking1 = new RanksFileReaderTask(dataset.getExpressionSets().getRanksByName(Ranking.GSEARanking).getFilename(),dataset,Ranking.GSEARanking,false,streamUtil);
                loadDataTaskIterator.append(ranking1);
    			}
    		}
    		else{
    			if(dataset.getExpressionSets().getRanksByName(EnrichmentMap.DATASET1) != null){
                    RanksFileReaderTask ranking1 = new RanksFileReaderTask(dataset.getExpressionSets().getRanksByName(EnrichmentMap.DATASET1).getFilename(),dataset,EnrichmentMap.DATASET1,false,streamUtil);
                    loadDataTaskIterator.append(ranking1);
                }
    			if(dataset.getExpressionSets().getRanksByName(EnrichmentMap.DATASET2) != null){
                    RanksFileReaderTask ranking1 = new RanksFileReaderTask(dataset.getExpressionSets().getRanksByName(EnrichmentMap.DATASET2).getFilename(),dataset,EnrichmentMap.DATASET2,false,streamUtil);
                    loadDataTaskIterator.append(ranking1);
                }
    		}
    }

       
	public TaskIterator createTaskIterator() {
		this.loadDataTaskIterator = new TaskIterator();
		load();
		return loadDataTaskIterator;
	}
	
	public TaskIterator getIterator(){
		if(loadDataTaskIterator == null)
			createTaskIterator();
		return loadDataTaskIterator;
	}
	
	public boolean isReady() {
		// TODO Auto-generated method stub
		return true;
	}
    
    
    
}
