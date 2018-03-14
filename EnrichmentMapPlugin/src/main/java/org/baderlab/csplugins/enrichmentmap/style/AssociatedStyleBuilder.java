package org.baderlab.csplugins.enrichmentmap.style;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_BORDER_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_BORDER_TRANSPARENCY;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_BORDER_WIDTH;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_FILL_COLOR;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_LABEL_TRANSPARENCY;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_SHAPE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_TRANSPARENCY;
import static org.cytoscape.view.presentation.property.NodeShapeVisualProperty.ELLIPSE;
import static org.cytoscape.view.presentation.property.NodeShapeVisualProperty.RECTANGLE;

import org.baderlab.csplugins.enrichmentmap.model.AssociatedApp;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder.Colors;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
import org.cytoscape.view.presentation.property.values.Justification;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.presentation.property.values.ObjectPosition;
import org.cytoscape.view.presentation.property.values.Position;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.events.VisualStyleChangeRecord;
import org.cytoscape.view.vizmap.events.VisualStyleChangedEvent;

import com.google.inject.Inject;

/**
 * Responsible for updating the styles of associated networks (e.g. GeneMANIA, STRING).
 */
public class AssociatedStyleBuilder {

	@Inject private RenderingEngineManager renderingEngineManager;
	@Inject private CyEventHelper eventHelper;
	
	public void updateProperties(VisualStyle vs, AssociatedStyleOptions options, CyCustomGraphics2<?> chart) {
		eventHelper.silenceEventSource(vs);
		
		try {
			setNodeDefaults(vs, options);
			setNodeChart(vs, chart, options.getChartOptions(), options.getAssociatedApp());
			
//			if (options.isPublicationReady()) {
//				vs.removeVisualMappingFunction(BasicVisualLexicon.NODE_LABEL);
//				vs.setDefaultValue(BasicVisualLexicon.NODE_LABEL, "");
//				vs.setDefaultValue(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT, Color.WHITE);
//			}
		} finally {
			eventHelper.unsilenceEventSource(vs);
			eventHelper.addEventPayload(vs, new VisualStyleChangeRecord(), VisualStyleChangedEvent.class);
		}
	}
	
	private void setNodeDefaults(VisualStyle vs, AssociatedStyleOptions options) {
		// Set the default node appearance
		vs.setDefaultValue(NODE_FILL_COLOR, Colors.DEF_NODE_COLOR);
		vs.setDefaultValue(NODE_BORDER_PAINT, Colors.DEF_NODE_BORDER_COLOR);
		vs.setDefaultValue(NODE_BORDER_WIDTH, EMStyleBuilder.DEF_NODE_BORDER_WIDTH);
		vs.setDefaultValue(NODE_TRANSPARENCY, EMStyleBuilder.DEF_NODE_TRANSPARENCY);
		vs.setDefaultValue(NODE_BORDER_TRANSPARENCY, EMStyleBuilder.DEF_NODE_TRANSPARENCY);
		vs.setDefaultValue(NODE_LABEL_TRANSPARENCY, EMStyleBuilder.DEF_NODE_TRANSPARENCY);
		setNodeChartDefaults(vs, options);
	}
	
	/**
	 * Sets default node visual properties that can be affected by the chart type.
	 */
	private void setNodeChartDefaults(VisualStyle vs, AssociatedStyleOptions options) {
		if (options.getAssociatedApp() == AssociatedApp.STRING) // Do not change the node shape of STRING networks!
			return;
		
		ChartType type = options.getChartOptions() != null ? options.getChartOptions().getType() : null;
		vs.setDefaultValue(NODE_SHAPE, getDefaultNodeShape(type));
	}
	
	public static NodeShape getDefaultNodeShape(ChartType type) {
		return type == ChartType.HEAT_STRIPS ? RECTANGLE : ELLIPSE;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void setNodeChart(VisualStyle vs, CyCustomGraphics2<?> chart, ChartOptions options, AssociatedApp app) {
		VisualLexicon lexicon = renderingEngineManager.getDefaultVisualLexicon();
		// Use Custom Graphics #9 to avoid interfering with other charts from the original app style
		VisualProperty customGraphics = lexicon.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_9");
		
		if (customGraphics != null) {
			vs.setDefaultValue(customGraphics, chart);
			
			if (chart != null && options != null) {
				VisualProperty graphicsPosition = lexicon.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_POSITION_9");
				
				if (graphicsPosition != null) {
					final ObjectPosition pos;
					
					if (options.getType() == ChartType.HEAT_STRIPS && app == AssociatedApp.STRING)
						pos = new ObjectPosition(Position.SOUTH, Position.NORTH, Justification.JUSTIFY_CENTER, 0, 10);
					else
						pos = new ObjectPosition(Position.CENTER, Position.CENTER, Justification.JUSTIFY_CENTER, 0, 0);
					
					vs.setDefaultValue(graphicsPosition, pos);
				}
			}
		}
	}
}
