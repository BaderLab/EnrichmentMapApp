package org.baderlab.csplugins.enrichmentmap.view.heatmap.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nullable;
import javax.swing.table.AbstractTableModel;

import org.apache.commons.lang3.tuple.Pair;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpression;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpressionMatrix;
import org.baderlab.csplugins.enrichmentmap.model.SetOfEnrichmentResults;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParams.Compress;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParams.Transform;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;



@SuppressWarnings("serial")
public class HeatMapTableModel extends AbstractTableModel {

	/** Number of columns at the start that don't show expression data (ie. gene name etc..) */
	public static final int DESC_COL_COUNT = 3; 
	public static final int GENE_COL = 0;
	public static final int DESC_COL = 1;
	public static final int RANK_COL = 2;

	private final EnrichmentMap map;
	
	private final List<EMDataSet> datasets;
	private Map<Compress,ExpressionData> data = new EnumMap<>(Compress.class);
	
	private List<String> genes;
	private Transform transform;
	private Compress compress;
	private Map<Integer,RankValue> ranking;
	private String ranksColName = "Ranks";

	
	private interface ExpressionData {
		EMDataSet getDataSet(int col);
		double getValue(int geneID, int col);
		String getColumnName(int col);
		public default Optional<String> getPhenotype(int col) { return Optional.empty(); };
		int getSize();
		public default void transformChanged() {};
	}
	
	
	private class Uncompressed implements ExpressionData {

		// for Compress.NONE, but storing the expression data again would use too much memory, so we access through the EMDataSet object
		private final NavigableMap<Integer,EMDataSet> colToDataSet = new TreeMap<>();
		private final int expressionCount;
		private final Cache<Pair<Integer,EMDataSet>, float[]> cache;
		
		public Uncompressed(List<EMDataSet> datasets) {
			int rangeFloor = DESC_COL_COUNT;
			colToDataSet.put(0, null);
			for(EMDataSet dataset : datasets) {
				GeneExpressionMatrix matrix = dataset.getExpressionSets();
				colToDataSet.put(rangeFloor, dataset);
				rangeFloor += matrix.getNumConditions() - 2;
			}
			expressionCount = rangeFloor - DESC_COL_COUNT;
			
			cache = CacheBuilder.newBuilder().maximumSize(20).build();
		}
		
		public EMDataSet getDataSet(int col) {
			return colToDataSet.floorEntry(col).getValue();
		}
		
		private int getIndexInDataSet(int col) {
			int start = colToDataSet.floorKey(col);
			return col - start;
		}
		
		@Override
		public double getValue(int geneID, int col) { // col value 
			EMDataSet dataset = getDataSet(col);
			
			try {
				float vals[] = cache.get(Pair.of(geneID, dataset), () -> getExpression(dataset, geneID, transform));
				return vals == null ? Double.NaN : (double) vals[getIndexInDataSet(col)];
			} catch (ExecutionException e) {
				return Double.NaN;
			}
		}

		@Override
		public String getColumnName(int col) {
			EMDataSet dataset = getDataSet(col);
			String[] columns = dataset.getExpressionSets().getColumnNames();
			int index = getIndexInDataSet(col) + 2;
			return columns[index];
		}

		@Override
		public int getSize() {
			return expressionCount;
		}

		@Override
		public Optional<String> getPhenotype(int col) {
			EMDataSet dataset = getDataSet(col);
			int index = getIndexInDataSet(col);
			String[] classes = dataset.getEnrichments().getPhenotypes();
			if(classes != null && index < classes.length) {
				return Optional.ofNullable(classes[index]);
			}
			return Optional.empty();
		}
		
		public void transformChanged() {
			cache.invalidateAll();
		}
	}
	
	
	
	private class CompressDataset implements ExpressionData {
		private final List<EMDataSet> datasets;
		
		public CompressDataset(List<EMDataSet> datasets) {
			this.datasets = datasets;
		}
		
		public EMDataSet getDataSet(int col) {
			return datasets.get(col-DESC_COL_COUNT);
		}
		
		@Override
		public double getValue(int geneID, int col) {
			EMDataSet dataset = getDataSet(col);
			return (double) getCompressedExpression(dataset, geneID);
		}

		@Override
		public String getColumnName(int col) {
			EMDataSet dataset = getDataSet(col);
			if(map.isDistinctExpressionSets())
				return dataset.getName();
			else
				return "Expressions";
		}

		@Override
		public int getSize() {
			return datasets.size();
		}
		
