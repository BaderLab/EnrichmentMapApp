package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
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
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParams.Distance;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParams.Operator;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParams.Transform;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.ColorAndValueRenderer;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.ColorRenderer;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.ColumnHeaderVerticalRenderer;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.DataSetColorRange;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.HeatMapTableModel;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.RankValue;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.RankValueRenderer;
import org.baderlab.csplugins.enrichmentmap.view.util.ComboItem;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.mskcc.colorgradient.ColorGradientWidget;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

@SuppressWarnings("serial")
public class HeatMapMainPanel extends JPanel {

	private static final int COLUMN_WIDTH_COLOR = 10;
	private static final int COLUMN_WIDTH_VALUE = 50;
	
	@Inject private Provider<ExportTXTAction> txtActionProvider;
	@Inject private Provider<ExportPDFAction> pdfActionProvider;
	
	@Inject private IconManager iconManager;
	
	private JPanel legendHolder;
	private JTable table;
	private JScrollPane scrollPane;
	private SettingsPopupPanel settingsPanel;
	private JComboBox<ComboItem<Transform>> normCombo;
	private JComboBox<ComboItem<Operator>> operatorCombo;
	private JComboBox<RankingOption> rankOptionCombo;
	
	private ActionListener rankOptionActionListener;
	private ActionListener normActionListener;
	private ActionListener operatorActionListener;
	
	private ClusterRankingOption clusterRankOption = null;
	private List<String> unionGenes;
	private List<String> interGenes;
	
	private final HeatMapParentPanel parent;
	private boolean isResetting = false;
	
	public interface Factory {
		HeatMapMainPanel create(HeatMapParentPanel parent);
	}
	
	
	@Inject
	public HeatMapMainPanel(@Assisted HeatMapParentPanel parent) {
		this.parent = parent;
	}
	
	private void settingChanged() {
		if(!isResetting)
			parent.settingsChanged(buildParams());
	}
	
	
	@AfterInjection
	private void createContents() {
		settingsPanel = new SettingsPopupPanel();
		settingsPanel.setShowValuesConsumer(this::updateSetting_ShowValues);
		settingsPanel.setDistanceConsumer(this::updateSetting_Distance);
		
		JPanel toolbarPanel = createToolbarPanel();
		JPanel expressionPanel = createTablePanel();
		
		setLayout(new BorderLayout());
		add(toolbarPanel, BorderLayout.NORTH);
		add(expressionPanel, BorderLayout.CENTER);
		setOpaque(false);
	}
	
