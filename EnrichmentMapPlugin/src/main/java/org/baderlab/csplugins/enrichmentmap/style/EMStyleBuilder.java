package org.baderlab.csplugins.enrichmentmap.style;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LABEL_TRANSPARENCY;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_TRANSPARENCY;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_UNSELECTED_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_WIDTH;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_BACKGROUND_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_BORDER_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_BORDER_TRANSPARENCY;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_BORDER_WIDTH;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_FILL_COLOR;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_LABEL;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_LABEL_TRANSPARENCY;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_SHAPE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_SIZE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_TRANSPARENCY;
import static org.cytoscape.view.presentation.property.NodeShapeVisualProperty.ELLIPSE;
import static org.cytoscape.view.presentation.property.NodeShapeVisualProperty.RECTANGLE;
import static org.cytoscape.view.presentation.property.NodeShapeVisualProperty.TRIANGLE;

import java.awt.Color;
import java.awt.Paint;

import org.baderlab.csplugins.enrichmentmap.CytoscapeServiceModule.Continuous;
import org.baderlab.csplugins.enrichmentmap.CytoscapeServiceModule.Discrete;
import org.baderlab.csplugins.enrichmentmap.CytoscapeServiceModule.Passthrough;
import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.events.VisualStyleChangeRecord;
import org.cytoscape.view.vizmap.events.VisualStyleChangedEvent;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;

import com.google.inject.Inject;
 
public class EMStyleBuilder {
	
	public final static String DEFAULT_NAME_SUFFIX = "Visual_Style"; // TEMPORARY probably won't be called 'MasterMap' in the final version
	
	public final static String COMBINED = "Combined";
	
	public final static Integer DEF_NODE_TRANSPARENCY = 220;
	public final static Integer FILTERED_OUT_NODE_TRANSPARENCY = 40;
	
	private final static Double MIN_NODE_SIZE = 20.0;
	private final static Double MAX_NODE_SIZE = 60.0;
	
	public final static Integer DEF_EDGE_TRANSPARENCY = 140;
	public final static Integer FILTERED_OUT_EDGE_TRANSPARENCY = 10;
	
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
		
		// Multi-edge case
		public static final ColumnDescriptor<String> EDGE_DATASET = new ColumnDescriptor<>("Data Set", String.class);
		public static final String EDGE_DATASET_VALUE_COMPOUND = "compound"; 
		public static final String EDGE_DATASET_VALUE_SIG = "signature"; // post-analysis edges
		
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
		
