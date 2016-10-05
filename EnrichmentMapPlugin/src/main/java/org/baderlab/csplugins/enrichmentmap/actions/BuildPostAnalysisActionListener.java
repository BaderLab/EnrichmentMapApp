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

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.model.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.task.BuildDiseaseSignatureTask;
import org.baderlab.csplugins.enrichmentmap.task.BuildDiseaseSignatureTaskResult;
import org.baderlab.csplugins.enrichmentmap.task.CreatePostAnalysisVisualStyleTask;
import org.baderlab.csplugins.enrichmentmap.task.ShowPanelTask;
import org.baderlab.csplugins.enrichmentmap.view.ParametersPanel;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class BuildPostAnalysisActionListener implements ActionListener {

	@Inject private CreatePostAnalysisVisualStyleTask.Factory paStyleTaskFactory;
	@Inject private BuildDiseaseSignatureTask.Factory signatureTaskFactory;
	@Inject private CyApplicationManager applicationManager;
	@Inject private CySwingApplication swingApplication;
	@Inject private DialogTaskManager dialog;
	@Inject private EnrichmentMapManager emManager;

	private final PostAnalysisParameters paParams;

	public interface Factory {
		BuildPostAnalysisActionListener create(PostAnalysisParameters paParams);
	}
	
	@Inject
	public BuildPostAnalysisActionListener(@Assisted PostAnalysisParameters paParams) {
		this.paParams = paParams;
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		runPostAnalysis();
	}

	public void runPostAnalysis() {
		//make sure that the minimum information is set in the current set of parameters
		EnrichmentMap map = emManager.getMap(applicationManager.getCurrentNetwork().getSUID());

		StringBuilder errorBuilder = new StringBuilder();
		paParams.checkMinimalRequirements(errorBuilder);
		if(paParams.getRankTestParameters().getType().isMannWhitney() && map.getAllRanks().isEmpty()) {
			errorBuilder.append("Mann-Whitney requires ranks. \n");
		}
		String errors = errorBuilder.toString();

		if(errors.isEmpty()) {
			TaskIterator currentTasks = new TaskIterator();

			BuildDiseaseSignatureTask new_signature = signatureTaskFactory.create(map, paParams);
			currentTasks.append(new_signature);

			CreatePostAnalysisVisualStyleTask visualStyleTask = paStyleTaskFactory.create(map);
			currentTasks.append(visualStyleTask);

			ParametersPanel paramsPanel = emManager.getParameterPanel();
			ShowPanelTask show_parameters_panel = new ShowPanelTask(swingApplication, paramsPanel);
			currentTasks.append(show_parameters_panel);

			TaskObserver dialogObserver = new DialogObserver(visualStyleTask);
			dialog.execute(currentTasks, dialogObserver);
		} else {
			JOptionPane.showMessageDialog(swingApplication.getJFrame(), errors, "Invalid Input", JOptionPane.WARNING_MESSAGE);
		}
	}

	private class DialogObserver implements TaskObserver {

		private CreatePostAnalysisVisualStyleTask visualStyleTask;
		private BuildDiseaseSignatureTaskResult result;

		private DialogObserver(CreatePostAnalysisVisualStyleTask visualStyleTask) {
			this.visualStyleTask = visualStyleTask;
		}

		@Override
		public void taskFinished(ObservableTask task) {
			if(task instanceof BuildDiseaseSignatureTask) {
				result = task.getResults(BuildDiseaseSignatureTaskResult.class);
				// Is there a better way to pass results from one task to another?
				visualStyleTask.setBuildDiseaseSignatureTaskResult(result);
			}
		}

		@Override
		public void allFinished(FinishStatus status) {
			if(result == null || result.isCancelled())
				return;

			// Only update the view once the tasks are complete
			result.getNetworkView().updateView();

			if(result.getPassedCutoffCount() == 0) {
				JOptionPane.showMessageDialog(swingApplication.getJFrame(),
						"No edges were found passing the cutoff value for the signature set(s)", 
						"Post Analysis",
						JOptionPane.WARNING_MESSAGE);
			}

			if(!result.getExistingEdgesFailingCutoff().isEmpty()) {
				String[] options = { "Delete Edges From Previous Run", "Keep All Edges" };
				int dialogResult = 
					JOptionPane.showOptionDialog(swingApplication.getJFrame(),
						"There are edges from a previous run of post-analysis that do not pass the current cutoff value.\nKeep these edges or delete them?",
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
		}
	};
}
