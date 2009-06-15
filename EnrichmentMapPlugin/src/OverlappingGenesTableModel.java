
import javax.swing.table.AbstractTableModel;

/**
 * Created by
 * User: risserlin
 * Date: Jan 30, 2009
 * Time: 10:56:40 AM
 */
public class OverlappingGenesTableModel  extends AbstractTableModel {

                private Object[] columnNames;
                private Object[][] data;


                public OverlappingGenesTableModel(Object[] columnNames,Object[][] data){
                    super();
                    this.columnNames = columnNames;
                    this.data=data;

                }

                public int getColumnCount() {
                    return columnNames.length;

                }

                public int getRowCount() {
                    return data.length;
                }

                public String getColumnName(int col) {


                     return (String)columnNames[col];


                }

                public Object getValueAt(int row, int col) {



                    return data[row][col];
                }

                /*
                 * JTable uses this method to determine the default renderer/
                 * editor for each cell.  If we didn't implement this method,
                 * then the last column would contain text ("true"/"false"),
                 * rather than a check box.
                 */
                public Class getColumnClass(int c) {


                    return getValueAt(0, c).getClass();
                }

                /*
                 * Don't need to implement this method unless your table's
                 * editable.
                 */
                public boolean isCellEditable(int row, int col) {
                    return false;

                }


                /*
                * Don't need to implement this method unless your table's
                * data can change.
                */
                public void setValueAt(Object value, int row, int col) {


                    data[row][col] = value;


                    fireTableCellUpdated(row, col);


                }

    }

