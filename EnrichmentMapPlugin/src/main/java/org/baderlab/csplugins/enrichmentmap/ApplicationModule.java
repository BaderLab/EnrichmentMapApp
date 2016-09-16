package org.baderlab.csplugins.enrichmentmap;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import org.baderlab.csplugins.enrichmentmap.actions.BuildPostAnalysisActionListener;
import org.baderlab.csplugins.enrichmentmap.actions.LoadSignatureSetsActionListener;
import org.baderlab.csplugins.enrichmentmap.task.BuildDiseaseSignatureTask;
import org.baderlab.csplugins.enrichmentmap.task.CreateEnrichmentMapNetworkTask;
import org.baderlab.csplugins.enrichmentmap.task.CreatePostAnalysisVisualStyleTask;
import org.baderlab.csplugins.enrichmentmap.task.EnrichmentMapBuildMapTaskFactory;
import org.baderlab.csplugins.enrichmentmap.task.VisualizeEnrichmentMapTask;
import org.baderlab.csplugins.enrichmentmap.view.HeatMapPanel;
import org.baderlab.csplugins.enrichmentmap.view.PostAnalysisKnownSignaturePanel;
import org.baderlab.csplugins.enrichmentmap.view.PostAnalysisSignatureDiscoveryPanel;

import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 * Guice module
 */
public class ApplicationModule extends AbstractModule {

	@BindingAnnotation @Retention(RUNTIME) public @interface Nodes {}
	@BindingAnnotation @Retention(RUNTIME) public @interface Edges {}
	
	private boolean headless = false;
	
	@Override
	protected void configure() {
		requestStaticInjection(EnrichmentMapManager.class); // TEMPORARY
		bind(EnrichmentMapManager.class).asEagerSingleton();
		
		install(new FactoryModule());
	}
	
	@Provides @Edges @Singleton
	public HeatMapPanel heatMapPanelForEdges(Provider<HeatMapPanel> panelProvider) {
		return headless ? null : panelProvider.get().setNode(false);
	}
	
	@Provides @Nodes @Singleton
	public HeatMapPanel heatMapPanelForNodes(Provider<HeatMapPanel> panelProvider) {
		return headless ? null : panelProvider.get().setNode(true);
	}
	
	public static Module createFactoryModule() {
		return new FactoryModule();
	}
	
	public static ApplicationModule headless() {
		ApplicationModule module = new ApplicationModule();
		module.headless = true;
		return module;
	}
}


class FactoryModule extends AbstractModule {
	
	@Override
	protected void configure() {
		// Factories using AssistedInject
		installFactory(VisualizeEnrichmentMapTask.Factory.class);
		installFactory(EnrichmentMapBuildMapTaskFactory.Factory.class);
		installFactory(EnrichmentMapVisualStyle.Factory.class);
		installFactory(PostAnalysisVisualStyle.Factory.class);
		installFactory(BuildPostAnalysisActionListener.Factory.class);
		installFactory(CreatePostAnalysisVisualStyleTask.Factory.class);
		installFactory(BuildDiseaseSignatureTask.Factory.class);
		installFactory(LoadSignatureSetsActionListener.Factory.class);
		installFactory(CreateEnrichmentMapNetworkTask.Factory.class);
		installFactory(PostAnalysisKnownSignaturePanel.Factory.class);
		installFactory(PostAnalysisSignatureDiscoveryPanel.Factory.class);
		installFactory(EnrichmentMapParameters.Factory.class);
	}
	
	private void installFactory(Class<?> factoryInterface) {
		install(new FactoryModuleBuilder().build(factoryInterface));
	}
}
