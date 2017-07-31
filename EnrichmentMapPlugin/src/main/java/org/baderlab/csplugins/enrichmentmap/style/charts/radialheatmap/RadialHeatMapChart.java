package org.baderlab.csplugins.enrichmentmap.style.charts.radialheatmap;

import java.awt.Color;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.baderlab.csplugins.enrichmentmap.style.charts.AbstractChart;
import org.baderlab.csplugins.enrichmentmap.style.charts.Rotation;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifier;

public class RadialHeatMapChart extends AbstractChart<RadialHeatMapLayer> {

	public static final String FACTORY_ID = "org.baderlab.enrichmentmap.RadialHeatMapChart";
	public static final String DISPLAY_NAME = "Radial Heat Map Chart";
	
	public static final String START_ANGLE = "cy_startAngle";
	
	public static ImageIcon ICON;
	
	static {
		try {
			ICON = new ImageIcon(ImageIO.read(
					RadialHeatMapChart.class.getClassLoader().getResource("images/radialheatmap-chart.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public RadialHeatMapChart(final Map<String, Object> properties, final CyServiceRegistrar serviceRegistrar) {
		super(DISPLAY_NAME, properties, serviceRegistrar);
	}
	
	public RadialHeatMapChart(final RadialHeatMapChart chart, final CyServiceRegistrar serviceRegistrar) {
		super(chart, serviceRegistrar);
	}
	
	public RadialHeatMapChart(final String input, final CyServiceRegistrar serviceRegistrar) {
		super(DISPLAY_NAME, input, serviceRegistrar);
	}
	
	@Override
	public List<RadialHeatMapLayer> getLayers(final CyNetworkView networkView, final View<? extends CyIdentifiable> view) {
		final CyNetwork network = networkView.getModel();
		final CyIdentifiable model = view.getModel();
		
		final double startAngle = get(START_ANGLE, Double.class, 0.0);
		final Rotation rotation = get(ROTATION, Rotation.class, Rotation.ANTICLOCKWISE);
		final List<String> labels = getItemLabels(network, model);
		
		final Map<String, List<Double>> data = getData(network, model);
		
		final List<Color> colors = getColors(data);
		final List<Double> colorPoints = getColorPoints(data);
		final double size = 32;
		final Rectangle2D bounds = new Rectangle2D.Double(-size / 2, -size / 2, size, size);
		final boolean showLabels = get(SHOW_ITEM_LABELS, Boolean.class, false);
		final float itemFontSize = convertFontSize(get(ITEM_LABEL_FONT_SIZE, Integer.class, 1));
		final float borderWidth = get(BORDER_WIDTH, Float.class, 0.25f);
		final Color borderColor = get(BORDER_COLOR, Color.class, Color.DARK_GRAY);
		
		final boolean global = get(GLOBAL_RANGE, Boolean.class, true);
		final List<Double> range = global ? getList(RANGE, Double.class) : null;
		
		final RadialHeatMapLayer layer = new RadialHeatMapLayer(data, labels, showLabels, itemFontSize, colors,
				colorPoints, borderWidth, borderColor, startAngle, rotation, range, bounds);
		
		return Collections.singletonList(layer);
	}

	@Override
	public Image getRenderedImage() {
		return ICON.getImage();
	}
	
	@Override
	public String getId() {
		return FACTORY_ID;
	}
	
	@Override
	public Map<String, List<Double>> getDataFromColumns(final CyNetwork network, final CyIdentifiable model,
			final List<CyColumnIdentifier> columnNames) {
		final Map<String, List<Double>> data = new HashMap<>();
		
		// Values from multiple series have to be merged into one single series
		final Map<String, List<Double>> rawData = super.getDataFromColumns(network, model, columnNames);
		final List<Double> allValues = new ArrayList<>();
		
		for (final List<Double> values : rawData.values())
			allValues.addAll(values);
		
		data.put("Values", allValues);
		
		return data;
	}
	
	@Override
	public Class<?> getSettingType(final String key) {
		if (key.equalsIgnoreCase(START_ANGLE)) return Double.class;
		
		return super.getSettingType(key);
	}
}
