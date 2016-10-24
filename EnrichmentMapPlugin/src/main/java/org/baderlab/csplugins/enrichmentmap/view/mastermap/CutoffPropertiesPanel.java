package org.baderlab.csplugins.enrichmentmap.view.mastermap;

import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.GroupLayout;
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
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.SimilarityMetric;
import org.baderlab.csplugins.enrichmentmap.util.SwingUtil;
import org.baderlab.csplugins.enrichmentmap.view.util.ComboItem;
import org.cytoscape.util.swing.LookAndFeelUtil;

import com.google.inject.Inject;

@SuppressWarnings("serial")
public class CutoffPropertiesPanel extends JPanel {

	@Inject private PropertyManager propertyManager;
	
	private JLabel pValueLabel;
	private JFormattedTextField pvalueTextField;
	private JFormattedTextField qvalueTextField;
	private JFormattedTextField cutoffTextField;
	private JComboBox<ComboItem<SimilarityMetric>> cutoffMetricCombo;
	private CombinedConstantSlider combinedConstantSlider;
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
			pvalueTextField.setFormatterFactory(factory);
			qvalueTextField.setFormatterFactory(factory);	
			cutoffTextField.setFormatterFactory(factory);
		});
		
		advancedCheckBox = new JCheckBox("Show P-value");
		advancedCheckBox.addActionListener(e -> {
			boolean advanced = advancedCheckBox.isSelected();
			pValueLabel.setVisible(advanced);
			pvalueTextField.setVisible(advanced);
		});
		
		SwingUtil.makeSmall(notationCheckBox, advancedCheckBox);
		
		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
   		layout.setHorizontalGroup(layout.createParallelGroup()
   			.addComponent(filterNodesPanel)
   			.addComponent(filterEdgesPanel)
   			.addGroup(layout.createSequentialGroup()
				.addGap(0, 0, Short.MAX_VALUE)
				.addComponent(notationCheckBox)
				.addGap(10)
				.addComponent(advancedCheckBox)
			)
   		);
   		layout.setVerticalGroup(layout.createSequentialGroup()
   			.addComponent(filterNodesPanel)
   			.addComponent(filterEdgesPanel)
   			.addGroup(layout.createParallelGroup()
				.addComponent(notationCheckBox)
				.addComponent(advancedCheckBox)
			)
   		);
	}
	
	
	private JPanel createFilterNodesPanel() {
		JPanel panel = new JPanel();
		panel.setBorder(LookAndFeelUtil.createTitledBorder("Gene-Set Filtering (Nodes)"));
		
		pValueLabel = new JLabel("p-value cutoff");
		JLabel qValueLabel = new JLabel("FDR q-value cutoff");
		
		SwingUtil.makeSmall(qValueLabel, pValueLabel);
		
		AbstractFormatterFactory formatterFactory = getFormatterFactory(false);
		pvalueTextField = new JFormattedTextField(formatterFactory);
		qvalueTextField = new JFormattedTextField(formatterFactory);
		
		pvalueTextField.setValue(propertyManager.getDefaultPvalue());
		qvalueTextField.setValue(propertyManager.getDefaultQvalue());
		
		pValueLabel.setVisible(false);
		pvalueTextField.setVisible(false);
		
		final GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(
			layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
					.addComponent(qValueLabel)
					.addComponent(qvalueTextField, PREFERRED_SIZE, 100, PREFERRED_SIZE)
				)
				.addGap(30)
				.addGroup(layout.createParallelGroup()
					.addComponent(pValueLabel)
					.addComponent(pvalueTextField, PREFERRED_SIZE, 100, PREFERRED_SIZE)
				)
				.addGap(0, 0, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(
			layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
					.addComponent(qValueLabel)
					.addComponent(pValueLabel)
				)
				.addGroup(layout.createParallelGroup()
					.addComponent(qvalueTextField)
					.addComponent(pvalueTextField)
				)
		);
		
		if (LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);
   		
   		return panel;
	}
	
	private JPanel createFilterEdgesPanel() {
		JPanel panel = new JPanel();
		panel.setBorder(LookAndFeelUtil.createTitledBorder("Similarity Filtering (Edges)"));
		
		JLabel cutoffLabel = new JLabel("Cutoff");
		JLabel metricLabel = new JLabel("Metric");
		
		SwingUtil.makeSmall(cutoffLabel, metricLabel);
		
		SimilarityMetric defaultMetric = propertyManager.getDefaultSimilarityMetric();
		double defaultCutoff = propertyManager.getDefaultCutOff(defaultMetric);
		
		cutoffTextField = new JFormattedTextField(getFormatterFactory(false));
		cutoffTextField.setValue(defaultCutoff);
		
		cutoffMetricCombo = new JComboBox<>();
		cutoffMetricCombo.addItem(new ComboItem<>(SimilarityMetric.JACCARD,  "Jaccard"));
		cutoffMetricCombo.addItem(new ComboItem<>(SimilarityMetric.OVERLAP,  "Overlap"));
		cutoffMetricCombo.addItem(new ComboItem<>(SimilarityMetric.COMBINED, "Jaccard+Overlap Combined"));
		
		ActionListener sliderUpdate = e -> {
			SimilarityMetric type = getSimilarityMetric();
			cutoffTextField.setValue(cutoffValues.get(type));
			combinedConstantSlider.setVisible(type == SimilarityMetric.COMBINED);
		};
		
		double combinedConstant = propertyManager.getDefaultCombinedConstant();
		int tick = (int)(combinedConstant * 100.0);
		
		combinedConstantSlider = new CombinedConstantSlider(tick);
		combinedConstantSlider.setOpaque(false);
		
		cutoffMetricCombo.setSelectedItem(ComboItem.of(defaultMetric)); // default
		cutoffMetricCombo.addActionListener(sliderUpdate);
		
		cutoffTextField.addPropertyChangeListener("value", e -> {
			double value = ((Number)e.getNewValue()).doubleValue();
			cutoffValues.put(getSimilarityMetric(), value);
		});
		
		sliderUpdate.actionPerformed(null);
		
		final GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(
			layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
					.addComponent(cutoffLabel)
					.addComponent(cutoffTextField, PREFERRED_SIZE, 100, PREFERRED_SIZE)
				)
				.addGap(30)
				.addGroup(layout.createParallelGroup()
					.addComponent(metricLabel)
					.addComponent(cutoffMetricCombo, PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE)
				)
				.addGap(30)
				.addComponent(combinedConstantSlider, PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE)
				.addGap(0, 0, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(
			layout.createParallelGroup()
				.addGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup()
						.addComponent(cutoffLabel)
						.addComponent(metricLabel)
					)
					.addGroup(layout.createParallelGroup()
						.addComponent(cutoffTextField)
						.addComponent(cutoffMetricCombo)
					)
				)
				.addComponent(combinedConstantSlider, PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE)
		);
		
		if (LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);
   		
   		return panel;
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
		return getValue(pvalueTextField);
	}
	
	public double getQValue() {
		return getValue(qvalueTextField);
	}
	
	public double getCombinedConstant() {
		return ((double)combinedConstantSlider.getValue()) / 100.0;
	}
	
	public double getCutoff() {
		return getValue(cutoffTextField);
	}
	
	private static double getValue(JFormattedTextField textField) {
		return ((Number)textField.getValue()).doubleValue();
	}
	
	public SimilarityMetric getSimilarityMetric() {
		return cutoffMetricCombo.getItemAt(cutoffMetricCombo.getSelectedIndex()).getValue();
	}
	
	
}
