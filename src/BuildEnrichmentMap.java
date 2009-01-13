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
import cytoscape.data.readers.VisualStyleBuilder;
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
public class BuildEnrichmentMap implements Task {

    private EnrichmentMapParameters params;

    private HashMap geneset_similarities;

    private String clustername;

    // Keep track of progress for monitoring:
    private TaskMonitor taskMonitor = null;
    private boolean interrupted = false;

    public BuildEnrichmentMap(EnrichmentMapParameters params,HashMap similarities_results, TaskMonitor taskMonitor) {
          this(params, similarities_results);
          this.taskMonitor = taskMonitor;
      }


    public BuildEnrichmentMap(EnrichmentMapParameters params, HashMap similarities_results) {
        this.params = params;
        this.geneset_similarities = similarities_results;
        clustername = "Enrichment Map";

    }

    public boolean computeMap(){
        if(taskMonitor == null){
            throw new IllegalStateException("Task Monitor is not set");
        }
        try{

            //create the network
            CyNetwork network = Cytoscape.createNetwork(clustername);


            HashMap GSEAResultsOfInterest = params.getGseaResultsOfInterest();

            int currentProgress = 0;
            int maxValue = GSEAResultsOfInterest.size();

            //create the nodes
            //Each geneset of interest is a node
            //its size is dependant on the size of the geneset

            //iterate through the each of the GSEA Results of interest
            for(Iterator i = GSEAResultsOfInterest.keySet().iterator(); i.hasNext(); ){
                String current_name =i.next().toString();
                GSEAResult current_result = (GSEAResult) GSEAResultsOfInterest.get(current_name);

                Node node = Cytoscape.getCyNode(current_name,true);

                network.addNode(node);

                CyAttributes nodeAttrs = Cytoscape.getNodeAttributes();
                nodeAttrs.setAttribute(node.getIdentifier(), "Name", current_name);
                nodeAttrs.setAttribute(node.getIdentifier(), "pValue", current_result.getPvalue());
                nodeAttrs.setAttribute(node.getIdentifier(), "fdrQValue", current_result.getFdrqvalue());
                nodeAttrs.setAttribute(node.getIdentifier(), "fwerQvalue", current_result.getFwerqvalue());
                nodeAttrs.setAttribute(node.getIdentifier(), "gs_size", current_result.getSize());
                nodeAttrs.setAttribute(node.getIdentifier(), "ES", current_result.getES());
                nodeAttrs.setAttribute(node.getIdentifier(), "NES", current_result.getNES());



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
                    edgeAttrs.setAttribute(edge.getIdentifier(), "jaccard_coeffecient", current_result.getJaccard_coeffecient());
                    edgeAttrs.setAttribute(edge.getIdentifier(), "overlap_size", current_result.getSizeOfOverlap());
                }
            }

            CyNetworkView view = Cytoscape.createNetworkView( network );

            // get the VisualMappingManager and CalculatorCatalog
            VisualMappingManager manager = Cytoscape.getVisualMappingManager();
            CalculatorCatalog catalog = manager.getCalculatorCatalog();



             // check to see if a visual style with this name already exists
                 VisualStyle vs = catalog.getVisualStyle("Enrichment map visual style");
                 if (vs == null) {
                     // if not, create it and add it to the catalog
                    // Create the visual style
                     vs = createVisualStyle(network);

                     //vs = createVisualStyle(network);
                     catalog.addVisualStyle(vs);
                }

               view.setVisualStyle(vs.getName()); // not strictly necessary

               // actually apply the visual style
               manager.setVisualStyle(vs);
               view.redrawGraph(true,true);

               view.applyLayout(CyLayouts.getDefaultLayout());


        } catch(IllegalThreadStateException e){
            taskMonitor.setException(e, "Unable to compute jaccard coeffecients");
            return false;
        }

