package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.baderlab.csplugins.enrichmentmap.view.util.IconUtil.EM_ICON_COLORS;
import static org.baderlab.csplugins.enrichmentmap.view.util.IconUtil.LAYERED_EM_ICON;
import static org.baderlab.csplugins.enrichmentmap.view.util.IconUtil.getIconFont;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.baderlab.csplugins.enrichmentmap.AfterInjection;
import org.cytoscape.application.swing.CytoPanelComponent2;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.util.swing.TextIcon;

import com.google.inject.Singleton;

@Singleton
@SuppressWarnings("serial")
public class HeatMapPanel extends JPanel implements CytoPanelComponent2 {

	public static final String ID = "enrichmentmap.view.ExpressionViewerPanel";
	
	private final NullContentPanel nullContentPanel = new NullContentPanel();
	
	private final Icon compIcon = new TextIcon(LAYERED_EM_ICON, getIconFont(18.0f), EM_ICON_COLORS, 16, 16);

	@AfterInjection
	public void CreateContents() {
		if (LookAndFeelUtil.isAquaLAF())
			setOpaque(false);
		
		setLayout(new BorderLayout());
		add(nullContentPanel, BorderLayout.CENTER);
	}
	
	
	public void showContentPanel(HeatMapContentPanel contentPanel) {
		removeAll();
		if(contentPanel != null)
			add(contentPanel, BorderLayout.CENTER);
		else
			add(nullContentPanel, BorderLayout.CENTER);
		repaint();
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
		return compIcon;
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
