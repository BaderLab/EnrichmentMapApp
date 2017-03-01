package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.baderlab.csplugins.brainlib.DistanceMetric;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.task.cluster.HierarchicalClusterTask;
import org.baderlab.csplugins.enrichmentmap.task.cluster.PearsonCorrelation;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.RankValue;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class HierarchicalClusterRankingOption implements RankingOption {

	@Inject private DialogTaskManager taskManager;
	
	private final EnrichmentMap map;
	private final Collection<String> genes;
	private final DistanceMetric distanceMetric = new PearsonCorrelation(); // MKTODO don't hardcode this
	

	public interface Factory {
		HierarchicalClusterRankingOption create(EnrichmentMap map, Collection<String> genes);
	}
	
	@Inject
	public HierarchicalClusterRankingOption(@Assisted EnrichmentMap map, @Assisted Collection<String> genes) {
		this.map = map;
		this.genes = genes;
	}

	@Override
	public String toString() {
		return "Hierarchical Cluster";
	}

	@Override
	public CompletableFuture<Map<Integer,RankValue>> computeRanking() {
		HierarchicalClusterTask task = new HierarchicalClusterTask(map, genes, distanceMetric);
		
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
				// Don't see why this would ever happens
				if(!future.isDone()) {
					future.completeExceptionally(new RuntimeException("Failed"));
				}
			}
		});
		
		return future;
	}

}
