package org.baderlab.csplugins.enrichmentmap.task;

import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.baderlab.csplugins.enrichmentmap.model.GenesetSimilarity;
import org.baderlab.csplugins.enrichmentmap.model.SimilarityKey;
import org.baderlab.csplugins.enrichmentmap.util.DiscreteTaskMonitor;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

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
						
						// returns null if the similarity coefficient doesn't pass the cutoff
						GenesetSimilarity similarity = ComputeSimilarityTask.computeGenesetSimilarity(map.getParams(), geneset1Name, geneset2Name, geneset1, geneset2, 0, true);
						if(similarity != null) {
							similarities.put(key.toString(), similarity);
						}
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
		
		if(!cancelled)
			map.getGenesetSimilarity().putAll(similarities);
	}

	
}
