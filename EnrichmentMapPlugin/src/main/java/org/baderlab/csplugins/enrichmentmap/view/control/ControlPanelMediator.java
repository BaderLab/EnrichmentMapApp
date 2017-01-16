package org.baderlab.csplugins.enrichmentmap.view.control;

import static org.baderlab.csplugins.enrichmentmap.style.MasterMapVisualStyle.Columns.NODE_GS_TYPE;
import static org.baderlab.csplugins.enrichmentmap.style.MasterMapVisualStyle.Columns.NODE_GS_TYPE_ENRICHMENT;
import static org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil.invokeOnEDT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_VISIBLE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_VISIBLE;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.Timer;

import org.baderlab.csplugins.enrichmentmap.AfterInjection;
import org.baderlab.csplugins.enrichmentmap.actions.ShowEnrichmentMapDialogAction;
import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.style.ChartFactoryManager;
import org.baderlab.csplugins.enrichmentmap.style.ChartType;
import org.baderlab.csplugins.enrichmentmap.style.ColorScheme;
import org.baderlab.csplugins.enrichmentmap.style.MasterMapStyleOptions;
import org.baderlab.csplugins.enrichmentmap.style.MasterMapVisualStyle;
import org.baderlab.csplugins.enrichmentmap.style.MasterMapVisualStyleTask;
import org.baderlab.csplugins.enrichmentmap.style.WidthFunction;
import org.baderlab.csplugins.enrichmentmap.task.CreatePublicationVisualStyleTaskFactory;
import org.baderlab.csplugins.enrichmentmap.view.control.ControlPanel.EMViewControlPanel;
import org.baderlab.csplugins.enrichmentmap.view.parameters.ParametersPanelMediator;
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
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedEvent;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedListener;
import org.cytoscape.view.model.events.NetworkViewAddedEvent;
import org.cytoscape.view.model.events.NetworkViewAddedListener;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifier;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class ControlPanelMediator
		implements SetCurrentNetworkViewListener, NetworkViewAddedListener, NetworkViewAboutToBeDestroyedListener {

	@Inject private Provider<ControlPanel> controlPanelProvider;
	@Inject private Provider<ParametersPanelMediator> parametersPanelMediatorProvider;
	@Inject private Provider<PostAnalysisPanelMediator> postAnalysisPanelMediatorProvider;
	@Inject private Provider<EdgeWidthDialog> dialogProvider;
	@Inject private EnrichmentMapManager emManager;
	@Inject private ShowEnrichmentMapDialogAction masterMapDialogAction;
	@Inject private Provider<CreatePublicationVisualStyleTaskFactory> visualStyleTaskFactoryProvider;

	@Inject private CyServiceRegistrar serviceRegistrar;
	@Inject private CyApplicationManager applicationManager;
	@Inject private CySwingApplication swingApplication;
	@Inject private CyNetworkViewManager networkViewManager;
	@Inject private CyNetworkManager networkManager;
	@Inject private MasterMapVisualStyleTask.Factory visualStyleTaskFactory;
	@Inject private DialogTaskManager dialogTaskManager;
	@Inject private CyColumnIdentifierFactory columnIdFactory;
	@Inject private ChartFactoryManager chartFactoryManager;
	
	private Map<CyNetworkView, Timer> filterTimers = new HashMap<>();
	
	private boolean firstTime = true;
	private boolean updating;
	
	@Override
	public void handleEvent(SetCurrentNetworkViewEvent e) {
		setCurrentNetworkView(e.getNetworkView());
	}
	
	
	private void setCurrentNetworkView(CyNetworkView view) {
		invokeOnEDT(() -> {
			updating = true;
			
			try {
				getControlPanel().update(view);
			} finally {
				updating = false;
			}
		});
	}
	
	
	@Override
	public void handleEvent(NetworkViewAddedEvent e) {
		addNetworkView(e.getNetworkView());
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

					viewPanel.getCheckboxListPanel().getCheckboxList().addListSelectionListener(evt -> {
						filterNodesAndEdges(viewPanel, map, netView);
						netView.updateView();
					});
					viewPanel.getCheckboxListPanel().setAddButtonCallback(model -> {
						postAnalysisPanelMediatorProvider.get().showDialog(viewPanel, getCurrentMap());
					});
					
					viewPanel.getTogglePublicationCheck().addActionListener((ActionEvent ae) -> {
						dialogTaskManager.execute(visualStyleTaskFactoryProvider.get().createTaskIterator());
					});
					
					viewPanel.getResetStyleButton().addActionListener(ae -> {
						Set<DataSet> dataSets = ImmutableSet.copyOf(
								viewPanel.getCheckboxListPanel().getSelectedDataItems());
						MasterMapStyleOptions options = new MasterMapStyleOptions(netView, map, dataSets::contains);
						
						ChartType type = (ChartType) viewPanel.getChartTypeCombo().getSelectedItem();
						ColorScheme colorScheme = (ColorScheme) viewPanel.getChartColorsCombo().getSelectedItem();
						CyCustomGraphics2<?> chart = getChart(type, colorScheme, options);
						
						applyVisualStyle(options, chart);
						// TODO update style fields
					});
					
					viewPanel.getSetEdgeWidthButton().addActionListener(ae -> {
						showEdgeWidthDialog();
					});
				}
			}
		});
	}

	
	public void reset() {
		invokeOnEDT(() -> {
			for(CyNetworkView view : networkViewManager.getNetworkViewSet()) {
				getControlPanel().removeEnrichmentMapView(view);
			}
			
			Map<Long, EnrichmentMap> maps = emManager.getAllEnrichmentMaps();
			for(EnrichmentMap map : maps.values()) {
				CyNetwork network = networkManager.getNetwork(map.getNetworkID());
				Collection<CyNetworkView> networkViews = networkViewManager.getNetworkViews(network);
				for(CyNetworkView netView : networkViews) {
					addNetworkView(netView);
				}
			}
			
			setCurrentNetworkView(applicationManager.getCurrentNetworkView());
		});
	}
	
	
	@Override
	public void handleEvent(NetworkViewAboutToBeDestroyedEvent event) {
		final CyNetworkView netView = event.getNetworkView();
		Timer timer = filterTimers.remove(netView);
		
		if (timer != null)
			timer.stop();
		
		invokeOnEDT(() -> {
			getControlPanel().removeEnrichmentMapView(netView);
		});
	}
	
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
			
			if (firstTime) {
				firstTime = false;
				controlPanelProvider.get().getCreateEmButton().doClick();
			}
		}
		
		// Select the panel
		CytoPanel cytoPanel = swingApplication.getCytoPanel(panel.getCytoPanelName());
		int index = cytoPanel.indexOfComponent(ControlPanel.ID);
		
		if (index >= 0)
			cytoPanel.setSelectedIndex(index);
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
		
		ctrlPanel.getOpenLegendsButton().addActionListener(evt -> {
			if (parametersPanelMediatorProvider.get().getDialog().isVisible()) {
				parametersPanelMediatorProvider.get().hideDialog();
			} else {
				EnrichmentMap map = getCurrentMap();
				parametersPanelMediatorProvider.get().showDialog(map);
			}
		});
		
		parametersPanelMediatorProvider.get().getDialog().addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent e) {
				updateButton();
			}
			@Override
			public void windowClosed(WindowEvent we) {
				updateButton();
			}
			private void updateButton() {
				ctrlPanel.getOpenLegendsButton().setSelected(
						parametersPanelMediatorProvider.get().getDialog().isVisible());
			}
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
	
	private EnrichmentMap getCurrentMap() {
		CyNetworkView view  = (CyNetworkView) getControlPanel().getEmViewCombo().getSelectedItem();
		
		return view != null ? emManager.getEnrichmentMap(view.getModel().getSUID()) : null;
	}
	
	private void applyVisualStyle(MasterMapStyleOptions options, CyCustomGraphics2<?> chart) {
		MasterMapVisualStyleTask task = visualStyleTaskFactory.create(options, chart);
		dialogTaskManager.execute(new TaskIterator(task));
	}
	
	private CyCustomGraphics2<?> getChart(ChartType type, ColorScheme colorScheme, MasterMapStyleOptions options) {
		CyCustomGraphics2<?> chart = null;
		
		if (type != null) {
			List<CyColumnIdentifier> columns = 
					options.getDataSets().stream()
					.map(ds -> MasterMapVisualStyle.Columns.NODE_COLOURING.with(options.getAttributePrefix(),ds.getName()))  // column name
					.map(columnIdFactory::createColumnIdentifier)  // column id
					.collect(Collectors.toList());
			
			Map<String, Object> props = new HashMap<>(type.getProperties());
			props.put("cy_colorScheme", colorScheme.getKey());
			props.put("cy_dataColumns", columns);
			
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
					swingApplication.getJFrame(),
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
			if (!dataSetNodes.contains(n.getSUID()))
				continue; // Do not include this node
			
			CyRow row = network.getRow(n);

			// Skip Node if it's not an Enrichment-Geneset (but e.g. a Signature-Hub)
			if (table.getColumn(prefix + NODE_GS_TYPE) != null
					&& !NODE_GS_TYPE_ENRICHMENT.equalsIgnoreCase(row.get(prefix + NODE_GS_TYPE, String.class)))
				continue; // Do not include this node

			boolean showNode = false;
			
			for (String colName : columnNames) {
				if (table.getColumn(colName) == null) {
					showNode = true;
					break;
				}
				
				Double value = row.get(colName, Double.class);
	
				// Possible that there isn't a p-value for this geneset
				if (value == null)
					value = 0.99;
	
				if (value >= minCutoff && value <= maxCutoff) {
					showNode = true;
					break;
				}
			}
			
			if (showNode)
				nodes.add(n);
		}
		
		return nodes;
	}

	private Set<CyEdge> getFilteredInEdges(SliderBarPanel sliderPanel, EnrichmentMap map, CyNetworkView networkView,
			Set<String> columnNames) {
		Set<CyEdge> edges = new HashSet<>();
		
		JSlider slider = sliderPanel.getSlider();
		Double maxCutoff = slider.getValue() / sliderPanel.getPrecision();
		Double minCutoff = slider.getMinimum() / sliderPanel.getPrecision();
		
		CyNetwork network = networkView.getModel();
		CyTable table = network.getDefaultEdgeTable();

		// Go through all the existing edges to see if we need to hide any new ones.
		for (CyEdge e : network.getEdgeList()) {
			CyRow row = network.getRow(e);
			boolean showEdge = false;
			
			for (String colName : columnNames) {
				if (table.getColumn(colName) == null) {
					showEdge = true;
					break;
				}

				Double value = row.get(colName, Double.class);

				// Possible that there isn't value for this interaction
				if (value == null)
					value = 0.1;

				if (value >= minCutoff && value <= maxCutoff) {
					showEdge = true;
					break;
				}
			}
		
			if (showEdge)
				edges.add(e);
		}
		
		return edges;
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
			Set<CyNode> nodesToShow = new HashSet<>();
			Set<CyEdge> edgesToShow = Collections.emptySet();
			
			EMCreationParameters params = map.getParams();
			List<DataSet> selectedDataSets = viewPanel.getSelectedDataSets();
			Set<Long> dataSetNodes = EnrichmentMap.getNodesUnion(selectedDataSets);
			
			// Only p or q value, but not both!
			if (viewPanel.getPValueSliderPanel() != null && viewPanel.getPValueSliderPanel().isVisible()) {
				Set<String> columnNames = getFilteredColumnNames(params.getPValueColumnNames(), selectedDataSets);
				nodesToShow.addAll(
						getFilteredInNodes(viewPanel.getPValueSliderPanel(), map, netView, columnNames, dataSetNodes));
			} else if (viewPanel.getQValueSliderPanel() != null && viewPanel.getQValueSliderPanel().isVisible()) {
				Set<String> columnNames = getFilteredColumnNames(params.getQValueColumnNames(), selectedDataSets);
				nodesToShow.addAll(
						getFilteredInNodes(viewPanel.getQValueSliderPanel(), map, netView, columnNames, dataSetNodes));
			}

			if (viewPanel.getSimilaritySliderPanel() != null)
				edgesToShow = getFilteredInEdges(viewPanel.getSimilaritySliderPanel(), map, netView,
						params.getSimilarityCutoffColumnNames());
			
			CyNetwork net = netView.getModel();
			
			// Hide or show nodes and their edges
			for (CyNode n : net.getNodeList()) {
				final View<CyNode> nv = netView.getNodeView(n);
				
				if (nv == null)
					continue; // Should never happen!
				
				boolean show = nodesToShow.contains(n);
				
				if (show) {
					nv.clearValueLock(NODE_VISIBLE);
				} else {
					net.getRow(n).set(CyNetwork.SELECTED, false);
					nv.setLockedValue(NODE_VISIBLE, false);
				}

				for (CyNode n2 : net.getNeighborList(n, CyEdge.Type.ANY)) {
					for (CyEdge e : net.getConnectingEdgeList(n, n2, CyEdge.Type.ANY)) {
						if (show)
							edgesToShow.add(e);
					}
				}
			}
			
			for (CyEdge e : net.getEdgeList()) {
				final View<CyEdge> ev = netView.getEdgeView(e);
				
				if (ev == null)
					continue; // Should never happen!
				
				boolean show = edgesToShow.contains(e) && nodesToShow.contains(e.getSource())
						&& nodesToShow.contains(e.getTarget());

				if (show) {
					ev.clearValueLock(EDGE_VISIBLE);
				} else {
					net.getRow(ev.getModel()).set(CyNetwork.SELECTED, false);
					ev.setLockedValue(EDGE_VISIBLE, false);
				}
			}
			
			netView.updateView();
			
			Timer timer = filterTimers.get(netView);
			
			if (timer != null)
				timer.stop();
		}

		private Set<String> getFilteredColumnNames(Set<String> columnNames, List<DataSet> dataSets) {
			Set<String> filteredNames = new HashSet<>();
			
			for (String name : columnNames) {
				for (DataSet ds : dataSets) {
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
