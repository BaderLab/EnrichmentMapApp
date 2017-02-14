package org.baderlab.csplugins.enrichmentmap.view.postanalysis;

import static org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil.invokeOnEDT;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapBuildProperties;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.model.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.model.PostAnalysisParameters.AnalysisType;
import org.baderlab.csplugins.enrichmentmap.task.BuildDiseaseSignatureTask;
import org.baderlab.csplugins.enrichmentmap.task.BuildDiseaseSignatureTaskResult;
import org.baderlab.csplugins.enrichmentmap.task.CreatePostAnalysisVisualStyleTask;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class PostAnalysisPanelMediator {

	@Inject private EnrichmentMapManager emManager;
	@Inject private PostAnalysisInputPanel.Factory panelFactory;
	@Inject private BuildDiseaseSignatureTask.Factory signatureTaskFactory;
	@Inject private CreatePostAnalysisVisualStyleTask.Factory paStyleTaskFactory;
	
	@Inject private CyServiceRegistrar serviceRegistrar;
	@Inject private CyApplicationManager applicationManager;
	@Inject private DialogTaskManager taskManager;
	@Inject private CySwingApplication swingApplication;
	
	@SuppressWarnings("serial")
	public void showDialog(Component parent, EnrichmentMap map) {
		invokeOnEDT(() -> {
			final PostAnalysisInputPanel panel = panelFactory.create(map);
			
			final JDialog dialog = new JDialog(swingApplication.getJFrame(), "Add Signature Gene Sets",
					ModalityType.APPLICATION_MODAL);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			
			JButton helpButton = SwingUtil.createOnlineHelpButton(EnrichmentMapBuildProperties.USER_MANUAL_URL,
					"Online Manual...", serviceRegistrar);

			JButton resetButton = new JButton("Reset");
			resetButton.addActionListener(e -> panel.reset());

			JButton closeButton = new JButton(new AbstractAction("Close") {
				@Override
				public void actionPerformed(ActionEvent e) {
					dialog.dispose();
				}
			});
			JButton runButton = new JButton(new AbstractAction("Add") {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (panel.isReady()) {
						addGeneSets(buildPostAnalysisParameters(panel, map));
						dialog.dispose();
					}
				}
			});

			JPanel buttonPanel =  LookAndFeelUtil.createOkCancelPanel(runButton, closeButton, helpButton, resetButton);
			
			dialog.getContentPane().add(panel, BorderLayout.CENTER);
			dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
			
			LookAndFeelUtil.setDefaultOkCancelKeyStrokes(dialog.getRootPane(), runButton.getAction(),
					closeButton.getAction());
			dialog.getRootPane().setDefaultButton(runButton);
			
			dialog.pack();
			dialog.setLocationRelativeTo(parent);
			dialog.setVisible(true);
		});
	}
	
	private void addGeneSets(PostAnalysisParameters params) {
		// Make sure that the minimum information is set in the current set of parameters
		EnrichmentMap map = emManager.getEnrichmentMap(applicationManager.getCurrentNetwork().getSUID());
		StringBuilder errorBuilder = new StringBuilder();
		params.checkMinimalRequirements(errorBuilder);
		
		if (params.getRankTestParameters().getType().isMannWhitney() && map.getAllRanks().isEmpty())
			errorBuilder.append("Mann-Whitney requires ranks. \n");
		
		String errors = errorBuilder.toString();

		if (errors.isEmpty()) {
			TaskIterator currentTasks = new TaskIterator();

			BuildDiseaseSignatureTask buildDiseaseSignatureTask = signatureTaskFactory.create(map, params);
			currentTasks.append(buildDiseaseSignatureTask);

			CreatePostAnalysisVisualStyleTask visualStyleTask = paStyleTaskFactory.create(map);
			currentTasks.append(visualStyleTask);

			taskManager.execute(currentTasks, new DialogObserver(visualStyleTask));
		} else {
			JOptionPane.showMessageDialog(swingApplication.getJFrame(), errors, "Invalid Input",
					JOptionPane.WARNING_MESSAGE);
		}
	}
	
	/**
	 * Creates a PostAnalysisParameters object based on the user's input.
	 */
	private PostAnalysisParameters buildPostAnalysisParameters(PostAnalysisInputPanel panel, EnrichmentMap map) {
		PostAnalysisParameters.Builder builder = new PostAnalysisParameters.Builder();

		if (panel.getKnownSignatureRadio().isSelected()) {
			builder.setAnalysisType(AnalysisType.KNOWN_SIGNATURE);
			panel.getKnownSignaturePanel().build(builder);
		} else {
			builder.setAnalysisType(AnalysisType.SIGNATURE_DISCOVERY);
			panel.getSignatureDiscoveryPanel().build(builder);
		}
		
		builder.setAttributePrefix(map.getParams().getAttributePrefix());
		
		return builder.build();
	}
	
	private class DialogObserver implements TaskObserver {

		private CreatePostAnalysisVisualStyleTask visualStyleTask;
		private BuildDiseaseSignatureTaskResult result;

		private DialogObserver(CreatePostAnalysisVisualStyleTask visualStyleTask) {
			this.visualStyleTask = visualStyleTask;
		}

		@Override
		public void taskFinished(ObservableTask task) {
			if (task instanceof BuildDiseaseSignatureTask) {
				result = task.getResults(BuildDiseaseSignatureTaskResult.class);
				// Is there a better way to pass results from one task to another?
				visualStyleTask.setBuildDiseaseSignatureTaskResult(result);
			}
		}

		@Override
		public void allFinished(FinishStatus status) {
			if (result == null || result.isCancelled())
				return;

			// Only update the view once the tasks are complete
			result.getNetworkView().updateView();

			if (result.getPassedCutoffCount() == 0)
				JOptionPane.showMessageDialog(
						swingApplication.getJFrame(),
						"No edges were found passing the cutoff value for the signature set(s)", "Post Analysis",
						JOptionPane.WARNING_MESSAGE
				);

			if (!result.getExistingEdgesFailingCutoff().isEmpty()) {
				String[] options = { "Delete Edges From Previous Run", "Keep All Edges" };
				int dialogResult = JOptionPane.showOptionDialog(
						swingApplication.getJFrame(),
						"There are edges from a previous run of post-analysis that do not pass the current cutoff value.\nKeep these edges or delete them?",
						"Existing post-analysis edges",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null,
						options,
						options[1]
				);

				if (dialogResult == JOptionPane.YES_OPTION) {
					Set<CyEdge> edgesToDelete = result.getExistingEdgesFailingCutoff();
					CyNetwork network = result.getNetwork();
					network.removeEdges(edgesToDelete);
					result.getNetworkView().updateView();
				}
			}
		}
	}
}
