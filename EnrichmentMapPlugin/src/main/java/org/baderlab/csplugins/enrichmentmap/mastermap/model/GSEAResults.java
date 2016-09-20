package org.baderlab.csplugins.enrichmentmap.mastermap.model;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

public class GSEAResults {

	private final List<GSEAResultsFolder> resultsFolders;
	
	private GSEAResults(Builder builder) {
		this.resultsFolders = ImmutableList.copyOf(builder.resultsFolders);
	}
	
	public static class Builder {
		
		private final List<GSEAResultsFolder> resultsFolders = new ArrayList<>();
		
		public Builder addResultsFolder(GSEAResultsFolder folder) {
			resultsFolders.add(folder);
			return this;
		}
		
		public GSEAResults build() {
			return new GSEAResults(this);
		}
		
	}
	
	public List<GSEAResultsFolder> getResultsFolders() {
		return resultsFolders;
	}
}
