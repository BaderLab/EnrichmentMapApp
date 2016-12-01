/**
 **                       EnrichmentMap Cytoscape Plugin
 **
 ** Copyright (c) 2008-2009 Bader Lab, Donnelly Centre for Cellular and Biomolecular 
 ** Research, University of Toronto
 **
 ** Contact: http://www.baderlab.org
 **
 ** Code written by: Ruth Isserlin
 ** Authors: Daniele Merico, Ruth Isserlin, Oliver Stueker, Gary D. Bader
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** (at your option) any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** University of Toronto
 ** has no obligations to provide maintenance, support, updates, 
 ** enhancements or modifications.  In no event shall the
 ** University of Toronto
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** University of Toronto
 ** has been advised of the possibility of such damage.  
 ** See the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **
 **/

// $Id$
// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$
// $HeadURL$

package org.baderlab.csplugins.enrichmentmap.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResult;
import org.baderlab.csplugins.enrichmentmap.model.GSEAResult;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.baderlab.csplugins.enrichmentmap.model.GenericResult;
import org.baderlab.csplugins.enrichmentmap.model.GenesetSimilarity;
import org.baderlab.csplugins.enrichmentmap.model.LegacySupport;
import org.baderlab.csplugins.enrichmentmap.style.EnrichmentMapVisualStyle;
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

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * Create visual representation of enrichment map in cytoscape
 */
@Deprecated
public class CreateEnrichmentMapNetworkTask extends AbstractTask {

	public static String node_table_suffix = "node_attribs";
	public static String edge_table_suffix = "edge_attribs";
	
	@Inject private CyNetworkManager networkManager;
	@Inject private CyNetworkFactory networkFactory;
	@Inject private CyNetworkNaming networkNaming;
	@Inject private EnrichmentMapManager emManager;
	
	private final EnrichmentMap map;

	private Map<String, GenesetSimilarity> geneset_similarities;
	
	public interface Factory {
		CreateEnrichmentMapNetworkTask create(EnrichmentMap map);
	}
	
	@Inject
	public CreateEnrichmentMapNetworkTask(@Assisted EnrichmentMap map) {
		this.map = map;
		this.geneset_similarities = map.getGenesetSimilarity();
	}

