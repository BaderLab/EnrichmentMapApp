package org.baderlab.csplugins.enrichmentmap.view.control;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil.makeSmall;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.baderlab.csplugins.enrichmentmap.AfterInjection;
import org.baderlab.csplugins.enrichmentmap.PropertyManager;
import org.baderlab.csplugins.enrichmentmap.model.AbstractDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EMSignatureDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder;
import org.baderlab.csplugins.enrichmentmap.view.postanalysis.PADialogMediator;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
 
@SuppressWarnings("serial")
public class DataSetSelector extends JPanel {
	
	public static final String PROP_CHECKED_DATA_SETS  = "checkedData";
	private static final String[] HEARDER_NAMES = new String[]{ "", "", "Name", "" };
	
	private static final int SELECTED_COL_IDX = 0;
	private static final int TYPE_COL_IDX = 1;
	private static final int NAME_COL_IDX = 2;
	private static final int GENES_COL_IDX = 3;
	
	private static final Border CELL_BORDER = new EmptyBorder(0, 0, 0, 0);
	
	@Inject private PropertyManager propertyManager;
	@Inject private IconManager iconManager;
	
	
	private JTable table;
	private JScrollPane tableScrollPane;
	private JMenuItem addMenuItem;
	private JMenuItem colorMenuItem;
	private JMenuItem selectAllMenuItem;
	private JMenuItem selectNoneMenuItem;
	private JMenuItem selectNodesMenuItem;
	private JMenuItem deleteSignatureMenuItem;
	private JMenuItem syncPropMenuItem;
	private JButton optionButton;
	
	private final EnrichmentMap map;
	
	private final Set<AbstractDataSet> items = new LinkedHashSet<>();
	private final Map<AbstractDataSet, Boolean> checkedItems = new HashMap<>();
	private List<Integer> previousSelectedRows;
	
	
	public static interface Factory {
		DataSetSelector create(EnrichmentMap map);
	}
	
	@AssistedInject
	public DataSetSelector(@Assisted EnrichmentMap map) {
		this.map = map;
		// init() called using @AfterInjection
	}

	public void update() {
		Map<AbstractDataSet, Boolean> oldCheckedItems = new HashMap<>(checkedItems);
		items.clear();
		checkedItems.clear();
		
		List<AbstractDataSet> newItems = new ArrayList<>();
		newItems.addAll(map.getDataSetList());
		newItems.addAll(map.getSignatureSetList());
		
		if (newItems != null) {
			for (AbstractDataSet ds : newItems) {
				items.add(ds);
				
				boolean selected = !oldCheckedItems.containsKey(ds) // New items are selected by default!
						|| oldCheckedItems.get(ds) == Boolean.TRUE;
				checkedItems.put(ds, selected);
			}
		}
		
		updateTable();
		updateSelectionButtons();
	}

	public Set<AbstractDataSet> getAllItems() {
		return new HashSet<>(items);
	}
	
	public Set<AbstractDataSet> getCheckedItems() {
		Set<AbstractDataSet> set = new HashSet<>();
		
		checkedItems.forEach((ds, checked) -> {
			if (checked == Boolean.TRUE)
				set.add(ds);
		});
		
		return set;
	}
	
	public void setCheckedItems(Set<AbstractDataSet> newValue) {
		final Set<AbstractDataSet> oldValue = getCheckedItems();
		checkedItems.clear();
		final int rowCount = getTable().getRowCount();
		
		for (int i = 0; i < rowCount; i++) {
			AbstractDataSet ds = (AbstractDataSet) getTable().getValueAt(i, NAME_COL_IDX);
			boolean checked = newValue != null && newValue.contains(ds);
			getTable().setValueAt(checked, i, SELECTED_COL_IDX);
			
			checkedItems.put(ds, checked);
		}
		
		getTable().repaint();
		updateSelectionButtons();
		firePropertyChange(PROP_CHECKED_DATA_SETS, oldValue, getCheckedItems());
	}
	
	public void setHighlightedDataSets(Collection<AbstractDataSet> dataSets) {
		ListSelectionModel selectionModel = getTable().getSelectionModel();
		selectionModel.clearSelection();
		
		final int rowCount = getTable().getRowCount();
		for (int i = 0; i < rowCount; i++) {
			AbstractDataSet ds = (AbstractDataSet) getTable().getValueAt(i, NAME_COL_IDX);
			if(dataSets != null && dataSets.contains(ds)) {
				selectionModel.addSelectionInterval(i, i);
			}
		}
	}
	
