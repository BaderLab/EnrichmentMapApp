
import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;
import cytoscape.data.readers.TextFileReader;

import java.util.HashMap;
import java.io.IOException;

/**
 * Created by
 * User: risserlin
 * Date: Jan 28, 2009
 * Time: 3:32:43 PM
 */
public class GenericResultFileReaderTask implements Task {
    // Keep track of progress for monitoring:

    private TaskMonitor taskMonitor = null;
    private boolean interrupted = false;

    private EnrichmentMapParameters params;
    private HashMap results ;
    private String filename;

    public GenericResultFileReaderTask(TaskMonitor taskMonitor, EnrichmentMapParameters params,int dataset) {
        this(params,dataset);
        this.taskMonitor = taskMonitor;
    }

    public GenericResultFileReaderTask(EnrichmentMapParameters params, int dataset) {
        this.params = params;
        if(dataset == 1){
            results = params.getEnrichmentResults1();
            filename = params.getEnrichmentDataset1FileName1();
        }
        else{
            results = params.getEnrichmentResults2();
            filename = params.getEnrichmentDataset2FileName1();
        }
    }

    public void parse(){

        TextFileReader reader = new TextFileReader(filename);
        reader.read();
        String fullText = reader.getText();

        //Get the current genesets so we can check that all the results are in the geneset list
        //and put the size of the genesets into the visual style
        HashMap genesets = params.getFilteredGenesets();

        String [] lines = fullText.split("\n");

        int currentProgress = 0;
        int maxValue = lines.length;
        boolean FDR = false;

         //skip the first line which just has the field names (start i=1)
        //check to see how many columns the data has
        String line = lines[0];
        String [] tokens = line.split("\t");
        int length = tokens.length;
        //if (length < 3)
           //not enough data in the file!! 

        for (int i = 1; i < lines.length; i++) {
            line = lines[i];

            tokens = line.split("\t");

            double pvalue = 1.0;
            double FDRqvalue = 1.0;
            GenericResult result;
            int gs_size = 0;
            double NES = 1.0;

            //The first column of the file is the name of the geneset
            String id = tokens[0].toUpperCase();

            if(genesets.containsKey(id)){
                GeneSet current_set = (GeneSet)genesets.get(id);
                gs_size = current_set.getGenes().size();
            }

            String Name = tokens[1].toUpperCase();

            //The seventh column is the nominal p-value
            if(tokens[2].equalsIgnoreCase("")){
                //do nothing
            }else{
                pvalue = Double.parseDouble(tokens[2]);
            }

            if(length > 3){
                //the fourth column is the FDR q-value
                if(tokens[3].equalsIgnoreCase("")){
                    //do nothing
                }else{
                    FDRqvalue = Double.parseDouble(tokens[3]);
                    FDR = true;
                }

                //the fifth column is the phenotype and it should be an integer but the only important
                //part is the sign
                if(length > 4){
                    if(tokens[4].equalsIgnoreCase("")){

                    }else{
                        NES = Double.parseDouble(tokens[4]);
                    }
                    result = new GenericResult(id,Name,pvalue,gs_size,FDRqvalue,NES);
                }
                else
                    result = new GenericResult(id, Name,pvalue,gs_size,FDRqvalue);
                                
            }
            else{
                result = new GenericResult(id, Name,pvalue,gs_size);
            }

            // Calculate Percentage.  This must be a value between 0..100.
            int percentComplete = (int) (((double) currentProgress / maxValue) * 100);
            //  Estimate Time Remaining
            long timeRemaining = maxValue - currentProgress;
            if (taskMonitor != null) {
                    taskMonitor.setPercentCompleted(percentComplete);
                    taskMonitor.setStatus("Parsing Generic Results file " + currentProgress + " of " + maxValue);
                    taskMonitor.setEstimatedTimeRemaining(timeRemaining);
                }
            currentProgress++;

            results.put(id, result);
        }
        if(FDR)
            params.setFDR(FDR);

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
        return new String("Parsing Generic Enrichment Result file");
    }
}
