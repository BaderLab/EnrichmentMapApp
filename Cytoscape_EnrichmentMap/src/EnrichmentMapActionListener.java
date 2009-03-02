
import giny.model.Edge;
import giny.model.Node;
import giny.view.GraphViewChangeListener;
import giny.view.GraphViewChangeEvent;

import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.net.URL;
import java.awt.event.ActionListener;

import cytoscape.view.CytoscapeDesktop;
import cytoscape.view.CyMenus;
import cytoscape.view.cytopanels.CytoPanel;
import cytoscape.view.cytopanels.CytoPanelState;
import cytoscape.Cytoscape;
import cytoscape.util.CytoscapeToolBar;

import javax.swing.*;

/**
 * Created by
 * User: risserlin
 * Date: Feb 2, 2009
 * Time: 1:25:36 PM
 */
public class EnrichmentMapActionListener implements  GraphViewChangeListener {

    private EnrichmentMapParameters params;
    private OverlappingGenesPanel edgeOverlapPanel;
    private OverlappingGenesPanel nodeOverlapPanel;
    private SummaryPanel summaryPanel;

    private List<Node> Nodes;
    private List<Edge> Edges;

    private final CytoPanel cytoPanel;
    private final CytoPanel cytoSidePanel;

    public EnrichmentMapActionListener(EnrichmentMapParameters params) {
        this.params = params;

        //initialize the cyto panel to have the expression viewing.
        final CytoscapeDesktop desktop = Cytoscape.getDesktop();
        cytoPanel = desktop.getCytoPanel(SwingConstants.SOUTH);
        cytoSidePanel = desktop.getCytoPanel(SwingConstants.EAST);
        //final URL url = new URL("http","www.baderlab.org","/wiki/common/network_bader_website_icon.gif");
        //final Icon icon = new ImageIcon(url);
        if(params.isData()){
            edgeOverlapPanel = new OverlappingGenesPanel(params);
            cytoPanel.add("EM Overlap Expression viewer",edgeOverlapPanel);
            nodeOverlapPanel = new OverlappingGenesPanel(params);
            cytoPanel.add("EM Geneset Expression viewer",nodeOverlapPanel);

            HeatMapParameters hmParams = new HeatMapParameters(edgeOverlapPanel, nodeOverlapPanel);
            hmParams.initColorGradients(params.getExpression());
            edgeOverlapPanel.setHmParams(hmParams);
            nodeOverlapPanel.setHmParams(hmParams);
        }

        summaryPanel = new SummaryPanel();
        cytoSidePanel.add("Geneset Summary", summaryPanel);

        //initialize node and edge lists
        Nodes = new ArrayList<Node>();
        Edges = new ArrayList<Edge>();
     
    }

    public void graphViewChanged(GraphViewChangeEvent event){
        if(event.isEdgesSelectedType()){

            Edge[] selectedEdges = event.getSelectedEdges();

            //Add all the selected edges to the list of edges
            for(int i=0;i<selectedEdges.length;i++){
                //check to see that the edge isn't already in the list
                if(!Edges.contains(selectedEdges[i]))
                    Edges.add(selectedEdges[i]);
            }

            createEdgesData();

        }
        if(event.isNodesSelectedType()){

            Node[] selectedNodes = event.getSelectedNodes();

            //Add all the selected nodes to the list of nodes
            for(int i=0;i<selectedNodes.length;i++){
                //check to see that the node isn't already in the list
                if(!Nodes.contains(selectedNodes[i]))
                    Nodes.add(selectedNodes[i]);
            }

            createNodesData();
        }
        if(event.isNodesUnselectedType()){
            Node[] unselectedNodes = event.getUnselectedNodes();

            //remove all the unselected nodes from the list of nodes
            for(int i = 0; i < unselectedNodes.length; i++){
                //check to make sure that the node is in the list
                if(Nodes.contains(unselectedNodes[i]))
                    Nodes.remove(unselectedNodes[i]);
            }

            if(!Nodes.isEmpty())
                createNodesData();

            if(Nodes.isEmpty() && Edges.isEmpty())
                clearPanels();
        }
        if(event.isEdgesUnselectedType()){

            Edge[] unselectedEdges = event.getUnselectedEdges();

            //Add all the selected edges to the list of edges
            for(int i=0;i<unselectedEdges.length;i++){
                 //check to see that the edge isn't already in the list
                 if(Edges.contains(unselectedEdges[i]))
                      Edges.remove(unselectedEdges[i]);
                 }

            if(!Edges.isEmpty())
                createEdgesData();

            if(Nodes.isEmpty() && Edges.isEmpty())
                clearPanels();
        }
    }

