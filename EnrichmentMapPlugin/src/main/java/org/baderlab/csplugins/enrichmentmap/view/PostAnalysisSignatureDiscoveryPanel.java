package org.baderlab.csplugins.enrichmentmap.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.baderlab.csplugins.enrichmentmap.util.SwingUtil.makeSmall;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.Collections;
import java.util.List;

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

import org.baderlab.csplugins.enrichmentmap.FilterParameters;
import org.baderlab.csplugins.enrichmentmap.FilterParameters.FilterType;
import org.baderlab.csplugins.enrichmentmap.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.actions.LoadSignatureSetsActionListener;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.SetOfGeneSets;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.work.swing.DialogTaskManager;

@SuppressWarnings("serial")
public class PostAnalysisSignatureDiscoveryPanel extends JPanel implements ListSelectionListener {

	private static final String AVAILABLE_FORMAT = "Available (%d)";
	private static final String SELECTED_FORMAT = "Selected (%d)";
	
	private final PostAnalysisInputPanel parentPanel;
	private final StreamUtil streamUtil;
	
	private PostAnalysisParameters paParams;
    
    private JFormattedTextField signatureDiscoveryGMTFileNameTextField;
		
    private PostAnalysisWeightPanel weightPanel;
    
	private JLabel availableLabel;
	private JLabel selectedLabel;
	private JList<String> availSigSetsField;
    private JList<String> selectedSigSetsField;
    private JButton addSelectedButton;
    private JButton removeSelectedButton;
    
    private DefaultListModel<String> availSigSetsModel;
    private DefaultListModel<String> selectedSigSetsModel;
    
    private JFormattedTextField filterTextField;
    private JComboBox<FilterType> filterTypeCombo;
    
    private final CyServiceRegistrar serviceRegistrar;
    
	public PostAnalysisSignatureDiscoveryPanel(
			PostAnalysisInputPanel parentPanel,
			StreamUtil streamUtil,
			CyServiceRegistrar serviceRegistrar
	) {
		this.parentPanel = parentPanel;
		this.streamUtil = streamUtil;
		this.serviceRegistrar = serviceRegistrar;
		
		createSignatureDiscoveryOptionsPanel();
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
		availSigSetsField.updateUI();
		selectedSigSetsField.updateUI();
		setAvSigCount(availSigSetsModel.size());
		setSelSigCount(selectedSigSetsModel.size());
	}
	
    private void createSignatureDiscoveryOptionsPanel() {
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
   		
   		if (LookAndFeelUtil.isAquaLAF())
			setOpaque(false);
    }

