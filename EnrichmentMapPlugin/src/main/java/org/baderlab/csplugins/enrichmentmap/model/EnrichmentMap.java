package org.baderlab.csplugins.enrichmentmap.model;

import static com.google.common.base.Strings.isNullOrEmpty;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet.Method;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder;
import org.baderlab.csplugins.enrichmentmap.util.NetworkUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/***
 * An Enrichment Map object contains the minimal information needed to build an enrichment map.
 */
public class EnrichmentMap {

	private transient CyServiceRegistrar serviceRegistrar;
	
	private long networkID;
	
	/** SUIDs of networks created by other apps (e.g. GeneMANIA, STRING) from EM genes. */
	private final Set<Long> associatedNetworkIDs = new LinkedHashSet<>();
	
	/** Parameters used to create this map */
	private final EMCreationParameters params;

	private final Map<String, EMDataSet> dataSets = new HashMap<>();

	/**
	 * In order to minimize memory usage (and session file size) we will store expressions
	 * here and have the EMDataSets keep a key to this map. That way datasets can share
	 * expression matrices.
	 */
	private final Map<String, GeneExpressionMatrix> expressions = new HashMap<>();
	private final Map<String, SetOfGeneSets> geneSets = new HashMap<>();
	
	/** The set of genes defined in the Enrichment map. */
	private final BiMap<Integer, String> genes = HashBiMap.create();

	/** Post analysis signature genesets associated with this map.*/
	private final Map<String, EMSignatureDataSet> signatureDataSets = new HashMap<>();
	
	private int NumberOfGenes = 0;
	private boolean isLegacy = false;
	private boolean isDistinctExpressionSets = false;
	private boolean isCommonExpressionValues = false;
	
	private Color compoundEdgeColor;

	private final Object lock = new Object();
	
	/**
	 * Used by the JSON deserializer only. Don't remove this constructor!
	 */
	@SuppressWarnings("unused")
	private EnrichmentMap() {
		this(null, null);
	}
	
	/**
	 * Class Constructor Given - EnrichmentnMapParameters create a new
	 * enrichment map. The parameters contain all cut-offs and file names for the analysis
	 */
	public EnrichmentMap(EMCreationParameters params, CyServiceRegistrar serviceRegistrar) {
		this.params = params;
		this.serviceRegistrar = serviceRegistrar;
	}

	public EMDataSet createDataSet(String name, Method method, DataSetFiles files) {
		if (dataSets.containsKey(name))
			throw new IllegalArgumentException("DataSet with name " + name + " already exists in this enrichment map");

		EMDataSet ds = new EMDataSet(this, name, method, files);
		dataSets.put(name, ds);
		initializeFiles(ds);
		
		return ds;
	}
	
	public void putExpressionMatrix(String key, GeneExpressionMatrix matrix) {
		expressions.put(key, matrix);
	}
	
	public GeneExpressionMatrix getExpressionMatrix(String key) {
		return expressions.get(key);
	}
	
	public GeneExpressionMatrix removeExpressionMatrix(String key) {
		return expressions.remove(key);
	}
	
	public Collection<String> getExpressionMatrixKeys() {
		return Collections.unmodifiableCollection(expressions.keySet());
	}
	
	public void putGeneSets(String key, SetOfGeneSets matrix) {
		geneSets.put(key, matrix);
	}
	
	public SetOfGeneSets getGeneSets(String key) {
		return geneSets.get(key);
	}
	
	public SetOfGeneSets removeGeneSets(String key) {
		return geneSets.remove(key);
	}
	
	public Collection<String> getGeneSetsKeys() {
		return Collections.unmodifiableCollection(geneSets.keySet());
	}
	
