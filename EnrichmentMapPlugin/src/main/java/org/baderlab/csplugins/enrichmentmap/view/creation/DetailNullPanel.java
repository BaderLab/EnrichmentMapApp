package org.baderlab.csplugins.enrichmentmap.view.creation;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.baderlab.csplugins.enrichmentmap.EnrichmentMapBuildProperties.HELP_URL_CREATE;

import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.baderlab.csplugins.enrichmentmap.view.util.OpenBrowser;

import com.google.inject.Inject;



@SuppressWarnings("serial")
public class DetailNullPanel extends JPanel {

	private @Inject OpenBrowser openBrowser;
	
	private Runnable scanButtonCallback;
	
	public DetailNullPanel setScanButtonCallback(Runnable callback) {
		this.scanButtonCallback = callback;
		return this;
	}
	
	@Inject
	public DetailNullPanel() {
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
	
	
	private ImageIcon getFolderIcon() {
		BufferedImage iconImg;
		try {
			URL url = getClass().getClassLoader().getResource("images/folder_button.png");
			iconImg = ImageIO.read(url);
		} catch (IOException e) {
			return null;
		}
		
		final int w = 28, h = 24;
		BufferedImage resized = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g = (Graphics2D) resized.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	    g.drawImage(iconImg, 0, 0, w, h, null);
	    g.dispose();
	    
	    return new ImageIcon(resized);
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
