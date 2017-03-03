package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.BorderLayout;
import java.awt.Component;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.baderlab.csplugins.enrichmentmap.AfterInjection;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.cytoscape.application.swing.CytoPanelComponent2;
import org.cytoscape.application.swing.CytoPanelName;

import com.google.inject.Inject;


@SuppressWarnings("serial")
public class HeatMapParentPanel extends JPanel implements CytoPanelComponent2 {

	public static final String ID = "enrichmentmap.view.ExpressionViewerPanel";
	
	@Inject private HeatMapMainPanel.Factory mainPanelFactory;
	
	private HeatMapMainPanel mainPanel;
	private Consumer<HeatMapParams> heatMapParamsChangeListener;
	
	
	@AfterInjection
	public void CreateContents() {
		setLayout(new BorderLayout());
		setOpaque(false);
		showEmptyView();
	}
	
	public void selectGenes(EnrichmentMap map, HeatMapParams params, ClusterRankingOption clusterRankOption, List<RankingOption> moreRankOptions, Set<String> union, Set<String> intersection) {
		if(mainPanel == null) {
			removeAll();
			mainPanel = mainPanelFactory.create(this);
			add(mainPanel, BorderLayout.CENTER);
		}
		mainPanel.reset(map, params, clusterRankOption, moreRankOptions, union, intersection);
	}
	
	
	public void showEmptyView() {
		removeAll();
		mainPanel = null;
		add(new NullViewPanel(), BorderLayout.CENTER);
	}
	
	
	public void setHeatMapParamsChangeListener(Consumer<HeatMapParams> listener) {
		this.heatMapParamsChangeListener = listener;
	}
	
	void settingsChanged(HeatMapParams params) {
		if(heatMapParamsChangeListener != null) {
			heatMapParamsChangeListener.accept(params);
		}
	}
	
	private class NullViewPanel extends JPanel {
		
		public NullViewPanel() {
			JLabel infoLabel = new JLabel("No EnrichmentMap View selected");
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
	
	
	@Override
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.SOUTH;
	}

	@Override
	public String getTitle() {
		return "Heat Map";
	}

	@Override
	public Icon getIcon() {
		String path = "org/baderlab/csplugins/enrichmentmap/view/enrichmentmap_logo_notext_small.png";
		URL url = getClass().getClassLoader().getResource(path);
		return url == null ? null : new ImageIcon(url);
	}

	@Override
	public String getIdentifier() {
		return ID;
	}

	@Override
	public Component getComponent() {
		return this;
	}
}
