package org.baderlab.csplugins.enrichmentmap.autoannotate.model;

import java.util.ArrayList;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;

/**
 * Created by:
 * @author arkadyark
 * <p>
 * Date   Jun 18, 2014<br>
 * Time   12:47 PM<br>
 * <p>
 * Class to store the relevant sets of data corresponding to each cluster
 */

public class Cluster implements Comparable<Cluster> {
	
	private int clusterNumber;
	private String cloudName;
	private ArrayList<CyNode> nodes;
	private ArrayList<double[]> coordinates;
	private String label;
	private TextAnnotation textAnnotation;
	private ShapeAnnotation ellipse;
	private int[] boundsX;
	private int[] boundsY;
	private boolean labelManuallyUpdated;
	private boolean selected;
	
	public Cluster(int clusterNumber, AnnotationSet parent) {
		this.clusterNumber = clusterNumber;
		this.cloudName = parent.getCloudNamePrefix() + " Cloud " + clusterNumber;
		this.nodes = new ArrayList<CyNode>();
		this.coordinates = new ArrayList<double[]>();
		selected = false;
		boundsX = new int[2];
		boundsY = new int[2];
		labelManuallyUpdated = false;
	}
	
	public int getClusterNumber() {
		return this.clusterNumber;
	}
	
	public ArrayList<double[]> getCoordinates() {
		return this.coordinates;
	}
	
	public void setCoordinates(ArrayList<double[]> coordinates) {
		this.coordinates = coordinates;
	}
	
	public ArrayList<CyNode> getNodes() {
		return this.nodes;
	}
	
	public void addNode(CyNode node) {
		this.nodes.add(node);
	}
	
	public void addCoordinates(double[] coordinates) {
		this.coordinates.add(coordinates);
	}
	
	public void setLabel(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
	
		public void setLabelManuallyUpdated(boolean b) {
		labelManuallyUpdated = b;
	}
	
	public boolean getLabelManuallyUpdated() {
		return labelManuallyUpdated;
	}
	
	public ShapeAnnotation getEllipse() {
		return ellipse;
	}
	
	public void setEllipse(ShapeAnnotation ellipse) {
		this.ellipse = ellipse;
	}
	
	public TextAnnotation getTextAnnotation() {
		return textAnnotation;
	}
	
	public void setTextAnnotation(TextAnnotation textAnnotation) { 
		this.textAnnotation = textAnnotation;
	}
	
	public int[] getBoundsX() {
		return boundsX;
	}
	
	public int[] getBoundsY() {
		return boundsY;
	}
	
	public String getCloudName() {
		return cloudName;
	}

	public void erase() {
		textAnnotation.removeAnnotation();
		ellipse.removeAnnotation();
	}
	
	@Override
	public int compareTo(Cluster cluster2) {
		return this.getClusterNumber() - cluster2.getClusterNumber();
	}
	
	@Override
	public String toString() {
		return label;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
}