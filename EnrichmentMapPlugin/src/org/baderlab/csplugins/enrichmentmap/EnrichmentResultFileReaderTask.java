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
import java.util.HashMap;

/**
 * Created by
 * User: risserlin
 * Date: Jun 25, 2009
 * Time: 2:40:14 PM
 * <p>
 * Read a set of enrichment results.  Results can be specific to a Gene set enrichment analysis or to
 * a generic enrichment analysis.
 * <p>
 * The two different results files are distinguished based on the number of columns, a GSEA results file
 * has exactly eleven columns.  Any other number of is assumed to be a generic results file.  (It is possible
 * that a generic file also has exactly 11 column so if the file has 11 column the 5 and 6 column headers are
 * checked.  If columns 5 and 6 are specified as ES and NES the file is for sure a GSEA result file.)
 */
public class EnrichmentResultFileReaderTask implements Task {

    private EnrichmentMapParameters params;

    //enrichment results file name
    private String EnrichmentResultFileName;

    //Stores the enrichment results
    private HashMap<String, EnrichmentResult> results ;

    //phenotypes defined by user - used to classify phenotype specifications
    //in the generic enrichment results file
    private String upPhenotype;
    private String downPhenotype;

    // Keep track of progress for monitoring:
    private TaskMonitor taskMonitor = null;
    private boolean interrupted = false;

    /**
     * Class constructor specifying a task monitor currently using.
     *
     * @param params -  enrichment map parameters for current map
     * @param taskMonitor - current monitor
     * @param FileName - enrichment results file name
     * @param dataset - dataset enrichment results are from
     */
    public EnrichmentResultFileReaderTask(EnrichmentMapParameters params, TaskMonitor taskMonitor, String FileName, int dataset) {
        this(params, FileName, dataset);
        this.taskMonitor = taskMonitor;
    }

    /**
     * Class constructor
     *
     * @param params -  enrichment map parameters for current map
     * @param FileName - enrichment results file name
     * @param dataset  - dataset enrichment results are from
     */
    public EnrichmentResultFileReaderTask(EnrichmentMapParameters params, String FileName, int dataset) {
        this.params = params;
        EnrichmentResultFileName = FileName;
        if(dataset == 1){
            results = params.getEnrichmentResults1();
            upPhenotype = params.getDataset1Phenotype1();
            downPhenotype = params.getDataset1Phenotype2();
        }
        else if(dataset == 2){
            results = params.getEnrichmentResults2();
            upPhenotype = params.getDataset2Phenotype1();
            downPhenotype = params.getDataset2Phenotype2();
        }

    }

    /**
     * Parse enrichment results file
     */
    public void parse() {

         //open Enrichment Result file

         TextFileReader reader = new TextFileReader(EnrichmentResultFileName);
         reader.read();
         String fullText = reader.getText();

         String [] lines = fullText.split("\n");


         //figure out what type of enrichment results file.  Either it is a GSEA result
        //file or it is a generic result file.
        // Currently the headings in the GSEA Results file are:
        // NAME <tab> GS<br> follow link to MSigDB <tab> GS DETAILS <tab> SIZE <tab> ES <tab> NES <tab> NOM p-val <tab> FDR q-val <tab> FWER p-val <tab> RANK AT MAX <tab> LEADING EDGE
        // There are eleven headings.

        //ES and NES columns are specific to the GSEA format
        String header_line = lines[0];
        String [] tokens = header_line.split("\t");

        //check to see if there are exactly 11 columns
        if(tokens.length == 11){
            //check to see if the ES is the 5th column and that NES is the 6th column
            if((tokens[4].equalsIgnoreCase("ES")) && (tokens[5].equalsIgnoreCase("NES")))
                parseGSEAFile(lines);
            //it is possible that the file can have 11 columns but that it is still a generic file
            //if it doesn't specify ES and NES in the 5 and 6th columns
            else
              parseGenericFile(lines);
        }
        else{
             parseGenericFile(lines);
        }



    }

    /**
     * Parse GSEA enrichment results file.
     *
     * @param lines - contents of results file
     */
    public void parseGSEAFile(String[] lines){
        //skip the first line which just has the field names (start i=1)

        params.setFDR(true);

         int currentProgress = 0;
         int maxValue = lines.length;
         for (int i = 1; i < lines.length; i++) {
                String line = lines[i];

                String [] tokens = line.split("\t");
                int size = 0;
                double ES = 0.0;
                double NES = 0.0;
                double pvalue = 1.0;
                double FDRqvalue = 1.0;
                double FWERqvalue = 1.0;

                //The first column of the file is the name of the geneset
                String Name = tokens[0].toUpperCase().trim();

                //The fourth column is the size of the geneset
                if(tokens[3].equalsIgnoreCase("")){
                    //do nothing
                }else{
                    size = Integer.parseInt(tokens[3]);
                }

                //The fifth column is the Enrichment score (ES)
                if(tokens[4].equalsIgnoreCase("")){
                    //do nothing
                }else{
                     ES = Double.parseDouble(tokens[4]);
                }

                //The sixth column is the Normalize Enrichment Score (NES)
                if(tokens[5].equalsIgnoreCase("")){
                    //do nothing
                }else{
                     NES = Double.parseDouble(tokens[5]);
                }

                //The seventh column is the nominal p-value
                if(tokens[6].equalsIgnoreCase("")){
                    //do nothing
                }else{
                    pvalue = Double.parseDouble(tokens[6]);
                }

                //the eighth column is the FDR q-value
                if(tokens[7].equalsIgnoreCase("")){
                    //do nothing
                }else{
                    FDRqvalue = Double.parseDouble(tokens[7]);
                }
                //the ninth column is the FWER q-value
                if(tokens[8].equalsIgnoreCase("")){
                    //do nothing
                }else{
                    FWERqvalue = Double.parseDouble(tokens[8]);
                }
                GSEAResult result = new GSEAResult(Name, size, ES, NES,pvalue,FDRqvalue,FWERqvalue);


                // Calculate Percentage.  This must be a value between 0..100.
                int percentComplete = (int) (((double) currentProgress / maxValue) * 100);
                //  Estimate Time Remaining
                long timeRemaining = maxValue - currentProgress;
                if (taskMonitor != null) {
                        taskMonitor.setPercentCompleted(percentComplete);
                        taskMonitor.setStatus("Parsing Enrichment Results file " + currentProgress + " of " + maxValue);
                        taskMonitor.setEstimatedTimeRemaining(timeRemaining);
                    }
                currentProgress++;

                results.put(Name, result);
            }
    }

