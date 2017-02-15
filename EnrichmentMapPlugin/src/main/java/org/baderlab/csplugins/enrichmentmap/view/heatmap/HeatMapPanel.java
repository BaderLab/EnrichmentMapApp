package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.baderlab.csplugins.enrichmentmap.AfterInjection;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParams.Sort;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParams.Transform;
import org.baderlab.csplugins.enrichmentmap.view.util.ComboItem;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.cytoscape.application.swing.CytoPanelComponent2;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
@SuppressWarnings("serial")
public class HeatMapPanel extends JPanel implements CytoPanelComponent2 {

	public static final String ID = "enrichmentmap.view.ExpressionViewerPanel";
	
	private static final int COLUMN_WIDTH_COLOR = 10;
	private static final int COLUMN_WIDTH_VALUE = 50;
	
	@Inject private Provider<ExportTXTAction> txtActionProvider;
	@Inject private Provider<ExportPDFAction> pdfActionProvider;
	
	@Inject private IconManager iconManager;
	
	private JTable table;
	private SettingsPopupPanel settingsPanel;
	private JComboBox<ComboItem<Transform>> normCombo;
	private JComboBox<ComboItem<Sort>> sortCombo;
	
	
	@AfterInjection
	private void createContents() {
		// MKTODO get the HeatMapParameters from the EnrichmentMapManager
		HeatMapParams params = HeatMapParams.defaults();
		settingsPanel = new SettingsPopupPanel(params);
		settingsPanel.setShowValuesListener(this::updateTableCellRenderer);
		
		JPanel toolbarPanel = createToolbarPanel();
		JPanel expressionPanel = createExpressionPanel();

		setLayout(new BorderLayout());
		add(toolbarPanel, BorderLayout.NORTH);
		add(expressionPanel, BorderLayout.CENTER);
		setOpaque(false);
	}
	
