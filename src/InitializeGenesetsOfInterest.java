import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by
 * User: risserlin
 * Date: Jan 9, 2009
 * Time: 10:59:05 AM
 */

public class InitializeGenesetsOfInterest implements Task {

    EnrichmentMapParameters params;

    // Keep track of progress for monitoring:
    private TaskMonitor taskMonitor = null;
    private boolean interrupted = false;

    public InitializeGenesetsOfInterest(EnrichmentMapParameters params, TaskMonitor taskMonitor) {
          this(params);
          this.taskMonitor = taskMonitor;
      }


    public InitializeGenesetsOfInterest(EnrichmentMapParameters params) {
        this.params = params;
    }

    public boolean computeMap(){
        if(taskMonitor == null){
            throw new IllegalStateException("Task Monitor is not set");
        }
        try{
            //create subset of genesets that contains only the genesets of interest with pvalue and qbalue less than values
            //specified by the user.
            HashMap GSEAResults = params.getGseaResults();
            HashMap genesets = params.getGenesets();

            HashMap GSEAResultsOfInterest = params.getGseaResultsOfInterest();
            HashMap genesetsOfInterest = params.getGenesetsOfInterest();

            int currentProgress = 0;
            int maxValue = GSEAResults.size();

            //iterate through the GSEA Results to figure out which genesets we want to use
            for(Iterator i = GSEAResults.keySet().iterator(); i.hasNext(); ){

                 // Calculate Percentage.  This must be a value between 0..100.
                int percentComplete = (int) (((double) currentProgress / maxValue) * 100);
                //  Estimate Time Remaining
                long timeRemaining = maxValue - currentProgress;
                if (taskMonitor != null) {
                   taskMonitor.setPercentCompleted(percentComplete);
                   taskMonitor.setStatus("Parsing GMT file " + currentProgress + " of " + maxValue);
                   taskMonitor.setEstimatedTimeRemaining(timeRemaining);
                }
                currentProgress++;

                String current_name = i.next().toString();
                //check geneset result to see if it meets the required cutoffs
                GSEAResult current_result = (GSEAResult)GSEAResults.get(current_name);

                if(current_result.geneSetOfInterest(params.getPvalue(),params.getQvalue())){
                    GSEAResultsOfInterest.put(current_name,current_result);

                    //check to see that the geneset in the results file is in the geneset talbe
                    //if it isn't then the user has given two files that don't match up
                   if(genesets.containsKey(current_name)){
                       GeneSet current_set = (GeneSet)genesets.get(current_name);
                       genesetsOfInterest.put(current_name, current_set);
                   }
                    else{
                       throw new IllegalThreadStateException("GMT file and GSEA Results file Do not match up.");
                    }
                }

            }

            //after successfully made subsets of interest
            //once we have limited our analysis get rid of the initial genesets list
            genesets.clear();
            GSEAResults.clear();           

        } catch(IllegalThreadStateException e){
            taskMonitor.setException(e, "Unable to match one of the Results to the GMT file provided.  Please make sure you supplied the correct gmt file");
            return false;
        }

       return true;
    }

    /**
       * Run the Task.
       */
      public void run() {
         computeMap();
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
          return new String("Initializing subset of genesets and GSEA results of interest");
      }

}
