package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.util.swing.IconManager.ICON_BARS;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.baderlab.csplugins.enrichmentmap.AfterInjection;
import org.baderlab.csplugins.enrichmentmap.model.Compress;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.Transform;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParams.Distance;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParams.Operator;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.ColumnHeaderRankOptionRenderer;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.ColumnHeaderVerticalRenderer;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.DataSetColorRange;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.GradientLegendPanel;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.HeatMapCellRenderer;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.HeatMapRowSorter;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.HeatMapTableModel;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.RankOptionErrorHeader;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.RankValue;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.RankValueRenderer;
import org.baderlab.csplugins.enrichmentmap.view.util.ComboItem;
import org.baderlab.csplugins.enrichmentmap.view.util.Labels;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
@SuppressWarnings("serial")
public class HeatMapContentPanel extends JPanel {

	private static final String NAME = "__HEAT_MAP_CONTENT_PANEL";
	
	private static final int COLUMN_WIDTH_COLOR = 10;
	private static final int COLUMN_WIDTH_VALUE = 50;
	
	@Inject private Provider<OptionsPopup> optionsPopupProvider;
	@Inject private ColumnHeaderRankOptionRenderer.Factory columnHeaderRankOptionRendererFactory;
	@Inject private IconManager iconManager;

	private GradientLegendPanel gradientLegendPanel;
	private OptionsPopup optionsPopup;
	
	private JTable table;
	private JScrollPane scrollPane;
	private JButton optionsButton;
	private JComboBox<ComboItem<Operator>> operatorCombo;
	private JComboBox<ComboItem<Transform>> normCombo;
	private JComboBox<ComboItem<Compress>> compressCombo;
	private JCheckBox showValuesCheck;
	
	private ClusterRankingOption clusterRankingOption;
	private List<RankingOption> moreRankOptions;
	private RankingOption selectedRankingOption;
	
	private List<String> unionGenes;
	private List<String> interGenes;
	
	@AfterInjection
	private void createContents() {
		setName(NAME);
		optionsPopup = optionsPopupProvider.get();
		
		JPanel expressionPanel = createTablePanel(); // must create table first
		JPanel toolbarPanel = createToolbarPanel();
		
		setLayout(new BorderLayout());
		add(toolbarPanel, BorderLayout.NORTH);
		add(expressionPanel, BorderLayout.CENTER);
		
		if (LookAndFeelUtil.isAquaLAF())
			setOpaque(false);
	}

	private JPanel createTablePanel() {
		scrollPane = new JScrollPane(getTable());
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(scrollPane, BorderLayout.CENTER);
		panel.setOpaque(false);
		
		return panel;
	}
	
	void clearTableHeader() {
		JTableHeader header = getTable().getTableHeader();
		TableColumnModel columnModel = getTable().getColumnModel();
		if(columnModel.getColumnCount() > 0) {
			TableColumn rankColumn = columnModel.getColumn(HeatMapTableModel.RANK_COL);
			TableCellRenderer existingRenderer = rankColumn.getHeaderRenderer();
			if(existingRenderer instanceof ColumnHeaderRankOptionRenderer) {
				((ColumnHeaderRankOptionRenderer)existingRenderer).dispose(header);
			}
		}
	}
	
