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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.baderlab.csplugins.enrichmentmap.model.AbstractDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EMSignatureDataSet;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class DataSetSelector extends JPanel {
	
	private static final String[] HEARDER_NAMES = new String[]{ "", "", "Name", "" };
	
	private static final int TYPE_COL_IDX = 0;
	private static final int SELECTED_COL_IDX = 1;
	private static final int NAME_COL_IDX = 2;
	private static final int GENES_COL_IDX = 3;
	
	private static final Border CELL_BORDER = new EmptyBorder(0, 0, 0, 0);
	
	private JTable table;
	private JScrollPane tableScrollPane;
	private JButton addButton;
	private JButton removeButton;
	private JButton selectAllButton;
	private JButton selectNoneButton;
	
	private final Set<AbstractDataSet> items;
	private final Map<AbstractDataSet, Boolean> checkedItems;
	private List<Integer> previousSelectedRows;
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public DataSetSelector(final CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
		this.items = new LinkedHashSet<>();
		this.checkedItems = new HashMap<>();
		
		init();
	}

	public void update(final Collection<AbstractDataSet> newItems) {
		Map<AbstractDataSet, Boolean> oldSelectedItems = new HashMap<>();
		items.clear();
		checkedItems.clear();
		
		if (newItems != null) {
			for (AbstractDataSet ds : newItems) {
				items.add(ds);
				
				boolean selected = !oldSelectedItems.containsKey(ds) // New items are selected by default!
						|| oldSelectedItems.get(ds) == Boolean.TRUE;
				checkedItems.put(ds, selected);
			}
		}
		
		updateTable();
		updateSelectionButtons();
		updateRemoveButton();
	}

	public Set<AbstractDataSet> getCheckedItems() {
		Set<AbstractDataSet> set = new HashSet<>();
		
		checkedItems.forEach((ds, checked) -> {
			if (checked == Boolean.TRUE)
				set.add(ds);
		});
		
		return set;
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
	
	private void init() {
		LookAndFeelUtil.equalizeSize(getAddButton(), getRemoveButton());
		LookAndFeelUtil.equalizeSize(getSelectAllButton(), getSelectNoneButton());
		
		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
   		layout.setHorizontalGroup(layout.createParallelGroup(CENTER, true)
				.addComponent(getTableScrollPane(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(layout.createSequentialGroup()
						.addComponent(getAddButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(getRemoveButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addGap(20,  20, Short.MAX_VALUE)
						.addComponent(getSelectAllButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(getSelectNoneButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   				)
   		);
   		layout.setVerticalGroup(layout.createSequentialGroup()
   				.addComponent(getTableScrollPane(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
   				.addGroup(layout.createParallelGroup(CENTER, false)
						.addComponent(getAddButton(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(getRemoveButton(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(getSelectAllButton(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(getSelectNoneButton(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
   				)
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
		
		getTable().getColumnModel().getColumn(TYPE_COL_IDX).setMaxWidth(20);
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
		
		getSelectAllButton().setEnabled(hasUnchecked);
		getSelectNoneButton().setEnabled(hasChecked);
	}
	
	private void updateRemoveButton() {
		boolean onlySignatureSelected = true;
		int[] selectedRows = getTable().getSelectedRows();
		
		if (selectedRows.length > 0) {
			for (int r : selectedRows) {
				AbstractDataSet ds = (AbstractDataSet) table.getModel().getValueAt(r, NAME_COL_IDX);
				
				if (ds instanceof EMSignatureDataSet == false) {
					onlySignatureSelected = false;
					break;
				}
			}
		} else {
			 onlySignatureSelected = false;
		}
		
		getRemoveButton().setEnabled(onlySignatureSelected);
	}
	
	private JTable getTable() {
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
					updateRemoveButton();
					// Workaround for preventing a click on the check-box in a selected row
					// from changing the selection when multiple table rows are already selected
					if (table.getSelectedRowCount() > 0)
						previousSelectedRows = Arrays.stream(table.getSelectedRows()).boxed()
								.collect(Collectors.toList());
				}
			});
			table.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					final boolean isMac = LookAndFeelUtil.isMac();
					
					// COMMAND button down on MacOS (or CONTROL button down on another OS) or SHIFT?
					if ((isMac && e.isMetaDown()) || (!isMac && e.isControlDown()) || e.isShiftDown())
						return; // Ignore!
					
				    final int col = table.columnAtPoint(e.getPoint());
				    
					if (col == SELECTED_COL_IDX) {
						final int row = table.rowAtPoint(e.getPoint());
						
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
	
	JButton getAddButton() {
		if (addButton == null) {
			addButton = new JButton(" " + IconManager.ICON_PLUS + " ");
			addButton.setFont(serviceRegistrar.getService(IconManager.class).getIconFont(11.0f));
			addButton.setToolTipText("Add Signature Gene Sets...");
			makeSmall(addButton);
			
			if (isAquaLAF())
				addButton.putClientProperty("JButton.buttonType", "gradient");
		}
		
		return addButton;
	}
	
	JButton getRemoveButton() {
		if (removeButton == null) {
			removeButton = new JButton(IconManager.ICON_TRASH_O);
			removeButton.setFont(serviceRegistrar.getService(IconManager.class).getIconFont(14.0f));
			removeButton.setToolTipText("Remove Signature Gene Sets");
			makeSmall(removeButton);
			
			if (isAquaLAF())
				removeButton.putClientProperty("JButton.buttonType", "gradient");
		}
		
		return removeButton;
	}
	
	JButton getSelectAllButton() {
		if (selectAllButton == null) {
			selectAllButton = new JButton("Select All");
			selectAllButton.addActionListener(evt -> {
				setCheckedToAllRows(true);
			});
			makeSmall(selectAllButton);
			
			if (isAquaLAF())
				selectAllButton.putClientProperty("JButton.buttonType", "gradient");
		}
		
		return selectAllButton;
	}
	
	JButton getSelectNoneButton() {
		if (selectNoneButton == null) {
			selectNoneButton = new JButton("Select None");
			selectNoneButton.addActionListener(evt -> {
				setCheckedToAllRows(false);
			});
			makeSmall(selectNoneButton);
			
			if (isAquaLAF())
				selectNoneButton.putClientProperty("JButton.buttonType", "gradient");
		}
		
		return selectNoneButton;
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
		firePropertyChange("selectedData", oldValue, getCheckedItems());
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
			firePropertyChange("selectedData", oldValue, getCheckedItems());
		}
	}
	
	private class DefaultSelectorTableCellRenderer extends DefaultTableCellRenderer {
		
		final Font defFont;
		final Font iconFont;
		final IconManager iconManager = serviceRegistrar.getService(IconManager.class);
		
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
					setText(IconManager.ICON_STAR_O);
					setForeground(EMStyleBuilder.Colors.SIG_NODE_BORDER_COLOR);
					setBackground(EMStyleBuilder.Colors.SIG_NODE_COLOR);
					setToolTipText("Signature Gene Sets");
				} else if (value instanceof EMDataSet) {
					setToolTipText("Data Set");
					
					if (((EMDataSet)value).getColor() != null) {
						setFont(iconFont);
						setText(IconManager.ICON_FILE_O);
						setForeground(EMStyleBuilder.Colors.DEF_NODE_BORDER_COLOR);
						setBackground(EMStyleBuilder.Colors.DEF_NODE_COLOR);
					}
				}
			} else if (column == NAME_COL_IDX) {
				setHorizontalAlignment(JLabel.LEFT);
				setText(((AbstractDataSet) value).getName());
				setToolTipText(((AbstractDataSet) value).getName());
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
