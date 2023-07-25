package org.baderlab.csplugins.enrichmentmap.parsers;

import java.io.IOException;
import java.util.Map;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResult;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.baderlab.csplugins.enrichmentmap.model.GenericResult;
import org.baderlab.csplugins.enrichmentmap.model.SetOfEnrichmentResults;
import org.baderlab.csplugins.enrichmentmap.util.NullTaskMonitor;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;

public class ParseEnrichrEnrichmentResults extends AbstractTask {

	private final EMDataSet dataset;
	
	public ParseEnrichrEnrichmentResults(EMDataSet dataset) {
		this.dataset = dataset;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws IOException {
		taskMonitor = NullTaskMonitor.check(taskMonitor);
		taskMonitor.setTitle("Parsing Enricher Result file");
		
		String fileName = dataset.getDataSetFiles().getEnrichmentFileName1();
		LineReader lines = LineReader.create(fileName);
		
		try(lines) {
			parse(lines);
		} catch(Exception e) {
			throw new IOException("Could not parse line " + lines.getLineNumber() + " of enrichment file '" + fileName +  "'", e);
		} finally {
			taskMonitor.setProgress(1.0);
		}
	}
	
	
	private void parse(LineReader lines) throws IOException {
		EnrichmentMap map = dataset.getMap();
		SetOfEnrichmentResults enrichments = dataset.getEnrichments();
		Map<String, EnrichmentResult> results = enrichments.getEnrichments();
		Map<String,GeneSet> genesets = dataset.getSetOfGeneSets().getGeneSets();
		boolean useFDR = false;
		
		// skip the first line which just has the field names
		lines.skip(1);
		
		while(lines.hasMoreLines()) {
			String line = lines.nextLine();
			
			String[] tokens = line.split("\t");
			
			//The first column of the file is the name of the geneset
			final String name = tokens[0].toUpperCase().trim();
			
			double pvalue = 1.0;
			if(!Strings.isNullOrEmpty(tokens[2])) {
				pvalue = Double.parseDouble(tokens[2]);
			}
			double qvalue = 1.0;
			if(!Strings.isNullOrEmpty(tokens[3])) { // adjusted p-value is the q-value
				qvalue = Double.parseDouble(tokens[3]);
				useFDR = true;
			}

			ImmutableSet.Builder<Integer> builder = ImmutableSet.builder();
			
			String[] geneTokens = tokens[8].split(";");
			for(String token : geneTokens) {
				Integer hash = map.addGene(token);
				if(hash != null)
					builder.add(hash);
			}

			GeneSet gs = new GeneSet(name, name, builder.build());
			int gsSize = gs.getGenes().size();
			genesets.put(name, gs);
			
			GenericResult result = new GenericResult(name, name, pvalue, gsSize, qvalue);
			results.put(name, result);
		}
		
		if(useFDR) {
			dataset.getMap().getParams().setFDR(useFDR);
		}
	}

}
