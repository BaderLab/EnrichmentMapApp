package org.baderlab.csplugins.enrichmentmap.view.postanalysis;

import static org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil.invokeOnEDT;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.util.Optional;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapBuildProperties;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EMSignatureDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.model.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.model.PostAnalysisParameters.AnalysisType;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleOptions;
import org.baderlab.csplugins.enrichmentmap.task.ApplyEMStyleTask;
import org.baderlab.csplugins.enrichmentmap.task.CreateDiseaseSignatureTask;
import org.baderlab.csplugins.enrichmentmap.task.CreateDiseaseSignatureTaskResult;
import org.baderlab.csplugins.enrichmentmap.util.NamingUtil;
import org.baderlab.csplugins.enrichmentmap.view.control.ControlPanelMediator;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
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
	@Inject private CreateDiseaseSignatureTask.Factory signatureTaskFactory;
	@Inject private ApplyEMStyleTask.Factory applyStyleTaskFactory;
	@Inject private Provider<ControlPanelMediator> controlPanelMediatorProvider;
	
	@Inject private CyServiceRegistrar serviceRegistrar;
	@Inject private CyApplicationManager applicationManager;
	@Inject private DialogTaskManager taskManager;
	@Inject private CySwingApplication swingApplication;
	
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
						Optional<PostAnalysisParameters> params = buildPostAnalysisParameters(panel, map);
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
		// Make sure that the minimum information is set in the current set of parameters
		EnrichmentMap map = emManager.getEnrichmentMap(applicationManager.getCurrentNetwork().getSUID());
		
		StringBuilder errorBuilder = new StringBuilder();
		checkMinimalRequirements(errorBuilder, params);
		if (params.getRankTestParameters().getType().isMannWhitney() && map.getAllRanks().isEmpty())
			errorBuilder.append("Mann-Whitney requires ranks. \n");
		
		String errors = errorBuilder.toString();

		if (errors.isEmpty()) {
			TaskIterator tasks = new TaskIterator();

			String sdsName = NamingUtil.getUniqueName(params.getLoadedGMTGeneSets().getName(), map.getSignatureDataSets().keySet());
			EMSignatureDataSet sigDataSet = new EMSignatureDataSet(sdsName);
			map.addSignatureDataSet(sigDataSet);
			
			// Run Post-Analysis in batch, once for each data set
			for(EMDataSet dataset : map.getDataSetList()) {
				CreateDiseaseSignatureTask task = signatureTaskFactory.create(map, params, dataset.getName());
				task.setSignatureDataSet(sigDataSet);
				task.setCreateSeparateEdges(true);
				tasks.append(task);
			}
			
			ControlPanelMediator controlPanelMediator = controlPanelMediatorProvider.get();
			EMStyleOptions options = controlPanelMediator.createStyleOptions(netView);
			CyCustomGraphics2<?> chart = controlPanelMediator.createChart(options);
			tasks.append(applyStyleTaskFactory.create(options, chart));

			taskManager.execute(tasks, new DialogObserver());
		} else {
			JOptionPane.showMessageDialog(swingApplication.getJFrame(), errors, "Invalid Input", JOptionPane.WARNING_MESSAGE);
		}
	}
	
	
	/**
	 * Checks all values of the PostAnalysisInputPanel
	 * 
	 * @return String with error messages (one error per line) or empty String if everything is okay.
	 * @see org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapParameters#checkMinimalRequirements()
	 */
	public void checkMinimalRequirements(StringBuilder errors, PostAnalysisParameters params) {
		errors.append(checkGMTfiles(params));
		if(params.getSelectedGeneSetNames().isEmpty()) {
			errors.append("No Signature Genesets selected \n");
		}
	}

	/**
	 * Checks if SignatureGMTFileName is provided and if the file can be read.
	 * 
	 * @return String with error messages (one error per line) or empty String
	 *         if everything is okay.
	 */
	public String checkGMTfiles(PostAnalysisParameters params) {
		String signatureGMTFileName = params.getSignatureGMTFileName();
		if(signatureGMTFileName == null || signatureGMTFileName.isEmpty() || !EnrichmentMapParameters.checkFile(signatureGMTFileName))
			return "Signature GMT file can not be found \n";
		return "";
	}
	
	
	/**
	 * Creates a PostAnalysisParameters object based on the user's input.
	 */
	private Optional<PostAnalysisParameters> buildPostAnalysisParameters(PostAnalysisInputPanel panel, EnrichmentMap map) {
		PostAnalysisParameters.Builder builder = new PostAnalysisParameters.Builder();

		boolean built;
		if (panel.getKnownSignatureRadio().isSelected()) {
			builder.setAnalysisType(AnalysisType.KNOWN_SIGNATURE);
			built = panel.getKnownSignaturePanel().build(builder);
		} else {
			builder.setAnalysisType(AnalysisType.SIGNATURE_DISCOVERY);
			built = panel.getSignatureDiscoveryPanel().build(builder);
		}
		
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
