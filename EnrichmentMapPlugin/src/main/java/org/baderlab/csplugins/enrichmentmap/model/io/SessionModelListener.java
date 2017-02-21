package org.baderlab.csplugins.enrichmentmap.model.io;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.baderlab.csplugins.enrichmentmap.ApplicationModule.Headless;
import org.baderlab.csplugins.enrichmentmap.CyActivator;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.style.ColumnDescriptor;
import org.baderlab.csplugins.enrichmentmap.view.control.ControlPanelMediator;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTable.Mutability;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CySession;
import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;


/**
 * Converts each EnrichmentMap object into JSON format and stores it in a private table.
 * This is designed to support App reloading like when the App is updated.
 * 
 * MKTODO restore the EnrichmentMap field in DataSet
 * MKTODO map old node suids to new ones after session is restored
 */
@Singleton
public class SessionModelListener implements SessionLoadedListener, SessionAboutToBeSavedListener {
	
	private static final int VERSION = 1;

	private static final String MODEL_TABLE_TITLE = CyActivator.APP_NAME + ".Model." + VERSION;
	
	private static final ColumnDescriptor<Integer> COL_PK         = new ColumnDescriptor<>("ID", Integer.class);
	private static final ColumnDescriptor<Long>    COL_NETWORK_ID = new ColumnDescriptor<>("Network.SUID", Long.class);
	private static final ColumnDescriptor<String>  COL_EM_JSON    = new ColumnDescriptor<>("Model.JSON", String.class);
	
	@Inject private CyNetworkManager networkManager;
	@Inject private CyTableManager tableManager;
	@Inject private CyTableFactory tableFactory;
	@Inject private CyServiceRegistrar serviceRegistrar;
	
	@Inject private Provider<LegacySessionLoader> legacySessionLoaderProvider;
	@Inject private Provider<ControlPanelMediator> controlPanelMediatorProvider;
	@Inject private EnrichmentMapManager emManager;
	
	@Inject private @Headless boolean headless;
	
	
	private static final boolean debug = false;
	
	
	@Override
	public void handleEvent(SessionLoadedEvent event) {
		CySession session = event.getLoadedSession();
		restoreModel(session);
	}
	
	@Override
	public void handleEvent(SessionAboutToBeSavedEvent event) {
		saveModel();
	}
	
	
	public void saveModel() {
		if(debug)
			System.out.println("SessionModelListener.saveModel()");
		
		CyTable table = getOrCreatePrivateModelTable();
		clearTable(table);
		
		int id[] = {0};
		
		Map<Long,EnrichmentMap> maps = emManager.getAllEnrichmentMaps();
		maps.forEach((suid, em) -> {
			CyNetwork network = networkManager.getNetwork(suid);
			if(network != null) { // MKTODO big error if its null
				String json = ModelSerializer.serialize(em);
				CyRow row = table.getRow(id[0]);
				COL_NETWORK_ID.set(row, suid);
				COL_EM_JSON.set(row, json);
				id[0]++;
			}
		});
	}
	
	public void restoreModel() {
		restoreModel(null);
	}
	
	public void restoreModel(CySession session) {
		if(debug)
			System.out.println("SessionModelListener.restoreModel()");
		
		emManager.reset();
		
		boolean sessionHasEM = false;
		if(session != null && LegacySessionLoader.isLegacy(session)) {
			sessionHasEM = true;
			legacySessionLoaderProvider.get().loadSession(session);
		} else {
			sessionHasEM = restoreModelFromTables(session);
		}
		
		if(!headless) {
			ControlPanelMediator controlPanelMediator = controlPanelMediatorProvider.get();
			controlPanelMediator.reset();
			if(sessionHasEM) {
				controlPanelMediator.showControlPanel();
			}
		}
		
		if(debug) {
			System.out.println("Session Restore Finished");
			System.out.println("Enrichment Maps:");
			emManager.getAllEnrichmentMaps().forEach((suid, em) -> {
				System.out.println("suid:" + suid);
				System.out.println("datasets:");
				em.getDataSetList().forEach(dataset -> System.out.println(dataset.getName()));
			});
			System.out.println();
		}
	}
	
	
	private boolean restoreModelFromTables(CySession session) {
		boolean sessionHasEM = false;
		CyTable table = getPrivateTable();
		if(table != null) {
			for(CyRow row : table.getAllRows()) {
				Long suid = COL_NETWORK_ID.get(row);
				String json = COL_EM_JSON.get(row);
				if(suid != null && json != null) {
					CyNetwork network = networkManager.getNetwork(suid);
					if(network != null) {
						EnrichmentMap em = ModelSerializer.deserialize(json);
						if(em != null) {
							em.setServiceRegistrar(serviceRegistrar);
							em.setNetworkID(network.getSUID());
							updateNodeSuids(em, session);
							emManager.registerEnrichmentMap(em);
							sessionHasEM = true;
						}
					}
				}
			}
		}
		return sessionHasEM;
	}
	
