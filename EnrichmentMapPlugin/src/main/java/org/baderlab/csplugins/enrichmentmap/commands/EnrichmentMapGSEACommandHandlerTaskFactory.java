package org.baderlab.csplugins.enrichmentmap.commands;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.task.edit.MapTableToNetworkTablesTaskFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class EnrichmentMapGSEACommandHandlerTaskFactory implements TaskFactory{
	private CySessionManager sessionManager;
	private StreamUtil streamUtil;
	
	private CyApplicationManager applicationManager;
	private CySwingApplication swingApplication;
	private CyNetworkManager networkManager;
	private CyNetworkViewManager networkViewManager;
	private CyNetworkViewFactory networkViewFactory;
	private CyNetworkFactory networkFactory;
	private CyTableFactory tableFactory;
	private CyTableManager tableManager;

	private VisualMappingManager visualMappingManager;
	private VisualStyleFactory visualStyleFactory;

	//we will need all three mappers
	private VisualMappingFunctionFactory vmfFactoryContinuous;
	private VisualMappingFunctionFactory vmfFactoryDiscrete;
	private VisualMappingFunctionFactory vmfFactoryPassthrough;

	private CyLayoutAlgorithmManager layoutManager;
	private MapTableToNetworkTablesTaskFactory mapTableToNetworkTable;


	public EnrichmentMapGSEACommandHandlerTaskFactory(CySessionManager sessionManager,
			StreamUtil streamUtil, CyApplicationManager applicationManager,
			CySwingApplication swingApplication,
			CyNetworkManager networkManager,
			CyNetworkViewManager networkViewManager,
			CyNetworkViewFactory networkViewFactory,
			CyNetworkFactory networkFactory, CyTableFactory tableFactory,
			CyTableManager tableManager,
			VisualMappingManager visualMappingManager,
			VisualStyleFactory visualStyleFactory,
			VisualMappingFunctionFactory vmfFactoryContinuous,
			VisualMappingFunctionFactory vmfFactoryDiscrete,
			VisualMappingFunctionFactory vmfFactoryPassthrough,
			CyLayoutAlgorithmManager layoutManager,
			MapTableToNetworkTablesTaskFactory mapTableToNetworkTable) {
		super();
		this.sessionManager = sessionManager;
		this.streamUtil = streamUtil;
		this.applicationManager = applicationManager;
		this.swingApplication = swingApplication;
		this.networkManager = networkManager;
		this.networkViewManager = networkViewManager;
		this.networkViewFactory = networkViewFactory;
		this.networkFactory = networkFactory;
		this.tableFactory = tableFactory;
		this.tableManager = tableManager;
		this.visualMappingManager = visualMappingManager;
		this.visualStyleFactory = visualStyleFactory;
		this.vmfFactoryContinuous = vmfFactoryContinuous;
		this.vmfFactoryDiscrete = vmfFactoryDiscrete;
		this.vmfFactoryPassthrough = vmfFactoryPassthrough;
		this.layoutManager = layoutManager;
		this.mapTableToNetworkTable = mapTableToNetworkTable;
	}

	public boolean isReady() {
		return true;
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(new EnrichmentMapGSEACommandHandlerTask(sessionManager, streamUtil, applicationManager, swingApplication, networkManager, networkViewManager, networkViewFactory, networkFactory, tableFactory, tableManager, visualMappingManager, visualStyleFactory, vmfFactoryContinuous, vmfFactoryDiscrete, vmfFactoryPassthrough, layoutManager, mapTableToNetworkTable));
	}

}
