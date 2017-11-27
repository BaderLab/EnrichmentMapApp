package org.baderlab.csplugins.enrichmentmap.task.postanalysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.baderlab.csplugins.enrichmentmap.model.EMSignatureDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.baderlab.csplugins.enrichmentmap.model.PostAnalysisFilterType;
import org.baderlab.csplugins.enrichmentmap.model.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.model.SignatureGenesetSimilarity;
import org.baderlab.csplugins.enrichmentmap.model.SimilarityKey;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder.Columns;
import org.baderlab.csplugins.enrichmentmap.style.WidthFunction;
import org.baderlab.csplugins.enrichmentmap.task.CreateEMNetworkTask;
import org.baderlab.csplugins.enrichmentmap.util.DiscreteTaskMonitor;
import org.baderlab.csplugins.enrichmentmap.util.NetworkUtil;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

public class CreatePANetworkTask extends AbstractTask implements ObservableTask {

	private static final double HUB_NODE_Y_GAP = 150.0;
	
	@Inject private CyNetworkManager networkManager;
	@Inject private CyNetworkViewManager networkViewManager;
	@Inject private CyEventHelper eventHelper;
	@Inject private Provider<WidthFunction> widthFunctionProvider;
	
	private final EnrichmentMap map;
	private final PostAnalysisParameters params;
	
	private final Map<String,GeneSet> signatureGeneSets;
	private final Map<SimilarityKey,SignatureGenesetSimilarity> geneSetSimilarities;
	
	// Some caches for performance reasons
	private Map<EdgeCacheKey,CyEdge> existingEdgeCache;
	private final Map<String,CyNode> nodeCache = new LinkedHashMap<>(); // maintain insertion order so layout is deterministic
	
	private CreatePANetworkTaskResult.Builder taskResult = new CreatePANetworkTaskResult.Builder();
	
	
	public static interface Factory {
		CreatePANetworkTask create(EnrichmentMap map, PostAnalysisParameters params, Map<String,GeneSet> signatureGeneSets, Map<SimilarityKey,SignatureGenesetSimilarity> geneSetSimilarities);
	}
	
	@Inject
	public CreatePANetworkTask(@Assisted EnrichmentMap map, @Assisted PostAnalysisParameters params, 
			@Assisted Map<String,GeneSet> signatureGeneSets, @Assisted Map<SimilarityKey,SignatureGenesetSimilarity> geneSetSimilarities) {
		this.map = map;
		this.params = params;
		this.signatureGeneSets = signatureGeneSets;
		this.geneSetSimilarities = geneSetSimilarities;
	}
	

