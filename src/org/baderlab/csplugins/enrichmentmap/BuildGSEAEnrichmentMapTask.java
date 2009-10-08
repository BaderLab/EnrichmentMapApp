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
import cytoscape.Cytoscape;

import javax.swing.*;
import java.util.HashMap;

/**
 * Created by
 * User: risserlin
 * Date: Jan 28, 2009
 * Time: 11:44:46 AM
 */
public class BuildGSEAEnrichmentMapTask implements Task {


    private EnrichmentMapParameters params;

    // Keep track of progress for monitoring:
    private int maxValue;
    private TaskMonitor taskMonitor = null;
    private boolean interrupted = false;


    public BuildGSEAEnrichmentMapTask( EnrichmentMapParameters params) {

        //create a new instance of the parameters
        this.params = new EnrichmentMapParameters();

        //copy the input variables into the new instance of the parameters
        this.params.copyInputParameters(params);

    }

    public void buildGSEAMap(){

        //Load in the GMT file
        try{
            //Load the GSEA geneset file
            GMTFileReaderTask gmtFile = new GMTFileReaderTask(params, taskMonitor);
            gmtFile.run();
            //boolean success = TaskManager.executeTask(gmtFile, config);

        } catch (OutOfMemoryError e) {
            taskMonitor.setException(e,"Out of Memory. Please increase memory allotement for cytoscape.");
            return;
        }  catch(Exception e){
            taskMonitor.setException(e,"unable to load GMT file");
            return;
        }

        //Load the Data if the user has supplied the data file.
        if(params.isData()){
            //Load in the GCT file
            try{
                //Load the GCT file
                GCTFileReaderTask gctFile1 = new GCTFileReaderTask(params,params.getGCTFileName1(),1,taskMonitor);
                gctFile1.run();
                params.getExpression().rowNormalizeMatrix();
                if(params.isData2()){
                    GCTFileReaderTask gctFile2 = new GCTFileReaderTask(params,params.getGCTFileName2(),2,taskMonitor);
                    gctFile2.run();
                    params.getExpression2().rowNormalizeMatrix();
                }
                //trim the genesets to only contain the genes that are in the data file.
                params.filterGenesets();

                //check to make sure that after filtering there are still genes in the genesets
                //if there aren't any genes it could mean that the IDs don't match or it could mean none
                //of the genes in the expression file are in the specified genesets.
                if(!params.checkGenesets())
                    throw new IllegalThreadStateException("No genes in the expression file are found in the GMT file ");

            } catch(IllegalThreadStateException e){
                taskMonitor.setException(e,"Either no genes in the expression file are found in the GMT file \n OR the identifiers in the Expression and GMT do not match up.", "Expression and GMT file do not match");
                return;
            }catch (OutOfMemoryError e) {
                taskMonitor.setException(e,"Out of Memory. Please increase memory allotement for cytoscape.");
                return;
            }catch(Exception e){
                taskMonitor.setException(e,"unable to load GSEA DATA (.GCT) file");
                return;
            }
        }
        else{
            params.noFilter();
        }




        try{

            //Load the GSEA result files
            //Dataset1 (each dataset should have two files.)
            EnrichmentResultFileReaderTask enrichmentResultsFilesDataset1File1 = new EnrichmentResultFileReaderTask(params,taskMonitor,  params.getEnrichmentDataset1FileName1(), 1);
            enrichmentResultsFilesDataset1File1.run();
            if(params.isGSEA()){
                EnrichmentResultFileReaderTask enrichmentResultsFilesDataset1File2 = new EnrichmentResultFileReaderTask(params,taskMonitor,  params.getEnrichmentDataset1FileName2(), 1);
                enrichmentResultsFilesDataset1File2.run();
            }

            //check to see if we have ranking files
            if(params.getDataset1RankedFile() != null){
                RanksFileReaderTask ranking1 = new RanksFileReaderTask(params,params.getDataset1RankedFile(),1,taskMonitor);
                ranking1.run();
            }

            //Load the second dataset only if there is a second dataset to load
            if (params.isTwoDatasets()){
                //Dataset2
                EnrichmentResultFileReaderTask enrichmentResultsFilesDataset2File1 = new EnrichmentResultFileReaderTask(params,taskMonitor,  params.getEnrichmentDataset2FileName1(), 2);
                enrichmentResultsFilesDataset2File1.run();

                if(params.isGSEA()){
                    EnrichmentResultFileReaderTask enrichmentResultsFilesDataset2File2 = new EnrichmentResultFileReaderTask(params,taskMonitor,  params.getEnrichmentDataset2FileName2(), 2);
                    enrichmentResultsFilesDataset2File2.run();
                }
                //check to see if we have ranking files
                if(params.getDataset2RankedFile() != null){
                    RanksFileReaderTask ranking2 = new RanksFileReaderTask(params,params.getDataset2RankedFile(),2,taskMonitor);
                    ranking2.run();
                }

            }
             } catch (OutOfMemoryError e) {
                taskMonitor.setException(e,"Out of Memory. Please increase memory allotement for cytoscape.");
                return;
            }   catch(Exception e){

                taskMonitor.setException(e,"unable to load enrichment results files");
                return;
        }

        try{
            //Initialize the set of genesets and GSEA results that we want to compute over
            InitializeGenesetsOfInterestTask genesets_init = new InitializeGenesetsOfInterestTask(params,taskMonitor);
            genesets_init.run();
            //boolean success4 = TaskManager.executeTask(genesets_init,config);
       } catch (OutOfMemoryError e) {
            taskMonitor.setException(e,"Out of Memory. Please increase memory allotement for cytoscape.");
            return;
        }catch(IllegalThreadStateException e){
            taskMonitor.setException(e,"Genesets defined in the results \nfile are not found in  gene set file (GMT).\n  Please make sure you are using the correct GMT file.");
            //JOptionPane.showMessageDialog(Cytoscape.getDesktop(),"Genesets defined in the results file are not found in \n gene set file (GMT).\n  Please make sure you are using the correct GMT file.");
            return;
        }

        try{
            //compute the geneset similarities
            ComputeSimilarityTask similarities = new ComputeSimilarityTask(params,taskMonitor);
            similarities.run();
            //boolean success5 = TaskManager.executeTask(similarities,config);
            HashMap<String, GenesetSimilarity> similarity_results = similarities.getGeneset_similarities();

            params.setGenesetSimilarity(similarity_results);

            //build the resulting map
            VisualizeEnrichmentMapTask map = new VisualizeEnrichmentMapTask(params,taskMonitor);
            map.run();
            //boolean success3 =TaskManager.executeTask(map,config);
        } catch (OutOfMemoryError e) {
            taskMonitor.setException(e,"Out of Memory. Please increase memory allotement for cytoscape.");

        }catch(Exception e){
            taskMonitor.setException(e,"unable to build/visualize map");
        }



    }


 /**
     * Run the Task.
     */
    public void run() {
        buildGSEAMap();
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
        return new String("Building Enrichment Map based on GSEA results");
    }
}
