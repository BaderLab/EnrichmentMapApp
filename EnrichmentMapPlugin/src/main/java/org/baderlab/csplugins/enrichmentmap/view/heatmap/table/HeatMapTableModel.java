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

import javax.swing.table.AbstractTableModel;

import org.apache.commons.lang3.tuple.Pair;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpression;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpressionMatrix;
import org.baderlab.csplugins.enrichmentmap.model.SetOfEnrichmentResults;
import org.baderlab.csplugins.enrichmentmap.util.NetworkUtil;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParams.Compress;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParams.Transform;
import org.cytoscape.model.CyNetwork;

@SuppressWarnings("serial")
public class HeatMapTableModel extends AbstractTableModel {

	/** Number of columns at the start that don't show expression data (ie. gene name etc..) */
	public static final int DESC_COL_COUNT = 3; 
	public static final int GENE_COL = 0;
	public static final int DESC_COL = 1;
	public static final int RANK_COL = 2;

	private CyNetwork network;
	private EnrichmentMap map;
	
	private List<EMDataSet> datasets;
	private Map<Compress, ExpressionData> data = new EnumMap<>(Compress.class);
	private ExpressionCache expressionCache;

	private List<String> genes;
	private Transform transform;
	private Compress compress;
	private Map<Integer, RankValue> ranking;
	private String ranksColName = "Ranks";
	
	public HeatMapTableModel() {
		update(null, null, null, Collections.emptyList(), Transform.AS_IS, Compress.NONE);
	}
	
	public void update(
			CyNetwork network,
			EnrichmentMap map,
			Map<Integer, RankValue> ranking,
			List<String> genes,
			Transform transform,
			Compress compress
	) {
		this.network = network;
		this.transform = transform;
		this.compress = compress;
		this.map = map;
		this.ranking = ranking;
		this.genes = genes != null ? new ArrayList<>(genes) : Collections.emptyList();

		// if all the expression sets are the same then just show one of them
		if (map != null) {
			if (map.isCommonExpressionValues())
				datasets = map.getDataSetList().subList(0, 1);
			else
				datasets = map.getDataSetList();
		} else {
			datasets = Collections.emptyList();
		}
		
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
		
		expressionCache = new ExpressionCache(transform);
		
		fireTableStructureChanged();	
		fireTableDataChanged();
	}
	
	public List<EMDataSet> getDataSets() {
		return Collections.unmodifiableList(datasets);
	}
	
	public ExpressionData getExpressionData(Compress compress) {
		return data.get(compress);
	}
	
	public void setTransform(Transform transform, Compress compress) {
		boolean structureChanged = !this.compress.sameStructure(compress);
		this.transform = transform;
		this.compress = compress;
		this.expressionCache = new ExpressionCache(transform);
		if(structureChanged)
			fireTableStructureChanged();
		fireTableDataChanged();
	}

	public void setRanking(String ranksColName, Map<Integer, RankValue> ranking) {
		this.ranksColName = Objects.requireNonNull(ranksColName);
		this.ranking = ranking;
		fireTableDataChanged();
	}
	
