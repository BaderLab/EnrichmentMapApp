package org.baderlab.csplugins.enrichmentmap.view.creation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

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
	private boolean shouldContinue = false;
	private JButton finishButton;
	private JCheckBox doNotShowCheckbox;
	private boolean hasErrors = false;
	
	public interface Factory {
		ErrorMessageDialog create(JDialog parent);
	}
	
	@Inject
	public ErrorMessageDialog(@Assisted JDialog parent) {
		super(parent);
		setResizable(true);
		setTitle("Create Enrichment Map: Validation");
		setMinimumSize(new Dimension(430, 100));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}
	
	@AfterInjection
	private void createContents() {
		messagePanel = new JPanel(new GridBagLayout());
		JPanel buttonPanel = createButtonPanel();
		
		messagePanel.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
		buttonPanel .setBorder(BorderFactory.createMatteBorder(1,0,0,0, UIManager.getColor("Separator.foreground")));
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(messagePanel, BorderLayout.CENTER);
		panel.add(buttonPanel, BorderLayout.SOUTH);
		setContentPane(panel);
	}
	
	private JPanel createButtonPanel() {
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		
		doNotShowCheckbox = new JCheckBox("Do not warn me again");
		SwingUtil.makeSmall(doNotShowCheckbox);
		
		JButton cancelButton = new JButton("Cancel");
		buttonPanel.add(cancelButton);
		cancelButton.addActionListener(e -> dispose());
		
		finishButton = new JButton("Continue to Build");
		buttonPanel.add(finishButton);
		finishButton.addActionListener(e -> {
			shouldContinue = true;
			dispose();
		});
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(doNotShowCheckbox, BorderLayout.WEST);
		panel.add(buttonPanel, BorderLayout.EAST);
		
		return panel;
	}
	
	public boolean isDontWarnAgain() {
		return doNotShowCheckbox.isSelected();
	}
	
	public boolean shouldContinue() {
		return shouldContinue;
	}
	
	public boolean isEmpty() {
		return y == 0;
	}
	
	public void addSection(Message message, DetailPanel panel) {
		addSection(message, panel.getDisplayName(), panel.getIcon());
	}

	public void addSection(List<Message> messages, DetailPanel panel) {
		addSection(messages, panel.getDisplayName(), panel.getIcon());
	}
	
	public void addSection(Message message, String title, String icon) {
		addSection(Arrays.asList(message), title, icon);
	}
	
	public void addSection(List<Message> messages, String title, String icon) {
		final boolean hasError = messages.stream().anyMatch(Message::isError);
		if(hasError) {
			finishButton.setVisible(false);
			doNotShowCheckbox.setVisible(false);
			hasErrors = true;
		}
		
		JLabel iconLabel = new JLabel(" " + icon + "  ");
		iconLabel.setFont(iconManager.getIconFont(13.0f));
		JLabel titleLabel = new JLabel(title);
		SwingUtil.makeSmall(titleLabel);
		
		messagePanel.add(iconLabel,  GBCFactory.grid(0,y).insets(2).get());
		messagePanel.add(titleLabel, GBCFactory.grid(1,y).insets(2).gridwidth(2).weightx(1.0).get());
		y++;
		
		for(Message message : messages) {
			JLabel errorIcon = new JLabel(message.isError() ? IconManager.ICON_TIMES_CIRCLE : IconManager.ICON_EXCLAMATION_TRIANGLE);
			errorIcon.setFont(iconManager.getIconFont(13.0f));
			errorIcon.setForeground(message.isError() ? Color.RED.darker() : Color.YELLOW.darker());
			
			JLabel messageLabel = new JLabel(message.getMessage());
			SwingUtil.makeSmall(messageLabel);
			messagePanel.add(errorIcon,    GBCFactory.grid(1,y).insets(2).get());
			messagePanel.add(messageLabel, GBCFactory.grid(2,y).insets(2).get());
			y++;
		}
	}
	
	public boolean hasErrors() {
		return hasErrors;
	}
	
}
