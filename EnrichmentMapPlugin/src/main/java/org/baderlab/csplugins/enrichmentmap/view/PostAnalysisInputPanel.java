/*
 *                       EnrichmentMap Cytoscape Plugin
 *
 * Copyright (c) 2008-2009 Bader Lab, Donnelly Centre for Cellular and Biomolecular
 * Research, University of Toronto
 *
 * Contact: http://www.baderlab.org
 *
 * Code written by: Ruth Isserlin
 * Authors: Daniele Merico, Ruth Isserlin, Oliver Stueker, Gary D. Bader
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * University of Toronto
 * has no obligations to provide maintenance, support, updates,
 * enhancements or modifications.  In no event shall the
 * University of Toronto
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * University of Toronto
 * has been advised of the possibility of such damage.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 *
 */

// $Id$
// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$
// $HeadURL$

package org.baderlab.csplugins.enrichmentmap.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

import org.baderlab.csplugins.enrichmentmap.AfterInjection;
import org.baderlab.csplugins.enrichmentmap.EnrichmentMapBuildProperties;
import org.baderlab.csplugins.enrichmentmap.FilterType;
import org.baderlab.csplugins.enrichmentmap.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.PostAnalysisParameters.AnalysisType;
import org.baderlab.csplugins.enrichmentmap.actions.BuildPostAnalysisActionListener;
import org.baderlab.csplugins.enrichmentmap.actions.ShowAboutPanelAction;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.util.SwingUtil;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.FileChooserFilter;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.util.swing.OpenBrowser;

import com.google.inject.Inject;
import com.google.inject.Provider;

@SuppressWarnings("serial")
public class PostAnalysisInputPanel extends JPanel {

	// tool tips
	protected static final String gmtTip = "File specifying gene sets.\nFormat: geneset name <tab> description <tab> gene ...";
	protected static final String gmt_instruction = "Please select the Gene Set file (.gmt)...";
	protected static final String siggmt_instruction = "Please select the Signature Gene Set file (.gmt)...";

	@Inject private OpenBrowser browser;
	@Inject private FileUtil fileUtil;
	@Inject private CyServiceRegistrar registrar;
	
	@Inject private Provider<ShowAboutPanelAction> aboutPanelActionProvider;
	@Inject private BuildPostAnalysisActionListener.Factory buildPostAnalysisActionListenerFactory;
	
	
	private JRadioButton knownSignature;
	private JRadioButton signatureDiscovery;

	// Top level panel for signature discovery or known signature
	private JPanel userInputPanel;

	private PostAnalysisSignatureDiscoveryPanel signatureDiscoveryPanel;
	private PostAnalysisKnownSignaturePanel knownSignaturePanel;
	
	private EnrichmentMap map;


	/**
	 * Note: The initialize() method must be called before the panel can be used.
	 */
	@Inject
	public PostAnalysisInputPanel(
			PostAnalysisKnownSignaturePanel.Factory knownSignaturePanelFactory,
			PostAnalysisSignatureDiscoveryPanel.Factory signatureDiscoveryPanelFactory) {
		
		// Create the two main panels, set the default one
		knownSignaturePanel = knownSignaturePanelFactory.create(this);
		signatureDiscoveryPanel = signatureDiscoveryPanelFactory.create(this);

		userInputPanel = new JPanel(new BorderLayout());
		userInputPanel.add(knownSignaturePanel, BorderLayout.CENTER); // Default panel
	}
	
