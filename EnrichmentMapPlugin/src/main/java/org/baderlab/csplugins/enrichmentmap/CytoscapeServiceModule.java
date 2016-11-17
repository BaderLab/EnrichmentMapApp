package org.baderlab.csplugins.enrichmentmap;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.ops4j.peaberry.Peaberry.service;
import static org.ops4j.peaberry.util.Filters.ldap;

import java.lang.annotation.Retention;

import javax.swing.JFrame;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.command.AvailableCommands;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.group.CyGroupSettingsManager;
import org.cytoscape.group.data.CyGroupAggregationManager;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.task.edit.MapTableToNetworkTablesTaskFactory;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;

/**
 * Guice configuration module. 
 * Binds Cytoscape services using Peaberry.
 * 
 * @author mkucera
 */
public class CytoscapeServiceModule extends AbstractModule {

	// VisualMappingFunctionFactory
	@BindingAnnotation @Retention(RUNTIME) public @interface Continuous {}
	@BindingAnnotation @Retention(RUNTIME) public @interface Discrete {}
	@BindingAnnotation @Retention(RUNTIME) public @interface Passthrough {}
	
	// TaskManager<?,?>
	@BindingAnnotation @Retention(RUNTIME) public @interface Dialog {}
	@BindingAnnotation @Retention(RUNTIME) public @interface Sync {}
	
	
	@Override
	protected void configure() {
		// Bind cytoscape OSGi services
		bindService(CyServiceRegistrar.class);
		bindService(CyApplicationManager.class);
		bindService(CySwingApplication.class);
		bindService(CyNetworkManager.class);
		bindService(CyNetworkViewFactory.class);
		bindService(CyNetworkViewManager.class);
		bindService(CyNetworkFactory.class);
		bindService(IconManager.class);
		bindService(CyLayoutAlgorithmManager.class);
		bindService(CyGroupManager.class);
		bindService(CyGroupFactory.class);
		bindService(CyGroupAggregationManager.class);
		bindService(CyGroupSettingsManager.class);
		bindService(AvailableCommands.class);
		bindService(CommandExecutorTaskFactory.class);
		bindService(CySessionManager.class);
		bindService(CyEventHelper.class);
		bindService(OpenBrowser.class);
		bindService(VisualMappingManager.class);
		bindService(VisualStyleFactory.class);
		bindService(CyNetworkTableManager.class);
		bindService(CyTableManager.class);
		bindService(CyTableFactory.class);
		bindService(FileUtil.class);
		bindService(StreamUtil.class);
		bindService(MapTableToNetworkTablesTaskFactory.class);
		bindService(RenderingEngineManager.class);
		bindService(CyColumnIdentifierFactory.class);
		bindService(CyNetworkNaming.class);
		
		bindService(DialogTaskManager.class);
		TypeLiteral<SynchronousTaskManager<?>> synchronousManager = new TypeLiteral<SynchronousTaskManager<?>>(){};
		bind(synchronousManager).toProvider(service(synchronousManager).single());
		
		TypeLiteral<TaskManager<?,?>> taskManager = new TypeLiteral<TaskManager<?,?>>(){};
		bind(taskManager).annotatedWith(Dialog.class).toProvider(service(DialogTaskManager.class).single());
		bind(taskManager).annotatedWith(Sync.class).toProvider(service(synchronousManager).single());
		
		bind(VisualMappingFunctionFactory.class).annotatedWith(Continuous.class).toProvider(service(VisualMappingFunctionFactory.class).filter(ldap("(mapping.type=continuous)")).single());
		bind(VisualMappingFunctionFactory.class).annotatedWith(Discrete.class).toProvider(service(VisualMappingFunctionFactory.class).filter(ldap("(mapping.type=discrete)")).single());
		bind(VisualMappingFunctionFactory.class).annotatedWith(Passthrough.class).toProvider(service(VisualMappingFunctionFactory.class).filter(ldap("(mapping.type=passthrough)")).single());
	}
		
	private <T> void bindService(Class<T> serviceClass) {
		bind(serviceClass).toProvider(service(serviceClass).single().direct());
	}
	
	@Provides
	public JFrame getJFrame(CySwingApplication swingApplication) {
		return swingApplication.getJFrame();
	}

}
