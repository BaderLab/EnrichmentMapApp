package org.baderlab.csplugins.enrichmentmap.view.mastermap;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.Border;

import org.baderlab.csplugins.enrichmentmap.model.DataSetFiles;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet.Method;
import org.baderlab.csplugins.enrichmentmap.resolver.DataSetParameters;
import org.baderlab.csplugins.enrichmentmap.view.util.CardDialogCallback;
import org.baderlab.csplugins.enrichmentmap.view.util.CardDialogPage;
import org.baderlab.csplugins.enrichmentmap.view.util.IterableListModel;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.cytoscape.util.swing.IconManager;

import com.google.inject.Inject;
import com.lowagie.text.Font;

@SuppressWarnings("serial")
public class MasterDetailDialogPage implements CardDialogPage {

	@Inject private IconManager iconManager;
	@Inject private CutoffPropertiesPanel cutoffPanel;
	@Inject private EditDataSetCompactPanel.Factory dataSetPanelFactory;
	
	private DataSetList dataSetMasterList;
	private IterableListModel<DataSetParameters> dataSetListModel;
	private JPanel dataSetDetailHolder;
	
	private JCheckBox distinctEdgesCheckbox;
	private CardDialogCallback callback;
	
	
	@Override
	public String getID() {
		return getClass().getSimpleName();
	}

	@Override
	public String getPageTitle() {
		return "Create Enrichment Map";
	}

	@Override
	public String getPageComboText() {
		return "Master/Detail - Experimental";
	}
	
	@Override
	public void finish() {
	}
	
	
	@Override
	public JPanel createBodyPanel(CardDialogCallback callback) {
		this.callback = callback;
		
		JPanel dataPanel = createDataSetPanel();
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(dataPanel, BorderLayout.CENTER);
		panel.add(cutoffPanel, BorderLayout.SOUTH);
		
		callback.setFinishButtonEnabled(false);
		return panel;
	}

	
	
	private JPanel createDataSetPanel() {
		JPanel titlePanel = createTitlePanel();
		
		dataSetListModel = new IterableListModel<>();
		dataSetMasterList = new DataSetList(dataSetListModel);
		dataSetMasterList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		dataSetMasterList.addListSelectionListener(e -> {
			DataSetParameters params = dataSetMasterList.getSelectedValue();
			editDataSet(params);
		});
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(dataSetMasterList);
		
		dataSetDetailHolder = new JPanel(new BorderLayout());
		dataSetDetailHolder.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY)); // MKTODO get the color properly
		dataSetDetailHolder.add(dataSetPanelFactory.create(null), BorderLayout.CENTER);
		
		distinctEdgesCheckbox = new JCheckBox("Create separate edges for each dataset");
		SwingUtil.makeSmall(distinctEdgesCheckbox);
		
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		
		layout.setHorizontalGroup(
			layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
					.addComponent(titlePanel)
					.addComponent(scrollPane, 250, 250, 250)
				)
				.addGroup(layout.createParallelGroup()
					.addComponent(distinctEdgesCheckbox, Alignment.TRAILING)
					.addComponent(dataSetDetailHolder, 0, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
				)
		);
		
		layout.setVerticalGroup(
			layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.TRAILING)
					.addComponent(titlePanel)
					.addComponent(distinctEdgesCheckbox)
				)
				.addGroup(layout.createParallelGroup()
					.addComponent(scrollPane)
					.addComponent(dataSetDetailHolder)
				)
		);
		
		return panel;
	}
	
	
	private JPanel createTitlePanel() {
		JLabel label = new JLabel("Data Sets:");
		SwingUtil.makeSmall(label);
		
		JButton addButton    = SwingUtil.createIconButton(iconManager, IconManager.ICON_PLUS,     "Add Data Set");
		JButton scanButton   = SwingUtil.createIconButton(iconManager, IconManager.ICON_FOLDER_O, "Scan Folder for Data Sets");
		JButton deleteButton = SwingUtil.createIconButton(iconManager, IconManager.ICON_TRASH_O,  "Delete Data Set");
		
		addButton.addActionListener(e -> addNewDataSetToList());
		
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(false);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
			.addComponent(label)
//			.addGap(0, 0, Short.MAX_VALUE)
			.addComponent(scanButton)
			.addComponent(addButton)
			.addComponent(deleteButton)
		);
		
		layout.setVerticalGroup(layout.createParallelGroup(Alignment.BASELINE)
			.addComponent(label)
			.addComponent(scanButton)
			.addComponent(addButton)
			.addComponent(deleteButton)
		);
		
		return panel;
	}
	
	
	private void addNewDataSetToList() {
		int n = 1 + dataSetListModel.size();
		DataSetParameters params = new DataSetParameters("Data Set " + n, Method.GSEA, new DataSetFiles());
		dataSetListModel.addElement(params);
		dataSetMasterList.setSelectedValue(params, true);
	}
	
	
	private void editDataSet(DataSetParameters params) {
		dataSetDetailHolder.removeAll();
		JPanel editPanel = dataSetPanelFactory.create(params);
		dataSetDetailHolder.add(editPanel, BorderLayout.CENTER);
		dataSetDetailHolder.revalidate();
	}
	
	
	private class DataSetList extends JList<DataSetParameters> {
		
		@Inject
		public DataSetList(ListModel<DataSetParameters> model) {
			setModel(model);
			setCellRenderer(new CellRenderer());
			setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		}
		
		private class CellRenderer implements ListCellRenderer<DataSetParameters> {

			@Override
			public Component getListCellRendererComponent(JList<? extends DataSetParameters> list,
					DataSetParameters dataSet, int index, boolean isSelected, boolean cellHasFocus) {
				
				Color bgColor = UIManager.getColor(isSelected ? "Table.selectionBackground" : "Table.background");
				Color fgColor = UIManager.getColor(isSelected ? "Table.selectionForeground" : "Table.foreground");
				
				JLabel iconLabel = new JLabel(" " + IconManager.ICON_FILE_TEXT + "  ");
				iconLabel.setFont(iconManager.getIconFont(13.0f));
				iconLabel.setForeground(fgColor);
				
				String title = dataSet.getName() + "  (" + dataSet.getMethod().getLabel() + ")";
				JLabel titleLabel = new JLabel(title);
				SwingUtil.makeSmall(titleLabel);
				titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
				titleLabel.setForeground(fgColor);
				
				JPanel panel = new JPanel(new BorderLayout());
				panel.add(iconLabel, BorderLayout.WEST);
				panel.add(titleLabel, BorderLayout.CENTER);
				
				panel.setBackground(bgColor);
				
				Border emptyBorder = BorderFactory.createEmptyBorder(2, 4, 2, 4);
				Border lineBorder  = BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Separator.foreground"));
				Border compound = BorderFactory.createCompoundBorder(lineBorder, emptyBorder);
				panel.setBorder(compound);
				
				return panel;
			}
		}
	}
	
}
