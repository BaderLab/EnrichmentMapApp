package org.baderlab.csplugins.enrichmentmap.task;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.FilterType;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.baderlab.csplugins.enrichmentmap.model.SetOfGeneSets;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskMonitor.Level;

public class FilterSignatureGSTask extends AbstractTask implements ObservableTask {

	private final EnrichmentMap map;
	private final SetOfGeneSets signatureGenesets;
	private final FilterMetric filterMetric;
	
	private Set<String> resultSignatureSetNames;
	
	
	public FilterSignatureGSTask(EnrichmentMap map, SetOfGeneSets signatureGenesets, FilterMetric filterMetric) {
		this.map = map;
		this.signatureGenesets = signatureGenesets;
		this.filterMetric = filterMetric;
	}
	
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("filtering Signature Gene set file");
		filterSignatureGS(taskMonitor);
	}

	
	private void filterSignatureGS(TaskMonitor taskMonitor) {
		resultSignatureSetNames = new HashSet<>();

		//filter the signature genesets to only include genesets that overlap with the genesets in our current map.

		// Use the same genesets that are saved to the session file (bug #66)
		// HashMap<String, GeneSet> genesets_in_map = map.getAllGenesets();
		HashMap<String, GeneSet> genesets_in_map = map.getAllGenesetsOfInterest();

		String[] setNamesArray = signatureGenesets.getGenesets().keySet().toArray(new String[0]);
		Arrays.sort(setNamesArray);

		if(taskMonitor != null)
			taskMonitor.setStatusMessage("Analyzing " + setNamesArray.length + " genesets");

		for(int i = 0; i < setNamesArray.length; i++) {

			int percentComplete = (int) (((double) i / setNamesArray.length) * 100);
			if(taskMonitor != null)
				taskMonitor.setProgress(percentComplete);
			if(cancelled) {
				taskMonitor.showMessage(Level.ERROR, "loading of GMT files cancelled");
				return;
			}
			String signatureGeneset = setNamesArray[i];


			boolean matchfound = false;

			if(filterMetric.getFilterType() != FilterType.NO_FILTER) {
				//only add the name if it overlaps with the sets in the map.
				for(String mapGeneset : genesets_in_map.keySet()) {
					//check if this set overlaps with current geneset
					Set<Integer> mapset = new HashSet<>(genesets_in_map.get(mapGeneset).getGenes());
					int original_size = mapset.size();
					Set<Integer> paset = new HashSet<>(signatureGenesets.getGenesets().get(signatureGeneset).getGenes());
					mapset.retainAll(paset);

					matchfound = filterMetric.match(original_size, mapset, paset);
					if(matchfound)
						break;
				}
			} else {
				matchfound = true;
			}

			if(matchfound) {
				resultSignatureSetNames.add(signatureGeneset);
			}
		}
	}

	
	/**
	 * type Set.class for filtered gene set names
	 */
	@Override
	public <R> R getResults(Class<? extends R> type) {
		if(Set.class.equals(type)) {
			return type.cast(resultSignatureSetNames);
		}
		if(SetOfGeneSets.class.equals(type)) {
			return type.cast(signatureGenesets);
		}
		return null;
	}

}
