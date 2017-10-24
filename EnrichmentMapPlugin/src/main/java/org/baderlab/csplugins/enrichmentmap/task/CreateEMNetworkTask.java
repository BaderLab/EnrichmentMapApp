package org.baderlab.csplugins.enrichmentmap.task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResult;
import org.baderlab.csplugins.enrichmentmap.model.GSEAResult;
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
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
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
		CyRootNetwork rootNetwork = ((CySubNetwork)network).getRootNetwork();
		
		String networkName = map.getParams().getNetworkName();
		if(networkName == null) {
			networkName = networkNaming.getSuggestedNetworkTitle(LegacySupport.EM_NAME);
		}
		
		rootNetwork.getRow(rootNetwork).set(CyNetwork.NAME, LegacySupport.EM_NAME);
		network.getRow(network).set(CyNetwork.NAME, networkName);
		
		map.setNetworkID(network.getSUID());
		
		createNodeColumns(network);
		createEdgeColumns(network);
		
		Map<String,CyNode> nodes = createNodes(network);
		createEdges(network, nodes);
		
		networkManager.addNetwork(network);
		emManager.registerEnrichmentMap(map);
		return network.getSUID();
	}
	
	private Map<String, CyNode> createNodes(CyNetwork network) {
		Map<String,CyNode> nodes = new HashMap<>();
		
		Map<String,Set<Integer>> geneSets = map.unionAllGeneSetsOfInterest();
		
		for(String genesetName : geneSets.keySet()) {
			CyNode node = network.addNode();
			nodes.put(genesetName, node);
			
			// Set common attributes
			CyRow row = network.getRow(node);
			row.set(CyNetwork.NAME, genesetName);
			Columns.NODE_FORMATTED_NAME.set(row, prefix, null, formatLabel(genesetName));
			Columns.NODE_NAME.set(row, prefix, null, genesetName); // MKTODO why is this column needed?
			Columns.NODE_GS_DESCR.set(row, prefix, null, map.findGeneSetDescription(genesetName));
			Columns.NODE_GS_TYPE.set(row, prefix, null, Columns.NODE_GS_TYPE_ENRICHMENT);
			Set<Integer> geneIds = geneSets.get(genesetName);
			List<String> genes = geneIds.stream().map(map::getGeneFromHashKey).collect(Collectors.toList());
			Columns.NODE_GENES.set(row, prefix, null, genes);
			Columns.NODE_GS_SIZE.set(row, prefix, null, genes.size());
			
			// Set attributes specific to each dataset
			for(EMDataSet ds : map.getDataSetList()) {
				if(ds.getGeneSetsOfInterest().getGeneSets().containsKey(genesetName))
					ds.addNodeSuid(node.getSUID());
				
				Map<String, EnrichmentResult> enrichmentResults = ds.getEnrichments().getEnrichments();
				EnrichmentResult result = enrichmentResults.get(genesetName);
				
				// if result is null it will fail both instanceof checks
				if(result instanceof GSEAResult)
					setGSEAResultNodeAttributes(row, ds, (GSEAResult) result);
				else if(result instanceof GenericResult)
					setGenericResultNodeAttributes(row, ds, (GenericResult) result);
			}
		}
		
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
			
			String datasetName = key.getName();
			if(datasetName != null) {
				EMDataSet dataset = map.getDataSet(datasetName);
				if(dataset != null) {
					dataset.addEdgeSuid(edge.getSUID());
				}
			}
			
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
		
		for (EMDataSet dataset : map.getDataSetList()) {
			Columns.NODE_PVALUE.createColumn(table, prefix, dataset);
			Columns.NODE_FDR_QVALUE.createColumn(table, prefix, dataset);
			Columns.NODE_FWER_QVALUE.createColumn(table, prefix, dataset);
			// MKTODO only create these if method is GSEA?
			Columns.NODE_ES.createColumn(table, prefix, dataset);
			Columns.NODE_NES.createColumn(table, prefix, dataset); 
			Columns.NODE_COLOURING.createColumn(table, prefix, dataset);
			
			params.addPValueColumnName(Columns.NODE_PVALUE.with(prefix, dataset));
			params.addQValueColumnName(Columns.NODE_FDR_QVALUE.with(prefix, dataset));
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
	
	private void setGenericResultNodeAttributes(CyRow row, EMDataSet dataset, GenericResult result) {
		Columns.NODE_PVALUE.set(row, prefix, dataset, result.getPvalue());
		Columns.NODE_FDR_QVALUE.set(row, prefix, dataset, result.getFdrqvalue());
		Columns.NODE_NES.set(row, prefix, dataset, result.getNES());
		Columns.NODE_COLOURING.set(row, prefix, dataset, getColorScore(result));
	}
	
	private void setGSEAResultNodeAttributes(CyRow row, EMDataSet dataset, GSEAResult result) {
		Columns.NODE_PVALUE.set(row, prefix, dataset, result.getPvalue());
		Columns.NODE_FDR_QVALUE.set(row, prefix, dataset, result.getFdrqvalue());
		Columns.NODE_FWER_QVALUE.set(row, prefix, dataset, result.getFwerqvalue());
		Columns.NODE_ES.set(row, prefix, dataset, result.getES());
		Columns.NODE_NES.set(row, prefix, dataset, result.getNES());
		Columns.NODE_COLOURING.set(row, prefix, dataset, getColorScore(result));
		
		EMCreationParameters params = map.getParams();
		params.addPValueColumnName(Columns.NODE_PVALUE.with(prefix, dataset));
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
