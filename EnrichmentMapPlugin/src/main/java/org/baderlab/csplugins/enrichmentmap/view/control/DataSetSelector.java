package org.baderlab.csplugins.enrichmentmap.view.control;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
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
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.baderlab.csplugins.enrichmentmap.model.AbstractDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EMSignatureDataSet;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class DataSetSelector extends JPanel {
	
	private static final String[] HEARDER_NAMES = new String[]{ "", "", "Name", "" };
	
	private static final int SELECTED_COL_IDX = 0;
	private static final int TYPE_COL_IDX = 1;
	private static final int NAME_COL_IDX = 2;
	private static final int GENES_COL_IDX = 3;
	
	private static final Border CELL_BORDER = new EmptyBorder(0, 0, 0, 0);
	
	private JTable table;
	private JScrollPane tableScrollPane;
	private JButton addButton;
	private JButton selectAllButton;
	private JButton selectNoneButton;
	
	private final Set<AbstractDataSet> items;
	private final Map<AbstractDataSet, Boolean> selectedItems;
	private List<Integer> previousSelectedRows;
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public DataSetSelector(final CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
		this.items = new LinkedHashSet<>();
		this.selectedItems = new HashMap<>();
		
		init();
	}

	public void update(final Collection<AbstractDataSet> newItems) {
		Map<AbstractDataSet, Boolean> oldSelectedItems = new HashMap<>();
		items.clear();
		selectedItems.clear();
		
		if (newItems != null) {
			for (AbstractDataSet ds : newItems) {
				items.add(ds);
				
				boolean selected = !oldSelectedItems.containsKey(ds) // New items are selected by default!
						|| oldSelectedItems.get(ds) == Boolean.TRUE;
				selectedItems.put(ds, selected);
			}
		}
		
		updateTable();
		updateSelectionButtons();
	}

	public Set<AbstractDataSet> getSelectedItems() {
		Set<AbstractDataSet> set = new HashSet<>();
		
		selectedItems.forEach((ds, selected) -> {
			if (selected == Boolean.TRUE)
				set.add(ds);
		});
		
		return set;
	}
	
	private void init() {
		LookAndFeelUtil.equalizeSize(getSelectAllButton(), getSelectNoneButton());
		
		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
   		layout.setHorizontalGroup(layout.createParallelGroup(CENTER, true)
				.addComponent(getTableScrollPane(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(layout.createSequentialGroup()
						.addComponent(getAddButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addGap(20,  20, Short.MAX_VALUE)
						.addComponent(getSelectAllButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(getSelectNoneButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   				)
   		);
   		layout.setVerticalGroup(layout.createSequentialGroup()
   				.addComponent(getTableScrollPane(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
   				.addGroup(layout.createParallelGroup(CENTER, false)
						.addComponent(getAddButton(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
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
			data[i][SELECTED_COL_IDX] = selectedItems.get(ds);
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
		
		getTable().getColumnModel().getColumn(SELECTED_COL_IDX).setMaxWidth(24);
		getTable().getColumnModel().getColumn(TYPE_COL_IDX).setMaxWidth(24);
		getTable().getColumnModel().getColumn(GENES_COL_IDX).setMaxWidth(48);
		
		getTable().getColumnModel().getColumn(SELECTED_COL_IDX).setResizable(false);
		getTable().getColumnModel().getColumn(TYPE_COL_IDX).setResizable(false);
	}
	
	private void updateSelectionButtons() {
		final int rowCount = table.getRowCount();
		boolean hasUnselected = false;
		boolean hasSelected = false;
		
		for (int i = 0; i < rowCount; i++) {
			final boolean selected = (boolean) table.getModel().getValueAt(i, SELECTED_COL_IDX);
			
			if (!hasUnselected)
				hasUnselected = !selected;
			if (!hasSelected)
				hasSelected = selected;
			if (hasUnselected && hasSelected)
				break;
		}
		
		getSelectAllButton().setEnabled(hasUnselected);
		getSelectNoneButton().setEnabled(hasSelected);
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
			
			table.getSelectionModel().addListSelectionListener(e -> {
				if (!e.getValueIsAdjusting()) {
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
						
						toggleSelection(row);
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
			
			if (isAquaLAF()) {
				addButton.putClientProperty("JButton.buttonType", "gradient");
				addButton.putClientProperty("JComponent.sizeVariant", "small");
			}
		}
		
		return addButton;
	}
	
	JButton getSelectAllButton() {
		if (selectAllButton == null) {
			selectAllButton = new JButton("Select All");
			selectAllButton.addActionListener(evt -> {
				setSelectedToAllRows(true);
			});
			
			if (isAquaLAF()) {
				selectAllButton.putClientProperty("JButton.buttonType", "gradient");
				selectAllButton.putClientProperty("JComponent.sizeVariant", "small");
			}
		}
		
		return selectAllButton;
	}
	
	JButton getSelectNoneButton() {
		if (selectNoneButton == null) {
			selectNoneButton = new JButton("Select None");
			selectNoneButton.addActionListener(evt -> {
				setSelectedToAllRows(false);
			});
			
			if (isAquaLAF()) {
				selectNoneButton.putClientProperty("JButton.buttonType", "gradient");
				selectNoneButton.putClientProperty("JComponent.sizeVariant", "small");
			}
		}
		
		return selectNoneButton;
	}
	
	private void setSelectedToAllRows(final boolean selected) {
		final Set<AbstractDataSet> oldValue = getSelectedItems();
		final int rowCount = getTable().getRowCount();
		
		for (int i = 0; i < rowCount; i++) {
			getTable().setValueAt(selected, i, SELECTED_COL_IDX);
			AbstractDataSet ds = (AbstractDataSet) getTable().getValueAt(i, NAME_COL_IDX);
			selectedItems.put(ds, selected);
		}
		
		getTable().repaint();
		updateSelectionButtons();
		firePropertyChange("selectedData", oldValue, getSelectedItems());
	}
	
	private void toggleSelection(final int row) {
		final Set<AbstractDataSet> oldValue = getSelectedItems();
		final boolean selected = (boolean) getTable().getValueAt(row, SELECTED_COL_IDX);
		final int[] selectedRows = getTable().getSelectedRows();
		
		if (selectedRows != null) {
			for (int i : selectedRows) {
				AbstractDataSet ds = (AbstractDataSet) getTable().getValueAt(i, NAME_COL_IDX);
				getTable().setValueAt(!selected, i, SELECTED_COL_IDX);
				selectedItems.put(ds, !selected);
			}
			
			getTable().repaint();
			updateSelectionButtons();
			firePropertyChange("selectedData", oldValue, getSelectedItems());
		}
	}
	
	private class DefaultSelectorTableCellRenderer extends DefaultTableCellRenderer {
		
		final Font defFont;
		final Font selectionFont;
		final Font iconFont;
		final IconManager iconManager = serviceRegistrar.getService(IconManager.class);
		
		DefaultSelectorTableCellRenderer() {
			defFont = getFont().deriveFont(LookAndFeelUtil.getSmallFontSize());
			selectionFont = iconManager.getIconFont(12.0f);
			iconFont = iconManager.getIconFont(16.0f);
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
			
			if (column == SELECTED_COL_IDX  && value instanceof Boolean) {
				setFont(selectionFont);
				setHorizontalAlignment(JLabel.CENTER);
				setText((boolean)value ? IconManager.ICON_CHECK_SQUARE : IconManager.ICON_SQUARE_O);
			} else if (column == TYPE_COL_IDX) {
				setHorizontalAlignment(JLabel.CENTER);

				if (value instanceof EMSignatureDataSet) {
					setFont(iconFont);
					setText(IconManager.ICON_STAR);
					setForeground(EMStyleBuilder.Colors.SIG_NODE_BORDER_COLOR);
					setToolTipText("Signature Gene Sets");
				} else {
					setToolTipText("Data Set");
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
	
	private class CheckBoxTableCellRenderer implements TableCellRenderer {
		
		final JPanel panel;
		final JCheckBox chk;
		
		CheckBoxTableCellRenderer() {
			chk = new JCheckBox();
			chk.putClientProperty("JComponent.sizeVariant", "mini"); // Aqua LAF only
			panel = new JPanel(new BorderLayout());
			panel.add(chk, BorderLayout.WEST);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {
			final Color bg = UIManager.getColor("Table.background");
			
			chk.setSelected((boolean)value);
			chk.setToolTipText((boolean)value ? "Show" : "Hide");
			chk.setBackground(isSelected ? UIManager.getColor("Table.selectionBackground") : bg);
			panel.setBackground(isSelected ? UIManager.getColor("Table.selectionBackground") : bg);
			panel.setBorder(CELL_BORDER);
			
			return panel;
		}
	}
}
