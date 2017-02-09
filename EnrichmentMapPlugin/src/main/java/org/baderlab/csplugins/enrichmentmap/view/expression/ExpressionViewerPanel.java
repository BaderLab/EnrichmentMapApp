package org.baderlab.csplugins.enrichmentmap.view.expression;

import java.awt.BorderLayout;
import java.awt.Component;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.table.TableColumnModel;

import org.baderlab.csplugins.enrichmentmap.AfterInjection;
import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.view.expression.ExpressionViewerParams.Transform;
import org.baderlab.csplugins.enrichmentmap.view.util.CheckboxListPanel;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.cytoscape.application.swing.CytoPanelComponent2;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
@SuppressWarnings("serial")
public class ExpressionViewerPanel extends JPanel implements CytoPanelComponent2 {

	public static final String ID = "enrichmentmap.view.ExpressionViewerPanel";
	
	@Inject private IconManager iconManager;
	
	private JToggleButton syncButton;
	private JButton gearButton;
	private JButton menuButton;
	
	private JTable table;
	private JScrollPane scrollPane;
	private CheckboxListPanel<DataSet> dataSetList;
	
	
	@AfterInjection
	private void createContents() {
//		JPanel dataSetPanel    = createDataSetPanel();
		JPanel expressionPanel = createExpressionPanel();

		LookAndFeelUtil.equalizeSize(gearButton, menuButton);

//		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, dataSetPanel, expressionPanel);
//		splitPane.setBorder(BorderFactory.createEmptyBorder());
		
		setLayout(new BorderLayout());
		add(expressionPanel, BorderLayout.CENTER);
		setOpaque(false);
	}


	private JPanel createDataSetPanel() {
		JLabel title = new JLabel("Data Sets");
		SwingUtil.makeSmall(title);
		syncButton = createIconToggleButton(IconManager.ICON_EXCHANGE, "Sync selection with control panel");
		CheckboxListPanel<DataSet> dataSetList = new CheckboxListPanel<>();
		
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addGroup(layout.createSequentialGroup()
				.addComponent(title)
				.addGap(0, 0, Short.MAX_VALUE)
				.addComponent(syncButton)
			)
			.addComponent(dataSetList)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup()
				.addComponent(title)
				.addComponent(syncButton)
			)
			.addComponent(dataSetList)
		);
		
		return panel;
	}

	private JPanel createExpressionPanel() {
		JLabel title = new JLabel(" Expression Data");
		SwingUtil.makeSmall(title);
		
		table = new JTable();
		
		scrollPane = new JScrollPane(table);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		table.setFillsViewportHeight(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		gearButton = createIconButton(IconManager.ICON_GEAR, "Settings");
		menuButton = createIconButton(IconManager.ICON_BARS, "Extras");
		
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addGroup(layout.createSequentialGroup()
				.addComponent(title)
				.addGap(0, 0, Short.MAX_VALUE)
				.addComponent(gearButton)
				.addComponent(menuButton)
			)
			.addComponent(scrollPane)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(title)
				.addComponent(gearButton)
				.addComponent(menuButton)
			)
			.addComponent(scrollPane)
		);
		
		panel.setOpaque(false);
		return panel;
	}
	
	
	private JToggleButton createIconToggleButton(String icon, String toolTip) {
		JToggleButton iconButton = new JToggleButton(icon);
		iconButton.setFont(iconManager.getIconFont(13.0f));
		iconButton.setToolTipText(toolTip);

		if(LookAndFeelUtil.isAquaLAF()) {
			iconButton.putClientProperty("JButton.buttonType", "gradient");
			iconButton.putClientProperty("JComponent.sizeVariant", "small");
		}
		return iconButton;
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
	
	
	public void update(EnrichmentMap map, Set<String> genes) {
		List<String> geneList = new ArrayList<>(genes);
		geneList.sort(Comparator.naturalOrder());
		ExpressionTableModel tableModel = new ExpressionTableModel(new ExpressionViewerParams(Transform.AS_IS), map, geneList);
		table.setModel(tableModel);
		createTableHeader();
	}
	
	
	private void createTableHeader() {
		ExpressionTableModel tableModel = (ExpressionTableModel)table.getModel();
		TableColumnModel columnModel = table.getColumnModel();
		
		int colCount = tableModel.getColumnCount();
		ColumnHeaderVerticalRenderer vertRenderer = new ColumnHeaderVerticalRenderer();
		for(int i = 1; i < colCount; i++) {
			columnModel.getColumn(i).setHeaderRenderer(vertRenderer);
		}
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
