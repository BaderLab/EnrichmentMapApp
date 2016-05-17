package org.baderlab.csplugins.enrichmentmap.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolTip;

import org.baderlab.csplugins.enrichmentmap.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.actions.LoadSignatureSetsActionListener;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.JMultiLineToolTip;
import org.baderlab.csplugins.enrichmentmap.model.SetOfGeneSets;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.work.SynchronousTaskManager;

@SuppressWarnings("serial")
public class PostAnalysisKnownSignaturePanel extends JPanel {

	private final PostAnalysisInputPanel parentPanel;

	private final CyApplicationManager cyApplicationManager;
	private final CySwingApplication application;
	private final StreamUtil streamUtil;
	private final SynchronousTaskManager syncTaskManager;

	// 'Known Signature Panel' parameters
	private EnrichmentMap map;
	private PostAnalysisParameters paParams;

	private PostAnalysisWeightPanel weightPanel;

	private JFormattedTextField knownSignatureGMTFileNameTextField;

	public PostAnalysisKnownSignaturePanel(PostAnalysisInputPanel parentPanel,
			CyApplicationManager cyApplicationManager, CySwingApplication application, StreamUtil streamUtil,
			SynchronousTaskManager syncTaskManager) {

		this.parentPanel = parentPanel;
		this.cyApplicationManager = cyApplicationManager;
		this.application = application;
		this.streamUtil = streamUtil;
		this.syncTaskManager = syncTaskManager;

		createKnownSignatureOptionsPanel();
	}

	/**
	 * @return collapsiblePanel to select Signature Genesets for Signature
	 *         Analysis
	 */
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
		selectSigGMTFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				parentPanel.chooseGMTFile(knownSignatureGMTFileNameTextField);
			}
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

	public boolean beforeRun() {
		String filePath = (String) knownSignatureGMTFileNameTextField.getValue();

		if(filePath == null || PostAnalysisInputPanel.checkFile(filePath).equals(Color.RED)) {
			String message = "SigGMT file name not valid.\n";
			knownSignatureGMTFileNameTextField.setForeground(Color.RED);
			JOptionPane.showMessageDialog(application.getJFrame(), message, "Post Analysis Known Signature",
					JOptionPane.WARNING_MESSAGE);
			return false;
		}

		paParams.setSignatureGMTFileName(filePath);

		// Load in the GMT file
		// Manually fire the same action listener that is used by the signature discovery panel.
		// Use the synchronousTaskManager so that this blocks
		LoadSignatureSetsActionListener loadAction = new LoadSignatureSetsActionListener(parentPanel, application,
				cyApplicationManager, syncTaskManager, streamUtil);
		loadAction.setSelectAll(true);
		loadAction.actionPerformed(null);

		return true;
	}

	void resetPanel() {
		paParams.setSignatureGenesets(new SetOfGeneSets());
		//Gene-Sets Panel
		knownSignatureGMTFileNameTextField.setText("");
		knownSignatureGMTFileNameTextField.setValue("");
		knownSignatureGMTFileNameTextField.setToolTipText(null);

		weightPanel.resetPanel();
	}

	void initialize(EnrichmentMap currentMap, PostAnalysisParameters paParams) {
		this.map = currentMap;
		this.paParams = paParams;

		weightPanel.initialize(currentMap, paParams);
	}
}
