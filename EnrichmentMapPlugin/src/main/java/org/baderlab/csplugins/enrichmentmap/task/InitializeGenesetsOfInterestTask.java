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
// $LastChangedRevision$
// $LastChangedBy$
// $HeadURL$

package org.baderlab.csplugins.enrichmentmap.task;
import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Collections;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResult;
import org.baderlab.csplugins.enrichmentmap.model.GSEAResult;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.baderlab.csplugins.enrichmentmap.model.GenericResult;
import org.baderlab.csplugins.enrichmentmap.model.Rank;
import org.baderlab.csplugins.enrichmentmap.model.Ranking;

/**
 * Created by
 * User: risserlin
 * Date: Jan 9, 2009
 * Time: 10:59:05 AM
 * <p>
 * Task to create a subset of the geneset in the total gmt file that contains only the genesets with pvalue and q-value
 * less than threshold values specified by the user.
 */

public class InitializeGenesetsOfInterestTask implements Task {

    private EnrichmentMap map;

    // Keep track of progress for monitoring:
    private TaskMonitor taskMonitor = null;
    private boolean interrupted = false;

    /**
     * Class constructor - specifying the current task monitor
     *
     * @param params - enrichment map parameters of current map
     * @param taskMonitor - current task monitor
     */
    public InitializeGenesetsOfInterestTask(EnrichmentMap map, TaskMonitor taskMonitor) {
          this(map);
          this.taskMonitor = taskMonitor;
      }

    /**
     * Class constructor
     *
     * @param params - enrichment map parameters of current map
     */
    public InitializeGenesetsOfInterestTask(EnrichmentMap map) {
        this.map = map;
    }

