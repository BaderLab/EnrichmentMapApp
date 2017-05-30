/*
 *                       EnrichmentMap Cytoscape Plugin
 *
 * Copyright (c) 2008-2009 Bader Lab, Donnelly Centre for Cellular and Biomolecular
 * Research, University of Toronto
 *
 * Contact: http://www.baderlab.org
 *
 * Code written by: Ruth Isserlin
 * Authors: Daniele Merico, Ruth Isserlin, Oliver Stueker, Gary D. Bader
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * University of Toronto
 * has no obligations to provide maintenance, support, updates,
 * enhancements or modifications.  In no event shall the
 * University of Toronto
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * University of Toronto
 * has been advised of the possibility of such damage.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 *
 */

// $Id$
// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$
// $HeadURL$

package org.baderlab.csplugins.enrichmentmap.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class PostAnalysisParameters {

	final public static String SIGNATURE_INTERACTION_TYPE = "sig";
	@Deprecated
	final public static String SIGNATURE_INTERACTION_TYPE_SET1 = "sig_set1";
	@Deprecated
	final public static String SIGNATURE_INTERACTION_TYPE_SET2 = "sig_set2";
	
	
	public static enum AnalysisType {
		KNOWN_SIGNATURE, SIGNATURE_DISCOVERY
	}
	
	public static enum UniverseType {
		GMT, EXPRESSION_SET, INTERSECTION, USER_DEFINED
	}
	
	private final String name;
	private final AnalysisType analysisType;
	private final UniverseType universeType;
	private final PostAnalysisFilterParameters rankTestParameters;
	private final String signatureGMTFileName;
	private final SetOfGeneSets loadedGMTGeneSets;
	private final Collection<String> selectedGeneSetNames;
	private final Map<String,String> dataSetToRankFile; // only used by Mann-Whitney
	private final int userDefinedUniverseSize;
	private final String attributePrefix;
	private final Optional<String> datasetName;
	
	private PostAnalysisParameters(PostAnalysisParameters.Builder builder) {
		this.name = builder.name;
		this.analysisType = builder.analysisType;
		this.universeType = builder.universeType;
		this.rankTestParameters = builder.rankTestParameters;
		this.signatureGMTFileName = builder.signatureGMTFileName;
		this.loadedGMTGeneSets = builder.loadedGMTGeneSets;
		this.selectedGeneSetNames = builder.selectedGeneSetNames;
		this.dataSetToRankFile = builder.dataSetToRankFile;
		this.userDefinedUniverseSize = builder.userDefinedUniverseSize;
		this.attributePrefix = builder.attributePrefix;
		this.datasetName = builder.datasetName;
	}

	public String getName() {
		return name;
	}
	
	public Optional<String> getDataSetName() {
		return datasetName;
	}
	
	public AnalysisType getAnalysisType() {
		return analysisType;
	}
	
	public UniverseType getUniverseType() {
		return universeType;
	}
	
	public PostAnalysisFilterParameters getRankTestParameters() {
		return rankTestParameters;
	}
	
	public String getSignatureGMTFileName() {
		return signatureGMTFileName;
	}

	// MKTODO should be just one list of selected gene sets
	// Right now it stores all the gene sets that were loaded, and a list of the ones that were selected
	public SetOfGeneSets getLoadedGMTGeneSets() {
		return loadedGMTGeneSets;
	}

	public Collection<String> getSelectedGeneSetNames() {
		return selectedGeneSetNames;
	}
	
	public Map<String,String> getDataSetToRankFile() {
		return Collections.unmodifiableMap(dataSetToRankFile);
	}
	
	public int getUserDefinedUniverseSize() {
		return userDefinedUniverseSize;
	}

	public String getAttributePrefix() {
		return attributePrefix;
	}

	
	public static class Builder {
		
		private String name;
		private AnalysisType analysisType;
		private UniverseType universeType;
		private PostAnalysisFilterParameters rankTestParameters;
		private String signatureGMTFileName;
		private SetOfGeneSets loadedGMTGeneSets;
		private Set<String> selectedGeneSetNames = new HashSet<>();
		private Map<String,String> dataSetToRankFile = new HashMap<>();
		private int userDefinedUniverseSize;
		private String attributePrefix;
		private Optional<String> datasetName = Optional.empty();
		
		public Builder() {
			// defaults
			setSignatureGMTFileName("");
		}

		public static Builder from(PostAnalysisParameters other) {
			Builder b = new Builder();
			b.setName(other.name);
			b.setAnalysisType(other.analysisType);
			b.setRankTestParameters(other.rankTestParameters);
			b.setSignatureGMTFileName(other.signatureGMTFileName);
			b.setLoadedGMTGeneSets(other.loadedGMTGeneSets);
			b.addSelectedGeneSetNames(other.selectedGeneSetNames);
			b.addDataSetToRankFile(other.dataSetToRankFile);
			b.setUserDefinedUniverseSize(other.userDefinedUniverseSize);
			b.setAttributePrefix(other.attributePrefix);
			b.setDataSetName(other.datasetName.orElse(null));
			return b;
		}
		
		public Builder setName(String name) {
			this.name = name;
			return this;
		}
		
		public Builder setDataSetName(String datasetName) {
			this.datasetName = Optional.ofNullable(datasetName);
			return this;
		}
		
		public Builder addSelectedGeneSetNames(Collection<String> names) {
			selectedGeneSetNames.addAll(names);
			return this;
		}
		
		public Builder addSelectedGeneSetName(String name) {
			selectedGeneSetNames.add(name);
			return this;
		}

		public Builder setAnalysisType(AnalysisType analysisType) {
			this.analysisType = analysisType;
			return this;
		}
		
		public Builder setUniverseType(UniverseType universeType) {
			this.universeType = universeType;
			return this;
		}

		public Builder setRankTestParameters(PostAnalysisFilterParameters rankTestParameters) {
			this.rankTestParameters = rankTestParameters;
			return this;
		}

		public Builder setSignatureGMTFileName(String signatureGMTFileName) {
			this.signatureGMTFileName = signatureGMTFileName;
			return this;
		}
		
		public String getSignatureGMTFileName() {
			return signatureGMTFileName;
		}
		
		public Map<String,String> getDataSetToRankFile() {
			return dataSetToRankFile;
		}

		public Builder setLoadedGMTGeneSets(SetOfGeneSets loadedGMTGeneSets) {
			this.loadedGMTGeneSets = loadedGMTGeneSets;
			return this;
		}
		
		public Builder addDataSetToRankFile(String dataSet, String rankFile) {
			this.dataSetToRankFile.put(dataSet, rankFile);
			return this;
		}
		
		public Builder addDataSetToRankFile(Map<String,String> map) {
			this.dataSetToRankFile.putAll(map);
			return this;
		}

		public Builder setUserDefinedUniverseSize(int universeSize) {
			this.userDefinedUniverseSize = universeSize;
			return this;
		}

		public Builder setAttributePrefix(String attributePrefix) {
			this.attributePrefix = attributePrefix;
			return this;
		}
		
		public PostAnalysisParameters build() {
			return new PostAnalysisParameters(this);
		}
	}

}
