package org.baderlab.csplugins.enrichmentmap.style;

import org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder.Columns;
import org.baderlab.csplugins.enrichmentmap.view.util.Labels;

public enum ChartData {
	
	NONE(Labels.NONE, null),
	NES_VALUE("NES Columns", Columns.NODE_NES),
	NES_SIG("NES Columns (significant)", Columns.NODE_NES),
	P_VALUE("P-value Columns", Columns.NODE_PVALUE),
	FDR_VALUE("Q-value (FDR) Columns", Columns.NODE_FDR_QVALUE),
	PHENOTYPES("Phenotypes", Columns.NODE_COLOURING),
	DATA_SET("Color by Data Set", Columns.DATASET_CHART),
	EXPRESSION_DATA("Color by Expression Data", Columns.EXPRESSION_DATA_CHART),
	MAX_NEG_LOG10_PVAL("-log10(pvalue)", Columns.NODE_MAX_LOG_PVALUE);
	
	private final String label;
	private final AbstractColumnDescriptor columnDescriptor;

	private ChartData(String label, AbstractColumnDescriptor columnDescriptor) {
		this.label = label;
		this.columnDescriptor = columnDescriptor;
	}
	
	public String getLabel() {
		return label;
	}
	
	public AbstractColumnDescriptor getColumnDescriptor() {
		return columnDescriptor;
	}
	
	public boolean isColumnPerDataset() {
		return this != MAX_NEG_LOG10_PVAL;
	}
	
	public boolean isChartTypeSelectable() {
		return this != NONE && this != DATA_SET && this != EXPRESSION_DATA;
	}
	
	@Override
	public String toString() {
		return label;
	}
}
