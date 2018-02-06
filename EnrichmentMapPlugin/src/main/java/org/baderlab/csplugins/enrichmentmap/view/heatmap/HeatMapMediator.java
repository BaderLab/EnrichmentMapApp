package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import static org.baderlab.csplugins.enrichmentmap.task.genemania.QueryGeneManiaTask.GENEMANIA_NAMESPACE;
import static org.baderlab.csplugins.enrichmentmap.task.genemania.QueryGeneManiaTask.GENEMANIA_ORGANISMS_COMMAND;
import static org.baderlab.csplugins.enrichmentmap.task.genemania.QueryGeneManiaTask.GENEMANIA_SEARCH_COMMAND;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.baderlab.csplugins.enrichmentmap.PropertyManager;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet.Method;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResult;
import org.baderlab.csplugins.enrichmentmap.model.GSEAResult;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpression;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpressionMatrix;
import org.baderlab.csplugins.enrichmentmap.model.Ranking;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder;
import org.baderlab.csplugins.enrichmentmap.task.genemania.GMOrganismsResult;
import org.baderlab.csplugins.enrichmentmap.task.genemania.GMSearchResult;
import org.baderlab.csplugins.enrichmentmap.task.genemania.QueryGeneManiaTask;
import org.baderlab.csplugins.enrichmentmap.util.CoalesceTimer;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParams.Compress;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParams.Operator;
import org.baderlab.csplugins.enrichmentmap.view.util.OpenBrowser;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.command.AvailableCommands;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.json.JSONResult;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * Updates the HeatMap panel in response to Cytoscape events.
 */
@Singleton
public class HeatMapMediator implements RowsSetListener, SetCurrentNetworkViewListener {

	private static final int COLLAPSE_THRESHOLD = 50;
	private static final String EM_NETWORK_SUID = "EM_Network.SUID";
	
	@Inject private HeatMapParentPanel.Factory panelFactory;
	@Inject private EnrichmentMapManager emManager;
	@Inject private PropertyManager propertyManager;
	@Inject private ClusterRankingOption.Factory clusterRankOptionFactory;
	@Inject private QueryGeneManiaTask.Factory queryGeneManiaTaskFactory;
	
	@Inject private CyNetworkManager networkManager;
	@Inject private CyServiceRegistrar serviceRegistrar;
	@Inject private Provider<CySwingApplication> swingApplicationProvider;
	@Inject private CyApplicationManager applicationManager;
	@Inject private AvailableCommands availableCommands;
	@Inject private CommandExecutorTaskFactory commandExecutorTaskFactory;
	@Inject private DialogTaskManager taskManager;
	@Inject private OpenBrowser openBrowser;
	
	private final CoalesceTimer selectionEventTimer = new CoalesceTimer(200, 1);
	private HeatMapParentPanel heatMapPanel;
	private boolean onlyEdges;
	
	
	public void showHeatMapPanel() {
		try {
			heatMapPanel = (HeatMapParentPanel) serviceRegistrar.getService(CytoPanelComponent.class, "(id=" + HeatMapParentPanel.ID + ")");
		} catch (Exception ex) { }
		
		if (heatMapPanel == null) {
			heatMapPanel = panelFactory.create(this);
			Properties props = new Properties();
			props.setProperty("id", HeatMapParentPanel.ID);
			serviceRegistrar.registerService(heatMapPanel, CytoPanelComponent.class, props);
		}
		
		bringToFront();
	}

	private void bringToFront() {
		CySwingApplication swingApplication = swingApplicationProvider.get();
		CytoPanel cytoPanel = swingApplication.getCytoPanel(heatMapPanel.getCytoPanelName());
		if(cytoPanel != null) {
			int index = cytoPanel.indexOfComponent(HeatMapParentPanel.ID);
			if(index >= 0) {
				cytoPanel.setSelectedIndex(index);
			}
		}
	}
	
