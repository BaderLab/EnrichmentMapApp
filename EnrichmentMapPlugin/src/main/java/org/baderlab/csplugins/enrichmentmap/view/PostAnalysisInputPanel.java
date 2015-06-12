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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapBuildProperties;
import org.baderlab.csplugins.enrichmentmap.FilterParameters;
import org.baderlab.csplugins.enrichmentmap.FilterParameters.FilterType;
import org.baderlab.csplugins.enrichmentmap.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.actions.BuildPostAnalysisActionListener;
import org.baderlab.csplugins.enrichmentmap.actions.ShowAboutPanelAction;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.util.SwingUtil;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.util.swing.FileChooserFilter;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.swing.DialogTaskManager;


@SuppressWarnings("serial")
public class PostAnalysisInputPanel extends JPanel {
    
    // tool tips
    protected static final String gmtTip = "File specifying gene sets.\n" + "Format: geneset name <tab> description <tab> gene ...";
    protected static final String gmt_instruction = "Please select the Gene Set file (.gmt)...";
    protected static final String siggmt_instruction = "Please select the Signature Gene Set file (.gmt)...";
    
    private final CyApplicationManager cyApplicationManager;
    private final CySwingApplication application;
	private final OpenBrowser browser;
	private final FileUtil fileUtil;
	private final CyServiceRegistrar registrar;
	private final CySessionManager sessionManager;
	private final StreamUtil streamUtil;
	private final DialogTaskManager dialog;
	private final CyEventHelper eventHelper;
	private final EquationCompiler equationCompiler;
    
	private final VisualMappingManager visualMappingManager;
	private final VisualStyleFactory visualStyleFactory;
	
	private final VisualMappingFunctionFactory vmfFactoryContinuous;
    private final VisualMappingFunctionFactory vmfFactoryDiscrete;
    private final VisualMappingFunctionFactory vmfFactoryPassthrough;
    
    
    private JRadioButton knownSignature;
    private JRadioButton signatureDiscovery;
    
    // Top level panel for signature discovery or known signature
    private JPanel userInputPanel;
    
    private PostAnalysisSignatureDiscoveryPanel signatureDiscoveryPanel;
    private PostAnalysisKnownSignaturePanel knownSignaturePanel;
    
