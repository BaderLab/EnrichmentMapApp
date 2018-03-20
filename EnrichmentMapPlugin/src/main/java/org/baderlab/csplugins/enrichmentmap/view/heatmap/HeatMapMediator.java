package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import static org.baderlab.csplugins.enrichmentmap.task.genemania.QueryGeneManiaTask.GENEMANIA_NAMESPACE;
import static org.baderlab.csplugins.enrichmentmap.task.genemania.QueryGeneManiaTask.GENEMANIA_ORGANISMS_COMMAND;
import static org.baderlab.csplugins.enrichmentmap.task.genemania.QueryGeneManiaTask.GENEMANIA_SEARCH_COMMAND;
import static org.baderlab.csplugins.enrichmentmap.task.string.QueryStringTask.STRING_NAMESPACE;
import static org.baderlab.csplugins.enrichmentmap.task.string.QueryStringTask.STRING_SPECIES_COMMAND;
import static org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil.invokeOnEDT;

import java.awt.event.ActionListener;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.baderlab.csplugins.enrichmentmap.AfterInjection;
import org.baderlab.csplugins.enrichmentmap.PropertyManager;
import org.baderlab.csplugins.enrichmentmap.model.AssociatedApp;
import org.baderlab.csplugins.enrichmentmap.model.Compress;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet.Method;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResult;
import org.baderlab.csplugins.enrichmentmap.model.GSEAResult;
import org.baderlab.csplugins.enrichmentmap.model.Ranking;
import org.baderlab.csplugins.enrichmentmap.model.Transform;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder;
import org.baderlab.csplugins.enrichmentmap.task.genemania.GMOrganismsResult;
import org.baderlab.csplugins.enrichmentmap.task.genemania.GMSearchResult;
import org.baderlab.csplugins.enrichmentmap.task.genemania.QueryGeneManiaTask;
import org.baderlab.csplugins.enrichmentmap.task.string.QueryStringTask;
import org.baderlab.csplugins.enrichmentmap.task.string.STRSpecies;
import org.baderlab.csplugins.enrichmentmap.util.CoalesceTimer;
import org.baderlab.csplugins.enrichmentmap.util.NetworkUtil;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParams.Distance;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParams.Operator;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.HeatMapCellRenderer;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.HeatMapTableModel;
import org.baderlab.csplugins.enrichmentmap.view.util.OpenBrowser;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.command.AvailableCommands;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
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
	
	private ActionListener normActionListener;
	private ActionListener operatorActionListener;
	private ActionListener compressActionListener;
	private ActionListener showValueActionListener;
	
	@Inject private EnrichmentMapManager emManager;
	@Inject private PropertyManager propertyManager;
	@Inject private ClusterRankingOption.Factory clusterRankOptionFactory;
	@Inject private AddRanksDialog.Factory ranksDialogFactory;
	@Inject private ExportTXTAction.Factory txtActionFactory;
	@Inject private ExportPDFAction.Factory pdfActionFactory;
	@Inject private QueryGeneManiaTask.Factory queryGeneManiaTaskFactory;
	@Inject private QueryStringTask.Factory queryStringTaskFactory;
	
	@Inject private CyNetworkManager networkManager;
	@Inject private CyServiceRegistrar serviceRegistrar;
	@Inject private Provider<CySwingApplication> swingApplicationProvider;
	@Inject private CyApplicationManager applicationManager;
	@Inject private AvailableCommands availableCommands;
	@Inject private CommandExecutorTaskFactory commandExecutorTaskFactory;
	@Inject private DialogTaskManager taskManager;
	@Inject private OpenBrowser openBrowser;
	
	@Inject private HeatMapPanel heatMapPanel;
	@Inject private HeatMapContentPanel contentPanel;
	
	private final CoalesceTimer selectionEventTimer = new CoalesceTimer(200, 1);
	private boolean onlyEdges;
	
	private boolean isResetting;
	
	@AfterInjection
	private void init() {
		// Add UI listeners
		if (operatorActionListener == null)
			operatorActionListener = evt -> updateSetting_Operator();
		if (normActionListener == null)
			normActionListener = evt -> updateSetting_Transform();
		if (compressActionListener == null)
			compressActionListener = evt -> updateSetting_Transform();
		if (showValueActionListener == null)
			showValueActionListener = evt -> updateSetting_ShowValues();
		
		// Tool Bar
		contentPanel.getOperatorCombo().addActionListener(operatorActionListener);
		contentPanel.getNormCombo().addActionListener(normActionListener);
		contentPanel.getCompressCombo().addActionListener(compressActionListener);
		contentPanel.getShowValuesCheck().addActionListener(showValueActionListener);
		
		// Fire a setting changed event when column sort changes
		contentPanel.getTable().getRowSorter().addRowSorterListener(e -> settingChanged()); 
		
		// Options Popup
		contentPanel.getOptionsPopup().setDistanceConsumer(this::updateSetting_Distance);
		contentPanel.getOptionsPopup().getGeneManiaButton().addActionListener(e -> runGeneMANIA());
		contentPanel.getOptionsPopup().getStringButton().addActionListener(e -> runString());
		contentPanel.getOptionsPopup().getAddRanksButton().addActionListener(e -> addRankings());
		contentPanel.getOptionsPopup().getExportTxtButton().addActionListener(txtActionFactory.create(contentPanel.getTable()));
		contentPanel.getOptionsPopup().getExportPdfButton().addActionListener(pdfActionFactory.create(contentPanel.getTable(), contentPanel::getRankingOption));
		
		// Property Change Listeners
		contentPanel.addPropertyChangeListener("selectedRankingOption", evt -> settingChanged());
	}
	
	public void showHeatMapPanel() {
		if (!isHeatMapPanelRegistered()) {
			Properties props = new Properties();
			props.setProperty("id", HeatMapPanel.ID);
			serviceRegistrar.registerService(heatMapPanel, CytoPanelComponent.class, props);
		}
		
		bringToFront();
	}

	private void bringToFront() {
		CySwingApplication swingApplication = swingApplicationProvider.get();
		CytoPanel cytoPanel = swingApplication.getCytoPanel(heatMapPanel.getCytoPanelName());
		if(cytoPanel != null) {
			int index = cytoPanel.indexOfComponent(HeatMapPanel.ID);
			if(index >= 0) {
				cytoPanel.setSelectedIndex(index);
			}
		}
	}
	
	@Override
	public void handleEvent(RowsSetEvent e) {
		if(!isHeatMapPanelRegistered())
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
		if (!isHeatMapPanelRegistered())
			return;
		
		CyNetworkView netView = e.getNetworkView();
		
		if (netView != null && (emManager.isEnrichmentMap(netView) || emManager.isAssociatedEnrichmentMap(netView)))
			updateHeatMap(netView);
		else
			heatMapPanel.showEmptyView();
	}
	
	public void reset() {
		CyNetworkView netView = applicationManager.getCurrentNetworkView();
		
		if (netView != null && (emManager.isEnrichmentMap(netView) || emManager.isAssociatedEnrichmentMap(netView)))
			updateHeatMap(netView);
		else
			heatMapPanel.showEmptyView();
	}
	
	private void heatMapParamsChanged(HeatMapParams params) {
		CyNetworkView networkView = applicationManager.getCurrentNetworkView();
		
		if (networkView != null) {
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
		if (!isHeatMapPanelRegistered())
			return;
		
		CyNetwork network = networkView.getModel();
		EnrichmentMap map = emManager.getEnrichmentMap(network.getSUID());
		
		if (map == null)
			return;
		
		List<CyNode> selectedNodes = CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, true);
		List<CyEdge> selectedEdges = CyTableUtil.getEdgesInState(network, CyNetwork.SELECTED, true);
		
		String prefix = map.getParams().getAttributePrefix();
		onlyEdges = selectedNodes.isEmpty() && !selectedEdges.isEmpty();
		
		final Set<String> union;
		final Set<String> inter;
		
		AssociatedApp app = NetworkUtil.getAssociatedApp(network);
		
		if (emManager.isEnrichmentMap(networkView)) {
			union = unionGenesets(network, selectedNodes, selectedEdges, prefix);
			inter = intersectionGenesets(network, selectedNodes, selectedEdges, prefix);
		} else if (app != null) {
			union = new HashSet<>();
			
			for (CyNode node : selectedNodes) {
				CyRow row = network.getRow(node);
				String geneName = app.getGeneNameColumn().get(row, null, null);
				
				if (geneName != null)
					union.add(geneName);
			}
			
			inter = union;
		} else {
			inter = union = Collections.emptySet();
		}
		
		List<RankingOption> rankOptions = getDataSetRankOptions(map, network, selectedNodes, selectedEdges);
		HeatMapParams params = getHeatMapParams(map, network.getSUID(), onlyEdges);
		ClusterRankingOption clusterRankingOption = getClusterRankingOption(map);
		
		invokeOnEDT(() -> {
			heatMapPanel.showContentPanel();
			contentPanel.update(network, map, params, rankOptions, union, inter, clusterRankingOption);
			
			if (propertyManager.getValue(PropertyManager.HEATMAP_AUTOFOCUS))
				bringToFront();
		});
	}
	
	private HeatMapParams getHeatMapParams(EnrichmentMap map, Long networkSUID, boolean onlyEdges) {
		HeatMapParams params = emManager.getHeatMapParams(networkSUID, onlyEdges);
		
		if (params == null) {
			HeatMapParams.Builder builder = new HeatMapParams.Builder();

			if (map.totalExpressionCount() > COLLAPSE_THRESHOLD)
				builder.setCompress(Compress.DATASET_MEDIAN);
			if (onlyEdges)
				builder.setOperator(Operator.INTERSECTION);

			params = builder.build();
			emManager.registerHeatMapParams(networkSUID, onlyEdges, params);
		}
		
		return params;
	}
	
	private void updateSetting_Operator() {
		Operator oper = contentPanel.getOperator();
		
		if (oper != null) {
			HeatMapTableModel tableModel = (HeatMapTableModel) contentPanel.getTable().getModel();
			tableModel.setGenes(contentPanel.getGenes(oper));
			settingChanged();
		}
	}
	
	private void updateSetting_Transform() {
		Transform transform = contentPanel.getTransform();
		Compress compress = contentPanel.getCompress();
		HeatMapTableModel tableModel = (HeatMapTableModel) contentPanel.getTable().getModel();
		
		if (tableModel.getCompress() != compress) {
			invokeOnEDT(() -> updateHeatMapPanel());
		} else {
			tableModel.setTransform(transform, compress);
			updateSetting_ShowValues(); // clear cached data used by the ColorRenderer
		}
		
		settingChanged();
	}
	
	private void updateHeatMapPanel() {
		HeatMapParams params = contentPanel.buildParams();
		HeatMapTableModel tableModel = (HeatMapTableModel) contentPanel.getTable().getModel();
		EnrichmentMap map = tableModel.getEnrichmentMap();
		CyNetwork network = networkManager.getNetwork(map.getNetworkID());
		List<RankingOption> rankOptions = getDataSetRankOptions(map);
		ClusterRankingOption clusterRankingOption = getClusterRankingOption(map);

		isResetting = true;
		
		contentPanel.getOperatorCombo().removeActionListener(operatorActionListener);
		contentPanel.getNormCombo().removeActionListener(normActionListener);
		contentPanel.getCompressCombo().removeActionListener(compressActionListener);
		contentPanel.getShowValuesCheck().removeActionListener(showValueActionListener);

		try {
			contentPanel.update(network, map, params, rankOptions, contentPanel.getUnionGenes(), contentPanel.getInterGenes(), clusterRankingOption);
		} finally {
			contentPanel.getOperatorCombo().addActionListener(operatorActionListener);
			contentPanel.getNormCombo().addActionListener(normActionListener);
			contentPanel.getCompressCombo().addActionListener(compressActionListener);
			contentPanel.getShowValuesCheck().addActionListener(showValueActionListener);
			
			isResetting = false;
		}
	}

	private void updateSetting_ShowValues() {
		boolean showValues = contentPanel.isShowValues();
		HeatMapCellRenderer renderer = (HeatMapCellRenderer) contentPanel.getTable().getDefaultRenderer(Double.class);
		renderer.setShowValues(showValues);
		contentPanel.clearTableHeader();
		contentPanel.updateTableHeader(showValues);
		contentPanel.getTable().revalidate();
		settingChanged();
	}
	
	private void updateSetting_Distance(Distance distance) {
		EnrichmentMap map = contentPanel.getEnrichmentMap();
		ClusterRankingOption clusterRankingOption = getClusterRankingOption(map);
		clusterRankingOption.setDistance(distance);
		contentPanel.setSelectedRankingOption(clusterRankingOption);
		settingChanged();
	}
	
	private void settingChanged() {
		if (!isResetting) {
			HeatMapParams params = contentPanel.buildParams();
			heatMapParamsChanged(params);
		}
	}
	
	public ClusterRankingOption getClusterRankingOption(EnrichmentMap map) {
		return clusterRankOptionFactory.create(map);
	}
	
	public List<RankingOption> getDataSetRankOptions(EnrichmentMap map) {
		CyNetwork network = networkManager.getNetwork(map.getNetworkID());
		
		if (network == null)
			return Collections.emptyList();
		
		List<CyNode> selectedNodes = CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, true);
		List<CyEdge> selectedEdges = CyTableUtil.getEdgesInState(network, CyNetwork.SELECTED, true);
		
		return getDataSetRankOptions(map, network, selectedNodes, selectedEdges);
	}
	
	private void addRankings() {
		EnrichmentMap map = contentPanel.getEnrichmentMap();
		AddRanksDialog dialog = ranksDialogFactory.create(map);
		Optional<String> ranksName = dialog.open();
		
		if (ranksName.isPresent())
			contentPanel.setMoreRankOptions(getDataSetRankOptions(map));
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
	
	private void runGeneMANIA() {
		// Show message to user if genemania not installed
		List<String> commands = availableCommands.getCommands(GENEMANIA_NAMESPACE);
		
		if (commands == null || !commands.contains(GENEMANIA_SEARCH_COMMAND)) {
			if (JOptionPane.showConfirmDialog(
					SwingUtilities.getWindowAncestor(contentPanel),
					"This action requires a version of the GeneMANIA app that is not installed?\n" +
					"Would you like to install or update the GeneMANIA app now?",
					"Cannot Find GeneMANIA App",
					JOptionPane.YES_NO_OPTION
				) == JOptionPane.YES_OPTION) {
				openBrowser.openURL("http://apps.cytoscape.org/apps/genemania");
			}
			
			return;
		}
		
		QueryGeneManiaTask queryTask = queryGeneManiaTaskFactory.create(contentPanel.getGenes());
		
		// Get list of organisms from GeneMANIA
		TaskIterator ti = commandExecutorTaskFactory.createTaskIterator(
				GENEMANIA_NAMESPACE, GENEMANIA_ORGANISMS_COMMAND, Collections.emptyMap(), new TaskObserver() {
					
					@Override
					@SuppressWarnings("serial")
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
					onGeneManiaQueryFinished(queryTask.getResult(), contentPanel);
			}
		});
	}

	private void onGeneManiaQueryFinished(GMSearchResult res, HeatMapContentPanel contentPanel) {
		CyNetwork net = null;
		
		if (res != null && res.getNetwork() != null && res.getGenes() != null
				&& !res.getGenes().isEmpty())
			net = networkManager.getNetwork(res.getNetwork());
		
		if (net == null) {
			invokeOnEDT(() -> {
				JOptionPane.showMessageDialog(
						SwingUtilities.getWindowAncestor(contentPanel),
						"The GeneMANIA search returned no results.",
						"No Results",
						JOptionPane.INFORMATION_MESSAGE
				);
			});
		} else {
			// Update the model
			EnrichmentMap map = contentPanel.getEnrichmentMap();
			map.addAssociatedNetworkID(net.getSUID());
			
			emManager.addAssociatedAppAttributes(net, map, AssociatedApp.GENEMANIA);
//	TODO	
//			// Modify GeneMANIA's style
//			Collection<CyNetworkView> netViewList = netViewManager.getNetworkViews(net);
//			
//			for (CyNetworkView netView : netViewList)
//				updateGeneManiaStyle(netView);
		}
	}
	
	private void runString() {
		// Show message to user if STRING App not installed
		List<String> commands = availableCommands.getCommands(STRING_NAMESPACE);
		
		if (commands == null || !commands.contains(STRING_SPECIES_COMMAND)) {
			if (JOptionPane.showConfirmDialog(
					SwingUtilities.getWindowAncestor(contentPanel),
					"This action requires a version of the STRING app that is not installed?\n" +
					"Would you like to install or update the STRING app now?",
					"Cannot Find STRING App",
					JOptionPane.YES_NO_OPTION
				) == JOptionPane.YES_OPTION) {
				openBrowser.openURL("http://apps.cytoscape.org/apps/stringapp");
			}
			
			return;
		}
		
		QueryStringTask queryTask = queryStringTaskFactory.create(contentPanel.getGenes());
		
		// Get list of organisms from STRING App
		TaskIterator ti = commandExecutorTaskFactory.createTaskIterator(
				STRING_NAMESPACE, STRING_SPECIES_COMMAND, Collections.emptyMap(), new TaskObserver() {
					
					@Override
					@SuppressWarnings("serial")
					public void taskFinished(ObservableTask task) {
						if (task instanceof ObservableTask) {
							if (((ObservableTask) task).getResultClasses().contains(JSONResult.class)) {
								JSONResult json = ((ObservableTask) task).getResults(JSONResult.class);
								
								if (json != null && json.getJSON() != null) {
									Gson gson = new Gson();
									Type type = new TypeToken<ArrayList<STRSpecies>>(){}.getType();
									List<STRSpecies> organisms = gson.fromJson(json.getJSON(), type);
									
									if (organisms != null && !organisms.isEmpty())
										queryTask.updatetOrganisms(organisms);
									else
										throw new RuntimeException("Unable to retrieve available species from STRING App.");
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
					onStringQueryFinished(queryTask.getResult(), contentPanel);
			}
		});
	}
	
	private void onStringQueryFinished(Long netId, HeatMapContentPanel contentPanel) {
		CyNetwork net = netId != null ? networkManager.getNetwork(netId) : null;
		
		if (net == null) {
			invokeOnEDT(() -> {
				JOptionPane.showMessageDialog(
						SwingUtilities.getWindowAncestor(contentPanel),
						"The STRING protein query returned no results.",
						"No Results",
						JOptionPane.INFORMATION_MESSAGE
				);
			});
		} else {
			// Update the model
			EnrichmentMap map = contentPanel.getEnrichmentMap();
			map.addAssociatedNetworkID(net.getSUID());
			
			emManager.addAssociatedAppAttributes(net, map, AssociatedApp.STRING);
// TODO		
//			// Modify GeneMANIA's style
//			Collection<CyNetworkView> netViewList = netViewManager.getNetworkViews(strNet);
//			
//			for (CyNetworkView netView : netViewList)
//				updateGeneManiaStyle(netView);
		}
	}

	private boolean isHeatMapPanelRegistered() {
		try {
			heatMapPanel = (HeatMapPanel) serviceRegistrar.getService(CytoPanelComponent.class, "(id=" + HeatMapPanel.ID + ")");
			return heatMapPanel != null;
		} catch (Exception ex) { }
		
		return false;
	}
	
	public void shutDown() {
		selectionEventTimer.shutdown();
	}
}
