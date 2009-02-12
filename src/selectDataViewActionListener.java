
import cytoscape.view.CytoscapeDesktop;
import cytoscape.view.cytopanels.CytoPanel;
import cytoscape.Cytoscape;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Created by
 * User: risserlin
 * Date: Feb 12, 2009
 * Time: 10:04:33 AM
 */
public class selectDataViewActionListener implements ActionListener {

    private OverlappingGenesPanel edgeOverlapPanel;
    private OverlappingGenesPanel nodeOverlapPanel;

    private HeatMapParameters hmParams;

    public selectDataViewActionListener(OverlappingGenesPanel edgeOverlapPanel, OverlappingGenesPanel nodeOverlapPanel, HeatMapParameters hmParams) {
        this.edgeOverlapPanel = edgeOverlapPanel;
        this.nodeOverlapPanel = nodeOverlapPanel;
        this.hmParams = hmParams;
    }

    public void actionPerformed(ActionEvent evt){

       edgeOverlapPanel.clearPanel();
       nodeOverlapPanel.clearPanel();

       if(evt.getActionCommand().equalsIgnoreCase("asis")){
           hmParams.setRowNorm(false);
           hmParams.setLogtransform(false);
        }
        else if(evt.getActionCommand().equalsIgnoreCase("rownorm")){
           hmParams.setRowNorm(true);
           hmParams.setLogtransform(false);
        }
        else if(evt.getActionCommand().equalsIgnoreCase("logtransform")){
           hmParams.setRowNorm(false);
           hmParams.setLogtransform(true);
        }

        hmParams.ResetColorGradient();
        edgeOverlapPanel.updatePanel();
        nodeOverlapPanel.updatePanel();

        final CytoscapeDesktop desktop = Cytoscape.getDesktop();
        CytoPanel cytoPanel = desktop.getCytoPanel(SwingConstants.SOUTH);

        int index  = cytoPanel.getSelectedIndex();
        cytoPanel.setSelectedIndex(index);

    }
}
