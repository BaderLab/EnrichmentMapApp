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

package org.baderlab.csplugins.enrichmentmap.heatmap;

import org.baderlab.csplugins.enrichmentmap.model.GeneExpressionMatrix;
import org.baderlab.csplugins.enrichmentmap.view.HeatMapPanel;
import org.mskcc.colorgradient.* ;



/**
 * Created by
 * User: risserlin
 * Date: Feb 11, 2009
 * Time: 12:23:01 PM
 * <p>
 * Parameters specific to the heat map functioning
 */
public class HeatMapParameters {

    private org.mskcc.colorgradient.ColorGradientRange range;
    private org.mskcc.colorgradient.ColorGradientTheme theme;
    

    //data transformation options (row normalized, as if or log transformed)
    public static enum Transformation{
        ROWNORM, ASIS, LOGTRANSFORM
    }

    //there are 5 sorting type, either by rank file or by a specific column, clustering, no order or default
    public  static enum Sort{
        RANK, COLUMN, CLUSTER, NONE, DEFAULT
    }

    Sort sort;
    public Transformation transformation;

    //name of column currently sorted by
    private String sortbycolumnName;

    //tag to indicate if this was a column click or if this was a normalization click.
    private boolean sortbycolumn_event_triggered = false;

    //store the index of the column that we are sorting by
    private int sortIndex = -1;

    //store the name of the rank file sorted by
    private String rankFileIndex = "none";

    //store the state (ascending or descending) of each column in the table as well as each of the rank files.
    private boolean[] ascending;
   
    //switch to turn off the coloring of the heatmap
    private boolean showValues = false;

    //minimum and maximum expression values used to create colour mapper
    private double minExpression;
    private double maxExpression;
    private double closestToZeroExpression;
    private double minExpression_rownorm;
    private double maxExpression_rownorm;
    
    //pointer to panels containing the heatmaps.
    private HeatMapPanel edgeOverlapPanel;
    private HeatMapPanel nodeOverlapPanel;

    public static String sort_hierarchical_cluster = "Hierarchical Cluster";
    public static String sort_rank = "Ranks";
    public static String sort_column = "Columns";
    public static String sort_none = "No Sort";

    public static String pearson_correlation = "Pearson Correlation";
    public static String cosine = "Cosine Distance";
    public static String euclidean = "Euclidean Distance";
    
    public static String asis = "Data As Is";
    public static String rownorm = "Row Normalize Data";
    public static String logtrans = "Log Transform Data";

    /**
     * Class constructor -
     *
     * @param edgeOverlapPanel - heatmap for edge genes overlaps
     * @param nodeOverlapPanel - heatmap for node genes unions
     */
    public HeatMapParameters(HeatMapPanel edgeOverlapPanel, HeatMapPanel nodeOverlapPanel) {
        this.edgeOverlapPanel = edgeOverlapPanel;
        this.nodeOverlapPanel = nodeOverlapPanel;
        sort = Sort.DEFAULT;
        transformation = Transformation.ASIS;
    }

    /**
     * Initialize the the color gradients based on the expression matrix
     * associated with this set of heatmap panels (ie. both node and edge heatmap panels)
     *
     * @param expression - expression matrix used for this heatmap set
     */
    public void initColorGradients(GeneExpressionMatrix expression){

        minExpression = expression.getMinExpression();
        maxExpression = expression.getMaxExpression();
        closestToZeroExpression = expression.getClosesttoZero();
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

    /**
     * Reset color gradients based on a change in the data transformation.
     */
    public void ResetColorGradient(){
          double min;
          double max;
          double median;

          switch(transformation){
              case ROWNORM:
                min = minExpression_rownorm;
                max = maxExpression_rownorm;

                //if both row normalization values are zero, can't perform row normalization
                //issue warning
                //This happens when there is only one data column in the dataset (or if it is rank file)
                if((min == 0) && (max == 0)){
                   //JOptionPane.showMessageDialog(Cytoscape.getDesktop(),"Row normalization does not work with only one data column per dataset.","Row normalization error",JOptionPane.WARNING_MESSAGE);
                }
                max = Math.max(Math.abs(min),max);
                break;

               case LOGTRANSFORM:

                    //can't take a log of a negative number
                   //if both the max and min are negative then log tranform won't work.
                   //issue a warning.
                    if((minExpression <= 0) && (maxExpression <= 0) ){
                        //both the max and min are probably negative values
                        //JOptionPane.showMessageDialog(Cytoscape.getDesktop(),"Both the max and min expression are negative, log of negative numbers is not valid", "log normalization error", JOptionPane.WARNING_MESSAGE);
                        min = 0;
                        max = 0;
                    }
                    //if min expression is negative then use the max expression as the max
                    else if(minExpression <= 0){
                        min =  Math.min(Math.log(closestToZeroExpression), Math.log1p(maxExpression));
                        max =  Math.max(Math.log(closestToZeroExpression), Math.log1p(maxExpression));
                    }
                    //if the max expression is negative then use the min expression as the max (should never happen!)
                    else if(maxExpression <= 0){
                        min = 0;
                        max = Math.log1p(minExpression);
                    }
                   else{
                        min = Math.log1p(minExpression);
                        max = Math.log1p(maxExpression) ;
                        max = Math.max(Math.abs(min),max);
                   }

                   break;

               case ASIS:
                default:
                    min = minExpression;
                    max = Math.max(Math.abs(minExpression), maxExpression);
                    break;
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

   


   
  
    /**
     * If data is sorted by a column update the sort by combo box to contain the column name sorted by
     */
/*    public void changeSortComboBoxToColumnSorted(){
          rankOptionComboBox.addItem(sort_column + ":" + sortbycolumnName);
          rankOptionComboBox.setSelectedItem(sort_column + ":" + sortbycolumnName);

      }
*/

    //Getters and Setters.
     public HeatMapPanel getEdgeOverlapPanel(){
    	return edgeOverlapPanel;
    }

    public HeatMapPanel getNodeOverlapPanel(){
    	return nodeOverlapPanel;
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

    public Transformation getTransformation() {
        return transformation;
    }

    public void setTransformation(Transformation transformation) {
        this.transformation = transformation;
    }

    public Sort getSort() {
        return sort;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }

    public int getSortIndex() {
        return sortIndex;
    }

    public void setSortIndex(int sortIndex) {
        this.sortIndex = sortIndex;
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

    public String getRankFileIndex() {
        return rankFileIndex;
    }

    public void setRankFileIndex(String rankFileIndex) {
        this.rankFileIndex = rankFileIndex;
    }

    public boolean[] getAscending() {
        return ascending;
    }

    public void setAscending(boolean[] ascending) {
        this.ascending = ascending;
    }

    public boolean isAscending(int index){
        if(index == -1)
            return true;
        return ascending[index];
    }

    public void flipAscending(int index){
        this.ascending[index] = !ascending[index];

        //reset the panel
        /*edgeOverlapPanel.updatePanel();
        nodeOverlapPanel.updatePanel();*/
    }

    public void changeAscendingValue(int index){
        if(index != -1)
            this.ascending[index] = !ascending[index];
    }

	public boolean isShowValues() {
		return showValues;
	}

	public void setShowValues(boolean showvalues) {
		this.showValues = showvalues;
	}

}
