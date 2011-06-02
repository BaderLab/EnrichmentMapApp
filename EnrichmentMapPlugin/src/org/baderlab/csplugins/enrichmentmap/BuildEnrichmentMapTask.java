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

package org.baderlab.csplugins.enrichmentmap;

import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

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

    // Keep track of progress for monitoring:
    private int maxValue;
    private TaskMonitor taskMonitor = null;
    private boolean interrupted = false;


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
     * buildEnrichmentMap - parses all GSEA input files and creates an enrichment map
     */
    public void buildEnrichmentMap(){

        //no GMT file is required for DAVID processing
        if(!params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_DAVID)){

            //Load in the GMT file
            try{
                //Load the geneset file
                GMTFileReaderTask gmtFile = new GMTFileReaderTask(params, taskMonitor);
                gmtFile.run();

            } catch (OutOfMemoryError e) {
                taskMonitor.setException(e,"Out of Memory. Please increase memory allotement for cytoscape.");
                return;
            }  catch(Exception e){
                taskMonitor.setException(e,"unable to load GMT file");
                return;
            }
        }
        //if these are DAVID results we need to load the enrichment results before we load the expression files
        else{
            LoadEnrichmentFiles();
        }


        //Load the Data (expression or rank file) if the user has supplied the data file.
        if(params.isData() && params.getExpressionFileName1() != null && !params.getExpressionFileName1().equalsIgnoreCase("")){
            //Load in the expression or rank file
            try{
                   LoadExpressionFiles();
            } catch(IllegalThreadStateException e){
                taskMonitor.setException(e,"Either no genes in the expression file are found in the GMT file \n OR the identifiers in the Expression and GMT do not match up.", "Expression and GMT file do not match");
                return;
            }catch (OutOfMemoryError e) {
                taskMonitor.setException(e,"Out of Memory. Please increase memory allotement for cytoscape.");
                return;
            }catch(Exception e){
                taskMonitor.setException(e,"unable to load GSEA DATA (.GCT) file");
                return;
            }
        }
        else{
            params.noFilter();
            createDummyExpressionFile(1);
            if(params.isTwoDatasets()){
                createDummyExpressionFile(2);
                //check to see if the datasets are distinct
                params.setTwoDistinctExpressionSets(isDistinctDatasets());
            }
        }




        try{
            if(!params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_DAVID))
                LoadEnrichmentFiles();

             } catch (OutOfMemoryError e) {
                taskMonitor.setException(e,"Out of Memory. Please increase memory allotement for cytoscape.");
                return;
            }   catch(Exception e){

                taskMonitor.setException(e,"unable to load enrichment results files");
                return;
        }

        try{

            //Initialize the set of genesets and GSEA results that we want to compute over
            InitializeGenesetsOfInterestTask genesets_init = new InitializeGenesetsOfInterestTask(params,taskMonitor);
            genesets_init.run();

            //initialize bitsets for the genesets once the genesets have been filtered
            //TODO: remove, implemented for different purpose but never turned out to be useful.
            params.computeEnrichmentMapGenes();

       } catch (OutOfMemoryError e) {
            taskMonitor.setException(e,"Out of Memory. Please increase memory allotement for cytoscape.");
            return;
        }catch(IllegalThreadStateException e){
            taskMonitor.setException(e,"Genesets defined in the Enrichment results file are not found in  gene set file (GMT).  (Click \"Show Error details\" to see which genesets is not found)Please make sure you are using the correct GMT file.");
            return;
        }

        try{
            //compute the geneset similarities
            ComputeSimilarityTask similarities = new ComputeSimilarityTask(params,taskMonitor);
            similarities.run();

            HashMap<String, GenesetSimilarity> similarity_results = similarities.getGeneset_similarities();

            params.setGenesetSimilarity(similarity_results);

            //build the resulting map
            VisualizeEnrichmentMapTask map = new VisualizeEnrichmentMapTask(params,taskMonitor);
            map.run();

        } catch (OutOfMemoryError e) {
            taskMonitor.setException(e,"Out of Memory. Please increase memory allotement for cytoscape.");

        }catch(Exception e){
            taskMonitor.setException(e,"unable to build/visualize map");
        }



    }

    //load GMT Files

    //load expression Files
    private void LoadExpressionFiles(){
        //Load the expression or rank file
        ExpressionFileReaderTask expressionFile1 = new ExpressionFileReaderTask(params,1,taskMonitor);
        expressionFile1.run();
        params.getExpression().rowNormalizeMatrix();
        if(params.isData2()){
            ExpressionFileReaderTask expressionFile2 = new ExpressionFileReaderTask(params,2,taskMonitor);
            expressionFile2.run();
            params.getExpression2().rowNormalizeMatrix();
        }
        //if there are two expression sets check to see that they have the same gene ids.
        if(params.isData2()){
            params.setTwoDistinctExpressionSets(isDistinctDatasets());
         /*   Set<Integer> expression_1_genes = params.getExpression().getGeneIds();

            Set<Integer> expression_2_genes = params.getExpression2().getGeneIds();
            if((expression_2_genes != null) && (expression_2_genes.size()>0)){
                expression_1_genes.removeAll(expression_2_genes);

                //if expression_1_genes is empty then all genes in 2 are in 1.
                //and if expression_1 genes are not empty then the two sets don't match and we have conflicting expression sets

                if(expression_1_genes.size() != 0){
                    params.setTwoDistinctExpressionSets(true);
                    params.setDatasetGenes(new HashSet<Integer>((Set<Integer>)params.getExpression().getGeneIds()));
                    params.setDatasetGenes_set2(new HashSet<Integer>((Set<Integer>)params.getExpression2().getGeneIds()));

                    //only set genesets_set2 to the first if it is null
                    if(params.getGenesets_set2().size() == 0){
                        params.setGenesets_set2(new HashMap<String,GeneSet>(params.getGenesets()));
                        params.setFilteredGenesets_set2(new HashMap<String, GeneSet>(params.getFilteredGenesets()));
                    }
                    else{
                        params.setFilteredGenesets_set2(new HashMap<String, GeneSet>(params.getGenesets_set2()));
                    }
                }
                else{
                    //if there were two david files but they are from the same species we want to merge the results
                    if(params.getGenesets_set2().size() > 0)
                        params.getGenesets().putAll(params.getGenesets_set2());
                }
                //System.out.println("the expression files don't have the exact same number of entities.");
            } */
        }

        //trim the genesets to only contain the genes that are in the data file.
        params.filterGenesets();

        //check to make sure that after filtering there are still genes in the genesets
        //if there aren't any genes it could mean that the IDs don't match or it could mean none
        //of the genes in the expression file are in the specified genesets.
        if(!params.checkGenesets())
            throw new IllegalThreadStateException("No genes in the expression file are found in the GMT file ");

    }

    //Load enrichment Files
    private void LoadEnrichmentFiles(){
               //Load the GSEA result files
            //Dataset1 (each dataset should have two files.)
            EnrichmentResultFileReaderTask enrichmentResultsFilesDataset1File1 = new EnrichmentResultFileReaderTask(params,taskMonitor,  params.getEnrichmentDataset1FileName1(), 1);
            enrichmentResultsFilesDataset1File1.run();
            if(params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_GSEA)){
                EnrichmentResultFileReaderTask enrichmentResultsFilesDataset1File2 = new EnrichmentResultFileReaderTask(params,taskMonitor,  params.getEnrichmentDataset1FileName2(), 1);
                enrichmentResultsFilesDataset1File2.run();
            }

            //check to see if we have ranking files
            if(params.getDataset1RankedFile() != null){
                RanksFileReaderTask ranking1 = new RanksFileReaderTask(params,params.getDataset1RankedFile(),1,taskMonitor);
                ranking1.run();
            }

            //Load the second dataset only if there is a second dataset to load
            if (params.isTwoDatasets()){
                //Dataset2
                EnrichmentResultFileReaderTask enrichmentResultsFilesDataset2File1 = new EnrichmentResultFileReaderTask(params,taskMonitor,  params.getEnrichmentDataset2FileName1(), 2);
                enrichmentResultsFilesDataset2File1.run();

                if(params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_GSEA)){
                    EnrichmentResultFileReaderTask enrichmentResultsFilesDataset2File2 = new EnrichmentResultFileReaderTask(params,taskMonitor,  params.getEnrichmentDataset2FileName2(), 2);
                    enrichmentResultsFilesDataset2File2.run();
                }
                //check to see if we have ranking files
                if(params.getDataset2RankedFile() != null){
                    RanksFileReaderTask ranking2 = new RanksFileReaderTask(params,params.getDataset2RankedFile(),2,taskMonitor);
                    ranking2.run();
                }

            }
    }

    //Create a dummy expression file so that when no expression files are loaded you can still
    //use the intersect and union viewers.
    private void createDummyExpressionFile(int dataset){
        //in order to see the gene in the expression viewer we also need a dummy expression file
        //get all the genes
        HashMap<String, Integer> genes= params.getGenes();
        HashSet datasetGenes;
        if(dataset ==1 ){
            genes = params.getGenesetsGenes(params.getGenesets());
            datasetGenes= params.getDatasetGenes();
        }else{
            genes = params.getGenesetsGenes(params.getGenesets_set2());
            datasetGenes= params.getDatasetGenes_set2();
        }

        String[] titletokens = new String[3];
        titletokens[0] = "Name";
        titletokens[1] = "Description";
        titletokens[2] = "Dummy Expression";

        GeneExpressionMatrix expressionMatrix = new GeneExpressionMatrix(titletokens);
        HashMap<Integer,GeneExpression> expression = new HashMap<Integer, GeneExpression>();
        expressionMatrix.setExpressionMatrix(expression);

        String[] tokens = new String[3];
        tokens[0] = "tmp";
        tokens[1] = "tmp";
        tokens[2] = "0.25";

        for (Iterator i = genes.keySet().iterator(); i.hasNext();) {
             String currentGene = (String)i.next();

             int genekey = genes.get(currentGene);
             if(datasetGenes != null)
                datasetGenes.add(genekey);

                GeneExpression expres = new GeneExpression(currentGene, currentGene);
                expres.setExpression(tokens);


                double newMax = expres.newMax(expressionMatrix.getMaxExpression());
                if(newMax != -100)
                    expressionMatrix.setMaxExpression(newMax);
                double newMin = expres.newMin(expressionMatrix.getMinExpression());
                if (newMin != -100)
                    expressionMatrix.setMinExpression(newMin);

                expression.put(genekey,expres);

        }

        //set the number of genes
        expressionMatrix.setNumGenes(expressionMatrix.getExpressionMatrix().size());

        //make sure that params is set to show there is data
        if(dataset == 1){
            params.setData(true);
            params.setExpression(expressionMatrix);
        }else{
            params.setExpression2(expressionMatrix);
            params.setData2(true);
        }

    }

  //Check to see if we are dealing with two distinct Data sets (i.e. two different species, or two different
  // expression platforms).  If the dataset are distinct then we need to separate the genesets
  private boolean isDistinctDatasets(){

      Set<Integer> expression_1_genes = new HashSet<Integer>();
              expression_1_genes.addAll(params.getExpression().getGeneIds());
      Set<Integer> expression_2_genes = new HashSet<Integer>();
              expression_2_genes.addAll(params.getExpression2().getGeneIds());

      if((expression_2_genes != null) && (expression_2_genes.size()>0)){

            expression_1_genes.removeAll(expression_2_genes);

            //if expression_1_genes is empty then all genes in 2 are in 1.
            //and if expression_1 genes are not empty then the two sets don't match and we have conflicting expression sets

            if(expression_1_genes.size() != 0){
                //params.setTwoDistinctExpressionSets(true);
                params.setDatasetGenes(new HashSet<Integer>((Set<Integer>)params.getExpression().getGeneIds()));
                params.setDatasetGenes_set2(new HashSet<Integer>((Set<Integer>)params.getExpression2().getGeneIds()));

                //only set genesets_set2 to the first if it is null
                if(params.getGenesets_set2().size() == 0){
                    params.setGenesets_set2(new HashMap<String,GeneSet>(params.getGenesets()));
                    params.setFilteredGenesets_set2(new HashMap<String, GeneSet>(params.getFilteredGenesets()));
                }
                else{
                    params.setFilteredGenesets_set2(new HashMap<String, GeneSet>(params.getGenesets_set2()));
                }
                return true;
            }
            else{
                //if there were two david files but they are from the same species we want to merge the results
                if(params.getGenesets_set2().size() > 0)
                    params.getGenesets().putAll(params.getGenesets_set2());
            }
                //System.out.println("the expression files don't have the exact same number of entities.");
      }
      return false;
  }

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
