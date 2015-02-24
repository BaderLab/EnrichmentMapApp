package org.baderlab.csplugins.enrichmentmap.autoannotate.task;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationParameters;
import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationUtils;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.AnnotationSet;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.Cluster;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class CreateClustersTask extends AbstractTask {
	
	private AnnotationSet annotationSet;
	private AutoAnnotationParameters params;

	private TaskMonitor taskMonitor;
	
	public CreateClustersTask(AnnotationSet annotationSet,
			AutoAnnotationParameters params) {
		super();
		this.annotationSet = annotationSet;
		this.params = params;
	}


	@SuppressWarnings("unchecked")
	private void makeClusters() {

		
		List<CyNode> nodes = params.getNetwork().getNodeList();
		Class<?> columnType = params.getNetwork().getDefaultNodeTable().getColumn(params.getClusterColumnName()).getType();
		for (CyNode node : nodes) {
			TreeMap<Integer, Cluster> clusterMap = annotationSet.getClusterMap();
			// Get coordinates from the nodeView
			View<CyNode> nodeView = params.getNetworkView().getNodeView(node);
			double x = nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
			double y = nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
			double[] coordinates = {x, y};
			double nodeRadius = nodeView.getVisualProperty(BasicVisualLexicon.NODE_WIDTH);

			if (columnType == Integer.class) { // Discrete clustering
				Integer clusterNumber;
				clusterNumber = params.getNetwork().getRow(node).get(params.getClusterColumnName(), Integer.class);
				if (clusterNumber != null) { // empty values (no cluster) are given null, ignore these
					addNodeToCluster(clusterNumber, node, coordinates, nodeRadius, clusterMap, annotationSet);
				}
			} else if (columnType == List.class) { // Fuzzy clustering
				List<Integer> clusterNumbers = new ArrayList<Integer>();
				clusterNumbers = params.getNetwork().getRow(node).get(params.getClusterColumnName(), List.class);
				// Iterate over each cluster for the node, and add the node to each cluster
				for (int i = 0; i < clusterNumbers.size(); i++) {
					int clusterNumber = clusterNumbers.get(i);
					addNodeToCluster(clusterNumber, node, coordinates, nodeRadius, clusterMap, annotationSet);
				}
			} // No other possible columnTypes (since the dropdown only contains these types
		}
		annotationSet.setUseGroups(params.isGroups());
		for (Cluster cluster : annotationSet.getClusterMap().values()) {
			AutoAnnotationUtils.updateNodeCentralities(cluster);
		}

	}
	
	
	// Adds the node and its coordinates to cluster number specified by clusterNumber
	private void addNodeToCluster(Integer clusterNumber, CyNode node, double[] coordinates,
			double nodeRadius, TreeMap<Integer, Cluster> clusterMap, AnnotationSet annotationSet) {
		Cluster cluster;
		if (!clusterMap.keySet().contains(clusterNumber)) {
			// Cluster doesn't exist, create it
			cluster = new Cluster(clusterNumber, annotationSet);
			annotationSet.addCluster(cluster);
		} else {
			// Cluster exists, look it up
			cluster = clusterMap.get(clusterNumber);
		}
		cluster.addNodeCoordinates(node, coordinates);
		cluster.addNodeRadius(node, nodeRadius);
	}


	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		this.taskMonitor = taskMonitor;
		this.taskMonitor.setStatusMessage("creating clusters");
		
		makeClusters();
		
	}
	
}
