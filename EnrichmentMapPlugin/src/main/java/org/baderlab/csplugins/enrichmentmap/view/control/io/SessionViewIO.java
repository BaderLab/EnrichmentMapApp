package org.baderlab.csplugins.enrichmentmap.view.control.io;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.baderlab.csplugins.enrichmentmap.ApplicationModule.Headless;
import org.baderlab.csplugins.enrichmentmap.CyActivator;
import org.baderlab.csplugins.enrichmentmap.style.ColumnDescriptor;
import org.baderlab.csplugins.enrichmentmap.view.control.ControlPanelMediator;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTable.Mutability;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.session.CySession;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;


/**
 * Converts each UI parameters into JSON format and stores it in a private table.
 * This is designed to support App reloading like when the App is updated.
 */
@Singleton
public class SessionViewIO {
	
	private static final int VERSION = 1;

	private static final String TABLE_TITLE = CyActivator.APP_NAME + ".View." + VERSION;
	
	private static final ColumnDescriptor<Integer> COL_PK = new ColumnDescriptor<>("ID", Integer.class);
	private static final ColumnDescriptor<Long> COL_NETWORK_VIEW_ID = new ColumnDescriptor<>("NetworkView.SUID", Long.class);
	private static final ColumnDescriptor<String> COL_VIEW_JSON = new ColumnDescriptor<>("View.JSON", String.class);

	@Inject private CyNetworkViewManager networkViewManager;
	@Inject private CyTableManager tableManager;
	@Inject private CyTableFactory tableFactory;
	
	@Inject private Provider<ControlPanelMediator> controlPanelMediatorProvider;
	
	@Inject private @Headless boolean headless;
	
	public void saveView() {
		CyTable table = getOrCreatePrivateModelTable();
		clearTable(table);
		
		int id[] = {0};
		
		Map<Long, ViewParams> maps = controlPanelMediatorProvider.get().getAllViewParams();
		
		maps.forEach((suid, params) -> {
			String json = ViewSerializer.serialize(params);
			CyRow row = table.getRow(id[0]);
			COL_NETWORK_VIEW_ID.set(row, suid);
			COL_VIEW_JSON.set(row, json);
			id[0]++;
		});
	}
	
	public void restoreView() {
		restoreView(null);
	}
	
	public void restoreView(CySession session) {
		if (!headless) {
			CyTable table = getPrivateTable();
			
			if (table != null) {
				for (CyRow row : table.getAllRows()) {
					Long suid = COL_NETWORK_VIEW_ID.get(row);
					String json = COL_VIEW_JSON.get(row);
					
					if (suid != null && json != null) {
						CyNetworkView netView = getNetworkView(suid);
						
						if (netView != null) {
							ViewParams params = ViewSerializer.deserialize(json);
							
							if (params != null) {
								params.setNetworkViewID(netView.getSUID());
								controlPanelMediatorProvider.get().reset(params);
							}
						}
					}
				}
			}
		}
	}
	
	private CyNetworkView getNetworkView(long suid) {
		Set<CyNetworkView> set = networkViewManager.getNetworkViewSet();
		
		for (CyNetworkView netView : set) {
			if (netView.getSUID() == suid)
				return netView;
		}
		
		return null;
	}
	
	private CyTable getPrivateTable() {
		for (CyTable table : tableManager.getAllTables(true)) {
			if (TABLE_TITLE.equals(table.getTitle()) && validateTableStructure(table))
				return table;
		}
		
		return null;
	}
	
	private CyTable deleteRedundantTables() {
		for (CyTable table : tableManager.getAllTables(true)) {
			if (TABLE_TITLE.equals(table.getTitle()) && !validateTableStructure(table)
					&& table.getMutability() == Mutability.MUTABLE)
				tableManager.deleteTable(table.getSUID());
		}
		
		return null;
	}
	
	private static boolean validateTableStructure(CyTable table) {
		return hasColumn(table, COL_PK) 
			&& hasColumn(table, COL_NETWORK_VIEW_ID)
			&& hasColumn(table, COL_VIEW_JSON)
			&& table.getColumn(COL_PK.getBaseName()).isPrimaryKey();
	}
	
	private CyTable createPrivateTable() {
		CyTable table = tableFactory.createTable(TABLE_TITLE, COL_PK.getBaseName(), COL_PK.getType(), false, false);
		COL_NETWORK_VIEW_ID.createColumn(table);
		COL_VIEW_JSON.createColumn(table);
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
		
		if (table == null)
			table = createPrivateTable();
		
		return table;
	}
	
	private void clearTable(CyTable table) {
		List<Object> rowKeys = new ArrayList<>();
		CyColumn keyColumn = table.getPrimaryKey();
		
		for (CyRow row : table.getAllRows()) {
			Object key = row.get(keyColumn.getName(), keyColumn.getType());
			rowKeys.add(key);
		}
		
		table.deleteRows(rowKeys);
	}
}
