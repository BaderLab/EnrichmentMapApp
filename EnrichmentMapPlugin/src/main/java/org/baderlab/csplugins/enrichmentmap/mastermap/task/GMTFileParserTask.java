package org.baderlab.csplugins.enrichmentmap.mastermap.task;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.baderlab.csplugins.enrichmentmap.mastermap.model.GeneSet;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class GMTFileParserTask extends AbstractTask {

	private final Path filePath;
	private final CompletableFuture<List<GeneSet>> future;
	
	
	public GMTFileParserTask(Path filePath) {
		this.filePath = filePath;
		this.future = new CompletableFuture<>();
	}

	public CompletableFuture<List<GeneSet>> ask() {
		return future;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws IOException, InterruptedException {
		List<GeneSet> geneSets = new ArrayList<>();
		Path path = filePath.resolve(Paths.get("edb/gene_sets.gmt"));
		
		int length = path.getNameCount();
		Path endPart = path.subpath(length - 3, length - 0);
		taskMonitor.setStatusMessage("Parsing ..." + endPart);
		
		try(BufferedReader reader = new BufferedReader(new FileReader(path.toString()))) {
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
