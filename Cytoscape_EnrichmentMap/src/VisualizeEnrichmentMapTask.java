import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.layout.CyLayouts;
import cytoscape.visual.*;
import cytoscape.visual.calculators.BasicCalculator;
import cytoscape.visual.calculators.Calculator;
import cytoscape.visual.mappings.*;
import cytoscape.data.CyAttributes;
import cytoscape.data.Semantics;
import cytoscape.view.CyNetworkView;

import java.util.*;
import java.awt.*;

import giny.model.Node;
import giny.model.Edge;


/**
 * Created by
 * User: risserlin
 * Date: Jan 8, 2009
 * Time: 4:11:11 PM
 */
public class VisualizeEnrichmentMapTask implements Task {

    private EnrichmentMapParameters params;

    private HashMap geneset_similarities;

    private String clustername;

    // Keep track of progress for monitoring:
    private TaskMonitor taskMonitor = null;
    private boolean interrupted = false;

    public VisualizeEnrichmentMapTask(EnrichmentMapParameters params,HashMap similarities_results, TaskMonitor taskMonitor) {
          this(params, similarities_results);
          this.taskMonitor = taskMonitor;
      }


    public VisualizeEnrichmentMapTask(EnrichmentMapParameters params, HashMap similarities_results) {
        this.params = params;
        this.geneset_similarities = similarities_results;
        clustername = "Enrichment Map";

    }

