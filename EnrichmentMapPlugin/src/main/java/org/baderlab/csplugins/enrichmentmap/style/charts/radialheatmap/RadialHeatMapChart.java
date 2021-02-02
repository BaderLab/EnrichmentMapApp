package org.baderlab.csplugins.enrichmentmap.style.charts.radialheatmap;

import java.awt.Color;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.baderlab.csplugins.enrichmentmap.style.charts.AbstractChart;
import org.baderlab.csplugins.enrichmentmap.style.charts.Rotation;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifier;

public class RadialHeatMapChart extends AbstractChart<RadialHeatMapLayer> {

	public static final String FACTORY_ID = "org.baderlab.enrichmentmap.RadialHeatMapChart";
	public static final String DISPLAY_NAME = "Radial Heat Map Chart";
	
	public static final String P_VALUE_COLS = "cy_p_value_cols";
	public static final String Q_VALUE_COLS = "cy_q_value_cols";
	public static final String P_VALUE = "cy_p_value";
	public static final String Q_VALUE = "cy_q_value";
	
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
	public Map<String, List<Double>> getDataFromColumns(CyNetwork network, CyIdentifiable model, List<CyColumnIdentifier> columnNames) {
		List<CyColumnIdentifier> pValueCols = get(P_VALUE_COLS, List.class);
		List<CyColumnIdentifier> qValueCols = get(Q_VALUE_COLS, List.class);
		Double pFilter = get(P_VALUE, Double.class);
		Double qFilter = get(Q_VALUE, Double.class);
		
		if(pValueCols == null || pFilter == null) {
			// do it exactly like it was done before, for backwards compatibility
			return getDataFromColumnsNormal(network, model, columnNames);
		}
		
		List<Double> allValues = new ArrayList<>();
		
		for(int i = 0; i < columnNames.size(); i++) {
			CyColumnIdentifier vCol = columnNames.get(i);
			CyColumnIdentifier pCol = pValueCols.get(i);
			
			CyRow row = network.getRow(model);
			
			Double v = getDouble(row, vCol);
			Double p = getDouble(row, pCol);

			if((!Double.isFinite(p) || p <= pFilter)) {
				if(qValueCols != null && qFilter != null) {
					CyColumnIdentifier qCol = qValueCols.get(i);
					Double q = getDouble(row, qCol);
					
					if((!Double.isFinite(q) || q <= qFilter)) {
						allValues.add(v);
					} else {
						allValues.add(Double.NaN); // results in gray slice
					}
				} else {
					allValues.add(v);
				}
			} else {
				allValues.add(Double.NaN); // results in gray slice
			}
		}
		
		Map<String,List<Double>> data = new LinkedHashMap<>();
		data.put("Values", allValues);
		return data;
	}
	
	
	private static Double getDouble(CyRow row, CyColumnIdentifier colId) {
		if(colId == null)
			return Double.NaN;
		CyTable table = row.getTable();
		String colName = colId.getColumnName();
		CyColumn col = table.getColumn(colName);
		if(col == null)
			return Double.NaN;
		Class<?> type = col.getType();
		
		if (Number.class.isAssignableFrom(type)) {
			if (!row.isSet(colName)) {
				return Double.NaN;
			} else if (type == Double.class) {
				return row.get(colName, Double.class);
			} else if (type == Integer.class) {
				Integer i = row.get(colName, Integer.class);
				if(i != null) {
					return i.doubleValue();
				}
			} else if (type == Float.class) {
				Float f = row.get(colName, Float.class);
				if(f != null) {
					return f.doubleValue();
				}
			}
		}
		return Double.NaN;
	}
	
	
	private Map<String, List<Double>> getDataFromColumnsNormal(CyNetwork network, CyIdentifiable model, List<CyColumnIdentifier> columnNames) {
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
		if (key.equalsIgnoreCase(START_ANGLE)) 
			return Double.class;
		if (key.equalsIgnoreCase(P_VALUE_COLS))
			return List.class; 
		if (key.equalsIgnoreCase(Q_VALUE_COLS))
			return List.class; 
		if (key.equalsIgnoreCase(P_VALUE)) 
			return Double.class;
		if (key.equalsIgnoreCase(Q_VALUE)) 
			return Double.class;
		return super.getSettingType(key);
	}
	
	@Override
	public Class<?> getSettingElementType(final String key) {
		if (key.equalsIgnoreCase(P_VALUE_COLS)) 
			return CyColumnIdentifier.class;
		if (key.equalsIgnoreCase(Q_VALUE_COLS)) 
			return CyColumnIdentifier.class;
		return super.getSettingElementType(key);
	}
}