  public void createEdgesData(){

    GeneExpressionMatrix expressionSet = params.getExpression();

    //convert Edge list to array
      Object[] edges = Edges.toArray();

    //if only one edge has been selected show the basic overlap tab
    if(edges.length == 1){
        Edge current_edge = (Edge)edges[0];
        String edgename = current_edge.getIdentifier();

        //only update the expression viewer if there is data loaded
        if(params.isData()){
            GenesetSimilarity similarity = params.getGenesetSimilarity().get(edgename);

            HashMap currentSubset = expressionSet.getExpressionMatrix(similarity.getOverlapping_genes());

            edgeOverlapPanel.setCurrentGeneExpressionSet(currentSubset);
            edgeOverlapPanel.updatePanel();
            cytoPanel.setSelectedIndex(cytoPanel.indexOfComponent(edgeOverlapPanel));
        }

        summaryPanel.updateEdgeInfo(edges);
        cytoSidePanel.setSelectedIndex(cytoSidePanel.indexOfComponent(summaryPanel));

     }else{
        if(params.isData()){
            HashSet intersect = null;
            HashSet union = null;

            for(int i = 0; i< edges.length;i++){

                Edge current_edge = (Edge) edges[i];
                String edgename = current_edge.getIdentifier();


                GenesetSimilarity similarity = params.getGenesetSimilarity().get(edgename);
                HashSet current_set = similarity.getOverlapping_genes();

                if(intersect == null && union == null){
                    intersect = new HashSet(current_set);
                    union = new HashSet(current_set);
                }else{
                    intersect.retainAll(current_set);
                    union.addAll(current_set);
                }
            }

            edgeOverlapPanel.setCurrentGeneExpressionSet(expressionSet.getExpressionMatrix(intersect));
            edgeOverlapPanel.updatePanel();
            cytoPanel.setSelectedIndex(cytoPanel.indexOfComponent(edgeOverlapPanel));
        }
     }
     if(params.isData())
        edgeOverlapPanel.revalidate();

  }

  private void createNodesData(){

    GeneExpressionMatrix expressionSet = params.getExpression();

    Object[] nodes = Nodes.toArray();

    //if only one edge has been selected show the basic overlap tab
    if(nodes.length == 1){
        if(params.isData()){
            Node current_node = (Node)nodes[0];
            String nodename = current_node.getIdentifier();

            GeneSet current_geneset = (GeneSet)params.getGenesetsOfInterest().get(nodename);

            nodeOverlapPanel.setCurrentGeneExpressionSet(expressionSet.getExpressionMatrix(current_geneset.getGenes()));
            nodeOverlapPanel.updatePanel();
            cytoPanel.setSelectedIndex(cytoPanel.indexOfComponent(nodeOverlapPanel));
        }
        summaryPanel.updateNodeInfo(nodes);
        cytoSidePanel.setSelectedIndex(cytoSidePanel.indexOfComponent(summaryPanel));
     }else{
        if(params.isData()){
            HashSet union = null;

            for(int i = 0; i< nodes.length;i++){

                Node current_node = (Node)nodes[i];
                String nodename = current_node.getIdentifier();

                GeneSet current_geneset = (GeneSet)params.getGenesetsOfInterest().get(nodename);

                HashSet current_set = current_geneset.getGenes();

                if( union == null){
                    union = new HashSet(current_set);
                }else{
                    union.addAll(current_set);
                }
            }
            nodeOverlapPanel.setCurrentGeneExpressionSet(expressionSet.getExpressionMatrix(union));
            nodeOverlapPanel.updatePanel();
            cytoPanel.setSelectedIndex(cytoPanel.indexOfComponent(nodeOverlapPanel));
        }
        summaryPanel.updateNodeInfo(nodes);
        cytoSidePanel.setSelectedIndex(cytoSidePanel.indexOfComponent(summaryPanel));
    }
    if(params.isData())
        nodeOverlapPanel.revalidate();
    summaryPanel.revalidate();
  }

    public void clearPanels(){
        summaryPanel.clearInfo();
        if(params.isData()){
            nodeOverlapPanel.clearPanel();
            edgeOverlapPanel.clearPanel();
            cytoPanel.setSelectedIndex(cytoPanel.indexOfComponent(nodeOverlapPanel));
            cytoPanel.setSelectedIndex(cytoPanel.indexOfComponent(edgeOverlapPanel));
        }
    }
}
