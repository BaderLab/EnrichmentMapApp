package org.baderlab.csplugins.enrichmentmap.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EMSignatureDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.baderlab.csplugins.enrichmentmap.model.GenesetSimilarity;
import org.baderlab.csplugins.enrichmentmap.model.PostAnalysisFilterParameters;
import org.baderlab.csplugins.enrichmentmap.model.PostAnalysisFilterType;
import org.baderlab.csplugins.enrichmentmap.model.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.model.SimilarityKey;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder.Columns;
import org.baderlab.csplugins.enrichmentmap.style.WidthFunction;
import org.baderlab.csplugins.enrichmentmap.util.NamingUtil;
import org.baderlab.csplugins.enrichmentmap.util.NetworkUtil;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
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

public class CreateDiseaseSignatureNetworkTask extends AbstractTask implements ObservableTask {

	private static final double currentNodeYIncrement = 150.0;
	
	@Inject private CyNetworkManager networkManager;
	@Inject private CyNetworkViewManager networkViewManager;
	@Inject private CyEventHelper eventHelper;
	@Inject private Provider<WidthFunction> widthFunctionProvider;
	
	private final EnrichmentMap map;
	private final PostAnalysisParameters params;
	
	private final Map<String,GeneSet> signatureGeneSets;
	private final Map<SimilarityKey,GenesetSimilarity> geneSetSimilarities;
	
	private CreateDiseaseSignatureTaskResult.Builder taskResult = new CreateDiseaseSignatureTaskResult.Builder();
	
	
	public static interface Factory {
		CreateDiseaseSignatureNetworkTask create(EnrichmentMap map, PostAnalysisParameters params, Map<String,GeneSet> signatureGeneSets, Map<SimilarityKey,GenesetSimilarity> geneSetSimilarities);
	}
	
	@Inject
	public CreateDiseaseSignatureNetworkTask(@Assisted EnrichmentMap map, @Assisted PostAnalysisParameters params, 
			@Assisted Map<String,GeneSet> signatureGeneSets, @Assisted Map<SimilarityKey,GenesetSimilarity> geneSetSimilarities) {
		this.map = map;
		this.params = params;
		this.signatureGeneSets = signatureGeneSets;
		this.geneSetSimilarities = geneSetSimilarities;
	}
	
	
	@Override
	public void run(TaskMonitor tm) {
		CyNetwork network = networkManager.getNetwork(map.getNetworkID());
		CyNetworkView networkView = getNetworKView(network);
		taskResult.setNetwork(network);
		taskResult.setNetworkView(networkView);
		
		// Gene universe is all enrichment genes in the map
		Set<Integer> geneUniverse = map.getAllEnrichmentGenes();
		String prefix = params.getAttributePrefix();
		
		//get the node attribute and edge attribute tables
		CyTable edgeTable = createEdgeColumns(network, "", prefix);
		CyTable nodeTable = createNodeColumns(network, "", prefix);
		
		// Create the data set
		EMSignatureDataSet sigDataSet = createSignatureDataSet();
		
		// Create Signature Hub Nodes
		Set<CyNode> hubNodes = new LinkedHashSet<>(); // maintain insertion order
		for(Map.Entry<String,GeneSet> entry : signatureGeneSets.entrySet()) {
			String hubName = entry.getKey();
			GeneSet sigGeneSet = entry.getValue();
			CyNode hubNode = createHubNode(hubName, network, networkView, prefix, edgeTable, nodeTable, geneUniverse, sigDataSet, sigGeneSet);
			hubNodes.add(hubNode);
		}		
		
		// Layout nodes
		// MKTODO wtf is going on here?
		eventHelper.flushPayloadEvents(); // make sure node views have been created
		double currentNodeYOffset = 0;
		for(CyNode node : hubNodes) {
			// add currentNodeY_offset to initial Y position of the Node and increase currentNodeY_offset for the next Node
			View<CyNode> hubNodeView = networkView.getNodeView(node);
			double hubNodeY = hubNodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
			hubNodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, hubNodeY + currentNodeYOffset);
			currentNodeYOffset += currentNodeYIncrement;
		}
		
