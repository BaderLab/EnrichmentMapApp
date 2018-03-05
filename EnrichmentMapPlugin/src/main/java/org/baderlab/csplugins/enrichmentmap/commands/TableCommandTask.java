package org.baderlab.csplugins.enrichmentmap.commands;

import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.model.TableParameters;
import org.baderlab.csplugins.enrichmentmap.task.CreateEnrichmentMapTaskFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;

public class TableCommandTask extends AbstractTask {

	@ContainsTunables 
	@Inject
	public FilterTunables filterArgs;
	
	@ContainsTunables 
	@Inject
	public TableTunables tableArgs;
	
	
	@Inject private CreateEnrichmentMapTaskFactory.Factory taskFactoryFactory;
	

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		EMCreationParameters creationParams = filterArgs.getCreationParameters();
		TableParameters tableParams = tableArgs.getTableParameters();
		
		
		
	}

}
