package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.baderlab.csplugins.enrichmentmap.CytoscapeServiceModule.Dialog;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.task.cluster.HierarchicalClusterTask;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParams.Distance;
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
	private Distance distance;

	public interface Factory {
		ClusterRankingOption create(EnrichmentMap map, Distance distance);
	}
	
	@Inject
	public ClusterRankingOption(@Assisted EnrichmentMap map, @Assisted Distance distance, @Dialog TaskManager<?,?> taskManager) {
		this.map = map;
		this.taskManager = taskManager;
		this.distance = distance;
	}

	@Override
	public String toString() {
		return "Hierarchical Cluster";
	}

	@Override
	public String getTableHeaderText() {
		return "<html>Hierarchical<br>Cluster</html>";
	}
	
	@Override
	public String getPdfHeaderText() {
		return "Hierarchical\nCluster";
	}
	
	public void setDistance(Distance distance) {
		this.distance = distance;
	}
	
	public Distance getDistance() {
		return distance;
	}
	

	@Override
	public CompletableFuture<Optional<RankingResult>> computeRanking(Collection<Integer> genes) {
		if(genes.size() < 2) {
			// The HierarchicalClusterTask requires at least 2 genes
			return CompletableFuture.completedFuture(Optional.of(RankingResult.empty()));
		}
		
		HierarchicalClusterTask task = new HierarchicalClusterTask(map, genes, distance.getMetric());
		
		CompletableFuture<Optional<RankingResult>> future = new CompletableFuture<>();
		
		taskManager.execute(new TaskIterator(task), new TaskObserver() {
			@Override
			public void taskFinished(ObservableTask task) {
				if(task instanceof HierarchicalClusterTask) {
					HierarchicalClusterTask clusterTask = (HierarchicalClusterTask) task;
					Optional<RankingResult> ranking = clusterTask.getActualResults();
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
