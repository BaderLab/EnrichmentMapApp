package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter.SortKey;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.baderlab.csplugins.enrichmentmap.AfterInjection;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParams.Distance;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParams.Operator;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParams.Transform;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.ColorAndValueRenderer;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.ColorRenderer;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.ColumnHeaderVerticalRenderer;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.GradientLegendPanel;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.HeatMapTableModel;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.RankValue;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.RankValueRenderer;
import org.baderlab.csplugins.enrichmentmap.view.util.ComboItem;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

@SuppressWarnings("serial")
public class HeatMapMainPanel extends JPanel {

	private static final int COLUMN_WIDTH_COLOR = 10;
	private static final int COLUMN_WIDTH_VALUE = 50;
	
	
	@Inject private ExportTXTAction.Factory txtActionFactory;
	@Inject private ExportPDFAction.Factory pdfActionFactory;
	@Inject private AddRanksDialog.Factory ranksDialogFactory;
	@Inject private IconManager iconManager;

	private GradientLegendPanel gradientLegendPanel;
	private SettingsPopupPanel settingsPanel;
	
	private JTable table;
	private JScrollPane scrollPane;
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
		if(!isResetting) {
			HeatMapParams params = buildParams();
			parent.getMediator().heatMapParamsChanged(params);
		}
	}
	
	
	@AfterInjection
	private void createContents() {
		settingsPanel = new SettingsPopupPanel();
		settingsPanel.setShowValuesConsumer(this::updateSetting_ShowValues);
		settingsPanel.setDistanceConsumer(this::updateSetting_Distance);
		
		JPanel expressionPanel = createTablePanel(); // must create table first
		JPanel toolbarPanel = createToolbarPanel();
		
		setLayout(new BorderLayout());
		add(toolbarPanel, BorderLayout.NORTH);
		add(expressionPanel, BorderLayout.CENTER);
		setOpaque(false);
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
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(scrollPane, BorderLayout.CENTER);
		panel.setOpaque(false);
		return panel;
	}
	
	
	private void createTableHeader(int width) {
		HeatMapTableModel tableModel = (HeatMapTableModel)table.getModel();
		TableColumnModel columnModel = table.getColumnModel();
		
		TableCellRenderer vertRenderer = new ColumnHeaderVerticalRenderer();
		TableCellRenderer vertRendererPheno1 = new ColumnHeaderVerticalRenderer(EMStyleBuilder.Colors.LIGHTEST_PHENOTYPE_1);
		TableCellRenderer vertRendererPheno2 = new ColumnHeaderVerticalRenderer(EMStyleBuilder.Colors.LIGHTEST_PHENOTYPE_2);
		
		int colCount = tableModel.getColumnCount();
		for(int col = HeatMapTableModel.DESC_COL_COUNT; col < colCount; col++) {
			EMDataSet dataset = tableModel.getDataSet(col);
			String pheno1 = dataset.getEnrichments().getPhenotype1();
			String pheno2 = dataset.getEnrichments().getPhenotype2();
			
			Optional<String> pheno = tableModel.getPhenotype(col);
			TableCellRenderer renderer;
			if(pheno.filter(p -> p.equals(pheno1)).isPresent())
				renderer = vertRendererPheno1;
			else if(pheno.filter(p -> p.equals(pheno2)).isPresent())
				renderer = vertRendererPheno2;
			else
				renderer = vertRenderer;
			
			TableColumn column = columnModel.getColumn(col);
			column.setHeaderRenderer(renderer);
			column.setPreferredWidth(width);
		}
	}
	
	
	private JPanel createToolbarPanel() {
		gradientLegendPanel = new GradientLegendPanel(table);
		
		JLabel operatorLabel = new JLabel("Genes:");
		operatorCombo = new JComboBox<>();
		JLabel normLabel = new JLabel("Normalize:");
		normCombo = new JComboBox<>();
		JLabel sortLabel = new JLabel("Ranks:");
		rankOptionCombo = new JComboBox<>();
		
		SwingUtil.makeSmall(operatorLabel, operatorCombo, normLabel, normCombo, sortLabel, rankOptionCombo);
		
		operatorCombo.addItem(new ComboItem<>(Operator.UNION, "Union"));
		operatorCombo.addItem(new ComboItem<>(Operator.INTERSECTION, "Intersection"));
		operatorCombo.setSelectedItem(ComboItem.of(Operator.UNION));
		
		normCombo.addItem(new ComboItem<>(Transform.AS_IS, "None"));
		normCombo.addItem(new ComboItem<>(Transform.ROW_NORMALIZE, "Row"));
		normCombo.addItem(new ComboItem<>(Transform.LOG_TRANSFORM, "Log"));
		normCombo.setSelectedItem(ComboItem.of(Transform.AS_IS));
		
		JButton plusButton = createPlusButton();
		plusButton.setToolTipText("Add Rankings...");
		plusButton.addActionListener(e -> addRankings());
		
		operatorCombo.addActionListener(operatorActionListener = e -> updateSetting_Operator(getOperator()));
		normCombo.addActionListener(normActionListener = e -> updateSetting_Transform(getTransform()));
		rankOptionCombo.addActionListener(rankOptionActionListener = e -> updateSetting_RankOption(getRankingOption()));
		
		JButton gearButton = SwingUtil.createIconButton(iconManager, IconManager.ICON_GEAR, "Settings");
		JButton menuButton = SwingUtil.createIconButton(iconManager, IconManager.ICON_EXTERNAL_LINK, "Export");
		LookAndFeelUtil.equalizeSize(gearButton, menuButton);
		gearButton.addActionListener(e -> settingsPanel.popup(gearButton));
		menuButton.addActionListener(this::showExportMenu);
		
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
			.addComponent(gradientLegendPanel, 180, 180, 180)
			.addGap(0, 0, Short.MAX_VALUE)
			.addComponent(operatorLabel)
			.addComponent(operatorCombo, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			.addGap(5)
			.addComponent(normLabel)
			.addComponent(normCombo, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			.addGap(5)
			.addComponent(sortLabel)
			.addComponent(rankOptionCombo, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			.addComponent(plusButton)
			.addGap(2)
			.addComponent(gearButton)
			.addComponent(menuButton)
		);
		layout.setVerticalGroup(layout.createParallelGroup(Alignment.BASELINE)
			.addComponent(gradientLegendPanel)
			.addComponent(operatorLabel)
			.addComponent(operatorCombo)
			.addComponent(normLabel)
			.addComponent(normCombo)
			.addComponent(sortLabel)
			.addComponent(rankOptionCombo)
			.addComponent(plusButton)
			.addComponent(gearButton)
			.addComponent(menuButton)
		);
		
		panel.setOpaque(false);
		return panel;
	}
	
	
	private JButton createPlusButton() {
		JButton button = new JButton(IconManager.ICON_PLUS);
		button.setFont(iconManager.getIconFont(10.0f));
		button.setBorder(null);
		button.setContentAreaFilled(false);
		button.setBorderPainted(false);
		int h = rankOptionCombo.getPreferredSize().height;
		button.setMinimumSize(new Dimension(h, h));
		button.setPreferredSize(new Dimension(h, h));
		return button;
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
	
	public String getRankingOptionName() {
		return rankOptionCombo.getSelectedItem().toString();
	}
	
	public HeatMapParams buildParams() {
		return new HeatMapParams.Builder()
				.setDistanceMetric(getDistance())
				.setOperator(getOperator())
				.setShowValues(isShowValues())
				.setRankingOptionName(getRankingOptionName())
				.setTransform(getTransform())
				.build();
	}
	
	
	public void reset(EnrichmentMap map, HeatMapParams params, List<RankingOption> moreRankOptions, Set<String> union, Set<String> intersection) {
		isResetting = true;
		this.clusterRankOption = parent.getMediator().getClusterRankOption(map);
		
		unionGenes = new ArrayList<>(union);
		unionGenes.sort(Comparator.naturalOrder());
		interGenes = new ArrayList<>(intersection);
		interGenes.sort(Comparator.naturalOrder());
		
		operatorCombo.removeActionListener(operatorActionListener);
		rankOptionCombo.removeActionListener(rankOptionActionListener);
		normCombo.removeActionListener(normActionListener);
		
		// Update Combo Boxes
		operatorCombo.removeAllItems();
		operatorCombo.addItem(new ComboItem<>(Operator.UNION, "Union (" + union.size() + ")"));
		operatorCombo.addItem(new ComboItem<>(Operator.INTERSECTION, "Intersection (" + intersection.size() + ")"));
		operatorCombo.setSelectedItem(ComboItem.of(params.getOperator()));
		
		normCombo.setSelectedItem(ComboItem.of(params.getTransform()));
		
		rankOptionCombo.removeAllItems();
		rankOptionCombo.addItem(RankingOption.none());
		rankOptionCombo.addItem(clusterRankOption);
		moreRankOptions.forEach(rankOptionCombo::addItem);
		
		for(int i = 0; i < rankOptionCombo.getItemCount(); i++) {
			if(rankOptionCombo.getItemAt(i).toString().equals(params.getRankingOptionName())) {
				rankOptionCombo.setSelectedIndex(i);
				break;
			}
		}

		// Update the setings panel
		settingsPanel.update(params);
		
		// Update the Table
		List<String> genesToUse = params.getOperator() == Operator.UNION ? unionGenes : interGenes;
		List<? extends SortKey> sortKeys = table.getRowSorter().getSortKeys();
		HeatMapTableModel tableModel = new HeatMapTableModel(map, null, genesToUse, params.getTransform());
		table.setModel(tableModel);
		
		updateSetting_ShowValues(settingsPanel.isShowValues());
		createTableHeader(params.isShowValues() ? COLUMN_WIDTH_VALUE : COLUMN_WIDTH_COLOR);
		try {
			table.getRowSorter().setSortKeys(sortKeys);
		} catch(IllegalArgumentException e) {}
		
		// Re-compute the ranking
		rankOptionActionListener.actionPerformed(null);
		
		operatorCombo.addActionListener(operatorActionListener);
		rankOptionCombo.addActionListener(rankOptionActionListener);
		normCombo.addActionListener(normActionListener);
		
		isResetting = false;
	}
	
	
	private void addRankings() {
		HeatMapTableModel model = (HeatMapTableModel) table.getModel();
		EnrichmentMap map = model.getEnrichmentMap();
		AddRanksDialog dialog = ranksDialogFactory.create(map);
		Optional<String> ranksName = dialog.open();
		if(ranksName.isPresent()) {
			List<RankingOption> moreRankOptions = parent.getMediator().getDataSetRankOptions(map);
			
			rankOptionCombo.removeActionListener(rankOptionActionListener);
			
			rankOptionCombo.removeAllItems();
			rankOptionCombo.addItem(RankingOption.none());
			rankOptionCombo.addItem(clusterRankOption);
			moreRankOptions.forEach(rankOptionCombo::addItem);
			
			for(int i = 0; i < rankOptionCombo.getItemCount(); i++) {
				RankingOption ranking = rankOptionCombo.getItemAt(i);
				if(ranking.getNameInDataSet().filter(ranksName.get()::equals).isPresent()) {
					rankOptionCombo.setSelectedIndex(i);
					break;
				}
			}
			rankOptionCombo.addActionListener(rankOptionActionListener);
		}
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
		
		HeatMapTableModel tableModel = (HeatMapTableModel) table.getModel();
		EnrichmentMap map = tableModel.getEnrichmentMap();
		List<Integer> geneIds = genes.stream().map(map::getHashFromGene).collect(Collectors.toList());
		
		CompletableFuture<Map<Integer,RankValue>> rankingFuture = rankOption.computeRanking(geneIds);
		if(rankingFuture != null) {
			rankingFuture.whenComplete((ranking, ex) -> {
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
	
	private void showExportMenu(ActionEvent event)  {
		JPopupMenu menu = new JPopupMenu();
		menu.add(txtActionFactory.create(table));
		menu.add(pdfActionFactory.create(table));
		Component c = (Component) event.getSource();
		menu.show(c, 0, c.getHeight());
	}
	
}
