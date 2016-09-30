package org.baderlab.csplugins.enrichmentmap.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/***
 * An Enrichment Map object contains the minimal information needed to build an
 * enrichment map.
 * 
 * @author risserlin
 *
 */

public class EnrichmentMap {

	//name of Enrichment map
	private String name = null;

	//The set of Datasets
	private Map<String, DataSet> datasets;

	//Hashmap of all the similarities between all the genesets
	//key = geneset1 + geneset2
	//value = geneset similarity 
	private Map<String, GenesetSimilarity> genesetSimilarity;

	//The set of genes defined in the Enrichment map
	private BiMap<Integer,String> genes;


	// Temporary constants for Dataset 1 and Dataset 2
	// Will eventually get rid of them
	final public static String DATASET1 = "Dataset 1";
	final public static String DATASET2 = "Dataset 2";

	//reference to the parameters used to create this map
	private EnrichmentMapParameters params;

	private int NumberOfGenes = 0;

	//post analysis signature genesets associated with this map.
	private Map<String, GeneSet> signatureGenesets;

	/*
	 * Class Constructor Given - EnrichmentnMapParameters create a new
	 * enrichment map. The parameters contain all cut-offs and file names for
	 * the analysis
	 */

	public EnrichmentMap(EnrichmentMapParameters params) {
		this.params = params;
		this.name = null;
		this.datasets = new HashMap<String, DataSet>();
		
		//initialize a new Dataset if the params have enrichment result or a GMT file
		if(params.getFiles().containsKey(EnrichmentMap.DATASET1)) {
			DataSetFiles dataset1files = params.getFiles().get(EnrichmentMap.DATASET1);
			if((dataset1files.getEnrichmentFileName1() != null && !dataset1files.getEnrichmentFileName1().isEmpty())
					|| (dataset1files.getGMTFileName() != null && !dataset1files.getGMTFileName().isEmpty())
					|| (dataset1files.getExpressionFileName() != null
							&& !dataset1files.getExpressionFileName().isEmpty())) {
				DataSet dataset1 = new DataSet(this, EnrichmentMap.DATASET1, dataset1files);
				this.datasets.put(EnrichmentMap.DATASET1, dataset1);
			}
		}
		//intialize a new Dataset 2 if the params have enrichment results for a second dataset.
		if(params.getFiles().containsKey(EnrichmentMap.DATASET2)) {
			DataSetFiles dataset2files = params.getFiles().get(EnrichmentMap.DATASET2);
			if((dataset2files.getEnrichmentFileName1() != null && !dataset2files.getEnrichmentFileName1().isEmpty())
					|| (dataset2files.getExpressionFileName() != null
							&& !dataset2files.getExpressionFileName().isEmpty())) {
				DataSet dataset2 = new DataSet(this, EnrichmentMap.DATASET2, dataset2files);
				this.datasets.put(EnrichmentMap.DATASET2, dataset2);
			}
		}

		this.genes = HashBiMap.create();
		this.genesetSimilarity = new HashMap<>();
		this.signatureGenesets = new HashMap<>();
		initialize_files();
	}

	public EnrichmentMap(EnrichmentMapParameters params, String name) {
		this(params);
		this.name = name;
	}


	/**
	 * Method to transfer files specified in the parameters to the objects they
	 * correspond to
	 */

