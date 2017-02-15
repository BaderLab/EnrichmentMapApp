package org.baderlab.csplugins.enrichmentmap;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import org.baderlab.csplugins.enrichmentmap.actions.LoadSignatureSetsActionListener;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.task.ApplyEMStyleTask;
import org.baderlab.csplugins.enrichmentmap.task.CreateDiseaseSignatureTask;
import org.baderlab.csplugins.enrichmentmap.task.CreateEMNetworkTask;
import org.baderlab.csplugins.enrichmentmap.task.CreateEMViewTask;
import org.baderlab.csplugins.enrichmentmap.task.CreateEnrichmentMapTaskFactory;
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
		installFactory(PostAnalysisInputPanel.Factory.class);
		installFactory(CreateDiseaseSignatureTask.Factory.class);
		installFactory(LoadSignatureSetsActionListener.Factory.class);
		installFactory(PostAnalysisKnownSignaturePanel.Factory.class);
		installFactory(PostAnalysisSignatureDiscoveryPanel.Factory.class);
		installFactory(EnrichmentMapParameters.Factory.class);
//		installFactory(UpdateHeatMapTask.Factory.class);
		installFactory(CreateEnrichmentMapTaskFactory.Factory.class);
		installFactory(CreateEMNetworkTask.Factory.class);
		installFactory(CreateEMViewTask.Factory.class);
		installFactory(ApplyEMStyleTask.Factory.class);
	}
	
	private void installFactory(Class<?> factoryInterface) {
		install(new FactoryModuleBuilder().build(factoryInterface));
	}
}
