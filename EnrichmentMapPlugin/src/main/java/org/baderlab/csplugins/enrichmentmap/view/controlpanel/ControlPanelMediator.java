package org.baderlab.csplugins.enrichmentmap.view.controlpanel;

import static org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil.invokeOnEDT;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JComboBox;

import org.baderlab.csplugins.enrichmentmap.AfterInjection;
import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.style.ChartFactoryManager;
import org.baderlab.csplugins.enrichmentmap.style.ChartType;
import org.baderlab.csplugins.enrichmentmap.style.ColorScheme;
import org.baderlab.csplugins.enrichmentmap.style.MasterMapStyleOptions;
import org.baderlab.csplugins.enrichmentmap.style.MasterMapVisualStyle;
import org.baderlab.csplugins.enrichmentmap.style.MasterMapVisualStyleTask;
import org.baderlab.csplugins.enrichmentmap.task.CreatePublicationVisualStyleTaskFactory;
import org.baderlab.csplugins.enrichmentmap.view.controlpanel.ControlPanel.EMViewControlPanel;
import org.baderlab.csplugins.enrichmentmap.view.mastermap.MasterMapDialogAction;
import org.baderlab.csplugins.enrichmentmap.view.util.SliderBarActionListener;
import org.baderlab.csplugins.enrichmentmap.view.util.SliderBarPanel;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
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
	@Inject private EnrichmentMapManager emManager;
	@Inject private MasterMapDialogAction masterMapDialogAction;
	@Inject private Provider<CreatePublicationVisualStyleTaskFactory> visualStyleTaskFactoryProvider;

	@Inject private CyServiceRegistrar serviceRegistrar;
	@Inject private CyApplicationManager applicationManager;
	@Inject private CySwingApplication swingApplication;
	@Inject private MasterMapVisualStyleTask.Factory visualStyleTaskFactory;
	@Inject private DialogTaskManager dialogTaskManager;
	@Inject private CyColumnIdentifierFactory columnIdFactory;
	@Inject private ChartFactoryManager chartFactoryManager;
	
	private boolean updatingEmViewCombo;
	
	@Override
	public void handleEvent(SetCurrentNetworkViewEvent e) {
		invokeOnEDT(() -> {
			getControlPanel().update(e.getNetworkView());
		});
	}
	
	@Override
	public void handleEvent(NetworkViewAddedEvent e) {
		invokeOnEDT(() -> {
			CyNetworkView netView = e.getNetworkView();
			
			// Is the new view an Enrichment Map one?
			if (emManager.getEnrichmentMap(netView.getModel().getSUID()) != null) {
				EMViewControlPanel viewPanel = getControlPanel().addEnrichmentMapView(netView);
				
				if (viewPanel != null) {
					// Add listeners to the new panel's fields
					SliderBarPanel pvSliderPanel = viewPanel.getPValueSliderPanel();
					SliderBarPanel qvSliderPanel = viewPanel.getQValueSliderPanel();
					SliderBarPanel sSliderPanel = viewPanel.getSimilaritySliderPanel();
					
					if (pvSliderPanel != null)
						pvSliderPanel.getSlider().addChangeListener(
								new SliderBarActionListener(pvSliderPanel, applicationManager, emManager));
					if (qvSliderPanel != null)
						qvSliderPanel.getSlider().addChangeListener(
								new SliderBarActionListener(qvSliderPanel, applicationManager, emManager));
					if (sSliderPanel != null)
						sSliderPanel.getSlider().addChangeListener(
								new SliderBarActionListener(sSliderPanel, applicationManager, emManager));
					
					viewPanel.getTogglePublicationCheck().addActionListener((ActionEvent ae) -> {
						dialogTaskManager.execute(visualStyleTaskFactoryProvider.get().createTaskIterator());
					});
					
					viewPanel.getResetStyleButton().addActionListener(ae -> {
						Set<DataSet> dataSets = ImmutableSet.copyOf(
								viewPanel.getCheckboxListPanel().getSelectedDataItems());
						EnrichmentMap map = emManager.getEnrichmentMap(netView.getModel().getSUID());
						MasterMapStyleOptions options = new MasterMapStyleOptions(netView, map, dataSets::contains);
						
						ChartType type = (ChartType) viewPanel.getChartTypeCombo().getSelectedItem();
						ColorScheme colorScheme = (ColorScheme) viewPanel.getChartColorsCombo().getSelectedItem();
						CyCustomGraphics2<?> chart = getChart(type, colorScheme, options);
						
						applyVisualStyle(options, chart);
						// TODO update style fields
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
			if (!updatingEmViewCombo)
				setCurrentView((CyNetworkView) cmb.getSelectedItem());
		});
		
		ctrlPanel.getCreateEmButton().addActionListener(evt -> {
			masterMapDialogAction.actionPerformed(evt);
		});
		ctrlPanel.getCreateEmButton().setToolTipText(masterMapDialogAction.getName());
		
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
	
	private void applyVisualStyle(MasterMapStyleOptions options, CyCustomGraphics2<?> chart) {
		MasterMapVisualStyleTask task = visualStyleTaskFactory.create(options, chart);
		dialogTaskManager.execute(new TaskIterator(task));
	}
	
	private CyCustomGraphics2<?> getChart(ChartType type, ColorScheme colorScheme, MasterMapStyleOptions options) {
		CyCustomGraphics2<?> chart = null;
		
		if (type != null) {
			List<CyColumnIdentifier> columns = 
					options.getDataSets().stream()
					.map(ds -> MasterMapVisualStyle.NODE_COLOURING.with(ds.getName()))  // column name
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
}
