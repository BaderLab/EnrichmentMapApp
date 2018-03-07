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
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.baderlab.csplugins.enrichmentmap.model.SetOfGeneSets;
import org.baderlab.csplugins.enrichmentmap.util.NullTaskMonitor;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;

import com.google.common.collect.ImmutableSet;

/**
 * This class parses a GMT (gene set) file and creates a set of genesets
 */
public class GMTFileReaderTask extends AbstractTask implements ObservableTask {

	private final EnrichmentMap map;
	private final SetOfGeneSets setOfGeneSets;
	private final Supplier<String> fileNameSupplier;
	private final Consumer<SetOfGeneSets> geneSetConsumer;


	public GMTFileReaderTask(EMDataSet dataset) {
		this.map = dataset.getMap();
		this.fileNameSupplier = () -> dataset.getDataSetFiles().getGMTFileName();
		this.setOfGeneSets = dataset.getSetOfGeneSets();
		this.geneSetConsumer = null;
	}
	
	public GMTFileReaderTask(EnrichmentMap map, String fileName, SetOfGeneSets geneSets) {
		this.map = map;
		this.fileNameSupplier = () -> fileName;
		this.setOfGeneSets = geneSets;
		this.geneSetConsumer = null;
		
	}
	public GMTFileReaderTask(EnrichmentMap map, Supplier<String> fileNameSupplier, Consumer<SetOfGeneSets> geneSetConsumer) {
		this.map = map;
		this.fileNameSupplier = fileNameSupplier;
		this.setOfGeneSets = new SetOfGeneSets();
		this.geneSetConsumer = geneSetConsumer;
	}
	
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if(taskMonitor == null)
			taskMonitor = new NullTaskMonitor();
		taskMonitor.setTitle("Parsing GMT file");
		parse();
	}
	
	public void parse() throws IOException, InterruptedException {
		String fileName = fileNameSupplier.get();
		try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
			for (String line; (line = reader.readLine()) != null;) {
				if (cancelled) {
					throw new InterruptedException();
				}
				GeneSet gs = readGeneSet(map, line);
				if (gs != null && setOfGeneSets != null) {
					Map<String, GeneSet> genesets = setOfGeneSets.getGeneSets();
					genesets.put(gs.getName(), gs);
				}
				if(geneSetConsumer != null) {
					geneSetConsumer.accept(setOfGeneSets);
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
				Integer hash = map.addGene(tokens[i]);
				if(hash != null)
					builder.add(hash);
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

	@Override
	public <R> R getResults(Class<? extends R> type) {
		if(SetOfGeneSets.class.equals(type)) {
			return type.cast(setOfGeneSets);
		}
		return null;
	}
}
