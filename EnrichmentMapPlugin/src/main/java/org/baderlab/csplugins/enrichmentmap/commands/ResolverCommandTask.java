package org.baderlab.csplugins.enrichmentmap.commands;

import java.io.File;
import java.util.List;

import org.baderlab.csplugins.enrichmentmap.model.DataSetParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.resolver.ResolverTask;
import org.baderlab.csplugins.enrichmentmap.task.CreateEnrichmentMapTaskFactory;
import org.baderlab.csplugins.enrichmentmap.util.NullTaskMonitor;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import com.google.inject.Inject;

public class ResolverCommandTask extends AbstractTask {

	@Tunable(required=true)
	public File rootFolder;
	
	@Tunable
	public File commonGMTFile;
	
	@Tunable
	public File commonExpressionFile;
	
	@ContainsTunables
	@Inject
	public FilterTunables filterArgs;
	
	
	@Inject private SynchronousTaskManager<?> taskManager;
	@Inject private CreateEnrichmentMapTaskFactory.Factory taskFactoryFactory;
	
	
	
	@Override
	public void run(TaskMonitor tm) throws Exception {
		if(tm == null)
			tm = new NullTaskMonitor();
		tm.setStatusMessage("Running EnrichmentMap Data Set Resolver Task");
		
		if(rootFolder == null || !rootFolder.exists()) {
			throw new IllegalArgumentException("rootFolder is invalid: " + rootFolder);
		}
		
		// Scan root folder (note: throws exception if no data sets were found)
		ResolverTask resolverTask = new ResolverTask(rootFolder);
		taskManager.execute(new TaskIterator(resolverTask)); // blocks
		List<DataSetParameters> dataSets = resolverTask.getDataSetResults();
		
		tm.setStatusMessage("resolved " + dataSets.size() + " data sets");
		for(DataSetParameters params : dataSets) {
			tm.setStatusMessage(params.toString());
		}
		
		// Common gmt and expression files
		// Overwrite all the expression files if the common file has been provided
		if(commonExpressionFile != null) {
			if(!commonExpressionFile.canRead()) {
				throw new IllegalArgumentException("Cannot read commonExpressionFile: " + commonExpressionFile);
			}
			for(DataSetParameters dsp : dataSets) {
				dsp.getFiles().ifPresent(d -> d.setExpressionFileName(commonExpressionFile.getAbsolutePath()));
			}
		}
		
		// Overwrite all the gmt files if a common file has been provided
		if(commonGMTFile != null) {
			if(!commonGMTFile.canRead()) {
				throw new IllegalArgumentException("Cannot read commonGMTFile: " + commonGMTFile);
			}
			for(DataSetParameters dsp : dataSets) {
				dsp.getFiles().ifPresent(d -> d.setGMTFileName(commonGMTFile.getAbsolutePath()));
			}
		}
		
		tm.setStatusMessage(filterArgs.toString());
		
		EMCreationParameters params = filterArgs.getCreationParameters();

		if(filterArgs.networkName != null && !filterArgs.networkName.trim().isEmpty()) {
			params.setNetworkName(filterArgs.networkName);
		}
		
		CreateEnrichmentMapTaskFactory taskFactory = taskFactoryFactory.create(params, dataSets);
		TaskIterator tasks = taskFactory.createTaskIterator();
		taskManager.execute(tasks);
		
		tm.setStatusMessage("Done.");
	}
	
	
}
