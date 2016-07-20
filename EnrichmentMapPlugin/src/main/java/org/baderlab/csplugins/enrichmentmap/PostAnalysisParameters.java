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

import org.baderlab.csplugins.enrichmentmap.model.SetOfGeneSets;
import org.inferred.freebuilder.FreeBuilder;

@FreeBuilder
public interface PostAnalysisParameters {

	final public static String SIGNATURE_INTERACTION_TYPE = "sig";
	final public static String SIGNATURE_INTERACTION_TYPE_SET1 = "sig_set1";
	final public static String SIGNATURE_INTERACTION_TYPE_SET2 = "sig_set2";
	
	public enum AnalysisType {
		KNOWN_SIGNATURE,
		SIGNATURE_DISCOVERY
	}
	
	public AnalysisType getAnalysisType();
	
	
	public FilterParameters getFilterParameters();

	public FilterParameters getRankTestParameters();

	
	public String getSignatureGMTFileName();


	// MKTODO should be just one list of selected gene sets
	// Right now it stores all the gene sets that were loaded, and a list of the ones that were selected
	public SetOfGeneSets getSignatureGenesets();

	public Collection<String> getSelectedSignatureSetNames();
	
	
	public double getCurrentNodePlacementY_Offset();

	public String getSignature_rankFile();

	public String getSignature_dataSet();

	public int getUniverseSize();

	public String getAttributePrefix();

	

	
	class Builder extends PostAnalysisParameters_Builder {
		public Builder() {
			// defaults
			setCurrentNodePlacementY_Offset(0.0);
			setSignatureGMTFileName("");
		}
	}

	/**
	 * Checks all values of the PostAnalysisInputPanel
	 * 
	 * @return String with error messages (one error per line) or empty String if everything is okay.
	 * @see org.baderlab.csplugins.enrichmentmap.EnrichmentMapParameters#checkMinimalRequirements()
	 */
	public default void checkMinimalRequirements(StringBuilder errors) {
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
	public default String checkGMTfiles() {
		String signatureGMTFileName = getSignatureGMTFileName();
		if(signatureGMTFileName == null || signatureGMTFileName.isEmpty() || !EnrichmentMapParameters.checkFile(signatureGMTFileName))
			return "Signature GMT file can not be found \n";
		return "";
	}

}
