/**
 **                       EnrichmentMap Cytoscape Plugin
 **
 ** Copyright (c) 2008-2009 Bader Lab, Donnelly Centre for Cellular and Biomolecular 
 ** Research, University of Toronto
 **
 ** Contact: http://www.baderlab.org
 **
 ** Code written by: Ruth Isserlin
 ** Authors: Daniele Merico, Ruth Isserlin, Oliver Stueker, Gary D. Bader
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** (at your option) any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** University of Toronto
 ** has no obligations to provide maintenance, support, updates, 
 ** enhancements or modifications.  In no event shall the
 ** University of Toronto
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** University of Toronto
 ** has been advised of the possibility of such damage.  
 ** See the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **
 **/

// $Id$
// $LastChangedDate$
// $LasrChangedRevision$
// $LastChangedBy$
// $HeadURL$

package org.baderlab.csplugins.enrichmentmap;
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
            HashMap enrichmentResults1 = params.getEnrichmentResults1();
            HashMap enrichmentResults2 = params.getEnrichmentResults2();
            HashMap genesets = params.getFilteredGenesets();

            HashMap enrichmentResults1OfInterest = params.getEnrichmentResults1OfInterest();
            HashMap enrichmentResults2OfInterest = params.getEnrichmentResults2OfInterest();
            HashMap genesetsOfInterest = params.getGenesetsOfInterest();


            int currentProgress = 0;
            int maxValue = enrichmentResults1.size() + enrichmentResults2.size();

            //iterate through the GSEA Results to figure out which genesets we want to use
            for(Iterator i = enrichmentResults1.keySet().iterator(); i.hasNext(); ){

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

                //if it is a GSEA Result then
                if(params.isGSEA()){
                    GSEAResult current_result = (GSEAResult)enrichmentResults1.get(current_name);

                    if(current_result.geneSetOfInterest(params.getPvalue(),params.getQvalue())){
                        enrichmentResults1OfInterest.put(current_name,current_result);

                        //check to see that the geneset in the results file is in the geneset talbe
                        //if it isn't then the user has given two files that don't match up
                        if(genesets.containsKey(current_name)){
                            GeneSet current_set = (GeneSet)genesets.get(current_name);
                            genesetsOfInterest.put(current_name, current_set);
                        }
                        else{
                            System.out.println(current_name);
                            //throw new IllegalThreadStateException("GMT file and GSEA Results file Do not match up.");
                        }
                    }
                }
                //otherwise it is a generic enrichment set
                else{
                   GenericResult current_result = (GenericResult)enrichmentResults1.get(current_name);

                    if(current_result.geneSetOfInterest(params.getPvalue(),params.getQvalue(), params.isFDR())){
                        enrichmentResults1OfInterest.put(current_name,current_result);

                        //check to see that the geneset in the results file is in the geneset talbe
                        //if it isn't then the user has given two files that don't match up
                        if(genesets.containsKey(current_name)){
                            GeneSet current_set = (GeneSet)genesets.get(current_name);
                            genesetsOfInterest.put(current_name, current_set);
                        }
                        else{
                            System.out.println(current_name);
                            //throw new IllegalThreadStateException("GMT file and Results file Do not match up.");
                        }
                    }
                }

            }

            //Do the same for the second dataset if there is a second dataset
            if(params.isTwoDatasets()){

                //iterate through the GSEA Results to figure out which genesets we want to use
                for(Iterator j = enrichmentResults2.keySet().iterator(); j.hasNext(); ){

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
                    //if it is a GSEA Result then
                    if(params.isGSEA()){
                       GSEAResult current_result = (GSEAResult)enrichmentResults2.get(current_name);

                        if(current_result.geneSetOfInterest(params.getPvalue(),params.getQvalue())){
                           enrichmentResults2OfInterest.put(current_name,current_result);

                           //check to see that the geneset in the results file is in the geneset talbe
                           //if it isn't then the user has given two files that don't match up
                           if(genesets.containsKey(current_name)){
                                 GeneSet current_set = (GeneSet)genesets.get(current_name);
                                 genesetsOfInterest.put(current_name, current_set);
                           }
                           else{
                               System.out.println(current_name);
                               //throw new IllegalThreadStateException("GMT file and GSEA Results file Do not match up.");
                           }
                       }
                    }
                    //otherwise it is a generic enrichment set
                   else{
                       GenericResult current_result = (GenericResult)enrichmentResults2.get(current_name);

                       if(current_result.geneSetOfInterest(params.getPvalue(),params.getQvalue(), params.isFDR())){
                           enrichmentResults2OfInterest.put(current_name,current_result);

                           //check to see that the geneset in the results file is in the geneset talbe
                           //if it isn't then the user has given two files that don't match up
                           if(genesets.containsKey(current_name)){
                                GeneSet current_set = (GeneSet)genesets.get(current_name);
                                genesetsOfInterest.put(current_name, current_set);
                           }
                           else{
                               System.out.println(current_name);
                               //throw new IllegalThreadStateException("GMT file and Results file Do not match up.");
                           }
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