	/**
	 * Compute, and create cytoscape enrichment map
	 *
	 * @return true if successful
	 */
	public boolean computeMap(TaskMonitor taskMonitor) {
		//on multiple runs of the program some of the nodes or all of them might already
		//be created but it is possible that they have different values for the attributes.  How do
		//we resolve this?
		CyNetwork network;
		final String prefix = map.getParams().getAttributePrefix();

		//create the new network.
		network = networkFactory.createNetwork();
		network.getRow(network).set(CyNetwork.NAME, networkNaming.getSuggestedNetworkTitle(LegacySupport.EM_NAME));

		//set the NetworkID in the EM parameters
		map.setNetworkID(network.getSUID());

		CyTable nodeTable = createNodeAttributes(network, map.getName().trim(), prefix);
		CyTable edgeTable = createEdgeAttributes(network, map.getName().trim(), prefix);

		// MKTODO create one attribute per dataset?xx
//		// store path to GSEA report in Network Attribute
//		if(map.getParams().getMethod() == Method.GSEA) {
//			CyTable network_table = createNetworkAttributes(network, map.getName().trim(), prefix);
//			CyRow network_row = network_table.getRow(network.getSUID());
//			if(map.getDataset(LegacySupport.DATASET1) != null
//					&& map.getDataset(LegacySupport.DATASET1).getDatasetFiles().getGseaHtmlReportFile() != null) {
//				String report1Path = map.getDataset(LegacySupport.DATASET1).getDatasetFiles().getGseaHtmlReportFile();
//				// On Windows we need to replace the Back-Slashes by forward-Slashes.
//				// Otherwise we might produce special characters (\r, \n, \t, ...) 
//				// when editing the attribute in Cytoscape.
//				// Anyway Windows supports slashes as separator in all NT based versions 
//				// (NT4, 2000, XP, Vista and newer)
//				report1Path = report1Path.replaceAll("\\\\", "/");
//				report1Path = report1Path.substring(0, report1Path.lastIndexOf('/'));
//				network_row.set(EnrichmentMapVisualStyle.NETW_REPORT1_DIR, report1Path);
//			}
//			if(map.getDataset(LegacySupport.DATASET2) != null
//					&& map.getDataset(LegacySupport.DATASET2).getDatasetFiles().getGseaHtmlReportFile() != null) {
//				String report2Path = map.getDataset(LegacySupport.DATASET2).getDatasetFiles().getGseaHtmlReportFile();
//				// On Windows we need to replace the Back-Slashes by forward-Slashes.
//				// Otherwise we might produce special characters (\r, \n, \t, ...) 
//				// when editing the attribute in Cytoscape.
//				// Anyway Windows supports slashes as separator in all NT based versions 
//				// (NT4, 2000, XP, Vista and newer)
//				report2Path = report2Path.replaceAll("\\\\", "/");
//				report2Path = report2Path.substring(0, report2Path.lastIndexOf('/'));
//				network_row.set(EnrichmentMapVisualStyle.NETW_REPORT2_DIR, report2Path);
//			}
//		}

		//Currently this supports two dataset
		//TODO:add multiple dataset support.
		//go through the datasets to get the enrichments
		//currently only 2 datasets are supported in the visualization
		Map<String, EnrichmentResult> enrichmentResults1 = null;
		Map<String, EnrichmentResult> enrichmentResults2 = null;
		Set<String> dataset_names = map.getDatasets().keySet();
		for(Iterator<String> m = dataset_names.iterator(); m.hasNext();) {
			String current_dataset = m.next();
			if(current_dataset.equalsIgnoreCase(LegacySupport.DATASET1))
				//get the enrichment results from the first one and place it in enrichment results 1
				enrichmentResults1 = map.getDataset(current_dataset).getEnrichments().getEnrichments();
			else
				enrichmentResults2 = map.getDataset(current_dataset).getEnrichments().getEnrichments();
		}

		Map<String, GeneSet> genesetsOfInterest = map.getDataset(LegacySupport.DATASET1).getGenesetsOfInterest().getGenesets();
		Map<String, GeneSet> genesetsOfInterest_set2 = null;
		if(LegacySupport.isLegacyTwoDatasets(map))
			genesetsOfInterest_set2 = map.getDataset(LegacySupport.DATASET2).getGenesetsOfInterest().getGenesets();

		int currentProgress = 0;
		int maxValue = genesetsOfInterest.size();
		if(taskMonitor != null)
			taskMonitor.setStatusMessage("Building Enrichment Map - " + maxValue + " genesets");

		//create the nodes
		//Each geneset of interest is a node
		//its size is dependent on the size of the geneset

		//on multiple runs of the program some of the nodes or all of them might already
		//be created but it is possible that they have different values for the attributes.  How do
		//we resolve this?

		//iterate through the each of the GSEA Results of interest
		for(String current_name : genesetsOfInterest.keySet()) {
			CyNode node = network.addNode();
			network.getRow(node).set(CyNetwork.NAME, current_name);

			//Add the description to the node
			GeneSet gs = null;
			GeneSet gs2 = null;
			if(!LegacySupport.isLegacyTwoDatasets(map))
				gs = (GeneSet) genesetsOfInterest.get(current_name);
			else {
				if(genesetsOfInterest.containsKey(current_name))
					gs = (GeneSet) genesetsOfInterest.get(current_name);
				if(genesetsOfInterest_set2.containsKey(current_name))
					gs2 = (GeneSet) genesetsOfInterest_set2.get(current_name);

				if(gs == null && gs2 != null)
					gs = gs2;
			}
			CyRow current_row = nodeTable.getRow(node.getSUID());
			current_row.set(prefix + EnrichmentMapVisualStyle.GS_DESCR, gs.getDescription());

			//create an attribute that stores the genes that are associated with this node as an attribute list
			//only create the list if the hashkey 2 genes is not null Otherwise it take too much time to populate the list
//			if(map.getHashkey2gene() != null) {
				Set<Integer> genes = gs.getGenes();
				if(gs2 != null)
					genes = Sets.union(genes, gs2.getGenes());
				
				List<String> gene_list = genes.stream().map(map::getGeneFromHashKey).collect(Collectors.toList());
				current_row.set(prefix + EnrichmentMapVisualStyle.GENES, gene_list);
//			}

			EnrichmentResult current_result = enrichmentResults1.get(current_name);
			setDataset1Attributes(current_row, current_result, prefix);

			//if we are using two datasets check to see if there is data for this node
			if(LegacySupport.isLegacyTwoDatasets(map) && enrichmentResults2.containsKey(current_name)) {
				EnrichmentResult second_result = enrichmentResults2.get(current_name);
				setDataset2Attributes(current_row, second_result, prefix);
			}

			// Calculate Percentage.  This must be a value between 0..100.
			int percentComplete = (int) (((double) currentProgress / maxValue) * 100);
			if(taskMonitor != null)
				taskMonitor.setProgress(percentComplete);

			currentProgress++;
		}

		//Add any additional nodes from the second dataset that haven't been added yet
		if(LegacySupport.isLegacyTwoDatasets(map)) {
			for(Iterator<String> i = genesetsOfInterest_set2.keySet().iterator(); i.hasNext();) {
				String current_name = i.next();

				//is this already a node from the first subset
				if(genesetsOfInterest.containsKey(current_name)) {
					//Don't need to add it
				} else {
					CyNode node = network.addNode();
					network.getRow(node).set(CyNetwork.NAME, current_name);

					//Add the description to the node
					GeneSet gs = null;
					GeneSet gs2 = null;
					if(!LegacySupport.isLegacyTwoDatasets(map))
						gs = (GeneSet) genesetsOfInterest.get(current_name);
					else {
						if(genesetsOfInterest.containsKey(current_name))
							gs = (GeneSet) genesetsOfInterest.get(current_name);
						if(genesetsOfInterest_set2.containsKey(current_name))
							gs2 = (GeneSet) genesetsOfInterest_set2.get(current_name);

						if(gs == null && gs2 != null)
							gs = gs2;
					}
					CyRow current_row = nodeTable.getRow(node.getSUID());
					//TODO: add own tables
					//CyRow current_row = nodeTable.getRow(current_name);
					current_row.set(prefix + EnrichmentMapVisualStyle.GS_DESCR, gs.getDescription());

					//create an attribute that stores the genes that are associated with this node as an attribute list
					//only create the list if the hashkey 2 genes is not null Otherwise it take too much time to populate the list
//					if(map.getHashkey2gene() != null) {
						List<String> gene_list = new ArrayList<String>();
						HashSet<Integer> genes_hash = new HashSet<Integer>();
						genes_hash.addAll(gs.getGenes());

						if(gs2 != null)
							genes_hash.addAll(gs2.getGenes());

						for(Iterator<Integer> j = genes_hash.iterator(); j.hasNext();) {
							Integer current = j.next();
							String gene = map.getGeneFromHashKey(current);
							if(gene_list != null)
								gene_list.add(gene);
						}

						current_row.set(prefix + EnrichmentMapVisualStyle.GENES, gene_list);
//					}

					if(enrichmentResults1.containsKey(current_name)) {
						EnrichmentResult result = enrichmentResults1.get(current_name);
						setDataset1Attributes(current_row, result, prefix);
					}
					EnrichmentResult second_result = enrichmentResults2.get(current_name);
					setDataset2Attributes(current_row, second_result, prefix);
				}
			}
		}

		//iterate through the similarities to create the edges
		for(Iterator<String> j = geneset_similarities.keySet().iterator(); j.hasNext();) {
			String current_name = j.next().toString();
			GenesetSimilarity current_result = geneset_similarities.get(current_name);

			//only create edges where the jaccard coefficient to great than
			//and if both nodes exist
			/*
			 * if(current_result.getSimilarity_coeffecient()>=map.getParams().
			 * getSimilarityCutOff() &&
			 * !getNodesWithValue(network,nodeTable,prefix +
			 * EnrichmentMapVisualStyle.NAME,
			 * current_result.getGeneset1_Name()).isEmpty() &&
			 * !getNodesWithValue(network,nodeTable,prefix +
			 * EnrichmentMapVisualStyle.NAME,current_result.getGeneset2_Name()).
			 * isEmpty()){ CyNode node1 =
			 * getUniqueNodeWithValue(network,nodeTable,prefix +
			 * EnrichmentMapVisualStyle.NAME,current_result.getGeneset1_Name());
			 * CyNode node2 = getUniqueNodeWithValue(network,nodeTable,prefix +
			 * EnrichmentMapVisualStyle.NAME,current_result.getGeneset2_Name());
			 */
			double similarity_coeffecient = current_result.getSimilarity_coeffecient();
			if(similarity_coeffecient >= map.getParams().getSimilarityCutoff()
					&& !getNodesWithValue(network, network.getDefaultNodeTable(), CyNetwork.NAME,
							current_result.getGeneset1_Name()).isEmpty()
					&& !getNodesWithValue(network, network.getDefaultNodeTable(), CyNetwork.NAME,
							current_result.getGeneset2_Name()).isEmpty()) {
				CyNode node1 = getUniqueNodeWithValue(network, network.getDefaultNodeTable(), CyNetwork.NAME,
						current_result.getGeneset1_Name());
				CyNode node2 = getUniqueNodeWithValue(network, network.getDefaultNodeTable(), CyNetwork.NAME,
						current_result.getGeneset2_Name());

				CyEdge edge = network.addEdge(node1, node2, false);
				String edge_type;
				//in order to create multiple edges we need to create different edge types between the same two nodes
				if(current_result.getEnrichment_set() == 1)
					edge_type = EnrichmentMapParameters.ENRICHMENT_INTERACTION_TYPE_SET1;
				else if(current_result.getEnrichment_set() == 2)
					edge_type = EnrichmentMapParameters.ENRICHMENT_INTERACTION_TYPE_SET2;
				else
					edge_type = EnrichmentMapParameters.ENRICHMENT_INTERACTION_TYPE;

				CyRow current_edgerow = edgeTable.getRow(/* current_name */edge.getSUID());
				current_edgerow.set(CyNetwork.NAME, current_name);
				current_edgerow.set(CyEdge.INTERACTION, current_result.getInteractionType());
				current_edgerow.set(prefix + EnrichmentMapVisualStyle.SIMILARITY_COEFFICIENT, similarity_coeffecient);
				current_edgerow.set(prefix + EnrichmentMapVisualStyle.OVERLAP_SIZE, current_result.getSizeOfOverlap());
				current_edgerow.set(prefix + EnrichmentMapVisualStyle.ENRICHMENT_SET, current_result.getEnrichment_set());

				//set the default table
				//TODO: add own tables
				//
				//CyRow current_edgerowDef = edgeTableDef.getRow(edge.getSUID());
				//current_edgerowDef.set(CyNetwork.NAME,current_name);
				//current_edgerowDef.set(CyEdge.INTERACTION, current_result.getInteractionType());

				//create an attribute that stores the genes that are associated with this edge as an attribute list
				//only create the list if the hashkey 2 genes is not null Otherwise it take too much time to populate the list
//				if(map.getHashkey2gene() != null) {
					List<String> gene_list = 
							current_result.getOverlapping_genes().stream()
							.map(map::getGeneFromHashKey)
							.collect(Collectors.toList());
					current_edgerow.set(prefix + EnrichmentMapVisualStyle.OVERLAP_GENES, gene_list);
//				}

			}
		}

		//register the network and tables
		this.networkManager.addNetwork(network);

		//TODO: add own tables
		/*
		 * this.tableManager.addTable(nodeTable);
		 * this.tableManager.addTable(edgeTable);
		 * 
		 * ArrayList<CyNetwork> networkSet = new ArrayList<CyNetwork>();
		 * networkSet.add(network);
		 * 
		 * super.insertTasksAfterCurrentTask(
		 * this.mapTableToNetworkTable.createTaskIterator(nodeTable,true,
		 * networkSet,CyNode.class )); super.insertTasksAfterCurrentTask(
		 * this.mapTableToNetworkTable.createTaskIterator(edgeTable,true,
		 * networkSet,CyEdge.class));
		 */
		//register the new Network with EM
		emManager.registerEnrichmentMap(network, map);

		return true;
	}

	
	private void setDataset1Attributes(CyRow row, EnrichmentResult result, String prefix) {
		if(result instanceof GSEAResult)
			setGSEAResultDataset1Attributes(row, (GSEAResult)result, prefix);
		else
			setGenericResultDataset1Attributes(row, (GenericResult)result, prefix);
	}
	