    private PostAnalysisParameters sigDiscoveryPaParams;
    private PostAnalysisParameters knownSigPaParams;
    
    
    public PostAnalysisInputPanel(CyApplicationManager cyApplicationManager, CySwingApplication application, 
    		OpenBrowser browser,FileUtil fileUtil, CySessionManager sessionManager,
    		StreamUtil streamUtil,CyServiceRegistrar registrar,
    		DialogTaskManager dialog,CyEventHelper eventHelper, EquationCompiler equationCompiler,
    		VisualMappingManager visualMappingManager, VisualStyleFactory visualStyleFactory,
    		VisualMappingFunctionFactory vmfFactoryContinuous, VisualMappingFunctionFactory vmfFactoryDiscrete, VisualMappingFunctionFactory vmfFactoryPassthrough) {
    	
    	this.cyApplicationManager = cyApplicationManager;
    	this.application = application;
        this.browser = browser;
        this.fileUtil = fileUtil;
        this.registrar = registrar;
        this.sessionManager = sessionManager;
        this.streamUtil = streamUtil;
        this.dialog = dialog;
        this.eventHelper = eventHelper;
        this.equationCompiler = equationCompiler;
        this.visualMappingManager = visualMappingManager;
        this.visualStyleFactory = visualStyleFactory;
        this.vmfFactoryContinuous = vmfFactoryContinuous;
        this.vmfFactoryDiscrete = vmfFactoryDiscrete;
        this.vmfFactoryPassthrough = vmfFactoryPassthrough;
    		
        
        // Create the two main panels, set the default one
        knownSignaturePanel = new PostAnalysisKnownSignaturePanel(this, cyApplicationManager, application, streamUtil, dialog, fileUtil);
        signatureDiscoveryPanel = new PostAnalysisSignatureDiscoveryPanel(this, cyApplicationManager, application, streamUtil, dialog, fileUtil);
       
        userInputPanel = new JPanel(new BorderLayout());
        userInputPanel.add(knownSignaturePanel, BorderLayout.CENTER); // Default panel

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
    

    private void flipPanels(JPanel toRemove, JPanel toAdd){
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
        help.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                browser.openURL(EnrichmentMapBuildProperties.USER_MANUAL_URL);
            }
        });

        JButton about = new JButton("About");
        Map<String, String> serviceProperties = new HashMap<String, String>();
        serviceProperties.put("inMenuBar", "true");
		serviceProperties.put("preferredMenu", "Apps.EnrichmentMap");
        about.addActionListener(new ShowAboutPanelAction(serviceProperties , cyApplicationManager, null, application, browser));
		   
        c_buttons.weighty = 1;
        c_buttons.weightx = 1;
        c_buttons.insets = new Insets(0,0,0,0);
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
        c.insets = new Insets(0,0,0,0);
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.setBorder(BorderFactory.createTitledBorder("Post Analysis Type"));
        
        knownSignature = new JRadioButton("Known Signature");
        knownSignature.setSelected(true);
        knownSignature.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	flipPanels(signatureDiscoveryPanel, knownSignaturePanel);
            }
        });        

        signatureDiscovery = new JRadioButton("Signature Discovery");
        signatureDiscovery.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	flipPanels(knownSignaturePanel, signatureDiscoveryPanel);
            }
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
     * Utility method that creates a panel for buttons at the bottom of the Enrichment Map Panel
     *
     * @return a flow layout panel containing the build map and cancel buttons
     */
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                resetPanel();
            }
        });

        JButton closeButton = new JButton();
        closeButton.setText("Close");
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	close();
            }
        });

        JButton importButton = new JButton();
        importButton.setText("Run");
        importButton.addActionListener(new BuildPostAnalysisActionListener(this, sessionManager, streamUtil, application, cyApplicationManager, 
        		                                                           dialog, eventHelper, equationCompiler, visualMappingManager, visualStyleFactory,
        																   vmfFactoryContinuous, vmfFactoryDiscrete, vmfFactoryPassthrough));
        importButton.setEnabled(true);

        panel.add(resetButton);
        panel.add(closeButton);
        panel.add(importButton);

        return panel;
    }
    
    
    protected File chooseGMTFile(JFormattedTextField textField) {
    	FileChooserFilter filter = new FileChooserFilter("All GMT Files", "gmt");          
        List<FileChooserFilter> all_filters = Arrays.asList(filter);
       
        // Get the file name
        File file = fileUtil.getFile(SwingUtil.getWindowInstance(this), "Import Signature GMT File", FileUtil.LOAD, all_filters);
        getPaParams().setSignatureGMTFileName("");
        
        if(file != null) {
        	textField.setForeground(PostAnalysisInputPanel.checkFile(file.getAbsolutePath()));
        	textField.setText(file.getAbsolutePath());
        	textField.setValue(file.getAbsolutePath());
            getPaParams().setSignatureGMTFileName(file.getAbsolutePath());
            textField.setToolTipText(file.getAbsolutePath());
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
	    	case MANN_WHIT:
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
    
    
    public void close() {
    	registrar.unregisterService(this, CytoPanelComponent.class);
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
			
	        knownSignature.setToolTipText(currentMap.getName());
		}
    }
    

	public PostAnalysisParameters getPaParams() {
		return knownSignature.isSelected() ? knownSigPaParams : sigDiscoveryPaParams;
    }

	
    
	/**
	 * Set available signature gene set count to specified value
	 * @param int avSigCount
	 * @return null
	 */
	public void setAvSigCount(int avSigCount) {
		if (signatureDiscovery.isSelected()) {
			signatureDiscoveryPanel.setAvSigCount(avSigCount);
		}
	}
	
}
