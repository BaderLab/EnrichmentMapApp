package org.baderlab.csplugins.enrichmentmap;

import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.Properties;

import org.baderlab.csplugins.enrichmentmap.actions.EdgeWidthTableColumnTaskFactory;
import org.baderlab.csplugins.enrichmentmap.actions.HeatMapSelectionListener;
import org.baderlab.csplugins.enrichmentmap.actions.LegacyEnrichmentMapSessionListener;
import org.baderlab.csplugins.enrichmentmap.actions.LoadEnrichmentsPanelAction;
import org.baderlab.csplugins.enrichmentmap.actions.LoadPostAnalysisPanelAction;
import org.baderlab.csplugins.enrichmentmap.actions.ShowAboutDialogAction;
import org.baderlab.csplugins.enrichmentmap.actions.ShowControlPanelAction;
import org.baderlab.csplugins.enrichmentmap.actions.ShowEdgeWidthDialogAction;
import org.baderlab.csplugins.enrichmentmap.commands.BuildEnrichmentMapTuneableTaskFactory;
import org.baderlab.csplugins.enrichmentmap.commands.EnrichmentMapGSEACommandHandlerTaskFactory;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.style.CustomChartListener;
import org.baderlab.csplugins.enrichmentmap.view.mastermap.MasterMapDialogAction;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.task.TableColumnTaskFactory;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;
import org.cytoscape.work.ServiceProperties;
import org.cytoscape.work.TaskFactory;
import org.ops4j.peaberry.osgi.OSGiModule;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.google.inject.Guice;
import com.google.inject.Injector; 



public class CyActivator extends AbstractCyActivator {

	public static final String APP_NAME = "EnrichmentMap";
	
	
	public void start(BundleContext bc) {
		ServiceReference ref = bc.getServiceReference(CySwingApplication.class.getName());
		if(ref == null) {
			return; // Cytoscape is running headless or integration tests are running, don't register UI components
		}
		
		Injector injector = Guice.createInjector(new OSGiModule(bc), new AfterInjectionModule(), 
												 new CytoscapeServiceModule(), new ApplicationModule());
		
		// manager
		EnrichmentMapManager manager = injector.getInstance(EnrichmentMapManager.class);
		registerService(bc, manager, NetworkAboutToBeDestroyedListener.class, new Properties());
		registerService(bc, manager, SetCurrentNetworkListener.class, new Properties());
		registerService(bc, manager, SetCurrentNetworkViewListener.class, new Properties());
		
		// heat map
		HeatMapSelectionListener selectionListener = injector.getInstance(HeatMapSelectionListener.class);
		registerService(bc, selectionListener, RowsSetListener.class, new Properties());		

		// register actions
		registerAction(bc, injector.getInstance(MasterMapDialogAction.class));
		registerAction(bc, injector.getInstance(LoadEnrichmentsPanelAction.class));
		registerAction(bc, injector.getInstance(LoadPostAnalysisPanelAction.class));	
		registerAction(bc, injector.getInstance(ShowEdgeWidthDialogAction.class));
		registerAction(bc, injector.getInstance(ShowControlPanelAction.class));
		registerAction(bc, injector.getInstance(ShowAboutDialogAction.class));

		// session save and restore
		LegacyEnrichmentMapSessionListener sessionAction = injector.getInstance(LegacyEnrichmentMapSessionListener.class);
//		registerService(bc, sessionAction, SessionAboutToBeSavedListener.class, new Properties());
		registerService(bc, sessionAction, SessionLoadedListener.class, new Properties());

		// chart listener
		CustomChartListener chartListener = injector.getInstance(CustomChartListener.class);
		registerServiceListener(bc, chartListener, "addFactory", "removeFactory", CyCustomGraphics2Factory.class);
		
		// commands
		TaskFactory buildCommandTask = injector.getInstance(BuildEnrichmentMapTuneableTaskFactory.class);
		Properties props = new Properties();
		props.put(ServiceProperties.COMMAND, "build");
		props.put(ServiceProperties.COMMAND_NAMESPACE, "enrichmentmap");
		registerService(bc, buildCommandTask, TaskFactory.class, props);

		TaskFactory gseaCommandTask = injector.getInstance(EnrichmentMapGSEACommandHandlerTaskFactory.class);
		props = new Properties();
		props.put(ServiceProperties.COMMAND, "gseabuild");
		props.put(ServiceProperties.COMMAND_NAMESPACE, "enrichmentmap");
		registerService(bc, gseaCommandTask, TaskFactory.class, props);

		// edge table context menu
		AbstractCyAction edgeWidthDialogAction = injector.getInstance(ShowEdgeWidthDialogAction.class);
		EdgeWidthTableColumnTaskFactory tableColumnTaskFactory = new EdgeWidthTableColumnTaskFactory(edgeWidthDialogAction);
		props = new Properties();
		props.setProperty(TITLE, "Post Analysis Edge Width...");
		props.setProperty("tableTypes", "edge");
		registerService(bc, tableColumnTaskFactory, TableColumnTaskFactory.class, props);
	}
	
	
	private void registerAction(BundleContext bc, AbstractCyAction action) {
		action.setPreferredMenu("Apps.EnrichmentMap");
		registerService(bc, action, CyAction.class, new Properties());
	}
	
}
