package org.baderlab.csplugins.enrichmentmap.commands;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.cytoscape.work.Tunable;

public class DatasetListTunable {
	
	@Tunable(description="Comma separated list of dataset names or indexes, or 'ALL' to indicate all datasets.",
			longDescription="Comma separated list of dataset names or indexes, for example 'dataset1,dataset2,3'. " +
							"The list may also contain positive integers that indicate the index of the dataset in the enrichment map. " +
							"Alternately use 'ALL' to indicate all data sets.")
	public String datasets = "ALL";
	
	
	public Collection<EMDataSet> getDataSets(EnrichmentMap map) {
		if(datasets == null) {
			return Collections.emptyList();
		}
		if(datasets.trim().equalsIgnoreCase("ALL")) {
			return map.getDataSetList();
		}
		
		Set<EMDataSet> result = new HashSet<>();
		List<EMDataSet> datasetList = map.getDataSetList();
		
		String[] names = datasets.split(",");
		for(String name : names) {
			int index = getIndex(name);
			if(index >= 0 && index < datasetList.size()) {
				result.add(datasetList.get(index));
			} else {
				EMDataSet ds = map.getDataSet(name);
				if(ds != null) {
					result.add(ds);
				}
			}
		}
		
		return result;
	}
	
	public Set<String> getDataSetNames(EnrichmentMap map) {
		return getDataSets(map).stream().map(EMDataSet::getName).collect(Collectors.toSet());
	}
	
	
	private static int getIndex(String s) {
		try {
			return Integer.parseInt(s);
		} catch(NumberFormatException e) {
			return -1;
		}
	}

}
