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

package org.baderlab.csplugins.enrichmentmap.view.postanalysis;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil.makeSmall;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

import org.baderlab.csplugins.enrichmentmap.AfterInjection;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.model.PostAnalysisFilterType;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.cytoscape.util.swing.FileChooserFilter;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.util.swing.LookAndFeelUtil;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

@SuppressWarnings("serial")
public class PostAnalysisInputPanel extends JPanel {
    
	@Inject private FileUtil fileUtil;
	
	private JRadioButton knownSignatureRadio;
	private JRadioButton signatureDiscoveryRadio;

	// Top level panel for signature discovery or known signature
	private JScrollPane userInputScrollPane;

	private PostAnalysisSignatureDiscoveryPanel signatureDiscoveryPanel;
	private PostAnalysisKnownSignaturePanel knownSignaturePanel;
	
	private final EnrichmentMap map;
	private final PostAnalysisKnownSignaturePanel.Factory knownSignaturePanelFactory;
	private final PostAnalysisSignatureDiscoveryPanel.Factory signatureDiscoveryPanelFactory;

	public interface Factory {
		PostAnalysisInputPanel create(EnrichmentMap map);
	}
	
	/**
	 * Note: The initialize() method must be called before the panel can be used.
	 */
	@Inject
	public PostAnalysisInputPanel(
			@Assisted EnrichmentMap map,
			PostAnalysisKnownSignaturePanel.Factory knownSignaturePanelFactory,
			PostAnalysisSignatureDiscoveryPanel.Factory signatureDiscoveryPanelFactory
	) {
		this.map = map;
		this.knownSignaturePanelFactory = knownSignaturePanelFactory;
		this.signatureDiscoveryPanelFactory = signatureDiscoveryPanelFactory;
	}
	
