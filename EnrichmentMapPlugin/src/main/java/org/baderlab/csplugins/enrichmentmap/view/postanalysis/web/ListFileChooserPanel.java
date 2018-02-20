package org.baderlab.csplugins.enrichmentmap.view.postanalysis.web;

import java.awt.BorderLayout;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;

@SuppressWarnings("serial")
public class ListFileChooserPanel extends JPanel implements FileChooserPanel {

	private final JTable table;
	private Consumer<Optional<String>> selectionListener = null;


	public ListFileChooserPanel() {
		table = new JTable();

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		table.setFillsViewportHeight(true);
		table.setCellSelectionEnabled(false);
		table.setRowSelectionAllowed(true);
		table.setAutoCreateRowSorter(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		table.getSelectionModel().addListSelectionListener(this::fireSelection);

		FileSizeRenderer fileSizeRenderer = new FileSizeRenderer();
		fileSizeRenderer.setHorizontalAlignment(JLabel.RIGHT);
		table.setDefaultRenderer(Integer.class, fileSizeRenderer);

		setFiles(null);

		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);
	}

	
	@Override
	public JPanel getPanel() {
		return this;
	}
	
	
	@Override
	public void setFiles(List<GmtFile> gmtFiles) {
		GmtFileTableModel tableModel = new GmtFileTableModel(gmtFiles);
		table.setModel(tableModel);
		
		TableColumnModel columnModel = table.getColumnModel();
		columnModel.getColumn(1).setMaxWidth(150);
	}

	
	@Override
	public Optional<String> getSelectedFilePath() {
		int row = table.getSelectionModel().getMinSelectionIndex();
		if(row == -1)
			return Optional.empty();
		String gmtFilePath = (String) table.getModel().getValueAt(row, 0);
		return Optional.ofNullable(gmtFilePath);
	}
	
	
	@Override
	public void setSelectionListener(Consumer<Optional<String>> listener) {
		this.selectionListener = listener;
	}

	private void fireSelection(ListSelectionEvent evt) {
		if(selectionListener != null)
			selectionListener.accept(getSelectedFilePath());
	}
	
	
	
	private class GmtFileTableModel extends AbstractTableModel {

		private final List<GmtFile> files;
		
		public GmtFileTableModel(List<GmtFile> files) {
			this.files = files == null ? Collections.emptyList() : files;
		}
		
		@Override
		public int getRowCount() {
			return files.size();
		}

		@Override
		public int getColumnCount() {
			return 2;
		}
		
		@Override
		public String getColumnName(int col) {
			switch(col) {
				case 0: return "GMT File";
				case 1: return "Size";
			}
			return null;
		}

		@Override
		public Object getValueAt(int row, int col) {
			GmtFile file = files.get(row);
			switch(col) {
				case 0: return file.getPath();
				case 1: return file.getSize();
			}
			return null;
		}
		
		@Override
		public Class<?> getColumnClass(int col) {
			switch(col) {
				case 0: return String.class;
				case 1: return Integer.class;
			}
			return null;
		}
	}
	
}
