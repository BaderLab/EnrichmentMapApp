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

package org.baderlab.csplugins.enrichmentmap.task;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet.Method;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResult;
import org.baderlab.csplugins.enrichmentmap.model.GSEAResult;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.baderlab.csplugins.enrichmentmap.model.Ranking;
import org.baderlab.csplugins.enrichmentmap.util.DiscreteTaskMonitor;
import org.baderlab.csplugins.enrichmentmap.util.NullTaskMonitor;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

/**
 * Task to create a subset of the geneset in the total gmt file that contains
 * only the genesets with pvalue and q-value less than threshold values
 * specified by the user.
 */

public class InitializeGenesetsOfInterestTask extends AbstractTask {

	private EnrichmentMap map;

	// TEMPORARY - this flag exists to turn off throwing of exception if a gene set is missing
	private boolean throwIfMissing = true;
	

	public InitializeGenesetsOfInterestTask(EnrichmentMap map) {
		this.map = map;
	}
	
	public void setThrowIfMissing(boolean throwIfMissing) {
		this.throwIfMissing = throwIfMissing;
	}
	

	/**
	 * filter the genesets, restricting them to only those passing the user
	 * specified thresholds.
	 * 
	 * @return true if successful and false otherwise.
	 */
	public boolean initializeSets(TaskMonitor tm) {
		if(tm == null)
			tm = new NullTaskMonitor();
		DiscreteTaskMonitor taskMonitor = new DiscreteTaskMonitor(tm, map.getDataSetCount());

		//create subset of genesets that contains only the genesets of interest with pvalue and qbalue less than values specified by the user.
		//Go through each Dataset populating the Gene set of interest in each dataset object
		Map<String, EMDataSet> datasets = map.getDataSets();
		
		// count how many experiments (DataSets) contain the geneset
		Optional<Integer> minExperiments = map.getParams().getMinExperiments();
		Map<String,Integer> occurrences = minExperiments.isPresent() ? new HashMap<>() : null;
		
		for(String datasetName : datasets.keySet()) {
			taskMonitor.inc();
			
			EMDataSet dataset = datasets.get(datasetName);

			// all these maps use the geneset name as key
			Map<String,EnrichmentResult> enrichmentResults = dataset.getEnrichments().getEnrichments();
			Map<String,GeneSet> genesets = dataset.getSetOfGeneSets().getGeneSets();
			Map<String,GeneSet> genesetsOfInterest = dataset.getGeneSetsOfInterest().getGeneSets();

			// If there are no genesets associated with this dataset then get the complete set assumption being that the gmt file applies to all datasets.
			if(genesets == null || genesets.isEmpty()) {
				genesets = map.getAllGeneSets();
			}

			//if there are no enrichment Results then do nothing
			if(enrichmentResults == null || enrichmentResults.isEmpty()) {
				return false;
			}
			
			//iterate through the GSEA Results to figure out which genesets we want to use
			for(String genesetName : enrichmentResults.keySet()) {
				EnrichmentResult result = enrichmentResults.get(genesetName);
				 
				// update rank at max for leading edge calculation
				if(dataset.getMethod() == Method.GSEA) {
					Ranking ranks = dataset.getExpressionSets().getRanksByName(datasetName);
					updateRankAtMax((GSEAResult)result, ranks);
				}
				
				GeneSet geneset = genesets.get(genesetName);
				if(geneset != null) {
					// while we are checking, update the size of the genesets based on post filtered data
					result.setGsSize(geneset.getGenes().size());
					
					if(result.geneSetOfInterest(map.getParams())) {
						if(occurrences != null) {
							occurrences.merge(genesetName, 1, (v,d) -> v + 1);
						}
						genesetsOfInterest.put(genesetName, geneset);
					}
				}
				else if(throwIfMissing) { // TEMPORARY
					throw new IllegalThreadStateException("The Geneset: " + genesetName + " is not found in the GMT file.");
				}
			}
		}
		
		// Remove gene-sets that don't pass the minimum occurrence cutoff
		if(occurrences != null) {
			for(EMDataSet dataset : datasets.values()) {
				Map<String,GeneSet> genesetsOfInterest = dataset.getGeneSetsOfInterest().getGeneSets();
				
				genesetsOfInterest.keySet().removeIf(geneset -> 
					occurrences.getOrDefault(geneset, 0) < minExperiments.get()
				);
			}
		}
		
		// MKTODO clear all the genesets that are not "of interest" just to free up memory
		
		return true;
	}

	private void updateRankAtMax(GSEAResult current_result, Ranking ranks) {
		//update the current geneset to reflect score at max
		if(ranks != null) {
			Set<Integer> allranks = ranks.getAllRanks();
			Integer largestRank = Collections.max(allranks);

			//get the max at rank for this geneset
			int currentRankAtMax = current_result.getRankAtMax();

			if(currentRankAtMax != -1) {
				//check the ES score.  If it is negative we need to adjust the rank to count from the end of the list
				double NES = current_result.getNES();
				int genekey = -1;
				//what gene corresponds to that rank
				if(NES < 0) {
					//it is possible that some of the proteins in the rank list won't be rank 2gene
					//conversion because some of the genes might not be in the genesets
					//so the size of the list can't be used to trace up from the bottom of the
					//ranks.  Instead we need to get the max rank used.
					currentRankAtMax = largestRank - currentRankAtMax;

					//reset the rank at max to reflect that it is counted from the bottom of the list.
					current_result.setRankAtMax(currentRankAtMax);
				}
				
				//check to see if this rank is in the conversion map
				if(ranks.containsRank(currentRankAtMax))
					genekey = ranks.getGene(currentRankAtMax);
				else {
					//if is possible that the gene associated with the max is not found in
					//our gene 2 rank conversions because the rank by GSEA are off by 1 or two
					//indexes (maybe a bug on their side).
					//so depending on the NES score we need to fiddle with the rank to find the
					//next protein that is the actual gene they are referring to

					while(genekey == -1 && (currentRankAtMax <= largestRank && currentRankAtMax > 0)) {
						if(NES < 0)
							currentRankAtMax = currentRankAtMax + 1;
						else
							currentRankAtMax = currentRankAtMax - 1;
						if(ranks.containsRank(currentRankAtMax))
							genekey = ranks.getGene(currentRankAtMax);
					}
				}

				if(genekey > -1) {
					//what is the score for that gene
					double scoreAtMax = ranks.getRank(genekey).getScore();
					current_result.setScoreAtMax(scoreAtMax);
					//update the score At max in the EnrichmentResults as well
				}
			}
		} // end of determining the leading edge
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Initializing subset of genesets and GSEA results of interest");
		initializeSets(taskMonitor);
		taskMonitor.setStatusMessage("");
	}

}
