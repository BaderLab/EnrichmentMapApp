package org.baderlab.csplugins.enrichmentmap.task.postanalysis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.baderlab.csplugins.enrichmentmap.model.PostAnalysisFilterType;
import org.baderlab.csplugins.enrichmentmap.model.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.model.SignatureGenesetSimilarity;
import org.baderlab.csplugins.enrichmentmap.model.SimilarityKey;
import org.baderlab.csplugins.enrichmentmap.task.CancellableParallelTask;
import org.baderlab.csplugins.enrichmentmap.task.ComputeSimilarityTaskParallel;
import org.baderlab.csplugins.enrichmentmap.util.DiscreteTaskMonitor;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

/**
 * Post Analysis.
 * Computes similarities between the signature gene sets that are being added and the 
 * existing gene sets in the enrichment map network.
 * 
 * Note: This task does not have side effects and is safely cancellable.
 * This task creates the EMSignatureDataSet and GenesetSimilarity objects, the next
 * task, CreateDiseaseSignatureNetworkTask has all the side effects of adding
 * nodes/edges to the network.
 */
public class PASimilarityTaskParallel extends CancellableParallelTask<Map<SimilarityKey,SignatureGenesetSimilarity>> {

	public static final String INTERACTION = PostAnalysisParameters.SIGNATURE_INTERACTION_TYPE;

	
	@Inject private CreatePANetworkTask.Factory networkTaskFactory;
	
	private final EnrichmentMap map;
	private final PostAnalysisParameters params;
	private final List<EMDataSet> dataSets;
	
	private final Map<String,GeneSet> signatureGeneSets;
	
	
	public static interface Factory {
		PASimilarityTaskParallel create(PostAnalysisParameters params, EnrichmentMap map, List<EMDataSet> dataSets);
	}
	
	
	@AssistedInject
	public PASimilarityTaskParallel(@Assisted PostAnalysisParameters params, @Assisted EnrichmentMap map, @Assisted List<EMDataSet> dataSets) {
		this.map = map;
		this.dataSets = dataSets;
		
		// Make sure there's a prefix
		if(params.getAttributePrefix() == null)
			this.params = PostAnalysisParameters.Builder.from(params).setAttributePrefix("EM1_").build();
		else
			this.params = params;
		
		Map<String,GeneSet> loadedGeneSets = params.getLoadedGMTGeneSets().getGeneSets();
		this.signatureGeneSets = new HashMap<>();
		for(String geneset : params.getSelectedGeneSetNames()) {
			signatureGeneSets.put(geneset, loadedGeneSets.get(geneset));
		}
	}
	
	@Override
	public void done(Map<SimilarityKey,SignatureGenesetSimilarity> similarities) {
		Task networkTask = networkTaskFactory.create(map, params, signatureGeneSets, similarities);
		insertTasksAfterCurrentTask(networkTask);
	}

	/**
	 * Returns immediately, need to wait on the executor to join all threads.
	 */
	@Override
	public Map<SimilarityKey,SignatureGenesetSimilarity> compute(TaskMonitor tm, ExecutorService executor) {
		Set<String> enrichmentGeneSetNames = getEnrichmentGeneSetNames();
		handleDuplicateNames(enrichmentGeneSetNames, signatureGeneSets);
		
		DiscreteTaskMonitor taskMonitor = discreteTaskMonitor(tm, signatureGeneSets.size());
		
		Set<Integer> geneUniverse = map.getAllEnrichmentGenes(); // Gene universe is all enrichment genes in the map
		
		Map<SimilarityKey, SignatureGenesetSimilarity> geneSetSimilarities = new ConcurrentHashMap<>();
		
		for(String hubName : signatureGeneSets.keySet()) {
			GeneSet sigGeneSet = signatureGeneSets.get(hubName);
			Set<Integer> sigGenesInUniverse = Sets.intersection(sigGeneSet.getGenes(), geneUniverse);
			
			// Compute similarities in batches
			executor.execute(() -> {
				loop:
				for(String geneSetName : enrichmentGeneSetNames) {
					FilterMetricSet rankTests = params.getRankTestParameters();
					
					for(EMDataSet dataSet : dataSets) {
						if(Thread.interrupted())
							break loop;
						
						GeneSet enrGeneSet = dataSet.getGeneSetsOfInterest().getGeneSetByName(geneSetName);
						if(enrGeneSet != null) {
							
							// restrict to a common gene universe
							Set<Integer> enrGenes = Sets.intersection(enrGeneSet.getGenes(), geneUniverse); // wait, is this necessary??, isn't enrGeneSet a subset of geneUniverse???
							Set<Integer> union = Sets.union(sigGeneSet.getGenes(), enrGenes);
							Set<Integer> intersection = Sets.intersection(sigGenesInUniverse, enrGenes);

							if(!intersection.isEmpty()) {
								// Jaccard or whatever from the original map
								double coeffecient = ComputeSimilarityTaskParallel.computeSimilarityCoeffecient(map.getParams(), intersection, union, sigGeneSet.getGenes(), enrGenes);
								SignatureGenesetSimilarity comparison = new SignatureGenesetSimilarity(hubName, geneSetName, coeffecient, INTERACTION, intersection, dataSet.getName());
								
								FilterMetric metric = rankTests.get(dataSet.getName());
								
								// always compute hypergeometric
								if(metric.getFilterType() != PostAnalysisFilterType.HYPERGEOM) {
									FilterMetric hypergeom = new FilterMetric.Hypergeom(PostAnalysisFilterType.HYPERGEOM.defaultValue, map.getNumberOfGenes()); // use GMT for universe size
									hypergeom.computeValue(enrGeneSet.getGenes(), sigGeneSet.getGenes(), comparison);
								}
								
								// now compute the similarity using the metric chosen by the user
								double value = metric.computeValue(enrGeneSet.getGenes(), sigGeneSet.getGenes(), comparison);
								boolean passesCutoff = metric.passes(value);
								comparison.setPassesCutoff(passesCutoff);

								// Very important that the SimilarityKey name is the dataset name.
								// This gets picked up by the CreatePANetworkTask and set on the "Dataset" edge column.
								SimilarityKey key = new SimilarityKey(hubName, geneSetName, INTERACTION, dataSet.getName());
								geneSetSimilarities.put(key, comparison);
							} 
						}
					}
				}
				taskMonitor.inc();
			});
		}
		
		return geneSetSimilarities;
	}
	
	
	private static void handleDuplicateNames(Set<String> enrichmentGeneSetNames, Map<String,GeneSet> signatureGeneSets) {
		Set<String> duplicates = signatureGeneSets.keySet().stream().filter(enrichmentGeneSetNames::contains).collect(Collectors.toSet());
		for(String name : duplicates) {
			signatureGeneSets.put("PA_" + name, signatureGeneSets.remove(name));
		}
	}
	
	/**
	 * Return the names of all the non-signature gene sets in the EnrichmentMap.
	 */
	private Set<String> getEnrichmentGeneSetNames() {
		Set<String> names = map.getAllGeneSetOfInterestNames();
		return names;
	}
	
}
