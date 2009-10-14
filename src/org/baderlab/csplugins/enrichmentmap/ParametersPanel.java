/**
 **                       EnrichmentMap Cytoscape Plugin
 **
 ** Copyright (c) 2008-2009 Bader Lab, Donnelly Centre for Cellular and Biomolecular 
 ** Research, University of Toronto
 **
 ** Contact: http://www.baderlab.org
 **
 ** Code written by: Ruth Isserlin
 ** Authors: Daniele Merico, Ruth Isserlin, Oliver Stueker, Gary D. Bader
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** (at your option) any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** University of Toronto
 ** has no obligations to provide maintenance, support, updates, 
 ** enhancements or modifications.  In no event shall the
 ** University of Toronto
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** University of Toronto
 ** has been advised of the possibility of such damage.  
 ** See the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **
 **/

// $Id$
// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$
// $HeadURL$

package org.baderlab.csplugins.enrichmentmap;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URL;

/**
 * Created by
 * User: risserlin
 * Date: Feb 9, 2009
 * Time: 2:41:48 PM
 */
public class ParametersPanel extends JPanel {

    public static int summaryPanelWidth = 150;
    public static int summaryPanelHeight = 1000;

    public ParametersPanel() {

       }

    public void updatePanel(EnrichmentMapParameters params){

            this.removeAll();
            this.revalidate();
            this.setLayout(new java.awt.BorderLayout());

            JPanel main = new JPanel(new BorderLayout());

           JPanel legends = createLegend(params);
           main.add(legends, BorderLayout.NORTH);

           JTextPane runInfo;
        //information about the current analysis
           runInfo = new JTextPane();
           runInfo.setEditable(false);
           runInfo.setContentType("text/html");
           runInfo.setText(getRunInfo(params));
           runInfo.setPreferredSize(new Dimension(summaryPanelWidth,summaryPanelHeight/2));
           main.add(runInfo, BorderLayout.SOUTH);

            JScrollPane jScrollPane = new javax.swing.JScrollPane(main);

          //jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
          this.add(jScrollPane);


        this.revalidate();

    }

       private String getRunInfo(EnrichmentMapParameters params){

           String runInfoText = "<html> <h1>Parameters:</h1>";

           runInfoText = runInfoText + "<b>P-value Cut-off:</b>" + params.getPvalue() + "<br>";
           runInfoText = runInfoText + "<b>FDR Q-value Cut-off:</b>" + params.getQvalue() + "<br>";

           if(params.isJaccard()){
               runInfoText = runInfoText + "<b>Jaccard Cut-off:</b>" + params.getSimilarityCutOff() + "<br>";
               runInfoText = runInfoText + "<b>Test used:</b>  Jaccard Index<br>";
           }
           else{
               runInfoText = runInfoText + "<b>Overlap Cut-off:</b>" + params.getSimilarityCutOff() + "<br>";
               runInfoText = runInfoText + "<b>Test used:</b>  Overlap Index<br>";
           }
           runInfoText = runInfoText + "<font size=-1><b>Genesets File:</b>" + shortenPathname(params.getGMTFileName()) + "<br>";
           runInfoText = runInfoText + "<b>Dataset 1 Data Files:</b> " + shortenPathname(params.getEnrichmentDataset1FileName1()) + ",<br>" + shortenPathname(params.getEnrichmentDataset1FileName2()) + "<br>";
           if(params.isTwoDatasets()){
               runInfoText = runInfoText + "<b>Dataset 2 Data Files:</b> " + shortenPathname(params.getEnrichmentDataset2FileName1()) + ",<br>" + shortenPathname(params.getEnrichmentDataset2FileName2()) + "<br>";
           }
           if(params.isData()){
               runInfoText = runInfoText + "<b>Data file:</b>" + shortenPathname(params.getExpressionFileName1()) + "<br>";
           }
           if(params.isData2()){
               runInfoText = runInfoText + "<b>Data file 2:</b>" + shortenPathname(params.getExpressionFileName2()) + "<br>";
           }

           runInfoText = runInfoText + "</font></html>";
           return runInfoText;
       }

