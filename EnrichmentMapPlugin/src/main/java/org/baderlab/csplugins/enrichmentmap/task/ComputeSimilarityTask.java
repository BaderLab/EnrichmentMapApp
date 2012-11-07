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
import cytoscape.logger.CyLogger;
import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;

import java.util.*;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.baderlab.csplugins.enrichmentmap.model.GenesetSimilarity;

/**
 * Created by
 * User: risserlin
 * Date: Jan 9, 2009
 * Time: 2:14:52 PM
 * <p>
 * Goes through all the gene sets and computes the jaccard or overlap coeffecient
 * (depends on what the user specified in the input panel) for each
 * pair of gene sets.  (all pairwise comparisons are performed but only those passing
 * the user specified are stored in the hash map of gene set similarityes)
*/
public class ComputeSimilarityTask implements Task {
    static final int ENRICHMENT = 0, SIGNATURE = 1;
    
    private EnrichmentMap map;
    
    private int type;

    //Hash map of the geneset_similarities computed that pass the cutoff.
    private HashMap<String, GenesetSimilarity> geneset_similarities;

    // Keep track of progress for monitoring:
    private TaskMonitor taskMonitor = null;
    private boolean interrupted = false;
    
    private CyLogger logger = CyLogger.getLogger(ComputeSimilarityTask.class);

    /**
     * Constructor for Compute Similarity task
     *
     * @param map - current enrichment map 
     * @param taskMonitor - task monitor if it has already been set.
     */
    public ComputeSimilarityTask(EnrichmentMap map, TaskMonitor taskMonitor) {
          this(map);
          this.taskMonitor = taskMonitor;
      }


    public ComputeSimilarityTask(EnrichmentMap map) {
        this.map = map;
        this.geneset_similarities = map.getGenesetSimilarity();
        this.type = 0;
    }

    public ComputeSimilarityTask(EnrichmentMap map, int type) {
        this.map = map;
        this.geneset_similarities = map.getGenesetSimilarity();
        this.type = type;
    }    
    
