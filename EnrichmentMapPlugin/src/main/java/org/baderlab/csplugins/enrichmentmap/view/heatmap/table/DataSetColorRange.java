package org.baderlab.csplugins.enrichmentmap.view.heatmap.table;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpressionMatrix;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParams.Transform;
import org.mskcc.colorgradient.ColorGradientRange;
import org.mskcc.colorgradient.ColorGradientTheme;

public class DataSetColorRange {

	private final ColorGradientTheme theme;
	private final ColorGradientRange range;
	
	private DataSetColorRange(ColorGradientTheme theme, ColorGradientRange range) {
		this.theme = theme;
		this.range = range;
	}

	/**
	 * Reset color gradients based on a change in the data transformation.
	 */
	public static DataSetColorRange create(EMDataSet ds, Transform transform) {
		GeneExpressionMatrix expression = ds.getExpressionSets();
		double minExpression = expression.getMinExpression();
		double maxExpression = expression.getMaxExpression();
		
		double min;
		double max;

		switch(transform) {
			case AS_IS:
			default:
				min = minExpression;
				max = Math.max(Math.abs(minExpression), maxExpression);
				break;
			
			case ROW_NORMALIZE:
				min = expression.getMinExpression(expression.getExpressionMatrix_rowNormalized());
				max = expression.getMaxExpression(expression.getExpressionMatrix_rowNormalized());

				//if both row normalization values are zero, can't perform row normalization issue warning
				//This happens when there is only one data column in the dataset (or if it is rank file)
				if((min == 0) && (max == 0)) {
					//JOptionPane.showMessageDialog(Cytoscape.getDesktop(),"Row normalization does not work with only one data column per dataset.","Row normalization error",JOptionPane.WARNING_MESSAGE);
				}
				max = Math.max(Math.abs(min), max);
				break;

			case LOG_TRANSFORM:
				//can't take a log of a negative number, if both the max and min are negative then log tranform won't work.
				//issue a warning.
				if((minExpression <= 0) && (maxExpression <= 0)) {
					//both the max and min are probably negative values
					//JOptionPane.showMessageDialog(Cytoscape.getDesktop(),"Both the max and min expression are negative, log of negative numbers is not valid", "log normalization error", JOptionPane.WARNING_MESSAGE);
					min = 0;
					max = 0;
				}
				//if min expression is negative then use the max expression as the max
				else if(minExpression <= 0) {
					double closestToZeroExpression = expression.getClosesttoZero();
					min = Math.min(Math.log(closestToZeroExpression), Math.log1p(maxExpression));
					max = Math.max(Math.log(closestToZeroExpression), Math.log1p(maxExpression));
				}
				//if the max expression is negative then use the min expression as the max (should never happen!)
				else if(maxExpression <= 0) {
					min = 0;
					max = Math.log1p(minExpression);
				} else {
					min = Math.log1p(minExpression);
					max = Math.log1p(maxExpression);
					max = Math.max(Math.abs(min), max);
				}
				break;
		}

		if(min >= 0) {
			double median = max / 2;
			ColorGradientRange range = ColorGradientRange.getInstance(0, median, median, max, 0, median, median, max);
			ColorGradientTheme theme = ColorGradientTheme.GREEN_ONECOLOR_GRADIENT_THEME;
			return new DataSetColorRange(theme, range);
		} else {
			ColorGradientRange range = ColorGradientRange.getInstance(-max, 0, 0, max, -max, 0, 0, max);
			ColorGradientTheme theme = ColorGradientTheme.PR_GN_GRADIENT_THEME;
			return new DataSetColorRange(theme, range);
		}
	}
	
	
	public ColorGradientTheme getTheme() {
		return theme;
	}

	public ColorGradientRange getRange() {
		return range;
	}
	
	@Override
	public String toString() {
		return String.format("Range[min:%f,max:%f]", range.getMinValue(), range.getMaxValue());
	}
}
