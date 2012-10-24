package org.baderlab.csplugins.enrichmentmap;

import cytoscape.task.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

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

               //Load the geneset file
               GMTFileReaderTask gmtFile = new GMTFileReaderTask(params, taskMonitor);
               gmtFile.run();

               //in this case all the genesets are of interest
               params.setGenesetsOfInterest(params.getGenesets());
               params.setMethod(EnrichmentMapParameters.method_generic);


               //compute the geneset similarities
                ComputeSimilarityTask similarities = new ComputeSimilarityTask(params,taskMonitor);
                similarities.run();

                HashMap<String, GenesetSimilarity> similarity_results = similarities.getGeneset_similarities();

                params.setGenesetSimilarity(similarity_results);
                HashMap<String, GeneSet> current_sets = params.getGenesets();
                HashMap<String,EnrichmentResult> currentEnrichments = params.getEnrichmentResults1();
                //need also to put all genesets into enrichment results
                for(Iterator i = current_sets.keySet().iterator(); i.hasNext();){
                    String geneset1_name = i.next().toString();

                    GeneSet current = (GeneSet)current_sets.get(geneset1_name);

                    GenericResult temp_result = new GenericResult(current.getName(),current.getDescription(),0.01,current.getGenes().size());

                    currentEnrichments.put(current.getName(), temp_result);

                }
               params.setEnrichmentResults1(currentEnrichments);
               params.setEnrichmentResults1OfInterest(currentEnrichments);

               //in order to see the gene in the expression viewer we also need a dummy expression file
               //get all the genes
               HashMap<String, Integer> genes = params.getGenes();

               HashSet datasetGenes= params.getDatasetGenes();

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
               params.setExpression(expressionMatrix);


                //build the resulting map
                VisualizeEnrichmentMapTask map = new VisualizeEnrichmentMapTask(params,taskMonitor);
                map.run();

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