    /**
     * filter the genesets, restricting them to only those passing the user specified thresholds.
     * @return  true if successful and false otherwise.
     */
    public boolean initializeSets(){
        /*if(taskMonitor == null){
            throw new IllegalStateException("Task Monitor is not set");
        } */
        //create subset of genesets that contains only the genesets of interest with pvalue and qbalue less than values
        //specified by the user.
               
        //Go through each Dataset populating the Gene set of interest in each dataset object
        HashMap<String,DataSet> datasets = map.getDatasets();
        for(Iterator<String> j = datasets.keySet().iterator();j.hasNext();){
        		String current_dataset_name = j.next();
        		DataSet current_dataset = datasets.get(current_dataset_name);
        		
        		HashMap<String,EnrichmentResult> enrichmentResults = current_dataset.getEnrichments().getEnrichments();       		
        		HashMap<String,GeneSet> genesets = current_dataset.getSetofgenesets().getGenesets();       		
        		HashMap<String, GeneSet> genesetsOfInterest = current_dataset.getGenesetsOfInterest().getGenesets();
        		
        		//get ranking files.
        		Ranking ranks = current_dataset.getExpressionSets().getRanksByName(current_dataset_name);

        		HashMap<Integer,Integer> rank2gene = null;
        		HashMap<Integer,Rank> gene2rank = null;
        		if(ranks != null){
            		rank2gene = ranks.getRank2gene();
            		gene2rank = ranks.getRanking();
        		}
        		int currentProgress = 0;
        		int maxValue = enrichmentResults.size() ;

        		//iterate through the GSEA Results to figure out which genesets we want to use
        		for(Iterator i = enrichmentResults.keySet().iterator(); i.hasNext(); ){

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
        			if(map.getParams().getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_GSEA)){
        				GSEAResult current_result = (GSEAResult)enrichmentResults.get(current_name);

        				//update the current geneset to reflect score at max
        				if((gene2rank != null) && (rank2gene != null)){

        					Set<Integer> allranks = rank2gene.keySet();
        					Integer largestRank = Collections.max(allranks);

        					//get the max at rank for this geneset
        					int currentRankAtMax = current_result.getRankAtMax();

        					if(currentRankAtMax != -1){
        						//check the ES score.  If it is negative we need to adjust the
        						//rank to count from the end of the list
        						double NES = current_result.getNES();
        						int genekey = -1;
        						//what gene corresponds to that rank
        						if(NES < 0 ){
        							//it is possible that some of the proteins in the rank list won't be rank 2gene
        							//conversion because some of the genes might not be in the genesets
        							//so the size of the list can't be used to trace up from the bottom of the
        							//ranks.  Instead we need to get the max rank used.
        							currentRankAtMax = largestRank - currentRankAtMax;

        							//reset the rank at max to reflect that it is counted from the bottom of the list.
        							current_result.setRankAtMax(currentRankAtMax);
        						}//check to see if this rank is in the conversion map
        						if(rank2gene.containsKey(currentRankAtMax))
        							genekey = rank2gene.get(currentRankAtMax);
        						else{
        							//if is possible that the gene associated with the max is not found in
        							//our gene 2 rank conversions because the rank by GSEA are off by 1 or two
        							//indexes (maybe a bug on their side).
        							//so depending on the NES score we need to fiddle with the rank to find the
        							//next protein that is the actual gene they are referring to

        							while (genekey == -1 && (currentRankAtMax <= largestRank && currentRankAtMax > 0)){
        								if(NES < 0 )
        									currentRankAtMax = currentRankAtMax + 1;
        								else
        									currentRankAtMax = currentRankAtMax - 1;
        								if(rank2gene.containsKey(currentRankAtMax))
        									genekey = rank2gene.get(currentRankAtMax);
        							}
        						}

        						if(genekey > -1){
        							//what is the score for that gene
        							double scoreAtMax = gene2rank.get(genekey).getScore();

        							current_result.setScoreAtMax(scoreAtMax);

        							//update the score At max in the EnrichmentResults as well
        						}
        					}
        				}// end of determining the leading edge
        				if(current_result.geneSetOfInterest(map.getParams().getPvalue(),map.getParams().getQvalue())){
        					//enrichmentResults1OfInterest.put(current_name,current_result);

        					//check to see that the geneset in the results file is in the geneset table
        					//if it isn't then the user has given two files that don't match up
        					if(genesets.containsKey(current_name)){
        						GeneSet current_set = (GeneSet)genesets.get(current_name);
        						genesetsOfInterest.put(current_name, current_set);
        					}
        					else if(genesetsOfInterest.containsKey(current_name)){
        						//already found in genesets of interest - loading from session
        					}
        					else
        						throw new IllegalThreadStateException("The Geneset: " + current_name + " is not found in the GMT file.");
        					
        					//TODO:check in multi species still needs this catch.
        					/*if(map.getParams().isTwoDatasets()) {
        						//if this geneset is also in the first set make sure the results is
        						//also put into the first set
        						if(enrichmentResults2.get(current_name) != null && genesets_set2 != null){
        							GSEAResult extra = (GSEAResult)enrichmentResults2.get(current_name);
        							enrichmentResults2OfInterest.put(current_name, extra);
        							GeneSet extra_set = (GeneSet)genesets_set2.get(current_name);
        							genesetsOfInterest_set2.put(current_name,extra_set);
        						}
        					}*/
        				}
        			}
            //otherwise it is a generic or David enrichment set
            else {
                GenericResult current_result = (GenericResult)enrichmentResults.get(current_name);

                if(current_result.geneSetOfInterest(map.getParams().getPvalue(),map.getParams().getQvalue(), map.getParams().isFDR())){
                    //enrichmentResults1OfInterest.put(current_name,current_result);

                    //check to see that the geneset in the results file is in the geneset talbe
                    //if it isn't then the user has given two files that don't match up
                    if(genesets.containsKey(current_name)){
                        GeneSet current_set = (GeneSet)genesets.get(current_name);
                        genesetsOfInterest.put(current_name, current_set);
                        
                      //TODO:check in multi species still needs this catch.
                        /*if(params.isTwoDatasets()) {
                        		//if this geneset is also in the first set make sure the results is
                        		//also put into the first set
                        		if(enrichmentResults2.get(current_name) != null){
                            		GenericResult extra = (GenericResult)enrichmentResults2.get(current_name);
                            		enrichmentResults2OfInterest.put(current_name, extra);
                            		GeneSet extra_set = (GeneSet)genesets_set2.get(current_name);
                            		genesetsOfInterest_set2.put(current_name,extra_set);
                        		}
                    		}*/
                    }
                    else if(genesetsOfInterest.containsKey(current_name) ){
                        //this geneset is already in the set of interest - loaded in from session
                    }
                    else
                        throw new IllegalThreadStateException("The Geneset: " + current_name + " is not found in the GMT file.");

                }
            }

        }
        }// end of current dataset.  If more than one dataset repeat process.

