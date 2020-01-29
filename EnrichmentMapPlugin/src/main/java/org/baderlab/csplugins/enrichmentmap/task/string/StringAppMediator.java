package org.baderlab.csplugins.enrichmentmap.task.string;

import static org.baderlab.csplugins.enrichmentmap.task.string.QueryStringTask.STRING_NAMESPACE;
import static org.baderlab.csplugins.enrichmentmap.task.string.QueryStringTask.STRING_SPECIES_COMMAND;
import static org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil.invokeOnEDT;

import java.lang.reflect.Type;
import java.util.ArrayList;
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
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.json.JSONResult;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class StringAppMediator {

	@Inject private QueryStringTask.Factory queryStringTaskFactory;
	
	@Inject private CyNetworkManager networkManager;
	@Inject private EnrichmentMapManager emManager;
	@Inject private CommandExecutorTaskFactory commandExecutorTaskFactory;
	@Inject private AvailableCommands availableCommands;
	@Inject private Provider<JFrame> jFrameProvider;
	@Inject private OpenBrowser openBrowser;
	@Inject private DialogTaskManager taskManager;

	
	public void runString(EnrichmentMap map, List<String> genes, Set<String> leadingEdgeGenes) {
		TaskIterator ti = createTaskIterator(map, genes, leadingEdgeGenes);
		taskManager.execute(ti);
	}
	
	
	public TaskIterator createTaskIterator(EnrichmentMap map, List<String> genes, Set<String> leadingEdgeGenes) {
		// Show message to user if STRING App not installed
		List<String> commands = availableCommands.getCommands(STRING_NAMESPACE);
		
		if (commands == null || !commands.contains(STRING_SPECIES_COMMAND)) {
			if (JOptionPane.showConfirmDialog(
					jFrameProvider.get(),
					"This action requires a version of the STRING app that is not installed.\n" +
					"Would you like to install or update the STRING app now?",
					"Cannot Find STRING App",
					JOptionPane.YES_NO_OPTION
				) == JOptionPane.YES_OPTION) {
				openBrowser.openURL("http://apps.cytoscape.org/apps/stringapp");
			}
			
			return null;
		}
		
		QueryStringTask queryTask = queryStringTaskFactory.create(genes, leadingEdgeGenes);
		
		// Get list of organisms from STRING App
		TaskIterator ti = commandExecutorTaskFactory.createTaskIterator(
				STRING_NAMESPACE, STRING_SPECIES_COMMAND, Collections.emptyMap(), TaskUtil.taskFinished(task -> {
							if (task.getResultClasses().contains(JSONResult.class)) {
								JSONResult json = task.getResults(JSONResult.class);
								
								if (json != null && json.getJSON() != null) {
									Gson gson = new Gson();
									@SuppressWarnings("serial")
									Type type = new TypeToken<ArrayList<STRSpecies>>(){}.getType();
									List<STRSpecies> organisms = gson.fromJson(json.getJSON(), type);
									
									if (organisms != null && !organisms.isEmpty())
										queryTask.updatetOrganisms(organisms);
									else
										throw new RuntimeException("Unable to retrieve available species from STRING App.");
								}
							}
				}));
		
		ti.append(queryTask);
		
		ti.append(new AbstractTask() {
			@Override
			public void run(TaskMonitor taskMonitor) throws Exception {
				onStringQueryFinished(queryTask.getResult(), map);
			}
		});
		
		return ti;
	}
	
	
	private void onStringQueryFinished(Long netId, EnrichmentMap map) {
		CyNetwork net = netId != null ? networkManager.getNetwork(netId) : null;
		
		if (net == null) {
			invokeOnEDT(() -> {
				JOptionPane.showMessageDialog(
						jFrameProvider.get(),
						"The STRING protein query returned no results.",
						"No Results",
						JOptionPane.INFORMATION_MESSAGE
				);
			});
		} else {
			// Update the model
			map.addAssociatedNetworkID(net.getSUID());
			emManager.addAssociatedAppAttributes(net, map, AssociatedApp.STRING);
		}
	}
	
}
