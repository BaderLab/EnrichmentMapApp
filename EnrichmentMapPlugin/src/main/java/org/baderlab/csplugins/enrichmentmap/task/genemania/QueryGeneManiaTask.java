package org.baderlab.csplugins.enrichmentmap.task.genemania;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.baderlab.csplugins.enrichmentmap.task.tunables.GeneListTunable;
import org.baderlab.csplugins.enrichmentmap.util.TaskUtil;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;
import org.cytoscape.work.util.BoundedInteger;
import org.cytoscape.work.util.ListSingleSelection;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

public class QueryGeneManiaTask extends AbstractTask {
	
	public static final String GENEMANIA_NAMESPACE = "genemania";
	public static final String GENEMANIA_ORGANISMS_COMMAND = "organisms";
	public static final String GENEMANIA_SEARCH_COMMAND = "search";
	
	@Tunable(description = "Genes:")
	public GeneListTunable geneList;
	
	@Tunable(description = "Organism:")
	public ListSingleSelection<GMOrganism> organisms;
	
	@Tunable(description = "Max Resultant Genes:", params = "slider=true")
	public BoundedInteger limit = new BoundedInteger(0, 0, 100, false, false);
	
	@Tunable(description = "Network Weighting:")
	public ListSingleSelection<GMWeightingMethod> weightingMethods;
	
	// not a tunable, used just for the UI
	public List<String> selectedGenes;
	
	private GMSearchResult result;
	
	private static long lastTaxonomyId = 9606; // H.sapiens

	@Inject private CommandExecutorTaskFactory commandExecutorTaskFactory;
	@Inject private SynchronousTaskManager<?> syncTaskManager;
	
	public static interface Factory {
		QueryGeneManiaTask create(GeneListTunable geneListTaskParams);
	}
	
	@AssistedInject
	public QueryGeneManiaTask(@Assisted GeneListTunable geneListTaskParams) {
		this.geneList = geneListTaskParams;
		this.organisms = new ListSingleSelection<>();
		this.weightingMethods = new ListSingleSelection<>(GMWeightingMethod.values());
		this.weightingMethods.setSelectedValue(weightingMethods.getPossibleValues().get(0));
	}
	
	@ProvidesTitle
	public String getTitle() {
		return "GeneMANIA Query";
	}
	
	public void updatetOrganisms(List<GMOrganism> orgValues) {
		organisms.setPossibleValues(orgValues);
		
		if (!orgValues.isEmpty()) {
			organisms.setSelectedValue(orgValues.get(0));
			
			for (GMOrganism org : orgValues) {
				if (org.getTaxonomyId() == lastTaxonomyId) {
					organisms.setSelectedValue(org);
					break;
				}
			}
		}
	}
	
	@Override
	public void run(TaskMonitor tm) throws Exception {
		if (organisms.getSelectedValue() != null) {
			tm.setTitle("EnrichmentMap");
			tm.setStatusMessage("Querying GeneMANIA...");
			
			String query = String.join("|", geneList.getSelectedGenes());
			
			Map<String, Object> args = new HashMap<>();
			args.put("organism", "" + organisms.getSelectedValue().getTaxonomyId());
			args.put("genes", query);
			args.put("geneLimit", limit.getValue());
			args.put("combiningMethod", weightingMethods.getSelectedValue().name());
			
			TaskIterator ti = commandExecutorTaskFactory.createTaskIterator(
					GENEMANIA_NAMESPACE, GENEMANIA_SEARCH_COMMAND, args, TaskUtil.taskFinished(task -> {
				
						if (task.getResultClasses().contains(JSONResult.class)) {
							JSONResult json = task.getResults(JSONResult.class);
							
							if (json != null && json.getJSON() != null) {
								Gson gson = new Gson();
								@SuppressWarnings("serial")
								Type type = new TypeToken<GMSearchResult>(){}.getType();
								result = gson.fromJson(json.getJSON(), type);
							} else {
								throw new RuntimeException("Unexpected error when getting search results from GeneMANIA.");
							}
						}
			}));
			
			
			// run the task right here so we can handle errors
			boolean[] timedOut = { false };
			Exception[] cause = { null };
			
			syncTaskManager.execute(ti, TaskUtil.onFail(finishStatus -> {
						Exception ex = finishStatus.getException();
						if(ex != null && ex.getMessage() != null && ex.getMessage().contains("timeout")) {
							timedOut[0] = true;
							cause[0] = ex;
						}
			}));
			
			if(timedOut[0]) {
				throw new RuntimeException("GeneMANIA query timed out. Please try again with fewer genes.", cause[0]);
			}
			
			// Save this as the default organism for next time
			lastTaxonomyId = organisms.getSelectedValue().getTaxonomyId();
		}
	}

	public GMSearchResult getResult() {
		return result;
	}
}
