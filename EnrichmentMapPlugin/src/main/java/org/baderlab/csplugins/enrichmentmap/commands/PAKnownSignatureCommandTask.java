package org.baderlab.csplugins.enrichmentmap.commands;

import static org.baderlab.csplugins.enrichmentmap.commands.ResolverCommandTask.enumNames;

import java.io.File;
import java.util.Collection;
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
import org.baderlab.csplugins.enrichmentmap.task.CreateDiseaseSignatureTaskFactory;
import org.baderlab.csplugins.enrichmentmap.task.FilterMetric;
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
	
	
	@Inject private CyApplicationManager applicationManager;
	@Inject private CyNetworkViewManager networkViewManager;
	
	@Inject private LoadSignatureSetsActionListener.Factory loadSignatureSetsActionListenerFactory;
	@Inject private CreateDiseaseSignatureTaskFactory.Factory taskFactoryFactory;
	@Inject private Provider<ControlPanelMediator> controlPanelMediatorProvider;
	@Inject private EnrichmentMapManager emManager;
	
	
	private SetOfGeneSets signatureGenesets = null;
	private Set<String> selectedGenesetNames = null; // result of filtering, but since we are using FilterNetric.None() this will be all the genesets
	
	
	public PAKnownSignatureCommandTask() {
		filterType   = enumNames(PostAnalysisFilterType.values());
		hypergeomUniverseType = enumNames(UniverseType.values());
	}
	
	
	private void loadGeneSets(EnrichmentMap map) {
		FilterMetric filterMetric = new FilterMetric.None();
		LoadSignatureSetsActionListener loadAction = loadSignatureSetsActionListenerFactory.create(gmtFile, filterMetric, map);
		loadAction.setGeneSetCallback(gs -> {
			signatureGenesets = gs;
		});
		loadAction.setFilteredSignatureSetsCallback(names -> {
			selectedGenesetNames = names;
		});
		
		loadAction.actionPerformed(null);
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
		
		PostAnalysisParameters.Builder builder = new PostAnalysisParameters.Builder();
		builder.setAttributePrefix(map.getParams().getAttributePrefix());
		builder.setSignatureGMTFileName(gmtFile.getAbsolutePath());
		builder.setLoadedGMTGeneSets(signatureGenesets);
		builder.addSelectedGeneSetNames(selectedGenesetNames);
		builder.setUniverseType(universe);
		builder.setUserDefinedUniverseSize(userDefinedUniverseSize);
		builder.setRankTestParameters(new PostAnalysisFilterParameters(filter, cutoff));
		builder.setName(name);
		
		if(filter.isMannWhitney()) {
			if(map.isSingleRanksPerDataset()) {
				for(EMDataSet dataset : map.getDataSetList()) {
					String ranksName = dataset.getExpressionSets().getAllRanksNames().iterator().next();
					builder.addDataSetToRankFile(dataset.getName(), ranksName);
				}
			} else {
				throw new RuntimeException("Mann-Whitney can only be run from a command if every data set has a single ranks file.");
			}
		}
		
		PostAnalysisParameters params = builder.build();
		
		CyNetworkView netView = applicationManager.getCurrentNetworkView();
		TaskFactory taskFactory = taskFactoryFactory.create(netView, params);
		
		TaskIterator taskIterator = new TaskIterator();
		taskIterator.append(taskFactory.createTaskIterator());
		
		Task updatePanelTask = new AbstractTask() {
			@Override
			public void run(TaskMonitor taskMonitor) throws Exception {
				controlPanelMediatorProvider.get().updateDataSetList(selectedView);
				selectedView.updateView();
			}
		};
		
		taskIterator.append(updatePanelTask);
		insertTasksAfterCurrentTask(taskIterator);
	}
	
}
