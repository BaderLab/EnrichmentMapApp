package org.baderlab.csplugins.enrichmentmap.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.net.URL;
import java.util.WeakHashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.util.SwingUtil;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.swing.DialogTaskManager;

/**
 * A simple top-level panel which manages an instance of PostAnalysisInputPanel
 * for each enrichment map network. This allows user input to be saved when the
 * user switches networks without have to overhaul how PostAnalysisInputPanel works.
 */
@SuppressWarnings("serial")
public class PostAnalysisPanel extends JPanel implements CytoPanelComponent {
	
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
    
    
    private WeakHashMap<EnrichmentMap,PostAnalysisInputPanel> panels = new WeakHashMap<>();
    private PostAnalysisInputPanel currentPanel;
    
    
    public PostAnalysisPanel(CyApplicationManager cyApplicationManager, CySwingApplication application, 
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
        
    	setLayout(new BorderLayout());
    }
	
    
    public void showPanelFor(EnrichmentMap currentMap) {
    	PostAnalysisInputPanel panel;
    	if(currentMap == null) {
    		if(!currentPanel.isEnabled()) // its already showing the disabled panel
    			return;
			panel = newPostAnalysisInputPanel();
			SwingUtil.recursiveEnable(panel, false);
    	}
    	else {
	    	panel = panels.get(currentMap);
	    	if(panel == null) {
	    		panel = newPostAnalysisInputPanel();
	    		panel.initialize(currentMap);
	    		panels.put(currentMap, panel);
	    	}
    	}
    	
    	removeAll();
    	add(currentPanel = panel, BorderLayout.CENTER);
    	revalidate();
    	repaint();
    }
    
    public void removeEnrichmentMap(EnrichmentMap map) {
		panels.remove(map);
    }    
    
    private PostAnalysisInputPanel newPostAnalysisInputPanel() {
    	return new PostAnalysisInputPanel(cyApplicationManager, application, browser, fileUtil, sessionManager, streamUtil, registrar, dialog, 
    			eventHelper, equationCompiler, visualMappingManager, visualStyleFactory, vmfFactoryContinuous, vmfFactoryDiscrete, vmfFactoryPassthrough);
    }
    
    
	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.WEST;
	}

	@Override
	public Icon getIcon() {
		URL EMIconURL = this.getClass().getResource("enrichmentmap_logo_notext_small.png");
        ImageIcon EMIcon = null;
        if (EMIconURL != null) {
            EMIcon = new ImageIcon(EMIconURL);
        }
		return EMIcon;
	}

	@Override
	public String getTitle() {
		return "Post Analysis Input Panel";
	}

}
