package org.baderlab.csplugins.enrichmentmap.view.postanalysis;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil.makeSmall;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;

import org.baderlab.csplugins.enrichmentmap.AfterInjection;
import org.baderlab.csplugins.enrichmentmap.actions.LoadSignatureSetsActionListener;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.model.SetOfGeneSets;
import org.baderlab.csplugins.enrichmentmap.task.FilterMetric;
import org.baderlab.csplugins.enrichmentmap.view.util.Messages;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.LookAndFeelUtil;

import com.google.common.base.Strings;
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
	private void createContents() {
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
	}

	/**
	 * @return Panel for choosing and loading GMT and SignatureGMT Geneset-Files
	 */
	private JPanel createKnownSignatureGMTPanel() {
		knownSignatureGMTFileNameTextField = new JFormattedTextField();
		knownSignatureGMTFileNameTextField.setColumns(15);
		knownSignatureGMTFileNameTextField.setToolTipText(Messages.GMT_INSTRUCTION);

		final Color textFieldForeground = knownSignatureGMTFileNameTextField.getForeground();
		knownSignatureGMTFileNameTextField.addPropertyChangeListener("value", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				// if the text is red set it back to black as soon as the user starts typing
				knownSignatureGMTFileNameTextField.setForeground(textFieldForeground);
			}
		});

		JButton selectSigGMTFileButton = new JButton("Browse...");
		selectSigGMTFileButton.setToolTipText(Messages.GMT_INSTRUCTION);
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

	public boolean isReady() {
		String filePath = (String) knownSignatureGMTFileNameTextField.getValue();

		// Load in the GMT file
		// Manually fire the same action listener that is used by the signature discovery panel.
		// Use the synchronousTaskManager so that this blocks
		
		FilterMetric filterMetric = new FilterMetric.None();
		LoadSignatureSetsActionListener loadAction = loadSignatureSetsActionListenerFactory.create(new File(filePath), filterMetric);
		
		loadAction.setGeneSetCallback(gs -> {
			this.signatureGenesets = gs;
		});
		
		loadAction.setFilteredSignatureSetsCallback(selected -> {
			this.selectedGenesetNames = selected;
		});
		
		loadAction.actionPerformed(null);
		return true;
	}

	void reset() {
		// Gene-Sets Panel
		knownSignatureGMTFileNameTextField.setText("");
		knownSignatureGMTFileNameTextField.setValue("");
		knownSignatureGMTFileNameTextField.setToolTipText(null);

		weightPanel.reset();
	}

	void initialize(EnrichmentMap currentMap) {
		weightPanel.initialize(currentMap);
	}
	
	
	public List<String> validateInput() {
		List<String> messages = new ArrayList<>();
		messages.addAll(weightPanel.validateInput());
		
		String filePath = (String) knownSignatureGMTFileNameTextField.getValue();
		try {
			if(Strings.isNullOrEmpty(filePath) || !Files.isReadable(Paths.get(filePath))) {
				messages.add("GMT file path is not valid.");
			}
		} catch(InvalidPathException e) {
			messages.add("GMT file path is not valid.");
		}
		return messages;
	}
	
	
	public boolean build(PostAnalysisParameters.Builder builder) {
		if(weightPanel.build(builder)) {
			String filePath = (String) knownSignatureGMTFileNameTextField.getValue();
			builder.setSignatureGMTFileName(filePath);
			builder.setLoadedGMTGeneSets(signatureGenesets);
			builder.addSelectedGeneSetNames(selectedGenesetNames);
			return true;
		}
		return false;
	}
}
