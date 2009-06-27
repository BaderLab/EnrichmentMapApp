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
// $LasrChangedRevision$
// $LastChangedBy$
// $HeadURL$

package org.baderlab.csplugins.enrichmentmap;

import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;
import cytoscape.data.readers.TextFileReader;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by
 * User: risserlin
 * Date: Jan 21, 2009
 * Time: 9:07:34 AM
 */
public class GCTFileReaderTask implements Task {

    private EnrichmentMapParameters params;

    private String GCTFileName;

    private int dataset;

    private String fullText;
    private String [] lines;

    private HashSet datasetGenes;
    private HashMap genes;

    // Keep track of progress for monitoring:
    private int maxValue;
    private TaskMonitor taskMonitor = null;
    private boolean interrupted = false;


     public GCTFileReaderTask(EnrichmentMapParameters params,String fileName, int dataset, TaskMonitor taskMonitor) {
        this(params,fileName,dataset);
        this.taskMonitor = taskMonitor;
    }

    public GCTFileReaderTask(EnrichmentMapParameters params,String fileName, int dataset )   {
        this.params = params;

        this.genes = params.getGenes();
        this.datasetGenes = params.getDatasetGenes();

        //open GCT file
        this.GCTFileName = fileName;
        this.dataset = dataset;



    }
      public void parse() {
        TextFileReader reader = new TextFileReader(GCTFileName);
        reader.read();
        fullText = reader.getText();

        lines = fullText.split("\n");
        int currentProgress = 0;
        maxValue = lines.length;
        GeneExpressionMatrix expressionMatrix = new GeneExpressionMatrix(lines[0].split("\t"));
        HashMap<Integer,GeneExpression> expression = new HashMap();

        for (int i = 0; i < lines.length; i++) {
            Integer genekey ;

            String line = lines[i];

            String [] tokens = line.split("\t");

            //The first column of the file is the name of the geneset
            String Name = tokens[0];

            if(i==0){
                //otherwise the first line is the header
                if(Name.equalsIgnoreCase("#1.2")){
                   line = lines[2];
                   i=2;
                }
                else{
                    line = lines[0];
                }
                tokens = line.split("\t");
                expressionMatrix = new GeneExpressionMatrix(tokens);
                expressionMatrix.setExpressionMatrix(expression);
                continue;
            }


            //Check to see if this gene is in the genes list
            if(genes.containsKey(Name)){
                genekey = (Integer)genes.get(Name);
                //we want the genes hashmap and dataset genes hashmap to have the same keys so it is
                //easier to compare.
                datasetGenes.add(genes.get(Name));

                String description = tokens[1];
                GeneExpression expres = new GeneExpression(Name, description);
                expres.setExpression(tokens);

                double newMax = expres.newMax(expressionMatrix.getMaxExpression());
                if(newMax != -100) expressionMatrix.setMaxExpression(newMax);
                double newMin = expres.newMin(expressionMatrix.getMinExpression());
                if (newMin != -100) expressionMatrix.setMinExpression(newMin);

                expression.put(genekey,expres);

            }

            // Calculate Percentage.  This must be a value between 0..100.
            int percentComplete = (int) (((double) currentProgress / maxValue) * 100);
            //  Estimate Time Remaining
            long timeRemaining = maxValue - currentProgress;
            if (taskMonitor != null) {
                    taskMonitor.setPercentCompleted(percentComplete);
                    taskMonitor.setStatus("Parsing GCT file " + currentProgress + " of " + maxValue);
                    taskMonitor.setEstimatedTimeRemaining(timeRemaining);
                }
            currentProgress++;

        }
        if(dataset == 1){
            //set up the classes definition if it is set.
            if(params.getClassFile1() != null)
                expressionMatrix.setPhenotypes(setClasses(expressionMatrix, params.getClassFile1()));
            params.setExpression(expressionMatrix);
        }
        else{
            //set up the classes definition if it is set.
            if(params.getClassFile2() != null)
                expressionMatrix.setPhenotypes(setClasses(expressionMatrix, params.getClassFile2()));
            params.setExpression2(expressionMatrix);
        }
    }

    private String[] setClasses(GeneExpressionMatrix expresson, String classFile){

        //check to see if the file was opened successfully
        if(!classFile.equalsIgnoreCase(null)){
            TextFileReader reader2 = new TextFileReader(classFile);

            reader2.read();
            String fullText2 = reader2.getText();

            String[] lines2 = fullText2.split("\n");

            //the third line of the class file defines the classes
            return lines2[2].split(" ");
        }
        else{
            String[] def_pheno = {"Na_pos","NA_neg"};
            return def_pheno;
        }
    }


 /**
     * Run the Task.
     */
    public void run() {
        parse();
    }

    /**
     * Non-blocking call to interrupt the task.
     */
    public void halt() {
        this.interrupted = true;
    }

     /**
     * Sets the Task Monitor.
     *
     * @param taskMonitor TaskMonitor Object.
     */
    public void setTaskMonitor(TaskMonitor taskMonitor) {
        if (this.taskMonitor != null) {
            throw new IllegalStateException("Task Monitor is already set.");
        }
        this.taskMonitor = taskMonitor;
    }

    /**
     * Gets the Task Title.
     *
     * @return human readable task title.
     */
    public String getTitle() {
        return new String("Parsing GCT file");
    }
}
