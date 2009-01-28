import cytoscape.task.ui.JTaskConfig;
import cytoscape.task.util.TaskManager;
import cytoscape.task.TaskMonitor;
import cytoscape.task.Task;


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

    public BuildEnrichmentMapActionListener (InputFilesPanel inputPanel, EnrichmentMapParameters params) {
        this.inputPanel = inputPanel;
        this.params = params;

    }

   public void actionPerformed(ActionEvent event) {

        config = new JTaskConfig();
        config.displayCancelButton(true);
        config.displayCloseButton(true);
        config.displayStatus(true);

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

        BuildGSEAEnrichmentMapTask new_map = new BuildGSEAEnrichmentMapTask(inputPanel,params);
        boolean success = TaskManager.executeTask(new_map,config);

     }


}
