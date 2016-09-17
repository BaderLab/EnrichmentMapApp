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

package org.baderlab.csplugins.enrichmentmap;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.baderlab.csplugins.enrichmentmap.model.SetOfGeneSets;

import com.google.common.collect.ImmutableSet;

public class PostAnalysisParameters {

	final public static String SIGNATURE_INTERACTION_TYPE = "sig";
	final public static String SIGNATURE_INTERACTION_TYPE_SET1 = "sig_set1";
	final public static String SIGNATURE_INTERACTION_TYPE_SET2 = "sig_set2";
	
	public enum AnalysisType {
		KNOWN_SIGNATURE,
		SIGNATURE_DISCOVERY
	}
	
	private final AnalysisType analysisType;
	private final FilterParameters filterParameters;
	private final FilterParameters rankTestParameters;
	private final String signatureGMTFileName;
	private final SetOfGeneSets signatureGenesets;
	private final Collection<String> selectedSignatureSetNames;
	private final double currentNodePlacementYOffset;
	private final String signatureRankFile;
	private final String signatureDataSet;
	private final int universeSize;
	private final String attributePrefix;
	
	
	
	private PostAnalysisParameters(AnalysisType analysisType, FilterParameters filterParameters,
			FilterParameters rankTestParameters, String signatureGMTFileName, SetOfGeneSets signatureGenesets,
			Collection<String> selectedSignatureSetNames, double currentNodePlacementYOffset, String signatureRankFile,
			String signatureDataSet, int universeSize, String attributePrefix) {
		this.analysisType = analysisType;
		this.filterParameters = filterParameters;
		this.rankTestParameters = rankTestParameters;
		this.signatureGMTFileName = signatureGMTFileName;
		this.signatureGenesets = signatureGenesets;
		this.selectedSignatureSetNames = selectedSignatureSetNames;
		this.currentNodePlacementYOffset = currentNodePlacementYOffset;
		this.signatureRankFile = signatureRankFile;
		this.signatureDataSet = signatureDataSet;
		this.universeSize = universeSize;
		this.attributePrefix = attributePrefix;
	}

	public AnalysisType getAnalysisType() {
		return analysisType;
	}
	
	public FilterParameters getFilterParameters() {
		return filterParameters;
	}

	public FilterParameters getRankTestParameters() {
		return rankTestParameters;
	}
	
	public String getSignatureGMTFileName() {
		return signatureGMTFileName;
	}


	// MKTODO should be just one list of selected gene sets
	// Right now it stores all the gene sets that were loaded, and a list of the ones that were selected
	public SetOfGeneSets getSignatureGenesets() {
		return signatureGenesets;
	}

	public Collection<String> getSelectedSignatureSetNames() {
		return selectedSignatureSetNames;
	}
	
	
	public double getCurrentNodePlacementY_Offset() {
		return currentNodePlacementYOffset;
	}

	public String getSignatureRankFile() {
		return signatureRankFile;
	}

	public String getSignatureDataSet() {
		return signatureDataSet;
	}

	public int getUniverseSize() {
		return universeSize;
	}

	public String getAttributePrefix() {
		return attributePrefix;
	}

	

	
	public static class Builder {
		
		private AnalysisType analysisType;
		private FilterParameters filterParameters;
		private FilterParameters rankTestParameters;
		private String signatureGMTFileName;
		private SetOfGeneSets signatureGenesets;
		private Set<String> selectedSignatureSetNames = new HashSet<>();
		private double currentNodePlacementYOffset;
		private String signatureRankFile;
		private String signatureDataSet;
		private int universeSize;
		private String attributePrefix;
		
		
		public Builder() {
			// defaults
			setCurrentNodePlacementYOffset(0.0);
			setSignatureGMTFileName("");
		}

		public static Builder from(PostAnalysisParameters other) {
			Builder b = new Builder();
			b.setAnalysisType(other.analysisType);
			b.setFilterParameters(other.filterParameters);
			b.setRankTestParameters(other.rankTestParameters);
			b.setSignatureGMTFileName(other.signatureGMTFileName);
			b.setSignatureGenesets(other.signatureGenesets);
			b.addSelectedSignatureSetNames(other.selectedSignatureSetNames);
			b.setCurrentNodePlacementYOffset(other.currentNodePlacementYOffset);
			b.setSignatureRankFile(other.signatureRankFile);
			b.setSignatureDataSet(other.signatureDataSet);
			b.setUniverseSize(other.universeSize);
			b.setAttributePrefix(other.attributePrefix);
			return b;
		}
		
		public Builder addSelectedSignatureSetNames(Collection<String> names) {
			selectedSignatureSetNames.addAll(names);
			return this;
		}
		
		public Builder addSelectedSignatureSetName(String name) {
			selectedSignatureSetNames.add(name);
			return this;
		}

		public Builder setAnalysisType(AnalysisType analysisType) {
			this.analysisType = analysisType;
			return this;
		}


		public Builder setFilterParameters(FilterParameters filterParameters) {
			this.filterParameters = filterParameters;
			return this;
		}


		public Builder setRankTestParameters(FilterParameters rankTestParameters) {
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

		public Builder setSignatureGenesets(SetOfGeneSets signatureGenesets) {
			this.signatureGenesets = signatureGenesets;
			return this;
		}


		public Builder setCurrentNodePlacementYOffset(double currentNodePlacementYOffset) {
			this.currentNodePlacementYOffset = currentNodePlacementYOffset;
			return this;
		}


		public Builder setSignatureRankFile(String signatureRankFile) {
			this.signatureRankFile = signatureRankFile;
			return this;
		}


		public Builder setSignatureDataSet(String signatureDataSet) {
			this.signatureDataSet = signatureDataSet;
			return this;
		}


		public Builder setUniverseSize(int universeSize) {
			this.universeSize = universeSize;
			return this;
		}


		public Builder setAttributePrefix(String attributePrefix) {
			this.attributePrefix = attributePrefix;
			return this;
		}
		
		public PostAnalysisParameters build() {
			return new PostAnalysisParameters(analysisType, filterParameters,
					rankTestParameters, signatureGMTFileName, signatureGenesets,
					ImmutableSet.copyOf(selectedSignatureSetNames), currentNodePlacementYOffset, signatureRankFile,
					signatureDataSet, universeSize, attributePrefix);
		}
		
		
	}

	/**
	 * Checks all values of the PostAnalysisInputPanel
	 * 
	 * @return String with error messages (one error per line) or empty String if everything is okay.
	 * @see org.baderlab.csplugins.enrichmentmap.EnrichmentMapParameters#checkMinimalRequirements()
	 */
	public void checkMinimalRequirements(StringBuilder errors) {
		errors.append(checkGMTfiles());
		if(getSelectedSignatureSetNames().isEmpty()) {
			errors.append("No Signature Genesets selected \n");
		}
	}

	/**
	 * Checks if SignatureGMTFileName is provided and if the file can be read.
	 * 
	 * @return String with error messages (one error per line) or empty String
	 *         if everything is okay.
	 */
	public String checkGMTfiles() {
		String signatureGMTFileName = getSignatureGMTFileName();
		if(signatureGMTFileName == null || signatureGMTFileName.isEmpty() || !EnrichmentMapParameters.checkFile(signatureGMTFileName))
			return "Signature GMT file can not be found \n";
		return "";
	}

}
