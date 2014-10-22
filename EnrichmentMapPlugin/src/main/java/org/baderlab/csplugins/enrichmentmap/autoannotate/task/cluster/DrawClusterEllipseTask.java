package org.baderlab.csplugins.enrichmentmap.autoannotate.task.cluster;

import java.awt.Color;
import java.util.HashMap;

import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationManager;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.AnnotationSet;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.Cluster;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class DrawClusterEllipseTask extends AbstractTask{

	private int min_size = 50; // Minimum size of the ellipse
	private Color fillColor = Color.getHSBColor(0.19f, 1.25f, 0.95f);
	
	private Cluster cluster;
	private TaskMonitor taskMonitor = null;
	
	
	public DrawClusterEllipseTask(Cluster cluster) {
		super();
		this.cluster = cluster;
	}
	

	public void drawEllipse() {
		AutoAnnotationManager autoAnnotationManager = AutoAnnotationManager.getInstance();
		AnnotationSet parent = cluster.getParent();
		CyNetworkView view = parent.getView();
		String shapeType = parent.getShapeType();
		int ellipseBorderWidth = parent.getEllipseWidth();
		boolean showEllipses = parent.isShowEllipses();
		int ellipseOpacity = parent.getEllipseOpacity();
		
		AnnotationFactory<ShapeAnnotation> shapeFactory = autoAnnotationManager.getShapeFactory();
		AnnotationManager annotationManager = autoAnnotationManager.getAnnotationManager();
		
		double zoom = view.getVisualProperty(BasicVisualLexicon.NETWORK_SCALE_FACTOR);

		double[] bounds = cluster.getBounds();
		double xmin = bounds[0];
		double xmax = bounds[1];
		double ymin = bounds[2];
		double ymax = bounds[3];
		
		double centreX = (xmin + xmax)/2;
		double centreY = (ymin + ymax)/2;
		double width = (xmax - xmin);
		width = width > min_size ? width : min_size;
		double height = (ymax - ymin);
		height = height > min_size ? height : min_size;
		HashMap<CyNode, double[]> nodesToCoordinates = cluster.getNodesToCoordinates();
		HashMap<CyNode, Double> nodesToRadii = cluster.getNodesToRadii();
		
		if (shapeType.equals("ELLIPSE")) {
			while (nodesOutOfCluster(nodesToCoordinates, nodesToRadii, 
					width, height, centreX, centreY, ellipseBorderWidth)) {
				width *= 1.1;
				height *= 1.1;
			}
			width += 40;
			height += 40;
		} else {
			width += 50;
			height += 50;
		}
		
		// Set the position of the top-left corner of the ellipse
		Integer xPos = (int) Math.round(centreX - width/2);
		Integer yPos = (int) Math.round(centreY - height/2);
		
		// Create and draw the ellipse
		HashMap<String, String> arguments = new HashMap<String,String>();
		arguments.put("x", String.valueOf(xPos));
		arguments.put("y", String.valueOf(yPos));
		arguments.put("zoom", String.valueOf(zoom));
		arguments.put("canvas", "background");			
		arguments.put("shapeType", shapeType);
		ShapeAnnotation ellipse = shapeFactory.createAnnotation(ShapeAnnotation.class, view, arguments);
		ellipse.setSize(width*zoom, height*zoom);
		ellipse.setBorderWidth(ellipseBorderWidth);
		ellipse.setBorderColor(Color.DARK_GRAY);
		ellipse.setFillColor(fillColor);
		ellipse.setFillOpacity(ellipseOpacity);
		cluster.setEllipse(ellipse);
		if (showEllipses) {
			annotationManager.addAnnotation(ellipse);
		}
	}
	private boolean nodesOutOfCluster(HashMap<CyNode, double[]> nodesToCoordinates, 
			HashMap<CyNode, Double> nodesToRadii, 
			double width, double height,
			double centreX, double centreY, int ellipseWidth) {
		double semimajor_axis = width/2;
		double semiminor_axis = height/2;
		for (CyNode node : nodesToCoordinates.keySet()) {
			double[] coordinates = nodesToCoordinates.get(node);
			double nodeSize = nodesToRadii.get(node);
			if (Math.pow((coordinates[0] - centreX - ellipseWidth)/semimajor_axis,2) +
					Math.pow(nodeSize/semimajor_axis, 2) +
					Math.pow((coordinates[1] - centreY - ellipseWidth)/semiminor_axis,2) +
					Math.pow(nodeSize/semiminor_axis,  2) >= 1) {
				return true;
			}
		}
		return false;
	}


	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		this.taskMonitor = taskMonitor;
		this.taskMonitor.setTitle("Drawing bounding ellipse/box around cluster");
	
		drawEllipse();
		
	}
	
}
