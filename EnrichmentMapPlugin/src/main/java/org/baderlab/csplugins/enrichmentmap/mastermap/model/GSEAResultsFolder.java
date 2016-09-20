package org.baderlab.csplugins.enrichmentmap.mastermap.model;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

public class GSEAResultsFolder {
	
	private List<GeneSet> geneSets;
	
	private GSEAResultsFolder(Builder builder) {
		this.geneSets = ImmutableList.copyOf(builder.geneSets);
	}
	
	public List<GeneSet> getGeneSets() {
		return geneSets;
	}
	
	public static class Builder {
		private List<GeneSet> geneSets = new ArrayList<>();
		
		public Builder setGeneSets(List<GeneSet> geneSets) {
			this.geneSets = geneSets;
			return this;
		}
		
		public GSEAResultsFolder build() {
			return new GSEAResultsFolder(this);
		}
	}
	

}