	public Set<AbstractDataSet> getSelectedItems() {
		Set<AbstractDataSet> set = new HashSet<>();
		int[] selectedRows = getTable().getSelectedRows();
		
		for (int r : selectedRows) {
			AbstractDataSet ds = (AbstractDataSet) table.getModel().getValueAt(r, NAME_COL_IDX);
			set.add(ds);
		}
		
		return set;
	}
	
	@AfterInjection
	private void init() {
		JLabel titleLabel = new JLabel("Data Sets:");
		makeSmall(titleLabel);
		
		final int rh = getTable().getRowHeight() + 2;
		
		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
   		layout.setHorizontalGroup(layout.createParallelGroup(CENTER, true)
   				.addGroup(layout.createSequentialGroup()
						.addComponent(titleLabel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(getOptionsButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   				)
				.addComponent(getTableScrollPane(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
   		);
   		layout.setVerticalGroup(layout.createSequentialGroup()
   				.addGroup(layout.createParallelGroup(CENTER, false)
						.addComponent(titleLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(getOptionsButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   				)
   				.addComponent(getTableScrollPane(), rh * 2, rh * 3, Short.MAX_VALUE)
   		);
		
		if (isAquaLAF())
			setOpaque(false);
	}
	
	private void updateTable() {
		final Object[][] data = new Object[items.size()][HEARDER_NAMES.length];
		int i = 0;
		
		for (AbstractDataSet ds : items) {
			data[i][SELECTED_COL_IDX] = checkedItems.get(ds);
			data[i][TYPE_COL_IDX] = ds;
			data[i][NAME_COL_IDX] = ds;
			data[i][GENES_COL_IDX] = ds.getGeneSetsOfInterest().size();
			i++;
		}
		
		final DefaultTableModel model = new DefaultTableModel(data, HEARDER_NAMES) {
			@Override
			public boolean isCellEditable(int row, int column) {
				// TODO Allow renaming?
				return false;
			}
		};
		getTable().setModel(model);
		
		JCheckBox tmpField = new JCheckBox();
		makeSmall(tmpField);
		
		getTable().getColumnModel().getColumn(TYPE_COL_IDX).setMaxWidth(16);
		getTable().getColumnModel().getColumn(SELECTED_COL_IDX).setMaxWidth(tmpField.getPreferredSize().width);
		getTable().getColumnModel().getColumn(GENES_COL_IDX).setMaxWidth(48);
		
		getTable().getColumnModel().getColumn(TYPE_COL_IDX).setResizable(false);
		getTable().getColumnModel().getColumn(SELECTED_COL_IDX).setResizable(false);
	}
	
	private void updateSelectionButtons() {
		final int rowCount = getTable().getRowCount();
		TableModel model = getTable().getModel();
		boolean hasUnchecked = false;
		boolean hasChecked = false;
		
		for (int i = 0; i < rowCount; i++) {
			final boolean checked = (boolean) model.getValueAt(i, SELECTED_COL_IDX);
			
			if (!hasUnchecked)
				hasUnchecked = !checked;
			if (!hasChecked)
				hasChecked = checked;
			if (hasUnchecked && hasChecked)
				break;
		}
		
		getSelectAllMenuItem().setEnabled(hasUnchecked);
		getSelectNoneMenuItem().setEnabled(hasChecked);
	}
	
	JTable getTable() {
		if (table == null) {
			final DefaultSelectorTableCellRenderer defRenderer = new DefaultSelectorTableCellRenderer();
			final CheckBoxTableCellRenderer checkBoxRenderer = new CheckBoxTableCellRenderer();
			
			table = new JTable(new DefaultTableModel(HEARDER_NAMES, 0)) {
				@Override
				public TableCellRenderer getCellRenderer(int row, int column) {
					if (column == SELECTED_COL_IDX) return checkBoxRenderer;
					return defRenderer;
				}
			};
			table.setTableHeader(null);
			table.setShowGrid(false);
			
			JTextField tmpField = new JTextField();
			makeSmall(tmpField);	
			
			table.setRowHeight(Math.max(table.getRowHeight(), tmpField.getPreferredSize().height - 4));
			table.setIntercellSpacing(new Dimension(0, 1));
			
			table.getSelectionModel().addListSelectionListener(e -> {
				if (!e.getValueIsAdjusting()) {
					// Workaround for preventing a click on the check-box in a selected row
					// from changing the selection when multiple table rows are already selected
					if (table.getSelectedRowCount() > 0)
						previousSelectedRows = Arrays.stream(table.getSelectedRows()).boxed().collect(Collectors.toList());
				}
			});
			
			table.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					final int row = table.rowAtPoint(e.getPoint());
					final boolean isMac = LookAndFeelUtil.isMac();
					
					// COMMAND button down on MacOS (or CONTROL button down on another OS)?
					if ((isMac && e.isMetaDown()) || (!isMac && e.isControlDown())) {
						// Right-click must select the row, if not selected already,
						// because the context menu (activated by right-click) acts on the selected rows
						if (!table.isRowSelected(row))
							table.setRowSelectionInterval(row, row);
						
						return;
					}
					
					if (e.isShiftDown())
						return; // Ignore!
					
				    final int col = table.columnAtPoint(e.getPoint());
				    
					if (col == SELECTED_COL_IDX) {
						// Restore previous multiple-row selection first
					    if (previousSelectedRows != null && previousSelectedRows.contains(row)) {
					    	for (int i : previousSelectedRows)
					    		table.addRowSelectionInterval(i, i);
					    }
						
						toggleChecked(row);
					}
				}
			});
		}
		
		return table;
	}
	
	private JScrollPane getTableScrollPane() {
		if (tableScrollPane == null) {
			tableScrollPane = new JScrollPane();
			tableScrollPane.setViewportView(getTable());
			
			final Color bg = UIManager.getColor("Table.background");
			tableScrollPane.setBackground(bg);
			tableScrollPane.getViewport().setBackground(bg);
			
			tableScrollPane.getViewport().addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					getTable().clearSelection();
				}
			});
		}

		return tableScrollPane;
	}
	
