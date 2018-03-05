package org.baderlab.csplugins.enrichmentmap.model;

import javax.annotation.Nullable;

import org.cytoscape.model.CyTable;

public class TableParameters {

	public CyTable table;
	
	public String nameColumn;
	public String genesColumn;
	public String pvalueColumn;
	public @Nullable String descriptionColumn;
	
	public TableParameters() {
	}

}
