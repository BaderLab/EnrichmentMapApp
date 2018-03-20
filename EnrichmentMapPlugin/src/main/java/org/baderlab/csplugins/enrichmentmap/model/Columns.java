package org.baderlab.csplugins.enrichmentmap.model;

import org.baderlab.csplugins.enrichmentmap.style.ColumnDescriptor;

public interface Columns {
	
	// GeneMANIA Attributes
	// NODE
	ColumnDescriptor<String> GM_GENE_NAME = new ColumnDescriptor<>("gene name", String.class);
	ColumnDescriptor<String> GM_QUERY_TERM = new ColumnDescriptor<>("query term", String.class);
	
	// STRING Attributes
	// NODE
	ColumnDescriptor<String> STR_GENE_NAME = new ColumnDescriptor<>("display name", String.class);
	ColumnDescriptor<String> STR_QUERY_TERM = new ColumnDescriptor<>("query term", String.class);
}