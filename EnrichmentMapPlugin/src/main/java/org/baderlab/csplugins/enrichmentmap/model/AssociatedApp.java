package org.baderlab.csplugins.enrichmentmap.model;

import org.baderlab.csplugins.enrichmentmap.style.ColumnDescriptor;

public enum AssociatedApp {
	GENEMANIA("GeneMANIA", Columns.GM_GENE_NAME, Columns.GM_QUERY_TERM),
	STRING("STRING", Columns.STR_GENE_NAME, Columns.STR_QUERY_TERM);
	
	private final String name;
	private final ColumnDescriptor<String> geneNameColumn;
	private final ColumnDescriptor<String> queryTermColumn;

	private AssociatedApp(String name, ColumnDescriptor<String> geneNameColumn,
			ColumnDescriptor<String> queryTermColumn) {
		this.name = name;
		this.geneNameColumn = geneNameColumn;
		this.queryTermColumn = queryTermColumn;
	}
	
	public ColumnDescriptor<String> getGeneNameColumn() {
		return geneNameColumn;
	}
	
	public ColumnDescriptor<String> getQueryTermColumn() {
		return queryTermColumn;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
