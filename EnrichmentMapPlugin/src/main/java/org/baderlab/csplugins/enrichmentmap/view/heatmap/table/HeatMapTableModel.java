package org.baderlab.csplugins.enrichmentmap.view.heatmap.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.swing.table.AbstractTableModel;

import org.baderlab.csplugins.enrichmentmap.model.Compress;
import org.baderlab.csplugins.enrichmentmap.model.CompressedClass;
import org.baderlab.csplugins.enrichmentmap.model.CompressedDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.ExpressionCache;
import org.baderlab.csplugins.enrichmentmap.model.ExpressionData;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpression;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpressionMatrix;
import org.baderlab.csplugins.enrichmentmap.model.Transform;
import org.baderlab.csplugins.enrichmentmap.model.Uncompressed;
import org.baderlab.csplugins.enrichmentmap.util.NetworkUtil;
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
	
	
	public HeatMapTableModel(
			CyNetwork network,
			EnrichmentMap map,
			Map<Integer, RankValue> ranking,
			List<String> genes,
			Transform transform,
			Compress compress
	) {
		update(network, map, ranking, genes, transform, compress);
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
		
		expressionCache = new ExpressionCache(transform);
		
		ExpressionData uncompressed = new Uncompressed(datasets, expressionCache);
		ExpressionData compressedDataSet = new CompressedDataSet(datasets, expressionCache,
				map != null && map.isDistinctExpressionSets());
		ExpressionData compressedClass = new CompressedClass(datasets, expressionCache);
		
		data.put(Compress.NONE, uncompressed);
		data.put(Compress.DATASET_MEDIAN, compressedDataSet);
		data.put(Compress.DATASET_MAX, compressedDataSet);
		data.put(Compress.DATASET_MIN, compressedDataSet);
		data.put(Compress.CLASS_MEDIAN, compressedClass);
		data.put(Compress.CLASS_MAX, compressedClass);
		data.put(Compress.CLASS_MIN, compressedClass);
		
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
		
		if (structureChanged)
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
		if (col == GENE_COL)
			return "Gene";
		if (col == DESC_COL)
			return "Description";
		if (col == RANK_COL)
			return ranksColName;
		
		ExpressionData exp = data.get(compress);
		
		return exp != null ? exp.getName(col - DESC_COL_COUNT) : null;
	}

	/**
	 * Note: For HeatMapRowSorter to work properly this method
	 * cannot return null for rank or expression values.
	 * Return RankValue.EMPTY for ranks and Double.NaN for expressions.
	 */
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
				if (gene != null) {
					geneID = map.getHashFromGene(gene);
				}
			}
		}
		
		if (col == DESC_COL)
			return geneID != null ? getDescription(geneID) : null;
		
		ExpressionData exp = data.get(compress);
		if(exp == null || geneID == null)
			return Double.NaN; // because the DefaultRowSorter doesn't sort null the way we want
		return exp.getValue(geneID, col - DESC_COL_COUNT, compress);
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
		switch (col) {
			case GENE_COL: return String.class;
			case DESC_COL: return String.class;
			case RANK_COL: return RankValue.class;
			 // most of the existing code uses double, so cast expression values to double to avoid making big changes
			default:       return Double.class;
		}
	}
	
	public boolean hasSignificantRanks() {
		if (ranking == null)
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
		for (EMDataSet dataset : datasets) {
			GeneExpression row = getGeneExpression(dataset, geneID);
			
			if (row != null)
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
}
