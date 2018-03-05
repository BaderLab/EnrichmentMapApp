package org.baderlab.csplugins.enrichmentmap.commands;

import org.baderlab.csplugins.enrichmentmap.model.TableParameters;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.work.Tunable;

import com.google.inject.Inject;

public class TableTunables {

	// TODO change tooltip to longDescription and set dependency on 3.6
	@Tunable(description="Table", context="nogui", tooltip="Specifies a table by table name. If the prefix ```SUID:``` is used, the table corresponding the SUID will be returned. Example: \"galFiltered.sif default node\"")
	public String table;

	@Tunable(required=true)
	public String nameColumn;
	
	@Tunable
	public String descriptionColumn;
	
	@Tunable(required=true)
	public String genesColumn;
	
	@Tunable(required=true)
	public String pvalueColumn;

	
	@Inject private CyTableManager tableManager;
	
	
	public String getTableString() {
		return table;
	}

	public CyTable getTable() { 
		if(table == null)
			return null;

		if(table.toLowerCase().startsWith("suid:")) {
			String[] tokens = table.split(":");
			return tableManager.getTable(Long.parseLong(tokens[1].trim()));
		} else {
			for(CyTable t: tableManager.getAllTables(true)) {
				if(t.getTitle().equalsIgnoreCase(table)) {
					return t;
				}
			}
		}
		return null;
	}
	
	public TableParameters getTableParameters() throws IllegalArgumentException {
		return null;
	}
}

