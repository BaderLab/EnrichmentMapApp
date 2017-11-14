package org.baderlab.csplugins.enrichmentmap.view.postanalysis2;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.baderlab.csplugins.enrichmentmap.model.SetOfGeneSets;
import org.baderlab.csplugins.enrichmentmap.parsers.GMTFileReaderTask;
import org.baderlab.csplugins.enrichmentmap.util.ResultTaskObserver;
import org.baderlab.csplugins.enrichmentmap.view.creation.NamePanel;
import org.baderlab.csplugins.enrichmentmap.view.util.CardDialogCallback;
import org.baderlab.csplugins.enrichmentmap.view.util.CardDialogPage;
import org.baderlab.csplugins.enrichmentmap.view.util.FileBrowser;
import org.baderlab.csplugins.enrichmentmap.view.util.GBCFactory;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class PADialogPage implements CardDialogPage {

	@Inject private PAWeightPanel.Factory weightPanelFactory;
	@Inject private FileUtil fileUtil;
	@Inject private DialogTaskManager dialogTaskManager;
	
	private final EnrichmentMap map;
	private CardDialogCallback callback;
	
	private NamePanel namePanel;
	private PAWeightPanel weightPanel;
	
	private JTable table;
	private SigGeneSetTableModel tableModel;
	
	
	public interface Factory {
		PADialogPage create(EnrichmentMap map);
	}
	
	@Inject
	public PADialogPage(@Assisted EnrichmentMap map) {
		this.map = map;
	}
	
	@Override
	public String getID() {
		return getClass().getSimpleName();
	}

	@Override
	public String getPageComboText() {
		return PADialogParameters.TITLE;
	}

	@Override
	public void finish() {

	}
	
	@Override
	public JPanel createBodyPanel(CardDialogCallback callback) {
		this.callback = callback;
		
		JPanel geneSetsPanel = createGeneSetsPanel();
		namePanel = new NamePanel("Data Set Name");
		weightPanel = weightPanelFactory.create(map);
		
		JPanel bottom = new JPanel(new GridBagLayout());
		bottom.add(namePanel, GBCFactory.grid(0,0).insets(0).weightx(1.0).fill(GridBagConstraints.HORIZONTAL).get());
		bottom.add(weightPanel, GBCFactory.grid(0,1).insets(0).weightx(1.0).fill(GridBagConstraints.HORIZONTAL).get());
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(geneSetsPanel, BorderLayout.CENTER);
		panel.add(bottom, BorderLayout.SOUTH);
		
		return panel;
	}
	
	
	private JPanel createGeneSetsPanel() {
		JPanel panel = new JPanel();
		
		JLabel title = new JLabel("Signature Gene Sets");
		JPanel tablePanel = createTablePanel();
		
		JButton loadFileButton = new JButton("Load from File...");
		JButton loadWebButton = new JButton("Load from Web...");
		JButton selectAllButton = new JButton("Select All");
		JButton selectNoneButton = new JButton("Select None");
		JButton filterButton = new JButton("Filter...");
		loadWebButton.setEnabled(false);
		
		loadFileButton.addActionListener(e -> loadFromFile());
		
		SwingUtil.makeSmall(title, loadFileButton, loadWebButton, selectAllButton, selectNoneButton, filterButton);
		LookAndFeelUtil.equalizeSize(loadFileButton, loadWebButton, selectAllButton, selectNoneButton, filterButton);
		
		final GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addComponent(title)
			.addGroup(layout.createSequentialGroup()
				.addComponent(tablePanel)
				.addGroup(layout.createParallelGroup()
					.addComponent(loadFileButton)
					.addComponent(loadWebButton)
					.addComponent(selectAllButton)
					.addComponent(selectNoneButton)
					.addComponent(filterButton)
				)
			)
		);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addComponent(title)
			.addGroup(layout.createParallelGroup()
				.addComponent(tablePanel)
				.addGroup(layout.createSequentialGroup()
					.addComponent(loadFileButton)
					.addComponent(loadWebButton)
					.addGap(20)
					.addComponent(selectAllButton)
					.addComponent(selectNoneButton)
					.addGap(20)
					.addComponent(filterButton)
				)
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
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.setCellSelectionEnabled(true);
		table.setAutoCreateRowSorter(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		JTableHeader header = table.getTableHeader();
		header.setReorderingAllowed(false);
		
		updateTableModel(null);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(scrollPane, BorderLayout.CENTER);
		panel.setOpaque(false);
		return panel;
	}
	
	private void updateTableModel(SetOfGeneSets setOfGeneSets) {
		List<SigGeneSetDescriptor> descriptors;
		if(setOfGeneSets == null) {
			descriptors = Collections.emptyList();
		} else {
			descriptors = new ArrayList<>(setOfGeneSets.size());
			for(GeneSet geneSet : setOfGeneSets.getGeneSets().values()) {
				descriptors.add(new SigGeneSetDescriptor(geneSet, 0));
			}
		}

		tableModel = new SigGeneSetTableModel(descriptors);
		table.setModel(tableModel);
		
		TableColumnModel columnModel = table.getColumnModel();
		columnModel.getColumn(0).setPreferredWidth(600);
		columnModel.getColumn(1).setPreferredWidth(100);
		columnModel.getColumn(2).setPreferredWidth(100);
	}
	
	private void loadFromFile() {
		System.out.println("PADialogPage.loadFromFile()");
		Optional<Path> gmtPath = FileBrowser.browse(fileUtil, callback.getDialogFrame(), FileBrowser.Filter.GMT);
		if(gmtPath.isPresent()) {
			System.out.println("here");
			SetOfGeneSets setOfGeneSets = new SetOfGeneSets();
			GMTFileReaderTask gmtTask = new GMTFileReaderTask(map, gmtPath.get().toString(), setOfGeneSets);
			TaskIterator taskIterator = new TaskIterator(gmtTask);
			dialogTaskManager.execute(taskIterator, new ResultTaskObserver() {
				@Override
				public void allFinished(FinishStatus finishStatus) {
					updateTableModel(setOfGeneSets);
				}
			});
		}
	}
	

}
