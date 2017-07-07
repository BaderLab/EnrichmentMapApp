package org.baderlab.csplugins.enrichmentmap.task.cluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.baderlab.csplugins.brainlib.AvgLinkHierarchicalClustering;
import org.baderlab.csplugins.brainlib.DistanceMatrix;
import org.baderlab.csplugins.brainlib.DistanceMetric;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpression;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpressionMatrix;
import org.baderlab.csplugins.enrichmentmap.util.NullTaskMonitor;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.RankValue;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;

import com.google.common.collect.ImmutableList;

public class HierarchicalClusterTask extends AbstractTask implements ObservableTask {

	private final Collection<Integer> genes;
	private final EnrichmentMap map;
	private final DistanceMetric distanceMetric;
	
	private Optional<Map<Integer,RankValue>> results;
	
	
	public HierarchicalClusterTask(EnrichmentMap map, Collection<Integer> genes, DistanceMetric distanceMetric) {
		this.map = map;
		this.genes = ImmutableList.copyOf(genes);
		this.distanceMetric = distanceMetric;
	}
	
	
	public Map<Integer,RankValue> cluster(TaskMonitor tm) {
		if(tm == null)	
			tm = new NullTaskMonitor();
		tm.setTitle("Hierarchical Cluster");
		
		tm.setStatusMessage("Loading expression data");
		
		List<double[]> clusteringExpressionSet = new ArrayList<>(genes.size()) ;
        ArrayList<Integer> labels = new ArrayList<>(genes.size());
        List<String> names = new ArrayList<>(genes.size());
        
        List<EMDataSet> dataSets = map.getDataSetList();
        final int expressionCount = getTotalExpressionCount(dataSets);
        
        for(int geneId : genes) {
        	double[] vals = new double[expressionCount]; // values all default to 0.0
        	int valsIndex = 0;
        	
        	boolean found = false;
        	String name = null;
        	
        	for(EMDataSet dataSet : dataSets) {
        		GeneExpressionMatrix expressionSets = dataSet.getExpressionSets();
        		int numConditions = expressionSets.getNumConditions() - 2;
				GeneExpression geneExpression = expressionSets.getExpressionMatrix().get(geneId);
        		if(geneExpression != null) {
        			found = true;
        			name = geneExpression.getName();
        			float[] expression = geneExpression.getExpression();
        			System.arraycopy(expression, 0, vals, valsIndex, expression.length);
        		}
        		valsIndex += numConditions;
        	}
        	
        	if(found) {
        		clusteringExpressionSet.add(vals);
        		labels.add(geneId);
        		names.add(name);
        	}
        }
        
        tm.setStatusMessage("Calculating Distance");
        
        DistanceMatrix distanceMatrix = new DistanceMatrix(genes.size());
        distanceMatrix.calcDistances(clusteringExpressionSet, distanceMetric);
        distanceMatrix.setLabels(labels);
        
        tm.setStatusMessage("Clustering");
        
        AvgLinkHierarchicalClustering clusterResult = new AvgLinkHierarchicalClustering(distanceMatrix);
        //check to see if there more than 1000 genes, if there are use eisen ordering otherwise use bar-joseph
        clusterResult.setOptimalLeafOrdering(genes.size() <= 1000);
        clusterResult.run();
        
        tm.setStatusMessage("Ranking");
        
        Map<Integer,RankValue> ranks = new HashMap<>();
        
        int[] order = clusterResult.getLeafOrder();
        for(int i = 0; i < order.length; i++) {
            Integer geneId = labels.get(order[i]);
            ranks.put(geneId, new RankValue(i+1, null, false));
        }
        
        tm.setStatusMessage("");
        return ranks;
	}
	
	
	private static int getTotalExpressionCount(List<EMDataSet> dataSetList) {
		return dataSetList.stream()
			.map(EMDataSet::getExpressionSets)
			.mapToInt(GeneExpressionMatrix::getNumConditions)
			.map(x -> x - 2)
			.sum();
	}
	
	@Override
	public void run(TaskMonitor tm) {
		try {
			results = Optional.of(cluster(tm));
		} catch(Exception e) {
			results = Optional.empty();
		}
	}

	public Optional<Map<Integer,RankValue>> getActualResults() {
		return results;
	}
	
	/** Use getActualResults() instead */
	@Override
	public <R> R getResults(Class<? extends R> type) {
		return null;
	}

}
