package org.baderlab.csplugins.enrichmentmap.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.baderlab.csplugins.enrichmentmap.PropertyManager;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.EdgeStrategy;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.SimilarityMetric;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResultFilterParams.NESFilter;
import org.baderlab.csplugins.enrichmentmap.model.LegacySupport;
import org.baderlab.csplugins.enrichmentmap.resolver.DataSetParameters;
import org.baderlab.csplugins.enrichmentmap.resolver.ResolverTask;
import org.baderlab.csplugins.enrichmentmap.task.CreateEnrichmentMapTaskFactory;
import org.baderlab.csplugins.enrichmentmap.util.NullTaskMonitor;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

import com.google.inject.Inject;

public class ResolverCommandTask extends AbstractTask {

	@Tunable(required=true)
	public File rootFolder;
	
	@Tunable
	public File commonGMTFile;
	
	@Tunable
	public File commonExpressionFile;
	
	@Tunable
	public ListSingleSelection<String> edgeStrategy;
	
	// Parameter Tuneables
	@Tunable
	public Double pvalue = 0.005;

	@Tunable
	public Double qvalue = 0.1;
	
	@Tunable
	public ListSingleSelection<String> nesFilter;
	
	@Tunable
	public Integer minExperiments = null;
	
	@Tunable
	public boolean filterByExpressions = false;
	
	@Tunable
	public Double similarityCutoff = LegacySupport.overlapCutOff_default;

	@Tunable
	public ListSingleSelection<String> similarityMetric;

	@Tunable
	public double combinedConstant = LegacySupport.combinedConstant_default;
	
	@Tunable
	public String networkName = null;
	
	
	@Inject private SynchronousTaskManager<?> taskManager;
	@Inject private LegacySupport legacySupport;
	@Inject private CreateEnrichmentMapTaskFactory.Factory taskFactoryFactory;
	
	
	@Inject
	public ResolverCommandTask(PropertyManager propertyManager) {
		similarityMetric = enumNames(SimilarityMetric.values());
		similarityMetric.setSelectedValue(LegacySupport.similarityMetric_default.name());
		
		edgeStrategy = enumNames(EdgeStrategy.values());
		edgeStrategy.setSelectedValue(EdgeStrategy.AUTOMATIC.name());
		
		nesFilter = enumNames(NESFilter.values());
		nesFilter.setSelectedValue(NESFilter.ALL.name());
	}
	
	
	@Override
	public void run(TaskMonitor tm) throws Exception {
		if(tm == null)
			tm = new NullTaskMonitor();
		
		tm.setStatusMessage("Running EnrichmentMap Data Set Resolver Task");
		
		EdgeStrategy strategy;
		try {
			strategy = EdgeStrategy.valueOf(edgeStrategy.getSelectedValue().toUpperCase());
		} catch(IllegalArgumentException e) {
			throw new IllegalArgumentException("edgeStrategy is invalid: '" + edgeStrategy.getSelectedValue() + "'");
		}
		
		SimilarityMetric sm;
		try {
			sm = SimilarityMetric.valueOf(similarityMetric.getSelectedValue().toUpperCase());
		} catch(IllegalArgumentException e) {
			throw new IllegalArgumentException("similarityMetric is invalid: '" + similarityMetric.getSelectedValue() + "'");
		}
		
		NESFilter nesf;
		try {
			nesf = NESFilter.valueOf(nesFilter.getSelectedValue().toUpperCase());
		} catch(IllegalArgumentException e) {
			throw new IllegalArgumentException("nesFilter is invalid: '" + nesFilter.getSelectedValue() + "'");
		}
		
		if(rootFolder == null || !rootFolder.exists()) {
			throw new IllegalArgumentException("rootFolder is invalid: " + rootFolder);
		}
		
		// Scan root folder (note: throws exception if no data sets were found)
		ResolverTask resolverTask = new ResolverTask(rootFolder);
		taskManager.execute(new TaskIterator(resolverTask)); // blocks
		List<DataSetParameters> dataSets = resolverTask.getDataSetResults();
		
		tm.setStatusMessage("resolved " + dataSets.size() + " data sets");
		for(DataSetParameters params : dataSets) {
			tm.setStatusMessage(params.toString());
		}
		
		// Common gmt and expression files
		// Overwrite all the expression files if the common file has been provided
		if(commonExpressionFile != null) {
			if(!commonExpressionFile.canRead()) {
				throw new IllegalArgumentException("Cannot read commonExpressionFile: " + commonExpressionFile);
			}
			for(DataSetParameters dsp : dataSets) {
				dsp.getFiles().setExpressionFileName(commonExpressionFile.getAbsolutePath());
			}
		}
		
		// Overwrite all the gmt files if a common file has been provided
		if(commonGMTFile != null) {
			if(!commonGMTFile.canRead()) {
				throw new IllegalArgumentException("Cannot read commonGMTFile: " + commonGMTFile);
			}
			for(DataSetParameters dsp : dataSets) {
				dsp.getFiles().setGMTFileName(commonGMTFile.getAbsolutePath());
			}
		}
		
		// Create Enrichment Map
		String prefix = legacySupport.getNextAttributePrefix();
		String info = String.format(
			"prefix:%s, pvalue:%f, qvalue:%f, nesFilter:%s, minExperiments:%d, similarityMetric:%s, similarityCutoff:%f, combinedConstant:%f", 
			prefix, pvalue, qvalue, nesf, minExperiments, sm, similarityCutoff, combinedConstant);
		tm.setStatusMessage(info);
		
		EMCreationParameters params = 
				new EMCreationParameters(prefix, pvalue, qvalue, nesf, Optional.ofNullable(minExperiments), filterByExpressions,
										 sm, similarityCutoff, combinedConstant, strategy);

		if(networkName != null && !networkName.trim().isEmpty()) {
			params.setNetworkName(networkName);
		}
		
		CreateEnrichmentMapTaskFactory taskFactory = taskFactoryFactory.create(params, dataSets);
		TaskIterator tasks = taskFactory.createTaskIterator();
		taskManager.execute(tasks);
		
		tm.setStatusMessage("Done.");
	}
	
	
	public static ListSingleSelection<String> enumNames(Enum<?>[] values) {
		List<String> names = new ArrayList<>(values.length);
		for(Enum<?> value : values) {
			names.add(value.name());
		}
		return new ListSingleSelection<>(names);
	}

}
