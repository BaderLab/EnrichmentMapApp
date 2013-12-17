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

// $Id: BuildEnrichmentMapTask.java 383 2009-10-08 20:06:35Z risserlin $
// $LastChangedDate: 2009-10-08 16:06:35 -0400 (Thu, 08 Oct 2009) $
// $LastChangedRevision: 383 $
// $LastChangedBy: risserlin $
// $HeadURL: svn+ssh://risserlin@server1.baderlab.med.utoronto.ca/svn/EnrichmentMap/trunk/EnrichmentMapPlugin/src/org/baderlab/csplugins/enrichmentmap/BuildEnrichmentMapTask.java $

package org.baderlab.csplugins.enrichmentmap.task;

import java.util.Iterator;

import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;


/**
 * Created by
 * User: risserlin
 * Date: Jan 28, 2009
 * Time: 11:44:46 AM
 * <p>
 * This class builds an Enrichment map from GSEA (Gene set Enrichment analysis) or Generic input.  There are two distinct ways
 * to build an enrichment map, from generic input or from GSEA input.  GSEA input has
 * specific files that were created by a run of GSEA, including two files specifying the enriched
 * results (one file for phenotype 1 and one file for phenotype 2) - the generic version
 * the enrichment results can be specified in one file.  The files also contain
 * additional parameters that would not be available to a generic enrichment analysis including
 * an Enrichment score (ES), normalized Enrichment score(NES).
 */
public class BuildEnrichmentMapTask implements Task {


    private EnrichmentMapParameters params;
    
    private String name = null;

    // Keep track of progress for monitoring:
    private TaskMonitor taskMonitor = null;
    private boolean interrupted = false;
    
    //values to track progress
    //TODO - implement usage
    //private int maxValue;
    


    /**
     * Constructor for Build enrichment map task - copies the parameters from
     * the passed instance of parameters into a new instance of parameters
     * which will be associated with the created map.
     *
     * @param params - the current specification of this run
     */
    public BuildEnrichmentMapTask( EnrichmentMapParameters params) {

        //create a new instance of the parameters
        this.params = new EnrichmentMapParameters();

        //copy the input variables into the new instance of the parameters
        this.params.copyInputParameters(params);

    }

    /**
     * Constructor for Build enrichment map task - copies the parameters from
     * the passed instance of parameters into a new instance of parameters
     * which will be associated with the created map.
     *
     * @param params - the current specification of this run
     * @params name - the name of the current 
     */
    public BuildEnrichmentMapTask( EnrichmentMapParameters params, String name) {
   		this(params);
    		this.name = name;


    }
    
    public BuildEnrichmentMapTask( EnrichmentMapParameters params, String name, TaskMonitor taskMonitor) {
   		this(params);
    		this.name = name;
    		this.taskMonitor = taskMonitor;


    }
    
