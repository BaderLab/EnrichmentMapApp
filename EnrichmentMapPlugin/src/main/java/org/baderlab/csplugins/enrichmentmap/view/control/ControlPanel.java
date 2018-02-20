package org.baderlab.csplugins.enrichmentmap.view.control;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static javax.swing.GroupLayout.Alignment.LEADING;
import static javax.swing.GroupLayout.Alignment.TRAILING;
import static org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil.makeSmall;
import static org.cytoscape.util.swing.IconManager.ICON_BARS;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
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
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.UIManager;

import org.baderlab.csplugins.enrichmentmap.AfterInjection;
import org.baderlab.csplugins.enrichmentmap.EnrichmentMapBuildProperties;
import org.baderlab.csplugins.enrichmentmap.actions.ShowAboutDialogAction;
import org.baderlab.csplugins.enrichmentmap.model.AbstractDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EMSignatureDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.style.ChartData;
import org.baderlab.csplugins.enrichmentmap.style.ChartType;
import org.baderlab.csplugins.enrichmentmap.style.ColorScheme;
import org.baderlab.csplugins.enrichmentmap.util.NetworkUtil;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParams.Compress;
import org.baderlab.csplugins.enrichmentmap.view.util.ComboItem;
import org.baderlab.csplugins.enrichmentmap.view.util.SliderBarPanel;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.baderlab.csplugins.enrichmentmap.view.util.TextIcon;
import org.cytoscape.application.swing.CytoPanelComponent2;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.model.CyDisposable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
@SuppressWarnings("serial")
public class ControlPanel extends JPanel implements CytoPanelComponent2, CyDisposable {
	
	public static final String ID = "enrichmentmap.view.ControlPanel";
	
	private static final String BORDER_COLOR_KEY = "Separator.foreground";
	
	private Font gmFont;
	
	@Inject private CyServiceRegistrar serviceRegistrar;
	@Inject private CyNetworkManager networkManager;
	@Inject private CyNetworkViewManager networkViewManager;
	@Inject private IconManager iconManager;
	@Inject private EnrichmentMapManager emManager;
	@Inject private ShowAboutDialogAction showAboutDialogAction;
	
	private JPanel ctrlPanelsContainer;
	private final CardLayout cardLayout = new CardLayout();
	private final NullViewControlPanel nullViewCtrlPanel = new NullViewControlPanel();
	private JComboBox<CyNetworkView> emViewCombo;
	private JButton createEmButton;
	private JButton optionsButton;
	private JButton aboutButton;
	private JButton closePanelButton;
	
	private Map<Long/*CynetworkView SUID*/, EMViewControlPanel> emViewCtrlPanels = new HashMap<>();
	private Map<Long/*CynetworkView SUID*/, GMViewControlPanel> gmViewCtrlPanels = new HashMap<>();
	
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
		URL url = getClass().getClassLoader().getResource("images/enrichmentmap_logo_16.png");
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
	
	public boolean contains(CyNetworkView networkView) {
		return emViewCtrlPanels.containsKey(networkView.getSUID())
				|| gmViewCtrlPanels.containsKey(networkView.getSUID());
	}
	
