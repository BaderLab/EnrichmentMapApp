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
import org.baderlab.csplugins.enrichmentmap.style.ChartOptions;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder.Columns;
import org.baderlab.csplugins.enrichmentmap.style.GMStyleBuilder;
import org.baderlab.csplugins.enrichmentmap.style.GMStyleOptions;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Ints;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

public class UpdateGMStyleTask extends AbstractTask {

	@Inject private VisualMappingManager visualMappingManager;
	
	@Inject private Provider<GMStyleBuilder> styleBuilderProvider;

	private final GMStyleOptions options;
	private final CyCustomGraphics2<?> chart;
	
	private static final Logger logger = LoggerFactory.getLogger(UpdateGMStyleTask.class);

	public interface Factory {
		UpdateGMStyleTask create(GMStyleOptions options, CyCustomGraphics2<?> chart);
	}

	@Inject
	public UpdateGMStyleTask(@Assisted GMStyleOptions options, @Assisted @Nullable CyCustomGraphics2<?> chart) {
		this.options = options;
		this.chart = chart;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Update GeneMANIA Style");
		ChartOptions chartOptions = options.getChartOptions();
		
		if (chartOptions != null && chartOptions.getData() != null) {
			switch (chartOptions.getData()) {
				case DATA_SET:
				default:
					createDataSetColumn();
					break;
			}
		}
		
		updateVisualStyle();
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

			String org = null;
			
			try {
				org = GMStyleBuilder.Columns.GM_ORGANISM.get(network.getRow(network));
			} catch (Exception e) {
				logger.error("Cannot get '" + GMStyleBuilder.Columns.GM_ORGANISM.getBaseName() + "' from GeneMANIA's Network table.", e);
			}
			
			Map<Long, int[]> columnData = new HashMap<>();
			EnrichmentMap map = options.getEnrichmentMap();
			List<EMDataSet> dataSets = map.getDataSetList();
			int n = dataSets.size();

			for (CyNode node : network.getNodeList()) {
				int[] data = new int[n];
				columnData.put(node.getSUID(), data);
				
				CyRow row = network.getRow(node);
				String name = GMStyleBuilder.Columns.GM_GENE_NAME.get(row, null, null);
				
				if (name == null)
					continue;
				
				if (org != null)
					name = map.getGeneManiaQuerySymbol(org, name);
				
				Integer id = map.getHashFromGene(name);
				
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