	JMenuItem getAddMenuItem() {
		if (addMenuItem == null) {
			addMenuItem = new JMenuItem(PADialogMediator.NAME);
		}
		return addMenuItem;
	}
	
	JMenuItem getDataSetColorMenuItem() {
		if (colorMenuItem == null) {
			colorMenuItem = new JMenuItem("Change Colors...");
		}
		return colorMenuItem;
	}
	
	JMenuItem getSelectAllMenuItem() {
		if (selectAllMenuItem == null) {
			selectAllMenuItem = new JMenuItem("Select All");
			selectAllMenuItem.addActionListener(evt -> {
				setCheckedToAllRows(true);
			});
		}
		return selectAllMenuItem;
	}
	
	JMenuItem getSelectNoneMenuItem() {
		if (selectNoneMenuItem == null) {
			selectNoneMenuItem = new JMenuItem("Select None");
			selectNoneMenuItem.addActionListener(evt -> {
				setCheckedToAllRows(false);
			});
		}
		return selectNoneMenuItem;
	}
	
	JMenuItem getSelectNodesMenuItem() {
		if (selectNodesMenuItem == null) {
			selectNodesMenuItem = new JMenuItem("Select nodes and edges from selected data sets");
		}
		return selectNodesMenuItem;
	}
	
	JMenuItem getDeleteSignatureMenuItem() {
		if (deleteSignatureMenuItem == null) {
			deleteSignatureMenuItem = new JMenuItem("Remove selected signature gene sets");
		}
		return deleteSignatureMenuItem;
	}
	
	JMenuItem getSyncPropMenuItem() {
		if (syncPropMenuItem == null) {
			syncPropMenuItem = propertyManager.createJCheckBoxMenuItem(
					PropertyManager.CONTROL_DATASET_SELECT_SYNC, 
					"Highlight Datasets for Selected Nodes and Edges");
		}
		return syncPropMenuItem;
	}
	
	JButton getOptionsButton() {
		if (optionButton == null) {
			optionButton = SwingUtil.createMenuButton("Options...", this::fillOptionsMenu);
		}
		return optionButton;
	}
	
	private void fillOptionsMenu(JPopupMenu menu) {
		menu.add(getAddMenuItem());
		menu.add(getDataSetColorMenuItem());
		menu.addSeparator();
		menu.add(getSelectAllMenuItem());
		menu.add(getSelectNoneMenuItem());
		menu.addSeparator();
		menu.add(getSelectNodesMenuItem());
		menu.add(getDeleteSignatureMenuItem());
		menu.add(getSyncPropMenuItem());
		getDeleteSignatureMenuItem().setEnabled(isOnlySignatureSelected());
	}
		
