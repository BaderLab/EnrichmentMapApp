package org.baderlab.csplugins.enrichmentmap;

import static org.cytoscape.work.ServiceProperties.COMMAND;
import static org.cytoscape.work.ServiceProperties.COMMAND_NAMESPACE;
import static org.cytoscape.work.ServiceProperties.INSERT_SEPARATOR_BEFORE;
import static org.cytoscape.work.ServiceProperties.IN_MENU_BAR;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.baderlab.csplugins.enrichmentmap.actions.EnrichmentMapActionListener;
import org.baderlab.csplugins.enrichmentmap.actions.EnrichmentMapSessionAction;
import org.baderlab.csplugins.enrichmentmap.actions.HelpAction;
import org.baderlab.csplugins.enrichmentmap.actions.LoadEnrichmentsPanelAction;
import org.baderlab.csplugins.enrichmentmap.actions.LoadPostAnalysisPanelAction;
import org.baderlab.csplugins.enrichmentmap.actions.ShowAboutPanelAction;
import org.baderlab.csplugins.enrichmentmap.actions.ShowEdgeWidthDialogAction;
import org.baderlab.csplugins.enrichmentmap.commands.EnrichmentMapGSEACommandHandlerTaskFactory;
import org.baderlab.csplugins.enrichmentmap.task.BuildEnrichmentMapTuneableTaskFactory;
import org.baderlab.csplugins.enrichmentmap.task.CreatePublicationVisualStyleTaskFactory;
import org.baderlab.csplugins.enrichmentmap.task.EdgeWidthTableColumnTaskFactory;
import org.baderlab.csplugins.enrichmentmap.view.EnrichmentMapInputPanel;
import org.baderlab.csplugins.enrichmentmap.view.HeatMapPanel;
import org.baderlab.csplugins.enrichmentmap.view.ParametersPanel;
import org.baderlab.csplugins.enrichmentmap.view.PostAnalysisPanel;
import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
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
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;



public class CyActivator extends AbstractCyActivator {

	private static final String APP_MENU = "Apps.EnrichmentMap";

