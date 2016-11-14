package org.baderlab.csplugins.enrichmentmap.view.controlpanel;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.baderlab.csplugins.enrichmentmap.util.SwingUtil.makeSmall;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

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
import javax.swing.LayoutStyle.ComponentPlacement;

import org.baderlab.csplugins.enrichmentmap.AfterInjection;
import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.style.EnrichmentMapVisualStyle;
import org.baderlab.csplugins.enrichmentmap.style.MasterMapStyleOptions;
import org.baderlab.csplugins.enrichmentmap.style.MasterMapVisualStyleTask;
import org.baderlab.csplugins.enrichmentmap.task.CreatePublicationVisualStyleTaskFactory;
import org.baderlab.csplugins.enrichmentmap.util.SwingUtil;
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
import org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Provider;

@SuppressWarnings("serial")
public class ControlPanel extends JPanel
		implements CytoPanelComponent2, CyDisposable, SetCurrentNetworkViewListener, NetworkAboutToBeDestroyedListener {
	
	public static final String ID = "enrichmentmap.view.ControlPanel";
	
	@Inject private CyApplicationManager applicationManager;
	@Inject private DialogTaskManager dialogTaskManager;
	
	@Inject private EnrichmentMapManager emManager;
//	@Inject private Provider<MasterMapDialogAction> masterMapDialogActionProvider;
	@Inject private MasterMapVisualStyleTask.Factory visualStyleTaskFactory;
	@Inject private Provider<CreatePublicationVisualStyleTaskFactory> visualStyleTaskFactoryProvider;
	
	private CheckboxListPanel<DataSet> checkboxListPanel;
	private JRadioButton anyRadio;
	private JRadioButton allRadio;
	private JButton resetStyleButton;
	
	private Map<Long, SliderBarPanel> pvalueSliderPanels = new HashMap<>(); // TODO: Delete??? Or advanced options
	private Map<Long, SliderBarPanel> qvalueSliderPanels = new HashMap<>();
	private Map<Long, SliderBarPanel> similaritySliderPanels = new HashMap<>();
	
	@AfterInjection
	private void createContents() {
		final JPanel filterPanel = createFilterPanel();
		final JPanel stylePanel = createStylePanel();
		
		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(LookAndFeelUtil.isWinLAF());
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
   		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
				.addComponent(filterPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(stylePanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
   		);
   		layout.setVerticalGroup(layout.createSequentialGroup()
   				.addComponent(filterPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(stylePanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   		);
		
		if (LookAndFeelUtil.isAquaLAF())
			setOpaque(false);
	}
	
	@Override
	public void handleEvent(NetworkAboutToBeDestroyedEvent event) {
		Long suid = event.getNetwork().getSUID();
		pvalueSliderPanels.remove(suid);
		qvalueSliderPanels.remove(suid);
		similaritySliderPanels.remove(suid);
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
		
		CyNetworkView networkView = applicationManager.getCurrentNetworkView();
		EnrichmentMap map = emManager.getEnrichmentMap(networkView.getModel().getSUID());
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
		JLabel chartTypeLabel = new JLabel("Chart Type:");
		JLabel chartColorsLabel = new JLabel("Chart Colors:");
		
		JComboBox<String> chartTypeCombo = new JComboBox<>();
		JComboBox<String> chartColorsCombo = new JComboBox<>();
		
		JCheckBox togglePublicationCheck = new JCheckBox("Publication-Ready Style");
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
		JLabel label1 = new JLabel("Show edges from ");
		JLabel label2 = new JLabel(" of these data sets:");
		
		anyRadio = new JRadioButton("any");
		allRadio = new JRadioButton("all");
		
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(anyRadio);
		buttonGroup.add(allRadio);
		
		anyRadio.setSelected(true);
		allRadio.setEnabled(false); // TODO TEMPORARY
		
		SwingUtil.makeSmall(label1, label2, anyRadio, allRadio);
		
		checkboxListPanel = new CheckboxListPanel<>();
		
		final JPanel panel = new JPanel();
		final GroupLayout layout = new GroupLayout(panel);
       	panel.setLayout(layout);
   		layout.setAutoCreateContainerGaps(true);
   		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
   		
   		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
   				.addGroup(layout.createSequentialGroup()
   						.addComponent(label1, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   						.addComponent(anyRadio, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   						.addComponent(allRadio, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   						.addComponent(label2, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   				)
   				.addComponent(checkboxListPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
   		);
   		layout.setVerticalGroup(layout.createSequentialGroup()
   				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
   						.addComponent(label1, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   						.addComponent(anyRadio, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   						.addComponent(allRadio, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   						.addComponent(label2, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   				)
   				.addComponent(checkboxListPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
   		);
		
		if (LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);
		
		return panel;
	}
	
	private void updateDataSetList(EnrichmentMap map) {
		// MKTODO remember the datasets that were selected
		CheckboxListModel<DataSet> model = checkboxListPanel.getModel();
		model.clear();
		for(DataSet dataset : map.getDatasetList()) {
			model.addElement(new CheckboxData<>(dataset.getName(), dataset));
		}
	}
	
	private void applyVisualStyle() {
		// FIXME
		CyNetworkView networkView = applicationManager.getCurrentNetworkView();
		EnrichmentMap map = emManager.getEnrichmentMap(networkView.getModel().getSUID());
		Set<DataSet> dataSets = ImmutableSet.copyOf(checkboxListPanel.getSelectedDataItems());
		MasterMapStyleOptions options = new MasterMapStyleOptions(networkView, map, dataSets::contains);
		MasterMapVisualStyleTask task = visualStyleTaskFactory.create(options);
		dialogTaskManager.execute(new TaskIterator(task));
	}

	@Override
	public void handleEvent(SetCurrentNetworkViewEvent e) {
		CyNetworkView networkView = e.getNetworkView();
		EnrichmentMap map = emManager.getEnrichmentMap(networkView.getModel().getSUID());
		
		if (map != null)
			updateDataSetList(map);
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

}
