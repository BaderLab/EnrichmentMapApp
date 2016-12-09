package org.baderlab.csplugins.enrichmentmap.task;

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
import org.baderlab.csplugins.enrichmentmap.model.LegacySupport;
import org.baderlab.csplugins.enrichmentmap.style.MasterMapVisualStyle.Columns;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class MasterMapNetworkTask extends AbstractTask {

	@Inject private CyNetworkManager networkManager;
	@Inject private CyNetworkFactory networkFactory;
	@Inject private CyNetworkNaming networkNaming;
	@Inject private EnrichmentMapManager emManager;
	
	private final EnrichmentMap map;
	private final String prefix;
	
	public interface Factory {
		MasterMapNetworkTask create(EnrichmentMap map);
	}
	
	@Inject
	public MasterMapNetworkTask(@Assisted EnrichmentMap map) {
		this.map = map;
		this.prefix = map.getParams().getAttributePrefix();
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
		
		network.getRow(network).set(CyNetwork.NAME, networkNaming.getSuggestedNetworkTitle(LegacySupport.EM_NAME));
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
					Columns.NODE_FORMATTED_NAME.set(row, prefix, null, CreateEnrichmentMapNetworkTask.formatLabel(genesetName));
					Columns.NODE_NAME.set(row, prefix, null, genesetName); // MKTODO why is this column needed?
					Columns.NODE_GS_DESCR.set(row, prefix, null, gs.getDescription());
					Columns.NODE_GS_TYPE.set(row, prefix, null, Columns.NODE_GS_TYPE_ENRICHMENT);
				}
				else {
					genesetGenes.get(genesetName).addAll(gs.getGenes());
				}
				
				EnrichmentResult result = enrichmentResults.get(genesetName);
				CyRow row = network.getRow(node);
				
				if(dataset.getMethod() == Method.GSEA)
					setGSEAResultNodeAttributes(row, dataset.getName(), (GSEAResult)result);
				else
					setGenericResultNodeAttributes(row, dataset.getName(), (GenericResult)result); 
			}
		}
		
		// Set the GENES attribute
		genesetGenes.forEach((genesetName, geneIds) -> {
			CyNode node = nodes.get(genesetName);
			CyRow row = network.getRow(node);
			List<String> genes = geneIds.stream().map(map::getGeneFromHashKey).collect(Collectors.toList());
			Columns.NODE_GENES.set(row, prefix, null, genes);
			Columns.NODE_GS_SIZE.set(row, prefix, null, genes.size());
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
			Columns.EDGE_SIMILARITY_COEFF.set(row, prefix, null, similarity.getSimilarity_coeffecient());
			Columns.EDGE_OVERLAP_SIZE.set(row, prefix, null, similarity.getSizeOfOverlap());
			Columns.EDGE_OVERLAP_GENES.set(row, prefix, null, overlapGenes);
			Columns.EDGE_ENRICHMENT_SET.set(row, prefix, null, Columns.EDGE_ENRICHMENT_SET_ENR);
		}
	}
	
	private CyTable createNodeAttributes(CyNetwork network) {
		CyTable table = network.getDefaultNodeTable();
		Columns.NODE_NAME.createColumn(table, prefix, null);// !
		Columns.NODE_GS_DESCR.createColumn(table, prefix, null);// !
		Columns.NODE_GS_TYPE.createColumn(table, prefix, null);// !
		Columns.NODE_FORMATTED_NAME.createColumn(table, prefix, null); // !
		Columns.NODE_GENES.createColumn(table, prefix, null); // Union of geneset genes across all datasets // !
		Columns.NODE_GS_SIZE.createColumn(table, prefix, null); // Size of the union // !
		
		for(String datasetName : map.getDatasetNames()) {
			Columns.NODE_PVALUE.createColumn(table, prefix, datasetName);
			Columns.NODE_FDR_QVALUE.createColumn(table, prefix, datasetName);
			Columns.NODE_FWER_QVALUE.createColumn(table, prefix, datasetName);
			// MKTODO only create these if method is GSEA?
			Columns.NODE_ES.createColumn(table, prefix, datasetName);
			Columns.NODE_NES.createColumn(table, prefix, datasetName); 
			Columns.NODE_COLOURING.createColumn(table, prefix, datasetName);
		}
		
		return table;
	}
	
	private CyTable createEdgeAttributes(CyNetwork network) {
		CyTable table = network.getDefaultEdgeTable();
		Columns.EDGE_SIMILARITY_COEFF.createColumn(table, prefix, null);
		Columns.EDGE_OVERLAP_SIZE.createColumn(table, prefix, null);
		Columns.EDGE_ENRICHMENT_SET.createColumn(table, prefix, null);
		Columns.EDGE_OVERLAP_GENES.createColumn(table, prefix, null);
		return table;
	}
	
	
	private void setGenericResultNodeAttributes(CyRow row, String datasetName, GenericResult result) {
		Columns.NODE_PVALUE.set(row, prefix, datasetName, result.getPvalue());
		Columns.NODE_FDR_QVALUE.set(row, prefix, datasetName, result.getFdrqvalue());
		Columns.NODE_NES.set(row, prefix, datasetName, result.getNES());
		Columns.NODE_COLOURING.set(row, prefix, datasetName, getColorScore(result));
	}
	
	private void setGSEAResultNodeAttributes(CyRow row, String datasetName, GSEAResult result) {
		Columns.NODE_PVALUE.set(row, prefix, datasetName, result.getPvalue());
		Columns.NODE_FDR_QVALUE.set(row, prefix, datasetName, result.getFdrqvalue());
		Columns.NODE_FWER_QVALUE.set(row, prefix, datasetName, result.getFwerqvalue());
		Columns.NODE_ES.set(row, prefix, datasetName, result.getES());
		Columns.NODE_NES.set(row, prefix, datasetName, result.getNES());
		Columns.NODE_COLOURING.set(row, prefix, datasetName, getColorScore(result));
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
}
