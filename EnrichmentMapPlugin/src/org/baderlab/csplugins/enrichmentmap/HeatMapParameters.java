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
import javax.swing.border.TitledBorder;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import prefuse.data.query.NumberRangeModel;
import org.mskcc.colorgradient.* ;

import cytoscape.Cytoscape;
import cytoscape.view.CytoscapeDesktop;
import cytoscape.view.cytopanels.CytoPanel;

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
      private boolean asIS= false;
      private boolean logtransform = false;

      private int num_ranks = 0;
      private boolean noSort=false;

     //there are two sorting type, either by rank file or by a specific column
     private boolean sortbyrank = false;
     private boolean sortbycolumn = false;

     private String sortbycolumnName;

    //tag to indicate if this was a column click or if this was a normalization click.
     private boolean sortbycolumn_event_triggered = false;

     //store the index of the rank file or the column that we are sorting by
     private int sortIndex = 0;

      private double minExpression;
      private double maxExpression;
      private double minExpression_rownorm;
      private double maxExpression_rownorm;
     
      private JPanel heatmapOptions;
      private JPanel RankOptions;
      private JComboBox hmOptionComboBox;
      private JComboBox rankOptionComboBox;

     private OverlappingGenesPanel edgeOverlapPanel;
     private OverlappingGenesPanel nodeOverlapPanel;
     private boolean dataset1=false;
     private boolean dataset2=false;
     
  
    public HeatMapParameters(OverlappingGenesPanel edgeOverlapPanel, OverlappingGenesPanel nodeOverlapPanel) {
        this.edgeOverlapPanel = edgeOverlapPanel;
        this.nodeOverlapPanel = nodeOverlapPanel;
    }
    
    public OverlappingGenesPanel getEdgeOverlapPanel(){
    	
    	return edgeOverlapPanel;
    }
public OverlappingGenesPanel getNodeOverlapPanel(){
    	
    	return nodeOverlapPanel;
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

              //if both row normalization values are zero, can't perform row normalization
              //issue warning
              //This happens when there is only one data column in the dataset (or if it is rank file)
              if((min == 0) && (max == 0)){
                   JOptionPane.showMessageDialog(Cytoscape.getDesktop(),"Row normalization does not work with only one data column per dataset.","Row normalization error",JOptionPane.WARNING_MESSAGE);
              }
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
// method to create Rank combo box
    public JPanel createRankOptionsPanel(){
    	 TitledBorder RankBorder = BorderFactory.createTitledBorder("Sorting");
    	 RankBorder.setTitleJustification(TitledBorder.LEFT);
    	RankOptions 		= new JPanel();
    	rankOptionComboBox	= new JComboBox();
    	rankOptionComboBox.addItem("No Sort");
        
      // set the appropriate choices in rank combo box
    	if(num_ranks==1){   
    		if(isDataset1()){
            rankOptionComboBox.addItem("Sort By Rank File Dataset 1");                    
          }
    		else if(isDataset2()){
            rankOptionComboBox.addItem("Sort By Rank File Dataset 2");                    
          }
	}
        if ( num_ranks==2){     
        	rankOptionComboBox.addItem("Sort By Rank File Dataset 1");
            rankOptionComboBox.addItem("Sort By Rank File Dataset 2");        	
        }
        
        
        
        // set the selection in the rank combo box
        if(this.noSort){
    		rankOptionComboBox.setSelectedItem("No Sort");            	
        }
    	else if(this.sortbyrank){
            if(this.sortIndex == 1)
                rankOptionComboBox.setSelectedItem("Sort By Rank File Dataset 1");
            else if (this.sortIndex == 2)
                rankOptionComboBox.setSelectedItem("Sort By Rank File Dataset 2");
         }
    	 else if(this.sortbycolumn){
            //int columnNumber = this.sortIndex + 1;
            rankOptionComboBox.addItem("Column: " + sortbycolumnName);
            rankOptionComboBox.setSelectedItem("Column: " + sortbycolumnName);
         }
        
        rankOptionComboBox.addActionListener(new selectDataViewActionListener(edgeOverlapPanel, nodeOverlapPanel,rankOptionComboBox,this));
        RankOptions.add(rankOptionComboBox);
        RankOptions.setBorder(RankBorder);
        return RankOptions;
    }
    

   // creates heat map combobox
    public JPanel createHeatMapOptionsPanel(){
    	 TitledBorder HMBorder = BorderFactory.createTitledBorder("Normalization");
    	 HMBorder.setTitleJustification(TitledBorder.LEFT);
    	heatmapOptions   = new JPanel();
    	hmOptionComboBox = new JComboBox();
        hmOptionComboBox.addItem("Data As Is");
        hmOptionComboBox.addItem("Row Normalize Data");
        hmOptionComboBox.addItem("Log Transform Data"); 
        
        // set the selection in the heatmap combobox
        if(this.isAsIS()){
        	hmOptionComboBox.setSelectedItem("Data As Is");
        }
        else if(this.isRowNorm()){
        	hmOptionComboBox.setSelectedItem("Row Normalize Data");
        }
        else if(this.isLogtransform()){
        	hmOptionComboBox.setSelectedItem("Log Transform Data");
        }
        
        hmOptionComboBox.addActionListener(new selectDataViewActionListener(edgeOverlapPanel, nodeOverlapPanel,hmOptionComboBox,this));
        heatmapOptions.add(hmOptionComboBox);
        heatmapOptions.setBorder(HMBorder);
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

    public int getNum_ranks() {
        return num_ranks;
    }

    public void setNum_ranks(int num_ranks) {
        this.num_ranks = num_ranks;
    }

	public void setAsIS(boolean asIS) {
		this.asIS = asIS;
	}

	public boolean isAsIS() {
		return asIS;
	}
	public boolean isDataset1() {
		return dataset1;
	}

	public void setDataset1(boolean dataset1) {
		this.dataset1 = dataset1;
	}

	public boolean isDataset2() {
		return dataset2;
	}

	public void setDataset2(boolean dataset2) {
		this.dataset2 = dataset2;
	}

	public boolean isNoSort() {
		return noSort;
	}
	public void setNoSort(boolean nosort) {
		this.noSort=nosort;
	}

    public boolean isSortbyrank() {
        return sortbyrank;
    }

    public void setSortbyrank(boolean sortbyrank) {
        this.sortbyrank = sortbyrank;
    }

    public boolean isSortbycolumn() {
        return sortbycolumn;
    }

    public void setSortbycolumn(boolean sortbycolumn) {
        this.sortbycolumn = sortbycolumn;
    }

    public int getSortIndex() {
        return sortIndex;
    }

    public void setSortIndex(int sortIndex) {
        this.sortIndex = sortIndex;
    }

    public void changeRankComboBoxToColumnSorted(){
        int columnNumber = this.sortIndex + 1;
        rankOptionComboBox.addItem("Column: " + sortbycolumnName);
        rankOptionComboBox.setSelectedItem("Column: " + sortbycolumnName);

    }

    public boolean isSortbycolumn_event_triggered() {
        return sortbycolumn_event_triggered;
    }

    public void setSortbycolumn_event_triggered(boolean sortbycolumn_event_triggered) {
        this.sortbycolumn_event_triggered = sortbycolumn_event_triggered;
    }

    public String getSortbycolumnName() {
        return sortbycolumnName;
    }

    public void setSortbycolumnName(String sortbycolumnName) {
        this.sortbycolumnName = sortbycolumnName;
    }
}
