/*
 *                       EnrichmentMap Cytoscape Plugin
 *
 * Copyright (c) 2008-2009 Bader Lab, Donnelly Centre for Cellular and Biomolecular 
 * Research, University of Toronto
 *
 * Contact: http://www.baderlab.org
 *
 * Code written by: Ruth Isserlin
 * Authors: Daniele Merico, Ruth Isserlin, Oliver Stueker, Gary D. Bader
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * University of Toronto
 * has no obligations to provide maintenance, support, updates, 
 * enhancements or modifications.  In no event shall the
 * University of Toronto
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * University of Toronto
 * has been advised of the possibility of such damage.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 *
 */

// $Id$
// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$
// $HeadURL$

package org.baderlab.csplugins.enrichmentmap.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
import org.baderlab.csplugins.enrichmentmap.model.Ranking;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder.Columns;
import org.baderlab.csplugins.enrichmentmap.style.WidthFunction;
import org.baderlab.csplugins.enrichmentmap.util.NamingUtil;
import org.baderlab.csplugins.enrichmentmap.util.NetworkUtil;
import org.baderlab.csplugins.mannwhit.MannWhitneyUTestSided;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

/**
 * Cytoscape-Task to perform Disease-Signature Post-Analysis
 */
public class CreateDiseaseSignatureTask extends AbstractTask implements ObservableTask {
	
	@Inject private CyApplicationManager applicationManager;
	@Inject private CyEventHelper eventHelper;
	@Inject private Provider<WidthFunction> widthFunctionProvider;

	private PostAnalysisParameters params;
	private final EnrichmentMap map;
	private final String interaction;
	private final String dataSetName;

	private Map<String, GeneSet> enrichmentGeneSets;
	private Map<String, GeneSet> selectedSignatureGeneSets;

	private double currentNodeYOffset;
	
	// Gene Populations:
	private Set<Integer> enrichmentGenes;
	private Set<Integer> signatureGenes;

	// Ranks
	private Ranking ranks;
	private Map<String, GenesetSimilarity> geneSetSimilarities;
	
	private EMSignatureDataSet signatureDataSet;
	private boolean createSeparateEdges;

	private CreateDiseaseSignatureTaskResult.Builder taskResult = new CreateDiseaseSignatureTaskResult.Builder();

	
	public interface Factory {
		CreateDiseaseSignatureTask create(EnrichmentMap map, PostAnalysisParameters paParams, String dataSetName);
	}
	
	
	public void setSignatureDataSet(EMSignatureDataSet signatureDataSet) {
		this.signatureDataSet = signatureDataSet;
	}
	
	private EMSignatureDataSet getSignatureDataSet() {
		if(signatureDataSet == null) {
			String sdsName = NamingUtil.getUniqueName(params.getLoadedGMTGeneSets().getName(), map.getSignatureDataSets().keySet());
			signatureDataSet = new EMSignatureDataSet(sdsName);
			map.addSignatureDataSet(signatureDataSet);
		}
		return signatureDataSet;
	}
	
