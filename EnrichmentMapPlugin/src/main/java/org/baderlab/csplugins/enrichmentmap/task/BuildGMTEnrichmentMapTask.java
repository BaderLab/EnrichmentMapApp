package org.baderlab.csplugins.enrichmentmap.task;

import cytoscape.task.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResult;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpression;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpressionMatrix;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.baderlab.csplugins.enrichmentmap.model.GenericResult;
import org.baderlab.csplugins.enrichmentmap.model.GenesetSimilarity;
import org.baderlab.csplugins.enrichmentmap.model.SetOfEnrichmentResults;

/**
 * Created by IntelliJ IDEA.
 * User: risserlin
 * Date: 11-02-16
 * Time: 4:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class BuildGMTEnrichmentMapTask implements Task{
    // Keep track of progress for monitoring:
    private int maxValue;
    private TaskMonitor taskMonitor = null;
    private boolean interrupted = false;

    private EnrichmentMapParameters params;

    public BuildGMTEnrichmentMapTask(EnrichmentMapParameters params) {
        //create a new instance of the parameters
        this.params = new EnrichmentMapParameters();

        //copy the input variables into the new instance of the parameters
        this.params.copyInputParameters(params);
    }


    public void buildEnrichmentMap(){
    //Load in the GMT file
           try{
        	   		
        	   		//create a new Enrichment Map
        	   		EnrichmentMap map = new EnrichmentMap(params);

        	   		//data is loaded into a dataset.
        	   		//Since we are building an enrichment map from only the gmt file default to put info into 
        	   		//dataset 1.
        	   		DataSet current_dataset = map.getDataset(EnrichmentMap.DATASET1);
        	   		
               //Load the geneset file as a dataset
        	   		LoadDataSetTask dataset = new LoadDataSetTask(current_dataset,taskMonitor);
        	   		dataset.run();
        	   		//GMTFileReaderTask gmtFile = new GMTFileReaderTask(map, taskMonitor);
               //gmtFile.run();

               //in this case all the genesets are of interest
               params.setMethod(EnrichmentMapParameters.method_generic);
               current_dataset.setGenesetsOfInterest(current_dataset.getSetofgenesets());
              
               //compute the geneset similarities
                ComputeSimilarityTask similarities = new ComputeSimilarityTask(map,taskMonitor);
                similarities.run();

                HashMap<String, GenesetSimilarity> similarity_results = similarities.getGeneset_similarities();

                map.setGenesetSimilarity(similarity_results);
                
                HashMap<String, GeneSet> current_sets = current_dataset.getSetofgenesets().getGenesets();
                
                //create an new Set of Enrichment Results
                SetOfEnrichmentResults setofenrichments = new SetOfEnrichmentResults();
                
                HashMap<String,EnrichmentResult> currentEnrichments = setofenrichments.getEnrichments();
                
                //need also to put all genesets into enrichment results
                for(Iterator i = current_sets.keySet().iterator(); i.hasNext();){
                    String geneset1_name = i.next().toString();

                    GeneSet current = (GeneSet)current_sets.get(geneset1_name);

                    GenericResult temp_result = new GenericResult(current.getName(),current.getDescription(),0.01,current.getGenes().size());

                    currentEnrichments.put(current.getName(), temp_result);

                }
               current_dataset.setEnrichments(setofenrichments);               

               //in order to see the gene in the expression viewer we also need a dummy expression file
               //get all the genes
               HashMap<String, Integer> genes = map.getGenes();

               HashSet<Integer> datasetGenes= current_dataset.getDatasetGenes();

               String[] tokens = new String[3];
               tokens[0] = "Name";
               tokens[1] = "Description";
               tokens[2] = "Fake Expression";

               GeneExpressionMatrix expressionMatrix = new GeneExpressionMatrix(tokens);
               HashMap<Integer,GeneExpression> expression = new HashMap<Integer, GeneExpression>();
               expressionMatrix.setExpressionMatrix(expression);
               tokens[2] = "1.0";

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
               params.setData(true);
               current_dataset.setExpressionSets(expressionMatrix);


                //build the resulting map
                VisualizeEnrichmentMapTask viz_map = new VisualizeEnrichmentMapTask(map,taskMonitor);
                viz_map.run();

       } catch (OutOfMemoryError e) {
           taskMonitor.setException(e,"Out of Memory. Please increase memory allotement for cytoscape.");

       }catch(Exception e){
           taskMonitor.setException(e,"unable to build/visualize map");
       }
    }

    /**
     * Run the Task.
     */
    public void run() {
        this.buildEnrichmentMap();
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
        return new String("Building Enrichment Map based on GMT File");
    }

}
