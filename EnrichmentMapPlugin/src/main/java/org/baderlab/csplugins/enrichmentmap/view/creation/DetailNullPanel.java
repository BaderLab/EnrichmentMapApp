package org.baderlab.csplugins.enrichmentmap.view.creation;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.baderlab.csplugins.enrichmentmap.EnrichmentMapBuildProperties.HELP_URL_CREATE;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.baderlab.csplugins.enrichmentmap.AfterInjection;
import org.baderlab.csplugins.enrichmentmap.view.util.OpenBrowser;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.TextIcon;

import com.google.inject.Inject;



@SuppressWarnings("serial")
public class DetailNullPanel extends JPanel {

	private @Inject IconManager iconManager;
	private @Inject OpenBrowser openBrowser;
	
	private Runnable scanButtonCallback;
	
	public DetailNullPanel setScanButtonCallback(Runnable callback) {
		this.scanButtonCallback = callback;
		return this;
	}
	
	@AfterInjection
	public void createContents() {
		JLabel header = new JLabel("<html><h2>Getting Started with EnrichmentMap</h2></html>");
		
		JButton scanButton = new JButton("Scan a folder for enrichment data");
		scanButton.setIcon(getFolderIcon());
		scanButton.addActionListener(e -> {
			if(scanButtonCallback != null)
				scanButtonCallback.run();
		});
		
		JButton link1 = createLinkButton("View online help", HELP_URL_CREATE);
		JButton link2 = createLinkButton("Download sample data", HELP_URL_CREATE);
		
		final GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(
			layout.createSequentialGroup()
				.addGap(0, 0, Short.MAX_VALUE)
				.addGroup(layout.createParallelGroup(Alignment.CENTER)
					.addComponent(header, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(scanButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(link1, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(link2, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addGap(0, 0, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addGap(0, 0, Short.MAX_VALUE)
			.addComponent(header, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			.addGap(2, 10, 10)
			.addComponent(scanButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			.addComponent(link1, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			.addComponent(link2, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			.addGap(0, 0, Short.MAX_VALUE)
		);
		
		setOpaque(false);
	}
	
	
	private Icon getFolderIcon() {
		Font iconFont = iconManager.getIconFont(12.0f);
		Color iconColor = UIManager.getColor("Label.foreground");
		int iconSize = 20;
		TextIcon icon = new TextIcon(IconManager.ICON_FOLDER_O, iconFont, iconColor, iconSize, iconSize);
		return icon;
	}
	
	
	private JButton createLinkButton(String text, String url) {
		JButton button = new JButton();
		button.setText("<html><font color=\"#000099\"><u>" + text + "</u></font></html>");
		button.setBorderPainted(false);
	    button.setOpaque(false);
	    button.setToolTipText(url);
	    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	    button.addActionListener(e -> {
	    	openBrowser.openURL(url);
	    });
	    return button;
	}
	
}