	public void setCreateSeparateEdges(boolean createSeparateEdges) {
		this.createSeparateEdges = createSeparateEdges;
	}
	
	
	@Inject
	public CreateDiseaseSignatureTask(@Assisted EnrichmentMap map, @Assisted PostAnalysisParameters params, @Assisted String dataSetName) {
		this.map = map;
		this.params = params;
		this.dataSetName = dataSetName;
		this.interaction = PostAnalysisParameters.SIGNATURE_INTERACTION_TYPE;

		EMDataSet dataset = map.getDataSet(dataSetName);
		ranks = dataset.getExpressionSets().getRanks().get(params.getDataSetToRankFile().get(dataSetName));

		// we want genesets of interest that are not signature genesets put there by previous runs of post-analysis
		enrichmentGeneSets = new HashMap<>();
		
		for (Map.Entry<String, GeneSet> gs : dataset.getGeneSetsOfInterest().getGeneSets().entrySet()) {
			if (map.getEnrichmentGenesets().containsKey(gs.getKey()))
				enrichmentGeneSets.put(gs.getKey(), gs.getValue());
		}
		
		Map<String, GeneSet> loadedGeneSets = this.params.getLoadedGMTGeneSets().getGeneSets();
		geneSetSimilarities = new HashMap<>();
		selectedSignatureGeneSets = new HashMap<>();
		
		for (String geneset : params.getSelectedGeneSetNames())
			selectedSignatureGeneSets.put(geneset, loadedGeneSets.get(geneset));

		// EnrichmentGenes: pool of all genes in Enrichment Gene Sets
		// TODO: get enrichment map genes from enrichment map parameters now that they are computed there.
		enrichmentGenes = new HashSet<>();
		for (GeneSet geneSet : enrichmentGeneSets.values())
			enrichmentGenes.addAll(geneSet.getGenes());
		
		// SignatureGenes: pool of all genes in Signature Gene Sets
		signatureGenes = new HashSet<>();
		for (GeneSet geneSet : loadedGeneSets.values())
			signatureGenes.addAll(geneSet.getGenes());
	}
	
	
	private int getUniverseSize() {
		switch(params.getUniverseType()) {
			default:
			case GMT:
				return map.getNumberOfGenes();
			case EXPRESSION_SET:
				return map.getDataSet(dataSetName).getExpressionSets().getExpressionUniverse();
			case INTERSECTION:
				return map.getDataSet(dataSetName).getExpressionSets().getExpressionMatrix().size();
			case USER_DEFINED:
				return params.getUserDefinedUniverseSize();
		}
	}
	
	
	public void buildDiseaseSignature(TaskMonitor taskMonitor) {
		// Calculate Similarity between Signature Gene Sets * and Enrichment Genesets.
		int maxValue = selectedSignatureGeneSets.size() * enrichmentGeneSets.size();
		
		if (taskMonitor != null)
			taskMonitor.setStatusMessage("Computing Geneset similarity - " + maxValue + " rows");
		
		int currentProgress = 0;
		double currentNodeYIncrement = 150.0;

		try {
			CyNetwork currentNetwork = applicationManager.getCurrentNetwork();
			CyNetworkView currentView = applicationManager.getCurrentNetworkView();
			taskResult.setNetwork(currentNetwork);
			taskResult.setNetworkView(currentView);

			String prefix = params.getAttributePrefix();
			
			if (prefix == null) {
				prefix = "EM1_";
				params = PostAnalysisParameters.Builder.from(params).setAttributePrefix(prefix).build();
			}

			EMSignatureDataSet sigDataSet = getSignatureDataSet();
			
			//get the node attribute and edge attribute tables
			CyTable edgeTable = createEdgeColumns(currentNetwork, "", prefix);
			CyTable nodeTable = createNodeColumns(currentNetwork, "", prefix);

			// make a HashMap of all Nodes in the Network
			Map<String, CyNode> nodesMap = createNodeMap(currentNetwork, nodeTable, prefix);

			// Common gene universe: Intersection of EnrichmentGenes and SignatureGenes
			Set<Integer> geneUniverse = ImmutableSet.copyOf(enrichmentGenes);

			Map<String, String> duplicateGeneSets = new HashMap<>();

			// Iterate over selected Signature genesets
			for (String hubName : selectedSignatureGeneSets.keySet()) {
				// get the Signature Genes, restrict them to the Gene-Universe and add them to the Parameters
				final GeneSet sigGeneSet = selectedSignatureGeneSets.get(hubName);
				sigDataSet.getGeneSetsOfInterest().addGeneSet(hubName, sigGeneSet);

				// Check to see if the signature geneset shares the same name with an 
				// enrichment geneset. If it does, give the signature geneset a unique name
				if (enrichmentGeneSets.containsKey(hubName)) {
					duplicateGeneSets.put(hubName, "PA_" + hubName);
					hubName = "PA_" + hubName;
				}

				//the signature genes in this signature gene set 
				Set<Integer> sigGenes = sigGeneSet.getGenes();

				// the genes that are in this signature gene set as well as in the Universe of Enrichment-GMT Genes.    
				Set<Integer> sigGenesInUniverse = Sets.intersection(sigGenes, geneUniverse);

				// iterate over Enrichment Genesets
				for (String genesetName : enrichmentGeneSets.keySet()) {
					// Calculate Percentage.  This must be a value between 0..100.
					int percentComplete = (int) (((double) currentProgress / maxValue) * 100);
					
					// Estimate Time Remaining
					if (taskMonitor != null) {
						taskMonitor.setProgress(percentComplete);
						taskMonitor.setTitle("Post Analysis");
					}
					
					currentProgress++;

					if (cancelled)
						throw new InterruptedException();

					// Calculate overlap:

					// Check to see if this comparison has been done
					// The key for the set of geneset similarities is the combination of the two names.
					// Check for either variation name1_name2 or name2_name1
					String similarityKey = hubName + " (" + interaction + ") " + genesetName;
					// String similarity_key2 = geneset_name + " (" + interaction + ") " + hub_name;

					// first check to see if the terms are the same
					if (hubName.equalsIgnoreCase(genesetName)) {
						// don't compare two identical genesets
					} else if (!nodesMap.containsKey(genesetName)) {
						// skip if the Geneset is not in the Network
					} else if (Columns.NODE_GS_TYPE
							.get(nodeTable.getRow(nodesMap.get(genesetName).getSUID()), prefix, null)
							.equalsIgnoreCase(Columns.NODE_GS_TYPE_SIGNATURE)) {
						// skip if the Geneset is a Signature Node from a previous analysis
					/*
					 * } else if(geneset_similarities.containsKey(similarity_key1)
					 * 		|| geneset_similarities.containsKey(similarity_key2)) {
					 * 		//skip this geneset comparison. It has already been done.
					 */
					} else {
						//get the Enrichment geneset
						GeneSet enrGeneset = enrichmentGeneSets.get(genesetName);
						
						// restrict to a common gene universe
						Set<Integer> enrGenes = Sets.intersection(enrGeneset.getGenes(), geneUniverse);
						Set<Integer> union = Sets.union(sigGenes, enrGenes);
						Set<Integer> intersection = Sets.intersection(sigGenesInUniverse, enrGenes);

						// Only calculate Mann-Whitney pValue if there is overlap
						if (intersection.size() > 0) {
							double coeffecient = ComputeSimilarityTaskParallel.computeSimilarityCoeffecient(map.getParams(), intersection, union, sigGenes, enrGenes);
							GenesetSimilarity comparison = new GenesetSimilarity(hubName, genesetName, coeffecient, interaction, intersection);

							PostAnalysisFilterType filterType = params.getRankTestParameters().getType();
							
							switch (filterType) {
								case HYPERGEOM:
									int universeSize1 = getUniverseSize();
									hypergeometric(universeSize1, sigGenesInUniverse, enrGenes, intersection, comparison);
									break;
								case MANN_WHIT_TWO_SIDED:
								case MANN_WHIT_GREATER:
								case MANN_WHIT_LESS:
									mannWhitney(intersection, comparison);
								default: // want mann-whit to fall through
									int universeSize2 = map.getNumberOfGenes(); // #70 calculate hypergeometric also
									hypergeometric(universeSize2, sigGenesInUniverse, enrGenes, intersection, comparison);
									break;
							}

							geneSetSimilarities.put(similarityKey, comparison);
						}
					}
				}

				// Create Signature Hub Node
				boolean created = createHubNode(hubName, currentNetwork, currentView, currentNodeYOffset, prefix,
						edgeTable, nodeTable, geneUniverse, sigDataSet, sigGeneSet);
				
				if (created)
					currentNodeYOffset += currentNodeYIncrement;
			}

			// Update signature geneset map with new names of all signature genesets that have duplicates
			for (String originalHubName : duplicateGeneSets.keySet()) {
				GeneSet geneset = selectedSignatureGeneSets.remove(originalHubName);
				selectedSignatureGeneSets.put(duplicateGeneSets.get(originalHubName), geneset);
			}
			
			duplicateGeneSets.clear();

			// Create Signature Hub Edges
			for (String edgeName : geneSetSimilarities.keySet()) {
				if (cancelled)
					throw new InterruptedException();

				if (!geneSetSimilarities.get(edgeName).getInteractionType().equals(interaction))
					// skip if it's not a signature edge from the same dataset
					continue;
				if (!(this.selectedSignatureGeneSets.containsKey(geneSetSimilarities.get(edgeName).getGeneset1Name())
						|| this.selectedSignatureGeneSets.containsKey(geneSetSimilarities.get(edgeName).getGeneset2Name())))
					// skip if not either of the adjacent nodes is a SelectedSignatureGenesets of the current analysis (fixes Bug #44)
					continue;

				boolean passedCutoff = passesCutoff(edgeName);
				createEdge(edgeName, currentNetwork, currentView, prefix, edgeTable, nodeTable, passedCutoff, sigDataSet);
			}

			widthFunctionProvider.get().setEdgeWidths(currentNetwork, prefix, taskMonitor);
		} catch (InterruptedException e) {
			// TODO cancel task
		}
	}

