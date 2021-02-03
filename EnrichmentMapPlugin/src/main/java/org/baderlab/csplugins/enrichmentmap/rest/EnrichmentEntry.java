package org.baderlab.csplugins.enrichmentmap.rest;

import java.util.List;

public class EnrichmentEntry {
	
	public String name;
	public List<String> genes;
	public Double pvalue;
	public Double qvalue;
	public Double nes;
	public String description;
	
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("EnrichmentEntry [name=");
		builder.append(name);
		builder.append(", genes=");
		builder.append(genes);
		builder.append(", pvalue=");
		builder.append(pvalue);
		builder.append(", qvalue=");
		builder.append(qvalue);
		builder.append(", nes=");
		builder.append(nes);
		builder.append(", description=");
		builder.append(description);
		builder.append("]");
		return builder.toString();
	}
}
