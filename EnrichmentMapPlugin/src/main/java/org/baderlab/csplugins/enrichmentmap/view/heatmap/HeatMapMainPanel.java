package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.baderlab.csplugins.enrichmentmap.AfterInjection;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParams.Compress;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParams.Distance;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParams.Operator;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParams.Transform;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.ColorAndValueRenderer;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.ColorRenderer;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.ColumnHeaderRankOptionRenderer;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.ColumnHeaderVerticalRenderer;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.GradientLegendPanel;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.HeatMapTableModel;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.RankOptionErrorHeader;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.RankValue;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.RankValueRenderer;
import org.baderlab.csplugins.enrichmentmap.view.util.ComboItem;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

@SuppressWarnings("serial")
public class HeatMapMainPanel extends JPanel {

	private static final int COLUMN_WIDTH_COLOR = 10;
	private static final int COLUMN_WIDTH_VALUE = 50;
	
	
	@Inject private ExportTXTAction.Factory txtActionFactory;
	@Inject private ExportPDFAction.Factory pdfActionFactory;
	@Inject private Provider<SettingsPopupPanel> settingsPopupPanelProvider;
	@Inject private AddRanksDialog.Factory ranksDialogFactory;
	@Inject private ColumnHeaderRankOptionRenderer.Factory columnHeaderRankOptionRendererFactory;
	@Inject private IconManager iconManager;

	private GradientLegendPanel gradientLegendPanel;
	private SettingsPopupPanel settingsPanel;
	
	private JTable table;
	private JScrollPane scrollPane;
	private JComboBox<ComboItem<Operator>> operatorCombo;
	private JComboBox<ComboItem<Transform>> normCombo;
	private JComboBox<ComboItem<Compress>> compressCombo;
	private JCheckBox showValuesCheck;
	
	private ActionListener normActionListener;
	private ActionListener operatorActionListener;
	private ActionListener compressActionListener;
	private ActionListener showValueActionListener;
	
	private ClusterRankingOption clusterRankOption = null;
	private List<RankingOption> moreRankOptions;
	private RankingOption selectedRankOption;
	
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
		settingsPanel = settingsPopupPanelProvider.get();
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
	
	
	private void clearTableHeader() {
		JTableHeader header = table.getTableHeader();
		TableColumnModel columnModel = table.getColumnModel();
		if(columnModel.getColumnCount() > 0) {
			TableColumn rankColumn = columnModel.getColumn(HeatMapTableModel.RANK_COL);
			TableCellRenderer existingRenderer = rankColumn.getHeaderRenderer();
			if(existingRenderer instanceof ColumnHeaderRankOptionRenderer) {
				((ColumnHeaderRankOptionRenderer)existingRenderer).dispose(header);
			}
		}
	}
	
