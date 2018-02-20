package org.baderlab.csplugins.enrichmentmap.view.control;

import static org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder.Columns.EDGE_DATASET_VALUE_COMPOUND;
import static org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder.Columns.EDGE_INTERACTION_VALUE_SIG;
import static org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder.Columns.NODE_GS_TYPE;
import static org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder.Columns.NODE_GS_TYPE_ENRICHMENT;
import static org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil.invokeOnEDT;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
import org.baderlab.csplugins.enrichmentmap.model.AbstractDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EMSignatureDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.model.LegacySupport;
import org.baderlab.csplugins.enrichmentmap.style.AbstractColumnDescriptor;
import org.baderlab.csplugins.enrichmentmap.style.ChartData;
import org.baderlab.csplugins.enrichmentmap.style.ChartFactoryManager;
import org.baderlab.csplugins.enrichmentmap.style.ChartOptions;
import org.baderlab.csplugins.enrichmentmap.style.ChartType;
import org.baderlab.csplugins.enrichmentmap.style.ColorScheme;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder.Columns;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleOptions;
import org.baderlab.csplugins.enrichmentmap.style.WidthFunction;
import org.baderlab.csplugins.enrichmentmap.style.charts.AbstractChart;
import org.baderlab.csplugins.enrichmentmap.task.ApplyEMStyleTask;
import org.baderlab.csplugins.enrichmentmap.task.FilterNodesEdgesTask;
import org.baderlab.csplugins.enrichmentmap.task.FilterNodesEdgesTask.FilterMode;
import org.baderlab.csplugins.enrichmentmap.task.SelectNodesEdgesTask;
import org.baderlab.csplugins.enrichmentmap.task.postanalysis.RemoveSignatureDataSetsTask;
import org.baderlab.csplugins.enrichmentmap.util.NetworkUtil;
import org.baderlab.csplugins.enrichmentmap.view.control.ControlPanel.AbstractViewControlPanel;
import org.baderlab.csplugins.enrichmentmap.view.control.ControlPanel.EMViewControlPanel;
import org.baderlab.csplugins.enrichmentmap.view.control.ControlPanel.GMViewControlPanel;
import org.baderlab.csplugins.enrichmentmap.view.control.io.ViewParams;
import org.baderlab.csplugins.enrichmentmap.view.control.io.ViewParams.CutoffParam;
import org.baderlab.csplugins.enrichmentmap.view.creation.CreationDialogShowAction;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParams.Compress;
import org.baderlab.csplugins.enrichmentmap.view.legend.CreationParametersPanel;
import org.baderlab.csplugins.enrichmentmap.view.legend.LegendPanelMediator;
import org.baderlab.csplugins.enrichmentmap.view.postanalysis.EdgeWidthDialog;
import org.baderlab.csplugins.enrichmentmap.view.postanalysis.PADialogMediator;
import org.baderlab.csplugins.enrichmentmap.view.util.ChartUtil;
import org.baderlab.csplugins.enrichmentmap.view.util.ComboItem;
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
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedEvent;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedListener;
import org.cytoscape.view.model.events.NetworkViewAddedEvent;
import org.cytoscape.view.model.events.NetworkViewAddedListener;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifier;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class ControlPanelMediator implements SetCurrentNetworkViewListener, NetworkViewAddedListener,
		NetworkViewAboutToBeDestroyedListener {

	@Inject private Provider<ControlPanel> controlPanelProvider;
	@Inject private Provider<LegendPanelMediator> legendPanelMediatorProvider;
	@Inject private Provider<PADialogMediator> paDialogMediatorProvider;
	@Inject private Provider<EdgeWidthDialog> dialogProvider;
	@Inject private EnrichmentMapManager emManager;
	@Inject private CreationDialogShowAction masterMapDialogAction;
	@Inject private VisualMappingManager visualMappingManager;
	
	@Inject private CyServiceRegistrar serviceRegistrar;
	@Inject private CyApplicationManager applicationManager;
	@Inject private CySwingApplication swingApplication;
	@Inject private CyNetworkViewManager networkViewManager;
	@Inject private CyNetworkManager networkManager;
	@Inject private ApplyEMStyleTask.Factory applyStyleTaskFactory;
	@Inject private RemoveSignatureDataSetsTask.Factory removeDataSetsTaskFactory;
	@Inject private FilterNodesEdgesTask.Factory filterNodesEdgesTaskFactory;
	@Inject private SelectNodesEdgesTask.Factory selectNodesEdgesTaskFactory;
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
		// Listen to events from EM Manager
		emManager.addPropertyChangeListener("geneManiaEnrichmentMaps", evt -> {
			// A GeneMANIA network (created from EM nodes) has been added...
			invokeOnEDT(() -> {
				updating = true;
			
				try {
					getControlPanel().updateEmViewCombo();
					CyNetworkView netView = applicationManager.getCurrentNetworkView();
					
					if (netView != null && emManager.isGeneManiaEnrichmentMap(netView))
						setCurrentNetworkView(netView);
				} finally {
					updating = false;
				}
			});
		});
		
		invokeOnEDT(() -> {
			updating = true;
			
			try {
				for (CyNetworkView view : networkViewManager.getNetworkViewSet())
					getControlPanel().removeEnrichmentMapView(view);
	
				Set<Long> netIds = new HashSet<>();
				Map<Long, EnrichmentMap> maps = emManager.getAllEnrichmentMaps();
				
				for (EnrichmentMap map : maps.values()) {
					netIds.add(map.getNetworkID());
					netIds.addAll(map.getGeneManiaNetworkIDs());
				}
				
				for (Long id : netIds) {
					CyNetwork network = networkManager.getNetwork(id);
					Collection<CyNetworkView> networkViews = networkViewManager.getNetworkViews(network);
					
					for (CyNetworkView netView : networkViews)
						addNetworkView(netView);
				}
			} finally {
				updating = false;	
			}

			setCurrentNetworkView(applicationManager.getCurrentNetworkView());
		});
	}
	
	public void reset(ViewParams params) {
		long netViewID = params.getNetworkViewID();
		
		invokeOnEDT(() -> {
			EMViewControlPanel viewPanel = getControlPanel().getViewControlPanel(netViewID);
			
			if (viewPanel == null)
				return;
			
			EnrichmentMap map = emManager.getEnrichmentMap(viewPanel.getNetworkView().getModel().getSUID());
			
			if (map == null)
				return;
			
			try {
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
				
				Set<String> filteredOutDataSetNames = params.getFilteredOutDataSets();
				
				if (filteredOutDataSetNames != null && !filteredOutDataSetNames.isEmpty()) {
					Set<AbstractDataSet> allDataSets = viewPanel.getAllDataSets();
					Set<AbstractDataSet> filteredDataSets = allDataSets.stream()
							.filter(ds -> !filteredOutDataSetNames.contains(ds.getName()))
							.collect(Collectors.toSet());
					viewPanel.getDataSetSelector().setCheckedItems(filteredDataSets);
				}
				
				// Update Style options
				ChartOptions chartOptions = params.getChartOptions();
				viewPanel.getChartDataCombo().setSelectedItem(chartOptions != null ? chartOptions.getData() : null);
				viewPanel.getChartTypeCombo().setSelectedItem(chartOptions != null ? chartOptions.getType() : null);
				viewPanel.getChartColorsCombo().setSelectedItem(chartOptions != null ? chartOptions.getColorScheme() : null);
				viewPanel.getShowChartLabelsCheck().setSelected(chartOptions != null && chartOptions.isShowLabels());
				viewPanel.getPublicationReadyCheck().setSelected(params.isPublicationReady());
				
				viewPanel.updateChartDataCombo();
				
				updateVisualStyle(map, viewPanel);
				filterNodesAndEdges(viewPanel, map);
			} finally {
				updating = false;
			}
		});
	}
	
	public Map<Long, ViewParams> getAllViewParams() {
		Map<Long, ViewParams> map = new HashMap<>();
		
		getControlPanel().getAllControlPanels().forEach((suid, panel) -> {
			CutoffParam cuttofParam = panel.getPValueRadio().isSelected() ? CutoffParam.P_VALUE : CutoffParam.Q_VALUE;
			Double pVal = panel.getPValueSliderPanel() != null ? panel.getPValueSliderPanel().getValue() : null;
			Double qVal = panel.getQValueSliderPanel() != null ? panel.getQValueSliderPanel().getValue() : null;
			Double sCoeff = panel.getSimilaritySliderPanel() != null ? panel.getSimilaritySliderPanel().getValue() : null;
			
			Set<AbstractDataSet> uncheckedDataSets = panel.getUncheckedDataSets();
			Set<String> filteredDataSets = uncheckedDataSets.stream()
					.map(AbstractDataSet::getName)
					.collect(Collectors.toSet());
			
			EMStyleOptions options = createStyleOptions(panel.getNetworkView());
			boolean pubReady = panel.getPublicationReadyCheck().isSelected();
			
			ViewParams params = new ViewParams(
					suid, cuttofParam, pVal, qVal, sCoeff, filteredDataSets, options.getChartOptions(), pubReady);
			
			map.put(suid, params);
		});
		
		return map;
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
	
	public CyCustomGraphics2<?> createChart(EMStyleOptions options) {
		CyCustomGraphics2<?> chart = null;
		ChartOptions chartOptions = options.getChartOptions();
		ChartData data = chartOptions != null ? chartOptions.getData() : null;
		
		if (data != null && data != ChartData.NONE) {
			// Ignore Signature Data Sets in charts
			Set<EMDataSet> dataSets = filterDataSets(options.getDataSets());
			
			if (!dataSets.isEmpty()) {
				ChartType type = chartOptions.getType();
				Map<String, Object> props = new HashMap<>(type.getProperties());
				AbstractColumnDescriptor columnDescriptor = data.getColumnDescriptor();
				
				if(data == ChartData.DATA_SET) {
					List<CyColumnIdentifier> columns = Arrays.asList(columnIdFactory.createColumnIdentifier(columnDescriptor.getBaseName()));
					List<Color> colors = options.getEnrichmentMap().getDataSetColors();
					props.put("cy_dataColumns", columns);
					props.put("cy_colors", colors);
					props.put("cy_showItemLabels", chartOptions.isShowLabels());
					
				} else {
					List<CyColumnIdentifier> columns = ChartUtil.getSortedColumnIdentifiers(options.getAttributePrefix(),
							dataSets, columnDescriptor, columnIdFactory);
	
					List<Color> colors = ChartUtil.getChartColors(chartOptions);
					List<Double> range = ChartUtil.calculateGlobalRange(options.getNetworkView().getModel(), columns);
					
					props.put("cy_dataColumns", columns);
					props.put("cy_range", range);
					props.put("cy_autoRange", false);
					props.put("cy_globalRange", true);
					props.put("cy_showItemLabels", chartOptions.isShowLabels());
					props.put("cy_colors", colors);
					
					ColorScheme colorScheme = chartOptions != null ? chartOptions.getColorScheme() : null;
					if (colorScheme != null && colorScheme.getPoints() != null) {
						List<Double> points = colorScheme.getPoints();
						if (!points.isEmpty())
							props.put(AbstractChart.COLOR_POINTS, points);
					}
				}
				try {
					CyCustomGraphics2Factory<?> factory = chartFactoryManager.getChartFactory(type.getId());
					
					if (factory != null)
						chart = factory.getInstance(props);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		return chart;
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
		EnrichmentMap map = emManager.getEnrichmentMap(netView.getModel().getSUID());
		
		// Is the new view an EnrichmentMap one?
		if (map != null) {
			if (NetworkUtil.isGeneManiaNetwork(netView.getModel()))
				viewPanel = getControlPanel().addGeneManiaView(netView);
			else
				viewPanel = getControlPanel().addEnrichmentMapView(netView);
			
			if (viewPanel instanceof EMViewControlPanel)
				addListeners((EMViewControlPanel) viewPanel, map);
			else if (viewPanel instanceof GMViewControlPanel)
				addListeners((GMViewControlPanel) viewPanel, map);
		}
		
		return viewPanel;
	}

	@SuppressWarnings("unchecked")
	private void addListeners(EMViewControlPanel viewPanel, EnrichmentMap map) {
		CyNetworkView netView = viewPanel.getNetworkView();
		
		viewPanel.getQValueRadio().addActionListener(evt -> {
			viewPanel.updateFilterPanel();
			
			if (!updating)
				filterNodesAndEdges(viewPanel, map);
		});
		viewPanel.getPValueRadio().addActionListener(evt -> {
			viewPanel.updateFilterPanel();
			
			if (!updating)
				filterNodesAndEdges(viewPanel, map);
		});
		
		SliderBarPanel pvSliderPanel = viewPanel.getPValueSliderPanel();
		SliderBarPanel qvSliderPanel = viewPanel.getQValueSliderPanel();
		SliderBarPanel sSliderPanel = viewPanel.getSimilaritySliderPanel();
		
		if (pvSliderPanel != null)
			pvSliderPanel.addChangeListener(evt -> {
				if (!updating)
					filterNodesAndEdges(viewPanel, map);
			});
		if (qvSliderPanel != null)
			qvSliderPanel.addChangeListener(evt -> {
				if (!updating)
					filterNodesAndEdges(viewPanel, map);
			});
		if (sSliderPanel != null)
			sSliderPanel.addChangeListener(evt -> {
				if (!updating)
					filterNodesAndEdges(viewPanel, map);
			});

		viewPanel.getDataSetSelector().addPropertyChangeListener("checkedData", evt -> {
			if (!updating) {
				viewPanel.updateChartDataCombo();
				
				filterNodesAndEdges(viewPanel, map);
				ChartData data = (ChartData) viewPanel.getChartDataCombo().getSelectedItem();
				
				Set<EMDataSet> oldDataSets = filterDataSets((Collection<AbstractDataSet>) evt.getOldValue());
				Set<EMDataSet> newDataSets = filterDataSets((Collection<AbstractDataSet>) evt.getNewValue());
				int oldSize = oldDataSets.size();
				int newSize = newDataSets.size();
				
				// Cases where changing the number of checked datasets (Signatures excluded)
				// requires the style to be updated:
				//    a) Chart data may change:
				boolean updateStyle = data != null && data != ChartData.NONE && oldSize != newSize;
				//    b) Node color/shape may change:
				updateStyle = updateStyle || oldSize == 0 && newSize > 0;
				updateStyle = updateStyle || oldSize > 0 && newSize == 0;
				
				if (updateStyle)
					updateVisualStyle(map, viewPanel);
				else
					netView.updateView();
			}
		});
		
		viewPanel.getDataSetSelector().getAddButton().addActionListener(evt -> {
			paDialogMediatorProvider.get().showDialog(netView);
		});
		
		viewPanel.getDataSetSelector().getTable().addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(final MouseEvent e) {
				maybeShowContextMenu(e);
			}
			@Override
			public void mouseReleased(final MouseEvent e) {
				maybeShowContextMenu(e);
			}
			private void maybeShowContextMenu(final MouseEvent e) {
				if (e.isPopupTrigger()) {
					final JPopupMenu contextMenu = new JPopupMenu();
					{
						JMenuItem mi = new JMenuItem("Select nodes and edges from selected data sets");
						mi.addActionListener(evt -> selectNodesEdges(
								viewPanel.getNetworkView(),
								viewPanel.getDataSetSelector().getSelectedItems(),
								map.getParams().getCreateDistinctEdges()
						));
						contextMenu.add(mi);
					}
					contextMenu.addSeparator();
					{
						Set<AbstractDataSet> selected = viewPanel.getDataSetSelector().getSelectedItems();
						boolean onlySignatureSelected = !selected.isEmpty();
						
						for (AbstractDataSet ds : selected) {
							if (ds instanceof EMSignatureDataSet == false) {
								onlySignatureSelected = false;
								break;
							}
						}
						
						JMenuItem mi = new JMenuItem("Remove selected signature gene sets");
						mi.addActionListener(evt -> removeSignatureDataSets(map, viewPanel));
						mi.setEnabled(onlySignatureSelected);
						contextMenu.add(mi);
					}
					showContextMenu(contextMenu, e);
				}
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
				
				updateVisualStyle(map, viewPanel, true);
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
				
				updateVisualStyle(map, viewPanel, true);
			}
		});
		viewPanel.getChartColorsCombo().addItemListener(evt -> {
			if (!updating && evt.getStateChange() == ItemEvent.SELECTED)
				updateVisualStyle(map, viewPanel, true);
		});
		viewPanel.getShowChartLabelsCheck().addActionListener(evt -> {
			if (!updating)
				updateVisualStyle(map, viewPanel, true);
		});
		viewPanel.getPublicationReadyCheck().addActionListener(evt -> {
			if (!updating)
				updateVisualStyle(map, viewPanel);
		});
		viewPanel.getResetStyleButton().addActionListener(evt -> {
			updateVisualStyle(map, viewPanel);
		});
		viewPanel.getSetEdgeWidthButton().addActionListener(evt -> {
			showEdgeWidthDialog();
		});
		
		viewPanel.updateChartDataCombo();
	}

	private void addListeners(GMViewControlPanel viewPanel, EnrichmentMap map) {
		viewPanel.getCompressCombo().addItemListener(evt -> {
			if (!updating && evt.getStateChange() == ItemEvent.SELECTED) {
				updateGeneManiaStyle(map, viewPanel);
			}
		});
		
	}
	
	/**
	 * Call this method on the EDT only!
	 */
	private void setDefaults(EMViewControlPanel viewPanel, EnrichmentMap map) {
		if(map.getDataSetCount() > 0) {
			EMCreationParameters params = map.getParams();
			
			ChartData chartData;
			if(map.isTwoPhenotypeGeneric())
				chartData = ChartData.PHENOTYPES;
			else if(params != null && params.isFDR() && map.hasNonGSEADataSet())
				chartData = ChartData.FDR_VALUE; // Default for other data sets
			else
				chartData = ChartData.NES_VALUE; // Default for GSEA data sets
			
			viewPanel.getChartDataCombo().setSelectedItem(chartData);
		}
	}

	@AfterInjection
	private void init() {
		ControlPanel ctrlPanel = getControlPanel();
		
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
		
		return filterDataSets(viewPanel.getCheckedDataSets()); // Ignore Signature Data Sets
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
			dialogTaskManager.execute(new TaskIterator(task), new TaskObserver() {
				@Override
				public void taskFinished(ObservableTask task) {
				}
				@Override
				public void allFinished(FinishStatus finishStatus) {
					viewPanel.updateDataSetSelector();
					updateLegends(viewPanel);
					viewPanel.getNetworkView().updateView();
				}
			});
		}
	}
	
	private void updateLegends(EMViewControlPanel viewPanel) {
		legendPanelMediatorProvider.get().updateDialog(
				viewPanel != null ? createStyleOptions(viewPanel.getNetworkView()) : null,
				getFilteredDataSets(viewPanel)
		);
	}
	
	private void updateVisualStyle(EnrichmentMap map, EMViewControlPanel viewPanel) {
		updateVisualStyle(map, viewPanel, false);
	}
	
	private void updateVisualStyle(EnrichmentMap map, EMViewControlPanel viewPanel, boolean updateChartOnly) {
		EMStyleOptions options = createStyleOptions(map, viewPanel);
		CyCustomGraphics2<?> chart = createChart(options);
		applyVisualStyle(options, chart, updateChartOnly);
	}

	private void applyVisualStyle(EMStyleOptions options, CyCustomGraphics2<?> chart, boolean updateChartOnly) {
		ApplyEMStyleTask task = applyStyleTaskFactory.create(options, chart, updateChartOnly);
		dialogTaskManager.execute(new TaskIterator(task), new TaskObserver() {
			@Override
			public void taskFinished(ObservableTask task) {
			}
			@Override
			public void allFinished(FinishStatus finishStatus) {
				EMViewControlPanel viewPanel = getControlPanel().getViewControlPanel(options.getNetworkView());
				updateLegends(viewPanel);
			}
		});
	}
	
	private EMStyleOptions createStyleOptions(EnrichmentMap map, EMViewControlPanel viewPanel) {
		if (map == null || viewPanel == null)
			return null;
		
		Set<AbstractDataSet> dataSets = viewPanel.getDataSetSelector().getCheckedItems();
		boolean publicationReady = viewPanel.getPublicationReadyCheck().isSelected();
		boolean postAnalysis = map.hasSignatureDataSets();
		
		ChartData data = (ChartData) viewPanel.getChartDataCombo().getSelectedItem();
		ChartType type;
		ColorScheme colorScheme;
		if(data == ChartData.DATA_SET) {
			type = ChartType.DATASET_PIE;
			colorScheme = null;
		} else {
			type = getChartType(viewPanel);
			colorScheme = (ColorScheme) viewPanel.getChartColorsCombo().getSelectedItem();
		}
		
		boolean showLabels = viewPanel.getShowChartLabelsCheck().isSelected();
		ChartOptions chartOptions = new ChartOptions(data, type, colorScheme, showLabels);

		return new EMStyleOptions(viewPanel.getNetworkView(), map, dataSets::contains, chartOptions, postAnalysis, publicationReady);
	}
	
	@SuppressWarnings("unchecked")
	private void updateGeneManiaStyle(EnrichmentMap map, GMViewControlPanel viewPanel) {
		ComboItem<Compress> compressItem = (ComboItem<Compress>) viewPanel.getCompressCombo().getSelectedItem();
		Compress compress = compressItem != null ? compressItem.getValue() : Compress.NONE;
		
		String compare = (String) viewPanel.getCompareCombo().getSelectedItem();
		
		// TODO
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
		
		timer.setInitialDelay(350);
		timer.start();
	}
	
	private JPopupMenu getOptionsMenu() {
		final JPopupMenu menu = new JPopupMenu();
		
		{
			JMenuItem showLegendItem = new JCheckBoxMenuItem("Show Legend");
			showLegendItem.addActionListener(evt -> {
				if (legendPanelMediatorProvider.get().getDialog().isVisible()) {
					legendPanelMediatorProvider.get().hideDialog();
				} else {
					CyNetworkView netView = getCurrentEMView();
					EMViewControlPanel viewPanel = getControlPanel().getViewControlPanel(netView);
					legendPanelMediatorProvider.get().showDialog(createStyleOptions(netView), getFilteredDataSets(viewPanel));
				}
			});
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
		
		for (FilterMode mode : FilterMode.values()) {
			final JMenuItem mi = new JCheckBoxMenuItem(mode.toString());
			mi.addActionListener(evt -> setFilterMode(mode));
			mi.setSelected(filterMode == mode);
			menu.add(mi);
		}
		
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
	private static Set<EMDataSet> filterDataSets(Collection<AbstractDataSet> abstractDataSets) {
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
	
	private void selectNodesEdges(CyNetworkView netView, Set<AbstractDataSet> dataSets, boolean distinctEdges) {
		SelectNodesEdgesTask task = selectNodesEdgesTaskFactory.create(netView, dataSets, distinctEdges);
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
			task = filterNodesEdgesTaskFactory.create(netView, filteredInNodes, filteredInEdges, filterMode);
			dialogTaskManager.execute(new TaskIterator(task), new TaskObserver() {
				@Override
				public void taskFinished(ObservableTask task) {
				}
				
				@Override
				public void allFinished(FinishStatus finishStatus) {
					task = null;
					if (!cancelled)
						netView.updateView();
				}
			});
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
				Set<String> columnNames = getFilteredColumnNames(params.getPValueColumnNames(), selectedDataSets);
				
				return getFilteredInNodes(viewPanel.getPValueSliderPanel(), map, netView, columnNames, dataSetNodes);
			}
			
			if (viewPanel.getQValueSliderPanel() != null && viewPanel.getQValueSliderPanel().isVisible()) {
				Set<String> columnNames = getFilteredColumnNames(params.getQValueColumnNames(), selectedDataSets);
				
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
			Set<Long> dataSetEdges = EnrichmentMap.getEdgesUnion(selectedDataSets);
			
			if (viewPanel.getSimilaritySliderPanel() != null)
				return getFilteredInEdges(viewPanel.getSimilaritySliderPanel(), map, netView,
						params.getSimilarityCutoffColumnNames(), dataSetEdges);
			
			Set<CyEdge> filteredInEdges = new HashSet<>();
			CyNetwork net = netView.getModel();
			boolean distinct = params.getCreateDistinctEdges();
			
			if (distinct) {
				for (CyEdge e : net.getEdgeList()) {
					if (dataSetEdges.contains(e.getSUID()))
						filteredInEdges.add(e);
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
					show = showElement(columnNames, table, row, maxCutoff, minCutoff);
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
					show = showElement(columnNames, table, row, maxCutoff, minCutoff);
				}
				else { // discrete edge
					if (dataSetEdges != null && !dataSetEdges.contains(e.getSUID())) {
						show = false;
					} else {
						String interaction = row.get(CyEdge.INTERACTION, String.class);
						if (EDGE_INTERACTION_VALUE_SIG.equals(interaction)) { 
							show = true;
						} else {
							show = showElement(columnNames, table, row, maxCutoff, minCutoff);
						}
					}
				}
				
				if (show)
					edges.add(e);
			}
			
			return edges;
		}

		private boolean showElement(Set<String> columnNames, CyTable table, CyRow row, Double maxCutoff, Double minCutoff) {
			boolean show = true;
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
			return show;
		}

		private Set<String> getFilteredColumnNames(Set<String> columnNames, Collection<AbstractDataSet> dataSets) {
			Set<String> filteredNames = new HashSet<>();
			
			for (String name : columnNames) {
				for (AbstractDataSet ds : dataSets) {
					if (ds.getMap().isLegacy()) {
						if(LegacySupport.DATASET1.equals(ds.getName()) && name.endsWith("dataset1")) {
							filteredNames.add(name);
							break;
						}
						if(LegacySupport.DATASET2.equals(ds.getName()) && name.endsWith("dataset2")) {
							filteredNames.add(name);
							break;
						}
					} 
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