    public boolean computeGenesetSimilarities(){
        try{
            HashMap<String, GeneSet> genesetsOfInterest = map.getAllGenesetsOfInterest();
            
            //if there are no gene sets of interest check to see if there are any genesets to use
            if(genesetsOfInterest == null || genesetsOfInterest.isEmpty())
            		genesetsOfInterest = map.getAllGenesets();
            if((genesetsOfInterest == null || genesetsOfInterest.isEmpty()))
            		this.logger.error("There are no genesets to compute similarity between");
            
            HashMap genesetsInnerLoop;
            String edgeType = "pp";
            
            if (type == ENRICHMENT ){
                genesetsInnerLoop = genesetsOfInterest;
                edgeType = map.getParams().getEnrichment_edge_type();
            }
            else if (type == SIGNATURE ){
            		//TODO refactor signature sets.
                genesetsInnerLoop = map.getParams().getSignatureGenesets();
                edgeType = PostAnalysisParameters.SIGNATURE_INTERACTION_TYPE;
            }
            else {
                genesetsInnerLoop = genesetsOfInterest;
                this.logger.error("Invalid type argument: " + type);
            }
            
            
            int currentProgress = 0;
            int maxValue = genesetsOfInterest.size();

            //figure out if we need to compute edges for two different expression sets or one.
            int enrichment_set =0;
            if(map.getParams().isTwoDistinctExpressionSets()){
            		//TODO if there are multiple species or different expression we need to loop through the datasets
            		//instead of treating all genesets as the same.
                enrichment_set = 1;
                //maxValue = genesetsOfInterest.size() + ((EnrichmentMap_multispecies)params.getEM()).getGenesetsOfInterest_set2().size();
            }

            //iterate through the each of the GSEA Results of interest
            for(Iterator i = genesetsOfInterest.keySet().iterator(); i.hasNext(); ){

                 // Calculate Percentage.  This must be a value between 0..100.
                int percentComplete = (int) (((double) currentProgress / maxValue) * 100);
                //  Estimate Time Remaining
                long timeRemaining = maxValue - currentProgress;
                if (taskMonitor != null) {
                   taskMonitor.setPercentCompleted(percentComplete);
                   taskMonitor.setStatus("Computing Geneset similarity " + currentProgress + " of " + maxValue);
                   taskMonitor.setEstimatedTimeRemaining(timeRemaining);
                }
                currentProgress++;

                String geneset1_name = i.next().toString();
                //for each individual geneset compute its jaccard index with all other genesets
                 for(Iterator j = genesetsInnerLoop.keySet().iterator(); j.hasNext(); ){

                    String geneset2_name = j.next().toString();

                    //Check to see if this comparison has been done
                     //The key for the set of geneset similarities is the
                     //combination of the two names.  Check for either variation name1_name2
                     //or name2_name1
                     String similarity_key1;
                     String similarity_key2;
                     if(enrichment_set == 0 ){
                        similarity_key1 = geneset1_name + " ("+ edgeType  + ") " + geneset2_name;
                        similarity_key2 = geneset2_name + " ("+ edgeType  + ") " + geneset1_name;
                     }
                     else{
                        similarity_key1 = geneset1_name + " ("+ EnrichmentMapParameters.ENRICHMENT_INTERACTION_TYPE_SET1  + ") " + geneset2_name ;
                        similarity_key2 = geneset2_name + " ("+ EnrichmentMapParameters.ENRICHMENT_INTERACTION_TYPE_SET1  + ") " + geneset1_name;
                     }

                     //first check to see if the terms are the same
                     if(geneset1_name.equalsIgnoreCase(geneset2_name)){
                        //don't compare two identical genesets
                     }
                     else if(geneset_similarities.containsKey(similarity_key1) || geneset_similarities.containsKey(similarity_key2)){
                         //skip this geneset comparison.  It has already been done.
                     }
                     else{
                         //get the two genesets
                         GeneSet geneset1 = (GeneSet)genesetsOfInterest.get(geneset1_name);
                         GeneSet geneset2 = (GeneSet)genesetsOfInterest.get(geneset2_name);

                         //if the geneset1 is null, check to see if it is post analysis node
                         if(geneset1 == null){
                             geneset1 = (GeneSet)genesetsInnerLoop.get(geneset1_name);
                         }
                         //if the geneset1 is null, check to see if it is post analysis node
                         if(geneset2 == null){
                             geneset2 = (GeneSet)genesetsInnerLoop.get(geneset2_name);
                         }


                         HashSet<Integer> genes1 = geneset1.getGenes();
                         HashSet<Integer> genes2 = geneset2.getGenes();

                        //Get the intersection
                         Set<Integer> intersection = new HashSet<Integer>(genes1);
                         intersection.retainAll(genes2);

                         //Get the union of the two sets
                         Set<Integer> union = new HashSet<Integer>(genes1);
                         union.addAll(genes2);

                         double coeffecient;

                         if(map.getParams().getSimilarityMetric().equalsIgnoreCase(EnrichmentMapParameters.SM_JACCARD)){

                            //compute Jaccard similarity
                            coeffecient = (double)intersection.size() / (double)union.size();
                         }
                         else if(map.getParams().getSimilarityMetric().equalsIgnoreCase(EnrichmentMapParameters.SM_OVERLAP)){
                             coeffecient = (double)intersection.size() / Math.min((double)genes1.size(), (double)genes2.size());
                         }
                         else { //else it must be combined
                             //Compute a combination of the overlap and jaccard coefecient
                             //we need both the Jaccard and the Overlap
                             double jaccard = (double)intersection.size() / (double)union.size();
                             double overlap = (double)intersection.size() / Math.min((double)genes1.size(), (double)genes2.size());

                             double k = map.getParams().getCombinedConstant();

                             coeffecient = (k * overlap) + ((1-k) * jaccard);

                         }

                         //create Geneset similarity object
                         GenesetSimilarity comparison = new GenesetSimilarity(geneset1_name,geneset2_name, coeffecient, map.getParams().getEnrichment_edge_type() ,(HashSet<Integer>)intersection,enrichment_set);

                         if (type == SIGNATURE) // as we iterate over the signature nodes in the inner loop, we have to switch the nodes in the edge name
                             this.geneset_similarities.put(similarity_key2,comparison);
                         else
                             this.geneset_similarities.put(similarity_key1,comparison);


                     }
                 }



            }
            //need to go through the second set of genesets in order to calculate the additional similarities
            //TODO:add two species support
 /*          if(map.getParams().isTwoDistinctExpressionSets()){
                enrichment_set = 2;
                HashMap<String, GeneSet> genesetsOfInterest_set2 = ((EnrichmentMap_multispecies)params.getEM()).getGenesetsOfInterest_set2();
                genesetsInnerLoop = genesetsOfInterest_set2;

               //iterate through the each of the GSEA Results of interest - for the second set.
                for(Iterator i = genesetsOfInterest_set2.keySet().iterator(); i.hasNext(); ){

                    // Calculate Percentage.  This must be a value between 0..100.
                    int percentComplete = (int) (((double) currentProgress / maxValue) * 100);
                    //  Estimate Time Remaining
                    long timeRemaining = maxValue - currentProgress;
                    if (taskMonitor != null) {
                        taskMonitor.setPercentCompleted(percentComplete);
                        taskMonitor.setStatus("Computing Geneset similarity " + currentProgress + " of " + maxValue);
                        taskMonitor.setEstimatedTimeRemaining(timeRemaining);
                    }
                    currentProgress++;

                    String geneset1_name = i.next().toString();
                    //for each individual geneset compute its jaccard index with all other genesets
                    for(Iterator j = genesetsInnerLoop.keySet().iterator(); j.hasNext(); ){

                        String geneset2_name = j.next().toString();

                        //Check to see if this comparison has been done
                        //The key for the set of geneset similarities is the
                        //combination of the two names.  Check for either variation name1_name2
                        //or name2_name1
                        String similarity_key1 = geneset1_name + " ("+ EnrichmentMapParameters.ENRICHMENT_INTERACTION_TYPE_SET2  + ") " + geneset2_name;
                        String similarity_key2 = geneset2_name + " ("+ EnrichmentMapParameters.ENRICHMENT_INTERACTION_TYPE_SET2  + ") " + geneset1_name;

                        //first check to see if the terms are the same
                        if(geneset1_name.equalsIgnoreCase(geneset2_name)){
                            //don't compare two identical genesets
                        }
                        else if(geneset_similarities.containsKey(similarity_key1) || geneset_similarities.containsKey(similarity_key2)){
                         //skip this geneset comparison.  It has already been done.
                        }
                        else{
                            //get the two genesets
                            GeneSet geneset1 = (GeneSet)genesetsOfInterest_set2.get(geneset1_name);
                            GeneSet geneset2 = (GeneSet)genesetsOfInterest_set2.get(geneset2_name);

                            HashSet<Integer> genes1 = geneset1.getGenes();
                            HashSet<Integer> genes2 = geneset2.getGenes();

                            //Get the intersection
                            Set<Integer> intersection = new HashSet<Integer>(genes1);
                            intersection.retainAll(genes2);

                            //Get the union of the two sets
                            Set<Integer> union = new HashSet<Integer>(genes1);
                            union.addAll(genes2);

                            double coeffecient;

                            if(map.getParams().getSimilarityMetric().equalsIgnoreCase(EnrichmentMapParameters.SM_JACCARD)){

                            //compute Jaccard similarity
                            coeffecient = (double)intersection.size() / (double)union.size();
                         }
                         else if(map.getParams().getSimilarityMetric().equalsIgnoreCase(EnrichmentMapParameters.SM_OVERLAP)){
                             coeffecient = (double)intersection.size() / Math.min((double)genes1.size(), (double)genes2.size());
                         }
                         else { //else it must be combined
                             //Compute a combination of the overlap and jaccard coefecient
                             //we need both the Jaccard and the Overlap
                             double jaccard = (double)intersection.size() / (double)union.size();
                             double overlap = (double)intersection.size() / Math.min((double)genes1.size(), (double)genes2.size());

                             double k = map.getParams().getCombinedConstant();

                             coeffecient = (k * overlap) + ((1-k) * jaccard);

                         }
                            //create Geneset similarity object
                            GenesetSimilarity comparison = new GenesetSimilarity(geneset1_name,geneset2_name, coeffecient, map.getParams().getEnrichment_edge_type() ,(HashSet<Integer>)intersection,enrichment_set);

                            if (type == SIGNATURE) // as we iterate over the signature nodes in the inner loop, we have to switch the nodes in the edge name
                                geneset_similarities.put(similarity_key2,comparison);
                            else
                                geneset_similarities.put(similarity_key1,comparison);


                        }
                    }
                }
               //Ticket #160 - there are missing edges between the two groups.
                //We need to also compute the edges between the two different groups.
                //Compute similarity for nodes that are in dataset1 or dataset2 (not necessarily significant in both)
               //Do dataset1
               HashMap<String, GeneSet> sig_genesets_set1 = params.getEM().getGenesetsOfInterest();
               HashMap<String, GeneSet> sig_genesets_set2 = ((EnrichmentMap_multispecies)params.getEM()).getGenesetsOfInterest_set2();

                HashMap<String, GeneSet> genesetsInnerLoop_missingedges = params.getEM().getGenesets();
                HashMap<String, GeneSet> genesetsOfInterest_missingedges = params.getEM().getGenesets();
               HashMap<String, EnrichmentResult> dataset1_results = params.getEM().getEnrichment(EnrichmentMap.DATASET1).getEnrichments();

                 //iterate through the each of the GSEA Results of interest - for the second set on the outer loop
               //and the first set on the inner loop
                for(Iterator i = genesetsOfInterest_missingedges.keySet().iterator(); i.hasNext(); ){
                    enrichment_set = 1;
                    String geneset1_name = i.next().toString();

                    //only look at this geneset if it is in dataset1
                    if(!dataset1_results.containsKey(geneset1_name))
                        continue;

                    //for each individual geneset compute its jaccard index with all other genesets
                    for(Iterator j = genesetsInnerLoop_missingedges.keySet().iterator(); j.hasNext(); ){

                        String geneset2_name = j.next().toString();
                        
                        //only look at this geneset if it is in dataset1
                        if(!dataset1_results.containsKey(geneset2_name))
                            continue;

                        //Check to see if this comparison has been done
                        //The key for the set of geneset similarities is the
                        //combination of the two names.  Check for either variation name1_name2
                        //or name2_name1
                        String similarity_key1 = geneset1_name + " ("+ EnrichmentMapParameters.ENRICHMENT_INTERACTION_TYPE_SET1  + ") " + geneset2_name;
                        String similarity_key2 = geneset2_name + " ("+ EnrichmentMapParameters.ENRICHMENT_INTERACTION_TYPE_SET1  + ") " + geneset1_name;

                        //first check to see if the terms are the same
                        if(geneset1_name.equalsIgnoreCase(geneset2_name)){
                            //don't compare two identical genesets
                        }
                        else if(geneset_similarities.containsKey(similarity_key1) || geneset_similarities.containsKey(similarity_key2)){
                         //skip this geneset comparison.  It has already been done.
                        }
                        else{

                            //only compute the similarity is geneset1_name is in either in dataset1 or dataset2 significant genesets
                            //  AND geneset2_name is in either dataset1 or dataset2 significant genesets
                            if( ((sig_genesets_set1.containsKey(geneset1_name) && !sig_genesets_set2.containsKey(geneset1_name))
                               &&  (!sig_genesets_set1.containsKey(geneset2_name) && sig_genesets_set2.containsKey(geneset2_name)))
                               || ((!sig_genesets_set1.containsKey(geneset1_name) && sig_genesets_set2.containsKey(geneset1_name))
                               &&  (sig_genesets_set1.containsKey(geneset2_name) && !sig_genesets_set2.containsKey(geneset2_name)))
                               || (sig_genesets_set2.containsKey(geneset1_name) && sig_genesets_set2.containsKey(geneset2_name))
                                    ){
                                //get the two genesets
                                GeneSet geneset1 = (GeneSet)genesetsOfInterest_missingedges.get(geneset1_name);
                                GeneSet geneset2 = (GeneSet)genesetsOfInterest_missingedges.get(geneset2_name);

                                HashSet<Integer> genes1 = geneset1.getGenes();
                                HashSet<Integer> genes2 = geneset2.getGenes();

                                //Get the intersection
                                Set<Integer> intersection = new HashSet<Integer>(genes1);
                                intersection.retainAll(genes2);

                                //Get the union of the two sets
                                Set<Integer> union = new HashSet<Integer>(genes1);
                                union.addAll(genes2);

                                double coeffecient;

                                if(map.getParams().getSimilarityMetric().equalsIgnoreCase(EnrichmentMapParameters.SM_JACCARD)){

                                    //compute Jaccard similarity
                                    coeffecient = (double)intersection.size() / (double)union.size();
                                }
                                else if(map.getParams().getSimilarityMetric().equalsIgnoreCase(EnrichmentMapParameters.SM_OVERLAP)){
                                    coeffecient = (double)intersection.size() / Math.min((double)genes1.size(), (double)genes2.size());
                                }
                                else { //else it must be combined
                                    //Compute a combination of the overlap and jaccard coefecient
                                    //we need both the Jaccard and the Overlap
                                    double jaccard = (double)intersection.size() / (double)union.size();
                                    double overlap = (double)intersection.size() / Math.min((double)genes1.size(), (double)genes2.size());

                                    double k = map.getParams().getCombinedConstant();

                                    coeffecient = (k * overlap) + ((1-k) * jaccard);

                                }
                                //create Geneset similarity object
                                GenesetSimilarity comparison = new GenesetSimilarity(geneset1_name,geneset2_name, coeffecient, map.getParams().getEnrichment_edge_type() ,(HashSet<Integer>)intersection,enrichment_set);

                                if (type == SIGNATURE) // as we iterate over the signature nodes in the inner loop, we have to switch the nodes in the edge name
                                    geneset_similarities.put(similarity_key2,comparison);
                                else
                                    geneset_similarities.put(similarity_key1,comparison);

                            }
                        }
                    }
                }

               //Do dataset2
               HashMap<String, GeneSet> genesetsInnerLoop_missingedges_d2 = ((EnrichmentMap_multispecies)params.getEM()).getGenesets_set2();
               HashMap<String, GeneSet> genesetsOfInterest_missingedges_d2 = ((EnrichmentMap_multispecies)params.getEM()).getGenesets_set2();
               HashMap<String, EnrichmentResult> dataset2_results = params.getEM().getEnrichment(EnrichmentMap.DATASET2).getEnrichments();
                 //iterate through the each of the GSEA Results of interest - for the second set on the outer loop
               //and the first set on the inner loop
                for(Iterator i = genesetsOfInterest_missingedges_d2.keySet().iterator(); i.hasNext(); ){
                    enrichment_set = 2;
                    String geneset1_name = i.next().toString();

                    //only look at this geneset if it is in dataset2
                    if(!dataset2_results.containsKey(geneset1_name))
                        continue;

                    //for each individual geneset compute its jaccard index with all other genesets
                    for(Iterator j = genesetsInnerLoop_missingedges_d2.keySet().iterator(); j.hasNext(); ){

                        String geneset2_name = j.next().toString();

                        if(!dataset2_results.containsKey(geneset2_name))
                            continue;

                        //Check to see if this comparison has been done
                        //The key for the set of geneset similarities is the
                        //combination of the two names.  Check for either variation name1_name2
                        //or name2_name1
                        String similarity_key1 = geneset1_name + " ("+ EnrichmentMapParameters.ENRICHMENT_INTERACTION_TYPE_SET2  + ") " + geneset2_name;
                        String similarity_key2 = geneset2_name + " ("+ EnrichmentMapParameters.ENRICHMENT_INTERACTION_TYPE_SET2  + ") " + geneset1_name;

                        //first check to see if the terms are the same
                        if(geneset1_name.equalsIgnoreCase(geneset2_name)){
                            //don't compare two identical genesets
                        }
                        else if(geneset_similarities.containsKey(similarity_key1) || geneset_similarities.containsKey(similarity_key2)){
                         //skip this geneset comparison.  It has already been done.
                        }
                        else{

                            //only compute the similarity is geneset1_name is in either in dataset1 or dataset2 significant genesets
                            //  AND geneset2_name is in either dataset1 or dataset2 significant genesets
                            if( ( ((sig_genesets_set1.containsKey(geneset1_name) && !sig_genesets_set2.containsKey(geneset1_name))
                               &&  (!sig_genesets_set1.containsKey(geneset2_name) && sig_genesets_set2.containsKey(geneset2_name)))
                               || ((!sig_genesets_set1.containsKey(geneset1_name) && sig_genesets_set2.containsKey(geneset1_name))
                               &&  (sig_genesets_set1.containsKey(geneset2_name) && !sig_genesets_set2.containsKey(geneset2_name)))
                               || (sig_genesets_set1.containsKey(geneset1_name) && sig_genesets_set1.containsKey(geneset2_name))
                                    )
                                    ){

                                //get the two genesets
                                GeneSet geneset1 = (GeneSet)genesetsOfInterest_missingedges_d2.get(geneset1_name);
                                GeneSet geneset2 = (GeneSet)genesetsOfInterest_missingedges_d2.get(geneset2_name);

                                HashSet<Integer> genes1 = geneset1.getGenes();
                                HashSet<Integer> genes2 = geneset2.getGenes();

                                //Get the intersection
                                Set<Integer> intersection = new HashSet<Integer>(genes1);
                                intersection.retainAll(genes2);

                                //Get the union of the two sets
                                Set<Integer> union = new HashSet<Integer>(genes1);
                                union.addAll(genes2);

                                double coeffecient;

                                if(map.getParams().getSimilarityMetric().equalsIgnoreCase(EnrichmentMapParameters.SM_JACCARD)){

                                    //compute Jaccard similarity
                                    coeffecient = (double)intersection.size() / (double)union.size();
                                 }
                                else if(map.getParams().getSimilarityMetric().equalsIgnoreCase(EnrichmentMapParameters.SM_OVERLAP)){
                                    coeffecient = (double)intersection.size() / Math.min((double)genes1.size(), (double)genes2.size());
                                }
                                else { //else it must be combined
                                    //Compute a combination of the overlap and jaccard coefecient
                                    //we need both the Jaccard and the Overlap
                                    double jaccard = (double)intersection.size() / (double)union.size();
                                    double overlap = (double)intersection.size() / Math.min((double)genes1.size(), (double)genes2.size());

                                    double k = map.getParams().getCombinedConstant();

                                     coeffecient = (k * overlap) + ((1-k) * jaccard);

                                }
                                //create Geneset similarity object
                                GenesetSimilarity comparison = new GenesetSimilarity(geneset1_name,geneset2_name, coeffecient, map.getParams().getEnrichment_edge_type() ,(HashSet<Integer>)intersection,enrichment_set);

                                if (type == SIGNATURE) // as we iterate over the signature nodes in the inner loop, we have to switch the nodes in the edge name
                                    geneset_similarities.put(similarity_key2,comparison);
                                else
                                    geneset_similarities.put(similarity_key1,comparison);

                            }
                        }
                    }
                }
            }
*/
        } catch(IllegalThreadStateException e){
            taskMonitor.setException(e, "Unable to compute similarity coeffecients");
            return false;
        }

       return true;
    }

    public HashMap<String, GenesetSimilarity> getGeneset_similarities() {
        return geneset_similarities;
    }

    /**
       * Run the Task.
       */
      public void run() {
         computeGenesetSimilarities();
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
          return new String("Computing geneset similarities");
      }

}
