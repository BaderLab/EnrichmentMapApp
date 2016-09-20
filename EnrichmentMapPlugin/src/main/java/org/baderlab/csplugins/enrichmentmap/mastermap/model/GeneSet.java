package org.baderlab.csplugins.enrichmentmap.mastermap.model;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

public class GeneSet {

	private final String name;
	private final String description;
	private final Set<String> genes;
	
	public GeneSet(String name, String description, Set<String> genes) {
		this.name = name;
		this.description = description;
		this.genes = ImmutableSet.copyOf(genes);
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public Set<String> getGenes() {
		return genes;
	}
	
}
