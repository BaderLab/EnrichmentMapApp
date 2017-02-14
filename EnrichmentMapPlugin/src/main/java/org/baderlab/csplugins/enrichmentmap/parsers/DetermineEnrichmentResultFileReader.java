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

import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.resolver.DataSetResolver;
import org.baderlab.csplugins.enrichmentmap.task.CreateGMTEnrichmentMapTask;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;

import com.google.common.base.Strings;

/**
 * Read a set of enrichment results. Results can be specific to a Gene set
 * enrichment analysis or to a generic enrichment analysis.
 * <br><br>
 * The two different results files are distinguished based on the number of
 * columns, a GSEA results file has exactly eleven columns. Any other number of
 * is assumed to be a generic results file. (It is possible that a generic file
 * also has exactly 11 column so if the file has 11 column the 5 and 6 column
 * headers are checked. If columns 5 and 6 are specified as ES and NES the file
 * is for sure a GSEA result file.)
 */
public class DetermineEnrichmentResultFileReader {

	//default Score at Max value
	public static final Double DefaultScoreAtMax = -1000000.0;
	
	private final DataSet dataset;
	

	public DetermineEnrichmentResultFileReader(DataSet dataset) {
		this.dataset = dataset;
	}

	/**
	 * Parse Enrichment results file
	 */
	public TaskIterator getParsers() {
		String enrichmentsFileName1 = dataset.getEnrichments().getFilename1();
		String enrichmentsFileName2 = dataset.getEnrichments().getFilename2();
		
		TaskIterator parserTasks = new TaskIterator();
		
		try {
			if(!Strings.isNullOrEmpty(enrichmentsFileName1)) {
				AbstractTask current = readFile(enrichmentsFileName1);
				if(current instanceof ParseGREATEnrichmentResults)
					parserTasks.append(new GREATWhichPvalueQuestionTask(dataset.getMap()));
				parserTasks.append(current);
			}
			
			if(!Strings.isNullOrEmpty(enrichmentsFileName2)) {
				parserTasks.append(readFile(enrichmentsFileName2));
			}
			
			//If both of the enrichment files are null then we want to default to building a gmt file only build
			if(Strings.isNullOrEmpty(enrichmentsFileName1) && Strings.isNullOrEmpty(enrichmentsFileName2)) {
				parserTasks.append(new CreateGMTEnrichmentMapTask(dataset));
			}
			
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		return parserTasks;
	}


	private AbstractTask readFile(String fileName) throws IOException {
		if(fileName.endsWith(".edb")) {
			return new ParseEDBEnrichmentResults(dataset);
		}
		else {
			DataSetResolver.Type fileType = DataSetResolver.guessEnrichmentType(fileName);
			switch(fileType) {
				case ENRICHMENT_GSEA:  return new ParseGSEAEnrichmentResults(dataset);
				case ENRICHMENT_BINGO: return new ParseBingoEnrichmentResults(dataset);
				case ENRICHMENT_GREAT: return new ParseGREATEnrichmentResults(dataset);
				case ENRICHMENT_DAVID: return new ParseDavidEnrichmentResults(dataset);
				default:               return new ParseGenericEnrichmentResults(dataset);
			}
		}
	}

}
