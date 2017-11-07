package org.baderlab.csplugins.enrichmentmap.view.heatmap.table;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

import javax.annotation.Nullable;
import javax.swing.table.AbstractTableModel;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpression;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpressionMatrix;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParams.Compress;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParams.Transform;



@SuppressWarnings("serial")
public class HeatMapTableModel extends AbstractTableModel {

	/** Number of columns at the start that don't show expression data (ie. gene name etc..) */
	public static final int DESC_COL_COUNT = 3; 
	public static final int GENE_COL = 0;
	public static final int DESC_COL = 1;
	public static final int RANK_COL = 2;

	private final EnrichmentMap map;
	private final List<EMDataSet> datasets;
	
	// Uncompressed column count
	private final int colCount;
	private final NavigableMap<Integer,EMDataSet> colToDataSet = new TreeMap<>();
	
	private List<String> genes;
	private Transform transform;
	private Compress compress;
	private Map<Integer,RankValue> ranking;
	private String ranksColName = "Ranks";

	
	public HeatMapTableModel(EnrichmentMap map, Map<Integer,RankValue> ranking, List<String> genes, Transform transform, Compress compress) {
		this.transform = transform;
		this.compress = compress;
		this.map = map;
		this.ranking = ranking;
		this.genes = genes;
		
		// populate colToDataSet
		int rangeFloor = DESC_COL_COUNT;
		
		if(map.isCommonExpressionValues()) {
			// if all the expression sets are the same then just show one of them
			this.datasets = map.getDataSetList().subList(0, 1);
		} else {
			this.datasets = map.getDataSetList();
		}
		
		colToDataSet.put(0, null);
		for(EMDataSet dataset : datasets) {
			GeneExpressionMatrix matrix = dataset.getExpressionSets();
			colToDataSet.put(rangeFloor, dataset);
			rangeFloor += matrix.getNumConditions() - 2;
		}
		colCount = rangeFloor;
	}
	
	
	public List<EMDataSet> getDataSets() {
		return Collections.unmodifiableList(datasets);
	}
	
	public void setTransform(Transform transform, Compress compress) {
		boolean c1 = this.compress.isNone();
		boolean c2 = compress.isNone();
		this.transform = transform;
		this.compress = compress;
		if(c1 != c2)
			fireTableStructureChanged();
		fireTableDataChanged();
	}

	public void setRanking(String ranksColName, Map<Integer,RankValue> ranking) {
		this.ranksColName = Objects.requireNonNull(ranksColName);
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
	
	public Compress getCompress() {
		return compress;
	}
	
	public EnrichmentMap getEnrichmentMap() {
		return map;
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
		if(compress.isNone())
			return colCount;
		else
			return datasets.size() + DESC_COL_COUNT;
	}
	
	@Override
	public String getColumnName(int col) {
		if(col == GENE_COL)
			return "Gene";
		if(col == DESC_COL)
			return "Description";
		if(col == RANK_COL)
			return ranksColName;
		
		EMDataSet dataset = getDataSet(col);
		if(compress.isNone()) {
			int index = getIndexInDataSet(col) + 2;
			String[] columns = dataset.getExpressionSets().getColumnNames();
			return columns[index];
		} else {
			if(map.isDistinctExpressionSets())
				return dataset.getName();
			else
				return "Expressions";
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		if(row < 0)
			return null; // Why is it passing -1?
		
		if(col == RANK_COL)
			return getRankValue(row);
		
		String gene = genes.get(row);
		if(col == GENE_COL)
			return gene;
		
		int geneID = map.getHashFromGene(gene);
		if(col == DESC_COL)
			return getDescription(geneID);
		
		EMDataSet dataset = getDataSet(col);
		
		// cast expressions to double, because most of the code in this package is based on doubles and I don't want to change it
		if(compress.isNone()) {
			float[] vals = getExpression(dataset, geneID, transform);
			return vals == null ? Double.NaN : (double) vals[getIndexInDataSet(col)];
		} else {
			return (double) getCompressedExpression(dataset, geneID, transform, compress);
		}
	}
	
	public RankValue getRankValue(int row) {
		// Use empty RankValue objects for missing genes instead of nulls so that they sort last (see RankValue.compareTo()).
		if(ranking == null)
			return RankValue.EMPTY;
		String gene = genes.get(row);
		int geneID = map.getHashFromGene(gene);
		return ranking.getOrDefault(geneID, RankValue.EMPTY);
	}
	
	@Override
	public Class<?> getColumnClass(int col) {
		switch(col) {
			case GENE_COL: return String.class;
			case DESC_COL: return String.class;
			case RANK_COL: return RankValue.class;
			 // most of the existing code uses double, so cast expression values to double to avoid making big changes
			default:       return Double.class;
		}
	}
	
	public boolean hasSignificantRanks() {
		if(ranking == null)
			return false;
		return ranking.values().stream().anyMatch(RankValue::isSignificant);
	}
	
	public Optional<String> getPhenotype(int col) {
		if(compress.isNone()) {
			EMDataSet dataset = getDataSet(col);
			int index = getIndexInDataSet(col);
			String[] classes = dataset.getEnrichments().getPhenotypes();
			if(classes != null && index < classes.length) {
				return Optional.ofNullable(classes[index]);
			}
		}
		return Optional.empty();
	}
	
	private int getIndexInDataSet(int col) {
		int start = colToDataSet.floorKey(col);
		return col - start;
	}
	
	public EMDataSet getDataSet(int col) {
		if(compress.isNone()) {
			return colToDataSet.floorEntry(col).getValue();
		} else {
			return datasets.get(col-DESC_COL_COUNT);
		}
	}
	
	
	private static float getCompressedExpression(EMDataSet dataset, int geneID, Transform transform, Compress compress) {
		float[] expression = getExpression(dataset, geneID, transform);
		if(expression == null)
			return Float.NaN;
		
		switch(compress) {
			case MEDIAN: return GeneExpression.median(expression);
			case MAX:    return GeneExpression.max(expression);
			case MIN:    return GeneExpression.min(expression);
			default:     return Float.NaN;
		}
	}
	
	private String getDescription(int geneID) {
		for(EMDataSet dataset : datasets) {
			GeneExpression row = getGeneExpression(dataset, geneID);
			if(row != null)
				return row.getDescription();
		}
		return null;
	}
	
	private static GeneExpression getGeneExpression(EMDataSet dataset, int geneID) {
		GeneExpressionMatrix matrix = dataset.getExpressionSets();
		Map<Integer,GeneExpression> expressions = matrix.getExpressionMatrix();
		GeneExpression row = expressions.get(geneID);
		return row;
	}
	
	private static @Nullable float[] getExpression(EMDataSet dataset, int geneID, Transform transform) {
		GeneExpression expression = getGeneExpression(dataset, geneID);
		if(expression != null) {
			switch(transform) {
				case ROW_NORMALIZE: return expression.rowNormalize();
				case LOG_TRANSFORM: return expression.rowLogTransform();
				case AS_IS:         return expression.getExpression();
			}
		}
		return null;
	}
}
