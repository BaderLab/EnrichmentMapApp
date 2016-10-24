package org.baderlab.csplugins.enrichmentmap.task;

import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.SimilarityMetric;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.baderlab.csplugins.enrichmentmap.model.GenesetSimilarity;
import org.baderlab.csplugins.enrichmentmap.model.SimilarityKey;
import org.baderlab.csplugins.enrichmentmap.util.DiscreteTaskMonitor;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.common.collect.Sets;

public class ComputeSimilarityTaskParallel extends AbstractTask {

	private final EnrichmentMap map;
	
	public ComputeSimilarityTaskParallel(EnrichmentMap map) {
		this.map = map;
	}

	@Override
	public void run(TaskMonitor tm) throws InterruptedException {
		// If the expression sets are distinct the do we need to generate separate edges for each dataset as well as edges between each data set?
		// How to support post-analysis on a master map?
		
		if(map.getParams().isDistinctExpressionSets())
			throw new IllegalArgumentException("Distinct expression sets are not supported yet");
		
		int cpus = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(cpus);
        
        Map<String,GeneSet> genesetsOfInterest = map.getAllGenesetsOfInterest();
        final String edgeType = map.getParams().getEnrichmentEdgeType();
        
        System.out.println("ComputeSimilarityTaskParallel.run() " + genesetsOfInterest.size());
        
        DiscreteTaskMonitor taskMonitor = new DiscreteTaskMonitor(tm, genesetsOfInterest.size());
        taskMonitor.setTitle("Computing Geneset Similarities...");
        taskMonitor.setStatusMessageTemplate("Computing Geneset similarity: {0} of {1} similarities");
		
		Set<SimilarityKey> similaritiesVisited = ConcurrentHashMap.newKeySet();
		Map<String,GenesetSimilarity> similarities = new ConcurrentHashMap<>();
		
		for(final String geneset1Name : genesetsOfInterest.keySet()) {
			
			// Compute similarities in batches, creating a Runnable for every similarity pair would create to many objects
			executor.execute(() -> {
				
				for(final String geneset2Name : genesetsOfInterest.keySet()) {
					if (geneset1Name.equalsIgnoreCase(geneset2Name))
						continue; //don't compare two identical gene sets
					
					SimilarityKey key = new SimilarityKey(geneset1Name, edgeType, geneset2Name);
					
					if(similaritiesVisited.add(key)) {
						GeneSet geneset1 = genesetsOfInterest.get(geneset1Name);
						GeneSet geneset2 = genesetsOfInterest.get(geneset2Name);
						GenesetSimilarity similarity = computeGenesetSimilarity(map.getParams(), geneset1Name, geneset2Name, geneset1, geneset2, 0);
						similarities.put(key.toString(), similarity);
					}
				}
				
				taskMonitor.inc();
			});
		}
		
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

		System.out.println("DONE WITH SIMILARITIES");
		
		if(!cancelled)
			map.getGenesetSimilarity().putAll(similarities);
	}

	
	private static GenesetSimilarity computeGenesetSimilarity(EMCreationParameters params, String geneset1Name, String geneset2Name, GeneSet geneset1, GeneSet geneset2, int enrichment_set) {
		// MKTODO: Should not need to pass in the geneset names, should just use geneset.getName(), but I'm nervous I might break something.
		
		Set<Integer> genes1 = geneset1.getGenes();
		Set<Integer> genes2 = geneset2.getGenes();

		Set<Integer> intersection = Sets.intersection(genes1, genes2);
		Set<Integer> union = Sets.union(genes1, genes2);

		double coeffecient = computeSimilarityCoeffecient(params, intersection, union, genes1, genes2);
		
		String edgeType = params.getEnrichmentEdgeType();
		GenesetSimilarity similarity = new GenesetSimilarity(geneset1Name, geneset2Name, coeffecient, edgeType, intersection, enrichment_set);
		return similarity;
	}
	
	
	public static double computeSimilarityCoeffecient(EMCreationParameters params, Set<?> intersection, Set<?> union, Set<?> genes1, Set<?> genes2) {
		// Note: Do not call intersection.size() or union.size() more than once on a Guava SetView! 
		// It is a potentially slow operation that needs to be recalculated each time it is called.
		
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
	
}
