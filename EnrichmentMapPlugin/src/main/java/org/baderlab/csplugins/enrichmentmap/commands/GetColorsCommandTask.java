package org.baderlab.csplugins.enrichmentmap.commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.baderlab.csplugins.enrichmentmap.commands.tunables.NetworkTunable;
import org.baderlab.csplugins.enrichmentmap.commands.tunables.NodeListTunable;
import org.baderlab.csplugins.enrichmentmap.style.ChartType;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder.StyleUpdateScope;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleOptions;
import org.baderlab.csplugins.enrichmentmap.style.charts.radialheatmap.RadialHeatMapChart;
import org.baderlab.csplugins.enrichmentmap.style.charts.radialheatmap.RadialHeatMapLayer;
import org.baderlab.csplugins.enrichmentmap.task.ApplyEMStyleTask;
import org.baderlab.csplugins.enrichmentmap.view.control.ControlPanelMediator;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.jfree.chart.plot.PiePlot;

import com.google.inject.Inject;

public class GetColorsCommandTask extends AbstractTask implements ObservableTask {
	
	@Inject private ControlPanelMediator controlPanelMediator;
	@Inject private ApplyEMStyleTask.Factory applyEmStyleTaskFactory;
	
	
	@ContainsTunables @Inject
	public NetworkTunable networkTunable;
	
	@ContainsTunables @Inject
	public NodeListTunable nodeListTunable;
	
	// TODO allow this param to be null, return "average" color of datasets
	@Tunable(description = "Name of data set")
	public String dataSet;
	
	
	public List<String> results;
	
	
	@Override
	public void run(TaskMonitor tm) {
		if(!networkTunable.isEnrichmentMap())
			throw new IllegalArgumentException("Network is not an Enrichment Map.");
		if(dataSet != null && !networkTunable.getEnrichmentMap().getDataSetNames().contains(dataSet))
			throw new IllegalArgumentException("dataSet '" + dataSet + "' not found");
		
		var network = networkTunable.getNetwork();
		var networkView = networkTunable.getNetworkView();
		
		RadialHeatMapChart radialHeatMapChart = getChart(networkView);
		
		var nodeSuids = nodeListTunable.getNodeSuids();
		results = new ArrayList<>(nodeSuids.size());
		
		for(var suid : nodeSuids) {
			var nodeView = networkView.getNodeView(network.getNode(suid));
			if(nodeView == null) {
				results.add(null);
			} else {
				var color = getColor(radialHeatMapChart, networkView, nodeView);
				results.add(color == null ? null : Integer.toString(color.getRGB()));
			}
		}
	}
	
	private RadialHeatMapChart getChart(CyNetworkView networkView) {
		EMStyleOptions options = controlPanelMediator.createStyleOptions(networkView);
		var applyStyleTask = applyEmStyleTaskFactory.create(options, StyleUpdateScope.ALL); // update scope doesn't matter
		var props = applyStyleTask.createChartProps();
		CyCustomGraphics2<?> chart = applyStyleTask.createChart(props, ChartType.RADIAL_HEAT_MAP);
		return (RadialHeatMapChart) chart;
	}
	
	
	private int getDataSetIndex(String dataSet) {
		var names = networkTunable.getEnrichmentMap().getDataSetNames();
		for(int i = 0; i < names.size(); i++) {
			String name = names.get(i);
			if(name.equals(dataSet)) {
				return i;
			}
		}
		return -1;
	}
	
	
	private List<String> getDataSetNamesToUse() {
		if(dataSet == null)
			return networkTunable.getEnrichmentMap().getDataSetNames();
		else
			return List.of(dataSet);
	}
	
	
	
	private Color getColor(RadialHeatMapChart radialHeatMapChart, CyNetworkView networkView, View<CyNode> nodeView) {
		List<RadialHeatMapLayer> layers = radialHeatMapChart.getLayers(networkView, nodeView);
		if(layers == null || layers.isEmpty())
			return null;
		
		var layer = layers.get(0);
		var dataset = layer.createDataset();
		var chart = layer.createChart(dataset);
		var plot = (PiePlot) chart.getPlot();
		
		List<Color> colors = new ArrayList<>();
		for(var dataSet : getDataSetNamesToUse()) {
			int dataSetIndex = getDataSetIndex(dataSet);
			var k = "#" + (dataSetIndex + 1);
			var paint = plot.getSectionPaint(k);
			if(paint instanceof Color) {
				colors.add((Color)paint);
			}
		}
		
		return average(colors);
	}
	
	private static Color average(List<Color> colors) {
		int n = colors.size();
		if(n == 0)
			return null;
		if(n == 1)
			return colors.get(0);
		
		int sumR = 0, sumG = 0, sumB = 0;
		for(var color : colors) {
			sumR += color.getRed();
			sumG += color.getGreen();
			sumB += color.getBlue();
		}
		return new Color(sumR / n, sumG / n, sumB / n);
	}

	
	@Override
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(String.class, List.class);
	}
	
	@Override
	public <R> R getResults(Class<? extends R> type) {
		if(String.class.equals(type)) {
			return type.cast(String.join(",", results));
		} else if(List.class.equals(type)) {
			return type.cast(results);
		}
		return null;
	}

}
