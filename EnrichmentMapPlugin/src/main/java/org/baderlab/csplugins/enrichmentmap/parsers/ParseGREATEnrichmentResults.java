package org.baderlab.csplugins.enrichmentmap.parsers;

import java.util.HashMap;
import java.util.List;

import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResult;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.baderlab.csplugins.enrichmentmap.model.GenericResult;
import org.baderlab.csplugins.enrichmentmap.task.NullTaskMonitor;
import org.cytoscape.work.TaskMonitor;

import com.google.common.collect.ImmutableSet;

public class ParseGREATEnrichmentResults extends DatasetLineParser {

	public ParseGREATEnrichmentResults(DataSet dataset) {
		super(dataset);
	}

	public void parseLines(List<String> lines, DataSet dataset, TaskMonitor taskMonitor) {
		if(taskMonitor == null)
			taskMonitor = new NullTaskMonitor();
		taskMonitor.setTitle("Parsing Enrichment Result file");

		boolean hasBackground = false;

		EnrichmentMapParameters params = dataset.getMap().getParams();

		//Get the type of filter user specified on the GREAT results
		//If it is hyper use column 14 Hypergeometric p-value and 16 FDR for hyper
		//If it is binom use column 5 bionomial p-value and 7 FDR for binom
		//If they specify both use the highest p-value and q-value from the above columns
		String filterType = dataset.getMap().getParams().getGreat_Filter();

		HashMap<String, GeneSet> genesets = dataset.getSetofgenesets().getGenesets();

		//get the genes (which should also be empty
		HashMap<String, Integer> genes = dataset.getMap().getGenes();
		HashMap<Integer, String> key2gene = dataset.getMap().getHashkey2gene();
		HashMap<String, EnrichmentResult> results = dataset.getEnrichments().getEnrichments();
		
		int currentProgress = 0;
		int maxValue = lines.size();
		taskMonitor.setStatusMessage("Parsing Great Results file - " + maxValue + " rows");
		//for great files there is an FDR
		dataset.getMap().getParams().setFDR(true);

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
			if((length == 24) && tokens[3].equalsIgnoreCase("BinomRank")) {
				break;
			}
			//If GREAT is done with a background set then the table looks different
			//There is not binom rank and no binomial data.
			else if((length == 20) && tokens[3].equalsIgnoreCase("Rank")) {
				hasBackground = true;
				break;
			}
		}

