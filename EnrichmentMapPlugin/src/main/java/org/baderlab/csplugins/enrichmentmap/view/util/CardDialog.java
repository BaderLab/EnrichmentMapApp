package org.baderlab.csplugins.enrichmentmap.view.util;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.baderlab.csplugins.enrichmentmap.view.util.CardDialogCallback.Message;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

public class CardDialog {
	
	private JLabel subTitle;
	private JPanel message;
	
	private JDialog dialog;
	
	private JComboBox<ComboItem<CardDialogPage>> pageChooserCombo;
	private CardDialogPage currentPage;
	
	private JButton finishButton;
	
	private final CardDialogParameters params;
	private final IconManager iconManager;
	
	public CardDialog(JFrame parent, IconManager iconManager, CardDialogParameters params) {
		if (iconManager == null)
			throw new IllegalArgumentException("'iconManager' must not be null.");
		if (params == null)
			throw new IllegalArgumentException("'params' must not be null.");
		
		this.params = params;
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
	
	public boolean isVisible() {
		return dialog != null && dialog.isVisible();
	}
	
	private void createComponents() {
		dialog.setLayout(new BorderLayout());
		dialog.setMinimumSize(params.getMinimumSize());
		dialog.setTitle(params.getTitle());
		
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
	    
	    List<CardDialogPage> pages = params.getPages();
	    if(pages == null || pages.isEmpty()) {
	    	throw new IllegalArgumentException("must be at least one page");
	    }
	    
		if(pages.size() == 1) {
			CardDialogPage page = pages.get(0);
	    	
	    	JPanel body = page.createBodyPanel(callback);
		    if(body == null) {
		    	throw new NullPointerException("body panel is null");
		    }
		    
		    JPanel bodyPanel = new JPanel(new BorderLayout());
		    bodyPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		    bodyPanel.add(body, BorderLayout.CENTER);
		    
		    currentPage = page;
		    subTitle.setText("<html><b>" + currentPage.getPageTitle() + "</b></html>");
		    
		    return bodyPanel;
	    }
	    
		JPanel chooserPanel = createChooserPanel(pages);
		
	    CardLayout cardLayout = new CardLayout();
	    JPanel cards = new JPanel(cardLayout);
	    
	    for(CardDialogPage page : pages) {
	    	JPanel pagePanel = page.createBodyPanel(callback);
	    	cards.add(pagePanel, page.getID());
	    }
	    
	    pageChooserCombo.addActionListener(e -> {
	    	ComboItem<CardDialogPage> selected = pageChooserCombo.getItemAt(pageChooserCombo.getSelectedIndex());
	    	CardDialogPage page = selected.getValue();
			cardLayout.show(cards, page.getID());
	    	subTitle.setText("<html><b>" + page.getPageTitle() + "</b></html>");
	    	currentPage = page;
	    });
	    
	    currentPage = pages.get(0);
	    subTitle.setText("<html><b>" + currentPage.getPageTitle() + "</b></html>");
	    
	    JPanel body = new JPanel(new BorderLayout());
	    body.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	    body.add(chooserPanel, BorderLayout.NORTH);
	    body.add(cards, BorderLayout.CENTER);
	    return body;
	}
	
	
	private JPanel createChooserPanel(List<CardDialogPage> pages) {
		JLabel label = new JLabel(params.getPageChooserLabelText());
		pageChooserCombo = new JComboBox<>();
		
		for(CardDialogPage page : pages) {
			pageChooserCombo.addItem(new ComboItem<>(page, page.getPageComboText()));
		}

		SwingUtil.makeSmall(label, pageChooserCombo);

		JPanel panel = new JPanel();
		final GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
	   		
   		layout.setHorizontalGroup(layout.createSequentialGroup()
   			.addComponent(label)
			.addComponent(pageChooserCombo)
			.addGap(0, 0, Short.MAX_VALUE)
   		);
   		layout.setVerticalGroup(layout.createParallelGroup(Alignment.CENTER, true)
   			.addComponent(label)
			.addComponent(pageChooserCombo, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   		);
   		
   		panel.setBorder(LookAndFeelUtil.createPanelBorder());
   		if (LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);
   		
   		return panel;
	}
	
	
	private JPanel createMessagePanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(panel.getBackground().brighter());

		JPanel messagePanel = new JPanel(new BorderLayout());
		subTitle = new JLabel();
		message = new JPanel(new BorderLayout());
		message.setOpaque(false);
		
		subTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
		
		messagePanel.add(subTitle, BorderLayout.NORTH);
		messagePanel.add(message, BorderLayout.CENTER);
		
		messagePanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		messagePanel.setOpaque(false);
		
		panel.add(messagePanel, BorderLayout.CENTER);
		
		Icon icon = params.getIcon();
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
		
		finishButton = new JButton(params.getFinishButtonText());
		buttonPanel.add(finishButton);
		finishButton.addActionListener(e -> currentPage.finish());
		
		panel.add(buttonPanel, BorderLayout.CENTER);
		return panel;
	}
	
	
	public class Callback implements CardDialogCallback {

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
		
		@Override
		public void close() {
			dialog.setVisible(false);
		}
		
	}
}
