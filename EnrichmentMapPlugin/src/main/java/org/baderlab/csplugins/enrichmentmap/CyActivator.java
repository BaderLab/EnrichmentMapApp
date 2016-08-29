package org.baderlab.csplugins.enrichmentmap;

import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.Properties;

import org.baderlab.csplugins.enrichmentmap.actions.EnrichmentMapActionListener;
import org.baderlab.csplugins.enrichmentmap.actions.EnrichmentMapSessionAction;
import org.baderlab.csplugins.enrichmentmap.actions.LoadEnrichmentsPanelAction;
import org.baderlab.csplugins.enrichmentmap.actions.LoadPostAnalysisPanelAction;
import org.baderlab.csplugins.enrichmentmap.actions.ShowAboutPanelAction;
import org.baderlab.csplugins.enrichmentmap.actions.ShowEdgeWidthDialogAction;
import org.baderlab.csplugins.enrichmentmap.commands.BuildEnrichmentMapTuneableTaskFactory;
import org.baderlab.csplugins.enrichmentmap.commands.EnrichmentMapGSEACommandHandlerTaskFactory;
import org.baderlab.csplugins.enrichmentmap.task.EdgeWidthTableColumnTaskFactory;
import org.baderlab.csplugins.enrichmentmap.view.EnrichmentMapInputPanel;
import org.baderlab.csplugins.enrichmentmap.view.HeatMapPanel;
import org.baderlab.csplugins.enrichmentmap.view.ParametersPanel;
import org.baderlab.csplugins.enrichmentmap.view.PostAnalysisPanel;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.task.TableColumnTaskFactory;
import org.cytoscape.work.ServiceProperties;
import org.cytoscape.work.TaskFactory;
import org.ops4j.peaberry.osgi.OSGiModule;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.google.inject.Guice;
import com.google.inject.Injector;



public class CyActivator extends AbstractCyActivator {

	public void start(BundleContext bc) {
		ServiceReference ref = bc.getServiceReference(CySwingApplication.class.getName());
		if(ref == null) {
			return; // Cytoscape is running headless or integration tests are running, don't register UI components
		}
		
		Injector injector = Guice.createInjector(new OSGiModule(bc), new AfterInjectionModule(), new CytoscapeServiceModule());
		
		
		CyServiceRegistrar registrar = getService(bc, CyServiceRegistrar.class);

		EnrichmentMapInputPanel emPanel = injector.getInstance(EnrichmentMapInputPanel.class);		
		PostAnalysisPanel postEMPanel   = injector.getInstance(PostAnalysisPanel.class);
		HeatMapPanel heatMapPanel_node  = injector.getInstance(HeatMapPanel.class).setNode(true);
		HeatMapPanel heatMapPanel_edge  = injector.getInstance(HeatMapPanel.class).setNode(false);
		ParametersPanel paramsPanel     = injector.getInstance(ParametersPanel.class);

		// MKTODO: convert EnrichmentMapManager into a guice eager singleton
		EnrichmentMapManager manager = EnrichmentMapManager.getInstance();
		manager.initialize(paramsPanel, heatMapPanel_node, heatMapPanel_edge, registrar);
		
		//register network events with manager class
		registerService(bc, manager, NetworkAboutToBeDestroyedListener.class, new Properties());
		registerService(bc, manager, SetCurrentNetworkListener.class, new Properties());
		registerService(bc, manager, SetCurrentNetworkViewListener.class, new Properties());

		//associate them with the action listener
		EnrichmentMapActionListener EMActionListener = injector.getInstance(EnrichmentMapActionListener.class).init(heatMapPanel_node, heatMapPanel_edge);
		registerService(bc, EMActionListener, RowsSetListener.class, new Properties());		

		AbstractCyAction aboutAction        = injector.getInstance(ShowAboutPanelAction.class);
		AbstractCyAction inputPanelAction   = injector.getInstance(LoadEnrichmentsPanelAction.class).init(emPanel);
		AbstractCyAction postAnalysisAction = injector.getInstance(LoadPostAnalysisPanelAction.class).init(postEMPanel);
		AbstractCyAction edgePanelAction    = injector.getInstance(ShowEdgeWidthDialogAction.class);

		//register the services
		registerAction(bc, inputPanelAction);
		registerAction(bc, postAnalysisAction);	
		registerAction(bc, edgePanelAction);
		registerAction(bc, aboutAction);

		//register the session save and restore
		EnrichmentMapSessionAction sessionAction = injector.getInstance(EnrichmentMapSessionAction.class);
		registerService(bc, sessionAction, SessionAboutToBeSavedListener.class, new Properties());
		registerService(bc, sessionAction, SessionLoadedListener.class, new Properties());

		//generic EM command line option
		TaskFactory buildCommandTask = injector.getInstance(BuildEnrichmentMapTuneableTaskFactory.class);
		Properties properties = new Properties();
		properties.put(ServiceProperties.COMMAND, "build");
		properties.put(ServiceProperties.COMMAND_NAMESPACE, "enrichmentmap");
		registerService(bc, buildCommandTask, TaskFactory.class, properties);

		//gsea specifc commandtool
		TaskFactory gseaCommandTask = injector.getInstance(EnrichmentMapGSEACommandHandlerTaskFactory.class);
		properties = new Properties();
		properties.put(ServiceProperties.COMMAND, "gseabuild");
		properties.put(ServiceProperties.COMMAND_NAMESPACE, "enrichmentmap");
		registerService(bc, gseaCommandTask, TaskFactory.class, properties);

		//edge table context menu
		EdgeWidthTableColumnTaskFactory tableColumnTaskFactory = new EdgeWidthTableColumnTaskFactory(edgePanelAction);
		Properties tableColumnTaskFactoryProps = new Properties();
		tableColumnTaskFactoryProps.setProperty(TITLE, "Post Analysis Edge Width...");
		tableColumnTaskFactoryProps.setProperty("tableTypes", "edge");
		registerService(bc, tableColumnTaskFactory, TableColumnTaskFactory.class, tableColumnTaskFactoryProps);

	}
	
	private void registerAction(BundleContext context, AbstractCyAction action) {
		action.setPreferredMenu("Apps.EnrichmentMap");
		registerService(context, action, CyAction.class, new Properties());
	}
	
}
