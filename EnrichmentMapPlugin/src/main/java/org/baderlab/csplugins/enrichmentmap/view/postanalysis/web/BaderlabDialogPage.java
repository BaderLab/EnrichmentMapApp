package org.baderlab.csplugins.enrichmentmap.view.postanalysis.web;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.table.TableColumnModel;

import org.baderlab.csplugins.enrichmentmap.view.postanalysis.PADialogPage;
import org.baderlab.csplugins.enrichmentmap.view.util.CardDialogCallback;
import org.baderlab.csplugins.enrichmentmap.view.util.CardDialogPage;
import org.baderlab.csplugins.enrichmentmap.view.util.ComboItem;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.cytoscape.util.swing.LookAndFeelUtil;


public class BaderlabDialogPage implements CardDialogPage {

	private final PADialogPage parent;
	
	private CardDialogCallback callback;
	private JComboBox<ComboItem<DateDir>> dateCombo;
	private JLabel spinnerLabel;
	private JTable table;
	private ActionListener dateActionListener;
	private boolean openCalled = false;
	
	
	public BaderlabDialogPage(PADialogPage parent) {
		this.parent = parent;
	}
	
	@Override
	public String getID() {
		return getClass().getSimpleName();
	}

	@Override
	public String getPageComboText() {
		return "download.baderlab.org";
	}

	@Override
	public void finish() {
		String dateFolder = getDateFolder();
		String filePath = getGmtFilePath();
		if(dateFolder == null || filePath == null) {
			return;
		}
		
		try {
			URL url = BaderlabRequests.buildUrl(dateFolder, filePath);
			parent.runLoadFromUrlTasks(url, callback.getDialog());
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void opened() {
		if(!openCalled) {
			requestDates();
		}
		openCalled = true;
	}
	
	@Override
	public JPanel createBodyPanel(CardDialogCallback callback) {
		this.callback = callback;
		callback.setFinishButtonEnabled(false);
		
		JPanel datePanel = createDatePanel();
		JPanel listPanel = createTablePanel();
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(datePanel, BorderLayout.NORTH);
		panel.add(listPanel, BorderLayout.CENTER);
		return panel;
	}

	private JPanel createDatePanel() {
		JLabel title = new JLabel("Download gene set files from download.baderlab.org");
		
		JLabel dateLabel = new JLabel("Date:");
		dateCombo = new JComboBox<>();
		dateCombo.addItem(new ComboItem<>(null, "Loading..."));
		
		spinnerLabel = new JLabel();
		
		SwingUtil.makeSmall(title, dateLabel, dateCombo, spinnerLabel);
		
		dateActionListener = e -> requestFiles();
		
		URL url = getClass().getClassLoader().getResource("images/spinner_16.gif");
		ImageIcon spinner = new ImageIcon(url);
		spinnerLabel.setIcon(spinner);
		spinner.setImageObserver(spinnerLabel);
		
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addGroup(layout.createSequentialGroup()
				.addComponent(title)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(spinnerLabel)
			)
			.addGroup(layout.createSequentialGroup()
				.addComponent(dateLabel)
				.addComponent(dateCombo, 300, 300, 300)
			)
		);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(title)
				.addComponent(spinnerLabel)
			)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(dateLabel)
				.addComponent(dateCombo)
			)
		);
		
		if(LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);
		return panel;
	}

	private JPanel createTablePanel() {
		table = new JTable();
		
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		table.setFillsViewportHeight(true);
		table.setCellSelectionEnabled(false);
		table.setRowSelectionAllowed(true);
		table.setAutoCreateRowSorter(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		table.getSelectionModel().addListSelectionListener(e -> updateFinishButton());
		
		FileSizeCellRenderer fileSizeRenderer = new FileSizeCellRenderer();
		fileSizeRenderer.setHorizontalAlignment(JLabel.RIGHT);
		table.setDefaultRenderer(Integer.class, fileSizeRenderer);
		
		updateTable(null);
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(scrollPane, BorderLayout.CENTER);
		panel.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));
		return panel;
	}
	
	private void updateFinishButton() {
		callback.setFinishButtonEnabled(table.getSelectionModel().getMinSelectionIndex() >= 0);
	}
	
	private void updateTable(List<GmtFile> gmtFiles) {
		GmtFileTableModel tableModel = new GmtFileTableModel(gmtFiles);
		table.setModel(tableModel);
		
		TableColumnModel columnModel = table.getColumnModel();
		columnModel.getColumn(1).setMaxWidth(150);
	}

	private String getDateFolder() {
		return dateCombo.getItemAt(dateCombo.getSelectedIndex()).getValue().getFolder();
	}
	
	private String getGmtFilePath() {
		int row = table.getSelectionModel().getMinSelectionIndex();
		String gmtFilePath = (String) table.getModel().getValueAt(row, 0);
		return gmtFilePath;
	}
	
	private <T> void doRequest(Callable<T> doInBackground, Consumer<T> done) {
		SwingWorker<T,Void> worker = new SwingWorker<T, Void>() {
			@Override
			protected T doInBackground() throws Exception {
				return doInBackground.call();
			}
			@Override
			protected void done() {
				try {
					done.accept(get());
				} catch(Exception e) {
					e.printStackTrace();
					if(callback.getDialogFrame().isVisible()) {
						JOptionPane.showMessageDialog(
							callback.getDialogFrame(), 
							"Error while accessing downloads.baderlab.org", 
							"HTTP Request Error",
							JOptionPane.ERROR_MESSAGE);
					}
					callback.close();
				} finally {
					spinnerLabel.setVisible(false);
					updateFinishButton();
				}
			}
		};
		
		callback.setFinishButtonEnabled(false);
		spinnerLabel.setVisible(true);
		worker.execute();
	}
	
	
	private void requestDates() {
		doRequest(
			BaderlabRequests::requestDateFolders,
			dates -> {
				dateCombo.removeActionListener(dateActionListener);
				dateCombo.removeAllItems();
				dates.sort(Comparator.comparing(DateDir::getTimestamp).reversed());
				for(DateDir dateDir : dates) {
					if(!"current_release".equals(dateDir.getFolder())) {
						dateCombo.addItem(new ComboItem<>(dateDir, dateDir.getFolder()));
					}
				}
				dateCombo.addActionListener(dateActionListener);	
				requestFiles();
			}
		);
	}
	
	private void requestFiles() {
		updateTable(null);
		String dateFolder = getDateFolder();
		
		doRequest(
			() -> BaderlabRequests.requestFiles(dateFolder),
			this::updateTable
		);
	}
	
}
