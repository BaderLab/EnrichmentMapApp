package org.baderlab.csplugins.enrichmentmap.task;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class InitializeGenesetsOfInterestTaskFactory implements TaskFactory {
	
	private TaskIterator initializeGenesets;
	private EnrichmentMap em;
	
	public InitializeGenesetsOfInterestTaskFactory(EnrichmentMap em){
		this.em = em;
	}
	
	public void intialize(){
		InitializeGenesetsOfInterestTask genesets_init = new InitializeGenesetsOfInterestTask(em);
		this.initializeGenesets.append(genesets_init);
	}
	
	
	public TaskIterator createTaskIterator() {
		TaskIterator initializeGenesets = new TaskIterator();
		intialize();
		return initializeGenesets;
	}

	public boolean isReady() {
		// TODO Auto-generated method stub
		return false;
	}

}
