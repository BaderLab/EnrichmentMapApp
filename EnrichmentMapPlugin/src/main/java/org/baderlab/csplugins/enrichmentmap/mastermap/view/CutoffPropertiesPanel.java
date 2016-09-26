package org.baderlab.csplugins.enrichmentmap.mastermap.view;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JFormattedTextField.AbstractFormatterFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.InternationalFormatter;

import org.baderlab.csplugins.enrichmentmap.mastermap.model.CutoffType;
import org.baderlab.csplugins.enrichmentmap.util.ComboItem;

@SuppressWarnings("serial")
public class CutoffPropertiesPanel extends JPanel {

	private static final double P_VALUE_DEFAULT = 0.005;
	private static final double Q_VALUE_DEFAULT = 0.1;
	private static final int COMBINED_CONSTANT_DEFAULT = 50;
	
	private static final double JACCARD_DEFAULT = 0.25;
	private static final double OVERLAP_DEFAULT = 0.5;
	private static final double COMBINED_DEFAULT = 0.375; 
	
	
	private JLabel pValueLabel;
	private JLabel qValueLabel;
	private JLabel metricLabel;
	private JLabel cutoffLabel;
	
	private JFormattedTextField pvalueTextField;
	private JFormattedTextField qvalueTextField;
	private JFormattedTextField cutoffTextField;
	private JComboBox<ComboItem<CutoffType>> cutoffMetricCombo;
	private CombinedConstantSlider combinedConstantSlider;
	private JCheckBox notationCheckBox;
	
	
	// MKTODO move this into CyProperties
	private Map<CutoffType,Double> cutoffValues = new EnumMap<>(CutoffType.class); {
		cutoffValues.put(CutoffType.JACCARD,  JACCARD_DEFAULT);
		cutoffValues.put(CutoffType.OVERLAP,  OVERLAP_DEFAULT);
		cutoffValues.put(CutoffType.COMBINED, COMBINED_DEFAULT); // MKTODO what's the actual value
	}
	
	
	public CutoffPropertiesPanel() {
		pValueLabel = new JLabel("p-value cutoff");
		qValueLabel = new JLabel("FDR q-value cutoff");
		metricLabel = new JLabel("Similarity metric");
		cutoffLabel = new JLabel("Similarity cutoff");
		
		AbstractFormatterFactory formatterFactory = getFormatterFactory(false);
		pvalueTextField = new JFormattedTextField(formatterFactory);
		qvalueTextField = new JFormattedTextField(formatterFactory);
		cutoffTextField = new JFormattedTextField(formatterFactory);
		
		pvalueTextField.setValue(P_VALUE_DEFAULT);
		qvalueTextField.setValue(Q_VALUE_DEFAULT);
		cutoffTextField.setValue(OVERLAP_DEFAULT);
		
		cutoffMetricCombo = new JComboBox<>();
		cutoffMetricCombo.addItem(new ComboItem<>(CutoffType.JACCARD, CutoffType.JACCARD.getDisplay()));
		cutoffMetricCombo.addItem(new ComboItem<>(CutoffType.OVERLAP, CutoffType.OVERLAP.getDisplay()));
		cutoffMetricCombo.addItem(new ComboItem<>(CutoffType.COMBINED, CutoffType.COMBINED.getDisplay()));
		
		cutoffMetricCombo.setSelectedItem(ComboItem.of(CutoffType.OVERLAP)); // default
		cutoffMetricCombo.addActionListener(e -> {
			CutoffType type = getCutoffType();
			cutoffTextField.setValue(cutoffValues.get(type));
			updateLayout(type == CutoffType.COMBINED);
		});
		
		combinedConstantSlider = new CombinedConstantSlider(COMBINED_CONSTANT_DEFAULT);
		
		notationCheckBox = new JCheckBox("Scientific Notation");
		notationCheckBox.addActionListener(e -> {
			boolean scientific = notationCheckBox.isSelected();
			AbstractFormatterFactory factory = getFormatterFactory(scientific);
			pvalueTextField.setFormatterFactory(factory);
			qvalueTextField.setFormatterFactory(factory);	
			cutoffTextField.setFormatterFactory(factory);
		});
		
		cutoffTextField.addPropertyChangeListener("value", e -> {
			double value = ((Number)e.getNewValue()).doubleValue();
			cutoffValues.put(getCutoffType(), value);
		});
		
		updateLayout(false);
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
	
	public CutoffType getCutoffType() {
		return cutoffMetricCombo.getItemAt(cutoffMetricCombo.getSelectedIndex()).getValue();
	}
	
	
	private void updateLayout(boolean showSlider) {
		removeAll();
		
		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		
		ParallelGroup pg = layout.createParallelGroup(Alignment.LEADING, false);
		pg.addComponent(pvalueTextField, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE);
		pg.addComponent(qvalueTextField, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE);
		pg.addComponent(cutoffTextField, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE);
		pg.addComponent(cutoffMetricCombo);
		if(showSlider) {
			pg.addComponent(combinedConstantSlider);
		}
		pg.addComponent(notationCheckBox);
		
		
		layout.setHorizontalGroup(
			layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.LEADING, false)
					.addComponent(pValueLabel)
					.addComponent(qValueLabel)
					.addComponent(metricLabel)
					.addComponent(cutoffLabel)
				)
				.addGroup(pg)
		);
		
		SequentialGroup sg = layout.createSequentialGroup();
		sg.addGroup(layout.createParallelGroup(Alignment.BASELINE)
			.addComponent(pValueLabel)
			.addComponent(pvalueTextField)
		);
		sg.addGroup(layout.createParallelGroup(Alignment.BASELINE)
			.addComponent(qValueLabel)
			.addComponent(qvalueTextField)
		);
		sg.addGroup(layout.createParallelGroup(Alignment.BASELINE)
			.addComponent(metricLabel)
			.addComponent(cutoffMetricCombo)
		);
		if(showSlider) {
			sg.addComponent(combinedConstantSlider);
		}
		sg.addGroup(layout.createParallelGroup(Alignment.BASELINE)
			.addComponent(cutoffLabel)
			.addComponent(cutoffTextField)
		);
		sg.addComponent(notationCheckBox);
		layout.setVerticalGroup(sg);
		
		invalidate();
		validate();
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
	
}
