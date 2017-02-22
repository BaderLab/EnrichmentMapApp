package org.baderlab.csplugins.enrichmentmap.parsers;

import java.util.List;
import java.util.Map;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResult;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.baderlab.csplugins.enrichmentmap.model.GenericResult;
import org.baderlab.csplugins.enrichmentmap.util.NullTaskMonitor;
import org.cytoscape.work.TaskMonitor;

import com.google.common.collect.ImmutableSet;

public class ParseDavidEnrichmentResults extends DatasetLineParser {
	
	public ParseDavidEnrichmentResults(EMDataSet dataset) {
		super(dataset);
	}

	/**
	 * Parse david enrichment results file
	 */
	@Override
	public void parseLines(List<String> lines, EMDataSet dataset, TaskMonitor taskMonitor) {
		if(taskMonitor == null)
			taskMonitor = new NullTaskMonitor();
		taskMonitor.setTitle("Parsing David Enrichment Result file");
		
		//with David results there are no genesets defined.  first pass through the file
		// needs to parse the genesets

		//parameters that can be extracted from David files:
		//Category	Term	Count	%	PValue	Genes	List Total	Pop Hits	Pop Total	Fold Enrichment	Bonferroni	Benjamini	FDR
		// Count = number of genes in the geneset that came from the input list, number of genes in the genelist mapping toa specific term.
		// List Total - number of genes in the gene list mapping to the category (ie. GO Cellular component)
		// Pop Hits - number of genes in the background gene list mapping to a specific term
		// Pop total - number of gene s in the background gene list mapping to the category (i.e. Go Cellular Component)

		// Column 2 is the geneset name
		// Column 1 is the category (and can be used for the description)
		// Column 6 is the list of genes (from the loaded list) in this geneset -- therefore pre-filtered.
		Map<String, GeneSet> genesets = dataset.getSetOfGeneSets().getGeneSets();


		EnrichmentMap map = dataset.getMap();
		Map<String, EnrichmentResult> results = dataset.getEnrichments().getEnrichments();

		int currentProgress = 0;
		int maxValue = lines.size();
		taskMonitor.setStatusMessage("Parsing Generic Results file - " + maxValue + " rows");

		boolean FDR = true;

		//skip the first line which just has the field names (start i=1)
		//check to see how many columns the data has
		String line = lines.get(0);
		String[] tokens = line.split("\t");
		int length = tokens.length;
		if(length != 13)
			throw new IllegalThreadStateException("David results file is missing data.");
		//not enough data in the file!!

		for(int i = 1; i < lines.size(); i++) {
			line = lines.get(i);

			tokens = line.split("\t");

			double pvalue = 1.0;
			double FDRqvalue = 1.0;
			GenericResult result;
			int gs_size = 0;
			double NES = 1.0;

			//The second column of the file is the name of the geneset
			final String name = tokens[1].toUpperCase().trim();

			//the first column of the file is the description
			final String description = tokens[0].toUpperCase();

			//when there are two different species it is possible that the gene set could
			//already exist in the set of genesets.  if it does exist then add the genes
			//in this set to the geneset
			ImmutableSet.Builder<Integer> builder = ImmutableSet.builder();
			if(genesets.containsKey(name))
				builder = builder.addAll(genesets.get(name).getGenes());

			String[] gene_tokens = tokens[5].split(", ");

			//All subsequent fields in the list are the geneset associated with this geneset.
			for(int j = 0; j < gene_tokens.length; j++) {

				String gene = gene_tokens[j].toUpperCase();
				
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

			//finished parsing that geneset
			//add the current geneset to the hashmap of genesets
			GeneSet gs = new GeneSet(name, description, builder.build());
			genesets.put(name, gs);

			//The 5th column is the nominal p-value
			if(tokens[4].equalsIgnoreCase("")) {
				//do nothing
			} else {
				pvalue = Double.parseDouble(tokens[4]);
			}

			//the Pop hits is the size of the geneset
			//the Count is the size of the geneset (restricted by the gene list)
			if(tokens[2].equalsIgnoreCase("")) {
				//do nothing
			} else {
				gs_size = Integer.parseInt(tokens[2]);
			}

			//Use the Benjamini value for the fdr
			if(tokens[11].equalsIgnoreCase("")) {
				//do nothing
			} else {
				FDRqvalue = Double.parseDouble(tokens[11]);
			}

			result = new GenericResult(name, description, pvalue, gs_size, FDRqvalue);

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
