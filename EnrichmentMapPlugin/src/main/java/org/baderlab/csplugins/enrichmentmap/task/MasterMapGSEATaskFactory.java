package org.baderlab.csplugins.enrichmentmap.task;

import java.util.List;

import org.baderlab.csplugins.enrichmentmap.ApplicationModule.Headless;
import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.DataSetFiles;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.LegacySupport;
import org.baderlab.csplugins.enrichmentmap.parsers.GMTFileReaderTask;
import org.baderlab.csplugins.enrichmentmap.parsers.ParseEDBEnrichmentResults;
import org.baderlab.csplugins.enrichmentmap.view.mastermap.DataSetParameters;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class MasterMapGSEATaskFactory extends AbstractTaskFactory {

	@Inject private CyServiceRegistrar serviceRegistrar;
	
	@Inject private LegacySupport legacySupport;
	@Inject private @Headless boolean headless;
	
	@Inject private MasterMapNetworkTask.Factory masterMapNetworkTaskFactory;
	@Inject private VisualizeMasterMapTask.Factory visualizeMasterMapTaskFactory;
	
	
	private final EMCreationParameters params;
	private final List<DataSetParameters> dataSets;
	
	
	public static interface Factory {
		MasterMapGSEATaskFactory create(EMCreationParameters params, List<DataSetParameters> dataSets);
	}
	
	@Inject
	public MasterMapGSEATaskFactory(@Assisted EMCreationParameters params, @Assisted List<DataSetParameters> dataSets) {
		this.dataSets = dataSets;
		this.params = params;
	}
	
	
	@Override
	public TaskIterator createTaskIterator() {
		TaskIterator tasks = new TaskIterator();
		if(dataSets.isEmpty())
			return tasks;
		
		tasks.append(new TitleTask("Building EnrichmentMap"));
		
		EnrichmentMap map = new EnrichmentMap(params, serviceRegistrar);
		
		for(DataSetParameters dataSetParameters : dataSets) {
			DataSetFiles files = dataSetParameters.getFiles();
			String datasetName = dataSetParameters.getName();
			DataSet dataset = new DataSet(map, datasetName, dataSetParameters.getMethod(), files);
			map.addDataSet(datasetName, dataset);
			
			// Load GMT File
			GMTFileReaderTask gmtTask = new GMTFileReaderTask(dataset);
			tasks.append(gmtTask);
			
			// Load Enrichment Results from EDB file
			ParseEDBEnrichmentResults edbTask = new ParseEDBEnrichmentResults(dataset);
			tasks.append(edbTask);
			
			// Load expression data or ranks...
			// MKTODO we don't have expression data per-se
			// Use Dummy task?
			// Load ranks as usual, but what does that do without expressions?
		}
		
		// trim the genesets to only contain the genes that are in the data file.
		// Not sure if this is even necessary for the GMT file that's in the gsea edb directory
		FilterGenesetsByDatasetGenes filterTask = new FilterGenesetsByDatasetGenes(map);
		tasks.append(filterTask);

		// Filter out genesets that don't pass the p-value and q-value thresholds
		InitializeGenesetsOfInterestTask genesetsTask = new InitializeGenesetsOfInterestTask(map);
		genesetsTask.setThrowIfMissing(false); // TEMPORARY
		tasks.append(genesetsTask);

		// compute the geneset similarities
		ComputeSimilarityTaskParallel similarityTask = new ComputeSimilarityTaskParallel(map);
		tasks.append(similarityTask);

		// create the network
		MasterMapNetworkTask networkTask = masterMapNetworkTaskFactory.create(map);
		tasks.append(networkTask);
		
		if(!headless) {
			VisualizeMasterMapTask visualizeTask = visualizeMasterMapTaskFactory.create(map);
			tasks.append(visualizeTask);
		}
//
//		// don't visualize the map if running headless
//		if(swingApplication != null) {
//			ParametersPanel paramsPanel = parametersPanelProvider.get();
//			ShowPanelTask show_parameters_panel = new ShowPanelTask(swingApplication, paramsPanel);
//			currentTasks.append(show_parameters_panel);
//			
//			//visualize Network
//			VisualizeEnrichmentMapTask map_viz = visualizeEnrichmentMapTaskFactory.create(map);
//			currentTasks.append(map_viz);
//		}
//
//		return currentTasks;
		
		return tasks;
	}
}
