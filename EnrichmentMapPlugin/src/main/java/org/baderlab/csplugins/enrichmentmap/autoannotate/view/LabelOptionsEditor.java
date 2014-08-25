package org.baderlab.csplugins.enrichmentmap.autoannotate.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baderlab.csplugins.enrichmentmap.autoannotate.model.AnnotationSet;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.LabelOptions;

public class LabelOptionsEditor extends JDialog {
	
	private static final long serialVersionUID = -883735369366365835L;
	
	private static final Map<Double, String> labelOffsetXToString;
	static {
		HashMap<Double, String> aMap = new HashMap<Double, String>();
		aMap.put(0.0, "Left");
		aMap.put(0.5, "Center");
		aMap.put(1.0, "Right");
		labelOffsetXToString = Collections.unmodifiableMap(aMap);
	}
	
	private static final Map<Double, String> labelOffsetYToString;
	static {
		HashMap<Double, String> aMap = new HashMap<Double, String>();
		aMap.put(0.0, "Above");
		aMap.put(0.5, "Center");
		aMap.put(1.0, "Below");
		labelOffsetYToString = Collections.unmodifiableMap(aMap);
	}
	
	private int maxWords;
	private List<Integer> wordSizeThresholds;
	private int sameClusterBonus;
	private int centralityBonus;
	private boolean applied;

	private JPanel slidersPanel;

	protected ArrayList<JSlider> thresholdSliders;

	private JPanel innerPanel;

	private JComboBox<Integer> maximumLabelLengthDropdown;

	private JSlider sameClusterBonusSlider;
	private JSlider centralityBonusSlider;

	private double labelPositionX;
	private double labelPositionY;

	private JComboBox<String> verticalPositionDropdown;

	private JComboBox<String> justificationDropdown;
	
