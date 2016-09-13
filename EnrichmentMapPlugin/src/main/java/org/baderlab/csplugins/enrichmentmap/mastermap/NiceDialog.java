package org.baderlab.csplugins.enrichmentmap.mastermap;

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

public class NiceDialog {
	
	private JButton finishButton;
	
	private JLabel titleMessage;
	private JLabel message;
	
	private JDialog dialog;
	
	private final NiceDialogController controller;
	
	public NiceDialog(JFrame parent, NiceDialogController controller) {
		if(controller == null)
			throw new NullPointerException();
		this.controller = controller;
		
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
		titleMessage = new JLabel("<html><b>" + controller.getSubTitle() + "</b></html>");
		message = new JLabel(" ");
		
		titleMessage.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
		
		messagePanel.add(titleMessage, BorderLayout.NORTH);
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

		public void setMessage(String text) {
			message.setText("   " + text);
		}

		@Override
		public void setFinishButtonEnabled(boolean enabled) {
			finishButton.setEnabled(enabled);
		}
		
	}
}
