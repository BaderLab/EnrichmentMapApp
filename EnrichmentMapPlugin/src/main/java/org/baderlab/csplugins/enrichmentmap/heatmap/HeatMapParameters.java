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
import org.mskcc.colorgradient.ColorGradientRange;
import org.mskcc.colorgradient.ColorGradientTheme;

/**
 * Created by User: risserlin Date: Feb 11, 2009 Time: 12:23:01 PM
 * <p>
 * Parameters specific to the heat map functioning
 */
public class HeatMapParameters {

	private org.mskcc.colorgradient.ColorGradientRange range_ds1;
	private org.mskcc.colorgradient.ColorGradientTheme theme_ds1;

	//data transformation options (row normalized, as if or log transformed)
	public static enum Transformation {
		ROWNORM, ASIS, LOGTRANSFORM
	}

	//there are 5 sorting type, either by rank file or by a specific column, clustering, no order or default
	public static enum Sort {
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

	//minimum and maximum expression values used to create colour mapper - dataset 1
	//defaults to dataset1 if there is only one dataset
	private double minExpression_ds1;
	private double maxExpression_ds1;
	private double closestToZeroExpression_ds1;
	private double minExpression_rownorm_ds1;
	private double maxExpression_rownorm_ds1;

	//minimum and maximum expression values used to create colour mapper - dataset 2
	private org.mskcc.colorgradient.ColorGradientRange range_ds2;
	private org.mskcc.colorgradient.ColorGradientTheme theme_ds2;
	private double minExpression_ds2;
	private double maxExpression_ds2;
	private double closestToZeroExpression_ds2;
	private double minExpression_rownorm_ds2;
	private double maxExpression_rownorm_ds2;

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
	 * @param edgeOverlapPanel
	 *            - heatmap for edge genes overlaps
	 * @param nodeOverlapPanel
	 *            - heatmap for node genes unions
	 */
	public HeatMapParameters(HeatMapPanel edgeOverlapPanel, HeatMapPanel nodeOverlapPanel) {
		this.edgeOverlapPanel = edgeOverlapPanel;
		this.nodeOverlapPanel = nodeOverlapPanel;
		sort = Sort.DEFAULT;
		transformation = Transformation.ASIS;
	}

	/**
	 * Initialize the the color gradients based on the expression matrix
	 * associated with this set of heatmap panels (ie. both node and edge
	 * heatmap panels)
	 *
	 * @param expression
	 *            - expression matrix used for this heatmap set
	 */
	public void initColorGradients(GeneExpressionMatrix expression) {

		minExpression_ds1 = expression.getMinExpression();
		maxExpression_ds1 = expression.getMaxExpression();
		closestToZeroExpression_ds1 = expression.getClosesttoZero();
		minExpression_rownorm_ds1 = expression.getMinExpression(expression.getExpressionMatrix_rowNormalized());
		maxExpression_rownorm_ds1 = expression.getMaxExpression(expression.getExpressionMatrix_rowNormalized());

		double max = Math.max(Math.abs(minExpression_ds1), maxExpression_ds1);

		double median = 0;

		//if the minimum expression is above zero make it a one colour heatmap
		if(minExpression_ds1 >= 0) {
			range_ds1 = ColorGradientRange.getInstance(0, max / 2, max / 2, max, 0, max / 2, max / 2, max);
			theme_ds1 = ColorGradientTheme.GREEN_ONECOLOR_GRADIENT_THEME;
		} else {
			range_ds1 = ColorGradientRange.getInstance(-max, median, median, max, -max, median, median, max);
			theme_ds1 = ColorGradientTheme.GREEN_MAGENTA_GRADIENT_THEME;
		}

	}

