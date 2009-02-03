
import giny.model.Edge;
import giny.view.GraphViewChangeListener;
import giny.view.GraphViewChangeEvent;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Created by
 * User: risserlin
 * Date: Feb 2, 2009
 * Time: 1:25:36 PM
 */
public class edgeOverlappingGenesActionListener implements  GraphViewChangeListener {

    private EnrichmentMapParameters params;
    private OverlappingGenesFrame overlapFrame;

    public edgeOverlappingGenesActionListener(EnrichmentMapParameters params) {
        this.params = params;
    }

    public void graphViewChanged(GraphViewChangeEvent event){
        if(event.isEdgesSelectedType()){
            //create an overlap viewer for one edge
            if(overlapFrame == null)
                overlapFrame = new OverlappingGenesFrame(params);
            GeneExpressionMatrix expressionSet = params.getExpression();

            Edge[] edges = event.getSelectedEdges();
            for(int i = 0; i< edges.length;i++){

                Edge current_edge = edges[i];
                String edgename = current_edge.getIdentifier();

                GenesetSimilarity similarity = params.getGenesetSimilarity().get(edgename);

                overlapFrame.createResultTab("gene overlap "+overlapFrame.getResultPanelCount(),expressionSet,expressionSet.getExpressionMatrix(similarity.getOverlapping_genes()), similarity);
            }
            overlapFrame.setVisible(true);
        }
    }


}
