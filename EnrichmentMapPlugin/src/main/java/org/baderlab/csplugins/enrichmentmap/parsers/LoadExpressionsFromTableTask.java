package org.baderlab.csplugins.enrichmentmap.parsers;

import java.util.Collection;
import java.util.Map;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpression;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpressionMatrix;
import org.baderlab.csplugins.enrichmentmap.model.TableExpressionParameters;
import org.baderlab.csplugins.enrichmentmap.util.DiscreteTaskMonitor;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class LoadExpressionsFromTableTask extends AbstractTask {

	private final TableExpressionParameters expressionParams;
	private final EMDataSet dataset;

	
	public LoadExpressionsFromTableTask(TableExpressionParameters expressionParams, EMDataSet dataset) {
		this.expressionParams = expressionParams;
		this.dataset = dataset;
	}

	
	@Override
	public void run(TaskMonitor taskMonitor) {
		EnrichmentMap map = dataset.getMap();
		
		GeneExpressionMatrix expressionMatrix = new GeneExpressionMatrix();
		final String expressionKey = "expressionKey"; // only needs to be unique within the DataSet
		dataset.setExpressionKey(expressionKey);
		map.putExpressionMatrix(expressionKey, expressionMatrix);
		Map<Integer,GeneExpression> expressionMap = expressionMatrix.getExpressionMatrix();
		
		CyTable table = expressionParams.getExprTable();
		Collection<CyRow> rows = table.getAllRows();
		
		String[] valCols = expressionParams.getExprValueColumns();
		
		expressionMatrix.setColumnNames(getColNames(valCols));
		expressionMatrix.setNumConditions(expressionMatrix.getColumnNames().length);
		
		int expressionUniverse = 0;
		
		DiscreteTaskMonitor tm = getDiscreteTaskMonitor(taskMonitor, rows.size());
		
		for(CyRow row : table.getAllRows()) {
			String geneName = row.get(expressionParams.getExprGeneNameColumn(), String.class);
			
			Integer geneKey = map.getHashFromGene(geneName);
			if(geneKey != null) {
				String description = "";
				String descCol = expressionParams.getExprDescriptionColumn();
				if(descCol != null) {
					description = row.get(descCol, String.class);
				}
				
				float[] expressionValues = new float[valCols.length];
				for(int i = 0; i < valCols.length; i++) {
					Double val = row.get(valCols[i], Double.class);
					expressionValues[i] = val == null ? Float.NaN : val.floatValue();
				}
				
				GeneExpression geneExpr = new GeneExpression(geneName, description, expressionValues);
				expressionMap.put(geneKey, geneExpr);
				expressionUniverse++;
			}
			
			tm.inc();
		}
		
		expressionMatrix.setExpressionUniverse(expressionUniverse);
	}

	
	private String[] getColNames(String[] valCols) {
		// The ExpressionFileReaderTask includes the names of the gene and description columns in the list of columns.
		// Keep it this way to avoid problems with legacy sessions.
		String[] colNames = new String[valCols.length + 2];
		colNames[0] = "Gene";
		colNames[1] = "Description";
		System.arraycopy(valCols, 0, colNames, 2, valCols.length);
		return colNames;
	}
	
	
	private static DiscreteTaskMonitor getDiscreteTaskMonitor(TaskMonitor delegate, int size) {
		DiscreteTaskMonitor tm = new DiscreteTaskMonitor(delegate, size);
		tm.setStatusMessage("Processing table for expression data - " + size + " rows");
		tm.setTitle("Loading Expression Data");
		return tm;
	}
}
