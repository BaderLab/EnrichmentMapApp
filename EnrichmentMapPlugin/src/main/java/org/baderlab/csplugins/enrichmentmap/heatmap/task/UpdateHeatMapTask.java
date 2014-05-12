package org.baderlab.csplugins.enrichmentmap.heatmap.task;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.baderlab.csplugins.enrichmentmap.heatmap.HeatMapParameters;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.baderlab.csplugins.enrichmentmap.model.GenesetSimilarity;
import org.baderlab.csplugins.enrichmentmap.view.HeatMapPanel;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class UpdateHeatMapTask extends AbstractTask{
	//heat map parameters for heat map
    private HeatMapParameters hmParams;

    private EnrichmentMap map;
    
    private List<CyNode> Nodes;
    private List<CyEdge> Edges;
    
    private HeatMapPanel edgeOverlapPanel;
    private HeatMapPanel nodeOverlapPanel;
    private final CytoPanel cytoPanelSouth;
    
    private TaskMonitor taskMonitor;
    
    private CyApplicationManager applicationManager;
       
	public UpdateHeatMapTask(EnrichmentMap map, List<CyNode> nodes,
			List<CyEdge> edges, HeatMapPanel edgeOverlapPanel,
			HeatMapPanel nodeOverlapPanel, CytoPanel cytoPanelSouth, CyApplicationManager applicationManager) {
		super();
		this.map = map;
		this.hmParams = map.getParams().getHmParams();
		Nodes = nodes;
		Edges = edges;
		this.edgeOverlapPanel = edgeOverlapPanel;
		this.nodeOverlapPanel = nodeOverlapPanel;
		this.cytoPanelSouth = cytoPanelSouth;
		
		this.applicationManager = applicationManager;
	}

	public void createEdgesData(){

        if(map.getParams().isData()){
        		this.setEdgeExpressionSet();
          edgeOverlapPanel.updatePanel(map);
          if ( ! map.getParams().isDisableHeatmapAutofocus() ) {
          		// If the state of the cytoPanelWest is HIDE, show it
              if (cytoPanelSouth.getState() == CytoPanelState.HIDE) {
            	  	cytoPanelSouth.setState(CytoPanelState.DOCK);
              }

             // Select my panel
            int index = cytoPanelSouth.indexOfComponent(this.edgeOverlapPanel);
            if (index == -1) {
            	 	return;
            }
            cytoPanelSouth.setSelectedIndex(index);
          }
          edgeOverlapPanel.revalidate();

        }

    }

    private void createNodesData(){

          if(map.getParams().isData()){
        	  	this.setNodeExpressionSet();
              nodeOverlapPanel.updatePanel(map);
              if ( ! map.getParams().isDisableHeatmapAutofocus() ) {
              	// If the state of the cytoPanelWest is HIDE, show it
                  if (cytoPanelSouth.getState() == CytoPanelState.HIDE) {
                	  	cytoPanelSouth.setState(CytoPanelState.DOCK);
                  }

                 // Select my panel
                int index = cytoPanelSouth.indexOfComponent(this.nodeOverlapPanel);
                if (index == -1) {
                	 	return;
                }
               cytoPanelSouth.setSelectedIndex(index);
              }
              nodeOverlapPanel.revalidate();
          }

    }

      public void clearPanels(){
          if(map.getParams().isData()){
              nodeOverlapPanel.clearPanel();
              edgeOverlapPanel.clearPanel();
              if ( ! map.getParams().isDisableHeatmapAutofocus() ) {
              		cytoPanelSouth.setSelectedIndex(cytoPanelSouth.indexOfComponent(this.nodeOverlapPanel));
              		cytoPanelSouth.setSelectedIndex(cytoPanelSouth.indexOfComponent(this.edgeOverlapPanel));
              	}
          }
      }
      
      
      /**
       * Collates the current selected nodes genes to represent the expression of the genes that
       * are in all the selected nodes.  and sets the expression sets (both if there are two datasets)
       *
       * @param params - enrichment map parameters of the current map
       *
       */
      private void setNodeExpressionSet(){

          Object[] nodes = map.getParams().getSelectedNodes().toArray();
          //all unique genesets - if there are two identical genesets in the two sets then 
          //one of them will get over written in the hash.
          //when using two distinct genesets we need to pull the gene info from each set separately.
          	HashMap<String,GeneSet> genesets = map.getAllGenesetsOfInterest();
          	HashMap<String, GeneSet> genesets_set1 = (map.getDatasets().containsKey(EnrichmentMap.DATASET1)) ? map.getDataset(EnrichmentMap.DATASET1).getSetofgenesets().getGenesets() : null;            
          	HashMap<String, GeneSet> genesets_set2 = (map.getDatasets().containsKey(EnrichmentMap.DATASET2)) ? map.getDataset(EnrichmentMap.DATASET2).getSetofgenesets().getGenesets() : null;
           
          //get the current Network
          CyNetwork network = applicationManager.getCurrentNetwork();
          	
          //go through the nodes only if there are some
          if(nodes.length > 0){

              HashSet<Integer> union = new HashSet<Integer>();

              for (Object node1 : nodes) {

                  CyNode current_node = (CyNode) node1;
                                   
                  String nodename = network.getRow(current_node).get(CyNetwork.NAME,String.class);
                  GeneSet current_geneset = genesets.get(nodename);
                  HashSet<Integer> additional_set = null;


                  //if we can't find the geneset and we are dealing with a two-distinct expression sets, check for the gene set in the second set
                  //TODO:Add multi species support
                  if(map.getParams().isTwoDistinctExpressionSets()){
                      GeneSet current_geneset_set1 = genesets_set1.get(nodename);
                      GeneSet current_geneset_set2 = genesets_set2.get(nodename);
                      if(current_geneset_set1 != null && current_geneset.equals(current_geneset_set1) && current_geneset_set2 != null)
                          additional_set = current_geneset_set2.getGenes();
                      if(current_geneset_set2 != null && current_geneset.equals(current_geneset_set2) && current_geneset_set1 != null)
                          additional_set = current_geneset_set1.getGenes();

                  }

                  
                  if (current_geneset == null)
                      continue;

                  HashSet<Integer> current_set = current_geneset.getGenes();

                  if (union == null) {
                      union = new HashSet<Integer>(current_set);

                  } else {
                      union.addAll(current_set);

                  }

                  if(additional_set != null)
                      union.addAll(additional_set);
              }

              HashSet<Integer> genes = union;
              this.nodeOverlapPanel.setCurrentExpressionSet( map.getDataset(EnrichmentMap.DATASET1).getExpressionSets().getExpressionMatrix(genes));
              if(map.getParams().isData2() && map.getDataset(EnrichmentMap.DATASET2).getExpressionSets() != null)
            	  this.nodeOverlapPanel.setCurrentExpressionSet2(map.getDataset(EnrichmentMap.DATASET2).getExpressionSets().getExpressionMatrix(genes));

          }
          else{
        	  this.nodeOverlapPanel.setCurrentExpressionSet(null);
        	  this.nodeOverlapPanel.setCurrentExpressionSet2(null);
          }


      }

      /**
       * Collates the current selected edges genes to represent the expression of the genes that
       * are in all the selected edges.
       *
       * @param params - enrichment map parameters of the current map
       *
       */
      private void setEdgeExpressionSet(){

          Object[] edges = map.getParams().getSelectedEdges().toArray();

        //get the current Network
          CyNetwork network = applicationManager.getCurrentNetwork();
          
          if(edges.length>0){
              HashSet<Integer> intersect = null;
              //HashSet union = null;

              for(int i = 0; i< edges.length;i++){

                  CyEdge current_edge = (CyEdge) edges[i];
                  String edgename = network.getRow(current_edge).get(CyNetwork.NAME, String.class);


                  GenesetSimilarity similarity = map.getGenesetSimilarity().get(edgename);
                  if(similarity == null)
                      continue;

                  HashSet<Integer> current_set = similarity.getOverlapping_genes();

                  //if(intersect == null && union == null){
                  if(intersect == null){
                      intersect = new HashSet<Integer>(current_set);
                  }else{
                      intersect.retainAll(current_set);
                  }

                  if(intersect.size() < 1)
                      break;

              }
              this.edgeOverlapPanel.setCurrentExpressionSet( map.getDataset(EnrichmentMap.DATASET1).getExpressionSets().getExpressionMatrix(intersect));
              if(map.getParams().isData2() && map.getDataset(EnrichmentMap.DATASET2).getExpressionSets() != null)
            	  this.edgeOverlapPanel.setCurrentExpressionSet2(map.getDataset(EnrichmentMap.DATASET2).getExpressionSets().getExpressionMatrix(intersect));
          }
          else{
        	  this.edgeOverlapPanel.setCurrentExpressionSet(null);
        	  this.edgeOverlapPanel.setCurrentExpressionSet2(null);
          }
      }
      
      
      
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		
		this.taskMonitor = taskMonitor;
		
		//if (Edges.size() <= Integer.parseInt(CytoscapeInit.getProperties().getProperty("EnrichmentMap.Heatmap_Edge_Limit",  "100") ) )
		if(Edges.size()>0)
			createEdgesData();
		
		//if (Nodes.size() <= Integer.parseInt(CytoscapeInit.getProperties().getProperty("EnrichmentMap.Heatmap_Node_Limit",  "50") ) )
		if(Nodes.size()>0)
			createNodesData();

		if(Nodes.isEmpty() && Edges.isEmpty())
			clearPanels();
		
	}

}
