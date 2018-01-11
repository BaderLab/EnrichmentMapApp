package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.baderlab.csplugins.enrichmentmap.AfterInjection;
import org.baderlab.csplugins.enrichmentmap.PropertyManager;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParams.Distance;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

import com.google.inject.Inject;

@SuppressWarnings("serial")
public class SettingsPopupPanel extends JPanel {
	
	@Inject private PropertyManager propertyManager;
	@Inject private IconManager iconManager;
	
	private JButton addRanksButton;
	private JButton exportTxtButton;
	private JButton exportPdfButton;
	
	private JButton geneManiaButton;
	
	private JRadioButton cosineRadio;
	private JRadioButton euclideanRadio;
	private JRadioButton pearsonRadio;
	
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
		setOpaque(false);
		
		Font iconFont = iconManager.getIconFont(13.0f);
		addRanksButton = new JButton("Add Rankings");
		addRanksButton.setIcon(SwingUtil.iconFromString(IconManager.ICON_PLUS, iconFont));
		exportTxtButton = new JButton("Export as TXT");
		exportTxtButton.setIcon(SwingUtil.iconFromString(IconManager.ICON_EXTERNAL_LINK, iconFont));
		exportPdfButton = new JButton("Export as PDF");
		exportPdfButton.setIcon(SwingUtil.iconFromString(IconManager.ICON_EXTERNAL_LINK, iconFont));
		SwingUtil.makeSmall(addRanksButton, exportTxtButton, exportPdfButton);
		
		geneManiaButton = new JButton("Open on GeneMANIA...");
		SwingUtil.makeSmall(geneManiaButton);
		
		JLabel distanceLabel = new JLabel("  Hierarchical Cluster - Distance Metric  ");
		cosineRadio = new JRadioButton("Cosine");
		euclideanRadio = new JRadioButton("Euclidean");
		pearsonRadio = new JRadioButton("Pearson Correlation");
		JPanel distanceRadioPanel = createButtonPanel(cosineRadio, euclideanRadio, pearsonRadio);
		SwingUtil.makeSmall(distanceLabel);
				
		cosineRadio.addActionListener(cosineListener = dmListenerFor(Distance.COSINE));
		euclideanRadio.addActionListener(euclideanListener = dmListenerFor(Distance.EUCLIDEAN));
		pearsonRadio.addActionListener(pearsonListener = dmListenerFor(Distance.PEARSON));
		
		JCheckBox autofocusCheckbox = new JCheckBox("Auto-Focus HeatMap");
		autofocusCheckbox.setSelected(propertyManager.getValue(PropertyManager.HEATMAP_AUTOFOCUS));
		autofocusCheckbox.addActionListener(e -> {
			propertyManager.setValue(PropertyManager.HEATMAP_AUTOFOCUS, autofocusCheckbox.isSelected());
		});
		SwingUtil.makeSmall(autofocusCheckbox);
		
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addComponent(addRanksButton)
			.addComponent(exportTxtButton)
			.addComponent(exportPdfButton)
			.addComponent(geneManiaButton)
			.addComponent(distanceLabel)
			.addComponent(distanceRadioPanel)
			.addComponent(autofocusCheckbox)
		);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addGap(5)
			.addComponent(addRanksButton)
			.addComponent(exportTxtButton)
			.addComponent(exportPdfButton)
			.addGap(5)
			.addComponent(geneManiaButton)
			.addGap(5)
			.addComponent(distanceLabel)
			.addComponent(distanceRadioPanel)
			.addGap(10)
			.addComponent(autofocusCheckbox)
			.addGap(5)
		);
		
		layout.linkSize(SwingConstants.HORIZONTAL, addRanksButton, exportTxtButton, exportPdfButton);
	}
	
	
	private <T> ActionListener dmListenerFor(Distance dm) {
		return e -> {
			if(distanceConsumer != null) {
				distanceConsumer.accept(dm);
			}
		};
	}
	
	public JButton getAddRanksButton() {
		return addRanksButton;
	}

	public JButton getExportTxtButton() {
		return exportTxtButton;
	}

	public JButton getExportPdfButton() {
		return exportPdfButton;
	}
	
	public JButton getGeneManiaButton() {
		return geneManiaButton;
	}
	
	public void update(HeatMapParams params) {
		cosineRadio.removeActionListener(cosineListener);
		euclideanRadio.removeActionListener(euclideanListener);
		pearsonRadio.removeActionListener(pearsonListener);
		
		switch(params.getDistanceMetric()) {
			case COSINE:    cosineRadio.setSelected(true);    break;
			case EUCLIDEAN: euclideanRadio.setSelected(true); break;
			case PEARSON:   pearsonRadio.setSelected(true);   break;
		}
		
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
	
	private static JPanel createButtonPanel(JRadioButton ... buttons) {
		JPanel panel = new JPanel(new GridLayout(buttons.length, 1));
		ButtonGroup group = new ButtonGroup();
		for(JRadioButton button : buttons) {
			panel.add(button);
			group.add(button);
			SwingUtil.makeSmall(button);
		}
		panel.setOpaque(false);
		return panel;
	}

	public void popup(Component parent) {
		JPopupMenu menu = new JPopupMenu();
		menu.add(this);
		menu.addMouseListener(new MouseAdapter() {
			@Override public void mouseClicked(MouseEvent e) {
				if(SwingUtilities.isRightMouseButton(e)) {
					menu.setVisible(false);
				}
			}
		});
		menu.show(parent, 0, parent.getHeight());
	}
}
