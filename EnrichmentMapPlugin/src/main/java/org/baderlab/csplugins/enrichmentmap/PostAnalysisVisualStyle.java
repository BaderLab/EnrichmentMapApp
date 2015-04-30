package org.baderlab.csplugins.enrichmentmap;

import java.awt.Color;
import java.awt.Paint;

import org.baderlab.csplugins.enrichmentmap.task.BuildDiseaseSignatureTaskResult;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.model.CyEdge;
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

public class PostAnalysisVisualStyle {

	private final VisualMappingFunctionFactory vmfFactoryContinuous;
    private final VisualMappingFunctionFactory vmfFactoryDiscrete;
    private final VisualMappingFunctionFactory vmfFactoryPassthrough;
    private final EquationCompiler equationCompiler;
    
	private final EnrichmentMapVisualStyle delegateStyle;
	
	private static final String EDGE_WIDTH_FORMULA_COLUMN = "Edge_width_formula";
	
	private static final Color PINK   = new Color(255,0,200);    
	private static final Color YELLOW = new Color(255,255,0);
	
	
	public PostAnalysisVisualStyle(PostAnalysisParameters paParams, EnrichmentMapParameters emParsms, EquationCompiler equationCompiler,
			                       VisualMappingFunctionFactory vmfFactoryContinuous, VisualMappingFunctionFactory vmfFactoryDiscrete, VisualMappingFunctionFactory vmfFactoryPassthrough) {
		
		this.delegateStyle = new EnrichmentMapVisualStyle(emParsms, vmfFactoryContinuous, vmfFactoryDiscrete, vmfFactoryPassthrough);
		this.vmfFactoryContinuous = vmfFactoryContinuous;
        this.vmfFactoryDiscrete = vmfFactoryDiscrete;
        this.vmfFactoryPassthrough = vmfFactoryPassthrough;   
        this.equationCompiler = equationCompiler;
	}
	
	
	public void applyVisualStyle(BuildDiseaseSignatureTaskResult taskResult, VisualStyle vs, String prefix) {
		delegateStyle.applyVisualStyle(vs, prefix);
		createPostAnalysisAppearance(taskResult, vs, prefix);
	}


	// First figure out how to handle the bypasses, and get rid of the flickering
	// Then get rid of the extra columns that you added before... or create the columns here if you have to
	// Then compute the formula stuff
	@SuppressWarnings("unchecked")
	private void createPostAnalysisAppearance(BuildDiseaseSignatureTaskResult taskResult, VisualStyle vs, String prefix) {
		// Post-analysis edge line type
		// Use a dashed line for post analysis on Dataset 2
        DiscreteMapping<String,LineType> lineType = (DiscreteMapping<String,LineType>) vmfFactoryDiscrete.createVisualMappingFunction(CyEdge.INTERACTION, String.class, BasicVisualLexicon.EDGE_LINE_TYPE);
        lineType.putMapValue(PostAnalysisParameters.SIGNATURE_INTERACTION_TYPE_SET2, LineTypeVisualProperty.LONG_DASH);
        vs.addVisualMappingFunction(lineType);
        
        // Add mapped value for post-analysis edge color
        DiscreteMapping<Integer,Paint> disMapping_edge2 = (DiscreteMapping<Integer,Paint>) vs.getVisualMappingFunction(BasicVisualLexicon.EDGE_UNSELECTED_PAINT);
        disMapping_edge2.putMapValue(4, PINK); // pink
        DiscreteMapping<Integer,Paint> disMapping_edge4 = (DiscreteMapping<Integer,Paint>) vs.getVisualMappingFunction(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);
        disMapping_edge4.putMapValue(4, PINK); // pink
        
        // Add mapping function for node shape
        DiscreteMapping<String,NodeShape> disMapping_nodeShape = (DiscreteMapping<String,NodeShape>) vmfFactoryDiscrete.createVisualMappingFunction(prefix + EnrichmentMapVisualStyle.GS_TYPE, String.class, BasicVisualLexicon.NODE_SHAPE);
        disMapping_nodeShape.putMapValue(EnrichmentMapVisualStyle.GS_TYPE_ENRICHMENT, NodeShapeVisualProperty.ELLIPSE);
        disMapping_nodeShape.putMapValue(EnrichmentMapVisualStyle.GS_TYPE_SIGNATURE, NodeShapeVisualProperty.TRIANGLE);
        vs.addVisualMappingFunction(disMapping_nodeShape);
        
        // Set bypass for signature hub node color
        for(CyNode node : taskResult.getNewNodes()) {
        	View<CyNode> hubNodeView = taskResult.getNetworkView().getNodeView(node);
        	if(hubNodeView != null) {
				hubNodeView.setLockedValue(BasicVisualLexicon.NODE_FILL_COLOR, YELLOW);               
				hubNodeView.setLockedValue(BasicVisualLexicon.NODE_BORDER_PAINT, YELLOW);
        	}
        }
        
        
//        // NOW... edge width
//        // Remove existing edge width attribute?
//        // Create a new column
//        // Create a formula for that column
//        // Create a vmf pointing at formula column
//        
//        String widthAttribute = prefix + EDGE_WIDTH_FORMULA_COLUMN;
//        String similarityAttribute = prefix + EnrichmentMapVisualStyle.SIMILARITY_COEFFICIENT;
//        
//        CyTable edgeTable = taskResult.getNetwork().getDefaultEdgeTable();
//        if(edgeTable.getColumn(widthAttribute) == null)
//        	edgeTable.createColumn(widthAttribute, Integer.class, false);
//        
//        
//        // If (interaction is "sig" or "sig_set1" or "sig_set2") then ? else $SIMILARITY_COEFFICIENT
//        String formula = "=" + WidthFunction.NAME + "($SUID)";
//        
//        Equation equation = compileEquation(edgeTable, formula);
//        for(CyRow row : edgeTable.getAllRows()) {
//        	row.set(widthAttribute, equation);
//        }
        
	}
	
	
//	private Equation compileEquation(CyTable edgeTable, String formula) {
//		Map<String, Class<?>> attribNameToType = attributeToTypeMap(edgeTable);
//        if(equationCompiler.compile(formula, attribNameToType)) {
//			return equationCompiler.getEquation();
//        }
//        else {
//        	throw new RuntimeException(equationCompiler.getLastErrorMsg());
//        }
//	}
//	
//	private static Map<String,Class<?>> attributeToTypeMap(CyTable table) {
//		Map<String,Class<?>> map = new HashMap<>();
//		for(CyColumn column : table.getColumns())
//			map.put(column.getName(), column.getType());
//		return map;
//	}
//	
	
}
