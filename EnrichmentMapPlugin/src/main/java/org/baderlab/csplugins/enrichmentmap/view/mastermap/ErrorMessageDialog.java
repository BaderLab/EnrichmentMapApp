package org.baderlab.csplugins.enrichmentmap.view.mastermap;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
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
	
	public static enum MessageType {
		WARN, ERROR
	}
	
	private JPanel messagePanel;
	private int y = 0;
	private boolean shouldContinue = false;
	private JButton finishButton;
	
	public interface Factory {
		ErrorMessageDialog create(JDialog parent);
	}
	
	@Inject
	public ErrorMessageDialog(@Assisted JDialog parent) {
		super(parent);
		setResizable(true);
		setTitle("Create Enrichment Map: Validation");
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
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		
		JButton cancelButton = new JButton("Cancel");
		buttonPanel.add(cancelButton);
		cancelButton.addActionListener(e -> dispose());
		
		finishButton = new JButton("Continue to Build");
		buttonPanel.add(finishButton);
		finishButton.addActionListener(e -> {
			System.out.println("Here!");
			shouldContinue = true;
			dispose();
		});
		
		return buttonPanel;
	}
	
	public boolean shouldContinue() {
		return shouldContinue;
	}
	
	public void addSection(MessageType messageType, String title, String icon, List<String> messages) {
		final boolean isError = messageType == MessageType.ERROR;
		if(isError) {
			finishButton.setVisible(false);
		}
		
		JLabel iconLabel = new JLabel(" " + icon + "  ");
		iconLabel.setFont(iconManager.getIconFont(13.0f));
		JLabel titleLabel = new JLabel(title);
		SwingUtil.makeSmall(titleLabel);
		
		messagePanel.add(iconLabel,  GBCFactory.grid(0,y).insets(2).get());
		messagePanel.add(titleLabel, GBCFactory.grid(1,y).insets(2).gridwidth(2).weightx(1.0).get());
		y++;
		
		for(String message : messages) {
			JLabel errorIcon = new JLabel(isError ? IconManager.ICON_TIMES_CIRCLE : IconManager.ICON_EXCLAMATION_TRIANGLE);
			errorIcon.setFont(iconManager.getIconFont(13.0f));
			errorIcon.setForeground(isError ? Color.RED.darker() : Color.YELLOW.darker());
			
			JLabel messageLabel = new JLabel(message);
			SwingUtil.makeSmall(messageLabel);
			messagePanel.add(errorIcon,    GBCFactory.grid(1,y).insets(2).get());
			messagePanel.add(messageLabel, GBCFactory.grid(2,y).insets(2).get());
			y++;
		}
	}
	
}
