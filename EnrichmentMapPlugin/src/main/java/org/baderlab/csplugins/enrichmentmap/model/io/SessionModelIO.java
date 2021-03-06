package org.baderlab.csplugins.enrichmentmap.model.io;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

import org.baderlab.csplugins.enrichmentmap.ApplicationModule.Headless;
import org.baderlab.csplugins.enrichmentmap.CyActivator;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EMSignatureDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.style.ColumnDescriptor;
import org.baderlab.csplugins.enrichmentmap.view.control.ControlPanelMediator;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapMediator;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
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

import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;


/**
 * Converts each EnrichmentMap object into JSON format and stores it in a private table.
 * This is designed to support App reloading like when the App is updated.
 */
@Singleton
public class SessionModelIO {
	
	/**
	 * WARNING
	 * Upgrading the version means the following:
	 * - Make sure session files saved with older versions of EM are still supported.
	 * - Change the version check in hasTablesInvalidVersion().
	 * - Make sure LegacySessionLoader still works.
	 * - Write an integration test.
	 */
	public static final int VERSION = 1;
	
	private static final String MODEL_TABLE_PREFIX = CyActivator.APP_NAME + ".Model.";
	private static final String MODEL_TABLE_TITLE =  MODEL_TABLE_PREFIX + VERSION;
	
	private static final ColumnDescriptor<Integer> COL_PK         = new ColumnDescriptor<>("ID", Integer.class);
	private static final ColumnDescriptor<Long>    COL_NETWORK_ID = new ColumnDescriptor<>("Network.SUID", Long.class);
	private static final ColumnDescriptor<String>  COL_EM_JSON    = new ColumnDescriptor<>("Model.JSON", String.class);
	
	@Inject private CyNetworkManager networkManager;
	@Inject private CyTableManager tableManager;
	@Inject private CyTableFactory tableFactory;
	@Inject private CyServiceRegistrar serviceRegistrar;
	
	@Inject private Provider<LegacySessionLoader> legacySessionLoaderProvider;
	@Inject private Provider<ControlPanelMediator> controlPanelMediatorProvider;
	@Inject private Provider<HeatMapMediator> heatMapMediatorProvider;
	@Inject private EnrichmentMapManager emManager;
	
	@Inject private @Headless boolean headless;
	
	
	private static final boolean debug = false;
	
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
	
	public void restoreModel(CySession session) {
		if(debug)
			System.out.println("SessionModelListener.restoreModel()");
		
		emManager.reset();
		
		boolean sessionHasEM;
		if(session != null && LegacySessionLoader.isLegacy(session)) {
			sessionHasEM = true;
			legacySessionLoaderProvider.get().loadSession(session);
		} else {
			sessionHasEM = restoreModelFromTables(session);
		}
		
		if(!headless) {
			ControlPanelMediator controlPanelMediator = controlPanelMediatorProvider.get();
			HeatMapMediator heatMapMediator = heatMapMediatorProvider.get();
			ListenableFuture<Void> future = controlPanelMediator.reset();
			future.addListener(() -> {
				heatMapMediator.reset();
				if(sessionHasEM) {
					controlPanelMediator.showControlPanel();
					heatMapMediator.showHeatMapPanel();
				}
			}, ForkJoinPool.commonPool());
		}
		
		if(debug) {
			System.out.println("Session Restore Finished");
			System.out.println("Enrichment Maps:");
			emManager.getAllEnrichmentMaps().forEach((suid, em) -> {
				System.out.println("suid:" + suid);
				System.out.println("datasets:");
				em.getDataSetList().forEach(ds -> System.out.println(ds.getName()));
				em.getSignatureSetList().forEach(ds -> System.out.println(ds.getName()));
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
							updateSuids(em, session);
							emManager.registerEnrichmentMap(em);
							sessionHasEM = true;
							// MKTODO set the attribute to null to save space
						}
					}
				}
			}
		}
		return sessionHasEM;
	}
	
	private static Set<Long> mapSuids(Set<Long> oldSuids, CySession session, Class<? extends CyIdentifiable> type) {
		Set<Long> newSuids = new HashSet<>();
		
		for (Long oldSuid : oldSuids) {
			if (session != null) {
				// If we are loading from a session file then we need to re-map the IDs.
				CyIdentifiable obj = session.getObject(oldSuid, type);
				if(obj == null) {
					// Case where nodes/edges might have been deleted.
					// We have to do the check here because older session files might not be saved properly.
					System.err.println("EnrichmentMap: SUID not found: " + oldSuid);
				} else {
					Long suid = obj.getSUID();
					newSuids.add(suid);
				}
			} else {
				newSuids.add(oldSuid);
			}
		}
		return newSuids;
	}
	
	private void updateSuids(EnrichmentMap map, CySession session) {
		for(EMDataSet ds : map.getDataSetList()) {
			ds.setNodeSuids(mapSuids(ds.getNodeSuids(), session, CyNode.class));
			ds.setEdgeSuids(mapSuids(ds.getEdgeSuids(), session, CyEdge.class));
		}
 		for(EMSignatureDataSet ds : map.getSignatureSetList()) {
 			ds.setNodeSuids(mapSuids(ds.getNodeSuids(), session, CyNode.class));
			ds.setEdgeSuids(mapSuids(ds.getEdgeSuids(), session, CyEdge.class));
 		}
 		map.setAssociatedNetworkIDs(mapSuids(map.getAssociatedNetworkIDs(), session, CyNetwork.class));
	}

	private CyTable getPrivateTable() {
		for(CyTable table : tableManager.getAllTables(true)) {
			if(MODEL_TABLE_TITLE.equals(table.getTitle()) && validateTableStructure(table)) {
				return table;
			}
		}
		return null;
	}
	
	public boolean hasTablesInvalidVersion() {
		// This simple check only works because the current version is 1.
		for(CyTable table : tableManager.getAllTables(true)) {
			String title = table.getTitle();
			if(title.startsWith(MODEL_TABLE_PREFIX) && !title.equals(MODEL_TABLE_TITLE)) {
				return true;
			}
		}
		return false;
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
	
}
