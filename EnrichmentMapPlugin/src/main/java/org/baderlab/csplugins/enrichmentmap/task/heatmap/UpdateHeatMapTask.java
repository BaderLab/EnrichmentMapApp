package org.baderlab.csplugins.enrichmentmap.task.heatmap;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.baderlab.csplugins.enrichmentmap.ApplicationModule.Edges;
import org.baderlab.csplugins.enrichmentmap.ApplicationModule.Nodes;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.baderlab.csplugins.enrichmentmap.model.GenesetSimilarity;
import org.baderlab.csplugins.enrichmentmap.model.LegacySupport;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapPanel;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class UpdateHeatMapTask extends AbstractTask {
	
	@Inject private EnrichmentMapManager emManager;
	@Inject private CyApplicationManager applicationManager;
	@Inject private @Edges HeatMapPanel edgeOverlapPanel;
	@Inject private @Nodes HeatMapPanel nodeOverlapPanel;
	
	private EnrichmentMap map;

	private List<CyNode> nodes;
	private List<CyEdge> edges;
	
	private final CytoPanel cytoPanelSouth;

	private static final ThreadLocal<Boolean> isCurrentlyFocusing = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			return Boolean.FALSE;
		}
	};
	
	public interface Factory {
		UpdateHeatMapTask create(EnrichmentMap map, @Assisted("nodes") List<CyNode> nodes, @Assisted("edges") List<CyEdge> edges, CytoPanel cytoPanelSouth);
	}
	
	
	@Inject
	public UpdateHeatMapTask(@Assisted EnrichmentMap map, @Assisted("nodes") List<CyNode> nodes, @Assisted("edges") List<CyEdge> edges, @Assisted CytoPanel cytoPanelSouth) {
		this.map = map;
		this.nodes = nodes;
		this.edges = edges;
		this.cytoPanelSouth = cytoPanelSouth;
	}

	private void createEdgesData() {
		this.setEdgeExpressionSet();
		edgeOverlapPanel.updatePanel(map);
		focusPanel(edgeOverlapPanel);
		edgeOverlapPanel.revalidate();
	}

	private void createNodesData() {
		this.setNodeExpressionSet();
		nodeOverlapPanel.setLeadingEdgeGenesetNode(nodes.isEmpty() ? null : nodes.get(0));
		nodeOverlapPanel.updatePanel(map);
		focusPanel(nodeOverlapPanel);
		nodeOverlapPanel.revalidate();
	}

	private void clearPanels() {
		nodeOverlapPanel.clearPanel();
		edgeOverlapPanel.clearPanel();
		focusPanel(nodeOverlapPanel);
		focusPanel(edgeOverlapPanel);
	}

	private void focusPanel(final HeatMapPanel panel) {
		if(emManager.isDisableHeatmapAutofocus() && !isCurrentlyFocusing.get()) {
			// Prevent this code from being reentrant.
			// There was a problem with the cytoscape event system that caused the panels to be focused over and over.
			isCurrentlyFocusing.set(true);

			try {
				if(cytoPanelSouth.getState() == CytoPanelState.HIDE) {
					cytoPanelSouth.setState(CytoPanelState.DOCK);
				}
				int index = cytoPanelSouth.indexOfComponent(panel);
				if(index != -1) {
					cytoPanelSouth.setSelectedIndex(index);
				}
			} finally {
				isCurrentlyFocusing.set(false);
			}
		}
	}

	/**
	 * Collates the current selected nodes genes to represent the expression of
	 * the genes that are in all the selected nodes. and sets the expression
	 * sets (both if there are two datasets)
	 *
	 * @param params
	 *            enrichment map parameters of the current map
	 */
	private void setNodeExpressionSet() {

		// all unique genesets - if there are two identical genesets in the two
		// sets then
		// one of them will get over written in the hash.
		// when using two distinct genesets we need to pull the gene info from
		// each set separately.
		Map<String, GeneSet> genesets = map.getAllGenesetsOfInterest();
		Map<String, GeneSet> genesets_set1 = (map.getDatasets().containsKey(LegacySupport.DATASET1))
				? map.getDataset(LegacySupport.DATASET1).getSetofgenesets().getGenesets() : null;
		Map<String, GeneSet> genesets_set2 = (map.getDatasets().containsKey(LegacySupport.DATASET2))
				? map.getDataset(LegacySupport.DATASET2).getSetofgenesets().getGenesets() : null;

		// get the current Network
		CyNetwork network = applicationManager.getCurrentNetwork();

		// go through the nodes only if there are some
		if(!nodes.isEmpty()) {

			HashSet<Integer> union = new HashSet<Integer>();

			for(CyNode current_node : nodes) {
				String nodename = network.getRow(current_node).get(CyNetwork.NAME, String.class);
				GeneSet current_geneset = genesets.get(nodename);
				Set<Integer> additional_set = null;

				// if we can't find the geneset and we are dealing with a
				// two-distinct expression sets, check for the gene set in the
				// second set
				// TODO:Add multi species support
				if(map.getParams().isDistinctExpressionSets()) {
					GeneSet current_geneset_set1 = genesets_set1.get(nodename);
					GeneSet current_geneset_set2 = genesets_set2.get(nodename);
					if(current_geneset_set1 != null && current_geneset.equals(current_geneset_set1)
							&& current_geneset_set2 != null)
						additional_set = current_geneset_set2.getGenes();
					if(current_geneset_set2 != null && current_geneset.equals(current_geneset_set2)
							&& current_geneset_set1 != null)
						additional_set = current_geneset_set1.getGenes();
				}

				if(current_geneset == null)
					continue;

				Set<Integer> current_set = current_geneset.getGenes();

				if(union == null) {
					union = new HashSet<Integer>(current_set);
				} else {
					union.addAll(current_set);
				}

				if(additional_set != null)
					union.addAll(additional_set);
			}

			HashSet<Integer> genes = union;
			this.nodeOverlapPanel.setCurrentExpressionSet(
					map.getDataset(LegacySupport.DATASET1).getExpressionSets().getExpressionMatrix(genes));
			if(map.getDataset(LegacySupport.DATASET2) != null && map.getDataset(LegacySupport.DATASET2).getExpressionSets() != null)
				this.nodeOverlapPanel.setCurrentExpressionSet2(
						map.getDataset(LegacySupport.DATASET2).getExpressionSets().getExpressionMatrix(genes));

		} else {
			this.nodeOverlapPanel.setCurrentExpressionSet(null);
			this.nodeOverlapPanel.setCurrentExpressionSet2(null);
		}

	}

	/**
	 * Collates the current selected edges genes to represent the expression of
	 * the genes that are in all the selected edges.
	 *
	 * @param params
	 *            - enrichment map parameters of the current map
	 *
	 */
	private void setEdgeExpressionSet() {
		// get the current Network
		CyNetwork network = applicationManager.getCurrentNetwork();

		if(!edges.isEmpty()) {
			HashSet<Integer> intersect = null;
			// HashSet union = null;

			for(CyEdge current_edge : edges) {
				String edgename = network.getRow(current_edge).get(CyNetwork.NAME, String.class);

				GenesetSimilarity similarity = map.getGenesetSimilarity().get(edgename);
				if(similarity == null)
					continue;

				Set<Integer> current_set = similarity.getOverlapping_genes();

				// if(intersect == null && union == null){
				if(intersect == null) {
					intersect = new HashSet<Integer>(current_set);
				} else {
					intersect.retainAll(current_set);
				}

				if(intersect.size() < 1)
					break;

			}
			this.edgeOverlapPanel.setCurrentExpressionSet(
					map.getDataset(LegacySupport.DATASET1).getExpressionSets().getExpressionMatrix(intersect));
			if(map.getDataset(LegacySupport.DATASET2) != null && map.getDataset(LegacySupport.DATASET2).getExpressionSets() != null)
				this.edgeOverlapPanel.setCurrentExpressionSet2(
						map.getDataset(LegacySupport.DATASET2).getExpressionSets().getExpressionMatrix(intersect));
		} else {
			this.edgeOverlapPanel.setCurrentExpressionSet(null);
			this.edgeOverlapPanel.setCurrentExpressionSet2(null);
		}
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		// if (Edges.size() <= Integer.parseInt(CytoscapeInit.getProperties().getProperty("EnrichmentMap.Heatmap_Edge_Limit", "100") ) )
		if(edges.size() > 0)
			createEdgesData();

		// if (Nodes.size() <= Integer.parseInt(CytoscapeInit.getProperties().getProperty("EnrichmentMap.Heatmap_Node_Limit", "50") ) )
		if(nodes.size() > 0)
			createNodesData();

		if(nodes.isEmpty() && edges.isEmpty())
			clearPanels();

	}


}
