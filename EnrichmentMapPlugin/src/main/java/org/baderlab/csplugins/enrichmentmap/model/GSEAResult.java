package org.baderlab.csplugins.enrichmentmap.model;
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


/**
 * Class representing a specialized enrichment result generated from Gene set
 * enrichment Analysis(GSEa) GSEA enrichment result contain additional
 * information (as compared to a generic result) including Enrichment score(ES),
 * normalized Enrichment Score (NES), Family-wise error rate (FWER)
 */
public class GSEAResult extends EnrichmentResult {

	//enrichment score
	private final double ES;
	//normalized enrichment score
	private final double NES;
	//false discovery rate q-value
	private final double fdrqvalue;
	//family wise error rate (fwer) q-value
	private final double fwerqvalue;
	//the rank (off by two) of the gene that is at the apex of ES score calculation
	private int rankAtMax;
	//translate the rank at max to the corresponding score at the max
	private double scoreAtMax;

	/**
	 * Class Constructor
	 *
	 * @param name - gene set name
	 * @param size - gene set size
	 * @param ES - enrichment score
	 * @param NES - normalized enrichment score
	 * @param pvalue
	 * @param fdrqvalue
	 * @param fwerqvalue
	 */
	public GSEAResult(String name, int size, double ES, double NES, double pvalue, double fdrqvalue, double fwerqvalue, int rankAtMax, double scoreAtMax) {
		super(name, name, pvalue, size);
		this.ES = ES;
		this.NES = NES;
		this.fdrqvalue = fdrqvalue;
		this.fwerqvalue = fwerqvalue;
		this.rankAtMax = rankAtMax;
		this.scoreAtMax = scoreAtMax;
	}

	/**
	 * Class constructor - build GSEA result from tokenized line from a GSEA
	 * results file
	 *
	 * @param tokens - tokenized line from a GSEA results file
	 * @deprecated Parsing of the tokens should be done by calling code.
	 */
	@Deprecated
	public GSEAResult(String[] tokens) {
		super(tokens[1], tokens[1], Double.parseDouble(tokens[5]), Integer.parseInt(tokens[2]));
		
		//old session files will be missing rankatmax and scoreatmax
		if(tokens.length != 8 && tokens.length != 10)
			throw new IllegalArgumentException("Length of tokens[] must be 8 or 10, got: " + tokens.length);
		
		this.ES = Double.parseDouble(tokens[3]);
		this.NES = Double.parseDouble(tokens[4]);
		this.fdrqvalue = Double.parseDouble(tokens[6]);
		this.fwerqvalue = Double.parseDouble(tokens[7]);

		if(tokens.length == 10) {
			this.rankAtMax = Integer.parseInt(tokens[8]);
			this.scoreAtMax = Double.parseDouble(tokens[9]);
		} else {
			this.rankAtMax = -1;
			this.scoreAtMax = -1;
		}
	}

	//Each Enrichment Result must implement a method to determine
	//if the current enrichment result is of interest to the analysis or not
	//returns true if the enrichment passes both pvalue and qvalue cut-offs 
	//returns false if it doesn't pass one or both the pvalue or qvalue cut-offs
	@Override
	public boolean geneSetOfInterest(EnrichmentResultFilterParams params) {
//		if(params.getNESFilter() == NESFilter.POSITIVE && getNES() <= 0)
//			return false;
//		if(params.getNESFilter() == NESFilter.NEGATIVE && getNES() >= 0)
//			return false;
		
		return (getPvalue() <= params.getPvalue()) && (this.fdrqvalue <= params.getQvalue());
	}

	public double getES() {
		return ES;
	}

	public double getNES() {
		return NES;
	}

	public double getFdrqvalue() {
		return fdrqvalue;
	}

	public double getFwerqvalue() {
		return fwerqvalue;
	}

	public int getRankAtMax() {
		return rankAtMax;
	}
	
	public void setRankAtMax(int rankAtMax) {
		this.rankAtMax = rankAtMax;
	}

	public double getScoreAtMax() {
		return scoreAtMax;
	}

	public void setScoreAtMax(double scoreAtMax) {
		this.scoreAtMax = scoreAtMax;
	}

	public String toString() {
		return getName() + "\t" + getGsSize() + "\t" + ES + "\t" + NES + "\t" + getPvalue() + "\t" + fdrqvalue + "\t" + fwerqvalue
				+ "\t" + rankAtMax + "\t" + scoreAtMax;
	}

}
