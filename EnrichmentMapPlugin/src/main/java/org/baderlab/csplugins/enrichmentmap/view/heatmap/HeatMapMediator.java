package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import static org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil.invokeOnEDT;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import org.baderlab.csplugins.enrichmentmap.PropertyManager;
import org.baderlab.csplugins.enrichmentmap.actions.OpenPathwayCommonsTask;
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
import org.baderlab.csplugins.enrichmentmap.task.genemania.GeneManiaMediator;
import org.baderlab.csplugins.enrichmentmap.task.string.StringAppMediator;
import org.baderlab.csplugins.enrichmentmap.util.CoalesceTimer;
import org.baderlab.csplugins.enrichmentmap.util.NetworkUtil;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParams.Distance;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParams.Operator;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.HeatMapCellRenderer;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.HeatMapTableModel;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
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
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

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
	@Inject private GeneManiaMediator geneManiaMediator;
	@Inject private StringAppMediator stringAppMediator;
	@Inject private PropertyManager propertyManager;
	@Inject private ClusterRankingOption.Factory clusterRankOptionFactory;
	@Inject private AddRanksDialog.Factory ranksDialogFactory;
	@Inject private ExportTXTAction.Factory txtActionFactory;
	@Inject private ExportPDFAction.Factory pdfActionFactory;
	@Inject private OpenPathwayCommonsTask.Factory pathwayCommonsFactory;
	
	@Inject private CyNetworkManager networkManager;
	@Inject private CyServiceRegistrar serviceRegistrar;
	@Inject private Provider<CySwingApplication> swingApplicationProvider;
	@Inject private CyApplicationManager applicationManager;
	@Inject private DialogTaskManager taskManager;
	
	@Inject private HeatMapPanel heatMapPanel;
	@Inject private Provider<HeatMapContentPanel> contentPanelProvider;
	
	private HeatMapContentPanel contentPanel2;
	private final CoalesceTimer selectionEventTimer = new CoalesceTimer(200, 1);
	private boolean onlyEdges;
	
	private boolean isResetting;
	
	private HeatMapContentPanel getContentPanel() {
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
		if(contentPanel2 == null) {
			contentPanel2 = contentPanelProvider.get();
			contentPanel2.getOperatorCombo().addActionListener(operatorActionListener);
			contentPanel2.getNormCombo().addActionListener(normActionListener);
			contentPanel2.getCompressCombo().addActionListener(compressActionListener);
			contentPanel2.getShowValuesCheck().addActionListener(showValueActionListener);
			
			// Fire a setting changed event when column sort changes
			contentPanel2.getTable().getRowSorter().addRowSorterListener(e -> settingChanged()); 
			
			// Options Popup
			contentPanel2.getOptionsPopup().setDistanceConsumer(this::updateSetting_Distance);
			contentPanel2.getOptionsPopup().getGeneManiaButton().addActionListener(e -> runGeneMANIA());
			contentPanel2.getOptionsPopup().getStringButton().addActionListener(e -> runString());
			contentPanel2.getOptionsPopup().getPathwayCommonsButton().addActionListener(e -> runPathwayCommons());
			contentPanel2.getOptionsPopup().getAddRanksButton().addActionListener(e -> addRankings());
			contentPanel2.getOptionsPopup().getExportTxtButton().addActionListener(txtActionFactory.create(contentPanel2.getTable()));
			contentPanel2.getOptionsPopup().getExportPdfButton().addActionListener(pdfActionFactory.create(contentPanel2.getTable(), contentPanel2::getRankingOption, contentPanel2::isShowValues));
			
			// Property Change Listeners
			contentPanel2.addPropertyChangeListener("selectedRankingOption", evt -> settingChanged());
		}
		return contentPanel2;
	}
	
	private void disposeContentPanel() {
		contentPanel2 = null;
		heatMapPanel.showContentPanel(null);
	}
	
	public List<String> getGenes() {
		return getContentPanel().getGenes();
	}
	
	public EnrichmentMap getEnrichmentMap() {
		return getContentPanel().getEnrichmentMap();
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
			disposeContentPanel();
	}
	
	public void reset() {
		CyNetworkView netView = applicationManager.getCurrentNetworkView();
		
		if (netView != null && (emManager.isEnrichmentMap(netView) || emManager.isAssociatedEnrichmentMap(netView)))
			updateHeatMap(netView);
		else
			disposeContentPanel();
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
		
		if (emManager.isEnrichmentMap(networkView)) {
			union = unionGenesets(network, selectedNodes, selectedEdges, prefix);
			inter = intersectionGenesets(network, selectedNodes, selectedEdges, prefix);
		} else {
			AssociatedApp app = NetworkUtil.getAssociatedApp(network);
			if (app != null) {
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
		}
		
		List<RankingOption> rankOptions = getDataSetRankOptions(map, network, selectedNodes, selectedEdges);
		HeatMapParams params = getHeatMapParams(map, network.getSUID(), onlyEdges);
		ClusterRankingOption clusterRankingOption = clusterRankOptionFactory.create(map, params.getDistanceMetric());
		
		invokeOnEDT(() -> {
			HeatMapContentPanel contentPanel = getContentPanel();
			heatMapPanel.showContentPanel(contentPanel);
			
			resetWithoutListeners(() ->
				contentPanel.update(network, map, params, rankOptions, union, inter, clusterRankingOption)
			);
			
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
		Operator oper = getContentPanel().getOperator();
		
		if (oper != null) {
			HeatMapTableModel tableModel = (HeatMapTableModel) getContentPanel().getTable().getModel();
			tableModel.setGenes(getContentPanel().getGenes(oper));
			settingChanged();
		}
	}
	
	private void updateSetting_Transform() {
		Transform transform = getContentPanel().getTransform();
		Compress compress = getContentPanel().getCompress();
		HeatMapTableModel tableModel = (HeatMapTableModel) getContentPanel().getTable().getModel();
		
		if (tableModel.getCompress() != compress) {
			invokeOnEDT(() -> updateHeatMapPanel());
		} else {
			tableModel.setTransform(transform, compress);
			updateSetting_ShowValues(); // clear cached data used by the ColorRenderer
		}
		
		settingChanged();
	}
	
	private void updateHeatMapPanel() {
		HeatMapParams params = getContentPanel().buildParams();
		HeatMapTableModel tableModel = (HeatMapTableModel) getContentPanel().getTable().getModel();
		EnrichmentMap map = tableModel.getEnrichmentMap();
		CyNetwork network = networkManager.getNetwork(map.getNetworkID());
		List<RankingOption> rankOptions = getDataSetRankOptions(map);
		ClusterRankingOption clusterRankingOption = clusterRankOptionFactory.create(map, params.getDistanceMetric());

		resetWithoutListeners(() -> {
			getContentPanel().update(network, map, params, rankOptions, getContentPanel().getUnionGenes(), getContentPanel().getInterGenes(), clusterRankingOption);
		});
	}
	
	
	private void resetWithoutListeners(Runnable runnable) {
		isResetting = true;
		
		getContentPanel().getOperatorCombo().removeActionListener(operatorActionListener);
		getContentPanel().getNormCombo().removeActionListener(normActionListener);
		getContentPanel().getCompressCombo().removeActionListener(compressActionListener);
		getContentPanel().getShowValuesCheck().removeActionListener(showValueActionListener);

		try {
			runnable.run();
		} finally {
			getContentPanel().getOperatorCombo().addActionListener(operatorActionListener);
			getContentPanel().getNormCombo().addActionListener(normActionListener);
			getContentPanel().getCompressCombo().addActionListener(compressActionListener);
			getContentPanel().getShowValuesCheck().addActionListener(showValueActionListener);
			
			isResetting = false;
		}
	}

	private void updateSetting_ShowValues() {
		boolean showValues = getContentPanel().isShowValues();
		HeatMapCellRenderer renderer = (HeatMapCellRenderer) getContentPanel().getTable().getDefaultRenderer(Double.class);
		renderer.setShowValues(showValues);
		getContentPanel().clearTableHeader();
		getContentPanel().updateTableHeader(showValues);
		getContentPanel().getTable().revalidate();
		settingChanged();
	}
	
	private void updateSetting_Distance(Distance distance) {
		RankingOption clusterRankingOption = getContentPanel().getClusterRankingOption();
		if(clusterRankingOption instanceof ClusterRankingOption) {
			((ClusterRankingOption)clusterRankingOption).setDistance(distance);
		}
		getContentPanel().setSelectedRankingOption(clusterRankingOption);
		settingChanged();
	}
	
	private void settingChanged() {
		if (!isResetting) {
			HeatMapParams params = getContentPanel().buildParams();
			heatMapParamsChanged(params);
		}
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
		EnrichmentMap map = getContentPanel().getEnrichmentMap();
		AddRanksDialog dialog = ranksDialogFactory.create(map);
		Optional<String> ranksName = dialog.open();
		
		if (ranksName.isPresent())
			getContentPanel().setMoreRankOptions(getDataSetRankOptions(map));
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

	public static Set<String> unionGenesets(CyNetwork network, List<CyNode> nodes, List<CyEdge> edges, String prefix) {
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
	
	public static Set<String> intersectionGenesets(CyNetwork network, List<CyNode> nodes, List<CyEdge> edges, String prefix) {
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
	
	public static Collection<String> getGenes(CyNetwork network, CyNode node, String prefix) {
		CyRow row = network.getRow(node);
		// This is already the union of all the genes across data sets
		return EMStyleBuilder.Columns.NODE_GENES.get(row, prefix, null);
	}
	
	
	private void runGeneMANIA() {
		EnrichmentMap map = getContentPanel().getEnrichmentMap();
		List<String> genes = getContentPanel().getGenes();
		Set<String> leadingEdgeGenes = getContentPanel().getLeadingEdgeGenes();
		geneManiaMediator.runGeneMANIA(map, genes, leadingEdgeGenes);
	}
	
	private void runString() {
		EnrichmentMap map = getContentPanel().getEnrichmentMap();
		List<String> genes = getContentPanel().getGenes();
		Set<String> leadingEdgeGenes = getContentPanel().getLeadingEdgeGenes();
		stringAppMediator.runString(map, genes, leadingEdgeGenes);
	}
	
	
	private void runPathwayCommons() {
		long uuid = getEnrichmentMap().getNetworkID();
		CyNetwork network = networkManager.getNetwork(uuid);
		OpenPathwayCommonsTask task = pathwayCommonsFactory.createForHeatMap(network);
		taskManager.execute(new TaskIterator(task));
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
