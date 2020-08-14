package org.baderlab.csplugins.enrichmentmap.model;

import java.util.Objects;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;

public class TableParameters {

	private final CyTable table;
	private final String nameColumn;
	private final String genesColumn;
	private final String pvalueColumn;
	private final String qvalueColumn;
	private final String nesColumn;
	private final String descriptionColumn;
	
	private final @Nullable Predicate<CyRow> filter;
	
	
	public TableParameters(
			CyTable table, 
			String nameColumn, 
			String genesColumn, 
			String pvalueColumn, 
			String qvalueColumn,
			String nesColumn,
			String descriptionColumn, 
			Predicate<CyRow> filter
	) {
		this.table = Objects.requireNonNull(table);
		this.nameColumn = Objects.requireNonNull(nameColumn);
		this.genesColumn = Objects.requireNonNull(genesColumn);
		this.pvalueColumn = pvalueColumn;
		this.qvalueColumn = qvalueColumn;
		this.nesColumn = nesColumn;
		this.descriptionColumn = descriptionColumn;
		this.filter = filter;
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
	
	public String getQvalueColumn() {
		return qvalueColumn;
	}
	
	public String getNesColumn() {
		return nesColumn;
	}

	public String getDescriptionColumn() {
		return descriptionColumn;
	}
	
	public Predicate<CyRow> getFilter() {
		return filter;
	}

}
