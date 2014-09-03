package org.baderlab.csplugins.enrichmentmap.parsers;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.cytoscape.io.util.StreamUtil;
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
	private static String both = "Both";
	private static String either = "Either";
	
	private EnrichmentMapParameters params;

	public GREATWhichPvalueQuestionTask(EnrichmentMap map) {
		filterResponse = new ListSingleSelection<String>( hyper,binom,both, either);
		this.params = map.getParams();
	}
	
	@Override
	public void run(TaskMonitor arg0) throws Exception {
		if(filterResponse.getSelectedValue().equals(hyper))
			params.setGreat_Filter(EnrichmentMapParameters.GREAT_hyper);
		if(filterResponse.getSelectedValue().equals(binom))
			params.setGreat_Filter(EnrichmentMapParameters.GREAT_binom);
		if(filterResponse.getSelectedValue().equals(both))
			params.setGreat_Filter(EnrichmentMapParameters.GREAT_both);
		if(filterResponse.getSelectedValue().equals(either))
			params.setGreat_Filter(EnrichmentMapParameters.GREAT_either);
				
		
		
			
	}
	
	public void run() throws Exception {
		if(filterResponse.getSelectedValue().equals(hyper))
			params.setGreat_Filter(EnrichmentMapParameters.GREAT_hyper);
		if(filterResponse.getSelectedValue().equals(binom))
			params.setGreat_Filter(EnrichmentMapParameters.GREAT_binom);
		if(filterResponse.getSelectedValue().equals(both))
			params.setGreat_Filter(EnrichmentMapParameters.GREAT_both);
		if(filterResponse.getSelectedValue().equals(either))
			params.setGreat_Filter(EnrichmentMapParameters.GREAT_either);
			
									
	}

	public <R> R getResults(Class<? extends R> arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
