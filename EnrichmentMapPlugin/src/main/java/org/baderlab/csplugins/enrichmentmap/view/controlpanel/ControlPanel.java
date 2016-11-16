package org.baderlab.csplugins.enrichmentmap.view.controlpanel;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.baderlab.csplugins.enrichmentmap.style.ColorScheme.CONTRASTING;
import static org.baderlab.csplugins.enrichmentmap.style.ColorScheme.CUSTOM;
import static org.baderlab.csplugins.enrichmentmap.style.ColorScheme.MODULATED;
import static org.baderlab.csplugins.enrichmentmap.style.ColorScheme.RAINBOW;
import static org.baderlab.csplugins.enrichmentmap.style.ColorScheme.RANDOM;
import static org.baderlab.csplugins.enrichmentmap.util.SwingUtil.invokeOnEDT;
import static org.baderlab.csplugins.enrichmentmap.util.SwingUtil.makeSmall;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.LayoutStyle.ComponentPlacement;

import org.baderlab.csplugins.enrichmentmap.AfterInjection;
import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.style.ChartFactoryManager;
import org.baderlab.csplugins.enrichmentmap.style.ChartType;
import org.baderlab.csplugins.enrichmentmap.style.ColorGradient;
import org.baderlab.csplugins.enrichmentmap.style.ColorScheme;
import org.baderlab.csplugins.enrichmentmap.style.EnrichmentMapVisualStyle;
import org.baderlab.csplugins.enrichmentmap.style.MasterMapStyleOptions;
import org.baderlab.csplugins.enrichmentmap.style.MasterMapVisualStyle;
import org.baderlab.csplugins.enrichmentmap.style.MasterMapVisualStyleTask;
import org.baderlab.csplugins.enrichmentmap.task.CreatePublicationVisualStyleTaskFactory;
import org.baderlab.csplugins.enrichmentmap.util.SwingUtil;
import org.baderlab.csplugins.enrichmentmap.view.mastermap.MasterMapDialogAction;
import org.baderlab.csplugins.enrichmentmap.view.util.CheckboxData;
import org.baderlab.csplugins.enrichmentmap.view.util.CheckboxListModel;
import org.baderlab.csplugins.enrichmentmap.view.util.CheckboxListPanel;
import org.baderlab.csplugins.enrichmentmap.view.util.SliderBarPanel;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.swing.CytoPanelComponent2;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.model.CyDisposable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifier;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Provider;

