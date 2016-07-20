package org.baderlab.csplugins.enrichmentmap;

import java.awt.Color;
import java.awt.Paint;

import org.baderlab.csplugins.enrichmentmap.task.BuildDiseaseSignatureTaskResult;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import org.cytoscape.work.TaskMonitor;

public class PostAnalysisVisualStyle {

	public static final String NAME = "Post_analysis_style";

	private final VisualMappingFunctionFactory vmfFactoryContinuous;
	private final VisualMappingFunctionFactory vmfFactoryDiscrete;
	private final VisualMappingFunctionFactory vmfFactoryPassthrough;

	private final EnrichmentMapVisualStyle delegateStyle;

	private static final Color PINK = new Color(255, 0, 200);
	private static final Color YELLOW = new Color(255, 255, 0);

	public PostAnalysisVisualStyle(EnrichmentMapParameters emParsms, VisualMappingFunctionFactory vmfFactoryContinuous,
			VisualMappingFunctionFactory vmfFactoryDiscrete, VisualMappingFunctionFactory vmfFactoryPassthrough) {

		this.delegateStyle = new EnrichmentMapVisualStyle(emParsms, vmfFactoryContinuous, vmfFactoryDiscrete, vmfFactoryPassthrough);
		this.vmfFactoryContinuous = vmfFactoryContinuous;
		this.vmfFactoryDiscrete = vmfFactoryDiscrete;
		this.vmfFactoryPassthrough = vmfFactoryPassthrough;
	}

	/**
	 * Create a new post analysis visual style.
	 */
	public void createVisualStyle(VisualStyle vs, String prefix) {
		delegateStyle.applyVisualStyle(vs, prefix);
		createPostAnalysisAppearance(vs, prefix);
	}

	/**
	 * Sets node bypasses and edge equations.
	 */
	public void applyNetworkSpeficifProperties(BuildDiseaseSignatureTaskResult taskResult, String prefix, TaskMonitor taskMonitor) {
		createNodeBypassForColor(taskResult);
		CyNetwork network = taskResult.getNetwork();

		WidthFunction widthFunction = new WidthFunction(vmfFactoryContinuous);
		widthFunction.setEdgeWidths(network, prefix, taskMonitor);
	}

	private void createPostAnalysisAppearance(VisualStyle vs, String prefix) {
		// Post-analysis edge line type
		// Use a dashed line for post analysis on Dataset 2
		DiscreteMapping<String, LineType> lineType = (DiscreteMapping<String, LineType>) vmfFactoryDiscrete.createVisualMappingFunction(CyEdge.INTERACTION, String.class, BasicVisualLexicon.EDGE_LINE_TYPE);
		lineType.putMapValue(PostAnalysisParameters.SIGNATURE_INTERACTION_TYPE_SET2, LineTypeVisualProperty.LONG_DASH);
		vs.addVisualMappingFunction(lineType);

		// Add mapped value for post-analysis edge color
		@SuppressWarnings("unchecked")
		DiscreteMapping<Integer, Paint> disMapping_edge2 = (DiscreteMapping<Integer, Paint>) vs.getVisualMappingFunction(BasicVisualLexicon.EDGE_UNSELECTED_PAINT);
		disMapping_edge2.putMapValue(4, PINK); // pink
		@SuppressWarnings("unchecked")
		DiscreteMapping<Integer, Paint> disMapping_edge4 = (DiscreteMapping<Integer, Paint>) vs.getVisualMappingFunction(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);
		disMapping_edge4.putMapValue(4, PINK); // pink

		// Add mapping function for node shape
		DiscreteMapping<String, NodeShape> disMapping_nodeShape = (DiscreteMapping<String, NodeShape>) vmfFactoryDiscrete
				.createVisualMappingFunction(prefix + EnrichmentMapVisualStyle.GS_TYPE, String.class, BasicVisualLexicon.NODE_SHAPE);
		disMapping_nodeShape.putMapValue(EnrichmentMapVisualStyle.GS_TYPE_ENRICHMENT, NodeShapeVisualProperty.ELLIPSE);
		disMapping_nodeShape.putMapValue(EnrichmentMapVisualStyle.GS_TYPE_SIGNATURE, NodeShapeVisualProperty.TRIANGLE);
		vs.addVisualMappingFunction(disMapping_nodeShape);

		// Replace the edge width mapping that was created by EnrichmentMapVisualStyle
		String widthAttribute = prefix + WidthFunction.EDGE_WIDTH_FORMULA_COLUMN;
		PassthroughMapping<Double, Double> edgeWidthMapping = (PassthroughMapping<Double, Double>) vmfFactoryPassthrough
				.createVisualMappingFunction(widthAttribute, Double.class, BasicVisualLexicon.EDGE_WIDTH);
		vs.addVisualMappingFunction(edgeWidthMapping);
	}

	private void createNodeBypassForColor(BuildDiseaseSignatureTaskResult taskResult) {
		for(CyNode node : taskResult.getNewNodes()) {
			View<CyNode> hubNodeView = taskResult.getNetworkView().getNodeView(node);
			if(hubNodeView != null) {
				hubNodeView.setLockedValue(BasicVisualLexicon.NODE_FILL_COLOR, YELLOW);
				hubNodeView.setLockedValue(BasicVisualLexicon.NODE_BORDER_PAINT, YELLOW);
			}
		}
	}

}
