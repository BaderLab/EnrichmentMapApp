package org.baderlab.csplugins.enrichmentmap.autoannotate.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationManager;
import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationUtils;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.AnnotationSet;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.Cluster;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.util.swing.BasicCollapsiblePanel;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;

public class DisplayOptionsPanel extends JPanel implements CytoPanelComponent {

	private static final long serialVersionUID = -7883203344022939503L;
	private AnnotationSet selectedAnnotationSet;
	private JTextField fontSizeTextField;
	private JCheckBox showEllipsesCheckBox;
	private AutoAnnotationManager autoAnnotationManager;
	private JRadioButton heatmapButton;
	private JSlider ellipseWidthSlider;
	private JSlider ellipseOpacitySlider;
	private JRadioButton ellipseButton;
	private String shapeType;
	private ArrayList<JSlider> wordSizeThresholdSliders;
	protected JPanel slidersPanel;
	private JCheckBox showTextCheckBox;

	private static String proportionalSizeButtonString = "Font size by # of nodes";
	private static String constantSizeButtonString = "Constant font size";
	
	public DisplayOptionsPanel() {
		this.autoAnnotationManager = AutoAnnotationManager.getInstance();
		shapeType = "ELLIPSE";
		
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setPreferredSize(new Dimension(300, 300));
		
		add(createEllipseWidthSliderPanel());
		add(createEllipseOpacitySliderPanel());
		add(createShowEllipsesCheckBoxPanel());
		add(createShapeTypePanel());
		add(createSelectionPanel());
		add(createFontSizePanel());
		//add(createLabelOptionsPanel());
	}
	
