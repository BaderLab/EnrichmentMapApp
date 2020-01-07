package org.baderlab.csplugins.enrichmentmap.view.creation;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.baderlab.csplugins.enrichmentmap.EnrichmentMapBuildProperties.HELP_URL_CREATE;

import java.awt.Insets;
import java.net.URL;

import javax.swing.GroupLayout;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;

import org.baderlab.csplugins.enrichmentmap.view.util.OpenBrowser;

import com.google.inject.Inject;

@SuppressWarnings("serial")
public class DetailNullPanel extends JPanel {

	private @Inject OpenBrowser openBrowser;
	
	@Inject
	public DetailNullPanel() {
		URL logoURL = getClass().getClassLoader().getResource("images/folder_button.png");
		
		JEditorPane editorPane = new JEditorPane();
		editorPane.setMargin(new Insets(10, 10, 10, 10));
		editorPane.setEditable(false);
		editorPane.setEditorKit(new HTMLEditorKit());
		editorPane.setOpaque(false);
		editorPane.addHyperlinkListener(new HyperlinkAction());
		editorPane.setText(
			"<html><body style='font-family:Arial,Helvetica,sans-serif;'>" +
			"<h2>Getting Started with EnrichmentMap</h2>" +
			"Click the <img height='22' width='26' src='" + logoURL + "'> button to scan a folder for enrichment data<br>" +
			"<a href='" + HELP_URL_CREATE + "'>View online help</a><br>" +
			"<a href='" + HELP_URL_CREATE + "'>Download sample data</a><br>" +
			"</body></html>"
		);
		
		final GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGap(0, 0, Short.MAX_VALUE)
				.addComponent(editorPane, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addGap(0, 0, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGap(0, 0, Short.MAX_VALUE)
				.addComponent(editorPane)
				.addGap(0, 0, Short.MAX_VALUE)
		);
		
		setOpaque(false);
	}
	
	
	private class HyperlinkAction implements HyperlinkListener {
		@Override
		public void hyperlinkUpdate(HyperlinkEvent event) {
			if(event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				openBrowser.openURL(event.getURL().toString());
			}
		}
	}
}
