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


import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.task.edit.MapTableToNetworkTablesTaskFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

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
public class BuildEnrichmentMapTask implements TaskFactory {


    private EnrichmentMap map;
    
    private String name = null;
    
    private TaskIterator buildEMTaskIterator;

    //services required
    private CyApplicationManager applicationManager;
    private CyNetworkManager networkManager;
    private CyNetworkFactory networkFactory;
    private CyTableFactory tableFactory;
    private CyTableManager tableManager;
    private MapTableToNetworkTablesTaskFactory mapTableToNetworkTable;
    

    
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
    public BuildEnrichmentMapTask( EnrichmentMap map,
    		CyNetworkFactory networkFactory, CyApplicationManager applicationManager, 
    		CyNetworkManager networkManager, CyNetworkViewManager networkViewManager,
    		CyTableFactory tableFactory,CyTableManager tableManager, MapTableToNetworkTablesTaskFactory mapTableToNetworkTable) {
        
        this.map = map;
        
        this.networkFactory = networkFactory;
        this.applicationManager = applicationManager;
        this.networkManager = networkManager;
        this.tableFactory = tableFactory;
        this.tableManager = tableManager;
        this.mapTableToNetworkTable = mapTableToNetworkTable;
           

    }

    /**
     * Constructor for Build enrichment map task - copies the parameters from
     * the passed instance of parameters into a new instance of parameters
     * which will be associated with the created map.
     *
     * @param params - the current specification of this run
     * @params name - the name of the current 
     */
    public BuildEnrichmentMapTask( EnrichmentMap map, String name,
    		CyNetworkFactory networkFactory, CyApplicationManager applicationManager, 
    		CyNetworkManager networkManager, CyNetworkViewManager networkViewManager,
    		CyTableFactory tableFactory,CyTableManager tableManager, MapTableToNetworkTablesTaskFactory mapTableToNetworkTable) {
   		this(map,networkFactory, applicationManager,networkManager,networkViewManager,tableFactory,tableManager,mapTableToNetworkTable);
    		this.name = name;


    }
       
    /**
     * buildEnrichmentMap - parses all GSEA input files and creates an enrichment map
     */
    public void buildEnrichmentMap(){
    		
    	   			    		
    		//trim the genesets to only contain the genes that are in the data file.
        map.filterGenesets();

        //check to make sure that after filtering there are still genes in the genesets
        //if there aren't any genes it could mean that the IDs don't match or it could mean none
        //of the genes in the expression file are in the specified genesets.
        if(!map.checkGenesets())
                throw new IllegalThreadStateException("No genes in the expression file are found in the GMT file ");

        //Initialize the set of genesets and GSEA results that we want to compute over
        InitializeGenesetsOfInterestTask genesets_init = new InitializeGenesetsOfInterestTask(map);
        buildEMTaskIterator.append(genesets_init);
     
        //compute the geneset similarities
        ComputeSimilarityTask similarities = new ComputeSimilarityTask(map);
        buildEMTaskIterator.append(similarities);

        //build the resulting map
        CreateEnrichmentMapNetworkTask create_map = new CreateEnrichmentMapNetworkTask(map,networkFactory, applicationManager,networkManager,tableFactory,tableManager,mapTableToNetworkTable);
        buildEMTaskIterator.append(create_map);
                                   
       
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
     * Gets the Task Title.
     *
     * @return human readable task title.
     */
    public String getTitle() {
        return new String("Building Enrichment Map based on GSEA results");
    }

    public TaskIterator createTaskIterator() {
		this.buildEMTaskIterator = new TaskIterator();
		
		//add all the steps to the iterator
		buildEnrichmentMap();
		
		return buildEMTaskIterator;
	}

	public boolean isReady() {

		return true;
	}
}
