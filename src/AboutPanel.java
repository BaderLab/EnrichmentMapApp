import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

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
	
	/**
	 * Constructor
	 * @param owner
	 * @param modal
	 * @throws HeadlessException
	 */
	public AboutPanel(Frame owner, boolean modal) throws HeadlessException {
		super(owner, modal);
		initComponents();
		pack();
	}
	
	/**
	 * Content of About Panel
	 */
	public void initComponents() {
		this.setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );

		// Window Title
		this.setTitle("About EnrichmentMap Plugin");

		// Buttons and ActionListeners
		ActionListener openPluginHomeInBrowser = new ActionListener() { 
			public void actionPerformed( ActionEvent e ) { 
				OpenBrowser.openURL(pluginUrl); 
			} 
		}; 
		JButton button1 = new JButton("Visit Plugin Homepage");
		button1.addActionListener(openPluginHomeInBrowser) ;

		ActionListener disposeWindowAction = new ActionListener() { 
			public void actionPerformed( ActionEvent e ) { 
				dispose(); 
			} 
		}; 
		JButton button2 = new JButton("Close");
		button2.addActionListener(disposeWindowAction) ;

		// Box container
		Box box = new Box(BoxLayout.Y_AXIS);

		// Header
		JPanel p1 = new JPanel();
		p1.setLayout(new FlowLayout());
		p1.add( new JLabel("<html><h1>Enrichment Map Plugin</h1></html>" ) );
		box.add( p1 );

		// Plugin Description
		//TODO: write Plugin Description
		
		
		// Link to Homepage
		JPanel p3 = new JPanel();
		p3.setLayout(new FlowLayout());
		p3.add( new JLabel("<html>For more Information visit <a href=\"" + pluginUrl + "\" target=\"_blank\">" + pluginUrl + "</a></html>" ) );
		box.add( p3 );
		
		// Version Info
		//TODO: Check local properties to show either Version Info (default) or BuildID  
		JPanel p4 = new JPanel();
		p4.setLayout(new FlowLayout());
		p4.add( new JLabel(Enrichment_Map_Plugin.buildId));
		box.add( p4 );

		// Visit Homepage and Close buttons
		JPanel p5 = new JPanel();
		p5.setLayout(new FlowLayout());
		p5.add(button1);
		p5.add(new JPanel());
		p5.add(button2);
		box.add( p5 ); 

		this.add( box);
		this.pack();
//		this.setSize(500,300);
		
	}
	

}
