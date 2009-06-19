

import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;

import javax.swing.*;
import java.util.HashMap;

/**
 * Created by
 * User: risserlin
 * Date: Jan 28, 2009
 * Time: 11:44:46 AM
 */
public class BuildGSEAEnrichmentMapTask implements Task {



    private GenericInputFilesPanel inputPanel;
    private EnrichmentMapParameters params;

    // Keep track of progress for monitoring:
    private int maxValue;
    private TaskMonitor taskMonitor = null;
    private boolean interrupted = false;


    public BuildGSEAEnrichmentMapTask(GenericInputFilesPanel inputPanel, EnrichmentMapParameters params) {
        this.inputPanel = inputPanel;
        this.params = params;
    }

    public void buildGSEAMap(){

        //Load in the GMT file
        try{
            //Load the GSEA geneset file
            GMTFileReaderTask gmtFile = new GMTFileReaderTask(params, taskMonitor);
            gmtFile.run();
            //boolean success = TaskManager.executeTask(gmtFile, config);

        } catch(Exception e){
            JOptionPane.showMessageDialog(inputPanel,"unable to load GMT file");

        }

        //Load in the GCT file
        try{
            //Load the GCT file
            GCTFileReaderTask gctFile1 = new GCTFileReaderTask(params,params.getGCTFileName1(),1,taskMonitor);
            gctFile1.run();
            params.getExpression().rowNormalizeMatrix();
            if(params.isData2()){
                GCTFileReaderTask gctFile2 = new GCTFileReaderTask(params,params.getGCTFileName2(),2,taskMonitor);
                gctFile2.run();
                params.getExpression2().rowNormalizeMatrix();
            }

        } catch(Exception e){
            JOptionPane.showMessageDialog(inputPanel,"unable to load GSEA DATA (.GCT) file");

        }

        //trim the genesets to only contain the genes that are in the data file.
        params.filterGenesets();

        try{
            //Load the GSEA result files
            //Dataset1 (each dataset should have two files.)
            GSEAResultFileReaderTask gseaResultsFilesDataset1File1 = new GSEAResultFileReaderTask(params,taskMonitor,  params.getEnrichmentDataset1FileName1(), 1);
            gseaResultsFilesDataset1File1.run();
            //boolean success1a = TaskManager.executeTask(gseaResultsFilesDataset1File1, config);
            GSEAResultFileReaderTask gseaResultsFilesDataset1File2 = new GSEAResultFileReaderTask(params,taskMonitor,  params.getEnrichmentDataset1FileName2(), 1);
            gseaResultsFilesDataset1File2.run();
            //boolean success1b = TaskManager.executeTask(gseaResultsFilesDataset1File2, config);

            //check to see if we have ranking files
            if(params.getDataset1RankedFile() != null){
                RanksFileReaderTask ranking1 = new RanksFileReaderTask(params,params.getDataset1RankedFile(),1,taskMonitor);
                ranking1.run();
            }

            //Load the second dataset only if there is a second dataset to load
            if (params.isTwoDatasets()){
                //Dataset2
                GSEAResultFileReaderTask gseaResultsFilesDataset2File1 = new GSEAResultFileReaderTask(params,taskMonitor,  params.getEnrichmentDataset2FileName1(), 2);
                gseaResultsFilesDataset2File1.run();
                //boolean success2a = TaskManager.executeTask(gseaResultsFilesDataset2File1, config);
                GSEAResultFileReaderTask gseaResultsFilesDataset2File2 = new GSEAResultFileReaderTask(params,taskMonitor,  params.getEnrichmentDataset2FileName2(), 2);
                gseaResultsFilesDataset2File2.run();
                //boolean success2b = TaskManager.executeTask(gseaResultsFilesDataset2File2, config);
                //check to see if we have ranking files
                if(params.getDataset2RankedFile() != null){
                    RanksFileReaderTask ranking2 = new RanksFileReaderTask(params,params.getDataset2RankedFile(),2,taskMonitor);
                    ranking2.run();
                }

            }

            //Initialize the set of genesets and GSEA results that we want to compute over
            InitializeGenesetsOfInterestTask genesets_init = new InitializeGenesetsOfInterestTask(params,taskMonitor);
            genesets_init.run();
            //boolean success4 = TaskManager.executeTask(genesets_init,config);

            //compute the geneset similarities
            ComputeSimilarityTask similarities = new ComputeSimilarityTask(params,taskMonitor);
            similarities.run();
            //boolean success5 = TaskManager.executeTask(similarities,config);
            HashMap<String, GenesetSimilarity> similarity_results = similarities.getGeneset_similarities();

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
        buildGSEAMap();
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
        return new String("Building Enrichment Map based on GSEA results");
    }
}