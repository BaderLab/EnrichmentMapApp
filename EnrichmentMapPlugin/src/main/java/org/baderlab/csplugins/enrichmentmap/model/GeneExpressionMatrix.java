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

package org.baderlab.csplugins.enrichmentmap.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Class representing a set of genes/proteins expresion profile
 */
public class GeneExpressionMatrix {

	//name of columns - specified by first or second row in the expression matrix
	private String[] columnNames;
	//number of conditions - number of columns
	private int numConditions;
	private int expressionUniverse;

	//Store two instances of the expression matrix, one with the raw expression values
	//and one with the row normalized values.  The row normalizes values are stored as opposed
	//to being computing on the fly to decrease the time needed to update a heatmap.
	private Map<Integer, GeneExpression> expressionMatrix = new HashMap<>();
	private transient Map<Integer, GeneExpression> expressionMatrix_rowNormalized = null;

	//maximum expression value of all expression values in the array - computed as matrix is
	//loaded in.
	private double maxExpression = -1000000;

	//minimun expression value of all expresssion values in the array - computed as matrix
	//is loaded in.
	private double minExpression = 10000000;

	//value closest to zero for the entire expression set (above zero) used for log scaling
	private double closesttoZero = 10000000;

	//phenotype designation of each column
	private String[] phenotypes;
	private String phenotype1;
	private String phenotype2;

	//File associated with this expression set
	private String filename;

	public GeneExpressionMatrix() {
	}

	public GeneExpressionMatrix(String filename) {
		this.filename = filename;
	}


//	/*
//	 * Given an array of strings set the column names up from given string
//	 */
//	public void SetColumnNames() {
//		numConditions = columnNames.length;
//
//		//As a bypass for people who want to run Enrichment map without expression data
//		//if the expression file only contains 2 columns (name and description) then make a dummy
//		//expression matrix with no expression data.
//		if(numConditions == 2) {
//			numConditions = 3;
//			String[] newNames = new String[3];
//
//			//the first column is the name and the second column is description
//			//then add a third column with no data
//			//otherwise assume this is a rank file and it is missing the description files
//
//			if(columnNames[1].equalsIgnoreCase("description")) {
//				newNames[0] = columnNames[0];
//				newNames[1] = columnNames[1];
//				newNames[2] = "NO DATA";
//			} else {
//				newNames[0] = columnNames[0];
//				newNames[1] = "description";
//				newNames[2] = columnNames[1];
//			}
//			this.columnNames = newNames;
//		}
//	}

	/**
	 * Get a subset of the expression matrix containing only the set of given
	 * genes
	 *
	 * @param subset
	 *            - hasset of integers representing the hash keys of the genes
	 *            to be included in the expression subset
	 * @return Hashmap of gene Hashkeys and there gene expression set for the
	 *         specified gene hashkeys
	 */
	@Deprecated
	public HashMap<Integer, GeneExpression> getExpressionMatrix(HashSet<Integer> subset) {

		if((subset == null) || (subset.size() == 0))
			return null;

		HashMap<Integer, GeneExpression> expression_subset = new HashMap<Integer, GeneExpression>();

		//go through the expression matrix and get the subset of
		//genes of interest
		for(Iterator<Integer> i = subset.iterator(); i.hasNext();) {
			Integer k = i.next();
			if(expressionMatrix.containsKey(k)) {
				expression_subset.put(k, expressionMatrix.get(k));
			} else {
				//With the implementation of Two distinct expression files it is possible that an expression
				//set will not contain a gene 
				//System.out.println("how is this key not in the hashmap?");
			}

		}

		return expression_subset;

	}

	/**
	 * Get the current maximum value of the given subset of the expression
	 * matrix
	 *
	 * @param currentMatrix
	 *            - subset of gene expression matrix
	 * @return maximum expression value of the expression subset
	 */
	public double getMaxExpression(Map<Integer, GeneExpression> currentMatrix) {
		double max = 0.0;
		if(currentMatrix != null) {
			//go through the expression matrix
			for(Iterator<Integer> i = currentMatrix.keySet().iterator(); i.hasNext();) {
				double[] currentRow = ((GeneExpression) currentMatrix.get(i.next())).getExpression();
				for(int j = 0; j < currentRow.length; j++) {
					if(max < currentRow[j])
						max = currentRow[j];
				}

			}
		}
		return max;

	}

