package org.baderlab.csplugins.enrichmentmap.style;

import java.awt.Color;
import java.awt.Paint;

import org.baderlab.csplugins.enrichmentmap.CytoscapeServiceModule.Continuous;
import org.baderlab.csplugins.enrichmentmap.CytoscapeServiceModule.Discrete;
import org.baderlab.csplugins.enrichmentmap.CytoscapeServiceModule.Passthrough;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;

import com.google.inject.Inject;
 
public class MasterMapVisualStyle {
	
	public final static String DEFAULT_NAME_SUFFIX = "Visual_Style"; // TEMPORARY probably won't be called 'MasterMap' in the final version
	public final static String COMBINED = "Combined";
	
	public static class Columns {
		// Common attributes that apply to the entire network
		public static final ColumnDescriptor<String> NODE_NAME = new ColumnDescriptor<>("Name", String.class);
		public static final ColumnDescriptor<String> NODE_GS_DESCR = new ColumnDescriptor<>("GS_DESCR", String.class);
		public static final ColumnDescriptor<String> NODE_GS_TYPE  = new ColumnDescriptor<>("GS_Type", String.class);
		public static final String NODE_GS_TYPE_ENRICHMENT = "ENR";
		public static final String NODE_GS_TYPE_SIGNATURE  = "SIG";
		public static final ColumnDescriptor<String> NODE_FORMATTED_NAME = new ColumnDescriptor<>("Formatted_name", String.class);
		public static final ColumnListDescriptor<String> NODE_GENES = new ColumnListDescriptor<>("Genes", String.class);
		public static final ColumnDescriptor<Integer> NODE_GS_SIZE  = new ColumnDescriptor<>("gs_size", Integer.class);
		
		// Per-DataSet attributes
		// GSEA attributes
		public static final ColumnDescriptor<Double> NODE_PVALUE      = new ColumnDescriptor<>("pvalue", Double.class);
		public static final ColumnDescriptor<Double> NODE_FDR_QVALUE  = new ColumnDescriptor<>("fdr_qvalue", Double.class);
		public static final ColumnDescriptor<Double> NODE_FWER_QVALUE = new ColumnDescriptor<>("fwer_qvalue", Double.class);
		public static final ColumnDescriptor<Double> NODE_ES          = new ColumnDescriptor<>("ES", Double.class);
		public static final ColumnDescriptor<Double> NODE_NES         = new ColumnDescriptor<>("NES", Double.class);
		public static final ColumnDescriptor<Double> NODE_COLOURING   = new ColumnDescriptor<>("Colouring", Double.class);
		
		// Post-analysis Node attributes
		public static final ColumnListDescriptor<String> NODE_ENR_GENES = new ColumnListDescriptor<>("Enrichment_Genes", String.class);
		
		// Per-DataSet attributes
		// Edge attributes
		public static final ColumnDescriptor<Double> EDGE_SIMILARITY_COEFF = new ColumnDescriptor<>("similarity_coefficient", Double.class);
		public static final ColumnDescriptor<Integer> EDGE_OVERLAP_SIZE = new ColumnDescriptor<>("Overlap_size", Integer.class);
		public static final ColumnListDescriptor<String> EDGE_OVERLAP_GENES = new ColumnListDescriptor<>("Overlap_genes", String.class);
		public static final ColumnDescriptor<Integer> EDGE_ENRICHMENT_SET = new ColumnDescriptor<>("ENRICHMENT_SET", Integer.class);
		public static final Integer EDGE_ENRICHMENT_SET_ENR = 0;
		public static final Integer EDGE_ENRICHMENT_SET_SIG = 4; // for backwards compatibility
		
		
		// Post-analysis Edge Attributes
		public static final ColumnDescriptor<Double> EDGE_HYPERGEOM_PVALUE = new ColumnDescriptor<>("Overlap_Hypergeom_pVal", Double.class);
		public static final ColumnDescriptor<Double> EDGE_HYPERGEOM_CUTOFF = new ColumnDescriptor<>("Overlap_Hypergeom_cutoff", Double.class);
		public static final ColumnDescriptor<Integer> EDGE_HYPERGEOM_N = new ColumnDescriptor<>("HyperGeom_N_Universe", Integer.class);
		public static final ColumnDescriptor<Integer> EDGE_HYPERGEOM_n = new ColumnDescriptor<>("HyperGeom_n_Sig_Universe", Integer.class);
		public static final ColumnDescriptor<Integer> EDGE_HYPERGEOM_k = new ColumnDescriptor<>("k_Intersection", Integer.class);
		public static final ColumnDescriptor<Integer> EDGE_HYPERGEOM_m = new ColumnDescriptor<>("m_Enr_Genes", Integer.class);
		public static final ColumnDescriptor<Double> EDGE_MANN_WHIT_TWOSIDED_PVALUE = new ColumnDescriptor<>("Overlap_Mann_Whit_pVal", Double.class);
		public static final ColumnDescriptor<Double> EDGE_MANN_WHIT_GREATER_PVALUE = new ColumnDescriptor<>("Overlap_Mann_Whit_greater_pVal", Double.class);
		public static final ColumnDescriptor<Double> EDGE_MANN_WHIT_LESS_PVALUE = new ColumnDescriptor<>("Overlap_Mann_Whit_less_pVal", Double.class);
		public static final ColumnDescriptor<Double> EDGE_MANN_WHIT_CUTOFF = new ColumnDescriptor<>("Overlap_Mann_Whit_cutoff", Double.class);
		public static final ColumnDescriptor<String> EDGE_CUTOFF_TYPE = new ColumnDescriptor<>("Overlap_cutoff", String.class);
	}

