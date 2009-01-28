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

public class InitializeGenesetsOfInterestTask implements Task {

    EnrichmentMapParameters params;

    // Keep track of progress for monitoring:
    private TaskMonitor taskMonitor = null;
    private boolean interrupted = false;

    public InitializeGenesetsOfInterestTask(EnrichmentMapParameters params, TaskMonitor taskMonitor) {
          this(params);
          this.taskMonitor = taskMonitor;
      }


    public InitializeGenesetsOfInterestTask(EnrichmentMapParameters params) {
        this.params = params;
    }

    public boolean initializeSets(){
        if(taskMonitor == null){
            throw new IllegalStateException("Task Monitor is not set");
        }
        try{
            //create subset of genesets that contains only the genesets of interest with pvalue and qbalue less than values
            //specified by the user.
            HashMap GSEAResults1 = params.getGseaResults1();
            HashMap GSEAResults2 = params.getGseaResults2();
            HashMap genesets = params.getFilteredGenesets();

            HashMap GSEAResults1OfInterest = params.getGseaResults1OfInterest();
            HashMap GSEAResults2OfInterest = params.getGseaResults2OfInterest();
            HashMap genesetsOfInterest = params.getGenesetsOfInterest();


            int currentProgress = 0;
            int maxValue = GSEAResults1.size() + GSEAResults2.size();

            //iterate through the GSEA Results to figure out which genesets we want to use
            for(Iterator i = GSEAResults1.keySet().iterator(); i.hasNext(); ){

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
                GSEAResult current_result = (GSEAResult)GSEAResults1.get(current_name);

                if(current_result.geneSetOfInterest(params.getPvalue(),params.getQvalue())){
                    GSEAResults1OfInterest.put(current_name,current_result);

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

            //Do the same for the second dataset if there is a second dataset
            if(params.isTwoDatasets()){

                //iterate through the GSEA Results to figure out which genesets we want to use
                for(Iterator j = GSEAResults2.keySet().iterator(); j.hasNext(); ){

                    // Calculate Percentage.  This must be a value between 0..100.
                    int percentComplete = (int) (((double) currentProgress / maxValue) * 100);
                     //  Estimate Time Remaining
                    long timeRemaining = maxValue - currentProgress;
                    if (taskMonitor != null) {
                        taskMonitor.setPercentCompleted(percentComplete);
                        taskMonitor.setStatus("Initializing genesets and gsea results of interest " + currentProgress + " of " + maxValue);
                        taskMonitor.setEstimatedTimeRemaining(timeRemaining);
                        }
                   currentProgress++;

                    String current_name = j.next().toString();
                    //check geneset result to see if it meets the required cutoffs
                    GSEAResult current_result = (GSEAResult)GSEAResults2.get(current_name);

                    if(current_result.geneSetOfInterest(params.getPvalue(),params.getQvalue())){
                        GSEAResults2OfInterest.put(current_name,current_result);

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
            }

            //after successfully made subsets of interest
            //once we have limited our analysis get rid of the initial genesets list
            genesets.clear();

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
         initializeSets();
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
