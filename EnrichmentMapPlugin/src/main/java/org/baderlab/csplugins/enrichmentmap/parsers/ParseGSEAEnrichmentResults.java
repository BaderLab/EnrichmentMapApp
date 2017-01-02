package org.baderlab.csplugins.enrichmentmap.parsers;

import java.util.List;
import java.util.Map;

import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResult;
import org.baderlab.csplugins.enrichmentmap.model.GSEAResult;
import org.baderlab.csplugins.enrichmentmap.util.NullTaskMonitor;
import org.cytoscape.work.TaskMonitor;

public class ParseGSEAEnrichmentResults extends DatasetLineParser {
	
	public ParseGSEAEnrichmentResults(DataSet dataset) {
		super(dataset);
	}
	
	@Override
	public void parseLines(List<String> lines, DataSet dataset, TaskMonitor taskMonitor) {
		if(taskMonitor == null)
			taskMonitor = new NullTaskMonitor();
		taskMonitor.setTitle("Parsing Bingo Enrichment Result file");
		
		//skip the first line which just has the field names (start i=1)

		dataset.getMap().getParams().setFDR(true);

		int currentProgress = 0;
		int maxValue = lines.size();
		taskMonitor.setStatusMessage("Parsing Enrichment Results file - " + maxValue + " rows");

		Map<String, EnrichmentResult> results = dataset.getEnrichments().getEnrichments();
		
		for(int i = 1; i < lines.size(); i++) {
			String line = lines.get(i);
			String[] tokens = line.split("\t");
			
			int size = 0;
			double ES = 0.0;
			double NES = 0.0;
			double pvalue = 1.0;
			double FDRqvalue = 1.0;
			double FWERqvalue = 1.0;
			int rankAtMax = -1;
			double scoreAtMax = DefaultScoreAtMax;

			//The first column of the file is the name of the geneset
			String Name = tokens[0].toUpperCase().trim();

			//The fourth column is the size of the geneset
			if(!tokens[3].isEmpty()) {
				size = Integer.parseInt(tokens[3]);
			}

			//The fifth column is the Enrichment score (ES)
			if(!tokens[4].isEmpty()) {
				ES = Double.parseDouble(tokens[4]);
			}

			//The sixth column is the Normalize Enrichment Score (NES)
			if(!tokens[5].isEmpty()) {
				NES = Double.parseDouble(tokens[5]);
			}

			//The seventh column is the nominal p-value
			if(!tokens[6].isEmpty()) {
				pvalue = Double.parseDouble(tokens[6]);
			}

			//the eighth column is the FDR q-value
			if(!tokens[7].isEmpty()) {
				FDRqvalue = Double.parseDouble(tokens[7]);
			}
			//the ninth column is the FWER q-value
			if(!tokens[8].isEmpty()) {
				FWERqvalue = Double.parseDouble(tokens[8]);
			}
			//the tenth column is the rankatmax
			if(!tokens[9].isEmpty()) {
				rankAtMax = Integer.parseInt(tokens[9]);
			}
			
			GSEAResult result = new GSEAResult(Name, size, ES, NES, pvalue, FDRqvalue, FWERqvalue, rankAtMax, scoreAtMax);

			// Calculate Percentage.  This must be a value between 0..100.
			int percentComplete = (int) (((double) currentProgress / maxValue) * 100);
			taskMonitor.setProgress(percentComplete);
			currentProgress++;

			results.put(Name, result);
		}
	}

}
