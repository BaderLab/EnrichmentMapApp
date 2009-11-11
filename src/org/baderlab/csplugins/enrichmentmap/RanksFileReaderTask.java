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

import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;
import cytoscape.data.readers.TextFileReader;
import cytoscape.Cytoscape;

import javax.swing.*;
import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by
 * User: risserlin
 * Date: May 1, 2009
 * Time: 9:10:22 AM
 * <p>
 * Task to parse ranks file <br>
 * There are multiple potential rank file formats: <br>
 * GSEA input rnk file - a two column file with genes and their specified rank represented as a double, commented lines
 * have a # at the line start.
 * GSEA output rank files (xls file) - a five column file with genes and specified rank but also have three bland
 * columns.
 *
 */
public class RanksFileReaderTask implements Task {

    private EnrichmentMapParameters params;
    private String RankFileName;
    private int dataset;
    private String ranks_name;

    // Keep track of progress for monitoring:
    private int maxValue;
    private TaskMonitor taskMonitor = null;
    private boolean interrupted = false;

    /**
     * Class constructor
     *
     * @param params - enrichment map parameters for current map
     * @param rankFileName - file name of ranks file
     * @param dataset - which dataset is this rank file related to (dataset 1 or dataset 2)
     */
    public RanksFileReaderTask(EnrichmentMapParameters params, String rankFileName, int dataset) {
        this.params = params;
        RankFileName = rankFileName;
        this.dataset = dataset;
    }

    /**
     *  Class constructor - curent task monitor specified.
     *
     * @param params - enrichment map parameters for current map
     * @param rankFileName - file name of ranks file
     * @param dataset - which dataset is this rank file related to (dataset 1 or dataset 2)
     * @param taskMonitor - current task monitor
     */
    public RanksFileReaderTask(EnrichmentMapParameters params, String rankFileName, int dataset, TaskMonitor taskMonitor) {
        this.params = params;
        RankFileName = rankFileName;
        this.dataset = dataset;
        this.taskMonitor = taskMonitor;
    }

    /**
     * Class constructor - for late loaded rank file that aren't specific to a dataset.
     *
     * @param params - enrichment map parameters for current map
     * @param rankFileName - file name of ranks file
     * @param ranks_name - name of rankings to be used in heat map drop down to refer to it.
     */
     public RanksFileReaderTask(EnrichmentMapParameters params, String rankFileName, String ranks_name) {
        this.params = params;
        RankFileName = rankFileName;
        this.ranks_name = ranks_name;
    }