	/**
	 * Initialize the the color gradients based on the expression matrix for
	 * both datasets associated with this set of heatmap panels (ie. both node
	 * and edge heatmap panels)
	 *
	 * @param expression
	 *            - expression matrix used for this heatmap set
	 */
	public void initColorGradients(GeneExpressionMatrix expression_ds1, GeneExpressionMatrix expression_ds2) {

		minExpression_ds1 = expression_ds1.getMinExpression();
		maxExpression_ds1 = expression_ds1.getMaxExpression();
		closestToZeroExpression_ds1 = expression_ds1.getClosesttoZero();
		minExpression_rownorm_ds1 = expression_ds1.getMinExpression(expression_ds1.getExpressionMatrix_rowNormalized());
		maxExpression_rownorm_ds1 = expression_ds1.getMaxExpression(expression_ds1.getExpressionMatrix_rowNormalized());

		double max = Math.max(Math.abs(minExpression_ds1), maxExpression_ds1);

		double median = 0;

		//if the minimum expression is above zero make it a one colour heatmap
		if(minExpression_ds1 >= 0) {
			range_ds1 = ColorGradientRange.getInstance(0, max / 2, max / 2, max, 0, max / 2, max / 2, max);
			theme_ds1 = ColorGradientTheme.GREEN_ONECOLOR_GRADIENT_THEME;
		} else {
			range_ds1 = ColorGradientRange.getInstance(-max, median, median, max, -max, median, median, max);
			theme_ds1 = ColorGradientTheme.GREEN_MAGENTA_GRADIENT_THEME;
		}

		//Do the same for dataset 2
		minExpression_ds2 = expression_ds2.getMinExpression();
		maxExpression_ds2 = expression_ds2.getMaxExpression();
		closestToZeroExpression_ds2 = expression_ds2.getClosesttoZero();
		minExpression_rownorm_ds2 = expression_ds2.getMinExpression(expression_ds2.getExpressionMatrix_rowNormalized());
		maxExpression_rownorm_ds2 = expression_ds2.getMaxExpression(expression_ds2.getExpressionMatrix_rowNormalized());

		max = Math.max(Math.abs(minExpression_ds2), maxExpression_ds2);

		median = 0;

		//if the minimum expression is above zero make it a one colour heatmap
		if(minExpression_ds2 >= 0) {
			range_ds2 = ColorGradientRange.getInstance(0, max / 2, max / 2, max, 0, max / 2, max / 2, max);
			theme_ds2 = ColorGradientTheme.GREEN_ONECOLOR_GRADIENT_THEME;
		} else {
			range_ds2 = ColorGradientRange.getInstance(-max, median, median, max, -max, median, median, max);
			theme_ds2 = ColorGradientTheme.GREEN_MAGENTA_GRADIENT_THEME;
		}

	}

	/**
	 * Reset color gradients based on a change in the data transformation.
	 */
	public void ResetColorGradient_ds1() {
		double min;
		double max;
		double median;

		switch(transformation) {
			case ROWNORM:
				min = minExpression_rownorm_ds1;
				max = maxExpression_rownorm_ds1;

				//if both row normalization values are zero, can't perform row normalization
				//issue warning
				//This happens when there is only one data column in the dataset (or if it is rank file)
				if((min == 0) && (max == 0)) {
					//JOptionPane.showMessageDialog(Cytoscape.getDesktop(),"Row normalization does not work with only one data column per dataset.","Row normalization error",JOptionPane.WARNING_MESSAGE);
				}
				max = Math.max(Math.abs(min), max);
				break;

			case LOGTRANSFORM:

				//can't take a log of a negative number
				//if both the max and min are negative then log tranform won't work.
				//issue a warning.
				if((minExpression_ds1 <= 0) && (maxExpression_ds1 <= 0)) {
					//both the max and min are probably negative values
					//JOptionPane.showMessageDialog(Cytoscape.getDesktop(),"Both the max and min expression are negative, log of negative numbers is not valid", "log normalization error", JOptionPane.WARNING_MESSAGE);
					min = 0;
					max = 0;
				}
				//if min expression is negative then use the max expression as the max
				else if(minExpression_ds1 <= 0) {
					min = Math.min(Math.log(closestToZeroExpression_ds1), Math.log1p(maxExpression_ds1));
					max = Math.max(Math.log(closestToZeroExpression_ds1), Math.log1p(maxExpression_ds1));
				}
				//if the max expression is negative then use the min expression as the max (should never happen!)
				else if(maxExpression_ds1 <= 0) {
					min = 0;
					max = Math.log1p(minExpression_ds1);
				} else {
					min = Math.log1p(minExpression_ds1);
					max = Math.log1p(maxExpression_ds1);
					max = Math.max(Math.abs(min), max);
				}

				break;

			case ASIS:
			default:
				min = minExpression_ds1;
				max = Math.max(Math.abs(minExpression_ds1), maxExpression_ds1);
				break;
		}

		median = max / 2;
		if(min >= 0) {
			median = max / 2;
			range_ds1 = ColorGradientRange.getInstance(0, median, median, max, 0, median, median, max);
			theme_ds1 = ColorGradientTheme.GREEN_ONECOLOR_GRADIENT_THEME;
		} else {
			median = 0;
			range_ds1 = ColorGradientRange.getInstance(-max, 0, 0, max, -max, 0, 0, max);
			theme_ds1 = ColorGradientTheme.GREEN_MAGENTA_GRADIENT_THEME;
		}

	}

