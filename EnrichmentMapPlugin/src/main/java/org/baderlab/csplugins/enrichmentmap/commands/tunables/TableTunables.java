package org.baderlab.csplugins.enrichmentmap.commands.tunables;

import org.baderlab.csplugins.enrichmentmap.model.TableExpressionParameters;
import org.baderlab.csplugins.enrichmentmap.model.TableParameters;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.work.Tunable;

import com.google.inject.Inject;

public class TableTunables {

	@Tunable(description="Specifies a table by table name. ", 
			longDescription = "If the prefix ```SUID:``` is used, the table corresponding the SUID will be returned. Example: \"galFiltered.sif default node\"")
	public String table;

	@Tunable(required=true, description="Name of column that contains the names of the gene sets.")
	public String nameColumn;
	
	@Tunable(required=true, description="Name of column that contains the list of genes.")
	public String genesColumn;
	
	@Tunable(description="Name of column that contains p values. At least one of 'pvalueColumn' or 'qvalueColumn' must be provided.")
	public String pvalueColumn;
	
	@Tunable(description="Name of column that contains q values. At least one of 'pvalueColumn' or 'qvalueColumn' must be provided.")
	public String qvalueColumn;
	
	@Tunable(description="Name of column that contains NES values (optional).")
	public String nesColumn;
	
	@Tunable(description="Name of column that contains the gene set description (optional).")
	public String descriptionColumn;

	
	@Tunable(description="Table to be used for loading expression data (optional). May be the same as the enrichment data table.")
	public String exprTable;
	
	@Tunable(description="Name of column in expression data table that contains gene names (required if 'exprTable' is provided).")
	public String exprGeneNameColumn;
	
	@Tunable(description="Name of column in expression data table that contains gene descriptions (optional).")
	public String exprDescriptionColumn;
	
	@Tunable(description="Comma separated list of column names in the expression data table that contain the numeric expression values (required if 'exprTable' is provided).")
	public String exprValueColumns;
	
	
	@Inject private CyTableManager tableManager;
	
	

	private CyTable getTable(String table) { 
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
		CyTable table = getTable(this.table);
		if(table == null)
			throw new IllegalArgumentException("Table '" + this.table + "' is invalid.");
		
		if(pvalueColumn == null && qvalueColumn == null)
			throw new IllegalArgumentException("At least one of 'pvalueColumn' or 'qvalueColumn' must be provided.");
			
		validateColumn(table, "nameColumn", nameColumn, false, String.class, true);
		validateColumn(table, "genesColumn", genesColumn, true, String.class, true);
		validateColumn(table, "descriptionColumn", descriptionColumn, false, String.class, false);
		validateColumn(table, "pvalueColumn", pvalueColumn, false, Double.class, false);
		validateColumn(table, "qvalueColumn", qvalueColumn, false, Double.class, false);
		validateColumn(table, "nesColumn", nesColumn, false, Double.class, false);
		
		return new TableParameters(table, nameColumn, genesColumn, pvalueColumn, qvalueColumn, nesColumn, descriptionColumn, null);
	}
	
	
	public TableExpressionParameters getTableExpressionParameters() throws IllegalArgumentException {
		if(this.exprTable == null)
			return null;
		
		CyTable table = getTable(this.exprTable);
		if(table == null)
			throw new IllegalArgumentException("Expression table '" + this.exprTable + "' is invalid.");
		
		validateColumn(table, "exprGeneNameColumn", exprGeneNameColumn, false, String.class, true);
		validateColumn(table, "exprDescriptionColumn", exprDescriptionColumn, false, String.class, false);
		
		if(exprValueColumns == null || exprValueColumns.isEmpty())
			throw new IllegalArgumentException("'exprValueColumns' is required when loading expression data");
		
		String[] valueColumns = exprValueColumns.split(",");
		for(String col : valueColumns) {
			validateColumn(table, "exprValueColumns", col, false, Double.class, true);
		}
		
		return new TableExpressionParameters(table, exprGeneNameColumn, exprDescriptionColumn, valueColumns);
	}
	
	
	private static void validateColumn(CyTable table, String argName, String name, boolean isList, Class<?> type, boolean required) throws IllegalArgumentException {
		if(name == null && required)
			throw new IllegalArgumentException("Argument '" + argName + "' is required.");
		if(name == null)
			return;
		
		CyColumn column = table.getColumn(name);
		if(column == null)
			throw new IllegalArgumentException("Column '" + name + "' is invalid for argument '" + argName + "'.");
		
		// if the column exists it must still be of the required type
		if((isList && !column.getListElementType().equals(type)) || (!isList && !column.getType().equals(type)))
			throw new IllegalArgumentException("Column '" + name + "' must be of type '" + (isList ? "list of " + type : type) + "' for argument '" + argName + "'");
	}
	
	
}

