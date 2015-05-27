package org.baderlab.csplugins.enrichmentmap;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.baderlab.csplugins.enrichmentmap.actions.BulkEMCreationAction;
import org.baderlab.csplugins.enrichmentmap.actions.EnrichmentMapActionListener;
import org.baderlab.csplugins.enrichmentmap.actions.EnrichmentMapSessionAction;
import org.baderlab.csplugins.enrichmentmap.actions.LoadEnrichmentsPanelAction;
import org.baderlab.csplugins.enrichmentmap.actions.LoadPostAnalysisPanelAction;
import org.baderlab.csplugins.enrichmentmap.actions.ShowAboutPanelAction;
import org.baderlab.csplugins.enrichmentmap.actions.ShowEdgeWidthPanelAction;
import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationManager;
import org.baderlab.csplugins.enrichmentmap.autoannotate.action.AutoAnnotationPanelAction;
import org.baderlab.csplugins.enrichmentmap.autoannotate.action.DisplayOptionsPanelAction;
import org.baderlab.csplugins.enrichmentmap.commands.EnrichmentMapGSEACommandHandlerTaskFactory;
import org.baderlab.csplugins.enrichmentmap.task.BuildEnrichmentMapTuneableTaskFactory;
import org.baderlab.csplugins.enrichmentmap.view.BulkEMCreationPanel;
import org.baderlab.csplugins.enrichmentmap.view.EnrichmentMapInputPanel;
import org.baderlab.csplugins.enrichmentmap.view.HeatMapPanel;
import org.baderlab.csplugins.enrichmentmap.view.ParametersPanel;
import org.baderlab.csplugins.enrichmentmap.view.PostAnalysisInputPanel;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.events.SetSelectedNetworkViewsListener;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.events.ColumnCreatedListener;
import org.cytoscape.model.events.ColumnDeletedListener;
import org.cytoscape.model.events.ColumnNameChangedListener;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.task.edit.MapTableToNetworkTablesTaskFactory;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedListener;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.ServiceProperties;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.framework.BundleContext;



public class CyActivator extends AbstractCyActivator {

