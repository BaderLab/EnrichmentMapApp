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
import cytoscape.data.readers.TextFileReader;
import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;

import java.util.HashSet;
import java.util.HashMap;
import java.io.IOException;

/**
 * Created by
 * User: risserlin
 * Date: Jan 8, 2009
 * Time: 11:59:17 AM
 */

/* GMTFileReader
*  given a GMT file name this class creates a set of genesets as generated from the GMT file.
*/
public class GMTFileReaderTask implements Task {

    private EnrichmentMapParameters params;

    private String GMTFileName;
    private HashMap genes;
    private HashMap hashkey2gene;

    private String fullText;
    private String [] lines;

    private HashMap genesets ;

    // Keep track of progress for monitoring:
    private int maxValue;
    private TaskMonitor taskMonitor = null;
    private boolean interrupted = false;


     public GMTFileReaderTask(EnrichmentMapParameters params, TaskMonitor taskMonitor) {
        this(params);
        this.taskMonitor = taskMonitor;
    }

    public GMTFileReaderTask(EnrichmentMapParameters params)   {
        this.params = params;

        this.GMTFileName = params.getGMTFileName();
        this.genes = params.getGenes();
        this.hashkey2gene = params.getHashkey2gene();

        this.genesets = params.getGenesets();

        //open GMT file

         TextFileReader reader = new TextFileReader(GMTFileName);
         reader.read();
         fullText = reader.getText();


        lines = fullText.split("\n");

    }

    public void parse() {
        int currentProgress = 0;
        maxValue = lines.length;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            String [] tokens = line.split("\t");

            //The first column of the file is the name of the geneset
            String Name = tokens[0].toUpperCase().trim();

            //The second column of the file is the description of the geneset
            String description = tokens[1].trim();

            //create an object of type Geneset with the above Name and description
            GeneSet gs = new GeneSet(Name, description);

            // Calculate Percentage.  This must be a value between 0..100.
            int percentComplete = (int) (((double) currentProgress / maxValue) * 100);
            //  Estimate Time Remaining
            long timeRemaining = maxValue - currentProgress;
            if (taskMonitor != null) {
                    taskMonitor.setPercentCompleted(percentComplete);
                    taskMonitor.setStatus("Parsing GMT file " + currentProgress + " of " + maxValue);
                    taskMonitor.setEstimatedTimeRemaining(timeRemaining);
                }
            currentProgress++;

            //All subsequent fields in the list are the geneset associated with this geneset.
            for(int j = 2; j < tokens.length ; j++){

                //Check to see if the gene is already in the hashmap of genes
                //if it is already in the hash then get its associated key and put it
                //into the set of genes
                if(genes.containsKey(tokens[j])){
                   gs.addGene((Integer)genes.get(tokens[j]));
                }

                //If the gene is not in the list then get the next value to be used and put it in the list
                else{
                  //add the gene to the master list of genes
                  int value = params.getNumberOfGenes();
                  genes.put(tokens[j], value);
                  hashkey2gene.put(value,tokens[j]);
                  params.setNumberOfGenes(value+1);

                  //add the gene to the genelist
                  gs.addGene((Integer)genes.get(tokens[j]));
                }
            }

            //finished parsing that geneset
            //add the current geneset to the hashmap of genesets
            genesets.put(Name, gs);

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
        return new String("Parsing GMT file");
    }
}