	private void initialize_files() {
		DataSetFiles dataset1files = params.getFiles().get(EnrichmentMap.DATASET1);
		if(dataset1files.getGMTFileName() != null && !dataset1files.getGMTFileName().isEmpty())
			this.getDataset(DATASET1).getSetofgenesets().setFilename(dataset1files.getGMTFileName());

		//expression files
		if(dataset1files.getExpressionFileName() != null && !dataset1files.getExpressionFileName().isEmpty())
			this.getDataset(DATASET1).getExpressionSets().setFilename(dataset1files.getExpressionFileName());

		//enrichment results files
		if(dataset1files.getEnrichmentFileName1() != null && !dataset1files.getEnrichmentFileName1().isEmpty())
			this.getDataset(DATASET1).getEnrichments().setFilename1(dataset1files.getEnrichmentFileName1());
		if(dataset1files.getEnrichmentFileName2() != null && !dataset1files.getEnrichmentFileName2().isEmpty())
			this.getDataset(DATASET1).getEnrichments().setFilename2(dataset1files.getEnrichmentFileName2());

		//phenotypes
		if(dataset1files.getPhenotype1() != null && !dataset1files.getPhenotype1().isEmpty())
			this.getDataset(DATASET1).getEnrichments().setPhenotype1(dataset1files.getPhenotype1());
		if(dataset1files.getPhenotype2() != null && !dataset1files.getPhenotype2().isEmpty())
			this.getDataset(DATASET1).getEnrichments().setPhenotype2(dataset1files.getPhenotype2());

		//rank files - dataset1 
		if(dataset1files.getRankedFile() != null && !dataset1files.getRankedFile().isEmpty())
			if(params.getMethod().equals(EnrichmentMapParameters.method_GSEA)) {
				this.getDataset(DATASET1).getExpressionSets().createNewRanking(Ranking.GSEARanking);
				this.getDataset(DATASET1).getExpressionSets().getRanksByName(Ranking.GSEARanking)
						.setFilename(dataset1files.getRankedFile());
			} else {
				this.getDataset(DATASET1).getExpressionSets().createNewRanking(DATASET1);
				this.getDataset(DATASET1).getExpressionSets().getRanksByName(DATASET1)
						.setFilename(dataset1files.getRankedFile());
			}

		if(params.getFiles().containsKey(EnrichmentMap.DATASET2)) {
			DataSetFiles dataset2files = params.getFiles().get(EnrichmentMap.DATASET2);
			if(dataset2files.getExpressionFileName() != null && !dataset2files.getExpressionFileName().isEmpty())
				this.getDataset(DATASET2).getExpressionSets().setFilename(dataset2files.getExpressionFileName());

			if(dataset2files.getEnrichmentFileName1() != null && !dataset2files.getEnrichmentFileName1().isEmpty())
				this.getDataset(DATASET2).getEnrichments().setFilename1(dataset2files.getEnrichmentFileName1());
			if(dataset2files.getEnrichmentFileName2() != null && !dataset2files.getEnrichmentFileName2().isEmpty())
				this.getDataset(DATASET2).getEnrichments().setFilename2(dataset2files.getEnrichmentFileName2());

			//phenotypes
			if(dataset2files.getPhenotype1() != null && !dataset2files.getPhenotype1().isEmpty())
				this.getDataset(DATASET2).getEnrichments().setPhenotype1(dataset2files.getPhenotype1());
			if(dataset2files.getPhenotype2() != null && !dataset2files.getPhenotype2().isEmpty())
				this.getDataset(DATASET2).getEnrichments().setPhenotype2(dataset2files.getPhenotype2());

			//rank files - dataset 2
			if(dataset2files.getRankedFile() != null && !dataset2files.getRankedFile().isEmpty()) {
				if(params.getMethod().equals(EnrichmentMapParameters.method_GSEA)) {
					this.getDataset(DATASET2).getExpressionSets().createNewRanking(Ranking.GSEARanking);
					this.getDataset(DATASET2).getExpressionSets().getRanksByName(Ranking.GSEARanking)
							.setFilename(dataset2files.getRankedFile());
				} else {
					this.getDataset(DATASET2).getExpressionSets().createNewRanking(DATASET2);
					this.getDataset(DATASET2).getExpressionSets().getRanksByName(DATASET2)
							.setFilename(dataset2files.getRankedFile());

				}
			}
		}
	}

	/**
	 * Check to see that there are genes in the filtered genesets If the ids do
	 * not match up, after a filtering there will be no genes in any of the
	 * genesets
	 * 
	 * @return true if Genesets have genes, return false if all the genesets are
	 *         empty
	 */
	public boolean checkGenesets() {

		for(Iterator<String> k = datasets.keySet().iterator(); k.hasNext();) {
			Map<String, GeneSet> genesets = (datasets.get(k.next())).getSetofgenesets().getGenesets();
			for(Iterator j = genesets.keySet().iterator(); j.hasNext();) {
				String geneset2_name = j.next().toString();
				GeneSet current_set = genesets.get(geneset2_name);

				//get the genes in the geneset
				Set<Integer> geneset_genes = current_set.getGenes();

				//if there is at least one gene in any of the genesets then the ids match.
				if(!geneset_genes.isEmpty())
					return true;

			}
		}
		if(params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_Specialized))
			return true;
		return false;

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
		return genes.values();
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
	
