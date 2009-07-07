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
        else if(evt.getActionCommand().equalsIgnoreCase("noSort")){
           hmParams.setRank_dataset1(false);
           hmParams.setRank_dataset2(false);
        }
        else if(evt.getActionCommand().equalsIgnoreCase("dataset1")){
           hmParams.setRank_dataset1(true);
           hmParams.setRank_dataset2(false);
        }
        else if(evt.getActionCommand().equalsIgnoreCase("dataset2")){
           hmParams.setRank_dataset1(false);
           hmParams.setRank_dataset2(true);
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