    private JPanel createLegend(EnrichmentMapParameters params){

        JPanel legends = new JPanel();
        setPreferredSize(new Dimension(summaryPanelWidth,summaryPanelHeight/2));

        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        legends.setLayout(gridbag);

        c.weighty = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.NONE;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(5,30,5,2);
        c.gridwidth = GridBagConstraints.REMAINDER;


        //first row - circle
        c.gridx = 0;
        c.gridy = 0;

        //represent the node color as an png/gif instead of using java to generate the representation
        URL nodeIconURL = Enrichment_Map_Plugin.class.getResource("resources/node_color_small.png");
        if (nodeIconURL != null) {
            ImageIcon nodeIcon;
             nodeIcon = new ImageIcon(nodeIconURL);
            JLabel nodeColorLabel = new JLabel(nodeIcon);
            gridbag.setConstraints(nodeColorLabel, c);
            legends.add(nodeColorLabel);
        }

        LegendPanel node_legend = new LegendPanel(-1.0,1.0,EnrichmentMapVisualStyle.max_phenotype1,EnrichmentMapVisualStyle.max_phenotype2, params.getDataset1Phenotype1(), params.getDataset1Phenotype2());
        node_legend.setToolTipText("Phenotype * (1-P_value)");

        //second row - legend 1
        c.gridx = 0;
        c.gridy = 1;
        c.insets = new Insets(0,0,0,0);
        gridbag.setConstraints(node_legend, c);
        legends.add(node_legend);

        //If there are two datasets then we need to define the node border legend as well.
        if(params.isTwoDatasets()){

            //third row - circle
            c.gridx = 0;
            c.gridy = 2;
            c.insets = new Insets(5,30,5,2);

            //represent the node border color as an png/gif instead of using java to generate the representation
            URL nodeborderIconURL = Enrichment_Map_Plugin.class.getResource("resources/node_border_color_small.png");
            if (nodeborderIconURL != null) {
                ImageIcon nodeborderIcon = new ImageIcon(nodeborderIconURL);
                JLabel nodeborderColorLabel = new JLabel(nodeborderIcon);
                gridbag.setConstraints(nodeborderColorLabel, c);
                legends.add(nodeborderColorLabel);
            }

            LegendPanel node_legend2 = new LegendPanel(-1.0,1.0,EnrichmentMapVisualStyle.max_phenotype1,EnrichmentMapVisualStyle.max_phenotype2, params.getDataset2Phenotype1(),params.getDataset2Phenotype2());
            node_legend2.setToolTipText("Phenotype * (1-P_value)");

            //fourth row - legend 2
            c.gridx = 0;
            c.gridy = 3;
            c.insets = new Insets(0,0,0,0);
            gridbag.setConstraints(node_legend2, c);
            legends.add(node_legend2);

        }


            c.gridx = 0;
            c.gridy = 4;
            c.insets = new Insets(0,0,0,0);
            c.fill = GridBagConstraints.NONE;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.anchor = GridBagConstraints.LINE_START;
            SliderBarPanel pvalueSlider = params.getPvalueSlider();

            gridbag.setConstraints(pvalueSlider,c);
            legends.add(pvalueSlider);

            if(params.isFDR()){
                SliderBarPanel qvalueSlider = params.getQvalueSlider();

                c.gridx = 0;
                c.gridy = 5;
                //qvalueSlider.setPreferredSize(new Dimension(summaryPanelWidth, 20));

                gridbag.setConstraints(qvalueSlider,c);
                legends.add(qvalueSlider);

            }


        return legends;


    }

    private String shortenPathname (String pathname){
        if(pathname != null){
            String[] tokens = pathname.split("\\"+File.separator);

            int num_tokens = tokens.length;

            String new_pathname;
            if(num_tokens >=2)
                new_pathname = "..." + File.separator + tokens[num_tokens -2] + File.separator + tokens[num_tokens -1];
            else
                new_pathname = pathname;

            return new_pathname;
        }
        return pathname;

    }


}
