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
import java.util.Map;
import java.util.Set;

import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.Method;
import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResult;
import org.baderlab.csplugins.enrichmentmap.model.GSEAResult;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.baderlab.csplugins.enrichmentmap.model.GenericResult;
import org.baderlab.csplugins.enrichmentmap.model.Ranking;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

/**
 * Task to create a subset of the geneset in the total gmt file that contains
 * only the genesets with pvalue and q-value less than threshold values
 * specified by the user.
 */

public class InitializeGenesetsOfInterestTask extends AbstractTask {

	private EnrichmentMap map;



	public InitializeGenesetsOfInterestTask(EnrichmentMap map) {
		this.map = map;
	}

	/**
	 * filter the genesets, restricting them to only those passing the user
	 * specified thresholds.
	 * 
	 * @return true if successful and false otherwise.
	 */
	public boolean initializeSets(TaskMonitor taskMonitor) {
		if(taskMonitor == null)
			taskMonitor = new NullTaskMonitor();

		//create subset of genesets that contains only the genesets of interest with pvalue and qbalue less than values
		//specified by the user.

		//Go through each Dataset populating the Gene set of interest in each dataset object
		Map<String, DataSet> datasets = map.getDatasets();
		for(String current_dataset_name : datasets.keySet()) {
			DataSet current_dataset = datasets.get(current_dataset_name);

			Map<String, EnrichmentResult> enrichmentResults = current_dataset.getEnrichments().getEnrichments();
			Map<String, GeneSet> genesets = current_dataset.getSetofgenesets().getGenesets();
			Map<String, GeneSet> genesetsOfInterest = current_dataset.getGenesetsOfInterest().getGenesets();

			//If there are no genesets associated with this dataset then get the complete set
			//assumption being that the gmt file applies to all datasets.
			if(genesets == null || genesets.isEmpty()) {
				genesets = map.getAllGenesets();
				//genesetsOfInterest = map.getAllGenesetsOfInterest();
			}

			//get ranking files.
			Ranking ranks = current_dataset.getExpressionSets().getRanksByName(current_dataset_name);

//			Map<Integer, Rank> gene2rank = null;
//			if(ranks != null) {
//				gene2rank = ranks.getRanking();
//			}
			int currentProgress = 0;
			int maxValue = enrichmentResults.size();

			taskMonitor.setStatusMessage("Initializing " + maxValue + " genesets");

			//if there are no enrichment Results then do nothing
			if(enrichmentResults == null || enrichmentResults.isEmpty())
				return false;

			//iterate through the GSEA Results to figure out which genesets we want to use
			for(String current_name : enrichmentResults.keySet()) {

				// Calculate Percentage.  This must be a value between 0..100.
				int percentComplete = (int) (((double) currentProgress / maxValue) * 100);
				taskMonitor.setProgress(percentComplete);
				currentProgress++;

				//check geneset result to see if it meets the required cutoffs

				//if it is a GSEA Result then
				if(map.getParams().getMethod() == Method.GSEA) {
					GSEAResult current_result = (GSEAResult) enrichmentResults.get(current_name);

					//update the current geneset to reflect score at max
					if(ranks != null) {

						Set<Integer> allranks = ranks.getAllRanks();
						
						Integer largestRank = Collections.max(allranks);

						//get the max at rank for this geneset
						int currentRankAtMax = current_result.getRankAtMax();

						if(currentRankAtMax != -1) {
							//check the ES score.  If it is negative we need to adjust the
							//rank to count from the end of the list
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
					
					if(current_result.geneSetOfInterest(map.getParams().getPvalue(), map.getParams().getQvalue())) {
						//check to see that the geneset in the results file is in the geneset table
						//if it isn't then the user has given two files that don't match up
						if(genesets.containsKey(current_name)) {
							GeneSet current_set = genesets.get(current_name);
							//while we are checking, update the size of the genesets based on post filtered data
							current_result.setGsSize(current_set.getGenes().size());
							genesetsOfInterest.put(current_name, current_set);
						} else if(genesetsOfInterest.containsKey(current_name)) {
							//already found in genesets of interest - loading from session
						} else {
							throw new IllegalThreadStateException("The Geneset: " + current_name + " is not found in the GMT file.");
						}
					}
					//if this result is not one of interest, still a good idea to update its size as might be significant in another dataset
					else {
						if(genesets.containsKey(current_name)) {
							GeneSet current_set = (GeneSet) genesets.get(current_name);
							//while we are checking, update the size of the genesets based on post filtered data
							current_result.setGsSize(current_set.getGenes().size());
						}
					}
				}
				//otherwise it is a generic or David enrichment set
				else {
					GenericResult current_result = (GenericResult) enrichmentResults.get(current_name);

					if(current_result.geneSetOfInterest(map.getParams().getPvalue(), map.getParams().getQvalue(), map.getParams().isFDR())) {

						//check to see that the geneset in the results file is in the geneset talbe
						//if it isn't then the user has given two files that don't match up
						if(genesets.containsKey(current_name)) {
							GeneSet current_set = (GeneSet) genesets.get(current_name);
							//while we are checking, update the size of the genesets based on post filtered data
							current_result.setGsSize(current_set.getGenes().size());
							genesetsOfInterest.put(current_name, current_set);

						} else if(genesetsOfInterest.containsKey(current_name)) {
							//this geneset is already in the set of interest - loaded in from session
						} else {
							throw new IllegalThreadStateException("The Geneset: " + current_name + " is not found in the GMT file.");
						}

					}
					//if this result is not one of interest, still a good idea to update its size as might be significant in another dataset
					else {
						if(genesets.containsKey(current_name)) {
							GeneSet current_set = (GeneSet) genesets.get(current_name);
							//while we are checking, update the size of the genesets based on post filtered data
							current_result.setGsSize(current_set.getGenes().size());
						}
					}
				}
			}

		} // end of current dataset.  If more than one dataset repeat process.

		return true;
	}


	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Initializing subset of genesets and GSEA results of interest");
		initializeSets(taskMonitor);
	}

}
