package org.baderlab.csplugins.enrichmentmap.style;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LABEL_TRANSPARENCY;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LINE_TYPE;
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
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_TOOLTIP;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_TRANSPARENCY;
import static org.cytoscape.view.presentation.property.NodeShapeVisualProperty.DIAMOND;
import static org.cytoscape.view.presentation.property.NodeShapeVisualProperty.ELLIPSE;
import static org.cytoscape.view.presentation.property.NodeShapeVisualProperty.RECTANGLE;

import java.awt.Color;
import java.awt.Paint;
import java.util.List;
import java.util.stream.Collectors;

import org.baderlab.csplugins.enrichmentmap.CytoscapeServiceModule.Continuous;
import org.baderlab.csplugins.enrichmentmap.CytoscapeServiceModule.Discrete;
import org.baderlab.csplugins.enrichmentmap.CytoscapeServiceModule.Passthrough;
import org.baderlab.csplugins.enrichmentmap.model.AbstractDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EMSignatureDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.events.VisualStyleChangeRecord;
import org.cytoscape.view.vizmap.events.VisualStyleChangedEvent;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import org.jcolorbrewer.ColorBrewer;

import com.google.inject.Inject;
 
public class EMStyleBuilder {
	
	public final static String DEFAULT_NAME_SUFFIX = "Visual_Style"; // TEMPORARY probably won't be called 'MasterMap' in the final version
	
	public final static String COMBINED = "Combined";
	
	public final static Integer DEF_NODE_TRANSPARENCY = 220;
	public final static Integer FILTERED_OUT_NODE_TRANSPARENCY = 40;
	
	private final static Double MIN_NODE_SIZE = 20.0;
	private final static Double MAX_NODE_SIZE = 60.0;
	
	public static final double DEF_NODE_BORDER_WIDTH = 1.0;
	
	public final static Integer DEF_EDGE_TRANSPARENCY = 200;
	public final static Integer FILTERED_OUT_EDGE_TRANSPARENCY = 10;
	
	private static final NodeShape SIGNATURE_NODE_SHAPE = DIAMOND;
	
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
		public static final ColumnDescriptor<Integer> EDGE_HYPERGEOM_U = new ColumnDescriptor<>("HyperGeom_N_Universe", Integer.class);
		public static final ColumnDescriptor<Integer> EDGE_HYPERGEOM_N = new ColumnDescriptor<>("HyperGeom_n_Sig_Universe", Integer.class);
		public static final ColumnDescriptor<Integer> EDGE_HYPERGEOM_K = new ColumnDescriptor<>("k_Intersection", Integer.class);
		public static final ColumnDescriptor<Integer> EDGE_HYPERGEOM_M = new ColumnDescriptor<>("m_Enr_Genes", Integer.class);
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
		public static final Color DEF_NODE_BORDER_COLOR = new Color(148, 148, 148);
		public static final Color DEF_NODE_COLOR = new Color(240, 240, 240);
		public static final Color SIG_NODE_BORDER_COLOR = new Color(239, 138, 98);
		public static final Color SIG_NODE_COLOR = new Color(253, 219, 199);

		// See http://colorbrewer2.org/#type=qualitative&scheme=Set2&n=3
		public static final Color SIG_EDGE_COLOR = new Color(252, 141, 98);
		public static final Color COMPOUND_EDGE_COLOR = new Color(102, 194, 165);
		
		/* See http://colorbrewer2.org/#type=diverging&scheme=RdBu&n=9 */
		public static final Color MAX_PHENOTYPE_1 = new Color(178, 24, 43);
		public static final Color LIGHTER_PHENOTYPE_1 = new Color(214, 96, 77);
		public static final Color LIGHTEST_PHENOTYPE_1 = new Color(244, 165, 130);
		public static final Color OVER_COLOR = new Color(247, 247, 247);
		public static final Color MAX_PHENOTYPE_2 = new Color(33, 102, 172);
		public static final Color LIGHTER_PHENOTYPE_2 = new Color(67, 147, 195);
		public static final Color LIGHTEST_PHENOTYPE_2 = new Color(146, 197, 222);
	
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
	
	public static NodeShape getGeneSetNodeShape(VisualStyle style) {
		return style.getDefaultValue(BasicVisualLexicon.NODE_SHAPE);
	}
	
	public static NodeShape getSignatureNodeShape(VisualStyle style) {
		return SIGNATURE_NODE_SHAPE;
	}
	
	public static NodeShape getDefaultNodeShape(ChartType chartType) {
		return chartType == null || chartType == ChartType.RADIAL_HEAT_MAP ? ELLIPSE : RECTANGLE;
	}
	