	private void updateNodeSuids(EnrichmentMap map, CySession session) {
		for (EMDataSet ds : map.getDataSetList()) {
			Map<String, Long> oldSuids = ds.getNodeSuids();
			Map<String, Long> newSuids = new HashMap<>();
			
			for (String key : oldSuids.keySet()) {
				Long suid = oldSuids.get(key);
				
				if (session != null) {
					// If we are loading from a session file then we need to re-map the ids
					CyNode node = session.getObject(suid, CyNode.class);
					suid = node.getSUID();
				}
				
				newSuids.put(key, suid);
			}
			
			ds.setNodeSuids(newSuids);
		}
	}

	private CyTable getPrivateTable() {
		for(CyTable table : tableManager.getAllTables(true)) {
			if(MODEL_TABLE_TITLE.equals(table.getTitle()) && validateTableStructure(table)) {
				return table;
			}
		}
		return null;
	}
	
	private CyTable deleteRedundantTables() {
		for(CyTable table : tableManager.getAllTables(true)) {
			if(MODEL_TABLE_TITLE.equals(table.getTitle()) && !validateTableStructure(table) && table.getMutability() == Mutability.MUTABLE) {
				if(debug) {
					System.out.println("Deleting table: " + table.getTitle());
				}
				tableManager.deleteTable(table.getSUID());
			}
		}
		return null;
	}
	
	private static boolean validateTableStructure(CyTable table) {
		return hasColumn(table, COL_PK) 
			&& hasColumn(table, COL_NETWORK_ID)
			&& hasColumn(table, COL_EM_JSON)
			&& table.getColumn(COL_PK.getBaseName()).isPrimaryKey();
	}
	
	private CyTable createPrivateTable() {
		CyTable table = tableFactory.createTable(MODEL_TABLE_TITLE, COL_PK.getBaseName(), COL_PK.getType(), false, false);
		COL_NETWORK_ID.createColumn(table);
		COL_EM_JSON.createColumn(table);
		table.setPublic(false);
		tableManager.addTable(table);
		return table;
	}
	
	
	private static boolean hasColumn(CyTable table, ColumnDescriptor<?> colDesc) {
		CyColumn col = table.getColumn(colDesc.getBaseName());
		return col.getName().equals(colDesc.getBaseName()) && col.getType().equals(colDesc.getType());
	}
	
	private CyTable getOrCreatePrivateModelTable() {
		deleteRedundantTables();
		CyTable table = getPrivateTable();
		if(table == null)
			table = createPrivateTable();
		return table;
	}
	
	private void clearTable(CyTable table) {
		List<Object> rowKeys = new ArrayList<>();
		CyColumn keyColumn = table.getPrimaryKey();
		for(CyRow row : table.getAllRows()) {
			Object key = row.get(keyColumn.getName(), keyColumn.getType());
			rowKeys.add(key);
		}
		table.deleteRows(rowKeys);
	}
	
	
	@SuppressWarnings("serial")
	public List<AbstractCyAction> getDebugActions() {
		return Arrays.asList(
			new AbstractCyAction(CyActivator.APP_NAME + ": Save Model") {
				{setPreferredMenu("Apps");}
				public void actionPerformed(ActionEvent e) {
					saveModel();
				}
			}
		);
	}
}
