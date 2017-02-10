package org.baderlab.csplugins.enrichmentmap.view.expression;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import javax.swing.table.AbstractTableModel;

import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpression;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpressionMatrix;


/**
 * MKTODO GeneExpressionMatrix is still kind of a mess. There are several methods in this class that
 * really should be in GeneExpressionMatrix. Refactoring should be done eventually.
 */
@SuppressWarnings("serial")
public class ExpressionTableModel extends AbstractTableModel {

	private final ExpressionViewerParams params;
	private final EnrichmentMap map;
	private final List<String> genes;
	private final int colCount;
	
	private final NavigableMap<Integer,DataSet> colToDataSet = new TreeMap<>();
	
	public ExpressionTableModel(ExpressionViewerParams params, EnrichmentMap map, List<String> genes) {
		this.params = params;
		this.map = map;
		this.genes = genes;
		
		// populate colToDataSet
		int rangeFloor = 1; // because col 0 is gene name
		List<DataSet> datasets = map.getDatasetList();
		colToDataSet.put(0, null);
		for(DataSet dataset : datasets) {
			GeneExpressionMatrix matrix = dataset.getExpressionSets();
			rangeFloor += getNumCols(matrix);
			colToDataSet.put(rangeFloor, dataset);
		}
		colCount = rangeFloor;
	}
	

	@Override
	public int getRowCount() {
		return genes.size();
	}

	@Override
	public int getColumnCount() {
		return colCount;
	}
	
	@Override
	public String getColumnName(int col) {
		if(col == 0)
			return "Gene";
		DataSet dataset = getDataSet(col);
		int index = getIndex(col) + 2;
		String[] columns = dataset.getExpressionSets().getColumnNames();
		return columns[index];
	}

	@Override
	public Object getValueAt(int row, int col) {
		String gene = genes.get(row);
		if(col == 0)
			return gene;
		
		int geneID = map.getHashFromGene(gene);
		DataSet dataset = getDataSet(col);
		int index = getIndex(col);
		Double[] vals = getExpression(dataset, geneID);
		return vals[index];
	}
	
	@Override
	public Class<?> getColumnClass(int col) {
		if(col == 0)
			return String.class;
		else
			return Double.class;
	}
	
	private int getIndex(int col) {
		int start = colToDataSet.floorKey(col);
		return col - start - 1;
	}
	
	public DataSet getDataSet(int col) {
		return colToDataSet.ceilingEntry(col).getValue();
	}
	
	private static int getNumCols(GeneExpressionMatrix matrix) {
		// ugh! so ugly
		return matrix.getExpressionMatrix().values().iterator().next().getExpression().length - 2;
	}

	
	private Double[] getExpression(DataSet dataset, int geneID) {
		GeneExpressionMatrix matrix = dataset.getExpressionSets();
		Map<Integer,GeneExpression> expressions = matrix.getExpressionMatrix();
		GeneExpression row = expressions.get(geneID);
		
		Double[] values = null;
		switch(params.getTransform()) {
			case ROW_NORMALIZE:  values = row.rowNormalize();    break;
			case LOG_TRANSFORM:  values = row.rowLogTransform(); break;
			case AS_IS:          values = row.getExpression();   break;
		}
		if(values == null) {
			values = new Double[matrix.getNumConditions() - 2];
			Arrays.fill(values, Double.NaN);
		}
		return values;
	}
}
