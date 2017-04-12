package org.baderlab.csplugins.enrichmentmap.view.heatmap.table;

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
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParams.Transform;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;

import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;



@SuppressWarnings("serial")
public class HeatMapTableModel extends AbstractTableModel {

	/** Number of columns at the start that don't show expression data (ie. gene name etc..) */
	public static final int DESC_COL_COUNT = 2; 
	public static final int GENE_COL = 0;
	public static final int RANK_COL = 1;
	
	private final EnrichmentMap map;
	
	// Uncompressed column count
	private final int colCount;
	private final NavigableMap<Integer,EMDataSet> colToDataSet = new TreeMap<>();
	
	private List<String> genes;
	private Transform transform;
	private Map<Integer,RankValue> ranking;
	private String ranksColName = "Ranks";

	
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
			rangeFloor += matrix.getNumConditions() - 2;
		}
		colCount = rangeFloor;
	}
	
	
	public void setTransform(Transform transform) {
		boolean c1 = this.transform.isCompress();
		boolean c2 = transform.isCompress();
		this.transform = transform;
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
		if(transform.isCompress())
			return map.getDataSetCount() + DESC_COL_COUNT;
		else
			return colCount;
	}
	
	@Override
	public String getColumnName(int col) {
		if(col == GENE_COL)
			return "Gene";
		if(col == RANK_COL)
			return ranksColName;
		
		EMDataSet dataset = getDataSet(col);
		if(transform.isCompress()) {
			return SwingUtil.abbreviate(dataset.getName(), 40);
		}
		else {
			int index = getIndexInDataSet(col) + 2;
			String[] columns = dataset.getExpressionSets().getColumnNames();
			return columns[index];
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
		EMDataSet dataset = getDataSet(col);
		if(transform.isCompress()) {
			return getCompressedExpression(dataset, geneID, transform);
		} else {
			double[] vals = getExpression(dataset, geneID, transform);
			return vals == null ? Double.NaN : vals[getIndexInDataSet(col)];
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
			case RANK_COL: return RankValue.class;
			default:       return Double.class;
		}
	}
	
	public boolean hasSignificantRanks() {
		if(ranking == null)
			return false;
		return ranking.values().stream().anyMatch(RankValue::isSignificant);
	}
	
	public Optional<String> getPhenotype(int col) {
		if(transform.isCompress())
			return Optional.empty();
		EMDataSet dataset = getDataSet(col);
		int index = getIndexInDataSet(col);
		String[] classes = dataset.getExpressionSets().getPhenotypes();
		if(classes != null && index < classes.length) {
			return Optional.ofNullable(classes[index]);
		}
		return Optional.empty();
	}
	
	private int getIndexInDataSet(int col) {
		int start = colToDataSet.floorKey(col);
		return col - start;
	}
	
	public EMDataSet getDataSet(int col) {
		if(transform.isCompress()) {
			List<EMDataSet> datasets = map.getDataSetList();
			return datasets.get(col-DESC_COL_COUNT);
		}
		else
			return colToDataSet.floorEntry(col).getValue();
	}
	
	
	private static double getCompressedExpression(EMDataSet dataset, int geneID, Transform transform) {
		double[] vals = getExpression(dataset, geneID, Transform.AS_IS);
		if(vals == null)
			return Double.NaN;
		DoubleArrayList doubleArrayList = new DoubleArrayList(vals);
		doubleArrayList.sort();
		switch(transform) {
			case COMPRESS_MEDIAN: return Descriptive.median(doubleArrayList);
			case COMPRESS_MAX:    return Descriptive.max(doubleArrayList);
			case COMPRESS_MIN:    return Descriptive.min(doubleArrayList);
			default:              return Double.NaN;
		}
	}
	
	@SuppressWarnings("incomplete-switch")
	private static @Nullable double[] getExpression(EMDataSet dataset, int geneID, Transform transform) {
		GeneExpressionMatrix matrix = dataset.getExpressionSets();
		Map<Integer,GeneExpression> expressions = matrix.getExpressionMatrix();
		GeneExpression row = expressions.get(geneID);
		if(row != null) {
			switch(transform) {
				case ROW_NORMALIZE: return row.rowNormalize();
				case LOG_TRANSFORM: return row.rowLogTransform();
				case AS_IS:         return row.getExpression();
			}
		}
		return null;
	}
}
