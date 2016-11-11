package org.baderlab.csplugins.enrichmentmap.view.controlpanel;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.baderlab.csplugins.enrichmentmap.util.SwingUtil.makeSmall;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
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
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

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
	private JRadioButton anyButton;
	private JRadioButton allButton;
	
	private Map<Long, SliderBarPanel> pvalueSliderPanels = new HashMap<>(); // TODO: Delete??? Or advanced options
	private Map<Long, SliderBarPanel> qvalueSliderPanels = new HashMap<>();
	private Map<Long, SliderBarPanel> similaritySliderPanels = new HashMap<>();
	
	@AfterInjection
	private void createContents() {
		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
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
		
		JToggleButton togglePublicationButton = new JToggleButton("Publication-Ready Visual Style");
		togglePublicationButton.addActionListener((ActionEvent e) -> {
			dialogTaskManager.execute(visualStyleTaskFactoryProvider.get().createTaskIterator());
		});
		
		makeSmall(togglePublicationButton);
		
		hGroup.addComponent(togglePublicationButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
		vGroup.addComponent(togglePublicationButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
		
		JPanel datasetListPanel = createDataSetListPanel();
		hGroup.addComponent(datasetListPanel);
		vGroup.addComponent(datasetListPanel);
		
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
	
//	private JMenuBar createMenuBar() {
//		JMenuBar menuBar = new JMenuBar();
//		
//		JMenu newMenu = new JMenu("New");
//		newMenu.add(new JMenuItem(masterMapDialogActionProvider.get()));
//		
//		JMenu optionsMenu = new JMenu("Options");
//		JMenu helpMenu = new JMenu("Help");
//		
//		menuBar.add(newMenu);
//		menuBar.add(optionsMenu);
//		menuBar.add(helpMenu);
//		
//		return menuBar;
//	}
	
	private SliderBarPanel createPvalueSlider(EnrichmentMap map) {
		return pvalueSliderPanels.computeIfAbsent(map.getNetworkID(), suid -> {
			double pvalue_min = map.getParams().getPvalueMin();
			double pvalue = map.getParams().getPvalue();
			return new SliderBarPanel(
					((pvalue_min == 1 || pvalue_min >= pvalue) ? 0 : pvalue_min), pvalue,
					"P-value Cutoff",
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
					"Q-value Cutoff",
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
					"Similarity Cutoff",
					EnrichmentMapVisualStyle.SIMILARITY_COEFFICIENT,
					EnrichmentMapVisualStyle.SIMILARITY_COEFFICIENT,
					true, similarityCutOff,
					applicationManager, emManager);
		});
	}
	
	private JPanel createDataSetListPanel() {
		// Radio button panel
		JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		JLabel edgesLabel = new JLabel("Edges:");
		anyButton = new JRadioButton("Any of");
		allButton = new JRadioButton("All of");
		SwingUtil.makeSmall(edgesLabel, anyButton, allButton);
		
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(anyButton);
		buttonGroup.add(allButton);
		
		anyButton.setSelected(true);
		allButton.setEnabled(false); // TEMPORARY
		
		radioPanel.add(edgesLabel);
		radioPanel.add(anyButton);
		radioPanel.add(allButton);
		
		// Top panel
		JPanel topPanel = new JPanel(new BorderLayout());
		JLabel visualizeLabel = new JLabel("Visualize Data Sets");
		SwingUtil.makeSmall(visualizeLabel);
		topPanel.add(visualizeLabel, BorderLayout.CENTER);
		topPanel.add(radioPanel, BorderLayout.EAST);
		
		checkboxListPanel = new CheckboxListPanel<>();
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(topPanel, BorderLayout.NORTH);
		panel.add(checkboxListPanel, BorderLayout.CENTER);
		
		if (LookAndFeelUtil.isAquaLAF()) {
			panel.setOpaque(false);
			topPanel.setOpaque(false);
			radioPanel.setOpaque(false);
		}
		
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
