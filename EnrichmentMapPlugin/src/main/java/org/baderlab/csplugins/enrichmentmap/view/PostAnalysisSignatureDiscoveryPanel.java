package org.baderlab.csplugins.enrichmentmap.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolTip;
import javax.swing.ScrollPaneConstants;

import org.baderlab.csplugins.enrichmentmap.FilterParameters;
import org.baderlab.csplugins.enrichmentmap.FilterType;
import org.baderlab.csplugins.enrichmentmap.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.actions.LoadSignatureSetsActionListener;
import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.JMultiLineToolTip;
import org.baderlab.csplugins.enrichmentmap.model.Ranking;
import org.baderlab.csplugins.enrichmentmap.model.SetOfGeneSets;
import org.baderlab.csplugins.enrichmentmap.task.FilterMetric;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.work.swing.DialogTaskManager;

@SuppressWarnings("serial")
public class PostAnalysisSignatureDiscoveryPanel extends JPanel {

	private final PostAnalysisInputPanel parentPanel;

	private final CyApplicationManager cyApplicationManager;
	private final CySwingApplication application;
	private final StreamUtil streamUtil;
	private final DialogTaskManager dialog;

	private final static int RIGHT = 0, DOWN = 1, UP = 2, LEFT = 3; // image States

	private JFormattedTextField signatureDiscoveryGMTFileNameTextField;
	private PostAnalysisWeightPanel weightPanel;

	private JLabel avail_sig_sets_counter_label;
	private JList<String> avail_sig_sets_field;
	private CollapsiblePanel signature_genesets;
	private JPanel signaturePanel;
	private JList<String> selected_sig_sets_field;
	private JLabel selected_sig_sets_counter_label;

	private DefaultListModel<String> avail_sig_sets = new DefaultListModel<>();
	private DefaultListModel<String> selected_sig_sets = new DefaultListModel<>();

	private JFormattedTextField filterTextField;
	private JComboBox<FilterType> filterTypeCombo;
	
	// used for filtering
	private int hypergomUniverseSize;
	private Ranking mannWhitRanks;
	
	private SetOfGeneSets signatureGenesets;
	
	private final Map<FilterType,Double> savedFilterValues = FilterType.createMapOfDefaults();
	

	public PostAnalysisSignatureDiscoveryPanel(PostAnalysisInputPanel parentPanel,
			CyApplicationManager cyApplicationManager, CySwingApplication application, StreamUtil streamUtil,
			DialogTaskManager dialog) {

		this.parentPanel = parentPanel;
		this.cyApplicationManager = cyApplicationManager;
		this.application = application;
		this.streamUtil = streamUtil;
		this.dialog = dialog;
		
		createSignatureDiscoveryOptionsPanel();
	}

	private void createSignatureDiscoveryOptionsPanel() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		//Gene set file panel
		CollapsiblePanel gmtPanel = createSignatureDiscoveryGMTPanel();
		gmtPanel.setCollapsed(false);

		//signature collapsible panel
		signature_genesets = new CollapsiblePanel("Signature Genesets");
		signature_genesets.setLayout(new BorderLayout());
		signature_genesets.setCollapsed(false);

		signaturePanel = new JPanel();
		signaturePanel.setLayout(new BoxLayout(signaturePanel, BoxLayout.Y_AXIS));
		//signaturePanel.setPreferredSize(new Dimension(280, 300));
		signaturePanel.setAlignmentX((float) 0.0); //LEFT

		//List of all Signature Genesets 
		JPanel availableLabel = new JPanel(new FlowLayout());
		availableLabel.add(new JLabel("Available Signature-Genesets:"));
		avail_sig_sets_counter_label = new JLabel("(0)");
		availableLabel.add(avail_sig_sets_counter_label);
		signaturePanel.add(availableLabel);
		
		avail_sig_sets_field = new JList<>();
		avail_sig_sets_field.setModel(avail_sig_sets);
		
