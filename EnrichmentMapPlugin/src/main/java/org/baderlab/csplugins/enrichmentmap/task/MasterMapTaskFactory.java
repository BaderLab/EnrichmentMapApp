package org.baderlab.csplugins.enrichmentmap.task;

import java.util.List;
import java.util.Map;

import org.baderlab.csplugins.enrichmentmap.ApplicationModule.Headless;
import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.DataSet.Method;
import org.baderlab.csplugins.enrichmentmap.model.DataSetFiles;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GenesetSimilarity;
import org.baderlab.csplugins.enrichmentmap.model.Ranking;
import org.baderlab.csplugins.enrichmentmap.parsers.DetermineEnrichmentResultFileReader;
import org.baderlab.csplugins.enrichmentmap.parsers.ExpressionFileReaderTask;
import org.baderlab.csplugins.enrichmentmap.parsers.GMTFileReaderTask;
import org.baderlab.csplugins.enrichmentmap.parsers.RanksFileReaderTask;
import org.baderlab.csplugins.enrichmentmap.util.Baton;
import org.baderlab.csplugins.enrichmentmap.view.mastermap.DataSetParameters;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class MasterMapTaskFactory extends AbstractTaskFactory {

	@Inject private CyServiceRegistrar serviceRegistrar;
	@Inject private @Headless boolean headless;
	
	@Inject private MasterMapNetworkTask.Factory masterMapNetworkTaskFactory;
	@Inject private VisualizeMasterMapTask.Factory visualizeMasterMapTaskFactory;
	
	
	private final EMCreationParameters params;
	private final List<DataSetParameters> dataSets;
	
	
	public static interface Factory {
		MasterMapTaskFactory create(EMCreationParameters params, List<DataSetParameters> dataSets);
	}
	
	@Inject
	public MasterMapTaskFactory(@Assisted EMCreationParameters params, @Assisted List<DataSetParameters> dataSets) {
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
		
		// Load global GMT file into each dataset
		if(params.getGlobalGmtFile() != null) {
			tasks.append(new GMTFileReaderTask(map, params.getGlobalGmtFile().toString(), map.getGlobalGenesets()));
		}

		for(DataSetParameters dataSetParameters : dataSets) {
			String datasetName = dataSetParameters.getName();
			Method method = dataSetParameters.getMethod();
			DataSetFiles files = dataSetParameters.getFiles();
			
			DataSet dataset = map.createDataSet(datasetName, method, files);
			
			// Load GMT File
			if(!Strings.isNullOrEmpty(dataset.getSetofgenesets().getFilename())) {
				tasks.append(new GMTFileReaderTask(dataset));
			}
			
			// Load the enrichments 
			tasks.append(new DetermineEnrichmentResultFileReader(dataset).getParsers());

			// Load expression file if specified in the dataset.
			// If there is no expression file then create a dummy file to associate with this dataset so we can still use the expression viewer (heat map)
			if(Strings.isNullOrEmpty(dataset.getDatasetFiles().getExpressionFileName())) {
				tasks.append(new CreateDummyExpressionTask(dataset));
			} else {
				tasks.append(new ExpressionFileReaderTask(dataset));
			}
			
			// Load ranks if present
			// Note the ranks objects are initialized in EnrichmentMap.initializeFiles()
			
			// MKTODO I don't understand why the ranks are in a Map that uses the data set name as key
			// It might have something to do with the leading edge calculation in the heat map.
			String ranksName = dataset.getMethod() == Method.GSEA ? Ranking.GSEARanking : datasetName;
			if(dataset.getExpressionSets().getRanksByName(ranksName) != null) {
				String filename = dataset.getExpressionSets().getRanksByName(ranksName).getFilename();
				tasks.append(new RanksFileReaderTask(filename, dataset, ranksName, false));
			}
			
		}
		
		// NOTE: First filter out genesets that don't pass the thresholds, 
		// Then filter the remaining genesets of interest to only contain genes from the expression file.

		// Filter out genesets that don't pass the p-value and q-value thresholds
		InitializeGenesetsOfInterestTask genesetsTask = new InitializeGenesetsOfInterestTask(map);
		genesetsTask.setThrowIfMissing(false); // TEMPORARY
		tasks.append(genesetsTask);
		
		// Trim the genesets to only contain the genes that are in the data file.
		tasks.append(new FilterGenesetsByDatasetGenes(map));

		// Link the ComputeSimilarityTask to the MasterMapNetworkTask by a "pipe"
		Baton<Map<String,GenesetSimilarity>> pipe = new Baton<>();
		
		// Compute the geneset similarities
		tasks.append(new ComputeSimilarityTaskParallel(map, pipe.consumer()));

		// Create the network
		tasks.append(masterMapNetworkTaskFactory.create(map, pipe.supplier()));
		
		// Create style and layout
		if(!headless) {
			tasks.append(visualizeMasterMapTaskFactory.create(map));
		}
		
		return tasks;
	}
}
