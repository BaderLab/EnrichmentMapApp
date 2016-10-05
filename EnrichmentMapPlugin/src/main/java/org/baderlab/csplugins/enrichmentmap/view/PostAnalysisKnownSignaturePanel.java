package org.baderlab.csplugins.enrichmentmap.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.baderlab.csplugins.enrichmentmap.util.SwingUtil.makeSmall;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.baderlab.csplugins.enrichmentmap.AfterInjection;
import org.baderlab.csplugins.enrichmentmap.actions.LoadSignatureSetsActionListener;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.model.SetOfGeneSets;
import org.baderlab.csplugins.enrichmentmap.task.FilterMetric;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.LookAndFeelUtil;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

@SuppressWarnings("serial")
public class PostAnalysisKnownSignaturePanel extends JPanel {

	private final PostAnalysisInputPanel parentPanel;

	@Inject private CyServiceRegistrar serviceRegistrar;
	@Inject private CySwingApplication application;
	@Inject private LoadSignatureSetsActionListener.Factory loadSignatureSetsActionListenerFactory;

	private SetOfGeneSets signatureGenesets;
	private Set<String> selectedGenesetNames;
	private PostAnalysisWeightPanel weightPanel;
	private JFormattedTextField knownSignatureGMTFileNameTextField;
	
	public interface Factory {
		PostAnalysisKnownSignaturePanel create(PostAnalysisInputPanel parentPanel);
	}
	
	@Inject
	public PostAnalysisKnownSignaturePanel(@Assisted PostAnalysisInputPanel parentPanel) {
		this.parentPanel = parentPanel;
	}
	
	@AfterInjection
	private void init() {
		// Gene set file panel
		JPanel gmtPanel = createKnownSignatureGMTPanel();

		// Parameters collapsible panel
		weightPanel = new PostAnalysisWeightPanel(serviceRegistrar);

		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(false);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
				.addComponent(gmtPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(weightPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(gmtPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(weightPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);

		if (LookAndFeelUtil.isAquaLAF())
			setOpaque(false);
	}

	/**
	 * @return Panel for choosing and loading GMT and SignatureGMT Geneset-Files
	 */
	private JPanel createKnownSignatureGMTPanel() {
		knownSignatureGMTFileNameTextField = new JFormattedTextField();
		knownSignatureGMTFileNameTextField.setColumns(15);
		knownSignatureGMTFileNameTextField.setToolTipText(EnrichmentMapInputPanel.gmtTip);

		final Color textFieldForeground = knownSignatureGMTFileNameTextField.getForeground();
		knownSignatureGMTFileNameTextField.addPropertyChangeListener("value", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				// if the text is red set it back to black as soon as the user starts typing
				knownSignatureGMTFileNameTextField.setForeground(textFieldForeground);
			}
		});

		JButton selectSigGMTFileButton = new JButton("Browse...");
		selectSigGMTFileButton.setToolTipText(EnrichmentMapInputPanel.gmtTip);
		selectSigGMTFileButton.setActionCommand("Known Signature");
		selectSigGMTFileButton.addActionListener(e -> {
			parentPanel.chooseGMTFile(knownSignatureGMTFileNameTextField);
		});

		makeSmall(knownSignatureGMTFileNameTextField, selectSigGMTFileButton);

		JPanel panel = new JPanel();
		panel.setBorder(LookAndFeelUtil.createTitledBorder("SigGMT File (contains signature-genesets)"));

		final GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
   		
   		layout.setHorizontalGroup(layout.createSequentialGroup()
   				.addComponent(knownSignatureGMTFileNameTextField, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
   				.addComponent(selectSigGMTFileButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   		);
   		layout.setVerticalGroup(layout.createParallelGroup(Alignment.CENTER, false)
   				.addComponent(knownSignatureGMTFileNameTextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   				.addComponent(selectSigGMTFileButton)
   		);
   		
		if (LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);

		return panel;
	}

	public boolean okToRun() {
		String filePath = (String) knownSignatureGMTFileNameTextField.getValue();

		if(filePath == null || PostAnalysisInputPanel.checkFile(filePath).equals(Color.RED)) {
			String message = "SigGMT file name not valid.\n";
			knownSignatureGMTFileNameTextField.setForeground(Color.RED);
			JOptionPane.showMessageDialog(application.getJFrame(), message, "Post Analysis Known Signature", JOptionPane.WARNING_MESSAGE);
			return false;
		}

		// Load in the GMT file
		// Manually fire the same action listener that is used by the signature discovery panel.
		// Use the synchronousTaskManager so that this blocks
		
		FilterMetric filterMetric = new FilterMetric.None();
		LoadSignatureSetsActionListener loadAction = loadSignatureSetsActionListenerFactory.create(filePath, filterMetric);
		
		loadAction.setGeneSetCallback(gs -> {
			this.signatureGenesets = gs;
		});
		
		loadAction.setLoadedSignatureSetsCallback(selected -> {
			this.selectedGenesetNames = selected;
		});

		
		loadAction.actionPerformed(null);
		
		return true;
	}

	void resetPanel() {
		// Gene-Sets Panel
		knownSignatureGMTFileNameTextField.setText("");
		knownSignatureGMTFileNameTextField.setValue("");
		knownSignatureGMTFileNameTextField.setToolTipText(null);

		weightPanel.resetPanel();
	}

	void initialize(EnrichmentMap currentMap) {
		weightPanel.initialize(currentMap);
	}
	
	public void build(PostAnalysisParameters.Builder builder) {
		weightPanel.build(builder);
		
		String filePath = (String) knownSignatureGMTFileNameTextField.getValue();
		builder.setSignatureGMTFileName(filePath);
		builder.setSignatureGenesets(signatureGenesets);
		builder.addSelectedSignatureSetNames(selectedGenesetNames);
	}
}
