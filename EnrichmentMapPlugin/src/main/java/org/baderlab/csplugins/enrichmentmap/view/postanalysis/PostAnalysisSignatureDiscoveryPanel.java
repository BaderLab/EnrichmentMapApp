package org.baderlab.csplugins.enrichmentmap.view.postanalysis;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil.makeSmall;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.baderlab.csplugins.enrichmentmap.AfterInjection;
import org.baderlab.csplugins.enrichmentmap.actions.LoadSignatureSetsActionListener;
import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.PostAnalysisFilterParameters;
import org.baderlab.csplugins.enrichmentmap.model.PostAnalysisFilterType;
import org.baderlab.csplugins.enrichmentmap.model.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.model.Ranking;
import org.baderlab.csplugins.enrichmentmap.model.SetOfGeneSets;
import org.baderlab.csplugins.enrichmentmap.task.FilterMetric;
import org.baderlab.csplugins.enrichmentmap.view.EnrichmentMapInputPanel;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

@SuppressWarnings("serial")
public class PostAnalysisSignatureDiscoveryPanel extends JPanel implements ListSelectionListener {

	private static final String AVAILABLE_FORMAT = "Available (%d)";
	private static final String SELECTED_FORMAT = "Selected (%d)";

	@Inject private LoadSignatureSetsActionListener.Factory loadSignatureSetsActionListenerFactory;
	@Inject private CyServiceRegistrar serviceRegistrar;
	@Inject private CySwingApplication application;
	@Inject private IconManager iconManager;

	private final PostAnalysisInputPanel parentPanel;

	private JFormattedTextField signatureDiscoveryGMTFileNameTextField;

	private PostAnalysisWeightPanel weightPanel;

	private JLabel availableLabel;
	private JLabel selectedLabel;
	private JList<String> availSigSetsField;
	private JList<String> selectedSigSetsField;
	private JButton addSelectedButton;
	private JButton removeSelectedButton;

	private DefaultListModel<String> availSigSetsModel = new DefaultListModel<>();
	private DefaultListModel<String> selectedSigSetsModel = new DefaultListModel<>();

	private JFormattedTextField filterTextField;
	private JComboBox<PostAnalysisFilterType> filterTypeCombo;

	// used for filtering
	private int hypergomUniverseSize;
	private Ranking mannWhitRanks;
    
	private SetOfGeneSets signatureGenesets;
	private final Map<PostAnalysisFilterType, Double> savedFilterValues = PostAnalysisFilterType.createMapOfDefaults();
	
	public interface Factory {
		PostAnalysisSignatureDiscoveryPanel create(PostAnalysisInputPanel parentPanel);
	}
	