	public boolean hasClassData() {
		for(EMDataSet dataset : dataSets.values()) {
			String[] classes = dataset.getEnrichments().getPhenotypes();
			if(classes != null && classes.length > 0) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Method to transfer files specified in the parameters to the objects they correspond to.
	 */
	private void initializeFiles(EMDataSet ds) {
		DataSetFiles files = ds.getDataSetFiles();

		//phenotypes
		if (!isNullOrEmpty(files.getPhenotype1()))
			ds.getEnrichments().setPhenotype1(files.getPhenotype1());
		if (!isNullOrEmpty(files.getPhenotype2()))
			ds.getEnrichments().setPhenotype2(files.getPhenotype2());

		//rank files - dataset1 
		if (!isNullOrEmpty(files.getRankedFile())) {
			if (ds.getMethod() == Method.GSEA) {
				ds.createNewRanking(Ranking.GSEARanking);
			} else {
				ds.createNewRanking(ds.getName());
			}
		}
	}

	public void setServiceRegistrar(CyServiceRegistrar registrar) {
		this.serviceRegistrar = registrar;
	}

	public boolean containsGene(String gene) {
		return genes.containsValue(gene);
	}

	public String getGeneFromHashKey(Integer hash) {
		return genes.get(hash);
	}
	
	public Integer getHashFromGene(String gene) {
		// MKTODO should I toUpperCase?
		return genes.inverse().get(gene);
	}
	
	/**
	 * Returns ALL of the genes that have ever been loaded. Warning: this is probably not what you
	 * want because you probably want a set of genes that has been filtered somehow.
	 */
	public Set<String> getAllGenes() {
		return Collections.unmodifiableSet(genes.values());
	}
	
	public Integer addGene(String gene) {
		if(gene == null || gene.isEmpty())
			return null;
		
		gene = gene.toUpperCase();
		
		Map<String,Integer> geneToHash = genes.inverse();
		
		Integer hash = geneToHash.get(gene);
		if(hash != null)
			return hash;

		Integer newHash = ++NumberOfGenes;
		genes.put(newHash, gene);
		return newHash;
	}
	
	@Deprecated // this is here to support legacy session loading
	public void addGene(String gene, int id) {
		genes.put(id, gene);
		if(id > NumberOfGenes)
			NumberOfGenes = id;
	}
	
	public int getNumberOfGenes() {
		return NumberOfGenes;
	}

	public void setNumberOfGenes(int numberOfGenes) {
		NumberOfGenes = numberOfGenes;
	}

	/**
	 * Filter all the genesets by the dataset genes If there are multiple sets
	 * of genesets make sure to filter by the specific dataset genes
	 */
	@Deprecated
	public void filterGenesets() {
		for (EMDataSet ds : dataSets.values()) {
			// only filter the genesets if dataset genes are not null or empty
			Set<Integer> expressionGenes = ds.getExpressionGenes();
			if (expressionGenes != null && !expressionGenes.isEmpty()) {
				ds.getSetOfGeneSets().filterGeneSets(expressionGenes);
			}
		}
	}

	public String getName() {
		final String undefined = "-- UNDEFINED --";
		if(serviceRegistrar == null)
			return undefined;
		CyNetworkManager networkManager = serviceRegistrar.getService(CyNetworkManager.class);
		if(networkManager == null)
			return undefined;
		CyNetwork net = networkManager.getNetwork(networkID);
		if(net == null)
			return undefined;
		return NetworkUtil.getName(net);
	}

	/*
	 * Return a hash of all the genesets in the set of genesets regardless of which dataset it comes from.
	 */
	@Deprecated
	public Map<String, GeneSet> getAllGeneSets() {
		// Go through each dataset and get the genesets from each
		Map<String, GeneSet> allGeneSets = new HashMap<>();
		
		synchronized (lock) {
			// If a GeneSet appears in more than one DataSet, then its totally arbitrary which version of it gets picked
			// If a GeneSet appears in an enrichment file it will override the one with the same name in the global GMT file
			for (EMDataSet ds : dataSets.values()) {
				allGeneSets.putAll(ds.getSetOfGeneSets().getGeneSets());
			}
			
			if (signatureDataSets != null) {
				for (EMSignatureDataSet sds : signatureDataSets.values())
					allGeneSets.putAll(sds.getGeneSetsOfInterest().getGeneSets());
			}
		}
		
		return allGeneSets;
	}

	/*
	 * Return a hash of all the genesets but not inlcuding the signature genesets.
	 */
	@Deprecated
	public Map<String, GeneSet> getEnrichmentGenesets() {
		//go through each dataset and get the genesets from each
		Map<String, GeneSet> allGeneSets = new HashMap<>();
		
		for (EMDataSet ds : dataSets.values()) {
			Map<String, GeneSet> geneSets = ds.getSetOfGeneSets().getGeneSets();
			allGeneSets.putAll(geneSets);
		}
		
		return allGeneSets;
	}
	
	public Map<String, Set<Integer>> unionAllGeneSetsOfInterest() {
		return unionGeneSetsOfInterest(x -> true);
	}
	
	public Map<String, Set<Integer>> unionGeneSetsOfInterest(Collection<? extends AbstractDataSet> dataSets) {
		return unionGeneSetsOfInterest(dataSets::contains);
	}
	
	private Map<String, Set<Integer>> unionGeneSetsOfInterest(Predicate<EMDataSet> filter) {
		Map<String, Set<Integer>> allGeneSets = new HashMap<>();

		for (EMDataSet ds : getDataSetList()) {
			if(filter.test(ds)) {
				Map<String, GeneSet> geneSets = ds.getGeneSetsOfInterest().getGeneSets();
				
				geneSets.forEach((name, gs) -> {
					allGeneSets.computeIfAbsent(name, k -> new HashSet<>()).addAll(gs.getGenes());
				});
			}
		}

		return allGeneSets;
	}
	
	/**
	 * Returns a set of all genes in the map that are of interest and not from a signature data set.
	 */
	public Set<Integer> getAllEnrichmentGenes() {
		Set<Integer> genes = new HashSet<>();
		for(EMDataSet ds : getDataSetList()) {
			Map<String,GeneSet> geneSets = ds.getGeneSetsOfInterest().getGeneSets();
			for(GeneSet geneSet : geneSets.values()) {
				genes.addAll(geneSet.getGenes());
			}
		}
		return genes;
	}
	
	// MKTODO write a JUnit
	public Set<String> getAllGeneSetOfInterestNames() {
		Set<String> names = new HashSet<>();
		
		for (EMDataSet ds : getDataSetList()) {
			Map<String, GeneSet> geneSets = ds.getGeneSetsOfInterest().getGeneSets();
			names.addAll(geneSets.keySet());
		}
		
		return names;
	}
	
	public GeneSet getGeneSet(String genesetName) {
		for(EMDataSet ds : dataSets.values()) {
			GeneSet gs = ds.getGeneSetsOfInterest().getGeneSets().get(genesetName);
			if(gs != null) {
				return gs;
			}
		}
		return null;
	}
	
	
	public Map<String, EMDataSet> getDataSets() {
		// this must return a new HashMap or client code might break
		return new HashMap<>(dataSets);
	}
	
	/**
	 * Returns all the DataSets in a predictable order.
	 */
	public List<EMDataSet> getDataSetList() {
		List<EMDataSet> list = new ArrayList<>(dataSets.values());
		list.sort(Comparator.naturalOrder());
		return list;
	}

	public void setDataSets(Map<String, EMDataSet> dataSets) {
		this.dataSets.clear();
		
		if (dataSets != null && !dataSets.isEmpty())
			this.dataSets.putAll(dataSets);
	}

	/**
	 * Returns the total number of data sets, excluding Signature Data Sets.
	 */
	public int getDataSetCount() {
		return dataSets.size();
	}
	
	public EMDataSet getDataSet(String dataSetName) {
		return dataSets.get(dataSetName);
	}
	
	/**
	 * Returns all the DataSet names in a predictable order.
	 */
	public List<String> getDataSetNames() {
		return getDataSetList().stream().map(EMDataSet::getName).collect(Collectors.toList());
	}
	
	public EMCreationParameters getParams() {
		return params;
	}

	public long getNetworkID() {
		return networkID;
	}

	public void setNetworkID(long networkID) {
		this.networkID = networkID;
	}
	
	public boolean addAssociatedNetworkID(long networkID) {
		return associatedNetworkIDs.add(networkID);
	}
	
	public boolean removeAssociatedNetworkID(long networkID) {
		return associatedNetworkIDs.remove(networkID);
	}
	
	public Set<Long> getAssociatedNetworkIDs() {
		return new LinkedHashSet<>(associatedNetworkIDs);
	}
	
	public void setAssociatedNetworkIDs(Set<Long> set) {
		associatedNetworkIDs.clear();
		
		if (set != null && !set.isEmpty())
			associatedNetworkIDs.addAll(set);
	}
	
	public static Set<Long> getNodesUnion(Collection<? extends AbstractDataSet> dataSets) {
		return getUnion(dataSets, AbstractDataSet::getNodeSuids);
	}
	
	public static Set<Long> getNodesIntersection(Collection<? extends AbstractDataSet> dataSets) { 
		return getIntersection(dataSets, AbstractDataSet::getNodeSuids);
	}
			
	
	/**
	 * Returns the SUIDs for all the gene-sets in the given collection of DataSets.
	 * Each returned gene-set is contained in at least one of the given DataSets.
	 * 
	 * Note, this will only return distinct edges, not compound edges.
	 * 
	 * This method considers edges that are connected to signature gene sets to be part of the
	 * signature data set.
	 */
	public static Set<Long> getEdgesUnion(Collection<? extends AbstractDataSet> dataSets) {
		return getUnion(dataSets, AbstractDataSet::getEdgeSuids);
	}
	
	/**
	 * This method checks if signature edges are actually part of one of the given regular data sets
	 * when including them or not.
	 */
	public static Set<Long> getEdgesUnionForFiltering(Collection<? extends AbstractDataSet> dataSets, EnrichmentMap map, CyNetwork network) {
		List<EMDataSet> regularDatasets = new ArrayList<>();
		List<EMSignatureDataSet> signatureDatasets = new ArrayList<>();
		
		for(var ds : dataSets) {
			if(ds instanceof EMDataSet) {
				regularDatasets.add((EMDataSet)ds);
			} else if(ds instanceof EMSignatureDataSet) {
				signatureDatasets.add((EMSignatureDataSet)ds);
			}
		}
		
		Set<Long> regularEdgesUnion = getUnion(regularDatasets, AbstractDataSet::getEdgeSuids);
		
		if(map == null || network == null || signatureDatasets.isEmpty()) {
			return regularEdgesUnion;
		}
		
		// The problem with Signature edges is that they are associated with the Signature Data Set,
		// not the data set they were computed against. This information is only available in the
		// edge table. So we have to go through all the Signature edges and check if they are associated
		// with any of the regular data sets.
		
		Set<Long> allEdges = new HashSet<>();
		allEdges.addAll(regularEdgesUnion);
		
		
		Set<String> regularDataSetNames = new HashSet<>();
		for(var ds : regularDatasets) {
			regularDataSetNames.add(ds.getName());
		}
		
		var edgeTable = network.getDefaultEdgeTable();
		
		for(var sigDS : signatureDatasets) {
			for(Long suid : sigDS.getEdgeSuids()) {
				if(edgeTable.rowExists(suid)) {
					var row = edgeTable.getRow(suid);
					String prefix = map.getParams().getAttributePrefix();
					String dsname = EMStyleBuilder.Columns.EDGE_DATASET.get(row, prefix);
					if(regularDataSetNames.contains(dsname)) {
						allEdges.add(suid);
					}
				}
			}
		}
		
		return allEdges;
	}
	
	/**
	 * Returns the SUIDs for all the gene-sets in the given collection of DataSets.
	 * Each returned gene-set is contained all of the given DataSets.
	 * 
	 * Note, this will only return distinct edges, not compound edges.
	 */
	public static Set<Long> getEdgesIntersection(Collection<? extends AbstractDataSet> dataSets) { 
		return getIntersection(dataSets, AbstractDataSet::getEdgeSuids);
	}
	
	private static Set<Long> getUnion(Collection<? extends AbstractDataSet> dataSets, Function<AbstractDataSet,Set<Long>> suidSupplier) {
		if (dataSets.isEmpty())
			return Collections.emptySet();
		
		Set<Long> suids = new HashSet<>();
		
		for (AbstractDataSet ds : dataSets) {
			suids.addAll(suidSupplier.apply(ds));
		}
		
		return suids;
	}
	
	private static Set<Long> getIntersection(Collection<? extends AbstractDataSet> dataSets, Function<AbstractDataSet,Set<Long>> suidSupplier) {
		if (dataSets.isEmpty())
			return Collections.emptySet();

		Iterator<? extends AbstractDataSet> iter = dataSets.iterator();
		AbstractDataSet first = iter.next();
		Set<Long> suids = new HashSet<>(suidSupplier.apply(first));

		while (iter.hasNext()) {
			AbstractDataSet dataset = iter.next();
			suids.retainAll(suidSupplier.apply(dataset));
		}

		return suids;
	}
	
	
	public Set<String> getAllRankNames() {
		Set<String> allRankNames = new HashSet<>();
		
		//go through each Dataset
		for (EMDataSet ds : dataSets.values()) {
			// there could be duplicate ranking names for two different datasets. Add the dataset to the ranks name
			Set<String> allNames = ds.getAllRanksNames();

			for (String name : allNames)
				allRankNames.add(name + "-" + ds.getName());
		}
		
		return allRankNames;
	}

	public Map<String, Ranking> getAllRanks() {
		Map<String, Ranking> allranks = new HashMap<>();
		for (EMDataSet dataset : dataSets.values())
			allranks.putAll(dataset.getRanks());
		return allranks;
	}
	
	/**
	 * Returns true if every data set contains exactly one Ranks object.
	 */
	public boolean isSingleRanksPerDataset() {
		for(EMDataSet dataset : dataSets.values()) {
			if(dataset.getRanks().size() != 1) {
				return false;
			}
		}
		return true;
	}
	
	
	/**
	 * Returns the total number of expressions in the map.
	 */
	public int totalExpressionCount() {
		int count = 0;
		for(EMDataSet dataset : dataSets.values()) {
			count += dataset.getExpressionSets().getNumConditions() - 2;
		}
		return count;
	}
	

	public Ranking getRanksByName(String ranksName) {
		// break the ranks file up by "-"
		// check to see if the rank file is dataset specific
		// needed for encoding the same ranking file name from two different dataset in the interface
		String ds = "";
		String rank = "";
		
		if (ranksName.split("-").length == 2) {
			ds = ranksName.split("-")[1];
			rank = ranksName.split("-")[0];
		}

		for (Iterator<String> k = dataSets.keySet().iterator(); k.hasNext();) {
			String nextDs = k.next();

			if (!ds.equalsIgnoreCase("") && !rank.equalsIgnoreCase("")) {
				// check that this is the right dataset
				if (ds.equalsIgnoreCase(nextDs) && (dataSets.get(nextDs)).getAllRanksNames().contains(rank))
					return dataSets.get(nextDs).getRanksByName(rank);
			} else if ((dataSets.get(nextDs)).getAllRanksNames().contains(ranksName)) {
				return dataSets.get(nextDs).getRanksByName(ranksName);
			}
		}
		
		return null;
	}

	public EMSignatureDataSet getSignatureDataSet(String name) {
		return signatureDataSets.get(name);
	}
	
	public void setSignatureDataSets(Collection<EMSignatureDataSet> newValue) {
		synchronized (lock) {
			signatureDataSets.clear();
			
			if (newValue != null && !newValue.isEmpty()) {
				for (EMSignatureDataSet sigDataSet: newValue)
					addSignatureDataSet(sigDataSet);
			}
		}
	}

	public Map<String, EMSignatureDataSet> getSignatureDataSets() {
		return new HashMap<>(signatureDataSets);
	}
	
	public boolean hasSignatureDataSets() {
		return !signatureDataSets.isEmpty();
	}
	
	public List<EMSignatureDataSet> getSignatureSetList() {
		List<EMSignatureDataSet> list = new ArrayList<>(signatureDataSets.values());
		list.sort(Comparator.naturalOrder());
		return list;
	}
	
	public void addSignatureDataSet(EMSignatureDataSet sigDataSet) {
		synchronized (lock) {
			signatureDataSets.put(sigDataSet.getName(), sigDataSet);
		}
	}
	
	public void removeSignatureDataSet(EMSignatureDataSet sigDataSet) {
		synchronized (lock) {
			signatureDataSets.remove(sigDataSet.getName());
		}
	}

	public void setCompoundEdgeColor(Color compoundEdgeColor) {
		this.compoundEdgeColor = compoundEdgeColor;
	}
	
	public Color getCompoundEdgeColor() {
		return compoundEdgeColor;
	}
	
	public boolean useCompoundEdgeColor() {
		return !(getDataSetCount() > 1 && getParams().getCreateDistinctEdges());
	}
	
	public void setDistinctExpressionSets(boolean d) {
		this.isDistinctExpressionSets = d;
	}
	
	public boolean isDistinctExpressionSets() {
		return isDistinctExpressionSets;
	}
	
	public void setCommonExpressionValues(boolean b) {
		this.isCommonExpressionValues = b;
	}
	
	public boolean isCommonExpressionValues() {
		return isCommonExpressionValues;
	}
	
	public void setLegacy(boolean legacy) {
		this.isLegacy = legacy;
	}
	
	/**
	 * Files loaded by LegacySessionLoader should set this flag to true
	 */
	public boolean isLegacy() {
		return isLegacy;
	}
	
	public boolean isTwoPhenotypeGeneric() {
		return dataSets.values().stream().allMatch(EMDataSet::getIsTwoPheotypeGeneric);
	}
	
	public boolean hasNonGSEADataSet() {
		return dataSets.values().stream().anyMatch(ds -> ds.getMethod() != Method.GSEA);
	}
	
	public boolean isSingleGSEA() {
		return getDataSetCount() == 1 && getDataSetList().get(0).getMethod() == Method.GSEA;
	}
	
	@Override
	public String toString() {
		return getName();
	}
}
