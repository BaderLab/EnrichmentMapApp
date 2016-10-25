package org.baderlab.csplugins.enrichmentmap.style;

public class MasterMapVisualStyle {
	
	// Common attributes that apply to the entire network
	public static final ColumnDescriptor<String> NODE_GS_DESCR       = new ColumnDescriptor<>("GS_DESCR", String.class);
//	public static final ColumnDescriptor<String> NODE_GS_TYPE        = new ColumnDescriptor<>("GS_Type", String.class);
	public static final ColumnDescriptor<String> NODE_FORMATTED_NAME = new ColumnDescriptor<>("Formatted_name", String.class);
	public static final ListColumnDescriptor<String> NODE_GENES      = new ListColumnDescriptor<>("Genes", String.class);
	
	// Per-DataSet attributes
	// GSEA attributes
	public static final ColumnDescriptor<Double>  NODE_PVALUE      = new ColumnDescriptor<>("pvalue", Double.class);
	public static final ColumnDescriptor<Double>  NODE_FDR_QVALUE  = new ColumnDescriptor<>("fdr_qvalue", Double.class);
	public static final ColumnDescriptor<Integer> NODE_GS_SIZE     = new ColumnDescriptor<>("gs_size", Integer.class);
	public static final ColumnDescriptor<Double>  NODE_FWER_QVALUE = new ColumnDescriptor<>("fwer_qvalue", Double.class);
	public static final ColumnDescriptor<Double>  NODE_ES          = new ColumnDescriptor<>("ES", Double.class);
	public static final ColumnDescriptor<Double>  NODE_NES         = new ColumnDescriptor<>("NES", Double.class);
	
	// Per-DataSet attributes
	// Edge attributes
	public static final ColumnDescriptor<Double>     EDGE_SIMILARITY_COEFF = new ColumnDescriptor<>("similarity_coefficient", Double.class);
	public static final ColumnDescriptor<Integer>    EDGE_OVERLAP_SIZE     = new ColumnDescriptor<>("Overlap_size", Integer.class);
	public static final ListColumnDescriptor<String> EDGE_OVERLAP_GENES    = new ListColumnDescriptor<>("Overlap_genes", String.class);
	
}
