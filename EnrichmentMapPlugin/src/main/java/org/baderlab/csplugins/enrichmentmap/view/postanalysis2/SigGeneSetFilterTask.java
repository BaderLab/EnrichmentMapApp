package org.baderlab.csplugins.enrichmentmap.view.postanalysis2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.PostAnalysisFilterType;
import org.baderlab.csplugins.enrichmentmap.task.postanalysis.FilterMetric;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;

import com.google.common.collect.Sets;


public class SigGeneSetFilterTask extends AbstractTask implements ObservableTask {

	private final EnrichmentMap map;
	private final List<SigGeneSetDescriptor> geneSets;
	private final FilterMetric filterMetric;
	
	private List<SigGeneSetDescriptor> resultGeneSets;
	
	
	public SigGeneSetFilterTask(EnrichmentMap map, List<SigGeneSetDescriptor> geneSets, FilterMetric filterMetric) {
		this.map = map;
		this.geneSets = geneSets;
		this.filterMetric = filterMetric;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Filtering Signature Gene sets");
		filterSignatureGS(taskMonitor);
	}

	
	private void filterSignatureGS(TaskMonitor taskMonitor) {
		resultGeneSets = new ArrayList<>();

		Map<String,Set<Integer>> allGenesets = map.unionAllGeneSetsOfInterest();

		for(SigGeneSetDescriptor descriptor : geneSets) {
			if(filterMetric.getFilterType() == PostAnalysisFilterType.NO_FILTER) {
				resultGeneSets.add(descriptor);
			} else {
				for(String mapGeneset : allGenesets.keySet()) {
					//check if this set overlaps with current gene set
					
					Set<Integer> geneSetGenes = allGenesets.get(mapGeneset);
					Set<Integer> sigSetGenes  = descriptor.getGeneSet().getGenes();
					
					// MKTODO why can't the filterMetric do the intersection itself?
					Set<Integer> intersection = Sets.intersection(geneSetGenes, sigSetGenes);
					
					if(filterMetric.match(geneSetGenes.size(), intersection, sigSetGenes)) {
						resultGeneSets.add(descriptor);
						break;
					}
				}
			} 
		}
	}
	
	
	/**
	 * type Set.class for filtered gene set names
	 */
	@Override
	public <R> R getResults(Class<? extends R> type) {
		if(List.class.equals(type)) {
			return type.cast(resultGeneSets);
		}
		return null;
	}

}