	public LabelOptionsEditor(AnnotationSet selectedAnnotationSet) {
		super();
		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		applied = false;
		if (selectedAnnotationSet != null) {
			maxWords = selectedAnnotationSet.getMaxWords();
			wordSizeThresholds = selectedAnnotationSet.getWordSizeThresholds();
			thresholdSliders = new ArrayList<JSlider>();
			double[] labelPosition = selectedAnnotationSet.getLabelPosition();
			labelPositionX = labelPosition[0];
			labelPositionY = labelPosition[1];
			sameClusterBonus = selectedAnnotationSet.getSameClusterBonus();
			centralityBonus = selectedAnnotationSet.getCentralityBonus();
			setTitle("Text Label Options");
			
			innerPanel = new JPanel();
			innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.PAGE_AXIS));			
			innerPanel.add(createThresholdPanel());
			innerPanel.add(createBonusSliderPanel());
			innerPanel.add(createLabelPositionDropdownsPanel());
			innerPanel.add(createButtonPanel());
			add(innerPanel);
			pack();
		} else {
			JOptionPane.showMessageDialog(null, "Please create an annotation set", "Error Message", 
					JOptionPane.ERROR_MESSAGE);
			dispose();
		}
	}
	
	private JPanel createLabelPositionDropdownsPanel() {
		JPanel labelPositionDropdownsPanel = new JPanel();
		labelPositionDropdownsPanel.setBorder(BorderFactory.createTitledBorder("Label position options"));
		
		String[] justifications = {"Left", "Center", "Right"};
		DefaultComboBoxModel<String> justificationModel = new DefaultComboBoxModel<String>(justifications);
		justificationDropdown = new JComboBox<String>(justificationModel);
		justificationDropdown.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				String selectedJustification = (String) ((JComboBox<String>) e.getSource()).getSelectedItem();
				if (selectedJustification.equals("Left")) {
					labelPositionX = 0.0;
				} else if (selectedJustification.equals("Center")) {
					labelPositionX = 0.5;
				} else {
					labelPositionX = 1.0;
				}
			}
		});
		justificationDropdown.setSelectedItem(labelOffsetXToString.get(labelPositionX));
		labelPositionDropdownsPanel.add(new JLabel("Horizontal Position: "));
		labelPositionDropdownsPanel.add(justificationDropdown);
		
		String[] verticalPositions = {"Above", "Center", "Below"};
		DefaultComboBoxModel<String> verticalPositionModel = new DefaultComboBoxModel<String>(verticalPositions);
		verticalPositionDropdown = new JComboBox<String>(verticalPositionModel);
		verticalPositionDropdown.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				String selectedPosition = (String) ((JComboBox<String>) e.getSource()).getSelectedItem();
				if (selectedPosition.equals("Above")) {
					labelPositionY = 0.0;
				} else if (selectedPosition.equals("Center")) {
					labelPositionY = 0.5;
				} else {
					labelPositionY = 1.0;
				}
			}
		});
		verticalPositionDropdown.setSelectedItem(labelOffsetYToString.get(labelPositionY));
		labelPositionDropdownsPanel.add(new JLabel("Vertical Position: "));
		labelPositionDropdownsPanel.add(verticalPositionDropdown);
		
		return labelPositionDropdownsPanel;
	}

	public JPanel createThresholdPanel() {
		final JPanel thresholdPanel = new JPanel(new BorderLayout());
		thresholdPanel.setBorder(BorderFactory.createTitledBorder("Label word threshold options"));
		
		JLabel dropdownLabel = new JLabel("Maximum label length (words)");
		
		Integer[] labelLengths = {1, 2, 3, 4, 5, 6, 7};
		DefaultComboBoxModel<Integer> labelLengthModel = new DefaultComboBoxModel<Integer>(labelLengths);
		maximumLabelLengthDropdown = new JComboBox<Integer>(labelLengthModel);
		
		JPanel dropdownPanel = new JPanel();
		dropdownPanel.add(dropdownLabel);
		dropdownPanel.add(maximumLabelLengthDropdown);
		thresholdPanel.add(dropdownPanel, BorderLayout.NORTH);
		
		slidersPanel = new JPanel();
		slidersPanel.setLayout(new BoxLayout(slidersPanel, BoxLayout.PAGE_AXIS));
		thresholdPanel.add(slidersPanel);
		
		maximumLabelLengthDropdown.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				thresholdPanel.remove(slidersPanel);
				thresholdSliders = new ArrayList<JSlider>();
				slidersPanel = new JPanel();
				slidersPanel.setLayout(new BoxLayout(slidersPanel, BoxLayout.PAGE_AXIS));
				maxWords = (Integer) maximumLabelLengthDropdown.getSelectedItem();
				for (int sliderNumber = 1; sliderNumber <  maxWords; sliderNumber++) {
					if (sliderNumber == 1) {
						JLabel slidersLabel = new JLabel("Word Inclusion Threshold(s)");
						slidersPanel.add(slidersLabel);
					}
					int sliderValue;
					if (wordSizeThresholds.size() > sliderNumber - 1) {
						sliderValue = wordSizeThresholds.get(sliderNumber - 1);
					} else {
						sliderValue = AnnotationSet.DEFAULT_WORDSIZE_THRESHOLDS.get(sliderNumber - 1);
					}
					final JLabel thresholdLabel = new JLabel("Word " + sliderNumber + 
							" to Word " + String.valueOf(sliderNumber+1) + ": " + 
							String.valueOf(sliderValue) + "%");
					JSlider thresholdSlider = new JSlider(0, 100, sliderValue);
					thresholdSlider.setLabelTable(thresholdSlider.createStandardLabels(10));
					thresholdSlider.setPaintLabels(true);
					thresholdSlider.addChangeListener(new ChangeListener() {
						@Override
						public void stateChanged(ChangeEvent e) {
							JSlider thisSlider = (JSlider) e.getSource();
							wordSizeThresholds = new ArrayList<Integer>();
							for (JSlider slider : thresholdSliders) {
								wordSizeThresholds.add(slider.getValue());
							}
							String thresholdText = thresholdLabel.getText();
							String newThresholdText = thresholdText.substring(0, thresholdText.indexOf(":")) 
									+ ": " + thisSlider.getValue() + "%";
							thresholdLabel.setText(newThresholdText);
							revalidate();
						}
					});
					thresholdSliders.add(thresholdSlider);
					JPanel sliderPanel = new JPanel();
					sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.PAGE_AXIS));
					sliderPanel.add(thresholdLabel);
					sliderPanel.add(thresholdSlider);
					slidersPanel.add(sliderPanel);
				}
				thresholdPanel.add(slidersPanel, BorderLayout.CENTER);
				thresholdPanel.updateUI();
				pack();
			}
		});
		maximumLabelLengthDropdown.setSelectedItem(maxWords);
		
		return thresholdPanel;
	}
	
	public JPanel createBonusSliderPanel() {
		JPanel bonusSliderPanel = new JPanel();
		bonusSliderPanel.setLayout(new BoxLayout(bonusSliderPanel, BoxLayout.PAGE_AXIS));
		bonusSliderPanel.setBorder(BorderFactory.createTitledBorder("Word Size Bonuses"));
		// Bonus for words in the same WordCloud cluster 
		final JLabel sameClusterBonusLabel = new JLabel("Bonus for words in same WordCloud cluster: " +
				sameClusterBonus);
		sameClusterBonusSlider = new JSlider(0, 15, sameClusterBonus);
		sameClusterBonusSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider thisSlider = (JSlider) e.getSource();
				sameClusterBonus = thisSlider.getValue();
				// Update label to show value
				String labelText = sameClusterBonusLabel.getText();
				String newThresholdText = labelText.substring(0, labelText.indexOf(":")) 
						+ ": " + thisSlider.getValue();
				sameClusterBonusLabel.setText(newThresholdText);
				revalidate();
			}
		});
		bonusSliderPanel.add(sameClusterBonusLabel);
		bonusSliderPanel.add(sameClusterBonusSlider);
		// Bonus for words coming from the most central nodes in the cluster
		final JLabel centralityBonusLabel = new JLabel("Bonus for words from most central nodes: " + 
				centralityBonus);
		centralityBonusSlider = new JSlider(0, 15, centralityBonus);
		centralityBonusSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider thisSlider = (JSlider) e.getSource();
				centralityBonus = thisSlider.getValue();
				// Update label to show value
				String labelText = centralityBonusLabel.getText();
				String newThresholdText = labelText.substring(0, labelText.indexOf(":")) 
						+ ": " + thisSlider.getValue();
				centralityBonusLabel.setText(newThresholdText);
				revalidate();
			}
		});
		bonusSliderPanel.add(centralityBonusLabel);
		bonusSliderPanel.add(centralityBonusSlider);

		return bonusSliderPanel;
	}
	
	public JPanel createButtonPanel() {
		JPanel buttonPanel = new JPanel();
		// Closes the dialog window
		JButton defaultButton = new JButton("Restore Defaults");
		defaultButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				double[] labelPosition = AnnotationSet.DEFAULT_LABEL_POSITION;
				labelPositionX = labelPosition[0];
				labelPositionY = labelPosition[1];
				justificationDropdown.setSelectedItem(labelOffsetXToString.get(labelPositionX));
				verticalPositionDropdown.setSelectedItem(labelOffsetYToString.get(labelPositionY));
				// Update the view
				maximumLabelLengthDropdown.setSelectedItem(AnnotationSet.DEFAULT_MAX_WORDS);
				wordSizeThresholds = AnnotationSet.DEFAULT_WORDSIZE_THRESHOLDS;
				for (int sliderIndex = 0; sliderIndex < thresholdSliders.size(); sliderIndex++) {
					thresholdSliders.get(sliderIndex).setValue(wordSizeThresholds.get(sliderIndex));
				}
				sameClusterBonusSlider.setValue(AnnotationSet.DEFAULT_SAME_CLUSTER_BONUS);
				centralityBonusSlider.setValue(AnnotationSet.DEFAULT_CENTRALITY_BONUS);
			}
		});
		// Closes the dialog window
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		
		// Closes the dialog window and passes back the data
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				applied = true;
				dispose();
			}
		});
		buttonPanel.add(defaultButton);
		buttonPanel.add(cancelButton);
		buttonPanel.add(okButton);
		return buttonPanel;
	}
	
	public LabelOptions showDialog() {
		setLocationRelativeTo(null);
		this.setVisible(true);
		if (applied) {
			double[] labelPosition = {labelPositionX, labelPositionY};
			LabelOptions labelOptions = new LabelOptions(maxWords, wordSizeThresholds,
					labelPosition, sameClusterBonus, centralityBonus);
			return labelOptions;
		} else {
			return null;
		}
	}
	
}
