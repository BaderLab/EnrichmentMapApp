package org.baderlab.csplugins.enrichmentmap.commands;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.baderlab.csplugins.enrichmentmap.commands.tunables.NetworkTunable;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.style.ChartData;
import org.baderlab.csplugins.enrichmentmap.style.ChartOptions;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder.StyleUpdateScope;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleOptions;
import org.baderlab.csplugins.enrichmentmap.task.ApplyEMStyleTask;
import org.baderlab.csplugins.enrichmentmap.view.control.ControlPanelMediator;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifier;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;

public class SignificanceListTask extends AbstractTask implements ObservableTask {

	@Inject private ControlPanelMediator controlPanelMediator;
	@Inject private ApplyEMStyleTask.Factory applyEmStyleTaskFactory;
	
	@ContainsTunables @Inject
	public NetworkTunable networkTunable;
	
	
	private List<CyNode> results = new ArrayList<>();
	
	
	@Override
	public void run(TaskMonitor tm) {
		// validate network
		CyNetwork network = networkTunable.getNetwork();
		CyNetworkView networkView = networkTunable.getNetworkView();
		EnrichmentMap map = networkTunable.getEnrichmentMap();
		if(networkView == null || map == null)
			throw new IllegalArgumentException("network is not an EnrichmentMap network");
		
		EMStyleOptions options = controlPanelMediator.createStyleOptions(networkView);
		ChartOptions chartOptions = options.getChartOptions();
		ChartData chartData = chartOptions.getData();
		if(chartData == ChartData.NONE || chartData == ChartData.DATA_SET)
			return;

		tm.setStatusMessage("Significance: " + chartData);
		
		List<CyColumnIdentifier> columnIDs = getSignificanceColumns(map, options);
		if(columnIDs == null || columnIDs.isEmpty())
			return;
		
		
		List<CyNode> nodes = new ArrayList<>(network.getNodeList());
		Map<CyNode,Double> nodeSig = new HashMap<>();
		
		for(CyNode node : nodes) {
			nodeSig.put(node, getNodeAvgSig(network, node, columnIDs));
		}
		
		Comparator<CyNode> comp = (n1, n2) -> Double.compare(nodeSig.get(n1), nodeSig.get(n2));
		nodes.sort(comp.reversed());
		
		results = nodes;
	}
	
	
	private List<CyColumnIdentifier> getSignificanceColumns(EnrichmentMap map, EMStyleOptions options) {
		// TODO how does NES_SIG work???
		var applyStyleTask = applyEmStyleTaskFactory.create(options, StyleUpdateScope.ALL); // update scope doesn't matter
		var props = applyStyleTask.createChartProps();
		var columns = (List<CyColumnIdentifier>) props.get("cy_dataColumns");
		return columns;
	}
	
	
	private double getNodeAvgSig(CyNetwork network, CyNode node, List<CyColumnIdentifier> columnIDs) {
		CyTable nodeTable = network.getDefaultNodeTable();
		
		double sigSum = 0;
		boolean hasSig = false;
		
		for(var colID : columnIDs) {
			String colName = colID.getColumnName();
			Object val = network.getRow(node).get(colName, nodeTable.getColumn(colName).getType());
			if(val instanceof Number) {
				double sig = ((Number)val).doubleValue();
				sigSum += sig;
				hasSig = true;
			}
		}
		
		if(!hasSig)
			return Double.NaN;

		return sigSum / columnIDs.size();
	}
	
	
	@Override
	public List<Class<?>> getResultClasses() {
		return List.of(String.class, List.class);
	}
	

	@Override
	public <R> R getResults(Class<? extends R> type) {
		if(String.class.equals(type)) {
			return type.cast(results.toString());
		}
		if(List.class.equals(type)) {
			return type.cast(new ArrayList<>(results));
		}
		return null;
	}

}