	@AfterInjection
	private void createContent() {
		setLayout(new BorderLayout());

		JPanel analysisTypePanel = createAnalysisTypePanel();
		add(analysisTypePanel, BorderLayout.NORTH);

		JPanel advancedOptionsContainer = new JPanel(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane(userInputPanel);
		advancedOptionsContainer.add(scrollPane, BorderLayout.CENTER);
		add(advancedOptionsContainer, BorderLayout.CENTER);

		JPanel bottomPanel = createBottomPanel();
		add(bottomPanel, BorderLayout.SOUTH);
	}

	private void flipPanels(JPanel toRemove, JPanel toAdd) {
		userInputPanel.remove(toRemove);
		userInputPanel.add(toAdd, BorderLayout.CENTER);
		userInputPanel.revalidate();
		userInputPanel.repaint();
	}

	/**
	 * Creates a JPanel containing scope radio buttons
	 *
	 * @return panel containing the scope option buttons
	 */
	private JPanel createAnalysisTypePanel() {
		JPanel buttonsPanel = new JPanel();
		GridBagLayout gridbag_buttons = new GridBagLayout();
		GridBagConstraints c_buttons = new GridBagConstraints();
		buttonsPanel.setLayout(gridbag_buttons);
		buttonsPanel.setBorder(BorderFactory.createTitledBorder("Info:"));

		JButton help = new JButton("Online Manual");
		help.addActionListener(e -> {
			browser.openURL(EnrichmentMapBuildProperties.USER_MANUAL_URL);
		});

		JButton about = new JButton("About");
		Map<String, String> serviceProperties = new HashMap<String, String>();
		serviceProperties.put("inMenuBar", "true");
		serviceProperties.put("preferredMenu", "Apps.EnrichmentMap");
		about.addActionListener(aboutPanelActionProvider.get());

		c_buttons.weighty = 1;
		c_buttons.weightx = 1;
		c_buttons.insets = new Insets(0, 0, 0, 0);
		c_buttons.gridx = 0;
		c_buttons.gridwidth = 1;
		c_buttons.gridy = 0;
		c_buttons.fill = GridBagConstraints.HORIZONTAL;

		c_buttons.gridy = 0;
		gridbag_buttons.setConstraints(about, c_buttons);
		buttonsPanel.add(about);

		c_buttons.gridy = 1;
		gridbag_buttons.setConstraints(help, c_buttons);
		buttonsPanel.add(help);

		JPanel panel = new JPanel();

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		panel.setLayout(gridbag);

		c.weighty = 1;
		c.weightx = 1;
		c.insets = new Insets(0, 0, 0, 0);
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.setBorder(BorderFactory.createTitledBorder("Post Analysis Type"));

		knownSignature = new JRadioButton("Known Signature");
		knownSignature.setSelected(true);
		knownSignature.addActionListener(e -> {
			flipPanels(signatureDiscoveryPanel, knownSignaturePanel);
		});

		signatureDiscovery = new JRadioButton("Signature Discovery");
		signatureDiscovery.addActionListener(e -> {
			flipPanels(knownSignaturePanel, signatureDiscoveryPanel);
		});

		ButtonGroup analysisOptions = new ButtonGroup();
		analysisOptions.add(knownSignature);
		analysisOptions.add(signatureDiscovery);

		c.gridx = 0;
		c.gridwidth = 3;
		c.gridy = 0;
		gridbag.setConstraints(knownSignature, c);
		panel.add(knownSignature);

		c.gridy = 1;
		gridbag.setConstraints(signatureDiscovery, c);
		panel.add(signatureDiscovery);

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		topPanel.add(buttonsPanel, BorderLayout.EAST);
		topPanel.add(panel, BorderLayout.CENTER);

		return topPanel;
	}

	/**
	 * Utility method that creates a panel for buttons at the bottom of the
	 * Enrichment Map Panel
	 *
	 * @return a flow layout panel containing the build map and cancel buttons
	 */
	private JPanel createBottomPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());

		JButton resetButton = new JButton("Reset");
		resetButton.addActionListener(e -> resetPanel());

		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(e -> close());

		JButton runButton = new JButton("Run");
		runButton.addActionListener(e -> {
			if(okToRun()) {
				PostAnalysisParameters paParams = buildPostAnalysisParameters();
				BuildPostAnalysisActionListener action = buildPostAnalysisActionListenerFactory.create(paParams);
				action.runPostAnalysis();
			}
		});

		runButton.setEnabled(true);

		panel.add(resetButton);
		panel.add(closeButton);
		panel.add(runButton);

