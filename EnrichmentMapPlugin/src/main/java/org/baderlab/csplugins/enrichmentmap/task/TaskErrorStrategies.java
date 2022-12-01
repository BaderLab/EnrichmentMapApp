package org.baderlab.csplugins.enrichmentmap.task;

import java.util.Objects;

import org.baderlab.csplugins.enrichmentmap.parsers.ParseGSEAEnrichmentResults.ParseGSEAEnrichmentStrategy;
import org.baderlab.csplugins.enrichmentmap.parsers.RanksFileReaderTask.UnsortedRanksStrategy;
import org.baderlab.csplugins.enrichmentmap.task.InitializeGenesetsOfInterestTask.MissingGenesetStrategy;

public class TaskErrorStrategies {

	private final MissingGenesetStrategy genesetStrategy;
	private final ParseGSEAEnrichmentStrategy gseaStrategy;
	private final UnsortedRanksStrategy ranksStrategy;
	
	
	public TaskErrorStrategies(MissingGenesetStrategy genesetStrategy, ParseGSEAEnrichmentStrategy gseaStrategy, UnsortedRanksStrategy ranksStrategy) {
		this.genesetStrategy = Objects.requireNonNull(genesetStrategy);
		this.gseaStrategy = Objects.requireNonNull(gseaStrategy);
		this.ranksStrategy = Objects.requireNonNull(ranksStrategy);
	}
	
	
	public static TaskErrorStrategies dialogDefaults() {
		return new TaskErrorStrategies(
				MissingGenesetStrategy.FAIL_AT_END, 
				ParseGSEAEnrichmentStrategy.FAIL_IMMEDIATELY, 
				UnsortedRanksStrategy.FAIL_IMMEDIATELY);
	}
	
	public static TaskErrorStrategies commandDefaults() {
		return new TaskErrorStrategies(
				MissingGenesetStrategy.FAIL_AT_END, 
				ParseGSEAEnrichmentStrategy.FAIL_IMMEDIATELY, 
				UnsortedRanksStrategy.LOG_WARNING);
	}
	
	public static TaskErrorStrategies testDefaults() {
		return new TaskErrorStrategies(
				MissingGenesetStrategy.FAIL_IMMEDIATELY, 
				ParseGSEAEnrichmentStrategy.FAIL_IMMEDIATELY, 
				UnsortedRanksStrategy.FAIL_IMMEDIATELY);
	}
	

	public MissingGenesetStrategy getGenesetStrategy() {
		return genesetStrategy;
	}

	public ParseGSEAEnrichmentStrategy getGseaStrategy() {
		return gseaStrategy;
	}

	public UnsortedRanksStrategy getRanksStrategy() {
		return ranksStrategy;
	}
	
	
	public TaskErrorStrategies with(MissingGenesetStrategy genesetStrategy) {
		return new TaskErrorStrategies(genesetStrategy, gseaStrategy, ranksStrategy);
	}
	
	public TaskErrorStrategies with(ParseGSEAEnrichmentStrategy gseaStrategy) {
		return new TaskErrorStrategies(genesetStrategy, gseaStrategy, ranksStrategy);
	}
	
	public TaskErrorStrategies with(UnsortedRanksStrategy ranksStrategy) {
		return new TaskErrorStrategies(genesetStrategy, gseaStrategy, ranksStrategy);
	}
	
}
