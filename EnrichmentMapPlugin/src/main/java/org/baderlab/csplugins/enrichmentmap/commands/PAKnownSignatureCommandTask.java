package org.baderlab.csplugins.enrichmentmap.commands;

import static org.baderlab.csplugins.enrichmentmap.commands.ResolverCommandTask.enumNames;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.baderlab.csplugins.enrichmentmap.actions.LoadSignatureSetsActionListener;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.model.PostAnalysisFilterParameters;
import org.baderlab.csplugins.enrichmentmap.model.PostAnalysisFilterType;
import org.baderlab.csplugins.enrichmentmap.model.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.model.PostAnalysisParameters.UniverseType;
import org.baderlab.csplugins.enrichmentmap.model.SetOfGeneSets;
import org.baderlab.csplugins.enrichmentmap.task.postanalysis.CreateDiseaseSignatureTaskFactory;
import org.baderlab.csplugins.enrichmentmap.task.postanalysis.FilterMetric;
import org.baderlab.csplugins.enrichmentmap.view.control.ControlPanelMediator;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class PAKnownSignatureCommandTask extends AbstractTask {

	
	@Tunable
	public File gmtFile;
	
	@Tunable
	public ListSingleSelection<String> filterType;
	
	@Tunable
	public double cutoff = 0.5;
	
	@Tunable
	public ListSingleSelection<String> hypergeomUniverseType;
	
	@Tunable
	public int userDefinedUniverseSize = 0;
	
	@Tunable
	public String name;
	
	@Tunable
	public CyNetwork network;
	
	@Tunable(description="Name of data set to run PA against, or \"ALL\" to run in batch mode against all data sets.")
	public String dataSetName = "ALL";
	
	@Tunable(description=MannWhitRanks.DESCRIPTION)
	public MannWhitRanks mannWhitRanks = new MannWhitRanks();
	
	
	@Inject private CyApplicationManager applicationManager;
	@Inject private CyNetworkViewManager networkViewManager;
	
	@Inject private LoadSignatureSetsActionListener.Factory loadSignatureSetsFactory;
	@Inject private CreateDiseaseSignatureTaskFactory.Factory taskFactoryFactory;
	@Inject private Provider<ControlPanelMediator> controlPanelMediatorProvider;
	@Inject private EnrichmentMapManager emManager;
	
	
	private SetOfGeneSets signatureGenesets = null;
	private Set<String> selectedGenesetNames = null; // result of filtering, but since we are using FilterNetric.None() this will be all the genesets
	private String autoName = null;
	
	public PAKnownSignatureCommandTask() {
		filterType = enumNames(PostAnalysisFilterType.values());
		hypergeomUniverseType = enumNames(UniverseType.values());
	}
	
	
	private void loadGeneSets(EnrichmentMap map) {
		LoadSignatureSetsActionListener loadAction = loadSignatureSetsFactory.create(gmtFile, new FilterMetric.NoFilter(), map);
		loadAction.actionPerformed(null);
		
		signatureGenesets = loadAction.getResultGeneSets();
		selectedGenesetNames = loadAction.getFilteredSignatureSets();
		autoName = loadAction.getAutoName();
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if(gmtFile == null || !gmtFile.canRead())
			throw new IllegalArgumentException("Signature GMT file name not valid");
		
		CyNetwork selectedNetwork;
		CyNetworkView selectedView;
		if(network == null) {
			selectedNetwork = applicationManager.getCurrentNetwork();
			selectedView = applicationManager.getCurrentNetworkView();
			if(selectedNetwork == null || selectedView == null) {
				throw new IllegalArgumentException("Current network not available.");
			}
		} else {
			selectedNetwork = network;
			Collection<CyNetworkView> networkViews = networkViewManager.getNetworkViews(network);
			if(networkViews == null || networkViews.isEmpty()) {
				throw new IllegalArgumentException("No network view for: " + network);
			}
			selectedView = networkViews.iterator().next();
		}
		
		EnrichmentMap map = emManager.getEnrichmentMap(selectedNetwork.getSUID());
		if(map == null)
			throw new IllegalArgumentException("Network is not an Enrichment Map.");
		
		loadGeneSets(map);
		
		PostAnalysisFilterType filter = PostAnalysisFilterType.valueOf(filterType.getSelectedValue());
		UniverseType universe = UniverseType.valueOf(hypergeomUniverseType.getSelectedValue());
		
		PostAnalysisParameters.Builder builder = new PostAnalysisParameters.Builder()
			.setAttributePrefix(map.getParams().getAttributePrefix())
			.setSignatureGMTFileName(gmtFile.getAbsolutePath())
			.setLoadedGMTGeneSets(signatureGenesets)
			.addSelectedGeneSetNames(selectedGenesetNames)
			.setUniverseType(universe)
			.setUserDefinedUniverseSize(userDefinedUniverseSize)
			.setRankTestParameters(new PostAnalysisFilterParameters(filter, cutoff))
			.setName(name)
			.setAutoName(autoName);
		
		if(isBatch()) {
			builder.setDataSetName(null); // run in batch mode
		} else {
			if(map.getDataSet(dataSetName) == null) {
				throw new IllegalArgumentException("Data set name not valid: '" + dataSetName + "'");
			}
			builder.setDataSetName(dataSetName);
		}
		
		// Mann-Whitney requires ranks
		if(filter.isMannWhitney()) {
			processMannWhitneyArgs(map, builder);
		}
		
		TaskFactory taskFactory = taskFactoryFactory.create(selectedView, builder.build());
		TaskIterator taskIterator = new TaskIterator();
		taskIterator.append(taskFactory.createTaskIterator());
		
		Task updatePanelTask = new AbstractTask() {
			@Override
			public void run(TaskMonitor taskMonitor) {
				controlPanelMediatorProvider.get().updateDataSetList(selectedView);
				selectedView.updateView();
			}
		};
		
		taskIterator.append(updatePanelTask);
		insertTasksAfterCurrentTask(taskIterator);
	}
	
	
	private void processMannWhitneyArgs(EnrichmentMap map, PostAnalysisParameters.Builder builder) {
		if(mannWhitRanks.isEmpty() && map.isSingleRanksPerDataset()) {
			for(EMDataSet dataset : map.getDataSetList()) {
				String ranksName = dataset.getAllRanksNames().iterator().next();
				builder.addDataSetToRankFile(dataset.getName(), ranksName);
			}
		} else {
			if(mannWhitRanks.isEmpty()) {
				throw new IllegalArgumentException("At least one of the data sets you have specified has more than one ranks file. "
						+ "You must use the 'mannWhitRanks' parameter to specify which ranking to use for each data set.");
			}
			List<EMDataSet> dataSetList = isBatch() ? map.getDataSetList() : Arrays.asList(map.getDataSet(dataSetName));
			for(EMDataSet dataSet : dataSetList) {
				String dsName = dataSet.getName();
				String rankFile = mannWhitRanks.getRankFile(dsName);
				Set<String> ranksNames = dataSet.getAllRanksNames();
				
				if(ranksNames.size() > 1) {
					if(rankFile == null)
						throw new IllegalArgumentException("The data set '" + dsName + "' has more than one ranks file, you must specify the rank file using the 'mannWhatRanks' parameter.");
					if(!ranksNames.contains(rankFile))
						throw new IllegalArgumentException("The data set '" + dsName + "' does not contain the rank file '" + rankFile + "'.");
				}
				
				if(rankFile == null && ranksNames.size() == 1) {
					builder.addDataSetToRankFile(dsName, ranksNames.iterator().next());
				} else {
					builder.addDataSetToRankFile(dsName, rankFile);
				}
			}
		}
	}
	
	
	private boolean isBatch() {
		return dataSetName == null || dataSetName.trim().equalsIgnoreCase("ALL");
	}
}