	private void setDataset2Attributes(CyRow row, EnrichmentResult result, String prefix) {
		if(result instanceof GSEAResult)
			setGSEAResultDataset2Attributes(row, (GSEAResult)result, prefix);
		else
			setGenericResultDataset2Attributes(row, (GenericResult)result, prefix);
	}
	
	
	/**
	 * set node attributes for dataset1 generic results
	 *
	 * @param node - node to associated attributes to
	 * @param result - generic results object to get values of the attributes
	 *            from
	 * @param prefix - attribute prefix
	 */
	private void setGenericResultDataset1Attributes(CyRow current_row, GenericResult result, String prefix) {

		if(result == null)
			return;

		//format the node name
		String formattedName = formatLabel(result.getName());

		current_row.set(prefix + EnrichmentMapVisualStyle.FORMATTED_NAME, formattedName);
		current_row.set(prefix + EnrichmentMapVisualStyle.NAME, result.getName());
		current_row.set(prefix + EnrichmentMapVisualStyle.PVALUE_DATASET1, result.getPvalue());
		current_row.set(prefix + EnrichmentMapVisualStyle.FDR_QVALUE_DATASET1, result.getFdrqvalue());
		current_row.set(prefix + EnrichmentMapVisualStyle.GS_SIZE_DATASET1, result.getGsSize());
		current_row.set(prefix + EnrichmentMapVisualStyle.GS_TYPE, EnrichmentMapVisualStyle.GS_TYPE_ENRICHMENT);
		current_row.set(prefix + EnrichmentMapVisualStyle.GS_SOURCE, result.getSource().orElse(null));
		if(result.getNES() >= 0) {
			current_row.set(prefix + EnrichmentMapVisualStyle.COLOURING_DATASET1, (1 - result.getPvalue()));
		} else {
			current_row.set(prefix + EnrichmentMapVisualStyle.COLOURING_DATASET1, ((-1) * (1 - result.getPvalue())));
		}
	}

