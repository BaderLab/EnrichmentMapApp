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

// $Id: HeatMapActionListener.java 368 2009-09-25 14:49:59Z risserlin $
// $LastChangedDate: 2009-09-25 10:49:59 -0400 (Fri, 25 Sep 2009) $
// $LastChangedRevision: 368 $
// $LastChangedBy: risserlin $
// $HeadURL: svn+ssh://risserlin@server1.baderlab.med.utoronto.ca/svn/EnrichmentMap/trunk/EnrichmentMapPlugin/src/org/baderlab/csplugins/enrichmentmap/HeatMapActionListener.java $

package org.baderlab.csplugins.enrichmentmap;

import cytoscape.view.CytoscapeDesktop;
import cytoscape.view.cytopanels.CytoPanel;
import cytoscape.Cytoscape;
import cytoscape.util.FileUtil;
import cytoscape.util.CyFileFilter;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.io.File;

/**
 * Created by
 * User: risserlin
 * Date: Feb 12, 2009
 * Time: 10:04:33 AM
 * <p>
 * Heat map action listener
 */
public class HeatMapActionListener implements ActionListener {

    private HeatMapPanel edgeOverlapPanel;
    private HeatMapPanel nodeOverlapPanel;

    private HeatMapParameters hmParams;
    private JComboBox box;

    //Need to add the enrichment map parameters here in order to add an additional ranks to the EM
    private EnrichmentMapParameters params;

    /**
     * Class constructor
     *
     * @param edgeOverlapPanel
     * @param nodeOverlapPanel
     * @param box
     * @param hmParams
     * @param params
     */
    public HeatMapActionListener(HeatMapPanel edgeOverlapPanel, HeatMapPanel nodeOverlapPanel,JComboBox box, HeatMapParameters hmParams, EnrichmentMapParameters params) {
        this.edgeOverlapPanel = edgeOverlapPanel;
        this.nodeOverlapPanel = nodeOverlapPanel;
        this.hmParams = hmParams;
        this.box= box;
        this.params = params;
    }

    /**
     * Update heat map according to action selection
     *
     * @param evt
     */
    public void actionPerformed(ActionEvent evt){

       edgeOverlapPanel.clearPanel();
       nodeOverlapPanel.clearPanel();
       String select=(String) box.getSelectedItem();

       if(select.equalsIgnoreCase("Data As Is")){
           hmParams.setRowNorm(false);
           hmParams.setLogtransform(false);
           hmParams.setAsIS(true);
        }
        else if(select.equalsIgnoreCase("Row Normalize Data")){
           hmParams.setRowNorm(true);
           hmParams.setLogtransform(false);
           hmParams.setAsIS(false);
        }
        else if(select.equalsIgnoreCase("Log Transform Data")){
           hmParams.setRowNorm(false);
           hmParams.setLogtransform(true);
           hmParams.setAsIS(false);
        }
        else if(select.equalsIgnoreCase(HeatMapParameters.sort_hierarchical_cluster)){
           hmParams.setSortbyHC(true);
           hmParams.setNoSort(false);
           hmParams.setSortbyrank(false);
           hmParams.setSortbycolumn(false);
           hmParams.setSortIndex(-1);
        }
        //Add a ranking file
        else if(select.equalsIgnoreCase("Add Rankings ... ")){

           HashMap<String, HashMap<Integer, Ranking>> all_ranks = params.getRanks();

           CyFileFilter filter = new CyFileFilter();

            // Add accepted File Extensions
           filter.addExtension("txt");
           filter.addExtension("xls");
           filter.addExtension("rnk");

           // Get the file name
            File file = FileUtil.getFile("Import new Rank file", FileUtil.LOAD, new CyFileFilter[]{ filter });
            if(file != null) {
                //find out from the user what they want to name these ranking

                String ranks_name = JOptionPane.showInputDialog(Cytoscape.getDesktop(),"What would you like to name these Rankings?", "My Rankings");
                boolean noname = true;
                while(noname){
                    noname = false;
                    //make sure the name is not already in the rankings
                    for(Iterator j = all_ranks.keySet().iterator(); j.hasNext(); ){
                        String current_name = j.next().toString();
                        if(current_name.equalsIgnoreCase(ranks_name)){
                            noname = true;
                            ranks_name = JOptionPane.showInputDialog(Cytoscape.getDesktop(),"Sorry that name already exists.Please choose another name.");
                        }
                    }
                }

                //load the new ranks file
                RanksFileReaderTask ranking1 = new RanksFileReaderTask(params,file.getAbsolutePath(),ranks_name);
                ranking1.run();

            }
       }
       else if(select.equalsIgnoreCase(HeatMapParameters.sort_none)){
           hmParams.setSortbyHC(false);
           hmParams.setNoSort(true);
           hmParams.setSortbyrank(false);
           hmParams.setSortbycolumn(false);
           hmParams.setSortIndex(-1);
       }

       else{
           HashMap<String, HashMap<Integer, Ranking>> ranks = params.getRanks();
           for(Iterator j = ranks.keySet().iterator(); j.hasNext(); ){
                String ranks_name = j.next().toString();
                if(ranks_name.equalsIgnoreCase(select)){
                    hmParams.setSortbyrank(true);
                    hmParams.setSortbyHC(false);
                    hmParams.setSortbycolumn(false);
                    hmParams.setNoSort(false);
                    hmParams.setRankFileIndex(ranks_name);
                    hmParams.setSortIndex(-1);
                }
            }
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
