package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.baderlab.csplugins.enrichmentmap.PropertyManager;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet.Method;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.model.Ranking;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder;
import org.baderlab.csplugins.enrichmentmap.util.CoalesceTimerStore;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParams.Compress;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParams.Operator;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Updates the HeatMap panel in response to Cytoscape events.
 */
@Singleton
public class HeatMapMediator implements RowsSetListener, SetCurrentNetworkViewListener {

	private static final int COLLAPSE_THRESHOLD = 50;
	
	@Inject private HeatMapParentPanel.Factory panelFactory;
	@Inject private EnrichmentMapManager emManager;
	@Inject private PropertyManager propertyManager;
	@Inject private ClusterRankingOption.Factory clusterRankOptionFactory;
	
	@Inject private CyNetworkManager networkManager;
	@Inject private CyServiceRegistrar serviceRegistrar;
	@Inject private CySwingApplication swingApplication;
	@Inject private CyApplicationManager applicationManager;
	
	private final CoalesceTimerStore<HeatMapMediator> selectionEventTimer = new CoalesceTimerStore<>(60);
	private HeatMapParentPanel heatMapPanel = null;
	private boolean onlyEdges;
	
	
	public void showHeatMapPanel() {
		try {
			heatMapPanel = (HeatMapParentPanel) serviceRegistrar.getService(CytoPanelComponent.class, "(id=" + HeatMapParentPanel.ID + ")");
		} catch (Exception ex) { }
		
		if(heatMapPanel == null) {
			heatMapPanel = panelFactory.create(this);
			Properties props = new Properties();
			props.setProperty("id", HeatMapParentPanel.ID);
			serviceRegistrar.registerService(heatMapPanel, CytoPanelComponent.class, props);
		}
		
		bringToFront();
	}

	private void bringToFront() {
		CytoPanel cytoPanel = swingApplication.getCytoPanel(heatMapPanel.getCytoPanelName());
		if(cytoPanel != null) {
			int index = cytoPanel.indexOfComponent(HeatMapParentPanel.ID);
			if(index >= 0) {
				cytoPanel.setSelectedIndex(index);
			}
		}
	}
	
	@Override
	public void handleEvent(RowsSetEvent e) {
		if(heatMapPanel == null)
			return;
		if(e.containsColumn(CyNetwork.SELECTED)) {
			CyNetworkView networkView = applicationManager.getCurrentNetworkView();
			if(networkView != null) {
				CyNetwork network = networkView.getModel();
				// only handle event if it is a selected node
				if(e.getSource() == network.getDefaultEdgeTable() || e.getSource() == network.getDefaultNodeTable()) {
					selectionEventTimer.coalesce(this, () -> updateHeatMap(networkView));
				}
			}
		}
	}
	
	
	@Override
	public void handleEvent(SetCurrentNetworkViewEvent e) {
		if(heatMapPanel == null)
			return;
		CyNetworkView networkView = e.getNetworkView();
		if(networkView != null && emManager.isEnrichmentMap(networkView)) {
			updateHeatMap(networkView);
		} else {
			heatMapPanel.showEmptyView();
		}
	}
	
	/**
	 * Callback that HeatMapMainPanel calls.
	 */
	public void heatMapParamsChanged(HeatMapParams params) {
		CyNetworkView networkView = applicationManager.getCurrentNetworkView();
		if(networkView != null) {
			Long suid = networkView.getModel().getSUID();
			EnrichmentMap map = emManager.getEnrichmentMap(suid);
			
			// Typically the user will want Union for nodes and Intersection for edges, so we keep
			// two HeatMapParams objects in order to keep the Operator field separate.
			// However we do want to share the other fields, so we copy them over to the other object.
			
			HeatMapParams otherParams = getHeatMapParams(map, suid, !onlyEdges);
			HeatMapParams newOtherParams = new HeatMapParams.Builder(params).setOperator(otherParams.getOperator()).build();
			
			emManager.registerHeatMapParams(suid, !onlyEdges, newOtherParams);
			emManager.registerHeatMapParams(suid, onlyEdges, params);
		}
	}
	
	
	private void updateHeatMap(CyNetworkView networkView) {
		if(heatMapPanel == null)
			return;
		
		CyNetwork network = networkView.getModel();
		EnrichmentMap map = emManager.getEnrichmentMap(network.getSUID());
		if(map == null)
			return;
		
		List<CyNode> selectedNodes = CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, true);
		List<CyEdge> selectedEdges = CyTableUtil.getEdgesInState(network, CyNetwork.SELECTED, true);
		
		String prefix = map.getParams().getAttributePrefix();
		this.onlyEdges = selectedNodes.isEmpty() && !selectedEdges.isEmpty();
		
		Set<String> union = unionGenesets(network, selectedNodes, selectedEdges, prefix);
		Set<String> inter = intersectionGenesets(network, selectedNodes, selectedEdges, prefix);
		List<RankingOption> rankOptions = getDataSetRankOptions(map, network, selectedNodes, selectedEdges);
		
		HeatMapParams params = getHeatMapParams(map, network.getSUID(), onlyEdges);
		
		heatMapPanel.selectGenes(map, params, rankOptions, union, inter);
		
		if(propertyManager.getValue(PropertyManager.HEATMAP_AUTOFOCUS)) {
			bringToFront();
		}
	}
	
	
	private HeatMapParams getHeatMapParams(EnrichmentMap map, Long networkSUID, boolean onlyEdges) {
		HeatMapParams params = emManager.getHeatMapParams(networkSUID, onlyEdges);
		if(params == null) {
			HeatMapParams.Builder builder = new HeatMapParams.Builder();
			
			if(map.totalExpressionCount() > COLLAPSE_THRESHOLD) 
				builder.setCompress(Compress.MEDIAN);
			if(onlyEdges)
				builder.setOperator(Operator.INTERSECTION);
			
			params = builder.build();
			emManager.registerHeatMapParams(networkSUID, onlyEdges, params);
		}
		return params;
	}
	
	
	public ClusterRankingOption getClusterRankOption(EnrichmentMap map) {
		return clusterRankOptionFactory.create(map);
	}
	
	public List<RankingOption> getDataSetRankOptions(EnrichmentMap map) {
		CyNetwork network = networkManager.getNetwork(map.getNetworkID());
		List<CyNode> selectedNodes = CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, true);
		List<CyEdge> selectedEdges = CyTableUtil.getEdgesInState(network, CyNetwork.SELECTED, true);
		return getDataSetRankOptions(map, network, selectedNodes, selectedEdges);
	}
	
	private List<RankingOption> getDataSetRankOptions(EnrichmentMap map, CyNetwork network, List<CyNode> nodes, List<CyEdge> edges) {
		List<RankingOption> options = new ArrayList<>();
		for(EMDataSet dataset : map.getDataSetList()) {
			if(nodes.size() == 1 && edges.isEmpty() && dataset.getMethod() == Method.GSEA) {
				String geneSetName = network.getRow(nodes.get(0)).get(CyNetwork.NAME, String.class);
				Map<String,Ranking> ranks = dataset.getRanks();
				ranks.forEach((name, ranking) -> {
					options.add(new GSEALeadingEdgeRankingOption(dataset, geneSetName, name));
				});
			} else {
				Map<String,Ranking> ranks = dataset.getRanks();
				ranks.forEach((name, ranking) -> {
					options.add(new BasicRankingOption(ranking, dataset, name));
				});
			}
		}
		return options;
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
