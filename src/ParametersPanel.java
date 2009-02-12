import cytoscape.visual.mappings.*;
import cytoscape.visual.VisualPropertyType;
import cytoscape.Cytoscape;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Created by
 * User: risserlin
 * Date: Feb 9, 2009
 * Time: 2:41:48 PM
 */
public class ParametersPanel extends JPanel {
    private JTextPane runInfo;
    private EnrichmentMapParameters params;

    public ParametersPanel(EnrichmentMapParameters params) {
           this.setLayout(new java.awt.BorderLayout());

           this.params = params;
           //information about the current analysis
           runInfo = new JTextPane();
           runInfo.setEditable(false);
           runInfo.setContentType("text/html");
           runInfo.setText(getRunInfo());

           JScrollPane jScrollPane = new javax.swing.JScrollPane(runInfo);

           this.add(jScrollPane);

           //this.add(createLegend());

       }

       private String getRunInfo(){

           String runInfoText = "<html> <h1>Parameters:</h1>";
           runInfoText = runInfoText + "<b>P-value Cut-off:</b>" + params.getPvalue() + "<br>";
           runInfoText = runInfoText + "<b>FDR Q-value Cut-off:</b>" + params.getQvalue() + "<br>";

           if(params.isJaccard()){
               runInfoText = runInfoText + "<b>Jaccard Cut-off:</b>" + params.getJaccardCutOff() + "<br>";
               runInfoText = runInfoText + "<b>Test used:</b>  Jaccard Index<br>";
           }
           else{
               runInfoText = runInfoText + "<b>Overlap Cut-off:</b>" + params.getJaccardCutOff() + "<br>";
               runInfoText = runInfoText + "<b>Test used:</b>  Overlap Index<br>";
           }
           runInfoText = runInfoText + "<font size=-1><b>Genesets File:</b>" + params.getGMTFileName() + "<br>";
           runInfoText = runInfoText + "<b>Dataset 1 Data Files:</b> " + params.getEnrichmentDataset1FileName1() + ",<br>" + params.getEnrichmentDataset1FileName2() + "<br>";
           if(params.isTwoDatasets()){
               runInfoText = runInfoText + "<b>Dataset 2 Data Files:</b> " + params.getEnrichmentDataset2FileName1() + ",<br>" + params.getEnrichmentDataset2FileName2() + "<br>";
           }
           if(params.isData()){
               runInfoText = runInfoText + "<b>Data file:</b>" + params.getGCTFileName1() + "<br>";
           }
           if(params.isData2()){
               runInfoText = runInfoText + "<b>Data file 2:</b>" + params.getGCTFileName2() + "<br>";
           }

           runInfoText = runInfoText + "</font></html>";
           return runInfoText;
       }

    private JPanel createLegend(){

        JPanel legends = new JPanel();

        //create a legend based on the colouring of the nodes
        ContinuousMapping continuousMapping = new ContinuousMapping(Color.WHITE, ObjectMapping.NODE_MAPPING);
        Interpolator numToColor = new LinearNumberToColorInterpolator();
        continuousMapping.setInterpolator(numToColor);

        JPanel legend1  = continuousMapping.getLegend(VisualPropertyType.NODE_FILL_COLOR);

        legends.add(legend1);

        if(params.isTwoDatasets()){
            continuousMapping.setControllingAttributeName("NODE_BORDER_COLOR", Cytoscape.getCurrentNetwork(),true);
            JPanel legend2 =continuousMapping.getLegend(VisualPropertyType.NODE_BORDER_COLOR);
            legends.add(legend2);
        }
        return legends;


    }

}
