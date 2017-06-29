package org.baderlab.csplugins.enrichmentmap.model;

import java.awt.Color;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


public class EMDataSet extends AbstractDataSet {
	
	public static enum Method {
		GSEA, Generic, Specialized;
		
		public String getLabel() {
			switch(this) {
				case GSEA: default: return "GSEA";
				case Generic:       return "Generic/gProfiler";
				case Specialized:   return "DAVID/BINGO/Great";
			}
		}
	}
	
	private Method method;
	private SetOfEnrichmentResults enrichments = new SetOfEnrichmentResults();
	private String expressionKey;
	private Color color;
	private SetOfGeneSets setOfGeneSets = new SetOfGeneSets();
	private DataSetFiles dataSetFiles;
	private Map<String, Ranking> ranks = new HashMap<>();
	
	protected EMDataSet(EnrichmentMap map, String name, Method method, DataSetFiles files) {
		super(map, name);
		this.method = method;

		//get the file name parameters for this map
		//initialize all the filenames from the parameters for this dataset
		if (name != null) {
			this.dataSetFiles = files;
			this.setOfGeneSets.setFilename(files.getGMTFileName());
			this.enrichments.setFilename1(files.getEnrichmentFileName1());
			this.enrichments.setFilename2(files.getEnrichmentFileName2());
			this.enrichments.setPhenotype1(files.getPhenotype1());
			this.enrichments.setPhenotype2(files.getPhenotype2());
		} else {
			System.out.println("There are no files initialized for this Dataset, named:" + name + "\n");
		}
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}
	
	public SetOfEnrichmentResults getEnrichments() {
		return enrichments;
	}
	
	public void setExpressionKey(String key) {
		this.expressionKey = key;
	}

	public void setEnrichments(SetOfEnrichmentResults enrichments) {
		this.enrichments = enrichments;
	}

	public synchronized GeneExpressionMatrix getExpressionSets() {
		EnrichmentMap map = getMap();
		GeneExpressionMatrix matrix = map.getExpressionMatrix(expressionKey);
		if(matrix == null) {
			// Avoid NPEs
			matrix = new GeneExpressionMatrix();
			String key = "Lazy_" + UUID.randomUUID().toString();
			setExpressionKey(key);
			map.putExpressionMatrix(key, matrix);
		}
		return matrix;
	}

	public SetOfGeneSets getSetOfGeneSets() {
		return setOfGeneSets;
	}

	public void setSetOfGeneSets(SetOfGeneSets setOfGeneSets) {
		this.setOfGeneSets = setOfGeneSets;
	}

	public Set<Integer> getDataSetGenes() {
		return Collections.unmodifiableSet(getExpressionSets().getGeneIds());
	}

	public DataSetFiles getDataSetFiles() {
		return dataSetFiles;
	}

	@Override
	public int hashCode() {
		final int prime = 11;
		int result = 5;
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EMDataSet other = (EMDataSet) obj;
		if (method != other.method)
			return false;
		if (getName() == null) {
			if (other.getName() != null)
				return false;
		} else if (!getName().equals(other.getName())) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "EMDataSet [name=" + getName() + ", method=" + method + "]";
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}
	
	public Map<String, Ranking> getRanks() {
		return ranks;
	}

	public void setRanks(Map<String, Ranking> ranks) {
		this.ranks = ranks;
	}

	public void addRanks(String ranks_name, Ranking new_rank) {
		if(this.ranks != null && ranks_name != null && new_rank != null)
			this.ranks.put(ranks_name, new_rank);
	}

	public Ranking getRanksByName(String ranks_name) {
		if(this.ranks != null) {
			return this.ranks.get(ranks_name);
		} else {
			return null;
		}
	}

	public Set<String> getAllRanksNames() {
		HashSet<String> allnames = new HashSet<String>();
		if(ranks != null && !ranks.isEmpty()) {
			for(Iterator<String> i = ranks.keySet().iterator(); i.hasNext();) {
				String current_name = (String) i.next();
				if(current_name != null)
					allnames.add(current_name);
			}
		}
		return allnames;
	}

	/**
	 * @return true if we have at least one list of gene ranks
	 */
	public boolean haveRanks() {
		if(this.ranks != null && this.ranks.size() > 0)
			return true;
		else
			return false;
	}

	public void createNewRanking(String name) {
		Ranking new_ranking = new Ranking();
		this.ranks.put(name, new_ranking);
	}

}
