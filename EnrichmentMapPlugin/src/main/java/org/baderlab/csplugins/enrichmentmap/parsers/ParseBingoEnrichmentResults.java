package org.baderlab.csplugins.enrichmentmap.parsers;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResult;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.baderlab.csplugins.enrichmentmap.model.GenericResult;
import org.baderlab.csplugins.enrichmentmap.util.NullTaskMonitor;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.common.collect.ImmutableSet;

public class ParseBingoEnrichmentResults extends AbstractTask {

	private final EMDataSet dataset;
	
	public ParseBingoEnrichmentResults(EMDataSet dataset) {
		this.dataset = dataset;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws IOException {
		if(taskMonitor == null)
			taskMonitor = new NullTaskMonitor();
		taskMonitor.setTitle("Parsing Bingo Enrichment Result file");
		
		List<String> lines = LineReader.readLines(dataset.getDataSetFiles().getEnrichmentFileName1());
				
		//with Bingo results there are no genesets defined.  first pass through the file
		// needs to parse the genesets

		//the bingo file has 20 lines of info at the top of the file before you get to results.

		//parameters that can be extracted from Bingo files:
		//GO-ID	p-value	corr p-value	x	n	X	N	Description	Genes in test set
		// (column 1 ) GO-Id - is just the numerical part of the GO term (does not contain GO:0000)
		//(column 2 ) p-value
		//(column 3 ) corr pvalue
		//(column 4 ) x - number of genes in the subset of interest with this annotation
		//(column 5 ) n - number of genes in the universe with this annotation
		//(column 6 ) X - number of genes in the subset
		//(column 7 ) N - number of genes in the universe
		//(column 8 ) Description - GO term name
		//(column 9 ) Gene in test set - a list of genes in the subset of interest that are annotated to this term

		// Column 8 is the geneset name
		// Column 9 is the list of genes in this geneset -- therefore pre-filtered.
		Map<String, GeneSet> genesets = dataset.getSetOfGeneSets().getGeneSets();

		//get the genes (which should also be empty
		EnrichmentMap map = dataset.getMap();
		Map<String, EnrichmentResult> results = dataset.getEnrichments().getEnrichments();

		int currentProgress = 0;
		int maxValue = lines.size();
		boolean FDR = true;
		taskMonitor.setStatusMessage("Parsing Generic Results file -" + maxValue + " rows");
		
		//skip the first l9 which just has the field names (start i=1)
		//check to see how many columns the data has

		//go through each line until we find the header line
		int k = 0;
		String line = lines.get(k);
		String[] tokens = line.split("\t");
		for(; k < lines.size(); k++) {
			line = lines.get(k);
			tokens = line.split("\t");
			int length = tokens.length;
			if((length == 9) && tokens[0].equalsIgnoreCase("GO-ID") && tokens[8].equalsIgnoreCase("Genes in test set")) {
				break;
			}
		}
		if(k == lines.size())
			throw new IllegalThreadStateException("Bingo results file is missing data.");
		//not enough data in the file!!

		for(int i = k + 1; i < lines.size(); i++) {
			line = lines.get(i);

			tokens = line.split("\t");

			double pvalue = 1.0;
			double FDRqvalue = 1.0;
			GenericResult result;
			int gs_size = 0;
			double NES = 1.0;

			//The 8th column of the file is the name of the geneset
			final String name = tokens[7].toUpperCase().trim();

			//the 8th column of the file is the description
			final String description = tokens[7].toUpperCase();

			//when there are two different species it is possible that the gene set could
			//already exist in the set of genesets.  if it does exist then add the genes
			//in this set to the geneset
			ImmutableSet.Builder<Integer> builder = ImmutableSet.builder();

			if(genesets.containsKey(name))
				builder = builder.addAll(genesets.get(name).getGenes());


			String[] gene_tokens = tokens[8].split("\\|");

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

			//The 2nd column is the nominal p-value
			if(tokens[1].equalsIgnoreCase("")) {
				//do nothing
			} else {
				pvalue = Double.parseDouble(tokens[1]);
			}

			//the 4th column is the size of the geneset
			//the Count is the size of the geneset (restricted by the gene list)
			if(tokens[3].equalsIgnoreCase("")) {
				//do nothing
			} else {
				gs_size = Integer.parseInt(tokens[3]);
			}

			//Use the correct p-value - 3rd column
			if(tokens[2].equalsIgnoreCase("")) {
				//do nothing
			} else {
				FDRqvalue = Double.parseDouble(tokens[2]);
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
