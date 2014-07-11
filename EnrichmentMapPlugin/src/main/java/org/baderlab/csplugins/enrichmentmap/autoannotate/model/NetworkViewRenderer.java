package org.baderlab.csplugins.enrichmentmap.autoannotate.model;

import java.awt.Component;

import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.text.Position;

import org.cytoscape.view.model.CyNetworkView;

public class NetworkViewRenderer extends BasicComboBoxRenderer {

	private static final long serialVersionUID = -5877635875395629866L;  

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, 
			boolean isSelected, boolean cellHasFocus) {
        
		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);  

        if (value != null) {
        	String label = ((CyNetworkView) value).getModel().toString();
        	int viewNumber = 1;
        	while (list.getNextMatch(label + " View " + String.valueOf(viewNumber), 0, Position.Bias.Forward) != -1) {
        		viewNumber++;
        	}
        	if (viewNumber > 1) {
        		label += " View " + String.valueOf(viewNumber);
        	}
            setText(label);
        } 

        return this;  
    }  
}