		//go through the rest of the lines
		for(int i = k + 1; i < lines.size(); i++) {
			line = lines.get(i);

			tokens = line.split("\t");
			//there are extra lines at the end of the file that should be ignored.
			if(!hasBackground && tokens.length != 24)
				continue;
			if(hasBackground && tokens.length != 20)
				continue;

			double pvalue = 1.0;
			double FDRqvalue = 1.0;
			GenericResult result;
			int gs_size = 0;
			double NES = 1.0;

			//details of export file
			//http://bejerano.stanford.edu/help/display/GREAT/Export

			//The second column of the file is the name of the geneset
			final String name = tokens[1].trim() + "-" + tokens[2].trim();

			//the first column of the file is the description
			final String description = tokens[2].trim();

			//when there are two different species it is possible that the gene set could
			//already exist in the set of genesets.  if it does exist then add the genes
			//in this set to the geneset
			
			ImmutableSet.Builder<Integer> builder = ImmutableSet.builder();
			
			if(genesets.containsKey(name))
				builder = builder.addAll(genesets.get(name).getGenes());

			String[] gene_tokens;
			if(!hasBackground)
				gene_tokens = tokens[23].split(",");
			else
				gene_tokens = tokens[18].split(",");

			//All subsequent fields in the list are the geneset associated with this geneset.
			for(int j = 0; j < gene_tokens.length; j++) {

				String gene = gene_tokens[j].toUpperCase();
				//Check to see if the gene is already in the hashmap of genes
				//if it is already in the hash then get its associated key and put it
				//into the set of genes
				if(genes.containsKey(gene)) {
					builder.add(genes.get(gene));
				}

				//If the gene is not in the list then get the next value to be used and put it in the list
				else {
					if(!gene.equalsIgnoreCase("")) {

						//add the gene to the master list of genes
						int value = dataset.getMap().getNumberOfGenes();
						genes.put(gene, value);
						key2gene.put(value, gene);
						dataset.getMap().setNumberOfGenes(value + 1);

						//add the gene to the genelist
						builder.add(genes.get(gene));
					}
				}
			}

			//finished parsing that geneset
			//add the current geneset to the hashmap of genesets
			GeneSet gs = new GeneSet(name, description, builder.build());
			genesets.put(name, gs);

			//There are two tests run by GREAT, the binomial on regions and the hypergeometric based on genes
			//The first pass of results shows only those that are significant both
			//The user can then choose to use either or both together
			//
			//If it is hyper use column 14 Hypergeometric p-value and 16 FDR for hyper
			//If it is binom use column 5 bionomial p-value and 7 FDR for binom
			//If they specify both use the highest p-value and q-value from the above columns
			double hyper_pvalue = 1;
			double hyper_fdr = 1;
			double binom_pvalue = 1;
			double binom_fdr = 1;
			if(!hasBackground) {
				if(!tokens[4].equalsIgnoreCase(""))
					binom_pvalue = Double.parseDouble(tokens[4]);
				if(!tokens[6].equalsIgnoreCase(""))
					binom_fdr = Double.parseDouble(tokens[6]);
				if(!tokens[13].equalsIgnoreCase(""))
					hyper_pvalue = Double.parseDouble(tokens[13]);
				if(!tokens[15].equalsIgnoreCase(""))
					hyper_fdr = Double.parseDouble(tokens[15]);
			} else {
				if(!tokens[4].equalsIgnoreCase(""))
					hyper_pvalue = Double.parseDouble(tokens[4]);
				if(!tokens[6].equalsIgnoreCase(""))
					hyper_fdr = Double.parseDouble(tokens[6]);
			}
			if(filterType.equalsIgnoreCase(EnrichmentMapParameters.GREAT_hyper)) {
				pvalue = hyper_pvalue;
				FDRqvalue = hyper_fdr;
			} else if(filterType.equalsIgnoreCase(EnrichmentMapParameters.GREAT_binom)) {
				pvalue = binom_pvalue;
				FDRqvalue = binom_fdr;
			} else if(filterType.equalsIgnoreCase(EnrichmentMapParameters.GREAT_both)) {
				pvalue = (hyper_pvalue >= binom_pvalue) ? hyper_pvalue : binom_pvalue;
				FDRqvalue = (hyper_fdr >= binom_fdr) ? hyper_fdr : binom_fdr;
			} else if(filterType.equalsIgnoreCase(EnrichmentMapParameters.GREAT_either)) {
				pvalue = (hyper_pvalue >= binom_pvalue) ? binom_pvalue : hyper_pvalue;
				FDRqvalue = (hyper_fdr >= binom_fdr) ? binom_fdr : hyper_fdr;
			} else {
				System.out.println("Invalid attribute setting for GREAT p-value specification");
			}

			//Keep track of minimum p-value to better calculate jslider
			if(pvalue < params.getPvalue_min())
				params.setPvalue_min(pvalue);

			if(FDRqvalue < params.getQvalue_min())
				params.setQvalue_min(FDRqvalue);

			//the Count is the size of the geneset - not restricted to the genes of interest
			//the 20th column total genes, a.k.a "annotation count" (K)
			//If this is a background set then it is in the 16th column
			if((!hasBackground) && (!tokens[19].equalsIgnoreCase("")))
				gs_size = Integer.parseInt(tokens[19]);
			else if((hasBackground) && (!tokens[15].equalsIgnoreCase("")))
				gs_size = Integer.parseInt(tokens[15]);

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
	}

}
