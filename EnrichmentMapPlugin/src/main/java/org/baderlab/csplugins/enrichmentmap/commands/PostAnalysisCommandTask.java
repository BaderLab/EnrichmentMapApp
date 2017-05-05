package org.baderlab.csplugins.enrichmentmap.commands;

import static org.baderlab.csplugins.enrichmentmap.commands.ResolverCommandTask.enumNames;

import java.io.File;
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
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class PostAnalysisCommandTask extends AbstractTask {

	
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
	
	
	@Inject private LoadSignatureSetsActionListener.Factory loadSignatureSetsActionListenerFactory;
	@Inject private CreateDiseaseSignatureTaskFactory.Factory taskFactoryFactory;
	@Inject private CyApplicationManager applicationManager;
	@Inject private Provider<ControlPanelMediator> controlPanelMediatorProvider;
	@Inject private EnrichmentMapManager emManager;
	
	
	private SetOfGeneSets signatureGenesets = null;
	private Set<String> selectedGenesetNames = null; // result of filtering, but since we are using FilterNetric.None() this will be all the genesets
	
	
	public PostAnalysisCommandTask() {
		filterType   = enumNames(PostAnalysisFilterType.values());
		hypergeomUniverseType = enumNames(UniverseType.values());
	}
	
	
	private void loadGeneSets() {
		FilterMetric filterMetric = new FilterMetric.None();
		LoadSignatureSetsActionListener loadAction = loadSignatureSetsActionListenerFactory.create(gmtFile, filterMetric);
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
		
		CyNetwork currentNetwork = applicationManager.getCurrentNetwork();
		CyNetworkView currentView = applicationManager.getCurrentNetworkView();
		if(currentNetwork == null || currentView == null)
			throw new IllegalArgumentException("Current network not available.");
		
		EnrichmentMap map = emManager.getEnrichmentMap(currentNetwork.getSUID());
		if(map == null)
			throw new IllegalArgumentException("Current network is not an Enrichment Map.");
		
		loadGeneSets();
		
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
				controlPanelMediatorProvider.get().updateDataSetList(currentView);
				currentView.updateView();
			}
		};
		
		taskIterator.append(updatePanelTask);
		insertTasksAfterCurrentTask(taskIterator);
	}
	
}
