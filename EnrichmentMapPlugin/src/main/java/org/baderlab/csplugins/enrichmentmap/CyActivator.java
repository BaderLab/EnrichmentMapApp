package org.baderlab.csplugins.enrichmentmap;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.baderlab.csplugins.enrichmentmap.actions.BulkEMCreationAction;
import org.baderlab.csplugins.enrichmentmap.actions.LoadEnrichmentsPanelAction;
import org.baderlab.csplugins.enrichmentmap.actions.LoadPostAnalysisPanelAction;
import org.baderlab.csplugins.enrichmentmap.actions.ShowAboutPanelAction;
import org.baderlab.csplugins.enrichmentmap.view.AboutPanel;
import org.baderlab.csplugins.enrichmentmap.view.BulkEMCreationPanel;
import org.baderlab.csplugins.enrichmentmap.view.EnrichmentMapInputPanel;
import org.baderlab.csplugins.enrichmentmap.view.PostAnalysisInputPanel;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.util.swing.FileChooserFilter;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.framework.BundleContext;



public class CyActivator extends AbstractCyActivator {

	public CyActivator(){
		super();
	}
	
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
		TaskManager<?, ?> taskManagerRef = getService(bc, TaskManager.class);
		CyServiceRegistrar cyServiceRegistrarRef = getService(bc,CyServiceRegistrar.class);
		CyEventHelper cyEventHelperRef = getService(bc, CyEventHelper.class);
		CySessionManager sessionManager = getService(bc, CySessionManager.class);
		RenderingEngineManager renderingEngineManager = getService(bc, RenderingEngineManager.class);
		CyTableFactory tableFactory = getService(bc, CyTableFactory.class);
		CyTableManager tableManager	= getService(bc, CyTableManager.class);
		
		//get the service registrar so we can register new services in different classes
		CyServiceRegistrar registrar = getService(bc, CyServiceRegistrar.class);
		
		// Get a Cytoscape service 'DialogTaskManager' in CyActivator class
		DialogTaskManager dialogTaskManager = getService(bc, DialogTaskManager.class);
		
		//create the Panels
		EnrichmentMapInputPanel emPanel = new EnrichmentMapInputPanel(cyNetworkFactoryRef, cyApplicationManagerRef, cyNetworkManagerRef, cyNetworkViewManagerRef, tableFactory, tableManager, cyNetworkViewFactoryRef, visualMappingManagerRef, visualStyleFactoryRef, continuousMappingFunctionFactoryRef, continuousMappingFunctionFactoryRef, continuousMappingFunctionFactoryRef, dialogTaskManager, sessionManager, cySwingApplicationRef, openBrowserRef,fileUtil,streamUtil,registrar);				
		BulkEMCreationPanel bulkEmPanel = new BulkEMCreationPanel(cySwingApplicationRef,fileUtil,registrar);		
		PostAnalysisInputPanel postEMPanel = new PostAnalysisInputPanel(cyApplicationManagerRef,cySwingApplicationRef, openBrowserRef,fileUtil,sessionManager, streamUtil);
		
		//Create each Action within Enrichment map as a service		
		Map<String, String> serviceProperties;
		
		//Build Enrichment Map Action - opens EM panel
		serviceProperties = new HashMap<String, String>();
		serviceProperties.put("inMenuBar", "true");
		serviceProperties.put("preferredMenu", "Apps.EnrichmentMap");
		LoadEnrichmentsPanelAction LoadEnrichmentMapInputPanelAction = new LoadEnrichmentsPanelAction(serviceProperties,cyApplicationManagerRef ,cyNetworkViewManagerRef, cySwingApplicationRef, emPanel,registrar);
				
		//Bulk Enrichment Map Action - open bulk em panel
		serviceProperties = new HashMap<String, String>();
		serviceProperties.put("inMenuBar", "true");
		serviceProperties.put("preferredMenu", "Apps.EnrichmentMap");
		BulkEMCreationAction BulkEMInputPanelAction = new BulkEMCreationAction(serviceProperties,cyApplicationManagerRef ,cyNetworkViewManagerRef, cySwingApplicationRef, bulkEmPanel,registrar);
		
		//Post Enrichment Map analysis Action - open post EM panel
		serviceProperties = new HashMap<String, String>();
		serviceProperties.put("inMenuBar", "true");
		serviceProperties.put("preferredMenu", "Apps.EnrichmentMap");
		LoadPostAnalysisPanelAction loadPostAnalysisAction = new LoadPostAnalysisPanelAction(serviceProperties,cyApplicationManagerRef ,cyNetworkViewManagerRef, cySwingApplicationRef, postEMPanel,registrar);
							
		
		//About Action
		serviceProperties = new HashMap<String, String>();
		serviceProperties.put("inMenuBar", "true");
		serviceProperties.put("preferredMenu", "Apps.EnrichmentMap");
		ShowAboutPanelAction aboutAction = new ShowAboutPanelAction(serviceProperties,cyApplicationManagerRef ,cyNetworkViewManagerRef, cySwingApplicationRef, openBrowserRef);		
				
		//register the services
		registerService(bc, aboutAction, CyAction.class,new Properties());
		registerService(bc, LoadEnrichmentMapInputPanelAction, CyAction.class, new Properties());
		registerService(bc, BulkEMInputPanelAction, CyAction.class, new Properties());
		registerService(bc, loadPostAnalysisAction, CyAction.class, new Properties());
	}
}
