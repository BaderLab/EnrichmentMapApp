
import giny.model.Edge;
import giny.model.Node;
import giny.view.GraphViewChangeListener;
import giny.view.GraphViewChangeEvent;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.HashMap;

/**
 * Created by
 * User: risserlin
 * Date: Feb 2, 2009
 * Time: 1:25:36 PM
 */
public class EnrichmentMapActionListener implements  GraphViewChangeListener {

    private EnrichmentMapParameters params;
    private OverlappingGenesFrame overlapFrame;

    public EnrichmentMapActionListener(EnrichmentMapParameters params) {
        this.params = params;
    }

    public void graphViewChanged(GraphViewChangeEvent event){
        if(event.isEdgesSelectedType()){
            //create an overlap viewer for one edge
            if(overlapFrame == null)
                overlapFrame = new OverlappingGenesFrame(params);

            Edge[] edges = event.getSelectedEdges();
            createEdgesData(edges);

        }
        if(event.isNodesSelectedType()){
            if(overlapFrame == null)
               overlapFrame = new OverlappingGenesFrame(params);

            Node[] nodes = event.getSelectedNodes();
            createNodesData(nodes);
        }
    }

  public void createEdgesData(Edge[] edges){

    GeneExpressionMatrix expressionSet = params.getExpression();

    //if only one edge has been selected show the basic overlap tab
    if(edges.length == 1){
        Edge current_edge = edges[0];
        String edgename = current_edge.getIdentifier();

        GenesetSimilarity similarity = params.getGenesetSimilarity().get(edgename);

        overlapFrame.createResultTab("GeneSet overlap ",expressionSet,expressionSet.getExpressionMatrix(similarity.getOverlapping_genes()), similarity);

     }else{
        HashSet intersect = null;
        HashSet union = null;
        HashMap<String,String> allNames = new HashMap();

        for(int i = 0; i< edges.length;i++){

            Edge current_edge = edges[i];
            String edgename = current_edge.getIdentifier();


            GenesetSimilarity similarity = params.getGenesetSimilarity().get(edgename);
            HashSet current_set = similarity.getOverlapping_genes();
            allNames.put(similarity.getGeneset1_Name(), similarity.getGeneset1_Name());
            allNames.put(similarity.getGeneset2_Name(), similarity.getGeneset2_Name());

            if(intersect == null && union == null){
                intersect = new HashSet(current_set);
                union = new HashSet(current_set);
            }else{
                intersect.retainAll(current_set);
                union.addAll(current_set);
             }
        }
        overlapFrame.createResultTab("Multiple GeneSet Overlaps - intersect ",expressionSet,expressionSet.getExpressionMatrix(intersect),allNames.keySet().toString());
        //overlapFrame.createResultTab("Multiple GeneSet Overlaps - union ",expressionSet,expressionSet.getExpressionMatrix(union),allNames.keySet().toString());
    }
    overlapFrame.setVisible(true);
  }

  private void createNodesData(Node[] nodes){

    GeneExpressionMatrix expressionSet = params.getExpression();

    //if only one edge has been selected show the basic overlap tab
    if(nodes.length == 1){
        Node current_node = nodes[0];
        String nodename = current_node.getIdentifier();

        GeneSet current_geneset = (GeneSet)params.getGenesetsOfInterest().get(nodename);

        overlapFrame.createResultTab("GeneSet ",expressionSet,expressionSet.getExpressionMatrix(current_geneset.getGenes()),nodename);

     }else{
        HashSet union = null;
        String allNames = "";

        for(int i = 0; i< nodes.length;i++){

            Node current_node = nodes[i];
            String nodename = current_node.getIdentifier();
            allNames = allNames + nodename + ", ";

            GeneSet current_geneset = (GeneSet)params.getGenesetsOfInterest().get(nodename);

            HashSet current_set = current_geneset.getGenes();

            if( union == null){
                union = new HashSet(current_set);
            }else{
                union.addAll(current_set);
             }
        }
        overlapFrame.createResultTab("Multiple Geneset - union ",expressionSet,expressionSet.getExpressionMatrix(union), allNames);
    }
    overlapFrame.setVisible(true);
  }
}
