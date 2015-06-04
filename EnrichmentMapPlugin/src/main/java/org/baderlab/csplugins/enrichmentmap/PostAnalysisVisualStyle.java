package org.baderlab.csplugins.enrichmentmap;

import java.awt.Color;
import java.awt.Paint;
import java.util.HashMap;
import java.util.Map;

import org.baderlab.csplugins.enrichmentmap.task.BuildDiseaseSignatureTaskResult;
import org.cytoscape.equations.Equation;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
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

public class PostAnalysisVisualStyle {

	public static final String NAME = "Post_analysis_style";
	
	public static final double DEFAULT_WIDTH_EM_LOWER = 1.0;
	public static final double DEFAULT_WIDTH_EM_UPPER = 5.0;
	public static final double DEFAULT_WIDTH_PA_LESS_THAN_100 = 8.0;
	public static final double DEFAULT_WIDTH_PA_LESS_THAN_10 = 4.5;
	public static final double DEFAULT_WIDTH_PA_GREATER = 1.0;
	
	
	@SuppressWarnings("unused")
	private final VisualMappingFunctionFactory vmfFactoryContinuous;
    private final VisualMappingFunctionFactory vmfFactoryDiscrete;
    private final VisualMappingFunctionFactory vmfFactoryPassthrough;
    private final EquationCompiler equationCompiler;
    
	private final EnrichmentMapVisualStyle delegateStyle;
	
	// Column in edge table that holds the formula
	public static final String EDGE_WIDTH_FORMULA_COLUMN = "Edge_width_formula";
	// Column in network table that holds the edge parameters
	public static final String EDGE_WIDTH_PARAMETERS_COLUMN = "EM_Edge_width_parameters";
	
