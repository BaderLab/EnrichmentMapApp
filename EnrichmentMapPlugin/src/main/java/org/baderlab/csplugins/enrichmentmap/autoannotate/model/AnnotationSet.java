package org.baderlab.csplugins.enrichmentmap.autoannotate.model;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTableManager;
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
	private CyTableManager tableManager;
	
	public AnnotationSet(String name, CyNetwork network, CyNetworkView view, String clusterColumnName, CyTableManager tableManager) {
		this.name = name;
		this.clusterSet = new TreeMap<Integer, Cluster>();
		this.network = network;
		this.view = view;
		this.tableManager = tableManager;
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
	
	public void destroyAnnotations() {
		for (Cluster cluster : clusterSet.values()) {
			cluster.destroy();
		}
	}
	
	public void updateCoordinates() {
		for (Cluster cluster : clusterSet.values()) {
			cluster.coordinates = new ArrayList<double[]>();
			for (CyNode node : cluster.nodes) {
				View<CyNode> nodeView = view.getNodeView(node);
				double x = nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
				double y = nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
				double[] coordinates = {x, y};
				cluster.addCoordinates(coordinates);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void updateLabels() {
		for (Cluster cluster : this.clusterSet.values()) {
			cluster.setLabel("");
			int clusterNumber = cluster.getClusterNumber();
			Long clusterTableSUID = network.getDefaultNetworkTable().getRow(network.getSUID()).get(name + " ", Long.class);
			CyRow clusterRow = tableManager.getTable(clusterTableSUID).getRow(clusterNumber);
			List<String> wordList = clusterRow.get("WC_Word", List.class);
			List<String> sizeList = clusterRow.get("WC_FontSize", List.class);
			List<String> clusterList = clusterRow.get("WC_Cluster", List.class);
			List<String> numberList = clusterRow.get("WC_Number", List.class);
			String label = WordUtils.makeLabel(wordList, sizeList, clusterList, numberList);
			cluster.setLabel(label);
		}
	}
	
	@Override
	public String toString() {
		return name;
	}

}