	@Override
	public void handleEvent(RowsSetEvent e) {
		if(heatMapPanel == null)
			return;
		if(e.containsColumn(CyNetwork.SELECTED)) {
			CyNetworkView networkView = applicationManager.getCurrentNetworkView();
			if(networkView != null) {
				CyNetwork network = networkView.getModel();
				// only handle event if it is a selected node
				if(e.getSource() == network.getDefaultEdgeTable() || e.getSource() == network.getDefaultNodeTable()) {
					selectionEventTimer.coalesce(() -> updateHeatMap(networkView));
				}
			}
		}
	}
	
	
	@Override
	public void handleEvent(SetCurrentNetworkViewEvent e) {
		if(heatMapPanel == null)
			return;
		CyNetworkView networkView = e.getNetworkView();
		if(networkView != null && emManager.isEnrichmentMap(networkView)) {
			updateHeatMap(networkView);
		} else {
			heatMapPanel.showEmptyView();
		}
	}
	
	/**
	 * Callback that HeatMapMainPanel calls.
	 */
	public void heatMapParamsChanged(HeatMapParams params) {
		CyNetworkView networkView = applicationManager.getCurrentNetworkView();
		if(networkView != null) {
			Long suid = networkView.getModel().getSUID();
			EnrichmentMap map = emManager.getEnrichmentMap(suid);
			
			// Typically the user will want Union for nodes and Intersection for edges, so we keep
			// two HeatMapParams objects in order to keep the Operator field separate.
			// However we do want to share the other fields, so we copy them over to the other object.
			
			HeatMapParams otherParams = getHeatMapParams(map, suid, !onlyEdges);
			HeatMapParams newOtherParams = new HeatMapParams.Builder(params).setOperator(otherParams.getOperator()).build();
			
			emManager.registerHeatMapParams(suid, !onlyEdges, newOtherParams);
			emManager.registerHeatMapParams(suid, onlyEdges, params);
		}
	}
	
	private void updateHeatMap(CyNetworkView networkView) {
		if(heatMapPanel == null)
			return;
		
		CyNetwork network = networkView.getModel();
		EnrichmentMap map = emManager.getEnrichmentMap(network.getSUID());
		if(map == null)
			return;
		
		List<CyNode> selectedNodes = CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, true);
		List<CyEdge> selectedEdges = CyTableUtil.getEdgesInState(network, CyNetwork.SELECTED, true);
		
		String prefix = map.getParams().getAttributePrefix();
		this.onlyEdges = selectedNodes.isEmpty() && !selectedEdges.isEmpty();
		
		Set<String> union = unionGenesets(network, selectedNodes, selectedEdges, prefix);
		Set<String> inter = intersectionGenesets(network, selectedNodes, selectedEdges, prefix);
		List<RankingOption> rankOptions = getDataSetRankOptions(map, network, selectedNodes, selectedEdges);
		
		HeatMapParams params = getHeatMapParams(map, network.getSUID(), onlyEdges);
		
		HeatMapMainPanel mainPanel = heatMapPanel.selectGenes(map, params, rankOptions, union, inter);
		
		if(mainPanel != null) {
			OptionsPopup optionsPopup = mainPanel.getOptionsPopup();
			
			if (optionsPopup != null && optionsPopup.getGeneManiaButton().getActionListeners().length == 0)
				optionsPopup.getGeneManiaButton().addActionListener(e -> runGeneMANIA(mainPanel));
		}
		
