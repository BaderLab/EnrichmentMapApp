package org.baderlab.csplugins.enrichmentmap.view.postanalysis2;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.baderlab.csplugins.enrichmentmap.view.creation.NamePanel;
import org.baderlab.csplugins.enrichmentmap.view.util.CardDialogCallback;
import org.baderlab.csplugins.enrichmentmap.view.util.CardDialogPage;
import org.baderlab.csplugins.enrichmentmap.view.util.CheckboxData;
import org.baderlab.csplugins.enrichmentmap.view.util.CheckboxListPanel;
import org.baderlab.csplugins.enrichmentmap.view.util.GBCFactory;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.cytoscape.util.swing.LookAndFeelUtil;

public class PADialogPage implements CardDialogPage {

	private NamePanel namePanel;
	
	private CardDialogCallback callback;
	
	@Override
	public String getID() {
		return getClass().getSimpleName();
	}

	@Override
	public String getPageComboText() {
		return PADialogParameters.TITLE;
	}

	@Override
	public void finish() {

	}
	
	@Override
	public JPanel createBodyPanel(CardDialogCallback callback) {
		this.callback = callback;
		
		JPanel geneSetsPanel = createGeneSetsPanel();
		namePanel = createNamePanel();
		JPanel edgePanel = createEdgePanel();
		
		JPanel bottom = new JPanel(new GridBagLayout());
		bottom.add(namePanel, GBCFactory.grid(0,0).insets(0).weightx(1.0).fill(GridBagConstraints.HORIZONTAL).get());
		bottom.add(edgePanel, GBCFactory.grid(0,1).insets(0).weightx(1.0).fill(GridBagConstraints.HORIZONTAL).get());
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(geneSetsPanel, BorderLayout.CENTER);
		panel.add(bottom, BorderLayout.SOUTH);
		
		return panel;
	}
	
	
	private JPanel createGeneSetsPanel() {
		JPanel panel = new JPanel();
		
		JLabel title = new JLabel("Signature Gene Sets");
		JButton loadFileButton = new JButton("Load from File...");
		JButton loadWebButton = new JButton("Load from Web...");
		loadWebButton.setEnabled(false);
		
		CheckboxListPanel<String> geneSetPanel = new CheckboxListPanel<>(false, false);
		geneSetPanel.getModel().addElement(new CheckboxData<String>("foo", "foo"));
		geneSetPanel.getModel().addElement(new CheckboxData<String>("bar", "bar"));
		
		JLabel filterLabel = new JLabel("Filter:");
		JComboBox filterCombo = new JComboBox<>();
		JTextField textField = new JTextField();
		JButton applyButton = new JButton("Apply");
		
		SwingUtil.makeSmall(title, loadFileButton, loadWebButton, filterLabel, filterCombo, textField, applyButton);
		LookAndFeelUtil.equalizeSize(loadFileButton, loadWebButton);
		
		final GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(false);
		
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addGroup(layout.createSequentialGroup()
				.addComponent(title)
				.addGap(0, 0, Short.MAX_VALUE)
				.addComponent(loadWebButton)
				.addComponent(loadFileButton)
			)
			.addComponent(geneSetPanel)
			.addGroup(layout.createSequentialGroup()
				.addComponent(geneSetPanel.getSelectAllButton())
				.addComponent(geneSetPanel.getSelectNoneButton())
				.addGap(0, 0, Short.MAX_VALUE)
				.addComponent(filterLabel)
				.addComponent(filterCombo)
				.addComponent(textField, 60, 60, 60)
				.addComponent(applyButton)
			)
		);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(title)
				.addComponent(loadWebButton)
				.addComponent(loadFileButton)
			)
			.addComponent(geneSetPanel)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(geneSetPanel.getSelectAllButton())
				.addComponent(geneSetPanel.getSelectNoneButton())
				.addComponent(filterLabel)
				.addComponent(filterCombo)
				.addComponent(textField)
				.addComponent(applyButton)
			)
		);
		
		if(LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);
		return panel;
	}
	
	
	private NamePanel createNamePanel() {
		NamePanel namePanel = new NamePanel("Data Set Name:");
		return namePanel;
	}
	
	
	private JPanel createEdgePanel() {
		JPanel panel = new JPanel();
		panel.setBorder(LookAndFeelUtil.createPanelBorder());
		
		JLabel title = new JLabel("Edge Weight Parameters");
		JLabel testLabel = new JLabel("Test:");
		JLabel cutoffLabel = new JLabel("Cutoff:");
		JLabel dataSetLabel = new JLabel("Data Set:");
		
		JComboBox testCombo = new JComboBox<>();
		JTextField cutoffText = new JTextField();
		JComboBox dataSetCombo = new JComboBox<>();
		
		final GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(false);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup(Alignment.TRAILING)
				.addComponent(testLabel)
				.addComponent(cutoffLabel)
				.addComponent(dataSetLabel)
			)
			.addGroup(layout.createParallelGroup(Alignment.LEADING)
				.addComponent(testCombo)
				.addComponent(cutoffText)
				.addComponent(dataSetCombo)
			)
		);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(testLabel)
				.addComponent(testCombo)
			)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(cutoffLabel)
				.addComponent(cutoffText)
			)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(dataSetLabel)
				.addComponent(dataSetCombo)
			)
		);
		
		
		if(LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);
		return panel;
	}
	

}