    /**
     * Parse generic enrichment results file
     *
     * @param lines - contents of results file
     */
    public void parseGenericFile(String [] lines){

        //Get the current genesets so we can check that all the results are in the geneset list
        //and put the size of the genesets into the visual style
        HashMap genesets = params.getFilteredGenesets();

        int currentProgress = 0;
        int maxValue = lines.length;
        boolean FDR = false;
        boolean ignore_phenotype = false;

         //skip the first line which just has the field names (start i=1)
        //check to see how many columns the data has
        String line = lines[0];
        String [] tokens = line.split("\t");
        int length = tokens.length;
        //if (length < 3)
           //not enough data in the file!!

        for (int i = 1; i < lines.length; i++) {
            line = lines[i];

            tokens = line.split("\t");

            double pvalue = 1.0;
            double FDRqvalue = 1.0;
            GenericResult result;
            int gs_size = 0;
            double NES = 1.0;

            //The first column of the file is the name of the geneset
            String name = tokens[0].toUpperCase().trim();

            if(genesets.containsKey(name)){
                GeneSet current_set = (GeneSet)genesets.get(name);
                gs_size = current_set.getGenes().size();
            }

            String description = tokens[1].toUpperCase();

            //The third column is the nominal p-value
            if(tokens[2].equalsIgnoreCase("")){
                //do nothing
            }else{
                pvalue = Double.parseDouble(tokens[2]);
            }

            if(length > 3){
                //the fourth column is the FDR q-value
                if(tokens[3].equalsIgnoreCase("")){
                    //do nothing
                }else{
                    FDRqvalue = Double.parseDouble(tokens[3]);
                    FDR = true;
                }
                //the fifth column is the phenotype.
                //it can either be a signed number or it can be text specifying the phenotype
                //in order for it to be parseable the text has to match the user specified phenotypes
                // and if it is a number the only important part is the sign
                if((length > 4) && !ignore_phenotype){

                    if(tokens[4].equalsIgnoreCase("")){

                    }else{

                        //check to see if the string matches the specified phenotypes
                        if(tokens[4].equalsIgnoreCase(upPhenotype))
                            NES = 1.0;
                        else if(tokens[4].equalsIgnoreCase(downPhenotype))
                            NES = -1.0;
                        //try and see if the user has specified the phenotype as a number
                        else{
                            try{
                                NES = Double.parseDouble(tokens[4]);
                            }catch (NumberFormatException nfe){
                                //would like to give user the option but it won't let me interupt the task using Joptionpane
                                /*int answer = JOptionPane.showConfirmDialog(Cytoscape.getDesktop(),"One or more of the enrichment results have a phenotype of unknown type specified.  Would you like me to ignore the phenotype Column?","Unrecognizable Phenotype.", JOptionPane.YES_NO_OPTION);
                                if(answer == JOptionPane.YES_OPTION)
                                    ignore_phenotype = true;
                                else*/
                                    throw new IllegalThreadStateException(tokens[4]+ " is not a valid phenotype.  Phenotype specified in generic enrichment results file must have the same phenotype as specified in advanced options or must be a positive or negative number.");
                            }
                        }
                    }
                    result = new GenericResult(name,description,pvalue,gs_size,FDRqvalue,NES);
                }
                else
                    result = new GenericResult(name,description,pvalue,gs_size,FDRqvalue);

            }
            else{
                result = new GenericResult(name, description,pvalue,gs_size);
            }

            // Calculate Percentage.  This must be a value between 0..100.
            int percentComplete = (int) (((double) currentProgress / maxValue) * 100);
            //  Estimate Time Remaining
            long timeRemaining = maxValue - currentProgress;
            if (taskMonitor != null) {
                    taskMonitor.setPercentCompleted(percentComplete);
                    taskMonitor.setStatus("Parsing Generic Results file " + currentProgress + " of " + maxValue);
                    taskMonitor.setEstimatedTimeRemaining(timeRemaining);
                }
            currentProgress++;

            results.put(name, result);
        }
        if(FDR)
            params.setFDR(FDR);
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
        return new String("Parsing Enrichment Result file");
    }
}
