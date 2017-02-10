package org.baderlab.csplugins.enrichmentmap.view.expression;

import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpressionMatrix;
import org.mskcc.colorgradient.ColorGradientRange;
import org.mskcc.colorgradient.ColorGradientTheme;

public class ColorRange {

	private final ColorGradientTheme theme;
	private final ColorGradientRange range;
	
	
	public ColorRange(DataSet ds) {
		GeneExpressionMatrix expression = ds.getExpressionSets();
		
		double min = expression.getMinExpression();
		double max = Math.max(Math.abs(min), expression.getMaxExpression());
		double median = 0;

		//if the minimum expression is above zero make it a one colour heatmap
		if(min >= 0) {
			range = ColorGradientRange.getInstance(0, max / 2, max / 2, max, 0, max / 2, max / 2, max);
			theme = ColorGradientTheme.GREEN_ONECOLOR_GRADIENT_THEME;
		} else {
			range = ColorGradientRange.getInstance(-max, median, median, max, -max, median, median, max);
			theme = ColorGradientTheme.PR_GN_GRADIENT_THEME;
		}
	}

	
	public ColorGradientTheme getTheme() {
		return theme;
	}

	public ColorGradientRange getRange() {
		return range;
	}
	
}
