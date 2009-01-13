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

        try{
            //Load the GSEA result files
            //UP
            GSEAResultFileReader gseaResultsFilesUP = new GSEAResultFileReader(params, params.getGSEAUpFileName());
            boolean success1 = TaskManager.executeTask(gseaResultsFilesUP, config);

            //Down
            GSEAResultFileReader gseaResultsFilesDOWN = new GSEAResultFileReader(params, params.getGSEADownFileName());
            boolean success2 = TaskManager.executeTask(gseaResultsFilesDOWN, config);


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