	public static class Colors {
		/* See http://colorbrewer2.org/#type=diverging&scheme=RdBu&n=9 */
		public static final Color MAX_PHENOTYPE_1 = new Color(178, 24, 43);
		public static final Color LIGHTER_PHENOTYPE_1 = new Color(214, 96, 77);
		public static final Color LIGHTEST_PHENOTYPE_1 = new Color(244, 165, 130);
		public static final Color OVER_COLOR = new Color(247, 247, 247);
		public static final Color MAX_PHENOTYPE_2 = new Color(33, 102, 172);
		public static final Color LIGHTER_PHENOTYPE_2 = new Color(67, 147, 195);
		public static final Color LIGHTEST_PHENOTYPE_2 = new Color(146, 197, 222);
	
		public static final Color LIGHT_GREY = new Color(190, 190, 190);
	
		/* See http://colorbrewer2.org/#type=qualitative&scheme=Dark2&n=3 */
		private static final Color EDGE_COLOR = new Color(27, 158, 119);
		private static final Color EDGE_COLOR_SIG = new Color(217, 95, 2);
		private static final Color BG_COLOR = Color.WHITE;
		
	}
	
	
	@Inject private @Continuous  VisualMappingFunctionFactory vmfFactoryContinuous;
	@Inject private @Discrete    VisualMappingFunctionFactory vmfFactoryDiscrete;
	@Inject private @Passthrough VisualMappingFunctionFactory vmfFactoryPassthrough;
	
	@Inject private RenderingEngineManager renderingEngineManager;
	
	
	public static String getStyleName(EnrichmentMap map) {
		String prefix = map.getParams().getAttributePrefix();
		return prefix + DEFAULT_NAME_SUFFIX;
	}
	
