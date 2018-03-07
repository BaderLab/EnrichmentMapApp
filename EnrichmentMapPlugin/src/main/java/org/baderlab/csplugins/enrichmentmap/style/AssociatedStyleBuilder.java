package org.baderlab.csplugins.enrichmentmap.style;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_BORDER_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_BORDER_TRANSPARENCY;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_BORDER_WIDTH;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_FILL_COLOR;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_LABEL_TRANSPARENCY;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_SHAPE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_TRANSPARENCY;

import org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder.Colors;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
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
	
	public static class Columns {
		// GeneMANIA Attributes
		// NODE
		public static final ColumnDescriptor<String> GM_GENE_NAME = new ColumnDescriptor<>("gene name", String.class);
		// NETWORK
		public static final ColumnDescriptor<String> GM_ORGANISM = new ColumnDescriptor<>("organism", String.class);
	}
	
	public void updateProperties(VisualStyle vs, AssociatedStyleOptions options, CyCustomGraphics2<?> chart) {
		eventHelper.silenceEventSource(vs);
		
		try {
			String chartName = chart != null ? chart.getDisplayName() : null;
			ChartType chartType = ChartType.toChartType(chartName);
			
			setNodeDefaults(vs, options, chartType);
			setNodeChart(vs, chart);
			
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
	
	private void setNodeDefaults(VisualStyle vs, AssociatedStyleOptions options, ChartType chartType) {
		// Set the default node appearance
		vs.setDefaultValue(NODE_FILL_COLOR, Colors.DEF_NODE_COLOR);
		vs.setDefaultValue(NODE_BORDER_PAINT, Colors.DEF_NODE_BORDER_COLOR);
		vs.setDefaultValue(NODE_BORDER_WIDTH, EMStyleBuilder.DEF_NODE_BORDER_WIDTH);
		vs.setDefaultValue(NODE_TRANSPARENCY, EMStyleBuilder.DEF_NODE_TRANSPARENCY);
		vs.setDefaultValue(NODE_BORDER_TRANSPARENCY, EMStyleBuilder.DEF_NODE_TRANSPARENCY);
		vs.setDefaultValue(NODE_LABEL_TRANSPARENCY, EMStyleBuilder.DEF_NODE_TRANSPARENCY);
		setNodeChartDefaults(vs, chartType);
	}
	
	/**
	 * Sets default node visual properties that can be affected by the chart type.
	 */
	private void setNodeChartDefaults(VisualStyle vs, ChartType chartType) {
		vs.setDefaultValue(NODE_SHAPE, EMStyleBuilder.getDefaultNodeShape(chartType));
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void setNodeChart(VisualStyle vs, CyCustomGraphics2<?> chart) {
		VisualLexicon lexicon = renderingEngineManager.getDefaultVisualLexicon();
		VisualProperty customPaint1 = lexicon.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_1");
		
		if (customPaint1 != null)
			vs.setDefaultValue(customPaint1, chart);
	}
}
