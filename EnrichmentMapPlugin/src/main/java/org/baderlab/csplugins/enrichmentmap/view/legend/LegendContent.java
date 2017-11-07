package org.baderlab.csplugins.enrichmentmap.view.legend;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.swing.Icon;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.style.ChartOptions;
import org.baderlab.csplugins.enrichmentmap.style.ChartType;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleOptions;
import org.baderlab.csplugins.enrichmentmap.view.util.ChartUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.jfree.chart.JFreeChart;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class LegendContent {

	static final int LEGEND_ICON_SIZE = 18;
	
	@Inject private RenderingEngineManager engineManager;
	@Inject private VisualMappingManager visualMappingManager;
	
	private EMStyleOptions options;
	private Collection<EMDataSet> dataSets;
	
	
	public interface Factory {
		LegendContent create(EMStyleOptions options, Collection<EMDataSet> dataSets);
	}
	
	@Inject
	public LegendContent(@Assisted EMStyleOptions options, @Assisted Collection<EMDataSet> dataSets) {
		this.options = Objects.requireNonNull(options);
		this.dataSets = dataSets == null ? Collections.emptyList() : dataSets;
	}
	
	
	public boolean hasStyle() {
		return getVisualStyle() != null;
	}
	
	public VisualStyle getVisualStyle() {
		CyNetworkView netView = options.getNetworkView();
		VisualStyle style = visualMappingManager.getVisualStyle(netView);
		return style;
	}
	
	public EnrichmentMap getEnrichmentMap() {
		return options.getEnrichmentMap();
	}
	
	public EMStyleOptions getStyleOptions() {
		return options;
	}
	
	public Collection<EMDataSet> getFilteredDataSets() {
		return dataSets;
	}
	
	public boolean isOneDataSet() {
		return dataSets.size() == 1;
	}
	
	public JFreeChart getChart() {
		ChartOptions chartOptions = options.getChartOptions();
		if(chartOptions == null)
			return null;
		
		List<EMDataSet> sortedDataSets = ChartUtil.sortDataSets(dataSets);
		ChartType chartType = options.getChartOptions().getType();
		
		// MKTODO add support for DATASET_PIE
		switch (chartType) {
			case RADIAL_HEAT_MAP:
				return ChartUtil.createRadialHeatMapLegend(sortedDataSets, options.getChartOptions());
			case HEAT_MAP:
				return ChartUtil.createHeatMapLegend(sortedDataSets, options.getChartOptions());
			case HEAT_STRIPS:
				return ChartUtil.createHeatStripsLegend(sortedDataSets, options.getChartOptions());
		}
		return null;
	}
	
	public String getChartLabel() {
		return "" + options.getChartOptions().getData();
	}
	
	public Icon getGeneSetNodeShape() {
		VisualStyle style = getVisualStyle();
		if(style == null)
			return null;
			
		NodeShape shape = EMStyleBuilder.getGeneSetNodeShape(style);
		return getIcon(BasicVisualLexicon.NODE_SHAPE, shape, options.getNetworkView());
	}
	
	public Icon getSignatureNodeShape() {
		VisualStyle style = getVisualStyle();
		if(style == null)
			return null;
			
		NodeShape shape = EMStyleBuilder.getSignatureNodeShape(style);
		return getIcon(BasicVisualLexicon.NODE_SHAPE, shape, options.getNetworkView());
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Icon getIcon(VisualProperty<?> vp, Object value, CyNetworkView netView) {
		if (value == null || netView == null)
			return null;
		
		Collection<RenderingEngine<?>> engines = engineManager.getRenderingEngines(netView);
		RenderingEngine<?> engine = null;
		
		for (RenderingEngine<?> re : engines) {
			if (re.getRendererId().equals(netView.getRendererId())) {
				engine = re;
				break;
			}
		}
		
		Icon icon = engine != null ?
				engine.createIcon((VisualProperty) vp, value, LEGEND_ICON_SIZE, LEGEND_ICON_SIZE) : null;
		
		return icon;
	}

}
