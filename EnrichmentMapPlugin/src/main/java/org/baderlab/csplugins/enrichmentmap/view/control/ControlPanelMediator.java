package org.baderlab.csplugins.enrichmentmap.view.control;

import static org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder.Columns.EDGE_DATASET_VALUE_COMPOUND;
import static org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder.Columns.EDGE_INTERACTION_VALUE_SIG;
import static org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder.Columns.NODE_GS_TYPE;
import static org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder.Columns.NODE_GS_TYPE_ENRICHMENT;
import static org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil.invokeOnEDT;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.Timer;

import org.baderlab.csplugins.enrichmentmap.AfterInjection;
import org.baderlab.csplugins.enrichmentmap.PropertyManager;
import org.baderlab.csplugins.enrichmentmap.actions.ShowAboutDialogAction;
import org.baderlab.csplugins.enrichmentmap.model.AbstractDataSet;
import org.baderlab.csplugins.enrichmentmap.model.AssociatedApp;
import org.baderlab.csplugins.enrichmentmap.model.Compress;
import org.baderlab.csplugins.enrichmentmap.model.CompressedClass;
import org.baderlab.csplugins.enrichmentmap.model.CompressedDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EMSignatureDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.model.ExpressionCache;
import org.baderlab.csplugins.enrichmentmap.model.ExpressionData;
import org.baderlab.csplugins.enrichmentmap.model.Transform;
import org.baderlab.csplugins.enrichmentmap.model.Uncompressed;
import org.baderlab.csplugins.enrichmentmap.model.event.AssociatedEnrichmentMapsChangedEvent;
import org.baderlab.csplugins.enrichmentmap.model.event.AssociatedEnrichmentMapsChangedListener;
import org.baderlab.csplugins.enrichmentmap.model.event.EnrichmentMapAboutToBeRemovedEvent;
import org.baderlab.csplugins.enrichmentmap.model.event.EnrichmentMapAboutToBeRemovedListener;
import org.baderlab.csplugins.enrichmentmap.model.event.EnrichmentMapAddedEvent;
import org.baderlab.csplugins.enrichmentmap.model.event.EnrichmentMapAddedListener;
import org.baderlab.csplugins.enrichmentmap.style.AssociatedStyleOptions;
import org.baderlab.csplugins.enrichmentmap.style.ChartData;
import org.baderlab.csplugins.enrichmentmap.style.ChartOptions;
import org.baderlab.csplugins.enrichmentmap.style.ChartType;
import org.baderlab.csplugins.enrichmentmap.style.ColorScheme;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder.Columns;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder.StyleUpdateScope;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleOptions;
import org.baderlab.csplugins.enrichmentmap.style.WidthFunction;
import org.baderlab.csplugins.enrichmentmap.task.ApplyEMStyleTask;
import org.baderlab.csplugins.enrichmentmap.task.AutoAnnotateOpenTask;
import org.baderlab.csplugins.enrichmentmap.task.AutoAnnotateRedrawTask;
import org.baderlab.csplugins.enrichmentmap.task.FilterNodesEdgesTask;
import org.baderlab.csplugins.enrichmentmap.task.FilterNodesEdgesTask.FilterMode;
import org.baderlab.csplugins.enrichmentmap.task.SelectNodesEdgesTask;
import org.baderlab.csplugins.enrichmentmap.task.UpdateAssociatedStyleTask;
import org.baderlab.csplugins.enrichmentmap.task.postanalysis.RemoveSignatureDataSetsTask;
import org.baderlab.csplugins.enrichmentmap.util.CoalesceTimer;
import org.baderlab.csplugins.enrichmentmap.util.NetworkUtil;
import org.baderlab.csplugins.enrichmentmap.util.TaskUtil;
import org.baderlab.csplugins.enrichmentmap.view.control.ControlPanel.AbstractViewControlPanel;
import org.baderlab.csplugins.enrichmentmap.view.control.ControlPanel.AssociatedViewControlPanel;
import org.baderlab.csplugins.enrichmentmap.view.control.ControlPanel.EMViewControlPanel;
import org.baderlab.csplugins.enrichmentmap.view.control.io.ViewParams;
import org.baderlab.csplugins.enrichmentmap.view.control.io.ViewParams.CutoffParam;
import org.baderlab.csplugins.enrichmentmap.view.creation.CreationDialogShowAction;
import org.baderlab.csplugins.enrichmentmap.view.creation.genemania.GenemaniaDialogShowAction;
import org.baderlab.csplugins.enrichmentmap.view.creation.genemania.StringDialogShowAction;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapMediator;
import org.baderlab.csplugins.enrichmentmap.view.legend.CreationParametersPanel;
import org.baderlab.csplugins.enrichmentmap.view.legend.LegendPanelMediator;
import org.baderlab.csplugins.enrichmentmap.view.postanalysis.EdgeWidthDialog;
import org.baderlab.csplugins.enrichmentmap.view.postanalysis.PADialogMediator;
import org.baderlab.csplugins.enrichmentmap.view.util.IconUtil;
import org.baderlab.csplugins.enrichmentmap.view.util.SliderBarPanel;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
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
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.util.swing.TextIcon;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class ControlPanelMediator implements SetCurrentNetworkViewListener, EnrichmentMapAddedListener,
	EnrichmentMapAboutToBeRemovedListener, AssociatedEnrichmentMapsChangedListener, RowsSetListener {

	@Inject private Provider<ControlPanel> controlPanelProvider;
	@Inject private Provider<LegendPanelMediator> legendPanelMediatorProvider;
	@Inject private Provider<PADialogMediator> paDialogMediatorProvider;
	@Inject private DataSetColorSelectorDialog.Factory colorSelectorDialogFactory;
	@Inject private Provider<EdgeWidthDialog> dialogProvider;
	@Inject private Provider<HeatMapMediator> heatMapMediatorProvider;
	@Inject private Provider<ShowAboutDialogAction> showAboutDialogActionProvider;
	@Inject private GenemaniaDialogShowAction genemaniaAction;
	@Inject private StringDialogShowAction stringAction;
	@Inject private EnrichmentMapManager emManager;
	@Inject private CreationDialogShowAction masterMapDialogAction;
	@Inject private VisualMappingManager visualMappingManager;
	@Inject private PropertyManager propertyManager;
	@Inject private Provider<AutoAnnotateOpenTask> autoAnnotateOpenTaskProvider;
	@Inject private Provider<AutoAnnotateRedrawTask> autoAnnotateRedrawTaskProvider;
	
	@Inject private CyServiceRegistrar serviceRegistrar;
	@Inject private CyApplicationManager applicationManager;
	@Inject private CySwingApplication swingApplication;
	@Inject private CyNetworkViewManager networkViewManager;
	@Inject private CyNetworkManager networkManager;
	@Inject private ApplyEMStyleTask.Factory applyStyleTaskFactory;
	@Inject private UpdateAssociatedStyleTask.Factory updateAssociatedStyleTaskFactory;
	@Inject private RemoveSignatureDataSetsTask.Factory removeDataSetsTaskFactory;
	@Inject private FilterNodesEdgesTask.Factory filterNodesEdgesTaskFactory;
	@Inject private SelectNodesEdgesTask.Factory selectNodesEdgesTaskFactory;
	@Inject private DialogTaskManager dialogTaskManager;
	@Inject private SynchronousTaskManager<?> syncTaskManager;
	
	private FilterMode filterMode = FilterMode.HIDE;
	
	private final CoalesceTimer selectionEventTimer = new CoalesceTimer(200, 1);
	private Map<CyNetworkView, Timer> filterTimers = new HashMap<>();
	
	private boolean firstTime = true;
	private boolean updating;
	
	public void showControlPanel() {
		CytoPanelComponent panel = null;
		
		try {
			panel = serviceRegistrar.getService(CytoPanelComponent.class, "(id=" + ControlPanel.ID + ")");
		} catch (Exception ex) {
		}
		
		if (panel == null) {
			panel = controlPanelProvider.get();
			
			Properties props = new Properties();
			props.setProperty("id", ControlPanel.ID);
			serviceRegistrar.registerService(panel, CytoPanelComponent.class, props);
			
			if (firstTime && emManager.getAllEnrichmentMaps().isEmpty()) {
				firstTime = false;
			} else {
				setCurrentNetworkView(applicationManager.getCurrentNetworkView());
			}
		}
		
		// Select the panel
		CytoPanel cytoPanel = swingApplication.getCytoPanel(panel.getCytoPanelName());
		int index = cytoPanel.indexOfComponent(ControlPanel.ID);
		if (index >= 0)
			cytoPanel.setSelectedIndex(index);
	}
	
	
	@Override
	public void handleEvent(AssociatedEnrichmentMapsChangedEvent e) {
		// A GeneMANIA network (created from EM nodes) has been added...
		invokeOnEDT(() -> {
			updating = true;
			try {
				getControlPanel().updateEmViewCombo();
				CyNetworkView netView = applicationManager.getCurrentNetworkView();
				if (netView != null && emManager.isAssociatedEnrichmentMap(netView)) {
					setCurrentNetworkView(netView);
				}
			} finally {
				updating = false;
			}
		});
	}
	
	
	public ListenableFuture<Void> reset() {
		ListenableFuture<Void> future = SwingUtil.invokeOnEDTFuture(() -> {
			updating = true;
			try {
				for (CyNetworkView view : networkViewManager.getNetworkViewSet())
					getControlPanel().removeEnrichmentMapView(view);
	
				Set<Long> netIds = new LinkedHashSet<>();
				Map<Long, EnrichmentMap> maps = emManager.getAllEnrichmentMaps();
				
				for (EnrichmentMap map : maps.values()) {
					netIds.add(map.getNetworkID());
					netIds.addAll(map.getAssociatedNetworkIDs());
				}
				
				for (Long id : netIds) {
					CyNetwork network = networkManager.getNetwork(id);
					
					if (network != null) {
						Collection<CyNetworkView> networkViews = networkViewManager.getNetworkViews(network);
						for (CyNetworkView netView : networkViews)
							addNetworkView(netView);
					}
				}
			} finally {
				updating = false;	
			}

			setCurrentNetworkView(applicationManager.getCurrentNetworkView());
		});
		
		return future;
	}
	
	public void reset(ViewParams params, StyleUpdateScope updateScope) {
		long netViewID = params.getNetworkViewID();
		
		invokeOnEDT(() -> {
			EMViewControlPanel viewPanel = getControlPanel().getViewControlPanel(netViewID);
			
			if (viewPanel == null)
				return;
			
			EnrichmentMap map = emManager.getEnrichmentMap(viewPanel.getNetworkView().getModel().getSUID());
			
			if (map == null)
				return;
			
			try {
				updating = true;
				
				// Update Filters
				if (params.getPValue() != null && viewPanel.getPValueSliderPanel() != null)
					viewPanel.getPValueSliderPanel().setValue(params.getPValue());
				if (params.getQValue() != null && viewPanel.getQValueSliderPanel() != null)
					viewPanel.getQValueSliderPanel().setValue(params.getQValue());
				if (params.getSimilarityCoefficient() != null && viewPanel.getSimilaritySliderPanel() != null)
					viewPanel.getSimilaritySliderPanel().setValue(params.getSimilarityCoefficient());
				
				if (params.getNodeCutoffParam() == CutoffParam.P_VALUE)
					viewPanel.getPValueRadio().doClick();
				else if (params.getNodeCutoffParam() == CutoffParam.Q_VALUE)
					viewPanel.getQValueRadio().doClick();
				
				Set<String> filteredOutDataSetNames = new HashSet<>(params.getFilteredOutDataSets());
				Set<AbstractDataSet> allDataSets = viewPanel.getAllDataSets();
				Set<AbstractDataSet> filteredDataSets = allDataSets.stream()
						.filter(ds -> !filteredOutDataSetNames.contains(ds.getName()))
						.collect(Collectors.toSet());
				viewPanel.getDataSetSelector().setCheckedItems(filteredDataSets);
				
				// Update Style options
				ChartOptions chartOptions = params.getChartOptions();
				viewPanel.getChartDataCombo().setSelectedItem(chartOptions != null ? chartOptions.getData() : null);
				viewPanel.updateChartCombos();
				viewPanel.getChartTypeCombo().setSelectedItem(chartOptions != null ? chartOptions.getType() : null);
				viewPanel.getChartColorsCombo().setSelectedItem(chartOptions != null ? chartOptions.getColorScheme() : null);
				viewPanel.getShowChartLabelsCheck().setSelected(chartOptions != null && chartOptions.isShowLabels());
				viewPanel.getPublicationReadyCheck().setSelected(params.isPublicationReady());
				viewPanel.updateChartDataCombo();
			} finally {
				updating = false;
			}
			
			if(updateScope != null) {
				updateVisualStyle(map, viewPanel, updateScope);
				filterNodesAndEdges(viewPanel, map);
			}
			
		});
	}
	
	public Map<Long, ViewParams> getAllViewParams() {
		Map<Long, ViewParams> map = new HashMap<>();
		
		getControlPanel().getAllControlPanels().forEach((suid, panel) -> {
			ViewParams params = createViewParams(suid, panel);
			map.put(suid, params);
		});
		
		return map;
	}
	
	public ViewParams getViewParams(Long networkViewSuid) {
		EMViewControlPanel panel = getControlPanel().getAllControlPanels().get(networkViewSuid);
		return createViewParams(networkViewSuid, panel);
	}
	
	private ViewParams createViewParams(Long networkViewSuid, EMViewControlPanel panel) {
		CutoffParam cuttofParam = panel.getPValueRadio().isSelected() ? CutoffParam.P_VALUE : CutoffParam.Q_VALUE;
		Double pVal = panel.getPValueSliderPanel() != null ? panel.getPValueSliderPanel().getValue() : null;
		Double qVal = panel.getQValueSliderPanel() != null ? panel.getQValueSliderPanel().getValue() : null;
		Double sCoeff = panel.getSimilaritySliderPanel() != null ? panel.getSimilaritySliderPanel().getValue() : null;
		
		Set<AbstractDataSet> uncheckedDataSets = panel.getUncheckedDataSets();
		Set<String> filteredDataSets = uncheckedDataSets.stream()
				.map(AbstractDataSet::getName)
				.collect(Collectors.toSet());
		
		EMStyleOptions options = createStyleOptions(panel.getNetworkView());
		ChartOptions chartOptions = options != null ? options.getChartOptions() : null;
		boolean pubReady = panel.getPublicationReadyCheck().isSelected();
		ViewParams params = new ViewParams(networkViewSuid, cuttofParam, pVal, qVal, sCoeff, filteredDataSets, chartOptions, pubReady);
		
		return params;
	}
	
	public double[] getPValueSliderValues(Long networkSuid) {
		EMViewControlPanel panel = getControlPanel().getAllControlPanels().get(networkSuid);
		SliderBarPanel sliderPanel = panel.getPValueSliderPanel();
		if(sliderPanel == null)
			return null;
		return new double[] { sliderPanel.getMin(), sliderPanel.getValue(), sliderPanel.getMax() };
	}
	
	public double[] getQValueSliderValues(Long networkSuid) {
		EMViewControlPanel panel = getControlPanel().getAllControlPanels().get(networkSuid);
		SliderBarPanel sliderPanel = panel.getQValueSliderPanel();
		if(sliderPanel == null)
			return null;
		return new double[] { sliderPanel.getMin(), sliderPanel.getValue(), sliderPanel.getMax() };
	}
	
	public void updateDataSetList(CyNetworkView netView) {
		EMViewControlPanel viewPanel = getControlPanel().getViewControlPanel(netView);
		viewPanel.updateDataSetSelector();
		updateLegends(viewPanel);
	}
	
	public EMStyleOptions createStyleOptions(CyNetworkView netView) {
		EnrichmentMap map = netView != null ? emManager.getEnrichmentMap(netView.getModel().getSUID()) : null;
		EMViewControlPanel viewPanel = getControlPanel().getViewControlPanel(netView);
		return createStyleOptions(map, viewPanel);
	}
	
	@Override
	public void handleEvent(SetCurrentNetworkViewEvent e) {
		if (getControlPanel().isDisplayable())
			setCurrentNetworkView(e.getNetworkView());
		
		invokeOnEDT(() -> {
			EMViewControlPanel viewPanel = getControlPanel().getViewControlPanel(e.getNetworkView());
			updateLegends(viewPanel);
		});
	}
	
	@Override
	public void handleEvent(RowsSetEvent e) {
		if(propertyManager.isFalse(PropertyManager.CONTROL_DATASET_SELECT_SYNC))
			return;
		
		if(e.containsColumn(CyNetwork.SELECTED)) {
			CyNetworkView networkView = applicationManager.getCurrentNetworkView();
			if(networkView != null) {
				CyNetwork network = networkView.getModel();
				// only handle event if it is a selected node
				if(e.getSource() == network.getDefaultEdgeTable() || e.getSource() == network.getDefaultNodeTable()) {
					selectionEventTimer.coalesce(() -> updateFromNodeSelection(networkView));
				}
			}
		}
	}
	
	private void updateFromNodeSelection(CyNetworkView networkView) {
		if(networkView == null)
			return;
		
		EnrichmentMap map = getCurrentMap();
		if(map == null)
			return;
		
		EMViewControlPanel controlPanel = getControlPanel().getViewControlPanel(networkView);
		if(controlPanel == null)
			return;
		
		CyNetwork network = networkView.getModel();
		List<CyNode> selectedNodes = CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, true);
		List<CyEdge> selectedEdges = CyTableUtil.getEdgesInState(network, CyNetwork.SELECTED, true);

		List<AbstractDataSet> dataSets = new ArrayList<>(map.getDataSetList());
		dataSets.addAll(map.getSignatureSetList());
				
		dataSets = HeatMapMediator.filterDataSetsForSelection(dataSets, map, selectedNodes, selectedEdges);
		
		controlPanel.getDataSetSelector().setHighlightedDataSets(dataSets);
	}

	@Override
	public void handleEvent(EnrichmentMapAddedEvent e) {
		invokeOnEDT(() -> {
			updating = true;
			try {
				getControlPanel().updateEmViewCombo();
			} finally {
				updating = false;
			}
		});
	}
	
	@Override
	public void handleEvent(EnrichmentMapAboutToBeRemovedEvent e) {
		final CyNetworkView netView = e.getNetworkView();
		
		Timer timer = filterTimers.remove(netView);
		if (timer != null)
			timer.stop();
		
		invokeOnEDT(() -> {
			getControlPanel().removeEnrichmentMapView(netView);
		});
	}
	
	private void setCurrentNetworkView(CyNetworkView netView) {
		invokeOnEDT(() -> {
			AbstractViewControlPanel viewPanel = null;
			updating = true;
			
			try {
				if (netView != null && !getControlPanel().contains(netView))
					viewPanel = addNetworkView(netView);
					
				getControlPanel().update(netView);
			} finally {
				updating = false;
			}
			
			if (viewPanel instanceof EMViewControlPanel)
				setDefaults((EMViewControlPanel) viewPanel, emManager.getEnrichmentMap(netView.getModel().getSUID()));
		});
	}
	
	/**
	 * Call this method on the EDT only!
	 */
	private AbstractViewControlPanel addNetworkView(CyNetworkView netView) {
		AbstractViewControlPanel viewPanel = null;
		var network = netView.getModel();
		EnrichmentMap map = emManager.getEnrichmentMap(network.getSUID());
		
		// Is the new view an EnrichmentMap one?
		if (map != null) {
			if (NetworkUtil.isAssociatedNetwork(network)) {
				var app = NetworkUtil.getAssociatedApp(network);
				viewPanel = getControlPanel().addAssociatedView(netView, app);
			} else {
				viewPanel = getControlPanel().addEnrichmentMapView(netView);
			}
			
			if (viewPanel instanceof EMViewControlPanel)
				addListeners((EMViewControlPanel) viewPanel, map);
			else if (viewPanel instanceof AssociatedViewControlPanel)
				addListeners((AssociatedViewControlPanel) viewPanel, map);
		}
		
		return viewPanel;
	}

	@SuppressWarnings("unchecked")
	private void addListeners(EMViewControlPanel viewPanel, EnrichmentMap map) {
		CyNetworkView netView = viewPanel.getNetworkView();
		
		Runnable updateFilters = () -> {
			if (!updating) {
				filterNodesAndEdges(viewPanel, map);
				ChartData data = (ChartData) viewPanel.getChartDataCombo().getSelectedItem();
				if(data == ChartData.DATA_SET) {
					updateVisualStyle(map, viewPanel, StyleUpdateScope.ONLY_CHARTS);
				}
			}
		};
		
		viewPanel.getQValueRadio().addActionListener(evt -> {
			viewPanel.updateFilterPanel();
			updateFilters.run();
		});
		viewPanel.getPValueRadio().addActionListener(evt -> {
			viewPanel.updateFilterPanel();
			updateFilters.run();
		});
		
		SliderBarPanel pvSliderPanel = viewPanel.getPValueSliderPanel();
		SliderBarPanel qvSliderPanel = viewPanel.getQValueSliderPanel();
		SliderBarPanel sSliderPanel = viewPanel.getSimilaritySliderPanel();
		
		
		if (pvSliderPanel != null)
			pvSliderPanel.addChangeListener(e -> updateFilters.run());
		if (qvSliderPanel != null)
			qvSliderPanel.addChangeListener(e -> updateFilters.run());
		if (sSliderPanel != null)
			sSliderPanel.addChangeListener(e -> updateFilters.run());

		viewPanel.getDataSetSelector().addPropertyChangeListener(DataSetSelector.PROP_CHECKED_DATA_SETS, evt -> {
			if (!updating) {
				viewPanel.updateChartDataCombo();
				
				filterNodesAndEdges(viewPanel, map);
				ChartData data = (ChartData) viewPanel.getChartDataCombo().getSelectedItem();
				
				Set<EMDataSet> oldDataSets = filterEMDataSets((Collection<AbstractDataSet>) evt.getOldValue());
				Set<EMDataSet> newDataSets = filterEMDataSets((Collection<AbstractDataSet>) evt.getNewValue());
				int oldSize = oldDataSets.size();
				int newSize = newDataSets.size();
				
				// Cases where changing the number of checked datasets (Signatures excluded)
				// requires the style to be updated:
				//    a) Chart data may change:
				boolean updateStyle = data != null && data != ChartData.NONE && oldSize != newSize;
				//    b) Node color/shape may change:
				updateStyle = updateStyle || oldSize == 0 && newSize > 0;
				updateStyle = updateStyle || oldSize > 0 && newSize == 0;
				
				if (updateStyle) {
					updateVisualStyle(map, viewPanel, StyleUpdateScope.ONLY_DATASETS);
					heatMapMediatorProvider.get().reset();
				} else {
					netView.updateView();
				}
			}
		});
		
		viewPanel.getDataSetSelector().getAddMenuItem().addActionListener(evt -> {
			paDialogMediatorProvider.get().showDialog(netView);
		});
		
		viewPanel.getDataSetSelector().getDataSetColorMenuItem().addActionListener(evt -> {
			boolean colorsChanged = showColorDialog(map);
			if(colorsChanged) {
				viewPanel.getDataSetSelector().update();
				heatMapMediatorProvider.get().reset();
				updateVisualStyle(map, viewPanel, StyleUpdateScope.ONLY_DATASETS);
			}
		});
		
		viewPanel.getDataSetSelector().getDeleteSignatureMenuItem().addActionListener(evt -> {
			removeSignatureDataSets(map, viewPanel);
		});
		
		viewPanel.getDataSetSelector().getSelectNodesMenuItem().addActionListener(evt -> {
			selectNodesEdges(
				map,
				viewPanel.getNetworkView(),
				viewPanel.getDataSetSelector().getSelectedItems(),
				map.getParams().getCreateDistinctEdges()
			);
		});
		
		viewPanel.getAutoAnnotateOpenLink().addMouseListener(new MouseAdapter() {
			@Override public void mouseClicked(MouseEvent e) {
				var task = autoAnnotateOpenTaskProvider.get();
				syncTaskManager.execute(new TaskIterator(task));
			}
		});
		
		viewPanel.getChartDataCombo().addItemListener(evt -> {
			if (!updating && evt.getStateChange() == ItemEvent.SELECTED) {
				updating = true;
				try {
					viewPanel.updateChartCombos();
				} finally {
					updating = false;
				}
				updateVisualStyle(map, viewPanel, StyleUpdateScope.ONLY_CHARTS);
			}
		});
		viewPanel.getChartTypeCombo().addItemListener(evt -> {
			if (!updating && evt.getStateChange() == ItemEvent.SELECTED) {
				updating = true;
				try {
					viewPanel.updateChartColorsCombo();
					viewPanel.updateChartLabelsCheck();
				} finally {
					updating = false;
				}
				updateVisualStyle(map, viewPanel, StyleUpdateScope.ONLY_CHARTS);
			}
		});
		viewPanel.getChartColorsCombo().addItemListener(evt -> {
			if (!updating && evt.getStateChange() == ItemEvent.SELECTED)
				updateVisualStyle(map, viewPanel, StyleUpdateScope.ONLY_CHARTS);
		});
		viewPanel.getShowChartLabelsCheck().addActionListener(evt -> {
			if (!updating)
				updateVisualStyle(map, viewPanel, StyleUpdateScope.ONLY_CHARTS);
		});
		viewPanel.getPublicationReadyCheck().addActionListener(evt -> {
			if (!updating)
				updateVisualStyle(map, viewPanel, StyleUpdateScope.PUBLICATION_READY);
		});
		
		// MKTODO we should have a warning dialog when resetting the entire style
		viewPanel.getResetStyleButton().addActionListener(evt -> resetStyle(map, viewPanel));
		viewPanel.getSetEdgeWidthButton().addActionListener(evt -> showEdgeWidthDialog());
		viewPanel.getShowLegendButton().addActionListener(evt -> showLegendDialog());
		
		viewPanel.updateChartDataCombo();
	}

	private void addListeners(AssociatedViewControlPanel viewPanel, EnrichmentMap map) {
		viewPanel.getChartDataCombo().addItemListener(evt -> {
			if (!updating && evt.getStateChange() == ItemEvent.SELECTED) {
				updating = true;
				
				try {
					viewPanel.update();
					updateAssociatedStyle(map, viewPanel);
				} finally {
					updating = false;
				}
			}
		});
		viewPanel.getNormCombo().addItemListener(evt -> {
			if (!updating && evt.getStateChange() == ItemEvent.SELECTED)
				updateAssociatedStyle(map, viewPanel);
		});
		viewPanel.getCompressCombo().addItemListener(evt -> {
			if (!updating && evt.getStateChange() == ItemEvent.SELECTED) {
				updating = true;
				
				try {
					viewPanel.updateChartTypeCombo();
					viewPanel.updateDataSetCombo();
					updateAssociatedStyle(map, viewPanel);
				} finally {
					updating = false;
				}
			}
		});
		viewPanel.getDataSetCombo().addItemListener(evt -> {
			if (!updating && evt.getStateChange() == ItemEvent.SELECTED)
				updateAssociatedStyle(map, viewPanel);
		});
		viewPanel.getChartTypeCombo().addItemListener(evt -> {
			if (!updating && evt.getStateChange() == ItemEvent.SELECTED)
				updateAssociatedStyle(map, viewPanel);
		});
		viewPanel.getResetStyleButton().addActionListener(evt -> {
			updateAssociatedStyle(map, viewPanel);
		});
	}
	
	/**
	 * Call this method on the EDT only!
	 */
	private void setDefaults(EMViewControlPanel viewPanel, EnrichmentMap map) {
		if(map.getDataSetCount() > 0) {
			EMCreationParameters params = map.getParams();
			
			ChartData chartData = ChartData.LOG10_PVAL; // default for multi-dataset
			if(map.isTwoPhenotypeGeneric()) {
				chartData = ChartData.PHENOTYPES;
			} else if(map.getDataSetCount() == 1) {
				chartData = ChartData.NONE; // one-dataset should show node fill color style mapping by default
			} else if(params != null && map.hasNonGSEADataSet() && params.isForceNES()) {
				chartData = ChartData.NES_VALUE;
			}
			
			viewPanel.getChartDataCombo().setSelectedItem(chartData);
		}
	}

	@AfterInjection
	private void init() {
		ControlPanel ctrlPanel = getControlPanel();

		ctrlPanel.getCreateEmButton().setToolTipText("" + masterMapDialogAction.getValue(Action.NAME));
		ctrlPanel.getCreateEmButton().addActionListener(evt -> {
			masterMapDialogAction.actionPerformed(evt);
		});
		
		ctrlPanel.getOptionsButton().addActionListener(evt -> {
			getOptionsMenu().show(ctrlPanel.getOptionsButton(), 0, ctrlPanel.getOptionsButton().getHeight());
		});
		
		ctrlPanel.getClosePanelButton().addActionListener(evt -> {
			closeControlPanel();
		});
		
		ctrlPanel.update(applicationManager.getCurrentNetworkView());
		
		// Wait until the UI is initialized to add this listener to the combo box
		JComboBox<CyNetworkView> cmb = ctrlPanel.getEmViewCombo();
		cmb.addActionListener(evt -> {
			if (!updating)
				setCurrentView((CyNetworkView) cmb.getSelectedItem());
		});
	}
	
	private ControlPanel getControlPanel() {
		return controlPanelProvider.get();
	}
	
	private void closeControlPanel() {
		serviceRegistrar.unregisterAllServices(getControlPanel());
		getControlPanel().dispose();
	}
	
	private void setCurrentView(CyNetworkView netView) {
		ForkJoinPool.commonPool().submit(() -> {
			// Work around a bug in Cytoscape.
			// When the current network view is changed it can lose its style, so set it back.
			CyNetworkView prevNetView = applicationManager.getCurrentNetworkView();
			VisualStyle visualStyle = visualMappingManager.getVisualStyle(prevNetView);
			applicationManager.setCurrentNetworkView(netView);
			visualMappingManager.setVisualStyle(visualStyle, prevNetView);
		});
	}
	
	private CyNetworkView getCurrentEMView() {
		return (CyNetworkView) getControlPanel().getEmViewCombo().getSelectedItem();
	}
	
	private EnrichmentMap getCurrentMap() {
		CyNetworkView view = getCurrentEMView();
		
		return view != null ? emManager.getEnrichmentMap(view.getModel().getSUID()) : null;
	}
	
	private Set<EMDataSet> getFilteredDataSets(EMViewControlPanel viewPanel) {
		if (viewPanel == null)
			return Collections.emptySet();
		
		return filterEMDataSets(viewPanel.getCheckedDataSets()); // Ignore Signature Data Sets
	}
	
	private ChartType getChartType(EMViewControlPanel viewPanel) {
		return (ChartType) viewPanel.getChartTypeCombo().getSelectedItem();
	}
	
	private void removeSignatureDataSets(EnrichmentMap map, EMViewControlPanel viewPanel) {
		Set<EMSignatureDataSet> dataSets = viewPanel.getSelectedSignatureDataSets();
		
		if (!dataSets.isEmpty()) {
			if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(
					getControlPanel(),
					"Are you sure you want to remove the selected Signature Gene Sets\nand associated nodes?",
					"Remove Signature Gene Sets",
					JOptionPane.YES_NO_OPTION
			))
				return;
			
			RemoveSignatureDataSetsTask task = removeDataSetsTaskFactory.create(dataSets , map);
			dialogTaskManager.execute(new TaskIterator(task), TaskUtil.allFinished(finishStatus -> {
					viewPanel.updateDataSetSelector();
					updateLegends(viewPanel);
					viewPanel.getNetworkView().updateView();
			}));
		}
	}
	
	private void updateLegends(EMViewControlPanel viewPanel) {
		legendPanelMediatorProvider.get().updateDialog(
				viewPanel != null ? createStyleOptions(viewPanel.getNetworkView()) : null,
				getFilteredDataSets(viewPanel)
		);
	}
	
	private void resetStyle(EnrichmentMap map, EMViewControlPanel viewPanel) {
		int result = JOptionPane.showConfirmDialog(
						viewPanel, 
						"Reset all style mappings for this network to their EnrichmentMap defaults?",
						"EnrichmentMap: Reset Style",
						JOptionPane.OK_CANCEL_OPTION);
		
		if(result == JOptionPane.OK_OPTION) {
			updateVisualStyle(map, viewPanel, StyleUpdateScope.ALL);
		}
	}
	
	private void updateVisualStyle(EnrichmentMap map, EMViewControlPanel viewPanel, StyleUpdateScope scope) {
		EMStyleOptions options = createStyleOptions(map, viewPanel);
		applyVisualStyle(options, scope);
	}

	public void applyVisualStyle(EMStyleOptions options, StyleUpdateScope scope) {
		var styleTask  = applyStyleTaskFactory.create(options, scope);
		var redrawTask = autoAnnotateRedrawTaskProvider.get();
		dialogTaskManager.execute(
			new TaskIterator(styleTask, redrawTask), 
			TaskUtil.allFinished(finishStatus -> {
				EMViewControlPanel viewPanel = getControlPanel().getViewControlPanel(options.getNetworkView());
				updateLegends(viewPanel);
			})
		);
	}
	
	private void updateAssociatedStyle(EnrichmentMap map, AssociatedViewControlPanel viewPanel) {
		CyNetworkView netView = viewPanel.getNetworkView();
		
		if (netView != null && map != null) {
			AssociatedStyleOptions options = createAssociatedStyleOptions(map, viewPanel);
			UpdateAssociatedStyleTask task = updateAssociatedStyleTaskFactory.create(options);
			dialogTaskManager.execute(new TaskIterator(task));
		}
	}
	
	private EMStyleOptions createStyleOptions(EnrichmentMap map, EMViewControlPanel viewPanel) {
		if (map == null || viewPanel == null)
			return null;
		
		Set<AbstractDataSet> checkedDataSets = viewPanel.getDataSetSelector().getCheckedItems();
		
		// Need to maintain the correct order
		List<AbstractDataSet> dataSetList = new ArrayList<>();
		for(AbstractDataSet ds : map.getDataSetList()) { // Need to maintain the same order that's in the EnrichmentMap object
			if(checkedDataSets.contains(ds)) {
				dataSetList.add(ds);
			}
		}
		
		boolean publicationReady = viewPanel.getPublicationReadyCheck().isSelected();
		boolean postAnalysis = map.hasSignatureDataSets();
		
		ChartData data = (ChartData) viewPanel.getChartDataCombo().getSelectedItem();
		ChartType type;
		ColorScheme colorScheme;
		
		if (data == ChartData.DATA_SET) {
			type = ChartType.DATASET_PIE;
			colorScheme = null;
		} else {
			type = getChartType(viewPanel);
			colorScheme = (ColorScheme) viewPanel.getChartColorsCombo().getSelectedItem();
		}
		
		boolean showLabels = viewPanel.getShowChartLabelsCheck().isSelected();
		ChartOptions chartOptions = new ChartOptions(data, type, colorScheme, showLabels);

		return new EMStyleOptions(viewPanel.getNetworkView(), map, dataSetList, chartOptions, postAnalysis, publicationReady);
	}
	
	private AssociatedStyleOptions createAssociatedStyleOptions(EnrichmentMap map, AssociatedViewControlPanel viewPanel) {
		CyNetworkView netView = viewPanel.getNetworkView();
		final ChartData data = viewPanel.getChartData();
		final Transform transform = viewPanel.getTransform();
		final Compress compress = viewPanel.getCompress() != null ? viewPanel.getCompress() : Compress.NONE;
		final ChartType type = viewPanel.getChartType();
		final EMDataSet ds = viewPanel.getDataSet();
		final AssociatedApp app = NetworkUtil.getAssociatedApp(netView.getModel());
		List<EMDataSet> datasets = ds != null ? Collections.singletonList(ds) : map.getDataSetList();
		
		ExpressionData exp = data == ChartData.EXPRESSION_DATA ? createExpressionData(map, datasets, transform, compress) : null;
		ChartOptions chartOptions = data != null ? new ChartOptions(data, type, null, false) : null;

		return new AssociatedStyleOptions(netView, map, transform, compress, exp, chartOptions, app);
	}
	
	private ExpressionData createExpressionData(EnrichmentMap map, List<EMDataSet> datasets, Transform transform, Compress compress) {
		ExpressionData exp = null;
		ExpressionCache cache = new ExpressionCache();
		
		switch (compress) {
			case DATASET_MEDIAN:
			case DATASET_MAX:
			case DATASET_MIN:
				boolean isDistinctExpressionSets = map != null && map.isDistinctExpressionSets();
				exp = new CompressedDataSet(datasets, cache, isDistinctExpressionSets);
				break;
			case CLASS_MEDIAN:
			case CLASS_MAX:
			case CLASS_MIN:
				exp = new CompressedClass(datasets, cache);
				break;
			default:
				exp = new Uncompressed(datasets, cache);
				break;
		}
		
		return exp;
	}
	
	/**
	 * Show Post Analysis Edge Width dialog"
	 */
	private void showEdgeWidthDialog() {
		if (WidthFunction.appliesTo(applicationManager.getCurrentNetwork())) {
			EdgeWidthDialog dialog = dialogProvider.get();
			dialog.pack();
			dialog.setLocationRelativeTo(swingApplication.getJFrame());
			dialog.setVisible(true);
		} else {
			JOptionPane.showMessageDialog(
					getControlPanel(),
					"Please add signature gene sets first.",
					"EnrichmentMap Edge Width",
					JOptionPane.WARNING_MESSAGE
			);
		}
	}
	
	private boolean showColorDialog(EnrichmentMap map) {
		var dialog = colorSelectorDialogFactory.create(map.getDataSetList());
		dialog.pack();
		dialog.setLocationRelativeTo(swingApplication.getJFrame());
		dialog.setModal(true);
		dialog.setVisible(true);
		return dialog.colorsChanged();
	}
	
	private void filterNodesAndEdges(EMViewControlPanel viewPanel, EnrichmentMap map) {
		CyNetworkView netView = viewPanel.getNetworkView();
		Timer timer = filterTimers.get(netView);
		
		if (timer == null) {
			timer = new Timer(0, new FilterActionListener(viewPanel, map, netView));
			timer.setRepeats(false);
			timer.setCoalesce(true);
			filterTimers.put(netView, timer);
		} else {
			for (ActionListener listener : timer.getActionListeners()) {
				if (listener instanceof FilterActionListener)
					((FilterActionListener) listener).cancel();
			}
			
			timer.stop();
		}
		
		timer.setInitialDelay(400);
		timer.start();
	}
	
	
	private void showLegendDialog() {
		if (legendPanelMediatorProvider.get().getDialog().isVisible()) {
			legendPanelMediatorProvider.get().hideDialog();
		} else {
			CyNetworkView netView = getCurrentEMView();
			EMViewControlPanel viewPanel = getControlPanel().getViewControlPanel(netView);
			legendPanelMediatorProvider.get().showDialog(createStyleOptions(netView), getFilteredDataSets(viewPanel));
		}
	}
	
	private JPopupMenu getOptionsMenu() {
		final JPopupMenu menu = new JPopupMenu();
		
		{
			JMenuItem showLegendItem = new JCheckBoxMenuItem("Show Legend");
			showLegendItem.addActionListener(evt -> showLegendDialog());
			showLegendItem.setSelected(legendPanelMediatorProvider.get().getDialog().isVisible());
			menu.add(showLegendItem);
			
			JMenuItem showParamsItem = new JMenuItem("Show Creation Parameters");
			showParamsItem.addActionListener(evt -> {
				CyNetworkView netView = getCurrentEMView();
				showCreationParamsDialog(netView);
			});
			menu.add(showParamsItem);
		}
		
		menu.addSeparator();
		{
			int iconSize = 16;
			menu.add(genemaniaAction).setIcon(new TextIcon(IconUtil.GENEMANIA_ICON, IconUtil.getIconFont(14.0f), IconUtil.GENEMANIA_ICON_COLOR, iconSize, iconSize));
			menu.add(stringAction).setIcon(new TextIcon(IconUtil.LAYERED_STRING_ICON, IconUtil.getIconFont(16.0f), IconUtil.STRING_ICON_COLORS, iconSize, iconSize));
		}
		
		menu.addSeparator();
		
		for (FilterMode mode : FilterMode.values()) {
			final JMenuItem mi = new JCheckBoxMenuItem(mode.toString());
			mi.addActionListener(evt -> setFilterMode(mode));
			mi.setSelected(filterMode == mode);
			menu.add(mi);
		}
		
		menu.addSeparator();
		
		menu.add(new JMenuItem(showAboutDialogActionProvider.get()));
		
		return menu;
	}
	
	
	private void showCreationParamsDialog(CyNetworkView netView) {
		JDialog d = new JDialog(swingApplication.getJFrame(), "EnrichmentMap Creation Parameters", ModalityType.APPLICATION_MODAL);
		d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		d.setMinimumSize(new Dimension(420, 260));
		d.setPreferredSize(new Dimension(580, 460));
		
		@SuppressWarnings("serial")
		JButton closeButton = new JButton(new AbstractAction("Close") {
			@Override
			public void actionPerformed(ActionEvent e) {
				d.dispose();
			}
		});
		
		JPanel bottomPanel = LookAndFeelUtil.createOkCancelPanel(null, closeButton);
		d.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
		
		if (netView != null) {
			EnrichmentMap map = emManager.getEnrichmentMap(netView.getModel().getSUID());
			CreationParametersPanel paramsPanel = new CreationParametersPanel(map);
			d.getContentPane().add(paramsPanel, BorderLayout.CENTER);
		}
		
		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(d.getRootPane(), null, closeButton.getAction());
		d.getRootPane().setDefaultButton(closeButton);
		
		d.setLocationRelativeTo(swingApplication.getJFrame());
		d.pack();
		d.setVisible(true);
	}
	
	
	private void setFilterMode(FilterMode filterMode) {
		this.filterMode = filterMode;
	}
	
	@SuppressWarnings("unchecked")
	private static Set<EMDataSet> filterEMDataSets(Collection<AbstractDataSet> abstractDataSets) {
		Set<?> set = abstractDataSets.stream()
				.filter(ds -> ds instanceof EMDataSet) // Ignore Signature Data Sets
				.collect(Collectors.toSet());
		
		return (Set<EMDataSet>) set;
	}
	
	private void showContextMenu(final JPopupMenu contextMenu, final MouseEvent e) {
		invokeOnEDT(() -> {
			final Component parent = (Component) e.getSource();
			contextMenu.show(parent, e.getX(), e.getY());
		});
	}
	
	private void selectNodesEdges(EnrichmentMap map, CyNetworkView netView, Set<AbstractDataSet> dataSets, boolean distinctEdges) {
		SelectNodesEdgesTask task = selectNodesEdgesTaskFactory.create(map, netView, dataSets, distinctEdges);
		dialogTaskManager.execute(new TaskIterator(task));
	}
	
	private class FilterActionListener implements ActionListener {

		private final EMViewControlPanel viewPanel;
		private final EnrichmentMap map;
		private final CyNetworkView netView;
		private FilterNodesEdgesTask task;
		
		private boolean cancelled;
		
		public FilterActionListener(EMViewControlPanel viewPanel, EnrichmentMap map, CyNetworkView netView) {
			this.viewPanel = viewPanel;
			this.map = map;
			this.netView = netView;
		}
		
		@Override
		public void actionPerformed(ActionEvent evt) {
			task = null;
			cancelled = false;
			
			Set<AbstractDataSet> selectedDataSets = viewPanel.getCheckedDataSets();
			
			// Find nodes and edges that must be displayed
			Set<CyNode> filteredInNodes = getFilteredInNodes(selectedDataSets);
			if (cancelled)
				return;
			
			Set<CyEdge> filteredInEdges = getFilteredInEdges(selectedDataSets);
			if (cancelled)
				return;
			
			// Run Task
			task = filterNodesEdgesTaskFactory.create(map, netView, filteredInNodes, filteredInEdges, filterMode);
			dialogTaskManager.execute(new TaskIterator(task), TaskUtil.allFinished(finishStatus -> {
				task = null;
				if (!cancelled)
					netView.updateView();
			}));
		}
		
		public void cancel() {
			cancelled = true;
			
			if (task != null) {
				try {
					task.cancel();
					task = null;
				} catch(NullPointerException e) {}
			}
		}

		private Set<CyNode> getFilteredInNodes(Set<AbstractDataSet> selectedDataSets) {
			Set<Long> dataSetNodes = EnrichmentMap.getNodesUnion(selectedDataSets);
			EMCreationParameters params = map.getParams();
			
			// Only p or q value, but not both!
			if (viewPanel.getPValueSliderPanel() != null && viewPanel.getPValueSliderPanel().isVisible()) {
				Set<String> columnNames = FilterUtil.getColumnNames(params.getPValueColumnNames(), selectedDataSets);
				return getFilteredInNodes(viewPanel.getPValueSliderPanel(), map, netView, columnNames, dataSetNodes);
			}
			
			if (viewPanel.getQValueSliderPanel() != null && viewPanel.getQValueSliderPanel().isVisible()) {
				Set<String> columnNames = FilterUtil.getColumnNames(params.getQValueColumnNames(), selectedDataSets);
				return getFilteredInNodes(viewPanel.getQValueSliderPanel(), map, netView, columnNames, dataSetNodes);
			}
			
			Set<CyNode> filteredInNodes = new HashSet<>();
			CyNetwork net = netView.getModel();
				
			for (CyNode n : net.getNodeList()) {
				if (dataSetNodes.contains(n.getSUID()))
					filteredInNodes.add(n);
			}
			
			return filteredInNodes; 
		}

		private Set<CyEdge> getFilteredInEdges(Set<AbstractDataSet> selectedDataSets) {
			EMCreationParameters params = map.getParams();

			// Compound edges are not associated with a specific data set
			Set<Long> dataSetEdges = EnrichmentMap.getEdgesUnionForFiltering(selectedDataSets, map, netView.getModel());
			
			var sliderPanel = viewPanel.getSimilaritySliderPanel();
			if (sliderPanel != null) {
				var names = params.getSimilarityCutoffColumnNames();
				return getFilteredInEdges(sliderPanel, map, netView, names, dataSetEdges);
			}
			
			Set<CyEdge> filteredInEdges = new HashSet<>();
			CyNetwork net = netView.getModel();
			boolean distinct = params.getCreateDistinctEdges();
			
			if (distinct) {
				for (CyEdge e : net.getEdgeList()) {
					if (dataSetEdges.contains(e.getSUID())) {
						filteredInEdges.add(e);
					}
				}
			} else {
				// If compound edges, all edges are filtered in, no matter the selected data sets
				filteredInEdges.addAll(net.getEdgeList());
			}
			
			return filteredInEdges;
		}
		
		private Set<CyNode> getFilteredInNodes(SliderBarPanel sliderPanel, EnrichmentMap map, CyNetworkView networkView,
				Set<String> columnNames, Set<Long> dataSetNodes) {
			Set<CyNode> nodes = new HashSet<>();
			
			Double maxCutoff = sliderPanel.getValue();
			Double minCutoff = sliderPanel.getMin();
			
			CyNetwork network = networkView.getModel();
			CyTable table = network.getDefaultNodeTable();

			EMCreationParameters params = map.getParams();
			
			// Get the prefix of the current network
			final String prefix = params.getAttributePrefix();
			
			// Go through all the existing nodes to see if we need to hide any new nodes.
			for (CyNode n : network.getNodeList()) {
				boolean show = true;
				CyRow row = network.getRow(n);
				
				if (dataSetNodes != null && !dataSetNodes.contains(n.getSUID())) {
					show = false;
				} else if (table.getColumn(prefix + NODE_GS_TYPE) != null
						&& NODE_GS_TYPE_ENRICHMENT.equalsIgnoreCase(row.get(prefix + NODE_GS_TYPE, String.class))) {
					show = FilterUtil.passesFilter(columnNames, table, row, maxCutoff, minCutoff);
				}
				
				if (show)
					nodes.add(n);
			}
			
			return nodes;
		}

		/**
		 * If dataSetEdges is null, don't filter by Data Sets.
		 */
		private Set<CyEdge> getFilteredInEdges(SliderBarPanel sliderPanel, EnrichmentMap map, CyNetworkView networkView,
				Set<String> columnNames, Set<Long> dataSetEdges) {
			Set<CyEdge> edges = new HashSet<>();
			
			Double maxCutoff = sliderPanel.getMax();
			Double minCutoff = sliderPanel.getValue();
			
			CyNetwork network = networkView.getModel();
			CyTable table = network.getDefaultEdgeTable();

			// Go through all the existing edges to see if we need to hide any new ones.
			for (CyEdge e : network.getEdgeList()) {
				CyRow row = network.getRow(e);
				String dataset = Columns.EDGE_DATASET.get(row, map.getParams().getAttributePrefix());
				
				boolean show;
				if(EDGE_DATASET_VALUE_COMPOUND.equals(dataset)) { // compound edge
					show = FilterUtil.passesFilter(columnNames, table, row, maxCutoff, minCutoff);
				}
				else { // discrete edge
					if (dataSetEdges != null && !dataSetEdges.contains(e.getSUID())) {
						show = false;
					} else {
						String interaction = row.get(CyEdge.INTERACTION, String.class);
						if (EDGE_INTERACTION_VALUE_SIG.equals(interaction)) { 
							show = true;
						} else {
							show = FilterUtil.passesFilter(columnNames, table, row, maxCutoff, minCutoff);
						}
					}
				}
				
				if (show)
					edges.add(e);
			}
			
			return edges;
		}
	}
}