	@Inject
	public PostAnalysisSignatureDiscoveryPanel(@Assisted PostAnalysisInputPanel parentPanel) {
		this.parentPanel = parentPanel;
	}
	
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting())
			return;
		
		// Enable/disable buttons when list selection changes
		if (addSelectedButton != null)
			addSelectedButton.setEnabled(availSigSetsField.getSelectedIndices().length > 0);
		if (removeSelectedButton != null)
			removeSelectedButton.setEnabled(selectedSigSetsField.getSelectedIndices().length > 0);
	}

	public void update() {
		setAvSigCount(availSigSetsModel.size());
		setSelSigCount(selectedSigSetsModel.size());
	}
	
	@AfterInjection
	private void createContents() {
		JPanel gmtPanel = createSignatureDiscoveryGMTPanel();
		JPanel sigGenesetsPanel = createSignatureGenesetsPanel();
		weightPanel = new PostAnalysisWeightPanel(serviceRegistrar);

		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(false);

   		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
   				.addComponent(gmtPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
   				.addComponent(sigGenesetsPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
   				.addComponent(weightPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
   		);
   		layout.setVerticalGroup(layout.createSequentialGroup()
   				.addComponent(gmtPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   				.addComponent(sigGenesetsPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   				.addComponent(weightPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   		);
    }

	private JPanel createSignatureGenesetsPanel() {
		// List of all Signature Genesets
		availableLabel = new JLabel();
		setAvSigCount(0);

		availSigSetsField = new JList<>(availSigSetsModel);

		JScrollPane availSigSetsScroll = new JScrollPane(availSigSetsField,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		// List of selected Signature Genesets
		selectedLabel = new JLabel();
		setSelSigCount(0);

		selectedSigSetsField = new JList<>(selectedSigSetsModel);

		JScrollPane selectedSigSetsScroll = new JScrollPane(selectedSigSetsField,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		Dimension d = new Dimension(360, 86);
		availSigSetsScroll.setPreferredSize(d);
		selectedSigSetsScroll.setPreferredSize(d);

		// Selection Buttons
		Font selectBtnFont = iconManager.getIconFont(14.0f);

		addSelectedButton = new JButton(IconManager.ICON_ANGLE_DOWN);
		addSelectedButton.setToolTipText("Add Selected");
		addSelectedButton.setFont(selectBtnFont);
		addSelectedButton.setEnabled(false);

		removeSelectedButton = new JButton(IconManager.ICON_ANGLE_UP);
		removeSelectedButton.setToolTipText("Remove Selected");
		removeSelectedButton.setFont(selectBtnFont);
		removeSelectedButton.setEnabled(false);

		addSelectedButton.addActionListener(e -> {
			int[] selected = availSigSetsField.getSelectedIndices();
			for (int i = selected.length; i > 0; i--) {
				selectedSigSetsModel.addElement(availSigSetsModel.get(selected[i - 1]));
				availSigSetsModel.remove(selected[i - 1]);
			}
			setSelSigCount(selectedSigSetsModel.size());
			setAvSigCount(availSigSetsModel.size());
		});        
		removeSelectedButton.addActionListener(e -> {
			int[] selected = selectedSigSetsField.getSelectedIndices();
			for (int i = selected.length; i > 0; i--) {
				availSigSetsModel.addElement(selectedSigSetsModel.get(selected[i - 1]));
				selectedSigSetsModel.remove(selected[i - 1]);
			}

			// Sort the Genesets:
			List<String> setNamesArray = Collections.list(availSigSetsModel.elements());
			Collections.sort(setNamesArray);
			availSigSetsModel.removeAllElements();
			for (String name : setNamesArray) {
				availSigSetsModel.addElement(name);
			}
			setAvSigCount(availSigSetsModel.size());
			setSelSigCount(selectedSigSetsModel.size());
		});
		
		if (LookAndFeelUtil.isAquaLAF()) {
			addSelectedButton.putClientProperty("JButton.buttonType", "gradient");
			removeSelectedButton.putClientProperty("JButton.buttonType", "gradient");
		}

		availSigSetsField.addListSelectionListener(this);
		selectedSigSetsField.addListSelectionListener(this);

		JButton clearButton = new JButton("Clear Signature Genesets");

		clearButton.addActionListener(e -> {
			availSigSetsModel.clear();
			availSigSetsField.clearSelection();
			setAvSigCount(0);

			selectedSigSetsModel.clear();
			selectedSigSetsField.clearSelection();
			setSelSigCount(0);
		});
		
		makeSmall(availableLabel, availSigSetsField, availSigSetsScroll);
		makeSmall(selectedLabel, selectedSigSetsField, selectedSigSetsScroll);
		makeSmall(addSelectedButton, removeSelectedButton, clearButton);
		
		JPanel panel = new JPanel();
        panel.setBorder(LookAndFeelUtil.createTitledBorder("Signature Genesets"));
        
        final GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
				.addComponent(availableLabel)
				.addComponent(availSigSetsScroll, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(layout.createSequentialGroup()
						.addGap(0, 0, Short.MAX_VALUE)
						.addComponent(addSelectedButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(removeSelectedButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addGap(0, 0, Short.MAX_VALUE)
				)
				.addComponent(selectedSigSetsScroll, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(selectedLabel)
				.addGroup(layout.createSequentialGroup()
						.addGap(0, 0, Short.MAX_VALUE)
						.addComponent(clearButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addGap(0, 0, Short.MAX_VALUE)
				)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(availableLabel)
				.addComponent(availSigSetsScroll, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(addSelectedButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(removeSelectedButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addComponent(selectedSigSetsScroll, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(selectedLabel)
				.addComponent(clearButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		
		if (LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);
        
        return panel;
	}
 
    /**
     * @return Panel for choosing and loading GMT and SignatureGMT Geneset-Files 
     */
    private JPanel createSignatureDiscoveryGMTPanel() {
    	signatureDiscoveryGMTFileNameTextField = new JFormattedTextField();
    	signatureDiscoveryGMTFileNameTextField.setColumns(15);
    	signatureDiscoveryGMTFileNameTextField.setToolTipText(EnrichmentMapInputPanel.gmt_instruction);
        
		final Color textFieldForeground = signatureDiscoveryGMTFileNameTextField.getForeground();
		signatureDiscoveryGMTFileNameTextField.addPropertyChangeListener("value", (PropertyChangeEvent e) -> {
			// if the text is red set it back to black as soon as the user starts typing
			signatureDiscoveryGMTFileNameTextField.setForeground(textFieldForeground);
		});

		JButton selectSigGMTFileButton = new JButton("Browse...");
		selectSigGMTFileButton.setToolTipText(EnrichmentMapInputPanel.gmt_instruction);
		selectSigGMTFileButton.setActionCommand("Signature Discovery");
		selectSigGMTFileButton.addActionListener((ActionEvent evt) -> {
			parentPanel.chooseGMTFile(signatureDiscoveryGMTFileNameTextField);
		});

		JLabel filterLabel = new JLabel("Filter:");
		
		filterTextField = new JFormattedTextField();
		filterTextField.setColumns(4);
		filterTextField.setHorizontalAlignment(JTextField.RIGHT);
		filterTextField.addPropertyChangeListener("value", (PropertyChangeEvent e) -> {
			StringBuilder message = new StringBuilder("The value you have entered is invalid.\n");
			Number number = (Number) filterTextField.getValue();
			PostAnalysisFilterType filterType = getFilterType();

			Optional<Double> value = PostAnalysisInputPanel.validateAndGetFilterValue(number, filterType, message);
			savedFilterValues.put(filterType, value.orElse(filterType.defaultValue));
			
			if (!value.isPresent()) {
				filterTextField.setValue(filterType.defaultValue);
				JOptionPane.showMessageDialog(application.getJFrame(), message.toString(), "Parameter out of bounds",
						JOptionPane.WARNING_MESSAGE);
			}
		});

		// Types of filters:
		// 1. filter by percent, i.e. the overlap between the signature geneset and EM geneset
		//    has to be X percentage of the EM set it overlaps with for at least one geneset in the enrichment map.
		// 2. filter by number, i.e. the overlap between the signature geneset and EM geneset
		//    has to be X genes of the EM set it overlaps with for at least one geneset in the enrichment map.
		// 3. filter by specificity, i.e looking for the signature genesets that are more specific than other genesets
		//    for instance a drug A that targets only X and Y as opposed to drug B that targets X,y,L,M,N,O,P.
		filterTypeCombo = new JComboBox<>();
		filterTypeCombo.addItem(PostAnalysisFilterType.NO_FILTER); // default
		filterTypeCombo.addItem(PostAnalysisFilterType.MANN_WHIT_TWO_SIDED);
		filterTypeCombo.addItem(PostAnalysisFilterType.MANN_WHIT_GREATER);
		filterTypeCombo.addItem(PostAnalysisFilterType.MANN_WHIT_LESS);
		filterTypeCombo.addItem(PostAnalysisFilterType.HYPERGEOM);
		filterTypeCombo.addItem(PostAnalysisFilterType.NUMBER);
		filterTypeCombo.addItem(PostAnalysisFilterType.PERCENT);
		filterTypeCombo.addItem(PostAnalysisFilterType.SPECIFIC);

		filterTypeCombo.addActionListener(e -> {
			updateFilterTextField();
		});
		
		updateFilterTextField();
		
        //TODO: Maybe move loading SigGMT to File-selection Event add load button
		JButton loadButton = new JButton();
		loadButton.setText("Load Genesets");
		loadButton.addActionListener(e -> {
			String filePath = (String) signatureDiscoveryGMTFileNameTextField.getValue();

			if (filePath == null || PostAnalysisInputPanel.checkFile(filePath).equals(Color.RED)) {
				String message = "SigGMT file name not valid.\n";
				signatureDiscoveryGMTFileNameTextField.setForeground(Color.RED);
				JOptionPane.showMessageDialog(application.getJFrame(), message, "Post Analysis Known Signature",
						JOptionPane.WARNING_MESSAGE);
				return;
			}

			FilterMetric filterMetric = createFilterMetric();
			LoadSignatureSetsActionListener action = loadSignatureSetsActionListenerFactory.create(filePath,
					filterMetric);

			action.setGeneSetCallback(gs -> {
				this.signatureGenesets = gs;
			});
			
			action.setLoadedSignatureSetsCallback(selected -> {
				availSigSetsModel.clear();
				selectedSigSetsModel.clear();

				for (String name : selected)
					availSigSetsModel.addElement(name);
				
				update();
			});
			
			action.actionPerformed(null);
		});
		
		makeSmall(signatureDiscoveryGMTFileNameTextField, selectSigGMTFileButton);
		makeSmall(filterLabel, filterTypeCombo, filterTextField);
		makeSmall(loadButton);
		
		JPanel panel = new JPanel();
		panel.setBorder(LookAndFeelUtil.createTitledBorder("SigGMT File (contains signature-genesets)"));
		final GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
   		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
   				.addGroup(layout.createSequentialGroup()
   						.addComponent(signatureDiscoveryGMTFileNameTextField, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
   						.addComponent(selectSigGMTFileButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   				)
   				.addGroup(layout.createSequentialGroup()
   						.addComponent(filterLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   						.addComponent(filterTypeCombo, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
   						.addComponent(filterTextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   				)
   				.addComponent(loadButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   		);
   		layout.setVerticalGroup(layout.createSequentialGroup()
   				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
		   				.addComponent(signatureDiscoveryGMTFileNameTextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		   				.addComponent(selectSigGMTFileButton)
		   		)
   				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
   						.addComponent(filterLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   						.addComponent(filterTypeCombo, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   						.addComponent(filterTextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   				)
   				.addComponent(loadButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   		);
   		
		if (LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);

		return panel;
	}

	private void updateFilterTextField() {
		PostAnalysisFilterType filterType = (PostAnalysisFilterType) filterTypeCombo.getSelectedItem();
		filterTextField.setValue(savedFilterValues.get(filterType));
		filterTextField.setEnabled(filterType != PostAnalysisFilterType.NO_FILTER);
	}
    
	void reset() {
		// Reset the text field
		signatureDiscoveryGMTFileNameTextField.setText("");
		signatureDiscoveryGMTFileNameTextField.setValue("");
		signatureDiscoveryGMTFileNameTextField.setToolTipText(null);

		// Reset the List fields:
		availSigSetsModel.clear();
		availSigSetsField.clearSelection();
		setAvSigCount(0);

		selectedSigSetsModel.clear();
		selectedSigSetsField.clearSelection();
		setSelSigCount(0);

		// Reset the filter field
		filterTypeCombo.setSelectedItem(PostAnalysisFilterType.NO_FILTER);
		weightPanel.reset();
	}
    
	private FilterMetric createFilterMetric() {
		Number number = (Number) filterTextField.getValue();
		double value = number.doubleValue();
		PostAnalysisFilterType type = getFilterType();
		
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
				return new FilterMetric.None();
		}
	}
	
	private PostAnalysisFilterType getFilterType() {
		return (PostAnalysisFilterType) filterTypeCombo.getSelectedItem();
	}
	
	void initialize(EnrichmentMap currentMap) {
		weightPanel.initialize(currentMap);
		hypergomUniverseSize = currentMap.getNumberOfGenes();

		Map<String, DataSet> dataSets = currentMap.getDatasets();
		DataSet ds = dataSets.get(weightPanel.getDataSet());
		mannWhitRanks = new Ranking();
		
		if (ds != null)
			mannWhitRanks = ds.getExpressionSets().getRanks().get(weightPanel.getRankFile());
	}
    
	void build(PostAnalysisParameters.Builder builder) {
		weightPanel.build(builder);
		
		for (int i = 0; i < selectedSigSetsModel.size(); i++) {
			builder.addSelectedSignatureSetName(selectedSigSetsModel.getElementAt(i));
		}
		
		Number number = (Number) filterTextField.getValue();
		PostAnalysisFilterParameters filterParameters = new PostAnalysisFilterParameters(getFilterType(), number.doubleValue());
		builder.setFilterParameters(filterParameters);
		
		String filePath = (String) signatureDiscoveryGMTFileNameTextField.getValue();
		builder.setSignatureGMTFileName(filePath);
		builder.setSignatureGenesets(signatureGenesets);
	}
	
    /**
	 * Set available signature gene set count to specified value
	 */
	public void setAvSigCount(int count) {
		availableLabel.setText(String.format(AVAILABLE_FORMAT, count));
	}
	
	/**
	 * Set selected signature gene set count to the specified value
	 */
	public void setSelSigCount(int count) {
		selectedLabel.setText(String.format(SELECTED_FORMAT, count));
	}
}
