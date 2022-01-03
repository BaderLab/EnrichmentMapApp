package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import static org.baderlab.csplugins.enrichmentmap.PropertyManager.HEATMAP_AUTOFOCUS;
import static org.baderlab.csplugins.enrichmentmap.PropertyManager.HEATMAP_AUTO_SORT;
import static org.baderlab.csplugins.enrichmentmap.PropertyManager.HEATMAP_DATASET_SYNC;
import static org.baderlab.csplugins.enrichmentmap.PropertyManager.HEATMAP_SELECT_SYNC;

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
import org.baderlab.csplugins.enrichmentmap.view.util.IconUtil;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.TextIcon;

import com.google.inject.Inject;

@SuppressWarnings("serial")
public class OptionsPopup extends JPopupMenu {
	
	@Inject private PropertyManager propertyManager;
	@Inject private IconManager iconManager;
	
	private JMenuItem addRanksButton;
	private JMenuItem exportTxtButton;
	private JMenuItem exportPdfButton;
	
	private JMenuItem geneManiaButton;
	private JMenuItem stringButton;
	private JMenuItem pcButton;
	
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
		
		geneManiaButton = new JMenuItem("Show in GeneMANIA...");
		geneManiaButton.setIcon(new TextIcon(IconUtil.GENEMANIA_ICON, IconUtil.getIconFont(14.0f), IconUtil.GENEMANIA_ICON_COLOR, iconSize, iconSize));

		stringButton = new JMenuItem("Show in STRING...");
		stringButton.setIcon(new TextIcon(IconUtil.LAYERED_STRING_ICON, IconUtil.getIconFont(16.0f), IconUtil.STRING_ICON_COLORS, iconSize, iconSize));
		
		pcButton = new JMenuItem("Show in Pathway Commons...");
		pcButton.setIcon(new TextIcon(IconUtil.LAYERED_PC_ICON, IconUtil.getIconFont(16.0f), IconUtil.PC_ICON_COLORS, iconSize, iconSize));
		
		JMenu distanceMenu = new JMenu("Hierarchical Cluster - Distance Metric");
		
		cosineRadio = new JCheckBoxMenuItem("Cosine");
		cosineRadio.addActionListener(cosineListener = distanceListenerFor(Distance.COSINE));
		distanceMenu.add(cosineRadio);
		
		euclideanRadio = new JCheckBoxMenuItem("Euclidean");
		euclideanRadio.addActionListener(euclideanListener = distanceListenerFor(Distance.EUCLIDEAN));
		distanceMenu.add(euclideanRadio);
		
		pearsonRadio = new JCheckBoxMenuItem("Pearson Correlation");
		pearsonRadio.addActionListener(pearsonListener = distanceListenerFor(Distance.PEARSON));
		distanceMenu.add(pearsonRadio);
		
		JCheckBoxMenuItem autofocusCheck = propertyManager.createJCheckBoxMenuItem(HEATMAP_AUTOFOCUS,    "Auto-Focus HeatMap");
		JCheckBoxMenuItem syncCheck      = propertyManager.createJCheckBoxMenuItem(HEATMAP_DATASET_SYNC, "Sync Data Sets with Control Panel");
		JCheckBoxMenuItem selectedCheck  = propertyManager.createJCheckBoxMenuItem(HEATMAP_SELECT_SYNC,  "Display only selected Data Sets");
		JCheckBoxMenuItem autoSortCheck  = propertyManager.createJCheckBoxMenuItem(HEATMAP_AUTO_SORT,    "Auto sort leading edge");
		
		add(geneManiaButton);
		add(stringButton);
		add(pcButton);
		addSeparator();
		add(addRanksButton);
		add(exportTxtButton);
		add(exportPdfButton);
		addSeparator();
		add(distanceMenu);
		add(autofocusCheck);
		add(syncCheck);
		add(selectedCheck);
		add(autoSortCheck);
	}
	
	
	private <T> ActionListener distanceListenerFor(Distance dm) {
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
	
	public JMenuItem getStringButton() {
		return stringButton;
	}
	
	public JMenuItem getPathwayCommonsButton() {
		return pcButton;
	}
	
	public void update(HeatMapParams params) {
		updateDistanceMenu(params.getDistanceMetric());
	}

	private void updateDistanceMenu(Distance dm) {
		cosineRadio.removeActionListener(cosineListener);
		euclideanRadio.removeActionListener(euclideanListener);
		pearsonRadio.removeActionListener(pearsonListener);
		
		cosineRadio.setSelected(dm == Distance.COSINE);
		euclideanRadio.setSelected(dm == Distance.EUCLIDEAN);
		pearsonRadio.setSelected(dm == Distance.PEARSON);
		
		cosineRadio.addActionListener(cosineListener);
		euclideanRadio.addActionListener(euclideanListener);
		pearsonRadio.addActionListener(pearsonListener);
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