	@AfterInjection
	private void createContent() {
		JPanel analysisTypePanel = createAnalysisTypePanel();
		
		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(false);

		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
   				.addComponent(analysisTypePanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
   				.addComponent(getUserInputScrollPane(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
   		layout.setVerticalGroup(layout.createSequentialGroup()
   				.addComponent(analysisTypePanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   				.addComponent(getUserInputScrollPane(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
   		);

		if (LookAndFeelUtil.isAquaLAF())
			setOpaque(false);
		
		initialize();
	}

	private void initialize() {
		if (map != null) {
			getKnownSignaturePanel().initialize(map);
			getSignatureDiscoveryPanel().initialize(map);
			getKnownSignatureRadio().setToolTipText(map.getName());
		}
	}
	
	private void showInputPanel(JPanel panel) {
		getUserInputScrollPane().setViewportView(panel);
	}

	/**
	 * Creates a JPanel containing scope radio buttons
	 */
	private JPanel createAnalysisTypePanel() {
		makeSmall(getKnownSignatureRadio(), getSignatureDiscoveryRadio());
		
		ButtonGroup analysisOptions = new ButtonGroup();
		analysisOptions.add(getKnownSignatureRadio());
		analysisOptions.add(getSignatureDiscoveryRadio());

		JPanel panel = new JPanel();
		panel.setBorder(LookAndFeelUtil.createTitledBorder("Post Analysis Type"));

		final GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);

   		layout.setHorizontalGroup(layout.createSequentialGroup()
   				.addGap(0, 0, Short.MAX_VALUE)
   				.addComponent(getKnownSignatureRadio())
   				.addComponent(getSignatureDiscoveryRadio())
   				.addGap(0, 0, Short.MAX_VALUE)
   		);
   		layout.setVerticalGroup(layout.createParallelGroup(Alignment.CENTER, true)
   				.addComponent(getKnownSignatureRadio(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   				.addComponent(getSignatureDiscoveryRadio(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   		);

		if (LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);

		return panel;
	}
	
	JScrollPane getUserInputScrollPane() {
		if (userInputScrollPane == null) {
			userInputScrollPane = new JScrollPane(getKnownSignaturePanel()); // Default panel
			
			int w = Math.max(
					getKnownSignaturePanel().getPreferredSize().width,
					getSignatureDiscoveryPanel().getPreferredSize().width
			);
			userInputScrollPane.setPreferredSize(new Dimension(w + 40, userInputScrollPane.getPreferredSize().height));
		}
		
		return userInputScrollPane;
	}
	
	PostAnalysisKnownSignaturePanel getKnownSignaturePanel() {
		if (knownSignaturePanel == null) {
			knownSignaturePanel = knownSignaturePanelFactory.create(this);
		}
		
		return knownSignaturePanel;
	}
	
	PostAnalysisSignatureDiscoveryPanel getSignatureDiscoveryPanel() {
		if (signatureDiscoveryPanel == null) {
			signatureDiscoveryPanel = signatureDiscoveryPanelFactory.create(this);
		}
		
		return signatureDiscoveryPanel;
	}
	
	JRadioButton getKnownSignatureRadio() {
		if (knownSignatureRadio == null) {
			knownSignatureRadio = new JRadioButton("Known Signature");
			knownSignatureRadio.setSelected(true);
			knownSignatureRadio.addActionListener(evt -> {
				showInputPanel(getKnownSignaturePanel());
			});
		}
		
		return knownSignatureRadio;
	}
	
	JRadioButton getSignatureDiscoveryRadio() {
		if (signatureDiscoveryRadio == null) {
			signatureDiscoveryRadio = new JRadioButton("Signature Discovery");
			signatureDiscoveryRadio.addActionListener(evt -> {
				showInputPanel(getSignatureDiscoveryPanel());
			});
		}
		
		return signatureDiscoveryRadio;
	}

	protected File chooseGMTFile(JFormattedTextField textField) {
		FileChooserFilter filter = new FileChooserFilter("All GMT Files", "gmt");
		List<FileChooserFilter> all_filters = Arrays.asList(filter);

		// Get the file name
		File file = fileUtil.getFile(SwingUtil.getWindowInstance(this), "Import Signature GMT File", FileUtil.LOAD,
				all_filters);

		if (file != null) {
			String absolutePath = file.getAbsolutePath();
			textField.setForeground(PostAnalysisInputPanel.checkFile(absolutePath));
			textField.setText(absolutePath);
			textField.setValue(absolutePath);
			textField.setToolTipText(absolutePath);
			textField.setCaretPosition(textField.getText().length());
		}

		return file;
	}

	protected static Optional<Double> validateAndGetFilterValue(Number value, PostAnalysisFilterType type, StringBuilder message) {
		boolean valid = false;

		switch (type) {
			case HYPERGEOM:
				if (value != null && value.doubleValue() >= 0.0 && value.intValue() <= 1.0)
					valid = true;
				else
					message.append("The value must be greater than or equal 0.0 and less than or equal to 1.0");
				break;
			case MANN_WHIT_TWO_SIDED:
			case MANN_WHIT_LESS:
			case MANN_WHIT_GREATER:
				if (value != null && value.doubleValue() >= 0.0 && value.intValue() <= 1.0)
					valid = true;
				else
					message.append("The value must be greater than or equal 0.0 and less than or equal to 1.0");
				break;
			case PERCENT:
			case SPECIFIC:
				if (value != null && value.intValue() >= 0 && value.intValue() <= 100)
					valid = true;
				else
					message.append("The value must be greater than or equal 0 and less than or equal to 100.");
				break;
			case NUMBER:
				if (value != null && value.intValue() >= 0)
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

	/**
	 * @deprecated Use {@link EnrichmentMapParameters#checkFile(String)} instead
	 */
	@Deprecated
	protected static Color checkFile(String filename) {
		// TODO Don't use color as a boolean!
		// check to see if the files exist and are readable.
		// if the file is unreadable change the color of the font to red
		// otherwise the font should be black.
		if (filename != null) {
			File tempfile = new File(filename);
			if (!tempfile.canRead())
				return Color.RED;
		}
		return Color.BLACK;
	}

	boolean isReady() {
		if (knownSignatureRadio.isSelected())
			return getKnownSignaturePanel().isReady();
		else
			return true;
	}
	
	/**
	 * Clear the current panel and clear the paParams associated with each panel
	 */
	void reset() {
		getKnownSignaturePanel().reset();
		getSignatureDiscoveryPanel().reset();
	}

//	/**
//	 * Set available signature gene set count to specified value
//	 */
//	public void setAvSigCount(int avSigCount) {
//		if (getSignatureDiscoveryRadio().isSelected()) {
//			getSignatureDiscoveryPanel().setAvSigCount(avSigCount);
//		}
//	}
}
