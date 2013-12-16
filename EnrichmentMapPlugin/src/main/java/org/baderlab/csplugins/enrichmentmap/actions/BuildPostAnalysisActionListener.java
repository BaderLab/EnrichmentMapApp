/*
 *                       EnrichmentMap Cytoscape Plugin
 *
 * Copyright (c) 2008-2009 Bader Lab, Donnelly Centre for Cellular and Biomolecular 
 * Research, University of Toronto
 *
 * Contact: http://www.baderlab.org
 *
 * Code written by: Ruth Isserlin
 * Authors: Daniele Merico, Ruth Isserlin, Oliver Stueker, Gary D. Bader
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * University of Toronto
 * has no obligations to provide maintenance, support, updates, 
 * enhancements or modifications.  In no event shall the
 * University of Toronto
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * University of Toronto
 * has been advised of the possibility of such damage.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 *
 */

// $Id$
// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$
// $HeadURL$

package org.baderlab.csplugins.enrichmentmap.actions;
import cytoscape.task.ui.JTaskConfig;
import cytoscape.task.util.TaskManager;
import cytoscape.Cytoscape;

import javax.swing.*;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.task.BuildDiseaseSignatureTask;
import org.baderlab.csplugins.enrichmentmap.view.PostAnalysisInputPanel;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Created by:
 * @author revilo
 * <p>
 * Date Jul 10, 2009<br>
 * Time 2:35:40 PM<br>
 * 
 * Based on: BuildEnrichmentMapActionListener.java (288) by risserlin
 */
public class BuildPostAnalysisActionListener implements ActionListener {

    private JTaskConfig config;

    private PostAnalysisInputPanel inputPanel;

    public BuildPostAnalysisActionListener (PostAnalysisInputPanel panel) {
        this.inputPanel = panel;

    }

    public void actionPerformed(ActionEvent event) {

        //make sure that the minimum information is set in the current set of parameters
        PostAnalysisParameters paParams = inputPanel.getPaParams();
        
        EnrichmentMap current_map = EnrichmentMapManager.getInstance().getMap(Cytoscape.getCurrentNetwork().getIdentifier());

        String errors = paParams.checkMinimalRequirements();

        if(errors.equalsIgnoreCase("")) {
            if ( paParams.isSignatureDiscovery() ) {
                config = new JTaskConfig();
                config.displayCancelButton(true);
                config.displayCloseButton(true);
                config.displayStatus(true);

                BuildDiseaseSignatureTask new_signature = new BuildDiseaseSignatureTask(current_map, paParams);
                /*boolean success =*/ TaskManager.executeTask(new_signature, config);
            } 
            else {
                JOptionPane.showMessageDialog(Cytoscape.getDesktop(),errors,"No such Post-Analysis",JOptionPane.WARNING_MESSAGE);
            }
        } 
        else {
            JOptionPane.showMessageDialog(Cytoscape.getDesktop(),errors,"Invalid Input",JOptionPane.WARNING_MESSAGE);
        }
    }

}
