package org.baderlab.csplugins.enrichmentmap.view.creation;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.baderlab.csplugins.enrichmentmap.EMBuildProps.HELP_URL_HOME;
import static org.baderlab.csplugins.enrichmentmap.EMBuildProps.HELP_URL_PROTOCOL;
import static org.baderlab.csplugins.enrichmentmap.EMBuildProps.HELP_URL_TUTORIAL;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.baderlab.csplugins.enrichmentmap.AfterInjection;
import org.baderlab.csplugins.enrichmentmap.view.util.OpenBrowser;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.cytoscape.util.swing.IconManager;

import com.google.inject.Inject;



@SuppressWarnings("serial")
public class DetailGettingStartedPanel extends JPanel {

	private @Inject IconManager iconManager;
	private @Inject OpenBrowser openBrowser;
	
	private Runnable scanButtonCallback;
	
	public DetailGettingStartedPanel setScanButtonCallback(Runnable callback) {
		this.scanButtonCallback = callback;
		return this;
	}
	
	@AfterInjection
	public void createContents() {
		JLabel header = new JLabel("<html><h2>Getting Started with EnrichmentMap</h2></html>");
		JLabel message = new JLabel("Click the \"Add\" button");
		
		JButton scanButton = SwingUtil.createIconTextButton(iconManager, IconManager.ICON_FOLDER_O, "Scan a folder for enrichment data", null);
		scanButton.addActionListener(e -> {
			if(scanButtonCallback != null)
				scanButtonCallback.run();
		});
		
		JButton link1 = SwingUtil.createLinkButton(openBrowser, "View online help", HELP_URL_HOME);
		JButton link2 = SwingUtil.createLinkButton(openBrowser, "Tutorial and sample data", HELP_URL_TUTORIAL);
		JButton link3 = SwingUtil.createLinkButton(openBrowser, "EnrichmentMap protocol", HELP_URL_PROTOCOL);
		
		final GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(
			layout.createSequentialGroup()
				.addGap(0, 0, Short.MAX_VALUE)
				.addGroup(layout.createParallelGroup(Alignment.CENTER)
					.addComponent(header, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(message)
//					.addComponent(scanButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(link1, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(link2, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(link3, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addGap(0, 0, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addGap(0, 0, Short.MAX_VALUE)
			.addComponent(header, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			.addGap(2, 10, 10)
			.addComponent(message)
			.addGap(2, 20, 20)
//			.addComponent(scanButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			.addComponent(link1, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			.addComponent(link2, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			.addComponent(link3, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			.addGap(0, 0, Short.MAX_VALUE)
		);
		
		setOpaque(false);
	}

	
}