		JScrollPane avail_sig_sets_scroll = new JScrollPane(avail_sig_sets_field,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		avail_sig_sets_scroll.setPreferredSize(new Dimension(250, 200));
		avail_sig_sets_scroll.setMinimumSize(new Dimension(250, 150));
		avail_sig_sets_scroll.setMaximumSize(new Dimension(290, 300));
		signaturePanel.add(avail_sig_sets_scroll);

		//(Un-)Select-Buttons
		Icon[] icons = createArrowIcons();
		JPanel selectButtonPanel = new JPanel();
		selectButtonPanel.add(new JPanel()); //spacer
		selectButtonPanel.setLayout(new BoxLayout(selectButtonPanel, BoxLayout.X_AXIS));
		JButton selectButton = new JButton(icons[DOWN]);
		selectButton.getSize().width = 30;
		selectButtonPanel.add(selectButton);
		selectButtonPanel.add(new JPanel()); //spacer
		JButton unselectButton = new JButton(icons[UP]);
		unselectButton.getSize().width = 30;
		selectButtonPanel.add(unselectButton);
		selectButtonPanel.add(new JPanel()); //spacer
		signaturePanel.add(selectButtonPanel);

		//List of selected Signature Genesets 
		JPanel selectedLabel = new JPanel();
		selectedLabel.add(new JLabel("Selected Signature-Genesets:"));
		selected_sig_sets_counter_label = new JLabel("(0)");
		selectedLabel.add(selected_sig_sets_counter_label);
		signaturePanel.add(selectedLabel);
		
		selected_sig_sets_field = new JList<>();
		selected_sig_sets_field.setModel(selected_sig_sets);

		JScrollPane selected_sig_sets_scroll = new JScrollPane(selected_sig_sets_field,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		selected_sig_sets_scroll.setPreferredSize(new Dimension(250, 100));
		selected_sig_sets_scroll.setMinimumSize(new Dimension(250, 100));
		selected_sig_sets_scroll.setMaximumSize(new Dimension(290, 200));
		signaturePanel.add(selected_sig_sets_scroll);

		// Add clear panels button
		JPanel clearButtonPanel = new JPanel();
		clearButtonPanel.setLayout(new FlowLayout());
		JButton clearButton = new JButton("Clear Signature Genesets");
		clearButtonPanel.add(clearButton);
		signaturePanel.add(clearButtonPanel);

		clearButton.addActionListener(e -> {
			avail_sig_sets.clear();
			avail_sig_sets_field.clearSelection();
			setAvSigCount(0);
			selected_sig_sets.clear();
			selected_sig_sets_field.clearSelection();
			setSelSigCount(0);
		});

		selectButton.addActionListener(e -> {
			int[] selected = avail_sig_sets_field.getSelectedIndices();
			for(int i = selected.length; i > 0; i--) {
				selected_sig_sets.addElement(avail_sig_sets.get(selected[i - 1]));
				avail_sig_sets.remove(selected[i - 1]);
			}
			setSelSigCount(selected_sig_sets.size());
			setAvSigCount(avail_sig_sets.size());
		});
		
		unselectButton.addActionListener(e -> {
			int[] selected = selected_sig_sets_field.getSelectedIndices();
			for(int i = selected.length; i > 0; i--) {
				avail_sig_sets.addElement(selected_sig_sets.get(selected[i - 1]));
				selected_sig_sets.remove(selected[i - 1]);
			}

			//Sort the Genesets:
			List<String> setNamesArray = Collections.list(avail_sig_sets.elements());
			Collections.sort(setNamesArray);
			avail_sig_sets.removeAllElements();
			for(String name : setNamesArray) {
				avail_sig_sets.addElement(name);
			}
			setAvSigCount(avail_sig_sets.size());
			setSelSigCount(selected_sig_sets.size());
		});
		
		signature_genesets.getContentPane().add(signaturePanel, BorderLayout.NORTH);

		//Parameters collapsible panel
		weightPanel = new PostAnalysisWeightPanel(application);
		weightPanel.setCollapsed(false);

		add(gmtPanel);
		add(signature_genesets);
		add(weightPanel);
	}

	/**
	 * @return CollapsiblePanel for choosing and loading GMT and SignatureGMT
	 *         Geneset-Files
	 */
	private CollapsiblePanel createSignatureDiscoveryGMTPanel() {
		CollapsiblePanel collapsiblePanel = new CollapsiblePanel("Gene-Sets");

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		//add SigGMT file
		JLabel SigGMTLabel = new JLabel("SigGMT:") {
			public JToolTip createToolTip() {
				return new JMultiLineToolTip();
			}
		};
		SigGMTLabel.setToolTipText(PostAnalysisInputPanel.gmtTip);
		JButton selectSigGMTFileButton = new JButton();
		signatureDiscoveryGMTFileNameTextField = new JFormattedTextField();
		signatureDiscoveryGMTFileNameTextField.setColumns(15);
		final Color textFieldForeground = signatureDiscoveryGMTFileNameTextField.getForeground();

		signatureDiscoveryGMTFileNameTextField.setFont(new Font("Dialog", 1, 10));
		signatureDiscoveryGMTFileNameTextField.addPropertyChangeListener("value", e -> {
			// if the text is red set it back to black as soon as the user starts typing
			signatureDiscoveryGMTFileNameTextField.setForeground(textFieldForeground);
		});

		selectSigGMTFileButton.setText("...");
		selectSigGMTFileButton.setMargin(new Insets(0, 0, 0, 0));
		selectSigGMTFileButton.setActionCommand("Signature Discovery");
		selectSigGMTFileButton.addActionListener(e -> {
			parentPanel.chooseGMTFile(signatureDiscoveryGMTFileNameTextField);
		});

		JPanel SigGMTPanel = new JPanel();
		SigGMTPanel.setLayout(new BorderLayout());

		SigGMTPanel.add(SigGMTLabel, BorderLayout.WEST);
		SigGMTPanel.add(signatureDiscoveryGMTFileNameTextField, BorderLayout.CENTER);
		SigGMTPanel.add(selectSigGMTFileButton, BorderLayout.EAST);

		panel.add(SigGMTPanel);

		CollapsiblePanel filterPanel = createFilterPanel();
		panel.add(filterPanel);

		//TODO: Maybe move loading SigGMT to File-selection Event add load button
		JButton loadButton = new JButton();
		loadButton.setText("Load Gene-Sets");
		loadButton.addActionListener(e -> {
			String filePath = (String) signatureDiscoveryGMTFileNameTextField.getValue();

			if(filePath == null || PostAnalysisInputPanel.checkFile(filePath).equals(Color.RED)) {
				String message = "SigGMT file name not valid.\n";
				signatureDiscoveryGMTFileNameTextField.setForeground(Color.RED);
				JOptionPane.showMessageDialog(application.getJFrame(), message, "Post Analysis Known Signature", JOptionPane.WARNING_MESSAGE);
				return;
			}

			FilterMetric filterMetric = createFilterMetric();
			LoadSignatureSetsActionListener action = new LoadSignatureSetsActionListener(filePath, filterMetric, application, cyApplicationManager, dialog, streamUtil);
			
			action.setGeneSetCallback(gs -> {
				this.signatureGenesets = gs;
			});
			
			action.setLoadedSignatureSetsCallback(selected -> {
				avail_sig_sets.clear();
				selected_sig_sets.clear();
				
				for(String name : selected) {
					avail_sig_sets.addElement(name);
				}
			});
			
			action.actionPerformed(null);
			
		});

		loadButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(loadButton);

		collapsiblePanel.getContentPane().add(panel, BorderLayout.NORTH);
		return collapsiblePanel;
	}
	
	
	private FilterMetric createFilterMetric() {
		Number number = (Number) filterTextField.getValue();
		double value = number.doubleValue();
		FilterType type = getFilterType();
		
		switch(type) {
			case NUMBER:
				return new FilterMetric.Number(value);
			case PERCENT:
				return new FilterMetric.Percent(value);
			case SPECIFIC:
				return new FilterMetric.Specific(value);
			case HYPERGEOM:
				return new FilterMetric.Hypergeom(value, hypergomUniverseSize);
			case MANN_WHIT_TWO_SIDED:
			case MANN_WHIT_GREATER:
			case MANN_WHIT_LESS:
				return new FilterMetric.MannWhit(value, mannWhitRanks, type);
			default:
				throw new RuntimeException("Unsupported FilterType: " + type);
		}
	}
	

	/**
	 * Create a sub-panel so the user can specify filters so when loading in
	 * Signature gene set files they can limit the genesets loaded in based on
	 * the how many genes overlap with the current EM analyzing.
	 *
	 * @return CollapsiblePanel to set Filter on Postanalysis genesets
	 */
	private CollapsiblePanel createFilterPanel() {
		CollapsiblePanel collapsiblePanel = new CollapsiblePanel("Filter");
		collapsiblePanel.setCollapsed(false);

		filterTextField = new JFormattedTextField();
		filterTextField.setColumns(4);
		filterTextField.addPropertyChangeListener("value", e -> {
			StringBuilder message = new StringBuilder("The value you have entered is invalid.\n");
			Number number = (Number) filterTextField.getValue();
			FilterType filterType = getFilterType();
			
			Optional<Double> value = PostAnalysisInputPanel.validateAndGetFilterValue(number, filterType, message);
			savedFilterValues.put(filterType, value.orElse(filterType.defaultValue));
			if(!value.isPresent()) {
				filterTextField.setValue(filterType.defaultValue);
				JOptionPane.showMessageDialog(application.getJFrame(), message.toString(), "Parameter out of bounds", JOptionPane.WARNING_MESSAGE);
			}
		});

		//Two types of filters:
		// 1. filter by percent, i.e. the overlap between the signature geneset and EM geneset
		// has to be X percentage of the EM set it overlaps with for at least one geneset in the enrichment map
		// 2. filter by number, i.e. the overlap between the signature geneset and EM geneset
		// has to be X genes of the EM set it overlaps with for at least one geneset in the enrichment map
		// 3. filter by specificity, i.e looking for the signature genesets that are more specific than other genesets
		// for instance a drug A that targets only X and Y as opposed to drug B that targets X,y,L,M,N,O,P
		filterTypeCombo = new JComboBox<FilterType>();
		filterTypeCombo.addItem(FilterType.NO_FILTER); // default
		filterTypeCombo.addItem(FilterType.MANN_WHIT_TWO_SIDED);
		filterTypeCombo.addItem(FilterType.MANN_WHIT_GREATER);
		filterTypeCombo.addItem(FilterType.MANN_WHIT_LESS);
		filterTypeCombo.addItem(FilterType.HYPERGEOM);
		filterTypeCombo.addItem(FilterType.NUMBER);
		filterTypeCombo.addItem(FilterType.PERCENT);
		filterTypeCombo.addItem(FilterType.SPECIFIC);

		filterTypeCombo.addActionListener(e -> {
			FilterType filterType = (FilterType) filterTypeCombo.getSelectedItem();
			filterTextField.setValue(savedFilterValues.get(filterType));
			filterTextField.setEnabled(filterType != FilterType.NO_FILTER);
		});

		JPanel filterTypePanel = new JPanel(new BorderLayout());
		filterTypePanel.add(filterTypeCombo, BorderLayout.CENTER);
		filterTypePanel.add(filterTextField, BorderLayout.EAST);

		collapsiblePanel.getContentPane().add(filterTypePanel);
		return collapsiblePanel;
	}
	
	private FilterType getFilterType() {
		return (FilterType) filterTypeCombo.getSelectedItem();
	}

	void resetPanel() {
		// Reset the text field
		signatureDiscoveryGMTFileNameTextField.setText("");
		signatureDiscoveryGMTFileNameTextField.setValue("");
		signatureDiscoveryGMTFileNameTextField.setToolTipText(null);

		// Reset the List fields:
		avail_sig_sets.clear();
		avail_sig_sets_field.clearSelection();
		setAvSigCount(0);

		selected_sig_sets.clear();
		selected_sig_sets_field.clearSelection();
		setSelSigCount(0);

		// Reset the filter field
		filterTypeCombo.setSelectedItem(FilterType.NO_FILTER);
		weightPanel.resetPanel();
	}

	void initialize(EnrichmentMap currentMap) {
		weightPanel.initialize(currentMap);
		
		hypergomUniverseSize = currentMap.getNumberOfGenes();
		
		Map<String, DataSet> data_sets = currentMap.getDatasets();
		DataSet dataset = data_sets.get(weightPanel.getDataSet());
		mannWhitRanks = new Ranking();
		if(dataset != null) {
			mannWhitRanks = dataset.getExpressionSets().getRanks().get(weightPanel.getRankFile());
		}
	}

	void build(PostAnalysisParameters.Builder builder) {
		weightPanel.build(builder);
		
		for(int i = 0; i < selected_sig_sets.size(); i++) {
			builder.addSelectedSignatureSetName(selected_sig_sets.getElementAt(i));
		}
		
		Number number = (Number) filterTextField.getValue();
		FilterParameters filterParameters = new FilterParameters(getFilterType(), number.doubleValue());
		builder.setFilterParameters(filterParameters);
		
		String filePath = (String) signatureDiscoveryGMTFileNameTextField.getValue();
		builder.setSignatureGMTFileName(filePath);
		
		builder.setSignatureGenesets(signatureGenesets);
	}
	
	
	/**
	 * @return Array with arrows UP, DOWN, LEFT and RIGHT
	 */
	private ImageIcon[] createArrowIcons() {
		ImageIcon[] iconArrow = new ImageIcon[4];
		URL iconURL;
		
		Class<?> cls = this.getClass();
		iconURL = cls.getResource("arrow_up.gif");
		if(iconURL != null) {
			iconArrow[UP] = new ImageIcon(iconURL);
		}
		iconURL = cls.getResource("arrow_down.gif");
		if(iconURL != null) {
			iconArrow[DOWN] = new ImageIcon(iconURL);
		}
		iconURL = cls.getResource("arrow_left.gif");
		if(iconURL != null) {
			iconArrow[LEFT] = new ImageIcon(iconURL);
		}
		iconURL = cls.getResource("arrow_right.gif");
		if(iconURL != null) {
			iconArrow[RIGHT] = new ImageIcon(iconURL);
		}
		return iconArrow;
	}

	/**
	 * Set available signature gene set count to specified value
	 * 
	 * @param int avSigCount
	 * @return null
	 */
	private void setAvSigCount(int avSigCount) {
		//		this.avail_sig_sets_count = avSigCount;
		this.avail_sig_sets_counter_label.setText("(" + Integer.toString(avSigCount) + ")");
	}

	/**
	 * Set selected signature gene set count to the specified value
	 * 
	 * @param int sigCount
	 * @return null
	 */
	private void setSelSigCount(int num) {
		//		this.sel_sig_sets_count = num;
		this.selected_sig_sets_counter_label.setText("(" + Integer.toString(num) + ")");
	}
	
	
	
}