	private JPanel createToolbarPanel() {
		JLabel title = new JLabel(" Expression Data");
		JLabel normLabel = new JLabel("Normalization:");
		normCombo = new JComboBox<>();
		JLabel sortLabel = new JLabel("Sorting:");
		sortCombo = new JComboBox<>();
		JButton sortDirectionButton = createSortDirectionButton();
		
		SwingUtil.makeSmall(title, normLabel, normCombo, sortLabel, sortCombo);
		
		normCombo.addItem(new ComboItem<>(Transform.AS_IS, "None"));
		normCombo.addItem(new ComboItem<>(Transform.ROW_NORMALIZE, "Row Normalize"));
		normCombo.addItem(new ComboItem<>(Transform.LOG_TRANSFORM, "Log Transform"));
		normCombo.setSelectedItem(ComboItem.of(Transform.AS_IS));
		
		sortCombo.addItem(new ComboItem<>(Sort.CLUSTER, "Hierarchical Cluster"));
		sortCombo.addItem(new ComboItem<>(Sort.RANKS, "Ranks"));
		sortCombo.addItem(new ComboItem<>(Sort.COLUMN, "Column"));
		sortCombo.setSelectedItem(ComboItem.of(Sort.CLUSTER));
		
		normCombo.addActionListener(e -> {
			Transform t = normCombo.getItemAt(normCombo.getSelectedIndex()).getValue();
			HeatMapTableModel tableModel = (HeatMapTableModel) table.getModel();
			table.setDefaultRenderer(Double.class, new ColorRenderer()); // clear cached data used by the ColorRenderer
			updateTableCellRenderer(settingsPanel.isShowValues());
			tableModel.setTransform(t);
		});
		
		JButton gearButton = createIconButton(IconManager.ICON_GEAR, "Settings");
		JButton menuButton = createIconButton(IconManager.ICON_EXTERNAL_LINK, "Export");
		LookAndFeelUtil.equalizeSize(gearButton, menuButton);
		gearButton.addActionListener(this::showSettings);
		menuButton.addActionListener(this::showMenu);
		
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
			.addComponent(title)
			.addGap(0, 0, Short.MAX_VALUE)
			.addComponent(normLabel)
			.addComponent(normCombo, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			.addGap(10, 10, 10)
			.addComponent(sortLabel)
			.addComponent(sortCombo, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			.addComponent(sortDirectionButton)
			.addGap(10, 10, 10)
			.addComponent(gearButton)
			.addComponent(menuButton)
		);
		layout.setVerticalGroup(layout.createParallelGroup(Alignment.BASELINE)
			.addComponent(title)
			.addComponent(normLabel)
			.addComponent(normCombo)
			.addComponent(sortLabel)
			.addComponent(sortCombo)
			.addComponent(sortDirectionButton)
			.addComponent(gearButton)
			.addComponent(menuButton)
		);
		
		return panel;
	}


	private JPanel createExpressionPanel() {
		table = new JTable();
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		table.setFillsViewportHeight(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(scrollPane, BorderLayout.CENTER);
		panel.setOpaque(false);
		return panel;
	}
	
	
	private JButton createIconButton(String icon, String toolTip) {
		JButton iconButton = new JButton(icon);
		iconButton.setFont(iconManager.getIconFont(13.0f));
		iconButton.setToolTipText(toolTip);

		if(LookAndFeelUtil.isAquaLAF()) {
			iconButton.putClientProperty("JButton.buttonType", "gradient");
			iconButton.putClientProperty("JComponent.sizeVariant", "small");
		}
		return iconButton;
	}
	
	
	private JButton createSortDirectionButton() {
		JButton sortButton = new JButton(IconManager.ICON_CARET_UP);
		sortButton.setFont(iconManager.getIconFont(10.0f));
		sortButton.setToolTipText("Sort direction...");
		sortButton.setBorderPainted(false);
		sortButton.setContentAreaFilled(false);
		sortButton.setFocusPainted(false);
		sortButton.setBorder(BorderFactory.createEmptyBorder());
		return sortButton;
	}
	
	
	public void update(EnrichmentMap map, Set<String> genes) {
		List<String> geneList = new ArrayList<>(genes);
		geneList.sort(Comparator.naturalOrder());
		HeatMapTableModel tableModel = new HeatMapTableModel(map, geneList, Transform.AS_IS);
		table.setModel(tableModel);
		table.setDefaultRenderer(Double.class, new ColorRenderer());
		createTableHeader(COLUMN_WIDTH_COLOR);
	}
	
	private void createTableHeader(int width) {
		HeatMapTableModel tableModel = (HeatMapTableModel)table.getModel();
		TableColumnModel columnModel = table.getColumnModel();
		
		int colCount = tableModel.getColumnCount();
		ColumnHeaderVerticalRenderer vertRenderer = new ColumnHeaderVerticalRenderer();
		for(int i = 1; i < colCount; i++) {
			TableColumn column = columnModel.getColumn(i);
			column.setHeaderRenderer(vertRenderer);
			column.setPreferredWidth(width);
		}
	}
	
	private void updateTableCellRenderer(boolean showValues) {
		table.setDefaultRenderer(Double.class, showValues ? new ColorAndValueRenderer() : new ColorRenderer());
		createTableHeader(showValues ? COLUMN_WIDTH_VALUE : COLUMN_WIDTH_COLOR);
		table.revalidate();
	}

	private void showSettings(ActionEvent event) {
		JPopupMenu menu = new JPopupMenu();
		menu.add(settingsPanel);
		menu.addMouseListener(new MouseAdapter() {
			@Override public void mouseClicked(MouseEvent e) {
				if(SwingUtilities.isRightMouseButton(e)) {
					menu.setVisible(false);
				}
			}
		});
//		menu.addPopupMenuListener(new PopupMenuListener() {
//			@Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) { }
//			@Override public void popupMenuCanceled(PopupMenuEvent e) { }
//			@Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
//				// Do stuff here
//			}
//		});
		Component c = (Component) event.getSource();
		menu.show(c, 0, c.getHeight());
	}
	
	private void showMenu(ActionEvent event)  {
		JPopupMenu menu = new JPopupMenu();
		menu.add(txtActionProvider.get());
		menu.add(pdfActionProvider.get());
		Component c = (Component) event.getSource();
		menu.show(c, 0, c.getHeight());
	}
	
	
	
	
	
	@Override
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.SOUTH;
	}

	@Override
	public String getTitle() {
		return "EnrichmentMap: Expression Viewer";
	}

	@Override
	public Icon getIcon() {
		String path = "org/baderlab/csplugins/enrichmentmap/view/enrichmentmap_logo_notext_small.png";
		URL url = getClass().getClassLoader().getResource(path);
		return url == null ? null : new ImageIcon(url);
	}

	@Override
	public String getIdentifier() {
		return ID;
	}

	@Override
	public Component getComponent() {
		return this;
	}
}
