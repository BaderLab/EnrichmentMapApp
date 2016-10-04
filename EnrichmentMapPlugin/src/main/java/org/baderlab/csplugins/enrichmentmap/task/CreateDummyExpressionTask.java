package org.baderlab.csplugins.enrichmentmap.task;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpression;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpressionMatrix;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

/* When there is no expression provided create a fake expression matrix so you can 
 * still view the genes associated with genesets in the heatmap panel
 */
public class CreateDummyExpressionTask extends AbstractTask {

	private DataSet dataset;
	private TaskMonitor taskMonitor;

	public CreateDummyExpressionTask(DataSet dataset) {
		super();
		this.dataset = dataset;
	}

	//Create a dummy expression file so that when no expression files are loaded you can still
	//use the intersect and union viewers.
	private void createDummyExpression() {
		//in order to see the gene in the expression viewer we also need a dummy expression file
		//get all the genes
		//HashMap<String, Integer> genes= dataset.getMap().getGenes();
		Set<Integer> datasetGenes;

		Map<String, Integer> genes = dataset.getMap().getGenesetsGenes(dataset.getSetofgenesets().getGenesets().values());
		datasetGenes = dataset.getDatasetGenes();

		String[] titletokens = new String[3];
		titletokens[0] = "Name";
		titletokens[1] = "Description";
		titletokens[2] = "Dummy Expression";

		GeneExpressionMatrix expressionMatrix = dataset.getExpressionSets();
		expressionMatrix.setColumnNames(titletokens);
		Map<Integer, GeneExpression> expression = expressionMatrix.getExpressionMatrix();
		expressionMatrix.setExpressionMatrix(expression);

		String[] tokens = new String[3];
		tokens[0] = "tmp";
		tokens[1] = "tmp";
		tokens[2] = "0.25";

		for(Iterator i = genes.keySet().iterator(); i.hasNext();) {
			String currentGene = (String) i.next();

			int genekey = genes.get(currentGene);
			if(datasetGenes != null)
				datasetGenes.add(genekey);

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

		//set that there is data for the expression viewer
		dataset.getMap().getParams().setData(true);
	}

	public void run(TaskMonitor taskMonitor) throws Exception {
		this.taskMonitor = taskMonitor;
		createDummyExpression();

	}

}