		if(propertyManager.getValue(PropertyManager.HEATMAP_AUTOFOCUS)) {
			bringToFront();
		}
	}
	
	
	private HeatMapParams getHeatMapParams(EnrichmentMap map, Long networkSUID, boolean onlyEdges) {
		HeatMapParams params = emManager.getHeatMapParams(networkSUID, onlyEdges);
		if(params == null) {
			HeatMapParams.Builder builder = new HeatMapParams.Builder();
			
			if(map.totalExpressionCount() > COLLAPSE_THRESHOLD) 
				builder.setCompress(Compress.DATASET_MEDIAN);
			if(onlyEdges)
				builder.setOperator(Operator.INTERSECTION);
			
			params = builder.build();
			emManager.registerHeatMapParams(networkSUID, onlyEdges, params);
		}
		return params;
	}
	
	
	public ClusterRankingOption getClusterRankOption(EnrichmentMap map) {
		return clusterRankOptionFactory.create(map);
	}
	
	public List<RankingOption> getDataSetRankOptions(EnrichmentMap map) {
		CyNetwork network = networkManager.getNetwork(map.getNetworkID());
		List<CyNode> selectedNodes = CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, true);
		List<CyEdge> selectedEdges = CyTableUtil.getEdgesInState(network, CyNetwork.SELECTED, true);
		return getDataSetRankOptions(map, network, selectedNodes, selectedEdges);
	}
	
	private List<RankingOption> getDataSetRankOptions(EnrichmentMap map, CyNetwork network, List<CyNode> nodes, List<CyEdge> edges) {
		List<RankingOption> options = new ArrayList<>();
		for(EMDataSet dataset : map.getDataSetList()) {
			if(nodes.size() == 1 && edges.isEmpty() && dataset.getMethod() == Method.GSEA) {
				String geneSetName = network.getRow(nodes.get(0)).get(CyNetwork.NAME, String.class);
				Map<String,EnrichmentResult> results = dataset.getEnrichments().getEnrichments();
				EnrichmentResult result = results.get(geneSetName);
				if(result instanceof GSEAResult) {
					GSEAResult gseaResult = (GSEAResult) result; 
					Map<String,Ranking> ranks = dataset.getRanks();
					ranks.forEach((name, ranking) -> {
						options.add(new GSEALeadingEdgeRankingOption(dataset, gseaResult, name));
					});
				} else {
					Map<String,Ranking> ranks = dataset.getRanks();
					ranks.forEach((name, ranking) -> {
						options.add(new BasicRankingOption(ranking, dataset, name));
					});
				}
			} else {
				Map<String,Ranking> ranks = dataset.getRanks();
				ranks.forEach((name, ranking) -> {
					options.add(new BasicRankingOption(ranking, dataset, name));
				});
			}
		}
		return options;
	}

	private static Set<String> unionGenesets(CyNetwork network, List<CyNode> nodes, List<CyEdge> edges, String prefix) {
		Set<String> union = new HashSet<>();
		for(CyNode node : nodes) {
			union.addAll(getGenes(network, node, prefix));
		}
		for(CyEdge edge : edges) {
			union.addAll(getGenes(network, edge.getSource(), prefix));
			union.addAll(getGenes(network, edge.getTarget(), prefix));
		}
		return union;
	}
	
	
	private static Set<String> intersectionGenesets(CyNetwork network, List<CyNode> nodes, List<CyEdge> edges, String prefix) {
		Set<String> inter = null;
		for(CyNode node : nodes) {
			Collection<String> genes = getGenes(network, node, prefix);
			if(inter == null)
				inter = new HashSet<>(genes);
			else
				inter.retainAll(genes);
		}
		for(CyEdge edge : edges) {
			Collection<String> genes = getGenes(network, edge.getSource(), prefix);
			if(inter == null)
				inter = new HashSet<>(genes);
			else
				inter.retainAll(genes);
			inter.retainAll(getGenes(network, edge.getTarget(), prefix));
		}
		return inter == null ? Collections.emptySet() : inter;
	}
	
	private static Collection<String> getGenes(CyNetwork network, CyNode node, String prefix) {
		CyRow row = network.getRow(node);
		// This is already the union of all the genes across data sets
		return EMStyleBuilder.Columns.NODE_GENES.get(row, prefix, null);
	}
	
	private void runGeneMANIA(HeatMapMainPanel mainPanel) {
		// Show message to user if genemania not installed
		List<String> commands = availableCommands.getCommands(GENEMANIA_NAMESPACE);
		
		if (commands == null || !commands.contains(GENEMANIA_SEARCH_COMMAND)) {
			if (JOptionPane.showConfirmDialog(
					SwingUtilities.getWindowAncestor(mainPanel),
					"This action requires a version of the GeneMANIA app that is not installed?\n" +
					"Would you like to install or update the GeneMANIA app now?",
					"Cannot Find GeneMANIA App",
					JOptionPane.YES_NO_OPTION
				) == JOptionPane.YES_OPTION) {
				openBrowser.openURL("http://apps.cytoscape.org/apps/genemania");
			}
			
			return;
		}
		
		QueryGeneManiaTask queryTask = queryGeneManiaTaskFactory.create(mainPanel.getGenes());
		
		// Get list of organisms from GeneMANIA
		TaskIterator ti = commandExecutorTaskFactory.createTaskIterator(
				GENEMANIA_NAMESPACE, GENEMANIA_ORGANISMS_COMMAND, Collections.emptyMap(), new TaskObserver() {
					
					@Override
					public void taskFinished(ObservableTask task) {
						if (task instanceof ObservableTask) {
							if (((ObservableTask) task).getResultClasses().contains(JSONResult.class)) {
								JSONResult json = ((ObservableTask) task).getResults(JSONResult.class);
								
								if (json != null && json.getJSON() != null) {
									Gson gson = new Gson();
									Type type = new TypeToken<GMOrganismsResult>(){}.getType();
									GMOrganismsResult res = gson.fromJson(json.getJSON(), type);
									
									if (res != null && res.getOrganisms() != null && !res.getOrganisms().isEmpty())
										queryTask.updatetOrganisms(res.getOrganisms());
									else
										throw new RuntimeException("Unable to retrieve available organisms from GeneMANIA.");
								}
							}
						}
					}
					
					@Override
					public void allFinished(FinishStatus finishStatus) {
						// Never called by Cytoscape...
					}
				});
		ti.append(queryTask);
		
		taskManager.execute(ti, new TaskObserver() {
			@Override
			public void taskFinished(ObservableTask task) {
				// Never called...
			}
			@Override
			public void allFinished(FinishStatus finishStatus) {
				if (finishStatus == FinishStatus.getSucceeded())
					onGeneManiaQueryFinished(queryTask.getResult(), mainPanel);
			}
		});
	}

	private void onGeneManiaQueryFinished(GMSearchResult res, HeatMapMainPanel mainPanel) {
		CyNetwork maniaNet = null;
		
		if (res != null && res.getNetwork() != null && res.getGenes() != null
				&& !res.getGenes().isEmpty())
			maniaNet = networkManager.getNetwork(res.getNetwork());
		
		if (maniaNet == null) {
			SwingUtilities.invokeLater(() -> {
				JOptionPane.showMessageDialog(
						SwingUtilities.getWindowAncestor(mainPanel),
						"The GeneMANIA search returned no results.",
						"No Results",
						JOptionPane.INFORMATION_MESSAGE
				);
			});
		} else {
			EnrichmentMap map = mainPanel.getEnrichmentMap();
			
			// Add EM Network SUID to genemania network's table.
			CyTable tgtNetTable = maniaNet.getDefaultNetworkTable();
			
			if (tgtNetTable.getColumn(EM_NETWORK_SUID) == null)
				tgtNetTable.createColumn(EM_NETWORK_SUID, Long.class, true);
			
			tgtNetTable.getRow(maniaNet.getSUID()).set(EM_NETWORK_SUID, map.getNetworkID());
			
			// Copy some EM columns to genemania's Node table
			CyNetwork emNet = networkManager.getNetwork(map.getNetworkID());
			CyTable srcNodeTable = emNet.getDefaultNodeTable();
			CyTable tgtNodeTable = maniaNet.getDefaultNodeTable();
			Collection<CyColumn> srcNodeColumns = new ArrayList<>(srcNodeTable.getColumns());
			
			for (String key : map.getExpressionMatrixKeys()) {
				GeneExpressionMatrix matrix = map.getExpressionMatrix(key);
				System.out.println("\n* " + key + ": ");
				System.out.println("\t>> " + String.join(", ", matrix.getColumnNames()));
				
				for (GeneExpression ge : matrix.getExpressionMatrix().values()) {
					System.out.println("\t. " + ge.getName() + ": ");
					
					for (Float exp : ge.getExpression())
						System.out.println("\t\t.. " + exp);
				}
			}
			
//			for (CyColumn col : srcNodeColumns) {
//				String colName = col.getName();

//					if (tgtNodeTable.getColumn(colName) == null) {
//						if (col.getListElementType() == null)
//							tgtNodeTable.createColumn(colName, col.getType(), col.isImmutable());
//						else
//							tgtNodeTable.createListColumn(colName, col.getListElementType(), col.isImmutable());
//						
//						for (CyRow tgtRow : tgtNodeTable.getAllRows()) {
//							Object pk = tgtRow.get(tgtNodeTable.getPrimaryKey().getName(), tgtNodeTable.getPrimaryKey().getType());
//							CyRow srcRow = srcNodeTable.getRow(pk);
//							
//							if (srcRow != null) {
//								Object value = srcRow.getRaw(colName);
//								tgtRow.set(colName, value);
//							}
//						}
//					}
//			}
			
			// TODO Update genemania's style, etc...
		}
	}

	public void shutDown() {
		selectionEventTimer.shutdown();
	}
}