	private void createTableHeader(int expressionColumnWidth) {
		JTableHeader header = table.getTableHeader();
		header.setReorderingAllowed(false);
		HeatMapTableModel tableModel = (HeatMapTableModel)table.getModel();
		TableColumnModel columnModel = table.getColumnModel();
		
		TableCellRenderer vertRenderer = new ColumnHeaderVerticalRenderer();
		TableCellRenderer vertRendererPheno1 = new ColumnHeaderVerticalRenderer(EMStyleBuilder.Colors.LIGHTEST_PHENOTYPE_1);
		TableCellRenderer vertRendererPheno2 = new ColumnHeaderVerticalRenderer(EMStyleBuilder.Colors.LIGHTEST_PHENOTYPE_2);
		
		TableColumn rankColumn = columnModel.getColumn(HeatMapTableModel.RANK_COL);

		rankColumn.setHeaderRenderer(columnHeaderRankOptionRendererFactory.create(this, HeatMapTableModel.RANK_COL));
		rankColumn.setPreferredWidth(100);
		
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
			column.setPreferredWidth(expressionColumnWidth);
		}
	}
	
	
	private JPanel createToolbarPanel() {
		gradientLegendPanel = new GradientLegendPanel(table);
		showValuesCheck = new JCheckBox("Show Values");
		
		JLabel operatorLabel = new JLabel("Genes:");
		operatorCombo = new JComboBox<>();
		JLabel normLabel = new JLabel("Expressions:");
		normCombo = new JComboBox<>();
		JLabel compressLabel = new JLabel("Compress:");
		compressCombo = new JComboBox<>();
		
		SwingUtil.makeSmall(operatorLabel, operatorCombo, normLabel, normCombo, compressLabel, compressCombo, showValuesCheck);
		
		operatorCombo.addItem(new ComboItem<>(Operator.UNION, "All"));
		operatorCombo.addItem(new ComboItem<>(Operator.INTERSECTION, "Common"));
		operatorCombo.setSelectedItem(ComboItem.of(Operator.UNION));
		
		normCombo.addItem(new ComboItem<>(Transform.AS_IS,         "Values"));
		normCombo.addItem(new ComboItem<>(Transform.ROW_NORMALIZE, "Row Norm"));
		normCombo.addItem(new ComboItem<>(Transform.LOG_TRANSFORM, "Log"));
		
		compressCombo.addItem(new ComboItem<>(Compress.NONE, "-None-"));
		compressCombo.addItem(new ComboItem<>(Compress.MEDIAN, "Median"));
		compressCombo.addItem(new ComboItem<>(Compress.MIN, "Min"));
		compressCombo.addItem(new ComboItem<>(Compress.MAX, "Max"));
		
		operatorCombo.addActionListener(operatorActionListener = e -> updateSetting_Operator(getOperator()));
		normCombo.addActionListener(normActionListener = e -> updateSetting_Transform(getTransform(), getCompress()));
		compressCombo.addActionListener(compressActionListener = e -> updateSetting_Transform(getTransform(), getCompress()));
		showValuesCheck.addActionListener(showValueActionListener = e -> updateSetting_ShowValues(isShowValues()));
		
		JButton plusButton = SwingUtil.createIconButton(iconManager, IconManager.ICON_PLUS, "Add Rankings...");
		JButton gearButton = SwingUtil.createIconButton(iconManager, IconManager.ICON_GEAR, "Settings");
		JButton menuButton = SwingUtil.createIconButton(iconManager, IconManager.ICON_EXTERNAL_LINK, "Export");
		LookAndFeelUtil.equalizeSize(gearButton, menuButton);
		plusButton.addActionListener(e -> addRankings());
		gearButton.addActionListener(e -> settingsPanel.popup(gearButton));
		menuButton.addActionListener(this::showExportMenu);
		
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		final int gap = 3;
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
			.addComponent(gradientLegendPanel, 180, 180, 180)
			.addGap(0, 0, Short.MAX_VALUE)
			.addComponent(operatorLabel)
			.addComponent(operatorCombo, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			.addGap(gap)
			.addComponent(normLabel)
			.addComponent(normCombo, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			.addGap(gap)
			.addComponent(compressLabel)
			.addComponent(compressCombo, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			.addGap(gap)
			.addComponent(showValuesCheck)
			.addGap(gap)
			.addComponent(plusButton)
			.addComponent(gearButton)
			.addComponent(menuButton)
		);
		layout.setVerticalGroup(layout.createParallelGroup(Alignment.BASELINE)
			.addComponent(gradientLegendPanel)
			.addComponent(operatorLabel)
			.addComponent(operatorCombo)
			.addComponent(normLabel)
			.addComponent(normCombo)
			.addComponent(compressLabel)
			.addComponent(compressCombo)
			.addComponent(showValuesCheck)
			.addComponent(plusButton)
			.addComponent(gearButton)
			.addComponent(menuButton)
		);
		
		panel.setOpaque(false);
		return panel;
	}
	
	
	public Operator getOperator() {
		return operatorCombo.getItemAt(operatorCombo.getSelectedIndex()).getValue();
	}
	
	public Transform getTransform() {
		return normCombo.getItemAt(normCombo.getSelectedIndex()).getValue();
	}
	
	public Compress getCompress() {
		return compressCombo.getItemAt(compressCombo.getSelectedIndex()).getValue();
	}
	
	public RankingOption getRankingOption() {
		return selectedRankOption;
	}
	
	public boolean isShowValues() {
		return showValuesCheck.isSelected();
	}
	
	public Distance getDistance() {
		return settingsPanel.getDistance();
	}
	
	public String getRankingOptionName() {
		return getRankingOption().toString();
	}
	
	public HeatMapParams buildParams() {
		return new HeatMapParams.Builder()
				.setDistanceMetric(getDistance())
				.setOperator(getOperator())
				.setCompress(getCompress())
				.setShowValues(isShowValues())
				.setRankingOptionName(getRankingOptionName())
				.setTransform(getTransform())
				.build();
	}
	
	
	public void reset(EnrichmentMap map, HeatMapParams params, List<RankingOption> moreRankOptions, Set<String> union, Set<String> intersection) {
		isResetting = true;
		this.clusterRankOption = parent.getMediator().getClusterRankOption(map);
		this.moreRankOptions = moreRankOptions.isEmpty() ? Arrays.asList(RankingOption.none()) : moreRankOptions;
		
		unionGenes = new ArrayList<>(union);
		unionGenes.sort(Comparator.naturalOrder());
		interGenes = new ArrayList<>(intersection);
		interGenes.sort(Comparator.naturalOrder());
		
		operatorCombo.removeActionListener(operatorActionListener);
		normCombo.removeActionListener(normActionListener);
		compressCombo.removeActionListener(compressActionListener);
		showValuesCheck.removeActionListener(showValueActionListener);
		
		// Update Combo Boxes
		operatorCombo.removeAllItems();
		operatorCombo.addItem(new ComboItem<>(Operator.UNION, "All (" + union.size() + ")"));
		operatorCombo.addItem(new ComboItem<>(Operator.INTERSECTION, "Common (" + intersection.size() + ")"));
		operatorCombo.setSelectedItem(ComboItem.of(params.getOperator()));
		
		normCombo.setSelectedItem(ComboItem.of(params.getTransform()));
		compressCombo.setSelectedItem(ComboItem.of(params.getCompress()));
		
		selectedRankOption = getRankOptionFromParams(params);

		// Update the setings panel
		settingsPanel.update(params);
		showValuesCheck.setSelected(params.isShowValues());
		
		// Update the Table
		clearTableHeader();
		List<String> genesToUse = params.getOperator() == Operator.UNION ? unionGenes : interGenes;
		HeatMapTableModel tableModel = new HeatMapTableModel(map, null, genesToUse, params.getTransform(), params.getCompress());
		table.setModel(tableModel);
		
		List<? extends SortKey> sortKeys = table.getRowSorter().getSortKeys();
		if(sortKeys.isEmpty()) {
			sortKeys = Collections.singletonList(new SortKey(HeatMapTableModel.RANK_COL, SortOrder.ASCENDING));
		}
		
		updateSetting_ShowValues(params.isShowValues());
		try {
			table.getRowSorter().setSortKeys(sortKeys);
		} catch(IllegalArgumentException e) {}
		
		// Re-compute the ranking
		updateSetting_RankOption(selectedRankOption);
		
		operatorCombo.addActionListener(operatorActionListener);
		normCombo.addActionListener(normActionListener);
		compressCombo.addActionListener(compressActionListener);
		showValuesCheck.addActionListener(showValueActionListener);
		
		isResetting = false;
	}
	
	
	private RankingOption getRankOptionFromParams(HeatMapParams params) {
		String name = params.getRankingOptionName();
		if(name == null) {
			return moreRankOptions.get(0);
		}
		else if(name.equals(clusterRankOption.toString())) {
			return clusterRankOption;
		}
		else {
			for(RankingOption option : moreRankOptions) {
				if(name.equals(option.toString())) {
					return option;
				}
			}
		}
		return moreRankOptions.get(0);
	}
	
	
	private void addRankings() {
		HeatMapTableModel model = (HeatMapTableModel) table.getModel();
		EnrichmentMap map = model.getEnrichmentMap();
		AddRanksDialog dialog = ranksDialogFactory.create(map);
		Optional<String> ranksName = dialog.open();
		if(ranksName.isPresent()) {
			this.moreRankOptions = parent.getMediator().getDataSetRankOptions(map);
		}
	}

	public RankingOption getClusterRankingOption() {
		return clusterRankOption;
	}
	
	public List<RankingOption> getRankingOptions() {
		return moreRankOptions;
	}
	
	public List<RankingOption> getAllRankingOptions() {
		List<RankingOption> options = new ArrayList<>(moreRankOptions.size() + 1);
		options.add(clusterRankOption);
		options.addAll(moreRankOptions);
		return options;
	}
	
	public RankingOption getSelectedRankOption() {
		return selectedRankOption;
	}
	
	private List<String> getGenes(Operator operator) {
		switch(operator) {
			case UNION: default: return unionGenes;
			case INTERSECTION:   return interGenes;
		}
	}
	
	
	private void updateSetting_Distance(Distance distance) {
		clusterRankOption.setDistance(distance);
		if(selectedRankOption == clusterRankOption) {
			updateSetting_RankOption(clusterRankOption);
		}
		settingChanged();
	}
	
	private void updateSetting_Operator(Operator oper) {
		HeatMapTableModel tableModel = (HeatMapTableModel) table.getModel();
		tableModel.setGenes(getGenes(oper));
		settingChanged();
	}
	
	public void updateSetting_RankOption(RankingOption rankOption) {
		selectedRankOption = rankOption;
		List<String> genes = getGenes(getOperator());
		
		HeatMapTableModel tableModel = (HeatMapTableModel) table.getModel();
		EnrichmentMap map = tableModel.getEnrichmentMap();
		List<Integer> geneIds = genes.stream().map(map::getHashFromGene).collect(Collectors.toList());
		
		CompletableFuture<Optional<Map<Integer,RankValue>>> rankingFuture = rankOption.computeRanking(geneIds);
		if(rankingFuture != null) {
			rankingFuture.whenComplete((ranking, ex) -> {
				if(ranking.isPresent()) {
					tableModel.setRanking(rankOption.getName(), ranking.get());
					table.getColumnModel().getColumn(HeatMapTableModel.RANK_COL).setHeaderValue(rankOption);
				} else {
					tableModel.setRanking(rankOption.getName(), null);
					table.getColumnModel().getColumn(HeatMapTableModel.RANK_COL).setHeaderValue(new RankOptionErrorHeader(rankOption));
				}
				table.getTableHeader().repaint();
			});
		}
		settingChanged();
	}
	
	private void updateSetting_ShowValues(boolean showValues) {
		table.setDefaultRenderer(Double.class, showValues ? new ColorAndValueRenderer() : new ColorRenderer());
		table.setDefaultRenderer(RankValue.class, new RankValueRenderer());
		clearTableHeader();
		createTableHeader(showValues ? COLUMN_WIDTH_VALUE : COLUMN_WIDTH_COLOR);
		table.revalidate();
		settingChanged();
	}
	
	private void updateSetting_Transform(Transform transform, Compress compress) {
		HeatMapTableModel tableModel = (HeatMapTableModel) table.getModel();
		if(tableModel.getCompress() != compress) {
			HeatMapParams params = this.buildParams();
			EnrichmentMap map = tableModel.getEnrichmentMap();
			List<RankingOption> rankOptions = parent.getMediator().getDataSetRankOptions(map);
			SwingUtilities.invokeLater(() ->
				reset(map, params, rankOptions, Sets.newHashSet(unionGenes), Sets.newHashSet(interGenes))
			);
		}
		else {
			tableModel.setTransform(transform, compress);
			updateSetting_ShowValues(isShowValues()); // clear cached data used by the ColorRenderer
		}
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
