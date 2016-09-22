package org.baderlab.csplugins.enrichmentmap.mastermap.view;

import java.text.DecimalFormat;
import java.text.NumberFormat;

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

import org.baderlab.csplugins.enrichmentmap.mastermap.model.CutoffType;
import org.baderlab.csplugins.enrichmentmap.util.ComboItem;

@SuppressWarnings("serial")
public class CutoffPropertiesPanel extends JPanel {

	
	public CutoffPropertiesPanel() {
		JLabel pValueLabel = new JLabel("p-value cutoff");
		JLabel qValueLabel = new JLabel("FDR q-value cutoff");
		JLabel metricLabel = new JLabel("Similarity metric");
		JLabel cutoffLabel = new JLabel("Similarity cutoff");
		
		AbstractFormatterFactory formatterFactory = getFormatterFactory(false);
		JFormattedTextField pvalueTextField = new JFormattedTextField(formatterFactory);
		JFormattedTextField qvalueTextField = new JFormattedTextField(formatterFactory);
		JFormattedTextField cutoffTextField = new JFormattedTextField(formatterFactory);
		
		JComboBox<ComboItem<CutoffType>> cutoffMetricCombo = new JComboBox<>();
		for(CutoffType c : CutoffType.values()) {
			cutoffMetricCombo.addItem(new ComboItem<>(c, c.getDisplay()));
		}
		
		JCheckBox notationCheckBox = new JCheckBox("Scientific Notation");
		notationCheckBox.addActionListener(e -> {
			boolean scientific = notationCheckBox.isSelected();
			AbstractFormatterFactory factory = getFormatterFactory(scientific);
			pvalueTextField.setFormatterFactory(factory);
			qvalueTextField.setFormatterFactory(factory);	
			cutoffTextField.setFormatterFactory(factory);
		});
		
		
		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		
		layout.setHorizontalGroup(
			layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.LEADING, false)
					.addComponent(pValueLabel)
					.addComponent(qValueLabel)
					.addComponent(metricLabel)
					.addComponent(cutoffLabel)
				)
				.addGroup(layout.createParallelGroup(Alignment.LEADING, false)
					.addComponent(pvalueTextField, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)
					.addComponent(qvalueTextField, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)
					.addComponent(cutoffTextField, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)
					.addComponent(cutoffMetricCombo)
					.addComponent(notationCheckBox)
				)
		);
		
		layout.setVerticalGroup(
			layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
					.addComponent(pValueLabel)
					.addComponent(pvalueTextField)
				)
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
					.addComponent(qValueLabel)
					.addComponent(qvalueTextField)
				)
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
					.addComponent(metricLabel)
					.addComponent(cutoffMetricCombo)
				)
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
					.addComponent(cutoffLabel)
					.addComponent(cutoffTextField)
				)
				.addComponent(notationCheckBox)
		);
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