	private JPanel createSignatureGenesetsPanel() {
		IconManager iconManager = serviceRegistrar.getService(IconManager.class);
		
//        //TODO: Make SearchBox functional
//        // search Box:
//        JFormattedTextField searchBox = new JFormattedTextField();
//        searchBox.setName("Search");
//        signaturePanel.add(searchBox);
        
//        avail_sig_sets = paParams.getSignatureSetNames(); 
//        selected_sig_sets = paParams.getSelectedSignatureSetNames();
        
		// List of all Signature Genesets
		availableLabel = new JLabel();
		setAvSigCount(0);
		
        availSigSetsField = new JList<>();
        
		JScrollPane availSigSetsScroll = new JScrollPane(availSigSetsField,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		// List of selected Signature Genesets
		selectedLabel = new JLabel();
		setSelSigCount(0);
        
        selectedSigSetsField = new JList<>();

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

		addSelectedButton.addActionListener((ActionEvent evt) -> {
			int[] selected = availSigSetsField.getSelectedIndices();
			for (int i = selected.length; i > 0; i--) {
				selectedSigSetsModel.addElement(availSigSetsModel.get(selected[i - 1]));
				availSigSetsModel.remove(selected[i - 1]);
			}
			setSelSigCount(selectedSigSetsModel.size());
			setAvSigCount(availSigSetsModel.size());
		});        
		removeSelectedButton.addActionListener((ActionEvent evt) -> {
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
        
		clearButton.addActionListener((ActionEvent e) -> {
			paParams.setSignatureGenesets(new SetOfGeneSets());
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
    	signatureDiscoveryGMTFileNameTextField.setToolTipText(EnrichmentMapInputPanel.gmtTip);
        
		final Color textFieldForeground = signatureDiscoveryGMTFileNameTextField.getForeground();
		signatureDiscoveryGMTFileNameTextField.addPropertyChangeListener("value", (PropertyChangeEvent e) -> {
			// if the text is red set it back to black as soon as the user starts typing
			signatureDiscoveryGMTFileNameTextField.setForeground(textFieldForeground);
		});

		JButton selectSigGMTFileButton = new JButton("Browse...");
		selectSigGMTFileButton.setToolTipText(EnrichmentMapInputPanel.gmtTip);
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
			boolean valid = PostAnalysisInputPanel.validateAndSetFilterValue(filterTextField,
					paParams.getFilterParameters(), message);
			CySwingApplication application = serviceRegistrar.getService(CySwingApplication.class);

			if (!valid)
				JOptionPane.showMessageDialog(application.getJFrame(), message.toString(), "Parameter out of bounds",
						JOptionPane.WARNING_MESSAGE);
		});

		// Types of filters:
		// 1. filter by percent, i.e. the overlap between the signature geneset and EM geneset
		//    has to be X percentage of the EM set it overlaps with for at least one geneset in the enrichment map.
		// 2. filter by number, i.e. the overlap between the signature geneset and EM geneset
		//    has to be X genes of the EM set it overlaps with for at least one geneset in the enrichment map.
		// 3. filter by specificity, i.e looking for the signature genesets that are more specific than other genesets
		//    for instance a drug A that targets only X and Y as opposed to drug B that targets X,y,L,M,N,O,P.
		filterTypeCombo = new JComboBox<>();
		filterTypeCombo.addItem(FilterType.NO_FILTER); // default
		filterTypeCombo.addItem(FilterType.MANN_WHIT_TWO_SIDED);
		filterTypeCombo.addItem(FilterType.MANN_WHIT_GREATER);
		filterTypeCombo.addItem(FilterType.MANN_WHIT_LESS);
		filterTypeCombo.addItem(FilterType.HYPERGEOM);
		filterTypeCombo.addItem(FilterType.NUMBER);
		filterTypeCombo.addItem(FilterType.PERCENT);
		filterTypeCombo.addItem(FilterType.SPECIFIC);

		filterTypeCombo.addActionListener((ActionEvent e) -> {
			FilterType filterType = (FilterType) filterTypeCombo.getSelectedItem();
			FilterParameters filterParams = paParams.getFilterParameters();
			filterParams.setType(filterType);
			filterTextField.setValue(filterParams.getValue(filterType));
			filterTextField.setEnabled(filterType != FilterType.NO_FILTER);
		});
		
        //TODO: Maybe move loading SigGMT to File-selection Event add load button
		JButton loadButton = new JButton();
		loadButton.setText("Load Genesets");
		loadButton.addActionListener((ActionEvent e) -> {
			String filePath = (String) signatureDiscoveryGMTFileNameTextField.getValue();

			if (filePath == null || PostAnalysisInputPanel.checkFile(filePath).equals(Color.RED)) {
				String message = "SigGMT file name not valid.\n";
				signatureDiscoveryGMTFileNameTextField.setForeground(Color.RED);
				CySwingApplication application = serviceRegistrar.getService(CySwingApplication.class);
				JOptionPane.showMessageDialog(application.getJFrame(), message, "Post Analysis Known Signature",
						JOptionPane.WARNING_MESSAGE);
				return;
			}

			paParams.setSignatureGMTFileName(filePath);
			LoadSignatureSetsActionListener action = new LoadSignatureSetsActionListener(parentPanel,
					serviceRegistrar.getService(DialogTaskManager.class), streamUtil, serviceRegistrar);
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
    
    void resetPanel() {
    	paParams.setSignatureGenesets(new SetOfGeneSets());
    	
    	// Reset the text field
        signatureDiscoveryGMTFileNameTextField.setText("");
        signatureDiscoveryGMTFileNameTextField.setValue("");
        signatureDiscoveryGMTFileNameTextField.setToolTipText(null);
                    	       
        // Reset the List fields:
        paParams.getSignatureSetNames().clear();
        availSigSetsModel.clear();
        availSigSetsField.clearSelection();
        setAvSigCount(0);
        
        paParams.getSelectedSignatureSetNames().clear();
        selectedSigSetsModel.clear();
        selectedSigSetsField.clearSelection();
        setSelSigCount(0);

        // Reset the filter field
        paParams.getFilterParameters().setType(FilterType.NO_FILTER);
        filterTypeCombo.setSelectedItem(FilterType.NO_FILTER);
        weightPanel.resetPanel();
    }
    
    void initialize(EnrichmentMap currentMap, PostAnalysisParameters paParams) {
		this.paParams = paParams;
        
		weightPanel.initialize(currentMap, paParams);
		
		FilterParameters filterParams = paParams.getFilterParameters();
        filterTypeCombo.setSelectedItem(filterParams.getType());
        filterTextField.setValue(filterParams.getValue(filterParams.getType()));
        
        availSigSetsModel = paParams.getSignatureSetNames(); 
        selectedSigSetsModel = paParams.getSelectedSignatureSetNames();
        
        availSigSetsField.setModel(availSigSetsModel);
        selectedSigSetsField.setModel(selectedSigSetsModel);
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
