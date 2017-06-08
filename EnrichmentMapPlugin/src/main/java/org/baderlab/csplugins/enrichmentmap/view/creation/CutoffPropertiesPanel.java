package org.baderlab.csplugins.enrichmentmap.view.creation;

import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.CardLayout;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JFormattedTextField.AbstractFormatterFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.InternationalFormatter;

import org.baderlab.csplugins.enrichmentmap.AfterInjection;
import org.baderlab.csplugins.enrichmentmap.PropertyManager;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.EdgeStrategy;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.SimilarityMetric;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResultFilterParams.NESFilter;
import org.baderlab.csplugins.enrichmentmap.view.util.ComboItem;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.cytoscape.util.swing.LookAndFeelUtil;

import com.google.inject.Inject;

@SuppressWarnings("serial")
public class CutoffPropertiesPanel extends JPanel {

	@Inject private PropertyManager propertyManager;
	
	// node filtering
	private JFormattedTextField qvalueText;
	private JLabel pvalueLabel;
	private JFormattedTextField pvalueText;
	private JLabel nesFilterLabel;
	private JComboBox<ComboItem<NESFilter>> nesFilterCombo;
	private JLabel shouldFilterMinLabel;
	private JCheckBox shouldFilterMinCheckbox;
	private JLabel minExperimentsLabel;
	private JFormattedTextField minExperimentsText;
	
	// edge filtering
	private JFormattedTextField similarityCutoffText;
	private JComboBox<ComboItem<SimilarityMetric>> cutoffMetricCombo;
	private JComboBox<ComboItem<EdgeStrategy>> edgeStrategyCombo;
	private CombinedConstantSlider combinedConstantSlider;
	private SimilaritySlider similaritySlider;
	private JPanel cardPanel;
	
