package org.baderlab.csplugins.enrichmentmap.task;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet.Method;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpression;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpressionMatrix;
import org.baderlab.csplugins.enrichmentmap.parsers.ParseGenericEnrichmentsForDummy;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.common.base.Strings;

/**
 * When there is no expression provided create a fake expression matrix so you can 
 * still view the genes associated with genesets in the heatmap panel.
 */
public class CreateDummyExpressionTask extends AbstractTask {

	private final EMDataSet dataset;

	public CreateDummyExpressionTask(EMDataSet dataset) {
		this.dataset = dataset;
	}

	//Create a dummy expression file so that when no expression files are loaded you can still use the HeatMap.
	private void createDummyExpression() throws IOException {
		EnrichmentMap map = dataset.getMap();
		GeneExpressionMatrix expressionMatrix = new GeneExpressionMatrix();
		expressionMatrix.setColumnNames(new String[] {"Name", "Description", "Dummy"});
		expressionMatrix.setNumConditions(3);
		
		// Not worried too much about sharing dummy expressions as they don't use up very much space.
		String expressionKey = "Dummy_" + UUID.randomUUID().toString();
		map.putExpressionMatrix(expressionKey, expressionMatrix);
		dataset.setExpressionKey(expressionKey);
		
		Map<Integer,GeneExpression> expression = expressionMatrix.getExpressionMatrix();
		
		Set<Integer> genes = getGenes();
		for(int geneKey : genes) {
			String geneName = map.getGeneFromHashKey(geneKey);
			GeneExpression expres = new GeneExpression(geneName, geneName, 0.25f);
			expression.put(geneKey, expres);
		}
		
	}
	
	private Set<Integer> getGenes() throws IOException {
		String enrFileName = dataset.getDataSetFiles().getEnrichmentFileName1();
		if(dataset.getMethod() == Method.Generic && !Strings.isNullOrEmpty(enrFileName)) { // generic or gprofiler
			ParseGenericEnrichmentsForDummy parseTask = new ParseGenericEnrichmentsForDummy(enrFileName);
			parseTask.run(null);
			Set<String> geneNames = parseTask.getEnrichmentFileGenes();
			return geneNames.stream()
				.map(dataset.getMap()::getHashFromGene)
				.filter(gene -> gene != null)
				.collect(Collectors.toSet());
		} else {
			return dataset.getGeneSetGenes(); 
		}
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		createDummyExpression();
	}

}