	/**
	 * set node attributes for dataset 2 generic results
	 *
	 * @param node - node to associated attributes to
	 * @param result - generic results object to get values of the attributes
	 *            from
	 * @param prefix - attribute prefix
	 */
	private void setGenericResultDataset2Attributes(CyRow current_row, GenericResult result, String prefix) {

		if(result == null)
			return;

		//format the node name
		String formattedName = formatLabel(result.getName());

		current_row.set(prefix + EnrichmentMapVisualStyle.FORMATTED_NAME, formattedName);
		current_row.set(prefix + EnrichmentMapVisualStyle.NAME, result.getName());
		current_row.set(prefix + EnrichmentMapVisualStyle.PVALUE_DATASET2, result.getPvalue());
		current_row.set(prefix + EnrichmentMapVisualStyle.FDR_QVALUE_DATASET2, result.getFdrqvalue());
		current_row.set(prefix + EnrichmentMapVisualStyle.GS_SIZE_DATASET2, result.getGsSize());
		current_row.set(prefix + EnrichmentMapVisualStyle.GS_TYPE, EnrichmentMapVisualStyle.GS_TYPE_ENRICHMENT);
		current_row.set(prefix + EnrichmentMapVisualStyle.GS_SOURCE, result.getSource().orElse(null));
		if(result.getNES() >= 0) {
			current_row.set(prefix + EnrichmentMapVisualStyle.COLOURING_DATASET2, (1 - result.getPvalue()));
		} else {
			current_row.set(prefix + EnrichmentMapVisualStyle.COLOURING_DATASET2, ((-1) * (1 - result.getPvalue())));
		}
	}

