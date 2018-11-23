package org.baderlab.csplugins.enrichmentmap.model;

import org.baderlab.csplugins.enrichmentmap.style.ColumnDescriptor;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder;

public interface Columns {
	
	// GeneMANIA Attributes
	// NODE
	ColumnDescriptor<String> GM_GENE_NAME = new ColumnDescriptor<>("gene name", String.class);
	ColumnDescriptor<String> GM_QUERY_TERM = new ColumnDescriptor<>("query term", String.class);
	
	// STRING Attributes
	// NODE
	ColumnDescriptor<String> STR_GENE_NAME = new ColumnDescriptor<>("display name", String.class);
	ColumnDescriptor<String> STR_QUERY_TERM = new ColumnDescriptor<>("query term", String.class);
	
	// Hidden network attributes in associated networks
	// Hard code the namespace prefix because when we go from the associated network back to the EM network we don't know the EM network's prefix yet
	ColumnDescriptor<String> EM_ASSOCIATED_APP = new ColumnDescriptor<>(EMStyleBuilder.Columns.NAMESPACE_PREFIX + "Associated_App", String.class);
	ColumnDescriptor<Long> EM_NETWORK_SUID = new ColumnDescriptor<>(EMStyleBuilder.Columns.NAMESPACE_PREFIX + "Network.SUID", Long.class);
}