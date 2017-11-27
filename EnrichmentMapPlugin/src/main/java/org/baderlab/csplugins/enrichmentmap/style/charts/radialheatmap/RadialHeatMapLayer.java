package org.baderlab.csplugins.enrichmentmap.style.charts.radialheatmap;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.baderlab.csplugins.enrichmentmap.style.charts.AbstractChartLayer;
import org.baderlab.csplugins.enrichmentmap.style.charts.CustomPieSectionLabelGenerator;
import org.baderlab.csplugins.enrichmentmap.style.charts.LabelPosition;
import org.baderlab.csplugins.enrichmentmap.style.charts.Rotation;
import org.baderlab.csplugins.enrichmentmap.view.util.ColorUtil;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.PieDataset;
import org.jfree.ui.RectangleInsets;

public class RadialHeatMapLayer extends AbstractChartLayer<PieDataset> {
	
	/** Just to prevent the circle's border from being cropped */
	public static final double INTERIOR_GAP = 0.004;
	
	private final Map<String, String> labels;
	private final double startAngle;
	private final Rotation rotation;

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public RadialHeatMapLayer(
			final Map<String, List<Double>> data,
			final List<String> itemLabels,
			final boolean showLabels,
			final float itemFontSize,
			final List<Color> colors,
			final List<Double> colorPoints,
			final float borderWidth,
			final Color borderColor,
			final double startAngle,
			final Rotation rotation,
			final List<Double> range,
			final Rectangle2D bounds
	) {
		super(data, itemLabels, null, null, showLabels, false, false, itemFontSize, LabelPosition.STANDARD, colors,
				colorPoints, 0.0f, TRANSPARENT_COLOR, 0.0f, borderWidth, borderColor, range, bounds);
		this.startAngle = startAngle;
		this.rotation = rotation;
		this.labels = new HashMap<>();
        
        // Range cannot be null
        if (this.range == null)
        	this.range = calculateRange(data.values(), false);
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	@Override
	protected PieDataset createDataset() {
		List<Double> values = data.isEmpty() ? null : data.values().iterator().next();
		
		// All the slices must have the same size
		Double[] equalValues = values.isEmpty() ? null : new Double[values.size()];
		
		if (equalValues != null)
			Arrays.fill(equalValues, new Double(1));
		
		final PieDataset dataset = createPieDataset(equalValues != null ? Arrays.asList(equalValues) : null);
		
		if (showItemLabels && itemLabels != null) {
			final List<?> keys = dataset.getKeys();
			
			for (int i = 0; i < keys.size(); i++) {
				String k = (String) keys.get(i);
				String label = itemLabels.size() > i ? itemLabels.get(i) : null;
				
				if (label == null && values.size() > i)
					label = "" + values.get(i);
				
				labels.put(k, label);
			}
        }
		
		return dataset;
	}
    
	@Override
	protected JFreeChart createChart(final PieDataset dataset) {
		JFreeChart chart = ChartFactory.createPieChart(
				null, // chart title
				dataset, // data
				false, // include legend
				false, // tooltips
				false); // urls
		
        chart.setAntiAlias(true);
        chart.setBorderVisible(false);
        chart.setBackgroundPaint(TRANSPARENT_COLOR);
        chart.setBackgroundImageAlpha(0.0f);
        chart.setPadding(new RectangleInsets(0.0, 0.0, 0.0, 0.0));
        
		final PiePlot plot = (PiePlot) chart.getPlot();
		plot.setCircular(true);
		plot.setStartAngle(startAngle);
		plot.setDirection(rotation == Rotation.ANTICLOCKWISE ?
				org.jfree.util.Rotation.ANTICLOCKWISE : org.jfree.util.Rotation.CLOCKWISE);
		plot.setOutlineVisible(false);
		plot.setInsets(new RectangleInsets(0.0, 0.0, 0.0, 0.0));
		plot.setInteriorGap(INTERIOR_GAP);
		plot.setBackgroundPaint(TRANSPARENT_COLOR);
		plot.setBackgroundAlpha(0.0f);
		plot.setShadowPaint(TRANSPARENT_COLOR);
		plot.setShadowXOffset(0.0);
		plot.setShadowYOffset(0.0);
		plot.setLabelGenerator(showItemLabels ? new CustomPieSectionLabelGenerator(labels) : null);
		plot.setSimpleLabels(true);
		plot.setLabelFont(plot.getLabelFont().deriveFont(itemFontSize));
		plot.setLabelBackgroundPaint(TRANSPARENT_COLOR);
		plot.setLabelOutlinePaint(TRANSPARENT_COLOR);
		plot.setLabelShadowPaint(TRANSPARENT_COLOR);
		plot.setLabelPaint(labelColor);
		
		final BasicStroke stroke = new BasicStroke(borderWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		
		Color upperColor = Color.BLUE;
		Color zeroColor = Color.WHITE;
		Color lowerColor = Color.RED;
		Color nanColor = TRANSPARENT_COLOR;
		
		if (colorPoints.isEmpty() || colorPoints.size() != colors.size()) {
			if (range != null && range.size() >= 2 && range.get(0) != null && range.get(1) != null) {
				final int colorsSize = colors != null ? colors.size() : 0;
				
				if (colorsSize > 0) upperColor = colors.get(0);
				if (colorsSize > 1) zeroColor = colors.get(1);
				if (colorsSize > 2) lowerColor = colors.get(2);
				if (colorsSize > 3) nanColor = colors.get(3);
			}
		}
		
		final List<?> keys = dataset.getKeys();
		final List<Double> values = data.isEmpty() ? null : data.values().iterator().next();
		
		for (int i = 0; i < keys.size(); i++) {
			String k = (String) keys.get(i);
			Double v =  values.size() > i ? values.get(i) : null;
			final Color c;
			
			if (v == null || !Double.isFinite(v)) {
				c = nanColor;
			} else {
				if (colorPoints.isEmpty() || colorPoints.size() != colors.size())
					c = ColorUtil.getColor(v, range.get(0), range.get(1), lowerColor, zeroColor, upperColor);
				else
					c = ColorUtil.getColor(v, colors, colorPoints);
			}
			
			plot.setSectionPaint(k, c);
			plot.setSectionOutlinePaint(k, borderWidth > 0 ? borderColor : TRANSPARENT_COLOR);
			plot.setSectionOutlineStroke(k, stroke);
		}
		
		return chart;
	}
}