	public void start(BundleContext bc) {
		
		//Initialize Enrichment Map parametres
		EnrichmentMapUtils utils = new EnrichmentMapUtils("");
		
		//fetch Cytoscape OSGi services that EM needs
		//main service for dealing with the cytoscape application.  (used when putting in cytopanels...)
		CySwingApplication cySwingApplicationRef = getService(bc,CySwingApplication.class);
		
		//open browser used by about, enrichment map panel,
		OpenBrowser openBrowserRef = getService(bc, OpenBrowser.class);
			
		//get FileUtil
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
		SynchronousTaskManager syncTaskManager = getService(bc, SynchronousTaskManager.class);
		DialogTaskManager dialogTaskManager = getService(bc, DialogTaskManager.class);
		// Used with annotations to make Groups of nodes
		CyGroupFactory groupFactory = getService(bc, CyGroupFactory.class);
		CyGroupManager groupManager = getService(bc, CyGroupManager.class);
		// Used to create/remove the annotations (ellipses and text labels)
		AnnotationManager annotationManager = getService(bc, AnnotationManager.class);
		AnnotationFactory<ShapeAnnotation> shapeFactory = (AnnotationFactory<ShapeAnnotation>) getService(bc, AnnotationFactory.class, "(type=ShapeAnnotation.class)");    	
		AnnotationFactory<TextAnnotation> textFactory = (AnnotationFactory<TextAnnotation>) getService(bc, AnnotationFactory.class, "(type=TextAnnotation.class)");
		CommandExecutorTaskFactory commandExecutor = getService(bc, CommandExecutorTaskFactory.class);
		//get the service registrar so we can register new services in different classes
		CyServiceRegistrar registrar = getService(bc, CyServiceRegistrar.class);
		EquationCompiler compilerServiceRef = getService(bc, EquationCompiler.class);
		
						
		//create the Panels
		BulkEMCreationPanel bulkEmPanel = new BulkEMCreationPanel(cySwingApplicationRef,fileUtil,registrar, sessionManager, streamUtil, cyApplicationManagerRef);		
		EnrichmentMapInputPanel emPanel = new EnrichmentMapInputPanel(cyNetworkFactoryRef, cyApplicationManagerRef, cyNetworkManagerRef, cyNetworkViewManagerRef, tableFactory, tableManager, cyNetworkViewFactoryRef, visualMappingManagerRef, visualStyleFactoryRef,  continuousMappingFunctionFactoryRef,discreteMappingFunctionFactoryRef, passthroughMappingFunctionFactoryRef, dialogTaskManager, sessionManager, cySwingApplicationRef, openBrowserRef,fileUtil,streamUtil,registrar,layoutManager,mapTableToNetworkTable,bulkEmPanel);				
		PostAnalysisInputPanel postEMPanel = new PostAnalysisInputPanel(cyApplicationManagerRef,cySwingApplicationRef, openBrowserRef,fileUtil,sessionManager, streamUtil,registrar, dialogTaskManager,eventHelper, compilerServiceRef, visualMappingManagerRef, visualStyleFactoryRef, continuousMappingFunctionFactoryRef, discreteMappingFunctionFactoryRef, passthroughMappingFunctionFactoryRef);		
		//create two instances of the heatmap panel
		HeatMapPanel heatMapPanel_node = new HeatMapPanel(true, cySwingApplicationRef, fileUtil, cyApplicationManagerRef, openBrowserRef,dialogTaskManager,streamUtil);
		HeatMapPanel heatMapPanel_edge = new HeatMapPanel(false, cySwingApplicationRef, fileUtil, cyApplicationManagerRef, openBrowserRef,dialogTaskManager,streamUtil);
		ParametersPanel paramsPanel = new ParametersPanel(openBrowserRef, cyApplicationManagerRef);

		//Get an instance of EM manager
		EnrichmentMapManager manager = EnrichmentMapManager.getInstance();
		manager.initialize(paramsPanel, heatMapPanel_node, heatMapPanel_edge, registrar);		
		//register network events with manager class
		registerService(bc,manager, NetworkAboutToBeDestroyedListener.class, new Properties());
		registerService(bc,manager, SetCurrentNetworkListener.class, new Properties());
				
		//associate them with the action listener
		EnrichmentMapActionListener EMActionListener = new EnrichmentMapActionListener(heatMapPanel_node,heatMapPanel_edge, cyApplicationManagerRef, cySwingApplicationRef,fileUtil,streamUtil,syncTaskManager);
		registerService(bc,EMActionListener, RowsSetListener.class, new Properties());		

		// Set up autoAnnotationManager, hand it all of the services it needs
		AutoAnnotationManager autoAnnotationManager = AutoAnnotationManager.getInstance();
		autoAnnotationManager.initialize(cySwingApplicationRef, tableManager, commandExecutor, dialogTaskManager, syncTaskManager, annotationManager, layoutManager, shapeFactory, textFactory, groupFactory, groupManager, heatMapPanel_node, EMActionListener, eventHelper,cyApplicationManagerRef);
		//register network/table events to autoAnnotationManager
		registerService(bc, autoAnnotationManager, SetSelectedNetworkViewsListener.class, new Properties());
		registerService(bc, autoAnnotationManager, NetworkViewAboutToBeDestroyedListener.class, new Properties());
		registerService(bc, autoAnnotationManager, ColumnCreatedListener.class, new Properties());
		registerService(bc, autoAnnotationManager, ColumnDeletedListener.class, new Properties());
		registerService(bc, autoAnnotationManager, ColumnNameChangedListener.class, new Properties());

		WidthFunction widthFunction = new WidthFunction(cyNetworkManagerRef, continuousMappingFunctionFactoryRef, manager);
		
		//Create each Action within Enrichment map as a service
		Map<String,String> serviceProperties;
		
		//About Action
		serviceProperties = new HashMap<>();
		serviceProperties.put("inMenuBar", "true");
		serviceProperties.put("preferredMenu", "Apps.EnrichmentMap");
		ShowAboutPanelAction aboutAction = new ShowAboutPanelAction(serviceProperties,cyApplicationManagerRef ,cyNetworkViewManagerRef, cySwingApplicationRef, openBrowserRef);		
		
		//Auto-annotate Panel Action - opens Annotation panel
		serviceProperties = new HashMap<>();
		serviceProperties.put("inMenuBar", "true");
		serviceProperties.put("preferredMenu", "Apps.EnrichmentMap");
 		AutoAnnotationPanelAction autoAnnotationPanelAction = new AutoAnnotationPanelAction(serviceProperties,cyApplicationManagerRef, cyNetworkViewManagerRef, cySwingApplicationRef, annotationManager, registrar);
		
		//Auto-annotate Display Options Panel Action - opens display options panel
 		serviceProperties = new HashMap<>();
 		DisplayOptionsPanelAction displayOptionsPanelAction = new DisplayOptionsPanelAction(serviceProperties,cyApplicationManagerRef, cyNetworkViewManagerRef, cySwingApplicationRef, annotationManager, registrar);
 		autoAnnotationManager.setDisplayOptionsPanelAction(displayOptionsPanelAction);
 		
		//Build Enrichment Map Action - opens EM panel
		serviceProperties = new HashMap<>();
		serviceProperties.put("inMenuBar", "true");
		serviceProperties.put("preferredMenu", "Apps.EnrichmentMap");
		LoadEnrichmentsPanelAction LoadEnrichmentMapInputPanelAction = new LoadEnrichmentsPanelAction(serviceProperties,cyApplicationManagerRef ,cyNetworkViewManagerRef, cySwingApplicationRef, emPanel,registrar);
		
		//Bulk Enrichment Map Action - open bulk em panel
		serviceProperties = new HashMap<>();
		serviceProperties.put("inMenuBar", "true");
		serviceProperties.put("preferredMenu", "Apps.EnrichmentMap");
		BulkEMCreationAction BulkEMInputPanelAction = new BulkEMCreationAction(serviceProperties,cyApplicationManagerRef ,cyNetworkViewManagerRef, cySwingApplicationRef, bulkEmPanel,registrar);
		
		//Post Enrichment Map analysis Action - open post EM panel
		serviceProperties = new HashMap<>();
		serviceProperties.put("inMenuBar", "true");
		serviceProperties.put("preferredMenu", "Apps.EnrichmentMap.Post Analysis");
		LoadPostAnalysisPanelAction loadPostAnalysisAction = new LoadPostAnalysisPanelAction(serviceProperties,cyApplicationManagerRef ,cyNetworkViewManagerRef, cySwingApplicationRef, postEMPanel,registrar);
		
		serviceProperties = new HashMap<>();
		serviceProperties.put("inMenuBar", "true");
		serviceProperties.put("preferredMenu", "Apps.EnrichmentMap.Post Analysis");
		ShowEdgeWidthPanelAction edgeWidthDialogAction = new ShowEdgeWidthPanelAction(serviceProperties, cyApplicationManagerRef ,cyNetworkViewManagerRef, cySwingApplicationRef, manager, compilerServiceRef);
		
		//register the services
		registerService(bc, LoadEnrichmentMapInputPanelAction, CyAction.class, new Properties());
		registerService(bc, autoAnnotationPanelAction, CyAction.class, new Properties());
		registerService(bc, BulkEMInputPanelAction, CyAction.class, new Properties());
		registerService(bc, loadPostAnalysisAction, CyAction.class, new Properties());	
		registerService(bc, edgeWidthDialogAction, CyAction.class, new Properties());
		registerService(bc, aboutAction, CyAction.class,new Properties());
		
		//register the session save and restore
		EnrichmentMapSessionAction sessionAction = new EnrichmentMapSessionAction(cyNetworkManagerRef, sessionManager, cyApplicationManagerRef, streamUtil, autoAnnotationPanelAction);
		registerService(bc,sessionAction,SessionAboutToBeSavedListener.class, new Properties());
		registerService(bc,sessionAction,SessionLoadedListener.class, new Properties());
		
		//generic EM command line option
		Properties properties = new Properties();
    	properties.put(ServiceProperties.COMMAND, "build");
    	properties.put(ServiceProperties.COMMAND_NAMESPACE, "enrichmentmap");
		registerService(bc, new BuildEnrichmentMapTuneableTaskFactory(sessionManager, streamUtil, cyApplicationManagerRef, cyNetworkManagerRef, cyNetworkViewManagerRef, cyNetworkViewFactoryRef, cyNetworkFactoryRef, tableFactory, tableManager, visualMappingManagerRef, visualStyleFactoryRef, continuousMappingFunctionFactoryRef, discreteMappingFunctionFactoryRef, passthroughMappingFunctionFactoryRef, layoutManager, mapTableToNetworkTable, dialogTaskManager), TaskFactory.class, properties);
		
		//gsea specifc commandtool
		properties = new Properties();
    	properties.put(ServiceProperties.COMMAND, "gseabuild");
    	properties.put(ServiceProperties.COMMAND_NAMESPACE, "enrichmentmap");
		registerService(bc, new EnrichmentMapGSEACommandHandlerTaskFactory(sessionManager, streamUtil, cyApplicationManagerRef, cyNetworkManagerRef, cyNetworkViewManagerRef, cyNetworkViewFactoryRef, cyNetworkFactoryRef, tableFactory, tableManager, visualMappingManagerRef, visualStyleFactoryRef, continuousMappingFunctionFactoryRef, discreteMappingFunctionFactoryRef, passthroughMappingFunctionFactoryRef, layoutManager, mapTableToNetworkTable, dialogTaskManager), TaskFactory.class, properties);
		
		// register width function (used in formulas/equations in the table browser)
		compilerServiceRef.getParser().registerFunction(widthFunction);
	}
}
