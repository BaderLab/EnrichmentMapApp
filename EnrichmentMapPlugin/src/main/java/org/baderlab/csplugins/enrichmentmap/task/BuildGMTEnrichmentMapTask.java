package org.baderlab.csplugins.enrichmentmap.task;

import java.util.Iterator;
import java.util.Map;

import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.DataSet.Method;
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
public class BuildGMTEnrichmentMapTask extends AbstractTask {

	// Keep track of progress for monitoring:
	private TaskMonitor taskMonitor = null;
	private boolean interrupted = false;

	private DataSet dataset;

	public BuildGMTEnrichmentMapTask(DataSet dataset) {

		this.dataset = dataset;

	}

	public void buildEnrichmentMap() {
		this.dataset.setMethod(Method.Generic);

		//in this case all the genesets are of interest
		this.dataset.setGenesetsOfInterest(this.dataset.getSetofgenesets());

		Map<String, GeneSet> current_sets = this.dataset.getSetofgenesets().getGenesets();

		//create an new Set of Enrichment Results                
		SetOfEnrichmentResults setofenrichments = new SetOfEnrichmentResults();

		Map<String, EnrichmentResult> currentEnrichments = setofenrichments.getEnrichments();

		//need also to put all genesets into enrichment results
		for(Iterator i = current_sets.keySet().iterator(); i.hasNext();) {
			String geneset1_name = i.next().toString();
			GeneSet current = (GeneSet) current_sets.get(geneset1_name);
			GenericResult temp_result = new GenericResult(current.getName(), current.getDescription(), 0.01, current.getGenes().size());
			currentEnrichments.put(current.getName(), temp_result);
		}
		this.dataset.setEnrichments(setofenrichments);

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
