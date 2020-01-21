package org.baderlab.csplugins.enrichmentmap.parsers;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.baderlab.csplugins.enrichmentmap.util.NullTaskMonitor;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;

/**
 * Just collects the names of the genes in the generic/gProfiler enrichment file
 */
public class ParseGenericEnrichmentsForDummy extends AbstractTask implements ObservableTask {
	
	private final String fileName;
	private final Set<String> enrichmentGenes = new HashSet<>();
	
	public ParseGenericEnrichmentsForDummy(String fileName) {
		this.fileName = fileName;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws IOException {
		taskMonitor = NullTaskMonitor.check(taskMonitor);
		taskMonitor.setTitle("Parsing Generic Result file");
		
		List<String> lines = LineReader.readLines(fileName);

		//skip the first line which just has the field names (start i=1)
		
		for(int i = 1; i < lines.size(); i++) {
			String line = lines.get(i);
			String[] tokens = line.split("\t");
			//update the length each time because some line might have missing values
			if(tokens.length > 5) {
				//get all the genes in the field
				String[] gene_tokens = tokens[5].split(",");
				for(String token : gene_tokens) {
					String gene = token.trim().toUpperCase();
					if(!gene.isEmpty()) {
						enrichmentGenes.add(gene);
					}
				}
			}
		}
		
	}

	@Override
	public <R> R getResults(Class<? extends R> type) {
		if(Set.class.equals(type)) {
			return type.cast(enrichmentGenes);
		}
		return null;
	}
	
	public Set<String> getEnrichmentFileGenes() {
		return enrichmentGenes;
	}

}
