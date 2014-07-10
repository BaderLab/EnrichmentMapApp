package org.baderlab.csplugins.enrichmentmap.autoannotate.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

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
	public String name;
	ArrayList<CyNode> nodes;
	public ArrayList<double[]> coordinates;
	ArrayList<NodeText> nodeTexts;
	private CyNetwork network;
	private CyNetworkView view;
	private String label;
	private AnnotationManager annotationManager;
	private TextAnnotation textAnnotation;
	private ShapeAnnotation ellipse;
	private String clusterColumnName;
	private int[] boundsX;
	private int[] boundsY;
	private AnnotationFactory<ShapeAnnotation> shapeFactory;
	private AnnotationFactory<TextAnnotation> textFactory;
	private String cloudName;
	private CyServiceRegistrar registrar;
	
	public Cluster(int clusterNumber, CyNetwork network, CyNetworkView view, AnnotationManager annotationManager, String clusterColumnName,
			AnnotationFactory<ShapeAnnotation> shapeFactory, AnnotationSet parent, AnnotationFactory<TextAnnotation> textFactory, CyServiceRegistrar registrar) {
		this.clusterNumber = clusterNumber;
		this.name = "Cluster " + clusterNumber;
		this.cloudName = parent.name + " Cloud " + clusterNumber;
		this.nodes = new ArrayList<CyNode>();
		this.coordinates = new ArrayList<double[]>();
		this.nodeTexts = new ArrayList<NodeText>();
		this.network = network;
		this.view = view;
		this.clusterColumnName = clusterColumnName;
		this.annotationManager = annotationManager;
		this.shapeFactory = shapeFactory;
		this.textFactory = textFactory;
		this.registrar = registrar;
		boundsX = new int[2];
		boundsY = new int[2];
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
	
	public int[] getBoundsX() {
		return boundsX;
	}
	
	public int[] getBoundsY() {
		return boundsY;
	}
	
	public void select() {
		// Select the corresponding WordCloud
		CommandExecutorTaskFactory executor = registrar.getService(CommandExecutorTaskFactory.class);
		ArrayList<String> commands = new ArrayList<String>();
		String command = "wordcloud delete cloudName=\"" + cloudName + "\"";
		commands.add(command);
		TaskIterator task = executor.createTaskIterator(commands, null);
		registrar.getService(DialogTaskManager.class).execute(task);
		
		for (CyRow row : network.getDefaultNodeTable().getAllRows()) {
			if (row.get(clusterColumnName, Integer.class) != null && row.get(clusterColumnName, Integer.class) == clusterNumber) {
				row.set(CyNetwork.SELECTED, true);
			} else {
				row.set(CyNetwork.SELECTED, false);
			}
			view.updateView();
		}
	}

	public void drawAnnotations() {

		
		// Constants used in making the appearance prettier
		double zoom = view.getVisualProperty(BasicVisualLexicon.NETWORK_SCALE_FACTOR);
		double min_size = 15;
		double padding = 1.9;

    	// Find the edges of the annotation
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
		
		double width = (xmax - xmin);
		width = width > min_size ? width : min_size;
		double height = (ymax - ymin);
		height = height > min_size ? height : min_size;
		
		// Parameters of the ellipse
		Integer xPos = (int) Math.round(xmin - width*padding/4);
		Integer yPos = (int) Math.round(ymin - height*padding/4);
		
		// Create and draw the ellipse
		HashMap<String, String> arguments = new HashMap<String,String>();
		arguments.put("x", String.valueOf(xPos));
		arguments.put("y", String.valueOf(yPos));
		arguments.put("zoom", String.valueOf(zoom));
		arguments.put("canvas", "foreground");
		ellipse = shapeFactory.createAnnotation(ShapeAnnotation.class, view, arguments);
		ellipse.setShapeType("Ellipse");
		ellipse.setSize(width*padding*zoom, height*padding*zoom);
		ellipse.setBorderWidth(5.0);
		annotationManager.addAnnotation(ellipse);

		// Parameters of the label
		Integer fontSize = (int) Math.round(0.35*Math.pow(Math.pow(width, 2)+ Math.pow(height, 2), 0.44)*zoom);
		// To centre the annotation at the middle of the annotation
		xPos = (int) Math.round(xPos + width*padding/2 - 1.1*fontSize*label.length());
		yPos = (int) Math.round(yPos - 5.3*fontSize);
		
		// Create and draw the label
		arguments = new HashMap<String,String>();
		arguments.put("x", String.valueOf(xPos));
		arguments.put("y", String.valueOf(yPos));
		arguments.put("zoom", String.valueOf(zoom));
		arguments.put("canvas", "foreground");
		arguments.put("fontSize", String.valueOf(fontSize));
		
		textAnnotation = textFactory.createAnnotation(TextAnnotation.class, view, arguments);
		textAnnotation.setText(label);
		annotationManager.addAnnotation(textAnnotation);
	}

	public void erase() {
		textAnnotation.removeAnnotation();
		ellipse.removeAnnotation();
	}
	
	public void destroy() {
		// Get rid of the WordCloud
		CommandExecutorTaskFactory executor = registrar.getService(CommandExecutorTaskFactory.class);
		ArrayList<String> commands = new ArrayList<String>();
		String command = "wordcloud delete cloudName=\"" + cloudName + "\"";
		commands.add(command);

		TaskIterator task = executor.createTaskIterator(commands, null);
		registrar.getService(DialogTaskManager.class).execute(task);
		// Erase the cluster
		erase();
	}

	@Override
	public int compareTo(Cluster cluster2) {
		// TODO Auto-generated method stub
		return this.getClusterNumber() - cluster2.getClusterNumber();
	}
	
	@Override
	public String toString() {
		return "Cluster " + getClusterNumber();
	}
}