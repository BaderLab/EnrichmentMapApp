package org.baderlab.csplugins.enrichmentmap.view.postanalysis2;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;

@SuppressWarnings("serial")
public class MannWhitRanksPanel extends JPanel {

	private JTable table;
	
	public MannWhitRanksPanel(EnrichmentMap map) {
		createContents(map);
	}
	
	private void createContents(EnrichmentMap map) {
		JLabel title = new JLabel("Ranks to use for Mann-Whitney test");
		SwingUtil.makeSmall(title);
		
		table = new JTable() {
			@Override
			public TableCellEditor getCellEditor(int row, int col) {
				int modelCol = convertColumnIndexToModel(col);
				int modelRow = convertRowIndexToModel(row);
				if(col == 1) {
					MannWhitTableModel model = (MannWhitTableModel)table.getModel();
					EMDataSet dataSet = model.getDataSet(modelRow);
					JComboBox<String> combo = new JComboBox<>();
					for(String ranking : dataSet.getAllRanksNames()) {
						combo.addItem(ranking);
					}
					combo.setSelectedItem(model.getValueAt(modelRow, modelCol));
					return new DefaultCellEditor(combo);
				} else {
					return super.getCellEditor(row, col);
				}
			}
		};
		
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		table.setFillsViewportHeight(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.setCellSelectionEnabled(false);
		table.setRowSelectionAllowed(false);
		table.setAutoCreateRowSorter(false);

		table.setModel(new MannWhitTableModel(map));
		
		setLayout(new BorderLayout());
		add(title, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
		setOpaque(false);
	}
	
	public Map<String,String> getResults() {
		MannWhitTableModel model = (MannWhitTableModel)table.getModel();
		Map<String,String> results = new HashMap<>();
		for(int row = 0; row < model.getRowCount(); row++) {
			EMDataSet dataSet = model.getDataSet(row);
			String ranking = model.getRanking(row);
			results.put(dataSet.getName(), ranking);
		}
		return results;
	}
	
	private static class MannWhitTableModel extends AbstractTableModel {
		
		List<EMDataSet> dataSets;
		List<String> rankingNames;
		
		public MannWhitTableModel(EnrichmentMap map) {
			dataSets = map.getDataSetList();
			rankingNames = new ArrayList<>(dataSets.size());
			for(EMDataSet dataSet : dataSets) {
				String ranking = dataSet.getAllRanksNames().iterator().next();
				rankingNames.add(ranking);
			}
		}
		
		@Override
		public String getColumnName(int col) {
			if(col == 0)
				return "Data Set";
			if(col == 1)
				return "Ranks";
			return null;
		}

		@Override
		public int getRowCount() {
			return dataSets.size();
		}
		
		public EMDataSet getDataSet(int row) {
			return dataSets.get(row);
		}
		
		public String getRanking(int row) {
			return rankingNames.get(row);
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public boolean isCellEditable(int row, int col) {
			return col == 1;
		}
		
		@Override
		public Object getValueAt(int row, int col) {
			if(col == 0)
				return getDataSet(row).getName();
			if(col == 1)
				return getRanking(row);
			return null;
		}
		
		public void setValueAt(Object value, int row, int col) {
			if(col == 1) {
				String ranking = value.toString();
				rankingNames.set(row, ranking);
			}
		}
		
	}

}