    public boolean computeMap(){
        if(taskMonitor == null){
            throw new IllegalStateException("Task Monitor is not set");
        }
        try{

            //on multiple runs of the program some of the nodes or all of them might already
            //be created but it is possible that they have different values for the attributes.  How do
            //we resolve this?
            Set<CyNetwork> networks = Cytoscape.getNetworkSet();
            CyNetwork network;
            String prefix;

            //There are no networks then create a new one.
            if(networks == null){
                //create the network

                prefix = "EM1_";
                network = Cytoscape.createNetwork(prefix + clustername);
            }
            else{
                //how many enrichment maps are there?
                int num_networks = 1;
                for(Iterator i = networks.iterator(); i.hasNext();){
                    CyNetwork current_network = (CyNetwork)i.next();
                    if( current_network.getTitle().startsWith("EM") )
                        num_networks++;
                }
                prefix = "EM" + num_networks + "_";
                network = Cytoscape.createNetwork(prefix + clustername);
            }


            HashMap GSEAResults1OfInterest = params.getGseaResults1OfInterest();
            HashMap GSEAResults2OfInterest = params.getGseaResults2OfInterest();
            HashMap GSEAResults1 = params.getGseaResults1();
            HashMap GSEAResults2 = params.getGseaResults2();

            int currentProgress = 0;
            int maxValue = GSEAResults1OfInterest.size();

            //create the nodes
            //Each geneset of interest is a node
            //its size is dependant on the size of the geneset

            //on multiple runs of the program some of the nodes or all of them might already
            //be created but it is possible that they have different values for the attributes.  How do
            //we resolve this?

            //iterate through the each of the GSEA Results of interest
            for(Iterator i = GSEAResults1OfInterest.keySet().iterator(); i.hasNext(); ){
                String current_name =i.next().toString();
                GSEAResult current_result = (GSEAResult) GSEAResults1OfInterest.get(current_name);

                Node node = Cytoscape.getCyNode(current_name,true);

                network.addNode(node);

                CyAttributes nodeAttrs = Cytoscape.getNodeAttributes();
                nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.NAME, current_name);
                nodeAttrs.setAttribute(node.getIdentifier(), prefix + EnrichmentMapVisualStyle.PVALUE_DATASET1, current_result.getPvalue());
                nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.FDR_QVALUE_DATASET1, current_result.getFdrqvalue());
                nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.FWER_QVALUE_DATASET1, current_result.getFwerqvalue());
                nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.GS_SIZE_DATASET1, current_result.getSize());
                nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.ES_DATASET1, current_result.getES());
                nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.NES_DATASET1, current_result.getNES());

                //colouring is based on the pvalue but in order to maintain the up down, the p-values are either
                // positive or negative depending on the ES score
                if(current_result.getNES()>=0){
                     nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.COLOURING_DATASET1,  (1-current_result.getPvalue()));
                }
                else{
                      nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.COLOURING_DATASET1,  ((-1) * (1-current_result.getPvalue())));
                }

                //if we are using two datasets check to see if there is data for this node
                if(params.isTwoDatasets()){
                       if(GSEAResults2.containsKey(current_name)){
                           GSEAResult second_result = (GSEAResult) GSEAResults2.get(current_name);
                           nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.PVALUE_DATASET2, second_result.getPvalue());
                           nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.FDR_QVALUE_DATASET2, second_result.getFdrqvalue());
                           nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.FWER_QVALUE_DATASET2, second_result.getFwerqvalue());
                           nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.GS_SIZE_DATASET2, second_result.getSize());
                           nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.ES_DATASET2, second_result.getES());
                           nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.NES_DATASET2, second_result.getNES());
                            //colouring is based on the pvalue but in order to maintain the up down, the p-values are either
                           // positive or negative depending on the ES score
                           if(second_result.getNES()>=0){
                                nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.COLOURING_DATASET2,  (1-second_result.getPvalue()));
                           }
                           else{
                                 nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.COLOURING_DATASET2,  ((-1) * (1-second_result.getPvalue())));
                           }

                       }
                      else{
                           nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.PVALUE_DATASET2, 1.0);
                           nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.FDR_QVALUE_DATASET2, 1.0);
                           nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.FWER_QVALUE_DATASET2, 1.0);
                           nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.GS_SIZE_DATASET2, 0);
                           nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.ES_DATASET2, 0.0);
                           nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.NES_DATASET2, 0.0);
                           nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.COLOURING_DATASET2, 0.0);
                       }
                }

                 // Calculate Percentage.  This must be a value between 0..100.
                int percentComplete = (int) (((double) currentProgress / maxValue) * 100);
                //  Estimate Time Remaining
                long timeRemaining = maxValue - currentProgress;
                if (taskMonitor != null) {
                   taskMonitor.setPercentCompleted(percentComplete);
                   taskMonitor.setStatus("Building Enrichment Map " + currentProgress + " of " + maxValue);
                   taskMonitor.setEstimatedTimeRemaining(timeRemaining);
                }
                currentProgress++;



            }

            //Add any additional nodes from the second dataset that haven't been added yet
            if(params.isTwoDatasets()){
                for(Iterator i = GSEAResults2OfInterest.keySet().iterator(); i.hasNext(); ){
                    String current_name =i.next().toString();

                    //is this already a node from the first subset
                    if(GSEAResults1OfInterest.containsKey(current_name)){
                        //Don't need to add it
                    }
                    else{
                        Node node = Cytoscape.getCyNode(current_name, true);

                        network.addNode(node);

                        CyAttributes nodeAttrs = Cytoscape.getNodeAttributes();

                        if(GSEAResults1.containsKey(current_name)){
                            GSEAResult result = (GSEAResult) GSEAResults1.get(current_name);
                            nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.NAME, result.getName());
                            nodeAttrs.setAttribute(node.getIdentifier(), prefix+EnrichmentMapVisualStyle.PVALUE_DATASET1, result.getPvalue());
                            nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.FDR_QVALUE_DATASET1, result.getFdrqvalue());
                            nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.FWER_QVALUE_DATASET1, result.getFwerqvalue());
                            nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.GS_SIZE_DATASET1, result.getSize());
                            nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.ES_DATASET1, result.getES());
                            nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.NES_DATASET1, result.getNES());
                            if(result.getNES()>=0){
                                nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.COLOURING_DATASET1,  (1-result.getPvalue()));
                           }
                           else{
                                 nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.COLOURING_DATASET1,  ((-1) * (1-result.getPvalue())));
                           }
                        }
                         else{
                            nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.NAME, current_name);
                            nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.PVALUE_DATASET1, 1.0);
                            nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.FDR_QVALUE_DATASET1, 1.0);
                            nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.FWER_QVALUE_DATASET1, 1.0);
                            nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.GS_SIZE_DATASET1, 0);
                            nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.ES_DATASET1, 0.0);
                            nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.NES_DATASET1, 0.0);
                            nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.COLOURING_DATASET1, 0.0);

                         }

                         GSEAResult second_result = (GSEAResult) GSEAResults2OfInterest.get(current_name);
                         nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.PVALUE_DATASET2, second_result.getPvalue());
                         nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.FDR_QVALUE_DATASET2, second_result.getFdrqvalue());
                         nodeAttrs.setAttribute(node.getIdentifier(), prefix+EnrichmentMapVisualStyle.FWER_QVALUE_DATASET2, second_result.getFwerqvalue());
                         nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.GS_SIZE_DATASET2, second_result.getSize());
                         nodeAttrs.setAttribute(node.getIdentifier(), prefix+EnrichmentMapVisualStyle.ES_DATASET2, second_result.getES());
                         nodeAttrs.setAttribute(node.getIdentifier(), prefix+EnrichmentMapVisualStyle.NES_DATASET2, second_result.getNES());
                         if(second_result.getNES()>=0){
                                nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.COLOURING_DATASET2,  (1-second_result.getPvalue()));
                           }
                         else{
                                 nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.COLOURING_DATASET2,  ((-1) * (1-second_result.getPvalue())));
                           }

                    }
                }
            }

            //iterate through the similiarities to create the edges
            for(Iterator j = geneset_similarities.keySet().iterator(); j.hasNext(); ){
              String current_name =j.next().toString();
              GenesetSimilarity current_result = (GenesetSimilarity) geneset_similarities.get(current_name);

              //only create edges where the jaccard coeffecient to great than 0.3
                if(current_result.getJaccard_coeffecient()>params.getJaccardCutOff()){
                    Node node1 = Cytoscape.getCyNode(current_result.getGeneset1_Name(),false);
                    Node node2 = Cytoscape.getCyNode(current_result.getGeneset2_Name(),false);
                    Edge edge = (Edge) Cytoscape.getCyEdge(node1, node2, Semantics.INTERACTION, "pp", true);

                    network.addEdge(edge);

                    CyAttributes edgeAttrs = Cytoscape.getEdgeAttributes();
                    edgeAttrs.setAttribute(edge.getIdentifier(), prefix+EnrichmentMapVisualStyle.JACCARD_COEFFECIENT, current_result.getJaccard_coeffecient());
                    edgeAttrs.setAttribute(edge.getIdentifier(), prefix+ EnrichmentMapVisualStyle.OVERLAP_SIZE, current_result.getSizeOfOverlap());
                }
            }

            CyNetworkView view = Cytoscape.createNetworkView( network );

            // get the VisualMappingManager and CalculatorCatalog
            VisualMappingManager manager = Cytoscape.getVisualMappingManager();
            CalculatorCatalog catalog = manager.getCalculatorCatalog();


             String vs_name = prefix + "Enrichment_map_style";
             // check to see if a visual style with this name already exists
                 VisualStyle vs = catalog.getVisualStyle(vs_name);

                 if (vs == null) {
                     // if not, create it and add it to the catalog
                    // Create the visual style
                     EnrichmentMapVisualStyle em_vs = new EnrichmentMapVisualStyle(vs_name,params);

                     vs = em_vs.createVisualStyle(network, prefix);
                     //vs = createVisualStyle(network,prefix);

                     catalog.addVisualStyle(vs);
                }

               view.setVisualStyle(vs.getName()); // not strictly necessary

               // actually apply the visual style
               manager.setVisualStyle(vs);
               view.redrawGraph(true,true);

                  //view.applyLayout(CyLayouts.getDefaultLayout());
               view.applyLayout(CyLayouts.getLayout("force-directed"));


        } catch(IllegalThreadStateException e){
            taskMonitor.setException(e, "Unable to compute jaccard coeffecients");
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
          return new String("Building Enrichment Map");
      }

}
