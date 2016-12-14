package org.baderlab.csplugins.enrichmentmap.view.control;

import static org.baderlab.csplugins.enrichmentmap.style.MasterMapVisualStyle.Columns.NODE_GS_TYPE;
import static org.baderlab.csplugins.enrichmentmap.style.MasterMapVisualStyle.Columns.NODE_GS_TYPE_ENRICHMENT;
import static org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil.invokeOnEDT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_VISIBLE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_VISIBLE;

import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import javax.swing.event.ChangeEvent;

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
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
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
	@Inject private MasterMapVisualStyleTask.Factory visualStyleTaskFactory;
	@Inject private DialogTaskManager dialogTaskManager;
	@Inject private CyColumnIdentifierFactory columnIdFactory;
	@Inject private ChartFactoryManager chartFactoryManager;
	
	private boolean firstTime = true;
	private boolean updating;
	
	@Override
	public void handleEvent(SetCurrentNetworkViewEvent e) {
		invokeOnEDT(() -> {
			updating = true;
			
			try {
				getControlPanel().update(e.getNetworkView());
			} finally {
				updating = false;
			}
		});
	}
	
	@Override
	public void handleEvent(NetworkViewAddedEvent e) {
		invokeOnEDT(() -> {
			CyNetworkView netView = e.getNetworkView();
			EnrichmentMap map = emManager.getEnrichmentMap(netView.getModel().getSUID());
			
			// Is the new view an Enrichment Map one?
			if (map != null) {
				EMViewControlPanel viewPanel = getControlPanel().addEnrichmentMapView(netView);
				
				if (viewPanel != null) {
					// Add listeners to the new panel's fields
					SliderBarPanel pvSliderPanel = viewPanel.getPValueSliderPanel();
					SliderBarPanel qvSliderPanel = viewPanel.getQValueSliderPanel();
					SliderBarPanel sSliderPanel = viewPanel.getSimilaritySliderPanel();
					
					if (pvSliderPanel != null)
						pvSliderPanel.getSlider().addChangeListener(evt ->
								onCutoffSliderChanged(evt, pvSliderPanel, viewPanel, map, netView, CyNode.class)
						);
					if (qvSliderPanel != null)
						qvSliderPanel.getSlider().addChangeListener(evt ->
								onCutoffSliderChanged(evt, pvSliderPanel, viewPanel, map, netView, CyNode.class)
					);
					if (sSliderPanel != null)
						sSliderPanel.getSlider().addChangeListener(evt ->
								onCutoffSliderChanged(evt, pvSliderPanel, viewPanel, map, netView, CyEdge.class)
					);

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
	
	@Override
	public void handleEvent(NetworkViewAboutToBeDestroyedEvent event) {
		invokeOnEDT(() -> {
			getControlPanel().removeEnrichmentMapView(event.getNetworkView());
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
	
	public void onCutoffSliderChanged(ChangeEvent e, SliderBarPanel sliderPanel, EMViewControlPanel viewPanel,
			EnrichmentMap map, CyNetworkView netView, Class<? extends CyIdentifiable> targetType) {
		JSlider slider = (JSlider) e.getSource();
		
		if (slider.getValueIsAdjusting())
			return;
		
		sliderPanel.setValue(slider.getValue());
		
		// Check to see if the event is associated with only edges
		if (targetType == CyEdge.class)
			filterEdges(viewPanel, map, netView);
		else
			filterNodes(viewPanel, map, netView);
		
		netView.updateView();
	}
	
	private void filterNodes(EMViewControlPanel viewPanel, EnrichmentMap map, CyNetworkView netView) {
		Set<CyNode> nodesToShow = new HashSet<>();
		Set<CyEdge> edgesToShow = Collections.emptySet();
		
		EMCreationParameters params = map.getParams();
		
		if (viewPanel.getPValueSliderPanel() != null)
			nodesToShow.addAll(
					getFilteredInNodes(viewPanel.getPValueSliderPanel(), map, netView, params.getPValueColumnNames()));
		if (viewPanel.getQValueSliderPanel() != null)
			nodesToShow.addAll(
					getFilteredInNodes(viewPanel.getQValueSliderPanel(), map, netView, params.getPValueColumnNames()));
		if (viewPanel.getSimilaritySliderPanel() != null)
			edgesToShow = getFilteredInEdges(viewPanel.getSimilaritySliderPanel(), map, netView,
							params.getSimilarityCutoffColumnNames());
		
		CyNetwork net = netView.getModel();
		
System.out.println("\n# Nodes: " + nodesToShow.size());
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
			
			boolean show = edgesToShow.contains(e);
			
			if (show) {
				ev.clearValueLock(EDGE_VISIBLE);
			} else {
				net.getRow(ev.getModel()).set(CyNetwork.SELECTED, false);
				ev.setLockedValue(EDGE_VISIBLE, false);
			}
		}
	}
	
	private void filterEdges(EMViewControlPanel viewPanel, EnrichmentMap map, CyNetworkView netView) {
		final Set<CyEdge> edgesToShow;
		EMCreationParameters params = map.getParams();
		CyNetwork net = netView.getModel();
		
		if (viewPanel.getSimilaritySliderPanel() != null)
			edgesToShow = getFilteredInEdges(viewPanel.getSimilaritySliderPanel(), map, netView,
							params.getSimilarityCutoffColumnNames());
		else
			edgesToShow = new HashSet<>(netView.getModel().getEdgeList());
		
		// Hide or show edges
		for (CyEdge e : net.getEdgeList()) {
			final View<CyEdge> ev = netView.getEdgeView(e);
			
			if (ev == null)
				continue; // Should not happen!
			
			boolean show = edgesToShow.contains(e);
			
			if (show) {
				// Also check if the source and target nodes are visible
				final View<CyNode> snv = netView.getNodeView(e.getSource());
				final View<CyNode> tnv = netView.getNodeView(e.getTarget());
				show = show && snv != null && tnv != null;
				
				if (show)
					show = snv.getVisualProperty(NODE_VISIBLE) == Boolean.TRUE;
				if (show)
					show = tnv.getVisualProperty(NODE_VISIBLE) == Boolean.TRUE;
			}
			
			if (show) {
				ev.clearValueLock(EDGE_VISIBLE);
			} else {
				net.getRow(ev.getModel()).set(CyNetwork.SELECTED, false);
				ev.setLockedValue(EDGE_VISIBLE, false);
			}
		}
	}

	private Set<CyNode> getFilteredInNodes(SliderBarPanel sliderPanel, EnrichmentMap map, CyNetworkView networkView,
			Set<String> columnNames) {
		Set<CyNode> nodes = new HashSet<>();
		
		JSlider slider = sliderPanel.getSlider();
		Double maxCutoff = slider.getValue() / sliderPanel.getPrecision();
		Double minCutoff = slider.getMinimum() / sliderPanel.getPrecision();
		System.out.println(minCutoff + " <<>> " + maxCutoff);

		CyNetwork network = networkView.getModel();
		CyTable table = network.getDefaultNodeTable();

		EMCreationParameters params = map.getParams();
		
		// Get the prefix of the current network
		final String prefix = params.getAttributePrefix();
		
		// Go through all the existing nodes to see if we need to hide any new nodes.
		for (CyNode n : network.getNodeList()) {
			CyRow row = network.getRow(n);

			// Skip Node if it's not an Enrichment-Geneset (but e.g. a Signature-Hub)
			if (table.getColumn(prefix + NODE_GS_TYPE) != null
					&& !NODE_GS_TYPE_ENRICHMENT.equalsIgnoreCase(row.get(prefix + NODE_GS_TYPE, String.class)))
				continue;

			boolean showNode = false;
			
			for (String colName : columnNames) {
				if (table.getColumn(colName) == null) // TODO show or hide?
					continue;
				
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
		System.out.println(minCutoff + " ---- " + maxCutoff);

		CyNetwork network = networkView.getModel();
		CyTable table = network.getDefaultEdgeTable();

		EMCreationParameters params = map.getParams();
		
		// Get the prefix of the current network
		final String prefix = params.getAttributePrefix();
		
		// Go through all the existing edges to see if we need to hide any new ones.
		for (CyEdge e : network.getEdgeList()) {
			CyRow row = network.getRow(e);

			// Skip Edge if it's not an Enrichment-Geneset (but e.g. a Signature-Hub)
			// TODO Why check node column here???
//			if (table.getColumn(prefix + NODE_GS_TYPE) != null
//					&& !NODE_GS_TYPE_ENRICHMENT.equalsIgnoreCase(row.get(prefix + NODE_GS_TYPE, String.class)))
//				continue;

			boolean showEdge = false;
			
			for (String colName : columnNames) {
				if (table.getColumn(colName) == null) // TODO show or hide?
					continue;
				
				Double value = row.get(colName, Double.class);
	
				// Possible that there isn't a p-value for this geneset
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
}
