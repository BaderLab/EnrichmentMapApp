
import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;
import cytoscape.data.readers.TextFileReader;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by
 * User: risserlin
 * Date: Jan 21, 2009
 * Time: 9:07:34 AM
 */
public class GCTFileReaderTask implements Task {

    private EnrichmentMapParameters params;

    private String GCTFileName;

    private String fullText;
    private String [] lines;

    private HashSet datasetGenes;
    private HashMap genes;

    // Keep track of progress for monitoring:
    private int maxValue;
    private TaskMonitor taskMonitor = null;
    private boolean interrupted = false;


     public GCTFileReaderTask(EnrichmentMapParameters params, TaskMonitor taskMonitor) {
        this(params);
        this.taskMonitor = taskMonitor;
    }

    public GCTFileReaderTask(EnrichmentMapParameters params)   {
        this.params = params;

        this.GCTFileName = params.getGCTFileName();
        this.genes = params.getGenes();
        this.datasetGenes = params.getDatasetGenes();

        //open GCT file
         TextFileReader reader = new TextFileReader(GCTFileName);
         reader.read();
         fullText = reader.getText();

        lines = fullText.split("\n");

    }
      public void parse() {
        int currentProgress = 0;
        maxValue = lines.length;


        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            String [] tokens = line.split("\t");

            //The first column of the file is the name of the geneset
            String Name = tokens[0];

            if(i==0 && Name.equalsIgnoreCase("#1.2")){
                //if the first line contains "#1.2" then this file is a gct file and we should ignore the first three lines
                //ignore the first three lines of a GCT file because it contains headers.
                i=2;
            }
            else if(i==0){
                //otherwise just skip the header line
                continue;
            }

            //Check to see if this gene is in the genes list
            if(genes.containsKey(Name)){
                //we want the genes hashmap and dataset genes hashmap to have the same keys so it is
                //easier to compare.
                datasetGenes.add(genes.get(Name));
            }

            // Calculate Percentage.  This must be a value between 0..100.
            int percentComplete = (int) (((double) currentProgress / maxValue) * 100);
            //  Estimate Time Remaining
            long timeRemaining = maxValue - currentProgress;
            if (taskMonitor != null) {
                    taskMonitor.setPercentCompleted(percentComplete);
                    taskMonitor.setStatus("Parsing GCT file " + currentProgress + " of " + maxValue);
                    taskMonitor.setEstimatedTimeRemaining(timeRemaining);
                }
            currentProgress++;


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
        return new String("Parsing GCT file");
    }
}
