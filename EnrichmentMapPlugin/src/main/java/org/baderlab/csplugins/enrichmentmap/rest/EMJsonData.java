package org.baderlab.csplugins.enrichmentmap.rest;

import java.util.List;

import org.baderlab.csplugins.enrichmentmap.commands.tunables.FilterTunables;

public class EMJsonData {
	
	public FilterTunables filter;
	public List<EnrichmentEntry> enrichments;
	
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("EMJsonData [filter=");
		builder.append(filter);
		builder.append(", enrichments=");
		builder.append(enrichments);
		builder.append("]");
		return builder.toString();
	}
	

}