		return panel;
	}

	protected File chooseGMTFile(JFormattedTextField textField) {
		FileChooserFilter filter = new FileChooserFilter("All GMT Files", "gmt");
		List<FileChooserFilter> all_filters = Arrays.asList(filter);

		// Get the file name
		File file = fileUtil.getFile(SwingUtil.getWindowInstance(this), "Import Signature GMT File", FileUtil.LOAD, all_filters);

		if(file != null) {
			String absolutePath = file.getAbsolutePath();
			textField.setForeground(PostAnalysisInputPanel.checkFile(absolutePath));
			textField.setText(absolutePath);
			textField.setValue(absolutePath);
			textField.setToolTipText(absolutePath);
			textField.setCaretPosition(textField.getText().length());
		}

		return file;
	}

	protected static Optional<Double> validateAndGetFilterValue(Number value, FilterType type, StringBuilder message) {
		boolean valid = false;

		switch(type) {
			case HYPERGEOM:
				if(value != null && value.doubleValue() >= 0.0 && value.intValue() <= 1.0)
					valid = true;
				else
					message.append("The value must be greater than or equal 0.0 and less than or equal to 1.0");
				break;
			case MANN_WHIT_TWO_SIDED:
			case MANN_WHIT_LESS:
			case MANN_WHIT_GREATER:
				if(value != null && value.doubleValue() >= 0.0 && value.intValue() <= 1.0)
					valid = true;
				else
					message.append("The value must be greater than or equal 0.0 and less than or equal to 1.0");
				break;
			case PERCENT:
			case SPECIFIC:
				if(value != null && value.intValue() >= 0 && value.intValue() <= 100)
					valid = true;
				else
					message.append("The value must be greater than or equal 0 and less than or equal to 100.");
				break;
			case NUMBER:
				if(value != null && value.intValue() >= 0)
					valid = true;
				else
					message.append("The value must be greater than or equal 0.");
				break;
			default:
				valid = true;
				break;
		}

		return valid ? Optional.of(value.doubleValue()) : Optional.empty();
	}

	public void close() {
		registrar.unregisterService(this, CytoPanelComponent.class);
	}

	protected static Color checkFile(String filename) {
		//check to see if the files exist and are readable.
		//if the file is unreadable change the color of the font to red
		//otherwise the font should be black.
		if(filename != null) {
			File tempfile = new File(filename);
			if(!tempfile.canRead())
				return Color.RED;
		}
		return Color.BLACK;
	}

	/**
	 * Clear the current panel and clear the paParams associated with each panel
	 */
	private void resetPanel() {
		knownSignaturePanel.resetPanel();
		signatureDiscoveryPanel.resetPanel();
	}

	/**
	 * Refresh content of PostAnalysisInputPanel when Network is changed or Panel is re-opened.
	 * 
	 * @param current_params
	 */
	public void initialize(EnrichmentMap currentMap) {
		this.map = currentMap;
		knownSignaturePanel.initialize(currentMap);
		signatureDiscoveryPanel.initialize(currentMap);
		knownSignature.setToolTipText(currentMap.getName());
	}

	/**
	 * Creates a PostAnalysisParameters object based on the user's input.
	 */
	public PostAnalysisParameters buildPostAnalysisParameters() {
		PostAnalysisParameters.Builder builder = new PostAnalysisParameters.Builder();
		
		if(knownSignature.isSelected()) {
			builder.setAnalysisType(AnalysisType.KNOWN_SIGNATURE);
			knownSignaturePanel.build(builder);
		}
		else {
			builder.setAnalysisType(AnalysisType.SIGNATURE_DISCOVERY);
			signatureDiscoveryPanel.build(builder);
		}
		
		builder.setAttributePrefix(map.getParams().getAttributePrefix());
		
		return builder.build();
	}

//	/**
//	 * Set available signature gene set count to specified value
//	 * 
//	 * @param int avSigCount
//	 * @return null
//	 */
//	public void setAvSigCount(int avSigCount) {
//		if(signatureDiscovery.isSelected()) {
//			signatureDiscoveryPanel.setAvSigCount(avSigCount);
//		}
//	}

	public boolean okToRun() {
		if(knownSignature.isSelected())
			return knownSignaturePanel.okToRun();
		else
			return true;
	}
	
}
