package org.baderlab.csplugins.enrichmentmap.commands;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;

/**
 * There is already a command in Cytoscape to export a network image. This command
 * is different in that it always exports to the user's home directory, and it
 * never prompts for overwrite permission.
 */
public class ExportNetworkImageCommandTask extends AbstractTask {

	@Inject private CommandExecutorTaskFactory commandTaskFactory;
	
	@Override
	public void run(TaskMonitor taskMonitor) {
		String fileName = "em_network_" + UUID.randomUUID() + ".png";
		String homeDir = System.getProperty("user.home");
		String filePath = Paths.get(homeDir, fileName).toString();
		
		Map<String,Object> args = new HashMap<>();
		args.put("options", "PNG (*.png)");
		args.put("outputFile", filePath);
		args.put("view", "CURRENT"); // required
		
		TaskIterator commandTasks = commandTaskFactory.createTaskIterator("view", "export", args, null);
		insertTasksAfterCurrentTask(commandTasks);
	}
	
}
