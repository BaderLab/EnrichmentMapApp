package org.baderlab.csplugins.enrichmentmap.task;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.baderlab.csplugins.enrichmentmap.model.AbstractDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.style.AbstractColumnDescriptor;
import org.baderlab.csplugins.enrichmentmap.style.ChartData;
import org.baderlab.csplugins.enrichmentmap.style.ChartFactoryManager;
import org.baderlab.csplugins.enrichmentmap.style.ChartOptions;
import org.baderlab.csplugins.enrichmentmap.style.ChartType;
import org.baderlab.csplugins.enrichmentmap.style.ColorScheme;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder.Columns;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleOptions;
import org.baderlab.csplugins.enrichmentmap.style.charts.AbstractChart;
import org.baderlab.csplugins.enrichmentmap.view.control.ControlPanelMediator;
import org.baderlab.csplugins.enrichmentmap.view.control.FilterUtil;
import org.baderlab.csplugins.enrichmentmap.view.control.io.ViewParams;
import org.baderlab.csplugins.enrichmentmap.view.control.io.ViewParams.CutoffParam;
import org.baderlab.csplugins.enrichmentmap.view.util.ChartUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifier;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.common.primitives.Ints;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

public class ApplyEMStyleTask extends AbstractTask {

	@Inject private VisualMappingManager visualMappingManager;
	@Inject private VisualStyleFactory visualStyleFactory;
	@Inject private ChartFactoryManager chartFactoryManager;
	@Inject private CyColumnIdentifierFactory columnIdFactory;
	@Inject private Provider<EMStyleBuilder> styleBuilderProvider;
	@Inject private Provider<ControlPanelMediator> controlPanelMediatorProvider;

	private final EMStyleOptions options;
	private final boolean updateChartOnly;

	public interface Factory {
		ApplyEMStyleTask create(EMStyleOptions options, boolean updateChartOnly);
	}

	@Inject
	public ApplyEMStyleTask(@Assisted EMStyleOptions options, @Assisted boolean updateChartOnly) {
		this.options = options;
		this.updateChartOnly = updateChartOnly;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setTitle("Apply EnrichmentMap Style");
		tm.setProgress(0.0);
		
		ChartOptions chartOptions = options.getChartOptions();
		
		if (chartOptions != null && chartOptions.getData() == ChartData.DATA_SET) {
			tm.setStatusMessage("Updating Data Columns...");
			createOrUpdateDataSetColumn();
		}
		
		tm.setStatusMessage("Updating Style...");
		tm.setProgress(0.6);
		
		applyVisualStyle();
	}

	private void createOrUpdateDataSetColumn() {
		EnrichmentMap map = options.getEnrichmentMap();
		CyNetworkView netView = options.getNetworkView();
		CyNetwork network = netView.getModel();
		CyTable nodeTable = network.getDefaultNodeTable();
		
		String prefix = map.getParams().getAttributePrefix();
		
		if(!Columns.DATASET_CHART.hasColumn(nodeTable, prefix)) {
			Columns.DATASET_CHART.createColumn(nodeTable, prefix);
		}

		Map<Long,int[]> columnData = getDatasetChartColumnData();
		
		columnData.forEach((suid, data) ->
			Columns.DATASET_CHART.set(nodeTable.getRow(suid), prefix, Ints.asList(data))
		);
	}
	
	
	private Map<Long, int[]> getDatasetChartColumnData() {
		EnrichmentMap map = options.getEnrichmentMap();
		CyNetworkView netView = options.getNetworkView();
		CyNetwork network = netView.getModel();
		CyTable nodeTable = network.getDefaultNodeTable();
		
		Collection<? extends AbstractDataSet> dataSets = filterEMDataSets(options.getDataSets());
		
		Map<Long, int[]> columnData = new HashMap<>();
		int n = dataSets.size();

		for(CyNode node : network.getNodeList()) {
			columnData.put(node.getSUID(), new int[n]);
		}

		// Don't show pie slices that are filtered out by the p-value or q-value sliders :)
		ViewParams viewParams = controlPanelMediatorProvider.get().getViewParams(netView.getSUID());
		CutoffParam cutoffParam = viewParams.getNodeCutoffParam();
		EMCreationParameters params = map.getParams();
		
		Set<String> columns;
		double[] values;
		
		if(cutoffParam == CutoffParam.P_VALUE) {
			columns = params.getPValueColumnNames();
			values = controlPanelMediatorProvider.get().getPValueSliderValues(netView.getSUID());
		} else {
			columns = params.getQValueColumnNames();
			values = controlPanelMediatorProvider.get().getQValueSliderValues(netView.getSUID());
		}
		
		if(values == null) {
			int dataSetIndex = 0;
			for(AbstractDataSet ds : dataSets) {
				for(Long nodeSuid : ds.getNodeSuids()) {
					columnData.get(nodeSuid)[dataSetIndex] = 1;
				}
				dataSetIndex++;
			}
		} else {
			Double maxCutoff = values[1];
			Double minCutoff = values[0];
			
			int dataSetIndex = 0;
			for(AbstractDataSet ds : dataSets) {
				String column = FilterUtil.getColumnName(columns, ds);
				for(Long nodeSuid : ds.getNodeSuids()) {
					CyRow row = nodeTable.getRow(nodeSuid);
					if(column == null || FilterUtil.passesFilter(column, nodeTable, row, maxCutoff, minCutoff)) {
						columnData.get(nodeSuid)[dataSetIndex] = 1;
					}
				}
				dataSetIndex++;
			}
		}
		
		return columnData;
	}
	
