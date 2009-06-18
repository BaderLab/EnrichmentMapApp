import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;
import cytoscape.*;
import cytoscape.util.*;

/**
 * 
 */

/**
 * @author revilo
 *
 */
public class AboutPanel extends JDialog {
	String pluginUrl = "http://www.baderlab.org/Software/EnrichmentMaps/";
	String pluginVersion = Enrichment_Map_Plugin.plugin_props.getProperty("pluginVersion", "0.1");
	
	public AboutPanel() {
	    super(Cytoscape.getDesktop(), "About Enrichment Map", false);
	    setResizable(false);
	
	    //main panel for dialog box
	    JEditorPane editorPane = new JEditorPane();
	    editorPane.setMargin(new Insets(10,10,10,10));
	    editorPane.setEditable(false);
	    editorPane.setEditorKit(new HTMLEditorKit());
	    editorPane.addHyperlinkListener(new HyperlinkAction(editorPane));
	
	    
	    editorPane.setText(
	            "<html><body><p align=center><b>Enrichment Map v" +  pluginVersion + " </b><BR>" +
	            "A Cytoscape Plugin<BR>" +
	            "<BR>" +
	            "by Gary Bader, Daniele Merico, Ruth Isserlin and Oliver Stueker<BR>" +
	            "(<a href='http://www.baderlab.org/'>Bader Lab</a>, University of Toronto)<BR>" +
	            "<BR>" +
	            "Plugin Homepage:<BR>" +
	            "<a href='http://www.baderlab.org/Software/EnrichmentMap'>http://www.baderlab.org/Software/EnrichmentMap</a><BR>" +
	            "<BR>" +
	            "If you use this plugin in your research, please cite:<BR>" +
	            "Merico D, Isserlin R, Stueker O, Emili A, Bader GD<BR>" +
	            "Enrichment Map: a network-based method for <BR>" +
	            "gene-set enrichment visualization and interpretation<BR>" +
	            "<i>in preparation</i><BR>" +
	            "<BR>" +
	            "<font size='-1'>" + Enrichment_Map_Plugin.buildId + "</font>" +
	            "</p></body></html>");
	    setContentPane(editorPane);
	}

	private class HyperlinkAction implements HyperlinkListener {
	    JEditorPane pane;
	
	    public HyperlinkAction(JEditorPane pane) {
	        this.pane = pane;
	    }
	
	    public void hyperlinkUpdate(HyperlinkEvent event) {
	        if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
	            cytoscape.util.OpenBrowser.openURL(event.getURL().toString());
	        }
	    }
	}	
}
