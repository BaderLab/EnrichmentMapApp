import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.layout.CyLayouts;
import cytoscape.visual.*;
import cytoscape.data.CyAttributes;
import cytoscape.data.Semantics;
import cytoscape.view.CyNetworkView;
import cytoscape.view.CytoscapeDesktop;
import cytoscape.view.cytopanels.CytoPanelState;
import cytoscape.view.cytopanels.CytoPanel;

import java.util.*;

import giny.model.Node;
import giny.model.Edge;

import javax.swing.*;


/**
 * Created by
 * User: risserlin
 * Date: Jan 8, 2009
 * Time: 4:11:11 PM
 */
public class VisualizeEnrichmentMapTask implements Task {

    private EnrichmentMapParameters params;

    private HashMap<String, GenesetSimilarity> geneset_similarities;

    private String clustername;

    // Keep track of progress for monitoring:
    private TaskMonitor taskMonitor = null;
    private boolean interrupted = false;

    public VisualizeEnrichmentMapTask(EnrichmentMapParameters params, TaskMonitor taskMonitor) {
          this(params);
          this.taskMonitor = taskMonitor;
      }


    public VisualizeEnrichmentMapTask(EnrichmentMapParameters params) {
        this.params = params;
        this.geneset_similarities = params.getGenesetSimilarity();
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
                params.setAttributePrefix(prefix);
                params.setNetworkName(prefix+clustername);
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
                params.setAttributePrefix(prefix);
                params.setNetworkName(prefix+clustername);
                network = Cytoscape.createNetwork(prefix + clustername);
            }


            HashMap enrichmentResults1OfInterest = params.getEnrichmentResults1OfInterest();
            HashMap enrichmentResults2OfInterest = params.getEnrichmentResults2OfInterest();
            HashMap enrichmentResults1 = params.getEnrichmentResults1();
            HashMap enrichmentResults2 = params.getEnrichmentResults2();

            HashMap genesetsOfInterest = params.getGenesetsOfInterest();

            int currentProgress = 0;
            int maxValue = enrichmentResults1OfInterest.size();

            //create the nodes
            //Each geneset of interest is a node
            //its size is dependant on the size of the geneset

            //on multiple runs of the program some of the nodes or all of them might already
            //be created but it is possible that they have different values for the attributes.  How do
            //we resolve this?

            //iterate through the each of the GSEA Results of interest
            for(Iterator i = enrichmentResults1OfInterest.keySet().iterator(); i.hasNext(); ){
                String current_name =i.next().toString();


                Node node = Cytoscape.getCyNode(current_name,true);

                network.addNode(node);

                //Add the description to the node
                GeneSet gs = (GeneSet)genesetsOfInterest.get(current_name);
                CyAttributes nodeAttrs = Cytoscape.getNodeAttributes();
                nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.GS_DESCR, gs.getDescription());


                if(params.isGSEA()){
                    GSEAResult current_result = (GSEAResult) enrichmentResults1OfInterest.get(current_name);
                    setGSEAResultDataset1Attributes(node, current_result,prefix);
                }
                else{
                   GenericResult current_result = (GenericResult) enrichmentResults1OfInterest.get(current_name);
                   setGenericResultDataset1Attributes(node, current_result, prefix);
                }

