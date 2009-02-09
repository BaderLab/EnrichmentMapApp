
import giny.model.Edge;
import giny.model.Node;
import giny.view.GraphViewChangeListener;
import giny.view.GraphViewChangeEvent;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.net.URL;

import cytoscape.view.CytoscapeDesktop;
import cytoscape.view.cytopanels.CytoPanel;
import cytoscape.view.cytopanels.CytoPanelState;
import cytoscape.Cytoscape;

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
    private ParametersPanel parametersPanel;

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

        edgeOverlapPanel = new OverlappingGenesPanel(params.getExpression());
        cytoPanel.add("EM Overlap Expression viewer",edgeOverlapPanel);
        nodeOverlapPanel = new OverlappingGenesPanel(params.getExpression());
        cytoPanel.add("EM Geneset Expression viewer",nodeOverlapPanel);
        summaryPanel = new SummaryPanel();
        parametersPanel = new ParametersPanel(params);
        cytoSidePanel.add("Geneset Summary", summaryPanel);
        cytoSidePanel.add("Parameters Used", parametersPanel);
        cytoSidePanel.setSelectedIndex(cytoSidePanel.indexOfComponent(parametersPanel));
        cytoSidePanel.setState(CytoPanelState.DOCK);

    }

    public void graphViewChanged(GraphViewChangeEvent event){
        if(event.isEdgesSelectedType()){

            Edge[] edges = event.getSelectedEdges();
            createEdgesData(edges);

        }
        if(event.isNodesSelectedType()){

            Node[] nodes = event.getSelectedNodes();
            createNodesData(nodes);
        }
        if(event.isNodesUnselectedType() || event.isEdgesUnselectedType()){
            clearPanels();
        }
    }

  public void createEdgesData(Edge[] edges){

    GeneExpressionMatrix expressionSet = params.getExpression();

    //if only one edge has been selected show the basic overlap tab
    if(edges.length == 1){
        Edge current_edge = edges[0];
        String edgename = current_edge.getIdentifier();

        GenesetSimilarity similarity = params.getGenesetSimilarity().get(edgename);

        HashMap currentSubset = expressionSet.getExpressionMatrix(similarity.getOverlapping_genes());

        edgeOverlapPanel.updatePanel(currentSubset);
        cytoPanel.setSelectedIndex(cytoPanel.indexOfComponent(edgeOverlapPanel));

        summaryPanel.updateEdgeInfo(edges);
        cytoSidePanel.setSelectedIndex(cytoSidePanel.indexOfComponent(summaryPanel));

     }else{
        HashSet intersect = null;
        HashSet union = null;

        for(int i = 0; i< edges.length;i++){

            Edge current_edge = edges[i];
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

        edgeOverlapPanel.updatePanel(expressionSet.getExpressionMatrix(intersect));
        cytoPanel.setSelectedIndex(cytoPanel.indexOfComponent(edgeOverlapPanel));
        //summaryPanel.updateEdgeInfo(edges);
     }
      edgeOverlapPanel.revalidate();
      //summaryPanel.revalidate();

  }

  private void createNodesData(Node[] nodes){

    GeneExpressionMatrix expressionSet = params.getExpression();

    //if only one edge has been selected show the basic overlap tab
    if(nodes.length == 1){
        Node current_node = nodes[0];
        String nodename = current_node.getIdentifier();

        GeneSet current_geneset = (GeneSet)params.getGenesetsOfInterest().get(nodename);

        nodeOverlapPanel.updatePanel(expressionSet.getExpressionMatrix(current_geneset.getGenes()));
        cytoPanel.setSelectedIndex(cytoPanel.indexOfComponent(nodeOverlapPanel));
        summaryPanel.updateNodeInfo(nodes);
        cytoSidePanel.setSelectedIndex(cytoSidePanel.indexOfComponent(summaryPanel));
     }else{
        HashSet union = null;


        for(int i = 0; i< nodes.length;i++){

            Node current_node = nodes[i];
            String nodename = current_node.getIdentifier();

            GeneSet current_geneset = (GeneSet)params.getGenesetsOfInterest().get(nodename);

            HashSet current_set = current_geneset.getGenes();

            if( union == null){
                union = new HashSet(current_set);
            }else{
                union.addAll(current_set);
             }
        }
        nodeOverlapPanel.updatePanel(expressionSet.getExpressionMatrix(union));
        cytoPanel.setSelectedIndex(cytoPanel.indexOfComponent(nodeOverlapPanel));
        summaryPanel.updateNodeInfo(nodes);
        cytoSidePanel.setSelectedIndex(cytoSidePanel.indexOfComponent(summaryPanel));
    }
    nodeOverlapPanel.revalidate();
    summaryPanel.revalidate();
  }

    public void clearPanels(){
        summaryPanel.clearInfo();
        nodeOverlapPanel.clearPanel();
        edgeOverlapPanel.clearPanel();
    }
}