    /**
     * buildEnrichmentMap - parses all GSEA input files and creates an enrichment map
     */
    public void buildEnrichmentMap(){
    		
    	
    		//create a new enrichment map
    		EnrichmentMap map = new EnrichmentMap(params,name);
    		
    		//Load in the first dataset
    		//call it Dataset 1.
    		DataSet dataset = map.getDataset(EnrichmentMap.DATASET1);    		
    		
    		//Get all user parameters
    		
    		//Load Dataset
    		try{
    			LoadDataSetTask loaddata = new LoadDataSetTask(dataset, taskMonitor);
    			loaddata.run();
    			
    			if(map.getParams().isTwoDatasets() && map.getDatasets().containsKey(EnrichmentMap.DATASET2)){
    				DataSet dataset2 = map.getDataset(EnrichmentMap.DATASET2);
    				
    				LoadDataSetTask loaddataset2 = new LoadDataSetTask(dataset2, taskMonitor);
        			loaddataset2.run();
        			params.setData2(true);
        			
        			//check to see if the two datasets are distinct
        			if(!(
        					(dataset.getDatasetGenes().containsAll(dataset2.getDatasetGenes())) && 
        					(dataset2.getDatasetGenes().containsAll(dataset.getDatasetGenes()))
        					))
        				params.setTwoDistinctExpressionSets(true);		
        				
    				
    			}
    			
    		} catch (OutOfMemoryError e) {
            taskMonitor.setException(e,"Out of Memory. Please increase memory allotement for cytoscape.");
            return;
        }  catch(Exception e){
            taskMonitor.setException(e,"unable to load DataSet");
            return;
        }
    	
    		//trim the genesets to only contain the genes that are in the data file.
        map.filterGenesets();

        //check to make sure that after filtering there are still genes in the genesets
        //if there aren't any genes it could mean that the IDs don't match or it could mean none
        //of the genes in the expression file are in the specified genesets.
        if(!map.checkGenesets())
                throw new IllegalThreadStateException("No genes in the expression file are found in the GMT file ");

        try{

            //Initialize the set of genesets and GSEA results that we want to compute over
            InitializeGenesetsOfInterestTask genesets_init = new InitializeGenesetsOfInterestTask(map,taskMonitor);
            genesets_init.run();

       } catch (OutOfMemoryError e) {
            taskMonitor.setException(e,"Out of Memory. Please increase memory allotement for cytoscape.");
            return;
        }catch(IllegalThreadStateException e){
            taskMonitor.setException(e,"Genesets defined in the Enrichment results file are not found in  gene set file (GMT).  (Click \"Show Error details\" to see which genesets is not found)Please make sure you are using the correct GMT file.");
            return;
        }

        try{
            //compute the geneset similarities
            ComputeSimilarityTask similarities = new ComputeSimilarityTask(map,taskMonitor);
            similarities.run();

            //build the resulting map
            VisualizeEnrichmentMapTask map_viz = new VisualizeEnrichmentMapTask(map,taskMonitor);
            map_viz.run();

        } catch (OutOfMemoryError e) {
            taskMonitor.setException(e,"Out of Memory. Please increase memory allotement for cytoscape.");

        }catch(Exception e){
            taskMonitor.setException(e,"unable to build/visualize map");
        }

    }

/*

  //Check to see if we are dealing with two distinct Data sets (i.e. two different species, or two different
  // expression platforms).  If the dataset are distinct then we need to separate the genesets
  private boolean isDistinctDatasets(){

      Set<Integer> expression_1_genes = new HashSet<Integer>();
      expression_1_genes.addAll(params.getEM().getExpression(EnrichmentMap.DATASET1).getGeneIds());
      Set<Integer> expression_2_genes = new HashSet<Integer>();
      //if there is expression set then grab the genes from the expression set
      if(params.getEM().getExpression(EnrichmentMap.DATASET2) != null)
              expression_2_genes.addAll(params.getEM().getExpression(EnrichmentMap.DATASET2).getGeneIds());
      //if there is no expression set then grab the genes from the defined genesetset2
      else{
          HashMap<String, GeneSet> geneset_set2 = ((EnrichmentMap_multispecies)params.getEM()).getGenesets_set2();
          if(geneset_set2 != null && geneset_set2.size()>0){
            for (Iterator i = geneset_set2.keySet().iterator(); i.hasNext();) {
                String currentGeneSet = (String)i.next();
                expression_2_genes.addAll(geneset_set2.get(currentGeneSet).getGenes());
            }
          }
      }

      if((expression_2_genes != null) && (expression_2_genes.size()>0)){

            expression_1_genes.removeAll(expression_2_genes);

            //if expression_1_genes is empty then all genes in 2 are in 1.
            //and if expression_1 genes are not empty then the two sets don't match and we have conflicting expression sets

            if(expression_1_genes.size() != 0){
                //params.setTwoDistinctExpressionSets(true);
                params.getEM().setDatasetGenes(new HashSet<Integer>(expression_1_genes));
                ((EnrichmentMap_multispecies)params.getEM()).setDatasetGenes_set2(new HashSet<Integer>(expression_2_genes));

                //only set genesets_set2 to the first if it is null
                if(((EnrichmentMap_multispecies)params.getEM()).getGenesets_set2().size() == 0){
                		((EnrichmentMap_multispecies)params.getEM()).setGenesets_set2(new HashMap<String,GeneSet>(params.getEM().getGenesets()));
                    
                }
                return true;
            }
            else{
                //if there were two david files but they are from the same species we want to merge the results
                if(((EnrichmentMap_multispecies)params.getEM()).getGenesets_set2().size() > 0)
                    params.getEM().getGenesets().putAll(((EnrichmentMap_multispecies)params.getEM()).getGenesets_set2());
            }
                //System.out.println("the expression files don't have the exact same number of entities.");
      }
      return false;
  }
*/
 /**
     * Run the Task.
     */
    public void run() {
        buildEnrichmentMap();
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
