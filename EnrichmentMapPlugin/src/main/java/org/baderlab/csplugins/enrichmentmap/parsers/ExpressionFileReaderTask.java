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

// $Id: ExpressionFileReaderTask.java 371 2009-09-25 20:24:18Z risserlin $
// $LastChangedDate: 2009-09-25 16:24:18 -0400 (Fri, 25 Sep 2009) $
// $LastChangedRevision: 371 $
// $LastChangedBy: risserlin $
// $HeadURL: svn+ssh://risserlin@server1.baderlab.med.utoronto.ca/svn/EnrichmentMap/trunk/EnrichmentMapPlugin/src/org/baderlab/csplugins/enrichmentmap/ExpressionFileReaderTask.java $

package org.baderlab.csplugins.enrichmentmap.parsers;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpression;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpressionMatrix;
import org.baderlab.csplugins.enrichmentmap.util.NullTaskMonitor;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

/**
 * Parse expression file. The user can also use a rank file instead of an
 * expression file so this class also handles reading of rank files.
 */
public class ExpressionFileReaderTask extends AbstractTask {

	private final EMDataSet dataset;

	/**
	 * @param dataset - dataset expression file is associated with
	 */
	public ExpressionFileReaderTask(EMDataSet dataset) {
		this.dataset = dataset;
	}

	/**
	 * Parse expression/rank file
	 */
	public GeneExpressionMatrix parse() throws IOException {
		return parse(null);
	}
	
	/**
	 * Parse expression/rank file
	 */
	public GeneExpressionMatrix parse(TaskMonitor taskMonitor) throws IOException {
		if(taskMonitor == null)
			taskMonitor = new NullTaskMonitor();

		//Need to check if the file specified as an expression file is actually a rank file
		//If it is a rank file it can either be 5 or 2 columns but it is important that the rank
		//value is extracted from the right column and placed in the expression matrix as if it
		//was an expression value in order for other features to work.

		//Also a problem with old session files that imported a rank file so it also
		//important to check if the file only has two columns.  If it only has two columns,
		//check to see if the second column is a double.  If it is then consider that column
		//expression

		EnrichmentMap map = dataset.getMap();
		String expressionFileName = dataset.getDataSetFiles().getExpressionFileName();
		dataset.setExpressionKey(expressionFileName);
		
		// Check to see if this expression file has already been loaded
		GeneExpressionMatrix existingExpressionMatrix = map.getExpressionMatrix(expressionFileName);
		if(existingExpressionMatrix != null) {
			return existingExpressionMatrix;
		}
		
		GeneExpressionMatrix expressionMatrix = new GeneExpressionMatrix();
		map.putExpressionMatrix(expressionFileName, expressionMatrix);
		
		Map<Integer, GeneExpression> expression = expressionMatrix.getExpressionMatrix();
		
		List<String> lines = LineReader.readLines(expressionFileName);
		int currentProgress = 0;
		int maxValue = lines.size();
		int expressionUniverse = 0;
		boolean twoColumns = false;

		taskMonitor.setStatusMessage("Parsing GCT file - " + maxValue + " rows");
		
		for(int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			String[] tokens = line.split("\t");

			//The first column of the file is the name of the geneset
			String Name = tokens[0].toUpperCase().trim();

			//if this is the first line and the expression matrix if still empty and the column names are empty
			//Added column names empty for GSEA rank files that have no heading but after going through the loop
			//the first time we have given them default headings
			if(i == 0 && (expressionMatrix == null || expressionMatrix.getExpressionMatrix().isEmpty()) && expressionMatrix.getColumnNames() == null) {
				//otherwise the first line is the header
				if(Name.equalsIgnoreCase("#1.2")) {
					line = lines.get(2);
					i = 2;
				} else {
					line = lines.get(0);
					//ignore all comment lines
					int k = 0;
					while(line.startsWith("#")) {
						k++;
						line = lines.get(k);
					}
					i = k;
				}
				tokens = line.split("\t");

				//check to see how many columns there are
				//if there are only 2 columns then we could be dealing with a ranked file
				//check to see if the second column contains expression values.
				if(tokens.length == 2) {
					twoColumns = true;
					//the assumption is the first line is the column names but
					//if we are loading a GSEA edb rnk file then their might not be column names
					try {
						int temp = Integer.parseInt(tokens[1]);
						i = -1;
						tokens[0] = "Name";
						tokens[1] = "Rank/Score";
					} catch(NumberFormatException v) {
						try {
							double temp2 = Double.parseDouble(tokens[1]);
							i = -1;
							tokens[0] = "Name";
							tokens[1] = "Rank/Score";

						} catch(NumberFormatException v2) {
							//if it isn't a double or int then we have a title line.
						}
					}
				}

				//expressionMatrix = new GeneExpressionMatrix(tokens);
				expressionMatrix.setColumnNames(tokens);
				expressionMatrix.setNumConditions(expressionMatrix.getColumnNames().length);
				expressionMatrix.setExpressionMatrix(expression);

				continue;
			}

			//Check to see if this gene is in the genes list
			//Currently we only load gene expression data for genes that are already in the gene list (i.e. are listed in at least one geneset)
			//TODO:is there the possibility that we need all the expression genes?  Currently this great decreases space when saving sessions
			Integer genekey = map.getHashFromGene(Name);
			if(genekey != null) {
				String description = "";
				//check to see if the second column is parseable
				if(twoColumns) {
					try {
						Double.parseDouble(tokens[1]);
					} catch(NumberFormatException e) {
						description = tokens[1];
					}
				} else {
					description = tokens[1];
				}

				GeneExpression expres = new GeneExpression(Name, description);
				expres.setExpression(tokens);
				expression.put(genekey, expres);
			}
			expressionUniverse++;

			// Calculate Percentage.  This must be a value between 0..100.
			int percentComplete = (int) (((double) currentProgress / maxValue) * 100);
			taskMonitor.setProgress(percentComplete);
			
			currentProgress++;
		}

		//set the number of genes
		expressionMatrix.setExpressionUniverse(expressionUniverse);
		//row Normalize expressionset
		expressionMatrix.rowNormalizeMatrix();

		return expressionMatrix;

		//TODO: intialize phenotypes associated with class files from expression file load
		/*
		 * if(dataset == 1){ //set up the classes definition if it is set.
		 * //check to see if the phenotypes were already set in the params from
		 * a session load if(params.getTemp_class1() != null)
		 * expressionMatrix.setPhenotypes(params.getTemp_class1());
		 * if(params.getClassFile1() != null)
		 * expressionMatrix.setPhenotypes(setClasses( params.getClassFile1()));
		 * //params.getEM().addExpression(EnrichmentMap.DATASET1,
		 * expressionMatrix); } else{ //set up the classes definition if it is
		 * set.
		 * 
		 * //check to see if the phenotypes were already set in the params from
		 * a session load if(params.getTemp_class2() != null)
		 * expressionMatrix.setPhenotypes(params.getTemp_class2()); else
		 * if(params.getClassFile2() != null)
		 * expressionMatrix.setPhenotypes(setClasses( params.getClassFile2()));
		 * //params.getEM().addExpression(EnrichmentMap.DATASET2,
		 * expressionMatrix); }
		 */

	}


	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Parsing GCT file");
		parse(taskMonitor);
	}
}
