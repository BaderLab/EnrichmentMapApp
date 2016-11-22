package org.baderlab.csplugins.enrichmentmap;

import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.Properties;

import org.baderlab.csplugins.enrichmentmap.actions.EdgeWidthTableColumnTaskFactory;
import org.baderlab.csplugins.enrichmentmap.actions.HeatMapSelectionListener;
import org.baderlab.csplugins.enrichmentmap.actions.LegacyEnrichmentMapSessionListener;
import org.baderlab.csplugins.enrichmentmap.actions.OpenEnrichmentMapAction;
import org.baderlab.csplugins.enrichmentmap.actions.ShowEdgeWidthDialogAction;
import org.baderlab.csplugins.enrichmentmap.commands.BuildEnrichmentMapTuneableTaskFactory;
import org.baderlab.csplugins.enrichmentmap.commands.EnrichmentMapGSEACommandHandlerTaskFactory;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.style.ChartFactoryManager;
import org.baderlab.csplugins.enrichmentmap.view.control.ControlPanelMediator;
import org.baderlab.csplugins.enrichmentmap.view.parameters.ParametersPanelMediator;
import org.baderlab.csplugins.enrichmentmap.view.postanalysis.PostAnalysisPanelMediator;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.service.util.AbstractCyActivator;
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
	
	@Override
	public void start(BundleContext bc) {
		ServiceReference ref = bc.getServiceReference(CySwingApplication.class.getName());
		
		if (ref == null)
			return; // Cytoscape is running headless or integration tests are running, don't register UI components
		
		Injector injector = Guice.createInjector(new OSGiModule(bc), new AfterInjectionModule(), 
												 new CytoscapeServiceModule(), new ApplicationModule());
		
		// manager
		EnrichmentMapManager manager = injector.getInstance(EnrichmentMapManager.class);
		registerAllServices(bc, manager, new Properties());
		
		// heat map
		HeatMapSelectionListener selectionListener = injector.getInstance(HeatMapSelectionListener.class);
		registerAllServices(bc, selectionListener, new Properties());		

		// register actions
		registerAllServices(bc, injector.getInstance(OpenEnrichmentMapAction.class), new Properties());
//		registerAction(bc, injector.getInstance(ShowEdgeWidthDialogAction.class));

		// session save and restore
		LegacyEnrichmentMapSessionListener sessionAction = injector.getInstance(LegacyEnrichmentMapSessionListener.class);
		registerAllServices(bc, sessionAction, new Properties());

		// chart listener
		ChartFactoryManager chartFactoryManager = injector.getInstance(ChartFactoryManager.class);
		registerServiceListener(bc, chartFactoryManager, "addFactory", "removeFactory", CyCustomGraphics2Factory.class);
		
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
		
		ControlPanelMediator controlPanelMediator = injector.getInstance(ControlPanelMediator.class);
		registerAllServices(bc, controlPanelMediator, new Properties());
		
		ParametersPanelMediator parametersPanelMediator = injector.getInstance(ParametersPanelMediator.class);
		registerAllServices(bc, parametersPanelMediator, new Properties());
		
		PostAnalysisPanelMediator postAnalysisPanelMediator = injector.getInstance(PostAnalysisPanelMediator.class);
		registerAllServices(bc, postAnalysisPanelMediator, new Properties());
	}
}
