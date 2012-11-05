package org.baderlab.csplugins.enrichmentmap.actions;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.data.writers.CytoscapeSessionWriter;
import cytoscape.data.writers.XGMMLWriter;
import cytoscape.view.CyNetworkView;
import cytoscape.util.export.PDFExporter;
import cytoscape.task.ui.JTaskConfig;
import cytoscape.task.util.TaskManager;
import giny.view.NodeView;

import javax.swing.*;
import javax.xml.bind.JAXBException;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.task.BuildEnrichmentMapTask;
import org.baderlab.csplugins.enrichmentmap.view.BulkEMCreationPanel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URISyntaxException;
import java.util.Iterator;

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


        //if the bulk results are for GSEA
        if(params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_GSEA)){

            for(int i=0;i<gsea_results.length;i++){
                File current = new File(mainDirectory, gsea_results[i]);
                //if this is a directory - go into it and get the .rpt file
                if(current.isDirectory()){
                    String[] children = current.list();
                    boolean foundRpt = false;
                    for(int k=0;k<children.length;k++){
                         if(children[k].endsWith(".rpt")){ // AL

		                    foundRpt = true; // AL

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

                                //params.setNetworkName(name);

                                //build an enrichment map
                                BuildEnrichmentMapTask new_map = new BuildEnrichmentMapTask(params,name);
                                boolean success = TaskManager.executeTask(new_map, config);

                                Cytoscape.getCurrentNetworkView().fitContent();

                                //reduce height and width by 50%
                                Cytoscape.getCurrentNetworkView().setZoom(0.5);
                                Cytoscape.getCurrentNetworkView().updateView();

                                //Cytoscape.getCurrentNetworkView().redrawGraph(true,true);

                                //export the network to a pdf file in the main directory by the name of
                                // of the rpt file
                                try{
                                    File outputFile = new File(mainDirectory, name + ".xgmml" );
                                    //FileOutputStream outputstream = new FileOutputStream(outputFile);
                                    CyNetworkView view  = Cytoscape.getCurrentNetworkView();
                                    CyNetwork nw = Cytoscape.getCurrentNetwork(); // AL

                                    //PDFExporter exporter = new PDFExporter();
                                    //exporter.export(view, outputstream);

                                    // AL start
		    		                FileWriter writer = new FileWriter(outputFile);

		    		                XGMMLWriter exporter = new XGMMLWriter(nw, view);
		    		                exporter.write(writer);
		    		                writer.close();
		    		                // AL end

                                    //output the session
                                    CytoscapeSessionWriter session = new CytoscapeSessionWriter(mainDirectory + File.separator + name + ".cys");
                                    System.out.println(mainDirectory + File.separator + name + ".cys");
                                    session.writeSessionToDisk();

                                    //make sure to empty the Enrichment map parameters
                                    Cytoscape.destroyNetwork(name);

                                    // AL
		    		                System.gc();

                                    //create a new session for the next network
                                    Cytoscape.createNewSession();

                                }
                                catch (FileNotFoundException e){
                                    System.out.println("Can't export network " + name + " .");
                                }catch (IOException e2){
                                   // AL start
                                    System.out.println("Can't export network " + name + " to xgmml.");
                                }catch (URISyntaxException e3) {
                                    System.out.println("Can't export network " + name + " to xgmml.");
		    	                }catch (JAXBException e4) {
                                    System.out.println("Can't export network " + name + " to xgmml.");
                                //}catch (Exception e3){
                                }catch (Exception eRest){ // AL end
                                    System.out.println("Can't export network " + name + ".cys");
                                }


                            }

                        }
                    }
                }

            }
        }
        else if(params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_generic)){

            //get the gmt file from the directory
            String gmtfile="";
            for(int i=0;i<gsea_results.length;i++){
                //if this is a directory - go into it and get the .rpt file
                if(gsea_results[i].endsWith("gmt")){
                    if(gmtfile.equalsIgnoreCase("")){
                        File current = new File(mainDirectory, gsea_results[i]);
                        gmtfile = current.getAbsolutePath();
                        params.setGMTFileName(gmtfile);
                    }
                    else{
                        JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "There are multiple gmt files defined." + gsea_results + ", " + gmtfile, "Too many gmt files", JOptionPane.WARNING_MESSAGE);
                    }
                }
            }

            if(gmtfile.equalsIgnoreCase(""))
                JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "There are no gmt files defined.  In order to run bulk EM with generic outputs a gmt file needs to be in the same directory with all the enrichment results" , "No gmt file found", JOptionPane.WARNING_MESSAGE);

            //go through all the files in the directory
            //for each txt file create an enrichment map.
            for(int i=0;i<gsea_results.length;i++){
                File current = new File(mainDirectory, gsea_results[i]);
                //if this is a directory - go into it and get the .rpt file

                if(gsea_results[i].endsWith(".txt")){

                    params.setEnrichmentDataset1FileName1(current.getAbsolutePath());

                    //make sure we have the up to date parameters.
                    params = inputPanel.getParams();

                    //Get the name of the RPT file
                    String run_name = current.getName();
                    //toeknize by "." and only use the first part (which should be the name of gsea run)
                    //String[] tokens = run_name.split("\\.");
                    //String name = tokens[0];

                    String name = run_name;
                    //params.setNetworkName(name);

                    //build an enrichment map
                    BuildEnrichmentMapTask new_map = new BuildEnrichmentMapTask(params,name);
                    boolean success = TaskManager.executeTask(new_map, config);

                    Cytoscape.getCurrentNetworkView().fitContent();

                    //reduce height and width by 50%
                    Cytoscape.getCurrentNetworkView().setZoom(0.5);
                    Cytoscape.getCurrentNetworkView().updateView();

                    //Cytoscape.getCurrentNetworkView().redrawGraph(true,true);

                    //export the network to a pdf file in the main directory by the name of
                    // of the rpt file
                    if(inputPanel.isSessions()){
                        try{
                            File outputFile = new File(mainDirectory, name + ".xgmml" );
                            //FileOutputStream outputstream = new FileOutputStream(outputFile);
                            CyNetworkView view  = Cytoscape.getCurrentNetworkView();
                            CyNetwork nw = Cytoscape.getCurrentNetwork(); // AL

                            //PDFExporter exporter = new PDFExporter();
                            //exporter.export(view, outputstream);

                            // AL start
		                     FileWriter writer = new FileWriter(outputFile);

		                     XGMMLWriter exporter = new XGMMLWriter(nw, view);
		                     exporter.write(writer);
		                     writer.close();
		                     // AL end

                            //output the session
                            CytoscapeSessionWriter session = new CytoscapeSessionWriter(mainDirectory + File.separator + name + ".cys");
                            System.out.println(mainDirectory + File.separator + name + ".cys");
                            session.writeSessionToDisk();

                            //make sure to empty the Enrichment map parameters
                            Cytoscape.destroyNetwork(name);

                            // AL
		                     System.gc();

                            //create a new session for the next network
                            Cytoscape.createNewSession();

                        }
                        catch (FileNotFoundException e){
                            System.out.println("Can't export network " + name + " .");
                        }catch (IOException e2){
                           // AL start
                            System.out.println("Can't export network " + name + " to xgmml.");
                        }catch (URISyntaxException e3) {
                            System.out.println("Can't export network " + name + " to xgmml.");
		                 }catch (JAXBException e4) {
                            System.out.println("Can't export network " + name + " to xgmml.");
                        //}catch (Exception e3){
                        }catch (Exception eRest){ // AL end
                            System.out.println("Can't export network " + name + ".cys");
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
