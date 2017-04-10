package org.baderlab.csplugins.enrichmentmap.view.heatmap.table;

import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.UIManager;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapMainPanel;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.RankingOption;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;

public class ColumnHeaderRankOptionRenderer implements TableCellRenderer {

	private final int colIndex;
	private final HeatMapMainPanel mainPanel;
	
	private SortOrder sortOrder = null;
	private MouseListener mouseListener = null;
	
	
	public ColumnHeaderRankOptionRenderer(HeatMapMainPanel mainPanel, int colIndex) {
		this.colIndex = colIndex;
		this.mainPanel = mainPanel;
	}

	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
		// Convert RankingOption to display String
		if(value instanceof RankingOption) {
			RankingOption rankingOption = (RankingOption) value;
			value = rankingOption.getTableHeaderText();
		}
		
		// Use the default renderer to paint the header nicely (with sort arrows)
		JTableHeader header = table.getTableHeader();
		TableCellRenderer delegate = table.getTableHeader().getDefaultRenderer();
		Component component = delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
		
		// Create the panel
		JButton button = new JButton("Change");
		SwingUtil.makeSmall(button);
		if (isAquaLAF())
			button.putClientProperty("JButton.buttonType", "gradient");
		button.addActionListener(e -> menuButtonClicked(table));
		
		JPanel buttonPanel = new JPanel(new BorderLayout());
		buttonPanel.add(button, BorderLayout.CENTER);
		buttonPanel.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
		buttonPanel.setForeground(header.getForeground());   
		buttonPanel.setBackground(header.getBackground());   
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(component, BorderLayout.CENTER);
		panel.add(buttonPanel, BorderLayout.SOUTH);
		
		// Add mouse listener
		if(mouseListener != null) {
			header.removeMouseListener(mouseListener);
		}
		
		header.addMouseListener(mouseListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
            	int col = header.columnAtPoint(e.getPoint());
            	if(col == colIndex)
            		if(e.getY() > component.getHeight())
            			button.doClick();
					else
						sortColumn(table);
            	else
            		sortOrder = null;
            }
		});
		
		return panel;
	}
	
	
	private SortOrder nextSortOrder() {
		if(sortOrder == null || sortOrder == SortOrder.DESCENDING)
			return sortOrder = SortOrder.ASCENDING;
		else
			return sortOrder = SortOrder.DESCENDING;
	}
	
	
	private void sortColumn(JTable table) {
		TableRowSorter<?> sorter = ((TableRowSorter<?>)table.getRowSorter());
		RowSorter.SortKey sortKey = new RowSorter.SortKey(colIndex, nextSortOrder());
		sorter.setSortKeys(Arrays.asList(sortKey));
		sorter.sort();
	}
	
	
	private void menuButtonClicked(JTable table) {
		JTableHeader header = table.getTableHeader();
		
		List<RankingOption> rankOptions = mainPanel.getAllRankingOptions();
		
		JPopupMenu menu = new JPopupMenu();
		for(RankingOption rankOption : rankOptions) {
			JMenuItem item = new JCheckBoxMenuItem(rankOption.getName());
			item.setSelected(rankOption == mainPanel.getSelectedRankOption());
			SwingUtil.makeSmall(item);
			menu.add(item);
			item.addActionListener(e ->
				mainPanel.updateSetting_RankOption(rankOption)
			);
		}
		
		int y = header.getHeight();
		int x = 0;
		for(int i = 0; i < colIndex; i++) {
			TableColumn tableColumn = table.getColumnModel().getColumn(i);
			x += tableColumn.getWidth();
		}
		menu.show(header, x, y);
	}
	
	public void dispose(JTableHeader header) {
		if(mouseListener != null)
			header.removeMouseListener(mouseListener);
	}
	
}