		// Create Signature Hub Edges
		for(SimilarityKey similarityKey : geneSetSimilarities.keySet()) {
			boolean passedCutoff = passesCutoff(similarityKey);
			createEdge(similarityKey, network, networkView, prefix, edgeTable, nodeTable, passedCutoff, sigDataSet);
		}

		widthFunctionProvider.get().setEdgeWidths(network, prefix, tm);
	}
	
	
	private EMSignatureDataSet createSignatureDataSet() {
		String sdsName;
		if(params.getName() == null || params.getName().trim().isEmpty()) {
			sdsName = NamingUtil.getUniqueName(params.getLoadedGMTGeneSets().getName(), map.getSignatureDataSets().keySet());
		} else {
			sdsName = params.getName();
		}
		EMSignatureDataSet signatureDataSet = new EMSignatureDataSet(sdsName);
		map.addSignatureDataSet(signatureDataSet);
		return signatureDataSet;
	}
	

	private CyNetworkView getNetworKView(CyNetwork network) {
		Collection<CyNetworkView> networkViews = networkViewManager.getNetworkViews(network);
		if(networkViews == null || networkViews.isEmpty()) {
			throw new IllegalArgumentException("No network view for: " + network);
		}
		return networkViews.iterator().next();
	}
	
	
	/**
	 * Returns true if a hub-node was actually created, false if the existing
	 * one was reused.
	 */
	private CyNode createHubNode(String hubName, CyNetwork network, CyNetworkView netView,
									String prefix, CyTable edgeTable, CyTable nodeTable, Set<Integer> geneUniverse, 
									EMSignatureDataSet sigDataSet, GeneSet sigGeneSet) {
		
		// Test for existing node first
		CyNode hubNode = NetworkUtil.getNodeWithValue(network, nodeTable, CyNetwork.NAME, hubName);
		
		if (hubNode == null) {
			hubNode = network.addNode();
			taskResult.addNewNode(hubNode);
			sigDataSet.addNodeSuid(hubNode.getSUID());
		}
		
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

		return hubNode;
	}
	
	
	/**
	 * Returns true iff the user should be warned about an existing edge that
	 * does not pass the new cutoff. If the edge already exists it will be
	 * returned, if the edge had to be created it will not be returned.
	 */
	private void createEdge(SimilarityKey similarityKey, CyNetwork network, CyNetworkView netView, String prefix, CyTable edgeTable,
			CyTable nodeTable, boolean passedCutoff, EMSignatureDataSet sigDataSet) {
		
		String edgeName = similarityKey.getCompoundName();
		GenesetSimilarity genesetSimilarity = geneSetSimilarities.get(similarityKey);
		
		CyEdge edge = null;
		if(!map.getParams().getCreateDistinctEdges())
			edge = NetworkUtil.getEdgeWithValue(network, edgeTable, CyNetwork.NAME, edgeName);
		
		if (edge == null) {
			if (passedCutoff) {
				CyNode hubNode = NetworkUtil.getNodeWithValue(network, nodeTable, CyNetwork.NAME, genesetSimilarity.getGeneset1Name());
				CyNode geneSet = NetworkUtil.getNodeWithValue(network, nodeTable, CyNetwork.NAME, genesetSimilarity.getGeneset2Name());

				if (hubNode == null || geneSet == null)
					return;

				edge = network.addEdge(hubNode, geneSet, false);
//				sigDataSet.addEdgeSuid(edge.getSUID());
				map.getDataSet(similarityKey.getName()).addEdgeSuid(edge.getSUID());
				taskResult.addNewEdge(edge);
			} else {
				return; // edge does not exist and does not pass cutoff, do nothing
			}
		} else {
			if (!passedCutoff)
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
		Columns.EDGE_DATASET.set(row, prefix, null, similarityKey.getName() /* Columns.EDGE_DATASET_VALUE_SIG */);
		
		if (passedCutoff)
			Columns.EDGE_CUTOFF_TYPE.set(row, prefix, null, params.getRankTestParameters().getType().display);

		PostAnalysisFilterType filterType = params.getRankTestParameters().getType();

		if (filterType.isMannWhitney()) {
			Columns.EDGE_MANN_WHIT_TWOSIDED_PVALUE.set(row, prefix, null, genesetSimilarity.getMannWhitPValueTwoSided());
			Columns.EDGE_MANN_WHIT_GREATER_PVALUE.set(row, prefix, null, genesetSimilarity.getMannWhitPValueGreater());
			Columns.EDGE_MANN_WHIT_LESS_PVALUE.set(row, prefix, null, genesetSimilarity.getMannWhitPValueLess());
			Columns.EDGE_MANN_WHIT_CUTOFF.set(row, prefix, null, params.getRankTestParameters().getValue());
		}

		// always calculate hypergeometric
		Columns.EDGE_HYPERGEOM_PVALUE.set(row, prefix, null, genesetSimilarity.getHypergeomPValue());
		Columns.EDGE_HYPERGEOM_U.set(row, prefix, null, genesetSimilarity.getHypergeomU());
		Columns.EDGE_HYPERGEOM_N.set(row, prefix, null, genesetSimilarity.getHypergeomN());
		Columns.EDGE_HYPERGEOM_M.set(row, prefix, null, genesetSimilarity.getHypergeomM());
		Columns.EDGE_HYPERGEOM_K.set(row, prefix, null, genesetSimilarity.getHypergeomK());
		Columns.EDGE_HYPERGEOM_CUTOFF.set(row, prefix, null, params.getRankTestParameters().getValue());
	}
	
	
	/**
	 * Why not put this in CreateDiseaseSignatureTaskParallel... don't even create the GenesetSimilarity object if it fails!!!
	 * @param similarityKey
	 * @return
	 */
	private boolean passesCutoff(SimilarityKey similarityKey) {
		GenesetSimilarity similarity = geneSetSimilarities.get(similarityKey);
		
		PostAnalysisFilterParameters filterParams = params.getRankTestParameters();
		switch (filterParams.getType()) {
			case HYPERGEOM:
				return similarity.getHypergeomPValue() <= filterParams.getValue();
			case MANN_WHIT_TWO_SIDED:
				return !similarity.isMannWhitMissingRanks() && similarity.getMannWhitPValueTwoSided() <= filterParams.getValue();
			case MANN_WHIT_GREATER:
				return !similarity.isMannWhitMissingRanks() && similarity.getMannWhitPValueGreater() <= filterParams.getValue();
			case MANN_WHIT_LESS:
				return !similarity.isMannWhitMissingRanks() && similarity.getMannWhitPValueLess() <= filterParams.getValue();
			case NUMBER:
				return similarity.getSizeOfOverlap() >= filterParams.getValue();
			case PERCENT:
				EMDataSet dataSet = map.getDataSet(similarityKey.getName());
				String enrGeneSetName = similarity.getGeneset2Name();
				GeneSet enrGeneset = dataSet.getGeneSetsOfInterest().getGeneSetByName(enrGeneSetName);
				int enrGenesetSize = enrGeneset.getGenes().size();
				double relative_per = (double) similarity.getSizeOfOverlap() / (double) enrGenesetSize;
				return relative_per >= filterParams.getValue() / 100.0;
			case SPECIFIC:
				String hubName = similarity.getGeneset1Name();
				GeneSet sigGeneSet = signatureGeneSets.get(hubName);
				int sigGeneSetSize = sigGeneSet.getGenes().size();
				double relativePer2 = (double) similarity.getSizeOfOverlap() / (double) sigGeneSetSize;
				return relativePer2 >= filterParams.getValue() / 100.0;
			default:
				return false;
		}
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
		
		return table;
	}
	
	
	@Override
	public <R> R getResults(Class<? extends R> type) {
		if (CreateDiseaseSignatureTaskResult.class.equals(type)) {
			taskResult.setCancelled(cancelled);
			return type.cast(taskResult.build());
		}
		return null;
	}

}