	private JPanel createToolbarPanel() {
		legendHolder = new JPanel(new BorderLayout());
		legendHolder.setOpaque(false);
		
		JLabel operatorLabel = new JLabel("Genes:");
		operatorCombo = new JComboBox<>();
		JLabel normLabel = new JLabel("Normalize:");
		normCombo = new JComboBox<>();
		JLabel sortLabel = new JLabel("Sort:");
		rankOptionCombo = new JComboBox<>();
		
		SwingUtil.makeSmall(operatorLabel, operatorCombo, normLabel, normCombo, sortLabel, rankOptionCombo);
		
		operatorCombo.addItem(new ComboItem<>(Operator.UNION, "Union"));
		operatorCombo.addItem(new ComboItem<>(Operator.INTERSECTION, "Intersection"));
		operatorCombo.setSelectedItem(ComboItem.of(Operator.UNION));
		
		normCombo.addItem(new ComboItem<>(Transform.AS_IS, "None"));
		normCombo.addItem(new ComboItem<>(Transform.ROW_NORMALIZE, "Row"));
		normCombo.addItem(new ComboItem<>(Transform.LOG_TRANSFORM, "Log"));
		normCombo.setSelectedItem(ComboItem.of(Transform.AS_IS));
		
		operatorCombo.addActionListener(operatorActionListener = e -> updateSetting_Operator(getOperator()));
		normCombo.addActionListener(normActionListener = e -> updateSetting_Transform(getTransform()));
		rankOptionCombo.addActionListener(rankOptionActionListener = e -> updateSetting_RankOption(getRankingOption()));
		
		JButton gearButton = createIconButton(IconManager.ICON_GEAR, "Settings");
		JButton menuButton = createIconButton(IconManager.ICON_EXTERNAL_LINK, "Export");
		LookAndFeelUtil.equalizeSize(gearButton, menuButton);
		gearButton.addActionListener(this::showSettingsPopup);
		menuButton.addActionListener(this::showMenu);
		
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
			.addComponent(legendHolder, 180, 180, 180)
			.addGap(0, 0, Short.MAX_VALUE)
			.addComponent(operatorLabel)
			.addComponent(operatorCombo, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			.addGap(5)
			.addComponent(normLabel)
			.addComponent(normCombo, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			.addGap(5)
			.addComponent(sortLabel)
			.addComponent(rankOptionCombo, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			.addGap(5)
			.addComponent(gearButton)
			.addComponent(menuButton)
		);
		layout.setVerticalGroup(layout.createParallelGroup(Alignment.BASELINE)
			.addComponent(legendHolder)
			.addComponent(operatorLabel)
			.addComponent(operatorCombo)
			.addComponent(normLabel)
			.addComponent(normCombo)
			.addComponent(sortLabel)
			.addComponent(rankOptionCombo)
			.addComponent(gearButton)
			.addComponent(menuButton)
		);
		
		panel.setOpaque(false);
		return panel;
	}


	private JPanel createTablePanel() {
		table = new JTable();
		scrollPane = new JScrollPane(table);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		table.setFillsViewportHeight(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setCellSelectionEnabled(true);
		table.setAutoCreateRowSorter(true);
		
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Point p = e.getPoint();
				int row = table.rowAtPoint(p);
				int col = table.columnAtPoint(p);
				renderLegend(row, col);
			}
		});
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(scrollPane, BorderLayout.CENTER);
		panel.setOpaque(false);
		return panel;
	}
	
	
	private void createTableHeader(int width) {
		HeatMapTableModel tableModel = (HeatMapTableModel)table.getModel();
		TableColumnModel columnModel = table.getColumnModel();
		
		int colCount = tableModel.getColumnCount();
		ColumnHeaderVerticalRenderer vertRenderer = new ColumnHeaderVerticalRenderer();
		for(int i = HeatMapTableModel.DESC_COL_COUNT; i < colCount; i++) {
			TableColumn column = columnModel.getColumn(i);
			column.setHeaderRenderer(vertRenderer);
			column.setPreferredWidth(width);
		}
		
	}
	
	
	private void renderLegend(int row, int col) {
		HeatMapTableModel tableModel = (HeatMapTableModel) table.getModel();
		Object value = tableModel.getValueAt(row, col);
		
		if(value instanceof Double) {
			EMDataSet dataset = tableModel.getDataSet(col);
			ColorRenderer renderer = (ColorRenderer) table.getCellRenderer(row, col);
			DataSetColorRange colorRange = renderer.getRange(dataset, tableModel.getTransform());
			
			JPanel panel = createExpressionLegendPanel(colorRange);
			legendHolder.removeAll();
			legendHolder.add(panel, BorderLayout.CENTER);
			legendHolder.revalidate();
		}
	}
	
	
	private static JPanel createExpressionLegendPanel(DataSetColorRange range) {
		JPanel panel = new JPanel();
		
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());

		ParallelGroup hGroup = layout.createParallelGroup(Alignment.CENTER, true);
		SequentialGroup vGroup = layout.createSequentialGroup();
		layout.setHorizontalGroup(hGroup);
		layout.setVerticalGroup(vGroup);

		ColorGradientWidget legend = ColorGradientWidget.getInstance("", range.getTheme(),
				range.getRange(), true, ColorGradientWidget.LEGEND_POSITION.NA);

		hGroup.addComponent(legend, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE);
		vGroup.addComponent(legend, 25, 25, 25);
		
		if (LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);

		panel.revalidate();
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
	
	
	public Operator getOperator() {
		return operatorCombo.getItemAt(operatorCombo.getSelectedIndex()).getValue();
	}
	
	public Transform getTransform() {
		return normCombo.getItemAt(normCombo.getSelectedIndex()).getValue();
	}
	
	public RankingOption getRankingOption() {
		return rankOptionCombo.getItemAt(rankOptionCombo.getSelectedIndex());
	}
	
	public boolean isShowValues() {
		return settingsPanel.isShowValues();
	}
	
	public Distance getDistance() {
		return settingsPanel.getDistance();
	}
	
	public int getRankingIndex() {
		return rankOptionCombo.getSelectedIndex();
	}
	
