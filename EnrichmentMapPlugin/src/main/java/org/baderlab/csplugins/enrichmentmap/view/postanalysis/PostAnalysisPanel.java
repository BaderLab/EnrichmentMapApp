package org.baderlab.csplugins.enrichmentmap.view.postanalysis;

import java.awt.BorderLayout;
import java.util.WeakHashMap;

import javax.swing.JPanel;

import org.baderlab.csplugins.enrichmentmap.AfterInjection;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.cytoscape.util.swing.LookAndFeelUtil;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * A simple top-level panel which manages an instance of PostAnalysisInputPanel
 * for each enrichment map network. This allows user input to be saved when the
 * user switches networks without have to overhaul how PostAnalysisInputPanel works.
 */
@Singleton
@SuppressWarnings("serial")
public class PostAnalysisPanel extends JPanel {
	
	@Inject private PostAnalysisInputPanel.Factory panelFactory;
	
	private WeakHashMap<EnrichmentMap, PostAnalysisInputPanel> panels = new WeakHashMap<>();
    
	@AfterInjection
	private void createContents() {
		if (LookAndFeelUtil.isAquaLAF())
			setOpaque(false);

		setLayout(new BorderLayout());
	}

	void update(EnrichmentMap map) {
		final PostAnalysisInputPanel panel;
		
		if (map == null) {
			// create a dummy panel that's disabled
			panel = createPostAnalysisInputPanel(null);
			SwingUtil.recursiveEnable(panel, false);
		} else {
			panel = panels.computeIfAbsent(map, this::createPostAnalysisInputPanel);
		}

		removeAll();
		add(panel, BorderLayout.CENTER);
		revalidate();
		repaint();
	}

	void removeEnrichmentMap(EnrichmentMap map) {
		panels.remove(map);
	}
	
	void reset(EnrichmentMap map) {
		PostAnalysisInputPanel panel = panels.get(map);
		
		if (panel != null)
			panel.reset();
	}
	
	void run(EnrichmentMap map) {
		PostAnalysisInputPanel panel = panels.get(map);
		
		if (panel != null)
			panel.run();
	}

	private PostAnalysisInputPanel createPostAnalysisInputPanel(EnrichmentMap map) {
		PostAnalysisInputPanel panel = panelFactory.create(this, map);
		return panel;
	}
}