	@AfterInjection
	private void createContents() {
		try {
			gmFont = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/fonts/genemania.ttf"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		setMinimumSize(new Dimension(390, 400));
		setPreferredSize(new Dimension(390, 600));
		
		JButton helpButton = SwingUtil.createOnlineHelpButton(EnrichmentMapBuildProperties.USER_MANUAL_URL,
				"Online Manual...", serviceRegistrar);
		
		makeSmall(getAboutButton(), getClosePanelButton());
		
		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(LookAndFeelUtil.isWinLAF());
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
   		layout.setHorizontalGroup(layout.createParallelGroup(CENTER, true)
   				.addGroup(layout.createSequentialGroup()
   						.addComponent(getEmViewCombo(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
   						.addPreferredGap(ComponentPlacement.RELATED)
   						.addComponent(getCreateEmButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   						.addPreferredGap(ComponentPlacement.RELATED)
   						.addComponent(getOptionsButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   				)
				.addComponent(getCtrlPanelsContainer(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(layout.createSequentialGroup()
   						.addComponent(helpButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   						.addComponent(getAboutButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   						.addGap(0, 0, Short.MAX_VALUE)
   						.addComponent(getClosePanelButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   				)
   		);
   		layout.setVerticalGroup(layout.createSequentialGroup()
   				.addGroup(layout.createParallelGroup(CENTER, false)
   						.addComponent(getEmViewCombo(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   						.addComponent(getCreateEmButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   						.addComponent(getOptionsButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   				)
   				.addComponent(getCtrlPanelsContainer(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
   				.addGroup(layout.createParallelGroup(CENTER, false)
   						.addComponent(helpButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   						.addComponent(getAboutButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   						.addComponent(getClosePanelButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   				)
   		);
		
		if (LookAndFeelUtil.isAquaLAF())
			setOpaque(false);
	}
	
	JComboBox<CyNetworkView> getEmViewCombo() {
		if (emViewCombo == null) {
			emViewCombo = new JComboBox<>();
			
			Icon gmIcon = gmFont != null
					? new TextIcon("d", gmFont.deriveFont(12.0f), UIManager.getColor("Label.foreground"), 16, 16)
					: null;
			
			emViewCombo.setRenderer(new DefaultListCellRenderer() {
				@Override
				public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
					super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
					
					if (value instanceof CyNetworkView) {
						String title = NetworkUtil.getTitle((CyNetworkView) value);
						String abbreviated = SwingUtil.abbreviate(title, 35);
						setText(abbreviated);
						list.setToolTipText(title);
						
						if (emManager.isGeneManiaEnrichmentMap((CyNetworkView) value))
							setIcon(gmIcon);
						else
							setIcon(null);
					} else {
						setText("-- Select EnrichmentMap View --");
					}
					
					return this;
				}
			});
		}
		
		return emViewCombo;
	}
	
	JButton getCreateEmButton() {
		if (createEmButton == null) {
			createEmButton = new JButton(IconManager.ICON_PLUS);
			SwingUtil.styleHeaderButton(createEmButton, iconManager.getIconFont(16.0f));
		}
		
		return createEmButton;
	}
	
	JButton getOptionsButton() {
		if (optionsButton == null) {
			optionsButton = new JButton(ICON_BARS);
			optionsButton.setToolTipText("Options...");
			SwingUtil.styleHeaderButton(optionsButton, iconManager.getIconFont(18.0f));
		}
		
		return optionsButton;
	}
	
	JButton getAboutButton() {
		if (aboutButton == null) {
			aboutButton = new JButton(showAboutDialogAction);
		}
		
		return aboutButton;
	}
	
	JButton getClosePanelButton() {
		if (closePanelButton == null) {
			closePanelButton = new JButton("Close");
		}
		
		return closePanelButton;
	}
	
	JPanel getCtrlPanelsContainer() {
		if (ctrlPanelsContainer == null) {
			ctrlPanelsContainer = new JPanel();
			ctrlPanelsContainer.setLayout(cardLayout);
			ctrlPanelsContainer.add(nullViewCtrlPanel, nullViewCtrlPanel.getName());
			
			if (LookAndFeelUtil.isAquaLAF())
				ctrlPanelsContainer.setOpaque(false);
		}
		
		return ctrlPanelsContainer;
	}
	
	EMViewControlPanel addEnrichmentMapView(CyNetworkView netView) {
		updateEmViewCombo();
		
		if (getViewControlPanel(netView) == null) {
			EMViewControlPanel p = new EMViewControlPanel(netView);
			getCtrlPanelsContainer().add(p, p.getName());
			emViewCtrlPanels.put(netView.getSUID(), p);
			
			return p;
		}
		
		return null;
	}
	
	GMViewControlPanel addGeneManiaView(CyNetworkView netView) {
		updateEmViewCombo();
		
		if (getGMViewControlPanel(netView) == null) {
			GMViewControlPanel p = new GMViewControlPanel(netView);
			getCtrlPanelsContainer().add(p, p.getName());
			gmViewCtrlPanels.put(netView.getSUID(), p);
			
			return p;
		}
		
		return null;
	}
	
	void removeEnrichmentMapView(CyNetworkView netView) {
		removeViewControlPanel(netView);
		updateEmViewCombo();
	}
	
	void update(CyNetworkView currentView) {
		// Is there an EnrichmentMap for this view?
		EnrichmentMap em = currentView != null ? emManager.getEnrichmentMap(currentView.getModel().getSUID()) : null;
		
		if (em == null)
			currentView = null; // This view is not an EnrichmentMap one!
			
		updateEmViewComboSelection(currentView);
		showViewControlPanel(currentView);
	}
	
	void updateEmViewCombo() {
		CyNetworkView selectedItem = (CyNetworkView) getEmViewCombo().getSelectedItem();
		
		Map<Long, EnrichmentMap> emMap = emManager.getAllEnrichmentMaps();
		getEmViewCombo().setEnabled(!emMap.isEmpty());
		getEmViewCombo().removeAllItems();
		
		if (!emMap.isEmpty()) {
			emMap.entrySet().stream().forEach(entry -> {
				// To make sure the original view order is preserved
				// (networkViewManager.getNetworkViewSet() may change the view order!)
				CyNetwork network = networkManager.getNetwork(entry.getKey());
				
				if (network != null) {
					// Add the primary EM network view
					Collection<CyNetworkView> views = networkViewManager.getNetworkViews(network);
					views.forEach(getEmViewCombo()::addItem);
					
					// Add GeneMANIA views associated with this EnrichmentMap
					Set<Long> gmNetIds = entry.getValue().getGeneManiaNetworkIDs();
					
					if (gmNetIds != null) {
						gmNetIds.forEach(id -> {
							CyNetwork gmNet = networkManager.getNetwork(id);
							
							if (gmNet != null) {
								Collection<CyNetworkView> gmViews = networkViewManager.getNetworkViews(gmNet);
								gmViews.forEach(getEmViewCombo()::addItem);
							}
						});
					}
				}
			});
			
			getEmViewCombo().setSelectedItem(selectedItem);
		}
	}
	
	private void updateEmViewComboSelection(CyNetworkView currentView) {
		EnrichmentMap em = currentView != null ? emManager.getEnrichmentMap(currentView.getModel().getSUID()) : null;
		getEmViewCombo().setSelectedItem(em != null ? currentView : null);
	}
	
	private void removeViewControlPanel(CyNetworkView netView) {
		EMViewControlPanel p = emViewCtrlPanels.remove(netView.getSUID());
		
		if (p == null)
			gmViewCtrlPanels.remove(netView.getSUID());
		
		if (p != null) {
			cardLayout.removeLayoutComponent(p);
			getCtrlPanelsContainer().remove(p);
		}
	}
	
	private void showViewControlPanel(CyNetworkView netView) {
		if (netView == null) {
			cardLayout.show(getCtrlPanelsContainer(), nullViewCtrlPanel.getName());
		} else {
			AbstractViewControlPanel p = getViewControlPanel(netView);
			
			if (p == null)
				p = getGMViewControlPanel(netView);
			
			if (p != null)
				cardLayout.show(getCtrlPanelsContainer(), p.getName());
		}
	}
	
	EMViewControlPanel getViewControlPanel(CyNetworkView netView) {
		return netView != null ? getViewControlPanel(netView.getSUID()) : null;
	}
	
	EMViewControlPanel getViewControlPanel(Long suid) {
		return emViewCtrlPanels.get(suid);
	}
	
	public Map<Long, EMViewControlPanel> getAllControlPanels() {
		return new HashMap<>(emViewCtrlPanels);
	}
	
	GMViewControlPanel getGMViewControlPanel(CyNetworkView netView) {
		return netView != null ? getGMViewControlPanel(netView.getSUID()) : null;
	}
	
	GMViewControlPanel getGMViewControlPanel(Long suid) {
		return gmViewCtrlPanels.get(suid);
	}
	
	public Map<Long, GMViewControlPanel> getAllGMControlPanels() {
		return new HashMap<>(gmViewCtrlPanels);
	}
	
	class AbstractViewControlPanel extends JPanel {
		
		protected final CyNetworkView networkView;
		
		protected AbstractViewControlPanel(CyNetworkView networkView, String name) {
			this.networkView = networkView;
			setName(name);
		}
	}
	
	/**
	 * Used when the current network view is a primary EnrichmentMap network.
	 */
	class EMViewControlPanel extends AbstractViewControlPanel {
		
		private JRadioButton pValueRadio;
		private JRadioButton qValueRadio;
		private final ButtonGroup nodeCutoffGroup = new ButtonGroup();
		private SliderBarPanel pValueSliderPanel;
		private SliderBarPanel qValueSliderPanel;
		private SliderBarPanel similaritySliderPanel;
		
		private JLabel chartDataLabel = new JLabel("Chart Data:");
		private JLabel chartTypeLabel = new JLabel("Chart Type:");
		private JLabel chartColorsLabel = new JLabel("Chart Colors:");
		
		private DataSetSelector dataSetSelector;
		private JCheckBox publicationReadyCheck;
		private JButton setEdgeWidthButton;
		private JButton resetStyleButton;
		
		private JComboBox<ChartData> chartDataCombo;
		private JComboBox<ChartType> chartTypeCombo;
		private JComboBox<ColorScheme> chartColorsCombo;
		private JCheckBox showChartLabelsCheck;
		
		private EMViewControlPanel(CyNetworkView networkView) {
			super(networkView, "__EM_VIEW_CONTROL_PANEL_" + networkView.getSUID());
			setBorder(BorderFactory.createLineBorder(UIManager.getColor(BORDER_COLOR_KEY)));
			
			final JPanel filterPanel = createFilterPanel();
			final JPanel stylePanel = createStylePanel();
			
			final GroupLayout layout = new GroupLayout(this);
			setLayout(layout);
			layout.setAutoCreateContainerGaps(LookAndFeelUtil.isWinLAF());
			layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
			
	   		layout.setHorizontalGroup(layout.createParallelGroup(CENTER, true)
					.addComponent(filterPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(stylePanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
	   		);
	   		layout.setVerticalGroup(layout.createSequentialGroup()
	   				.addComponent(filterPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(stylePanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
	   		);
			
			if (LookAndFeelUtil.isAquaLAF())
				setOpaque(false);
			
			update();
		}
		
		Set<AbstractDataSet> getAllDataSets() {
			return getDataSetSelector().getAllItems();
		}
		
		Set<AbstractDataSet> getCheckedDataSets() {
			return getDataSetSelector().getCheckedItems();
		}
		
		Set<AbstractDataSet> getUncheckedDataSets() {
			Set<AbstractDataSet> set = getAllDataSets();
			Set<AbstractDataSet> checkedItems = getCheckedDataSets();
			set.removeAll(checkedItems);
			
			return set;
		}
		
		@SuppressWarnings("unchecked")
		Set<EMSignatureDataSet> getSelectedSignatureDataSets() {
			final Set<?> dataSets = getDataSetSelector().getSelectedItems();
			Set<EMSignatureDataSet> sigDataSets = (Set<EMSignatureDataSet>) dataSets.stream()
					.filter(ds -> ds instanceof EMSignatureDataSet)
					.collect(Collectors.toSet());
			
			return sigDataSets;
		}
		
		void update() {
			updateDataSetSelector();
			updateChartCombos();
		}
		
		void updateDataSetSelector() {
			getDataSetSelector().update();
		}
		
		void updateChartCombos() {
			updateChartTypeCombo();
			updateChartColorsCombo();
			updateChartLabelsCheck();
		}
		
		void updateChartDataCombo() {
			long dsCount = getDataSetSelector().getCheckedItems()
					.stream()
					.filter(ds -> ds instanceof EMDataSet) // Ignore Signature Data Sets in charts
					.count();
			
			boolean b1 = getChartDataCombo().isEnabled();
			boolean b2 = dsCount > 0; // Can't have charts when no datasets are checked!
			
			if (b1 != b2) {
				getChartDataCombo().setEnabled(b2);
				updateChartCombos();
			}
		}
		
		void updateChartTypeCombo() {
			ChartData data = (ChartData) getChartDataCombo().getSelectedItem();
			
			ChartType selectedItem = (ChartType) getChartTypeCombo().getSelectedItem();
			getChartTypeCombo().removeAllItems();
			
			if(data.isChartTypeSelectable()) {
				getChartTypeCombo().addItem(ChartType.RADIAL_HEAT_MAP);
				getChartTypeCombo().addItem(ChartType.HEAT_MAP);
				getChartTypeCombo().addItem(ChartType.HEAT_STRIPS);
				
				if(selectedItem != null)
					getChartTypeCombo().setSelectedItem(selectedItem);
			}
			
			getChartTypeCombo().setEnabled(data.isChartTypeSelectable());
		}
		
		void updateChartColorsCombo() {
			ColorScheme selectedItem = (ColorScheme) getChartColorsCombo().getSelectedItem();
			ChartData data = (ChartData) getChartDataCombo().getSelectedItem();
			ChartType type = (ChartType) getChartTypeCombo().getSelectedItem();
			
			getChartColorsCombo().removeAllItems();
			
			if (data.isChartTypeSelectable()) {
				for (ColorScheme scheme : ColorScheme.values()) {
					// If "NES" and Radial Heat Map, use RD_BU_9 instead of RD_BU_3
					if (data == ChartData.NES_VALUE && type == ChartType.RADIAL_HEAT_MAP) {
						if (scheme == ColorScheme.RD_BU_3)
							continue;
					} else if (scheme == ColorScheme.RD_BU_9) {
						continue;
					}
					
					getChartColorsCombo().addItem(scheme);
				}
				
				if (selectedItem != null)
					getChartColorsCombo().setSelectedItem(selectedItem);
			}
			
			getChartColorsCombo().setEnabled(getChartTypeCombo().isEnabled() && data.isChartTypeSelectable());
		}
		
		void updateChartLabelsCheck() {
			ChartData data = (ChartData) getChartDataCombo().getSelectedItem();
			getShowChartLabelsCheck().setEnabled(data.isChartTypeSelectable());
			if(!data.isChartTypeSelectable())
				getShowChartLabelsCheck().setSelected(false);
		}
		
		CyNetworkView getNetworkView() {
			return networkView;
		}
		
		private EnrichmentMap getEnrichmentMap() {
			return emManager.getEnrichmentMap(networkView.getModel().getSUID());
		}
		
		private JPanel createFilterPanel() {
			final JPanel panel = new JPanel();
			panel.setBorder(LookAndFeelUtil.createTitledBorder("Filter"));
			
			final GroupLayout layout = new GroupLayout(panel);
	       	panel.setLayout(layout);
	   		layout.setAutoCreateContainerGaps(true);
	   		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
	   		
			ParallelGroup hGroup = layout.createParallelGroup(CENTER, true);
			SequentialGroup vGroup = layout.createSequentialGroup();
			layout.setHorizontalGroup(hGroup);
			layout.setVerticalGroup(vGroup);
			
			EnrichmentMap map = getEnrichmentMap();
			
			if (map != null) {
				EMCreationParameters params = map.getParams();
				List<JTextField> sliderPanelFields = new ArrayList<>();
				
				JLabel nodeCutoffLabel = new JLabel();
				makeSmall(nodeCutoffLabel);
				
				if (params != null) {
					if (params.isFDR()) {
						nodeCutoffLabel.setText("Node Cutoff:");
					
						qValueSliderPanel = createQvalueSlider(map);
						sliderPanelFields.add(qValueSliderPanel.getTextField());
						
						hGroup.addGroup(layout.createSequentialGroup()
								.addComponent(nodeCutoffLabel)
								.addGap(10, 20, Short.MAX_VALUE)
								.addComponent(getPValueRadio())
								.addComponent(getQValueRadio())
						);
						vGroup.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
								.addComponent(nodeCutoffLabel)
								.addComponent(getPValueRadio())
								.addComponent(getQValueRadio())
						);
						
						hGroup.addComponent(qValueSliderPanel);
						vGroup.addComponent(qValueSliderPanel);
						
						nodeCutoffGroup.add(getPValueRadio());
						nodeCutoffGroup.add(getQValueRadio());
						nodeCutoffGroup.setSelected(getQValueRadio().getModel(), true);
					} else {
						nodeCutoffLabel.setText("Node Cutoff (P-value):");
						hGroup.addComponent(nodeCutoffLabel, Alignment.LEADING);
						vGroup.addComponent(nodeCutoffLabel);
					}
				}
				
				pValueSliderPanel = createPvalueSlider(map);
				sliderPanelFields.add(pValueSliderPanel.getTextField());
				
				hGroup.addComponent(pValueSliderPanel);
				vGroup.addComponent(pValueSliderPanel);
				
				similaritySliderPanel = createSimilaritySlider(map);
				sliderPanelFields.add(similaritySliderPanel.getTextField());
				
				hGroup.addComponent(similaritySliderPanel);
				vGroup.addComponent(similaritySliderPanel);
				
				LookAndFeelUtil.equalizeSize(sliderPanelFields.toArray(new JComponent[sliderPanelFields.size()]));
			}
			
			hGroup.addComponent(getDataSetSelector());
			vGroup.addPreferredGap(ComponentPlacement.UNRELATED).addComponent(getDataSetSelector());
	   		
			if (LookAndFeelUtil.isAquaLAF())
				panel.setOpaque(false);
			
			updateFilterPanel();
			
			return panel;
		}
		
		private JPanel createStylePanel() {
			makeSmall(chartDataLabel, chartTypeLabel, chartColorsLabel);
			makeSmall(getChartDataCombo(), getChartTypeCombo(), getChartColorsCombo(), getShowChartLabelsCheck());
			makeSmall(getPublicationReadyCheck(), getSetEdgeWidthButton(), getResetStyleButton());
			
			final JPanel panel = new JPanel();
			panel.setBorder(LookAndFeelUtil.createTitledBorder("Style"));
			
			final GroupLayout layout = new GroupLayout(panel);
	       	panel.setLayout(layout);
	   		layout.setAutoCreateContainerGaps(true);
	   		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
	   		
			layout.setHorizontalGroup(layout.createParallelGroup(CENTER, true)
					.addGroup(layout.createSequentialGroup()
							.addGroup(layout.createParallelGroup(TRAILING, true)
									.addComponent(chartDataLabel)
									.addComponent(chartTypeLabel)
									.addComponent(chartColorsLabel)
							)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(layout.createParallelGroup(LEADING, true)
									.addComponent(getChartDataCombo(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
									.addComponent(getChartTypeCombo(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
									.addComponent(getChartColorsCombo(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
									.addComponent(getShowChartLabelsCheck(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
									.addComponent(getPublicationReadyCheck(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
							)
					)
					.addGroup(layout.createSequentialGroup()
							.addComponent(getSetEdgeWidthButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(getResetStyleButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(CENTER, false)
							.addComponent(chartDataLabel)
							.addComponent(getChartDataCombo(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addGroup(layout.createParallelGroup(CENTER, false)
							.addComponent(chartTypeLabel)
							.addComponent(getChartTypeCombo(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addGroup(layout.createParallelGroup(CENTER, false)
							.addComponent(chartColorsLabel)
							.addComponent(getChartColorsCombo(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addComponent(getShowChartLabelsCheck(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getPublicationReadyCheck(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(layout.createParallelGroup(CENTER, false)
							.addComponent(getSetEdgeWidthButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getResetStyleButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
			);
			
			if (LookAndFeelUtil.isAquaLAF())
				panel.setOpaque(false);
			
			return panel;
		}
		
		private SliderBarPanel createPvalueSlider(EnrichmentMap map) {
			double pvalueMin = map.getParams().getPvalueMin();
			double pvalue = map.getParams().getPvalue();
			
			return new SliderBarPanel(
					(pvalueMin == 1 || pvalueMin >= pvalue ? 0 : pvalueMin),
					pvalue,
					pvalue
			);
		}
		
		private SliderBarPanel createQvalueSlider(EnrichmentMap map) {
			double qvalueMin = map.getParams().getQvalueMin();
			double qvalue = map.getParams().getQvalue();
			
			return new SliderBarPanel(
					(qvalueMin == 1 || qvalueMin >= qvalue ? 0 : qvalueMin),
					qvalue,
					qvalue
			);
		}
		
		private SliderBarPanel createSimilaritySlider(EnrichmentMap map) {
			double similarityCutOff = map.getParams().getSimilarityCutoff();
			
			return new SliderBarPanel(
					similarityCutOff,
					1,
					similarityCutOff,
					"Edge Cutoff (Similarity):"
			);
		}
		
		JRadioButton getPValueRadio() {
			if (pValueRadio == null) {
				pValueRadio = new JRadioButton("P-value");
				makeSmall(pValueRadio);
			}
			
			return pValueRadio;
		}
		
		JRadioButton getQValueRadio() {
			if (qValueRadio == null) {
				qValueRadio = new JRadioButton("Q-value");
				makeSmall(qValueRadio);
			}
			
			return qValueRadio;
		}
		
		SliderBarPanel getPValueSliderPanel() {
			return pValueSliderPanel;
		}
		
		SliderBarPanel getQValueSliderPanel() {
			return qValueSliderPanel;
		}
		
		SliderBarPanel getSimilaritySliderPanel() {
			return similaritySliderPanel;
		}
		
		DataSetSelector getDataSetSelector() {
			if (dataSetSelector == null) {
				dataSetSelector = new DataSetSelector(getEnrichmentMap(), serviceRegistrar);
			}

			return dataSetSelector;
		}

		JComboBox<ChartData> getChartDataCombo() {
			if (chartDataCombo == null) {
				chartDataCombo = new JComboBox<>();
				chartDataCombo.addItem(ChartData.NONE);
				chartDataCombo.addItem(ChartData.NES_VALUE);
				chartDataCombo.addItem(ChartData.P_VALUE);
				
				EnrichmentMap map = getEnrichmentMap();
				if (map != null) {
					EMCreationParameters params = map.getParams();
					if(params != null && params.isFDR())
						chartDataCombo.addItem(ChartData.FDR_VALUE);
					if(map.isTwoPhenotypeGeneric())
						chartDataCombo.addItem(ChartData.PHENOTYPES);
				}
				
				chartDataCombo.addItem(ChartData.DATA_SET);
			}
			
			return chartDataCombo;
		}
		
		JComboBox<ChartType> getChartTypeCombo() {
			if (chartTypeCombo == null) {
				chartTypeCombo = new JComboBox<>();
				chartTypeCombo.addItem(ChartType.RADIAL_HEAT_MAP);
				chartTypeCombo.addItem(ChartType.HEAT_MAP);
				chartTypeCombo.addItem(ChartType.HEAT_STRIPS);
			}
			
			return chartTypeCombo;
		}
		
		JComboBox<ColorScheme> getChartColorsCombo() {
			if (chartColorsCombo == null) {
				chartColorsCombo = new JComboBox<>();
				
				final JPanel cell = new JPanel();
				cell.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
				
				final JLabel nameLabel = new JLabel(" --- ");
				nameLabel.setFont(nameLabel.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
				
				final BoxLayout layout = new BoxLayout(cell, BoxLayout.X_AXIS);
				cell.setLayout(layout);
				
				final Color borderColor = UIManager.getColor("Label.disabledForeground");
				
				chartColorsCombo.setRenderer((JList<? extends ColorScheme> list,
						ColorScheme value, int index, boolean isSelected, boolean cellHasFocus) -> {
					String bg = isSelected ? "Table.selectionBackground" : "Table.background";
					String fg = isSelected ? "Table.selectionForeground" : "Table.foreground";
					
					if (!chartColorsCombo.isEnabled())
						fg = "ComboBox.disabledForeground";
					
					cell.setBackground(UIManager.getColor(bg));
					nameLabel.setForeground(UIManager.getColor(fg));

					nameLabel.setText(value != null ? value.getName() : " ");
					cell.setToolTipText(value != null ? value.getDescription() : null);
					
					cell.removeAll();
					
					if (chartColorsCombo.isEnabled()) {
						List<Color> colors = value != null && chartColorsCombo.isEnabled() ?
								value.getColors() : Collections.emptyList();
						int total = colors.size();
						
						for (int i = 0; i < total; i++) {
							JLabel lbl = new JLabel(total > 3 ? " " : Strings.repeat(" ", 4));
							
							// Make middle color legend larger
							if (total > 9 && i == (total - 1) / 2)
								lbl.setText(Strings.repeat(" ", total - 9));
							
							lbl.setOpaque(true);
							lbl.setBackground(colors.size() > 2 ? colors.get(i) : cell.getBackground());
							
							if (i == 0)
								lbl.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 0, borderColor));
							else if (i == total - 1)
								lbl.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 1, borderColor));
							else
								lbl.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, borderColor));
							
							cell.add(lbl);
						}
						
						cell.add(Box.createHorizontalStrut(15));
						cell.add(nameLabel);
					}
					
					return cell;
				});
			}
			
			return chartColorsCombo;
		}
		
		JCheckBox getShowChartLabelsCheck() {
			if (showChartLabelsCheck == null) {
				showChartLabelsCheck = new JCheckBox("Show Chart Labels");
			}
			
			return showChartLabelsCheck;
		}
		
		JCheckBox getPublicationReadyCheck() {
			if (publicationReadyCheck == null) {
				publicationReadyCheck = new JCheckBox("Publication-Ready");
			}
			
			return publicationReadyCheck;
		}
		
		JButton getSetEdgeWidthButton() {
			if (setEdgeWidthButton == null) {
				setEdgeWidthButton = new JButton("Set Signature Edge Width...");
				
				if (isAquaLAF())
					setEdgeWidthButton.putClientProperty("JButton.buttonType", "gradient");
			}
			
			return setEdgeWidthButton;
		}
		
		JButton getResetStyleButton() {
			if (resetStyleButton == null) {
				resetStyleButton = new JButton(IconManager.ICON_REFRESH);
				resetStyleButton.setFont(serviceRegistrar.getService(IconManager.class).getIconFont(13.0f));
				resetStyleButton.setToolTipText("Reset Style");
				
				if (isAquaLAF())
					resetStyleButton.putClientProperty("JButton.buttonType", "gradient");
			}
			
			return resetStyleButton;
		}
		
		void updateFilterPanel() {
			if (nodeCutoffGroup.getSelection() != null) {
				boolean isQValue = nodeCutoffGroup.getSelection().equals(getQValueRadio().getModel());
				getPValueSliderPanel().setVisible(!isQValue);
				getQValueSliderPanel().setVisible(isQValue);
			}
		}
	}
	
	/**
	 * Used when the current network view is a GeneMANIA one from EnrichmentMap genes.
	 */
	class GMViewControlPanel extends AbstractViewControlPanel {
		
		private JComboBox<ComboItem<Compress>> compressCombo;
		private JComboBox<String> compareCombo;
		
		private GMViewControlPanel(CyNetworkView networkView) {
			super(networkView, "__EM_CHILD_VIEW_CONTROL_PANEL_" + networkView.getSUID());
			setBorder(BorderFactory.createLineBorder(UIManager.getColor(BORDER_COLOR_KEY)));
			
			final JPanel stylePanel = createStylePanel();
			
			final GroupLayout layout = new GroupLayout(this);
			setLayout(layout);
			layout.setAutoCreateContainerGaps(LookAndFeelUtil.isWinLAF());
			layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
			
	   		layout.setHorizontalGroup(layout.createParallelGroup(CENTER, true)
					.addComponent(stylePanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
	   		);
	   		layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(stylePanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
	   		);
			
			if (LookAndFeelUtil.isAquaLAF())
				setOpaque(false);
			
			update();
		}
		
		private JPanel createStylePanel() {
			final JLabel compressLabel = new JLabel("Compress:");
			final JLabel compareLabel = new JLabel("Compare:");
			
			makeSmall(compressLabel, compareLabel, getCompressCombo(), getCompareCombo());
			
			final JPanel panel = new JPanel();
			panel.setBorder(LookAndFeelUtil.createTitledBorder("Style"));
			
			final GroupLayout layout = new GroupLayout(panel);
	       	panel.setLayout(layout);
	   		layout.setAutoCreateContainerGaps(true);
	   		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
	   		
			layout.setHorizontalGroup(layout.createParallelGroup(CENTER, true)
					.addGroup(layout.createSequentialGroup()
							.addGroup(layout.createParallelGroup(TRAILING, true)
									.addComponent(compressLabel)
									.addComponent(compareLabel)
							)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(layout.createParallelGroup(LEADING, true)
									.addComponent(getCompressCombo(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
									.addComponent(getCompareCombo(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
							)
					)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(CENTER, false)
							.addComponent(compressLabel)
							.addComponent(getCompressCombo(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addGroup(layout.createParallelGroup(CENTER, false)
							.addComponent(compareLabel)
							.addComponent(getCompareCombo(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
			);
			
			if (LookAndFeelUtil.isAquaLAF())
				panel.setOpaque(false);
			
			return panel;
		}
		
		JComboBox<ComboItem<Compress>> getCompressCombo() {
			if (compressCombo == null) {
				compressCombo = new JComboBox<>();
				compressCombo.addItem(new ComboItem<>(Compress.NONE, "-None-"));
				compressCombo.setSelectedIndex(0);
			}
			
			return compressCombo;
		}
		
		public JComboBox<String> getCompareCombo() {
			if (compareCombo == null) {
				compareCombo = new JComboBox<>();
				
				// TODO add values
			}
			
			return compareCombo;
		}
		
		void update() {
			EnrichmentMap em = networkView != null ? emManager.getEnrichmentMap(networkView.getModel().getSUID()) : null;
			
			compressCombo.removeAllItems();
			compressCombo.addItem(new ComboItem<>(Compress.NONE, "-None-"));
			
			if (em != null) {
				if (em.hasClassData()) {
					compressCombo.addItem(new ComboItem<>(Compress.CLASS_MEDIAN, "Class: Median"));
					compressCombo.addItem(new ComboItem<>(Compress.CLASS_MIN, "Class: Min"));
					compressCombo.addItem(new ComboItem<>(Compress.CLASS_MAX, "Class: Max"));
				}
				
				compressCombo.addItem(new ComboItem<>(Compress.DATASET_MEDIAN, "Data Set: Median"));
				compressCombo.addItem(new ComboItem<>(Compress.DATASET_MIN, "Data Set: Min"));
				compressCombo.addItem(new ComboItem<>(Compress.DATASET_MAX, "Data Set: Max"));
			}
			
			if (em != null) {
				if (em.hasClassData())
					compressCombo.setSelectedItem(ComboItem.of(Compress.CLASS_MEDIAN));
				else
					compressCombo.setSelectedItem(ComboItem.of(Compress.DATASET_MEDIAN));
			} else {
				compressCombo.setSelectedItem(ComboItem.of(Compress.NONE));
			}
		}
		
		private Object updateGeneManiaStyle() {
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	private class NullViewControlPanel extends JPanel {

		public static final String NAME = "__NULL_VIEW_CONTROL_PANEL";
		
		private JLabel infoLabel;
		
		NullViewControlPanel() {
			setName(NAME);
			setBorder(BorderFactory.createLineBorder(UIManager.getColor(BORDER_COLOR_KEY)));
			
			final GroupLayout layout = new GroupLayout(this);
			this.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGap(0, 0, Short.MAX_VALUE)
					.addComponent(getInfoLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addGap(0, 0, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGap(0, 0, Short.MAX_VALUE)
					.addComponent(getInfoLabel())
					.addGap(0, 0, Short.MAX_VALUE)
			);
			
			if (LookAndFeelUtil.isAquaLAF())
				setOpaque(false);
		}
		
		private JLabel getInfoLabel() {
			if (infoLabel == null) {
				infoLabel = new JLabel("No EnrichmentMap View selected");
				infoLabel.setEnabled(false);
				infoLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
			}
			
			return infoLabel;
		}
	}
}