	/**
	 * set node attributes for dataset 1 gsea results
	 *
	 * @param node - node to associated attributes to
	 * @param result - gsea results object to get values of the attributes from
	 * @param prefix - attribute prefix
	 */
	private void setGSEAResultDataset1Attributes(CyRow current_row, GSEAResult result, String prefix) {
		if(result == null)
			return;
		//format the node name
		String formattedName = formatLabel(result.getName());

		current_row.set(prefix + EnrichmentMapVisualStyle.FORMATTED_NAME, formattedName);
		current_row.set(prefix + EnrichmentMapVisualStyle.NAME, result.getName());
		current_row.set(prefix + EnrichmentMapVisualStyle.PVALUE_DATASET1, result.getPvalue());
		current_row.set(prefix + EnrichmentMapVisualStyle.FDR_QVALUE_DATASET1, result.getFdrqvalue());
		current_row.set(prefix + EnrichmentMapVisualStyle.FWER_QVALUE_DATASET1, result.getFwerqvalue());
		current_row.set(prefix + EnrichmentMapVisualStyle.GS_SIZE_DATASET1, result.getGsSize());
		current_row.set(prefix + EnrichmentMapVisualStyle.ES_DATASET1, result.getES());
		current_row.set(prefix + EnrichmentMapVisualStyle.NES_DATASET1, result.getNES());
		current_row.set(prefix + EnrichmentMapVisualStyle.GS_TYPE, EnrichmentMapVisualStyle.GS_TYPE_ENRICHMENT);
		current_row.set(prefix + EnrichmentMapVisualStyle.GS_SOURCE, result.getSource().orElse(null));
		if(result.getNES() >= 0) {
			current_row.set(prefix + EnrichmentMapVisualStyle.COLOURING_DATASET1, (1 - result.getPvalue()));
		} else {
			current_row.set(prefix + EnrichmentMapVisualStyle.COLOURING_DATASET1, ((-1) * (1 - result.getPvalue())));
		}
	}