	// options
	private JCheckBox notationCheckBox;
	private JCheckBox advancedCheckBox;
	private Map<SimilarityMetric,Double> cutoffValues;
	
	
	@AfterInjection
	public void createContents() {
		cutoffValues = new EnumMap<>(SimilarityMetric.class);
		cutoffValues.put(SimilarityMetric.JACCARD,  propertyManager.getDefaultCutOff(SimilarityMetric.JACCARD));
		cutoffValues.put(SimilarityMetric.OVERLAP,  propertyManager.getDefaultCutOff(SimilarityMetric.OVERLAP));
		cutoffValues.put(SimilarityMetric.COMBINED, propertyManager.getDefaultCutOff(SimilarityMetric.COMBINED));
		
		setBorder(LookAndFeelUtil.createPanelBorder());
		
		JPanel filterNodesPanel = createFilterNodesPanel();
		JPanel filterEdgesPanel = createFilterEdgesPanel();
		
		notationCheckBox = new JCheckBox("Scientific Notation");
		notationCheckBox.addActionListener(e -> {
			boolean scientific = notationCheckBox.isSelected();
			AbstractFormatterFactory factory = getFormatterFactory(scientific);
			pvalueText.setFormatterFactory(factory);
			qvalueText.setFormatterFactory(factory);	
			similarityCutoffText.setFormatterFactory(factory);
		});
		
		advancedCheckBox = new JCheckBox("Show Advanced Options");
		advancedCheckBox.addActionListener(e -> 
			showAdvancedOptions(advancedCheckBox.isSelected())
		);
		
		showAdvancedOptions(false);
		
		SwingUtil.makeSmall(notationCheckBox, advancedCheckBox);
		
		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
   		layout.setHorizontalGroup(layout.createParallelGroup()
   			.addGroup(layout.createSequentialGroup()
   				.addComponent(filterNodesPanel)
   				.addComponent(filterEdgesPanel)
   			)
   			.addGroup(layout.createSequentialGroup()
				.addGap(0, 0, Short.MAX_VALUE)
				.addComponent(notationCheckBox)
				.addGap(10)
				.addComponent(advancedCheckBox)
			)
   		);
   		layout.setVerticalGroup(layout.createSequentialGroup()
   			.addGroup(layout.createParallelGroup()
   				.addComponent(filterNodesPanel)
   				.addComponent(filterEdgesPanel)
   			)
   			.addGroup(layout.createParallelGroup()
				.addComponent(notationCheckBox)
				.addComponent(advancedCheckBox)
			)
   		);
	}
	
	
	private JPanel createFilterNodesPanel() {
		JPanel panel = new JPanel();
		panel.setBorder(LookAndFeelUtil.createTitledBorder("Number of Nodes (gene-set filtering)"));
		
		pvalueLabel = new JLabel("p-value cutoff:");
		JLabel qvalueLabel = new JLabel("FDR q-value cutoff:");
		nesFilterLabel = new JLabel("NES:");
		shouldFilterMinLabel = new JLabel("Filter by minimum experiments:");
		minExperimentsLabel = new JLabel("Minimum experiments:");
		
		SwingUtil.makeSmall(qvalueLabel, pvalueLabel, minExperimentsLabel, shouldFilterMinLabel, nesFilterLabel);
		
		AbstractFormatterFactory formatterFactory = getFormatterFactory(false);
		pvalueText = new JFormattedTextField(formatterFactory);
		qvalueText = new JFormattedTextField(formatterFactory);
		
		shouldFilterMinCheckbox = new JCheckBox("");
		minExperimentsText = new JFormattedTextField(NumberFormat.getIntegerInstance());
		
		pvalueText.setValue(propertyManager.getPvalue());
		qvalueText.setValue(propertyManager.getQvalue());
		minExperimentsText.setValue(3);
		
		nesFilterCombo = new JComboBox<>();
		nesFilterCombo.addItem(new ComboItem<>(NESFilter.ALL, "All"));
		nesFilterCombo.addItem(new ComboItem<>(NESFilter.POSITIVE, "Positive"));
		nesFilterCombo.addItem(new ComboItem<>(NESFilter.NEGATIVE, "Negative"));
		nesFilterCombo.setSelectedItem(ComboItem.of(NESFilter.ALL));
		
		minExperimentsLabel.setEnabled(false);
		minExperimentsText.setEnabled(false);
		
		shouldFilterMinCheckbox.addActionListener(e -> {
			boolean enable = shouldFilterMinCheckbox.isSelected();
			minExperimentsLabel.setEnabled(enable);
			minExperimentsText.setEnabled(enable);
		});
		
		SwingUtil.makeSmall(pvalueText, qvalueText, shouldFilterMinCheckbox, nesFilterCombo, minExperimentsText);
		
		final GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(
			layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.TRAILING)
					.addComponent(qvalueLabel)
					.addComponent(pvalueLabel)
					.addComponent(nesFilterLabel)
					.addComponent(shouldFilterMinLabel)
					.addComponent(minExperimentsLabel)
				)
				.addGroup(layout.createParallelGroup(Alignment.LEADING)
					.addComponent(qvalueText, PREFERRED_SIZE, 100, PREFERRED_SIZE)
					.addComponent(pvalueText, PREFERRED_SIZE, 100, PREFERRED_SIZE)
					.addComponent(nesFilterCombo)
					.addComponent(shouldFilterMinCheckbox)
					.addComponent(minExperimentsText, PREFERRED_SIZE, 100, PREFERRED_SIZE)
				)
				.addGap(0, 0, Short.MAX_VALUE)
		);
		
		layout.setVerticalGroup(
			layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
					.addComponent(qvalueLabel)
					.addComponent(qvalueText)
				)
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
					.addComponent(pvalueLabel)
					.addComponent(pvalueText)
				)
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
					.addComponent(nesFilterLabel)
					.addComponent(nesFilterCombo)
				)
				.addGroup(layout.createParallelGroup(Alignment.CENTER)
					.addComponent(shouldFilterMinLabel)
					.addComponent(shouldFilterMinCheckbox)
				)
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
					.addComponent(minExperimentsLabel)
					.addComponent(minExperimentsText)
				)
				.addGap(0, 0, Short.MAX_VALUE)
		);
		
		if (LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);
   		
   		return panel;
	}
	
	
	
	private JPanel createFilterEdgesPanel() {
		JPanel panel = new JPanel();
		panel.setBorder(LookAndFeelUtil.createTitledBorder("Number of Edges (gene-set similarity filtering)"));
		
		JPanel topPanel      = createFilterEdgesPanel_Top();
		JPanel simplePanel   = createFilterEdgesPanel_Simple();
		JPanel advancedPanel = createFilterEdgesPanel_Advanced();
		
		CardLayout cardLayout = new CardLayout();
		cardPanel = new JPanel(cardLayout);
		cardPanel.add(simplePanel, "simple");
		cardPanel.add(advancedPanel, "advanced");
		cardPanel.setOpaque(false);
		
		
		final GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(
			layout.createParallelGroup()
				.addComponent(topPanel)
				.addComponent(cardPanel)
		);
		
		layout.setVerticalGroup(
			layout.createSequentialGroup()
				.addComponent(topPanel)
				.addComponent(cardPanel)
				.addGap(0, 0, Short.MAX_VALUE)
		);
		
		if (LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);
		
		return panel;
	}
	
	
	private JPanel createFilterEdgesPanel_Top() {
		JLabel edgesLabel = new JLabel("Data Set Edges:");
		edgeStrategyCombo = new JComboBox<>();
		edgeStrategyCombo.addItem(new ComboItem<>(EdgeStrategy.AUTOMATIC, "Automatic"));
		edgeStrategyCombo.addItem(new ComboItem<>(EdgeStrategy.DISTINCT, "Separate edge for each data set (denser)"));
		edgeStrategyCombo.addItem(new ComboItem<>(EdgeStrategy.COMPOUND, "Combine edges across data sets (sparser)"));
		SwingUtil.makeSmall(edgeStrategyCombo, edgesLabel);
		
		JPanel panel = new JPanel();
		final GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(
			layout.createSequentialGroup()
				.addComponent(edgesLabel)
				.addComponent(edgeStrategyCombo)
		);
		layout.setVerticalGroup(
			layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(edgesLabel)
				.addComponent(edgeStrategyCombo)
		);
		
		if (LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);
		
		return panel;
	}
	
	
	private JPanel createFilterEdgesPanel_Simple() {
		JLabel sliderLabel = new JLabel("Connectivity:   ");
		similaritySlider = new SimilaritySlider();
		similaritySlider.setOpaque(false);
		SwingUtil.makeSmall(sliderLabel);
		
		JPanel panel = new JPanel();
		final GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(
			layout.createSequentialGroup()
				.addComponent(sliderLabel)
				.addComponent(similaritySlider, PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE)
		);
		layout.setVerticalGroup(
			layout.createParallelGroup(Alignment.CENTER)
				.addComponent(sliderLabel)
				.addComponent(similaritySlider, PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE)
		);
		
		if (LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);
		
		return panel;
	}
		
		
	private JPanel createFilterEdgesPanel_Advanced() {	
		JPanel panel = new JPanel();
		JLabel cutoffLabel = new JLabel("Cutoff:");
		JLabel metricLabel = new JLabel("Metric:");
		
		SwingUtil.makeSmall(cutoffLabel, metricLabel);
		
		SimilarityMetric defaultMetric = propertyManager.getSimilarityMetric();
		double defaultCutoff = propertyManager.getDefaultCutOff(defaultMetric);
		
		similarityCutoffText = new JFormattedTextField(getFormatterFactory(false));
		similarityCutoffText.setValue(defaultCutoff);
		
		cutoffMetricCombo = new JComboBox<>();
		cutoffMetricCombo.addItem(new ComboItem<>(SimilarityMetric.JACCARD,  "Jaccard"));
		cutoffMetricCombo.addItem(new ComboItem<>(SimilarityMetric.OVERLAP,  "Overlap"));
		cutoffMetricCombo.addItem(new ComboItem<>(SimilarityMetric.COMBINED, "Jaccard+Overlap Combined"));
		
		SwingUtil.makeSmall(similarityCutoffText, cutoffMetricCombo);
				
		ActionListener sliderUpdate = e -> {
			SimilarityMetric type = getSimilarityMetric();
			similarityCutoffText.setValue(cutoffValues.get(type));
			combinedConstantSlider.setVisible(type == SimilarityMetric.COMBINED);
		};
		
		double combinedConstant = propertyManager.getCombinedConstant();
		int tick = (int)(combinedConstant * 100.0);
		
		combinedConstantSlider = new CombinedConstantSlider(tick);
		combinedConstantSlider.setOpaque(false);
		
		cutoffMetricCombo.setSelectedItem(ComboItem.of(defaultMetric)); // default
		cutoffMetricCombo.addActionListener(sliderUpdate);
		similarityCutoffText.setValue(cutoffValues.get(defaultMetric));
		combinedConstantSlider.setVisible(defaultMetric == SimilarityMetric.COMBINED);
		
		similarityCutoffText.addPropertyChangeListener("value", e -> {
			double value = ((Number)e.getNewValue()).doubleValue();
			cutoffValues.put(getSimilarityMetric(), value);
		});
		
		final GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(
			layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
					.addComponent(cutoffLabel)
					.addComponent(metricLabel)
				)
				.addGroup(layout.createParallelGroup()
					.addComponent(similarityCutoffText, PREFERRED_SIZE, 100, PREFERRED_SIZE)
					.addComponent(cutoffMetricCombo, PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE)
					.addComponent(combinedConstantSlider, PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE)
				)
				.addGap(0, 0, Short.MAX_VALUE)
		);
		
		layout.setVerticalGroup(
			layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
					.addComponent(cutoffLabel)
					.addComponent(similarityCutoffText)
				)
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
					.addComponent(metricLabel)
					.addComponent(cutoffMetricCombo)
				)
				.addComponent(combinedConstantSlider, PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE)
				.addGap(0, 0, Short.MAX_VALUE)
		);
		
		if (LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);
   		
   		return panel;
	}
	
	
	public void reset() {
		pvalueText.setValue(propertyManager.getPvalue());
		qvalueText.setValue(propertyManager.getQvalue());
		nesFilterCombo.setSelectedItem(ComboItem.of(NESFilter.ALL));
		shouldFilterMinCheckbox.setSelected(false);
		minExperimentsText.setValue(3);
		minExperimentsLabel.setEnabled(false);
		minExperimentsText.setEnabled(false);
		
		cutoffValues = new EnumMap<>(SimilarityMetric.class);
		cutoffValues.put(SimilarityMetric.JACCARD,  propertyManager.getDefaultCutOff(SimilarityMetric.JACCARD));
		cutoffValues.put(SimilarityMetric.OVERLAP,  propertyManager.getDefaultCutOff(SimilarityMetric.OVERLAP));
		cutoffValues.put(SimilarityMetric.COMBINED, propertyManager.getDefaultCutOff(SimilarityMetric.COMBINED));
		
		SimilarityMetric defaultMetric = propertyManager.getSimilarityMetric();
		cutoffMetricCombo.setSelectedItem(ComboItem.of(defaultMetric));
		edgeStrategyCombo.setSelectedItem(ComboItem.of(EdgeStrategy.AUTOMATIC));
		
		combinedConstantSlider.reset();
		similaritySlider.reset();
	}
	
	
	private void showAdvancedOptions(boolean show) {
		pvalueLabel.setVisible(show);
		pvalueText.setVisible(show);
		nesFilterLabel.setVisible(show);
		nesFilterCombo.setVisible(show);
		minExperimentsLabel.setVisible(show);
		minExperimentsText.setVisible(show);
		shouldFilterMinLabel.setVisible(show);
		shouldFilterMinCheckbox.setVisible(show);
		
		CardLayout cardLayout = (CardLayout)cardPanel.getLayout();
		cardLayout.show(cardPanel, show ? "advanced" : "simple");
	}
	
	
	private static AbstractFormatterFactory getFormatterFactory(boolean scientific) {
		return new AbstractFormatterFactory() {
			@Override
			public AbstractFormatter getFormatter(JFormattedTextField tf) {
				NumberFormat format = scientific ? new DecimalFormat("0.######E00") : new DecimalFormat();
				format.setMinimumFractionDigits(scientific ? 0 : 1);
				format.setMaximumFractionDigits(12);
				InternationalFormatter formatter = new InternationalFormatter(format);
				formatter.setAllowsInvalid(true);
				return formatter;
			}
		};
	}
	
	
	public double getPValue() {
		return pvalueText.isVisible()
			? getValue(pvalueText)
			: propertyManager.getPvalue();
	}
	
	public NESFilter getNESFilter() {
		return nesFilterCombo.isVisible()
			? nesFilterCombo.getItemAt(nesFilterCombo.getSelectedIndex()).getValue()
			: NESFilter.ALL;
	}
	
	public Optional<Integer> getMinimumExperiments() {
		if(minExperimentsText.isVisible() && shouldFilterMinCheckbox.isSelected()) {
 			Integer value = (Integer)minExperimentsText.getValue();
 			if(value != null && value.intValue() > 0) {
 				return Optional.of(value);
 			}
		}
		return Optional.empty();
	}
	
	public double getQValue() {
		return getValue(qvalueText);
	}
	
	public double getCombinedConstant() {
		if(advancedCheckBox.isSelected())
			return ((double)combinedConstantSlider.getValue()) / 100.0;
		else
			return 0.5;
	}
	
	public double getCutoff() {
		if(advancedCheckBox.isSelected())
			return getValue(similarityCutoffText);
		else
			return similaritySlider.getCutoff();
	}
	
	public SimilarityMetric getSimilarityMetric() {
		if(advancedCheckBox.isSelected())
			return cutoffMetricCombo.getItemAt(cutoffMetricCombo.getSelectedIndex()).getValue();
		else
			return similaritySlider.getSimilarityMetric();
	}
	
	public EdgeStrategy getEdgeStrategy() {
		return edgeStrategyCombo.getItemAt(edgeStrategyCombo.getSelectedIndex()).getValue();
	}
	
	private static double getValue(JFormattedTextField textField) {
		return ((Number)textField.getValue()).doubleValue();
	}
	
	
}
