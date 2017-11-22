package org.baderlab.csplugins.enrichmentmap.view.postanalysis2;

import static java.awt.GridBagConstraints.EAST;
import static java.awt.GridBagConstraints.NONE;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.view.util.GBCFactory;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;

@SuppressWarnings("serial")
public class MannWhitRanksPanel extends JPanel {

	private Map<String,String> results = new HashMap<>();
	
	public MannWhitRanksPanel(EnrichmentMap map) {
		createContents(map);
	}
	
	private void createContents(EnrichmentMap map) {
		JLabel title = new JLabel("Ranks to use for Mann-Whitney test");
		SwingUtil.makeSmall(title);
		
		List<EMDataSet> dataSets = map.getDataSetList();

		JPanel body = new JPanel(new GridBagLayout());
		int y = 0;
		for(EMDataSet dataset : dataSets) {
			final String dataSetName = dataset.getName();
			JLabel label = new JLabel(dataSetName + ":");
			JComboBox<String> combo = new JComboBox<>();
			for(String ranksName : dataset.getAllRanksNames()) {
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
			
			body.add(label, GBCFactory.grid(0,y).weightx(.5).anchor(EAST).fill(NONE).get());
			body.add(combo, GBCFactory.grid(1,y).weightx(.5).get());
			y++;
		}
		
		JPanel container = new JPanel(new BorderLayout());
		container.add(body, BorderLayout.NORTH);
		
		JScrollPane scrollPane = new JScrollPane(container);
		scrollPane.setAlignmentX(TOP_ALIGNMENT);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		setLayout(new BorderLayout());
		add(title, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
		setOpaque(false);
	}
	
	public String getRanks(String dataset) {
		return results.get(dataset);
	}
	
	public Map<String,String> getResults() {
		return results;
	}

}