       return true;
    }

    public VisualStyle createVisualStyle(CyNetwork network){

        NodeAppearanceCalculator nodeAppCalc = new NodeAppearanceCalculator();
        EdgeAppearanceCalculator edgeAppCalc = new EdgeAppearanceCalculator();
        GlobalAppearanceCalculator globalAppCalc = new GlobalAppearanceCalculator();

        //set the default node appearance
        NodeAppearance nodeAppear = new NodeAppearance();
        nodeAppear.set(VisualPropertyType.NODE_FILL_COLOR, Color.orange);
        nodeAppear.set(VisualPropertyType.NODE_SHAPE, NodeShape.ELLIPSE);
        nodeAppear.set(VisualPropertyType.NODE_SIZE, new Double(35.0));
        nodeAppCalc.setDefaultAppearance(nodeAppear);

        globalAppCalc.setDefaultBackgroundColor(Color.white);

        //set the default edge appearance
        EdgeAppearance edgeAppear = new EdgeAppearance();
        edgeAppear.set(VisualPropertyType.EDGE_COLOR, Color.BLUE );

        // Passthrough Mapping - set node label
        PassThroughMapping pm = new PassThroughMapping(new String(), "Name");
        Calculator nlc = new BasicCalculator("Example Node Label Calculator",
                                                   pm, VisualPropertyType.NODE_LABEL);
        nodeAppCalc.setCalculator(nlc);

        //Continuous Mapping - set node size based on the size of the geneset
        ContinuousMapping continuousMapping_size = new ContinuousMapping(35, ObjectMapping.NODE_MAPPING);
        continuousMapping_size.setControllingAttributeName("gs_size", network, false);
        Interpolator numTonum = new LinearNumberToNumberInterpolator();
        continuousMapping_size.setInterpolator(numTonum);

        Integer under = 15;
        Integer min = 20;
        Integer max = 65;
        Integer over = 80;

           // Create boundary conditions                  less than,   equals,  greater than
         BoundaryRangeValues bv0 = new BoundaryRangeValues(under, min, min);
         BoundaryRangeValues bv1 = new BoundaryRangeValues(max, max, over);
        continuousMapping_size.addPoint(25.0, bv0);
        continuousMapping_size.addPoint(500.0, bv1);
        Calculator nodeSizeCalculator = new BasicCalculator("size2size", continuousMapping_size, VisualPropertyType.NODE_SIZE);
         nodeAppCalc.setCalculator(nodeSizeCalculator);


        //Continuous Mapping - set node colour based on the sign of the ES score
        ContinuousMapping continuousMapping = new ContinuousMapping(Color.WHITE, ObjectMapping.NODE_MAPPING);
        continuousMapping.setControllingAttributeName("ES", network, false);
        Interpolator numToColor = new LinearNumberToColorInterpolator();
        continuousMapping.setInterpolator(numToColor);

        Color underColor = Color.GRAY;
        Color minColor = Color.RED;
        Color maxColor = Color.GREEN;

          // Create boundary conditions                  less than,   equals,  greater than
         BoundaryRangeValues bv3 = new BoundaryRangeValues(minColor, underColor, maxColor);
         // Set the attribute point values associated with the boundary values
         continuousMapping.addPoint(0.0, bv3);
        Calculator nodeColorCalculator = new BasicCalculator("ES2Colour", continuousMapping, VisualPropertyType.NODE_FILL_COLOR);
         nodeAppCalc.setCalculator(nodeColorCalculator);

        //Continous Mapping - set edge line thickness based on the number of genes in the overlap
        ContinuousMapping continuousMapping_edgewidth = new ContinuousMapping(1, ObjectMapping.EDGE_MAPPING);
        continuousMapping_edgewidth.setControllingAttributeName("overlap_size", network, false);
        Interpolator numTonum2 = new LinearNumberToNumberInterpolator();
        continuousMapping_edgewidth.setInterpolator(numTonum2);

        Double under_width = 0.5;
        Double min_width = 1.0;
        Double max_width = 5.0;
        Double over_width = 6.0;

                   // Create boundary conditions                  less than,   equals,  greater than
        BoundaryRangeValues bv4 = new BoundaryRangeValues(under_width, min_width, min_width);
        BoundaryRangeValues bv5 = new BoundaryRangeValues(max_width, max_width, over_width);
        continuousMapping_edgewidth.addPoint(25.0, bv4);
        continuousMapping_edgewidth.addPoint(500.0, bv5);
        Calculator edgeWidthCalculator = new BasicCalculator("edgesize", continuousMapping_edgewidth, VisualPropertyType.EDGE_LINE_WIDTH);
        edgeAppCalc.setCalculator(edgeWidthCalculator);



        VisualStyle vs = new VisualStyle(Cytoscape.getVisualMappingManager().getVisualStyle(),"Enrichment map visual style");
        vs.setEdgeAppearanceCalculator(edgeAppCalc);
        vs.setNodeAppearanceCalculator(nodeAppCalc);
        vs.setGlobalAppearanceCalculator(globalAppCalc);

        return vs;

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