    /**
     * parse the rank file
     */
    public void parse(){

        TextFileReader reader = new TextFileReader(RankFileName);
        reader.read();
        String fullText = reader.getText();
        int lineNumber = 0;

        String[] lines = fullText.split("\n");
        int currentProgress = 0;
        maxValue = lines.length;

        HashMap genes = params.getGenes();
        // we don't know the number of scores in the rank file yet, but it can't be more than the number of lines.
        Double[] score_collector = new Double[lines.length];

        HashMap<Integer,Ranking> ranks = new HashMap<Integer,Ranking>();

        /* there are two possible Rank files:
         * If loaded through the rpt file the file is the one generated by
         * GSEA and will have 5 columns (name, description, empty,empty,score)
         * If the user loaded it through the generic of specifying advanced options
         * then it will 2 columns (name,score).
         * The score in either case should be a double and the name a string so
         * check for either option.
         */

        int nScores = 0;    //number of found scores
        for (int i = 0; i < lines.length; i++) {
            Integer genekey ;

            String line = lines[i];

            //check to see if the line is commented out and should be ignored.
            if ( line.startsWith("#") ) {
                // look for ranks_name in comment line e.g.: "# Ranks Name : My Ranks"
                if (Pattern.matches("^# *Ranks[ _-]?Name *:.+", line) ) {
                    this.ranks_name = line.split(":", 2)[1];
                    while (this.ranks_name.startsWith(" "))
                        this.ranks_name = this.ranks_name.substring(1);
                }
                //ignore comment line
                continue;
            }

            String [] tokens = line.split("\t");



            String name = tokens[0];
            double score = 0;

            //if there are 5 columns in the data then the rank is the last column
            if(tokens.length == 5 ){
                //ignore rows where the expected rank value is not a valid double
                try{
                    score = Double.parseDouble(tokens[4]);
                } catch (NumberFormatException nfe){
                    if(lineNumber == 0){
                        lineNumber++;
                        continue;
                    }
                    else
                        throw new IllegalThreadStateException("rank value for"+ tokens[0]+ "is not a valid number");
                }
                nScores++;
            }
            //if there are 2 columns in the data then the rank is the 2 column
            else if(tokens.length == 2){
                try{
                    score = Double.parseDouble(tokens[1]);
                }catch (NumberFormatException nfe){
                    if(lineNumber == 0){
                        lineNumber++;
                        continue;
                    }
                    else
                        throw new IllegalThreadStateException("rank value for"+ tokens[0]+ "is not a valid number");
                }
                nScores++;
            }
            else{
                System.out.println("Invalid number of tokens line of Rank File (should be 5 or 2)");
                //skip invalid line
                continue;
            }

            //add score to array of scores
            score_collector[nScores-1] = score;

            //check to see if the gene is in the genelist
            if(genes.containsKey(name)){
                genekey = (Integer)genes.get(name);
                Ranking current_ranking = new Ranking(name,score);
                ranks.put(genekey, current_ranking);
            }


            // Calculate Percentage.  This must be a value between 0..100.
            int percentComplete = (int) (((double) currentProgress / maxValue) * 100);
            //  Estimate Time Remaining
            long timeRemaining = maxValue - currentProgress;
            if (taskMonitor != null) {
                    taskMonitor.setPercentCompleted(percentComplete);
                    taskMonitor.setStatus("Parsing Rank file " + currentProgress + " of " + maxValue);
                    taskMonitor.setEstimatedTimeRemaining(timeRemaining);
                }
            currentProgress++;

        }

         //the none of the genes are in the gene list
         if(ranks.isEmpty()){
             throw new IllegalThreadStateException("None of the genes in the rank file are found in the expression file.  Make sure the identifiers of the two files match.");
         }

         //remove Null values from collector
         Double[] sort_scores = new Double[nScores];
         for (int i=0; i < nScores; i++)
             sort_scores[i] = score_collector[i];
         
         //after we have loaded in all the scores, sort the score to compute ranks
         //create hash of scores to ranks.
         HashMap<Double,Integer> score2ranks = new HashMap<Double,Integer> ();
         Arrays.sort(sort_scores);

        for(int j = 0; j<sort_scores.length;j++){
            //check to see if this score is already enter
            if(!score2ranks.containsKey(sort_scores[j]))
                    score2ranks.put(sort_scores[j],j-1);
        }

        //update scores Hash to contain the ranks as well.
        for(Iterator k = ranks.keySet().iterator(); k.hasNext();){
            Ranking current_ranking = ranks.get(k.next());
            current_ranking.setRank(score2ranks.get(current_ranking.getScore()));
        }

        //check to see if some of the dataset genes are not in this rank file
        HashSet<Integer> current_genes = params.getDatasetGenes();

        Set<Integer> current_ranks = ranks.keySet();

        //intersect the genes with the ranks.  only retain the genes that have ranks.
        Set<Integer> intersection = new HashSet<Integer>(current_genes);
        intersection.retainAll(current_ranks);

        //see if there more genes than there are ranks
        if(!(intersection.size() == current_genes.size())){
            JOptionPane.showMessageDialog(Cytoscape.getDesktop(),"Ranks for some of the genes/proteins listed in the expression file are missing. \n These genes/proteins will be excluded from ranked listing in the heat map.");

        }
        if(dataset == 1){
            params.setDataset1Rankings(ranks);
        }
        else if(dataset == 2){
            params.setDataset2Rankings(ranks);
        }
        else{
            params.addRanks(ranks_name, ranks);
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
        return new String("Parsing Ranks file");
    }
}