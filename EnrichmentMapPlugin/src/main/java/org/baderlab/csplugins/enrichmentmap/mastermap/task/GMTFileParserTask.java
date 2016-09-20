package org.baderlab.csplugins.enrichmentmap.mastermap.task;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.baderlab.csplugins.enrichmentmap.mastermap.model.GeneSet;

public class GMTFileParserTask extends FutureTask<List<GeneSet>> {

	
	public GMTFileParserTask(Path filePath, String datasetName) {
		super(filePath, datasetName);
	}


	@Override
	public void parse(Path filePath, CompletableFuture<List<GeneSet>> future) throws IOException, InterruptedException {
		List<GeneSet> geneSets = new ArrayList<>();
		
		try(BufferedReader reader = new BufferedReader(new FileReader(filePath.toString()))) {
		    for(String line; (line = reader.readLine()) != null;) {
		    	if(cancelled) {
					throw new InterruptedException();
		    	}
		    	GeneSet geneSet = readGeneSet(line);
		    	if(geneSet != null) {
		    		geneSets.add(geneSet);
		    	}
		    }
		}
		
		future.complete(geneSets);
	}


	private GeneSet readGeneSet(String line) {
		String[] tokens = line.split("\t");
		//only go through the lines that have at least a gene set name and description.
		if(tokens.length >= 2) {
			Set<String> genes = new HashSet<>();
			
			String name = tokens[0].toUpperCase().trim();
			String description = tokens[1].trim();
			
			for(int i = 2; i < tokens.length; i++) {
				String gene = tokens[i];
				if(!gene.isEmpty()) {
					genes.add(gene.toUpperCase());
				}
			}
			return new GeneSet(name, description, genes);
		}
		return null;
	}


}
