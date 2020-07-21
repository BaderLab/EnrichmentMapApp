package org.baderlab.csplugins.enrichmentmap.commands;

import java.util.Collections;
import java.util.List;

import org.baderlab.csplugins.enrichmentmap.commands.tunables.FilterTunables;
import org.baderlab.csplugins.enrichmentmap.commands.tunables.TableTunables;
import org.baderlab.csplugins.enrichmentmap.model.DataSetParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.model.TableExpressionParameters;
import org.baderlab.csplugins.enrichmentmap.model.TableParameters;
import org.baderlab.csplugins.enrichmentmap.task.CreateEnrichmentMapTaskFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import com.google.inject.Inject;

public class TableCommandTask extends AbstractTask {
	
	@ContainsTunables 
	@Inject
	public FilterTunables filterArgs;
	
	@ContainsTunables 
	@Inject
	public TableTunables tableArgs;
	
	@Tunable(description="Name of the data set (optional).")
	public String dataSetName = null;
	
	@Inject private CreateEnrichmentMapTaskFactory.Factory taskFactoryFactory;
	

	@Override
	public void run(TaskMonitor taskMonitor) {
		EMCreationParameters creationParams = filterArgs.getCreationParameters();
		TableParameters tableParams = tableArgs.getTableParameters();
		TableExpressionParameters tableExpressionParams = tableArgs.getTableExpressionParameters();
		
		if(filterArgs.networkName != null && !filterArgs.networkName.trim().isEmpty())
			creationParams.setNetworkName(filterArgs.networkName);
		
		String dataSetName = this.dataSetName == null ? "Data Set 1" : this.dataSetName;
		DataSetParameters dsParams = new DataSetParameters(dataSetName, tableParams, tableExpressionParams);
		List<DataSetParameters> dataSets = Collections.singletonList(dsParams);
		
		CreateEnrichmentMapTaskFactory taskFactory = taskFactoryFactory.create(creationParams, dataSets);
		insertTasksAfterCurrentTask(taskFactory.createTaskIterator());
	}

}
