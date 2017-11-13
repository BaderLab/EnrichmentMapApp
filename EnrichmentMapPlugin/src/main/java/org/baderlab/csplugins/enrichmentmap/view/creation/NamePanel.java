package org.baderlab.csplugins.enrichmentmap.view.creation;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil.simpleDocumentListener;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class NamePanel extends JPanel {

	private JCheckBox useAutomaticCheck;
	private JTextField nameText;
	
	private String automaticValue;
	private String manualValue;
	
	public NamePanel(String labelText) {
		createContents(labelText);
	}
	
	private void createContents(String labelText) {
		setBorder(LookAndFeelUtil.createPanelBorder());
		
		JLabel label = new JLabel(labelText + ":");
		useAutomaticCheck = new JCheckBox("Use Default");
		nameText = new JTextField();
		
		SwingUtil.makeSmall(label, useAutomaticCheck, nameText);
		
		useAutomaticCheck.setSelected(true);
		nameText.setEnabled(false);
		
		useAutomaticCheck.addActionListener(e -> {
			if(isAutomatic()) {
				nameText.setText(automaticValue);
				nameText.setEnabled(false);
			} else {
				nameText.setText(manualValue);
				nameText.setEnabled(true);
			}
		});
		
		nameText.getDocument().addDocumentListener(simpleDocumentListener(() -> {
			if(!isAutomatic()) {
				manualValue = nameText.getText();
			}
		}));
		
		
		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
			.addComponent(label)
			.addComponent(useAutomaticCheck)
			.addComponent(nameText)
		);
		
		layout.setVerticalGroup(layout.createParallelGroup(Alignment.BASELINE)
			.addComponent(label)
			.addComponent(useAutomaticCheck)
			.addComponent(nameText, 0, DEFAULT_SIZE, Short.MAX_VALUE)
		);
	}
	
	public boolean isAutomatic() {
		return useAutomaticCheck.isSelected();
	}

	public String getNameText() {
		return isAutomatic() ? automaticValue : manualValue;
	}
	
	public void setAutomaticName(String networkName) {
		this.automaticValue = networkName;
		if(isAutomatic()	) {
			nameText.setText(automaticValue);
		}
	}
}
