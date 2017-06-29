package org.baderlab.csplugins.enrichmentmap.task;

import java.util.Map;
import java.util.UUID;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
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
		// Not worried too much about sharing dummy expressions as they don't use up very much space.
		GeneExpressionMatrix expressionMatrix = new GeneExpressionMatrix();
		String expressionKey = "Dummy_" + UUID.randomUUID().toString();
		dataset.getMap().putExpressionMatrix(expressionKey, expressionMatrix);
		dataset.setExpressionKey(expressionKey);
		
		//in order to see the gene in the expression viewer we also need a dummy expression file get all the genes
		Map<String, Integer> genes = dataset.getMap().getGeneSetsGenes(dataset.getSetOfGeneSets().getGeneSets().values());

		String[] titletokens = {"Name", "Description", "Dummy"};
		expressionMatrix.setColumnNames(titletokens);
		Map<Integer, GeneExpression> expression = expressionMatrix.getExpressionMatrix();
		expressionMatrix.setExpressionMatrix(expression);

		String[] tokens = {"tmp", "tmp", "0.25"};

		for(String currentGene : genes.keySet()) {
			int genekey = genes.get(currentGene);

			GeneExpression expres = new GeneExpression(currentGene, currentGene);
			expres.setExpression(tokens);

			double newMax = expres.newMax(expressionMatrix.getMaxExpression());
			if(newMax != -100)
				expressionMatrix.setMaxExpression(newMax);
			double newMin = expres.newMin(expressionMatrix.getMinExpression());
			if(newMin != -100)
				expressionMatrix.setMinExpression(newMin);
			double newClosest = expres.newclosesttoZero(expressionMatrix.getClosesttoZero());
			if(newClosest != -100)
				expressionMatrix.setClosesttoZero(newClosest);

			expression.put(genekey, expres);
		}

		//set the number of genes
		//expressionMatrix.setNumGenes(expressionMatrix.getExpressionMatrix().size());
		expressionMatrix.setNumConditions(3);
		expressionMatrix.setFilename("Dummy Expression_" + dataset.getName().toString());
	}

	
	public void run(TaskMonitor taskMonitor) {
		createDummyExpression();
	}

}
