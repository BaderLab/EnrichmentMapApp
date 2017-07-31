package org.baderlab.csplugins.enrichmentmap.style.charts.radialheatmap;

import java.util.Map;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.UIManager;

import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;

import com.google.inject.Inject;

public class RadialHeatMapChartFactory implements CyCustomGraphics2Factory<RadialHeatMapLayer> {

	@Inject private CyServiceRegistrar serviceRegistrar;

	@Override
	public CyCustomGraphics2<RadialHeatMapLayer> getInstance(final String input) {
		return new RadialHeatMapChart(input, serviceRegistrar);
	}

	@Override
	public CyCustomGraphics2<RadialHeatMapLayer> getInstance(final CyCustomGraphics2<RadialHeatMapLayer> chart) {
		return new RadialHeatMapChart((RadialHeatMapChart)chart, serviceRegistrar);
	}
	
	@Override
	public CyCustomGraphics2<RadialHeatMapLayer> getInstance(final Map<String, Object> properties) {
		return new RadialHeatMapChart(properties, serviceRegistrar);
	}

	@Override
	public String getId() {
		return RadialHeatMapChart.FACTORY_ID;
	}
	
	@Override
	public Class<? extends CyCustomGraphics2<RadialHeatMapLayer>> getSupportedClass() {
		return RadialHeatMapChart.class;
	}

	@Override
	public String getDisplayName() {
		return "Radial Heat Map (by EnrichmentMap)";
	}
	
	@Override
	public JComponent createEditor(final CyCustomGraphics2<RadialHeatMapLayer> chart) {
		JLabel infoLabel = new JLabel(getDisplayName());
		infoLabel.setHorizontalAlignment(JLabel.CENTER);
		infoLabel.setFont(infoLabel.getFont().deriveFont(18.0f));
		infoLabel.setEnabled(false);
		infoLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
		
		return infoLabel;
	}
	
	@Override
	public Icon getIcon(int width, int height) {
		return SwingUtil.resizeIcon(RadialHeatMapChart.ICON, width, height);
	}
	
	@Override
	public String toString() {
		return getDisplayName();
	}
}