	public void setGenes(List<String> genes) {
		this.genes = genes != null ? new ArrayList<>(genes) : Collections.emptyList();
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
	
	public List<String> getGenes() {
		return new ArrayList<>(genes);
	}
	
	public String getGene(int row) {
		return genes.size() > row ? genes.get(row) : null;
	}
	
	@Override
	public int getRowCount() {
		return genes.size();
	}

	@Override
	public int getColumnCount() {
		ExpressionData exp = data.get(compress);
		return (exp != null ? exp.getSize() : 0) + DESC_COL_COUNT;
	}
	
	@Override
	public String getColumnName(int col) {
		if(col == GENE_COL)
			return "Gene";
		if(col == DESC_COL)
			return "Description";
		if(col == RANK_COL)
			return ranksColName;
		ExpressionData exp = data.get(compress);
		return exp != null ? exp.getName(col - DESC_COL_COUNT) : null;
	}

	@Override
	public Object getValueAt(int row, int col) {
		if (row < 0)
			return null; // Why is it passing -1?
		if (col == RANK_COL)
			return getRankValue(row);
		
		String gene = getGene(row);
		
		if (col == GENE_COL)
			return gene;
		
		Integer geneID = null;
		
		if (map != null && gene != null) {
			geneID = map.getHashFromGene(gene);
			
			if (geneID == null) {
				// It may be another gene symbol (given by another app), other than the original query term,
				// so we need to get the original query term
				gene = NetworkUtil.getQueryTerm(network, gene);
				
				if (gene != null)
					geneID = map.getHashFromGene(gene);
			}
		}
		
		if (col == DESC_COL)
			return geneID != null ? getDescription(geneID) : null;
		
		ExpressionData exp = data.get(compress);
		
		return exp != null && geneID != null ? exp.getValue(geneID, col - DESC_COL_COUNT) : null;
	}
	
	public RankValue getRankValue(int row) {
		// Use empty RankValue objects for missing genes instead of nulls so that they sort last (see RankValue.compareTo()).
		if (ranking == null)
			return RankValue.EMPTY;
		
		String gene = getGene(row);
		Integer geneID = map != null && gene != null ? map.getHashFromGene(gene) : null;
		
		return geneID != null ? ranking.getOrDefault(geneID, RankValue.EMPTY) : RankValue.EMPTY;
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
		ExpressionData exp = data.get(compress);
		return exp != null ? exp.getPhenotype(col - DESC_COL_COUNT) : null;
	}
	
	public EMDataSet getDataSet(int col) {
		ExpressionData exp = data.get(compress);
		return exp != null ? exp.getDataSet(col - DESC_COL_COUNT) : null;
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
	
	private class Uncompressed implements ExpressionData {

		private final NavigableMap<Integer, EMDataSet> colToDataSet = new TreeMap<>();
		private final int expressionCount;
		
		public Uncompressed(List<EMDataSet> datasets) {
			int rangeFloor = 0;
			colToDataSet.put(0, null);
			for(EMDataSet dataset : datasets) {
				GeneExpressionMatrix matrix = dataset.getExpressionSets();
				colToDataSet.put(rangeFloor, dataset);
				rangeFloor += matrix.getNumConditions() - 2;
			}
			expressionCount = rangeFloor;
		}
		
		@Override
		public EMDataSet getDataSet(int idx) {
			return colToDataSet.floorEntry(idx).getValue();
		}
		
		private int getIndexInDataSet(int idx) {
			int start = colToDataSet.floorKey(idx);
			return idx - start;
		}
		
		@Override
		public double getValue(int geneID, int idx) {
			EMDataSet dataset = getDataSet(idx);
			return expressionCache.getExpression(dataset, geneID, getIndexInDataSet(idx));
		}

		@Override
		public String getName(int idx) {
			EMDataSet dataset = getDataSet(idx);
			String[] columns = dataset.getExpressionSets().getColumnNames();
			int index = getIndexInDataSet(idx) + 2;
			return columns[index];
		}

		@Override
		public int getSize() {
			return expressionCount;
		}

		@Override
		public Optional<String> getPhenotype(int idx) {
			EMDataSet dataset = getDataSet(idx);
			int index = getIndexInDataSet(idx);
			String[] classes = dataset.getEnrichments().getPhenotypes();
			if(classes != null && index < classes.length) {
				return Optional.ofNullable(classes[index]);
			}
			return Optional.empty();
		}
	}
	
	private class CompressDataset implements ExpressionData {
		private final List<EMDataSet> datasets;
		
		public CompressDataset(List<EMDataSet> datasets) {
			this.datasets = datasets;
		}
		
		@Override
		public EMDataSet getDataSet(int idx) {
			return datasets.get(idx);
		}
		
		@Override
		public double getValue(int geneID, int idx) {
			EMDataSet dataset = getDataSet(idx);
			return (double) getCompressedExpression(dataset, geneID);
		}

		@Override
		public String getName(int idx) {
			EMDataSet dataset = getDataSet(idx);
			return map != null && map.isDistinctExpressionSets() ? dataset.getName() : "Expressions";
		}

		@Override
		public int getSize() {
			return datasets.size();
		}
		
		private float getCompressedExpression(EMDataSet dataset, int geneID) {
			Optional<float[]> expression = expressionCache.getExpressions(dataset, geneID);
			
			if (compress == null || !expression.isPresent())
				return Float.NaN;
			
			switch (compress) {
				case DATASET_MEDIAN:	return GeneExpression.median(expression.get());
				case DATASET_MAX:	return GeneExpression.max(expression.get());
				case DATASET_MIN:	return GeneExpression.min(expression.get());
				default:				return Float.NaN;
			}
		}
	}
	
	private class CompressClass implements ExpressionData {

		private List<Pair<EMDataSet, String>> headers = new ArrayList<>();
		
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
		public EMDataSet getDataSet(int idx) {
			return headers.get(idx).getLeft();
		}

		@Override
		public String getName(int idx) {
			return headers.get(idx).getRight();
		}
		
		@Override
		public Optional<String> getPhenotype(int idx) {
			return Optional.of(getName(idx));
		}
		
		@Override
		public double getValue(int geneID, int idx) {
			EMDataSet dataset = getDataSet(idx);
			String pheno = getName(idx);
			
			String[] phenotypes = dataset.getEnrichments().getPhenotypes();
			if(phenotypes == null || phenotypes.length == 0)
				return Double.NaN;
			
			Optional<float[]> optExpr = expressionCache.getExpressions(dataset, geneID); 
			if(!optExpr.isPresent())
				return Double.NaN;
			float[] expressions = optExpr.get();
			if(expressions.length == 0 || expressions.length != phenotypes.length)
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
}
