package org.baderlab.csplugins.enrichmentmap.autoannotate;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.model.CyNode;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import javax.media.j3d.View;
import javax.swing.JComponent;

final class AnnotationGraphicsLayer extends JComponent {
	private CyNetwork network;
	
	private CyNetworkView view;
	
	private final Object clusterLock = new Object();
	
	private static final Color fillColor = new Color(0x40ECD96F,true);
    private static final Color frameColor = new Color(0xECD96F);
    
    /**
     * Constructor. Creates the graphics layer and registers a mouse listener with
     * the view in order to respond to clicks.
     * @param view 
     */
    AnnotationGraphicsLayer(final CyNetworkView view) {
        this.view = view;
    }
        
}