		private float getCompressedExpression(EMDataSet dataset, int geneID) {
			float[] expression = getExpression(dataset, geneID, transform);
			if(expression == null)
				return Float.NaN;
			
			switch(compress) {
				case DATASET_MEDIAN:	return GeneExpression.median(expression);
				case DATASET_MAX:	return GeneExpression.max(expression);
				case DATASET_MIN:	return GeneExpression.min(expression);
				default:				return Float.NaN;
			}
		}
	}
	
	
	
	private class CompressClass implements ExpressionData {

		private List<Pair<EMDataSet,String>> headers = new ArrayList<>();
		
		public CompressClass(List<EMDataSet> datasets) {
			for(EMDataSet dataset : datasets) {
				SetOfEnrichmentResults enrichments = dataset.getEnrichments();
				String pheno1 = enrichments.getPhenotype1();
				String pheno2 = enrichments.getPhenotype2();
				if(pheno1 != null)
					headers.add(Pair.of(dataset, pheno1));
				if(pheno2 != null)
					headers.add(Pair.of(dataset, pheno2));
			}
		}
		
		@Override
		public EMDataSet getDataSet(int col) {
			return headers.get(col-DESC_COL_COUNT).getLeft();
		}

		@Override
		public String getColumnName(int col) {
			return headers.get(col-DESC_COL_COUNT).getRight();
		}
		
		@Override
		public Optional<String> getPhenotype(int col) {
			return Optional.of(getColumnName(col));
		}
		
		@Override
		public double getValue(int geneID, int col) {
			EMDataSet dataset = getDataSet(col);
			String pheno = getColumnName(col);
			
			String[] phenotypes = dataset.getEnrichments().getPhenotypes();
			if(phenotypes == null || phenotypes.length == 0)
				return Double.NaN;
			
			float[] expressions = getExpression(dataset, geneID, transform); 
			if(expressions == null || expressions.length == 0 || expressions.length != phenotypes.length)
				return Double.NaN;
			
			int size = 0;
			for(int i = 0; i < expressions.length; i++) {
				if(pheno.equals(phenotypes[i])) {
					size++;
				}
			}
			
			float[] vals = new float[size];
			int vi = 0;
			
			for(int i = 0; i < expressions.length; i++) {
				if(pheno.equals(phenotypes[i])) {
					vals[vi++] = expressions[i];
				}
			}
			
			switch(compress) {
				case CLASS_MEDIAN: return GeneExpression.median(vals);
				case CLASS_MAX:    return GeneExpression.max(vals);
				case CLASS_MIN:    return GeneExpression.min(vals);
				default:	           return Double.NaN;
			}
		}

		@Override
		public int getSize() {
			return headers.size();
		}
		
	}
	
	
	public HeatMapTableModel(EnrichmentMap map, Map<Integer,RankValue> ranking, List<String> genes, Transform transform, Compress compress) {
		this.transform = transform;
		this.compress = compress;
		this.map = map;
		this.ranking = ranking;
		this.genes = genes;
		
		// if all the expression sets are the same then just show one of them
		if(map.isCommonExpressionValues())
			datasets = map.getDataSetList().subList(0, 1);
		else
			datasets = map.getDataSetList();
		
		ExpressionData uncompressed = new Uncompressed(datasets);
		ExpressionData compressDataset = new CompressDataset(datasets);
		ExpressionData compressClass = new CompressClass(datasets);
		
		data.put(Compress.NONE, uncompressed);
		data.put(Compress.DATASET_MEDIAN, compressDataset);
		data.put(Compress.DATASET_MAX, compressDataset);
		data.put(Compress.DATASET_MIN, compressDataset);
		data.put(Compress.CLASS_MEDIAN, compressClass);
		data.put(Compress.CLASS_MAX, compressClass);
		data.put(Compress.CLASS_MIN, compressClass);
	}
	
	
	public List<EMDataSet> getDataSets() {
		return Collections.unmodifiableList(datasets);
	}
	
	public void setTransform(Transform transform, Compress compress) {
		boolean structureChanged = !this.compress.sameStructure(compress);
		this.transform = transform;
		this.compress = compress;
		data.values().forEach(ExpressionData::transformChanged);
		if(structureChanged)
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
		return data.get(compress).getSize() + DESC_COL_COUNT;
	}
	
	@Override
	public String getColumnName(int col) {
		if(col == GENE_COL)
			return "Gene";
		if(col == DESC_COL)
			return "Description";
		if(col == RANK_COL)
			return ranksColName;
		return data.get(compress).getColumnName(col);
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
		return data.get(compress).getValue(geneID, col);
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
		return data.get(compress).getPhenotype(col);
	}
	
	
	public EMDataSet getDataSet(int col) {
		return data.get(compress).getDataSet(col);
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
