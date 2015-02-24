package org.baderlab.csplugins.enrichmentmap.autoannotate.task.cluster;

import java.util.HashMap;
import java.util.Map;

import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationManager;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.AnnotationSet;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.Cluster;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class DrawClusterLabelTask extends AbstractTask{
	
	private Cluster cluster;
	private TaskMonitor taskMonitor;
	
	public DrawClusterLabelTask(Cluster cluster) {
		super();
		this.cluster = cluster;
	}
	
	public void drawTextLabel() {
		AutoAnnotationManager autoAnnotationManager = AutoAnnotationManager.getInstance();
		AnnotationSet parent = cluster.getParent();
		CyNetworkView view = parent.getView();
		boolean constantFontSize = parent.isConstantFontSize();
		int fontSize = parent.getFontSize();
		double[] labelPosition = parent.getLabelPosition();
		boolean showLabel = parent.isShowLabel();
		
		AnnotationFactory<TextAnnotation> textFactory = autoAnnotationManager.getTextFactory();
		AnnotationManager annotationManager = autoAnnotationManager.getAnnotationManager();
		double zoom = view.getVisualProperty(BasicVisualLexicon.NETWORK_SCALE_FACTOR);
		// Set the text of the label
		String labelText = cluster.getLabel();

		Map<String, String> ellipseArgs = cluster.getEllipse().getArgMap();
		double xPos = Double.parseDouble(ellipseArgs.get("x"));
		double yPos = Double.parseDouble(ellipseArgs.get("y"));
		double width = Double.parseDouble(ellipseArgs.get("width"));
		double height = Double.parseDouble(ellipseArgs.get("height"));
		
		// Create the text annotation 
		Integer labelFontSize = null;
		if (constantFontSize) {
			labelFontSize = fontSize;
		} else {
			labelFontSize = (int) Math.round(5*Math.pow(cluster.getSize(), 0.4));
		}
		double labelWidth = 2.3;
		double labelHeight = 4.8;
		if(labelText != null && labelFontSize != null){
			labelWidth= 2.3*labelFontSize*labelText.length();
			labelHeight = 4.8*labelFontSize;
		}
		
		
		double xOffset = labelPosition[0];
		double yOffset = labelPosition[1];
		
		// Set the position of the label relative to the ellipse
		if (yOffset == 0.5 && xOffset != 0.5) {
			// If vertically centered, label should go outside of cluster (to the right or left)
			xPos = (int) Math.round(xPos + width/zoom*xOffset + labelWidth*(xOffset-1));
		} else {
			xPos = (int) Math.round(xPos + width/zoom*xOffset - labelWidth*xOffset);
		}
		yPos = (int) Math.round(yPos + height/zoom*yOffset - labelHeight*(1.0-yOffset) - 10 + yOffset*20.0);
		
		// Create and draw the label
		HashMap<String, String> arguments = new HashMap<String,String>();
		arguments.put("x", String.valueOf(xPos));
		arguments.put("y", String.valueOf(yPos));
		arguments.put("zoom", String.valueOf(zoom));
		arguments.put("canvas", "foreground");
		TextAnnotation textAnnotation = textFactory.createAnnotation(TextAnnotation.class, view, arguments);
		
		if(textAnnotation != null && labelText != null){
		
			textAnnotation.setText(labelText);
			textAnnotation.setFontSize(5*zoom*labelFontSize);
			cluster.setTextAnnotation(textAnnotation);
			if (showLabel) {
				annotationManager.addAnnotation(textAnnotation);
			}
		}
	}


	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		this.taskMonitor = taskMonitor;
		this.taskMonitor.setTitle("Drawing cluster Text Label");

		drawTextLabel();
	}
}
