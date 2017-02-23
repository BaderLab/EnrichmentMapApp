package org.baderlab.csplugins.enrichmentmap.task;

import java.util.Iterator;
import java.util.Map;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet.Method;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResult;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.baderlab.csplugins.enrichmentmap.model.GenericResult;
import org.baderlab.csplugins.enrichmentmap.model.SetOfEnrichmentResults;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

/**
 * Created by IntelliJ IDEA. User: risserlin Date: 11-02-16 Time: 4:27 PM To
 * change this template use File | Settings | File Templates.
 */
public class CreateGMTEnrichmentMapTask extends AbstractTask {

	// Keep track of progress for monitoring:
	private TaskMonitor taskMonitor = null;
	private boolean interrupted = false;

	private EMDataSet dataset;

	public CreateGMTEnrichmentMapTask(EMDataSet dataset) {
		this.dataset = dataset;
	}

	public void buildEnrichmentMap() {
		dataset.setMethod(Method.Generic);
		// in this case all the genesets are of interest
		dataset.setGeneSetsOfInterest(dataset.getSetOfGeneSets());

		Map<String, GeneSet> currentSets = dataset.getSetOfGeneSets().getGeneSets();

		// create an new Set of Enrichment Results                
		SetOfEnrichmentResults setOfEnrichments = new SetOfEnrichmentResults();
		Map<String, EnrichmentResult> currentEnrichments = setOfEnrichments.getEnrichments();

		// need also to put all genesets into enrichment results
		for (Iterator<String> i = currentSets.keySet().iterator(); i.hasNext();) {
			String geneset1Name = i.next();
			GeneSet gs = currentSets.get(geneset1Name);
			GenericResult tempResult = new GenericResult(gs.getName(), gs.getDescription(), 0.01, gs.getGenes().size());
			currentEnrichments.put(gs.getName(), tempResult);
		}
		
		dataset.setEnrichments(setOfEnrichments);
	}

	/**
	 * Non-blocking call to interrupt the task.
	 */
	public void halt() {
		this.interrupted = true;
	}

	/**
	 * Sets the Task Monitor.
	 *
	 * @param taskMonitor TaskMonitor Object.
	 */
	public void setTaskMonitor(TaskMonitor taskMonitor) {
		if(this.taskMonitor != null) {
			throw new IllegalStateException("Task Monitor is already set.");
		}
		this.taskMonitor = taskMonitor;
	}

	/**
	 * Gets the Task Title.
	 *
	 * @return human readable task title.
	 */
	public String getTitle() {
		return new String("Computing geneset similarities");
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		this.taskMonitor = taskMonitor;
		this.taskMonitor.setTitle("Computing geneset similarities");

		buildEnrichmentMap();

	}

}
