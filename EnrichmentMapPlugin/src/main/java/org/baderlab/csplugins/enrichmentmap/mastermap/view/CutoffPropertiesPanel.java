package org.baderlab.csplugins.enrichmentmap.mastermap.view;

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

import org.baderlab.csplugins.enrichmentmap.mastermap.model.CutoffType;
import org.baderlab.csplugins.enrichmentmap.util.ComboItem;
import org.baderlab.csplugins.enrichmentmap.util.SwingUtil;
import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class CutoffPropertiesPanel extends JPanel {

	private static final double P_VALUE_DEFAULT = 1.0;
	private static final double Q_VALUE_DEFAULT = 0.1;
	private static final int COMBINED_CONSTANT_DEFAULT = 50;
	
	private static final double JACCARD_DEFAULT = 0.25;
	private static final double OVERLAP_DEFAULT = 0.5;
	private static final double COMBINED_DEFAULT = 0.375; 
	
	
	private JLabel pValueLabel;
	private JFormattedTextField pvalueTextField;
	private JFormattedTextField qvalueTextField;
	private JFormattedTextField cutoffTextField;
	private JComboBox<ComboItem<CutoffType>> cutoffMetricCombo;
	private CombinedConstantSlider combinedConstantSlider;
	private JCheckBox notationCheckBox;
	private JCheckBox advancedCheckBox;
	
	
	// MKTODO move this into CyProperties
	private Map<CutoffType,Double> cutoffValues = new EnumMap<>(CutoffType.class); {
		cutoffValues.put(CutoffType.JACCARD,  JACCARD_DEFAULT);
		cutoffValues.put(CutoffType.OVERLAP,  OVERLAP_DEFAULT);
		cutoffValues.put(CutoffType.COMBINED, COMBINED_DEFAULT);
	}
	
	
	public CutoffPropertiesPanel() {
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
		
		pvalueTextField.setValue(P_VALUE_DEFAULT);
		qvalueTextField.setValue(Q_VALUE_DEFAULT);
		
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
		
		cutoffTextField = new JFormattedTextField(getFormatterFactory(false));
		cutoffTextField.setValue(OVERLAP_DEFAULT);
		
		cutoffMetricCombo = new JComboBox<>();
		cutoffMetricCombo.addItem(new ComboItem<>(CutoffType.JACCARD,  CutoffType.JACCARD.display));
		cutoffMetricCombo.addItem(new ComboItem<>(CutoffType.OVERLAP,  CutoffType.OVERLAP.display));
		cutoffMetricCombo.addItem(new ComboItem<>(CutoffType.COMBINED, CutoffType.COMBINED.display));
		
		ActionListener sliderUpdate = e -> {
			CutoffType type = getCutoffType();
			cutoffTextField.setValue(cutoffValues.get(type));
			combinedConstantSlider.setVisible(type == CutoffType.COMBINED);
		};
		
		combinedConstantSlider = new CombinedConstantSlider(COMBINED_CONSTANT_DEFAULT);
		combinedConstantSlider.setOpaque(false);
		
		cutoffMetricCombo.setSelectedItem(ComboItem.of(CutoffType.OVERLAP)); // default
		cutoffMetricCombo.addActionListener(sliderUpdate);
		
		cutoffTextField.addPropertyChangeListener("value", e -> {
			double value = ((Number)e.getNewValue()).doubleValue();
			cutoffValues.put(getCutoffType(), value);
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
	
	public CutoffType getCutoffType() {
		return cutoffMetricCombo.getItemAt(cutoffMetricCombo.getSelectedIndex()).getValue();
	}
	
	
}
