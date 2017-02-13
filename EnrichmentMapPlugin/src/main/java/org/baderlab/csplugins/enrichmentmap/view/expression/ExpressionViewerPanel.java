package org.baderlab.csplugins.enrichmentmap.view.expression;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

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
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.baderlab.csplugins.enrichmentmap.AfterInjection;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.view.expression.ExpressionViewerParams.Transform;
import org.baderlab.csplugins.enrichmentmap.view.util.ComboItem;
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
	
	private static final int COLUMN_WIDTH_COLOR = 10;
	private static final int COLUMN_WIDTH_VALUE = 50;
	
	@Inject private IconManager iconManager;
	
	private JTable table;
	
	
	@AfterInjection
	private void createContents() {
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
		JComboBox<ComboItem<Transform>> normCombo = new JComboBox<>();
		JLabel sortLabel = new JLabel("Sorting:");
		JComboBox<String> sortCombo = new JComboBox<>();
		SwingUtil.makeSmall(title, normLabel, normCombo, sortLabel, sortCombo);
		
		normCombo.addItem(new ComboItem<>(Transform.AS_IS, "None"));
		normCombo.addItem(new ComboItem<>(Transform.ROW_NORMALIZE, "Row Normalize"));
		normCombo.addItem(new ComboItem<>(Transform.LOG_TRANSFORM, "Log Transform"));
		normCombo.setSelectedItem(ComboItem.of(Transform.AS_IS));
		
		normCombo.addActionListener(e -> {
			Transform t = normCombo.getItemAt(normCombo.getSelectedIndex()).getValue();
			ExpressionTableModel tableModel = (ExpressionTableModel) table.getModel();
			table.setDefaultRenderer(Double.class, new ColorRenderer()); // clear cached data used by the ColorRenderer
			tableModel.setTransform(t);
		});
		
		JButton gearButton = createIconButton(IconManager.ICON_GEAR, "Settings");
		JButton menuButton = createIconButton(IconManager.ICON_BARS, "Extras");
		LookAndFeelUtil.equalizeSize(gearButton, menuButton);
		
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
			.addComponent(gearButton)
			.addComponent(menuButton)
		);
		layout.setVerticalGroup(layout.createParallelGroup(Alignment.BASELINE)
			.addComponent(title)
			.addComponent(normLabel)
			.addComponent(normCombo)
			.addComponent(sortLabel)
			.addComponent(sortCombo)
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
	
	
	public void update(EnrichmentMap map, Set<String> genes) {
		List<String> geneList = new ArrayList<>(genes);
		geneList.sort(Comparator.naturalOrder());
		ExpressionTableModel tableModel = new ExpressionTableModel(map, geneList, Transform.AS_IS);
		table.setModel(tableModel);
		table.setDefaultRenderer(Double.class, new ColorRenderer());
		createTableHeader();
	}
	
	
	private void createTableHeader() {
		ExpressionTableModel tableModel = (ExpressionTableModel)table.getModel();
		TableColumnModel columnModel = table.getColumnModel();
		
		int colCount = tableModel.getColumnCount();
		ColumnHeaderVerticalRenderer vertRenderer = new ColumnHeaderVerticalRenderer();
		for(int i = 1; i < colCount; i++) {
			TableColumn column = columnModel.getColumn(i);
			column.setHeaderRenderer(vertRenderer);
			column.setPreferredWidth(COLUMN_WIDTH_COLOR);
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
