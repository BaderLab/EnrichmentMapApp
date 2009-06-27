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
// $LasrChangedRevision$
// $LastChangedBy$
// $HeadURL$

package org.baderlab.csplugins.enrichmentmap;
import cytoscape.visual.mappings.*;
import cytoscape.visual.VisualPropertyType;
import cytoscape.Cytoscape;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

import org.mskcc.colorgradient.*;

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
           runInfo.setPreferredSize(new Dimension(summaryPanelWidth,summaryPanelHeight));
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
               runInfoText = runInfoText + "<b>Jaccard Cut-off:</b>" + params.getJaccardCutOff() + "<br>";
               runInfoText = runInfoText + "<b>Test used:</b>  Jaccard Index<br>";
           }
           else{
               runInfoText = runInfoText + "<b>Overlap Cut-off:</b>" + params.getJaccardCutOff() + "<br>";
               runInfoText = runInfoText + "<b>Test used:</b>  Overlap Index<br>";
           }
           runInfoText = runInfoText + "<font size=-1><b>Genesets File:</b>" + shortenPathname(params.getGMTFileName()) + "<br>";
           runInfoText = runInfoText + "<b>Dataset 1 Data Files:</b> " + shortenPathname(params.getEnrichmentDataset1FileName1()) + ",<br>" + shortenPathname(params.getEnrichmentDataset1FileName2()) + "<br>";
           if(params.isTwoDatasets()){
               runInfoText = runInfoText + "<b>Dataset 2 Data Files:</b> " + shortenPathname(params.getEnrichmentDataset2FileName1()) + ",<br>" + shortenPathname(params.getEnrichmentDataset2FileName2()) + "<br>";
           }
           if(params.isData()){
               runInfoText = runInfoText + "<b>Data file:</b>" + shortenPathname(params.getGCTFileName1()) + "<br>";
           }
           if(params.isData2()){
               runInfoText = runInfoText + "<b>Data file 2:</b>" + shortenPathname(params.getGCTFileName2()) + "<br>";
           }

           runInfoText = runInfoText + "</font></html>";
           return runInfoText;          
       }

    private JPanel createLegend(EnrichmentMapParameters params){

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
            SliderBarPanel pvalueSlider = params.getPvalueSlider();
            //pvalueSlider.setPreferredSize(new Dimension(summaryPanelWidth, 20));

            gridbag.setConstraints(pvalueSlider,c);
            legends.add(pvalueSlider);

            if(params.isFDR()){
                SliderBarPanel qvalueSlider =params.getQvalueSlider();

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
