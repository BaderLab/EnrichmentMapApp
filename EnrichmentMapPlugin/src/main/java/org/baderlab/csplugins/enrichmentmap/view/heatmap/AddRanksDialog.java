package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import static javax.swing.GroupLayout.DEFAULT_SIZE;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.nio.file.Path;
import java.util.Optional;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.parsers.RanksFileReaderTask;
import org.baderlab.csplugins.enrichmentmap.util.TaskUtil;
import org.baderlab.csplugins.enrichmentmap.view.util.ComboItem;
import org.baderlab.csplugins.enrichmentmap.view.util.FileBrowser;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

@SuppressWarnings("serial")
public class AddRanksDialog extends JDialog {

	@Inject private FileUtil fileUtil;
	@Inject private DialogTaskManager dialogTaskManager;
	
	private final EnrichmentMap map;
	
	private String resultRanksName;
	
	private JTextField ranksNameText;
	private JTextField ranksFileText;
	private JComboBox<ComboItem<EMDataSet>> dataSetCombo;
	private JButton okButton;
	private Color textFieldForeground;
	
	
	public interface Factory {
		AddRanksDialog create(EnrichmentMap map);
	}
	
	@Inject
	public AddRanksDialog(Provider<JFrame> jframeProvider, @Assisted EnrichmentMap map) {
		super(jframeProvider.get(), true);
		this.map = map;
		setMinimumSize(new Dimension(500, 160));
		setResizable(true);
		setTitle("Add Ranks");
		createContents();
		pack();
		setLocationRelativeTo(jframeProvider.get());
	}
	
	public Optional<String> open() {
		setVisible(true); // blocks until dispose() is called, must be model to work
		return Optional.ofNullable(resultRanksName);
	}
	
	private void loadRanksAndClose() {
		String rankFileName = ranksFileText.getText();
		String ranksName = getRanksName();
		EMDataSet dataset = getDataSet();
		RanksFileReaderTask task = new RanksFileReaderTask(rankFileName, dataset, ranksName, true);
		
		dialogTaskManager.execute(new TaskIterator(task), TaskUtil.allFinished(finishStatus -> {
				resultRanksName = ranksName;
				dispose();
		}));
	}
	
	private void createContents() {
		JPanel textFieldPanel = createTextFieldPanel();
		JPanel buttonPanel = createButtonPanel();
	
		Container contentPane = this.getContentPane();
		GroupLayout layout = new GroupLayout(contentPane);
		contentPane.setLayout(layout);
		
		layout.setHorizontalGroup(
			layout.createParallelGroup()
				.addComponent(textFieldPanel)
				.addComponent(buttonPanel)
		);
		layout.setVerticalGroup(
			layout.createSequentialGroup()
				.addComponent(textFieldPanel)
				.addGap(0, 0, Short.MAX_VALUE)
				.addComponent(buttonPanel)
		);
	}
	
	private JPanel createTextFieldPanel() {
		JLabel ranksNameLabel = new JLabel("Ranks Name:");
		ranksNameText = new JTextField();
		textFieldForeground = ranksNameText.getForeground();
		ranksNameText.setText("MyRanks");
		ranksNameText.addFocusListener(new FocusValidator(ranksNameText));
		
		JLabel ranksFileLabel = new JLabel("Ranks File:");
		ranksFileText = new JTextField();
		JButton ranksBrowse = new JButton("Browse...");
		ranksFileText.addFocusListener(new FocusValidator(ranksFileText));
		ranksBrowse.addActionListener(e -> browse(ranksFileText, FileBrowser.Filter.RANK));
		
		JLabel datasetLabel = new JLabel("Data Set:");
		dataSetCombo = new JComboBox<>();
		for(EMDataSet dataset : map.getDataSetList()) {
			dataSetCombo.addItem(new ComboItem<>(dataset, dataset.getName()));
		}

		SwingUtil.makeSmall(ranksNameLabel, ranksNameText, ranksFileLabel, ranksFileText, ranksBrowse, datasetLabel, dataSetCombo);
		
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);

		layout.setHorizontalGroup(
			layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.TRAILING)
					.addComponent(ranksNameLabel)
					.addComponent(ranksFileLabel)
					.addComponent(datasetLabel)
				)
				.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
					.addComponent(ranksNameText, 0, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(ranksFileText, 0, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(dataSetCombo, 0, DEFAULT_SIZE, Short.MAX_VALUE)
				)
				.addGroup(layout.createParallelGroup()
					.addComponent(ranksBrowse)
				)
		);
		
		layout.setVerticalGroup(
			layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
					.addComponent(ranksNameLabel)
					.addComponent(ranksNameText)
				)
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
					.addComponent(ranksFileLabel)
					.addComponent(ranksFileText)
					.addComponent(ranksBrowse)
				)
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
					.addComponent(datasetLabel)
					.addComponent(dataSetCombo)
				)
		);
		
   		if(LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);
		return panel;
	}
	
	
	private JPanel createButtonPanel() {
		okButton = new JButton(new AbstractAction("OK") {
			public void actionPerformed(ActionEvent e) {
				if(validateDuplicateRankName()) {
					loadRanksAndClose();
				}
			}
		});
		JButton cancelButton = new JButton(new AbstractAction("Cancel") {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

		JPanel buttonPanel = LookAndFeelUtil.createOkCancelPanel(okButton, cancelButton);
		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), okButton.getAction(), cancelButton.getAction());
		getRootPane().setDefaultButton(okButton);
		okButton.setEnabled(false);
		return buttonPanel;
	}
	
	private EMDataSet getDataSet() {
		return dataSetCombo.getItemAt(dataSetCombo.getSelectedIndex()).getValue();
	}
	
	private String getRanksName() {
		return ranksNameText.getText().trim();
	}
	
	private void browse(JTextField textField, FileBrowser.Filter filter) {
		Optional<Path> path = FileBrowser.browse(fileUtil, this, filter);
		path.map(Path::toString).ifPresent(textField::setText);
		validateInput();
	}
	
	private class FocusValidator implements FocusListener {
		private final JTextField textField;
		public FocusValidator(JTextField textField) {
			this.textField = textField;
		}
		@Override
		public void focusLost(FocusEvent e) {
			validateInput();
		}
		@Override
		public void focusGained(FocusEvent e) {
			textField.setForeground(textFieldForeground);
		}
	};
	
	private void validateInput() {
		boolean valid = true;
		valid &= !Strings.isNullOrEmpty(getRanksName());
		valid &= SwingUtil.validatePathTextField(ranksFileText, textFieldForeground, false);
		okButton.setEnabled(valid);
	}
	
	private boolean validateDuplicateRankName() {
		String ranksName = getRanksName();
		EMDataSet dataset = getDataSet();
		if(dataset.getRanks().containsKey(ranksName)) {
			JOptionPane.showMessageDialog(this, "Ranks name already exists", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
}