	private JPanel createEllipseWidthSliderPanel() {
		// Slider to set width of the ellipses
		JLabel sliderLabel = new JLabel("Shape Border Width");
		ellipseWidthSlider = new JSlider(1, 10, 3);
		ellipseWidthSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
		        int ellipseWidth = ellipseWidthSlider.getValue();
		        for (Cluster cluster : selectedAnnotationSet.getClusterMap().values()) {
		        	if (cluster.isSelected()) {
		        		cluster.getEllipse().setBorderWidth(ellipseWidth*2);
		        	} else {
		        		cluster.getEllipse().setBorderWidth(ellipseWidth);
		        	}
		        	cluster.getEllipse().update();
		        }
			}
		});
		JPanel ellipseWidthSliderPanel = new JPanel();
		ellipseWidthSliderPanel.setLayout(new BoxLayout(ellipseWidthSliderPanel, BoxLayout.PAGE_AXIS));
		ellipseWidthSliderPanel.add(sliderLabel);
		ellipseWidthSliderPanel.add(ellipseWidthSlider);
		return ellipseWidthSliderPanel;
	}
	
	public int getEllipseWidth() {
		return ellipseWidthSlider.getValue();
	}
	
	private JPanel createEllipseOpacitySliderPanel() {
		// Slider to set width of the ellipses
		JLabel sliderLabel = new JLabel("Shape Opacity");
		ellipseOpacitySlider = new JSlider(1, 100, 20);
		ellipseOpacitySlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
		        int ellipseOpacity = ellipseOpacitySlider.getValue();
		        for (Cluster cluster : selectedAnnotationSet.getClusterMap().values()) {
		        	cluster.getEllipse().setFillOpacity(ellipseOpacity);
		        	cluster.getEllipse().update();
		        }
			}
		});
		JPanel ellipseOpacitySliderPanel = new JPanel();
		ellipseOpacitySliderPanel.setLayout(new BoxLayout(ellipseOpacitySliderPanel, BoxLayout.PAGE_AXIS));
		ellipseOpacitySliderPanel.add(sliderLabel);
		ellipseOpacitySliderPanel.add(ellipseOpacitySlider);
		return ellipseOpacitySliderPanel;
	}
	
	public int getEllipseOpacity() {
		return ellipseOpacitySlider.getValue();
	}
	
	private JPanel createFontSizePanel() {
		// Font size options
		JRadioButton proportionalSizeButton = new JRadioButton(proportionalSizeButtonString);
		proportionalSizeButton.setSelected(true);
		JRadioButton constantSizeButton = new JRadioButton(constantSizeButtonString);
		fontSizeTextField = new JTextField();
		fontSizeTextField.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		    	try {
		    		int fontSize = Integer.parseInt(fontSizeTextField.getText());
		    		if (fontSize <= 0) {
		    			throw new Exception();
		    		}
		    		AutoAnnotationUtils.updateFontSizes(fontSize, fontSizeTextField.isEnabled(), 
							showTextCheckBox.isSelected());
		    	} catch (Exception ex) {
		            JOptionPane.showMessageDialog(null,
		                    "Error: Please enter an integer bigger than 0", "Error Message",
		                    JOptionPane.ERROR_MESSAGE);
		    	}
		    }
		});
		fontSizeTextField.setText("12");
		// Initially disabled
		fontSizeTextField.setEnabled(false);
		constantSizeButton.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				int fontSize = 1;
				if (e.getStateChange() == ItemEvent.SELECTED) {
					fontSizeTextField.setEnabled(true);
			    	fontSize = Integer.parseInt(fontSizeTextField.getText());
				} else if (e.getStateChange() == ItemEvent.DESELECTED) {
					fontSizeTextField.setEnabled(false);
				}
				AutoAnnotationUtils.updateFontSizes(fontSize, fontSizeTextField.isEnabled(), 
						showTextCheckBox.isSelected());
				updateUI();
			}
		});

		
		// Group buttons together to make them mutually exclusive
		ButtonGroup fontButtonGroup = new ButtonGroup();
		fontButtonGroup.add(proportionalSizeButton);
		fontButtonGroup.add(constantSizeButton);

		JPanel fontSizePanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridwidth = 5;
		c.gridy = 0;
		fontSizePanel.add(proportionalSizeButton,c);
		c.gridx = 0;
		c.gridwidth = 4;
		c.gridy = 1;
		fontSizePanel.add(constantSizeButton,c);
		c.gridx = 10;
		c.gridwidth = 1;
		c.gridy = 1;
		fontSizePanel.add(fontSizeTextField,c);
		return fontSizePanel;
	}
	
	
	private JPanel createShowEllipsesCheckBoxPanel() {
		// By default show ellipses around clusters
		showEllipsesCheckBox = new JCheckBox("Draw shapes around clusters");
		showEllipsesCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean showEllipses = showEllipsesCheckBox.isSelected();
				if (showEllipses) {
					AnnotationFactory<ShapeAnnotation> shapeFactory = autoAnnotationManager.getShapeFactory();
					AnnotationManager annotationManager = autoAnnotationManager.getAnnotationManager();
					selectedAnnotationSet.updateCoordinates();
					for (Cluster cluster : selectedAnnotationSet.getClusterMap().values()) {
						AutoAnnotationUtils.drawEllipse(cluster, selectedAnnotationSet.getView(),
								shapeFactory, annotationManager, showEllipses, ellipseWidthSlider.getValue(), 
								ellipseOpacitySlider.getValue(), shapeType);
					}
				} else {
					for (Cluster cluster : selectedAnnotationSet.getClusterMap().values()) {
						cluster.eraseEllipse();
					}
				}
			}
		});
		showEllipsesCheckBox.setSelected(true);
		// By default show ellipses around clusters
		showTextCheckBox = new JCheckBox("Show text labels above clusters");
		showTextCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean showLabel = showTextCheckBox.isSelected();
				boolean constantFontSize = fontSizeTextField.isEnabled();
				int fontSize = Integer.valueOf(fontSizeTextField.getText());
				if (showLabel) {
					AnnotationFactory<TextAnnotation> textFactory = autoAnnotationManager.getTextFactory();
					AnnotationManager annotationManager = autoAnnotationManager.getAnnotationManager();
					selectedAnnotationSet.updateCoordinates();
					for (Cluster cluster : selectedAnnotationSet.getClusterMap().values()) {
						AutoAnnotationUtils.drawTextLabel(cluster, selectedAnnotationSet.getView(),
								textFactory, annotationManager, constantFontSize, fontSize, showLabel);
					}
				} else {
					for (Cluster cluster : selectedAnnotationSet.getClusterMap().values()) {
						cluster.eraseText();
					}
				}
			}
		});
		showTextCheckBox.setSelected(true);
		JPanel checkBoxPanel = new JPanel(new GridLayout(2, 1));
		checkBoxPanel.add(showEllipsesCheckBox);
		checkBoxPanel.add(showTextCheckBox);
		return checkBoxPanel;
	}	
	
	public boolean isShowEllipsesCheckBoxSelected() {
		return showEllipsesCheckBox.isSelected();
	}
	
	public boolean isShowTextCheckBoxSelected() {
		return showTextCheckBox.isSelected();
	}
	
	private JPanel createShapeTypePanel() {
		JPanel shapeTypePanel = new JPanel();

		JLabel label = new JLabel("Shape type:");

		ellipseButton = new JRadioButton("Ellipse");
		JRadioButton rectangleButton = new JRadioButton("Rectangle");
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(ellipseButton);
		buttonGroup.add(rectangleButton);

		ellipseButton.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				AnnotationFactory<ShapeAnnotation> shapeFactory = autoAnnotationManager.getShapeFactory();
				AnnotationManager annotationManager = autoAnnotationManager.getAnnotationManager();
				shapeType = ellipseButton.isSelected() ? "ELLIPSE" : "ROUNDEDRECTANGLE";
				if (selectedAnnotationSet != null) {
					for (Cluster cluster : selectedAnnotationSet.getClusterMap().values()) {
						cluster.eraseEllipse();
						AutoAnnotationUtils.drawEllipse(cluster, selectedAnnotationSet.getView(),
								shapeFactory, annotationManager, showEllipsesCheckBox.isSelected(), 
								ellipseWidthSlider.getValue(), ellipseOpacitySlider.getValue(), 
								shapeType);
					}
				}
			}
		});
		
		ellipseButton.setSelected(true);
		
		JPanel radioButtonPanel = new JPanel();
		radioButtonPanel.setLayout(new BoxLayout(radioButtonPanel, BoxLayout.PAGE_AXIS));
		radioButtonPanel.add(ellipseButton);
		radioButtonPanel.add(rectangleButton);

		shapeTypePanel.add(label);
		shapeTypePanel.add(radioButtonPanel);
		
		return shapeTypePanel;
	}
	
	public String getShapeType() {
		return shapeType;
	}
	
	private JPanel createSelectionPanel() {
		JPanel selectionPanel = new JPanel();

		JLabel label = new JLabel("Show on selection:");

		heatmapButton = new JRadioButton("Heat Map");
		JRadioButton wordCloudButton = new JRadioButton("WordCloud");
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(wordCloudButton);
		buttonGroup.add(heatmapButton);

		heatmapButton.setSelected(true);
		wordCloudButton.setSelected(true);

		JPanel radioButtonPanel = new JPanel();
		radioButtonPanel.setLayout(new BoxLayout(radioButtonPanel, BoxLayout.PAGE_AXIS));
		radioButtonPanel.add(wordCloudButton);
		radioButtonPanel.add(heatmapButton);

		selectionPanel.add(label);
		selectionPanel.add(radioButtonPanel);
		
		return selectionPanel;
	}
	
	public boolean isHeatmapButtonSelected() {
		return heatmapButton.isSelected();
	}
	
	private BasicCollapsiblePanel createLabelOptionsPanel() {
		BasicCollapsiblePanel labelOptionsPanel = new BasicCollapsiblePanel("Label Options");
		
		final JPanel innerPanel = new JPanel(new BorderLayout());
		
		JLabel dropdownLabel = new JLabel("Maximum label length (words)");
		
		Integer[] labelLengths = {1, 2, 3, 4, 5, 6, 7};
		DefaultComboBoxModel<Integer> labelLengthModel = new DefaultComboBoxModel<Integer>(labelLengths);
		final JComboBox<Integer> maximumLabelLengthDropdown = new JComboBox<Integer>(labelLengthModel);
		
		JPanel dropdownPanel = new JPanel();
		dropdownPanel.add(dropdownLabel);
		dropdownPanel.add(maximumLabelLengthDropdown);
		innerPanel.add(dropdownPanel, BorderLayout.NORTH);
		
		slidersPanel = new JPanel();
		slidersPanel.setLayout(new BoxLayout(slidersPanel, BoxLayout.PAGE_AXIS));
		innerPanel.add(slidersPanel);
		
		wordSizeThresholdSliders = new ArrayList<JSlider>();
		maximumLabelLengthDropdown.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				innerPanel.remove(slidersPanel);
				slidersPanel = new JPanel();
				slidersPanel.setLayout(new BoxLayout(slidersPanel, BoxLayout.PAGE_AXIS));
				int[] defaultSliderValues = {30, 80, 90, 90, 90, 95};
				wordSizeThresholdSliders = new ArrayList<JSlider>();
				int numWordSizeThresholds = ((Integer) maximumLabelLengthDropdown.getSelectedItem());
				for (int sliderNumber = 1; sliderNumber <  numWordSizeThresholds; sliderNumber++) {
					if (sliderNumber == 1) {
						JLabel slidersLabel = new JLabel("Word Inclusion Threshold(s)");
						slidersPanel.add(slidersLabel);
					}
					JLabel thresholdLabel = new JLabel("Word " + sliderNumber + 
							" to Word " + String.valueOf(sliderNumber+1)); 
					JSlider thresholdSlider = new JSlider(1, 100, defaultSliderValues[sliderNumber - 1]);
					JPanel sliderPanel = new JPanel();
					sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.PAGE_AXIS));
					sliderPanel.add(thresholdLabel);
					sliderPanel.add(thresholdSlider);
					wordSizeThresholdSliders.add(thresholdSlider);
					slidersPanel.add(sliderPanel);
				}
				innerPanel.add(slidersPanel, BorderLayout.CENTER);
				innerPanel.updateUI();
			}
		});
		maximumLabelLengthDropdown.setSelectedItem(4);
		
		labelOptionsPanel.add(innerPanel);
		
		return labelOptionsPanel;
	}

	public ArrayList<Integer> getWordSizeThresholds() {
		ArrayList<Integer> wordSizeThresholds = new ArrayList<Integer>();
		for (JSlider slider : wordSizeThresholdSliders) {
			wordSizeThresholds.add(slider.getValue());
		}
		return wordSizeThresholds;
	}
	
	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.EAST;
	}

	@Override
	public Icon getIcon() {
		//create an icon for the enrichment map panels
		URL EMIconURL = this.getClass().getResource("enrichmentmap_logo_notext_small.png");
		ImageIcon EMIcon = null;
		if (EMIconURL != null) {
			EMIcon = new ImageIcon(EMIconURL);
		}
		return EMIcon;
	}

	@Override
	public String getTitle() {
		return "Annotation Display Options Panel";
	}

	public void setSelectedAnnotationSet(AnnotationSet selectedAnnotationSet) {
		this.selectedAnnotationSet = selectedAnnotationSet;
	}

	public JTextField getFontSizeTextField() {
		return fontSizeTextField;
	}
}
