package org.baderlab.csplugins.enrichmentmap.view.mastermap;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.Iterator;
import java.util.stream.Stream;

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

@SuppressWarnings("serial")
public class MasterDetailDialogPage implements CardDialogPage {

	@Inject private IconManager iconManager;
	
	@Inject private CutoffPropertiesPanel cutoffPanel;
	@Inject private EditCommonPropertiesPanel.Factory commonPanelFactory;
	@Inject private EditDataSetPanel.Factory dataSetPanelFactory;
	
	
	private DataSetListItem commonParams;
	
	private DataSetList dataSetList;
	private IterableListModel<DataSetListItem> dataSetListModel;
	private JPanel dataSetDetailPanel;
	private CardLayout cardLayout;
	
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
		
		commonParams = new DataSetListItem() {
			private final JPanel panel = commonPanelFactory.create(null);
			@Override String getIcon()  { return IconManager.ICON_FILE_O; }
			@Override String getName()  { return "Common Files"; }
			@Override JPanel getPanel() { return panel; }
		};
				
		JPanel dataPanel = createDataSetPanel();
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(dataPanel, BorderLayout.CENTER);
		panel.add(cutoffPanel, BorderLayout.SOUTH);
		
		this.callback.setFinishButtonEnabled(false);
		return panel;
	}

	
	
	private JPanel createDataSetPanel() {
		JPanel titlePanel = createTitlePanel();
		
		dataSetListModel = new IterableListModel<>();
		dataSetList = new DataSetList(dataSetListModel);
		dataSetList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		dataSetList.addListSelectionListener(e -> {
			DataSetListItem item = dataSetList.getSelectedValue();
			editDataSet(item);
		});
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(dataSetList);
		
		dataSetDetailPanel = new JPanel(new BorderLayout());
		dataSetDetailPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY)); // MKTODO get the color properly
		cardLayout = new CardLayout();
		dataSetDetailPanel.setLayout(cardLayout);
		
		// Blank page
		dataSetDetailPanel.add(new EditNothingPanel(), "nothing");
		
		// Common page
		dataSetListModel.addElement(commonParams);
		dataSetDetailPanel.add(commonParams.getPanel(), commonParams.id);
		
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
					.addComponent(dataSetDetailPanel, 0, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
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
					.addComponent(dataSetDetailPanel)
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
		int n = dataSetListModel.size();
		DataSetParameters params = new DataSetParameters("Data Set " + n, Method.GSEA, new DataSetFiles());
		EditDataSetPanel panel = dataSetPanelFactory.create(params);
		
		panel.addPropertyChangeListener(EditDataSetPanel.PROP_NAME, e ->
			((IterableListModel<?>)dataSetList.getModel()).update()
		);
		
		DataSetListItem item = new DataSetListItem() {
			@Override JPanel getPanel() { return panel; }
			@Override String getName()  { return panel.getDisplayName(); }
			@Override String getIcon()  { return IconManager.ICON_FILE_TEXT_O; }
		};
		
		dataSetListModel.addElement(item);
		dataSetDetailPanel.add(panel, item.id);
		
		dataSetList.setSelectedValue(params, true);
	}
	
	
	private void editDataSet(DataSetListItem params) {
		if(params == null)
			cardLayout.show(dataSetDetailPanel, "nothing");
		else 
			cardLayout.show(dataSetDetailPanel, params.id);
	}
	
	
	private static abstract class DataSetListItem {
		private static final Iterator<String> idGenerator = Stream.iterate(0, x -> x + 1).map(String::valueOf).iterator();
		public final String id = idGenerator.next();
		
		abstract JPanel getPanel();
		abstract String getName();
		abstract String getIcon();
	}
	
	private class DataSetList extends JList<DataSetListItem> {
		
		@Inject
		public DataSetList(ListModel<DataSetListItem> model) {
			setModel(model);
			setCellRenderer(new CellRenderer());
			setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		}
		
		private class CellRenderer implements ListCellRenderer<DataSetListItem> {

			@Override
			public Component getListCellRendererComponent(JList<? extends DataSetListItem> list,
					DataSetListItem dataSet, int index, boolean isSelected, boolean cellHasFocus) {
				
				Color bgColor = UIManager.getColor(isSelected ? "Table.selectionBackground" : "Table.background");
				Color fgColor = UIManager.getColor(isSelected ? "Table.selectionForeground" : "Table.foreground");
				
				JLabel iconLabel = new JLabel(" " + dataSet.getIcon() + "  ");
				iconLabel.setFont(iconManager.getIconFont(13.0f));
				iconLabel.setForeground(fgColor);
				
				String title = dataSet.getName();
				JLabel titleLabel = new JLabel(title);
				SwingUtil.makeSmall(titleLabel);
				titleLabel.setForeground(fgColor);
				
				JPanel panel = new JPanel(new BorderLayout());
				panel.add(iconLabel, BorderLayout.WEST);
				panel.add(titleLabel, BorderLayout.CENTER);
				
				panel.setBackground(bgColor);
				
				Border emptyBorder = BorderFactory.createEmptyBorder(2, 4, 2, 4);
				panel.setBorder(emptyBorder);
				
				return panel;
			}
		}
	}
	
}
