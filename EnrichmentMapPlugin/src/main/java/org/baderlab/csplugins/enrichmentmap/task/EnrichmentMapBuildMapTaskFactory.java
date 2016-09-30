package org.baderlab.csplugins.enrichmentmap.task;

import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.Nullable;

import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.model.Ranking;
import org.baderlab.csplugins.enrichmentmap.parsers.DetermineEnrichmentResultFileReader;
import org.baderlab.csplugins.enrichmentmap.parsers.ExpressionFileReaderTask;
import org.baderlab.csplugins.enrichmentmap.parsers.GMTFileReaderTask;
import org.baderlab.csplugins.enrichmentmap.parsers.RanksFileReaderTask;
import org.baderlab.csplugins.enrichmentmap.view.ParametersPanel;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class EnrichmentMapBuildMapTaskFactory implements TaskFactory {

	@Inject private VisualizeEnrichmentMapTask.Factory visualizeEnrichmentMapTaskFactory;
	@Inject private CreateEnrichmentMapNetworkTask.Factory createEnrichmentMapNetworkTaskFactory;
	@Inject private EnrichmentMapManager emManager;
	@Inject private @Nullable CySwingApplication swingApplication;
	

	private EnrichmentMap map;
	
	public interface Factory {
		EnrichmentMapBuildMapTaskFactory create(EnrichmentMap map);
	}
	
	@Inject
	public EnrichmentMapBuildMapTaskFactory(@Assisted EnrichmentMap map) {
		this.map = map;
	}

	
	public TaskIterator createTaskIterator() {
		TitleTask dummyTaskToSetTitle = new TitleTask("Building Enrichment Map");

		//initialize with 8 tasks so the progress bar can be set better.
		TaskIterator currentTasks = new TaskIterator(8, dummyTaskToSetTitle);

		Map<String, DataSet> datasets = map.getDatasets();

		//Make sure that Dataset 1 gets parsed first because if there are 2 datasets
		//the geneset file is only associated with the first dataset.
		SortedSet<String> dataset_names = new TreeSet<>(datasets.keySet());

		for(String dataset_name : dataset_names) {
			DataSet dataset = datasets.get(dataset_name);
			
			//first step: load GMT file if a file is specified in this dataset    		
			if(!Strings.isNullOrEmpty(dataset.getSetofgenesets().getFilename())) {
				//Load the geneset file
				GMTFileReaderTask gmtFileTask = new GMTFileReaderTask(dataset);
				currentTasks.append(gmtFileTask);
			}

			//second step: load the enrichments 
			DetermineEnrichmentResultFileReader enrichmentResultsFilesTask = new DetermineEnrichmentResultFileReader(dataset);
			currentTasks.append(enrichmentResultsFilesTask.getParsers());

			//third step: load expression file if specified in the dataset.
			//if there is no expression file then create a dummy file to associate with 
			//this dataset so we can still use the expression viewer
			if(Strings.isNullOrEmpty(dataset.getDatasetFiles().getExpressionFileName())) {
				CreateDummyExpressionTask dummyExpressionTask = new CreateDummyExpressionTask(dataset);
				currentTasks.append(dummyExpressionTask);
			} else {
				ExpressionFileReaderTask expressionFileTask = new ExpressionFileReaderTask(dataset);
				currentTasks.append(expressionFileTask);
			}
			
			//fourth step: Load ranks
			//check to see if we have ranking files
			if(dataset.getMap().getParams().getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_GSEA)) {
				if(dataset.getExpressionSets().getRanksByName(Ranking.GSEARanking) != null) {
					RanksFileReaderTask ranking1 = new RanksFileReaderTask(
							dataset.getExpressionSets().getRanksByName(Ranking.GSEARanking).getFilename(), dataset, Ranking.GSEARanking, false);
					currentTasks.append(ranking1);
				}
			} else {
				if(dataset.getExpressionSets().getRanksByName(EnrichmentMap.DATASET1) != null) {
					RanksFileReaderTask ranking1 = new RanksFileReaderTask(
							dataset.getExpressionSets().getRanksByName(EnrichmentMap.DATASET1).getFilename(), dataset, EnrichmentMap.DATASET1, false);
					currentTasks.append(ranking1);
				}
				if(dataset.getExpressionSets().getRanksByName(EnrichmentMap.DATASET2) != null) {
					RanksFileReaderTask ranking1 = new RanksFileReaderTask(
							dataset.getExpressionSets().getRanksByName(EnrichmentMap.DATASET2).getFilename(), dataset, EnrichmentMap.DATASET2, false);
					currentTasks.append(ranking1);
				}
			}
		}

		//trim the genesets to only contain the genes that are in the data file.
		FilterGenesetsByDatasetGenes filter = new FilterGenesetsByDatasetGenes(map);
		currentTasks.append(filter);

		//Initialize the set of genesets and GSEA results that we want to compute over
		InitializeGenesetsOfInterestTask genesets_init = new InitializeGenesetsOfInterestTask(map);
		currentTasks.append(genesets_init);

		//compute the geneset similarities
		ComputeSimilarityTask similarities = new ComputeSimilarityTask(map);
		currentTasks.append(similarities);

		//build the resulting map
		CreateEnrichmentMapNetworkTask create_map = createEnrichmentMapNetworkTaskFactory.create(map);
		currentTasks.append(create_map);

		// don't visualize the map if running headless
		if(swingApplication != null) {
			ParametersPanel paramsPanel = emManager.getParameterPanel();
			ShowPanelTask show_parameters_panel = new ShowPanelTask(swingApplication, paramsPanel);
			currentTasks.append(show_parameters_panel);
			
			//visualize Network
			VisualizeEnrichmentMapTask map_viz = visualizeEnrichmentMapTaskFactory.create(map);
			currentTasks.append(map_viz);
		}

		return currentTasks;
	}

	
	public boolean isReady() {
		return map != null && !map.getDatasets().isEmpty();
	}

}
