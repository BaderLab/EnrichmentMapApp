package org.baderlab.csplugins.enrichmentmap.commands;

import static org.baderlab.csplugins.enrichmentmap.commands.tunables.CommandUtil.lssFromEnum;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.baderlab.csplugins.enrichmentmap.commands.tunables.NetworkTunable;
import org.baderlab.csplugins.enrichmentmap.model.Compress;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.Transform;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.ExportPDFTask;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapMediator;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.RankingOption;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.HeatMapTableModel;
import org.baderlab.csplugins.enrichmentmap.view.util.OpenPDFViewerTask;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class ExportPDFCommandTask extends AbstractTask {
	
	@ContainsTunables @Inject
	public NetworkTunable networkTunable;
	
	@Tunable(required=true)
	public File file;
	
	@Tunable(description="true (default) for only selected nodes and edges, false for all nodes and edges")
	public boolean selectedOnly = true;
	
	@Tunable
	public boolean showValues = false;

	@Tunable
	public ListSingleSelection<String> transform = lssFromEnum(Transform.values());
	
	@Tunable
	public ListSingleSelection<String> compress = lssFromEnum(Compress.values());
	
	@Tunable
	public ListSingleSelection<String> operator = new ListSingleSelection<>("union", "intersection");
	
	@Tunable(description="If true attempts to open a system PDF viewer on the exported file, does not work in headless mode.")
	public boolean openViewer = false;
	
	
	@Override
	public void run(TaskMonitor taskMonitor) {
		if(file == null)
			throw new IllegalArgumentException("file parameter required");
		
		// validate network
		CyNetwork network = networkTunable.getNetwork();
		EnrichmentMap map = networkTunable.getEnrichmentMap();
		if(network == null || map == null)
			throw new IllegalArgumentException("network is not an EnrichmentMap network");
		
		Transform transformValue = Transform.valueOf(transform.getSelectedValue());
		Compress compressValue = Compress.valueOf(compress.getSelectedValue());
		
		List<String> genes = getGenes();
		
		HeatMapTableModel model = new HeatMapTableModel(network, map, null, genes, transformValue, compressValue);
		ExportPDFTask exportTask = new ExportPDFTask(file, model, RankingOption.none(), showValues);
		
		TaskIterator moreTasks = new TaskIterator(exportTask);
		if(openViewer)
			moreTasks.append(new OpenPDFViewerTask(file));
		
		logOutput();
		insertTasksAfterCurrentTask(moreTasks);
	}
	
	
	private List<String> getGenes() {
		CyNetwork network = networkTunable.getNetwork();
		EnrichmentMap map = networkTunable.getEnrichmentMap();
		String prefix = map.getParams().getAttributePrefix();
		
		List<CyNode> nodes;
		List<CyEdge> edges;
		if(selectedOnly) {
			nodes = CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, true);
			edges = CyTableUtil.getEdgesInState(network, CyNetwork.SELECTED, true);
		} else {
			nodes = network.getNodeList();
			edges = network.getEdgeList();
		}
		
		List<String> genes;
		if("union".equals(operator.getSelectedValue())) {
			genes = new ArrayList<>(HeatMapMediator.unionGenesets(network, nodes, edges, prefix));
		} else {
			genes = new ArrayList<>(HeatMapMediator.intersectionGenesets(network, nodes, edges, prefix));
		}
		genes.sort(Comparator.naturalOrder());
		
		return genes;
	}
	
	
	private void logOutput() {
		Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
		logger.info("Exporting HeatMap PDF to: " + file.getAbsolutePath());
		logger.info("selectedOnly=" + selectedOnly);
		logger.info("transform=" + transform.getSelectedValue());
		logger.info("compress=" + compress.getSelectedValue());
		logger.info("operator=" + operator.getSelectedValue());
		logger.info("showValues=" + showValues);
	}

}
