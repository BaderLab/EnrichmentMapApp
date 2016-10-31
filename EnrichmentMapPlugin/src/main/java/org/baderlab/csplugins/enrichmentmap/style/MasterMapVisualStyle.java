package org.baderlab.csplugins.enrichmentmap.style;

import java.awt.Color;

import org.baderlab.csplugins.enrichmentmap.CytoscapeServiceModule.Continuous;
import org.baderlab.csplugins.enrichmentmap.CytoscapeServiceModule.Discrete;
import org.baderlab.csplugins.enrichmentmap.CytoscapeServiceModule.Passthrough;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;

import com.google.inject.Inject;

public class MasterMapVisualStyle {
	
	public final static String DEFAULT_NAME_SUFFIX = "MasterMap_Visual_Style"; // TEMPORARY probably won't be called 'MasterMap' in the final version
	public final static String COMBINED = "Combined";
	
	// Common attributes that apply to the entire network
	public static final ColumnDescriptor<String> NODE_GS_DESCR       = new ColumnDescriptor<>("GS_DESCR", String.class);
//	public static final ColumnDescriptor<String> NODE_GS_TYPE        = new ColumnDescriptor<>("GS_Type", String.class);
	public static final ColumnDescriptor<String> NODE_FORMATTED_NAME = new ColumnDescriptor<>("Formatted_name", String.class);
	// Take the union of all the genes across all the data-sets
	public static final ListColumnDescriptor<String> NODE_GENES      = new ListColumnDescriptor<>("Genes", String.class);
	public static final ColumnDescriptor<Integer> NODE_GS_SIZE       = new ColumnDescriptor<>("gs_size", Integer.class);
	
	// Per-DataSet attributes
	// GSEA attributes
	public static final ColumnDescriptor<Double>  NODE_PVALUE      = new ColumnDescriptor<>("pvalue", Double.class);
	public static final ColumnDescriptor<Double>  NODE_FDR_QVALUE  = new ColumnDescriptor<>("fdr_qvalue", Double.class);
	public static final ColumnDescriptor<Double>  NODE_FWER_QVALUE = new ColumnDescriptor<>("fwer_qvalue", Double.class);
	public static final ColumnDescriptor<Double>  NODE_ES          = new ColumnDescriptor<>("ES", Double.class);
	public static final ColumnDescriptor<Double>  NODE_NES         = new ColumnDescriptor<>("NES", Double.class);
	
	// Per-DataSet attributes
	// Edge attributes
	public static final ColumnDescriptor<Double>     EDGE_SIMILARITY_COEFF = new ColumnDescriptor<>("similarity_coefficient", Double.class);
	public static final ColumnDescriptor<Integer>    EDGE_OVERLAP_SIZE     = new ColumnDescriptor<>("Overlap_size", Integer.class);
	public static final ListColumnDescriptor<String> EDGE_OVERLAP_GENES    = new ListColumnDescriptor<>("Overlap_genes", String.class);
	
	
	@Inject private CyNetworkManager networkManager;
	
