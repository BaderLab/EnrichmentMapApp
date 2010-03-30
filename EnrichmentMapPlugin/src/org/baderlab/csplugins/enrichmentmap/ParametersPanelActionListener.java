package org.baderlab.csplugins.enrichmentmap;

import java.awt.event.ActionListener;

/**
 * Created by
 * User: risserlin
 * Date: Nov 9, 2009
 * Time: 1:33:03 PM
 */
public class ParametersPanelActionListener implements ActionListener {

    EnrichmentMapParameters params;

    public ParametersPanelActionListener(EnrichmentMapParameters params) {
        this.params = params;
    }

    public void actionPerformed(java.awt.event.ActionEvent evt) {
        if(evt.getActionCommand().equalsIgnoreCase(HeatMapParameters.sort_hierarchical_cluster)){
            params.setDefaultSortMethod(HeatMapParameters.sort_hierarchical_cluster);
        }
        else if(evt.getActionCommand().equalsIgnoreCase(HeatMapParameters.sort_none)){
            params.setDefaultSortMethod(HeatMapParameters.sort_none);
        }
        else if(evt.getActionCommand().equalsIgnoreCase(HeatMapParameters.sort_rank)){
            params.setDefaultSortMethod(HeatMapParameters.sort_rank);
        }
        else if(evt.getActionCommand().equalsIgnoreCase(HeatMapParameters.sort_column)){
                    params.setDefaultSortMethod(HeatMapParameters.sort_column);
                }

    }

}