	private boolean isOnlySignatureSelected() {
		Set<AbstractDataSet> selected = getSelectedItems();
		boolean onlySignatureSelected = !selected.isEmpty();
		for (AbstractDataSet ds : selected) {
			if (ds instanceof EMSignatureDataSet == false) {
				onlySignatureSelected = false;
				break;
			}
		}
		return onlySignatureSelected;
	}
	
	private void setCheckedToAllRows(final boolean checked) {
		final Set<AbstractDataSet> oldValue = getCheckedItems();
		final int rowCount = getTable().getRowCount();
		
		for (int i = 0; i < rowCount; i++) {
			getTable().setValueAt(checked, i, SELECTED_COL_IDX);
			AbstractDataSet ds = (AbstractDataSet) getTable().getValueAt(i, NAME_COL_IDX);
			checkedItems.put(ds, checked);
		}
		
		getTable().repaint();
		updateSelectionButtons();
		firePropertyChange(PROP_CHECKED_DATA_SETS, oldValue, getCheckedItems());
	}
	
	private void toggleChecked(final int row) {
		final Set<AbstractDataSet> oldValue = getCheckedItems();
		final boolean checked = (boolean) getTable().getValueAt(row, SELECTED_COL_IDX);
		final int[] checkedRows = getTable().getSelectedRows();
		
		if (checkedRows != null) {
			for (int i : checkedRows) {
				AbstractDataSet ds = (AbstractDataSet) getTable().getValueAt(i, NAME_COL_IDX);
				getTable().setValueAt(!checked, i, SELECTED_COL_IDX);
				checkedItems.put(ds, !checked);
			}
			
			getTable().repaint();
			updateSelectionButtons();
			firePropertyChange(PROP_CHECKED_DATA_SETS, oldValue, getCheckedItems());
		}
	}
	
	private class DefaultSelectorTableCellRenderer extends DefaultTableCellRenderer {
		
		final Font defFont;
		final Font iconFont;
		
		DefaultSelectorTableCellRenderer() {
			defFont = getFont().deriveFont(LookAndFeelUtil.getSmallFontSize());
			iconFont = iconManager.getIconFont(12.0f);
		}
		
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			
			setForeground(table.getForeground());
			setFont(defFont);
			setText("");
			setToolTipText(null);
			
			final Color bg = UIManager.getColor("Table.background");
			setBackground(isSelected ? UIManager.getColor("Table.selectionBackground") : bg);
			setBorder(CELL_BORDER);
			
			if (column == TYPE_COL_IDX) {
				setHorizontalAlignment(JLabel.CENTER);

				if (value instanceof EMSignatureDataSet) {
					setFont(iconFont);
					setText(IconManager.ICON_STAR);
					setToolTipText("Signature Gene Sets");
					setForeground(EMStyleBuilder.Colors.SIG_EDGE_COLOR);
				} else if (value instanceof EMDataSet) {
					setToolTipText("Data Set");
					
					EMDataSet ds = (EMDataSet)value;
					
					if (ds.getColor() != null) {
						setFont(iconFont);
						setText(IconManager.ICON_FILE);
						setForeground(ds.getColor());
					}
				}
			} else if (column == NAME_COL_IDX) {
				setHorizontalAlignment(JLabel.LEFT);
				setText(((AbstractDataSet) value).getName());
				
				String method = value instanceof EMDataSet ? 
						" (" + ((EMDataSet) value).getMethod().getLabel() + ")" : "";
				setToolTipText(((AbstractDataSet) value).getName() + method);
			} else if (column == GENES_COL_IDX) {
				setText("" + value);
				setToolTipText(value + " Gene Sets");
				setHorizontalAlignment(JLabel.RIGHT);
			}
			
			return this;
		}
	}
	
	private class CheckBoxTableCellRenderer extends JCheckBox implements TableCellRenderer {
		
		CheckBoxTableCellRenderer() {
			makeSmall(this);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {
			final Color bg = UIManager.getColor("Table.background");
			
			setSelected((boolean)value);
			setToolTipText((boolean)value ? "Show" : "Hide");
			setBackground(isSelected ? UIManager.getColor("Table.selectionBackground") : bg);
			
			return this;
		}
	}
}
