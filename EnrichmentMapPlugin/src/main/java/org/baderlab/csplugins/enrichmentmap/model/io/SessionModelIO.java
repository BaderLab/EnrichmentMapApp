package org.baderlab.csplugins.enrichmentmap.model.io;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.baderlab.csplugins.enrichmentmap.ApplicationModule.Headless;
import org.baderlab.csplugins.enrichmentmap.CyActivator;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EMSignatureDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.style.ColumnDescriptor;
import org.baderlab.csplugins.enrichmentmap.view.control.ControlPanelMediator;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapMediator;
import org.cytoscape.application.CyUserLog;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	private static final Logger log = LoggerFactory.getLogger(CyUserLog.NAME);
	
	private static final String SESSION_FILE_NAME_FORMAT  = "EM_%d_";
	private static final String SESSION_FILE_NAME_PATTERN = "EM_(\\d+)_";
	
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
	
	public List<File> saveModel() {
		if(debug)
			System.out.println("SessionModelListener.saveModel()");
		
		CyTable table = getOrCreatePrivateModelTable();
		clearTable(table);
		
		int id[] = {0};
		List<File> files = new ArrayList<>(); // Ideally this will remain empty.
		
		Map<Long,EnrichmentMap> maps = emManager.getAllEnrichmentMaps();
		maps.forEach((suid, em) -> {
			CyNetwork network = networkManager.getNetwork(suid);
			if(network != null) { // MKTODO big error if its null
				try {
					CyRow row = table.getRow(id[0]);
					serializeToTableRow(em, suid, row);
					id[0]++;
				} catch(OutOfMemoryError oome) {
					// https://github.com/BaderLab/EnrichmentMapApp/issues/482
					// The size of the serialized data is to big to fit into a String.
					// No choice but to do it the old way... serialize to a file in the session.
					log.warn("EnrichmentMap: JSON data to large for a String object. Saving to session file instead of table.");
					try {
						File file = serializeToFile(em, suid);
						files.add(file);
					} catch(IOException ioe) {
						ioe.printStackTrace();
						log.error("EnrichmentMap: Could not save JSON data to session.", ioe);
					}
				}
			}
		});
		
		return files;
	}
	
	
	/**
	 * Serializing the model to a CyTable has the advantage that if the EM App is restarted
	 * for any reason then the model is automatically saved/reloaded.
	 * This can happen if the App is updated from the App store while Cytoscape is running,
	 * or if the app is recompiled and reloaded during development. It does however have one
	 * big limitation, the entire model has to fit into one String object, which
	 * isn't always possible.
	 */
	private void serializeToTableRow(EnrichmentMap em, Long suid, CyRow row) throws OutOfMemoryError {
		String json = ModelSerializer.serialize(em);
		COL_NETWORK_ID.set(row, suid);
		COL_EM_JSON.set(row, json);
	}
	
	/**
	 * Serializing the model to a file, which then gets zipped up into the session.
	 * This has no limits on the size of the model that can be serialized, but it donesn't
	 * have the other advantages that serializing to a CyTable has. This is a fallback
	 * for the rare times when serializing to a table doesn't work.
	 * 
	 * @since 3.3.4 Restoring the model from a file in the session won't work 
	 * for versions of EM prior to 3.3.4.
	 */
	private File serializeToFile(EnrichmentMap em, Long suid) throws IOException {
		File file = File.createTempFile(String.format(SESSION_FILE_NAME_FORMAT, suid), ".json");
		file.deleteOnExit();
		ModelSerializer.serialize(em, file);
		return file;
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
			sessionHasEM = restoreModelFromSession(session);
		}
		
		if(!headless) {
			resetUI(sessionHasEM);
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
	
	private void resetUI(boolean showPanels) {
		// Clear the UI state from any previous EM session that was was loaded.
		ControlPanelMediator controlPanelMediator = controlPanelMediatorProvider.get();
		HeatMapMediator heatMapMediator = heatMapMediatorProvider.get();
		ListenableFuture<Void> future = controlPanelMediator.reset();
		future.addListener(() -> {
			heatMapMediator.reset();
			if(showPanels) {
				controlPanelMediator.showControlPanel();
				heatMapMediator.showHeatMapPanel();
			}
		}, ForkJoinPool.commonPool());
	}
	
	
	private boolean restoreModelFromSession(CySession session) {
		boolean sessionHasEM = false;
		sessionHasEM |= restoreModelFromTables(session);
		sessionHasEM |= restoreModelFromFiles(session);
		return sessionHasEM;
	}
	
	
	private boolean restoreModelFromTables(CySession session) {
		CyTable table = getPrivateTable();
		if(table == null)
			return false;
		
		boolean sessionHasEM = false;
		for(CyRow row : table.getAllRows()) {
			Long suid = COL_NETWORK_ID.get(row); // suid is automatically mapped because the column name ends with .SUID
			String json = COL_EM_JSON.get(row);
			if(suid != null && json != null) {
				CyNetwork network = networkManager.getNetwork(suid);
				if(network != null) {
					EnrichmentMap em = ModelSerializer.deserialize(json);
					if(em != null) {
						registerEnrichmentMap(em, network, session);
						sessionHasEM = true;
					}
				}
			}
		}
		return sessionHasEM;
	}
	
	
	private boolean restoreModelFromFiles(CySession session) {
		if(session == null)
			return false;
		var appFileListMap = session.getAppFileListMap();
		var files = appFileListMap.get(CyActivator.SESSION_DATA_FOLDER);
		if(files == null)
			return false;
		
		boolean sessionHasEM = false;
		
		for(var file : files) {
			Long networkSuid = getSuid(file, session);
			if(networkSuid != null) {
				CyNetwork network = networkManager.getNetwork(networkSuid);
				if(network != null) {
					try(var reader = new FileReader(file)) {
						EnrichmentMap em = ModelSerializer.deserialize(file);
						if(em != null) {
							registerEnrichmentMap(em, network, session);
							sessionHasEM = true;
						}
					} catch(IOException ioe) {
						ioe.printStackTrace();
						log.error("EnrichmentMap: Could not load JSON data from session.", ioe);
					}
				}
			}
		}
		return sessionHasEM;
	}
	
	
	private Long getSuid(File file, CySession session) {
		String fileName = file.getName();
		Pattern p = Pattern.compile(SESSION_FILE_NAME_PATTERN);
		Matcher m = p.matcher(fileName);
		if(m.find()) {
			String suidStr = m.group(1);
			long suid;
			try {
				suid = Long.parseLong(suidStr);
			} catch(NumberFormatException e) {
				return null;
			}
			
			return mapSuid(suid, session, CyNetwork.class);
		}
		return null;
	}
	
	
	private void registerEnrichmentMap(EnrichmentMap em, CyNetwork network, CySession session) {
		em.setServiceRegistrar(serviceRegistrar);
		em.setNetworkID(network.getSUID());
		updateSuids(em, session);
		emManager.registerEnrichmentMap(em);
	}
	
	private static Set<Long> mapSuids(Set<Long> oldSuids, CySession session, Class<? extends CyIdentifiable> type) {
		Set<Long> newSuids = new HashSet<>();
		for(Long oldSuid : oldSuids) {
			Long newSuid = mapSuid(oldSuid, session, type);
			if(newSuid != null) {
				newSuids.add(newSuid);
			}
		}
		return newSuids;
	}
	
	private static Long mapSuid(Long oldSuid, CySession session, Class<? extends CyIdentifiable> type) {
		if (session != null) {
			// If we are loading from a session file then we need to re-map the IDs.
			CyIdentifiable obj = session.getObject(oldSuid, type);
			if(obj == null) {
				// Case where nodes/edges might have been deleted.
				// We have to do the check here because older session files might not be saved properly.
				System.err.println("EnrichmentMap: SUID not found: " + oldSuid);
				return null;
			} else {
				Long suid = obj.getSUID();
				return suid;
			}
		} else {
			return oldSuid;
		}
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
