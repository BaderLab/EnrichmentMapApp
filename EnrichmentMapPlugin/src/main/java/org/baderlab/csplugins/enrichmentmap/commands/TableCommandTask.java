package org.baderlab.csplugins.enrichmentmap.commands;

import java.util.Collections;
import java.util.List;

import org.baderlab.csplugins.enrichmentmap.model.DataSetParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet.Method;
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
	public void run(TaskMonitor taskMonitor) {
		EMCreationParameters creationParams = filterArgs.getCreationParameters();
		TableParameters tableParams = tableArgs.getTableParameters();
		
		String dataSetName = filterArgs.networkName == null ? "Data Set 1" : filterArgs.networkName;
		DataSetParameters dsParams = new DataSetParameters(dataSetName, Method.Generic, tableParams);
		List<DataSetParameters> dataSets = Collections.singletonList(dsParams);
		
		CreateEnrichmentMapTaskFactory taskFactory = taskFactoryFactory.create(creationParams, dataSets);
		insertTasksAfterCurrentTask(taskFactory.createTaskIterator());
	}

}
