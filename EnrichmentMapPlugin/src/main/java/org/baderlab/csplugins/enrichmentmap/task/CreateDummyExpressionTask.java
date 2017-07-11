package org.baderlab.csplugins.enrichmentmap.task;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpression;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpressionMatrix;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

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
	private void createDummyExpression() {
		EnrichmentMap map = dataset.getMap();
		
		GeneExpressionMatrix expressionMatrix = new GeneExpressionMatrix();
		expressionMatrix.setColumnNames(new String[] {"Name", "Description", "Dummy"});
		expressionMatrix.setNumConditions(3);
		
		// Not worried too much about sharing dummy expressions as they don't use up very much space.
		String expressionKey = "Dummy_" + UUID.randomUUID().toString();
		map.putExpressionMatrix(expressionKey, expressionMatrix);
		dataset.setExpressionKey(expressionKey);
		
		Set<Integer> allGenes = dataset.getGeneSetGenes();
		Set<Integer> enrichmentGenes = dataset.getEnrichmentGenes();
		
		Map<Integer,GeneExpression> expression = expressionMatrix.getExpressionMatrix();

		for(int geneKey : allGenes) {
			float dummyVal = enrichmentGenes.contains(geneKey) ? 0.25f : 0.0f;
			String geneName = map.getGeneFromHashKey(geneKey);
			GeneExpression expres = new GeneExpression(geneName, geneName, dummyVal);
			expression.put(geneKey, expres);
		}
	}

	@Override
	public void run(TaskMonitor taskMonitor) {
		createDummyExpression();
	}

}
