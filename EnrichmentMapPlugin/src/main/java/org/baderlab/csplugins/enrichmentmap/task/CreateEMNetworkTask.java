package org.baderlab.csplugins.enrichmentmap.task;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet.Method;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResult;
import org.baderlab.csplugins.enrichmentmap.model.GSEAResult;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.baderlab.csplugins.enrichmentmap.model.GenericResult;
import org.baderlab.csplugins.enrichmentmap.model.GenesetSimilarity;
import org.baderlab.csplugins.enrichmentmap.model.LegacySupport;
import org.baderlab.csplugins.enrichmentmap.model.SimilarityKey;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder.Columns;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class CreateEMNetworkTask extends AbstractTask implements ObservableTask {

	@Inject private CyNetworkManager networkManager;
	@Inject private CyNetworkFactory networkFactory;
	@Inject private CyNetworkNaming networkNaming;
	@Inject private EnrichmentMapManager emManager;
	
	private final EnrichmentMap map;
	private final String prefix;
	
	private final Supplier<Map<SimilarityKey,GenesetSimilarity>> supplier;
	
	private long networkSuidResult;
	
	public interface Factory {
		CreateEMNetworkTask create(EnrichmentMap map, Supplier<Map<SimilarityKey,GenesetSimilarity>> supplier);
	}
	
	@Inject
	public CreateEMNetworkTask(@Assisted EnrichmentMap map, @Assisted Supplier<Map<SimilarityKey,GenesetSimilarity>> supplier) {
		this.map = map;
		this.prefix = map.getParams().getAttributePrefix();
		this.supplier = supplier;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Creating EnrichmentMap Network");
		networkSuidResult = createEMNetwork();
		taskMonitor.setStatusMessage("");
	}
	
	@Override
	public <R> R getResults(Class<? extends R> type) {
		if(String.class.equals(type)) {
			return type.cast(String.valueOf(networkSuidResult));
		} else if(Long.class.equals(type)) {
			return type.cast(networkSuidResult);
		}
		return null;
	}
	
	private long createEMNetwork() {
		// Create the CyNetwork
		CyNetwork network = networkFactory.createNetwork();
		
		network.getRow(network).set(CyNetwork.NAME, networkNaming.getSuggestedNetworkTitle(LegacySupport.EM_NAME));
		map.setNetworkID(network.getSUID());
		
		createNodeColumns(network);
		createEdgeColumns(network);
		
		Map<String,CyNode> nodes = createNodes(network);
		createEdges(network, nodes);
		
		networkManager.addNetwork(network);
		emManager.registerEnrichmentMap(map);
		return network.getSUID();
	}
	
	private Map<String,CyNode> createNodes(CyNetwork network) {
		// Keep track of nodes as we create them, key is geneset name
		Map<String,CyNode> nodes = new HashMap<>();
		// Keep a running union of all the genes in each geneset across all datasets
		Map<String,Set<Integer>> genesetGenes = new HashMap<>();
		
		// Create nodes for all genesets of interest
		for(EMDataSet dataset : map.getDatasetList()) {
			Map<String,GeneSet> genesetsOfInterest = dataset.getGeneSetsOfInterest().getGeneSets();
			Map<String,EnrichmentResult> enrichmentResults = dataset.getEnrichments().getEnrichments();
			
			for(String genesetName : genesetsOfInterest.keySet()) {
				GeneSet gs = genesetsOfInterest.get(genesetName);
				
				CyNode node = nodes.get(genesetName);
				if(node == null) {
					node = network.addNode();
					nodes.put(genesetName, node);
					dataset.getNodeSuids().put(genesetName, node.getSUID());
					genesetGenes.put(genesetName, new HashSet<>(gs.getGenes()));
					
					CyRow row = network.getRow(node);
					row.set(CyNetwork.NAME, genesetName);
					Columns.NODE_FORMATTED_NAME.set(row, prefix, null, formatLabel(genesetName));
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
		Map<SimilarityKey,GenesetSimilarity> similarities = supplier.get();
		for(SimilarityKey key : similarities.keySet()) {
			GenesetSimilarity similarity = similarities.get(key);
			
			CyNode node1 = nodes.get(similarity.getGeneset1Name());
			CyNode node2 = nodes.get(similarity.getGeneset2Name());
			
			CyEdge edge = network.addEdge(node1, node2, false);
			
			List<String> overlapGenes = 
				similarity.getOverlappingGenes().stream()
				.map(map::getGeneFromHashKey)
				.collect(Collectors.toList());
			
			String edgeName = key.toString();
			
			CyRow row = network.getRow(edge);
			row.set(CyNetwork.NAME, edgeName);
			row.set(CyEdge.INTERACTION, similarity.getInteractionType());
			Columns.EDGE_SIMILARITY_COEFF.set(row, prefix, null, similarity.getSimilarityCoeffecient());
			Columns.EDGE_OVERLAP_SIZE.set(row, prefix, null, similarity.getSizeOfOverlap());
			Columns.EDGE_OVERLAP_GENES.set(row, prefix, null, overlapGenes);
			if(key.isCompound()) {
				Columns.EDGE_DATASET.set(row, prefix, null, Columns.EDGE_DATASET_VALUE_COMPOUND);
			} else {
				Columns.EDGE_DATASET.set(row, prefix, null, similarity.getDataSetName());
			}
		}
	}
	
	private CyTable createNodeColumns(CyNetwork network) {
		CyTable table = network.getDefaultNodeTable();
		Columns.NODE_NAME.createColumn(table, prefix, null);// !
		Columns.NODE_GS_DESCR.createColumn(table, prefix, null);// !
		Columns.NODE_GS_TYPE.createColumn(table, prefix, null);// !
		Columns.NODE_FORMATTED_NAME.createColumn(table, prefix, null); // !
		Columns.NODE_GENES.createColumn(table, prefix, null); // Union of geneset genes across all datasets // !
		Columns.NODE_GS_SIZE.createColumn(table, prefix, null); // Size of the union // !
		
		EMCreationParameters params = map.getParams();
		
		for (String datasetName : map.getDatasetNames()) {
			Columns.NODE_PVALUE.createColumn(table, prefix, datasetName);
			Columns.NODE_FDR_QVALUE.createColumn(table, prefix, datasetName);
			Columns.NODE_FWER_QVALUE.createColumn(table, prefix, datasetName);
			// MKTODO only create these if method is GSEA?
			Columns.NODE_ES.createColumn(table, prefix, datasetName);
			Columns.NODE_NES.createColumn(table, prefix, datasetName); 
			Columns.NODE_COLOURING.createColumn(table, prefix, datasetName);
			
			params.addPValueColumnName(Columns.NODE_PVALUE.with(prefix, datasetName));
			params.addQValueColumnName(Columns.NODE_FDR_QVALUE.with(prefix, datasetName));
		}
		
		return table;
	}
	
	private CyTable createEdgeColumns(CyNetwork network) {
		CyTable table = network.getDefaultEdgeTable();
		Columns.EDGE_SIMILARITY_COEFF.createColumn(table, prefix, null);
		Columns.EDGE_OVERLAP_SIZE.createColumn(table, prefix, null);
		Columns.EDGE_DATASET.createColumn(table, prefix, null);
		Columns.EDGE_OVERLAP_GENES.createColumn(table, prefix, null);
		
		map.getParams().addSimilarityCutoffColumnName(Columns.EDGE_SIMILARITY_COEFF.with(prefix, null));
		
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
		
		EMCreationParameters params = map.getParams();
		params.addPValueColumnName(Columns.NODE_PVALUE.with(prefix, datasetName));
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
	
	
	/**
	 * Wrap label
	 *
	 * @param label - current one line representation of label
	 * @return formatted, wrapped label
	 */
	public static String formatLabel(String label) {
		final int maxNodeLabelLength = 15;
		String formattedLabel = "";

		int i = 0;
		int k = 1;

		//only wrap at spaces
		String[] tokens = label.split(" ");
		//first try and wrap label based on spacing
		if(tokens.length > 1) {
			int current_count = 0;
			for(int j = 0; j < tokens.length; j++) {
				if(current_count + tokens[j].length() <= maxNodeLabelLength) {
					formattedLabel = formattedLabel + tokens[j] + " ";
					current_count = current_count + tokens[j].length();
				} else if(current_count + tokens[j].length() > maxNodeLabelLength) {
					formattedLabel = formattedLabel + "\n" + tokens[j] + " ";
					current_count = tokens[j].length();
				}
			}
		} else {
			tokens = label.split("_");

			if(tokens.length > 1) {
				int current_count = 0;
				for(int j = 0; j < tokens.length; j++) {
					if(j != 0)
						formattedLabel = formattedLabel + "_";
					if(current_count + tokens[j].length() <= maxNodeLabelLength) {
						formattedLabel = formattedLabel + tokens[j];
						current_count = current_count + tokens[j].length();
					} else if(current_count + tokens[j].length() > maxNodeLabelLength) {
						formattedLabel = formattedLabel + "\n" + tokens[j];
						current_count = tokens[j].length();
					}
				}
			}

			//if there is only one token wrap it anyways.
			else if(tokens.length == 1) {
				while(i <= label.length()) {

					if(i + maxNodeLabelLength > label.length())
						formattedLabel = formattedLabel + label.substring(i, label.length()) + "\n";
					else
						formattedLabel = formattedLabel + label.substring(i, k * maxNodeLabelLength) + "\n";
					i = (k * maxNodeLabelLength);
					k++;
				}
			}
		}

		return formattedLabel;
	}
}
