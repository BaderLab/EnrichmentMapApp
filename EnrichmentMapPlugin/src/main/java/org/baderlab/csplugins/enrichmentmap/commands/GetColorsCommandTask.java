package org.baderlab.csplugins.enrichmentmap.commands;

import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.baderlab.csplugins.enrichmentmap.commands.tunables.NetworkTunable;
import org.baderlab.csplugins.enrichmentmap.commands.tunables.NodeListTunable;
import org.baderlab.csplugins.enrichmentmap.style.ChartData;
import org.baderlab.csplugins.enrichmentmap.style.ChartOptions;
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
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
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
		
		var networkView = networkTunable.getNetworkView();
		EMStyleOptions styleOptions = getStyleOptions(networkView);
		
		var chartData = getChartType(styleOptions);
		var nodeSuids = nodeListTunable.getNodeSuids();
		
		if(chartData == null || chartData == ChartData.NONE) {
			// get colors from node fill style
			var colors = getColorsFromVisualProperties(nodeSuids, networkView);
			results = colorsToStrings(colors);
		} else {
			// get colors from chart
			RadialHeatMapChart radialHeatMapChart = getChart(styleOptions);
			var colors = getColorsFromChart(nodeSuids, networkView, radialHeatMapChart);
			results = colorsToStrings(colors);
		}
		
		System.out.println("** colors command **");
		results.forEach(System.out::println);
	}
	
	
	private EMStyleOptions getStyleOptions(CyNetworkView networkView) {
		return controlPanelMediator.createStyleOptions(networkView);
	}
	
	
	private static ChartData getChartType(EMStyleOptions styleOptions) {
		ChartOptions chartOptions = styleOptions.getChartOptions();
		if(chartOptions != null) {
			return chartOptions.getData();
		}
		return null;
	}
	
	
	private RadialHeatMapChart getChart(EMStyleOptions options) {
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
		return dataSet == null
			? networkTunable.getEnrichmentMap().getDataSetNames()
			: List.of(dataSet);
	}
	
	
	private static List<Color> getColors(List<Long> nodeSuids, CyNetworkView networkView, Function<View<CyNode>,Color> colorForNode) {
		var network = networkView.getModel();
		List<Color> colors = new ArrayList<>(nodeSuids.size());
		
		for(var suid : nodeSuids) {
			var nodeView = networkView.getNodeView(network.getNode(suid));
			if(nodeView == null) {
				colors.add(null);
			} else {
				var color = colorForNode.apply(nodeView);
				colors.add(color);
			}
		}
		
		return colors;
	}
	
	
	private List<Color> getColorsFromVisualProperties(List<Long> nodeSuids, CyNetworkView networkView) {
		return getColors(nodeSuids, networkView, nodeView -> {
			Paint paint = nodeView.getVisualProperty(BasicVisualLexicon.NODE_FILL_COLOR);
			return paint instanceof Color ? (Color)paint : null;
		});
	}
	
	
	private List<Color> getColorsFromChart(List<Long> nodeSuids, CyNetworkView networkView, RadialHeatMapChart radialHeatMapChart) {
		return getColors(nodeSuids, networkView, nodeView -> {
			return getColorFromChart(radialHeatMapChart, networkView, nodeView);
		});
	}
	
	
	private Color getColorFromChart(RadialHeatMapChart radialHeatMapChart, CyNetworkView networkView, View<CyNode> nodeView) {
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

	
	private static List<String> colorsToStrings(List<Color> colors) {
		return colors.stream()
				.map(color -> color == null ? null : Integer.toString(color.getRGB()))
				.collect(Collectors.toList());
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
