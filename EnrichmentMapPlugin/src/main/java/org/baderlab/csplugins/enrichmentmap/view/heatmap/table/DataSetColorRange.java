package org.baderlab.csplugins.enrichmentmap.view.heatmap.table;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.baderlab.csplugins.enrichmentmap.model.GeneExpressionMatrix;
import org.baderlab.csplugins.enrichmentmap.model.Transform;
import org.baderlab.csplugins.enrichmentmap.view.util.ColorUtil;
import org.baderlab.csplugins.org.mskcc.colorgradient.ColorGradientRange;
import org.baderlab.csplugins.org.mskcc.colorgradient.ColorGradientTheme;

public class DataSetColorRange {

	private final ColorGradientTheme theme;
	private final ColorGradientRange range;
	
	private List<Double> points;
	private List<Color>  colors;
	
	
	private DataSetColorRange(ColorGradientTheme theme, ColorGradientRange range) {
		this.theme = theme;
		this.range = range;
	}

	/**
	 * Reset color gradients based on a change in the data transformation.
	 */
	public static Optional<DataSetColorRange> create(GeneExpressionMatrix expression, Transform transform) {
		float[] minMax = expression.getMinMax(transform);
		if(minMax == null || minMax.length < 2)
			return Optional.empty();
		
		float min = minMax[0];
		float max = minMax[1];

		if(!Double.isFinite(min) || !Double.isFinite(max) || (min == 0 && max == 0)) {
			return Optional.empty();
		}
		
		if(min >= 0) {
			double median = max / 2;
			ColorGradientRange range = ColorGradientRange.getInstance(0, median, median, max, 0, median, median, max);
			ColorGradientTheme theme = ColorGradientTheme.GREEN_ONECOLOR_GRADIENT_THEME;
			return Optional.of(new DataSetColorRange(theme, range));
		} else if(Math.abs(min) > max) {
			min = Math.abs(min);
			ColorGradientRange range = ColorGradientRange.getInstance(-min, 0, 0, min, -min, 0, 0, min);
			ColorGradientTheme theme = ColorGradientTheme.PR_GN_GRADIENT_THEME;
			return Optional.of(new DataSetColorRange(theme, range));
		} else {
			ColorGradientRange range = ColorGradientRange.getInstance(-max, 0, 0, max, -max, 0, 0, max);
			ColorGradientTheme theme = ColorGradientTheme.PR_GN_GRADIENT_THEME;
			return Optional.of(new DataSetColorRange(theme, range));
		}
	}
	
	public ColorGradientTheme getTheme() {
		return theme;
	}

	public ColorGradientRange getRange() {
		return range;
	}
	
	
	public Color getColor(Double value) {
		if(value == null || !Double.isFinite(value))
			return theme.getNoDataColor();
		
		if(points == null)
			points = Arrays.asList(range.getMaxValue(), range.getCenterHighValue(), range.getMinValue());
		if(colors == null)
			colors = Arrays.asList(theme.getMaxColor(), theme.getCenterColor(),     theme.getMinColor());
		
		return ColorUtil.getColor(value, colors, points);
	}
	
	
	/**
	 * Returns the (max, center, min) colors. 
	 * Compatible with the "cy_colors" property of the RadialHeatMap.
	 */
	public List<Color> getColors() {
		if(colors == null)
			colors = Arrays.asList(theme.getMaxColor(), theme.getCenterColor(), theme.getMinColor());
		return colors;
	}
	
	/**
	 * Returns the (max, center, min) points of the range.
	 * Compatible with the "cy_colorPoints" property of the RadialHeatMap.
	 */
	public List<Double> getPoints() {
		if(points == null)
			points = Arrays.asList(range.getMaxValue(), range.getCenterHighValue(), range.getMinValue());
		return points;
	}
	
	/**
	 * Returns the range bounds as a two-element List.
	 * Compatible with the "cy_range" property of the RadialHeatMap.
	 */
	public List<Double> getRangeMinMax() {
		return Arrays.asList(range.getMinValue(), range.getMaxValue());
	}
	
	
	@Override
	public String toString() {
		return String.format("Range[min:%f,max:%f]", range.getMinValue(), range.getMaxValue());
	}
}
