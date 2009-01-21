import cytoscape.task.ui.JTaskConfig;
import cytoscape.task.util.TaskManager;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.HashMap;

/**
 * Created by
 * User: risserlin
 * Date: Jan 8, 2009
 * Time: 1:35:40 PM
 */
public class BuildEnrichmentMapActionListener implements ActionListener {

    private JTaskConfig config;

    private InputFilesPanel inputPanel;
    private EnrichmentMapParameters params;

    public BuildEnrichmentMapActionListener(InputFilesPanel inputPanel, EnrichmentMapParameters params) {
        this.inputPanel = inputPanel;
        this.params = params;

    }

    public void actionPerformed(ActionEvent event) {

        GMTFileReader gmtFile;
        GSEAResultFileReader gseaResultsFiles;
        GCTFileReader gctFile;


        //set the pvalue, qvalue, and jaccardCurOff
        double pvalue = this.inputPanel.getPvalue();
        if(pvalue > 1.0 || pvalue < 0.0)
             JOptionPane.showMessageDialog(inputPanel,"invalid p-value");
        else{
            params.setPvalue(pvalue);
        }

        double qvalue = this.inputPanel.getQvalue();
        if(qvalue > 1.0 || qvalue < 0.0)
             JOptionPane.showMessageDialog(inputPanel,"invalid q-value");
        else{
            params.setQvalue(qvalue);
        }

        double jaccardCutOff = this.inputPanel.getJaccard();
        if(jaccardCutOff > 1.0 || jaccardCutOff < 0.0)
             JOptionPane.showMessageDialog(inputPanel,"invalid jaccard CutOff");
        else{
            params.setJaccardCutOff(jaccardCutOff);
        }


        //Initialize a Task to perform all the tasks
        //  Configure JTask
        config = new JTaskConfig();

        //  Show Cancel/Close Buttons
        config.displayCancelButton(true);
        config.displayStatus(true);

        //Load in the GMT file
        try{
            //Load the GSEA geneset file
            gmtFile = new GMTFileReader(params);
            boolean success = TaskManager.executeTask(gmtFile, config);


        } catch(Exception e){
            JOptionPane.showMessageDialog(inputPanel,"unable to load GMT file");

        }

        //Load in the GCT file
        try{
            //Load the GSEA geneset file
            gctFile = new GCTFileReader(params);
            boolean success = TaskManager.executeTask(gctFile, config);


        } catch(Exception e){
            JOptionPane.showMessageDialog(inputPanel,"unable to load GSEA DATA (.GCT) file");

        }

        //trim the genesets to only contain the genes that are in the data file.
        params.filterGenesets();

        try{
            //Load the GSEA result files
            //Dataset1 (each dataset should have two files.)
            GSEAResultFileReader gseaResultsFilesDataset1File1 = new GSEAResultFileReader(params, params.getGSEADataset1FileName1(), 1);
            boolean success1a = TaskManager.executeTask(gseaResultsFilesDataset1File1, config);
            GSEAResultFileReader gseaResultsFilesDataset1File2 = new GSEAResultFileReader(params, params.getGSEADataset1FileName2(), 1);
            boolean success1b = TaskManager.executeTask(gseaResultsFilesDataset1File2, config);

            //Load the second dataset only if there is a second dataset to load
            if (params.isTwoDatasets()){
                //Dataset2
                GSEAResultFileReader gseaResultsFilesDataset2File1 = new GSEAResultFileReader(params, params.getGSEADataset2FileName1(), 2);
                boolean success2a = TaskManager.executeTask(gseaResultsFilesDataset2File1, config);
                GSEAResultFileReader gseaResultsFilesDataset2File2 = new GSEAResultFileReader(params, params.getGSEADataset2FileName2(), 2);
                boolean success2b = TaskManager.executeTask(gseaResultsFilesDataset2File2, config);
            }

            //Initialize the set of genesets and GSEA results that we want to compute over
            InitializeGenesetsOfInterest genesets_init = new InitializeGenesetsOfInterest(params);
            boolean success4 = TaskManager.executeTask(genesets_init,config);

            //compute the geneset similarities
            ComputeSimilarity similarities = new ComputeSimilarity(params);
            boolean success5 = TaskManager.executeTask(similarities,config);
            HashMap similarity_results = similarities.getGeneset_similarities();

            BuildEnrichmentMap map = new BuildEnrichmentMap(params,similarity_results);
            boolean success3 = TaskManager.executeTask(map,config);

            //close input panel
            inputPanel.dispose();

        } catch(Exception e){

            JOptionPane.showMessageDialog(inputPanel,"unable to build map");

        }


     }
}
