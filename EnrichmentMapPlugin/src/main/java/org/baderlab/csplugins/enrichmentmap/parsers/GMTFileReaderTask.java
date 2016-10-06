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

package org.baderlab.csplugins.enrichmentmap.parsers;

import java.io.IOException;
import java.text.Normalizer;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.baderlab.csplugins.enrichmentmap.model.SetOfGeneSets;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.common.collect.ImmutableSet;

/**
 * Created by User: risserlin Date: Jan 8, 2009 Time: 11:59:17 AM
 * <p>
 * This class parses a GMT (gene set) file and creates a set of genesets
 */
public class GMTFileReaderTask extends AbstractTask {

	private EnrichmentMap map;

	//gene set file name
	private String GMTFileName;
	// gene hash (and inverse hash)

	//gene sets
	private SetOfGeneSets setOfgenesets;

	public final static int ENRICHMENT_GMT = 1, SIGNATURE_GMT = 2;


	/**
	 * @param DataSet - a gmt file is associated with a dataset
	 */
	public GMTFileReaderTask(DataSet dataset) {
		this.GMTFileName = dataset.getSetofgenesets().getFilename();
		this.setOfgenesets = dataset.getSetofgenesets();
		this.map = dataset.getMap();
	}

	/**
	 * for BuildDiseaseSignatureTask
	 *
	 * @param params
	 * @param genesets_file
	 */
	public GMTFileReaderTask(EnrichmentMap map, String gmtFileName, SetOfGeneSets setOfgensets, int genesets_file) {
		this.map = map;

		if(genesets_file == ENRICHMENT_GMT) {
			//open GMT file
			//this.GMTFileName = params.getGMTFileName();
			//this.genesets = params.getEM().getGenesets();
			//this.setOfgenesets = map.get
		} else if(genesets_file == SIGNATURE_GMT) {
			//open signature-GMT file
			this.GMTFileName = gmtFileName;
			this.setOfgenesets = setOfgensets;
		} else
			throw new IllegalArgumentException("argument not allowed:" + genesets_file);
	}
	
	
	

	private String deAccent(String str) {
		String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
		Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
		return pattern.matcher(nfdNormalizedString).replaceAll("");
	}

	/**
	 * parse GMT (gene set) file
	 */
	public void parse(TaskMonitor taskMonitor) throws IOException {
		Map<String, GeneSet> genesets = setOfgenesets.getGenesets();
		List<String> lines = DatasetLineParser.readLines(GMTFileName);
		
		int currentProgress = 0;

		taskMonitor.setStatusMessage("Parsing GMT file - " + lines.size() + " rows");
		try {
			for(String line : lines) {
				if(cancelled)
					throw new InterruptedException();

				String[] tokens = line.split("\t");

				//only go through the lines that have at least a gene set name and description.
				if(tokens.length >= 2) {
					final String name = tokens[0].toUpperCase().trim();
					final String description = tokens[1].trim();

					// set of genes keys
					ImmutableSet.Builder<Integer> builder = ImmutableSet.builder();

					// Calculate Percentage.  This must be a value between 0..100.
					int percentComplete = (int) (((double) currentProgress / lines.size()) * 100);
					taskMonitor.setProgress(percentComplete);
					currentProgress++;

					//All subsequent fields in the list are the geneset associated with this geneset.
					for(int j = 2; j < tokens.length; j++) {
						//Check to see if the gene is already in the hashmap of genes
						//if it is already in the hash then get its associated key and put it into the set of genes
						String gene = tokens[j].toUpperCase();
						if(map.containsGene(gene)) {
							builder.add(map.getHashFromGene(gene));
						}
						else if(!gene.isEmpty()) {
							Integer hash = map.addGene(gene).get();
							builder.add(hash);
						}
					}

					//finished parsing that geneset
					//add the current geneset to the hashmap of genesets
					GeneSet gs = new GeneSet(name, description, builder.build());
					genesets.put(name, gs);

					//add the geneset type to the list of types
					gs.getSource().ifPresent(source -> setOfgenesets.addGenesetType(source));
				}

			}
		} catch(InterruptedException e) {
			taskMonitor.setStatusMessage("Loading of GMT file cancelled");
		}
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("parsing GMT file");
		parse(taskMonitor);
	}
}