	public HeatMapParams buildParams() {
		return new HeatMapParams.Builder()
				.setDistanceMetric(getDistance())
				.setOperator(getOperator())
				.setShowValues(isShowValues())
				.setSortIndex(getRankingIndex())
				.setTransform(getTransform())
				.build();
	}
	
	
	public void reset(EnrichmentMap map, HeatMapParams params, ClusterRankingOption clusterRankOption, List<RankingOption> moreRankOptions, Set<String> union, Set<String> intersection) {
		isResetting = true;
		this.clusterRankOption = clusterRankOption;
		
		unionGenes = new ArrayList<>(union);
		unionGenes.sort(Comparator.naturalOrder());
		interGenes = new ArrayList<>(intersection);
		interGenes.sort(Comparator.naturalOrder());
		
		// Update Combo Boxes
		operatorCombo.removeActionListener(operatorActionListener);
		rankOptionCombo.removeActionListener(rankOptionActionListener);
		normCombo.removeActionListener(normActionListener);
		
		operatorCombo.removeAllItems();
		operatorCombo.addItem(new ComboItem<>(Operator.UNION, "Union (" + union.size() + ")"));
		operatorCombo.addItem(new ComboItem<>(Operator.INTERSECTION, "Intersection (" + intersection.size() + ")"));
		operatorCombo.setSelectedItem(ComboItem.of(params.getOperator()));
		
		normCombo.setSelectedItem(ComboItem.of(params.getTransform()));
		
		rankOptionCombo.removeAllItems();
		rankOptionCombo.addItem(RankingOption.none());
		rankOptionCombo.addItem(clusterRankOption);
		moreRankOptions.forEach(rankOptionCombo::addItem);
		rankOptionCombo.setSelectedIndex(params.getSortIndex());

		settingsPanel.update(params);
		
		List<String> genesToUse = params.getOperator() == Operator.UNION ? unionGenes : interGenes;
		
		HeatMapTableModel tableModel = new HeatMapTableModel(map, null, genesToUse, params.getTransform());
		table.setModel(tableModel);
		updateSetting_ShowValues(settingsPanel.isShowValues());
		createTableHeader(COLUMN_WIDTH_COLOR);
		
		// Re-compute the ranking
		rankOptionActionListener.actionPerformed(null);
		
		operatorCombo.addActionListener(operatorActionListener);
		rankOptionCombo.addActionListener(rankOptionActionListener);
		normCombo.addActionListener(normActionListener);
		
		isResetting = false;
	}
	
	private List<String> getGenes(Operator operator) {
		switch(operator) {
			case UNION: default: return unionGenes;
			case INTERSECTION:   return interGenes;
		}
	}
	
	
	private void updateSetting_Distance(Distance distance) {
		clusterRankOption.setDistance(distance);
		if(rankOptionCombo.getSelectedItem() == clusterRankOption) {
			updateSetting_RankOption(clusterRankOption);
		}
		settingChanged();
	}
	
	private void updateSetting_Operator(Operator oper) {
		HeatMapTableModel tableModel = (HeatMapTableModel) table.getModel();
		tableModel.setGenes(getGenes(oper));
		settingChanged();
	}
	
	private void updateSetting_RankOption(RankingOption rankOption) {
		//rankOptionCombo.setEnabled(false);
		List<String> genes = getGenes(getOperator());
		CompletableFuture<Map<Integer,RankValue>> rankingFuture = rankOption.computeRanking(genes);
		if(rankingFuture != null) {
			rankingFuture.whenComplete((ranking, ex) -> {
				HeatMapTableModel tableModel = (HeatMapTableModel) table.getModel();
				tableModel.setRanking(ranking);
				//rankOptionCombo.setEnabled(true);
			});
		}
		settingChanged();
	}
	
	private void updateSetting_ShowValues(boolean showValues) {
		table.setDefaultRenderer(Double.class, showValues ? new ColorAndValueRenderer() : new ColorRenderer());
		table.setDefaultRenderer(RankValue.class, new RankValueRenderer());
		createTableHeader(showValues ? COLUMN_WIDTH_VALUE : COLUMN_WIDTH_COLOR);
		table.revalidate();
		settingChanged();
	}
	
	private void updateSetting_Transform(Transform transform) {
		updateSetting_ShowValues(settingsPanel.isShowValues()); // clear cached data used by the ColorRenderer
		HeatMapTableModel tableModel = (HeatMapTableModel) table.getModel();
		tableModel.setTransform(transform);
		settingChanged();
	}
	
	
	private void showSettingsPopup(ActionEvent event) {
		JPopupMenu menu = new JPopupMenu();
		menu.add(settingsPanel);
		menu.addMouseListener(new MouseAdapter() {
			@Override public void mouseClicked(MouseEvent e) {
				if(SwingUtilities.isRightMouseButton(e)) {
					menu.setVisible(false);
				}
			}
		});
		Component c = (Component) event.getSource();
		menu.show(c, 0, c.getHeight());
	}
	
	private void showMenu(ActionEvent event)  {
		JPopupMenu menu = new JPopupMenu();
		ExportTXTAction txtAction = txtActionProvider.get();
		ExportPDFAction pdfAction = pdfActionProvider.get();
		txtAction.setEnabled(false); // TEMPORARY
		pdfAction.setEnabled(false); // TEMPORARY
		menu.add(txtAction);
		menu.add(pdfAction);
		Component c = (Component) event.getSource();
		menu.show(c, 0, c.getHeight());
	}
	
}