	/**
	 * Reset color gradients based on a change in the data transformation.
	 */
	public void ResetColorGradient_ds2() {
		double min;
		double max;
		double median;

		switch(transformation) {
			case ROWNORM:
				min = minExpression_rownorm_ds2;
				max = maxExpression_rownorm_ds2;

				//if both row normalization values are zero, can't perform row normalization
				//issue warning
				//This happens when there is only one data column in the dataset (or if it is rank file)
				if((min == 0) && (max == 0)) {
					//JOptionPane.showMessageDialog(Cytoscape.getDesktop(),"Row normalization does not work with only one data column per dataset.","Row normalization error",JOptionPane.WARNING_MESSAGE);
				}
				max = Math.max(Math.abs(min), max);
				break;

			case LOGTRANSFORM:

				//can't take a log of a negative number
				//if both the max and min are negative then log tranform won't work.
				//issue a warning.
				if((minExpression_ds2 <= 0) && (maxExpression_ds2 <= 0)) {
					//both the max and min are probably negative values
					//JOptionPane.showMessageDialog(Cytoscape.getDesktop(),"Both the max and min expression are negative, log of negative numbers is not valid", "log normalization error", JOptionPane.WARNING_MESSAGE);
					min = 0;
					max = 0;
				}
				//if min expression is negative then use the max expression as the max
				else if(minExpression_ds2 <= 0) {
					min = Math.min(Math.log(closestToZeroExpression_ds2), Math.log1p(maxExpression_ds2));
					max = Math.max(Math.log(closestToZeroExpression_ds2), Math.log1p(maxExpression_ds2));
				}
				//if the max expression is negative then use the min expression as the max (should never happen!)
				else if(maxExpression_ds2 <= 0) {
					min = 0;
					max = Math.log1p(minExpression_ds2);
				} else {
					min = Math.log1p(minExpression_ds2);
					max = Math.log1p(maxExpression_ds2);
					max = Math.max(Math.abs(min), max);
				}

				break;

			case ASIS:
			default:
				min = minExpression_ds2;
				max = Math.max(Math.abs(minExpression_ds2), maxExpression_ds2);
				break;
		}

		median = max / 2;
		if(min >= 0) {
			median = max / 2;
			range_ds2 = ColorGradientRange.getInstance(0, median, median, max, 0, median, median, max);
			theme_ds2 = ColorGradientTheme.GREEN_ONECOLOR_GRADIENT_THEME;
		} else {
			median = 0;
			range_ds2 = ColorGradientRange.getInstance(-max, 0, 0, max, -max, 0, 0, max);
			theme_ds2 = ColorGradientTheme.GREEN_MAGENTA_GRADIENT_THEME;
		}

	}

	/**
	 * If data is sorted by a column update the sort by combo box to contain the
	 * column name sorted by
	 */
	/*
	 * public void changeSortComboBoxToColumnSorted(){
	 * rankOptionComboBox.addItem(sort_column + ":" + sortbycolumnName);
	 * rankOptionComboBox.setSelectedItem(sort_column + ":" + sortbycolumnName);
	 * 
	 * }
	 */

	//Getters and Setters.
	public HeatMapPanel getEdgeOverlapPanel() {
		return edgeOverlapPanel;
	}

	public HeatMapPanel getNodeOverlapPanel() {
		return nodeOverlapPanel;
	}

	public ColorGradientRange getRange_ds1() {
		return range_ds1;
	}

	public void setRange_ds1(ColorGradientRange range) {
		this.range_ds1 = range;
	}

	public ColorGradientTheme getTheme_ds1() {
		return theme_ds1;
	}

	public void setTheme_ds1(ColorGradientTheme theme) {
		this.theme_ds1 = theme;
	}

	public ColorGradientRange getRange_ds2() {
		return range_ds2;
	}

	public void setRange_ds2(ColorGradientRange range) {
		this.range_ds2 = range;
	}

	public ColorGradientTheme getTheme_ds2() {
		return theme_ds2;
	}

	public void setTheme_ds2(ColorGradientTheme theme) {
		this.theme_ds2 = theme;
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

	public boolean isAscending(int index) {
		if(index == -1)
			return true;
		return ascending[index];
	}

	public void flipAscending(int index) {
		this.ascending[index] = !ascending[index];

		//reset the panel
		/*
		 * edgeOverlapPanel.updatePanel(); nodeOverlapPanel.updatePanel();
		 */
	}

	public void changeAscendingValue(int index) {
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
