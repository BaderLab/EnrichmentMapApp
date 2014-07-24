package org.baderlab.csplugins.enrichmentmap.autoannotate;

import java.util.HashMap;
import java.util.TreeMap;

import javax.swing.JPanel;

import org.baderlab.csplugins.enrichmentmap.autoannotate.view.AutoAnnotationPanel;
import org.cytoscape.application.events.SetSelectedNetworkViewsEvent;
import org.cytoscape.application.events.SetSelectedNetworkViewsListener;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.events.ColumnCreatedEvent;
import org.cytoscape.model.events.ColumnCreatedListener;
import org.cytoscape.model.events.ColumnDeletedEvent;
import org.cytoscape.model.events.ColumnDeletedListener;
import org.cytoscape.model.events.ColumnNameChangedEvent;
import org.cytoscape.model.events.ColumnNameChangedListener;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedEvent;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedListener;
/**
 * Created by
 * User: arkadyark
 * Date: July 9, 2014
 * Time: 9:46 AM
 */

public class AutoAnnotationManager implements
		SetSelectedNetworkViewsListener, ColumnCreatedListener, 
		ColumnDeletedListener, ColumnNameChangedListener,
		NetworkViewAboutToBeDestroyedListener {

	private static AutoAnnotationManager manager = null;
	
	private AutoAnnotationPanel annotationPanel;
	private HashMap<Long, AutoAnnotationParameters> networkViewToAutoAnnotationParameters;
	// Used to set clusterMaker default parameters
	private TreeMap<String, String> algorithmToColumnName;
	// used to read from the tables that WordCloud creates
	private CyTableManager tableManager;
	
	public AutoAnnotationManager() {
		networkViewToAutoAnnotationParameters = new HashMap<Long, AutoAnnotationParameters>();
		
		algorithmToColumnName = new TreeMap<String, String>();		
		algorithmToColumnName.put("Affinity Propagation Cluster", "__APCluster");
		algorithmToColumnName.put("Cluster Fuzzifier", "__fuzzifierCluster");
		algorithmToColumnName.put("Community cluster (GLay)", "__glayCluster");
		algorithmToColumnName.put("ConnectedComponents Cluster", "__ccCluster");
		algorithmToColumnName.put("Fuzzy C-Means Cluster", "__fcmlCluster");
		algorithmToColumnName.put("MCL Cluster", "__mclCluster");
		algorithmToColumnName.put("SCPS Cluster", "__scpsCluster");
	}
	
    public static AutoAnnotationManager getInstance() {
        if(manager == null)
            manager = new AutoAnnotationManager();
        return manager;
    }

	@Override
	public void handleEvent(SetSelectedNetworkViewsEvent e) {
		annotationPanel.updateSelectedView(e.getNetworkViews().get(0));
	}
	
	@Override
	public void handleEvent(ColumnNameChangedEvent e) {
		if (annotationPanel != null) {
			annotationPanel.updateColumnName(e.getSource(), e.getOldColumnName(), e.getNewColumnName());
		}
	}

	@Override
	public void handleEvent(ColumnDeletedEvent e) {
		if (annotationPanel != null) {
			annotationPanel.columnDeleted(e.getSource(), e.getColumnName());
		}
	}

	@Override
	public void handleEvent(ColumnCreatedEvent e) {
		if (annotationPanel != null) {
			annotationPanel.columnCreated(e.getSource(), e.getColumnName());
		}
	}

	@Override
	public void handleEvent(NetworkViewAboutToBeDestroyedEvent e) {
		if (annotationPanel != null) {
			annotationPanel.removeNetworkView(e.getNetworkView());
		}
	}
	
    public AutoAnnotationPanel getAnnotationPanel() {
		return annotationPanel;
	}

	public void setAnnotationPanel(AutoAnnotationPanel inputPanel) {
    	this.annotationPanel = inputPanel;
    }
	
	public TreeMap<String, String> getAlgorithmToColumnName() {
		return algorithmToColumnName;
	}
    	
	
	public HashMap<Long, AutoAnnotationParameters> getNetworkViewToAutoAnnotationParameters() {
		return networkViewToAutoAnnotationParameters;
	}

	public void setNetworkViewToAutoAnnotation(
			HashMap<Long, AutoAnnotationParameters> networkViewToAutoAnnotationParameters) {
		this.networkViewToAutoAnnotationParameters = networkViewToAutoAnnotationParameters;
	}

	public CyTableManager getTableManager() {
		return tableManager;
	}

	public void setTableManager(CyTableManager tableManager) {
		this.tableManager = tableManager;
	}
}
