package org.baderlab.csplugins.enrichmentmap.task;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.baderlab.csplugins.enrichmentmap.model.SetOfGeneSets;
import org.baderlab.csplugins.enrichmentmap.style.AssociatedStyleBuilder;
import org.baderlab.csplugins.enrichmentmap.style.AssociatedStyleOptions;
import org.baderlab.csplugins.enrichmentmap.style.ChartOptions;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder.Columns;
import org.baderlab.csplugins.enrichmentmap.util.NetworkUtil;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.ExpressionData;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

public class UpdateAssociatedStyleTask extends AbstractTask {

	@Inject private VisualMappingManager visualMappingManager;
	
	@Inject private Provider<AssociatedStyleBuilder> styleBuilderProvider;

	private final AssociatedStyleOptions options;
	private final CyCustomGraphics2<?> chart;
	
	private static final Logger logger = LoggerFactory.getLogger(UpdateAssociatedStyleTask.class);

	public interface Factory {
		UpdateAssociatedStyleTask create(AssociatedStyleOptions options, CyCustomGraphics2<?> chart);
	}

	@Inject
	public UpdateAssociatedStyleTask(@Assisted AssociatedStyleOptions options,
			@Assisted @Nullable CyCustomGraphics2<?> chart) {
		this.options = options;
		this.chart = chart;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Update GeneMANIA Style");
		ChartOptions chartOptions = options.getChartOptions();
		
		if (chartOptions != null && chartOptions.getData() != null) {
			switch (chartOptions.getData()) {
				case EXPRESSION_DATA:
					updateExpressionDataColumn();
					break;
				case DATA_SET:
					createDataSetColumn();
					break;
				default:
					break;
			}
		}
		
		updateVisualStyle();
	}

	private void updateExpressionDataColumn() {
		CyNetwork network = options.getNetworkView().getModel();
		CyTable nodeTable = network.getDefaultNodeTable();
		
		if (!Columns.EXPRESSION_DATA_CHART.hasColumn(nodeTable)) {
			try {
				Columns.EXPRESSION_DATA_CHART.createColumn(nodeTable);
			} catch (Exception e) {
				logger.error("Cannot create column " + Columns.EXPRESSION_DATA_CHART.getBaseName(), e);
			}
		}
		
		Map<Long, double[]> columnData = new HashMap<>();
		EnrichmentMap map = options.getEnrichmentMap();
		ExpressionData exp = options.getExpressionData();
		int n = exp.getSize();

		for (CyNode node : network.getNodeList()) {
			double[] data = new double[n];
			columnData.put(node.getSUID(), data);
			
			String name = NetworkUtil.getGeneName(network, node);
			
			if (name == null)
				continue;
			
			String queryTerm = NetworkUtil.getQueryTerm(network, name);
			Integer id = map.getHashFromGene(queryTerm != null ? queryTerm : name);
			
			if (id == null)
				continue;
			
			for (int i = 0; i < n; i++) {
				double value = exp.getValue(id, i);
				data[i] = value;
			}
		}

		columnData.forEach((suid, data) -> {
			Columns.EXPRESSION_DATA_CHART.set(nodeTable.getRow(suid), Doubles.asList(data));
		});
	}

	private void createDataSetColumn() {
		CyNetwork network = options.getNetworkView().getModel();
		CyTable nodeTable = network.getDefaultNodeTable();
		
		if (!Columns.DATASET_CHART.hasColumn(nodeTable)) {
			try {
				Columns.DATASET_CHART.createColumn(nodeTable);
			} catch (Exception e) {
				logger.error("Cannot create column " + Columns.DATASET_CHART.getBaseName(), e);
			}

			Map<Long, int[]> columnData = new HashMap<>();
			EnrichmentMap map = options.getEnrichmentMap();
			List<EMDataSet> dataSets = map.getDataSetList();
			int n = dataSets.size();

			for (CyNode node : network.getNodeList()) {
				int[] data = new int[n];
				columnData.put(node.getSUID(), data);
				
				String name = NetworkUtil.getGeneName(network, node);
				
				if (name == null)
					continue;
				
				String queryTerm = NetworkUtil.getQueryTerm(network, name);
				Integer id = map.getHashFromGene(queryTerm != null ? queryTerm : name);
				
				if (id == null)
					continue;
				
				for (int i = 0; i < n; i++) {
					EMDataSet ds = dataSets.get(i);
					SetOfGeneSets geneSetsOfInterest = ds.getGeneSetsOfInterest();
					Collection<GeneSet> geneSets = geneSetsOfInterest.getGeneSets().values();
					
					for (GeneSet gs : geneSets) {
						if (gs.getGenes().contains(id)) {
							data[i] = 1;
							break;
						}
					}
				}
			}

			columnData.forEach((suid, data) -> {
				Columns.DATASET_CHART.set(nodeTable.getRow(suid), Ints.asList(data));
			});
		}
	}
	
	private void updateVisualStyle() {
		CyNetworkView view = options.getNetworkView();
		VisualStyle vs = visualMappingManager.getVisualStyle(view);
		styleBuilderProvider.get().updateProperties(vs, options, chart);
	}
}
