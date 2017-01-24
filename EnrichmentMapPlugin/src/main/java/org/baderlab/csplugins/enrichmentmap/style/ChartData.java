package org.baderlab.csplugins.enrichmentmap.style;

import org.baderlab.csplugins.enrichmentmap.style.MasterMapVisualStyle.Columns;

public enum ChartData {
	NONE("-- None --", null),
	P_VALUE("P-value Columns", Columns.NODE_PVALUE),
	Q_VALUE("Q-value (FDR) Columns", Columns.NODE_FDR_QVALUE);
	
	private final String label;
	private final ColumnDescriptor<Double> columnDescriptor;

	private ChartData(String label, ColumnDescriptor<Double> columnDescriptor) {
		this.label = label;
		this.columnDescriptor = columnDescriptor;
	}
	
	public String getLabel() {
		return label;
	}
	
	public ColumnDescriptor<Double> getColumnDescriptor() {
		return columnDescriptor;
	}
	
	@Override
	public String toString() {
		return label;
	}
}