		public static final ColumnDescriptor<String> NET_REPORT1_DIR = new ColumnDescriptor<>("GSEA_Report_Dataset1_folder", String.class);
		public static final ColumnDescriptor<String> NET_REPORT2_DIR = new ColumnDescriptor<>("GSEA_Report_Dataset2_folder", String.class);
	}

	public static class Colors {
		// See http://colorbrewer2.org/#type=diverging&scheme=RdBu&n=6
		public static final Color DEF_NODE_BORDER_COLOR = new Color(33, 102, 172);
		public static final Color DEF_NODE_COLOR = new Color(209, 229, 240);
		public static final Color SIG_NODE_BORDER_COLOR = new Color(239, 138, 98);
		public static final Color SIG_NODE_COLOR = new Color(253, 219, 199);

		// See http://colorbrewer2.org/?type=qualitative&scheme=Dark2&n=8#type=qualitative&scheme=Dark2&n=8
		private static final Color EDGE_COLOR_SIG = new Color(231,41,138);
		private static final Color EDGE_COLOR_COMPOUND = new Color(27,158,119);
		private static final Color[] EDGE_COLORS_DISTINCT = 
			{new Color(217,95,2), new Color(117,112,179), new Color(231,41,138), new Color(102,166,30), 
			 new Color(230,171,2), new Color(166,118,29), new Color(102,102,102)};
	
		public static final Color LIGHT_GREY = new Color(190, 190, 190);
		private static final Color BG_COLOR = Color.WHITE;
	}
	
	@Inject private @Continuous  VisualMappingFunctionFactory cmFactory;
	@Inject private @Discrete    VisualMappingFunctionFactory dmFactory;
	@Inject private @Passthrough VisualMappingFunctionFactory pmFactory;
	
	@Inject private RenderingEngineManager renderingEngineManager;
	@Inject private CyEventHelper eventHelper;
	
	public static String getStyleName(EnrichmentMap map) {
		String prefix = map.getParams().getAttributePrefix();
		return prefix + DEFAULT_NAME_SUFFIX;
	}
	
	public void updateProperties(VisualStyle vs, EMStyleOptions options, CyCustomGraphics2<?> chart) {
		eventHelper.silenceEventSource(vs);
		
		try {
			// Network Properties
			vs.setDefaultValue(NETWORK_BACKGROUND_PAINT, Colors.BG_COLOR);    	        
	    	
			setEdgeDefaults(vs, options);
			setEdgePaint(vs, options);
			setEdgeWidth(vs, options);
	 		
			String chartName = chart != null ? chart.getDisplayName().toLowerCase() : null;
			boolean hasChart = chartName != null && !chartName.startsWith("ring") && !chartName.startsWith("pie");
			
			setNodeDefaults(vs, options, hasChart);
			setNodeShapes(vs, options, hasChart);
			setNodeSize(vs, options, hasChart);
			setNodeBorderColors(vs, options, hasChart);
			setNodeColors(vs, options, hasChart);
			setNodeLabels(vs, options);
			setNodeChart(vs, chart);
			
			if (options.isPublicationReady()) {
				// TODO review:
				vs.removeVisualMappingFunction(BasicVisualLexicon.NODE_LABEL);
				vs.setDefaultValue(BasicVisualLexicon.NODE_LABEL, "");
				vs.removeVisualMappingFunction(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT);
				vs.setDefaultValue(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT, Color.WHITE);
			}
		} finally {
			eventHelper.unsilenceEventSource(vs);
			eventHelper.addEventPayload(vs, new VisualStyleChangeRecord(), VisualStyleChangedEvent.class);
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void setNodeChart(VisualStyle vs, CyCustomGraphics2<?> chart) {
		VisualLexicon lexicon = renderingEngineManager.getDefaultVisualLexicon();
		VisualProperty customPaint1 = lexicon.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_1");
		
		if (customPaint1 != null)
			vs.setDefaultValue(customPaint1, chart);
	}
	
	private void setEdgeDefaults(VisualStyle vs, EMStyleOptions options) {
		vs.setDefaultValue(EDGE_TRANSPARENCY, DEF_EDGE_TRANSPARENCY);
		vs.setDefaultValue(EDGE_LABEL_TRANSPARENCY, DEF_EDGE_TRANSPARENCY);
	}
	
	private void setEdgePaint(VisualStyle vs, EMStyleOptions options) {
		DiscreteMapping<String, Paint> edgePaint = createEdgeColorMapping(options, EDGE_UNSELECTED_PAINT);
		vs.addVisualMappingFunction(edgePaint);
		DiscreteMapping<String, Paint> edgeStrokePaint = createEdgeColorMapping(options, EDGE_STROKE_UNSELECTED_PAINT);
		vs.addVisualMappingFunction(edgeStrokePaint);
	}
	
	private DiscreteMapping<String,Paint> createEdgeColorMapping(EMStyleOptions options, VisualProperty<Paint> vp) {
		String prefix = options.getAttributePrefix();
		String col = Columns.EDGE_DATASET.with(prefix, null);
		DiscreteMapping<String, Paint> edgePaint = (DiscreteMapping<String,Paint>) dmFactory.createVisualMappingFunction(col, String.class, vp);
		edgePaint.putMapValue(Columns.EDGE_DATASET_VALUE_COMPOUND, Colors.EDGE_COLOR_COMPOUND);
		edgePaint.putMapValue(Columns.EDGE_DATASET_VALUE_SIG, Colors.EDGE_COLOR_SIG);
		int i = 0;
		for(DataSet dataSet : options.getDataSets()) {
			Color color = Colors.EDGE_COLORS_DISTINCT[i++ % Colors.EDGE_COLORS_DISTINCT.length];
			edgePaint.putMapValue(dataSet.getName(), color);
			dataSet.setColor(color);
		}
		return edgePaint;
	}
	
	private void setEdgeWidth(VisualStyle vs, EMStyleOptions options) {
		String prefix = options.getAttributePrefix();
		EnrichmentMap map = options.getEnrichmentMap();
		
		// Continous Mapping - set edge line thickness based on the number of genes in the overlap
		ContinuousMapping<Double, Double> edgewidth = (ContinuousMapping<Double, Double>) cmFactory
				.createVisualMappingFunction(Columns.EDGE_SIMILARITY_COEFF.with(prefix,null), Double.class, EDGE_WIDTH);
		
		Double underWidth = 0.5;
		Double minWidth   = 1.0;
		Double maxWidth   = 5.0;
		Double overWidth  = 6.0;

		// Create boundary conditions                  less than,   equals,  greater than
		BoundaryRangeValues<Double> bv4 = new BoundaryRangeValues<>(underWidth, minWidth, minWidth);
		BoundaryRangeValues<Double> bv5 = new BoundaryRangeValues<>(maxWidth, maxWidth, overWidth);
		edgewidth.addPoint(map.getParams().getSimilarityCutoff(), bv4);
		edgewidth.addPoint(1.0, bv5);

		vs.addVisualMappingFunction(edgewidth);
	}
	
	private void setNodeDefaults(VisualStyle vs, EMStyleOptions options, boolean hasChart) {
		// Set the default node appearance
		vs.setDefaultValue(NODE_FILL_COLOR, Colors.DEF_NODE_COLOR);
		vs.setDefaultValue(NODE_BORDER_PAINT, Colors.DEF_NODE_BORDER_COLOR);
		vs.setDefaultValue(NODE_SHAPE, hasChart ? RECTANGLE : ELLIPSE);
		vs.setDefaultValue(NODE_SIZE, hasChart ? (MAX_NODE_SIZE + MIN_NODE_SIZE) / 2 : MIN_NODE_SIZE);
		vs.setDefaultValue(NODE_BORDER_WIDTH, 1.0);
		vs.setDefaultValue(NODE_TRANSPARENCY, DEF_NODE_TRANSPARENCY);
		vs.setDefaultValue(NODE_BORDER_TRANSPARENCY, DEF_NODE_TRANSPARENCY);
		vs.setDefaultValue(NODE_LABEL_TRANSPARENCY, DEF_NODE_TRANSPARENCY);
	}
	
	private void setNodeShapes(VisualStyle vs, EMStyleOptions options, boolean hasChart) {
		String prefix = options.getAttributePrefix();
		
		// Add mapping function for node shape
		DiscreteMapping<String, NodeShape> nodeShape = (DiscreteMapping<String, NodeShape>) dmFactory
				.createVisualMappingFunction(Columns.NODE_GS_TYPE.with(prefix, null), String.class, NODE_SHAPE);
		nodeShape.putMapValue(Columns.NODE_GS_TYPE_ENRICHMENT, hasChart ? RECTANGLE : ELLIPSE);
		nodeShape.putMapValue(Columns.NODE_GS_TYPE_SIGNATURE, TRIANGLE);
		vs.addVisualMappingFunction(nodeShape);
	}
	
	private void setNodeBorderColors(VisualStyle vs, EMStyleOptions options, boolean hasChart) {
		String prefix = options.getAttributePrefix();
		
		// Add mapping function for node border color
		DiscreteMapping<String, Paint> nodePaint = (DiscreteMapping<String, Paint>) dmFactory
				.createVisualMappingFunction(Columns.NODE_GS_TYPE.with(prefix, null), String.class, NODE_BORDER_PAINT);
		nodePaint.putMapValue(Columns.NODE_GS_TYPE_ENRICHMENT, Colors.DEF_NODE_BORDER_COLOR);
		nodePaint.putMapValue(Columns.NODE_GS_TYPE_SIGNATURE, Colors.SIG_NODE_BORDER_COLOR);
		vs.addVisualMappingFunction(nodePaint);
	}
	
	private void setNodeColors(VisualStyle vs, EMStyleOptions options, boolean hasChart) {
		String prefix = options.getAttributePrefix();
		
		// Add mapping function for node fill color
		DiscreteMapping<String, Paint> nodePaint = (DiscreteMapping<String, Paint>) dmFactory
				.createVisualMappingFunction(Columns.NODE_GS_TYPE.with(prefix, null), String.class, NODE_FILL_COLOR);
		nodePaint.putMapValue(Columns.NODE_GS_TYPE_ENRICHMENT, Colors.DEF_NODE_COLOR);
		nodePaint.putMapValue(Columns.NODE_GS_TYPE_SIGNATURE, Colors.SIG_NODE_COLOR);
		vs.addVisualMappingFunction(nodePaint);
	}
	
	private void setNodeLabels(VisualStyle vs, EMStyleOptions options) {
		String prefix = options.getAttributePrefix();
		PassthroughMapping<String, String> nodeLabel = (PassthroughMapping<String, String>) pmFactory
				.createVisualMappingFunction(Columns.NODE_GS_DESCR.with(prefix,null), String.class, NODE_LABEL);
		vs.addVisualMappingFunction(nodeLabel);
	}
	
	private void setNodeSize(VisualStyle vs, EMStyleOptions options, boolean hasChart) {
		if (hasChart) {
			vs.removeVisualMappingFunction(NODE_SIZE);
		} else {
			String prefix = options.getAttributePrefix();
			ContinuousMapping<Integer, Double> nodeSize = (ContinuousMapping<Integer, Double>) cmFactory
					.createVisualMappingFunction(Columns.NODE_GS_SIZE.with(prefix,null), Integer.class, NODE_SIZE);
	
			BoundaryRangeValues<Double> bv0 = new BoundaryRangeValues<Double>(MIN_NODE_SIZE, MIN_NODE_SIZE, MIN_NODE_SIZE);
			BoundaryRangeValues<Double> bv1 = new BoundaryRangeValues<Double>(MAX_NODE_SIZE, MAX_NODE_SIZE, MAX_NODE_SIZE);
			nodeSize.addPoint(10, bv0);
			nodeSize.addPoint(474, bv1);
	
			vs.addVisualMappingFunction(nodeSize);
		}
	}
}
