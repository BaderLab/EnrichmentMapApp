package org.baderlab.csplugins.enrichmentmap;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.util.Properties;

import org.baderlab.csplugins.enrichmentmap.actions.LoadSignatureSetsActionListener;
import org.baderlab.csplugins.enrichmentmap.actions.OpenPathwayCommonsTask;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.task.ApplyEMStyleTask;
import org.baderlab.csplugins.enrichmentmap.task.CreateEMNetworkTask;
import org.baderlab.csplugins.enrichmentmap.task.CreateEMViewTask;
import org.baderlab.csplugins.enrichmentmap.task.CreateEnrichmentMapTaskFactory;
import org.baderlab.csplugins.enrichmentmap.task.FilterNodesEdgesTask;
import org.baderlab.csplugins.enrichmentmap.task.SelectNodesEdgesTask;
import org.baderlab.csplugins.enrichmentmap.task.postanalysis.CreateDiseaseSignatureNetworkTask;
import org.baderlab.csplugins.enrichmentmap.task.postanalysis.CreateDiseaseSignatureTaskFactory;
import org.baderlab.csplugins.enrichmentmap.task.postanalysis.CreateDiseaseSignatureTaskParallel;
import org.baderlab.csplugins.enrichmentmap.task.postanalysis.RemoveSignatureDataSetsTask;
import org.baderlab.csplugins.enrichmentmap.view.creation.DetailDataSetPanel;
import org.baderlab.csplugins.enrichmentmap.view.creation.ErrorMessageDialog;
import org.baderlab.csplugins.enrichmentmap.view.creation.PathTextField;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.AddRanksDialog;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.ClusterRankingOption;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.ExportPDFAction;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.ExportTXTAction;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapMainPanel;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParentPanel;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.ColumnHeaderRankOptionRenderer;
import org.baderlab.csplugins.enrichmentmap.view.postanalysis.PostAnalysisInputPanel;
import org.baderlab.csplugins.enrichmentmap.view.postanalysis.PostAnalysisKnownSignaturePanel;
import org.baderlab.csplugins.enrichmentmap.view.postanalysis.PostAnalysisSignatureDiscoveryPanel;
import org.baderlab.csplugins.enrichmentmap.view.postanalysis2.PADialogPage;
import org.baderlab.csplugins.enrichmentmap.view.postanalysis2.PADialogParameters;
import org.baderlab.csplugins.enrichmentmap.view.postanalysis2.PAWeightPanel;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.property.CyProperty;
import org.osgi.framework.BundleContext;

import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
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
		
		// Set up CyProperty
		bind(new TypeLiteral<CyProperty<Properties>>(){}).toInstance(new PropsReader());
		bind(PropertyManager.class).asEagerSingleton();
	}
	
	/** For tests */
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
		installFactory(CreateDiseaseSignatureNetworkTask.Factory.class);
		installFactory(CreateDiseaseSignatureTaskParallel.Factory.class);
		installFactory(RemoveSignatureDataSetsTask.Factory.class);
		installFactory(LoadSignatureSetsActionListener.Factory.class);
		installFactory(PostAnalysisKnownSignaturePanel.Factory.class);
		installFactory(PostAnalysisSignatureDiscoveryPanel.Factory.class);
		installFactory(EnrichmentMapParameters.Factory.class);
		installFactory(CreateEnrichmentMapTaskFactory.Factory.class);
		installFactory(CreateEMNetworkTask.Factory.class);
		installFactory(CreateEMViewTask.Factory.class);
		installFactory(ApplyEMStyleTask.Factory.class);
		installFactory(FilterNodesEdgesTask.Factory.class);
		installFactory(SelectNodesEdgesTask.Factory.class);
		installFactory(ClusterRankingOption.Factory.class);
		installFactory(HeatMapParentPanel.Factory.class);
		installFactory(HeatMapMainPanel.Factory.class);
		installFactory(ExportPDFAction.Factory.class);
		installFactory(ExportTXTAction.Factory.class);
		installFactory(AddRanksDialog.Factory.class);
		installFactory(DetailDataSetPanel.Factory.class);
		installFactory(ErrorMessageDialog.Factory.class);
		installFactory(ColumnHeaderRankOptionRenderer.Factory.class);
		installFactory(PathTextField.Factory.class);
		installFactory(CreateDiseaseSignatureTaskFactory.Factory.class);
		installFactory(OpenPathwayCommonsTask.Factory.class);
		installFactory(PADialogParameters.Factory.class);
		installFactory(PADialogPage.Factory.class);
		installFactory(PAWeightPanel.Factory.class);
	}
	
	private void installFactory(Class<?> factoryInterface) {
		install(new FactoryModuleBuilder().build(factoryInterface));
	}
}
