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

/**
 * Created by
 * User: risserlin
 * Date: Jan 29, 2009
 * Time: 3:49:44 PM
 */
public class GeneExpression {

    private String name;
    private String description;

    private Double[] expression;

    private String[] row;

    private String separator = "\t";

    public GeneExpression(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String toString(){
        String GE_string;

        GE_string = name + separator + description;

        for(int i =0;i<expression.length;i++){

                GE_string = GE_string + separator + expression[i];
        }

        GE_string = GE_string + "\n";

        return GE_string;
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

    //Given a string representing a line in the file create
    //an array of expression values
    public void setExpression(String[] expres){

        row = expres;
        //ignore the first two cells
        int size = expres.length;

        expression = new Double[size-2];

        for(int i = 2; i< size;i++){
            expression[i-2] = Double.parseDouble(expres[i]);
        }

    }

    //given an expression row and the current maximum is there an element
    //that is higher and should be the new max
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

    public String[] getRow() {
        return row;
    }

    public Double[] rowNormalize(){
        Double[] normalize = new Double[expression.length];

        double mean = getMean();
        double std = getSTD(mean);

        for(int i = 0;i<expression.length;i++)
            normalize[i] = (expression[i] - mean)/std;

        return normalize;
    }

    private double getMean(){
        double sum = 0.0;

        for(int i = 0;i<expression.length;i++)
           sum = sum + expression[i];

        return sum/expression.length;
    }

    private double getSTD(double mean){
        double sum = 0.0;

        for(int i = 0;i<expression.length;i++)
            sum = sum + Math.pow(expression[i] - mean,2);

        return Math.sqrt(sum)/expression.length;
    }

   public Double[] rowLogTransform(){
        Double[] logtransformed = new Double[expression.length];



        for(int i = 0;i<expression.length;i++)
            logtransformed[i] = Math.log1p(expression[i]);

        return logtransformed;
    }
}
