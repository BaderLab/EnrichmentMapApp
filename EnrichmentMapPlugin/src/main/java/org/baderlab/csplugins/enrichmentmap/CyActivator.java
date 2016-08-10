package org.baderlab.csplugins.enrichmentmap;

import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.Map;
import java.util.Properties;

import org.baderlab.csplugins.enrichmentmap.actions.BulkEMCreationAction;
import org.baderlab.csplugins.enrichmentmap.actions.EnrichmentMapActionListener;
import org.baderlab.csplugins.enrichmentmap.actions.EnrichmentMapSessionAction;
import org.baderlab.csplugins.enrichmentmap.actions.LoadEnrichmentsPanelAction;
import org.baderlab.csplugins.enrichmentmap.actions.LoadPostAnalysisPanelAction;
import org.baderlab.csplugins.enrichmentmap.actions.ShowAboutPanelAction;
import org.baderlab.csplugins.enrichmentmap.actions.ShowEdgeWidthDialogAction;
import org.baderlab.csplugins.enrichmentmap.commands.BuildEnrichmentMapTuneableTaskFactory;
import org.baderlab.csplugins.enrichmentmap.commands.EnrichmentMapGSEACommandHandlerTaskFactory;
import org.baderlab.csplugins.enrichmentmap.task.CreatePublicationVisualStyleTaskFactory;
import org.baderlab.csplugins.enrichmentmap.task.EdgeWidthTableColumnTaskFactory;
import org.baderlab.csplugins.enrichmentmap.view.BulkEMCreationPanel;
import org.baderlab.csplugins.enrichmentmap.view.EnrichmentMapInputPanel;
import org.baderlab.csplugins.enrichmentmap.view.HeatMapPanel;
import org.baderlab.csplugins.enrichmentmap.view.ParametersPanel;
import org.baderlab.csplugins.enrichmentmap.view.PostAnalysisPanel;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.task.TableColumnTaskFactory;
import org.cytoscape.task.edit.MapTableToNetworkTablesTaskFactory;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.ServiceProperties;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.google.common.collect.ImmutableMap;



public class CyActivator extends AbstractCyActivator {

