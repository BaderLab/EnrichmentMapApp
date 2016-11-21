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

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.baderlab.csplugins.enrichmentmap.util.SwingUtil.makeSmall;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapBuildProperties;
import org.baderlab.csplugins.enrichmentmap.FilterParameters;
import org.baderlab.csplugins.enrichmentmap.FilterParameters.FilterType;
import org.baderlab.csplugins.enrichmentmap.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.actions.BuildPostAnalysisActionListener;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.util.SwingUtil;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.util.swing.FileChooserFilter;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.swing.DialogTaskManager;


@SuppressWarnings("serial")
public class PostAnalysisInputPanel extends JPanel {
    
	// tool tips
    protected static final String gmt_instruction = "Please select the Gene Set file (.gmt)...";
    protected static final String siggmt_instruction = "Please select the Signature Gene Set file (.gmt)...";
    
    private final CyApplicationManager cyApplicationManager;
    private final CySwingApplication application;
	private final FileUtil fileUtil;
	private final CyServiceRegistrar registrar;
	private final CySessionManager sessionManager;
	private final StreamUtil streamUtil;
	private final DialogTaskManager dialogTaskManager;
	private final CyEventHelper eventHelper;
    
	private final VisualMappingManager visualMappingManager;
	private final VisualStyleFactory visualStyleFactory;
	
	private final VisualMappingFunctionFactory vmfFactoryContinuous;
    private final VisualMappingFunctionFactory vmfFactoryDiscrete;
    private final VisualMappingFunctionFactory vmfFactoryPassthrough;
    
    
    private JRadioButton knownSignatureRadio;
    private JRadioButton signatureDiscoveryRadio;
    
    // Top level panel for signature discovery or known signature
    private JPanel userInputPanel;
    
    private PostAnalysisSignatureDiscoveryPanel signatureDiscoveryPanel;
    private PostAnalysisKnownSignaturePanel knownSignaturePanel;
    
    private PostAnalysisParameters sigDiscoveryPaParams;
    private PostAnalysisParameters knownSigPaParams;
    
    private final PostAnalysisPanel parent;
    
