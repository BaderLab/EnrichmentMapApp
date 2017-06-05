package org.baderlab.csplugins.enrichmentmap.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.baderlab.csplugins.enrichmentmap.PropertyManager;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.SimilarityMetric;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResultFilterParams.NESFilter;
import org.baderlab.csplugins.enrichmentmap.model.LegacySupport;
import org.baderlab.csplugins.enrichmentmap.resolver.DataSetParameters;
import org.baderlab.csplugins.enrichmentmap.resolver.ResolverTask;
import org.baderlab.csplugins.enrichmentmap.task.CreateEnrichmentMapTaskFactory;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

import com.google.inject.Inject;

public class ResolverCommandTask extends AbstractTask {

	@Tunable
	public File rootFolder;
	
	@Tunable
	public File commonGMTFile;
	
	@Tunable
	public File commonExpressionFile;
	
	@Tunable
	public boolean distinctEdges = false;
	
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
	public Double similarityCutoff = 0.25;

	@Tunable
	public ListSingleSelection<String> similarityMetric;

	@Tunable
	public double combinedConstant = 0.5;
	
	
	
	private static final Logger logger = Logger.getLogger(CyUserLog.NAME);

	@Inject private SynchronousTaskManager<?> taskManager;
	@Inject private LegacySupport legacySupport;
	@Inject private CreateEnrichmentMapTaskFactory.Factory taskFactoryFactory;
	
	
	@Inject
	public ResolverCommandTask(PropertyManager propertyManager) {
		SimilarityMetric defaultMetric = propertyManager.getSimilarityMetric();
		similarityMetric = enumNames(SimilarityMetric.values());
		similarityMetric.setSelectedValue(defaultMetric.name());
		
		similarityCutoff = propertyManager.getDefaultCutOff(defaultMetric);
		
		nesFilter = enumNames(NESFilter.values());
		nesFilter.setSelectedValue(NESFilter.ALL.name());
	}
	
	
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		logger.info("Running EnrichmentMap Data Set Resolver Task");
		
		// Scan root folder (note: throws exception if no data sets were found)
		ResolverTask resolverTask = new ResolverTask(rootFolder);
		taskManager.execute(new TaskIterator(resolverTask)); // blocks
		List<DataSetParameters> dataSets = resolverTask.getDataSetResults();
		
		logger.info("resolved " + dataSets.size() + " data sets");
		dataSets.forEach(params -> logger.info(params.toString()));
		
		
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
		SimilarityMetric sm = SimilarityMetric.valueOf(similarityMetric.getSelectedValue());
		NESFilter nesf = NESFilter.valueOf(nesFilter.getSelectedValue());
		
		String info = String.format(
			"prefix:%s, pvalue:%f, qvalue:%f, nesFilter:%s, minExperiments:%d, similarityMetric:%s, similarityCutoff:%f, combinedConstant:%f", 
			prefix, pvalue, qvalue, nesf, minExperiments, sm, similarityCutoff, combinedConstant);
		logger.info(info);
		
		EMCreationParameters params = new EMCreationParameters(prefix, pvalue, qvalue, nesf, Optional.ofNullable(minExperiments), sm, similarityCutoff, combinedConstant);
		params.setCreateDistinctEdges(distinctEdges);

		CreateEnrichmentMapTaskFactory taskFactory = taskFactoryFactory.create(params, dataSets);
		TaskIterator tasks = taskFactory.createTaskIterator();
		taskManager.execute(tasks);
		
		logger.info("Done.");
	}
	
	
	public static ListSingleSelection<String> enumNames(Enum<?>[] values) {
		List<String> names = new ArrayList<>(values.length);
		for(Enum<?> value : values) {
			names.add(value.name());
		}
		return new ListSingleSelection<>(names);
	}

}
