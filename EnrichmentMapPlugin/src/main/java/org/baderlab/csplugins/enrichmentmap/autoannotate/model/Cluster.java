package org.baderlab.csplugins.enrichmentmap.autoannotate.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationManager;
import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationUtils;
import org.cytoscape.model.CyNode;
import org.cytoscape.group.CyGroup;
import org.cytoscape.session.CySession;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
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
	private CyGroup group;
	private ArrayList<CyNode> nodeList;
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
		this.coordinates = new ArrayList<double[]>();
		this.wordInfos = new ArrayList<WordInfo>();
		this.nodeList = new ArrayList<CyNode>();
		size = 1;
	}
	
	// Used when creating clusters in the task
	public Cluster(int clusterNumber, AnnotationSet parent, CyGroup group) {
		this(clusterNumber, parent);
		this.group = group;
		// Group node doesn't get added
		size++;
	}
	
	public Cluster(int clusterNumber, AnnotationSet parent) {
		this.clusterNumber = clusterNumber;
		this.parent = parent;
		this.cloudName = parent.getCloudNamePrefix() + " Cloud " + clusterNumber;
		this.coordinates = new ArrayList<double[]>();
		this.wordInfos = new ArrayList<WordInfo>();
		this.nodeList = new ArrayList<CyNode>();
		size = 0; // Starts at one because it is created with the group node, which doesn't get added to the group
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
	
	public boolean isCollapsed() {
		if (group != null) {
			return group.isCollapsed(parent.getView().getModel());
		} return false;
	}
	
	public CyNode getGroupNode() {
		if (group != null) {
			return group.getGroupNode();
		}
		return null;
	}
	
	public CyGroup getGroup()  {
		return group;
	}
	
	public void destroyGroup() {
		if (group != null) { // user could have destroyed the group themselves
			AutoAnnotationManager.getInstance().getGroupManager().destroyGroup(group);
		}
	}
	
	public ArrayList<double[]> getCoordinates() {
		return this.coordinates;
	}
	
	public void setCoordinates(ArrayList<double[]> coordinates) {
		this.coordinates = coordinates;
	}
	
	public List<CyNode> getNodes() {
		if (group != null) {
			// Have to also include the group's nodeList
			List<CyNode> nodeListWithGroupNode = (List<CyNode>) nodeList.clone();
			nodeListWithGroupNode.add(getGroupNode());
			return nodeListWithGroupNode;
		}
		return nodeList;
	}
	
	public void addNode(CyNode node) {
		if (group != null) {
			ArrayList<CyNode> nodeToAdd = new ArrayList<CyNode>();
			nodeToAdd.add(node);
			group.addNodes(nodeToAdd);
		}
		nodeList.add(node);
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
		// Write parameters of the annotations to find them after
			// Ellipse
		sessionString += ellipse.getArgMap().get("x") + "\t" 
					  + ellipse.getArgMap().get("y") + "\n";
			// Text
		sessionString += textAnnotation.getArgMap().get("x") + "\t"
					  + textAnnotation.getArgMap().get("y") + "\n";
		
		// Write each node
		for (int nodeIndex=0 ; nodeIndex < size ; nodeIndex++) {
			long nodeID = getNodes().get(nodeIndex).getSUID();
			sessionString += nodeID + "\n";
		}
		sessionString += "End of nodes\n";
		// Write coordinates (separately, in case a node has been collapsed)
		for (int coordinateIndex=0 ; coordinateIndex < getCoordinates().size() ; coordinateIndex++) {
			double nodeX = coordinates.get(coordinateIndex)[0];
			double nodeY = coordinates.get(coordinateIndex)[1];
			sessionString += nodeX + "\t" + nodeY + "\n";
		}
		sessionString += "End of coordinates\n";
		sessionString += "End of cluster\n";
		return sessionString;
	}
	
	public void load(ArrayList<String> text, CySession session) {
		clusterNumber = Integer.valueOf(text.get(0));
		cloudName = parent.getCloudNamePrefix() + " Cloud " + clusterNumber;
		label = text.get(1);
		selected = Boolean.valueOf(text.get(2));
		
		// Load the parameters of the annotations
		String[] ellipseParameters = text.get(3).split("\t");
		String ellipseX = ellipseParameters[0];
		String ellipseY = ellipseParameters[1];
		String[] textParameters = text.get(4).split("\t");
		String textX = textParameters[0];
		String textY = textParameters[1];
		
		// Create the group for the first node
		String groupNodeLine = text.get(5);
		CyNode groupNode = session.getObject(Long.valueOf(groupNodeLine), CyNode.class);
		group = AutoAnnotationManager.getInstance().getGroupFactory().createGroup(parent.getView().getModel(), groupNode, false);
		
		int lineNumber = 6;
		// Reload nodes
		String line = text.get(lineNumber);
		while (!line.equals("End of nodes")) {
			addNode(session.getObject(Long.valueOf(line), CyNode.class));
			lineNumber++;
			line = text.get(lineNumber);
		}
		lineNumber++;
		line = text.get(lineNumber);
		// Reload coordinates
		while (!line.equals("End of coordinates")) {
			String[] splitLine = line.split("\t");
			double[] nodeCoordinates = {Double.valueOf(splitLine[0]), Double.valueOf(splitLine[1])}; 
			addCoordinates(nodeCoordinates);
			lineNumber++;
			line = text.get(lineNumber);
		}
		// Try to find annotation by coordinates
		// TODO - if annotations have been moved then it redraws all of them
		AutoAnnotationManager autoAnnotationManager = AutoAnnotationManager.getInstance();
		AnnotationManager annotationManager = autoAnnotationManager.getAnnotationManager();
		for (Annotation annotation : annotationManager.getAnnotations(parent.getView())) {
			Map<String, String> annotationParameters = annotation.getArgMap();
			String annotationX = annotationParameters.get("x");
			String annotationY = annotationParameters.get("y");
			if (annotationX.equals(ellipseX) &&
				annotationY.equals(ellipseY)) {
				ellipse = (ShapeAnnotation) annotation;
				if (textAnnotation != null) break;
			} else if (annotationX.equals(textX) &&
					annotationY.equals(textY)) {
				textAnnotation = (TextAnnotation) annotation;
				if (ellipse != null) break;
			}
		}
		if (ellipse == null && textAnnotation == null) {
			// Annotation was moved after save, cluster needs to be redrawn
			AnnotationFactory<ShapeAnnotation> shapeFactory = autoAnnotationManager.getShapeFactory();
			AnnotationFactory<TextAnnotation> textFactory = autoAnnotationManager.getTextFactory();
			AutoAnnotationUtils.drawCluster(this, parent.getView(), shapeFactory, textFactory, annotationManager);
		} else if (ellipse != null && textAnnotation != null) {
			erase();
		}
	}
	
	@Override
	public String toString() {
		return label;
	}
}