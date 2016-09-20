package org.baderlab.csplugins.enrichmentmap.mastermap.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResult;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class GSEAResultsFolder {
	
	private List<GeneSet> geneSets;
	private Map<String, EnrichmentResult> enrichments;
	
	
	private GSEAResultsFolder(Builder builder) {
		this.geneSets = ImmutableList.copyOf(builder.geneSets);
		this.enrichments = ImmutableMap.copyOf(builder.enrichments);
	}
	
	public List<GeneSet> getGeneSets() {
		return geneSets;
	}
	
	public Map<String, EnrichmentResult> getEnrichments() {
		return enrichments;
	}
	
	public static class Builder {
		private List<GeneSet> geneSets;
		private Map<String, EnrichmentResult> enrichments;
		
		public Builder setGeneSets(List<GeneSet> geneSets) {
			this.geneSets = geneSets;
			return this;
		}
		
		public Builder setEnrichments(Map<String, EnrichmentResult> enrichments) {
			this.enrichments = enrichments;
			return this;
		}
		
		public GSEAResultsFolder build() {
			Objects.requireNonNull(geneSets);
			Objects.requireNonNull(enrichments);
			return new GSEAResultsFolder(this);
		}
	}
	

}