                //if we are using two datasets check to see if there is data for this node
                if(params.isTwoDatasets()){
                    if(params.isGSEA()){
                       if(enrichmentResults2.containsKey(current_name)){
                           GSEAResult second_result = (GSEAResult) enrichmentResults2.get(current_name);
                           setGSEAResultDataset2Attributes(node, second_result,prefix);

                       }
                      else{
                          setdefaultGSEAResultDataset2Attributes(node,prefix);
                       }
                    }
                    else{
                       if(enrichmentResults2.containsKey(current_name)){
                           GenericResult second_result = (GenericResult) enrichmentResults2.get(current_name);
                           setGenericResultDataset2Attributes(node, second_result,prefix);

                       }
                      else{
                          setdefaultGenericResultDataset2Attributes(node,prefix);
                       }
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
                for(Iterator i = enrichmentResults2OfInterest.keySet().iterator(); i.hasNext(); ){
                    String current_name =i.next().toString();

                    //is this already a node from the first subset
                    if(enrichmentResults1OfInterest.containsKey(current_name)){
                        //Don't need to add it
                    }
                    else{
                        Node node = Cytoscape.getCyNode(current_name, true);

                        network.addNode(node);

                        //Add the description to the node
                        GeneSet gs = (GeneSet)genesetsOfInterest.get(current_name);
                        CyAttributes nodeAttrs = Cytoscape.getNodeAttributes();
                        nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.GS_DESCR, gs.getDescription());

                        if(params.isGSEA()){
                            if(enrichmentResults1.containsKey(current_name)){
                                GSEAResult result = (GSEAResult) enrichmentResults1.get(current_name);
                                setGSEAResultDataset1Attributes(node,result, prefix);
                            }
                             else{
                                setdefaultGSEAResultDataset1Attributes(node, prefix);
                            }

                            GSEAResult second_result = (GSEAResult) enrichmentResults2OfInterest.get(current_name);
                            setGSEAResultDataset2Attributes(node, second_result,prefix);
                        }
                        else{
                            if(enrichmentResults1.containsKey(current_name)){
                                GenericResult result = (GenericResult) enrichmentResults1.get(current_name);
                                setGenericResultDataset1Attributes(node,result, prefix);
                            }
                             else{
                                setdefaultGenericResultDataset1Attributes(node, prefix);
                            }

                            GenericResult second_result = (GenericResult) enrichmentResults2OfInterest.get(current_name);
                            setGenericResultDataset2Attributes(node, second_result,prefix);
                        }
                    }
                }
            }
            int k = 0;
            //iterate through the similiarities to create the edges
            for(Iterator j = geneset_similarities.keySet().iterator(); j.hasNext(); ){
              String current_name =j.next().toString();
              GenesetSimilarity current_result = (GenesetSimilarity) geneset_similarities.get(current_name);

              //only create edges where the jaccard coeffecient to great than
                if(current_result.getJaccard_coeffecient()>params.getJaccardCutOff()){
                    Node node1 = Cytoscape.getCyNode(current_result.getGeneset1_Name(),false);
                    Node node2 = Cytoscape.getCyNode(current_result.getGeneset2_Name(),false);
                    Edge edge = (Edge) Cytoscape.getCyEdge(node1, node2, Semantics.INTERACTION, "pp", true);

                    network.addEdge(edge);

                    //Cytoscape.getNetworkView(network.getIdentifier()).addEdgeContextMenuListener(getEMEdgeContextMenuListener(current_result));

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


            //register the new Network
            EnrichmentMapManager EMmanager = EnrichmentMapManager.getInstance();
            EMmanager.registerNetwork(network,params);

            //initialize parameter panel with info for this network
            ParametersPanel parametersPanel = EMmanager.getParameterPanel();
            parametersPanel.updatePanel(params);
            final CytoscapeDesktop desktop = Cytoscape.getDesktop();
            final CytoPanel cytoSidePanel = desktop.getCytoPanel(SwingConstants.EAST);
            cytoSidePanel.setSelectedIndex(cytoSidePanel.indexOfComponent(parametersPanel));

            //Add the parameters summary panel

            //ParametersPanel parametersPanel = new ParametersPanel(params);
            //final CytoscapeDesktop desktop = Cytoscape.getDesktop();
            //final CytoPanel cytoSidePanel = desktop.getCytoPanel(SwingConstants.EAST);
            //cytoSidePanel.add("Parameters Used", parametersPanel);
            //cytoSidePanel.setSelectedIndex(cytoSidePanel.indexOfComponent(parametersPanel));
            //cytoSidePanel.setState(CytoPanelState.DOCK);

            //add the click on edge listener
            view.addGraphViewChangeListener(new EnrichmentMapActionListener(params));




        } catch(IllegalThreadStateException e){
            taskMonitor.setException(e, "Unable to compute jaccard coeffecients");
            return false;
        }

       return true;
    }

