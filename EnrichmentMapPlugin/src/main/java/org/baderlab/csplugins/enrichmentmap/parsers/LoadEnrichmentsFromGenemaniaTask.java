package org.baderlab.csplugins.enrichmentmap.parsers;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.baderlab.csplugins.enrichmentmap.model.GenemaniaParameters;
import org.baderlab.csplugins.enrichmentmap.model.GenericResult;
import org.baderlab.csplugins.enrichmentmap.model.SetOfEnrichmentResults;
import org.baderlab.csplugins.enrichmentmap.view.creation.genemania.GenemaniaAnnotation;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

public class LoadEnrichmentsFromGenemaniaTask extends AbstractTask {

	private final GenemaniaParameters genemaniaParams;
	private final EMDataSet dataset;
	
	public LoadEnrichmentsFromGenemaniaTask(GenemaniaParameters genemaniaParams, EMDataSet dataset) {
		this.genemaniaParams = genemaniaParams;
		this.dataset = dataset;
	}

	@Override
	public void run(TaskMonitor tm) {
		tm.setStatusMessage("Loading enrichment data from Genemania network.");
		tm.setProgress(0.0);
		
		CyNetwork genemaniaNetwork = genemaniaParams.getNetwork();
		
		Map<String,Set<String>> geneSets = computeGeneSets(genemaniaNetwork);
		if(geneSets == null || geneSets.isEmpty())
			throw new RuntimeException("Gene set data not found in Genemania network.");
		
		tm.setProgress(0.3);
		
		Map<String,GenemaniaAnnotation> annotations = parseAnnotationJson(genemaniaNetwork);
		if(annotations == null || annotations.isEmpty())
			throw new RuntimeException("Annotation data not found in Genemania network.");
		
		tm.setProgress(0.7);
		
		createDataSet(genemaniaNetwork, geneSets, annotations);
		dataset.getMap().getParams().setFDR(true);
		
		tm.setProgress(1.0);
	}
	
	
	private void createDataSet(CyNetwork genemaniaNetwork, Map<String,Set<String>> geneSets, Map<String,GenemaniaAnnotation> annotations) {
		EnrichmentMap map = dataset.getMap();
		SetOfEnrichmentResults enrichments = dataset.getEnrichments();
		Map<String,GeneSet> genesets = dataset.getSetOfGeneSets().getGeneSets();
		
		for(Map.Entry<String, Set<String>> entry : geneSets.entrySet()) {
			String name = entry.getKey();
			Set<String> genes = entry.getValue();
			
			
			GenemaniaAnnotation annotation = annotations.get(name);
			if(annotation == null)
				continue;
			
			// Build the GeneSet object
			ImmutableSet.Builder<Integer> builder = ImmutableSet.builder();
			
			for(String gene : genes) {
				Integer hash = map.addGene(gene);
				if(hash != null)
					builder.add(hash);
			}
			
			GeneSet gs = new GeneSet(annotation.getName(), name, builder.build());
			int gsSize = gs.getGenes().size();
			genesets.put(name, gs);
			
			GenericResult result = new GenericResult(annotation.getName(), name, 1.0, gsSize, annotation.getqValue());
			enrichments.getEnrichments().put(name, result);
		}
	}
	

	private Map<String,Set<String>> computeGeneSets(CyNetwork genemaniaNetwork) {
		CyTable nodeTable = genemaniaNetwork.getDefaultNodeTable();
		Map<String,Set<String>> geneSets = new HashMap<>();
		
		for(CyRow row : nodeTable.getAllRows()) {
			String gene = row.get("gene name", String.class);
			List<String> annotations = row.getList("annotation name", String.class);
			
			if(gene != null && annotations != null && !annotations.isEmpty()) {
				for(String annotation : annotations) {
					geneSets.computeIfAbsent(annotation, k -> new HashSet<>()).add(gene);
				}
			}
		}
		
		return geneSets;
	}
	
	
	private Map<String,GenemaniaAnnotation> parseAnnotationJson(CyNetwork genemaniaNetwork) {
		CyTable table = genemaniaNetwork.getDefaultNetworkTable();
		
		CyRow row = table.getRow(genemaniaNetwork.getSUID());
		if(row == null)
			return null;
		
		String jsonString = row.get("annotations", String.class);
		if(jsonString == null)
			return null;
		
		try {
			Gson gson = new GsonBuilder().create();
			Type listType = new TypeToken<ArrayList<GenemaniaAnnotation>>(){}.getType();
			List<GenemaniaAnnotation> fromJson = gson.fromJson(jsonString, listType);
			Map<String,GenemaniaAnnotation> annotations = new HashMap<>();
			for(GenemaniaAnnotation a : fromJson) {
				annotations.put(a.getDescription(), a);
			}
			return annotations;
		} catch(JsonParseException e) {
			return null;
		}
	}

}
