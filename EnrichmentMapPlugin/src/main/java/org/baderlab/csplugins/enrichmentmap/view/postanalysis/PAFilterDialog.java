package org.baderlab.csplugins.enrichmentmap.view.postanalysis;

import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Map;
import java.util.Optional;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.PostAnalysisFilterType;
import org.baderlab.csplugins.enrichmentmap.task.postanalysis.FilterMetric;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class PAFilterDialog extends JDialog {

	private IconManager iconManager;
	
	private JComboBox<PostAnalysisFilterType> filterTypeCombo;
	private JTextField filterTextField;
	private int hypergomUniverseSize;
	
	private JLabel iconLabel;
	private JLabel warnLabel;
	
	private boolean cancelled = false;
	private JButton applyButton;
	
	private final Map<PostAnalysisFilterType,String> savedFilterValues = PostAnalysisFilterType.createMapOfDefaults();
	
	
	public PAFilterDialog(JFrame parent, IconManager iconManager, EnrichmentMap map, FilterMetric metric) {
		super(parent, true);
		this.hypergomUniverseSize = map.getNumberOfGenes();
		this.iconManager = iconManager;
		setMinimumSize(new Dimension(300, 120));
		setResizable(true);
		setTitle("Filter Signature Gene Sets");
		createContents(metric);
		pack();
		setLocationRelativeTo(parent);
	}
	
	public Optional<FilterMetric> open() {
		setVisible(true); // blocks until dispose() is called, must be model to work
		return cancelled ? Optional.empty() : createFilterMetric();
	}
	
	private void createContents(FilterMetric metric) {
		JPanel bodyPanel = createBodyPanel(metric);
		JPanel buttonPanel = createButtonPanel();
		
		setLayout(new BorderLayout());
		add(bodyPanel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
	}
	
	private void showWarning(String message) {
		applyButton.setEnabled(message == null);
		warnLabel.setText(message == null ? "" : message);
		iconLabel.setVisible(message != null);
		warnLabel.setVisible(message != null);
	}
	
	private JPanel createBodyPanel(FilterMetric metric) {
		JLabel filterLabel = new JLabel("Filter:");
		JLabel cutoffLabel = new JLabel("Cutoff:");
		
		filterTextField = new JFormattedTextField();
		filterTextField.setColumns(4);
		filterTextField.setHorizontalAlignment(JTextField.RIGHT);
		
		filterTypeCombo = new JComboBox<>();
		filterTypeCombo.addItem(PostAnalysisFilterType.NO_FILTER);
//		filterTypeCombo.addItem(PostAnalysisFilterType.MANN_WHIT_TWO_SIDED);
//		filterTypeCombo.addItem(PostAnalysisFilterType.MANN_WHIT_GREATER);
//		filterTypeCombo.addItem(PostAnalysisFilterType.MANN_WHIT_LESS);
		filterTypeCombo.addItem(PostAnalysisFilterType.HYPERGEOM);
		filterTypeCombo.addItem(PostAnalysisFilterType.NUMBER);
		filterTypeCombo.addItem(PostAnalysisFilterType.PERCENT);
		filterTypeCombo.addItem(PostAnalysisFilterType.SPECIFIC);

		if(metric != null) {
			filterTextField.setText(String.valueOf(metric.getCutoff()));
			filterTypeCombo.setSelectedItem(metric.getFilterType());
		}
		
		iconLabel = new JLabel(IconManager.ICON_WARNING);
		iconLabel.setFont(iconManager.getIconFont(16.0f));
		iconLabel.setForeground(LookAndFeelUtil.getWarnColor());
		warnLabel = new JLabel("warn");
		iconLabel.setVisible(false);
		warnLabel.setVisible(false);
		
		filterTextField.getDocument().addDocumentListener(SwingUtil.simpleDocumentListener(() -> {
			String text = filterTextField.getText();
			try {
				double val = Double.parseDouble(text);
				PostAnalysisFilterType filterType = getFilterType();
				savedFilterValues.put(filterType, text);
				showWarning(filterType.isValid(val) ? null : filterType.getErrorMessage());
			} catch(NumberFormatException e) {
				showWarning("Not a number");
			}
		}));
		
		filterTypeCombo.addActionListener(e -> {
			PostAnalysisFilterType filterType = (PostAnalysisFilterType) filterTypeCombo.getSelectedItem();
			filterTextField.setText(savedFilterValues.get(filterType));
			filterTextField.setEnabled(filterType != PostAnalysisFilterType.NO_FILTER);
		});
		
		SwingUtil.makeSmall(filterLabel, cutoffLabel, filterTextField, filterTypeCombo, iconLabel, warnLabel);
		
		JPanel panel = new JPanel();
		final GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup()
				.addComponent(filterLabel)
				.addComponent(cutoffLabel)
			)
			.addGroup(layout.createParallelGroup()
				.addComponent(filterTypeCombo)
				.addGroup(layout.createSequentialGroup()
					.addComponent(filterTextField, PREFERRED_SIZE, 60, PREFERRED_SIZE)
					.addComponent(iconLabel)
					.addComponent(warnLabel)
				)
			)
		);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(filterLabel)
				.addComponent(filterTypeCombo)
			)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(cutoffLabel)
				.addComponent(filterTextField)
				.addComponent(iconLabel)
				.addComponent(warnLabel)
			)
		);
		
		if(LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);
		return panel;
	}
	
	
	private JPanel createButtonPanel() {
		JButton cancelButton = new JButton(new AbstractAction("Cancel") {
			@Override
			public void actionPerformed(ActionEvent e) {
				cancelled = true;
				dispose();
			}
		});
		applyButton = new JButton(new AbstractAction("Apply") {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		
		return LookAndFeelUtil.createOkCancelPanel(applyButton, cancelButton);
	}
	
	
	public Optional<FilterMetric> createFilterMetric() {
		String text = filterTextField.getText();
		double value = Double.parseDouble(text);
		PostAnalysisFilterType type = getFilterType();
		
		switch(type) {
			case NO_FILTER:
				return Optional.of(new FilterMetric.NoFilter());
			case NUMBER:
				return Optional.of(new FilterMetric.Number(value));
			case PERCENT:
				return Optional.of(new FilterMetric.Percent(value));
			case SPECIFIC:
				return Optional.of(new FilterMetric.Specific(value));
			case HYPERGEOM:
				return Optional.of(new FilterMetric.Hypergeom(value, hypergomUniverseSize));
//			case MANN_WHIT_TWO_SIDED:
//			case MANN_WHIT_GREATER:
//			case MANN_WHIT_LESS:
//				return new FilterMetric.MannWhit(value, mannWhitRanks, type);
			default:
				return Optional.empty();
		}
	}
	
	private PostAnalysisFilterType getFilterType() {
		return (PostAnalysisFilterType) filterTypeCombo.getSelectedItem();
	}
	
	public Optional<String> getErrorMessage() {
		return Optional.ofNullable(getFilterType().getErrorMessage());
	}
	
}