	public PostAnalysisInputPanel(PostAnalysisPanel parent, 
			CyApplicationManager cyApplicationManager, CySwingApplication application,
			FileUtil fileUtil, CySessionManager sessionManager, StreamUtil streamUtil, CyServiceRegistrar registrar,
			DialogTaskManager dialog, SynchronousTaskManager syncTaskManager, CyEventHelper eventHelper,
			VisualMappingManager visualMappingManager, VisualStyleFactory visualStyleFactory,
			VisualMappingFunctionFactory vmfFactoryContinuous, VisualMappingFunctionFactory vmfFactoryDiscrete,
			VisualMappingFunctionFactory vmfFactoryPassthrough) {
		this.parent = parent;
		
    	this.cyApplicationManager = cyApplicationManager;
    	this.application = application;
        this.fileUtil = fileUtil;
        this.registrar = registrar;
        this.sessionManager = sessionManager;
        this.streamUtil = streamUtil;
        this.dialogTaskManager = dialog;
        this.eventHelper = eventHelper;
        this.visualMappingManager = visualMappingManager;
        this.visualStyleFactory = visualStyleFactory;
        this.vmfFactoryContinuous = vmfFactoryContinuous;
        this.vmfFactoryDiscrete = vmfFactoryDiscrete;
        this.vmfFactoryPassthrough = vmfFactoryPassthrough;
    		
        // Create the two main panels, set the default one
        knownSignaturePanel = new PostAnalysisKnownSignaturePanel(this, streamUtil, registrar);
        signatureDiscoveryPanel = new PostAnalysisSignatureDiscoveryPanel(this, streamUtil, registrar);
       
        userInputPanel = new JPanel(new BorderLayout());
        userInputPanel.add(knownSignaturePanel, BorderLayout.CENTER); // Default panel

        JPanel analysisTypePanel = createAnalysisTypePanel();
        
        JScrollPane scrollPane = new JScrollPane(userInputPanel);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setBackground(UIManager.getColor("Panel.background"));
        
        JPanel bottomPanel = createBottomPanel();
        
        final GroupLayout layout = new GroupLayout(this);
       	setLayout(layout);
   		layout.setAutoCreateContainerGaps(false);
   		layout.setAutoCreateGaps(false);
   		
   		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
   				.addComponent(analysisTypePanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
   				.addComponent(scrollPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
   				.addComponent(bottomPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
   		);
   		layout.setVerticalGroup(layout.createSequentialGroup()
   				.addComponent(analysisTypePanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   				.addComponent(scrollPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
   				.addComponent(bottomPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   		);
   		
   		if (LookAndFeelUtil.isAquaLAF()) {
			setOpaque(false);
			userInputPanel.setOpaque(false);
   		}
    }
	
	public void update() {
    	signatureDiscoveryPanel.update();
	}

    private void flipPanels(JPanel toRemove, JPanel toAdd) {
    	userInputPanel.remove(toRemove);
    	userInputPanel.add(toAdd, BorderLayout.CENTER);
    	userInputPanel.revalidate();
    	userInputPanel.repaint();
    }
    
	/**
     * Creates a JPanel containing scope radio buttons
     */
	private JPanel createAnalysisTypePanel() {
		knownSignatureRadio = new JRadioButton("Known Signature");
		knownSignatureRadio.setSelected(true);
		knownSignatureRadio.addActionListener((ActionEvent e) -> {
			flipPanels(signatureDiscoveryPanel, knownSignaturePanel);
		});

		signatureDiscoveryRadio = new JRadioButton("Signature Discovery");
		signatureDiscoveryRadio.addActionListener((ActionEvent e) -> {
			flipPanels(knownSignaturePanel, signatureDiscoveryPanel);
		});

		makeSmall(knownSignatureRadio, signatureDiscoveryRadio);
		
		ButtonGroup analysisOptions = new ButtonGroup();
		analysisOptions.add(knownSignatureRadio);
		analysisOptions.add(signatureDiscoveryRadio);

		JPanel panel = new JPanel();
		panel.setBorder(LookAndFeelUtil.createTitledBorder("Post Analysis Type"));
		
		final GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
	   		
   		layout.setHorizontalGroup(layout.createSequentialGroup()
   				.addGap(0, 0, Short.MAX_VALUE)
   				.addComponent(knownSignatureRadio)
   				.addComponent(signatureDiscoveryRadio)
   				.addGap(0, 0, Short.MAX_VALUE)
   		);
   		layout.setVerticalGroup(layout.createParallelGroup(Alignment.CENTER, true)
   				.addComponent(knownSignatureRadio, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   				.addComponent(signatureDiscoveryRadio, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   		);
   		
   		if (LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);

		return panel;
	}
    
    /**
     * Utility method that creates a panel for buttons at the bottom of the Enrichment Map Panel
     */
	private JPanel createBottomPanel() {
		JButton helpButton = SwingUtil.createOnlineHelpButton(EnrichmentMapBuildProperties.USER_MANUAL_URL,
				"Online Manual...", registrar);
		
		JButton resetButton = new JButton("Reset");
		resetButton.addActionListener((ActionEvent e) -> {
			resetPanel();
		});

		JButton closeButton = new JButton("Close");
		closeButton.addActionListener((ActionEvent e) -> {
			parent.close();
		});

		JButton importButton = new JButton("Run");
		importButton.addActionListener((ActionEvent e) -> {
			boolean okToRun = beforeRun();
			if (okToRun) {
				BuildPostAnalysisActionListener action = new BuildPostAnalysisActionListener(
						PostAnalysisInputPanel.this, sessionManager, streamUtil, application, cyApplicationManager,
						dialogTaskManager, eventHelper, visualMappingManager, visualStyleFactory, vmfFactoryContinuous,
						vmfFactoryDiscrete, vmfFactoryPassthrough);
				action.runPostAnalysis();
			}
		});

		JPanel panel = LookAndFeelUtil.createOkCancelPanel(importButton, closeButton, helpButton, resetButton);

		if (LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);

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
        
    
	protected static boolean validateAndSetFilterValue(JFormattedTextField source, FilterParameters filterParams, StringBuilder message) {
    	Number value = (Number) source.getValue();
    	boolean valid = false;
    	FilterType type = filterParams.getType();
    	
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
    	
    	if(valid)
    		filterParams.setValue(type, value.doubleValue());
    	else
    		source.setValue(type.defaultValue);
    	
    	return valid;
    }
    
    protected static Color checkFile(String filename){
        //check to see if the files exist and are readable.
        //if the file is unreadable change the color of the font to red
        //otherwise the font should be black.
        if(filename != null){
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
    	if(knownSigPaParams != null)
    		knownSignaturePanel.resetPanel();
    	if(sigDiscoveryPaParams != null)
    		signatureDiscoveryPanel.resetPanel();
    }

    /**
     * Refresh content of PostAnalysisInputPanel when Network is changed or Panel is re-opened.
     * 
     * @param current_params
     */
    public void initialize(EnrichmentMap currentMap) {
    	resetPanel();
		if(currentMap != null) {
			// Use two separate parameters objects so that the two panels don't interfere with each other
			knownSigPaParams = new PostAnalysisParameters();
			knownSigPaParams.setSignatureHub(false);
			
			sigDiscoveryPaParams = new PostAnalysisParameters();
			sigDiscoveryPaParams.setSignatureHub(true);
			
			knownSignaturePanel.initialize(currentMap, knownSigPaParams);
			signatureDiscoveryPanel.initialize(currentMap, sigDiscoveryPaParams);
			
	        knownSignatureRadio.setToolTipText(currentMap.getName());
		}
    }
    

	public PostAnalysisParameters getPaParams() {
		return knownSignatureRadio.isSelected() ? knownSigPaParams : sigDiscoveryPaParams;
    }

	/**
	 * Set available signature gene set count to specified value
	 * @param int avSigCount
	 * @return null
	 */
	public void setAvSigCount(int avSigCount) {
		if (signatureDiscoveryRadio.isSelected()) {
			signatureDiscoveryPanel.setAvSigCount(avSigCount);
		}
	}
	
	public boolean beforeRun() {
		if(knownSignatureRadio.isSelected())
			return knownSignaturePanel.beforeRun();
		else
			return true;
	}
}