	/**
	 * This task is NOT cancellable.
	 */
	@Override
	public void run(TaskMonitor tm) {
		tm.setTitle("Post Analysis Geneset Similarities...");
		
		CyNetwork network = networkManager.getNetwork(map.getNetworkID());
		CyNetworkView networkView = getNetworKView(network);
		taskResult.setNetwork(network);
		taskResult.setNetworkView(networkView);
		
		// Gene universe is all enrichment genes in the map
		Set<Integer> geneUniverse = map.getAllEnrichmentGenes();
		String prefix = params.getAttributePrefix();
		
		//get the node attribute and edge attribute tables
		tm.setStatusMessage("Creating Columns");
		CyTable edgeTable = createEdgeColumns(network, "", prefix);
		CyTable nodeTable = createNodeColumns(network, "", prefix);
		tm.setProgress(0.1);
		
		tm.setStatusMessage("Caching Nodes");
		existingEdgeCache = createExistingEdgeCache(prefix, network, edgeTable);
		tm.setProgress(0.2);
		
		EMSignatureDataSet sigDataSet = createSignatureDataSet();
		
		// Create Signature Hub Nodes
		tm.setStatusMessage("Creating Nodes");
		signatureGeneSets.forEach(sigDataSet.getGeneSetsOfInterest()::addGeneSet);
		signatureGeneSets.forEach((hubName, sigGeneSet) ->
			createHubNode(hubName, network, networkView, prefix, edgeTable, nodeTable, geneUniverse, sigGeneSet, sigDataSet)
		);
		tm.setProgress(0.3);
		
		// Layout nodes
		tm.setStatusMessage("Laying out Nodes");
		layoutHubNodes(networkView);
		tm.setProgress(0.4);
		
		// Create Signature Hub Edges
		tm.setStatusMessage("Creating Edges");
		DiscreteTaskMonitor dtm = new DiscreteTaskMonitor(tm, geneSetSimilarities.size(), 0.4, 0.9);
		dtm.setStatusMessageTemplate("Similarity {0} of {1}");
		for(SimilarityKey similarityKey : geneSetSimilarities.keySet()) {
			createEdge(similarityKey, network, networkView, prefix, edgeTable, nodeTable, sigDataSet);
			dtm.inc();
		}

		// Set edge widths
		tm.setStatusMessage("Setting Edge Widths");
		widthFunctionProvider.get().setEdgeWidths(network, prefix, tm);
		tm.setProgress(1.0);
		
		// Add the new data set to the map
		map.addSignatureDataSet(sigDataSet);
	}
	
	
	/**
	 * Note: This method does not add the data set to the map. This task can be cancelled so it should be added at the end.
	 */
	private EMSignatureDataSet createSignatureDataSet() {
		String name = params.getName();
		return new EMSignatureDataSet(map, name);
	}
	
	
	private CyNetworkView getNetworKView(CyNetwork network) {
		Collection<CyNetworkView> networkViews = networkViewManager.getNetworkViews(network);
		if(networkViews == null || networkViews.isEmpty()) {
			throw new IllegalArgumentException("No network view for: " + network);
		}
		return networkViews.iterator().next();
	}
	
	
	private static Map<EdgeCacheKey,CyEdge> createExistingEdgeCache(String prefix, CyNetwork network, CyTable edgeTable) {
		Map<EdgeCacheKey,CyEdge> cache = new HashMap<>();
		// Get rows for signature edges
		Collection<CyRow> rows = edgeTable.getMatchingRows(CyEdge.INTERACTION, PASimilarityTaskParallel.INTERACTION);
		for(CyRow row : rows) {
			String name = row.get(CyNetwork.NAME, String.class);
			Long suid = row.get(CyIdentifiable.SUID, Long.class);
			String signatureDataSetName = Columns.EDGE_SIG_DATASET.get(row, prefix);
			CyEdge edge = network.getEdge(suid);
			
			// we are assuming that the EM data set name is part of the edge name
			cache.put(new EdgeCacheKey(name, signatureDataSetName), edge);
		}
		return cache;
	}
	
	
	/**
	 * Creates a signature hub node if it doesn't already exist.
	 * Otherwise it updates the attributes of the existing node.
	 */
	private void createHubNode(String hubName, CyNetwork network, CyNetworkView netView, String prefix, 
								 CyTable edgeTable, CyTable nodeTable, Set<Integer> geneUniverse, GeneSet sigGeneSet, EMSignatureDataSet sigDataSet) {
		
		// Test for existing node first
		CyNode hubNode = NetworkUtil.getNodeWithValue(network, nodeTable, CyNetwork.NAME, hubName);
		
		if (hubNode == null) {
			hubNode = network.addNode();
			taskResult.addNewNode(hubNode);
		}
		sigDataSet.addNodeSuid(hubNode.getSUID());
		
		network.getRow(hubNode).set(CyNetwork.NAME, hubName);

		String formattedLabel = CreateEMNetworkTask.formatLabel(hubName);
		CyRow row = nodeTable.getRow(hubNode.getSUID());
		Columns.NODE_FORMATTED_NAME.set(row, prefix, null, formattedLabel);

		List<String> geneList = sigGeneSet.getGenes().stream()
				.map(map::getGeneFromHashKey)
				.filter(Objects::nonNull)
				.sorted()
				.collect(Collectors.toList());
		
		List<String> enrGeneList = sigGeneSet.getGenes().stream()
				.filter(geneUniverse::contains)
				.map(map::getGeneFromHashKey)
				.filter(Objects::nonNull)
				.sorted()
				.collect(Collectors.toList());
		
		Columns.NODE_GENES.set(row, prefix, null, geneList);
		Columns.NODE_ENR_GENES.set(row, prefix, null, enrGeneList);
		Columns.NODE_GS_DESCR.set(row, prefix, null, sigGeneSet.getDescription());
		Columns.NODE_GS_TYPE.set(row, prefix, null, Columns.NODE_GS_TYPE_SIGNATURE);
		Columns.NODE_NAME.set(row, prefix, null, sigGeneSet.getName());
		Columns.NODE_GS_SIZE.set(row, prefix, null, sigGeneSet.getGenes().size());

//		// Add the geneset of the signature node to the GenesetsOfInterest,
//		// as the Heatmap will grep it's data from there.
//		EMDataSet dataSet = map.getDataSet(dataSetName);
//		Set<Integer> sigGenesInDataSet = ImmutableSet.copyOf(Sets.intersection(sigGeneSet.getGenes(), dataSet.getDataSetGenes()));
//		GeneSet geneSetInDataSet = new GeneSet(sigGeneSet.getName(), sigGeneSet.getDescription(), sigGenesInDataSet);
//		dataSet.getGeneSetsOfInterest().getGeneSets().put(hubName, geneSetInDataSet);

		nodeCache.put(hubName, hubNode);
	}
	
