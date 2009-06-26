package org.baderlab.csplugins.enrichmentmap;

import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;

import javax.swing.*;
import java.util.HashMap;

/**
 * Created by
 * User: risserlin
 * Date: Jan 29, 2009
 * Time: 9:45:04 AM
 */
public class BuildGenericEnrichmentMapTask implements Task {
    // Keep track of progress for monitoring:
    private int maxValue;
    private TaskMonitor taskMonitor = null;
    private boolean interrupted = false;

    private EnrichmentMapParameters params;
    private EnrichmentMapInputPanel inputPanel;

    public BuildGenericEnrichmentMapTask( EnrichmentMapInputPanel inputPanel, EnrichmentMapParameters params,TaskMonitor taskMonitor) {
        this.taskMonitor = taskMonitor;
        this.params = params;
        this.inputPanel = inputPanel;
    }

    public BuildGenericEnrichmentMapTask( EnrichmentMapInputPanel inputPanel, EnrichmentMapParameters params) {
        this.params = params;
        this.inputPanel = inputPanel;
    }


    public void buildGenericEnrichmentMap(){

         //Load in the GMT file
         try{
             //Load the GSEA geneset file
             GMTFileReaderTask gmtFile = new GMTFileReaderTask(params, taskMonitor);
             gmtFile.run();
             //boolean success = TaskManager.executeTask(gmtFile, config);

         } catch(Exception e){
             JOptionPane.showMessageDialog(inputPanel,"unable to load GMT file");

         }

        //Load the Data if the user has supplied the data file.
        if(params.isData()){
            //Load in the GCT file
            try{
                 //Load the GSEA geneset file
                GCTFileReaderTask gctFile1 = new GCTFileReaderTask(params,params.getGCTFileName1(),1,taskMonitor);
                gctFile1.run();
                params.getExpression().rowNormalizeMatrix();
                //boolean success = TaskManager.executeTask(gctFile, config);
                if(params.isData2()){
                    GCTFileReaderTask gctFile2 = new GCTFileReaderTask(params,params.getGCTFileName2(),2,taskMonitor);
                    gctFile2.run();
                    params.getExpression2().rowNormalizeMatrix();
                }
                //trim the genesets to only contain the genes that are in the data file.
                //Only perform if the data file has been supplied
                params.filterGenesets();

            } catch(Exception e){
                 JOptionPane.showMessageDialog(inputPanel,"unable to load GSEA DATA (.GCT) file");

            }
        }
        else{
            params.noFilter();
        }



         try{
             //Load the enrichmentresult files
             //Dataset1
             GenericResultFileReaderTask genericResultsFilesDataset1 = new GenericResultFileReaderTask(taskMonitor,params,  1);
             genericResultsFilesDataset1.run();

             //Load the second dataset only if there is a second dataset to load
             if (params.isTwoDatasets()){
                 //Dataset2
                GenericResultFileReaderTask genericResultsFilesDataset2 = new GenericResultFileReaderTask(taskMonitor,params,  2);
                 genericResultsFilesDataset2.run();
             }

             //Initialize the set of genesets and GSEA results that we want to compute over
             InitializeGenesetsOfInterestTask genesets_init = new InitializeGenesetsOfInterestTask(params,taskMonitor);
             genesets_init.run();
             //boolean success4 = TaskManager.executeTask(genesets_init,config);

             //compute the geneset similarities
             ComputeSimilarityTask similarities = new ComputeSimilarityTask(params,taskMonitor);
             similarities.run();
             //boolean success5 = TaskManager.executeTask(similarities,config);
             HashMap<String,GenesetSimilarity> similarity_results = similarities.getGeneset_similarities();

             params.setGenesetSimilarity(similarity_results);

             //build the resulting map
             VisualizeEnrichmentMapTask map = new VisualizeEnrichmentMapTask(params,taskMonitor);
             map.run();
             //boolean success3 =TaskManager.executeTask(map,config);

             //close input panel
             inputPanel.close();


         } catch(Exception e){

                    JOptionPane.showMessageDialog(inputPanel,"unable to build map");

                }


    }

    /**
     * Run the Task.
     */
    public void run() {
        buildGenericEnrichmentMap();
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
        return new String("Building Enrichment Map based on Generic Enrichment results");
    }
}
