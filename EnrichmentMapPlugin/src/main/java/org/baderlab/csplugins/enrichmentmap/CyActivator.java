package org.baderlab.csplugins.enrichmentmap;

import java.util.Properties;

import org.baderlab.csplugins.enrichmentmap.ApplicationModule.Headless;
import org.baderlab.csplugins.enrichmentmap.actions.HeatMapSelectionListener;
import org.baderlab.csplugins.enrichmentmap.actions.OpenEnrichmentMapAction;
import org.baderlab.csplugins.enrichmentmap.commands.BuildEnrichmentMapTuneableTaskFactory;
import org.baderlab.csplugins.enrichmentmap.commands.EnrichmentMapGSEACommandHandlerTaskFactory;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.model.io.LegacySessionLoader;
import org.baderlab.csplugins.enrichmentmap.model.io.SessionModelListener;
import org.baderlab.csplugins.enrichmentmap.style.ChartFactoryManager;
import org.baderlab.csplugins.enrichmentmap.view.control.ControlPanelMediator;
import org.baderlab.csplugins.enrichmentmap.view.parameters.ParametersPanelMediator;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;
import org.cytoscape.work.ServiceProperties;
import org.cytoscape.work.TaskFactory;
import org.ops4j.peaberry.osgi.OSGiModule;
import org.osgi.framework.BundleContext;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key; 


public class CyActivator extends AbstractCyActivator {

	public static final String APP_NAME = "EnrichmentMap";
	
	private Injector injector;
	
	
	@Override
	public void start(BundleContext bc) {
		injector = Guice.createInjector(new OSGiModule(bc), new AfterInjectionModule(), 
										new CytoscapeServiceModule(), new ApplicationModule());
		
		// register the injector as an OSGi service so the integration tests can access it
		registerService(bc, injector, Injector.class, new Properties());
		
		// manager
		EnrichmentMapManager manager = injector.getInstance(EnrichmentMapManager.class);
		registerAllServices(bc, manager, new Properties());
		
		// session save and restore
		SessionModelListener sessionListener = injector.getInstance(SessionModelListener.class);
		registerAllServices(bc, sessionListener, new Properties());
		sessionListener.getDebugActions().forEach(a -> registerService(bc, a, CyAction.class, new Properties())); // TEMPORARY
		
		LegacySessionLoader legacyListener = injector.getInstance(LegacySessionLoader.class);
		registerAllServices(bc, legacyListener, new Properties());

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

		// Don't load UI services if running headless
		boolean headless = injector.getInstance(Key.get(Boolean.class, Headless.class));
		if(!headless) {
			// heat map
			HeatMapSelectionListener selectionListener = injector.getInstance(HeatMapSelectionListener.class);
			registerAllServices(bc, selectionListener, new Properties());	
					
			// register actions
			registerAllServices(bc, injector.getInstance(OpenEnrichmentMapAction.class), new Properties());
				
			// chart listener
			ChartFactoryManager chartFactoryManager = injector.getInstance(ChartFactoryManager.class);
			registerServiceListener(bc, chartFactoryManager, "addFactory", "removeFactory", CyCustomGraphics2Factory.class);
					
			// UI Mediators
			ControlPanelMediator controlPanelMediator = injector.getInstance(ControlPanelMediator.class);
			registerAllServices(bc, controlPanelMediator, new Properties());
			
			ParametersPanelMediator parametersPanelMediator = injector.getInstance(ParametersPanelMediator.class);
			registerAllServices(bc, parametersPanelMediator, new Properties());
		}
	}
	
	
	@Override
	public void shutDown() {
		// If the App gets updated or restarted we need to save all the data first
		if(injector != null) {
			SessionModelListener sessionListener = injector.getInstance(SessionModelListener.class);
			sessionListener.saveModel();
		}
	}
	
}
