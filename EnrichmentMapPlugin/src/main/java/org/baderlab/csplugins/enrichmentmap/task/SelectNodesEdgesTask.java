package org.baderlab.csplugins.enrichmentmap.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.baderlab.csplugins.enrichmentmap.model.AbstractDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EMSignatureDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class SelectNodesEdgesTask extends AbstractTask {

	private final EnrichmentMap map;
	private final CyNetworkView networkView;
	private final Set<AbstractDataSet> dataSets;
	private final boolean distinctEdges;
	
	@Inject private CyEventHelper eventHelper;

	public interface Factory {
		SelectNodesEdgesTask create(
			EnrichmentMap map,
			CyNetworkView networkView, 
			Set<AbstractDataSet> dataSets,
			boolean distinctEdges);
	}
	
	@Inject
	public SelectNodesEdgesTask(
			@Assisted EnrichmentMap map,
			@Assisted CyNetworkView networkView, 
			@Assisted Set<AbstractDataSet> dataSets,
			@Assisted boolean distinctEdges
	) {
		this.map = map;
		this.networkView = networkView;
		this.dataSets = dataSets;
		this.distinctEdges = distinctEdges;
	}
	
	@Override
	public void run(TaskMonitor tm) {
		tm.setTitle("Select Nodes and Edges from Data Sets");
		tm.setStatusMessage("Getting Nodes and Edges from Data Sets...");
		tm.setProgress(0.0);
		
		final Set<Long> dataSetNodes = EnrichmentMap.getNodesUnion(dataSets);
		final Set<Long> dataSetEdges;
		
		if (distinctEdges) {
			dataSetEdges = EnrichmentMap.getEdgesUnionForFiltering(dataSets, map, networkView.getModel());
		} else {
			dataSetEdges = new HashSet<>();
			
			for (CyEdge e : networkView.getModel().getEdgeList()) {
				if (dataSetNodes.contains(e.getSource().getSUID()) && dataSetNodes.contains(e.getTarget().getSUID()))
					dataSetEdges.add(e.getSUID());
			}
			
			for (AbstractDataSet ds : dataSets) {
				if (ds instanceof EMSignatureDataSet)
					dataSetEdges.addAll(EnrichmentMap.getEdgesUnion(Collections.singleton(ds)));
			}
		}
		
		if (!cancelled) {
			tm.setStatusMessage("Selecting Edges...");
			tm.setProgress(0.1);
			select(networkView.getEdgeViews(), dataSetEdges, BasicVisualLexicon.EDGE_VISIBLE);
		}
		
		if (!cancelled) {
			tm.setStatusMessage("Selecting Nodes...");
			tm.setProgress(0.5);
			select(networkView.getNodeViews(), dataSetNodes, BasicVisualLexicon.NODE_VISIBLE);
		}
		
		tm.setStatusMessage("Updating View...");
		tm.setProgress(0.7);
		eventHelper.flushPayloadEvents();
		
		tm.setProgress(0.9);
		networkView.updateView();
		
		tm.setProgress(1.0);
	}
	
	public <S extends CyIdentifiable> void select(Collection<View<S>> views, Set<Long> dataSetElements,
			VisualProperty<Boolean> vp) {
		List<RowSetRecord> records = new ArrayList<RowSetRecord>();
		CyNetwork net = networkView.getModel();
		
		// Disable all events from our table
		CyTable table = vp == BasicVisualLexicon.NODE_VISIBLE ? net.getDefaultNodeTable() : net.getDefaultEdgeTable();
		eventHelper.silenceEventSource(table);

		try {
			for (View<S> v : views) {
				if (cancelled)
					break;
				
				if (v.getVisualProperty(vp) == Boolean.TRUE) {
					boolean selected = dataSetElements.contains(v.getModel().getSUID());
					CyRow row = net.getRow(v.getModel());
					row.set(CyNetwork.SELECTED, selected);
					records.add(new RowSetRecord(row, CyNetwork.SELECTED, selected, selected));
				}
			}
		} finally {
			eventHelper.unsilenceEventSource(table);
			eventHelper.fireEvent(new RowsSetEvent(table, records));
		}
	}
}
