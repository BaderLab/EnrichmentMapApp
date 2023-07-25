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

public class ParseGenericEnrichmentResults extends AbstractTask {
	
	private final EMDataSet dataset;
	
	public ParseGenericEnrichmentResults(EMDataSet dataset) {
		this.dataset = dataset;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws IOException {
		taskMonitor = NullTaskMonitor.check(taskMonitor);
		taskMonitor.setStatusMessage("Parsing Generic Enrichment file");
		taskMonitor.setTitle("Parsing Generic Result file");
		
		var fileName = dataset.getDataSetFiles().getEnrichmentFileName1();
		LineReader lines = LineReader.create(fileName);
		
		try(lines) {
			parse(lines);
		} catch(Exception e) {
			throw new IOException("Could not parse line " + lines.getLineNumber() + " of enrichment file '" + fileName +  "'", e);
		} finally {
			taskMonitor.setProgress(1.0);
		}
	}
	
	
	private void parse(LineReader lineReader) throws IOException {
		boolean FDR = false; // false data rate
		boolean hasNegOneNES = false, hasPosOneNES = false, hasOtherNES = false;
		
		EnrichmentMap map = dataset.getMap();
		SetOfEnrichmentResults enrichments = dataset.getEnrichments();
		Map<String, EnrichmentResult> results = enrichments.getEnrichments();
		String upPhenotype = enrichments.getPhenotype1();
		String downPhenotype = enrichments.getPhenotype2();
		
		//Get the current genesets so we can check that all the results are in the geneset list
		//and put the size of the genesets into the visual style
		Map<String,GeneSet> genesets = dataset.getSetOfGeneSets().getGeneSets();
				
		//check to see if there are genesets.
		//if there are no genesets then populate the genesets from the generic file
		//can only do this if the 6th column has a list of genes for that geneset.
		boolean populate_gs = false;
		if(genesets == null || genesets.isEmpty()) {
			populate_gs = true;
		} else {
			//as this is the default for gprofiler use the Description in the visual style instead of the formatted name
			//but only if there is a gmt supplied.  If using just the generic output file there is not field for description
			dataset.getMap().getParams().setEMgmt(true);
		}

		lineReader.skip(1); // skip header line
		
		while(lineReader.hasMoreLines()) {
			String line = lineReader.nextLine();
			if(line.isBlank())
				continue;
			
			String[] tokens = line.split("\t");

			double pvalue = 1.0;
			double FDRqvalue = 1.0;
			int gs_size = 0;
			double NES = 1.0;
			
			GenericResult result;

			//The first column of the file is the name of the geneset
			final String name = tokens[0].toUpperCase().trim();
			final String description = tokens[1].toUpperCase();
			if(genesets.containsKey(name)) {
				gs_size = genesets.get(name).getGenes().size();
			} 
			if(!Strings.isNullOrEmpty(tokens[2])) {
				pvalue = Double.parseDouble(tokens[2]);
			}
			
			// if (length < 3) not enough data in the file!! The fourth column is the FDR q-value.
			if(tokens.length > 3) { 
				if(!Strings.isNullOrEmpty(tokens[3])) {
					FDRqvalue = Double.parseDouble(tokens[3]);
					FDR = true;
				}
				
				// the fifth column is the phenotype.
				// it can either be a signed number or it can be text specifying the phenotype
				// in order for it to be parseable the text has to match the user specified phenotypes
				// and if it is a number the only important part is the sign
				if(tokens.length > 4) {
					if(!Strings.isNullOrEmpty(tokens[4])) {
						//check to see if the string matches the specified phenotypes
						if(tokens[4].equalsIgnoreCase(upPhenotype)) {
							NES = 1.0;
						} else if(tokens[4].equalsIgnoreCase(downPhenotype)) {
							NES = -1.0;
						} else {
							try {
								//try and see if the user has specified the phenotype as a number
								NES = Double.parseDouble(tokens[4]);
							} catch(NumberFormatException nfe) {
								throw new IllegalArgumentException(tokens[4] + " is not a valid phenotype. Phenotype specified in generic enrichment results file must have the same phenotype as specified in advanced options or must be a positive or negative number.");
							}
						}
					}
					
					if(NES == 1.0) 
						hasPosOneNES = true;
					else if(NES == -1.0) 
						hasNegOneNES = true;
					else
						hasOtherNES = true;

					// ticket#57 - adding additional column to generic format, similiar to Bingo and David
					// that outlines the genes from the query that are found in the geneset and results in its enrichment
					if(tokens.length > 5 && populate_gs) {
						String[] gene_tokens = tokens[5].split(",");

						ImmutableSet.Builder<Integer> builder = ImmutableSet.builder();
						
						//All subsequent fields in the list are the genes associated with this geneset.
						for(String token : gene_tokens) {
							Integer hash = map.addGene(token);
							if(hash != null)
								builder.add(hash);
						}

						GeneSet gs = new GeneSet(name, description, builder.build());
						gs_size = gs.getGenes().size();
						genesets.put(name, gs);

					} //end of tokens>5
					result = new GenericResult(name, description, pvalue, gs_size, FDRqvalue, NES);
				} else { //end of tokens>4
					result = new GenericResult(name, description, pvalue, gs_size, FDRqvalue);
				}
			} else {
				result = new GenericResult(name, description, pvalue, gs_size);
			}

			//check to see if the gene set has already been entered in the results
			//it is possible that one geneset will be in both phenotypes.
			//if it is already exists then we want to make sure the one retained is the result with the
			//lower p-value.
			//ticket #149
			GenericResult temp = (GenericResult) results.get(name);
			if(temp == null)
				results.put(name, result);
			else if(result.getPvalue() < temp.getPvalue()) 
				results.put(name, result);
		}
		
		if(FDR)
			dataset.getMap().getParams().setFDR(FDR);
		if(hasPosOneNES && hasNegOneNES && !hasOtherNES)
			dataset.setIsTwoPhenotypeGeneric(true);
	}

}
