package org.baderlab.csplugins.enrichmentmap.mastermap.task;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import org.baderlab.csplugins.enrichmentmap.mastermap.model.GSEAResults;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class GSEAResultsDisplayTask extends AbstractTask {
	
	private Future<GSEAResults> gseaResults;
	
	public GSEAResultsDisplayTask(GSEAResults gseaResults) {
		this.gseaResults = CompletableFuture.completedFuture(gseaResults);
	}
	
	public GSEAResultsDisplayTask(Future<GSEAResults> gseaResults) {
		this.gseaResults = gseaResults;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		System.out.println("GSEAResultsDisplayTask " + gseaResults.get().getResultsFolders().size());
	}
}
