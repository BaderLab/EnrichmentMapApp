 package org.baderlab.csplugins.enrichmentmap;

import static org.cytoscape.work.ServiceProperties.APPS_MENU;
import static org.cytoscape.work.ServiceProperties.IN_MENU_BAR;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.Properties;

import org.baderlab.csplugins.enrichmentmap.ApplicationModule.Headless;
import org.baderlab.csplugins.enrichmentmap.CommandModule.BuildCommand;
import org.baderlab.csplugins.enrichmentmap.CommandModule.BuildTableCommand;
import org.baderlab.csplugins.enrichmentmap.CommandModule.ChartCommand;
import org.baderlab.csplugins.enrichmentmap.CommandModule.DatasetHideCommand;
import org.baderlab.csplugins.enrichmentmap.CommandModule.DatasetShowCommand;
import org.baderlab.csplugins.enrichmentmap.CommandModule.ExportPDFCommand;
import org.baderlab.csplugins.enrichmentmap.CommandModule.GSEACommand;
import org.baderlab.csplugins.enrichmentmap.CommandModule.JsonCommand;
import org.baderlab.csplugins.enrichmentmap.CommandModule.PACommand;
import org.baderlab.csplugins.enrichmentmap.CommandModule.ResolveCommand;
import org.baderlab.csplugins.enrichmentmap.actions.OpenEnrichmentMapAction;
import org.baderlab.csplugins.enrichmentmap.actions.OpenPathwayCommonsTaskFactory;
import org.baderlab.csplugins.enrichmentmap.commands.tunables.MannWhitRanksTunableHandlerFactory;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.model.io.SessionListener;
import org.baderlab.csplugins.enrichmentmap.rest.EnrichmentMapResource;
import org.baderlab.csplugins.enrichmentmap.style.ChartFactoryManager;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder;
import org.baderlab.csplugins.enrichmentmap.style.charts.radialheatmap.RadialHeatMapChartFactory;
import org.baderlab.csplugins.enrichmentmap.task.genemania.QueryGeneManiaNodeViewTaskFactory;
import org.baderlab.csplugins.enrichmentmap.task.string.QueryStringNodeViewTaskFactory;
import org.baderlab.csplugins.enrichmentmap.task.tunables.GeneListGUITunableHandler;
import org.baderlab.csplugins.enrichmentmap.task.tunables.GeneListTunable;
import org.baderlab.csplugins.enrichmentmap.view.EMColumnPresentation;
import org.baderlab.csplugins.enrichmentmap.view.control.ControlPanelMediator;
import org.baderlab.csplugins.enrichmentmap.view.creation.CreationDialogShowAction;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapMediator;
import org.baderlab.csplugins.enrichmentmap.view.legend.LegendPanelMediator;
import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.swing.CyColumnPresentation;
import org.cytoscape.command.StringTunableHandlerFactory;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;
import org.cytoscape.work.ServiceProperties;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.GUITunableHandlerFactory;
import org.cytoscape.work.swing.SimpleGUITunableHandlerFactory;
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
		registerAllServices(bc, manager);
		
		// session save and restore
		SessionListener sessionListener = injector.getInstance(SessionListener.class);
		registerAllServices(bc, sessionListener);
		
		// commands
		initializeCommands(bc);
		
		// jax-rs resources
		registerService(bc, injector.getInstance(EnrichmentMapResource.class), EnrichmentMapResource.class);
		
		// CyProperty
		CyProperty<Properties> cyProperty = injector.getInstance(Key.get(new TypeLiteral<CyProperty<Properties>>(){}));
		registerAllServices(bc, cyProperty, PropsReader.getServiceProps());
				
		boolean headless = injector.getInstance(Key.get(Boolean.class, Headless.class));
		
		if(headless) {
			// register the injector as an OSGi service so the integration tests can access it
			registerService(bc, injector, Injector.class);
		} else {
			// Don't load UI services if running headless
			// register actions
			registerAllServices(bc, injector.getInstance(OpenEnrichmentMapAction.class));
			
			// context menu actions in network view
			registerNodeViewMenu(bc, "Pathway Commons", OpenPathwayCommonsTaskFactory.class);
			registerNodeViewMenu(bc, "GeneMANIA",       QueryGeneManiaNodeViewTaskFactory.class);
			registerNodeViewMenu(bc, "STRING",          QueryStringNodeViewTaskFactory.class);
			
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
			registerAllServices(bc, controlPanelMediator);
			HeatMapMediator heatMapMediator = injector.getInstance(HeatMapMediator.class);
			registerAllServices(bc, heatMapMediator);
			
			// column namespace
			Properties colProps = new Properties();
			colProps.put(CyColumnPresentation.NAMESPACE, EMStyleBuilder.Columns.NAMESPACE);
			registerService(bc, new EMColumnPresentation(), CyColumnPresentation.class, colProps);
			
			// gene list GUI tunable handler
			SimpleGUITunableHandlerFactory<GeneListGUITunableHandler> geneListGuiHandler 
				= new SimpleGUITunableHandlerFactory<>(GeneListGUITunableHandler.class, GeneListTunable.class);
			registerService(bc, geneListGuiHandler, GUITunableHandlerFactory.class);
		}
		
		// If the App is updated or restarted then we want to reload the model and view from the tables
		sessionListener.restore(null);
		
		Em21Handler.removeVersion21(bc, injector.getInstance(CyApplicationConfiguration.class));
	}
	
	private void registerNodeViewMenu(BundleContext bc, String text, Class<?> klass) {
		Properties props = new Properties();
		props.setProperty(IN_MENU_BAR, "false");
		props.setProperty(PREFERRED_MENU, APPS_MENU);
		props.setProperty(TITLE, "EnrichmentMap - Show in " + text);
		registerAllServices(bc, injector.getInstance(klass), props);
	}
	
	private void initializeCommands(BundleContext bc) {
		registerCommand(bc, "build",        injector.getInstance(Key.get(TaskFactory.class, BuildCommand.class)));
		registerCommand(bc, "gseabuild",    injector.getInstance(Key.get(TaskFactory.class, GSEACommand.class)));
		registerCommand(bc, "mastermap",    injector.getInstance(Key.get(TaskFactory.class, ResolveCommand.class)));
		registerCommand(bc, "pa",           injector.getInstance(Key.get(TaskFactory.class, PACommand.class)));
		registerCommand(bc, "export model", injector.getInstance(Key.get(TaskFactory.class, JsonCommand.class)));
		registerCommand(bc, "build-table",  injector.getInstance(Key.get(TaskFactory.class, BuildTableCommand.class)));
		registerCommand(bc, "dataset show", injector.getInstance(Key.get(TaskFactory.class, DatasetShowCommand.class)));
		registerCommand(bc, "dataset hide", injector.getInstance(Key.get(TaskFactory.class, DatasetHideCommand.class)));
		registerCommand(bc, "chart",        injector.getInstance(Key.get(TaskFactory.class, ChartCommand.class)));
		registerCommand(bc, "export pdf",   injector.getInstance(Key.get(TaskFactory.class, ExportPDFCommand.class)));
		registerService(bc, new MannWhitRanksTunableHandlerFactory(), StringTunableHandlerFactory.class);
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
				
				HeatMapMediator heatMapMediator = injector.getInstance(HeatMapMediator.class);
				heatMapMediator.shutDown();
				
				// If the App gets updated or restarted we need to save all the data first
				SessionListener sessionListener = injector.getInstance(SessionListener.class);
				sessionListener.appShutdown();
				
				
				if(!headless) {
					// Close the legend panel
					LegendPanelMediator legendPanelMediator = injector.getInstance(LegendPanelMediator.class);
					legendPanelMediator.hideDialog();
					
					// Dispose the creation dialog, or else lots of memory leaks.
					CreationDialogShowAction dialogAction = injector.getInstance(CreationDialogShowAction.class);
					dialogAction.dispose();
				}
			}
		} finally {
			super.shutDown();
		}
	}
}
