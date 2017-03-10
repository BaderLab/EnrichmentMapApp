package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet.Method;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.model.Ranking;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * Updates the HeatMap panel in response to Cytoscape events.
 */
@Singleton
public class HeatMapMediator implements RowsSetListener, SetCurrentNetworkViewListener {

	@Inject private Provider<HeatMapParentPanel> panelProvider;
	@Inject private EnrichmentMapManager emManager;
	@Inject private ClusterRankingOption.Factory clusterRankOptionFactory;
	
	@Inject private CyServiceRegistrar serviceRegistrar;
	@Inject private CySwingApplication swingApplication;
	@Inject private CyApplicationManager applicationManager;
	
	private HeatMapParentPanel heatMapPanel = null;
	
	
	public void showHeatMapPanel() {
		try {
			heatMapPanel = (HeatMapParentPanel) serviceRegistrar.getService(CytoPanelComponent.class, "(id=" + HeatMapParentPanel.ID + ")");
		} catch (Exception ex) { }
		
		if(heatMapPanel == null) {
			heatMapPanel = panelProvider.get();
			heatMapPanel.setHeatMapParamsChangeListener(this::heatMapParamsChanged);
			Properties props = new Properties();
			props.setProperty("id", HeatMapParentPanel.ID);
			serviceRegistrar.registerService(heatMapPanel, CytoPanelComponent.class, props);
		}
		
		// Bring panel to front
		CytoPanel cytoPanel = swingApplication.getCytoPanel(heatMapPanel.getCytoPanelName());
		int index = cytoPanel.indexOfComponent(HeatMapParentPanel.ID);
		if(index >= 0)
			cytoPanel.setSelectedIndex(index);
	}

	
	@Override
	public void handleEvent(RowsSetEvent e) {
		// MKTODO If this has bad performance then add a reconciler timer delay.
		// Cytoscape selection events can come in sets of 1-4 events.
		if(e.containsColumn(CyNetwork.SELECTED)) {
			CyNetworkView networkView = applicationManager.getCurrentNetworkView();
			if(networkView != null) {
				CyNetwork network = networkView.getModel();
				// only handle event if it is a selected node
				if(e.getSource() == network.getDefaultEdgeTable() || e.getSource() == network.getDefaultNodeTable()) {
					updateHeatMap(networkView);
				}
			}
		}
	}
	
	@Override
	public void handleEvent(SetCurrentNetworkViewEvent e) {
		CyNetworkView networkView = e.getNetworkView();
		if(networkView != null && emManager.isEnrichmentMap(networkView)) {
			updateHeatMap(networkView);
		} else {
			heatMapPanel.showEmptyView();
		}
	}
	
	private void heatMapParamsChanged(HeatMapParams params) {
		CyNetworkView networkView = applicationManager.getCurrentNetworkView();
		if(networkView != null) {
			emManager.registerHeatMapParams(networkView.getModel().getSUID(), params);
		}
	}
	
	
	private void updateHeatMap(CyNetworkView networkView) {
		if(heatMapPanel == null)
			return;
		
		CyNetwork network = networkView.getModel();
		HeatMapParams params = emManager.getHeatMapParams(network.getSUID());
		if(params == null) {
			params = HeatMapParams.defaults();
			emManager.registerHeatMapParams(network.getSUID(), params);
		}
		
		final EnrichmentMap map = emManager.getEnrichmentMap(network.getSUID());
		if(map != null) {
			List<CyNode> selectedNodes = CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, true);
			List<CyEdge> selectedEdges = CyTableUtil.getEdgesInState(network, CyNetwork.SELECTED, true);
			
			String prefix = map.getParams().getAttributePrefix();
			
			Set<String> union = unionGenesets(network, selectedNodes, selectedEdges, prefix);
			Set<String> inter = intersectionGenesets(network, selectedNodes, selectedEdges, prefix);
			
			ClusterRankingOption clusterRankOption = clusterRankOptionFactory.create(map);
			List<RankingOption> rankOptions = getDataSetRankOptions(map, network, selectedNodes, selectedEdges);
			
			heatMapPanel.selectGenes(map, params, clusterRankOption, rankOptions, union, inter);
		}
	}
	
	
	private List<RankingOption> getDataSetRankOptions(EnrichmentMap map, CyNetwork network, List<CyNode> nodes, List<CyEdge> edges) {
		List<RankingOption> options = new ArrayList<>();
		for(EMDataSet dataset : map.getDataSetList()) {
			if(nodes.size() == 1 && edges.isEmpty() && dataset.getMethod() == Method.GSEA && contains(dataset, network, nodes.get(0))) {
				String name = network.getRow(nodes.get(0)).get(CyNetwork.NAME, String.class);
				options.add(new GSEALeadingEdgeRankingOption(dataset, name));
			}
			else if(contains(network, dataset, nodes, edges)) {
				Map<String,Ranking> ranks = dataset.getExpressionSets().getRanks();
				ranks.forEach((name, ranking) -> {
					options.add(new BasicRankingOption(ranking, dataset, name));
				});
			}
		}
		
		return options;
	}

	
	private static boolean contains(CyNetwork network, EMDataSet dataset, List<CyNode> nodes, List<CyEdge> edges) {
		for(CyNode node : nodes) {
			if(contains(dataset, network, node))
				return true;
		}
		for(CyEdge edge : edges) {
			if(contains(dataset, network, edge.getSource()) || contains(dataset, network, edge.getTarget()))
				return true;
		}
		return false;
	}
	
	private static boolean contains(EMDataSet dataset, CyNetwork network, CyNode node) {
		String name = network.getRow(node).get(CyNetwork.NAME, String.class);
		return dataset.getGeneSetsOfInterest().getGeneSetByName(name) != null;
	}
	
	
	private static Set<String> unionGenesets(CyNetwork network, List<CyNode> nodes, List<CyEdge> edges, String prefix) {
		Set<String> union = new HashSet<>();
		for(CyNode node : nodes) {
			union.addAll(getGenes(network, node, prefix));
		}
		for(CyEdge edge : edges) {
			union.addAll(getGenes(network, edge.getSource(), prefix));
			union.addAll(getGenes(network, edge.getTarget(), prefix));
		}
		return union;
	}
	
	
	private static Set<String> intersectionGenesets(CyNetwork network, List<CyNode> nodes, List<CyEdge> edges, String prefix) {
		Set<String> inter = null;
		for(CyNode node : nodes) {
			Collection<String> genes = getGenes(network, node, prefix);
			if(inter == null)
				inter = new HashSet<>(genes);
			else
				inter.retainAll(genes);
		}
		for(CyEdge edge : edges) {
			Collection<String> genes = getGenes(network, edge.getSource(), prefix);
			if(inter == null)
				inter = new HashSet<>(genes);
			else
				inter.retainAll(genes);
			inter.retainAll(getGenes(network, edge.getTarget(), prefix));
		}
		return inter == null ? Collections.emptySet() : inter;
	}
	
	private static Collection<String> getGenes(CyNetwork network, CyNode node, String prefix) {
		CyRow row = network.getRow(node);
		// This is already the union of all the genes across data sets
		return EMStyleBuilder.Columns.NODE_GENES.get(row, prefix, null);
	}


	
}