    private void setGenericResultDataset1Attributes(Node node, GenericResult result, String prefix){

        CyAttributes nodeAttrs = Cytoscape.getNodeAttributes();
        //format the node name
       String formattedName = formatLabel(result.getName());

        nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.FORMATTED_NAME, formattedName);
        nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.NAME, result.getName());
        nodeAttrs.setAttribute(node.getIdentifier(), prefix + EnrichmentMapVisualStyle.PVALUE_DATASET1, result.getPvalue());
        nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.FDR_QVALUE_DATASET1, result.getFdrqvalue());
        nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.GS_SIZE_DATASET1, result.getGsSize());
        if(result.getNES()>=0){
            nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.COLOURING_DATASET1,  (1-result.getPvalue()));
       }
      else{
            nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.COLOURING_DATASET1,  ((-1) * (1-result.getPvalue())));
      }
    }

    private void setGenericResultDataset2Attributes(Node node, GenericResult result, String prefix){

        CyAttributes nodeAttrs = Cytoscape.getNodeAttributes();
        //format the node name
       String formattedName = formatLabel(result.getName());

        nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.FORMATTED_NAME, formattedName);
        nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.NAME, result.getName());
        nodeAttrs.setAttribute(node.getIdentifier(), prefix + EnrichmentMapVisualStyle.PVALUE_DATASET2, result.getPvalue());
        nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.FDR_QVALUE_DATASET2, result.getFdrqvalue());
        nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.GS_SIZE_DATASET2, result.getGsSize());
        if(result.getNES()>=0){
            nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.COLOURING_DATASET1,  (1-result.getPvalue()));
       }
       else{
            nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.COLOURING_DATASET1,  ((-1) * (1-result.getPvalue())));
      }
    }

   private void setGSEAResultDataset1Attributes(Node node, GSEAResult result, String prefix){

       CyAttributes nodeAttrs = Cytoscape.getNodeAttributes();
        //format the node name
       String formattedName = formatLabel(result.getName());

       nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.FORMATTED_NAME, formattedName);
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

  private void setGSEAResultDataset2Attributes(Node node, GSEAResult result, String prefix){

       CyAttributes nodeAttrs = Cytoscape.getNodeAttributes();

      //format the node name
      String formattedName = formatLabel(result.getName());

      nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.FORMATTED_NAME, formattedName);
       nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.NAME, result.getName());
       nodeAttrs.setAttribute(node.getIdentifier(), prefix+EnrichmentMapVisualStyle.PVALUE_DATASET2, result.getPvalue());
       nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.FDR_QVALUE_DATASET2, result.getFdrqvalue());
       nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.FWER_QVALUE_DATASET2, result.getFwerqvalue());
       nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.GS_SIZE_DATASET2, result.getSize());
       nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.ES_DATASET2, result.getES());
       nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.NES_DATASET2, result.getNES());
       if(result.getNES()>=0){
            nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.COLOURING_DATASET2,  (1-result.getPvalue()));
       }
      else{
            nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.COLOURING_DATASET2,  ((-1) * (1-result.getPvalue())));
      }

   }

     private void setdefaultGSEAResultDataset1Attributes(Node node, String prefix){

       CyAttributes nodeAttrs = Cytoscape.getNodeAttributes();
       nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.PVALUE_DATASET1, 1.0);
       nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.FDR_QVALUE_DATASET1, 1.0);
       nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.FWER_QVALUE_DATASET1, 1.0);
       nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.GS_SIZE_DATASET1, 0);
       nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.ES_DATASET1, 0.0);
       nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.NES_DATASET1, 0.0);
       nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.COLOURING_DATASET1, 0.0);


   }

     private void setdefaultGSEAResultDataset2Attributes(Node node, String prefix){

       CyAttributes nodeAttrs = Cytoscape.getNodeAttributes();
       nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.PVALUE_DATASET2, 1.0);
       nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.FDR_QVALUE_DATASET2, 1.0);
       nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.FWER_QVALUE_DATASET2, 1.0);
       nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.GS_SIZE_DATASET2, 0);
       nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.ES_DATASET2, 0.0);
       nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.NES_DATASET2, 0.0);
       nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.COLOURING_DATASET2, 0.0);


   }

      private void setdefaultGenericResultDataset1Attributes(Node node, String prefix){

       CyAttributes nodeAttrs = Cytoscape.getNodeAttributes();
       nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.PVALUE_DATASET1, 1.0);
       nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.FDR_QVALUE_DATASET1, 1.0);
       nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.GS_SIZE_DATASET1, 0);
       nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.COLOURING_DATASET1, 0.0);


   }


    private void setdefaultGenericResultDataset2Attributes(Node node, String prefix){

     CyAttributes nodeAttrs = Cytoscape.getNodeAttributes();
     nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.PVALUE_DATASET2, 1.0);
     nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.FDR_QVALUE_DATASET2, 1.0);
     nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.GS_SIZE_DATASET2, 0);
     nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.COLOURING_DATASET2, 0.0);


 }

    private String formatLabel(String label){
       String formattedLabel = "";

       int i = 0;
       int k = 1;

        //only wrap at spaces
        String[] tokens = label.split(" ");
        //first try and wrap label based on spacing
        if(tokens.length > 1){
            int current_count = 0;
            for(int j = 0; j< tokens.length;j++){
                if(current_count + tokens[j].length() <= EnrichmentMapVisualStyle.maxNodeLabelLength){
                    formattedLabel = formattedLabel + tokens[j] + " ";
                    current_count = current_count + tokens[j].length();
                }
                else if(current_count + tokens[j].length() > EnrichmentMapVisualStyle.maxNodeLabelLength) {
                    formattedLabel = formattedLabel + "\n" + tokens[j] + " ";
                    current_count = tokens[j].length();
                }
            }
        }
        else{
           tokens = label.split("_");

           if(tokens.length > 1){
                int current_count = 0;
                for(int j = 0; j< tokens.length;j++){
                    if(j != 0)
                      formattedLabel = formattedLabel +  "_";
                    if(current_count + tokens[j].length() <= EnrichmentMapVisualStyle.maxNodeLabelLength){
                        formattedLabel = formattedLabel + tokens[j] ;
                        current_count = current_count + tokens[j].length();
                    }
                    else if(current_count + tokens[j].length() > EnrichmentMapVisualStyle.maxNodeLabelLength) {
                        formattedLabel = formattedLabel + "\n" + tokens[j] ;
                        current_count = tokens[j].length();
                    }
                }
            }

            //if there is only one token wrap it anyways.
            else if(tokens.length == 1){
                while(i<=label.length()){

                    if(i+EnrichmentMapVisualStyle.maxNodeLabelLength > label.length())
                        formattedLabel = formattedLabel + label.substring(i, label.length()) + "\n";
                    else
                        formattedLabel = formattedLabel + label.substring(i, k* EnrichmentMapVisualStyle.maxNodeLabelLength) + "\n";
                    i = (k * EnrichmentMapVisualStyle.maxNodeLabelLength) ;
                    k++;
                }
            }
        }

        return formattedLabel;
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
