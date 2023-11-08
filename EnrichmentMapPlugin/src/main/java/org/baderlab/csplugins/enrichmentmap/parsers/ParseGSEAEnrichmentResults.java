package org.baderlab.csplugins.enrichmentmap.parsers;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

import org.apache.commons.math3.util.Precision;
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
	
	public static enum ParseGSEAEnrichmentStrategy {
		FAIL_IMMEDIATELY,
		REPLACE_WITH_1;
	}
	
	public final EMDataSet dataset;
	private final ParseGSEAEnrichmentStrategy strategy;
	
	public ParseGSEAEnrichmentResults(EMDataSet dataset, ParseGSEAEnrichmentStrategy strategy) {
		this.dataset = dataset;
		this.strategy = strategy;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws IOException {
		taskMonitor = NullTaskMonitor.check(taskMonitor);
		taskMonitor.setTitle("Parsing GSEA Enrichment Result file");
		
		dataset.getMap().getParams().setFDR(true);
		NESFilter nesFilter = dataset.getMap().getParams().getNESFilter();
		
		if(nesFilter == NESFilter.ALL || nesFilter == NESFilter.POSITIVE) {
			readFile(dataset.getDataSetFiles().getEnrichmentFileName1());
		}
		if(nesFilter == NESFilter.ALL || nesFilter == NESFilter.NEGATIVE) {
			readFile(dataset.getDataSetFiles().getEnrichmentFileName2());
		}
	}
	
	private void readFile(String fileName) throws IOException {
		if(!Strings.isNullOrEmpty(fileName)) {
			LineReader lines = LineReader.create(fileName);
			try(lines) {
				parse(lines);
			} catch(IOException | UncheckedIOException e) {
				throw new IOException("Error parsing line " + lines.getLineNumber() + " of file: '" + fileName + "'", e);
			}
		}
	}
	
	private void parse(LineReader lines) throws IOException {
		Map<String, EnrichmentResult> results = dataset.getEnrichments().getEnrichments();
		
		//skip the first line which just has the field names (start i=1)
		lines.skip(1);
		
		while(lines.hasMoreLines()) {
			String line = lines.nextLine();
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
				size = parseInt(tokens[3]);
			}

			//The fifth column is the Enrichment score (ES)
			if(!tokens[4].isEmpty()) {
				ES = parseDouble(tokens[4]);
			}

			//The sixth column is the Normalize Enrichment Score (NES)
			if(!tokens[5].isEmpty()) {
				NES = parseDouble(tokens[5]);
			}

			//The seventh column is the nominal p-value
			if(!tokens[6].isEmpty()) {
				pvalue = parseDouble(tokens[6]);
			}

			//the eighth column is the FDR q-value
			if(!tokens[7].isEmpty()) {
				FDRqvalue = parseDouble(tokens[7]);
			}
			//the ninth column is the FWER q-value
			if(!tokens[8].isEmpty()) {
				FWERqvalue = parseDouble(tokens[8]);
			}
			//the tenth column is the rankatmax
			if(!tokens[9].isEmpty()) {
				rankAtMax = parseInt(tokens[9]);
			}
			
			// Values in EDB files are rounded to 4 decimal places. 
			// So to be consistent we will round the xls values to 4 decimal places as well.
			
			ES = Precision.round(ES, 4);
			NES = Precision.round(NES, 4);
			pvalue = Precision.round(pvalue, 4);
			FDRqvalue = Precision.round(FDRqvalue, 4);
			FWERqvalue = Precision.round(FWERqvalue, 4);
			
			GSEAResult result = new GSEAResult(Name, size, ES, NES, pvalue, FDRqvalue, FWERqvalue, rankAtMax, scoreAtMax);
			results.put(Name, result);
		}
	}
	
	private double parseDouble(String token) {
		try {
			return Double.parseDouble(token);
		} catch(NumberFormatException e) {
			if(strategy == ParseGSEAEnrichmentStrategy.REPLACE_WITH_1) {
				return 1;
			}
			throw new ParseGSEAEnrichmentException(e, token);
		}
	}
	
	private int parseInt(String token) {
		try {
			return Integer.parseInt(token);
		} catch(NumberFormatException e) {
			if(strategy == ParseGSEAEnrichmentStrategy.REPLACE_WITH_1) {
				return 1;
			}
			throw new ParseGSEAEnrichmentException(e, token);
		}
	}
	
	/**
	 * Values in EDB files are rounded to 4 decimal places. 
	 */
	private float parseAndRound(String exp) {
		float f = Float.parseFloat(exp);
		float r = Precision.round(f, 4);
		return r;
	}

}
