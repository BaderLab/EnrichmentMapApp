package org.baderlab.csplugins.enrichmentmap.view.postanalysis;

import static java.awt.GridBagConstraints.EAST;
import static java.awt.GridBagConstraints.NONE;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.baderlab.csplugins.enrichmentmap.AfterInjection;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.view.util.GBCFactory;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class MannWhitneyRanksDialog extends JDialog {

	private final EnrichmentMap map;
	private Map<String,String> results = new HashMap<>();
	private boolean cancelled = false;
	

	public MannWhitneyRanksDialog(JFrame parent, EnrichmentMap map) {
		super(parent, true);
		this.map = map;
		setMinimumSize(new Dimension(500, 160));
		setResizable(true);
		setTitle("Mann-Whitney Ranks");
		createContents();
		pack();
		setLocationRelativeTo(parent);
	}
	
	public Optional<Map<String,String>> open() {
		setVisible(true); // blocks until dispose() is called, must be model to work
		return cancelled ? Optional.empty() : Optional.of(results);
	}
	
	@AfterInjection
	private void createContents() {
		JPanel dataSetPanel = createDataSetPanel();
		JPanel buttonPanel  = createButtonPanel();
		
		buttonPanel.setBorder(BorderFactory.createMatteBorder(1,0,0,0, UIManager.getColor("Separator.foreground")));
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(dataSetPanel, BorderLayout.CENTER);
		panel.add(buttonPanel, BorderLayout.SOUTH);
		
		setContentPane(panel);
	}
	
	
	private JPanel createDataSetPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(LookAndFeelUtil.createTitledBorder("Select ranks to use for each data set."));
		
		int y = 0;
		
		for(EMDataSet dataset : map.getDataSetList()) {
			final String dataSetName = dataset.getName();
			JLabel label = new JLabel(dataSetName + ":");
			JComboBox<String> combo = new JComboBox<>();
			for(String ranksName : dataset.getExpressionSets().getAllRanksNames()) {
				combo.addItem(ranksName);
			}
			SwingUtil.makeSmall(label, combo);
			if(combo.getItemCount() <= 1) {
				combo.setEnabled(false);
			}
			
			combo.addActionListener(e -> {
				String ranks = combo.getSelectedItem().toString();
				results.put(dataSetName, ranks);
			});
			
			results.put(dataSetName, combo.getSelectedItem().toString());
			
			panel.add(label, GBCFactory.grid(0,y).weightx(.5).anchor(EAST).fill(NONE).get());
			panel.add(combo, GBCFactory.grid(1,y).weightx(.5).get());
			y++;
		}
		
		return panel;
	}
	
	
	private JPanel createButtonPanel() {
		JButton okButton = new JButton(new AbstractAction("OK") {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		JButton cancelButton = new JButton(new AbstractAction("Cancel") {
			public void actionPerformed(ActionEvent e) {
				cancelled = true;
				dispose();
			}
		});

		JPanel buttonPanel = LookAndFeelUtil.createOkCancelPanel(okButton, cancelButton);
		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), okButton.getAction(), cancelButton.getAction());
		getRootPane().setDefaultButton(okButton);
		//okButton.setEnabled(false);
		return buttonPanel;
	}
}
