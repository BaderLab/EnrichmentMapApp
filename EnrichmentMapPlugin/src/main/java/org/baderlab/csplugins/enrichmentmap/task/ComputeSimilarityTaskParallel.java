package org.baderlab.csplugins.enrichmentmap.task;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.SimilarityMetric;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.baderlab.csplugins.enrichmentmap.model.GenesetSimilarity;
import org.baderlab.csplugins.enrichmentmap.model.SimilarityKey;
import org.baderlab.csplugins.enrichmentmap.util.DiscreteTaskMonitor;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.common.collect.Sets;

/**
 * Three cases:
 * - single edges
 * - multiple edges
 * - compound edges
 */
public class ComputeSimilarityTaskParallel extends AbstractTask {

	private final EnrichmentMap map;
	private final Consumer<Map<SimilarityKey,GenesetSimilarity>> consumer;
	
	public ComputeSimilarityTaskParallel(EnrichmentMap map, Consumer<Map<SimilarityKey,GenesetSimilarity>> consumer) {
		this.map = map;
		this.consumer = consumer;
	}
	
	@Override
	public void run(TaskMonitor tm) throws InterruptedException {
		int cpus = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(cpus);
        
        boolean compound = map.isDistinctExpressionSets() ? !map.getParams().getCreateDistinctEdges() : true;
        boolean distinct = map.isDistinctExpressionSets() && map.getParams().getCreateDistinctEdges();
        
        Map<SimilarityKey,GenesetSimilarity> similarities = startComputeSimilarities(tm, executor, distinct, compound);

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
		
		if(!cancelled)
			consumer.accept(similarities);
	}
	
	
	private Map<SimilarityKey,GenesetSimilarity> startComputeSimilarities(TaskMonitor tm, ExecutorService executor, boolean distinct, boolean compound) {
		Set<String> names = map.getAllGeneSetOfInterestNames();
		Map<String,Set<Integer>> unionedGenesets = compound ? map.unionAllGeneSetsOfInterest() : null;
		
		DiscreteTaskMonitor taskMonitor = discreteTaskMonitor(tm, names.size());
		String edgeType = map.getParams().getEnrichmentEdgeType();
		Map<SimilarityKey,GenesetSimilarity> similarities = new ConcurrentHashMap<>();
		
		Collection<EMDataSet> dataSets = map.getDataSetList();
		
		for(final String geneset1Name : names) {
			// Compute similarities in batches, creating a Runnable for every similarity pair would create too many objects
			executor.execute(() -> {
				for(final String geneset2Name : names) {
					if (geneset1Name.equalsIgnoreCase(geneset2Name))
						continue; //don't compare two identical gene sets
					
					if(distinct) {
						for(EMDataSet dataset : dataSets) {
							SimilarityKey key = new SimilarityKey(geneset1Name, geneset2Name, edgeType, dataset.getName());
							
							if(!similarities.containsKey(key)) {
								Map<String,GeneSet> genesets = dataset.getGeneSetsOfInterest().getGeneSets();
								GeneSet geneset1 = genesets.get(geneset1Name);
								GeneSet geneset2 = genesets.get(geneset2Name);
								
								if(geneset1 != null && geneset2 != null) {
									// returns null if the similarity coefficient doesn't pass the cutoff
									GenesetSimilarity similarity = computeGenesetSimilarity(map.getParams(), geneset1Name, geneset2Name, geneset1.getGenes(), geneset2.getGenes(), dataset.getName());
									if(similarity != null) {
										similarities.put(key, similarity);
									}
								}
							}
						}
					}
					
					if(compound) {
						SimilarityKey key = new SimilarityKey(geneset1Name, geneset2Name, edgeType, null);
						
						if(!similarities.containsKey(key)) {
							Set<Integer> geneset1 = unionedGenesets.get(geneset1Name);
							Set<Integer> geneset2 = unionedGenesets.get(geneset2Name);
							
							// returns null if the similarity coefficient doesn't pass the cutoff
							GenesetSimilarity similarity = computeGenesetSimilarity(map.getParams(), geneset1Name, geneset2Name, geneset1, geneset2, "compound");
							if(similarity != null) {
								similarities.put(key, similarity);
							}
						}
					}
				}
				
				taskMonitor.inc();
			});
		}
		
		return similarities;
	}
	
	
	private static DiscreteTaskMonitor discreteTaskMonitor(TaskMonitor tm, int size) {
		DiscreteTaskMonitor taskMonitor = new DiscreteTaskMonitor(tm, size);
        taskMonitor.setTitle("Computing Geneset Similarities...");
        taskMonitor.setStatusMessageTemplate("Computing Geneset Similarity: {0} of {1} tasks");
        return taskMonitor;
	}

	
	public static double computeSimilarityCoeffecient(EMCreationParameters params, Set<?> intersection, Set<?> union, Set<?> genes1, Set<?> genes2) {
		// Note: Do not call intersection.size() or union.size() more than once on a Guava SetView! 
		// It is a potentially slow operation that needs to be recalcuated each time it is called.
		
		if (params.getSimilarityMetric() == SimilarityMetric.JACCARD) {
			return (double) intersection.size() / (double) union.size();
		} 
		else if (params.getSimilarityMetric() == SimilarityMetric.OVERLAP) {
			return (double) intersection.size() / Math.min((double) genes1.size(), (double) genes2.size());
		} 
		else { 
			// It must be combined. Compute a combination of the overlap and jaccard coefecient. We need both the Jaccard and the Overlap.
			double intersectionSize = (double) intersection.size(); // do not call size() more than once on the same SetView
			
			double jaccard = intersectionSize / (double) union.size();
			double overlap = intersectionSize / Math.min((double) genes1.size(), (double) genes2.size());

			double k = params.getCombinedConstant();

			return (k * overlap) + ((1 - k) * jaccard);
		}
	}

	
	static GenesetSimilarity computeGenesetSimilarity(EMCreationParameters params, String geneset1Name, String geneset2Name, Set<Integer> geneset1, Set<Integer> geneset2, String dataset) {
		Set<Integer> intersection = Sets.intersection(geneset1, geneset2);
		Set<Integer> union = Sets.union(geneset1, geneset2);

		double coeffecient = computeSimilarityCoeffecient(params, intersection, union, geneset1, geneset2);
		
		if(coeffecient < params.getSimilarityCutoff())
			return null;
			
		String edgeType = params.getEnrichmentEdgeType();
		GenesetSimilarity similarity = new GenesetSimilarity(geneset1Name, geneset2Name, coeffecient, edgeType, intersection, dataset);
		return similarity;
	}
	
}
