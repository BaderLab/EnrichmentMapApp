package org.baderlab.csplugins.enrichmentmap.task;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.baderlab.csplugins.enrichmentmap.ApplicationModule.Headless;
import org.baderlab.csplugins.enrichmentmap.model.DataSetFiles;
import org.baderlab.csplugins.enrichmentmap.model.DataSetParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet.Method;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GenesetSimilarity;
import org.baderlab.csplugins.enrichmentmap.model.Ranking;
import org.baderlab.csplugins.enrichmentmap.model.SimilarityKey;
import org.baderlab.csplugins.enrichmentmap.model.TableParameters;
import org.baderlab.csplugins.enrichmentmap.parsers.ClassFileReaderTask;
import org.baderlab.csplugins.enrichmentmap.parsers.ExpressionFileReaderTask;
import org.baderlab.csplugins.enrichmentmap.parsers.GMTFileReaderTask;
import org.baderlab.csplugins.enrichmentmap.parsers.GREATWhichPvalueQuestionTask;
import org.baderlab.csplugins.enrichmentmap.parsers.LoadEnrichmentsFromTableTask;
import org.baderlab.csplugins.enrichmentmap.parsers.ParseBingoEnrichmentResults;
import org.baderlab.csplugins.enrichmentmap.parsers.ParseDavidEnrichmentResults;
import org.baderlab.csplugins.enrichmentmap.parsers.ParseEDBEnrichmentResults;
import org.baderlab.csplugins.enrichmentmap.parsers.ParseGREATEnrichmentResults;
import org.baderlab.csplugins.enrichmentmap.parsers.ParseGSEAEnrichmentResults;
import org.baderlab.csplugins.enrichmentmap.parsers.ParseGenericEnrichmentResults;
import org.baderlab.csplugins.enrichmentmap.parsers.RanksFileReaderTask;
import org.baderlab.csplugins.enrichmentmap.resolver.DataSetResolver;
import org.baderlab.csplugins.enrichmentmap.util.Baton;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class CreateEnrichmentMapTaskFactory extends AbstractTaskFactory {

	@Inject private CyServiceRegistrar serviceRegistrar;
	@Inject private @Headless boolean headless;
	
	@Inject private CreateEMNetworkTask.Factory createEMNetworkTaskFactory;
	@Inject private CreateEMViewTask.Factory createEMViewTaskFactory;
	
	private final EMCreationParameters params;
	private final List<DataSetParameters> dataSets;
	
	public static interface Factory {
		CreateEnrichmentMapTaskFactory create(EMCreationParameters params, List<DataSetParameters> dataSets);
	}
	
	@Inject
	public CreateEnrichmentMapTaskFactory(@Assisted EMCreationParameters params, @Assisted List<DataSetParameters> dataSets) {
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
		createTasks(map, tasks);
		
		return tasks;
	}
	
	
	private void createTasks(EnrichmentMap map, TaskIterator tasks) {
		for(DataSetParameters dataSetParameters : dataSets) {
			String datasetName = dataSetParameters.getName();
			Method method = dataSetParameters.getMethod();
			
			// Standard approach, load data from files
			DataSetFiles files = dataSetParameters.getFiles();
			EMDataSet dataset = map.createDataSet(datasetName, method, files);
			
			// Load GMT File
			if(!Strings.isNullOrEmpty(dataset.getDataSetFiles().getGMTFileName())) {
				tasks.append(new GMTFileReaderTask(dataset));
			}
			
			// Load the enrichments 
			if(dataSetParameters.getTableParams().isPresent()) {
				TableParameters tableParams = dataSetParameters.getTableParams().get();
				tasks.append(new LoadEnrichmentsFromTableTask(tableParams, dataset));
			} else {
				tasks.append(getEnrichmentParserTasks(dataset));
			}

			// Load expression file if specified in the dataset.
			// If there is no expression file then create a dummy file to associate with this dataset so we can still use the expression viewer (heat map)
			if(Strings.isNullOrEmpty(dataset.getDataSetFiles().getExpressionFileName())) {
				tasks.append(new CreateDummyExpressionTask(dataset));
			} else {
				tasks.append(new ExpressionFileReaderTask(dataset));
			}
			
			// Load ranks if present
			String ranksName = dataset.getMethod() == Method.GSEA ? Ranking.GSEARanking : datasetName;
			if(dataset.getRanksByName(ranksName) != null) {
				String filename = files.getRankedFile();
				tasks.append(new RanksFileReaderTask(filename, dataset, ranksName, false));
			}
			
			if(!Strings.isNullOrEmpty(dataset.getDataSetFiles().getClassFile())) {
				tasks.append(new ClassFileReaderTask(dataset));
			}
		}
		
		// Filter out genesets that don't pass the p-value and q-value thresholds
		InitializeGenesetsOfInterestTask genesetsTask = new InitializeGenesetsOfInterestTask(map);
//		genesetsTask.setThrowIfMissing(false); // TEMPORARY
		tasks.append(genesetsTask);
		
		// Trim the genesets to only contain the genes that are in the data file.
		tasks.append(new FilterGenesetsByDatasetGenes(map));

		// Link the ComputeSimilarityTask to the MasterMapNetworkTask by a "pipe"
		Baton<Map<SimilarityKey,GenesetSimilarity>> pipe = new Baton<>();
		
		// Compute the geneset similarities
		tasks.append(new ComputeSimilarityTaskParallel(map, pipe.consumer()));

		// Create the network
		tasks.append(createEMNetworkTaskFactory.create(map, pipe.supplier()));
		
		// Make any final adjustments to the model
		tasks.append(new ModelCleanupTask(map));
		
		// Create style and layout
		if(!headless) {
			tasks.append(createEMViewTaskFactory.create(map));
		}
	}
	
	
	/**
	 * Parse Enrichment results file
	 */
	private static TaskIterator getEnrichmentParserTasks(EMDataSet dataset) {
		String enrichmentsFileName1 = dataset.getDataSetFiles().getEnrichmentFileName1();
		String enrichmentsFileName2 = dataset.getDataSetFiles().getEnrichmentFileName2();
		
		TaskIterator parserTasks = new TaskIterator();
		
		try {
			if(!Strings.isNullOrEmpty(enrichmentsFileName1)) {
				AbstractTask current = readFile(dataset, enrichmentsFileName1);
				if(current instanceof ParseGREATEnrichmentResults)
					parserTasks.append(new GREATWhichPvalueQuestionTask(dataset.getMap()));
				parserTasks.append(current);
			}
			
			if(!Strings.isNullOrEmpty(enrichmentsFileName2)) {
				parserTasks.append(readFile(dataset, enrichmentsFileName2));
			}
			
			//If both of the enrichment files are null then we want to default to building a gmt file only build
			if(Strings.isNullOrEmpty(enrichmentsFileName1) && Strings.isNullOrEmpty(enrichmentsFileName2)) {
				parserTasks.append(new CreateGMTEnrichmentMapTask(dataset));
			}
			
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		return parserTasks;
	}


	private static AbstractTask readFile(EMDataSet dataset, String fileName) throws IOException {
		if(fileName.endsWith(".edb")) {
			return new ParseEDBEnrichmentResults(dataset);
		}
		else {
			DataSetResolver.Type fileType = DataSetResolver.guessEnrichmentType(fileName);
			switch(fileType) {
				case ENRICHMENT_GSEA:  return new ParseGSEAEnrichmentResults(dataset);
				case ENRICHMENT_BINGO: return new ParseBingoEnrichmentResults(dataset);
				case ENRICHMENT_GREAT: return new ParseGREATEnrichmentResults(dataset);
				case ENRICHMENT_DAVID: return new ParseDavidEnrichmentResults(dataset);
				default:               return new ParseGenericEnrichmentResults(dataset);
			}
		}
	}
	
}
