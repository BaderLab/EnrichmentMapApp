package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.baderlab.csplugins.enrichmentmap.CytoscapeServiceModule.Dialog;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.task.cluster.HierarchicalClusterTask;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParams.Distance;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.RankValue;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskObserver;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class ClusterRankingOption implements RankingOption {

	private final TaskManager<?,?> taskManager;
	
	private final EnrichmentMap map;
	private Distance distance = Distance.PEARSON;

	public interface Factory {
		ClusterRankingOption create(EnrichmentMap map);
	}
	
	@Inject
	public ClusterRankingOption(@Assisted EnrichmentMap map, @Dialog TaskManager<?,?> taskManager) {
		this.map = map;
		this.taskManager = taskManager;
	}

	@Override
	public String toString() {
		return "Hierarchical Cluster";
	}

	public void setDistance(Distance distance) {
		this.distance = distance;
	}
	
	public Distance getDistance() {
		return distance;
	}
	

	@Override
	public CompletableFuture<Map<Integer,RankValue>> computeRanking(Collection<Integer> genes) {
		if(genes.size() < 2) {
			// The HierarchicalClusterTask requires at least 2 genes
			return CompletableFuture.completedFuture(Collections.emptyMap());
		}
		
		HierarchicalClusterTask task = new HierarchicalClusterTask(map, genes, distance.getMetric());
		
		CompletableFuture<Map<Integer,RankValue>> future = new CompletableFuture<>();
		
		taskManager.execute(new TaskIterator(task), new TaskObserver() {
			@Override
			public void taskFinished(ObservableTask task) {
				if(task instanceof HierarchicalClusterTask) {
					HierarchicalClusterTask clusterTask = (HierarchicalClusterTask) task;
					@SuppressWarnings("unchecked")
					Map<Integer,RankValue> ranking = clusterTask.getResults(Map.class);
					future.complete(ranking);
				}
			}
			@Override
			public void allFinished(FinishStatus finishStatus) {
				// Don't see why this would ever happen
				if(!future.isDone()) {
					future.completeExceptionally(new RuntimeException("Failed"));
				}
			}
		});
		
		return future;
	}

}
