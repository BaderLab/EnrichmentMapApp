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
	public  ListSingleSelection<String> filterResponse;
	
	private static String hyper = "Hypergeometric p-value";
	private static String binom = "Binomial p-value";
	private static String both  = "Both";
	private static String either = "Either";
	
	private EMCreationParameters params;

	public GREATWhichPvalueQuestionTask(EnrichmentMap map) {
		filterResponse = new ListSingleSelection<String>(hyper, binom, both, either);
		params = map.getParams();
	}
	
	@Override
	public void run(TaskMonitor arg0) throws Exception {
		if(filterResponse.getSelectedValue().equals(hyper))
			params.setGreatFilter(GreatFilter.HYPER);
		if(filterResponse.getSelectedValue().equals(binom))
			params.setGreatFilter(GreatFilter.BINOM);
		if(filterResponse.getSelectedValue().equals(both))
			params.setGreatFilter(GreatFilter.BOTH);
		if(filterResponse.getSelectedValue().equals(either))
			params.setGreatFilter(GreatFilter.EITHER);
	}
	
	public void run() throws Exception {
		if(filterResponse.getSelectedValue().equals(hyper))
			params.setGreatFilter(GreatFilter.HYPER);
		if(filterResponse.getSelectedValue().equals(binom))
			params.setGreatFilter(GreatFilter.BINOM);
		if(filterResponse.getSelectedValue().equals(both))
			params.setGreatFilter(GreatFilter.BOTH);
		if(filterResponse.getSelectedValue().equals(either))
			params.setGreatFilter(GreatFilter.EITHER);
	}

	public <R> R getResults(Class<? extends R> arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
