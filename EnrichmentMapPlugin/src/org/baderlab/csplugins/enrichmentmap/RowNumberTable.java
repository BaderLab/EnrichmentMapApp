package org.baderlab.csplugins.enrichmentmap;
import javax.swing.JTable;
import javax.swing.table.TableColumn;


	//public class LineNumberTable 
public class RowNumberTable extends JTable{
	
	
	private JTable mainTable;

	

	//public RowNumberTable(JTable table1) {
		// TODO Auto-generated constructor stub
	//}

	public RowNumberTable(JTable table1) {
		// TODO Auto-generated constructor stub
		super();
		mainTable = table1;
		setAutoCreateColumnsFromModel( false );
		setModel( mainTable.getModel() );
		setSelectionModel( mainTable.getSelectionModel() );
		setAutoscrolls( false );

		addColumn( new TableColumn() );
		getColumnModel().getColumn(0).setCellRenderer(
		mainTable.getTableHeader().getDefaultRenderer() );
		getColumnModel().getColumn(0).setPreferredWidth(50);
		setPreferredScrollableViewportSize(getPreferredSize());
	}

	public boolean isCellEditable(int row, int column)
	{
	return false;
	}

	public Object getValueAt(int row, int column)
	{
	return new Integer(row + 1);
	}

	public int getRowHeight(int row)
	{
	return mainTable.getRowHeight();
	}

}
