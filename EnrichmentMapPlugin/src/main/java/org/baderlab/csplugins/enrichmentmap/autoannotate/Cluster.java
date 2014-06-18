package org.baderlab.csplugins.enrichmentmap.autoannotate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.cytoscape.model.CyNode;

/**
 * Created by:
 * @author arkadyark
 * <p>
 * Date   Jun 18, 2014<br>
 * Time   12:47 PM<br>
 * <p>
 * Class to store the relevant sets of data corresponding to each cluster
 */

public final class Cluster {
	
	int clusterNumber;
	ArrayList<CyNode> nodes;
	ArrayList<double[]> coordinates;
	ArrayList<NodeText> nodeTexts;
	
	public Cluster(int clusterNumber) {
		this.clusterNumber = clusterNumber;
		this.nodes = new ArrayList<CyNode>();
		this.coordinates = new ArrayList<double[]>();
		this.nodeTexts = new ArrayList<NodeText>();
	}
	
	public int getClusterNumber() {
		return this.clusterNumber;
	}
	
	public ArrayList<double[]> getCoordinates() {
		return this.coordinates;
	}
	
	public ArrayList<NodeText> getNodeTexts() {
		return this.nodeTexts;
	}
	
	public void addNode(CyNode node) {
		this.nodes.add(node);
	}
	
	public void addCoordinates(double[] coordinates) {
		this.coordinates.add(coordinates);
	}
	
	public void addNodeText(NodeText nodeText) {
		this.nodeTexts.add(nodeText);
	}
}