package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import java.awt.GridLayout;
import java.util.function.Consumer;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParams.DistanceMetric;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParams.Operator;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")

public class SettingsPopupPanel extends JPanel {
	
	// MKTODO these might not need to be fields
	private JRadioButton unionRadio;
	private JRadioButton interRadio;
	
	private JRadioButton cosineRadio;
	private JRadioButton euclideanRadio;
	private JRadioButton pearsonRadio;
	
	private JCheckBox showValuesCheck;
	
//	private Consumer<Operator> operatorListener;
//	private Consumer<DistanceMetric> distanceListener;
	private Consumer<Boolean> showValuesListener;
	
	
	public SettingsPopupPanel(HeatMapParams params) {
		createContents();
		setInitialValues(params);
		setOpaque(false);
	}
	

	public void setShowValuesListener(Consumer<Boolean> showValuesListener) {
		this.showValuesListener = showValuesListener;
	}
	
	/**
	 * Cannot use JComboBox on a JPopupMenu because of a bug in swing: 
	 * http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4799266
	 */
	private void createContents() {
		JLabel genesLabel = new JLabel(" Genes:");
		unionRadio = new JRadioButton("Union of selected gene sets");
		interRadio = new JRadioButton("Intersection of selected gene sets");
		JPanel genesRadioPanel = createButtonPanel(unionRadio, interRadio);
		
		JLabel distanceLabel = new JLabel(" Distance Metric:");
		cosineRadio = new JRadioButton("Cosine");
		euclideanRadio = new JRadioButton("Euclidean");
		pearsonRadio = new JRadioButton("Pearson Correlation");
		JPanel distanceRadioPanel = createButtonPanel(cosineRadio, euclideanRadio, pearsonRadio);
				
		JLabel valuesLabel = new JLabel(" Show Values:");
		showValuesCheck = new JCheckBox();
		showValuesCheck.addActionListener(e -> {
			if(showValuesListener != null) {
				showValuesListener.accept(showValuesCheck.isSelected());
			}
		});
		
		SwingUtil.makeSmall(genesLabel, distanceLabel, valuesLabel, showValuesCheck);
		
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup()
				.addComponent(genesLabel)
				.addComponent(distanceLabel)
				.addComponent(valuesLabel)
			)
			.addGroup(layout.createParallelGroup()
				.addComponent(genesRadioPanel)
				.addComponent(distanceRadioPanel)
				.addComponent(showValuesCheck)
			)
		);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(genesLabel)
				.addComponent(genesRadioPanel)
			)
			.addGap(10)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(distanceLabel)
				.addComponent(distanceRadioPanel)
			)
			.addGap(10)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(valuesLabel)
				.addComponent(showValuesCheck)
			)
		);
	}
	
	
	private void setInitialValues(HeatMapParams params) {
		switch(params.getOperator()) {
			case UNION:        unionRadio.setSelected(true); break;
			case INTERSECTION: interRadio.setSelected(true); break;
		}
		switch(params.getDistanceMetric()) {
			case COSINE:    cosineRadio.setSelected(true); break;
			case EUCLIDEAN: euclideanRadio.setSelected(true); break;
			case PEARSON:   pearsonRadio.setSelected(true); break;
		}
		showValuesCheck.setSelected(params.isShowValues());
	}
	
	
	public Operator getOperator() {
		return unionRadio.isSelected() ? Operator.UNION : Operator.INTERSECTION;
	}
	
	public DistanceMetric getDistanceMetric() {
		if(cosineRadio.isSelected())
			return DistanceMetric.COSINE;
		if(euclideanRadio.isSelected())
			return DistanceMetric.EUCLIDEAN;
		return DistanceMetric.PEARSON;
	}
	
	public boolean isShowValues() {
		return showValuesCheck.isSelected();
	}
	
//	public HeatMapParams get() {
//		HeatMapParams.Builder builder = new HeatMapParams.Builder(params);
//		builder.setOperator(getOperator());
//		builder.setDistanceMetric(getDistanceMetric());
//		builder.setShowValues(isShowValues());
//		return builder.build();
//	}
	
	private static JPanel createButtonPanel(JRadioButton ... buttons) {
		JPanel panel = new JPanel(new GridLayout(buttons.length, 1));
		ButtonGroup group = new ButtonGroup();
		for(JRadioButton button : buttons) {
			panel.add(button);
			group.add(button);
			SwingUtil.makeSmall(button);
		}
		panel.setOpaque(false);
		return panel;
	}

}