	private void layoutHubNodes(CyNetworkView networkView) {
		eventHelper.flushPayloadEvents(); // make sure node views have been created
		double yOffset = 0;
		
		for (CyNode node : nodeCache.values()) {
			// add currentNodeY_offset to initial Y position of the Node and increase currentNodeY_offset for the next Node
			View<CyNode> nodeView = networkView.getNodeView(node);
			double nodeY = nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
			nodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, nodeY + yOffset);
			yOffset += HUB_NODE_Y_GAP;
		}
	}
	
	/**
	 * Returns true iff the user should be warned about an existing edge that
	 * does not pass the new cutoff. If the edge already exists it will be
	 * returned, if the edge had to be created it will not be returned.
	 */
	private void createEdge(SimilarityKey similarityKey, CyNetwork network, CyNetworkView netView, String prefix, CyTable edgeTable, CyTable nodeTable, EMSignatureDataSet sigDataSet) {
		
		// PA always generates distinct edges
		final String edgeName = similarityKey.toString();
		SignatureGenesetSimilarity genesetSimilarity = geneSetSimilarities.get(similarityKey);
		
		CyEdge edge = existingEdgeCache.get(new EdgeCacheKey(edgeName, sigDataSet.getName()));
		boolean passedCutoff = genesetSimilarity.getPassesCutoff();
		if (edge == null) {
			if (passedCutoff) {
				CyNode hubNode = nodeCache.get(genesetSimilarity.getGeneset1Name());
				CyNode geneSet = NetworkUtil.getNodeWithValue(network, nodeTable, CyNetwork.NAME, genesetSimilarity.getGeneset2Name());

				if (hubNode == null || geneSet == null)
					return;

				edge = network.addEdge(hubNode, geneSet, false);
				sigDataSet.addEdgeSuid(edge.getSUID());
//				map.getDataSet(similarityKey.getName()).addEdgeSuid(edge.getSUID());
				taskResult.addNewEdge(edge);
			} else {
				return; // edge does not exist and does not pass cutoff, do nothing
			}
		} else {
			if (!passedCutoff && existingEdgeCache.containsKey(edgeName))
				taskResult.addExistingEdgeFailsCutoff(edge);
		}

		if (passedCutoff)
			taskResult.incrementPassedCutoffCount();

		CyRow row = edgeTable.getRow(edge.getSUID());
		row.set(CyNetwork.NAME, edgeName);
		row.set(CyEdge.INTERACTION, similarityKey.getInteraction());

		List<String> geneList = new ArrayList<>();
		Set<Integer> genesHash = genesetSimilarity.getOverlappingGenes();

		for (Integer current : genesHash) {
			String gene = map.getGeneFromHashKey(current);
			if (gene != null) 
				geneList.add(gene);
		}

		Collections.sort(geneList);

		Columns.EDGE_OVERLAP_GENES.set(row, prefix, null, geneList);
		Columns.EDGE_OVERLAP_SIZE.set(row, prefix, null, genesetSimilarity.getSizeOfOverlap());
		Columns.EDGE_SIMILARITY_COEFF.set(row, prefix, null, genesetSimilarity.getSimilarityCoeffecient());
		Columns.EDGE_DATASET.set(row, prefix, null, similarityKey.getName());
		Columns.EDGE_SIG_DATASET.set(row, prefix, null, sigDataSet.getName());
		
		String dataset = genesetSimilarity.getDataSetName();
		if (passedCutoff)
			Columns.EDGE_CUTOFF_TYPE.set(row, prefix, null, params.getRankTestParameters().get(dataset).getFilterType().display);

		PostAnalysisFilterType filterType = params.getRankTestParameters().get(dataset).getFilterType();

		if (filterType.isMannWhitney()) {
			Columns.EDGE_MANN_WHIT_TWOSIDED_PVALUE.set(row, prefix, null, genesetSimilarity.getMannWhitPValueTwoSided());
			Columns.EDGE_MANN_WHIT_GREATER_PVALUE.set(row, prefix, null, genesetSimilarity.getMannWhitPValueGreater());
			Columns.EDGE_MANN_WHIT_LESS_PVALUE.set(row, prefix, null, genesetSimilarity.getMannWhitPValueLess());
			Columns.EDGE_MANN_WHIT_CUTOFF.set(row, prefix, null, params.getRankTestParameters().get(dataset).getCutoff());
		}

		// always calculate hypergeometric
		Columns.EDGE_HYPERGEOM_PVALUE.set(row, prefix, null, genesetSimilarity.getHypergeomPValue());
		Columns.EDGE_HYPERGEOM_U.set(row, prefix, null, genesetSimilarity.getHypergeomU());
		Columns.EDGE_HYPERGEOM_N.set(row, prefix, null, genesetSimilarity.getHypergeomN());
		Columns.EDGE_HYPERGEOM_M.set(row, prefix, null, genesetSimilarity.getHypergeomM());
		Columns.EDGE_HYPERGEOM_K.set(row, prefix, null, genesetSimilarity.getHypergeomK());
		Columns.EDGE_HYPERGEOM_CUTOFF.set(row, prefix, null, params.getRankTestParameters().get(dataset).getCutoff());
	}
	
	
	/**
	 * Create Node attribute table with post analysis parameters not in the main EM table
	 */
	private CyTable createNodeColumns(CyNetwork network, String name, String prefix) {
		CyTable table = network.getDefaultNodeTable();
		Columns.NODE_ENR_GENES.createColumnIfAbsent(table, prefix, null);
		Columns.NODE_GS_SIZE.createColumnIfAbsent(table, prefix, null);
		
		return table;
	}

	// create the edge attribue table
	private CyTable createEdgeColumns(CyNetwork network, String name, String prefix) {
		CyTable table = network.getDefaultEdgeTable();
		//check to see if column exists.  If it doesn't then create it
		Columns.EDGE_HYPERGEOM_PVALUE.createColumnIfAbsent(table, prefix, null);
		Columns.EDGE_HYPERGEOM_U.createColumnIfAbsent(table, prefix, null);
		Columns.EDGE_HYPERGEOM_N.createColumnIfAbsent(table, prefix, null);
		Columns.EDGE_HYPERGEOM_K.createColumnIfAbsent(table, prefix, null);
		Columns.EDGE_HYPERGEOM_M.createColumnIfAbsent(table, prefix, null);
		Columns.EDGE_HYPERGEOM_CUTOFF.createColumnIfAbsent(table, prefix, null);
		Columns.EDGE_MANN_WHIT_TWOSIDED_PVALUE.createColumnIfAbsent(table, prefix, null);
		Columns.EDGE_MANN_WHIT_GREATER_PVALUE.createColumnIfAbsent(table, prefix, null);
		Columns.EDGE_MANN_WHIT_LESS_PVALUE.createColumnIfAbsent(table, prefix, null);
		Columns.EDGE_MANN_WHIT_CUTOFF.createColumnIfAbsent(table, prefix, null);
		Columns.EDGE_CUTOFF_TYPE.createColumnIfAbsent(table, prefix, null);
		Columns.EDGE_SIG_DATASET.createColumnIfAbsent(table, prefix, null);
		return table;
	}
	
	
	@Override
	public <R> R getResults(Class<? extends R> type) {
		if (CreatePANetworkTaskResult.class.equals(type)) {
			taskResult.setCancelled(cancelled);
			return type.cast(taskResult.build());
		}
		return null;
	}

	
	
	private static class EdgeCacheKey {
		
		private final String name;
		private final String signatureDataSetName;
		
		public EdgeCacheKey(String name, String signatureDataSetName) {
			this.name = name;
			this.signatureDataSetName = signatureDataSetName;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + ((signatureDataSetName == null) ? 0 : signatureDataSetName.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			EdgeCacheKey other = (EdgeCacheKey) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			if (signatureDataSetName == null) {
				if (other.signatureDataSetName != null)
					return false;
			} else if (!signatureDataSetName.equals(other.signatureDataSetName))
				return false;
			return true;
		}
		

	}

}