        //Do the same for the second dataset if there is a second dataset
 /*       if(params.isTwoDatasets()){



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
                    if(params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_GSEA)){
                        GSEAResult current_result = (GSEAResult)enrichmentResults2.get(current_name);

                        if(current_result.geneSetOfInterest(params.getPvalue(),params.getQvalue())){
                            enrichmentResults2OfInterest.put(current_name,current_result);

                            //check to see that the geneset in the results file is in the geneset talbe
                            //if it isn't then the user has given two files that don't match up
                            if(genesets_set2.containsKey(current_name)){
                                GeneSet current_set = (GeneSet)genesets_set2.get(current_name);
                                //if we have two distinct expression sets we need to use the filtered genesets from the second
                                //expression set for the second set of enrichments.
                                if(params.isTwoDistinctExpressionSets()){
                                   genesetsOfInterest_set2.put(current_name, current_set);

                                   //if this geneset is also in the first set make sure the results is
                                    //also put into the first set
                                    if(enrichmentResults1.get(current_name) != null){
                                        GSEAResult extra = (GSEAResult)enrichmentResults1.get(current_name);
                                        enrichmentResults1OfInterest.put(current_name, extra);
                                        GeneSet extra_set = (GeneSet)genesets.get(current_name);
                                        genesetsOfInterest.put(current_name,extra_set);
                                    }
                                }else
                                    genesetsOfInterest.put(current_name, current_set);
                            }
                            else if(genesetsOfInterest.containsKey(current_name) || genesetsOfInterest_set2.containsKey(current_name)){
                                //this geneset is already in the set of interest - loaded in from session
                            }
                            else{
                                throw new IllegalThreadStateException("The Geneset: " + current_name + " is not found in the GMT file.");
                            }
                        }
                        //update the current geneset to reflect score at max
                        if((dataset2ranks != null) && (rank2geneDataset2 != null)){

                            Set<Integer> allranks = rank2geneDataset2.keySet();
                            Integer largestRank = Collections.max(allranks);

                            //get the max at rank for this geneset
                            int currentRankAtMax = current_result.getRankAtMax();
                            if(currentRankAtMax != -1){
                                //check the ES score.  If it is negative we need to adjust the
                                //rank to count from the end of the list
                                double NES = current_result.getNES();
                                int genekey = -1;


                                //what gene corresponds to that rank
                                if(NES < 0 ){
                                    //it is possible that some of the proteins in the rank list won't be rank 2gene
                                    //conversion because some of the genes might not be in the genesets
                                    //so the size of the list can't be used to trace up from the bottaom of the
                                    //ranks.  Instead we need to get the max rank used.
                                    currentRankAtMax = largestRank - currentRankAtMax;
                                    //reset the rank at max to reflect that it is counted from the bottom of the list.
                                    current_result.setRankAtMax(currentRankAtMax);
                                }
                                //check to see if this rank is in the conversion map
                                if(rank2geneDataset2.containsKey(currentRankAtMax))
                                    genekey = rank2geneDataset2.get(currentRankAtMax);
                                else{                                        //if is possible that the gene associated with the max is not found in
                                    //our gene 2 rank conversions because the rank by GSEA are off by 1 or two
                                    //indexes (maybe a bug on their side).
                                    //so depending on the NES score we need to fiddle with the rank to find the
                                    //next protein that is the actual gene they are referring to

                                    while (genekey == -1 && (currentRankAtMax <= largestRank && currentRankAtMax > 0)){
                                        if(NES < 0 )
                                            currentRankAtMax = currentRankAtMax + 1;
                                        else
                                            currentRankAtMax = currentRankAtMax - 1;
                                        if(rank2geneDataset2.containsKey(currentRankAtMax))
                                            genekey = rank2geneDataset2.get(currentRankAtMax);
                                    }
                                }

                                if(genekey > -1){
                                    //what is the score for that gene
                                    double scoreAtMax = dataset2ranks.get(genekey).getScore();

                                    current_result.setScoreAtMax(scoreAtMax);
                                }
                            }


                        }
                    }
                    //otherwise it is a generic or David enrichment set
                    else {
                        GenericResult current_result = (GenericResult)enrichmentResults2.get(current_name);

                        if(current_result.geneSetOfInterest(params.getPvalue(),params.getQvalue(), params.isFDR())){
                            enrichmentResults2OfInterest.put(current_name,current_result);

                            //check to see that the geneset in the results file is in the geneset talbe
                            //if it isn't then the user has given two files that don't match up
                            if(genesets_set2.containsKey(current_name)){
                                GeneSet current_set = (GeneSet)genesets_set2.get(current_name);
                                if(params.isTwoDistinctExpressionSets()){
                                   genesetsOfInterest_set2.put(current_name, current_set);
                                    //if this geneset is also in the first set make sure the results is
                                    //also put into the first set
                                    if(enrichmentResults1.get(current_name) != null){
                                        GenericResult extra = (GenericResult)enrichmentResults1.get(current_name);
                                        enrichmentResults1OfInterest.put(current_name, extra);
                                        GeneSet extra_set = (GeneSet)genesets.get(current_name);
                                        genesetsOfInterest.put(current_name,extra_set);
                                    }
                                }else
                                    genesetsOfInterest.put(current_name, current_set);
                            }
                            else if(genesetsOfInterest.containsKey(current_name) || genesetsOfInterest_set2.containsKey(current_name)){
                                //this geneset is already in the set of interest - loaded in from session
                            }
                            else
                                throw new IllegalThreadStateException("The Geneset: " + current_name + " is not found in the GMT file.");
                        }
                    }


                }

        }*/
  
        
        //after successfully made subsets of interest
        //once we have limited our analysis get rid of the initial genesets list
        //TODO - make sure that the clear is no longer needed
//        genesets.clear();



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
