package org.baderlab.csplugins.enrichmentmap.task;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class ComputeSimilarityTaskFactory implements TaskFactory {
	private TaskIterator computeSimilarity;
	private EnrichmentMap em;
	
	public ComputeSimilarityTaskFactory(EnrichmentMap em){
		this.em = em;
	}
	
	public void intialize(){
		 ComputeSimilarityTask similarities = new ComputeSimilarityTask(em);
		this.computeSimilarity.append(similarities);
	}
	
	
	public TaskIterator createTaskIterator() {
		TaskIterator computeSimilarity = new TaskIterator();
		intialize();
		return computeSimilarity;
	}

	public boolean isReady() {
		// TODO Auto-generated method stub
		return false;
	}
}
