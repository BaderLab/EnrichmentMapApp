package org.baderlab.csplugins.enrichmentmap.view.postanalysis;

import static org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil.invokeOnEDT;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Optional;
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
import org.baderlab.csplugins.enrichmentmap.task.CreateDiseaseSignatureTask;
import org.baderlab.csplugins.enrichmentmap.task.CreateDiseaseSignatureTaskFactory;
import org.baderlab.csplugins.enrichmentmap.task.CreateDiseaseSignatureTaskResult;
import org.baderlab.csplugins.enrichmentmap.view.control.ControlPanelMediator;
import org.baderlab.csplugins.enrichmentmap.view.creation.ErrorMessageDialog;
import org.baderlab.csplugins.enrichmentmap.view.creation.ErrorMessageDialog.MessageType;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class PostAnalysisPanelMediator {

	@Inject private EnrichmentMapManager emManager;
	@Inject private PostAnalysisInputPanel.Factory panelFactory;
	@Inject private Provider<ControlPanelMediator> controlPanelMediatorProvider;
	@Inject private CyServiceRegistrar serviceRegistrar;
	@Inject private CySwingApplication swingApplication;
	@Inject private DialogTaskManager taskManager;
	@Inject private CreateDiseaseSignatureTaskFactory.Factory signatureTaskFactoryFactory;
	@Inject private ErrorMessageDialog.Factory errorMessageDialogFactory;
	
	
	@SuppressWarnings("serial")
	public void showDialog(Component parent, CyNetworkView netView) {
		final EnrichmentMap map = emManager.getEnrichmentMap(netView.getModel().getSUID());
		
		invokeOnEDT(() -> {
			final PostAnalysisInputPanel panel = panelFactory.create(map);
			
			final JDialog dialog = new JDialog(swingApplication.getJFrame(), "Add Signature Gene Sets", ModalityType.APPLICATION_MODAL);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			
			JButton helpButton = SwingUtil.createOnlineHelpButton(EnrichmentMapBuildProperties.USER_MANUAL_URL, "Online Manual...", serviceRegistrar);
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
						Optional<PostAnalysisParameters> params = buildPostAnalysisParameters(panel, map, dialog);
						if(params.isPresent()) {
							addGeneSets(netView, params.get());
							dialog.dispose();
						} else {
							JOptionPane.showMessageDialog(panel, "Could not run post analysis.", "EnrichmentMap: Error", JOptionPane.WARNING_MESSAGE);
						}
					}
				}
			});

			JPanel buttonPanel =  LookAndFeelUtil.createOkCancelPanel(runButton, closeButton, helpButton, resetButton);
			
			dialog.getContentPane().add(panel, BorderLayout.CENTER);
			dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
			
			LookAndFeelUtil.setDefaultOkCancelKeyStrokes(dialog.getRootPane(), runButton.getAction(), closeButton.getAction());
			dialog.getRootPane().setDefaultButton(runButton);
			
			dialog.pack();
			dialog.setLocationRelativeTo(swingApplication.getJFrame());
			dialog.setVisible(true);
		});
	}
	
	
	private void addGeneSets(CyNetworkView netView, PostAnalysisParameters params) {
		CreateDiseaseSignatureTaskFactory taskFactory = signatureTaskFactoryFactory.create(netView, params);
		TaskIterator tasks = taskFactory.createTaskIterator();
		
		String errors = taskFactory.getErrors();
		if(errors.isEmpty()) {
			taskManager.execute(tasks, new DialogObserver());
		}
		else {
			JOptionPane.showMessageDialog(swingApplication.getJFrame(), errors, "Invalid Input", JOptionPane.WARNING_MESSAGE);
		}
	}
	
	
	/**
	 * Creates a PostAnalysisParameters object based on the user's input.
	 */
	private Optional<PostAnalysisParameters> buildPostAnalysisParameters(PostAnalysisInputPanel inputPanel, EnrichmentMap map, JDialog parent) {
		PostAnalysisParameters.Builder builder = new PostAnalysisParameters.Builder();
		
		List<String> messages = inputPanel.validateInput();
		if(!messages.isEmpty()) {
			ErrorMessageDialog dialog = errorMessageDialogFactory.create(parent);
			dialog.addSection(MessageType.ERROR, "Post Analysis: Error", IconManager.ICON_FILE_O, messages);
			dialog.pack();
			dialog.setLocationRelativeTo(parent);
			dialog.setModal(true);
			dialog.setVisible(true);
			return Optional.empty();
		}
		
		boolean built = inputPanel.build(builder);
		if(!built) {
			return Optional.empty();
		}
		
		builder.setAttributePrefix(map.getParams().getAttributePrefix());
		return Optional.of(builder.build());
	}
	
	
	private class DialogObserver implements TaskObserver {

		private CreateDiseaseSignatureTaskResult result;

		@Override
		public void taskFinished(ObservableTask task) {
			if (task instanceof CreateDiseaseSignatureTask)
				result = task.getResults(CreateDiseaseSignatureTaskResult.class);
		}

		@Override
		public void allFinished(FinishStatus status) {
			if (result == null || result.isCancelled())
				return;

			// Only update the view once the tasks are complete
			controlPanelMediatorProvider.get().updateDataSetList(result.getNetworkView());
			result.getNetworkView().updateView();

			if (result.getPassedCutoffCount() == 0)
				JOptionPane.showMessageDialog(
						swingApplication.getJFrame(),
						"No edges were found passing the cutoff value for the signature set(s)",
						"Post Analysis",
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