	public void start(BundleContext bc) {
		ServiceReference ref = bc.getServiceReference(CySwingApplication.class.getName());
		if(ref == null) {
			// Cytoscape is running headless or integration tests are running, don't register UI components
			return;
		}
		
		CySwingApplication cySwingApplicationRef = getService(bc, CySwingApplication.class);
		OpenBrowser openBrowserRef = getService(bc, OpenBrowser.class);
		FileUtil fileUtil = getService(bc,FileUtil.class);
		StreamUtil streamUtil = getService(bc, StreamUtil.class);
		CyApplicationManager cyApplicationManagerRef = getService(bc,CyApplicationManager.class);
		CyNetworkManager cyNetworkManagerRef = getService(bc,CyNetworkManager.class);
		CyNetworkFactory cyNetworkFactoryRef = getService(bc, CyNetworkFactory.class);
		CyNetworkViewManager cyNetworkViewManagerRef = getService(bc,CyNetworkViewManager.class);
		CyNetworkViewFactory cyNetworkViewFactoryRef = getService(bc,CyNetworkViewFactory.class);
		VisualMappingManager visualMappingManagerRef = getService(bc,VisualMappingManager.class);
		VisualStyleFactory visualStyleFactoryRef = getService(bc,VisualStyleFactory.class);
		VisualMappingFunctionFactory discreteMappingFunctionFactoryRef = getService(bc, VisualMappingFunctionFactory.class,"(mapping.type=discrete)");
		VisualMappingFunctionFactory passthroughMappingFunctionFactoryRef = getService(bc, VisualMappingFunctionFactory.class,"(mapping.type=passthrough)");
		VisualMappingFunctionFactory continuousMappingFunctionFactoryRef = getService(bc, VisualMappingFunctionFactory.class,"(mapping.type=continuous)");
		CySessionManager sessionManager = getService(bc, CySessionManager.class);
		CyTableFactory tableFactory = getService(bc, CyTableFactory.class);
		CyTableManager tableManager	= getService(bc, CyTableManager.class);
		CyLayoutAlgorithmManager layoutManager = getService(bc, CyLayoutAlgorithmManager.class);
		MapTableToNetworkTablesTaskFactory mapTableToNetworkTable = getService(bc, MapTableToNetworkTablesTaskFactory.class);
		CyEventHelper eventHelper = getService(bc,CyEventHelper.class);
		@SuppressWarnings("rawtypes")
		SynchronousTaskManager syncTaskManager = getService(bc, SynchronousTaskManager.class);
		DialogTaskManager dialogTaskManager = getService(bc, DialogTaskManager.class);
		//get the service registrar so we can register new services in different classes
		CyServiceRegistrar registrar = getService(bc, CyServiceRegistrar.class);


		//create the Panels
		BulkEMCreationPanel bulkEmPanel = new BulkEMCreationPanel(cySwingApplicationRef,fileUtil,registrar, sessionManager, streamUtil, cyApplicationManagerRef);		
		EnrichmentMapInputPanel emPanel = new EnrichmentMapInputPanel(cyNetworkFactoryRef, cyApplicationManagerRef, cyNetworkManagerRef, cyNetworkViewManagerRef, tableFactory, tableManager, cyNetworkViewFactoryRef, visualMappingManagerRef, visualStyleFactoryRef,  continuousMappingFunctionFactoryRef,discreteMappingFunctionFactoryRef, passthroughMappingFunctionFactoryRef, dialogTaskManager, sessionManager, cySwingApplicationRef, openBrowserRef,fileUtil,streamUtil,registrar,layoutManager,mapTableToNetworkTable,bulkEmPanel);				
		PostAnalysisPanel postEMPanel = new PostAnalysisPanel(cyApplicationManagerRef,cySwingApplicationRef, openBrowserRef,fileUtil,sessionManager, streamUtil,registrar, dialogTaskManager, syncTaskManager, eventHelper, visualMappingManagerRef, visualStyleFactoryRef, continuousMappingFunctionFactoryRef, discreteMappingFunctionFactoryRef, passthroughMappingFunctionFactoryRef);		
		//create two instances of the heatmap panel
		HeatMapPanel heatMapPanel_node = new HeatMapPanel(true, cySwingApplicationRef, fileUtil, cyApplicationManagerRef, openBrowserRef,dialogTaskManager,streamUtil);
		HeatMapPanel heatMapPanel_edge = new HeatMapPanel(false, cySwingApplicationRef, fileUtil, cyApplicationManagerRef, openBrowserRef,dialogTaskManager,streamUtil);
		CreatePublicationVisualStyleTaskFactory taskRunner = new CreatePublicationVisualStyleTaskFactory(cyApplicationManagerRef, visualMappingManagerRef, visualStyleFactoryRef, eventHelper);
		ParametersPanel paramsPanel = new ParametersPanel(openBrowserRef, cyApplicationManagerRef, dialogTaskManager, taskRunner);

		//Get an instance of EM manager
		EnrichmentMapManager manager = EnrichmentMapManager.getInstance();
		manager.initialize(paramsPanel, heatMapPanel_node, heatMapPanel_edge, registrar);		
		//register network events with manager class
		registerService(bc,manager, NetworkAboutToBeDestroyedListener.class, new Properties());
		registerService(bc,manager, SetCurrentNetworkListener.class, new Properties());
		registerService(bc,manager, SetCurrentNetworkViewListener.class, new Properties());

		//associate them with the action listener
		EnrichmentMapActionListener EMActionListener = new EnrichmentMapActionListener(heatMapPanel_node,heatMapPanel_edge, cyApplicationManagerRef, cySwingApplicationRef,fileUtil,streamUtil,syncTaskManager);
		registerService(bc,EMActionListener, RowsSetListener.class, new Properties());		


		ShowAboutPanelAction aboutAction = new ShowAboutPanelAction(actionProps(),cyApplicationManagerRef ,cyNetworkViewManagerRef, cySwingApplicationRef, openBrowserRef);		
		LoadEnrichmentsPanelAction loadEnrichmentMapInputPanelAction = new LoadEnrichmentsPanelAction(actionProps(),cyApplicationManagerRef ,cyNetworkViewManagerRef, cySwingApplicationRef, emPanel,registrar);
		BulkEMCreationAction bulkEMInputPanelAction = new BulkEMCreationAction(actionProps(),cyApplicationManagerRef ,cyNetworkViewManagerRef, cySwingApplicationRef, bulkEmPanel,registrar);
		LoadPostAnalysisPanelAction loadPostAnalysisAction = new LoadPostAnalysisPanelAction(actionProps(),cyApplicationManagerRef ,cyNetworkViewManagerRef, cySwingApplicationRef, postEMPanel,registrar);
		ShowEdgeWidthDialogAction edgeWidthPanelAction = new ShowEdgeWidthDialogAction(actionProps(), cyApplicationManagerRef, continuousMappingFunctionFactoryRef, dialogTaskManager, cyNetworkViewManagerRef, cySwingApplicationRef);

		//register the services
		registerService(bc, loadEnrichmentMapInputPanelAction, CyAction.class, new Properties());
		registerService(bc, bulkEMInputPanelAction, CyAction.class, new Properties());
		registerService(bc, loadPostAnalysisAction, CyAction.class, new Properties());	
		registerService(bc, edgeWidthPanelAction, CyAction.class, new Properties());
		registerService(bc, aboutAction, CyAction.class, new Properties());

		//register the session save and restore
		EnrichmentMapSessionAction sessionAction = new EnrichmentMapSessionAction(cyNetworkManagerRef, sessionManager, cyApplicationManagerRef, streamUtil);
		registerService(bc,sessionAction,SessionAboutToBeSavedListener.class, new Properties());
		registerService(bc,sessionAction,SessionLoadedListener.class, new Properties());

		//generic EM command line option
		Properties properties = new Properties();
		properties.put(ServiceProperties.COMMAND, "build");
		properties.put(ServiceProperties.COMMAND_NAMESPACE, "enrichmentmap");
		registerService(bc, new BuildEnrichmentMapTuneableTaskFactory(sessionManager, streamUtil, cyApplicationManagerRef, cySwingApplicationRef, cyNetworkManagerRef, cyNetworkViewManagerRef, cyNetworkViewFactoryRef, cyNetworkFactoryRef, tableFactory, tableManager, visualMappingManagerRef, visualStyleFactoryRef, continuousMappingFunctionFactoryRef, discreteMappingFunctionFactoryRef, passthroughMappingFunctionFactoryRef, layoutManager, mapTableToNetworkTable), TaskFactory.class, properties);

		//gsea specifc commandtool
		properties = new Properties();
		properties.put(ServiceProperties.COMMAND, "gseabuild");
		properties.put(ServiceProperties.COMMAND_NAMESPACE, "enrichmentmap");
		registerService(bc, new EnrichmentMapGSEACommandHandlerTaskFactory(sessionManager, streamUtil, cyApplicationManagerRef, cySwingApplicationRef, cyNetworkManagerRef, cyNetworkViewManagerRef, cyNetworkViewFactoryRef, cyNetworkFactoryRef, tableFactory, tableManager, visualMappingManagerRef, visualStyleFactoryRef, continuousMappingFunctionFactoryRef, discreteMappingFunctionFactoryRef, passthroughMappingFunctionFactoryRef, layoutManager, mapTableToNetworkTable), TaskFactory.class, properties);

		//edge table context menu
		EdgeWidthTableColumnTaskFactory tableColumnTaskFactory = new EdgeWidthTableColumnTaskFactory(edgeWidthPanelAction);
		Properties tableColumnTaskFactoryProps = new Properties();
		tableColumnTaskFactoryProps.setProperty(TITLE, "Post Analysis Edge Width...");
		tableColumnTaskFactoryProps.setProperty("tableTypes", "edge");
		registerService(bc, tableColumnTaskFactory, TableColumnTaskFactory.class, tableColumnTaskFactoryProps);

	}
	
	
	public static Map<String,String> actionProps() {
		return ImmutableMap.of(
			"inMenuBar", "true",
			"preferredMenu", "Apps.EnrichmentMap"
		);
	}
}
