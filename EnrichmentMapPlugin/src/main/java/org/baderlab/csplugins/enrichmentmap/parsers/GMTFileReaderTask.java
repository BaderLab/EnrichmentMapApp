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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.Normalizer;
import java.util.Map;
import java.util.regex.Pattern;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.baderlab.csplugins.enrichmentmap.model.SetOfGeneSets;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.common.collect.ImmutableSet;

/**
 * This class parses a GMT (gene set) file and creates a set of genesets
 */
public class GMTFileReaderTask extends AbstractTask {

	private final EnrichmentMap map;
	private final String gmtFileName;
	private final SetOfGeneSets setOfgenesets;


	public GMTFileReaderTask(EMDataSet dataset) {
		this.map = dataset.getMap();
		this.gmtFileName = dataset.getSetOfGeneSets().getFilename();
		this.setOfgenesets = dataset.getSetOfGeneSets();
	}
	
	/**
	 * for BuildDiseaseSignatureTask
	 */
	public GMTFileReaderTask(EnrichmentMap map, String gmtFileName, SetOfGeneSets setOfgensets) {
		this.map = map;
		this.gmtFileName = gmtFileName;
		this.setOfgenesets = setOfgensets;
	}
	
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Parsing GMT file");
		parse();
	}
	
	public void parse() throws IOException, InterruptedException {
		try(BufferedReader reader = new BufferedReader(new FileReader(gmtFileName))) {
		    for(String line; (line = reader.readLine()) != null;) {
		    	if(cancelled) {
					throw new InterruptedException();
		    	}
		    	GeneSet gs = readGeneSet(map, line);
		    	if(gs != null && setOfgenesets != null) {
		    		Map<String, GeneSet> genesets = setOfgenesets.getGeneSets();
					genesets.put(gs.getName(), gs);
		    	}
		    }
		}
	}

	private static GeneSet readGeneSet(EnrichmentMap map, String line) {
		String[] tokens = line.split("\t");
		//only go through the lines that have at least a gene set name and description.
		if(tokens.length >= 2) {
			// set of genes keys
			ImmutableSet.Builder<Integer> builder = ImmutableSet.builder();
			
			String name = tokens[0].toUpperCase().trim();
			String description = tokens[1].trim();
			
			for(int i = 2; i < tokens.length; i++) {
				String gene = tokens[i].toUpperCase();
				if(map.containsGene(gene)) {
					builder.add(map.getHashFromGene(gene));
				}
				else if(!gene.isEmpty()) {
					Integer hash = map.addGene(gene).get();
					builder.add(hash);
				}
			}
			return new GeneSet(name, description, builder.build());
		}
		return null;
	}
	
	
	private String deAccent(String str) {
		String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
		Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
		return pattern.matcher(nfdNormalizedString).replaceAll("");
	}
	
}
