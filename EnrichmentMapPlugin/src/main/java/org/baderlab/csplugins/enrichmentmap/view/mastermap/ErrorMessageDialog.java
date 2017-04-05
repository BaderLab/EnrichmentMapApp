package org.baderlab.csplugins.enrichmentmap.view.mastermap;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.baderlab.csplugins.enrichmentmap.AfterInjection;
import org.baderlab.csplugins.enrichmentmap.view.util.GBCFactory;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.cytoscape.util.swing.IconManager;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

@SuppressWarnings("serial")
public class ErrorMessageDialog extends JDialog {

	@Inject private IconManager iconManager;
	
	private JPanel messagePanel;
	private int y = 0;
	
	public interface Factory {
		ErrorMessageDialog create(JDialog parent);
	}
	
	@Inject
	public ErrorMessageDialog(@Assisted JDialog parent) {
		super(parent);
		setMinimumSize(new Dimension(300, 350));
		setResizable(true);
		setTitle("Create Enrichment Map: Error");
	}
	
	@AfterInjection
	private void createContents() {
		messagePanel = new JPanel(new GridBagLayout());
		JPanel buttonPanel = createButtonPanel();
		
		messagePanel.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
		buttonPanel .setBorder(BorderFactory.createMatteBorder(1,0,0,0,Color.LIGHT_GRAY));
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(messagePanel, BorderLayout.CENTER);
		panel.add(buttonPanel, BorderLayout.SOUTH);
		setContentPane(panel);
	}
	
	private JPanel createButtonPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		JButton cancelButton = new JButton("OK");
		cancelButton.addActionListener(e -> dispose());
		panel.add(cancelButton, BorderLayout.EAST);
		return panel;
	}
	
	public void addSection(String title, String icon, List<String> messages) {
		JLabel iconLabel = new JLabel(" " + icon + "  ");
		iconLabel.setFont(iconManager.getIconFont(13.0f));
		JLabel titleLabel = new JLabel(title);
		SwingUtil.makeSmall(titleLabel);
		
		messagePanel.add(iconLabel,  GBCFactory.grid(0,y).insets(2).get());
		messagePanel.add(titleLabel, GBCFactory.grid(1,y).insets(2).gridwidth(2).weightx(1.0).get());
		y++;
		
		for(String message : messages) {
			JLabel errorIcon = new JLabel(IconManager.ICON_TIMES_CIRCLE);
			errorIcon.setFont(iconManager.getIconFont(13.0f));
			errorIcon.setForeground(Color.RED.darker());
			
			JLabel messageLabel = new JLabel(message);
			SwingUtil.makeSmall(messageLabel);
			messagePanel.add(errorIcon,    GBCFactory.grid(1,y).insets(2).get());
			messagePanel.add(messageLabel, GBCFactory.grid(2,y).insets(2).get());
			y++;
		}
	}
	
}
