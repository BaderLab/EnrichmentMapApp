package org.baderlab.csplugins.enrichmentmap.autoannotate;

import java.util.TreeMap;

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
	
	public AnnotationSet() {
		this.clusterSet = new TreeMap<Integer, Cluster>();
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
	
	@Override
	public String toString() {
		return name;
	}
}