	@Deprecated // MKTODO this is here to support legacy session loading, TEMPORARY until a builder is available
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

	/*
	 * Given a set of genesets Go through the genesets and extract all the genes
	 * Return - hashmap of genes to hash keys (used to create an expression file
	 * when it is not present so user can use expression viewer to navigate
	 * genes in a geneset without have to generate their own dummy expression
	 * file)
	 */
	public Map<String, Integer> getGenesetsGenes(Map<String, GeneSet> current_genesets) {

		HashMap<String, Integer> genesetGenes = new HashMap<String, Integer>();

		for(Iterator j = current_genesets.keySet().iterator(); j.hasNext();) {

			String geneset_name = j.next().toString();
			GeneSet current_set = current_genesets.get(geneset_name);

			//compare the HashSet of dataset genes to the HashSet of the current Geneset
			//only keep the genes from the geneset that are in the dataset genes
			Set<Integer> geneset_genes = current_set.getGenes();

			for(Iterator k = geneset_genes.iterator(); k.hasNext();) {
				Integer current_genekey = (Integer) k.next();
				//get the current geneName
				if(genes.containsKey(current_genekey)) {
					String name = genes.get(current_genekey);
					genesetGenes.put(name, current_genekey);
				}

			}
		}
		return genesetGenes;

	}

