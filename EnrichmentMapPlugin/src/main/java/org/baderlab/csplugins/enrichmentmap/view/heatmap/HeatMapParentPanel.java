package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.CardLayout;
import java.awt.Component;
import java.net.URL;

import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.baderlab.csplugins.enrichmentmap.AfterInjection;
import org.cytoscape.application.swing.CytoPanelComponent2;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.util.swing.LookAndFeelUtil;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
@SuppressWarnings("serial")
public class HeatMapParentPanel extends JPanel implements CytoPanelComponent2 {

	public static final String ID = "enrichmentmap.view.ExpressionViewerPanel";
	
	@Inject private HeatMapContentPanel contentPanel;
	
	private final NullContentPanel nullContentPanel = new NullContentPanel();
	private final CardLayout cardLayout = new CardLayout();

	@AfterInjection
	public void CreateContents() {
		if (LookAndFeelUtil.isAquaLAF())
			setOpaque(false);
		
		setLayout(cardLayout);
		add(nullContentPanel, nullContentPanel.getName());
		add(contentPanel, contentPanel.getName());
		showEmptyView();
	}
	
	public void showContentPanel() {
		cardLayout.show(this, contentPanel.getName());
	}
	
	public void showEmptyView() {
		cardLayout.show(this, nullContentPanel.getName());
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
	
	private class NullContentPanel extends JPanel {
		
		public static final String NAME = "__NULL_HEAT_MAP_CONTENT_PANEL";
		
		public NullContentPanel() {
			setName(NAME);
			
			JLabel infoLabel = new JLabel("No EnrichmentMap View selected");
			infoLabel.setEnabled(false);
			infoLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
			
			GroupLayout layout = new GroupLayout(this);
			setLayout(layout);
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
}
