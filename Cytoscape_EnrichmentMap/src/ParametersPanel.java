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

    private int summaryPanelWidth = 150;
    private int summaryPanelHeight = 1000;

    public ParametersPanel(EnrichmentMapParameters params) {
           this.setLayout(new java.awt.BorderLayout());

           this.params = params;

           JPanel main = new JPanel(new BorderLayout());

           JPanel legends = createLegend();
           main.add(legends, BorderLayout.NORTH);

            //add slider bars
 /*           JPanel center = new JPanel(new GridLayout(2,1));
            SliderBarPanel pvalueSlider = new SliderBarPanel(0,params.getPvalue(),"P-value Cutoff",params, EnrichmentMapVisualStyle.PVALUE_DATASET1, EnrichmentMapVisualStyle.PVALUE_DATASET2);
            pvalueSlider.setPreferredSize(new Dimension(summaryPanelWidth, 20));

            center.add(pvalueSlider);

            if(params.isFDR()){
                SliderBarPanel qvalueSlider = new SliderBarPanel(0,params.getQvalue(),"Q-value Cutoff",params, EnrichmentMapVisualStyle.FDR_QVALUE_DATASET1, EnrichmentMapVisualStyle.FDR_QVALUE_DATASET2);

		        qvalueSlider.setPreferredSize(new Dimension(summaryPanelWidth, 20));

                center.add(qvalueSlider);

            }

            main.add(center, BorderLayout.CENTER);
*/
           //information about the current analysis
           runInfo = new JTextPane();
           runInfo.setEditable(false);
           runInfo.setContentType("text/html");
           runInfo.setText(getRunInfo());
           runInfo.setPreferredSize(new Dimension(summaryPanelWidth,summaryPanelHeight));
           main.add(runInfo, BorderLayout.SOUTH);

           JScrollPane jScrollPane = new javax.swing.JScrollPane(main);
           //jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
           this.add(jScrollPane);

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

        //legends.setPreferredSize(new Dimension(summaryPanelWidth,summaryPanelHeight/4));
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        legends.setLayout(gridbag);

        c.weighty = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;

        //create a legend based on the colours of the nodes but use the mondrian gradients and rendering

        ColorGradientRange range = ColorGradientRange.getInstance(-1,0, 0,1, -1,0,0,1);
        ColorGradientTheme theme = ColorGradientTheme.ENRICHMENTMAP_NODE_THEME;
        ColorGradientWidget node_legend = ColorGradientWidget.getInstance("Node Colour Legend",summaryPanelWidth,60,5,5,theme,range,true,ColorGradientWidget.LEGEND_POSITION.TOP);

        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;
        //c.gridwidth = 10;
        gridbag.setConstraints(node_legend, c);
        legends.add(node_legend);

        JLabel minlabel = new JLabel(params.getDataset1Phenotype2());
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.LINE_START;
        c.fill = GridBagConstraints.NONE;
        gridbag.setConstraints(minlabel, c);
        legends.add(minlabel);

        JLabel maxlabel = new JLabel(params.getDataset1Phenotype1());
        c.gridx = 10;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_END;
        c.gridy = 2;
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(maxlabel, c);
        legends.add(maxlabel);


        if(params.isTwoDatasets()){
            ColorGradientWidget node_legend2 = ColorGradientWidget.getInstance("Node Border Colour Legend",summaryPanelWidth,60,5,5,theme,range,true,ColorGradientWidget.LEGEND_POSITION.TOP);

            c.gridx = 0;
            c.gridy = 3;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridwidth = GridBagConstraints.REMAINDER;
            gridbag.setConstraints(node_legend2, c);
            legends.add(node_legend2);

            JLabel minlabel2 = new JLabel(params.getDataset2Phenotype2());
            c.gridx = 0;
            c.gridy = 4;
            c.gridwidth = 1;
            c.anchor = GridBagConstraints.LINE_START;
            c.fill = GridBagConstraints.NONE;
            gridbag.setConstraints(minlabel2, c);
            legends.add(minlabel2);

            JLabel maxlabel2 = new JLabel(params.getDataset2Phenotype1());
            c.gridx = 10;
            c.fill = GridBagConstraints.NONE;
            c.anchor = GridBagConstraints.LINE_END;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.gridy = 4;
            gridbag.setConstraints(maxlabel2, c);
            legends.add(maxlabel2);
        }


            c.gridx = 0;
            c.gridy = 5;
            c.insets = new Insets(10,0,10,0);
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.anchor = GridBagConstraints.LINE_START;
            SliderBarPanel pvalueSlider = new SliderBarPanel(0,params.getPvalue(),"P-value Cutoff",params, EnrichmentMapVisualStyle.PVALUE_DATASET1, EnrichmentMapVisualStyle.PVALUE_DATASET2,summaryPanelWidth);
            //pvalueSlider.setPreferredSize(new Dimension(summaryPanelWidth, 20));

            gridbag.setConstraints(pvalueSlider,c);
            legends.add(pvalueSlider);

            if(params.isFDR()){
                SliderBarPanel qvalueSlider = new SliderBarPanel(0,params.getQvalue(),"Q-value Cutoff",params, EnrichmentMapVisualStyle.FDR_QVALUE_DATASET1, EnrichmentMapVisualStyle.FDR_QVALUE_DATASET2,summaryPanelWidth);


                c.gridx = 0;
                c.gridy = 6;
                c.insets = new Insets(10,0,10,0);
                c.gridwidth = GridBagConstraints.REMAINDER;
                c.anchor = GridBagConstraints.LINE_START;
                //qvalueSlider.setPreferredSize(new Dimension(summaryPanelWidth, 20));

                gridbag.setConstraints(qvalueSlider,c);
                legends.add(qvalueSlider);

            }


        return legends;


    }

}
