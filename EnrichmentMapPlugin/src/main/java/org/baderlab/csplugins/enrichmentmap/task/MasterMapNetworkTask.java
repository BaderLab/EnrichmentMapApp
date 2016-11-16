package org.baderlab.csplugins.enrichmentmap.task;

import static org.baderlab.csplugins.enrichmentmap.style.MasterMapVisualStyle.EDGE_OVERLAP_GENES;
import static org.baderlab.csplugins.enrichmentmap.style.MasterMapVisualStyle.EDGE_OVERLAP_SIZE;
import static org.baderlab.csplugins.enrichmentmap.style.MasterMapVisualStyle.EDGE_SIMILARITY_COEFF;
import static org.baderlab.csplugins.enrichmentmap.style.MasterMapVisualStyle.NODE_COLOURING;
import static org.baderlab.csplugins.enrichmentmap.style.MasterMapVisualStyle.NODE_ES;
import static org.baderlab.csplugins.enrichmentmap.style.MasterMapVisualStyle.NODE_FDR_QVALUE;
import static org.baderlab.csplugins.enrichmentmap.style.MasterMapVisualStyle.NODE_FORMATTED_NAME;
import static org.baderlab.csplugins.enrichmentmap.style.MasterMapVisualStyle.NODE_FWER_QVALUE;
import static org.baderlab.csplugins.enrichmentmap.style.MasterMapVisualStyle.NODE_GENES;
import static org.baderlab.csplugins.enrichmentmap.style.MasterMapVisualStyle.NODE_GS_DESCR;
import static org.baderlab.csplugins.enrichmentmap.style.MasterMapVisualStyle.NODE_GS_SIZE;
import static org.baderlab.csplugins.enrichmentmap.style.MasterMapVisualStyle.NODE_NES;
import static org.baderlab.csplugins.enrichmentmap.style.MasterMapVisualStyle.NODE_PVALUE;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.DataSet.Method;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResult;
import org.baderlab.csplugins.enrichmentmap.model.GSEAResult;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.baderlab.csplugins.enrichmentmap.model.GenericResult;
import org.baderlab.csplugins.enrichmentmap.model.GenesetSimilarity;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class MasterMapNetworkTask extends AbstractTask {

	@Inject private CyNetworkManager networkManager;
	@Inject private CyNetworkFactory networkFactory;
	@Inject private EnrichmentMapManager emManager;
	
	private final EnrichmentMap map;
	
	public interface Factory {
		MasterMapNetworkTask create(EnrichmentMap map);
	}
	
	@Inject
	public MasterMapNetworkTask(@Assisted EnrichmentMap map) {
		this.map = map;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Building Enrichment Map Network");
		createMasterMapNetwork();
		taskMonitor.setStatusMessage("");
	}
	
	private void createMasterMapNetwork() {
		// Create the CyNetwork
		CyNetwork network = networkFactory.createNetwork();
		network.getRow(network).set(CyNetwork.NAME, map.getName());
		map.setNetworkID(network.getSUID());
		
		createNodeAttributes(network);
		createEdgeAttributes(network);
		
		Map<String,CyNode> nodes = createNodes(network);
		createEdges(network, nodes);
		
		networkManager.addNetwork(network);
		emManager.registerEnrichmentMap(network, map);
	}
	
	private Map<String,CyNode> createNodes(CyNetwork network) {
		// Keep track of nodes as we create them, key is geneset name
		Map<String,CyNode> nodes = new HashMap<>();
		// Keep a running union of all the genes in each geneset across all datasets
		Map<String,Set<Integer>> genesetGenes = new HashMap<>();
		
		// Create nodes for all genesets of interest
		for(DataSet dataset : map.getDatasetList()) {
			Map<String,GeneSet> genesetsOfInterest = dataset.getGenesetsOfInterest().getGenesets();
			Map<String,EnrichmentResult> enrichmentResults = dataset.getEnrichments().getEnrichments();
			
			for(String genesetName : genesetsOfInterest.keySet()) {
				GeneSet gs = genesetsOfInterest.get(genesetName);
				
				CyNode node = nodes.get(genesetName);
				if(node == null) {
					node = network.addNode();
					nodes.put(genesetName, node);
					genesetGenes.put(genesetName, new HashSet<>(gs.getGenes()));
					
					CyRow row = network.getRow(node);
					row.set(CyNetwork.NAME, genesetName);
					NODE_FORMATTED_NAME.set(row, CreateEnrichmentMapNetworkTask.formatLabel(genesetName));
					NODE_GS_DESCR.set(row, gs.getDescription());
				}
				else {
					genesetGenes.get(genesetName).addAll(gs.getGenes());
				}
				
				EnrichmentResult result = enrichmentResults.get(genesetName);
				CyRow row = network.getRow(node);
				
				if(dataset.getMethod() == Method.GSEA)
					setGSEAResultNodeAttributes(row, dataset.getName(), (GSEAResult) result);
				else
					throw new UnsupportedOperationException("Generic not ready yet, stay tuned");
			}
		}
		
		// Set the GENES attribute
		genesetGenes.forEach((genesetName, geneIds) -> {
			CyNode node = nodes.get(genesetName);
			CyRow row = network.getRow(node);
			List<String> genes = geneIds.stream().map(map::getGeneFromHashKey).collect(Collectors.toList());
			NODE_GENES.set(row, genes);
			NODE_GS_SIZE.set(row, genes.size());
		});
		
		return nodes;
	}
	
	/**
	 * Note, we expect that GenesetSimilarity object that don't pass the cutoff have already been filtered out.
	 * @param network
	 * @param nodes
	 */
	private void createEdges(CyNetwork network, Map<String,CyNode> nodes) {
		Map<String, GenesetSimilarity> similarities = map.getGenesetSimilarity();
		for(String edgeName : similarities.keySet()) {
			GenesetSimilarity similarity = similarities.get(edgeName);
			
			CyNode node1 = nodes.get(similarity.getGeneset1_Name());
			CyNode node2 = nodes.get(similarity.getGeneset2_Name());
			
			CyEdge edge = network.addEdge(node1, node2, false);
			
			List<String> overlapGenes = 
				similarity.getOverlapping_genes().stream()
				.map(map::getGeneFromHashKey)
				.collect(Collectors.toList());
			
			CyRow row = network.getRow(edge);
			row.set(CyNetwork.NAME, edgeName);
			row.set(CyEdge.INTERACTION, similarity.getInteractionType());
			EDGE_SIMILARITY_COEFF.set(row, similarity.getSimilarity_coeffecient());
			EDGE_OVERLAP_SIZE.set(row, similarity.getSizeOfOverlap());
			EDGE_OVERLAP_GENES.set(row, overlapGenes);
		}
	}
	
	private static double getColorScore(EnrichmentResult result) {
		if(result == null)
			return 0.0;
		
		double nes;
		if(result instanceof GSEAResult)
			nes = ((GSEAResult)result).getNES();
		else
			nes = ((GenericResult)result).getNES();
			
		if(nes >= 0)
			return 1 - result.getPvalue();
		else
			return (-1) * (1 - result.getPvalue());
	}
	
	
	private CyTable createNodeAttributes(CyNetwork network) {
		CyTable table = network.getDefaultNodeTable();
		NODE_GS_DESCR.createColumn(table);
//		NODE_GS_TYPE.createColumn(table);
		NODE_FORMATTED_NAME.createColumn(table);
		NODE_GENES.createColumn(table); // Union of geneset genes across all datasets
		NODE_GS_SIZE.createColumn(table); // Size of the union
		
		for(String datasetName : map.getDatasetNames()) {
			NODE_PVALUE.createColumn(table, datasetName);
			NODE_FDR_QVALUE.createColumn(table, datasetName);
			NODE_FWER_QVALUE.createColumn(table, datasetName);
			// MKTODO only create these if method is GSEA?
			NODE_ES.createColumn(table, datasetName);
			NODE_NES.createColumn(table, datasetName); 
			NODE_COLOURING.createColumn(table, datasetName);
		}
		
		return table;
	}
	
	private CyTable createEdgeAttributes(CyNetwork network) {
		CyTable table = network.getDefaultEdgeTable();
//		for(String datasetName : map.getDatasetNames()) {
//			EDGE_SIMILARITY_COEFF.createColumn(table, datasetName);
//			EDGE_OVERLAP_SIZE.createColumn(table, datasetName);
//			EDGE_OVERLAP_GENES.createColumn(table, datasetName);
//		}
		EDGE_SIMILARITY_COEFF.createColumn(table);
		EDGE_OVERLAP_SIZE.createColumn(table);
		EDGE_OVERLAP_GENES.createColumn(table);
		return table;
	}
	
	private void setGSEAResultNodeAttributes(CyRow row, String datasetName, GSEAResult result) {
		NODE_PVALUE.set(row, datasetName, result.getPvalue());
		NODE_NES.set(row, datasetName, result.getNES());
		NODE_COLOURING.set(row, datasetName, getColorScore(result));
	}
}
