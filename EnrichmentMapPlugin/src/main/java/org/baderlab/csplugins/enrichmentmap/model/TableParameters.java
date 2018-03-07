package org.baderlab.csplugins.enrichmentmap.model;

import java.util.Objects;
import java.util.Optional;

import org.cytoscape.model.CyTable;

public class TableParameters {

	private final CyTable table;
	private final String nameColumn;
	private final String genesColumn;
	private final String pvalueColumn;
	private final Optional<String> descriptionColumn;
	
	public TableParameters(CyTable table, String nameColumn, String genesColumn, String pvalueColumn, String descriptionColumn) {
		this.table = Objects.requireNonNull(table);
		this.nameColumn = Objects.requireNonNull(nameColumn);
		this.genesColumn = Objects.requireNonNull(genesColumn);
		this.pvalueColumn = Objects.requireNonNull(pvalueColumn);
		this.descriptionColumn = Optional.ofNullable(descriptionColumn);
	}

	public CyTable getTable() {
		return table;
	}

	public String getNameColumn() {
		return nameColumn;
	}

	public String getGenesColumn() {
		return genesColumn;
	}

	public String getPvalueColumn() {
		return pvalueColumn;
	}

	public Optional<String> getDescriptionColumn() {
		return descriptionColumn;
	}

}
