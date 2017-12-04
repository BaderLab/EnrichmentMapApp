package org.baderlab.csplugins.enrichmentmap.view.postanalysis;

import java.util.Set;
import java.util.WeakHashMap;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.model.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.task.postanalysis.CreatePANetworkTask;
import org.baderlab.csplugins.enrichmentmap.task.postanalysis.CreatePANetworkTaskResult;
import org.baderlab.csplugins.enrichmentmap.task.postanalysis.PATaskFactory;
import org.baderlab.csplugins.enrichmentmap.view.control.ControlPanelMediator;
import org.baderlab.csplugins.enrichmentmap.view.util.CardDialog;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class PADialogMediator  {

	public static final String NAME = "Add Signature Gene Sets...";
	
	@Inject private DialogTaskManager dialogTaskManager;
	@Inject private Provider<JFrame> jFrameProvider;

	@Inject private EnrichmentMapManager emManager;
	@Inject private Provider<ControlPanelMediator> controlPanelMediatorProvider;
	@Inject private PATaskFactory.Factory taskFactoryFactory;
	@Inject private PADialogParameters.Factory paDialogParametersFactory;
	
	
	private WeakHashMap<EnrichmentMap,CardDialog<Void>> dialogs = new WeakHashMap<>();
	

	public void showDialog(CyNetworkView netView) {
		EnrichmentMap map = emManager.getEnrichmentMap(netView.getModel().getSUID());
		if(map == null)
			return;
		
		CardDialog<Void> dialog = dialogs.computeIfAbsent(map, k -> {
			PADialogParameters params = paDialogParametersFactory.create(map, netView);
			return new CardDialog<>(jFrameProvider.get(), params);
		});
		
		dialog.open();
	}
	
	public void removeEnrichmentMap(EnrichmentMap map) {
		dialogs.remove(map);
	}
	
	
	protected void runPostAnalysis(EnrichmentMap map, CyNetworkView netView, PostAnalysisParameters params) {
		TaskIterator tasks = new TaskIterator();
		PATaskFactory taskFactory = taskFactoryFactory.create(netView, params);
		tasks.append(taskFactory.createTaskIterator());
		
		// Close the dialog after the progress dialog finishes normally
		tasks.append(new AbstractTask() {
			public void run(TaskMonitor taskMonitor) {
				CardDialog<Void> dialog = dialogs.get(map);
				if(dialog != null)
					dialog.getCallback().close();
			}
		});
		
		dialogTaskManager.execute(tasks, new DialogObserver());
	}
	
	
	/**
	 * This class isn't very important anymore.
	 * Prior to v3.0 post-analysis did not create a separate dataset, it just added its nodes/edges to the existing 'DataSet 1'.
	 * That meant that running post-analysis a second time basically overwrote the previous run.
	 * Now running post-analysis a second time creates a separate signature data set so the problems created by overwriting
	 * are not much of a concern anymore. However if the user explicitly uses the name of an existing signature data
	 * set then we still need to do these checks.
	 */
	private class DialogObserver implements TaskObserver {

		private CreatePANetworkTaskResult result;

		@Override
		public void taskFinished(ObservableTask task) {
			if (task instanceof CreatePANetworkTask)
				result = task.getResults(CreatePANetworkTaskResult.class);
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
						jFrameProvider.get(),
						"No edges were found passing the cutoff value for the signature set(s)",
						"Post Analysis",
						JOptionPane.WARNING_MESSAGE
				);

			if (!result.getExistingEdgesFailingCutoff().isEmpty()) {
				String[] options = { "Delete Edges From Previous Run", "Keep All Edges" };
				int dialogResult = JOptionPane.showOptionDialog(
						jFrameProvider.get(),
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
