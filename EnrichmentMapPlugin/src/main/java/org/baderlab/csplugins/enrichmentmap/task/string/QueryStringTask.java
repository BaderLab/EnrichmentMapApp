package org.baderlab.csplugins.enrichmentmap.task.string;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;
import org.cytoscape.work.util.BoundedDouble;
import org.cytoscape.work.util.BoundedInteger;
import org.cytoscape.work.util.ListSingleSelection;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class QueryStringTask extends AbstractTask {
	
	public static final String STRING_NAMESPACE = "string";
	public static final String STRING_SPECIES_COMMAND = "list species";
	public static final String STRING_SEARCH_COMMAND = "protein query";
	
	@Tunable(description = "Species:")
	public ListSingleSelection<STRSpecies> organisms;
	
	@Tunable(
			description = "Confidence Cutoff:",
			longDescription =
					"The confidence score reflects the cumulated evidence that this "
					+ "interaction exists.  Only interactions with scores greater than "
					+ "this cutoff will be returned.",
			params = "slider=true",
			format = "0.00"
	)
	public BoundedDouble cutoff = new BoundedDouble(0.0, 0.4, 1.0, false, false);
	
	@Tunable(
			description = "Max Additional Interactors:",
			longDescription = "The maximum number of proteins to return in addition to the query set.",
			params = "slider=true"
	)
	public BoundedInteger limit = new BoundedInteger(0, 0, 100, false, false);
	
	private final String query;
	
	private Long result;

	@Inject private CommandExecutorTaskFactory commandExecutorTaskFactory;
	
	public static interface Factory {
		QueryStringTask create(List<String> geneList);
	}
	
	@Inject
	public QueryStringTask(@Assisted List<String> geneList) {
		query = String.join(",", geneList);
		organisms = new ListSingleSelection<>();
	}
	
	@ProvidesTitle
	public String getTitle() {
		return "Select an Organism";
	}
	
	public void updatetOrganisms(List<STRSpecies> orgValues) {
		organisms.setPossibleValues(orgValues);
		
		if (!orgValues.isEmpty()) {
			for (STRSpecies org : orgValues) {
				if (org.getTaxonomyId() == 9606) { // H.sapiens
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
			tm.setStatusMessage("Querying STRING...");
			
			Map<String, Object> args = new HashMap<>();
			args.put("taxonID", "" + organisms.getSelectedValue().getTaxonomyId());
			args.put("query", query);
			args.put("cutoff", "" + cutoff.getValue());
			args.put("limit", "" + limit.getValue());
			
			TaskIterator ti = commandExecutorTaskFactory.createTaskIterator(
					STRING_NAMESPACE, STRING_SEARCH_COMMAND, args, new TaskObserver() {
				
				@Override
				public void taskFinished(ObservableTask task) {
					if (task instanceof ObservableTask) {
						if (((ObservableTask) task).getResultClasses().contains(JSONResult.class)) {
							JSONResult json = ((ObservableTask) task).getResults(JSONResult.class);
							
							if (json != null && json.getJSON() != null) {
								Gson gson = new Gson();
								Map<?, ?> map = gson.fromJson(json.getJSON(), Map.class);
								Number suid = (Number) map.get("SUID");
								result = suid != null ? suid.longValue() : null;
							} else {
								throw new RuntimeException("Unexpected error when getting search results from STRING.");
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
		}
	}

	/**
	 * @return The SUID of the created {@link CyNetwork}
	 */
	public Long getResult() {
		return result;
	}
}
