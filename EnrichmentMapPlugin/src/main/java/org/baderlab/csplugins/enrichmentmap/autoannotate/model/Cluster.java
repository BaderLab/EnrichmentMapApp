package org.baderlab.csplugins.enrichmentmap.autoannotate.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationManager;
import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationUtils;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.group.CyGroup;
import org.cytoscape.session.CySession;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.annotations.Annotation;
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
	
	private int clusterNumber;
	private String cloudName;
	private ArrayList<CyNode> nodes;
	private ArrayList<double[]> coordinates;
	private int size;
	private String label;
	private TextAnnotation textAnnotation;
	private ShapeAnnotation ellipse;
	private AnnotationSet parent;
	private boolean selected;
	private ArrayList<WordInfo> wordInfos;
	
	// Used when initializing from a session file
	public Cluster() {
		this.nodes = new ArrayList<CyNode>();
		this.coordinates = new ArrayList<double[]>();
		this.setWordInfos(new ArrayList<WordInfo>());
	}
	
	public Cluster(int clusterNumber, AnnotationSet parent) {
		this.clusterNumber = clusterNumber;
		this.parent = parent;
		this.cloudName = parent.getCloudNamePrefix() + " Cloud " + clusterNumber;
		this.nodes = new ArrayList<CyNode>();
		this.coordinates = new ArrayList<double[]>();
		this.setWordInfos(new ArrayList<WordInfo>());
		size = 0;
		selected = false;
	}
	
	public int getClusterNumber() {
		return this.clusterNumber;
	}
	
	public AnnotationSet getParent() {
		return parent;
	}
	
	public void setParent(AnnotationSet annotationSet) {
		parent = annotationSet;
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
		size++;
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

	public int getSize() {
		return size;
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
	
	public String getCloudName() {
		return cloudName;
	}

	public void erase() {
		textAnnotation.removeAnnotation();
		ellipse.removeAnnotation();
	}
	
	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	public ArrayList<WordInfo> getWordInfos() {
		return wordInfos;
	}

	public void setWordInfos(ArrayList<WordInfo> wordInfos) {
		this.wordInfos = wordInfos;
	}

	@Override
	public int compareTo(Cluster cluster2) {
		return label.compareTo(cluster2.getLabel());
	}
	
	public String toSessionString() {
		/* Each cluster is stored in the format:
	    	 *  		1 - Cluster number
	    	 *  		2 - Cluster label
	    	 *  		3 - Selected (0/1)
	    	 *  		4 - labelManuallyUpdated
	    	 *  		5... - NodeSUID x y
	    	 *  		-1 - End of cluster
	    	 */

		String sessionString = "";
		// Write parameters of the cluster
		sessionString += clusterNumber + "\n";
		sessionString += label + "\n";
		sessionString += selected + "\n";
		// Write each node
		for (int nodeIndex=0 ; nodeIndex < size ; nodeIndex++) {
			double nodeX = coordinates.get(nodeIndex)[0];
			double nodeY = coordinates.get(nodeIndex)[1];
			long nodeID = nodes.get(nodeIndex).getSUID();
			sessionString += nodeX + "\t" + nodeY + "\t" + nodeID + "\n";
		}
		sessionString += "End of cluster\n";
		return sessionString;
	}
	
	public void load(ArrayList<String> text, CySession session) {
		clusterNumber = Integer.valueOf(text.get(0));
		cloudName = parent.getCloudNamePrefix() + " Cloud " + clusterNumber;
		label = text.get(1);
		selected = Boolean.valueOf(text.get(2));
		int lineNumber = 3;
		while (lineNumber < text.size()) {
			String line = text.get(lineNumber);
			String[] splitLine = line.split("\t");
			double[] nodeCoordinates = {Double.valueOf(splitLine[0]), Double.valueOf(splitLine[1])}; 
			addCoordinates(nodeCoordinates);
			addNode(session.getObject(Long.valueOf(splitLine[2]), CyNode.class));
			lineNumber++;
		}
		AutoAnnotationManager autoAnnotationManager = AutoAnnotationManager.getInstance();
		AnnotationManager annotationManager = autoAnnotationManager.getAnnotationManager();
		AnnotationFactory<ShapeAnnotation> shapeFactory = autoAnnotationManager.getShapeFactory();
		AnnotationFactory<TextAnnotation> textFactory = autoAnnotationManager.getTextFactory();
		for (Annotation annotation : annotationManager.getAnnotations(parent.getView())) {
			// Get rid of previously showing annotations (if any)
			annotation.removeAnnotation();
		}
		AutoAnnotationUtils.drawCluster(this, parent.getView(), shapeFactory, textFactory, annotationManager);
	}
	
	@Override
	public String toString() {
		return label;
	}
}