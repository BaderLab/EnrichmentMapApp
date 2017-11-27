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
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.baderlab.csplugins.enrichmentmap.task.postanalysis.FilterMetricSet;

public class PostAnalysisParameters {

	final public static String SIGNATURE_INTERACTION_TYPE = "sig";
	@Deprecated
	final public static String SIGNATURE_INTERACTION_TYPE_SET1 = "sig_set1";
	@Deprecated
	final public static String SIGNATURE_INTERACTION_TYPE_SET2 = "sig_set2";
	
	
	public static enum UniverseType {
		GMT, EXPRESSION_SET, INTERSECTION, USER_DEFINED
	}
	
	private final String name;
	private final FilterMetricSet rankTestParameters;
	private final SetOfGeneSets loadedGMTGeneSets;
	private final Collection<String> selectedGeneSetNames;
	private final String attributePrefix;
	private final Optional<String> datasetName;
	
	private PostAnalysisParameters(PostAnalysisParameters.Builder builder) {
		this.name = builder.name;
		this.rankTestParameters = builder.rankTestParameters;
		this.loadedGMTGeneSets = builder.loadedGMTGeneSets;
		this.selectedGeneSetNames = builder.selectedGeneSetNames;
		this.attributePrefix = builder.attributePrefix;
		this.datasetName = builder.datasetName;
	}

	public String getName() {
		return name;
	}
	
	public Optional<String> getDataSetName() {
		return datasetName;
	}
	
	public FilterMetricSet getRankTestParameters() {
		return rankTestParameters;
	}

	public SetOfGeneSets getLoadedGMTGeneSets() {
		return loadedGMTGeneSets;
	}

	public Collection<String> getSelectedGeneSetNames() {
		return selectedGeneSetNames;
	}
	
	public String getAttributePrefix() {
		return attributePrefix;
	}

	
	public static class Builder {
		
		private String name;
		private FilterMetricSet rankTestParameters;
		private SetOfGeneSets loadedGMTGeneSets;
		private Set<String> selectedGeneSetNames = new HashSet<>();
		private String attributePrefix;
		private Optional<String> datasetName = Optional.empty();
		

		public static Builder from(PostAnalysisParameters other) {
			Builder b = new Builder();
			b.setName(other.name);
			b.setRankTestParameters(other.rankTestParameters);
			b.setLoadedGMTGeneSets(other.loadedGMTGeneSets);
			b.addSelectedGeneSetNames(other.selectedGeneSetNames);
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

		public Builder setRankTestParameters(FilterMetricSet rankTestParameters) {
			this.rankTestParameters = rankTestParameters;
			return this;
		}
		
		public FilterMetricSet getRankTestParameters() {
			return rankTestParameters;
		}

		public Builder setLoadedGMTGeneSets(SetOfGeneSets loadedGMTGeneSets) {
			this.loadedGMTGeneSets = loadedGMTGeneSets;
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
