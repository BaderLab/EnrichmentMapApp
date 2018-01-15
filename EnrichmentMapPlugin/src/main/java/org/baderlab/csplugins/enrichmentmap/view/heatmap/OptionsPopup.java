package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;

import org.baderlab.csplugins.enrichmentmap.AfterInjection;
import org.baderlab.csplugins.enrichmentmap.PropertyManager;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParams.Distance;
import org.baderlab.csplugins.enrichmentmap.view.util.TextIcon;
import org.cytoscape.util.swing.IconManager;

import com.google.inject.Inject;

@SuppressWarnings("serial")
public class OptionsPopup extends JPopupMenu {
	
	@Inject private PropertyManager propertyManager;
	@Inject private IconManager iconManager;
	
	private JMenuItem addRanksButton;
	private JMenuItem exportTxtButton;
	private JMenuItem exportPdfButton;
	
	private JMenuItem geneManiaButton;
	
	private JCheckBoxMenuItem cosineRadio;
	private JCheckBoxMenuItem euclideanRadio;
	private JCheckBoxMenuItem pearsonRadio;
	
	private Consumer<Distance> distanceConsumer;
	
	private ActionListener cosineListener;
	private ActionListener euclideanListener;
	private ActionListener pearsonListener;
	
	public void setDistanceConsumer(Consumer<Distance> dmConsumer) {
		this.distanceConsumer = dmConsumer;
	}
	
	/**
	 * Cannot use JComboBox on a JPopupMenu because of a bug in swing: 
	 * http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4799266
	 */
	@AfterInjection
	private void createContents() {	
		Font iconFont = iconManager.getIconFont(12.0f);
		Color iconColor = UIManager.getColor("Label.foreground");
		int iconSize = 16;
		
		addRanksButton = new JMenuItem("Add Rankings...");
		addRanksButton.setIcon(new TextIcon(IconManager.ICON_PLUS, iconFont, iconColor, iconSize, iconSize));
		exportTxtButton = new JMenuItem("Export as TXT...");
		exportTxtButton.setIcon(new TextIcon(IconManager.ICON_EXTERNAL_LINK, iconFont, iconColor, iconSize, iconSize));
		exportPdfButton = new JMenuItem("Export as PDF...");
		exportPdfButton.setIcon(new TextIcon(IconManager.ICON_EXTERNAL_LINK, iconFont, iconColor, iconSize, iconSize));
		
		geneManiaButton = new JMenuItem("Open on GeneMANIA...");
		
		JMenu distanceMenu = new JMenu("Hierarchical Cluster - Distance Metric");
		
		cosineRadio = new JCheckBoxMenuItem("Cosine");
		cosineRadio.addActionListener(cosineListener = dmListenerFor(Distance.COSINE));
		distanceMenu.add(cosineRadio);
		
		euclideanRadio = new JCheckBoxMenuItem("Euclidean");
		euclideanRadio.addActionListener(euclideanListener = dmListenerFor(Distance.EUCLIDEAN));
		distanceMenu.add(euclideanRadio);
		
		pearsonRadio = new JCheckBoxMenuItem("Pearson Correlation");
		pearsonRadio.addActionListener(pearsonListener = dmListenerFor(Distance.PEARSON));
		distanceMenu.add(pearsonRadio);
				
		JCheckBoxMenuItem autofocusCheckbox = new JCheckBoxMenuItem("Auto-Focus HeatMap");
		autofocusCheckbox.setSelected(propertyManager.getValue(PropertyManager.HEATMAP_AUTOFOCUS));
		autofocusCheckbox.addActionListener(e -> {
			propertyManager.setValue(PropertyManager.HEATMAP_AUTOFOCUS, autofocusCheckbox.isSelected());
		});
		
		add(geneManiaButton);
		addSeparator();
		add(addRanksButton);
		add(exportTxtButton);
		add(exportPdfButton);
		addSeparator();
		add(distanceMenu);
		add(autofocusCheckbox);
	}
	
	private <T> ActionListener dmListenerFor(Distance dm) {
		return e -> {
			if(distanceConsumer != null) {
				distanceConsumer.accept(dm);
				updateDistanceMenu(dm);
			}
		};
	}
	
	public JMenuItem getAddRanksButton() {
		return addRanksButton;
	}

	public JMenuItem getExportTxtButton() {
		return exportTxtButton;
	}

	public JMenuItem getExportPdfButton() {
		return exportPdfButton;
	}
	
	public JMenuItem getGeneManiaButton() {
		return geneManiaButton;
	}
	
	public void update(HeatMapParams params) {
		cosineRadio.removeActionListener(cosineListener);
		euclideanRadio.removeActionListener(euclideanListener);
		pearsonRadio.removeActionListener(pearsonListener);
		
		updateDistanceMenu(params.getDistanceMetric());
		
		cosineRadio.addActionListener(cosineListener);
		euclideanRadio.addActionListener(euclideanListener);
		pearsonRadio.addActionListener(pearsonListener);
	}

	private void updateDistanceMenu(Distance dm) {
		switch (dm) {
			case COSINE:
				cosineRadio.setSelected(true);
				euclideanRadio.setSelected(false);
				pearsonRadio.setSelected(false);
				break;
			case EUCLIDEAN:
				cosineRadio.setSelected(false);
				euclideanRadio.setSelected(true);
				pearsonRadio.setSelected(false);
				break;
			case PEARSON:
				cosineRadio.setSelected(false);
				euclideanRadio.setSelected(false);
				pearsonRadio.setSelected(true);
				break;
		}
	}
	
	
	public Distance getDistance() {
		if(cosineRadio.isSelected())
			return Distance.COSINE;
		if(euclideanRadio.isSelected())
			return Distance.EUCLIDEAN;
		return Distance.PEARSON;
	}
	
	public void popup(Component parent) {
		show(parent, 0, parent.getHeight());
	}
}
