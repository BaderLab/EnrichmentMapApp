package org.baderlab.csplugins.enrichmentmap.view.creation;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

@SuppressWarnings("serial")
public class EditNothingPanel extends JPanel {

	public EditNothingPanel() {
		JLabel infoLabel = new JLabel("Click the (+) button to add a data set");
		infoLabel.setEnabled(false);
		infoLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
		
		
		final GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGap(0, 0, Short.MAX_VALUE)
				.addComponent(infoLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addGap(0, 0, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGap(0, 0, Short.MAX_VALUE)
				.addComponent(infoLabel)
				.addGap(0, 0, Short.MAX_VALUE)
		);
		
		setOpaque(false);
	}
	
}
