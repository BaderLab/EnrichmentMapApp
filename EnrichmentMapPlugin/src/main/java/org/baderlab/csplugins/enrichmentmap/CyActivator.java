 package org.baderlab.csplugins.enrichmentmap;

import java.util.Properties;

import org.baderlab.csplugins.enrichmentmap.ApplicationModule.Headless;
import org.baderlab.csplugins.enrichmentmap.actions.OpenEnrichmentMapAction;
import org.baderlab.csplugins.enrichmentmap.actions.ShowEnrichmentMapDialogAction;
import org.baderlab.csplugins.enrichmentmap.commands.CommandModule;
import org.baderlab.csplugins.enrichmentmap.commands.CommandModule.BuildCommand;
import org.baderlab.csplugins.enrichmentmap.commands.CommandModule.GSEACommand;
import org.baderlab.csplugins.enrichmentmap.commands.CommandModule.JsonCommand;
import org.baderlab.csplugins.enrichmentmap.commands.CommandModule.PACommand;
import org.baderlab.csplugins.enrichmentmap.commands.CommandModule.ResolveCommand;
import org.baderlab.csplugins.enrichmentmap.commands.MannWhitRanksTunableHandlerFactory;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.model.io.SessionListener;
import org.baderlab.csplugins.enrichmentmap.style.ChartFactoryManager;
import org.baderlab.csplugins.enrichmentmap.style.charts.radialheatmap.RadialHeatMapChartFactory;
import org.baderlab.csplugins.enrichmentmap.view.control.ControlPanelMediator;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapMediator;
import org.baderlab.csplugins.enrichmentmap.view.legend.LegendPanelMediator;
import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.command.StringTunableHandlerFactory;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;
import org.cytoscape.work.ServiceProperties;
import org.cytoscape.work.TaskFactory;
import org.ops4j.peaberry.osgi.OSGiModule;
import org.osgi.framework.BundleContext;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral; 


public class CyActivator extends AbstractCyActivator {

	public static final String APP_NAME = "EnrichmentMap";
	
	private Injector injector;
	
	
	@Override
	public void start(BundleContext bc) {
		injector = Guice.createInjector(new OSGiModule(bc), new AfterInjectionModule(), 
										new CytoscapeServiceModule(), new ApplicationModule(), 
										new CommandModule());
		
		// manager
		EnrichmentMapManager manager = injector.getInstance(EnrichmentMapManager.class);
		registerAllServices(bc, manager, new Properties());
		
		// session save and restore
		SessionListener sessionListener = injector.getInstance(SessionListener.class);
		registerAllServices(bc, sessionListener, new Properties());
		
		// commands
		registerCommand(bc, "build",        injector.getInstance(Key.get(TaskFactory.class, BuildCommand.class)));
		registerCommand(bc, "gseabuild",         injector.getInstance(Key.get(TaskFactory.class, GSEACommand.class)));
		registerCommand(bc, "mastermap",    injector.getInstance(Key.get(TaskFactory.class, ResolveCommand.class)));
		registerCommand(bc, "pa",           injector.getInstance(Key.get(TaskFactory.class, PACommand.class)));
		registerCommand(bc, "export-model", injector.getInstance(Key.get(TaskFactory.class, JsonCommand.class)));
		registerService(bc, new MannWhitRanksTunableHandlerFactory(), StringTunableHandlerFactory.class, new Properties());
		
		// CyProperty
		CyProperty<Properties> cyProperty = injector.getInstance(Key.get(new TypeLiteral<CyProperty<Properties>>(){}));
		registerAllServices(bc, cyProperty, PropsReader.getServiceProps());
				
		boolean headless = injector.getInstance(Key.get(Boolean.class, Headless.class));
		
		if(headless) {
			// register the injector as an OSGi service so the integration tests can access it
			registerService(bc, injector, Injector.class, new Properties());
		} else {
			// Don't load UI services if running headless
			// register actions
			registerAllServices(bc, injector.getInstance(OpenEnrichmentMapAction.class), new Properties());
			
			// chart listener
			ChartFactoryManager chartFactoryManager = injector.getInstance(ChartFactoryManager.class);
			registerServiceListener(bc, chartFactoryManager, "addFactory", "removeFactory", CyCustomGraphics2Factory.class);
			
			// chart factories
			final Properties chartProps = new Properties();
			chartProps.setProperty(CyCustomGraphics2Factory.GROUP, "Charts");
			RadialHeatMapChartFactory radialHeatMapChartFactory = injector.getInstance(RadialHeatMapChartFactory.class);
			registerService(bc, radialHeatMapChartFactory, CyCustomGraphics2Factory.class, chartProps);
			
			// UI Mediators
			ControlPanelMediator controlPanelMediator = injector.getInstance(ControlPanelMediator.class);
			registerAllServices(bc, controlPanelMediator, new Properties());
			
			HeatMapMediator heatMapMediator = injector.getInstance(HeatMapMediator.class);
			registerAllServices(bc, heatMapMediator, new Properties());
		}
		
		// If the App is updated or restarted then we want to reload the model and view from the tables
		sessionListener.restore(null);
		
		Em21Handler.removeVersion21(bc, injector.getInstance(CyApplicationConfiguration.class));
	}
	
	
	private void registerCommand(BundleContext bc, String command, TaskFactory taskFactory) {
		Properties props = new Properties();
		props.put(ServiceProperties.COMMAND, command);
		props.put(ServiceProperties.COMMAND_NAMESPACE, "enrichmentmap");
		registerService(bc, taskFactory, TaskFactory.class, props);
	}
	
	
	@Override
	public void shutDown() {
		try {
			if (injector != null) {
				boolean headless = injector.getInstance(Key.get(Boolean.class, Headless.class));
				
				// If the App gets updated or restarted we need to save all the data first
				SessionListener sessionListener = injector.getInstance(SessionListener.class);
				sessionListener.appShutdown();
				
				if(!headless) {
					// Close the legend panel
					LegendPanelMediator legendPanelMediator = injector.getInstance(LegendPanelMediator.class);
					legendPanelMediator.hideDialog();
					
					// Dispose the creation dialog, or else lots of memory leaks.
					ShowEnrichmentMapDialogAction dialogAction = injector.getInstance(ShowEnrichmentMapDialogAction.class);
					dialogAction.dispose();
				}
			}
		} finally {
			super.shutDown();
		}
	}
}
