package org.baderlab.csplugins.enrichmentmap.model;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class PhenotypeHighlight {
	
	public static enum Highlight {
		POSITIVE,
		NEGATIVE,
		NONE
	}
	
	private final LinkedHashMap<EMDataSet, Highlight> highlights;
	private final String name; // A bit redundant, ExpressionData has the same information
	
	
	public PhenotypeHighlight(EMDataSet dataset, String name, Highlight highlight) {
		this.highlights = new LinkedHashMap<>();
		this.highlights.put(Objects.requireNonNull(dataset), Objects.requireNonNull(highlight));
		this.name = Objects.requireNonNull(name);
	}
	
	public PhenotypeHighlight(List<EMDataSet> datasets, String name, Highlight highlight) {
		this.highlights = new LinkedHashMap<>();
		for(var ds : datasets) {
			this.highlights.put(ds, highlight);
		}
		this.name = Objects.requireNonNull(name);
	}
	
	private PhenotypeHighlight(LinkedHashMap<EMDataSet, Highlight> highlights, String name) {
		this.highlights = highlights;
		this.name = name;
	}
	
	public Set<EMDataSet> getDatasets() {
		return new LinkedHashSet<EMDataSet>(highlights.keySet());
	}

	public String getName() {
		return name;
	}

	public Highlight getHighlight(EMDataSet dataset) {
		return highlights.get(dataset);
	}
	
	
	public PhenotypeHighlight merge(PhenotypeHighlight that) {
		if(that == null)
			return this;
		
		if(name.equals(that.name)) {
			var highlights = new LinkedHashMap<EMDataSet, Highlight>();
			highlights.putAll(this.highlights);
			highlights.putAll(that.highlights);
			return new PhenotypeHighlight(highlights, name);
		}

		throw new IllegalArgumentException("Phenotype names do not match: " + name + " != " + that.name);
	}

	@Override
	public String toString() {
		return "PhenotypeHighlight[name=" + name + ", highlights=" + highlights + "]";
	}
	
	
}
