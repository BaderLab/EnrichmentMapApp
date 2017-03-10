package org.baderlab.csplugins.enrichmentmap.view.heatmap.table;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import javax.swing.table.AbstractTableModel;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpression;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpressionMatrix;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParams.Transform;


/**
 * MKTODO GeneExpressionMatrix is still kind of a mess. There are several methods in this class that
 * really should be in GeneExpressionMatrix. Refactoring should be done eventually.
 */
@SuppressWarnings("serial")
public class HeatMapTableModel extends AbstractTableModel {

	/** Number of columns at the start that don't show expression data (ie. gene name etc..) */
	public static final int DESC_COL_COUNT = 2; 
	public static final int GENE_COL = 0;
	public static final int RANK_COL = 1;
	
	private final EnrichmentMap map;
	private final int colCount;
	private final NavigableMap<Integer,EMDataSet> colToDataSet = new TreeMap<>();
	
	private List<String> genes;
	private Transform transform;
	private Map<Integer,RankValue> ranking;

	
	public HeatMapTableModel(EnrichmentMap map, Map<Integer,RankValue> ranking, List<String> genes, Transform transform) {
		this.transform = transform;
		this.map = map;
		this.ranking = ranking;
		this.genes = genes;
		
		// populate colToDataSet
		int rangeFloor = DESC_COL_COUNT;
		List<EMDataSet> datasets = map.getDataSetList();
		colToDataSet.put(0, null);
		for(EMDataSet dataset : datasets) {
			GeneExpressionMatrix matrix = dataset.getExpressionSets();
			colToDataSet.put(rangeFloor, dataset);
			rangeFloor += getNumCols(matrix);
		}
		colCount = rangeFloor;
	}
	
	
	public void setTransform(Transform transform) {
		this.transform = transform;
		fireTableDataChanged();
	}

	public void setRanking(Map<Integer,RankValue> ranking) {
		this.ranking = ranking;
		fireTableDataChanged();
	}
	
	public void setGenes(List<String> genes) {
		this.genes = genes;
		fireTableDataChanged();
	}
	
	public Transform getTransform() {
		return transform;
	}
	
	
	public String getGene(int row) {
		return genes.get(row);
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
		if(col == GENE_COL)
			return "Gene";
		if(col == RANK_COL)
			return "Ranks";
		
		EMDataSet dataset = getDataSet(col);
		int index = getIndex(col) + 2;
		String[] columns = dataset.getExpressionSets().getColumnNames();
		return columns[index];
	}

	@Override
	public Object getValueAt(int row, int col) {
		if(row < 0)
			return null; // Why is it passing -1?
		String gene = genes.get(row);
		if(col == GENE_COL)
			return gene;
		int geneID = map.getHashFromGene(gene);
		
		// Use empty RankValue objects for missing genes instead of nulls so that they sort last (see RankValue.compareTo()).
		if(col == RANK_COL)
			return (ranking == null) ? RankValue.EMPTY : ranking.getOrDefault(geneID, RankValue.EMPTY);
		
		EMDataSet dataset = getDataSet(col);
		int index = getIndex(col);
		double[] vals = getExpression(dataset, geneID);
		return vals[index];
	}
	
	@Override
	public Class<?> getColumnClass(int col) {
		switch(col) {
			case GENE_COL: return String.class;
			case RANK_COL: return RankValue.class;
			default:       return Double.class;
		}
	}
	
	private int getIndex(int col) {
		int start = colToDataSet.floorKey(col);
		return col - start;
	}
	
	public EMDataSet getDataSet(int col) {
		return colToDataSet.floorEntry(col).getValue();
	}
	
	private static int getNumCols(GeneExpressionMatrix matrix) {
		return matrix.getNumConditions() - 2;
	}

	
	private double[] getExpression(EMDataSet dataset, int geneID) {
		GeneExpressionMatrix matrix = dataset.getExpressionSets();
		Map<Integer,GeneExpression> expressions = matrix.getExpressionMatrix();
		GeneExpression row = expressions.get(geneID);
		
		double[] values = null;
		if(row != null) {
			switch(transform) {
				case ROW_NORMALIZE:  values = row.rowNormalize();    break;
				case LOG_TRANSFORM:  values = row.rowLogTransform(); break;
				case AS_IS:          values = row.getExpression();   break;
			}
		}
		if(values == null) {
			values = new double[matrix.getNumConditions() - 2];
			Arrays.fill(values, Double.NaN);
		}
		return values;
	}
}
