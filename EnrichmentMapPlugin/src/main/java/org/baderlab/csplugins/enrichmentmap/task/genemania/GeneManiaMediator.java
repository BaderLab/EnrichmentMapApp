package org.baderlab.csplugins.enrichmentmap.task.genemania;

import static org.baderlab.csplugins.enrichmentmap.task.genemania.QueryGeneManiaTask.GENEMANIA_NAMESPACE;
import static org.baderlab.csplugins.enrichmentmap.task.genemania.QueryGeneManiaTask.GENEMANIA_ORGANISMS_COMMAND;
import static org.baderlab.csplugins.enrichmentmap.task.genemania.QueryGeneManiaTask.GENEMANIA_SEARCH_COMMAND;
import static org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil.invokeOnEDT;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.baderlab.csplugins.enrichmentmap.model.AssociatedApp;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.util.TaskUtil;
import org.baderlab.csplugins.enrichmentmap.view.util.OpenBrowser;
import org.cytoscape.command.AvailableCommands;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.json.JSONResult;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class GeneManiaMediator {

	@Inject private QueryGeneManiaTask.Factory queryGeneManiaTaskFactory;
	
	@Inject private CyNetworkManager networkManager;
	@Inject private EnrichmentMapManager emManager;
	@Inject private CommandExecutorTaskFactory commandExecutorTaskFactory;
	@Inject private AvailableCommands availableCommands;
	@Inject private Provider<JFrame> jFrameProvider;
	@Inject private OpenBrowser openBrowser;
	@Inject private DialogTaskManager taskManager;

	
	public void runGeneMANIA(EnrichmentMap map, List<String> genes, Set<String> leadingEdgeGenes) {
		TaskIterator ti = createTaskIterator(map, genes, leadingEdgeGenes);
		taskManager.execute(ti);
	}
	
	
	public TaskIterator createTaskIterator(EnrichmentMap map, List<String> genes, Set<String> leadingEdgeGenes) {
		// Show message to user if genemania not installed
		List<String> commands = availableCommands.getCommands(GENEMANIA_NAMESPACE);
		
		if (commands == null || !commands.contains(GENEMANIA_SEARCH_COMMAND)) {
			if (JOptionPane.showConfirmDialog(
					jFrameProvider.get(),
					"This action requires a version of the GeneMANIA app that is not installed.\n" +
					"Would you like to install or update the GeneMANIA app now?",
					"Cannot Find GeneMANIA App",
					JOptionPane.YES_NO_OPTION
				) == JOptionPane.YES_OPTION) {
				openBrowser.openURL("http://apps.cytoscape.org/apps/genemania");
			}
			
			return null;
		}
		
		QueryGeneManiaTask queryTask = queryGeneManiaTaskFactory.create(genes, leadingEdgeGenes);
		
		// Get list of organisms from GeneMANIA
		TaskIterator ti = commandExecutorTaskFactory.createTaskIterator(
				GENEMANIA_NAMESPACE, GENEMANIA_ORGANISMS_COMMAND, Collections.emptyMap(), TaskUtil.taskFinished(task -> {
					
					if (task.getResultClasses().contains(JSONResult.class)) {
						JSONResult json = ((ObservableTask) task).getResults(JSONResult.class);
						
						if (json != null && json.getJSON() != null) {
							Gson gson = new Gson();
							@SuppressWarnings("serial")
							Type type = new TypeToken<GMOrganismsResult>(){}.getType();
							GMOrganismsResult res = gson.fromJson(json.getJSON(), type);
							
							if (res != null && res.getOrganisms() != null && !res.getOrganisms().isEmpty())
								queryTask.updatetOrganisms(res.getOrganisms());
							else
								throw new RuntimeException("Unable to retrieve available organisms from GeneMANIA.");
						}
					}
				}));
		
		ti.append(queryTask);
		
		ti.append(new AbstractTask() {
			@Override
			public void run(TaskMonitor taskMonitor) throws Exception {
				onGeneManiaQueryFinished(queryTask.getResult(), map);
			}
		});
		
		return ti;
	}
	
	
	private void onGeneManiaQueryFinished(GMSearchResult res, EnrichmentMap map) {
		CyNetwork net = null;
		
		if (res != null && res.getNetwork() != null && res.getGenes() != null && !res.getGenes().isEmpty())
			net = networkManager.getNetwork(res.getNetwork());
		
		if (net == null) {
			invokeOnEDT(() -> {
				JOptionPane.showMessageDialog(
						jFrameProvider.get(),
						"The GeneMANIA search returned no results.",
						"No Results",
						JOptionPane.INFORMATION_MESSAGE
				);
			});
		} else {
			// Update the model
			map.addAssociatedNetworkID(net.getSUID());
			emManager.addAssociatedAppAttributes(net, map, AssociatedApp.GENEMANIA);
//	TODO	
//			// Modify GeneMANIA's style
//			Collection<CyNetworkView> netViewList = netViewManager.getNetworkViews(net);
//			
//			for (CyNetworkView netView : netViewList)
//				updateGeneManiaStyle(netView);
		}
	}
}
