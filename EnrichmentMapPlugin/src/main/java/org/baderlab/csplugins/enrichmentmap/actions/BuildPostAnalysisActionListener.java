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


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.JOptionPane;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.task.BuildDiseaseSignatureTask;
import org.baderlab.csplugins.enrichmentmap.task.BuildDiseaseSignatureTaskResult;
import org.baderlab.csplugins.enrichmentmap.view.PostAnalysisInputPanel;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.swing.DialogTaskManager;


public class BuildPostAnalysisActionListener implements ActionListener {

    private PostAnalysisInputPanel inputPanel;
    private CyApplicationManager applicationManager;
    private CySwingApplication swingApplication;
    private CySessionManager sessionManager;
    private StreamUtil streamUtil;
    private DialogTaskManager dialog;
    private CyEventHelper eventHelper;

    public BuildPostAnalysisActionListener (PostAnalysisInputPanel panel,  
    		CySessionManager sessionManager, StreamUtil streamUtil, CySwingApplication swingApplication,
    		CyApplicationManager applicationManager, DialogTaskManager dialog,CyEventHelper eventHelper) {
        this.inputPanel = panel;
        this.sessionManager = sessionManager;
        this.streamUtil = streamUtil;
        this.applicationManager = applicationManager;
        this.swingApplication = swingApplication;
        this.dialog = dialog;
        this.eventHelper = eventHelper;

    }

    public void actionPerformed(ActionEvent event) {

        //make sure that the minimum information is set in the current set of parameters
    	PostAnalysisParameters paParams = inputPanel.getPaParams();
        
        EnrichmentMap current_map = EnrichmentMapManager.getInstance().getMap(applicationManager.getCurrentNetwork().getSUID());
        
        //set attribute prefix based on the selected Enrichment map
        if(current_map != null)
        	paParams.setAttributePrefix(current_map.getParams().getAttributePrefix());
        
        String errors = paParams.checkMinimalRequirements();
        TaskIterator currentTasks = new TaskIterator();

        if(errors.isEmpty()) {
            if(paParams.isSignatureDiscovery() || paParams.isKnownSignature()) {
                BuildDiseaseSignatureTask new_signature = new BuildDiseaseSignatureTask(current_map, paParams, sessionManager, streamUtil, applicationManager, eventHelper, swingApplication);
                currentTasks.append(new_signature);
                
                TaskObserver dialogObserver = new DialogObserver();
                dialog.execute(currentTasks, dialogObserver);
            } 
            else {
                JOptionPane.showMessageDialog(inputPanel, errors, "No such Post-Analysis", JOptionPane.WARNING_MESSAGE);
            }
        } 
        else {
            JOptionPane.showMessageDialog(inputPanel, errors, "Invalid Input", JOptionPane.WARNING_MESSAGE);
        }
    }

    
    private class DialogObserver implements TaskObserver {
    	BuildDiseaseSignatureTaskResult result;
    	
		@Override 
		public void taskFinished(ObservableTask task) {
			if(task instanceof BuildDiseaseSignatureTask) {
				result = task.getResults(BuildDiseaseSignatureTaskResult.class);
			}
		}
		
		@Override 
		public void allFinished(FinishStatus status) {
			if(result == null)
				return;
			
			if(result.getCreatedEdgeCount() == 0) {
				JOptionPane.showMessageDialog(swingApplication.getJFrame(), 
						"No edges were found passing the cutoff value for the signature set(s)", 
						"Post Analysis", JOptionPane.WARNING_MESSAGE);
			}

			if(!result.getExistingEdgesFailingCutoff().isEmpty()) {
				String[] options = {"Delete Edges From Previous Run", "Keep All Edges"};
				int dialogResult = JOptionPane.showOptionDialog(
						swingApplication.getJFrame(), 
						"There are edges from a previous run of post-analysis that do not pass the current cutoff value.\n"
						+ "Keep these edges or delete them?", 
						"Existing post-analysis edges", 
						JOptionPane.YES_NO_OPTION, 
						JOptionPane.QUESTION_MESSAGE, 
						null, 
						options, 
						options[1]);
				
				if(dialogResult == JOptionPane.YES_OPTION) {
					Set<CyEdge> edgesToDelete = result.getExistingEdgesFailingCutoff();
					CyNetwork network = result.getNetwork();
					network.removeEdges(edgesToDelete);
					result.getNetworkView().updateView();
				}
			}
			
			if(result.isWarnUserBypassStyle()) {
				JOptionPane.showMessageDialog(swingApplication.getJFrame(), 
						"The graph was created with an older version of EnrichmentMap.\n"
						+ "The Visual Properties used for Post Analysis nodes and edges have been set to bypass.\n\n"
						+ "If you would like your visual style to be upgraded so that it does not use bypass then \n"
						+ "please rebuild your Enrichment Map graph and then re-run Post Analysis.", 
						"Visual Property Bypass Used", JOptionPane.WARNING_MESSAGE);
			}
		}
	};
}
