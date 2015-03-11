package org.baderlab.csplugins.enrichmentmap.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.task.LoadSignatureGMTFilesTask;
import org.baderlab.csplugins.enrichmentmap.view.PostAnalysisInputPanel;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.work.swing.DialogTaskManager;

public class LoadSignatureSetsActionListener implements ActionListener {

	private PostAnalysisInputPanel inputPanel;
	private CySwingApplication application;
	private CyApplicationManager applicationManager;
	private DialogTaskManager dialog;
	private StreamUtil streamUtil;

	public LoadSignatureSetsActionListener(PostAnalysisInputPanel inputPanel,
			CySwingApplication application,
			CyApplicationManager applicationManager, 
			DialogTaskManager dialog,
			StreamUtil streamUtil) {
		this.inputPanel = inputPanel;
		this.application = application;
		this.applicationManager = applicationManager;
		this.dialog = dialog;
		this.streamUtil = streamUtil;

	}

	public void actionPerformed(ActionEvent event) {

		// make sure that the minimum information is set in the current set of
		// parameters
		EnrichmentMap current_map = EnrichmentMapManager.getInstance().getMap(applicationManager.getCurrentNetwork().getSUID());

		PostAnalysisParameters paParams = inputPanel.getPaParams();
		String errors = paParams.checkGMTfiles();

		if (errors.equalsIgnoreCase("")) {

			LoadSignatureGMTFilesTask load_GMTs = new LoadSignatureGMTFilesTask(current_map, paParams, streamUtil);

			dialog.execute(load_GMTs.createTaskIterator());

		} else {
			JOptionPane.showMessageDialog(application.getJFrame(), errors, "Invalid Input", JOptionPane.WARNING_MESSAGE);
		}
	}

}
