package org.baderlab.csplugins.enrichmentmap.view.control;

import static org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder.FILTERED_OUT_EDGE_TRANSPARENCY;
import static org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder.FILTERED_OUT_NODE_TRANSPARENCY;
import static org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder.Columns.NODE_GS_TYPE;
import static org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder.Columns.NODE_GS_TYPE_ENRICHMENT;
import static org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil.invokeOnEDT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LABEL_TRANSPARENCY;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_TRANSPARENCY;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_VISIBLE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_BORDER_TRANSPARENCY;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_LABEL_TRANSPARENCY;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_TRANSPARENCY;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_VISIBLE;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.Timer;

import org.baderlab.csplugins.enrichmentmap.AfterInjection;
import org.baderlab.csplugins.enrichmentmap.actions.ShowEnrichmentMapDialogAction;
import org.baderlab.csplugins.enrichmentmap.model.AbstractDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMSignatureDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.style.ChartData;
import org.baderlab.csplugins.enrichmentmap.style.ChartFactoryManager;
import org.baderlab.csplugins.enrichmentmap.style.ChartType;
import org.baderlab.csplugins.enrichmentmap.style.ColorScheme;
import org.baderlab.csplugins.enrichmentmap.style.ColumnDescriptor;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleOptions;
import org.baderlab.csplugins.enrichmentmap.style.NullCustomGraphics;
import org.baderlab.csplugins.enrichmentmap.style.WidthFunction;
import org.baderlab.csplugins.enrichmentmap.task.ApplyEMStyleTask;
import org.baderlab.csplugins.enrichmentmap.task.RemoveSignatureDataSetsTask;
import org.baderlab.csplugins.enrichmentmap.view.control.ControlPanel.EMViewControlPanel;
import org.baderlab.csplugins.enrichmentmap.view.parameters.LegendPanelMediator;
import org.baderlab.csplugins.enrichmentmap.view.postanalysis.EdgeWidthDialog;
import org.baderlab.csplugins.enrichmentmap.view.postanalysis.PostAnalysisPanelMediator;
import org.baderlab.csplugins.enrichmentmap.view.util.SliderBarPanel;
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
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedEvent;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedListener;
import org.cytoscape.view.model.events.NetworkViewAddedEvent;
import org.cytoscape.view.model.events.NetworkViewAddedListener;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifier;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class ControlPanelMediator implements SetCurrentNetworkViewListener, NetworkViewAddedListener,
		NetworkViewAboutToBeDestroyedListener {

	private enum FilterMode {
		HIDE("Hide filtered out nodes and edges"),
		HIGHLIGHT("Highlight filtered nodes and edges"),
		SELECT("Select filtered nodes and edges");
		
		private final String label;

		private FilterMode(String label) {
			this.label = label;
		}
		
		@Override
		public String toString() {
			return label;
		}
	}

	@Inject private Provider<ControlPanel> controlPanelProvider;
	@Inject private Provider<LegendPanelMediator> legendPanelMediatorProvider;
	@Inject private Provider<PostAnalysisPanelMediator> postAnalysisPanelMediatorProvider;
	@Inject private Provider<EdgeWidthDialog> dialogProvider;
	@Inject private EnrichmentMapManager emManager;
	@Inject private ShowEnrichmentMapDialogAction masterMapDialogAction;
	
	@Inject private CyServiceRegistrar serviceRegistrar;
	@Inject private CyApplicationManager applicationManager;
	@Inject private CySwingApplication swingApplication;
	@Inject private CyNetworkViewManager networkViewManager;
	@Inject private CyNetworkManager networkManager;
	@Inject private RenderingEngineManager renderingEngineManager;
	@Inject private ApplyEMStyleTask.Factory applyStyleTaskFactory;
	@Inject private RemoveSignatureDataSetsTask.Factory removeDataSetsTaskFactory;
	@Inject private DialogTaskManager dialogTaskManager;
	@Inject private CyColumnIdentifierFactory columnIdFactory;
	@Inject private ChartFactoryManager chartFactoryManager;
	
	private FilterMode filterMode = FilterMode.HIDE;
	
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
				controlPanelProvider.get().getCreateEmButton().doClick();
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
	
	public void reset() {
		invokeOnEDT(() -> {
			for (CyNetworkView view : networkViewManager.getNetworkViewSet()) {
				getControlPanel().removeEnrichmentMapView(view);
			}

			Map<Long, EnrichmentMap> maps = emManager.getAllEnrichmentMaps();
			
			for (EnrichmentMap map : maps.values()) {
				CyNetwork network = networkManager.getNetwork(map.getNetworkID());
				Collection<CyNetworkView> networkViews = networkViewManager.getNetworkViews(network);
				
				for (CyNetworkView netView : networkViews) {
					addNetworkView(netView);
				}
			}

			setCurrentNetworkView(applicationManager.getCurrentNetworkView());
		});
	}
	
	public void updateDataSetList(CyNetworkView netView) {
		getControlPanel().getViewControlPanel(netView).updateDataSetSelector();
	}
	
	public EMStyleOptions createStyleOptions(CyNetworkView netView) {
		EnrichmentMap map = emManager.getEnrichmentMap(netView.getModel().getSUID());
		EMViewControlPanel viewPanel = getControlPanel().getViewControlPanel(netView);

		return createStyleOptions(map, viewPanel);
	}
	
	public CyCustomGraphics2<?> createChart(CyNetworkView netView, EMStyleOptions options) {
		EMViewControlPanel viewPanel = getControlPanel().getViewControlPanel(netView);
		
		return createChart(viewPanel, options);
	}
	
	@Override
	public void handleEvent(SetCurrentNetworkViewEvent e) {
		if (getControlPanel().isDisplayable())
			setCurrentNetworkView(e.getNetworkView());
	}
	
	@Override
	public void handleEvent(NetworkViewAddedEvent e) {
		CyNetworkView netView = e.getNetworkView();
		
		if (netView != null && emManager.getEnrichmentMap(netView.getModel().getSUID()) != null) {
			invokeOnEDT(() -> {
				updating = true;
			
				try {
					getControlPanel().updateEmViewCombo();
				} finally {
					updating = false;
				}
			});
		}
	}
	
	@Override
	public void handleEvent(NetworkViewAboutToBeDestroyedEvent e) {
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
			updating = true;
			
			try {
				if (netView != null && !getControlPanel().contains(netView))
					addNetworkView(netView);
					
				getControlPanel().update(netView);
			} finally {
				updating = false;
			}
		});
	}
	
	private void addNetworkView(CyNetworkView netView) {
		invokeOnEDT(() -> {
			EnrichmentMap map = emManager.getEnrichmentMap(netView.getModel().getSUID());
			
			// Is the new view an Enrichment Map one?
			if (map != null) {
				EMViewControlPanel viewPanel = getControlPanel().addEnrichmentMapView(netView);
				
				if (viewPanel != null) {
					// Add listeners to the new panel's fields
					viewPanel.getQValueRadio().addActionListener(evt -> {
						viewPanel.updateFilterPanel();
						filterNodesAndEdges(viewPanel, map, netView);
					});
					viewPanel.getPValueRadio().addActionListener(evt -> {
						viewPanel.updateFilterPanel();
						filterNodesAndEdges(viewPanel, map, netView);
					});
					
					SliderBarPanel pvSliderPanel = viewPanel.getPValueSliderPanel();
					SliderBarPanel qvSliderPanel = viewPanel.getQValueSliderPanel();
					SliderBarPanel sSliderPanel = viewPanel.getSimilaritySliderPanel();
					
					if (pvSliderPanel != null)
						pvSliderPanel.addPropertyChangeListener("value",
								evt -> filterNodesAndEdges(viewPanel, map, netView));
					if (qvSliderPanel != null)
						qvSliderPanel.addPropertyChangeListener("value",
								evt -> filterNodesAndEdges(viewPanel, map, netView));
					if (sSliderPanel != null)
						sSliderPanel.addPropertyChangeListener("value",
								evt -> filterNodesAndEdges(viewPanel, map, netView));

					viewPanel.getDataSetSelector().addPropertyChangeListener("selectedData", evt -> {
						if (!updating) {
							viewPanel.updateChartDataCombo(viewPanel.getDataSetSelector().getCheckedItems());
							
							filterNodesAndEdges(viewPanel, map, netView);
							ChartData data = (ChartData) viewPanel.getChartDataCombo().getSelectedItem();
							
							if (data != null && data != ChartData.NONE)
								updateVisualStyle(map, viewPanel);
							else
								netView.updateView();
						}
					});
					
					viewPanel.getDataSetSelector().getAddButton().addActionListener(evt -> {
						postAnalysisPanelMediatorProvider.get().showDialog(viewPanel, netView);
					});
					viewPanel.getDataSetSelector().getRemoveButton().addActionListener(evt -> {
						removeSignatureDataSets(map, viewPanel);
					});
					
					viewPanel.getChartDataCombo().addItemListener(evt -> {
						if (evt.getStateChange() == ItemEvent.SELECTED) {
							updating = true;
							
							try {
								viewPanel.updateChartCombos();
							} finally {
								updating = false;
							}
							
							updateVisualStyle(map, viewPanel);
						}
					});
					viewPanel.getChartTypeCombo().addItemListener(evt -> {
						if (!updating && evt.getStateChange() == ItemEvent.SELECTED) {
							updating = true;
							
							try {
								viewPanel.updateChartColorsCombo();
							} finally {
								updating = false;
							}
							
							updateVisualStyle(map, viewPanel);
						}
					});
					viewPanel.getChartColorsCombo().addItemListener(evt -> {
						if (!updating && evt.getStateChange() == ItemEvent.SELECTED)
							updateVisualStyle(map, viewPanel);
					});
					
					viewPanel.getPublicationReadyCheck().addActionListener(evt -> {
						updateVisualStyle(map, viewPanel);
					});
					
					viewPanel.getResetStyleButton().addActionListener(evt -> {
						updateVisualStyle(map, viewPanel);
					});
					
					viewPanel.getSetEdgeWidthButton().addActionListener(evt -> {
						showEdgeWidthDialog();
					});
					
					viewPanel.updateChartDataCombo(viewPanel.getDataSetSelector().getCheckedItems());
				}
			}
		});
	}

	@AfterInjection
	private void init() {
		ControlPanel ctrlPanel = getControlPanel();
		
		JComboBox<CyNetworkView> cmb = ctrlPanel.getEmViewCombo();
		cmb.addActionListener(evt -> {
			if (!updating)
				setCurrentView((CyNetworkView) cmb.getSelectedItem());
		});
		
		ctrlPanel.getCreateEmButton().addActionListener(evt -> {
			masterMapDialogAction.actionPerformed(evt);
		});
		ctrlPanel.getCreateEmButton().setToolTipText("" + masterMapDialogAction.getValue(Action.NAME));
		
		ctrlPanel.getOptionsButton().addActionListener(evt -> {
			getOptionsMenu().show(ctrlPanel.getOptionsButton(), 0, ctrlPanel.getOptionsButton().getHeight());
		});
		
		ctrlPanel.getClosePanelButton().addActionListener(evt -> {
			closeControlPanel();
		});
		
		ctrlPanel.update(applicationManager.getCurrentNetworkView());
	}
	
	private ControlPanel getControlPanel() {
		return controlPanelProvider.get();
	}
	
	private void closeControlPanel() {
		serviceRegistrar.unregisterAllServices(getControlPanel());
		getControlPanel().dispose();
	}
	
	private void setCurrentView(CyNetworkView netView) {
		applicationManager.setCurrentNetworkView(netView);
	}
	
	private CyNetworkView getCurrentEMView() {
		return (CyNetworkView) getControlPanel().getEmViewCombo().getSelectedItem();
	}
	
	private EnrichmentMap getCurrentMap() {
		CyNetworkView view = getCurrentEMView();
		
		return view != null ? emManager.getEnrichmentMap(view.getModel().getSUID()) : null;
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
			dialogTaskManager.execute(new TaskIterator(task), new TaskObserver() {
				@Override
				public void taskFinished(ObservableTask task) {
				}
				@Override
				public void allFinished(FinishStatus finishStatus) {
					viewPanel.updateDataSetSelector();
				}
			});
		}
	}
	
	private void updateVisualStyle(EnrichmentMap map, EMViewControlPanel viewPanel) {
		EMStyleOptions options = createStyleOptions(map, viewPanel);
		CyCustomGraphics2<?> chart = createChart(viewPanel, options);
		applyVisualStyle(options, chart);
	}

	private void applyVisualStyle(EMStyleOptions options, CyCustomGraphics2<?> chart) {
		ApplyEMStyleTask task = applyStyleTaskFactory.create(options, chart);
		dialogTaskManager.execute(new TaskIterator(task));
	}
	
	private EMStyleOptions createStyleOptions(EnrichmentMap map, EMViewControlPanel viewPanel) {
		Set<AbstractDataSet> dataSets = ImmutableSet.copyOf(viewPanel.getDataSetSelector().getCheckedItems());
		boolean publicationReady = viewPanel.getPublicationReadyCheck().isSelected();
		boolean postAnalysis = map.hasSignatureDataSets();
		EMStyleOptions options =
				new EMStyleOptions(viewPanel.getNetworkView(), map, dataSets::contains, postAnalysis, publicationReady);

		return options;
	}
	
	private CyCustomGraphics2<?> createChart(EMViewControlPanel viewPanel, EMStyleOptions options) {
		CyCustomGraphics2<?> chart = null;
		Set<AbstractDataSet> dataSets = viewPanel.getDataSetSelector().getCheckedItems();
		
		if (dataSets != null && dataSets.size() > 1) {
			ChartData data = (ChartData) viewPanel.getChartDataCombo().getSelectedItem();
			ChartType type = (ChartType) viewPanel.getChartTypeCombo().getSelectedItem();
			ColorScheme colorScheme = (ColorScheme) viewPanel.getChartColorsCombo().getSelectedItem();
			chart = createChart(data, type, colorScheme, options);
		}
		
		return chart;
	}
	
	private CyCustomGraphics2<?> createChart(ChartData data, ChartType type, ColorScheme colorScheme,
			EMStyleOptions options) {
		CyCustomGraphics2<?> chart = null;
		
		if (data != null && data != ChartData.NONE) {
			ColumnDescriptor<Double> columnDescriptor = data.getColumnDescriptor();
			
			List<CyColumnIdentifier> columns = 
					options.getDataSets().stream()
					.map(ds -> columnDescriptor.with(options.getAttributePrefix(), ds.getName()))  // column name
					.map(columnIdFactory::createColumnIdentifier) // column id
					.collect(Collectors.toList());
			
			Map<String, Object> props = new HashMap<>(type.getProperties());
			props.put("cy_dataColumns", columns);
			
			if (type == ChartType.LINE) {
				props.put("cy_colors", colorScheme.getColors(1));
			} else {
				if (colorScheme == ColorScheme.RANDOM)
					props.put("cy_colors", colorScheme.getColors(columns.size()));
				else
					props.put("cy_colorScheme", colorScheme.getKey());
			}
			
			try {
				CyCustomGraphics2Factory<?> factory = chartFactoryManager.getChartFactory(type.getId());
				
				if (factory != null)
					chart = factory.getInstance(props);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return chart;
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
	
	private void filterNodesAndEdges(EMViewControlPanel viewPanel, EnrichmentMap map, CyNetworkView netView) {
		Timer timer = filterTimers.get(netView);
		
		if (timer == null) {
			timer = new Timer(0, new FilterActionListener(viewPanel, map, netView));
			timer.setRepeats(false);
			timer.setCoalesce(true);
			filterTimers.put(netView, timer);
		} else {
			timer.stop();
		}
		
		timer.setInitialDelay(250);
		timer.start();
	}
	
	private Set<CyNode> getFilteredInNodes(SliderBarPanel sliderPanel, EnrichmentMap map, CyNetworkView networkView,
			Set<String> columnNames, Set<Long> dataSetNodes) {
		Set<CyNode> nodes = new HashSet<>();
		
		Double maxCutoff = (double) sliderPanel.getValue() / sliderPanel.getPrecision();
		Double minCutoff = (double) sliderPanel.getMin() / sliderPanel.getPrecision();
		
		CyNetwork network = networkView.getModel();
		CyTable table = network.getDefaultNodeTable();

		EMCreationParameters params = map.getParams();
		
		// Get the prefix of the current network
		final String prefix = params.getAttributePrefix();
		
		// Go through all the existing nodes to see if we need to hide any new nodes.
		for (CyNode n : network.getNodeList()) {
			boolean show = true;
			CyRow row = network.getRow(n);
			
			if (!dataSetNodes.contains(n.getSUID())) {
				show = false;
			} else if (table.getColumn(prefix + NODE_GS_TYPE) != null
					&& NODE_GS_TYPE_ENRICHMENT.equalsIgnoreCase(row.get(prefix + NODE_GS_TYPE, String.class))) {
				// Skip Node if it's not an Enrichment-Geneset (but e.g. a Signature-Hub)...
				for (String colName : columnNames) {
					if (table.getColumn(colName) == null)
						continue; // Ignore this column name (maybe the user deleted it)

					Double value = row.get(colName, Double.class);

					// Possible that there isn't a cutoff value for this geneset
					if (value == null)
						continue;

					if (value >= minCutoff && value <= maxCutoff) {
						show = true;
						break;
					} else {
						show = false;
					}
				}
			}
			
			if (show)
				nodes.add(n);
		}
		
		return nodes;
	}

	private Set<CyEdge> getFilteredInEdges(SliderBarPanel sliderPanel, EnrichmentMap map, CyNetworkView networkView,
			Set<String> columnNames) {
		Set<CyEdge> edges = new HashSet<>();
		
		JSlider slider = sliderPanel.getSlider();
		Double maxCutoff = slider.getMaximum() / sliderPanel.getPrecision();
		Double minCutoff = slider.getValue() / sliderPanel.getPrecision();
		
		CyNetwork network = networkView.getModel();
		CyTable table = network.getDefaultEdgeTable();

		// Go through all the existing edges to see if we need to hide any new ones.
		for (CyEdge e : network.getEdgeList()) {
			boolean show = true;
			CyRow row = network.getRow(e);
			
			for (String colName : columnNames) {
				if (table.getColumn(colName) == null)
					continue; // Ignore this column name (maybe the user deleted it)

				Double value = row.get(colName, Double.class);

				// Possible that there isn't value for this interaction
				if (value == null)
					continue;

				if (value >= minCutoff && value <= maxCutoff) {
					show = true;
					break;
				} else {
					show = false;
				}
			}
		
			if (show)
				edges.add(e);
		}
		
		return edges;
	}
	
	private JPopupMenu getOptionsMenu() {
		final JPopupMenu menu = new JPopupMenu();
		
		{
			final JMenuItem mi = new JCheckBoxMenuItem("Show Legend");
			mi.addActionListener(evt -> {
				if (legendPanelMediatorProvider.get().getDialog().isVisible()) {
					legendPanelMediatorProvider.get().hideDialog();
				} else {
					EnrichmentMap map = getCurrentMap();
					legendPanelMediatorProvider.get().showDialog(map, getCurrentEMView());
				}
			});
			mi.setSelected(legendPanelMediatorProvider.get().getDialog().isVisible());
			menu.add(mi);
		}
		
		menu.addSeparator();
		
		for (FilterMode mode : FilterMode.values()) {
			final JMenuItem mi = new JCheckBoxMenuItem(mode.toString());
			mi.addActionListener(evt -> setFilterMode(mode));
			mi.setSelected(filterMode == mode);
			menu.add(mi);
		}
		
		return menu;
	}
	
	private void setFilterMode(FilterMode filterMode) {
		this.filterMode = filterMode;
	}
	
	private class FilterActionListener implements ActionListener {

		private final EMViewControlPanel viewPanel;
		private final EnrichmentMap map;
		private final CyNetworkView netView;
		
		public FilterActionListener(EMViewControlPanel viewPanel, EnrichmentMap map, CyNetworkView netView) {
			this.viewPanel = viewPanel;
			this.map = map;
			this.netView = netView;
		}
		
		@Override
		public void actionPerformed(ActionEvent evt) {
			Set<CyNode> filteredInNodes = new HashSet<>();
			Set<CyEdge> filteredInEdges = Collections.emptySet();
			
			EMCreationParameters params = map.getParams();
			Set<AbstractDataSet> selectedDataSets = viewPanel.getCheckedDataSets();
			Set<Long> dataSetNodes = EnrichmentMap.getNodesUnion(selectedDataSets);
			
			// Only p or q value, but not both!
			if (viewPanel.getPValueSliderPanel() != null && viewPanel.getPValueSliderPanel().isVisible()) {
				Set<String> columnNames = getFilteredColumnNames(params.getPValueColumnNames(), selectedDataSets);
				filteredInNodes.addAll(
						getFilteredInNodes(viewPanel.getPValueSliderPanel(), map, netView, columnNames, dataSetNodes));
			} else if (viewPanel.getQValueSliderPanel() != null && viewPanel.getQValueSliderPanel().isVisible()) {
				Set<String> columnNames = getFilteredColumnNames(params.getQValueColumnNames(), selectedDataSets);
				filteredInNodes.addAll(
						getFilteredInNodes(viewPanel.getQValueSliderPanel(), map, netView, columnNames, dataSetNodes));
			}

			if (viewPanel.getSimilaritySliderPanel() != null)
				filteredInEdges = getFilteredInEdges(viewPanel.getSimilaritySliderPanel(), map, netView,
						params.getSimilarityCutoffColumnNames());
			
			CyNetwork net = netView.getModel();
			
			// Hide or show nodes and their edges
			for (CyNode n : net.getNodeList()) {
				final View<CyNode> nv = netView.getNodeView(n);
				
				if (nv == null)
					continue; // Should never happen!
				
				boolean filteredIn = filteredInNodes.contains(n);
				
				VisualLexicon lexicon = renderingEngineManager.getDefaultVisualLexicon(); 
				VisualProperty<?> customGraphics1 = lexicon.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_1");
				
				// Don't forget to remove all previous locked values first!
				nv.clearValueLock(NODE_VISIBLE);
				nv.clearValueLock(NODE_TRANSPARENCY);
				nv.clearValueLock(NODE_BORDER_TRANSPARENCY);
				nv.clearValueLock(NODE_LABEL_TRANSPARENCY);
				
				if (customGraphics1 != null)
					nv.clearValueLock(customGraphics1);
				
				if (filteredIn) {
					if (filterMode == FilterMode.SELECT)
						net.getRow(n).set(CyNetwork.SELECTED, true);
				} else {
					switch (filterMode) {
						case HIDE:
							net.getRow(n).set(CyNetwork.SELECTED, false);
							nv.setLockedValue(NODE_VISIBLE, false);
							break;
						case HIGHLIGHT:
							nv.setLockedValue(NODE_TRANSPARENCY, FILTERED_OUT_NODE_TRANSPARENCY);
							nv.setLockedValue(NODE_BORDER_TRANSPARENCY, FILTERED_OUT_NODE_TRANSPARENCY);
							nv.setLockedValue(NODE_LABEL_TRANSPARENCY, 0);
							if (customGraphics1 != null)
								nv.setLockedValue(customGraphics1, NullCustomGraphics.getNullObject());
							break;
						case SELECT:
							net.getRow(n).set(CyNetwork.SELECTED, false);
							break;
					}
				}
			}
			
			for (CyEdge e : net.getEdgeList()) {
				final View<CyEdge> ev = netView.getEdgeView(e);
				
				if (ev == null)
					continue; // Should never happen!
				
				boolean filteredIn = filteredInEdges.contains(e) && filteredInNodes.contains(e.getSource())
						&& filteredInNodes.contains(e.getTarget());

				// Don't forget to remove all locked values first!
				ev.clearValueLock(EDGE_VISIBLE);
				ev.clearValueLock(EDGE_TRANSPARENCY);
				ev.clearValueLock(EDGE_LABEL_TRANSPARENCY);
				
				if (filteredIn) {
					if (filterMode == FilterMode.SELECT)
						net.getRow(e).set(CyNetwork.SELECTED, true);
				} else {
					switch (filterMode) {
						case HIDE:
							net.getRow(e).set(CyNetwork.SELECTED, false);
							ev.setLockedValue(EDGE_VISIBLE, false);
							break;
						case HIGHLIGHT:
							ev.setLockedValue(EDGE_TRANSPARENCY, FILTERED_OUT_EDGE_TRANSPARENCY);
							ev.setLockedValue(EDGE_LABEL_TRANSPARENCY, FILTERED_OUT_EDGE_TRANSPARENCY);
							break;
						case SELECT:
							net.getRow(e).set(CyNetwork.SELECTED, false);
							break;
					}
				}
			}
			
			netView.updateView();
			
			Timer timer = filterTimers.get(netView);
			
			if (timer != null)
				timer.stop();
		}

		private Set<String> getFilteredColumnNames(Set<String> columnNames, Collection<AbstractDataSet> dataSets) {
			Set<String> filteredNames = new HashSet<>();
			
			for (String name : columnNames) {
				for (AbstractDataSet ds : dataSets) {
					// TODO What about 2.x columns?
					if (name.endsWith(" (" + ds.getName() + ")")) {
						filteredNames.add(name);
						break;
					}
				}
			}
			
			return filteredNames;
		}
	}
}