@SuppressWarnings("serial")
public class ControlPanel extends JPanel
		implements CytoPanelComponent2, CyDisposable, SetCurrentNetworkViewListener, NetworkAboutToBeDestroyedListener {
	
	public static final String ID = "enrichmentmap.view.ControlPanel";
	
	private final ColorScheme[] REGULAR_COLOR_SCHEMES = new ColorScheme[] {
			CONTRASTING, MODULATED, RAINBOW, RANDOM, CUSTOM
	};
	private final ColorScheme[] HEAT_STRIP_COLOR_SCHEMES;
	
	@Inject private CyApplicationManager applicationManager;
	@Inject private CyNetworkManager networkManager;
	@Inject private CyNetworkViewManager networkViewManager;
	@Inject private DialogTaskManager dialogTaskManager;
	@Inject private CyColumnIdentifierFactory columnIdFactory;
	@Inject private IconManager iconManager;
	
	@Inject private EnrichmentMapManager emManager;
//	@Inject private Provider<MasterMapDialogAction> masterMapDialogActionProvider;
	@Inject private MasterMapVisualStyleTask.Factory visualStyleTaskFactory;
	@Inject private Provider<CreatePublicationVisualStyleTaskFactory> visualStyleTaskFactoryProvider;
	@Inject private ChartFactoryManager chartFactoryManager;
	@Inject private MasterMapDialogAction masterMapDialogAction;
	
	private JLabel chartTypeLabel = new JLabel("Chart Type:");
	private JLabel chartColorsLabel = new JLabel("Chart Colors:");
	private JLabel dsFilterLabel1 = new JLabel("Show edges from ");
	private JLabel dsFilterLabel2 = new JLabel(" of these data sets:");
	
	private JComboBox<EnrichmentMap> emCombo = new JComboBox<>();
	private JToggleButton openLegendsButton = new JToggleButton(IconManager.ICON_LIST_ALT);
	private JButton createEmButton = new JButton(IconManager.ICON_PLUS);
	
	private CheckboxListPanel<DataSet> checkboxListPanel;
	private JRadioButton anyRadio;
	private JRadioButton allRadio;
	private JCheckBox togglePublicationCheck;
	private JButton resetStyleButton;
	
	private Map<Long/*CyNetwork SUID*/, SliderBarPanel> pvalueSliderPanels = new HashMap<>(); // TODO: Delete??? Or advanced options
	private Map<Long/*CyNetwork SUID*/, SliderBarPanel> qvalueSliderPanels = new HashMap<>();
	private Map<Long/*CyNetwork SUID*/, SliderBarPanel> similaritySliderPanels = new HashMap<>();
	
	private JComboBox<ChartType> chartTypeCombo = new JComboBox<>();
	private JComboBox<ColorScheme> chartColorsCombo = new JComboBox<>();

	private boolean updating;
	
	public ControlPanel() {
		// Init colors
		final List<ColorScheme> heatStripSchemeList = new ArrayList<>();
		
		for (final ColorGradient cg : ColorGradient.values()) {
			if (cg.getColors().size() == 3)
				heatStripSchemeList.add(new ColorScheme(cg));
		}
		
		heatStripSchemeList.add(CUSTOM);
		
		HEAT_STRIP_COLOR_SCHEMES = heatStripSchemeList.toArray(new ColorScheme[heatStripSchemeList.size()]);
	}
	
	@Override
	public void handleEvent(SetCurrentNetworkViewEvent e) {
		invokeOnEDT(() -> {
			update(e.getNetworkView());
		});
	}
	
	@Override
	public void handleEvent(NetworkAboutToBeDestroyedEvent event) {
		Long suid = event.getNetwork().getSUID();
		
		invokeOnEDT(() -> {
			pvalueSliderPanels.remove(suid);
			qvalueSliderPanels.remove(suid);
			similaritySliderPanels.remove(suid);
		});
	}
	
	@Override
	public void dispose() {
		
	}

	@Override
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.WEST;
	}

	@Override
	public String getTitle() {
		return "EnrichmentMap";
	}

	@Override
	public Icon getIcon() {
		String path = "org/baderlab/csplugins/enrichmentmap/view/enrichmentmap_logo_notext_small.png";
		URL url = getClass().getClassLoader().getResource(path);
		return url == null ? null : new ImageIcon(url);
	}

	@Override
	public String getIdentifier() {
		return ID;
	}

	@Override
	public Component getComponent() {
		return this;
	}
	
	@AfterInjection
	private void createContents() {
		emCombo.addActionListener(evt -> {
			if (!updating)
				setCurrentView((EnrichmentMap) emCombo.getSelectedItem());
		});
		
		openLegendsButton.setFont(iconManager.getIconFont(14.0f));
		openLegendsButton.setToolTipText("Show Legend...");
		
		createEmButton.setFont(iconManager.getIconFont(13.0f));
		createEmButton.setToolTipText(masterMapDialogAction.getName());
		createEmButton.addActionListener(evt -> {
			masterMapDialogAction.actionPerformed(evt);
		});
		
		if (LookAndFeelUtil.isAquaLAF()) {
			openLegendsButton.putClientProperty("JButton.buttonType", "gradient");
			openLegendsButton.putClientProperty("JComponent.sizeVariant", "small");
			createEmButton.putClientProperty("JButton.buttonType", "gradient");
			createEmButton.putClientProperty("JComponent.sizeVariant", "small");
		}
		
		LookAndFeelUtil.equalizeSize(openLegendsButton, createEmButton);
		
		final JPanel filterPanel = createFilterPanel();
		final JPanel stylePanel = createStylePanel();
		
		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(LookAndFeelUtil.isWinLAF());
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
   		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
   				.addGroup(layout.createSequentialGroup()
   						.addComponent(emCombo, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
   						.addComponent(createEmButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   						.addComponent(openLegendsButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   				)
				.addComponent(filterPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(stylePanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
   		);
   		layout.setVerticalGroup(layout.createSequentialGroup()
   				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
   						.addComponent(emCombo, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   						.addComponent(createEmButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   						.addComponent(openLegendsButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   				)
   				.addComponent(filterPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(stylePanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   		);
		
		if (LookAndFeelUtil.isAquaLAF())
			setOpaque(false);
		
		update(applicationManager.getCurrentNetworkView());
	}
	
	@Inject
	public void registerListener(CyServiceRegistrar registrar) {
		registrar.registerService(this, NetworkAboutToBeDestroyedListener.class, new Properties());
	}
	
	private JPanel createFilterPanel() {
		final JPanel panel = new JPanel();
		panel.setBorder(LookAndFeelUtil.createTitledBorder("Filter"));
		
		final GroupLayout layout = new GroupLayout(panel);
       	panel.setLayout(layout);
   		layout.setAutoCreateContainerGaps(true);
   		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
   		
		ParallelGroup hGroup = layout.createParallelGroup(Alignment.CENTER, true);
		SequentialGroup vGroup = layout.createSequentialGroup();
		layout.setHorizontalGroup(hGroup);
		layout.setVerticalGroup(vGroup);
		
		CyNetworkView netView = applicationManager.getCurrentNetworkView();
		EnrichmentMap map = emManager.getEnrichmentMap(netView.getModel().getSUID());
		EMCreationParameters params = map.getParams();
		
		List<JTextField> sliderPanelFields = new ArrayList<>();
		
		SliderBarPanel pValueSliderPanel = createPvalueSlider(map);
		sliderPanelFields.add(pValueSliderPanel.getTextField());
		
		hGroup.addComponent(pValueSliderPanel);
		vGroup.addComponent(pValueSliderPanel);
		
		if (params.isFDR()) {
			SliderBarPanel qValueSliderPanel = createQvalueSlider(map);
			sliderPanelFields.add(qValueSliderPanel.getTextField());
			
			hGroup.addComponent(qValueSliderPanel);
			vGroup.addComponent(qValueSliderPanel);
		}
		
		SliderBarPanel similaritySliderPanel = createSimilaritySlider(map);
		sliderPanelFields.add(similaritySliderPanel.getTextField());
		
		hGroup.addComponent(similaritySliderPanel);
		vGroup.addComponent(similaritySliderPanel);
		
		LookAndFeelUtil.equalizeSize(sliderPanelFields.toArray(new JComponent[sliderPanelFields.size()]));
		
		JPanel datasetListPanel = createDataSetListPanel();
		hGroup.addComponent(datasetListPanel);
		vGroup.addComponent(datasetListPanel);
   		
		if (LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);
		
		return panel;
	}
	
	private JPanel createStylePanel() {
		for (ChartType chart : ChartType.values())
			chartTypeCombo.addItem(chart);
		
		chartTypeCombo.addActionListener(evt -> {
			updateChartColorsCombo();
		});
		
		togglePublicationCheck = new JCheckBox("Publication-Ready Style");
		togglePublicationCheck.addActionListener((ActionEvent e) -> {
			dialogTaskManager.execute(visualStyleTaskFactoryProvider.get().createTaskIterator());
		});
		
		resetStyleButton = new JButton("Reset Style");
		resetStyleButton.addActionListener(evt -> {
			applyVisualStyle();
			// TODO update style fields
		});
		
		makeSmall(chartTypeLabel, chartColorsLabel, chartTypeCombo, chartColorsCombo, togglePublicationCheck,
				resetStyleButton);
		
		final JPanel panel = new JPanel();
		panel.setBorder(LookAndFeelUtil.createTitledBorder("Style"));
		
		final GroupLayout layout = new GroupLayout(panel);
       	panel.setLayout(layout);
   		layout.setAutoCreateContainerGaps(true);
   		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
   		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
				.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(Alignment.TRAILING, true)
								.addComponent(chartTypeLabel)
								.addComponent(chartColorsLabel)
						)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
								.addComponent(chartTypeCombo, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(chartColorsCombo, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(togglePublicationCheck, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						)
				)
				.addComponent(resetStyleButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(chartTypeLabel)
						.addComponent(chartTypeCombo, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(chartColorsLabel)
						.addComponent(chartColorsCombo, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addComponent(togglePublicationCheck, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addComponent(resetStyleButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		
		if (LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);
		
		return panel;
	}
	
	private SliderBarPanel createPvalueSlider(EnrichmentMap map) {
		return pvalueSliderPanels.computeIfAbsent(map.getNetworkID(), suid -> {
			double pvalue_min = map.getParams().getPvalueMin();
			double pvalue = map.getParams().getPvalue();
			return new SliderBarPanel(
					((pvalue_min == 1 || pvalue_min >= pvalue) ? 0 : pvalue_min), pvalue,
					"P-value Cutoff:",
					EnrichmentMapVisualStyle.PVALUE_DATASET1,
					EnrichmentMapVisualStyle.PVALUE_DATASET2,
					false, pvalue,
					applicationManager, emManager);
		});
	}
	
	private SliderBarPanel createQvalueSlider(EnrichmentMap map) {
		return qvalueSliderPanels.computeIfAbsent(map.getNetworkID(), suid -> {
			double qvalue_min = map.getParams().getQvalueMin();
			double qvalue = map.getParams().getQvalue();
			return new SliderBarPanel(
					((qvalue_min == 1 || qvalue_min >= qvalue) ? 0 : qvalue_min), qvalue,
					"Q-value Cutoff:",
					EnrichmentMapVisualStyle.FDR_QVALUE_DATASET1,
					EnrichmentMapVisualStyle.FDR_QVALUE_DATASET2,
					false, qvalue,
					applicationManager, emManager);
		});
	}
	
	private SliderBarPanel createSimilaritySlider(EnrichmentMap map) {
		return similaritySliderPanels.computeIfAbsent(map.getNetworkID(), suid -> {
			double similarityCutOff = map.getParams().getSimilarityCutoff();
			return new SliderBarPanel(
					similarityCutOff, 1,
					"Similarity Cutoff:",
					EnrichmentMapVisualStyle.SIMILARITY_COEFFICIENT,
					EnrichmentMapVisualStyle.SIMILARITY_COEFFICIENT,
					true, similarityCutOff,
					applicationManager, emManager);
		});
	}
	
	private JPanel createDataSetListPanel() {
		anyRadio = new JRadioButton("any");
		allRadio = new JRadioButton("all");
		
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(anyRadio);
		buttonGroup.add(allRadio);
		
		anyRadio.setSelected(true);
		allRadio.setEnabled(false); // TODO TEMPORARY
		
		SwingUtil.makeSmall(dsFilterLabel1, dsFilterLabel2, anyRadio, allRadio);
		
		checkboxListPanel = new CheckboxListPanel<>();
		
		final JPanel panel = new JPanel();
		final GroupLayout layout = new GroupLayout(panel);
       	panel.setLayout(layout);
   		layout.setAutoCreateContainerGaps(false);
   		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
   		
   		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
   				.addGroup(layout.createSequentialGroup()
   						.addComponent(dsFilterLabel1, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   						.addComponent(anyRadio, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   						.addComponent(allRadio, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   						.addComponent(dsFilterLabel2, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   				)
   				.addComponent(checkboxListPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
   		);
   		layout.setVerticalGroup(layout.createSequentialGroup()
   				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
   						.addComponent(dsFilterLabel1, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   						.addComponent(anyRadio, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   						.addComponent(allRadio, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   						.addComponent(dsFilterLabel2, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   				)
   				.addComponent(checkboxListPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
   		);
		
		if (LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);
		
		return panel;
	}
	
	private void applyVisualStyle() {
		// FIXME
		CyNetworkView netView = applicationManager.getCurrentNetworkView();
		EnrichmentMap map = emManager.getEnrichmentMap(netView.getModel().getSUID());
		Set<DataSet> dataSets = ImmutableSet.copyOf(checkboxListPanel.getSelectedDataItems());
		MasterMapStyleOptions options = new MasterMapStyleOptions(netView, map, dataSets::contains);
		MasterMapVisualStyleTask task = visualStyleTaskFactory.create(options, getChart(options));
		dialogTaskManager.execute(new TaskIterator(task));
	}

	private CyCustomGraphics2<?> getChart(MasterMapStyleOptions options) {
		CyCustomGraphics2<?> chart = null;
		ChartType type = (ChartType) chartTypeCombo.getSelectedItem();
		
		if (type != null) {
			// TODO move to another class
			ColorScheme colorScheme = (ColorScheme) chartColorsCombo.getSelectedItem();
			
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
	
	private List<Color> getColors(final ColorScheme scheme, final Map<String, List<Double>> data) {
		List<Color> colors = null;
		
		if (scheme != null && data != null && !data.isEmpty()) {
			int nColors = 0;
			
			for (final List<Double> values : data.values()) {
				if (values != null)
					nColors = Math.max(nColors, values.size());
			}
			
			colors = scheme.getColors(nColors);
		}
		
		return colors;
	}
	
	private void update(CyNetworkView netView) {
		Long netId = netView != null ? netView.getModel().getSUID() : null;
		EnrichmentMap map = netId != null ? emManager.getEnrichmentMap(netId) : null;
		
		boolean enabled = map != null;
		
		chartTypeLabel.setEnabled(enabled);
		chartColorsLabel.setEnabled(enabled);
		dsFilterLabel1.setEnabled(enabled);
		dsFilterLabel2.setEnabled(enabled);
		
		checkboxListPanel.setEnabled(enabled);
		anyRadio.setEnabled(enabled);
//		allRadio.setEnabled(enabled); TODO
		chartTypeCombo.setEnabled(enabled);
		chartColorsCombo.setEnabled(enabled);
		togglePublicationCheck.setEnabled(enabled);
		resetStyleButton.setEnabled(enabled);
		
		pvalueSliderPanels.forEach((id, p) -> p.setEnabled(id.equals(netId)));
		qvalueSliderPanels.forEach((id, p) -> p.setEnabled(id.equals(netId)));
		similaritySliderPanels.forEach((id, p) -> p.setEnabled(id.equals(netId)));
		
		updating = true;
		
		try {
			updateEmCombo(netView);
			updateDataSetList(netView);
			updateChartColorsCombo();
		} finally {
			updating = false;
		}
	}
	
	private void updateEmCombo(CyNetworkView netView) {
		Map<Long, EnrichmentMap> emMap = emManager.getAllEnrichmentMaps();
		emCombo.setEnabled(!emMap.isEmpty());
		emCombo.removeAllItems();
		
		if (!emMap.isEmpty()) {
			List<EnrichmentMap> enrichmentMaps = new ArrayList<>(emMap.values());
			Collator collator = Collator.getInstance(Locale.getDefault());
			enrichmentMaps.sort((EnrichmentMap o1, EnrichmentMap o2) -> {
				return collator.compare(o1.getName(), o2.getName());
			});
			
			enrichmentMaps.forEach((item) -> emCombo.addItem(item));
			
			EnrichmentMap em = netView != null ? emManager.getEnrichmentMap(netView.getModel().getSUID()) : null;
			emCombo.setSelectedItem(em);
		}
	}
	
	private void updateDataSetList(CyNetworkView networkView) {
		// MKTODO remember the datasets that were selected
		CheckboxListModel<DataSet> model = checkboxListPanel.getModel();
		model.clear();
		
		EnrichmentMap map = emManager.getEnrichmentMap(networkView.getModel().getSUID());
		
		if (map != null) {
			for (DataSet dataset : map.getDatasetList())
				model.addElement(new CheckboxData<>(dataset.getName(), dataset));
		}
	}
	
	private void updateChartColorsCombo() {
		ChartType type = (ChartType) chartTypeCombo.getSelectedItem();
		ColorScheme[] colorSchemes = null;
		
		switch (type) {
			case HEAT_STRIPS:
				colorSchemes = HEAT_STRIP_COLOR_SCHEMES;
				break;
			default:
				colorSchemes = REGULAR_COLOR_SCHEMES;
				break;
		}
		
		chartColorsCombo.removeAllItems();
		
		for (ColorScheme scheme : colorSchemes)
			chartColorsCombo.addItem(scheme);
	}
	
	private void setCurrentView(EnrichmentMap em) {
		// TODO should be getNetworkViewID!!!
		CyNetwork net = em != null ? networkManager.getNetwork(em.getNetworkID()) : null;
		Collection<CyNetworkView> viewList = networkViewManager.getNetworkViews(net);
		CyNetworkView view = viewList.isEmpty() ? null : viewList.iterator().next();
		applicationManager.setCurrentNetworkView(view);
	}
}