	/**
	 * set node attributes for dataset 2 gsea results
	 *
	 * @param node - node to associated attributes to
	 * @param result - gsea results object to get values of the attributes from
	 * @param prefix - attribute prefix
	 */
	private void setGSEAResultDataset2Attributes(CyRow current_row, GSEAResult result, String prefix) {

		if(result == null)
			return;
		//format the node name
		String formattedName = formatLabel(result.getName());

		current_row.set(prefix + EnrichmentMapVisualStyle.FORMATTED_NAME, formattedName);
		current_row.set(prefix + EnrichmentMapVisualStyle.NAME, result.getName());
		current_row.set(prefix + EnrichmentMapVisualStyle.PVALUE_DATASET2, result.getPvalue());
		current_row.set(prefix + EnrichmentMapVisualStyle.FDR_QVALUE_DATASET2, result.getFdrqvalue());
		current_row.set(prefix + EnrichmentMapVisualStyle.FWER_QVALUE_DATASET2, result.getFwerqvalue());
		current_row.set(prefix + EnrichmentMapVisualStyle.GS_SIZE_DATASET2, result.getGsSize());
		current_row.set(prefix + EnrichmentMapVisualStyle.ES_DATASET2, result.getES());
		current_row.set(prefix + EnrichmentMapVisualStyle.NES_DATASET2, result.getNES());
		current_row.set(prefix + EnrichmentMapVisualStyle.GS_TYPE, EnrichmentMapVisualStyle.GS_TYPE_ENRICHMENT);
		current_row.set(prefix + EnrichmentMapVisualStyle.GS_SOURCE, result.getSource().orElse(null));
		if(result.getNES() >= 0) {
			current_row.set(prefix + EnrichmentMapVisualStyle.COLOURING_DATASET2, (1 - result.getPvalue()));
		} else {
			current_row.set(prefix + EnrichmentMapVisualStyle.COLOURING_DATASET2, ((-1) * (1 - result.getPvalue())));
		}

	}

