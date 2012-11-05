package org.baderlab.csplugins.enrichmentmap.task;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.Ranking;
import org.baderlab.csplugins.enrichmentmap.parsers.EnrichmentResultFileReaderTask;
import org.baderlab.csplugins.enrichmentmap.parsers.ExpressionFileReaderTask;
import org.baderlab.csplugins.enrichmentmap.parsers.GMTFileReaderTask;
import org.baderlab.csplugins.enrichmentmap.parsers.RanksFileReaderTask;

import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;

/*
 * A dataset is the unit that consists of genesets, enrichments and expressions
 * Not all data is required 
 */

public class LoadDataSetTask implements Task{
	
	private DataSet dataset;
	
	// Keep track of progress for monitoring:
    private int maxValue;
    private TaskMonitor taskMonitor = null;
    private boolean interrupted = false;

    
    /*
     * In order to load a dataset you need to supply an dataset in which to
     * put all loaded information
     */
    public LoadDataSetTask(DataSet dataset){
    		this.dataset = dataset;
    }
    
    /*
     * In order to load a dataset you need to supply an dataset in which to
     * put all loaded information
     */
    public LoadDataSetTask(DataSet dataset, TaskMonitor taskMonitor){
    		this.dataset = dataset;
    		this.taskMonitor = taskMonitor;
    }

    /**
     * parse Dataset Files
     */
    public void parse() {
    	
    		//first step: load GMT file if a file is specified in this dataset
    		try{
    			if(!dataset.getSetofgenesets().getFilename().isEmpty()){
    				//Load the geneset file
    				GMTFileReaderTask gmtFile = new GMTFileReaderTask(dataset, taskMonitor);
    				gmtFile.run();
    			}
    		} catch (OutOfMemoryError e) {
    			taskMonitor.setException(e,"Out of Memory. Please increase memory allotement for cytoscape.");
    			return;
    		}  catch(Exception e){
    			taskMonitor.setException(e,"unable to load GMT file");
    			return;
    		}
    	
    
    		//second step: load expression file if specified in the dataset.
    		//if there is no expression file then create a dummy file to associate with 
    		//this dataset so we can still use the expression viewer
    		try{
    			ExpressionFileReaderTask expressionFile = new ExpressionFileReaderTask(dataset,taskMonitor);
            expressionFile.run();
    		} catch(IllegalThreadStateException e){
    			taskMonitor.setException(e,"Either no genes in the expression file are found in the GMT file \n OR the identifiers in the Expression and GMT do not match up.", "Expression and GMT file do not match");
    			return;
    		}catch (OutOfMemoryError e) {
    			taskMonitor.setException(e,"Out of Memory. Please increase memory allotement for cytoscape.");
    			return;
    		}catch(Exception e){
    			taskMonitor.setException(e,"unable to load GSEA DATA (.GCT) file");
    			return;
    		}
    	
    		//third step: load the enrichments 
    		try{
             EnrichmentResultFileReaderTask enrichmentResultsFiles = new EnrichmentResultFileReaderTask(dataset,taskMonitor);
             enrichmentResultsFiles.run();            

        } catch (OutOfMemoryError e) {
             taskMonitor.setException(e,"Out of Memory. Please increase memory allotement for cytoscape.");
             return;
        }   catch(Exception e){
             taskMonitor.setException(e,"unable to load enrichment results files");
             return;
         }
    	
    		//fourth step: Load ranks
    		//check to see if we have ranking files
    		if(dataset.getMap().getParams().getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_GSEA)){
    			if(dataset.getExpressionSets().getRanksByName(Ranking.GSEARanking) != null){
                RanksFileReaderTask ranking1 = new RanksFileReaderTask(dataset.getExpressionSets().getRanksByName(Ranking.GSEARanking).getFilename(),dataset,taskMonitor,false);
                ranking1.run();
            }
    		}
    		else{
    			if(dataset.getExpressionSets().getRanksByName(EnrichmentMap.DATASET1) != null){
                    RanksFileReaderTask ranking1 = new RanksFileReaderTask(dataset.getExpressionSets().getRanksByName(EnrichmentMap.DATASET1).getFilename(),dataset,taskMonitor,false);
                    ranking1.run();
                }
    			if(dataset.getExpressionSets().getRanksByName(EnrichmentMap.DATASET2) != null){
                    RanksFileReaderTask ranking1 = new RanksFileReaderTask(dataset.getExpressionSets().getRanksByName(EnrichmentMap.DATASET2).getFilename(),dataset,taskMonitor,false);
                    ranking1.run();
                }
    		}
    }
    
    /**
     * Run the Task.
     */
    public void run() {
        parse();
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


    /**
     * Gets the Task Title.
     *
     * @return human readable task title.
     */
    public String getTitle() {
        return new String("Parsing Dataset file");
    }

}
