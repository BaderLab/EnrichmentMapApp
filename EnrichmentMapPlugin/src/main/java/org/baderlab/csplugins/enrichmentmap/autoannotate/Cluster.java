package org.baderlab.csplugins.enrichmentmap.autoannotate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

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
	
	int clusterNumber;
	ArrayList<CyNode> nodes;
	ArrayList<double[]> coordinates;
	ArrayList<NodeText> nodeTexts;
	private CyNetworkView view;
	private String label;
	private AnnotationManager annotationManager;
	private TextAnnotation textAnnotation;
	private ShapeAnnotation ellipse;
	
	public Cluster(int clusterNumber, CyNetworkView view, AnnotationManager annotationManager) {
		this.clusterNumber = clusterNumber;
		this.nodes = new ArrayList<CyNode>();
		this.coordinates = new ArrayList<double[]>();
		this.nodeTexts = new ArrayList<NodeText>();
		this.view = view;
		this.annotationManager = annotationManager;
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

	public void setLabel(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
	
	public ShapeAnnotation getEllipse() {
		return ellipse;
	}
	
	public TextAnnotation getTextAnnotation() {
		return textAnnotation;
	}
	
	public AnnotationManager getAnnotationManager() {
		return annotationManager;
	}
	
	public CyNetworkView getNetworkView() {
		return view;
	}
	
	public void drawAnnotations(AnnotationFactory<ShapeAnnotation> shapeFactory, AnnotationFactory<TextAnnotation> textFactory) {
		// Factories to create the annotations
		// Constants used in making the appearance prettier
    	double min_size = 10.0;
    	double padding = 1.7;
		double zoom = view.getVisualProperty(BasicVisualLexicon.NETWORK_SCALE_FACTOR);

		double xmin = 100000000;
		double ymin = 100000000;
		double xmax = -100000000;
		double ymax = -100000000;
		
		for (double[] coordinates : this.getCoordinates()) {
			xmin = coordinates[0] < xmin ? coordinates[0] : xmin;
			xmax = coordinates[0] > xmax ? coordinates[0] : xmax;
			ymin = coordinates[1] < ymin ? coordinates[1] : ymin;
			ymax = coordinates[1] > ymax ? coordinates[1] : ymax;
		}
		
		double width = (xmax - xmin)*zoom;
		width = width > min_size ? width : min_size;
		double height = (ymax - ymin)*zoom;
		height = height > min_size ? height : min_size;

		// Parameters of the ellipse
		Integer xPos = (int) Math.round(xmin - width/zoom*padding/5.0);
		Integer yPos = (int) Math.round(ymin - height/zoom*padding/5.0);
		
		// Create and draw the ellipse
		HashMap<String, String> arguments = new HashMap<String,String>();
		arguments.put("x", String.valueOf(xPos));
		arguments.put("y", String.valueOf(yPos));
		arguments.put("zoom", String.valueOf(zoom));
		arguments.put("canvas", "foreground");
		ellipse = shapeFactory.createAnnotation(ShapeAnnotation.class, view, arguments);
		ellipse.setShapeType("Ellipse");
		ellipse.setSize(width*padding, height*padding);
		this.annotationManager.addAnnotation(ellipse);
		
		// Parameters of the label
		Integer fontSize = (int) Math.round(0.2*Math.sqrt(Math.pow(width, 2)+ Math.pow(height, 2)));
		// To centre the annotation at the middle of the annotation
		xPos = (int) Math.round((xmin + xmax)/2 - 0.15*width*padding - 0.8*fontSize*label.length());
		yPos = (int) Math.round(ymin - height*padding - 4.5*fontSize);
		
		// Create and draw the label
		arguments = new HashMap<String,String>();
		arguments.put("x", String.valueOf(xPos));
		arguments.put("y", String.valueOf(yPos));
		arguments.put("zoom", String.valueOf(zoom));
		arguments.put("canvas", "foreground");
		arguments.put("fontSize", String.valueOf(fontSize));
		textAnnotation = textFactory.createAnnotation(TextAnnotation.class, view, arguments);
		textAnnotation.setText(label);
		this.annotationManager.addAnnotation(textAnnotation);
	}

	@Override
	public int compareTo(Cluster cluster2) {
		// TODO Auto-generated method stub
		return this.getClusterNumber() - cluster2.getClusterNumber();
	}
}