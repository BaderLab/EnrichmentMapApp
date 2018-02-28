package org.baderlab.csplugins.enrichmentmap.task.genemania;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;
import org.cytoscape.work.util.ListSingleSelection;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class QueryGeneManiaTask extends AbstractTask {
	
	public static final String GENEMANIA_NAMESPACE = "genemania";
	public static final String GENEMANIA_ORGANISMS_COMMAND = "organisms";
	public static final String GENEMANIA_SEARCH_COMMAND = "search";
	
	@Tunable(description = "Organism:")
	public ListSingleSelection<GMOrganism> organisms;
	
	private final String query;
	private GMSearchResult result;
	
	private static long lastTaxonomyId = 9606; // H.sapiens

	@Inject private CommandExecutorTaskFactory commandExecutorTaskFactory;
	
	public static interface Factory {
		QueryGeneManiaTask create(List<String> geneList);
	}
	
	@Inject
	public QueryGeneManiaTask(@Assisted List<String> geneList) {
		query = String.join("|", geneList);
		organisms = new ListSingleSelection<>();
	}
	
	@ProvidesTitle
	public String getTitle() {
		return "Select an Organism";
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
			
			Map<String, Object> args = new HashMap<>();
			args.put("organism", "" + organisms.getSelectedValue().getTaxonomyId());
			args.put("genes", query);
			args.put("geneLimit", 0); // Do not find more genes
			
			TaskIterator ti = commandExecutorTaskFactory.createTaskIterator(
					GENEMANIA_NAMESPACE, GENEMANIA_SEARCH_COMMAND, args, new TaskObserver() {
				
				@Override
				@SuppressWarnings("serial")
				public void taskFinished(ObservableTask task) {
					if (task instanceof ObservableTask) {
						if (((ObservableTask) task).getResultClasses().contains(JSONResult.class)) {
							JSONResult json = ((ObservableTask) task).getResults(JSONResult.class);
							
							if (json != null && json.getJSON() != null) {
								Gson gson = new Gson();
								Type type = new TypeToken<GMSearchResult>(){}.getType();
								result = gson.fromJson(json.getJSON(), type);
							} else {
								throw new RuntimeException("Unexpected error when getting search results from GeneMANIA.");
							}
						}
					}
				}
				
				@Override
				public void allFinished(FinishStatus finishStatus) {
					// Never called by Cytoscape...
				}
			});
			getTaskIterator().append(ti);
			
			// Save this as the default organism for next time
			lastTaxonomyId = organisms.getSelectedValue().getTaxonomyId();
		}
	}

	public GMSearchResult getResult() {
		return result;
	}
}