	private boolean passesCutoff(String edgeName) {
		GenesetSimilarity similarity = geneSetSimilarities.get(edgeName);
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
				String genesetName = similarity.getGeneset2Name();
				GeneSet enrGeneset = enrichmentGeneSets.get(genesetName);
				int enrGenesetSize = enrGeneset.getGenes().size();
				double relative_per = (double) similarity.getSizeOfOverlap() / (double) enrGenesetSize;
				return relative_per >= filterParams.getValue() / 100.0;
			case SPECIFIC:
				String hubName = similarity.getGeneset1Name();
				GeneSet sigGeneSet = selectedSignatureGeneSets.get(hubName);
				int sigGeneSetSize = sigGeneSet.getGenes().size();
				double relativePer2 = (double) similarity.getSizeOfOverlap() / (double) sigGeneSetSize;
				return relativePer2 >= filterParams.getValue() / 100.0;
			default:
				return false;
		}
	}

	/**
	 * Returns true if a hub-node was actually created, false if the existing
	 * one was reused.
	 */
	private boolean createHubNode(
			String hubName,
			CyNetwork network,
			CyNetworkView netView,
			double currentNodeYOffset,
			String prefix,
			CyTable edgeTable,
			CyTable nodeTable,
			Set<Integer> geneUniverse,
			EMSignatureDataSet sigDataSet,
			GeneSet sigGeneSet
	) {
		boolean created = false;
		
		// Test for existing node first
		CyNode hubNode = NetworkUtil.getNodeWithValue(network, nodeTable, CyNetwork.NAME, hubName);
		
		if (hubNode == null) {
			hubNode = network.addNode();
			taskResult.addNewNode(hubNode);
			sigDataSet.addNodeSuid(hubNode.getSUID());
			created = true;
		}
		
		network.getRow(hubNode).set(CyNetwork.NAME, hubName);
		// flush events to make sure view has been created.
		eventHelper.flushPayloadEvents();

		// add currentNodeY_offset to initial Y position of the Node
		// and increase currentNodeY_offset for the next Node
		View<CyNode> hubNodeView = netView.getNodeView(hubNode);
		
		double hubNodeY = hubNodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
		
		if (created) // don't move nodes that already exist
			hubNodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, hubNodeY + currentNodeYOffset);

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

		// Add the geneset of the signature node to the GenesetsOfInterest,
		// as the Heatmap will grep it's data from there.
		EMDataSet dataSet = map.getDataSet(dataSetName);
		Set<Integer> sigGenesInDataSet = ImmutableSet.copyOf(Sets.intersection(sigGeneSet.getGenes(), dataSet.getDataSetGenes()));
		GeneSet geneSetInDataSet = new GeneSet(sigGeneSet.getName(), sigGeneSet.getDescription(), sigGenesInDataSet);
		dataSet.getGeneSetsOfInterest().getGeneSets().put(hubName, geneSetInDataSet);

		return created;
	}

	/**
	 * Returns true iff the user should be warned about an existing edge that
	 * does not pass the new cutoff. If the edge already exists it will be
	 * returned, if the edge had to be created it will not be returned.
	 */
	private void createEdge(String edgeName, CyNetwork network, CyNetworkView netView, String prefix, CyTable edgeTable,
			CyTable nodeTable, boolean passedCutoff, EMSignatureDataSet sigDataSet) {
		
		GenesetSimilarity genesetSimilarity = geneSetSimilarities.get(edgeName);
		CyEdge edge = null;
		if(!createSeparateEdges)
			edge = NetworkUtil.getEdgeWithValue(network, edgeTable, CyNetwork.NAME, edgeName);
		
		if (edge == null) {
			if (passedCutoff) {
				CyNode hubNode = NetworkUtil.getNodeWithValue(network, nodeTable, CyNetwork.NAME, genesetSimilarity.getGeneset1Name());
				CyNode geneSet = NetworkUtil.getNodeWithValue(network, nodeTable, CyNetwork.NAME, genesetSimilarity.getGeneset2Name());

				if (hubNode == null || geneSet == null)
					return;

				edge = network.addEdge(hubNode, geneSet, false);
				sigDataSet.addEdgeSuid(edge.getSUID());
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
		row.set(CyEdge.INTERACTION, interaction);

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
		Columns.EDGE_DATASET.set(row, prefix, null, Columns.EDGE_DATASET_VALUE_SIG);
		
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
	
	private void hypergeometric(int universeSize, Set<Integer> sigGenesInUniverse, Set<Integer> enrGenes,
			Set<Integer> intersection, GenesetSimilarity comparison) {
		// Calculate Hypergeometric pValue for Overlap
		int u = universeSize; //number of total genes (size of population / total number of balls)
		int n = sigGenesInUniverse.size(); //size of signature geneset (sample size / number of extracted balls)
		int m = enrGenes.size(); //size of enrichment geneset (success Items / number of white balls in population)
		int k = intersection.size(); //size of intersection (successes /number of extracted white balls)
		double hyperPval;

		if (k > 0)
			hyperPval = Hypergeometric.hyperGeomPvalueSum(u, n, m, k, 0);
		else // Correct p-value of empty intersections to 1 (i.e. not significant)
			hyperPval = 1.0;

		comparison.setHypergeomPValue(hyperPval);
		comparison.setHypergeomU(u);
		comparison.setHypergeomN(n);
		comparison.setHypergeomM(m);
		comparison.setHypergeomK(k);
	}

	private void mannWhitney(Set<Integer> intersection, GenesetSimilarity comparison) {
		// Calculate Mann-Whitney U pValue for Overlap
		Integer[] overlapGeneIds = intersection.toArray(new Integer[intersection.size()]);

		double[] overlapGeneScores = new double[overlapGeneIds.length];
		int j = 0;
		
		for (Integer geneId : overlapGeneIds) {
			Double score = ranks.getScore(geneId);
			
			if (score != null)
				overlapGeneScores[j++] = score; // unbox
		}

		overlapGeneScores = Arrays.copyOf(overlapGeneScores, j);
		
		if (ranks.isEmpty()) {
			comparison.setMannWhitPValueTwoSided(1.5); // avoid NoDataException
			comparison.setMannWhitPValueGreater(1.5);
			comparison.setMannWhitPValueLess(1.5);
			comparison.setMannWhitMissingRanks(true);
		} else {
			double[] scores = ranks.getScores();
			// MKTODO could modify MannWHitneyUTestSided to return all three values from one call
			MannWhitneyUTestSided mannWhit = new MannWhitneyUTestSided();
			double mannPvalTwoSided = mannWhit.mannWhitneyUTest(overlapGeneScores, scores, MannWhitneyUTestSided.Type.TWO_SIDED);
			comparison.setMannWhitPValueTwoSided(mannPvalTwoSided);
			double mannPvalGreater = mannWhit.mannWhitneyUTest(overlapGeneScores, scores, MannWhitneyUTestSided.Type.GREATER);
			comparison.setMannWhitPValueGreater(mannPvalGreater);
			double mannPvalLess = mannWhit.mannWhitneyUTest(overlapGeneScores, scores, MannWhitneyUTestSided.Type.LESS);
			comparison.setMannWhitPValueLess(mannPvalLess);
		}
	}

	private Map<String, CyNode> createNodeMap(CyNetwork network, CyTable nodeTable, String prefix) {
		Map<String, CyNode> nodesMap = new HashMap<>();
		
		for (CyNode node : network.getNodeList()) {
			CyRow row = nodeTable.getRow(node.getSUID());
			String name = Columns.NODE_NAME.get(row, prefix, null);
			nodesMap.put(name, node);
		}

		return nodesMap;
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
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Generating Signature Hubs");
		buildDiseaseSignature(taskMonitor);
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
