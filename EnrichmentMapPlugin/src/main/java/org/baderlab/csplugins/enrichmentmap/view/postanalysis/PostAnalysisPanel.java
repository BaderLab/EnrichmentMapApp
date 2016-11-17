package org.baderlab.csplugins.enrichmentmap.view.postanalysis;

import java.awt.BorderLayout;
import java.awt.Component;
import java.net.URL;
import java.util.WeakHashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import org.baderlab.csplugins.enrichmentmap.AfterInjection;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.util.swing.LookAndFeelUtil;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * A simple top-level panel which manages an instance of PostAnalysisInputPanel
 * for each enrichment map network. This allows user input to be saved when the
 * user switches networks without have to overhaul how PostAnalysisInputPanel works.
 */
@SuppressWarnings("serial")
@Singleton
public class PostAnalysisPanel extends JPanel implements CytoPanelComponent {
	
	@Inject private Provider<PostAnalysisInputPanel> panelProvider;
	
	private WeakHashMap<EnrichmentMap, PostAnalysisInputPanel> panels = new WeakHashMap<>();
    
	@AfterInjection
	private void createContents() {
		if (LookAndFeelUtil.isAquaLAF())
			setOpaque(false);

		setLayout(new BorderLayout());
	}

	public void showPanelFor(EnrichmentMap map) {
		PostAnalysisInputPanel panel;
		if (map == null) {
			// create a dummy panel that's disabled
			panel = newPostAnalysisInputPanel(null);
			SwingUtil.recursiveEnable(panel, false);
		} else {
			panel = panels.computeIfAbsent(map, this::newPostAnalysisInputPanel);
		}

		removeAll();
		add(panel, BorderLayout.CENTER);
		revalidate();
		repaint();
	}

	public void removeEnrichmentMap(EnrichmentMap map) {
		panels.remove(map);
	}

	private PostAnalysisInputPanel newPostAnalysisInputPanel(EnrichmentMap map) {
		PostAnalysisInputPanel panel = panelProvider.get();
		
		if (map != null)
			panel.initialize(map);
		
		return panel;
	}

	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.WEST;
	}

	@Override
	public Icon getIcon() {
		URL EMIconURL = this.getClass().getResource("enrichmentmap_logo_notext_small.png");
		ImageIcon EMIcon = null;
		
		if (EMIconURL != null)
			EMIcon = new ImageIcon(EMIconURL);
		
		return EMIcon;
	}

	@Override
	public String getTitle() {
		return "Post Analysis Input";
	}
}
