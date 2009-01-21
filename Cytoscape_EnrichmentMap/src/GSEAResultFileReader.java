import cytoscape.task.TaskMonitor;
import cytoscape.task.Task;
import cytoscape.data.readers.TextFileReader;

import javax.swing.*;
import java.util.HashMap;

/**
 * Created by
 * User: risserlin
 * Date: Jan 8, 2009
 * Time: 3:04:02 PM
 */
public class GSEAResultFileReader implements Task {


    private EnrichmentMapParameters params;

    private String GSEAResultFileName;

    private HashMap results ;

    // Keep track of progress for monitoring:

    private TaskMonitor taskMonitor = null;
    private boolean interrupted = false;

    public GSEAResultFileReader(EnrichmentMapParameters params, TaskMonitor taskMonitor, String FileName, int dataset) {
        this(params, FileName, dataset);
        this.taskMonitor = taskMonitor;
    }

    public GSEAResultFileReader(EnrichmentMapParameters params, String FileName, int dataset) {
        this.params = params;
        GSEAResultFileName = FileName;
        if(dataset == 1)
            results = params.getGseaResults1();
        else if(dataset == 2)
            results = params.getGseaResults2();


    }

    public void parse() {


         //open GSEA Result file

            TextFileReader reader = new TextFileReader(GSEAResultFileName);
            reader.read();
            String fullText = reader.getText();


            String [] lines = fullText.split("\n");

            int currentProgress = 0;
            int maxValue = lines.length;

             //skip the first line which just has the field names (start i=1)

            for (int i = 1; i < lines.length; i++) {
                String line = lines[i];

                String [] tokens = line.split("\t");
                int size = 0;
                double ES = 0.0;
                double NES = 0.0;
                double pvalue = 1.0;
                double FDRqvalue = 1.0;
                double FWERqvalue = 1.0;

                //The first column of the file is the name of the geneset
                String Name = tokens[0].toUpperCase();

                //The fourth column is the size of the geneset
                if(tokens[3].equalsIgnoreCase("")){
                    //do nothing
                }else{
                    size = Integer.parseInt(tokens[3]);
                }

                //The fifth column is the Enrichment score (ES)
                if(tokens[4].equalsIgnoreCase("")){
                    //do nothing
                }else{
                     ES = Double.parseDouble(tokens[4]);
                }

                //The sixth column is the Normalize Enrichment Score (NES)
                if(tokens[5].equalsIgnoreCase("")){
                    //do nothing
                }else{
                     NES = Double.parseDouble(tokens[5]);
                }

                //The seventh column is the nominal p-value
                if(tokens[6].equalsIgnoreCase("")){
                    //do nothing
                }else{
                    pvalue = Double.parseDouble(tokens[6]);
                }

                //the eighth column is the FDR q-value
                if(tokens[7].equalsIgnoreCase("")){
                    //do nothing
                }else{
                    FDRqvalue = Double.parseDouble(tokens[7]);
                }
                //the ninth column is the FWER q-value
                if(tokens[8].equalsIgnoreCase("")){
                    //do nothing
                }else{
                    FWERqvalue = Double.parseDouble(tokens[8]);
                }
                GSEAResult result = new GSEAResult(Name, size, ES, NES,pvalue,FDRqvalue,FWERqvalue);


                // Calculate Percentage.  This must be a value between 0..100.
                int percentComplete = (int) (((double) currentProgress / maxValue) * 100);
                //  Estimate Time Remaining
                long timeRemaining = maxValue - currentProgress;
                if (taskMonitor != null) {
                        taskMonitor.setPercentCompleted(percentComplete);
                        taskMonitor.setStatus("Parsing GSEA Results file " + currentProgress + " of " + maxValue);
                        taskMonitor.setEstimatedTimeRemaining(timeRemaining);
                    }
                currentProgress++;

                results.put(Name, result);
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
        return new String("Parsing GSEA Result file");
    }

}
