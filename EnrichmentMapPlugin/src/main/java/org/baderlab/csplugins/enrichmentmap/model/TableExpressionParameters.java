package org.baderlab.csplugins.enrichmentmap.model;

import java.util.Objects;

import org.cytoscape.model.CyTable;

public class TableExpressionParameters {

	// expression table
	private final CyTable exprTable;
	private final String exprGeneNameColumn;
	private final String exprDescriptionColumn;
	private final String[] exprValueColumns;
	
	
	public TableExpressionParameters(CyTable exprTable, String exprGeneNameColumn, String exprDescriptionColumn, String[] exprValueColumns) {
		this.exprTable = Objects.requireNonNull(exprTable);
		this.exprGeneNameColumn = exprGeneNameColumn;
		this.exprDescriptionColumn = exprDescriptionColumn;
		this.exprValueColumns = exprValueColumns;
	}
	
	public CyTable getExprTable() {
		return exprTable;
	}

	public String getExprGeneNameColumn() {
		return exprGeneNameColumn;
	}

	public String getExprDescriptionColumn() {
		return exprDescriptionColumn;
	}

	public String[] getExprValueColumns() {
		return exprValueColumns;
	}

}