	/**
	 * Wrap label
	 *
	 * @param label - current one line representation of label
	 * @return formatted, wrapped label
	 */
	public static String formatLabel(String label) {
		String formattedLabel = "";

		int i = 0;
		int k = 1;

		//only wrap at spaces
		String[] tokens = label.split(" ");
		//first try and wrap label based on spacing
		if(tokens.length > 1) {
			int current_count = 0;
			for(int j = 0; j < tokens.length; j++) {
				if(current_count + tokens[j].length() <= EnrichmentMapVisualStyle.maxNodeLabelLength) {
					formattedLabel = formattedLabel + tokens[j] + " ";
					current_count = current_count + tokens[j].length();
				} else if(current_count + tokens[j].length() > EnrichmentMapVisualStyle.maxNodeLabelLength) {
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
					if(current_count + tokens[j].length() <= EnrichmentMapVisualStyle.maxNodeLabelLength) {
						formattedLabel = formattedLabel + tokens[j];
						current_count = current_count + tokens[j].length();
					} else if(current_count + tokens[j].length() > EnrichmentMapVisualStyle.maxNodeLabelLength) {
						formattedLabel = formattedLabel + "\n" + tokens[j];
						current_count = tokens[j].length();
					}
				}
			}

			//if there is only one token wrap it anyways.
			else if(tokens.length == 1) {
				while(i <= label.length()) {

					if(i + EnrichmentMapVisualStyle.maxNodeLabelLength > label.length())
						formattedLabel = formattedLabel + label.substring(i, label.length()) + "\n";
					else
						formattedLabel = formattedLabel + label.substring(i, k * EnrichmentMapVisualStyle.maxNodeLabelLength) + "\n";
					i = (k * EnrichmentMapVisualStyle.maxNodeLabelLength);
					k++;
				}
			}
		}

		return formattedLabel;
	}

	//create the Nodes attribute table
	public CyTable createNetworkAttributes(CyNetwork network, String name, String prefix) {
		//TODO:change back to creating our own table.  Currently can only map to a string column.
		//in mean time use the default node table
		//CyTable nodeTable = tableFactory.createTable(/*name*/ prefix + "_" + node_table_suffix, CyNetwork.SUID, Long.class, true, true);
		CyTable networkTable = network.getDefaultNetworkTable();
		networkTable.createColumn(EnrichmentMapVisualStyle.NETW_REPORT1_DIR, String.class, false);
		if(map.getDataset(LegacySupport.DATASET2) != null
				&& map.getDataset(LegacySupport.DATASET2).getDatasetFiles().getGseaHtmlReportFile() != null)
			networkTable.createColumn(EnrichmentMapVisualStyle.NETW_REPORT2_DIR, String.class, false);

		return networkTable;

	}

