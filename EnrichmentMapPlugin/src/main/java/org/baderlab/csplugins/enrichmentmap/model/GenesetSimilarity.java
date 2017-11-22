/**
 **                       EnrichmentMap Cytoscape Plugin
 **
 ** Copyright (c) 2008-2009 Bader Lab, Donnelly Centre for Cellular and Biomolecular 
 ** Research, University of Toronto
 **
 ** Contact: http://www.baderlab.org
 **
 ** Code written by: Ruth Isserlin
 ** Authors: Daniele Merico, Ruth Isserlin, Oliver Stueker, Gary D. Bader
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** (at your option) any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** University of Toronto
 ** has no obligations to provide maintenance, support, updates, 
 ** enhancements or modifications.  In no event shall the
 ** University of Toronto
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** University of Toronto
 ** has been advised of the possibility of such damage.  
 ** See the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **
 **/

// $Id$
// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$
// $HeadURL$

package org.baderlab.csplugins.enrichmentmap.model;

import java.util.Objects;
import java.util.Set;

/**
 * Created by User: risserlin Date: Jan 9, 2009 Time: 10:49:55 AM
 * <p>
 * Class representing a comparison of two gene sets (represents an edge in the network)
 */
public class GenesetSimilarity {

	private String geneset1Name;
	private String geneset2Name;

	// currently the interaction type is pp which actually means a protein protein interaction
	// but there is no specification of an enrichment interaction in cytoscape.
	private String interactionType;

	// either jaccard or overlap coeffecient, depends on statistic user specified.
	private double similarityCoeffecient;

	// set of genes in common to both gene sets.
	private Set<Integer> overlappingGenes;

	private String datasetName;

	/**
	 * @param geneset1Name - gene set 1 name
	 * @param geneset2Name - gene set 2 name
	 * @param similarityCoeffecient - jaccard or overlap coeffecient for geneset 1 and geneset 2
	 * @param overlappingGenes - set of genes in common to gene set 1 and gene set 2
	 * @param enrichment_set - the enrichment set the similarity comes from.
	 */
	public GenesetSimilarity(String geneset1Name, String geneset2Name, double similarityCoeffecient,
			String interactionType, Set<Integer> overlappingGenes, String datasetName) {
		Objects.requireNonNull(interactionType);
		
		this.geneset1Name = geneset1Name;
		this.geneset2Name = geneset2Name;
		this.similarityCoeffecient = similarityCoeffecient;
		this.overlappingGenes = overlappingGenes;
		this.interactionType = interactionType;
		this.datasetName = datasetName;
	}

	@Override
	public String toString() {
		return "GenesetSimilarity [geneset1Name=" + geneset1Name + ", geneset2Name=" + geneset2Name + "]";
	}

	public String getGeneset1Name() {
		return geneset1Name;
	}

	public void setGeneset1Name(String geneset1Name) {
		this.geneset1Name = geneset1Name;
	}

	public String getGeneset2Name() {
		return geneset2Name;
	}

	public void setGeneset2Name(String geneset2Name) {
		this.geneset2Name = geneset2Name;
	}

	public String getInteractionType() {
		return interactionType;
	}

	public double getSimilarityCoeffecient() {
		return similarityCoeffecient;
	}

	public void setSimilarityCoeffecient(double value) {
		this.similarityCoeffecient = value;
	}

	public Set<Integer> getOverlappingGenes() {
		return overlappingGenes;
	}

	public void setOverlappingGenes(Set<Integer> overlappingGenes) {
		this.overlappingGenes = overlappingGenes;
	}

	public int getSizeOfOverlap() {
		return overlappingGenes.size();
	}

	public String getDataSetName() {
		return datasetName;
	}


}
