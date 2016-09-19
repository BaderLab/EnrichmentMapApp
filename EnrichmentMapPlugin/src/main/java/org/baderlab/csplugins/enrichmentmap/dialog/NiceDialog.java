package org.baderlab.csplugins.enrichmentmap.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.baderlab.csplugins.enrichmentmap.dialog.NiceDialogCallback.Message;
import org.cytoscape.util.swing.IconManager;

public class NiceDialog {
	
	private JLabel subTitle;
	private JPanel message;
	
	private JDialog dialog;
	private JButton finishButton;
	
	private final NiceDialogController controller;
	private final IconManager iconManager;
	
	
	public NiceDialog(JFrame parent, IconManager iconManager, NiceDialogController controller) {
		if(controller == null || iconManager == null)
			throw new NullPointerException();
		this.controller = controller;
		this.iconManager = iconManager;
		
		dialog = new JDialog(parent);
		createComponents();
	}
	
	
	public void open() {
		dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		dialog.pack();
		dialog.setLocationRelativeTo(dialog.getParent());
		dialog.setVisible(true);
	}
	
	
	private void createComponents() {
		dialog.setLayout(new BorderLayout());
		dialog.setMinimumSize(controller.getMinimumSize());
		dialog.setTitle(controller.getTitle());
		
		// Create message and button panel first because 
		// the controller can call callbacks from inside createBodyPanel()
		JPanel messagePanel = createMessagePanel();
		JPanel buttonPanel  = createButtonPanel();
		JPanel bodyPanel    = createBodyPanel();
	    
	    messagePanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
	    buttonPanel .setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
	    
	    dialog.add(messagePanel, BorderLayout.NORTH);
	    dialog.add(bodyPanel,    BorderLayout.CENTER);
	    dialog.add(buttonPanel,  BorderLayout.SOUTH);
	}
	
	private JPanel createBodyPanel() {
	    Callback callback = new Callback();
	    JPanel body = controller.createBodyPanel(callback);
	    if(body == null) {
	    	throw new NullPointerException("body panel is null");
	    }
	    
	    JPanel bodyPanel = new JPanel(new BorderLayout());
	    bodyPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	    bodyPanel.add(body, BorderLayout.CENTER);
	    return bodyPanel;
	}
	
	
	private JPanel createMessagePanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(panel.getBackground().brighter());

		JPanel messagePanel = new JPanel(new BorderLayout());
		subTitle = new JLabel("<html><b>" + controller.getSubTitle() + "</b></html>");
		message = new JPanel(new BorderLayout());
		message.setOpaque(false);
		
		subTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
		
		messagePanel.add(subTitle, BorderLayout.NORTH);
		messagePanel.add(message, BorderLayout.CENTER);
		
		messagePanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		messagePanel.setOpaque(false);
		
		panel.add(messagePanel, BorderLayout.CENTER);
		
		Icon icon = controller.getIcon();
		if(icon != null) {
			JPanel iconPanel = new JPanel(new BorderLayout());
			iconPanel.add(new JLabel(icon), BorderLayout.CENTER);
			iconPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
			iconPanel.setOpaque(false);
			
			panel.add(iconPanel, BorderLayout.EAST);
		}
		
		return panel;
	}
	
	
	private JPanel createMessage(Message severity, String message) {
		JPanel panel = new JPanel(new BorderLayout());
		JLabel icon = getMessageIcon(severity);
		JLabel messageLabel = new JLabel(message + "  ");
		panel.add(icon, BorderLayout.WEST);
		panel.add(messageLabel, BorderLayout.CENTER);
		panel.setOpaque(false);
		return panel;
	}
	
	private JLabel getMessageIcon(Message severity) {
		JLabel icon = new JLabel();
		switch(severity) {
			default:
			case INFO:  
				icon.setText("");
				break;
			case WARN:
				icon.setText(IconManager.ICON_EXCLAMATION_CIRCLE);
				icon.setForeground(Color.YELLOW.darker());
				break;
			case ERROR:
				icon.setText(IconManager.ICON_TIMES_CIRCLE);
				icon.setForeground(Color.RED.darker());
				break;
		}
		
		icon.setFont(iconManager.getIconFont(16));
		icon.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
		icon.setOpaque(false);
		return icon;
	}
	
	private JPanel createButtonPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		
		JButton cancelButton = new JButton("Cancel");
		buttonPanel.add(cancelButton);
		cancelButton.addActionListener(e -> dialog.setVisible(false));
		
		finishButton = new JButton(controller.getFinishButtonText());
		buttonPanel.add(finishButton);
		finishButton.addActionListener(e -> controller.finish());
		
		panel.add(buttonPanel, BorderLayout.CENTER);
		return panel;
	}
	
	
	public class Callback implements NiceDialogCallback {

		@Override
		public void setMessage(Message severity, String text) {
			JPanel messageContent = createMessage(severity, text);
			message.removeAll();
			message.add(messageContent, BorderLayout.CENTER);
			dialog.revalidate();
		}

		@Override
		public void setFinishButtonEnabled(boolean enabled) {
			finishButton.setEnabled(enabled);
		}
		
		@Override
		public JDialog getDialogFrame() {
			return dialog;
		}
		
	}
}
