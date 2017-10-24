package org.baderlab.csplugins.enrichmentmap.view.creation;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil.simpleDocumentListener;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.baderlab.csplugins.enrichmentmap.AfterInjection;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class NetworkNamePanel extends JPanel {

	private JCheckBox useAutomaticCheck;
	private JTextField networkNameText;
	
	private String automaticValue;
	private String manualValue;
	
	@AfterInjection
	public void createContents() {
		setBorder(LookAndFeelUtil.createPanelBorder());
		
		JLabel label = new JLabel("Network Name:");
		useAutomaticCheck = new JCheckBox("Use Default");
		networkNameText = new JTextField();
		
		SwingUtil.makeSmall(label, useAutomaticCheck, networkNameText);
		
		useAutomaticCheck.setSelected(true);
		networkNameText.setEnabled(false);
		
		useAutomaticCheck.addActionListener(e -> {
			if(isAutomatic()) {
				networkNameText.setText(automaticValue);
				networkNameText.setEnabled(false);
			} else {
				networkNameText.setText(manualValue);
				networkNameText.setEnabled(true);
			}
		});
		
		networkNameText.getDocument().addDocumentListener(simpleDocumentListener(() -> {
			if(!isAutomatic()) {
				manualValue = networkNameText.getText();
			}
		}));
		
		
		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
			.addComponent(label)
			.addComponent(useAutomaticCheck)
			.addComponent(networkNameText)
		);
		
		layout.setVerticalGroup(layout.createParallelGroup(Alignment.BASELINE)
			.addComponent(label)
			.addComponent(useAutomaticCheck)
			.addComponent(networkNameText, 0, DEFAULT_SIZE, Short.MAX_VALUE)
		);
	}
	
	public boolean isAutomatic() {
		return useAutomaticCheck.isSelected();
	}

	public String getNetworkName() {
		return isAutomatic() ? automaticValue : manualValue;
	}
	
	public void setAutomaticNetworkName(String networkName) {
		this.automaticValue = networkName;
		if(isAutomatic()	) {
			networkNameText.setText(automaticValue);
		}
	}
}