	public void start(BundleContext bc) {
		
		//fetch Cytoscape OSGi services that EM needs
		CySwingApplication cySwingApplicationRef = getService(bc,CySwingApplication.class);
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
		MapTableToNetworkTablesTaskFactory mapTableToNetworkTable = getService(bc,  MapTableToNetworkTablesTaskFactory.class);
		CyEventHelper eventHelper = getService(bc,CyEventHelper.class);
		@SuppressWarnings("rawtypes")
		SynchronousTaskManager syncTaskManager = getService(bc, SynchronousTaskManager.class);
		DialogTaskManager dialogTaskManager = getService(bc, DialogTaskManager.class);
		CyApplicationConfiguration applicationConfiguration = getService(bc, CyApplicationConfiguration.class);
		CyServiceRegistrar registrar = getService(bc, CyServiceRegistrar.class);
		
						
		//create the Panels
//		BulkEMCreationPanel bulkEmPanel = new BulkEMCreationPanel(cySwingApplicationRef,fileUtil,registrar, sessionManager, streamUtil, cyApplicationManagerRef);		
		EnrichmentMapInputPanel emPanel = new EnrichmentMapInputPanel(cyNetworkFactoryRef, cyApplicationManagerRef, cyNetworkManagerRef, cyNetworkViewManagerRef, tableFactory, tableManager, cyNetworkViewFactoryRef, visualMappingManagerRef, visualStyleFactoryRef,  continuousMappingFunctionFactoryRef,discreteMappingFunctionFactoryRef, passthroughMappingFunctionFactoryRef, dialogTaskManager, sessionManager, cySwingApplicationRef,fileUtil,streamUtil,registrar,layoutManager,mapTableToNetworkTable);				
		PostAnalysisPanel postEMPanel = new PostAnalysisPanel(cyApplicationManagerRef,cySwingApplicationRef,fileUtil,sessionManager, streamUtil,registrar, dialogTaskManager, syncTaskManager, eventHelper, visualMappingManagerRef, visualStyleFactoryRef, continuousMappingFunctionFactoryRef, discreteMappingFunctionFactoryRef, passthroughMappingFunctionFactoryRef);		
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


		//Create each Action within Enrichment map as a service
		Map<String,String> serviceProperties;
		//Online Help Action
		serviceProperties = new HashMap<>();
		serviceProperties.put(IN_MENU_BAR, "true");
		serviceProperties.put(PREFERRED_MENU, APP_MENU);
		serviceProperties.put(INSERT_SEPARATOR_BEFORE, "true");
		HelpAction helpAction = new HelpAction(serviceProperties, cyApplicationManagerRef, cyNetworkViewManagerRef, registrar);
		helpAction.setMenuGravity(2.1f);

		//About Action
		serviceProperties = new HashMap<>();
		serviceProperties.put(IN_MENU_BAR, "true");
		serviceProperties.put(PREFERRED_MENU, APP_MENU);
		ShowAboutPanelAction aboutAction = new ShowAboutPanelAction(serviceProperties,cyApplicationManagerRef ,cyNetworkViewManagerRef, cySwingApplicationRef, openBrowserRef);		
		aboutAction.setMenuGravity(2.2f);
		
		//Build Enrichment Map Action - opens EM panel
		serviceProperties = new HashMap<>();
		serviceProperties.put(IN_MENU_BAR, "true");
		serviceProperties.put(PREFERRED_MENU, APP_MENU);
		LoadEnrichmentsPanelAction loadEnrichmentMapInputPanelAction = new LoadEnrichmentsPanelAction(serviceProperties,cyApplicationManagerRef ,cyNetworkViewManagerRef, cySwingApplicationRef, emPanel,registrar);
		loadEnrichmentMapInputPanelAction.setMenuGravity(1.1f);
		
		//Bulk Enrichment Map Action - open bulk em panel
//		serviceProperties = new HashMap<>();
//		serviceProperties.put(IN_MENU_BAR, "true");
//		serviceProperties.put(PREFERRED_MENU, APP_MENU);
//		BulkEMCreationAction BulkEMInputPanelAction = new BulkEMCreationAction(serviceProperties,cyApplicationManagerRef ,cyNetworkViewManagerRef, cySwingApplicationRef, bulkEmPanel,registrar);
		
		//Post Enrichment Map analysis Action - open post EM panel
		serviceProperties = new HashMap<>();
		serviceProperties.put(IN_MENU_BAR, "true");
		serviceProperties.put(PREFERRED_MENU, APP_MENU);
		LoadPostAnalysisPanelAction loadPostAnalysisAction = new LoadPostAnalysisPanelAction(serviceProperties,cyApplicationManagerRef ,cyNetworkViewManagerRef, cySwingApplicationRef, postEMPanel,registrar);
		loadPostAnalysisAction.setMenuGravity(1.2f);
		
		serviceProperties = new HashMap<>();
		serviceProperties.put(IN_MENU_BAR, "true");
		serviceProperties.put(PREFERRED_MENU, APP_MENU);
		ShowEdgeWidthDialogAction edgeWidthPanelAction = new ShowEdgeWidthDialogAction(serviceProperties, cyApplicationManagerRef, continuousMappingFunctionFactoryRef, dialogTaskManager, cyNetworkViewManagerRef, cySwingApplicationRef);
		edgeWidthPanelAction.setMenuGravity(1.3f);
		
		//register the services
		registerService(bc, loadEnrichmentMapInputPanelAction, CyAction.class, new Properties());
		registerService(bc, loadPostAnalysisAction, CyAction.class, new Properties());
		registerService(bc, edgeWidthPanelAction, CyAction.class, new Properties());
		registerService(bc, helpAction, CyAction.class, new Properties());
		registerService(bc, aboutAction, CyAction.class, new Properties());
		
		//register the session save and restore
		EnrichmentMapSessionAction sessionAction = new EnrichmentMapSessionAction(cyNetworkManagerRef, sessionManager, cyApplicationManagerRef, streamUtil);
		registerService(bc,sessionAction,SessionAboutToBeSavedListener.class, new Properties());
		registerService(bc,sessionAction,SessionLoadedListener.class, new Properties());
		
		//generic EM command line option
		Properties properties = new Properties();
    	properties.put(COMMAND, "build");
    	properties.put(COMMAND_NAMESPACE, "enrichmentmap");
		registerService(bc, new BuildEnrichmentMapTuneableTaskFactory(sessionManager, streamUtil, cyApplicationManagerRef, cySwingApplicationRef, cyNetworkManagerRef, cyNetworkViewManagerRef, cyNetworkViewFactoryRef, cyNetworkFactoryRef, tableFactory, tableManager, visualMappingManagerRef, visualStyleFactoryRef, continuousMappingFunctionFactoryRef, discreteMappingFunctionFactoryRef, passthroughMappingFunctionFactoryRef, layoutManager, mapTableToNetworkTable, dialogTaskManager), TaskFactory.class, properties);
		
		//gsea specifc commandtool
		properties = new Properties();
    	properties.put(COMMAND, "gseabuild");
    	properties.put(COMMAND_NAMESPACE, "enrichmentmap");
		registerService(bc, new EnrichmentMapGSEACommandHandlerTaskFactory(sessionManager, streamUtil, cyApplicationManagerRef, cySwingApplicationRef, cyNetworkManagerRef, cyNetworkViewManagerRef, cyNetworkViewFactoryRef, cyNetworkFactoryRef, tableFactory, tableManager, visualMappingManagerRef, visualStyleFactoryRef, continuousMappingFunctionFactoryRef, discreteMappingFunctionFactoryRef, passthroughMappingFunctionFactoryRef, layoutManager, mapTableToNetworkTable, dialogTaskManager), TaskFactory.class, properties);
		
		//edge table context menu
		EdgeWidthTableColumnTaskFactory tableColumnTaskFactory = new EdgeWidthTableColumnTaskFactory(edgeWidthPanelAction);
		Properties tableColumnTaskFactoryProps = new Properties();
		tableColumnTaskFactoryProps.setProperty(TITLE, "Post Analysis Edge Width...");
		tableColumnTaskFactoryProps.setProperty("tableTypes", "edge");
		registerService(bc, tableColumnTaskFactory, TableColumnTaskFactory.class, tableColumnTaskFactoryProps);
		
		Em21Handler.removeVersion21(bc, applicationConfiguration);
	}
	
}
