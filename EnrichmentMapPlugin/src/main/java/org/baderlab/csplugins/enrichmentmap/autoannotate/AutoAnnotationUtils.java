/**
 * Created by
 * User: arkadyark
 * Date: Jul 24, 2014
 * Time: 12:50:09 PM
 */
package org.baderlab.csplugins.enrichmentmap.autoannotate;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;

import org.baderlab.csplugins.enrichmentmap.autoannotate.model.Cluster;
import org.baderlab.csplugins.enrichmentmap.autoannotate.task.ExecutorObserver;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

public class AutoAnnotationUtils {
	
	private static int min_size = 35; // Minimum size of the cluster
	private static double padding = 1.9; // Amount the ellipses are stretched by
	
	public static void selectCluster(Cluster selectedCluster, CyNetwork network, boolean showHeatmap,
									 CommandExecutorTaskFactory executor, DialogTaskManager dialogTaskManager) {
		// Select the corresponding WordCloud
		if (showHeatmap) {
			// Deselect all nodes currently selected
			for (CyNode node : CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, true)) {
				network.getRow(node).set(CyNetwork.SELECTED, false);
			}
			// Select nodes in this cluster (updates heatmap)
			for (CyNode node : selectedCluster.getNodes()) {
				network.getRow(node).set(CyNetwork.SELECTED, true);
			}
		} else {
			ArrayList<String> commands = new ArrayList<String>();
			String command = "wordcloud select cloudName=\"" + selectedCluster.getCloudName() + "\"";
			commands.add(command);
			TaskIterator task = executor.createTaskIterator(commands, null);
			dialogTaskManager.execute(task);
		}
		ShapeAnnotation ellipse = selectedCluster.getEllipse();
		ellipse.setBorderWidth(3*ellipse.getBorderWidth());
		ellipse.setBorderColor(Color.yellow);
		selectedCluster.getTextAnnotation().setTextColor(Color.yellow);
	}

	public static void deselectCluster(Cluster deselectedCluster, CyNetwork network) {
		// Deselect nodes in the cluster
		for (CyNode node : deselectedCluster.getNodes()) {
			network.getRow(node).set(CyNetwork.SELECTED, false);
		}
		// Reset the size/color of the annotations
		ShapeAnnotation ellipse = deselectedCluster.getEllipse();
		ellipse.setBorderWidth(ellipse.getBorderWidth()/3);
		ellipse.setBorderColor(Color.black);
		deselectedCluster.getTextAnnotation().setTextColor(Color.black);
	}

	public static void destroyCluster(Cluster clusterToDestroy, CommandExecutorTaskFactory executor, 
			DialogTaskManager dialogTaskManager) {
		// Get rid of the WordCloud
		ArrayList<String> commands = new ArrayList<String>();
		String command = "wordcloud delete cloudName=\"" + clusterToDestroy.getCloudName() + "\"";
		commands.add(command);
		ExecutorObserver observer = new ExecutorObserver();
		TaskIterator task = executor.createTaskIterator(commands, null);
		dialogTaskManager.execute(task, observer);
		while (!observer.isFinished()) {
			// Erase the cluster
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		clusterToDestroy.erase();
	}

	public static void drawCluster(Cluster cluster, CyNetworkView view, AnnotationFactory<ShapeAnnotation> shapeFactory, 
			AnnotationFactory<TextAnnotation> textFactory, AnnotationManager annotationManager) {
		// Constants used in making the appearance prettier
		double zoom = view.getVisualProperty(BasicVisualLexicon.NETWORK_SCALE_FACTOR);

    	// Find the edges of the annotation
		double xmin = 100000000;
		double ymin = 100000000;
		double xmax = -100000000;
		double ymax = -100000000;
		
		for (double[] coordinates : cluster.getCoordinates()) {
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
		ShapeAnnotation ellipse = shapeFactory.createAnnotation(ShapeAnnotation.class, view, arguments);
		ellipse.setShapeType("Ellipse");
		ellipse.setSize(width*padding*zoom, height*padding*zoom);
		ellipse.setBorderWidth(5.0);
		cluster.setEllipse(ellipse);
		annotationManager.addAnnotation(ellipse);

		// Parameters of the text label
		String labelText = cluster.getLabel();
		Integer fontSize = (int) Math.round((0.02*Math.pow(Math.pow(width, 2)+ Math.pow(height, 2), 0.5)));
		// To centre the annotation at the middle of the annotation
		xPos = (int) Math.round(xPos + width*padding/2 - 2.1*fontSize*labelText.length());
		yPos = (int) Math.round(yPos - 10.1*fontSize);
		
		// Create and draw the label
		arguments = new HashMap<String,String>();
		arguments.put("x", String.valueOf(xPos));
		arguments.put("y", String.valueOf(yPos));
		arguments.put("zoom", String.valueOf(zoom));
		arguments.put("canvas", "foreground");
		arguments.put("fontSize", String.valueOf((int) Math.round(fontSize*11*zoom)));
		TextAnnotation textAnnotation = textFactory.createAnnotation(TextAnnotation.class, view, arguments);
		textAnnotation.setText(labelText);
		cluster.setTextAnnotation(textAnnotation);
		annotationManager.addAnnotation(textAnnotation);
	}
}
