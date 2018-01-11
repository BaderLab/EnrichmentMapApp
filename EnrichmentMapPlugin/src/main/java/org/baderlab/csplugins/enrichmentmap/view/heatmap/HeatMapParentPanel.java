package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.BorderLayout;
import java.awt.Component;
import java.net.URL;
import java.util.List;
import java.util.Set;

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
import com.google.inject.assistedinject.Assisted;


@SuppressWarnings("serial")
public class HeatMapParentPanel extends JPanel implements CytoPanelComponent2 {

	public static final String ID = "enrichmentmap.view.ExpressionViewerPanel";
	
	@Inject private HeatMapMainPanel.Factory mainPanelFactory;
	
	private final HeatMapMediator mediator;
	private HeatMapMainPanel mainPanel;
	
	
	public interface Factory {
		HeatMapParentPanel create(HeatMapMediator mediator);
	}
	
	@Inject
	public HeatMapParentPanel(@Assisted HeatMapMediator mediator) {
		this.mediator = mediator;
	}
	
	@AfterInjection
	public void CreateContents() {
		setLayout(new BorderLayout());
		setOpaque(false);
		showEmptyView();
	}
	
	public HeatMapMediator getMediator() {
		return mediator;
	}
	
	public synchronized HeatMapMainPanel selectGenes(EnrichmentMap map, HeatMapParams params,
			List<RankingOption> moreRankOptions, Set<String> union, Set<String> intersection) {
		if (mainPanel == null) {
			removeAll();
			mainPanel = mainPanelFactory.create(this);
			add(mainPanel, BorderLayout.CENTER);
		}
		
		mainPanel.reset(map, params, moreRankOptions, union, intersection);
		
		return mainPanel;
	}
	
	public synchronized void showEmptyView() {
		removeAll();
		mainPanel = null;
		add(new NullViewPanel(), BorderLayout.CENTER);
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
		URL url = getClass().getClassLoader().getResource("images/enrichmentmap_logo_16.png");
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
