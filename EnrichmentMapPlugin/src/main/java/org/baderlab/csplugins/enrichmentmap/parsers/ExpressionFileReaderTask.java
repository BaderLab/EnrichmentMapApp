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
import java.util.Map;

import org.apache.commons.math3.util.Precision;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpression;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpressionMatrix;
import org.baderlab.csplugins.enrichmentmap.util.MathUtil;
import org.baderlab.csplugins.enrichmentmap.util.NullTaskMonitor;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

/**
 * Parse expression file. The user can also use a rank file instead of an
 * expression file so this class also handles reading of rank files.
 */
public class ExpressionFileReaderTask extends AbstractTask {

	private final EMDataSet dataset;

	public ExpressionFileReaderTask(EMDataSet dataset) {
		this.dataset = dataset;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws IOException {
		parse(taskMonitor);
	}
	
	public GeneExpressionMatrix parse() throws IOException {
		return parse(null);
	}
	
	
	private GeneExpressionMatrix parse(TaskMonitor taskMonitor) throws IOException {
		taskMonitor = NullTaskMonitor.check(taskMonitor);
		taskMonitor.setTitle("Parsing Expression file");
		
		String fileName = dataset.getDataSetFiles().getExpressionFileName();
		LineReader lines = LineReader.create(fileName);
		
		try(lines) {
			return parseLines(lines);
		} catch(NumberFormatException e) {
			throw new IOException("Invalid number on line " + lines.getLineNumber() + " of expression file: '" + fileName + "'", e);
		} catch(Exception e) {
			throw new IOException("Could not parse line " + lines.getLineNumber() + " of expression file '" + fileName +  "'", e);
		} finally {
			taskMonitor.setProgress(1.0);
		}
	}
	
	
	private GeneExpressionMatrix parseLines(LineReader lineReader) throws IOException {

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
		
		int expressionUniverse = 0;
		boolean twoColumns = false;


		while(lineReader.hasMoreLines()) {
			String line = lineReader.nextLine();
			
			String[] tokens = line.split("\t");
			String name = tokens[0].toUpperCase().trim();

			//if this is the first line and the expression matrix if still empty and the column names are empty
			//Added column names empty for GSEA rank files that have no heading but after going through the loop
			//the first time we have given them default headings
			if(lineReader.getLineNumber() == 1 && (expressionMatrix == null || expressionMatrix.getExpressionMatrix().isEmpty()) && expressionMatrix.getColumnNames() == null) {
				//otherwise the first line is the header
				if(name.equalsIgnoreCase("#1.2")) {
					lineReader.nextLine();
					line = lineReader.nextLine();
				} else {
					while(line.startsWith("#")) {
						line = lineReader.nextLine(); //ignore all comment lines
					}
				}
				
				tokens = line.split("\t"); // May have skipped lines, need to split again
				
				if(tokens.length == 2) {
					twoColumns = true;
				}
				
				if(tokens.length == 2 && MathUtil.isNumber(tokens[1])) {
					// Found a data line, use default names for columns, continue to parse the data below
					expressionMatrix.setColumnNames(new String[] { "Name", "Rank/Score" });
					expressionMatrix.setNumConditions(expressionMatrix.getColumnNames().length);
					expressionMatrix.setExpressionMatrix(expression);
				} else {
					// Found a header line, use it for the column names, skip to next line
					expressionMatrix.setColumnNames(tokens);
					expressionMatrix.setNumConditions(expressionMatrix.getColumnNames().length);
					expressionMatrix.setExpressionMatrix(expression);
					
					continue; 
				}
			}

			//Check to see if this gene is in the genes list
			//Currently we only load gene expression data for genes that are already in the gene list (i.e. are listed in at least one geneset)
			//TODO:is there the possibility that we need all the expression genes?  Currently this great decreases space when saving sessions
			Integer genekey = map.getHashFromGene(name);
			if(genekey != null) {
				String description = "";
				if(twoColumns) {
					try {
						Double.parseDouble(tokens[1]);
					} catch(NumberFormatException e) {
						description = tokens[1];
					}
				} else {
					description = tokens[1];
				}

				float[] expressionsAsFloat = parseExpressions(tokens);
				GeneExpression expres = new GeneExpression(name, description, expressionsAsFloat);
				expression.put(genekey, expres);
			}
			expressionUniverse++;
		}

		//set the number of genes
		expressionMatrix.setExpressionUniverse(expressionUniverse);
		return expressionMatrix;
	}

	
	public static float[] parseExpressions(String[] expres) {
		// ignore the first two cells --> only if there are at least 3 cells
		int size = expres.length;
		float[] expression;
		
		if (size > 2) {
			expression = new float[size - 2];
			for (int i = 2; i < size; i++) {
				try {
					expression[i - 2] = parseAndRound(expres[i]);
				} catch(NumberFormatException e) {
					throw new NumberFormatException("The expression file contains the text '" + expres[i] + "' where a number was expected.");
				}
			}
		} else {
			expression = new float[1];
			try {
				expression[0] = parseAndRound(expres[1]);
			} catch (NumberFormatException e) {
				// if the column doesn't contain doubles then just assume that the expression file is empty
				expression[0] = 0.0f;
			}
		}
		return expression;
	}
	
	
	private static float parseAndRound(String exp) {
		float f = Float.parseFloat(exp);
		float r = Precision.round(f, 4);
		return r;
	}

	
}
