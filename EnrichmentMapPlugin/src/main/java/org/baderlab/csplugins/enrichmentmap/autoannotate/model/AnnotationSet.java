package org.baderlab.csplugins.enrichmentmap.autoannotate.model;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

/**
 * @author arkadyark
 * <p>
 * Date   July 7, 2014<br>
 * Time   09:23:28 PM<br>
 */

public class AnnotationSet {
	
	public String name;
	public TreeMap<Integer, Cluster> clusterSet; // Having it be sorted is useful for the displayPanel
	public boolean drawn;
	public CyNetwork network;
	public CyNetworkView view;
	private String clusterColumnName;
	
	public AnnotationSet(CyNetwork network, CyNetworkView view, String clusterColumnName) {
		this.clusterSet = new TreeMap<Integer, Cluster>();
		this.network = network;
		this.clusterColumnName = clusterColumnName;
		this.view = view;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void addCluster(Cluster cluster) {
		clusterSet.put(cluster.getClusterNumber(), cluster);
	}

	public void drawAnnotations() {
		for (Cluster cluster : clusterSet.values()) {
			cluster.drawAnnotations();
		}
	}
	
	public void eraseAnnotations() {
		for (Cluster cluster : clusterSet.values()) {
			cluster.erase();
		}
	}
	
	public void updateCoordinates() {
		
		for (Cluster cluster : clusterSet.values()) {
			cluster.coordinates = new ArrayList<double[]>();
		}
		
		List<CyNode> nodes = network.getNodeList();
		for (CyNode node : nodes) {
			Integer clusterNumber = network.getRow(node).get(this.clusterColumnName, Integer.class);
			if (clusterNumber != null) {
				View<CyNode> nodeView = view.getNodeView(node);
				double x = nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
				double y = nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
				double[] coordinates = {x, y};
				
				Cluster cluster = clusterSet.get(clusterNumber);
				cluster.addCoordinates(coordinates);
			}
		}
	}
	
	
	@Override
	public String toString() {
		return name;
	}
}
