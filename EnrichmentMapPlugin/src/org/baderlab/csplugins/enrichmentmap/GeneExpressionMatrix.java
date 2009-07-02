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

package org.baderlab.csplugins.enrichmentmap;

import giny.model.Node;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;

/**
 * Created by
 * User: risserlin
 * Date: Jan 30, 2009
 * Time: 9:32:17 AM
 */
public class GeneExpressionMatrix {

    private String[] columnNames;

    private int numConditions;
    private int numGenes;

    private HashMap<Integer, GeneExpression> expressionMatrix;
    private HashMap<Integer, GeneExpression> expressionMatrix_rowNormalized;

    private double maxExpression = 0;
    private double minExpression = 0;

    private String[] phenotypes;


    public GeneExpressionMatrix(String[] columnNames) {
        numConditions = columnNames.length;
        this.columnNames = columnNames;
    }

    public String[] getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(String[] columnNames) {
        this.columnNames = columnNames;
    }

    public int getNumConditions() {
        return numConditions;
    }

    public void setNumConditions(int numConditions) {
        this.numConditions = numConditions;
    }

    public int getNumGenes() {
        return numGenes;
    }

    public void setNumGenes(int numGenes) {
        this.numGenes = numGenes;
    }

    public HashMap getExpressionMatrix() {
        return expressionMatrix;
    }

    public void setExpressionMatrix(HashMap expressionMatrix) {
        this.expressionMatrix = expressionMatrix;
    }

    public HashMap getExpressionMatrix_rowNormalized() {
        return expressionMatrix_rowNormalized;
    }

    public void setExpressionMatrix_rowNormalized(HashMap expressionMatrix_rowNormalized) {
        this.expressionMatrix_rowNormalized = expressionMatrix_rowNormalized;
    }

    public HashMap getExpressionMatrix(HashSet<Integer> subset){
        HashMap expression_subset = new HashMap();

        //go through the expression matrix and get the subset of
        //genes of interest
        for(Iterator i = subset.iterator(); i.hasNext();){
            Integer k = (Integer)i.next();
            if(expressionMatrix.containsKey(k)){
                expression_subset.put(k,expressionMatrix.get(k));
            }
            else{
                System.out.println("how is this key not in the hashmap?");
            }

        }

        return expression_subset;

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

    public double getMeanExpression(HashMap currentMatrix){
        double sum = 0.0;
        int k = 0;
        //go through the expression matrix
        for(Iterator i = currentMatrix.keySet().iterator(); i.hasNext();){
            Double[] currentRow = ((GeneExpression)currentMatrix.get(i.next())).getExpression();
            for(int j = 0; j< currentRow.length;j++){
                sum = sum + currentRow[j];
                k++;
            }

        }

        return sum/k;

    }

      public double getMaxExpression(HashMap currentMatrix){
        double max = 0.0;
          if(currentMatrix != null){
            //go through the expression matrix
            for(Iterator i = currentMatrix.keySet().iterator(); i.hasNext();){
                Double[] currentRow = ((GeneExpression)currentMatrix.get(i.next())).getExpression();
                for(int j = 0; j< currentRow.length;j++){
                    if(max < currentRow[j])
                        max = currentRow[j];
                }

            }
          }
        return max;

    }

public double getMinExpression(HashMap currentMatrix){
        double min = 0.0;
        //go through the expression matrix
        if(currentMatrix != null){
            for(Iterator i = currentMatrix.keySet().iterator(); i.hasNext();){
                Double[] currentRow = ((GeneExpression)currentMatrix.get(i.next())).getExpression();
                for(int j = 0; j< currentRow.length;j++){
                    if(min > currentRow[j])
                        min = currentRow[j];
                }

            }
        }
        return min;

    }

    public double getSTDExpression(double mean, HashMap currentMatrix){
        double sum = 0.0;
        int k= 0;
        //go through the expression matrix
        for(Iterator i = currentMatrix.keySet().iterator(); i.hasNext();){
            Double[] currentRow = ((GeneExpression)currentMatrix.get(i.next())).getExpression();
            for(int j = 0; j< currentRow.length;j++){
                sum = sum + Math.pow((currentRow[j]-mean),2);
                k++;
            }
       }

        return Math.sqrt(sum)/k;
    }

    public void rowNormalizeMatrix(){

        if(expressionMatrix == null)
            return;

        //create new matrix
        expressionMatrix_rowNormalized = new HashMap();

         int k= 0;
        //go through the expression matrix
        for(Iterator i = expressionMatrix.keySet().iterator(); i.hasNext();){
            Integer key = (Integer)i.next();
            GeneExpression currentexpression = ((GeneExpression)expressionMatrix.get(key));
            String Name = currentexpression.getName();
            String description = currentexpression.getDescription();
            GeneExpression norm_row = new GeneExpression(Name,description);
            Double[] currentexpression_row_normalized = currentexpression.rowNormalize();
            norm_row.setExpression( currentexpression_row_normalized);

            expressionMatrix_rowNormalized.put(key,norm_row);
       }

    }

    public String[] getPhenotypes() {
        return phenotypes;
    }

    public void setPhenotypes(String[] phenotypes) {
        this.phenotypes = phenotypes;
    }

    public String toString(){

        String expressionString = "";

        for(int i = 0; i<columnNames.length; i++)
            expressionString += columnNames[i] + "\t" ;

        expressionString += "\n";

        for(Iterator i = expressionMatrix.keySet().iterator(); i.hasNext();){
                expressionString += ((GeneExpression)expressionMatrix.get(i.next())).toString() ;
           }

        return expressionString;
    }

}
