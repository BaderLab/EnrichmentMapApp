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
import java.awt.event.ActionEvent;
import prefuse.data.query.NumberRangeModel;
import org.mskcc.colorgradient.* ;

/**
 * Created by
 * User: risserlin
 * Date: Feb 11, 2009
 * Time: 12:23:01 PM
 */
public class HeatMapParameters {
      private org.mskcc.colorgradient.ColorGradientRange range;
      private org.mskcc.colorgradient.ColorGradientTheme theme;

      private boolean rowNorm = false;
      private boolean logtransform = false;

      private int num_ranks = 0;
      private boolean rank_dataset1 = false;
      private boolean rank_dataset2 = false;

      private double minExpression;
      private double maxExpression;
      private double minExpression_rownorm;
      private double maxExpression_rownorm;

     private OverlappingGenesPanel edgeOverlapPanel;
     private OverlappingGenesPanel nodeOverlapPanel;

    public HeatMapParameters(OverlappingGenesPanel edgeOverlapPanel, OverlappingGenesPanel nodeOverlapPanel) {
        this.edgeOverlapPanel = edgeOverlapPanel;
        this.nodeOverlapPanel = nodeOverlapPanel;
    }

    public void initColorGradients(GeneExpressionMatrix expression){

        minExpression = expression.getMinExpression();
        maxExpression = expression.getMaxExpression();
        minExpression_rownorm = expression.getMinExpression(expression.getExpressionMatrix_rowNormalized());
        maxExpression_rownorm = expression.getMaxExpression(expression.getExpressionMatrix_rowNormalized());

        double max = Math.max(Math.abs(minExpression), maxExpression);

        double median = 0;

        //if the minimum expression is above zero make it a one colour heatmap
        if(minExpression >= 0){
            range = ColorGradientRange.getInstance(0,max/2, max/2,max, 0,max/2,max/2,max);
            theme = ColorGradientTheme.GREEN_ONECOLOR_GRADIENT_THEME;
        }
        else{
            range = ColorGradientRange.getInstance(-max,median, median,max, -max,median,median,max);
            theme = ColorGradientTheme.GREEN_MAGENTA_GRADIENT_THEME;
        }

    }

    public void ResetColorGradient(){
          double min;
          double max;
          double median;

          if(rowNorm){
              min = minExpression_rownorm;
              max = maxExpression_rownorm;
              max = Math.max(Math.abs(min),max);

          }
          else if(logtransform){
              min = Math.log1p(minExpression);
              max = Math.log1p(maxExpression) ;
              max = Math.max(Math.abs(min),max);

          }
          else{
              min = minExpression;
              max = Math.max(Math.abs(minExpression), maxExpression);
          }
          median = max/2;
          if(min >= 0){
              median = max/2;
              range = ColorGradientRange.getInstance(0,median, median,max, 0,median,median,max);
              theme = ColorGradientTheme.GREEN_ONECOLOR_GRADIENT_THEME;
           }
          else{
              median = 0;
              range = ColorGradientRange.getInstance(-max,0, 0,max, -max,0,0,max);
              theme = ColorGradientTheme.GREEN_MAGENTA_GRADIENT_THEME;
          }

      }

    public JPanel createRankOptionsPanel(){
       JPanel RankOptions = new JPanel();
        RankOptions.setLayout(new GridLayout(3,1));

        JRadioButton noSort;
        JRadioButton RankDataset1;
        JRadioButton RankDataset2;
        ButtonGroup rankView;

        noSort = new JRadioButton("No Sorting");
        noSort.setActionCommand("noSort");
        RankDataset1 = new JRadioButton("Sort by Dataset1 Rank");
        RankDataset1.setActionCommand("dataset1");
        RankDataset2 = new JRadioButton("Sort by Dataset2 Rank");
        RankDataset2.setActionCommand("dataset2");

        if(rank_dataset1)
            RankDataset1.setSelected(true);
        else if(rank_dataset2)
             RankDataset2.setSelected(true);
        else
            noSort.setSelected(true);

         rankView = new javax.swing.ButtonGroup();
         rankView.add(noSort);
         rankView.add(RankDataset1);


        noSort.addActionListener(new selectDataViewActionListener(edgeOverlapPanel, nodeOverlapPanel,this));
        RankDataset1.addActionListener(new selectDataViewActionListener(edgeOverlapPanel, nodeOverlapPanel,this));

        RankOptions.add(noSort);
        RankOptions.add(RankDataset1);

        if(num_ranks == 2){
            rankView.add(RankDataset2);
            RankDataset2.addActionListener(new selectDataViewActionListener(edgeOverlapPanel, nodeOverlapPanel,this));
            RankOptions.add(RankDataset2);
        }

        return RankOptions;
    }

    public JPanel createHeatMapOptionsPanel(){

        JPanel heatmapOptions = new JPanel();
        heatmapOptions.setPreferredSize(new Dimension(200,75));
        heatmapOptions.setLayout(new GridLayout(3,1));

        JRadioButton asIs;
        JRadioButton rowNormalized;
        JRadioButton logTransform;
        ButtonGroup dataView;

         asIs = new JRadioButton("Data As Is");
         asIs.setActionCommand("asis");

         rowNormalized = new JRadioButton("Row Normalize Data");
         rowNormalized.setActionCommand("rownorm");

        logTransform = new JRadioButton("Log Transform Data");
        logTransform.setActionCommand("logtransform");

         if(rowNorm)
             rowNormalized.setSelected(true);
         else if(logtransform)
            logTransform.setSelected(true);
         else
             asIs.setSelected(true);

         dataView = new javax.swing.ButtonGroup();
         dataView.add(asIs);
         dataView.add(rowNormalized);
         dataView.add(logTransform);

        asIs.addActionListener(new selectDataViewActionListener(edgeOverlapPanel, nodeOverlapPanel,this));

        rowNormalized.addActionListener(new selectDataViewActionListener(edgeOverlapPanel, nodeOverlapPanel,this));

        logTransform.addActionListener(new selectDataViewActionListener(edgeOverlapPanel, nodeOverlapPanel,this));

        heatmapOptions.add(asIs);
        heatmapOptions.add(rowNormalized);
        heatmapOptions.add(logTransform);

        return heatmapOptions;

    }


    public ColorGradientRange getRange() {
        return range;
    }

    public void setRange(ColorGradientRange range) {
        this.range = range;
    }

    public ColorGradientTheme getTheme() {
        return theme;
    }

    public void setTheme(ColorGradientTheme theme) {
        this.theme = theme;
    }

    public boolean isRowNorm() {
        return rowNorm;
    }

    public void setRowNorm(boolean rowNorm) {
        this.rowNorm = rowNorm;
    }

    public boolean isLogtransform() {
        return logtransform;
    }

    public void setLogtransform(boolean logtransform) {
        this.logtransform = logtransform;
    }

    public boolean isRank_dataset1() {
        return rank_dataset1;
    }

    public void setRank_dataset1(boolean rank_dataset1) {
        this.rank_dataset1 = rank_dataset1;
    }

    public boolean isRank_dataset2() {
        return rank_dataset2;
    }

    public void setRank_dataset2(boolean rank_dataset2) {
        this.rank_dataset2 = rank_dataset2;
    }

    public int getNum_ranks() {
        return num_ranks;
    }

    public void setNum_ranks(int num_ranks) {
        this.num_ranks = num_ranks;
    }
}