	@Inject private @Continuous  VisualMappingFunctionFactory vmfFactoryContinuous;
	@Inject private @Discrete    VisualMappingFunctionFactory vmfFactoryDiscrete;
	@Inject private @Passthrough VisualMappingFunctionFactory vmfFactoryPassthrough;
	
	
	// TEMPORARY MKTODO support color themes
	/* See http://colorbrewer2.org/#type=qualitative&scheme=Dark2&n=3 */
	private static final Color BG_COLOR = Color.WHITE;
	private static final Color EDGE_COLOR = new Color(27, 158, 119);
	private static final Color LIGHT_GREY = new Color(190, 190, 190);
	
//	@Inject private CyEventHelper eventHelper;
	
	
	public void applyVisualStyle(VisualStyle vs, MasterMapStyleOptions options) {
		// MKTODO silence events?
		
		// Network Properties
		vs.setDefaultValue(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT, BG_COLOR);    	        
    	vs.setDefaultValue(BasicVisualLexicon.EDGE_TRANSPARENCY, 100);
    	
		setEdgeDefaults(vs);
		setEdgeWidth(vs, options);
 		
		setNodeDefaults(vs);
		setNodeLabels(vs);
		setNodeSize(vs);
		
//		 
	}
	
	
//	private void setNodeColorUsingAveraging(VisualStyle vs, MasterMapStyleOptions options) {
//		long suid = options.getEnrichmentMap().getNetworkID();
//		CyNetwork network = networkManager.getNetwork(suid);
//		
//		Collection<DataSet> datasets = options.getDataSets();
//		
//		for(CyNode node : network.getNodeList()) {
//			CyRow row = network.getRow(node);
//			
//			double nesSum = 0.0;
//			double pvalueSum = 0.0;
//			
//			for(DataSet dataset : datasets) {
//				// MKTODO what if null?
//				Double nes = NODE_NES.get(row, dataset.getName());
//				nesSum += nes == null ? 0 : nes;
//				
//				Double pvalue = NODE_PVALUE.get(row, dataset.getName());
//				pvalueSum += pvalue == null ? 0 : pvalue;
//			}
//			
//			double pvalueAvg = pvalueSum / datasets.size();
//			
//			if(nesSum >= 0) {
//				NODE_COLORING.set(row, (1 - pvalueAvg));
//			} else {
//				NODE_COLORING.set(row, ((-1) * (1 - pvalueAvg)));
//			}
//		}
//	}


	private void setEdgeDefaults(VisualStyle vs) {
		vs.setDefaultValue(BasicVisualLexicon.EDGE_UNSELECTED_PAINT, EDGE_COLOR);
		vs.setDefaultValue(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT, EDGE_COLOR);
	}
	

	private void setEdgeWidth(VisualStyle vs, MasterMapStyleOptions options) {
		EnrichmentMap map = options.getEnrichmentMap();
		//Continous Mapping - set edge line thickness based on the number of genes in the overlap
		ContinuousMapping<Double, Double> edgewidth = (ContinuousMapping<Double, Double>) vmfFactoryContinuous
				.createVisualMappingFunction(EDGE_SIMILARITY_COEFF.getName(), Double.class, BasicVisualLexicon.EDGE_WIDTH);
		
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
	
	
	private void setNodeDefaults(VisualStyle vs) {
		//set the default node appearance
		vs.setDefaultValue(BasicVisualLexicon.NODE_FILL_COLOR, EnrichmentMapVisualStyle.MAX_PHENOTYPE_1);
		vs.setDefaultValue(BasicVisualLexicon.NODE_BORDER_PAINT, EnrichmentMapVisualStyle.MAX_PHENOTYPE_1);
		vs.setDefaultValue(BasicVisualLexicon.NODE_SHAPE, NodeShapeVisualProperty.ELLIPSE);
		vs.setDefaultValue(BasicVisualLexicon.NODE_SIZE, new Double(15.0));
		vs.setDefaultValue(BasicVisualLexicon.NODE_BORDER_WIDTH, new Double(15.0));
	}
	
	
	private void setNodeLabels(VisualStyle vs) {
		PassthroughMapping<String, String> nodeLabel = (PassthroughMapping<String, String>) vmfFactoryPassthrough
				.createVisualMappingFunction(NODE_GS_DESCR.getName(), String.class, BasicVisualLexicon.NODE_LABEL);
		vs.addVisualMappingFunction(nodeLabel);
	}
	
	
	private void setNodeSize(VisualStyle vs) {
		ContinuousMapping<Integer, Double> nodeSize = (ContinuousMapping<Integer, Double>) vmfFactoryContinuous
				.createVisualMappingFunction(NODE_GS_SIZE.getName(), Integer.class, BasicVisualLexicon.NODE_SIZE);

		Double min = 20.0;
		Double max = 65.0;

		BoundaryRangeValues<Double> bv0 = new BoundaryRangeValues<Double>(min, min, min);
		BoundaryRangeValues<Double> bv1 = new BoundaryRangeValues<Double>(max, max, max);
		nodeSize.addPoint(10, bv0);
		nodeSize.addPoint(474, bv1);

		vs.addVisualMappingFunction(nodeSize);
	}
	
	
}