	//create the Nodes attribute table
	public CyTable createNodeAttributes(CyNetwork network, String name, String prefix) {
		//TODO: add own tables
		//CyTable nodeTable = tableFactory.createTable(/*name*/ prefix + "_" + node_table_suffix, CyNetwork.NAME, String.class, true, true);
		CyTable nodeTable = network.getDefaultNodeTable();
		nodeTable.createColumn(prefix + EnrichmentMapVisualStyle.GS_DESCR, String.class, false);
		nodeTable.createColumn(prefix + EnrichmentMapVisualStyle.FORMATTED_NAME, String.class, false);
		nodeTable.createColumn(prefix + EnrichmentMapVisualStyle.NAME, String.class, false);
		nodeTable.createColumn(prefix + EnrichmentMapVisualStyle.GS_SOURCE, String.class, false);
		nodeTable.createColumn(prefix + EnrichmentMapVisualStyle.GS_TYPE, String.class, false);
		nodeTable.createListColumn(prefix + EnrichmentMapVisualStyle.GENES, String.class, false);

		nodeTable.createColumn(prefix + EnrichmentMapVisualStyle.PVALUE_DATASET1, Double.class, false);
		nodeTable.createColumn(prefix + EnrichmentMapVisualStyle.COLOURING_DATASET1, Double.class, false);
		nodeTable.createColumn(prefix + EnrichmentMapVisualStyle.ES_DATASET1, Double.class, false);
		nodeTable.createColumn(prefix + EnrichmentMapVisualStyle.NES_DATASET1, Double.class, false);
		nodeTable.createColumn(prefix + EnrichmentMapVisualStyle.FDR_QVALUE_DATASET1, Double.class, false);
		nodeTable.createColumn(prefix + EnrichmentMapVisualStyle.FWER_QVALUE_DATASET1, Double.class, false);
		nodeTable.createColumn(prefix + EnrichmentMapVisualStyle.GS_SIZE_DATASET1, Integer.class, false);

		//only create dataset2 if this map has two datasets
		if(map.getDatasets().size() > 1) {
			nodeTable.createColumn(prefix + EnrichmentMapVisualStyle.PVALUE_DATASET2, Double.class, false);
			nodeTable.createColumn(prefix + EnrichmentMapVisualStyle.COLOURING_DATASET2, Double.class, false);
			nodeTable.createColumn(prefix + EnrichmentMapVisualStyle.ES_DATASET2, Double.class, false);
			nodeTable.createColumn(prefix + EnrichmentMapVisualStyle.NES_DATASET2, Double.class, false);
			nodeTable.createColumn(prefix + EnrichmentMapVisualStyle.FDR_QVALUE_DATASET2, Double.class, false);
			nodeTable.createColumn(prefix + EnrichmentMapVisualStyle.FWER_QVALUE_DATASET2, Double.class, false);
			nodeTable.createColumn(prefix + EnrichmentMapVisualStyle.GS_SIZE_DATASET2, Integer.class, false);
		}

		return nodeTable;
	}

	//create the edge attribue table
	public CyTable createEdgeAttributes(CyNetwork network, String name, String prefix) {
		//TODO: add own tables
		//CyTable edgeTable = tableFactory.createTable(/*name*/ prefix + "_" + edge_table_suffix, CyNetwork.NAME,String.class, true, true);
		CyTable edgeTable = network.getDefaultEdgeTable();
		edgeTable.createColumn(prefix + EnrichmentMapVisualStyle.SIMILARITY_COEFFICIENT, Double.class, false);
		edgeTable.createColumn(prefix + EnrichmentMapVisualStyle.OVERLAP_SIZE, Integer.class, false);
		edgeTable.createListColumn(prefix + EnrichmentMapVisualStyle.OVERLAP_GENES, String.class, false);
		edgeTable.createColumn(prefix + EnrichmentMapVisualStyle.ENRICHMENT_SET, Integer.class, false);

		return edgeTable;
	}

	//TODO:move this method to utilities method.
	private static Set<CyNode> getNodesWithValue(final CyNetwork net, final CyTable table, final String colname, final Object value) {
		final Collection<CyRow> matchingRows = table.getMatchingRows(colname, value);
		final Set<CyNode> nodes = new HashSet<CyNode>();
		final String primaryKeyColname = table.getPrimaryKey().getName();
		for(final CyRow row : matchingRows) {
			final Long nodeId = row.get(primaryKeyColname, Long.class);
			if(nodeId == null)
				continue;
			final CyNode node = net.getNode(nodeId);
			if(node == null)
				continue;
			nodes.add(node);
		}
		return nodes;
	}

	//TODO:move this method to utilities method.
	private static CyNode getUniqueNodeWithValue(final CyNetwork net, final CyTable table, final String colname, final Object value) {
		final Collection<CyRow> matchingRows = table.getMatchingRows(colname, value);
		//if this id matches more than one node then don't return anything
		if(matchingRows.size() > 1 || matchingRows.size() <= 0)
			return null;

		final String primaryKeyColname = table.getPrimaryKey().getName();
		for(final CyRow row : matchingRows) {
			final Long nodeId = row.get(primaryKeyColname, Long.class);
			if(nodeId == null)
				continue;
			final CyNode node = net.getNode(nodeId);
			if(node == null)
				continue;
			return node;
		}
		return null;
	}

	public String getTitle() {
		return "Building Enrichment Map";
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		computeMap(taskMonitor);
	}
}
