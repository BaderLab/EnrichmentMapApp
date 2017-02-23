package org.baderlab.csplugins.enrichmentmap.model;

import static com.google.common.base.Strings.isNullOrEmpty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet.Method;
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
	
	/** Parameters used to create this map */
	private final EMCreationParameters params;

	private Map<String, EMDataSet> dataSets = new HashMap<>();

	/** The set of genes defined in the Enrichment map. */
	private BiMap<Integer, String> genes = HashBiMap.create();

	/** Post analysis signature genesets associated with this map.*/
	private Map<String, EMSignatureDataSet> signatureDataSets = new HashMap<>();
	
	private SetOfGeneSets globalGenesets = new SetOfGeneSets();

	private int NumberOfGenes = 0;
	private boolean isLegacy = false;
	private boolean isDistinctExpressionSets = false;

	private final Object lock = new Object();
	
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

		if (isNullOrEmpty(files.getEnrichmentFileName1()) && isNullOrEmpty(files.getGMTFileName())
				&& isNullOrEmpty(files.getExpressionFileName()))
			throw new IllegalArgumentException("At least one of the required files must be given");

		EMDataSet ds = new EMDataSet(this, name, method, files);
		dataSets.put(name, ds);
		initializeFiles(ds);
		
		return ds;
	}
	
	/**
	 * Method to transfer files specified in the parameters to the objects they correspond to.
	 */
	private void initializeFiles(EMDataSet ds) {
		DataSetFiles files = ds.getDataSetFiles();
		if (!isNullOrEmpty(files.getGMTFileName()))
			ds.getSetOfGeneSets().setFilename(files.getGMTFileName());

		// expression files
		if (!isNullOrEmpty(files.getExpressionFileName()))
			ds.getExpressionSets().setFilename(files.getExpressionFileName());

		// enrichment results files
		if (!isNullOrEmpty(files.getEnrichmentFileName1()))
			ds.getEnrichments().setFilename1(files.getEnrichmentFileName1());
		if (files.getEnrichmentFileName2() != null && !files.getEnrichmentFileName2().isEmpty())
			ds.getEnrichments().setFilename2(files.getEnrichmentFileName2());

		//phenotypes
		if (!isNullOrEmpty(files.getPhenotype1()))
			ds.getEnrichments().setPhenotype1(files.getPhenotype1());
		if (!isNullOrEmpty(files.getPhenotype2()))
			ds.getEnrichments().setPhenotype2(files.getPhenotype2());

		//rank files - dataset1 
		if (!isNullOrEmpty(files.getRankedFile())) {
			if (ds.getMethod() == Method.GSEA) {
				ds.getExpressionSets().createNewRanking(Ranking.GSEARanking);
				ds.getExpressionSets().getRanksByName(Ranking.GSEARanking).setFilename(files.getRankedFile());
			} else {
				ds.getExpressionSets().createNewRanking(ds.getName());
				ds.getExpressionSets().getRanksByName(ds.getName()).setFilename(files.getRankedFile());
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
		// MKTOD should I toUpperCase?
		return genes.inverse().get(gene);
	}
	
	public Collection<String> getAllGenes() {
		return Collections.unmodifiableCollection(genes.values());
	}
	
	public Optional<Integer> addGene(String gene) {
		gene = gene.toUpperCase();
		
		Map<String,Integer> geneToHash = genes.inverse();
		
		Integer hash = geneToHash.get(gene);
		if(hash != null)
			return Optional.of(hash);

		Integer newHash = ++NumberOfGenes;
		genes.put(newHash, gene);
		return Optional.of(newHash);
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
	 * Given a set of genesets Go through the genesets and extract all the genes
	 * Return - hashmap of genes to hash keys (used to create an expression file
	 * when it is not present so user can use expression viewer to navigate
	 * genes in a geneset without have to generate their own dummy expression file)
	 */
	public Map<String, Integer> getGeneSetsGenes(Collection<GeneSet> currentGeneSets) {
		Map<String, Integer> geneSetsGenes = new HashMap<>();

		for (GeneSet geneSet : currentGeneSets) {
			// Compare the HashSet of dataset genes to the HashSet of the current Geneset
			// only keep the genes from the geneset that are in the dataset genes
			for (Integer geneKey : geneSet.getGenes()) {
				// Get the current geneName
				if (genes.containsKey(geneKey)) {
					String name = genes.get(geneKey);
					geneSetsGenes.put(name, geneKey);
				}
			}
		}
		
		return geneSetsGenes;
	}

	/**
	 * Filter all the genesets by the dataset genes If there are multiple sets
	 * of genesets make sure to filter by the specific dataset genes
	 */
	public void filterGenesets() {
		for (EMDataSet ds : dataSets.values()) {
			// only filter the genesets if dataset genes are not null or empty
			if (ds.getDataSetGenes() != null && !ds.getDataSetGenes().isEmpty()) {
				ds.getSetOfGeneSets().filterGeneSets(ds.getDataSetGenes());
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
			allGeneSets.putAll(globalGenesets.getGeneSets());
		
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

	@Deprecated
	public Map<String, GeneSet> getAllGeneSetsOfInterest() {
		//go through each dataset and get the genesets from each
		Map<String, GeneSet> allGeneSets = new HashMap<>();
		
		for (EMDataSet ds : dataSets.values())
			allGeneSets.putAll(ds.getGeneSetsOfInterest().getGeneSets());
		
		// if there are post analysis genesets, add them to the set of all genesets
		if (signatureDataSets != null) {
			for (EMSignatureDataSet sds : signatureDataSets.values())
				allGeneSets.putAll(sds.getGeneSetsOfInterest().getGeneSets());
		}
		
		return allGeneSets;
	}
	
	// MKTODO write a JUnit
	public Map<String, Set<Integer>> unionAllGeneSetsOfInterest() {
		Map<String, Set<Integer>> allGeneSets = new HashMap<>();

		for (EMDataSet ds : getDataSetList()) {
			Map<String, GeneSet> geneSets = ds.getGeneSetsOfInterest().getGeneSets();
			
			geneSets.forEach((name, gs) -> {
				allGeneSets.computeIfAbsent(name, k -> new HashSet<>()).addAll(gs.getGenes());
			});
		}

		return allGeneSets;
	}
	
	// MKTODO write a JUnit
	public Set<String> getAllGeneSetNames() {
		Set<String> names = new HashSet<>();
		
		for (EMDataSet ds : getDataSetList()) {
			Map<String, GeneSet> geneSets = ds.getGeneSetsOfInterest().getGeneSets();
			names.addAll(geneSets.keySet());
		}
		
		return names;
	}

	public SetOfGeneSets getGlobalGenesets() {
		return globalGenesets;
	}
	
	public Map<String, EMDataSet> getDataSets() {
		return dataSets;
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
		this.dataSets = dataSets;
	}

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

	/**
	 * Returns the node SUIDs for all the gene-sets in the given collection of DataSets.
	 * Each returned gene-set is contained in at least one of the given DataSets.
	 */
	public static Set<Long> getNodesUnion(Collection<AbstractDataSet> dataSets) {
		if (dataSets.isEmpty())
			return Collections.emptySet();
		
		Set<Long> suids = new HashSet<>();
		
		for (AbstractDataSet ds : dataSets) {
			suids.addAll(ds.getNodeSuids().values());
		}
		
		return suids;
	}
	
	/**
	 * Returns the node SUIDs for all the gene-sets in the given collection of DataSets.
	 * Each returned gene-set is contained all of the given DataSets.
	 */
	public static Set<Long> getNodesIntersection(Collection<EMDataSet> queryDatasets) {
		if (queryDatasets.isEmpty())
			return Collections.emptySet();

		Iterator<EMDataSet> iter = queryDatasets.iterator();
		EMDataSet first = iter.next();
		Set<Long> suids = new HashSet<>(first.getNodeSuids().values());

		while (iter.hasNext()) {
			EMDataSet dataset = iter.next();
			suids.retainAll(dataset.getNodeSuids().values());
		}

		return suids;
	}
	
	public Set<String> getAllRankNames() {
		Set<String> allRankNames = new HashSet<>();
		
		//go through each Dataset
		for (EMDataSet ds : dataSets.values()) {
			// there could be duplicate ranking names for two different datasets. Add the dataset to the ranks name
			Set<String> allNames = ds.getExpressionSets().getAllRanksNames();

			for (String name : allNames)
				allRankNames.add(name + "-" + ds.getName());
		}
		
		return allRankNames;
	}

	public Map<String, Ranking> getAllRanks() {
		Map<String, Ranking> allranks = new HashMap<>();
		
		for (EMDataSet dataset : dataSets.values())
			allranks.putAll(dataset.getExpressionSets().getRanks());
		
		return allranks;
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
				if (ds.equalsIgnoreCase(nextDs)
						&& (dataSets.get(nextDs)).getExpressionSets().getAllRanksNames().contains(rank))
					return dataSets.get(nextDs).getExpressionSets().getRanksByName(rank);
			} else if ((dataSets.get(nextDs)).getExpressionSets().getAllRanksNames().contains(ranksName)) {
				return dataSets.get(nextDs).getExpressionSets().getRanksByName(ranksName);
			}
		}
		
		return null;
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
	
	public void addSignatureDataSet(EMSignatureDataSet sigDataSet) {
		synchronized (lock) {
			signatureDataSets.put(sigDataSet.getName(), sigDataSet);
		}
	}

	public void setDistinctExpressionSets(boolean d) {
		this.isDistinctExpressionSets = d;
	}
	
	public boolean isDistinctExpressionSets() {
		return isDistinctExpressionSets;
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
	
	@Override
	public String toString() {
		return getName();
	}
}
