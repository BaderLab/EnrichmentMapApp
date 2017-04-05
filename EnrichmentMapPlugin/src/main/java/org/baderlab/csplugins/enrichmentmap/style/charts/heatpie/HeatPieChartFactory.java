package org.baderlab.csplugins.enrichmentmap.style.charts.heatpie;

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

public class HeatPieChartFactory implements CyCustomGraphics2Factory<HeatPieLayer> {

	@Inject private CyServiceRegistrar serviceRegistrar;

	@Override
	public CyCustomGraphics2<HeatPieLayer> getInstance(final String input) {
		return new HeatPieChart(input, serviceRegistrar);
	}

	@Override
	public CyCustomGraphics2<HeatPieLayer> getInstance(final CyCustomGraphics2<HeatPieLayer> chart) {
		return new HeatPieChart((HeatPieChart)chart, serviceRegistrar);
	}
	
	@Override
	public CyCustomGraphics2<HeatPieLayer> getInstance(final Map<String, Object> properties) {
		return new HeatPieChart(properties, serviceRegistrar);
	}

	@Override
	public String getId() {
		return HeatPieChart.FACTORY_ID;
	}
	
	@Override
	public Class<? extends CyCustomGraphics2<HeatPieLayer>> getSupportedClass() {
		return HeatPieChart.class;
	}

	@Override
	public String getDisplayName() {
		return "Heat Pie (by Enrichment Map)";
	}
	
	@Override
	public JComponent createEditor(final CyCustomGraphics2<HeatPieLayer> chart) {
		JLabel infoLabel = new JLabel(getDisplayName());
		infoLabel.setHorizontalAlignment(JLabel.CENTER);
		infoLabel.setFont(infoLabel.getFont().deriveFont(18.0f));
		infoLabel.setEnabled(false);
		infoLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
		
		return infoLabel;
	}
	
	@Override
	public Icon getIcon(int width, int height) {
		return SwingUtil.resizeIcon(HeatPieChart.ICON, width, height);
	}
	
	@Override
	public String toString() {
		return getDisplayName();
	}
}
