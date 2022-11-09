package org.baderlab.csplugins.enrichmentmap.model;

import org.baderlab.csplugins.enrichmentmap.style.ColumnDescriptor;
import org.baderlab.csplugins.enrichmentmap.style.ColumnListDescriptor;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder;

public enum AssociatedApp {
	
	GENEMANIA("GeneMANIA", AssociatedAppColumns.GM_GENE_NAME, AssociatedAppColumns.GM_QUERY_TERM),
	STRING("STRING", AssociatedAppColumns.STR_GENE_NAME, AssociatedAppColumns.STR_QUERY_TERM),
	AUTOANNOTATE("AUTOANNOTATE", EMStyleBuilder.Columns.NODE_GENES);
	
	
	private final String name;
	private final ColumnDescriptor<String> geneNameColumn;
	private final ColumnListDescriptor<String> geneNameListColumn;
	private final ColumnDescriptor<String> queryTermColumn;

	
	private AssociatedApp(String name, ColumnDescriptor<String> geneNameColumn, ColumnDescriptor<String> queryTermColumn) {
		this.name = name;
		this.geneNameColumn = geneNameColumn;
		this.geneNameListColumn = null;
		this.queryTermColumn = queryTermColumn;
	}
	
	private AssociatedApp(String name, ColumnListDescriptor<String> geneNameListColumn) {
		this.name = name;
		this.geneNameColumn = null;
		this.geneNameListColumn = geneNameListColumn;
		this.queryTermColumn = null;
	}
	
	
	public ColumnDescriptor<String> getGeneNameColumn() {
		return geneNameColumn;
	}
	
	public ColumnDescriptor<String> getQueryTermColumn() {
		return queryTermColumn;
	}
	
	public ColumnListDescriptor<String> getGeneNameListColumn() {
		return geneNameListColumn;
	}
	
	
	@Override
	public String toString() {
		return name;
	}
}