	public void updateProperties(VisualStyle vs, MasterMapStyleOptions options, CyCustomGraphics2<?> chart) {
		// MKTODO silence events?
		
		// Network Properties
		vs.setDefaultValue(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT, Colors.BG_COLOR);    	        
    	vs.setDefaultValue(BasicVisualLexicon.EDGE_TRANSPARENCY, 100);
    	
		setEdgeDefaults(vs, options);
		setEdgeWidth(vs, options);
 		
		setNodeDefaults(vs, options);
		setNodeLabels(vs, options);
		setNodeSize(vs, options);
		setNodeChart(vs, chart);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void setNodeChart(VisualStyle vs, CyCustomGraphics2<?> chart) {
		VisualLexicon lexicon = renderingEngineManager.getDefaultVisualLexicon(); 
		VisualProperty customPaint1 = lexicon.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_1");
		
		if (customPaint1 != null)
			vs.setDefaultValue(customPaint1, chart);
	}
	
	private void setEdgeDefaults(VisualStyle vs, MasterMapStyleOptions options) {
		String prefix = options.getAttributePrefix();
		
		DiscreteMapping<Integer, Paint> edgePaint = (DiscreteMapping<Integer, Paint>) vmfFactoryDiscrete
				.createVisualMappingFunction(Columns.EDGE_ENRICHMENT_SET.with(prefix, null), Integer.class, BasicVisualLexicon.EDGE_UNSELECTED_PAINT);
		edgePaint.putMapValue(Columns.EDGE_ENRICHMENT_SET_ENR, Colors.EDGE_COLOR);
		edgePaint.putMapValue(Columns.EDGE_ENRICHMENT_SET_SIG, Colors.EDGE_COLOR_SIG);
		vs.addVisualMappingFunction(edgePaint);
		
		DiscreteMapping<Integer, Paint> edgeStrokePaint = (DiscreteMapping<Integer, Paint>) vmfFactoryDiscrete
				.createVisualMappingFunction(Columns.EDGE_ENRICHMENT_SET.with(prefix, null), Integer.class, BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);
		edgeStrokePaint.putMapValue(Columns.EDGE_ENRICHMENT_SET_ENR, Colors.EDGE_COLOR);
		edgeStrokePaint.putMapValue(Columns.EDGE_ENRICHMENT_SET_SIG, Colors.EDGE_COLOR_SIG);
		vs.addVisualMappingFunction(edgeStrokePaint);
	}
	
	private void setEdgeWidth(VisualStyle vs, MasterMapStyleOptions options) {
		String prefix = options.getAttributePrefix();
		
		EnrichmentMap map = options.getEnrichmentMap();
		//Continous Mapping - set edge line thickness based on the number of genes in the overlap
		ContinuousMapping<Double, Double> edgewidth = (ContinuousMapping<Double, Double>) vmfFactoryContinuous
				.createVisualMappingFunction(Columns.EDGE_SIMILARITY_COEFF.with(prefix,null), Double.class, BasicVisualLexicon.EDGE_WIDTH);
		
		Double under_width = 0.5;
		Double min_width   = 1.0;
		Double max_width   = 5.0;
		Double over_width  = 6.0;

		// Create boundary conditions                  less than,   equals,  greater than
		BoundaryRangeValues<Double> bv4 = new BoundaryRangeValues<>(under_width, min_width, min_width);
		BoundaryRangeValues<Double> bv5 = new BoundaryRangeValues<>(max_width, max_width, over_width);
		edgewidth.addPoint(map.getParams().getSimilarityCutoff(), bv4);
		edgewidth.addPoint(1.0, bv5);

		vs.addVisualMappingFunction(edgewidth);
	}
	
	private void setNodeDefaults(VisualStyle vs, MasterMapStyleOptions options) {
		String prefix = options.getAttributePrefix();
		//set the default node appearance
		vs.setDefaultValue(BasicVisualLexicon.NODE_FILL_COLOR, Colors.MAX_PHENOTYPE_1);
		vs.setDefaultValue(BasicVisualLexicon.NODE_BORDER_PAINT, Colors.MAX_PHENOTYPE_1);
		vs.setDefaultValue(BasicVisualLexicon.NODE_SHAPE, NodeShapeVisualProperty.ELLIPSE);
		vs.setDefaultValue(BasicVisualLexicon.NODE_SIZE, 15.0);
		vs.setDefaultValue(BasicVisualLexicon.NODE_BORDER_WIDTH, 0.0);
		
		// Add mapping function for node shape
		DiscreteMapping<String, NodeShape> nodeShape = (DiscreteMapping<String, NodeShape>) vmfFactoryDiscrete
				.createVisualMappingFunction(Columns.NODE_GS_TYPE.with(prefix, null), String.class, BasicVisualLexicon.NODE_SHAPE);
		nodeShape.putMapValue(Columns.NODE_GS_TYPE_ENRICHMENT, NodeShapeVisualProperty.ELLIPSE);
		nodeShape.putMapValue(Columns.NODE_GS_TYPE_SIGNATURE, NodeShapeVisualProperty.TRIANGLE);
		vs.addVisualMappingFunction(nodeShape);
	}
	
	private void setNodeLabels(VisualStyle vs, MasterMapStyleOptions options) {
		String prefix = options.getAttributePrefix();
		PassthroughMapping<String, String> nodeLabel = (PassthroughMapping<String, String>) vmfFactoryPassthrough
				.createVisualMappingFunction(Columns.NODE_GS_DESCR.with(prefix,null), String.class, BasicVisualLexicon.NODE_LABEL);
		vs.addVisualMappingFunction(nodeLabel);
	}
	
	private void setNodeSize(VisualStyle vs, MasterMapStyleOptions options) {
		String prefix = options.getAttributePrefix();
		ContinuousMapping<Integer, Double> nodeSize = (ContinuousMapping<Integer, Double>) vmfFactoryContinuous
				.createVisualMappingFunction(Columns.NODE_GS_SIZE.with(prefix,null), Integer.class, BasicVisualLexicon.NODE_SIZE);

		Double min = 20.0;
		Double max = 65.0;

		BoundaryRangeValues<Double> bv0 = new BoundaryRangeValues<Double>(min, min, min);
		BoundaryRangeValues<Double> bv1 = new BoundaryRangeValues<Double>(max, max, max);
		nodeSize.addPoint(10, bv0);
		nodeSize.addPoint(474, bv1);

		vs.addVisualMappingFunction(nodeSize);
	}
}
