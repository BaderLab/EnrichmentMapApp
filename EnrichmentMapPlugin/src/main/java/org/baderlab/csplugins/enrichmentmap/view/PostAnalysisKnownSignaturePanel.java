package org.baderlab.csplugins.enrichmentmap.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolTip;

import org.baderlab.csplugins.enrichmentmap.AfterInjection;
import org.baderlab.csplugins.enrichmentmap.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.actions.LoadSignatureSetsActionListener;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.JMultiLineToolTip;
import org.baderlab.csplugins.enrichmentmap.model.SetOfGeneSets;
import org.baderlab.csplugins.enrichmentmap.task.FilterMetric;
import org.cytoscape.application.swing.CySwingApplication;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

@SuppressWarnings("serial")
public class PostAnalysisKnownSignaturePanel extends JPanel {

	private final PostAnalysisInputPanel parentPanel;

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
	private void createKnownSignatureOptionsPanel() {
		setLayout(new BorderLayout());

		//Gene set file panel
		CollapsiblePanel gmtPanel = createKnownSignatureGMTPanel();
		gmtPanel.setCollapsed(false);

		//Parameters collapsible panel
		weightPanel = new PostAnalysisWeightPanel(application);
		weightPanel.setCollapsed(false);

		add(gmtPanel, BorderLayout.NORTH);
		add(weightPanel, BorderLayout.CENTER);
	}

	/**
	 * @return CollapsiblePanel for choosing and loading GMT and SignatureGMT
	 *         Geneset-Files
	 */
	private CollapsiblePanel createKnownSignatureGMTPanel() {
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
		knownSignatureGMTFileNameTextField = new JFormattedTextField();
		knownSignatureGMTFileNameTextField.setColumns(15);
		final Color textFieldForeground = knownSignatureGMTFileNameTextField.getForeground();

		knownSignatureGMTFileNameTextField.setFont(new Font("Dialog", 1, 10));
		knownSignatureGMTFileNameTextField.addPropertyChangeListener("value", new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				// if the text is red set it back to black as soon as the user starts typing
				knownSignatureGMTFileNameTextField.setForeground(textFieldForeground);
			}
		});

		selectSigGMTFileButton.setText("...");
		selectSigGMTFileButton.setMargin(new Insets(0, 0, 0, 0));
		selectSigGMTFileButton.setActionCommand("Known Signature");
		selectSigGMTFileButton.addActionListener(e -> {
			parentPanel.chooseGMTFile(knownSignatureGMTFileNameTextField);
		});

		JPanel SigGMTPanel = new JPanel();
		SigGMTPanel.setLayout(new BorderLayout());

		SigGMTPanel.add(SigGMTLabel, BorderLayout.WEST);
		SigGMTPanel.add(knownSignatureGMTFileNameTextField, BorderLayout.CENTER);
		SigGMTPanel.add(selectSigGMTFileButton, BorderLayout.EAST);
		//add the components to the panel
		panel.add(SigGMTPanel);

		collapsiblePanel.getContentPane().add(panel, BorderLayout.NORTH);
		return collapsiblePanel;

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
		
		System.out.println(this.signatureGenesets);
		System.out.println(this.selectedGenesetNames);
		
		return true;
	}

	void resetPanel() {
		//Gene-Sets Panel
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