	/*
	 * Filter all the genesets by the dataset genes If there are multiple sets
	 * of genesets make sure to filter by the specific dataset genes
	 */
	public void filterGenesets() {
		for(Iterator<String> k = datasets.keySet().iterator(); k.hasNext();) {
			DataSet current_set = datasets.get(k.next());
			//only filter the genesets if dataset genes are not null or empty
			if(current_set.getDatasetGenes() != null && !current_set.getDatasetGenes().isEmpty())
				current_set.getSetofgenesets().filterGenesets(current_set.getDatasetGenes());
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/*
	 * Return a hash of all the genesets in the set of genesets regardless of
	 * which dataset it comes from
	 */
	public Map<String, GeneSet> getAllGenesets() {
		//go through each dataset and get the genesets from each
		HashMap<String, GeneSet> all_genesets = new HashMap<String, GeneSet>();
		for(Iterator<String> k = datasets.keySet().iterator(); k.hasNext();) {
			Map<String, GeneSet> current_genesets = (datasets.get(k.next())).getSetofgenesets().getGenesets();
			all_genesets.putAll(current_genesets);
		}
		if(signatureGenesets != null && !signatureGenesets.isEmpty())
			all_genesets.putAll(signatureGenesets);
		return all_genesets;
	}

	/*
	 * Return a hash of all the genesets but not inlcuding the signature
	 * genesets.
	 */
	public Map<String, GeneSet> getEnrichmentGenesets() {
		//go through each dataset and get the genesets from each
		HashMap<String, GeneSet> all_genesets = new HashMap<String, GeneSet>();
		for(Iterator<String> k = datasets.keySet().iterator(); k.hasNext();) {
			Map<String, GeneSet> current_genesets = (datasets.get(k.next())).getSetofgenesets().getGenesets();
			all_genesets.putAll(current_genesets);
		}
		return all_genesets;
	}

	/*
	 * Return a hash of all the genesets in the set of genesets of interest
	 * regardless of which dataset it comes from
	 */
	public Map<String, GeneSet> getAllGenesetsOfInterest() {
		//go through each dataset and get the genesets from each
		HashMap<String, GeneSet> all_genesetsOfInterest = new HashMap<String, GeneSet>();
		for(Iterator<String> k = datasets.keySet().iterator(); k.hasNext();) {
			Map<String, GeneSet> current_genesets = (datasets.get(k.next())).getGenesetsOfInterest().getGenesets();
			all_genesetsOfInterest.putAll(current_genesets);
		}
		//if there are post analysis genesets, add them to the set of all genesets
		if(signatureGenesets != null && !signatureGenesets.isEmpty())
			all_genesetsOfInterest.putAll(signatureGenesets);
		return all_genesetsOfInterest;
	}

	public Map<String, GenesetSimilarity> getGenesetSimilarity() {
		return genesetSimilarity;
	}

	public void setGenesetSimilarity(Map<String, GenesetSimilarity> genesetSimilarity) {
		this.genesetSimilarity = genesetSimilarity;
	}
	
	public Map<String, DataSet> getDatasets() {
		return datasets;
	}

	public void setDatasets(Map<String, DataSet> datasets) {
		this.datasets = datasets;
	}

	/*
	 * Adds a new dataset of name datasetName. If dataset is null, it creates a
	 * new dataset
	 */
	public void addDataset(String datasetName, DataSet dataset) {
		if(this.datasets != null)
			this.datasets.put(datasetName, dataset);
		if(dataset == null)
			this.datasets.put(datasetName, new DataSet(this, datasetName, new DataSetFiles()));
	}

	public DataSet getDataset(String datasetname) {
		if(this.datasets != null && this.datasets.containsKey(datasetname))
			return this.datasets.get(datasetname);
		else
			return null;
	}

	public EnrichmentMapParameters getParams() {
		return params;
	}

	public void setParams(EnrichmentMapParameters params) {
		this.params = params;
	}

	public Set<String> getAllRankNames() {
		HashSet<String> allRankNames = new HashSet<String>();
		//go through each Dataset
		for(Iterator<String> k = datasets.keySet().iterator(); k.hasNext();) {
			String current_ds = k.next();
			//there could be duplicate ranking names for two different datasets. Add the dataset to the ranks name
			Set<String> all_names = (datasets.get(current_ds)).getExpressionSets().getAllRanksNames();
			for(Iterator<String> l = all_names.iterator(); l.hasNext();)
				allRankNames.add(l.next() + "-" + current_ds);

		}
		return allRankNames;
	}

	public Map<String, Ranking> getAllRanks() {
		HashMap<String, Ranking> allranks = new HashMap<String, Ranking>();
		for(Iterator<String> k = datasets.keySet().iterator(); k.hasNext();) {
			allranks.putAll((datasets.get(k.next())).getExpressionSets().getRanks());

		}
		return allranks;

	}

	public Ranking getRanksByName(String ranks_name) {

		//break the ranks file up by "-"
		//check to see if the rank file is dataset specific
		//needed for encoding the same ranking file name from two different dataset in the interface
		String ds = "";
		String rank = "";
		if(ranks_name.split("-").length == 2) {
			ds = ranks_name.split("-")[1];
			rank = ranks_name.split("-")[0];
		}

		for(Iterator<String> k = datasets.keySet().iterator(); k.hasNext();) {
			String current_dataset = k.next();
			if(!ds.equalsIgnoreCase("") && !rank.equalsIgnoreCase("")) {
				//check that this is the right dataset
				if(ds.equalsIgnoreCase(current_dataset)
						&& (datasets.get(current_dataset)).getExpressionSets().getAllRanksNames().contains(rank)) {
					return datasets.get(current_dataset).getExpressionSets().getRanksByName(rank);
				}
			} else if((datasets.get(current_dataset)).getExpressionSets().getAllRanksNames().contains(ranks_name)) {
				return datasets.get(current_dataset).getExpressionSets().getRanksByName(ranks_name);
			}
		}
		return null;
	}

	/*
	 * Return a hash of all different type of genesets from all the datasets
	 * regardless of which dataset it comes from
	 */
	public Set<String> getAllGenesetTypes() {
		//go through each dataset and get the genesets from each
		HashSet<String> all_genesetTypes = new HashSet<String>();
		for(Iterator<String> k = datasets.keySet().iterator(); k.hasNext();) {
			Set<String> current_genesets_types = (datasets.get(k.next())).getSetofgenesets().getGenesetTypes();
			all_genesetTypes.addAll(current_genesets_types);
		}
		return all_genesetTypes;
	}

	/**
	 * @param signatureGenesets
	 *            the signatureGenesets to set
	 */
	public void setSignatureGenesets(Map<String, GeneSet> signatureGenesets) {
		this.signatureGenesets = signatureGenesets;
	}

	/**
	 * @return the signatureGenesets
	 */
	public Map<String, GeneSet> getSignatureGenesets() {
		return signatureGenesets;
	}

}
