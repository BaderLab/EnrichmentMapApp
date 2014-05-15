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

/**
 * Created by
 * User: risserlin
 * Date: Jan 29, 2009
 * Time: 3:49:44 PM
 * <p>
 * Class representing the expression of one gene/protein
 */
public class GeneExpression {

    //gene/protein name
    private String name;
    //gene/protein description
    private String description;

    //expression values associated with this gene
    private Double[] expression;
    //the entire row as read in from the expression file
    private String[] row;

    private String separator = "\t";

    /**
     * Class constructor
     *
     * @param name - gene/protein name
     * @param description - gene/protein description
     */
    public GeneExpression(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * Convert Object into a string of the contents
     *
     * @return String representation of object as tab separate items followed by newline.
     */
    public String toString(){
        StringBuffer GE_StrBuff = new StringBuffer();

        GE_StrBuff.append(name);
        GE_StrBuff.append(separator);
        GE_StrBuff.append(description);

        for(int i =0;i<expression.length;i++){

            GE_StrBuff.append(separator);
            GE_StrBuff.append(expression[i]);
        }

        GE_StrBuff.append("\n");

        return GE_StrBuff.toString();
    }


    /**
     * Create an array of the expression values.
     *
     * @param expres - a string representing a line in the expression file
     */
    public void setExpression(String[] expres){

        row = expres;
        //ignore the first two cells --> only if there are at least 3 cells
        int size = expres.length;

        if(size > 2){
            expression = new Double[size-2];
            for(int i = 2; i< size;i++){
                expression[i-2] = Double.parseDouble(expres[i]);
            }
        }
        else{
            expression = new Double[1];
            try{
                expression[0] = Double.parseDouble(expres[1]);
            }catch(NumberFormatException e){
                //if the column doesn't contain doubles then just assume that the expression
                //file is empty
                expression[0] = 0.0;
            }
        }


    }

    /**
     * Go through current object's expression row and check if there is an element that is higher
     * than the current max.  If there is a value higher than the current max then return that value,
     * if not then return - 100
     *
     * @param currentMax  - the current maximum
     * @return the new maximum or -100 if the maximum remains the same.
     */
    public double newMax(double currentMax){
        double newMax = -100;
         boolean found_newmin = false;

        for(int i =0;i<expression.length;i++){
            if(expression[i] > currentMax){
                //if we have already found a new min check if the new one is even smaller
                if(found_newmin){
                    if(expression[i] > newMax)
                        newMax = expression[i];
                }else{
                    newMax = expression[i];
                    found_newmin = true;
                    }
            }
        }
        return newMax;
    }
    /**
     * Go through current object's expression row and check if there is an element that is lower
     * than the current minimum.  If there is a value lower than the current minimum then return that value,
     * if not then return - 100
     *
     * @param currentMin  - the current minimum
     * @return the new minimum or -100 if the maximum remains the same.
     */
    public double newMin(double currentMin){
        double newMin = -100;
        boolean found_newmin = false;

        for(int i =0;i<expression.length;i++){
            if(expression[i] < currentMin){
                //if we have already found a new min check if the new one is even smaller
                if(found_newmin){
                    if(expression[i] < newMin)
                        newMin = expression[i];
                }else{
                    newMin = expression[i];
                    found_newmin = true;
                    }
            }
        }
        return newMin;
    }

    /**
     * Go through current object's expression row and check if there is an element that is lower
     * than the current minimum.  If there is a value lower than the current minimum then return that value,
     * if not then return - 100
     *
     * @param currentMin  - the current minimum
     * @return the new minimum or -100 if the maximum remains the same.
     */
    public double newclosesttoZero(double currentClosest){
        double newClosest = -100;
        boolean found_newclosest = false;

        
        for(int i =0;i<expression.length;i++){
            if(expression[i] < currentClosest && expression[i]>0){
                //if we have already found a new min check if the new one is even smaller
                if(found_newclosest){
                    if(expression[i] < newClosest)
                        newClosest = expression[i];
                }else{
                    newClosest = expression[i];
                    found_newclosest = true;
                    }
            }
        }
        return newClosest;
    }

    
    /**
     * Row normalize the current gene expression set.  Row normalization involved subtracting the mena
     * of the row from each expression value in the row and subsequently dividing it by the standard deviation
     * of the expression row.
     *
     * @return an array of the row normalized values of the gene expression set.
     */
    public Double[] rowNormalize(){
        Double[] normalize = new Double[expression.length];

        double mean = getMean();
        double std = getSTD(mean);

        for(int i = 0;i<expression.length;i++)
            normalize[i] = (expression[i] - mean)/std;

        return normalize;
    }

    /**
     * Calculate the mean of the current gene expression set
     *
     *  @return mean of current gene expression set
     */
    private double getMean(){
        double sum = 0.0;

        for(int i = 0;i<expression.length;i++)
           sum = sum + expression[i];

        return sum/expression.length;
    }

    /**
     * Calculate the standard deviation of the current gene expression set
     *
     * @param mean of current gene expression set
     * @return stantard deviation of current gene expression set
     */
    private double getSTD(double mean){
        double sum = 0.0;

        for(int i = 0;i<expression.length;i++)
            sum = sum + Math.pow(expression[i] - mean,2);

        return Math.sqrt(sum)/expression.length;
    }

    /**
     * log transform all the expression value in the current gene expression set
     *
     * @return array of log transformed expression values
     */
   public Double[] rowLogTransform(){
        Double[] logtransformed = new Double[expression.length];

        for(int i = 0;i<expression.length;i++)
            logtransformed[i] = Math.log1p(expression[i]);

        return logtransformed;
    }

    //Getters amd Setters

    public String[] getRow() {
           return row;
       }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double[] getExpression() {
        return expression;
    }

    public void setExpression(Double[] expression) {
        this.expression = expression;
    }
    
}
