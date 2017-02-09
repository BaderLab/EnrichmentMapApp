package org.baderlab.csplugins.enrichmentmap;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import org.baderlab.csplugins.enrichmentmap.actions.BuildPostAnalysisActionListener;
import org.baderlab.csplugins.enrichmentmap.actions.LoadSignatureSetsActionListener;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.style.EnrichmentMapVisualStyle;
import org.baderlab.csplugins.enrichmentmap.style.LegacyPostAnalysisVisualStyle;
import org.baderlab.csplugins.enrichmentmap.style.MasterMapVisualStyleTask;
import org.baderlab.csplugins.enrichmentmap.task.BuildDiseaseSignatureTask;
import org.baderlab.csplugins.enrichmentmap.task.CreatePostAnalysisVisualStyleTask;
import org.baderlab.csplugins.enrichmentmap.task.MasterMapNetworkTask;
import org.baderlab.csplugins.enrichmentmap.task.MasterMapTaskFactory;
import org.baderlab.csplugins.enrichmentmap.task.TogglePublicationVisualStyleTask;
import org.baderlab.csplugins.enrichmentmap.task.VisualizeMasterMapTask;
import org.baderlab.csplugins.enrichmentmap.view.postanalysis.PostAnalysisInputPanel;
import org.baderlab.csplugins.enrichmentmap.view.postanalysis.PostAnalysisKnownSignaturePanel;
import org.baderlab.csplugins.enrichmentmap.view.postanalysis.PostAnalysisSignatureDiscoveryPanel;
import org.cytoscape.application.swing.CySwingApplication;
import org.osgi.framework.BundleContext;

import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 * Guice module
 */
public class ApplicationModule extends AbstractModule {

	@BindingAnnotation @Retention(RUNTIME) public @interface Nodes {}
	@BindingAnnotation @Retention(RUNTIME) public @interface Edges {}
	
	@BindingAnnotation @Retention(RUNTIME) public @interface Headless {}
	
	@Override
	protected void configure() {
		bind(EnrichmentMapManager.class).asEagerSingleton();		
		install(new FactoryModule());
	}
	
	public static Module createFactoryModule() {
		return new FactoryModule();
	}
	
	@Provides @Headless
	public Boolean provideHeadlessFlag(BundleContext bc) {
		return bc.getServiceReference(CySwingApplication.class.getName()) == null;
	}
	
}


// This is a separate class so it can be used by the integration tests
class FactoryModule extends AbstractModule {
	
	@Override
	protected void configure() {
		// Factories using AssistedInject
		installFactory(EnrichmentMapVisualStyle.Factory.class);
		installFactory(LegacyPostAnalysisVisualStyle.Factory.class);
		installFactory(PostAnalysisInputPanel.Factory.class);
		installFactory(BuildPostAnalysisActionListener.Factory.class);
		installFactory(CreatePostAnalysisVisualStyleTask.Factory.class);
		installFactory(TogglePublicationVisualStyleTask.Factory.class);
		installFactory(BuildDiseaseSignatureTask.Factory.class);
		installFactory(LoadSignatureSetsActionListener.Factory.class);
		installFactory(PostAnalysisKnownSignaturePanel.Factory.class);
		installFactory(PostAnalysisSignatureDiscoveryPanel.Factory.class);
		installFactory(EnrichmentMapParameters.Factory.class);
//		installFactory(UpdateHeatMapTask.Factory.class);
		installFactory(MasterMapTaskFactory.Factory.class);
		installFactory(MasterMapNetworkTask.Factory.class);
		installFactory(VisualizeMasterMapTask.Factory.class);
		installFactory(MasterMapVisualStyleTask.Factory.class);
	}
	
	private void installFactory(Class<?> factoryInterface) {
		install(new FactoryModuleBuilder().build(factoryInterface));
	}
}
