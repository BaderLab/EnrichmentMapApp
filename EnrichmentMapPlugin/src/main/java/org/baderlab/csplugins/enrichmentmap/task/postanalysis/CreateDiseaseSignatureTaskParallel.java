package org.baderlab.csplugins.enrichmentmap.task.postanalysis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.baderlab.csplugins.enrichmentmap.model.PostAnalysisFilterType;
import org.baderlab.csplugins.enrichmentmap.model.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.model.SignatureGenesetSimilarity;
import org.baderlab.csplugins.enrichmentmap.model.SimilarityKey;
import org.baderlab.csplugins.enrichmentmap.task.ComputeSimilarityTaskParallel;
import org.baderlab.csplugins.enrichmentmap.util.DiscreteTaskMonitor;
import org.cytoscape.work.AbstractTask;
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
public class CreateDiseaseSignatureTaskParallel extends AbstractTask {

	public static final String INTERACTION = PostAnalysisParameters.SIGNATURE_INTERACTION_TYPE;

	
	@Inject private CreateDiseaseSignatureNetworkTask.Factory networkTaskFactory;
	
	private final EnrichmentMap map;
	private final PostAnalysisParameters params;
	private final List<EMDataSet> dataSets;
	
	
	public static interface Factory {
		CreateDiseaseSignatureTaskParallel create(PostAnalysisParameters params, EnrichmentMap map);
		CreateDiseaseSignatureTaskParallel create(PostAnalysisParameters params, EnrichmentMap map, List<EMDataSet> dataSets);
	}
	
	@AssistedInject
	public CreateDiseaseSignatureTaskParallel(@Assisted PostAnalysisParameters params, @Assisted EnrichmentMap map) {
		this(params, map, map.getDataSetList());
	}
	
	@AssistedInject
	public CreateDiseaseSignatureTaskParallel(@Assisted PostAnalysisParameters params, @Assisted EnrichmentMap map, @Assisted List<EMDataSet> dataSets) {
		this.map = map;
		this.dataSets = dataSets;
		
		// Make sure there's a prefix
		if(params.getAttributePrefix() == null)
			this.params = PostAnalysisParameters.Builder.from(params).setAttributePrefix("EM1_").build();
		else
			this.params = params;
	}
	
	
	@Override
	public void run(TaskMonitor tm) throws InterruptedException {
		int cpus = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(cpus);
        
        // Compare enrichment gene sets to signature gene sets
 		Set<String> enrichmentGeneSetNames = getEnrichmentGeneSetNames();
 		Map<String,GeneSet> signatureGeneSets = getSignatureGeneSets();
 		handleDuplicateNames(enrichmentGeneSetNames, signatureGeneSets);
     		
        Map<SimilarityKey,SignatureGenesetSimilarity> geneSetSimilarities = startBuildDiseaseSignatureParallel(tm, executor, enrichmentGeneSetNames, signatureGeneSets);

        // Support cancellation
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				if(cancelled) {
					executor.shutdownNow();
				}
			}
		}, 0, 1000);
			
		executor.shutdown();
		executor.awaitTermination(3, TimeUnit.HOURS);
		timer.cancel();
		
		// create the network here
		if(!cancelled) {
			Task networkTask = networkTaskFactory.create(map, params, signatureGeneSets, geneSetSimilarities);
			insertTasksAfterCurrentTask(networkTask);
		}
	}


	/**
	 * Returns immediately, need to wait on the executor to join all threads.
	 */
	private Map<SimilarityKey,SignatureGenesetSimilarity> startBuildDiseaseSignatureParallel(TaskMonitor tm, 
			ExecutorService executor, Set<String> enrichmentGeneSetNames, Map<String,GeneSet> signatureGeneSets) {
		
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
					Map<String,FilterMetric> rankTests = params.getRankTestParameters();
					
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
	
	
	/**
	 * Return the signature gene sets that the user want's to add.
	 * Take all the gene sets that were loaded and filter out ones that were not selected.
	 */
	private Map<String,GeneSet> getSignatureGeneSets() {
		Map<String,GeneSet> loadedGeneSets = params.getLoadedGMTGeneSets().getGeneSets();
		Map<String,GeneSet> selectedGeneSets = new HashMap<>();
		for(String geneset : params.getSelectedGeneSetNames()) {
			selectedGeneSets.put(geneset, loadedGeneSets.get(geneset));
		}
		return selectedGeneSets;
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
	
	
	private static DiscreteTaskMonitor discreteTaskMonitor(TaskMonitor tm, int size) {
		DiscreteTaskMonitor taskMonitor = new DiscreteTaskMonitor(tm, size);
        taskMonitor.setTitle("Post Analysis Geneset Similarities...");
        taskMonitor.setStatusMessageTemplate("Computing Geneset Similarity: {0} of {1} tasks");
        return taskMonitor;
	}
	
}
