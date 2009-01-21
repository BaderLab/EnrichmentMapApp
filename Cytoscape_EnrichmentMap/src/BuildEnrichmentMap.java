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


            HashMap GSEAResults1OfInterest = params.getGseaResults1OfInterest();
            HashMap GSEAResults2OfInterest = params.getGseaResults2OfInterest();

            int currentProgress = 0;
            int maxValue = GSEAResults1OfInterest.size();

            //create the nodes
            //Each geneset of interest is a node
            //its size is dependant on the size of the geneset

            //iterate through the each of the GSEA Results of interest
            for(Iterator i = GSEAResults1OfInterest.keySet().iterator(); i.hasNext(); ){
                String current_name =i.next().toString();
                GSEAResult current_result = (GSEAResult) GSEAResults1OfInterest.get(current_name);

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

                //colouring is based on the pvalue but in order to maintain the up down, the p-values are either
                // positive or negative depending on the ES score
                nodeAttrs.setAttribute(node.getIdentifier(), "Colouring", (current_result.getNES() * (1 - current_result.getPvalue()))/Math.abs(current_result.getNES()));

                //if we are using two datasets check to see if there is data for this node
                if(params.isTwoDatasets()){
                       if(GSEAResults2OfInterest.containsKey(current_name)){
                           GSEAResult second_result = (GSEAResult) GSEAResults2OfInterest.get(current_name);
                           nodeAttrs.setAttribute(node.getIdentifier(), "pValue_dataset2", second_result.getPvalue());
                           nodeAttrs.setAttribute(node.getIdentifier(), "fdrQValue_dataset2", second_result.getFdrqvalue());
                           nodeAttrs.setAttribute(node.getIdentifier(), "fwerQvalue_dataset2", second_result.getFwerqvalue());
                           nodeAttrs.setAttribute(node.getIdentifier(), "gs_size_dataset2", second_result.getSize());
                           nodeAttrs.setAttribute(node.getIdentifier(), "ES_dataset2", second_result.getES());
                           nodeAttrs.setAttribute(node.getIdentifier(), "NES_dataset2", second_result.getNES());
                            //colouring is based on the pvalue but in order to maintain the up down, the p-values are either
                           // positive or negative depending on the ES score
                           nodeAttrs.setAttribute(node.getIdentifier(), "Colouring_dataset2",  (second_result.getNES() * (1-second_result.getPvalue()))/Math.abs(second_result.getNES()));

                       }
                      else{
                           nodeAttrs.setAttribute(node.getIdentifier(), "pValue_dataset2", 1.0);
                           nodeAttrs.setAttribute(node.getIdentifier(), "fdrQValue_dataset2", 1.0);
                           nodeAttrs.setAttribute(node.getIdentifier(), "fwerQvalue_dataset2", 1.0);
                           nodeAttrs.setAttribute(node.getIdentifier(), "gs_size_dataset2", 0);
                           nodeAttrs.setAttribute(node.getIdentifier(), "ES_dataset2", 0.0);
                           nodeAttrs.setAttribute(node.getIdentifier(), "NES_dataset2", 0.0);
                           nodeAttrs.setAttribute(node.getIdentifier(), "Colouring_dataset2", 0.0);
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

                    //is this already a node?
                    Node node = Cytoscape.getCyNode(current_name, false);

                    if(node == null){
                        node = Cytoscape.getCyNode(current_name,true);

                        network.addNode(node);

                        CyAttributes nodeAttrs = Cytoscape.getNodeAttributes();
                        nodeAttrs.setAttribute(node.getIdentifier(), "Name", current_name);
                        nodeAttrs.setAttribute(node.getIdentifier(), "pValue", 1.0);
                        nodeAttrs.setAttribute(node.getIdentifier(), "fdrQValue", 1.0);
                        nodeAttrs.setAttribute(node.getIdentifier(), "fwerQvalue", 1.0);
                        nodeAttrs.setAttribute(node.getIdentifier(), "gs_size", 0);
                        nodeAttrs.setAttribute(node.getIdentifier(), "ES", 0.0);
                        nodeAttrs.setAttribute(node.getIdentifier(), "NES", 0.0);
                        nodeAttrs.setAttribute(node.getIdentifier(), "Colouring", 0.0);


                        GSEAResult second_result = (GSEAResult) GSEAResults2OfInterest.get(current_name);
                        nodeAttrs.setAttribute(node.getIdentifier(), "pValue_dataset2", second_result.getPvalue());
                        nodeAttrs.setAttribute(node.getIdentifier(), "fdrQValue_dataset2", second_result.getFdrqvalue());
                        nodeAttrs.setAttribute(node.getIdentifier(), "fwerQvalue_dataset2", second_result.getFwerqvalue());
                        nodeAttrs.setAttribute(node.getIdentifier(), "gs_size_dataset2", second_result.getSize());
                        nodeAttrs.setAttribute(node.getIdentifier(), "ES_dataset2", second_result.getES());
                        nodeAttrs.setAttribute(node.getIdentifier(), "NES_dataset2", second_result.getNES());
                        nodeAttrs.setAttribute(node.getIdentifier(), "Colouring_dataset2", (second_result.getNES() * (1 - second_result.getPvalue()))/Math.abs(second_result.getNES()));

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

                  //view.applyLayout(CyLayouts.getDefaultLayout());
               view.applyLayout(CyLayouts.getLayout("force-directed"));


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



         Color underColor = Color.WHITE;
         Color max_red = new Color(255,000,000);
         Color light_red1 = new Color(255,102,102);
         Color light_red2 = new Color(255,179,179);
         Color max_blue = new Color(0,100,255);
         Color light_blue1 = new Color(102,162,255);
         Color light_blue2 = new Color(179,208,255);
         Color overColor = Color.WHITE;

         // Create boundary conditions                  less than,   equals,  greater than
         BoundaryRangeValues bv3a = new BoundaryRangeValues(max_blue,max_blue,max_blue);
         BoundaryRangeValues bv3b = new BoundaryRangeValues(light_blue2, light_blue2, max_blue);
         BoundaryRangeValues bv3c = new BoundaryRangeValues(light_blue1, light_blue1,light_blue2);
         BoundaryRangeValues bv3d = new BoundaryRangeValues(overColor, overColor, light_blue1);
         BoundaryRangeValues bv3e = new BoundaryRangeValues(overColor, overColor,overColor);
         BoundaryRangeValues bv3f = new BoundaryRangeValues(overColor, overColor, light_red2);
         BoundaryRangeValues bv3g = new BoundaryRangeValues(light_red2, light_red2, light_red1);
         BoundaryRangeValues bv3h = new BoundaryRangeValues(light_red1, light_red1, max_red);
         BoundaryRangeValues bv3i = new BoundaryRangeValues(max_red, max_red, max_red);

        //set the default node appearance
        NodeAppearance nodeAppear = new NodeAppearance();
        nodeAppear.set(VisualPropertyType.NODE_FILL_COLOR, Color.orange);
        nodeAppear.set(VisualPropertyType.NODE_SHAPE, NodeShape.ELLIPSE);
        nodeAppear.set(VisualPropertyType.NODE_SIZE, new Double(35.0));
        nodeAppCalc.setDefaultAppearance(nodeAppear);

        globalAppCalc.setDefaultBackgroundColor(new Color(205,205,235));

        //set the default edge appearance
        EdgeAppearance edgeAppear = new EdgeAppearance();
        edgeAppear.set(VisualPropertyType.EDGE_COLOR, new Color(100,200,000) );

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

        Integer min = 20;
        Integer max = 65;

           // Create boundary conditions                  less than,   equals,  greater than
         BoundaryRangeValues bv0 = new BoundaryRangeValues(min, min, min);
         BoundaryRangeValues bv1 = new BoundaryRangeValues(max, max, max);
        continuousMapping_size.addPoint(10.0, bv0);
        continuousMapping_size.addPoint(474.0, bv1);
        Calculator nodeSizeCalculator = new BasicCalculator("size2size", continuousMapping_size, VisualPropertyType.NODE_SIZE);
         nodeAppCalc.setCalculator(nodeSizeCalculator);

        if(params.isTwoDatasets()){

            //Continuous Mapping - set node size based on the size of the geneset
            ContinuousMapping continuousMapping_size_dataset2 = new ContinuousMapping(35, ObjectMapping.NODE_MAPPING);
            continuousMapping_size_dataset2.setControllingAttributeName("gs_size_dataset2", network, false);
            Interpolator numTonum2 = new LinearNumberToNumberInterpolator();
            continuousMapping_size_dataset2.setInterpolator(numTonum2);

            Integer min_line = 4;
            Integer max_line = 15;
               // Create boundary conditions                  less than,   equals,  greater than
             BoundaryRangeValues bv0a = new BoundaryRangeValues(min_line, min_line, min_line);
             BoundaryRangeValues bv1a = new BoundaryRangeValues(max_line, max_line, max_line);
            continuousMapping_size_dataset2.addPoint(10.0, bv0a);
            continuousMapping_size_dataset2.addPoint(474.0, bv1a);
            Calculator nodelineSizeCalculator = new BasicCalculator("size2size", continuousMapping_size_dataset2, VisualPropertyType.NODE_LINE_WIDTH);
             nodeAppCalc.setCalculator(nodelineSizeCalculator);

            //Continuous Mapping - set node line colour based on the sign of the ES score of second dataset
            //Color scale and mapper used by node colour and node line colour
            ContinuousMapping continuousMapping_width_col = new ContinuousMapping(Color.WHITE, ObjectMapping.NODE_MAPPING);
            continuousMapping_width_col.setControllingAttributeName("Colouring_dataset2", network, false);
            Interpolator numToColor2 = new LinearNumberToColorInterpolator();
            continuousMapping_width_col.setInterpolator(numToColor2);

            // Set the attribute point values associated with the boundary values
            continuousMapping_width_col.addPoint(-1.0, bv3a);
            continuousMapping_width_col.addPoint(-0.995, bv3b);
            continuousMapping_width_col.addPoint(-0.95, bv3c);
            continuousMapping_width_col.addPoint(-0.9, bv3d);
            continuousMapping_width_col.addPoint(0.0, bv3e);
            continuousMapping_width_col.addPoint(0.9, bv3f);
            continuousMapping_width_col.addPoint(0.95, bv3g);
            continuousMapping_width_col.addPoint(0.995, bv3h);
            continuousMapping_width_col.addPoint(1.0, bv3i);

            Calculator nodeColorCalculator_width_col = new BasicCalculator("ES2Colour", continuousMapping_width_col, VisualPropertyType.NODE_BORDER_COLOR);
             nodeAppCalc.setCalculator(nodeColorCalculator_width_col);


        }

        //Continuous Mapping - set node colour based on the sign of the ES score of first dataset
        ContinuousMapping continuousMapping = new ContinuousMapping(Color.WHITE, ObjectMapping.NODE_MAPPING);
        continuousMapping.setControllingAttributeName("Colouring", network, false);
        Interpolator numToColor = new LinearNumberToColorInterpolator();
        continuousMapping.setInterpolator(numToColor);

         // Set the attribute point values associated with the boundary values
         // Set the attribute point values associated with the boundary values
        continuousMapping.addPoint(-1.0, bv3a);
        continuousMapping.addPoint(-0.995, bv3b);
        continuousMapping.addPoint(-0.95, bv3c);
        continuousMapping.addPoint(-0.9, bv3d);
        continuousMapping.addPoint(0.0, bv3e);
        continuousMapping.addPoint(0.9, bv3f);
        continuousMapping.addPoint(0.95, bv3g);
        continuousMapping.addPoint(0.995, bv3h);
        continuousMapping.addPoint(1.0, bv3i);
        
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
