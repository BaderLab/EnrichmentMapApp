package org.baderlab.csplugins.enrichmentmap.view.util;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;

import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;

public class CardDialog {

	private JDialog dialog;

	private JComboBox<ComboItem<CardDialogPage>> pageChooserCombo;
	private CardDialogPage currentPage;

	private JButton finishButton;

	private final CardDialogParameters params;

	public CardDialog(JFrame parent, CardDialogParameters params) {
		if (params == null)
			throw new IllegalArgumentException("'params' must not be null.");
		this.params = params;
		dialog = new JDialog(parent);
		createComponents();
	}
	
	public CardDialog(JDialog parent, CardDialogParameters params) {
		if (params == null)
			throw new IllegalArgumentException("'params' must not be null.");
		this.params = params;
		dialog = new JDialog(parent);
		createComponents();
	}

	public void open() {
		dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		dialog.pack();
		dialog.setLocationRelativeTo(dialog.getParent());
		dialog.setVisible(true);
		currentPage.opened();
	}

	public void dispose() {
		dialog.setVisible(false);
		dialog.dispose();
	}
	

	public boolean isVisible() {
		return dialog != null && dialog.isVisible();
	}

	private void createComponents() {
		dialog.setLayout(new BorderLayout());
		dialog.setPreferredSize(params.getPreferredSize());
		Dimension minimumSize = params.getMinimumSize();
		if (minimumSize != null) {
			dialog.setMinimumSize(minimumSize);
		}
		dialog.setTitle(params.getTitle());

		// Create message and button panel first because the controller can call
		// callbacks from inside createBodyPanel()
		JPanel buttonPanel = createButtonPanel();
		JPanel bodyPanel = createBodyPanel();

		Border padding = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		Border separator = BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Separator.foreground"));
		Border border = BorderFactory.createCompoundBorder(separator, padding);
		buttonPanel.setBorder(border);

		dialog.add(bodyPanel, BorderLayout.CENTER);
		dialog.add(buttonPanel, BorderLayout.SOUTH);
	}

	public CardDialogCallback getCallback() {
		return new Callback();
	}
	
	private JPanel createBodyPanel() {
		Callback callback = new Callback();

		List<CardDialogPage> pages = params.getPages();
		if (pages == null || pages.isEmpty()) {
			throw new IllegalArgumentException("must be at least one page");
		}

		if (pages.size() == 1) {
			CardDialogPage page = pages.get(0);

			JPanel body = page.createBodyPanel(callback);
			if (body == null) {
				throw new NullPointerException("body panel is null");
			}

			JPanel bodyPanel = new JPanel(new BorderLayout());
			bodyPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			bodyPanel.add(body, BorderLayout.CENTER);

			currentPage = page;
			return bodyPanel;
		}

		JPanel chooserPanel = createChooserPanel(pages);

		CardLayout cardLayout = new CardLayout();
		JPanel cards = new JPanel(cardLayout);

		for (CardDialogPage page : pages) {
			JPanel pagePanel = page.createBodyPanel(callback);
			cards.add(pagePanel, page.getID());
		}

		pageChooserCombo.addActionListener(e -> {
			ComboItem<CardDialogPage> selected = pageChooserCombo.getItemAt(pageChooserCombo.getSelectedIndex());
			CardDialogPage page = selected.getValue();
			cardLayout.show(cards, page.getID());
			currentPage = page;
			currentPage.opened();
		});

		currentPage = pages.get(0);

		JPanel body = new JPanel(new BorderLayout());
		body.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		body.add(chooserPanel, BorderLayout.NORTH);
		body.add(cards, BorderLayout.CENTER);
		return body;
	}

	private JPanel createChooserPanel(List<CardDialogPage> pages) {
		JLabel label = new JLabel(params.getPageChooserLabelText());
		pageChooserCombo = new JComboBox<>();

		for (CardDialogPage page : pages) {
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
				.addGap(0, 0, Short.MAX_VALUE));
		
		layout.setVerticalGroup(layout.createParallelGroup(Alignment.CENTER, true)
				.addComponent(label)
				.addComponent(pageChooserCombo, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE));

		panel.setBorder(LookAndFeelUtil.createPanelBorder());
		if (LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);

		return panel;
	}

	@SuppressWarnings("serial")
	private JPanel createButtonPanel() {
		finishButton = new JButton(new AbstractAction(params.getFinishButtonText()) {
			public void actionPerformed(ActionEvent e) {
				finishButton.setEnabled(false);
				currentPage.finish();
			}
		});
		JButton cancelButton = new JButton(new AbstractAction("Cancel") {
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
			}
		});

		AbstractButton[] additional = params.getAdditionalButtons();
		if (additional != null) {
			for (AbstractButton button : additional) {
				button.addActionListener(e -> {
					currentPage.extraButtonClicked(e.getActionCommand());
				});
			}
		}

		JPanel buttonPanel = LookAndFeelUtil.createOkCancelPanel(finishButton, cancelButton, additional);
		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(dialog.getRootPane(), finishButton.getAction(), cancelButton.getAction());
		dialog.getRootPane().setDefaultButton(finishButton);
		return buttonPanel;
	}

	
	public class Callback implements CardDialogCallback {

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
		
		@Override
		public Task getCloseTask() {
			return new AbstractTask() {
				@Override
				public void run(TaskMonitor taskMonitor) throws Exception {
					close();
				}
			};
		}
		
		@Override
		public CardDialog getDialog() {
			return CardDialog.this;
		}

	}
}
