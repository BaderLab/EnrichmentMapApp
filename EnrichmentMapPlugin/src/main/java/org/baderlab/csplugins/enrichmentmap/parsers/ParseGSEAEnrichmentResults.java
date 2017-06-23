package org.baderlab.csplugins.enrichmentmap.parsers;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResult;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResultFilterParams.NESFilter;
import org.baderlab.csplugins.enrichmentmap.model.GSEAResult;
import org.baderlab.csplugins.enrichmentmap.util.NullTaskMonitor;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.common.base.Strings;

public class ParseGSEAEnrichmentResults extends AbstractTask {
	
	public static final Double DefaultScoreAtMax = -1000000.0;
	
	
	public final EMDataSet dataset;
	
	public ParseGSEAEnrichmentResults(EMDataSet dataset) {
		this.dataset = dataset;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws IOException {
		if(taskMonitor == null)
			taskMonitor = new NullTaskMonitor();
		taskMonitor.setTitle("Parsing Bingo Enrichment Result file");
		
		NESFilter nesFilter = dataset.getMap().getParams().getNESFilter();
		
		if(nesFilter == NESFilter.ALL || nesFilter == NESFilter.POSITIVE) {
			String positiveEnrichmentResults = dataset.getEnrichments().getFilename1();
			if(!Strings.isNullOrEmpty(positiveEnrichmentResults)) {
				readFile(taskMonitor, positiveEnrichmentResults);
			}
		}
		
		if(nesFilter == NESFilter.ALL || nesFilter == NESFilter.NEGATIVE) {
			String negativeEnrichmentResults = dataset.getEnrichments().getFilename2();
			if(!Strings.isNullOrEmpty(negativeEnrichmentResults)) {
				readFile(taskMonitor, negativeEnrichmentResults);
			}
		}
	}
	
	
	private void readFile(TaskMonitor taskMonitor, String enrichmentFile) throws IOException {
		dataset.getMap().getParams().setFDR(true);

		List<String> lines = LineReader.readLines(enrichmentFile);
		
		int currentProgress = 0;
		int maxValue = lines.size();
		taskMonitor.setStatusMessage("Parsing Enrichment Results file - " + maxValue + " rows");

		
		Map<String, EnrichmentResult> results = dataset.getEnrichments().getEnrichments();
		
		//skip the first line which just has the field names (start i=1)
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
