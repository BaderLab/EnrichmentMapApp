package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import java.awt.GridLayout;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")

public class SettingsPopupPanel extends JPanel {
	
	public SettingsPopupPanel() {
		createContents();
		setOpaque(false);
	}
	
	/**
	 * Cannot use JComboBox on a JPopupMenu because of a bug in swing: 
	 * http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4799266
	 */
	private void createContents() {
		JLabel genesLabel = new JLabel(" Genes:");
		JRadioButton unionRadio = new JRadioButton("Union of selected gene sets");
		JRadioButton interRadio = new JRadioButton("Intersection of selected gene sets");
		JPanel genesRadioPanel = createButtonPanel(unionRadio, interRadio);
		
		
		JLabel distanceLabel = new JLabel(" Distance Metric:");
		JRadioButton cosineRadio = new JRadioButton("Cosine");
		JRadioButton euclideanRadio = new JRadioButton("Euclidean");
		JRadioButton pearsonRadio = new JRadioButton("Pearson Correlation");
		JPanel distanceRadioPanel = createButtonPanel(cosineRadio, euclideanRadio, pearsonRadio);
				
		JLabel valuesLabel = new JLabel(" Show Values:");
		JCheckBox showValuesCheck = new JCheckBox();
		
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