	public void updateProperties(VisualStyle vs, EMStyleOptions options, CyCustomGraphics2<?> chart) {
		eventHelper.silenceEventSource(vs);
		
		try {
			// Network Properties
			vs.setDefaultValue(NETWORK_BACKGROUND_PAINT, Colors.BG_COLOR);    	        
	    	
			setEdgeDefaults(vs, options);
			setEdgePaint(vs, options);
			setEdgeWidth(vs, options);
			setEdgeLineType(vs, options);
	 		
			String chartName = chart != null ? chart.getDisplayName() : null;
			ChartType chartType = ChartType.toChartType(chartName);
			
			setNodeDefaults(vs, options, chartType);
			setNodeShapes(vs, options, chartType);
			setNodeSize(vs, options, chartType);
			setNodeBorderColors(vs, options);
			setNodeColors(vs, options);
			setNodeLabels(vs, options);
			setNodeTooltip(vs, options);
			setNodeChart(vs, chart);
			
			if (options.isPublicationReady()) {
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
	
	private DiscreteMapping<String, Paint> createEdgeColorMapping(EMStyleOptions options, VisualProperty<Paint> vp) {
		String prefix = options.getAttributePrefix();
		String col = Columns.EDGE_DATASET.with(prefix, null);
		
		DiscreteMapping<String, Paint> edgePaint = (DiscreteMapping<String, Paint>) dmFactory
				.createVisualMappingFunction(col, String.class, vp);
		
		edgePaint.putMapValue(Columns.EDGE_DATASET_VALUE_COMPOUND, Colors.COMPOUND_EDGE_COLOR);
		edgePaint.putMapValue(Columns.EDGE_DATASET_VALUE_SIG, Colors.SIG_EDGE_COLOR);
		
		List<EMDataSet> dataSets = options.getEnrichmentMap().getDataSetList();
		final ColorBrewer colorBrewer;
		
		// Try colorblind and/or print friendly colours first
		if (dataSets.size() <= 4) // Try a colorblind safe color scheme first
			colorBrewer = ColorBrewer.Paired; // http://colorbrewer2.org/#type=qualitative&scheme=Paired&n=4
		else if (dataSets.size() <= 5) // Same--more than 5, it adds a RED that can be confused with the edge selection color
			colorBrewer = ColorBrewer.Paired; // http://colorbrewer2.org/#type=qualitative&scheme=Paired&n=5
		else
			colorBrewer = ColorBrewer.Set3; // http://colorbrewer2.org/#type=qualitative&scheme=Set3&n=12
			
		Color[] colors = colorBrewer.getColorPalette(dataSets.size());
		
		// Do not use the filtered data sets here, because we don't want edge colours changing when filtering
		for (int i = 0; i < dataSets.size(); i++) {
			EMDataSet ds = dataSets.get(i);
			Color color = colors[i];
			edgePaint.putMapValue(ds.getName(), color);
			ds.setColor(color);
		}
		
		return edgePaint;
	}
	
	private void setEdgeWidth(VisualStyle vs, EMStyleOptions options) {
		String prefix = options.getAttributePrefix();
		EnrichmentMap map = options.getEnrichmentMap();
		
		if (options.isPostAnalysis()) {
			// Replace the edge width mapping that was created by EnrichmentMapVisualStyle
			String widthAttribute = WidthFunction.EDGE_WIDTH_FORMULA_COLUMN.with(prefix, null);
			PassthroughMapping<Double, Double> edgewidth = (PassthroughMapping<Double, Double>) pmFactory
					.createVisualMappingFunction(widthAttribute, Double.class, BasicVisualLexicon.EDGE_WIDTH);
			vs.addVisualMappingFunction(edgewidth);
		} else {
			// Continous Mapping - set edge line thickness based on the number of genes in the overlap
			ContinuousMapping<Double, Double> edgewidth = (ContinuousMapping<Double, Double>) cmFactory
					.createVisualMappingFunction(Columns.EDGE_SIMILARITY_COEFF.with(prefix, null), Double.class, EDGE_WIDTH);
			
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
	}
	
	private void setEdgeLineType(VisualStyle vs, EMStyleOptions options) {
		String prefix = options.getAttributePrefix();
		String col = Columns.EDGE_DATASET.with(prefix, null);
		
		DiscreteMapping<String, LineType> dm = (DiscreteMapping<String, LineType>) dmFactory
				.createVisualMappingFunction(col, String.class, EDGE_LINE_TYPE);
		dm.putMapValue(Columns.EDGE_DATASET_VALUE_COMPOUND, LineTypeVisualProperty.SOLID);
		dm.putMapValue(Columns.EDGE_DATASET_VALUE_SIG, LineTypeVisualProperty.DOT);
		
		vs.addVisualMappingFunction(dm);
	}
	
	private void setNodeDefaults(VisualStyle vs, EMStyleOptions options, ChartType chartType) {
		// Set the default node appearance
		vs.setDefaultValue(NODE_FILL_COLOR, Colors.DEF_NODE_COLOR);
		vs.setDefaultValue(NODE_BORDER_PAINT, Colors.DEF_NODE_BORDER_COLOR);
		vs.setDefaultValue(NODE_SHAPE, getDefaultNodeShape(chartType));
		vs.setDefaultValue(NODE_SIZE, chartType == ChartType.RADIAL_HEAT_MAP ? MIN_NODE_SIZE : (MAX_NODE_SIZE + MIN_NODE_SIZE) / 2);
		vs.setDefaultValue(NODE_BORDER_WIDTH, DEF_NODE_BORDER_WIDTH);
		vs.setDefaultValue(NODE_TRANSPARENCY, DEF_NODE_TRANSPARENCY);
		vs.setDefaultValue(NODE_BORDER_TRANSPARENCY, DEF_NODE_TRANSPARENCY);
		vs.setDefaultValue(NODE_LABEL_TRANSPARENCY, DEF_NODE_TRANSPARENCY);
	}
	
	private void setNodeShapes(VisualStyle vs, EMStyleOptions options, ChartType chartType) {
		String prefix = options.getAttributePrefix();
		
		// Add mapping function for node shape
		DiscreteMapping<String, NodeShape> nodeShape = (DiscreteMapping<String, NodeShape>) dmFactory
				.createVisualMappingFunction(Columns.NODE_GS_TYPE.with(prefix, null), String.class, NODE_SHAPE);
		nodeShape.putMapValue(Columns.NODE_GS_TYPE_ENRICHMENT, getDefaultNodeShape(chartType));
		nodeShape.putMapValue(Columns.NODE_GS_TYPE_SIGNATURE, SIGNATURE_NODE_SHAPE);
		vs.addVisualMappingFunction(nodeShape);
	}
	
	private void setNodeBorderColors(VisualStyle vs, EMStyleOptions options) {
		String prefix = options.getAttributePrefix();
		
		// Add mapping function for node border color
		DiscreteMapping<String, Paint> nodePaint = (DiscreteMapping<String, Paint>) dmFactory
				.createVisualMappingFunction(Columns.NODE_GS_TYPE.with(prefix, null), String.class, NODE_BORDER_PAINT);
		nodePaint.putMapValue(Columns.NODE_GS_TYPE_ENRICHMENT, Colors.DEF_NODE_BORDER_COLOR);
		nodePaint.putMapValue(Columns.NODE_GS_TYPE_SIGNATURE, Colors.SIG_NODE_BORDER_COLOR);
		vs.addVisualMappingFunction(nodePaint);
	}
	
	private void setNodeColors(VisualStyle vs, EMStyleOptions options) {
		String prefix = options.getAttributePrefix();
		List<AbstractDataSet> dataSets = options.getDataSets()
				.stream()
				.filter(ds -> ds instanceof EMDataSet) // Ignore Signature Data Sets in charts
				.collect(Collectors.toList());
		
		if (dataSets.size() == 1) {
			// Only 1 Data Set? Use node colour instead of charts...
			EMDataSet ds = (EMDataSet) dataSets.iterator().next();
			
			// Create boundary conditions
			BoundaryRangeValues<Paint> bv3a = new BoundaryRangeValues<>(
					Colors.MAX_PHENOTYPE_2, Colors.MAX_PHENOTYPE_2, Colors.MAX_PHENOTYPE_2);
			BoundaryRangeValues<Paint> bv3b = new BoundaryRangeValues<>(
					Colors.LIGHTER_PHENOTYPE_2, Colors.LIGHTER_PHENOTYPE_2, Colors.MAX_PHENOTYPE_2);
			BoundaryRangeValues<Paint> bv3c = new BoundaryRangeValues<>(
					Colors.LIGHTEST_PHENOTYPE_2, Colors.LIGHTEST_PHENOTYPE_2, Colors.LIGHTER_PHENOTYPE_2);
			BoundaryRangeValues<Paint> bv3d = new BoundaryRangeValues<>(
					Colors.LIGHTEST_PHENOTYPE_2, Colors.OVER_COLOR, Colors.OVER_COLOR);
			BoundaryRangeValues<Paint> bv3e = new BoundaryRangeValues<>(
					Colors.OVER_COLOR, Colors.OVER_COLOR, Colors.OVER_COLOR);
			BoundaryRangeValues<Paint> bv3f = new BoundaryRangeValues<>(
					Colors.OVER_COLOR, Colors.OVER_COLOR, Colors.LIGHTEST_PHENOTYPE_1);
			BoundaryRangeValues<Paint> bv3g = new BoundaryRangeValues<>(
					Colors.LIGHTEST_PHENOTYPE_1, Colors.LIGHTEST_PHENOTYPE_1, Colors.LIGHTER_PHENOTYPE_1);
			BoundaryRangeValues<Paint> bv3h = new BoundaryRangeValues<>(
					Colors.LIGHTER_PHENOTYPE_1, Colors.LIGHTER_PHENOTYPE_1, Colors.MAX_PHENOTYPE_1);
			BoundaryRangeValues<Paint> bv3i = new BoundaryRangeValues<>(
					Colors.MAX_PHENOTYPE_1, Colors.MAX_PHENOTYPE_1, Colors.MAX_PHENOTYPE_1);
	
			// Continuous Mapping - set node colour based on the sign of the ES score of the dataset
			ContinuousMapping<Double, Paint> cm = (ContinuousMapping<Double, Paint>) cmFactory
					.createVisualMappingFunction(Columns.NODE_COLOURING.with(prefix, ds.getName()), Double.class,
							BasicVisualLexicon.NODE_FILL_COLOR);
	
			// Set the attribute point values associated with the boundary values
			cm.addPoint(-1.0, bv3a);
			cm.addPoint(-0.995, bv3b);
			cm.addPoint(-0.95, bv3c);
			cm.addPoint(-0.9, bv3d);
			cm.addPoint(0.0, bv3e);
			cm.addPoint(0.9, bv3f);
			cm.addPoint(0.95, bv3g);
			cm.addPoint(0.995, bv3h);
			cm.addPoint(1.0, bv3i);
	
			vs.addVisualMappingFunction(cm);
			
			// Then we need to use bypass to colour the hub nodes (signature genesets)
			List<EMSignatureDataSet> signatureDataSets = options.getEnrichmentMap().getSignatureSetList();
			CyNetworkView netView = options.getNetworkView();
			CyNetwork net = netView.getModel();
			
			for (EMSignatureDataSet sds : signatureDataSets) {
				for (Long suid : sds.getNodeSuids()) {
					CyNode node = net.getNode(suid);
					
					if (node != null) {
						View<CyNode> nv = netView.getNodeView(node);
						
						if (nv != null)
							nv.setLockedValue(NODE_FILL_COLOR, Colors.SIG_NODE_COLOR);
					}
				}
			}
		} else {
			// 2 or more Data Sets? Use simple node colours and charts...
			// Add mapping function for node fill color
			DiscreteMapping<String, Paint> dm = (DiscreteMapping<String, Paint>) dmFactory.createVisualMappingFunction(
					Columns.NODE_GS_TYPE.with(prefix, null), String.class, NODE_FILL_COLOR);
			dm.putMapValue(Columns.NODE_GS_TYPE_ENRICHMENT, Colors.DEF_NODE_COLOR);
			dm.putMapValue(Columns.NODE_GS_TYPE_SIGNATURE, Colors.SIG_NODE_COLOR);
			
			vs.addVisualMappingFunction(dm);
		}
	}
	
	private void setNodeLabels(VisualStyle vs, EMStyleOptions options) {
		String prefix = options.getAttributePrefix();
		PassthroughMapping<String, String> nodeLabel = (PassthroughMapping<String, String>) pmFactory
				.createVisualMappingFunction(Columns.NODE_GS_DESCR.with(prefix, null), String.class, NODE_LABEL);
		vs.addVisualMappingFunction(nodeLabel);
	}
	
	private void setNodeTooltip(VisualStyle vs, EMStyleOptions options) {
		String prefix = options.getAttributePrefix();
		PassthroughMapping<String, String> nodeLabel = (PassthroughMapping<String, String>) pmFactory
				.createVisualMappingFunction(Columns.NODE_GS_DESCR.with(prefix ,null), String.class, NODE_TOOLTIP);
		vs.addVisualMappingFunction(nodeLabel);
	}
	
	private void setNodeSize(VisualStyle vs, EMStyleOptions options, ChartType chartType) {
		if (chartType == null || chartType == ChartType.RADIAL_HEAT_MAP) {
			String prefix = options.getAttributePrefix();
			ContinuousMapping<Integer, Double> nodeSize = (ContinuousMapping<Integer, Double>) cmFactory
					.createVisualMappingFunction(Columns.NODE_GS_SIZE.with(prefix,null), Integer.class, NODE_SIZE);
	
			BoundaryRangeValues<Double> bv0 = new BoundaryRangeValues<Double>(MIN_NODE_SIZE, MIN_NODE_SIZE, MIN_NODE_SIZE);
			BoundaryRangeValues<Double> bv1 = new BoundaryRangeValues<Double>(MAX_NODE_SIZE, MAX_NODE_SIZE, MAX_NODE_SIZE);
			nodeSize.addPoint(10, bv0);
			nodeSize.addPoint(474, bv1);
	
			vs.addVisualMappingFunction(nodeSize);
		} else {
			vs.removeVisualMappingFunction(NODE_SIZE);
		}
	}
}