	void updateTableHeader(boolean showValues) {
		int expressionColumnWidth = showValues ? COLUMN_WIDTH_VALUE : COLUMN_WIDTH_COLOR;
		JTableHeader header = getTable().getTableHeader();
		header.setReorderingAllowed(false);
		HeatMapTableModel tableModel = (HeatMapTableModel) getTable().getModel();
		TableColumnModel columnModel = getTable().getColumnModel();
		
		TableCellRenderer vertRenderer = new ColumnHeaderVerticalRenderer();
		TableCellRenderer vertRendererPheno1 = new ColumnHeaderVerticalRenderer(EMStyleBuilder.Colors.LIGHTEST_PHENOTYPE_1);
		TableCellRenderer vertRendererPheno2 = new ColumnHeaderVerticalRenderer(EMStyleBuilder.Colors.LIGHTEST_PHENOTYPE_2);
		
		TableColumn rankColumn = columnModel.getColumn(HeatMapTableModel.RANK_COL);

		rankColumn.setHeaderRenderer(columnHeaderRankOptionRendererFactory.create(this, HeatMapTableModel.RANK_COL));
		rankColumn.setPreferredWidth(100);
		
		int colCount = tableModel.getColumnCount();
		for (int col = HeatMapTableModel.DESC_COL_COUNT; col < colCount; col++) {
			EMDataSet dataset = tableModel.getDataSet(col);
			String pheno1 = dataset.getEnrichments().getPhenotype1();
			String pheno2 = dataset.getEnrichments().getPhenotype2();
			
			Optional<String> pheno = tableModel.getPhenotype(col);
			TableCellRenderer renderer;
			if (pheno.filter(p -> p.equals(pheno1)).isPresent())
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
		gradientLegendPanel = new GradientLegendPanel(getTable());
		
		JLabel operatorLabel = new JLabel("Genes:");
		JLabel normLabel = new JLabel("Expressions:");
		JLabel compressLabel = new JLabel("Compress:");
		
		SwingUtil.makeSmall(operatorLabel, getOperatorCombo(), normLabel, getNormCombo(), compressLabel,
				getCompressCombo(), getShowValuesCheck());
		
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addComponent(gradientLegendPanel, 140, 160, 180)
				.addGap(15, 15, Short.MAX_VALUE)
				.addComponent(operatorLabel)
				.addComponent(getOperatorCombo(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addComponent(normLabel)
				.addComponent(getNormCombo(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addComponent(compressLabel)
				.addComponent(getCompressCombo(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(getShowValuesCheck())
				.addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(getOptionsButton())
				.addContainerGap()
		);
		layout.setVerticalGroup(layout.createParallelGroup(Alignment.CENTER)
				.addComponent(gradientLegendPanel)
				.addComponent(operatorLabel)
				.addComponent(getOperatorCombo())
				.addComponent(normLabel)
				.addComponent(getNormCombo())
				.addComponent(compressLabel)
				.addComponent(getCompressCombo())
				.addComponent(getShowValuesCheck())
				.addComponent(getOptionsButton())
		);
		
		panel.setOpaque(false);
		
		return panel;
	}
	
	JComboBox<ComboItem<Operator>> getOperatorCombo() {
		if (operatorCombo == null) {
			operatorCombo = new JComboBox<>();
			operatorCombo.addItem(new ComboItem<>(Operator.UNION, "All"));
			operatorCombo.addItem(new ComboItem<>(Operator.INTERSECTION, "Common"));
			operatorCombo.setSelectedItem(ComboItem.of(Operator.UNION));
		}
		
		return operatorCombo;
	}
	
	JComboBox<ComboItem<Transform>> getNormCombo() {
		if (normCombo == null) {
			normCombo = new JComboBox<>();
			normCombo.addItem(new ComboItem<>(Transform.AS_IS,         "Values"));
			normCombo.addItem(new ComboItem<>(Transform.ROW_NORMALIZE, "Row Norm"));
			normCombo.addItem(new ComboItem<>(Transform.LOG_TRANSFORM, "Log"));
		}
		
		return normCombo;
	}
	
	JComboBox<ComboItem<Compress>> getCompressCombo() {
		if (compressCombo == null) {
			compressCombo = new JComboBox<>();
		}
		
		return compressCombo;
	}
	
	JCheckBox getShowValuesCheck() {
		if (showValuesCheck == null) {
			showValuesCheck = new JCheckBox("Values");
		}
		
		return showValuesCheck;
	}
	
	JButton getOptionsButton() {
		if (optionsButton == null) {
			optionsButton = new JButton(ICON_BARS);
			optionsButton.setToolTipText("Options...");
			SwingUtil.styleHeaderButton(optionsButton, iconManager.getIconFont(18.0f));
			optionsButton.addActionListener(evt -> optionsPopup.popup(optionsButton));
		}
		
		return optionsButton;
	}
	
	JTable getTable() {
		if (table == null) {
			HeatMapTableModel model = new HeatMapTableModel();
			table = new JTable(model);
			table.setFillsViewportHeight(true);
			table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			table.setCellSelectionEnabled(true);
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			table.setDefaultRenderer(Double.class, new HeatMapCellRenderer());
			table.setDefaultRenderer(RankValue.class, new RankValueRenderer());
			table.setRowSorter(new HeatMapRowSorter(model));
		}
		
		return table;
	}
	
	public Operator getOperator() {
		ComboItem<Operator> item = getOperatorCombo().getItemAt(getOperatorCombo().getSelectedIndex());
		return item != null ? item.getValue() : null;
	}
	
	public Transform getTransform() {
		ComboItem<Transform> item = getNormCombo().getItemAt(getNormCombo().getSelectedIndex());
		return item != null ? item.getValue() : null;
	}
	
	public Compress getCompress() {
		ComboItem<Compress> item = getCompressCombo().getItemAt(getCompressCombo().getSelectedIndex());
		return item != null ? item.getValue() : null;
	}
	
	public RankingOption getRankingOption() {
		return selectedRankingOption;
	}
	
	public boolean isShowValues() {
		return getShowValuesCheck().isSelected();
	}
	
	public Distance getDistance() {
		return optionsPopup.getDistance();
	}
	
	public String getRankingOptionName() {
		RankingOption rankingOption = getRankingOption();
		return rankingOption != null ? rankingOption.toString() : null;
	}
	
	public HeatMapParams buildParams() {
		return new HeatMapParams.Builder()
				.setDistanceMetric(getDistance())
				.setOperator(getOperator())
				.setCompress(getCompress())
				.setShowValues(isShowValues())
				.setRankingOptionName(getRankingOptionName())
				.setTransform(getTransform())
				.setSortKeys(getTable().getRowSorter().getSortKeys())
				.build();
	}
	
	public List<String> getGenes() {
		HeatMapTableModel model = (HeatMapTableModel) getTable().getModel();
		
		return model.getGenes();
	}
	
	public List<String> getUnionGenes() {
		return new ArrayList<>(unionGenes);
	}
	
	public List<String> getInterGenes() {
		return new ArrayList<>(interGenes);
	}
	
	void update(
			CyNetwork network,
			EnrichmentMap map,
			HeatMapParams params,
			List<RankingOption> moreRankOptions,
			Collection<String> union,
			Collection<String> intersection,
			ClusterRankingOption clusterRankingOption
	) {
		this.clusterRankingOption = clusterRankingOption;
		this.moreRankOptions = moreRankOptions.isEmpty() ? Arrays.asList(RankingOption.none()) : moreRankOptions;

		unionGenes = new ArrayList<>(union);
		unionGenes.sort(Comparator.naturalOrder());
		interGenes = new ArrayList<>(intersection);
		interGenes.sort(Comparator.naturalOrder());
		
		// Update Combo Boxes
		getOperatorCombo().removeAllItems();
		getOperatorCombo().addItem(new ComboItem<>(Operator.UNION, "All (" + union.size() + ")"));
		getOperatorCombo().addItem(new ComboItem<>(Operator.INTERSECTION, "Common (" + intersection.size() + ")"));
		getOperatorCombo().setSelectedItem(ComboItem.of(params.getOperator()));

		getCompressCombo().removeAllItems();
		getCompressCombo().addItem(new ComboItem<>(Compress.NONE, Labels.NONE));

		if (map.hasClassData()) {
			getCompressCombo().addItem(new ComboItem<>(Compress.CLASS_MEDIAN, "Class: Median"));
			getCompressCombo().addItem(new ComboItem<>(Compress.CLASS_MIN, "Class: Min"));
			getCompressCombo().addItem(new ComboItem<>(Compress.CLASS_MAX, "Class: Max"));
		}

		getCompressCombo().addItem(new ComboItem<>(Compress.DATASET_MEDIAN, "Data Set: Median"));
		getCompressCombo().addItem(new ComboItem<>(Compress.DATASET_MIN, "Data Set: Min"));
		getCompressCombo().addItem(new ComboItem<>(Compress.DATASET_MAX, "Data Set: Max"));

		getNormCombo().setSelectedItem(ComboItem.of(params.getTransform()));
		
		// MKTODO this wont work if selected item is Class but doesn't exist anymore
		getCompressCombo().setSelectedItem(ComboItem.of(params.getCompress()));
		
		selectedRankingOption = getRankOptionFromParams(params);

		// Update the setings panel
		optionsPopup.update(params);
		getShowValuesCheck().setSelected(params.isShowValues());
		
		// Update the Table
		clearTableHeader();
		
		List<String> genesToUse = params.getOperator() == Operator.UNION ? unionGenes : interGenes;
		HeatMapTableModel tableModel = (HeatMapTableModel) getTable().getModel();
		tableModel.update(network, map, null, genesToUse, params.getTransform(), params.getCompress());
		
		updateTableHeader(isShowValues());
		getTable().revalidate();
		
		List<? extends SortKey> sortKeys = params.getSortKeys();
		
		if (sortKeys == null)
			sortKeys = getTable().getRowSorter().getSortKeys();
		if (sortKeys.isEmpty())
			sortKeys = Collections.singletonList(new SortKey(HeatMapTableModel.RANK_COL, SortOrder.ASCENDING));
		
		try {
			getTable().getRowSorter().setSortKeys(sortKeys);
		} catch(IllegalArgumentException e) {}
		
		// Re-compute the ranking
		setSelectedRankingOption(selectedRankingOption);
	}
	
	protected OptionsPopup getOptionsPopup() {
		return optionsPopup;
	}
	
	protected EnrichmentMap getEnrichmentMap() {
		HeatMapTableModel model = (HeatMapTableModel) getTable().getModel();
		
		return model.getEnrichmentMap();
	}
	
	private RankingOption getRankOptionFromParams(HeatMapParams params) {
		String name = params.getRankingOptionName();
		if(name == null) {
			return moreRankOptions.get(0);
		}
		else if(name.equals(clusterRankingOption.toString())) {
			return clusterRankingOption;
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
	
	void setMoreRankOptions(List<RankingOption> moreRankOptions) {
		this.moreRankOptions = moreRankOptions;
	}
	
	public RankingOption getClusterRankingOption() {
		return clusterRankingOption;
	}
	
	public List<RankingOption> getRankingOptions() {
		return moreRankOptions;
	}
	
	public List<RankingOption> getAllRankingOptions() {
		List<RankingOption> options = new ArrayList<>(moreRankOptions.size() + 1);
		options.add(clusterRankingOption);
		options.addAll(moreRankOptions);
		return options;
	}
	
	public RankingOption getSelectedRankingOption() {
		return selectedRankingOption;
	}
	
	public void setSelectedRankingOption(RankingOption newValue) {
		if (selectedRankingOption != newValue) {
			RankingOption oldValue = selectedRankingOption;
			selectedRankingOption = newValue;
			List<String> genes = unionGenes; // always use all the genes, fixes #310
			
			HeatMapTableModel tableModel = (HeatMapTableModel) getTable().getModel();
			EnrichmentMap map = tableModel.getEnrichmentMap();
			List<Integer> geneIds = genes.stream().map(map::getHashFromGene).collect(Collectors.toList());
			
			CompletableFuture<Optional<Map<Integer,RankValue>>> rankingFuture = newValue.computeRanking(geneIds);
			
			if (rankingFuture != null) {
				rankingFuture.whenComplete((ranking, ex) -> {
					if (ranking.isPresent()) {
						tableModel.setRanking(newValue.getName(), ranking.get());
						getTable().getColumnModel().getColumn(HeatMapTableModel.RANK_COL).setHeaderValue(newValue);
					} else {
						tableModel.setRanking(newValue.getName(), null);
						getTable().getColumnModel().getColumn(HeatMapTableModel.RANK_COL)
								.setHeaderValue(new RankOptionErrorHeader(newValue));
					}
					getTable().getTableHeader().repaint();
				});
			}
			
			firePropertyChange("selectedRankingOption", oldValue, newValue);
		}
	}
	
	List<String> getGenes(Operator operator) {
		switch (operator) {
			case UNION: default: return unionGenes;
			case INTERSECTION:   return interGenes;
		}
	}
	
	public DataSetColorRange getDataSetColorRange(EMDataSet dataSet) {
		HeatMapTableModel tableModel = (HeatMapTableModel) getTable().getModel();
		HeatMapCellRenderer renderer = (HeatMapCellRenderer) getTable().getDefaultRenderer(Double.class);
		Optional<DataSetColorRange> colorRange = renderer.getRange(dataSet, tableModel.getTransform());
		
		return colorRange.isPresent() ? colorRange.get() : null;
	}
}