	/**
	 * Get the current minimum value of the given subset of the expression
	 * matrix
	 *
	 * @param currentMatrix
	 *            - subset of gene expression matrix
	 * @return minimum expression value of the expression subset
	 */
	public double getMinExpression(Map<Integer, GeneExpression> currentMatrix) {
		double min = 0.0;
		//go through the expression matrix
		if(currentMatrix != null) {
			for(Iterator<Integer> i = currentMatrix.keySet().iterator(); i.hasNext();) {
				double[] currentRow = ((GeneExpression) currentMatrix.get(i.next())).getExpression();
				for(int j = 0; j < currentRow.length; j++) {
					if(min > currentRow[j])
						min = currentRow[j];
				}
			}
		}
		return min;

	}

	/**
	 * Compute the row Normalized version of the current expression matrix. Row
	 * Normalization involves computing the mean and standard deviation for each
	 * row in the matrix. Each value in that specific row has the mean
	 * subtracted and is divided by the standard deviation. Row normalization is
	 * computed lazily and cached with the expression matrix. 
	 * (Log normalization is computed on the fly)
	 */
	public synchronized Map<Integer, GeneExpression> rowNormalizeMatrix() {
		if (expressionMatrix_rowNormalized == null) {
			expressionMatrix_rowNormalized = new HashMap<Integer, GeneExpression>();

			for (Integer key : expressionMatrix.keySet()) {
				GeneExpression expression = (GeneExpression) expressionMatrix.get(key);
				GeneExpression norm_row = new GeneExpression(expression.getName(), expression.getDescription());
				double[] row_normalized = expression.rowNormalize();
				norm_row.setExpression(row_normalized);
				expressionMatrix_rowNormalized.put(key, norm_row);
			}
		}
		return expressionMatrix_rowNormalized;
	}

	//Getters and Setters

	public String[] getColumnNames() {
		return columnNames;
	}

	public void setColumnNames(String[] columnNames) {
		if(columnNames.length == 2) {
			String[] new_names = new String[3];
			new_names[0] = columnNames[0];
			new_names[1] = "Description";
			new_names[2] = columnNames[1];
			this.columnNames = new_names;
		} else
			this.columnNames = columnNames;
	}

	public void setExpressionUniverse(int size) {
		this.expressionUniverse = size;
	}

	public int getExpressionUniverse() {
		return expressionUniverse;
	}

	public int getNumConditions() {
		return numConditions;
	}

	public void setNumConditions(int numConditions) {
		this.numConditions = numConditions;
	}

	public int getNumGenes() {
		return expressionMatrix.size();
	}

	public Map<Integer, GeneExpression> getExpressionMatrix() {
		return expressionMatrix;
	}

	public void setExpressionMatrix(Map<Integer, GeneExpression> expressionMatrix) {
		this.expressionMatrix = expressionMatrix;
	}

	public double getMaxExpression() {
		return maxExpression;
	}

	public void setMaxExpression(double maxExpression) {
		this.maxExpression = maxExpression;
	}

	public double getMinExpression() {
		return minExpression;
	}

	public void setMinExpression(double minExpression) {
		this.minExpression = minExpression;
	}

	public String[] getPhenotypes() {
		return phenotypes;
	}

	public void setPhenotypes(String[] phenotypes) {
		this.phenotypes = phenotypes;
	}

	public String getPhenotype1() {
		return phenotype1;
	}

	public void setPhenotype1(String phenotype1) {
		this.phenotype1 = phenotype1;
	}

	public String getPhenotype2() {
		return phenotype2;
	}

	public void setPhenotype2(String phenotype2) {
		this.phenotype2 = phenotype2;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public double getClosesttoZero() {
		return closesttoZero;
	}

	public void setClosesttoZero(double closesttoZero) {
		this.closesttoZero = closesttoZero;
	}


	/**
	 * Restores parameters saved in the session file. Note, most of this object
	 * is restored by the ExpressionFileReaderTask.
	 */
	public void restoreProps(String ds, Map<String, String> props) {
		String simpleName = this.getClass().getSimpleName();
		String val = props.get(ds + "%" + simpleName + "%expressionUniverse");
		if(val != null) {
			try {
				expressionUniverse = Integer.parseInt(val);
			} catch(NumberFormatException e) {
			}
		}
	}

	public Set<Integer> getGeneIds() {
		return expressionMatrix.keySet();
	}

	
}
