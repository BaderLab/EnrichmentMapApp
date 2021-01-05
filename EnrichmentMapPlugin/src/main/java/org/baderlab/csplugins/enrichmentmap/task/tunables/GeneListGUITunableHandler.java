package org.baderlab.csplugins.enrichmentmap.task.tunables;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.GSEALeadingEdgeRankingOption;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.AbstractGUITunableHandler;

public class GeneListGUITunableHandler extends AbstractGUITunableHandler {

	private JPanel panel;
	private GeneListPanel checkboxPanel;
	
	public GeneListGUITunableHandler(Field field, Object instance, Tunable tunable) {
		super(field, instance, tunable);
		init();
	}

	public GeneListGUITunableHandler(Method getter, Method setter, Object instance, Tunable tunable) {
		super(getter, setter, instance, tunable);
		init();
	}
	
	private GeneListTunable getGeneListTunable() {
		try {
			return (GeneListTunable) getValue();
		} catch(final Exception e) {
			throw new IllegalStateException("bad object", e);	
		}
	}
	
	private void init() {
		panel = createGeneListPanel();
	}
	
	private JPanel createGeneListPanel() {
		JLabel title = new JLabel(getDescription());
		
		GeneListTunable geneListTunable = getGeneListTunable();
		EnrichmentMap map = geneListTunable.getEnrichmentMap();
		List<String> genes = geneListTunable.getGenes();
		List<GSEALeadingEdgeRankingOption> leadingEdgeRanks = geneListTunable.getLeadingEdgeRanks();
		
		checkboxPanel = new GeneListPanel(map, genes, leadingEdgeRanks);
		
		JPanel panel = new JPanel();
		
		final GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addComponent(title)
			.addGroup(layout.createSequentialGroup()
				.addComponent(checkboxPanel)
			)
		);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addComponent(title)
			.addGroup(layout.createParallelGroup()
				.addComponent(checkboxPanel)
			)
		);
		
		if(LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);
		return panel;
	}
	 
	@Override
	public JPanel getJPanel() {
		return panel;
	}

	@Override
	public void handle() {
		List<String> selectedGenes = checkboxPanel.getSelectedDataItems();
		getGeneListTunable().setSelectedGenes(selectedGenes);
	}

	@Override
	public void update() {
	}

}
