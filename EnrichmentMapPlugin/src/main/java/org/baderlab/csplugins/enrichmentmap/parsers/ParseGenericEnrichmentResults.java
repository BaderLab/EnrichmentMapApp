package org.baderlab.csplugins.enrichmentmap.parsers;

import java.io.IOException;
import java.util.List;
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

import com.google.common.collect.ImmutableSet;

public class ParseGenericEnrichmentResults extends AbstractTask {
	
	private final EMDataSet dataset;
	
	public ParseGenericEnrichmentResults(EMDataSet dataset) {
		this.dataset = dataset;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws IOException {
		if(taskMonitor == null)
			taskMonitor = new NullTaskMonitor();
		taskMonitor.setTitle("Parsing Generic Result file");
		
		List<String> lines = LineReader.readLines(dataset.getEnrichments().getFilename1());

		//Get the current genesets so we can check that all the results are in the geneset list
		//and put the size of the genesets into the visual style
		Map<String, GeneSet> genesets = dataset.getSetOfGeneSets().getGeneSets();

		int currentProgress = 0;
		int maxValue = lines.size();
		taskMonitor.setStatusMessage("Parsing Generic Results file - " + maxValue + " rows");
		boolean FDR = false;

		//skip the first line which just has the field names (start i=1)
		//check to see how many columns the data has
		String line = lines.get(0);
		String[] tokens = line.split("\t");
		int length = tokens.length;

		EnrichmentMap map = dataset.getMap();
		SetOfEnrichmentResults enrichments = dataset.getEnrichments();
		Map<String, EnrichmentResult> results = enrichments.getEnrichments();
		String upPhenotype = enrichments.getPhenotype1();
		String downPhenotype = enrichments.getPhenotype2();
		
		//check to see if there are genesets.
		//if there are no genesets then populate the genesets from the generic file
		//can only do this if the 6th column has a list of genes for that geneset.
		boolean populate_gs = false;
		if(genesets == null || genesets.isEmpty())
			populate_gs = true;
		//as this is the default for gprofiler use the Description in the visual style instead of the formatted name
		//but only if there is a gmt supplied.  If using just the generic output file there is not field for description
		else
			dataset.getMap().getParams().setEMgmt(true);

		//if (length < 3)
		//not enough data in the file!!

		for(int i = 1; i < lines.size(); i++) {
			line = lines.get(i);

			tokens = line.split("\t");

			//update the length each time because some line might have missing values
			length = tokens.length;

			double pvalue = 1.0;
			double FDRqvalue = 1.0;
			GenericResult result;
			int gs_size = 0;
			double NES = 1.0;

			//The first column of the file is the name of the geneset
			final String name = tokens[0].toUpperCase().trim();
			final String description = tokens[1].toUpperCase();

			if(genesets.containsKey(name)) {
				gs_size = genesets.get(name).getGenes().size();
			} 

			//The third column is the nominal p-value
			if(tokens[2] == null || tokens[2].equalsIgnoreCase("")) {
				//do nothing
			} else {
				pvalue = Double.parseDouble(tokens[2]);
			}

			if(length > 3) {
				//the fourth column is the FDR q-value
				if(tokens[3] == null || tokens[3].equalsIgnoreCase("")) {
					//do nothing
				} else {
					FDRqvalue = Double.parseDouble(tokens[3]);
					FDR = true;
				}
				//the fifth column is the phenotype.
				//it can either be a signed number or it can be text specifying the phenotype
				//in order for it to be parseable the text has to match the user specified phenotypes
				// and if it is a number the only important part is the sign
				if(length > 4) {

					if(tokens[4] == null || tokens[4].equalsIgnoreCase("")) {

					} else {
						//check to see if the string matches the specified phenotypes
						if(tokens[4].equalsIgnoreCase(upPhenotype))
							NES = 1.0;
						else if(tokens[4].equalsIgnoreCase(downPhenotype))
							NES = -1.0;
						//try and see if the user has specified the phenotype as a number
						else {
							try {
								NES = Double.parseDouble(tokens[4]);
							} catch(NumberFormatException nfe) {
								throw new IllegalThreadStateException(tokens[4]
										+ " is not a valid phenotype.  Phenotype specified in generic enrichment results file must have the same phenotype as specified in advanced options or must be a positive or negative number.");
							}
						}
					}

					//ticket#57 - adding additional column to generic format, similiar to Bingo and David
					// that outlines the genes from the query that are found in the geneset and results in
					//its enrichment
					if(length > 5 && populate_gs) {

						//get all the genes in the field
						String[] gene_tokens = tokens[5].split(",");

						ImmutableSet.Builder<Integer> builder = ImmutableSet.builder();
						
						//All subsequent fields in the list are the geneset associated with this geneset.
						for(String token : gene_tokens) {
							String gene = token.trim().toUpperCase();

							//Check to see if the gene is already in the hashmap of genes
							//if it is already in the hash then get its associated key and put it into the set of genes
							if(map.containsGene(gene)) {
								builder.add(map.getHashFromGene(gene));
							}
							else if(!gene.isEmpty()) {
								Integer hash = map.addGene(gene).get();
								builder.add(hash);
							}
						}

						GeneSet gs = new GeneSet(name, description, builder.build());
						gs_size = gs.getGenes().size();
						//put the new or filtered geneset back into the set.
						genesets.put(name, gs);

					} //end of tokens>5
					result = new GenericResult(name, description, pvalue, gs_size, FDRqvalue, NES);
				} //end of tokens>4

				else
					result = new GenericResult(name, description, pvalue, gs_size, FDRqvalue);

			} else {
				result = new GenericResult(name, description, pvalue, gs_size);
			}

			// Calculate Percentage.  This must be a value between 0..100.
			int percentComplete = (int) (((double) currentProgress / maxValue) * 100);
			taskMonitor.setProgress(percentComplete);
			currentProgress++;

			//check to see if the gene set has already been entered in the results
			//it is possible that one geneset will be in both phenotypes.
			//if it is already exists then we want to make sure the one retained is the result with the
			//lower p-value.
			//ticket #149
			GenericResult temp = (GenericResult) results.get(name);
			if(temp == null)
				results.put(name, result);
			else {
				if(result.getPvalue() < temp.getPvalue())
					results.put(name, result);
			}

		}
		if(FDR)
			dataset.getMap().getParams().setFDR(FDR);
	}

}
