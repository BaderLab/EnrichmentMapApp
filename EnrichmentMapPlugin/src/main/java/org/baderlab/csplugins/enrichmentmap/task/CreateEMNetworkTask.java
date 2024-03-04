package org.baderlab.csplugins.enrichmentmap.task;

import java.util.Arrays;
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
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.baderlab.csplugins.enrichmentmap.model.GenericResult;
import org.baderlab.csplugins.enrichmentmap.model.GenesetSimilarity;
import org.baderlab.csplugins.enrichmentmap.model.LegacySupport;
import org.baderlab.csplugins.enrichmentmap.model.SimilarityKey;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder.Columns;
import org.baderlab.csplugins.enrichmentmap.util.DiscreteTaskMonitor;
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
	
	private Long networkSuidResult;
	
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
	public void run(TaskMonitor tm) {
		tm.setTitle("Creating EnrichmentMap Network");
		networkSuidResult = createEMNetwork(tm);
		tm.setStatusMessage("");
	}
	
	
	@Override
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(String.class, Long.class);
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
	
	
	private Long createEMNetwork(TaskMonitor tm) {
		CyNetwork network = createNetwork();
		map.setNetworkID(network.getSUID());
		
		createNodeColumns(network);
		createEdgeColumns(network);
		
		Map<String,Set<Integer>> geneSets = map.unionAllGeneSetsOfInterest();
		Map<SimilarityKey,GenesetSimilarity> similarities = supplier.get();
		
		int numNodes = geneSets.size();
		int numEdges = similarities.size();
		
		tm.setProgress(0.1);
		
		var nodeTm = new DiscreteTaskMonitor(tm, numNodes, 0.1, 0.2).percentMessage("Creating " + numNodes + " Nodes: {0,number,#%}");
		Map<String,CyNode> nodes = createNodes(network, geneSets, nodeTm);
		
		if(cancelled)
			return null;
		
		var edgeTm = new DiscreteTaskMonitor(tm, numEdges, 0.2, 1.0).percentMessage("Creating " + numEdges + " Edges: {0,number,#%}");
		createEdges(network, nodes, similarities, edgeTm);
		
		if(cancelled)
			return null;
		
		networkManager.addNetwork(network);
		emManager.registerEnrichmentMap(map);
		
		return network.getSUID();
	}
	
	
	private CyNetwork createNetwork() {
		CyNetwork network = networkFactory.createNetwork();
		CyRootNetwork rootNetwork = ((CySubNetwork)network).getRootNetwork();
		
		String networkName = map.getParams().getNetworkName();
		if(networkName == null) {
			networkName = networkNaming.getSuggestedNetworkTitle(LegacySupport.EM_NAME);
		}
		
		rootNetwork.getRow(rootNetwork).set(CyNetwork.NAME, LegacySupport.EM_NAME);
		network.getRow(network).set(CyNetwork.NAME, networkName);
		return network;
	}
	
	
	private Map<String, CyNode> createNodes(CyNetwork network, Map<String,Set<Integer>> geneSets, DiscreteTaskMonitor tm) {
		Map<String,CyNode> nodes = new HashMap<>();
		
		for(String genesetName : geneSets.keySet()) {
			if(cancelled)
				return null;
			
			CyNode node = network.addNode();
			nodes.put(genesetName, node);
			
			// Set common attributes
			CyRow row = network.getRow(node);
			row.set(CyNetwork.NAME, genesetName);
			Columns.NODE_NAME.set(row, prefix, null, genesetName); // MKTODO why is this column needed?
			
			GeneSet geneSet = map.getGeneSet(genesetName);
			if(geneSet != null) {
				Columns.NODE_GS_DESCR.set(row, prefix, null, geneSet.getLabel());
				if(map.getParams().isParseBaderlabGeneSets()) {
					if(geneSet.getSource().isPresent()) {
						Columns.NODE_DATASOURCE.set(row, prefix, null, geneSet.getSource().get());
					}
					if(geneSet.getDatasourceId().isPresent()) {
						Columns.NODE_DATASOURCEID.set(row, prefix, null, geneSet.getDatasourceId().get());
					}
				}
				if(map.getParams().isDavid()) {
					if(geneSet.getDavidCategory().isPresent()) {
						Columns.NODE_DAVID_CATEGORY.set(row, prefix, null, geneSet.getDavidCategory().get());
					}
				}
			}
			
			Columns.NODE_GS_TYPE.set(row, prefix, null, Columns.NODE_GS_TYPE_ENRICHMENT);
			Set<Integer> geneIds = geneSets.get(genesetName);
			List<String> genes = geneIds.stream().map(map::getGeneFromHashKey).collect(Collectors.toList());
			Columns.NODE_GENES.set(row, prefix, null, genes);
			Columns.NODE_GS_SIZE.set(row, prefix, null, genes.size());
			Columns.NODE_LOG_PVALUE_MAX.set(row, prefix, null, getMaxNegLog10pval(genesetName));
			
			
			for(EMDataSet ds : map.getDataSetList()) {
				if(ds.getGeneSetsOfInterest().getGeneSets().containsKey(genesetName))
					ds.addNodeSuid(node.getSUID());
				
				var result = ds.getEnrichment(genesetName);
				
				// if result is null it will fail both instanceof checks
				if(result instanceof GSEAResult)
					setGSEAResultNodeAttributes(row, ds, (GSEAResult) result);
				else if(result instanceof GenericResult)
					setGenericResultNodeAttributes(row, ds, (GenericResult) result);
			}
			
			tm.inc();
		}
		
		return nodes;
	}
	
	
	
	private boolean needsInit = true;
	private Double minPValueThatsNotZero = null;
	
	private Double getMinPValueThatsNotZero() {
		if(needsInit) {
			Double minPValue = null;
			
			for(String genesetName : map.getAllGeneSetOfInterestNames()) {
				for(EMDataSet ds : map.getDataSetList()) { 
					var result = ds.getEnrichment(genesetName);
					if(result != null) {
						double pval = result.getPvalue();
						if(pval > 0.0) {
							minPValue = minPValue == null ? pval : Math.min(minPValue, pval);
						}
					}
				}
			}
			
			minPValueThatsNotZero = minPValue; // could still possibly be null
			needsInit = false;
		}
		return minPValueThatsNotZero;
	}
		
	
	private Double getNegLog10pval(EMDataSet dataset, EnrichmentResult result) {
		double pval = result.getPvalue();
		if(pval > 0.0) {
			return -Math.log10(pval);
		} else if(pval == 0.0) {
			Double minPval = getMinPValueThatsNotZero();
			if(minPval != null) {
				return -Math.log10(minPval);
			}
		}
		return null;
	}
	
	
	private Double getMaxNegLog10pval(String genesetName) {
		boolean hasVal = false;
		double maxVal = Double.MIN_VALUE;
		
		for(EMDataSet ds : map.getDataSetList()) {
			var result = ds.getEnrichment(genesetName);
			if(result != null) {
				double pval = result.getPvalue();
				if(pval > 0.0) {
					hasVal = true;
					maxVal = Math.max(maxVal, -Math.log10(pval));
				} else {
					Double minPval = getMinPValueThatsNotZero();
					if(minPval != null) {
						hasVal = true;
						maxVal = Math.max(maxVal, -Math.log10(minPval));
					}
				}
			}
		}
		return hasVal ? maxVal : null; // leave the cell blank
	}
	
		
	/**
	 * Note, we expect that GenesetSimilarity object that don't pass the cutoff have already been filtered out.
	 */
	private void createEdges(CyNetwork network, Map<String,CyNode> nodes, Map<SimilarityKey,GenesetSimilarity> similarities, DiscreteTaskMonitor tm) {
		for(SimilarityKey key : similarities.keySet()) {
			if(cancelled)
				return;
			
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
			
			tm.inc();
		}
	}
	
	private CyTable createNodeColumns(CyNetwork network) {
		EMCreationParameters params = map.getParams();
		CyTable table = network.getDefaultNodeTable();
		
		Columns.NODE_NAME.createColumn(table, prefix, null);// !
		Columns.NODE_GS_DESCR.createColumn(table, prefix, null);// !
		Columns.NODE_GS_TYPE.createColumn(table, prefix, null);// !
		Columns.NODE_GENES.createColumn(table, prefix, null); // Union of geneset genes across all datasets // !
		Columns.NODE_GS_SIZE.createColumn(table, prefix, null); // Size of the union // !
		Columns.NODE_LOG_PVALUE_MAX.createColumn(table, prefix, null); // max -log10(pval) for all datasets
		
		if(params.isParseBaderlabGeneSets()) {
			Columns.NODE_DATASOURCE.createColumn(table, prefix, null);
			Columns.NODE_DATASOURCEID.createColumn(table, prefix, null);
		}
		if(params.isDavid()) {
			Columns.NODE_DAVID_CATEGORY.createColumn(table, prefix, null);
		}
		
		for (EMDataSet dataset : map.getDataSetList()) {
			Columns.NODE_PVALUE.createColumn(table, prefix, dataset);
			Columns.NODE_FDR_QVALUE.createColumn(table, prefix, dataset);
			Columns.NODE_FWER_QVALUE.createColumn(table, prefix, dataset);
			// MKTODO only create these if method is GSEA?
			Columns.NODE_ES.createColumn(table, prefix, dataset);
			Columns.NODE_NES.createColumn(table, prefix, dataset); 
			Columns.NODE_LOG_PVALUE.createColumn(table, prefix, dataset); 
			Columns.NODE_LOG_PVALUE_NES.createColumn(table, prefix, dataset); 
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
		
		var log10 = getNegLog10pval(dataset, result);
		Columns.NODE_LOG_PVALUE.set(row, prefix, dataset, log10);
		Columns.NODE_LOG_PVALUE_NES.set(row, prefix, dataset, log10 * sign(result.getNES()));
	}
	
	private void setGSEAResultNodeAttributes(CyRow row, EMDataSet dataset, GSEAResult result) {
		Columns.NODE_PVALUE.set(row, prefix, dataset, result.getPvalue());
		Columns.NODE_FDR_QVALUE.set(row, prefix, dataset, result.getFdrqvalue());
		Columns.NODE_FWER_QVALUE.set(row, prefix, dataset, result.getFwerqvalue());
		Columns.NODE_ES.set(row, prefix, dataset, result.getES());
		Columns.NODE_NES.set(row, prefix, dataset, result.getNES());
		Columns.NODE_COLOURING.set(row, prefix, dataset, getColorScore(result));
		
		var log10 = getNegLog10pval(dataset, result);
		Columns.NODE_LOG_PVALUE.set(row, prefix, dataset, log10);
		Columns.NODE_LOG_PVALUE_NES.set(row, prefix, dataset, log10 * sign(result.getNES()));
		
		EMCreationParameters params = map.getParams();
		params.addPValueColumnName(Columns.NODE_PVALUE.with(prefix, dataset));
	}
	
	private static double sign(double x) {
		// Math.signum() can return 0, we don't want that
		return x < 0 ? -1 : 1;
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
