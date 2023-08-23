package org.baderlab.csplugins.enrichmentmap.commands;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
		
		tm.setStatusMessage("Significance: " + chartData);
		if(!isValidSignificance(chartData))
			return;

		List<CyColumnIdentifier> columnIDs = getSignificanceColumns(map, options);
		if(columnIDs.isEmpty())
			return;
		
		
		List<CyNode> nodes = new ArrayList<>(network.getNodeList());
		
		Map<CyNode,Double> nodeSig = new HashMap<>();
		for(CyNode node : nodes) {
			nodeSig.put(node, getNodeAvgSig(network, node, columnIDs));
		}
		
		nodes.sort(getComparator(nodeSig, chartData));
		
		for(CyNode node: nodes) {
			System.out.println("Node:" + node.getSUID() + ", sig:" + nodeSig.get(node));
		}
		
		results = nodes;
	}
	
	
	private static boolean isValidSignificance(ChartData chartData) {
		switch(chartData) {
			case DATA_SET: case EXPRESSION_DATA: case NONE: case PHENOTYPES:
				return false;
			default:
				return true;
		}
	}
	
	
	private Comparator<CyNode> getComparator(Map<CyNode,Double> nodeSig, ChartData chartData) {
		switch(chartData) {
			default:
			case NES_VALUE:
			case NES_SIG:
				return (n1, n2) -> {
					var sig1 = Math.abs(nodeSig.get(n1));
					var sig2 = Math.abs(nodeSig.get(n2));
					return -Double.compare(sig1, sig2);
				};
			case P_VALUE: 
			case FDR_VALUE:
				return (n1, n2) -> Double.compare(nodeSig.get(n1), nodeSig.get(n2));
			case MAX_NEG_LOG10_PVAL:
				return (n1, n2) -> -Double.compare(nodeSig.get(n1), nodeSig.get(n2));
		}
	}
	
	
	private List<CyColumnIdentifier> getSignificanceColumns(EnrichmentMap map, EMStyleOptions options) {
		// TODO how does NES_SIG work???
		var applyStyleTask = applyEmStyleTaskFactory.create(options, StyleUpdateScope.ALL); // update scope doesn't matter
		var props = applyStyleTask.createChartProps();
		var columns = (List<CyColumnIdentifier>) props.get("cy_dataColumns");
		return columns == null ? List.of() : columns;
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
			String str = results.stream()
					.map(n -> n.getSUID().toString())
					.collect(Collectors.joining(", ", "[", "]"));
			return type.cast(str);
		}
		if(List.class.equals(type)) {
			return type.cast(new ArrayList<>(results));
		}
		return null;
	}

}
