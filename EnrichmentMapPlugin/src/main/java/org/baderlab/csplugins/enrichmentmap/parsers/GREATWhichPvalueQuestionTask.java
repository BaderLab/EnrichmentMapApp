package org.baderlab.csplugins.enrichmentmap.parsers;

import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.GreatFilter;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

public class GREATWhichPvalueQuestionTask extends AbstractTask implements ObservableTask{
	//tunable representing the question
	@Tunable(description="<html>GREAT results can be filtered by Hypergeometric, <<BR>>Binomial, both[AND] or either[OR] tests.<BR> Which would you like to filter by?")
	public ListSingleSelection<String> filterResponse;
	
	private static final String hyper = "Hypergeometric p-value";
	private static final String binom = "Binomial p-value";
	private static final String both  = "Both";
	private static final String either = "Either";
	
	private final EMCreationParameters params;

	public GREATWhichPvalueQuestionTask(EnrichmentMap map) {
		filterResponse = new ListSingleSelection<>(hyper, binom, both, either);
		params = map.getParams();
	}
	
	@Override
	public void run(TaskMonitor tm) {
		var filter = getFilter(filterResponse.getSelectedValue());
		params.setGreatFilter(filter);
	}
	
	private static GreatFilter getFilter(String value) {
		switch(value) {
			case hyper:  return GreatFilter.HYPER;
			case binom:  return GreatFilter.BINOM;
			case both:   return GreatFilter.BOTH;
			case either: return GreatFilter.EITHER;
		}
		return null;
	}
	
	public <R> R getResults(Class<? extends R> arg) {
		return null;
	}
	
}
