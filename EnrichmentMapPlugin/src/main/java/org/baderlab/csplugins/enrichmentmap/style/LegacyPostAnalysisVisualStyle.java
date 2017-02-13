package org.baderlab.csplugins.enrichmentmap.style;

import org.baderlab.csplugins.enrichmentmap.CytoscapeServiceModule.Continuous;
import org.baderlab.csplugins.enrichmentmap.CytoscapeServiceModule.Discrete;
import org.baderlab.csplugins.enrichmentmap.CytoscapeServiceModule.Passthrough;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.task.BuildDiseaseSignatureTaskResult;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

/**
 * This allows to apply a post-analysis visual style to legacy EM networks.
 * This is only used for legacy networks. New MasterMap networks have post analysis
 * style properties already set in MasterMapVisualStyle. 
 */
@Deprecated
public class LegacyPostAnalysisVisualStyle {
	
	public static final String NAME = "Post_analysis_style";
	
//	/* See http://colorbrewer2.org/#type=qualitative&scheme=Dark2&n=3 */
//	private static final Color SIG_EDGE_COLOR = new Color(217, 95, 2);
//	
//	private static final Color SIG_NODE_BORDER_COLOR = new Color(223, 194, 125);
//	private static final Color SIG_NODE_COLOR = new Color(246, 232, 195);

	@Inject private @Continuous  VisualMappingFunctionFactory vmfFactoryContinuous;
	@Inject private @Discrete    VisualMappingFunctionFactory vmfFactoryDiscrete;
	@Inject private @Passthrough VisualMappingFunctionFactory vmfFactoryPassthrough;
	@Inject private Provider<WidthFunction> widthFunctionProvider;
	@Inject private EMStyleBuilder styleBuilder;

	private final EnrichmentMap map;
	
	public interface Factory {
		LegacyPostAnalysisVisualStyle create(EnrichmentMap map);
	}
	
	@Inject
	public LegacyPostAnalysisVisualStyle(@Assisted EnrichmentMap map) {
		this.map = map;
	}

	/**
	 * Create a new post analysis visual style.
	 */
	public void createVisualStyle(VisualStyle vs, String prefix) {
		// TODO
//		styleBuilder.applyVisualStyle(vs, prefix);
//		createPostAnalysisAppearance(vs, prefix);
	}

	/**
	 * Sets node bypasses and edge equations.
	 */
	public void applyNetworkSpeficifProperties(BuildDiseaseSignatureTaskResult taskResult, String prefix, TaskMonitor taskMonitor) {
//		createNodeBypassForColor(taskResult);
		CyNetwork network = taskResult.getNetwork();

		WidthFunction widthFunction = widthFunctionProvider.get();
		widthFunction.setEdgeWidths(network, prefix, taskMonitor);
	}

//	private void createPostAnalysisAppearance(VisualStyle vs, String prefix) {
//		// Post-analysis edge line type
//		// Use a dashed line for post analysis on Dataset 2
//		DiscreteMapping<String, LineType> lineType = (DiscreteMapping<String, LineType>) vmfFactoryDiscrete.createVisualMappingFunction(CyEdge.INTERACTION, String.class, BasicVisualLexicon.EDGE_LINE_TYPE);
//		lineType.putMapValue(PostAnalysisParameters.SIGNATURE_INTERACTION_TYPE_SET2, LineTypeVisualProperty.LONG_DASH);
//		vs.addVisualMappingFunction(lineType);
//
//		// Add mapped value for post-analysis edge color
//		@SuppressWarnings("unchecked")
//		DiscreteMapping<Integer, Paint> disMapping_edge2 = (DiscreteMapping<Integer, Paint>) vs.getVisualMappingFunction(BasicVisualLexicon.EDGE_UNSELECTED_PAINT);
//		disMapping_edge2.putMapValue(4, SIG_EDGE_COLOR);
//		@SuppressWarnings("unchecked")
//		DiscreteMapping<Integer, Paint> disMapping_edge4 = (DiscreteMapping<Integer, Paint>) vs.getVisualMappingFunction(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);
//		disMapping_edge4.putMapValue(4, SIG_EDGE_COLOR);
//
//		// Add mapping function for node shape
//		DiscreteMapping<String, NodeShape> disMapping_nodeShape = (DiscreteMapping<String, NodeShape>) vmfFactoryDiscrete
//				.createVisualMappingFunction(prefix + EnrichmentMapVisualStyle.GS_TYPE, String.class, BasicVisualLexicon.NODE_SHAPE);
//		disMapping_nodeShape.putMapValue(EnrichmentMapVisualStyle.GS_TYPE_ENRICHMENT, NodeShapeVisualProperty.ELLIPSE);
//		disMapping_nodeShape.putMapValue(EnrichmentMapVisualStyle.GS_TYPE_SIGNATURE, NodeShapeVisualProperty.TRIANGLE);
//		vs.addVisualMappingFunction(disMapping_nodeShape);
//
//		// Replace the edge width mapping that was created by EnrichmentMapVisualStyle
//		useFormulaForEdgeWidth(vs, prefix, vmfFactoryPassthrough);
//	}
	
	public static void useFormulaForEdgeWidth(VisualStyle vs, String prefix, VisualMappingFunctionFactory vmfFactoryPassthrough) {
		// Replace the edge width mapping that was created by EnrichmentMapVisualStyle
		String widthAttribute = WidthFunction.EDGE_WIDTH_FORMULA_COLUMN.with(prefix, null);
		PassthroughMapping<Double, Double> edgeWidthMapping = (PassthroughMapping<Double, Double>) vmfFactoryPassthrough
				.createVisualMappingFunction(widthAttribute, Double.class, BasicVisualLexicon.EDGE_WIDTH);
		vs.addVisualMappingFunction(edgeWidthMapping);
	}

//	public static void createNodeBypassForColor(BuildDiseaseSignatureTaskResult taskResult) {
//		for(CyNode node : taskResult.getNewNodes()) {
//			View<CyNode> hubNodeView = taskResult.getNetworkView().getNodeView(node);
//			if(hubNodeView != null) {
//				hubNodeView.setLockedValue(BasicVisualLexicon.NODE_FILL_COLOR, SIG_NODE_COLOR);
//				hubNodeView.setLockedValue(BasicVisualLexicon.NODE_BORDER_PAINT, SIG_NODE_BORDER_COLOR);
//			}
//		}
//	}
}
