package org.baderlab.csplugins.enrichmentmap.view.postanalysis2;

import java.util.ArrayList;
import java.util.List;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.SetOfGeneSets;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;

public class SigGeneSetDescriptorTask extends AbstractTask implements ObservableTask {

	private final EnrichmentMap map;
	private final SetOfGeneSets setOfGeneSets;
	
	private List<SigGeneSetDescriptor> descriptors;
	
	public SigGeneSetDescriptorTask(EnrichmentMap map, SetOfGeneSets setOfGeneSets) {
		this.map = map;
		this.setOfGeneSets = setOfGeneSets;
	}

	@Override
	public void run(TaskMonitor taskMonitor) {
		descriptors = new ArrayList<>(setOfGeneSets.size());
		
		
	}

	@Override
	public <R> R getResults(Class<? extends R> type) {
		if(List.class.equals(type)) {
			return type.cast(descriptors);
		}
		return null;
	}
	
	

}