	private void applyVisualStyle() {
		CyNetworkView view = options.getNetworkView();
		VisualStyle vs = getVisualStyle(options.getEnrichmentMap());
		
		if (!vs.equals(visualMappingManager.getVisualStyle(view)))
			visualMappingManager.setVisualStyle(vs, view);
		
		CyCustomGraphics2<?> chart = createChart();
		
		if (updateChartOnly)
			styleBuilderProvider.get().updateNodeChart(vs, options, chart);
		else
			styleBuilderProvider.get().updateProperties(vs, options, chart);
		
		
	}

	private VisualStyle getVisualStyle(EnrichmentMap map) {
		String vsName = EMStyleBuilder.getStyleName(map);
		VisualStyle vs = getExistingVisualStyle(vsName);

		if (vs == null) {
			vs = visualStyleFactory.createVisualStyle(vsName);
			visualMappingManager.addVisualStyle(vs);
		}
		
		return vs;
	}

	/**
	 * Note: Cytoscape does not provide a way to uniquely identify a visual
	 * style. Here we use the name we previously generated to attempt to
	 * identify the visual style. This is just a heuristic, it is possible the
	 * user changed the name. In that case a new visual style will be generated.
	 */
	private VisualStyle getExistingVisualStyle(String name) {
		for (VisualStyle vs : visualMappingManager.getAllVisualStyles()) {
			if (vs.getTitle() != null && vs.getTitle().equals(name))
				return vs;
		}
		
		return null;
	}
	
	public CyCustomGraphics2<?> createChart() {
		CyCustomGraphics2<?> chart = null;
		ChartOptions chartOptions = options.getChartOptions();
		ChartData data = chartOptions != null ? chartOptions.getData() : null;
		
		if (data != null && data != ChartData.NONE) {
			List<EMDataSet> dataSets = filterEMDataSets(options.getDataSets()); // Ignore Signature Data Sets in charts
			
			if (!dataSets.isEmpty()) {
				ChartType type = chartOptions.getType();
				Map<String, Object> props = new HashMap<>(type.getProperties());
				
				String prefix = options.getAttributePrefix();
				AbstractColumnDescriptor columnDescriptor = data.getColumnDescriptor();
				
				if (data == ChartData.DATA_SET) {
					List<CyColumnIdentifier> columns = Arrays.asList(columnIdFactory.createColumnIdentifier(columnDescriptor.with(prefix)));
					List<Color> colors = getColors(dataSets);
					props.put("cy_dataColumns", columns);
					props.put("cy_colors", colors);
					props.put("cy_showItemLabels", chartOptions.isShowLabels());
					props.put("cy_rotation", "CLOCKWISE");
					
				} else {
					List<CyColumnIdentifier> columns = ChartUtil.getSortedColumnIdentifiers(prefix,
							dataSets, columnDescriptor, columnIdFactory);
	
					List<Color> colors = ChartUtil.getChartColors(chartOptions, true);
					List<Double> range = ChartUtil.calculateGlobalRange(options.getNetworkView().getModel(), columns);
					
					props.put("cy_dataColumns", columns);
					props.put("cy_range", range);
					props.put("cy_autoRange", false);
					props.put("cy_globalRange", true);
					props.put("cy_showItemLabels", chartOptions.isShowLabels());
					props.put("cy_colors", colors);
					
					ColorScheme colorScheme = chartOptions != null ? chartOptions.getColorScheme() : null;
					
					if (colorScheme != null && colorScheme.getPoints() != null) {
						List<Double> points = colorScheme.getPoints();
						
						if (!points.isEmpty())
							props.put(AbstractChart.COLOR_POINTS, points);
					}
				}
				try {
					CyCustomGraphics2Factory<?> factory = chartFactoryManager.getChartFactory(type.getId());
					
					if (factory != null)
						chart = factory.getInstance(props);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		return chart;
	}
	
	
	public static List<Color> getColors(Collection<EMDataSet> dataSets) {
		return dataSets.stream().map(EMDataSet::getColor).collect(Collectors.toList());
	}
	
	@SuppressWarnings("unchecked")
	public static List<EMDataSet> filterEMDataSets(Collection<? extends AbstractDataSet> abstractDataSets) {
		Collection<?> set = abstractDataSets.stream()
				.filter(ds -> ds instanceof EMDataSet) // Ignore Signature Data Sets
				.collect(Collectors.toList());
		
		return (List<EMDataSet>) set;
	}
}
