package org.baderlab.csplugins.enrichmentmap;

import cytoscape.Cytoscape;
import cytoscape.view.CyNetworkView;
import cytoscape.util.export.PDFExporter;
import cytoscape.task.ui.JTaskConfig;
import cytoscape.task.util.TaskManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: User
 * Date: 1/28/11
 * Time: 10:07 AM
 * To change this template use File | Settings | File Templates.
 */
public class BuildBulkEnrichmentMapActionListener implements ActionListener{


    private JTaskConfig config;

    private BulkEMCreationPanel inputPanel;

    /**
     * @param panel - Enrichment map input panel
     */
    public BuildBulkEnrichmentMapActionListener (BulkEMCreationPanel panel) {
        this.inputPanel = panel;

    }

    /**
     * Creates a new task, checks the info in the parameters for the minimum amount of information
     *
     * @param event
     */
    public void actionPerformed(ActionEvent event) {

        config = new JTaskConfig();
        config.displayCancelButton(true);
        config.displayCloseButton(true);
        config.displayStatus(true);

       //make sure that the minimum information is set in the current set of parameters
       EnrichmentMapParameters params = inputPanel.getParams();

        //set the bulk em flag
        params.setBulkEM(true);

       //The Enrichment Map parameters contains only some of the info as in this
        //version a directory is given with the GSEA results and an EM needs to be generated for each
        //GSEA result

        String mainDirectory = params.getGSEAResultsDirName();
        //the user has given a file in the main directory.
        //String mainDirectory = new File(directory_file).getParent();

        //get all the directories in this directory

        String[] gsea_results = new File(mainDirectory).list();

        //get the lower and upper limits if the user has specified any
        int lower = params.getLowerlimit();
        int upper = params.getUpperlimit();
        if(lower ==1 && upper ==1)
            upper = gsea_results.length;

        int counter = 0;


        for(int i=0;i<gsea_results.length;i++){
            File current = new File(mainDirectory, gsea_results[i]);
            //if this is a directory - go into it and get the .rpt file
            if(current.isDirectory()){
                String[] children = current.list();
                for(int k=0;k<children.length;k++){
                    if(children[k].contains(".rpt")){

                        //only count the directories that are GSEA results files
                        counter++;

                        if(counter >= lower && counter < upper){
                            File rpt_file = new File(current, children[k]);

                            //populate the fields based on this rpt file
                            inputPanel.populateFieldsFromRpt(rpt_file);
                            //make sure we have the up to date parameters.
                            params = inputPanel.getParams();

                            //Get the name of the RPT file
                            String rpt_name = rpt_file.getName();
                            //toeknize by "." and only use the first part (which should be the name of gsea run)
                            String[] tokens = rpt_name.split("\\.");
                            String name = tokens[0];

                            params.setNetworkName(name);

                            //build an enrichment map
                            BuildEnrichmentMapTask new_map = new BuildEnrichmentMapTask(params);
                            boolean success = TaskManager.executeTask(new_map, config);

                            //export the network to a pdf file in the main directory by the name of
                            // of the rpt file
                            try{
                                File outputFile = new File(mainDirectory, name + ".pdf" );
                                FileOutputStream outputstream = new FileOutputStream(outputFile);
                                CyNetworkView view  = Cytoscape.getCurrentNetworkView();

                                PDFExporter exporter = new PDFExporter();
                                exporter.export(view, outputstream);

                            }catch (FileNotFoundException e){

                            }catch (IOException e2){
                                System.out.println("Can't export network " + name + " to pdf.");
                            }

                        }

                    }
                }
            }

        }

       /*String errors = params.checkMinimalRequirements();

       if(errors.equalsIgnoreCase("")){
            BuildEnrichmentMapTask new_map = new BuildEnrichmentMapTask(params);
            boolean success = TaskManager.executeTask(new_map, config);
       }
       else{
           JOptionPane.showMessageDialog(Cytoscape.getDesktop(), errors, "Invalid Input", JOptionPane.WARNING_MESSAGE);
       }
        */
    }

}