	private static final Color PINK   = new Color(255,0,200);    
	private static final Color YELLOW = new Color(255,255,0);
	
	
	public PostAnalysisVisualStyle(EnrichmentMapParameters emParsms, EquationCompiler equationCompiler,
			                       VisualMappingFunctionFactory vmfFactoryContinuous, VisualMappingFunctionFactory vmfFactoryDiscrete, VisualMappingFunctionFactory vmfFactoryPassthrough) {
		
		this.delegateStyle = new EnrichmentMapVisualStyle(emParsms, vmfFactoryContinuous, vmfFactoryDiscrete, vmfFactoryPassthrough);
		this.vmfFactoryContinuous = vmfFactoryContinuous;
        this.vmfFactoryDiscrete = vmfFactoryDiscrete;
        this.vmfFactoryPassthrough = vmfFactoryPassthrough;   
        this.equationCompiler = equationCompiler;
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
	public void applyNetworkSpeficifProperties(BuildDiseaseSignatureTaskResult taskResult, String prefix) {
		createNodeBypassForColor(taskResult);
		CyNetwork network = taskResult.getNetwork();
        
        CyTable networkTable = network.getDefaultNetworkTable();
        if(networkTable.getColumn(EDGE_WIDTH_PARAMETERS_COLUMN) == null) {
        	networkTable.createColumn(EDGE_WIDTH_PARAMETERS_COLUMN, String.class, false);
        }
        
		applyWidthEquation(equationCompiler, prefix, network);
	}

	
	private void createPostAnalysisAppearance(VisualStyle vs, String prefix) {
		// Post-analysis edge line type
		// Use a dashed line for post analysis on Dataset 2
        DiscreteMapping<String,LineType> lineType = (DiscreteMapping<String,LineType>) vmfFactoryDiscrete.createVisualMappingFunction(CyEdge.INTERACTION, String.class, BasicVisualLexicon.EDGE_LINE_TYPE);
        lineType.putMapValue(PostAnalysisParameters.SIGNATURE_INTERACTION_TYPE_SET2, LineTypeVisualProperty.LONG_DASH);
        vs.addVisualMappingFunction(lineType);
        
        // Add mapped value for post-analysis edge color
        @SuppressWarnings("unchecked")
		DiscreteMapping<Integer,Paint> disMapping_edge2 = (DiscreteMapping<Integer,Paint>) vs.getVisualMappingFunction(BasicVisualLexicon.EDGE_UNSELECTED_PAINT);
        disMapping_edge2.putMapValue(4, PINK); // pink
        @SuppressWarnings("unchecked")
		DiscreteMapping<Integer,Paint> disMapping_edge4 = (DiscreteMapping<Integer,Paint>) vs.getVisualMappingFunction(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);
        disMapping_edge4.putMapValue(4, PINK); // pink
        
        // Add mapping function for node shape
        DiscreteMapping<String,NodeShape> disMapping_nodeShape = (DiscreteMapping<String,NodeShape>) vmfFactoryDiscrete.createVisualMappingFunction(prefix + EnrichmentMapVisualStyle.GS_TYPE, String.class, BasicVisualLexicon.NODE_SHAPE);
        disMapping_nodeShape.putMapValue(EnrichmentMapVisualStyle.GS_TYPE_ENRICHMENT, NodeShapeVisualProperty.ELLIPSE);
        disMapping_nodeShape.putMapValue(EnrichmentMapVisualStyle.GS_TYPE_SIGNATURE, NodeShapeVisualProperty.TRIANGLE);
        vs.addVisualMappingFunction(disMapping_nodeShape);
        
        // Replace the edge width mapping that was created by EnrichmentMapVisualStyle
        String widthAttribute = prefix + EDGE_WIDTH_FORMULA_COLUMN;
        PassthroughMapping<Double,Double> edgeWidthMapping = (PassthroughMapping<Double,Double>) vmfFactoryPassthrough.createVisualMappingFunction(widthAttribute, Double.class, BasicVisualLexicon.EDGE_WIDTH);
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
	
	
	public static void applyWidthEquation(EquationCompiler equationCompiler, String prefix, CyNetwork network) {
		String widthAttribute = prefix + EDGE_WIDTH_FORMULA_COLUMN;
        CyTable edgeTable = network.getDefaultEdgeTable();
        if(edgeTable.getColumn(widthAttribute) == null) {
        	edgeTable.createColumn(widthAttribute, Double.class, false);
        }
        
        String formula = "=" + WidthFunction.NAME + "($SUID)";
        
        Equation equation = compileEquation(equationCompiler, edgeTable, formula);
        for(CyRow row : edgeTable.getAllRows()) {
        	row.set(widthAttribute, equation);
        }
	}
	
	
	private static Equation compileEquation(EquationCompiler equationCompiler, CyTable edgeTable, String formula) {
		Map<String, Class<?>> attribNameToType = attributeToTypeMap(edgeTable);
        if(equationCompiler.compile(formula, attribNameToType)) {
			return equationCompiler.getEquation();
        }
        else {
        	throw new RuntimeException(equationCompiler.getLastErrorMsg());
        }
	}
	
	private static Map<String,Class<?>> attributeToTypeMap(CyTable table) {
		Map<String,Class<?>> map = new HashMap<>();
		for(CyColumn column : table.getColumns())
			map.put(column.getName(), column.getType());
		return map;
	}
	
	
	public static boolean appliesTo(CyNetwork network) {
		CyTable networkTable = network.getDefaultNetworkTable();
	    return networkTable.getColumn(EDGE_WIDTH_PARAMETERS_COLUMN) != null;
	}
	
	
	/**
	 * Parameters typically used by the EdgeWidthDialog and stored in the network table.
	 */
	public static class EdgeWidthParams {
		public final double em_lower;
		public final double em_upper;
		public final double pa_lessThan100;
		public final double pa_lessThan10;
		public final double pa_greater;
		
		public EdgeWidthParams(double em_lower, double em_upper, double pa_lessThan100, double pa_lessThan10, double pa_greater) {
			this.em_lower = em_lower;
			this.em_upper = em_upper;
			this.pa_lessThan100 = pa_lessThan100;
			this.pa_lessThan10 = pa_lessThan10;
			this.pa_greater = pa_greater;
		}
		
		public static EdgeWidthParams defaultValues() {
			return new EdgeWidthParams(DEFAULT_WIDTH_EM_LOWER, DEFAULT_WIDTH_EM_UPPER,
					                   DEFAULT_WIDTH_PA_LESS_THAN_100, DEFAULT_WIDTH_PA_LESS_THAN_10, DEFAULT_WIDTH_PA_GREATER);
		}
		
		public static EdgeWidthParams restore(CyNetwork network) {
			try {
				String val = network.getRow(network).get(EDGE_WIDTH_PARAMETERS_COLUMN, String.class);
				String[] params = val.split(",");
				double em_lower = Double.parseDouble(params[0]);
				double em_upper = Double.parseDouble(params[1]);
				double pa_lessThan100 = Double.parseDouble(params[2]);
				double pa_lessThan10 = Double.parseDouble(params[3]);
				double pa_greater = Double.parseDouble(params[4]);
				return new EdgeWidthParams(em_lower, em_upper, pa_lessThan100, pa_lessThan10, pa_greater);
			} catch(NullPointerException | ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
				return defaultValues();
			}
		}
		
		public void save(CyNetwork network) {
			CyRow row = network.getRow(network);
			String val = String.format("%f,%f,%f,%f,%f", em_lower, em_upper, pa_lessThan100, pa_lessThan10, pa_greater);
			row.set(EDGE_WIDTH_PARAMETERS_COLUMN, val);
		}
	}
	
	
}
