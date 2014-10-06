package org.baderlab.csplugins.enrichmentmap.autoannotate.view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URL;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationManager;
import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationParameters;
import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationUtils;
import org.baderlab.csplugins.enrichmentmap.autoannotate.action.AutoAnnotationActions;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.AnnotationSet;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.Cluster;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.LabelOptions;
import org.baderlab.csplugins.enrichmentmap.autoannotate.task.DrawClusterEllipseTask;
import org.baderlab.csplugins.enrichmentmap.autoannotate.task.DrawClusterLabelTask;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.work.TaskIterator;

public class DisplayOptionsPanel extends JPanel implements CytoPanelComponent {

	private static final long serialVersionUID = -7883203344022939503L;
	protected static final String WORDCLOUD_PANEL_NAME = "WordCloud Display";
	private AnnotationSet selectedAnnotationSet;
	private JTextField fontSizeTextField;
	private JCheckBox showEllipsesCheckBox;
	private JRadioButton heatmapButton;
	protected JPanel slidersPanel;

	private static String proportionalSizeButtonString = "Font size by # of nodes";
	private static String constantSizeButtonString = "Constant font size";
	
	public DisplayOptionsPanel() {
		
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setPreferredSize(new Dimension(300, 300));
		
		add(createEllipseWidthSliderPanel());
		add(createEllipseOpacitySliderPanel());
		add(createShowAnnotationsCheckBoxPanel());
		add(createShapeTypePanel());
		add(createSelectionPanel());
		add(createFontSizePanel());
		add(createLabelOptionsButtonPanel());
	}
	
	private JPanel createEllipseWidthSliderPanel() {
		// Slider to set width of the ellipses
		JLabel sliderLabel = new JLabel("Shape Border Width");
		JSlider ellipseWidthSlider = new JSlider(1, 10, 3);
		ellipseWidthSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
		        JSlider ellipseWidthSlider = (JSlider) e.getSource();
		        if (selectedAnnotationSet != null) {
		        	selectedAnnotationSet.setEllipseWidth(ellipseWidthSlider.getValue());
		        }
			}
		});
		JPanel ellipseWidthSliderPanel = new JPanel();
		ellipseWidthSliderPanel.setLayout(new BoxLayout(ellipseWidthSliderPanel, BoxLayout.PAGE_AXIS));
		ellipseWidthSliderPanel.add(sliderLabel);
		ellipseWidthSliderPanel.add(ellipseWidthSlider);
		return ellipseWidthSliderPanel;
	}
	
	private JPanel createEllipseOpacitySliderPanel() {
		// Slider to set width of the ellipses
		JLabel sliderLabel = new JLabel("Shape Opacity");
		JSlider ellipseOpacitySlider = new JSlider(1, 100, 20);
		ellipseOpacitySlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider ellipseOpacitySlider = (JSlider) e.getSource();
				if (selectedAnnotationSet != null) {
					selectedAnnotationSet.setEllipseOpacity(ellipseOpacitySlider.getValue());
				}
			}
		});
		JPanel ellipseOpacitySliderPanel = new JPanel();
		ellipseOpacitySliderPanel.setLayout(new BoxLayout(ellipseOpacitySliderPanel, BoxLayout.PAGE_AXIS));
		ellipseOpacitySliderPanel.add(sliderLabel);
		ellipseOpacitySliderPanel.add(ellipseOpacitySlider);
		return ellipseOpacitySliderPanel;
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
		    		selectedAnnotationSet.setFontSize(fontSize);
		    		AutoAnnotationUtils.updateFontSizes();
		    	} catch (Exception ex) {
		            JOptionPane.showMessageDialog(null,
		                    "Error: Please enter an integer bigger than 0", "Error Message",
		                    JOptionPane.ERROR_MESSAGE);
		    	}
		    }
		});
		fontSizeTextField.setText(String.valueOf(AnnotationSet.DEFAULT_FONT_SIZE));
		// Initially disabled
		fontSizeTextField.setEnabled(false);
		constantSizeButton.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				Integer fontSize = 1;
				if (e.getStateChange() == ItemEvent.SELECTED) {
					fontSizeTextField.setEnabled(true);
			    	fontSize = Integer.parseInt(fontSizeTextField.getText());
				} else if (e.getStateChange() == ItemEvent.DESELECTED) {
					fontSizeTextField.setEnabled(false);
				}
				if (selectedAnnotationSet != null) {
					selectedAnnotationSet.setFontSize(fontSize);
					selectedAnnotationSet.setConstantFontSize(fontSizeTextField.isEnabled());
					AutoAnnotationUtils.updateFontSizes();
				}
				updateUI();
			}
		});

		
		// Group buttons together to make them mutually exclusive
		ButtonGroup fontButtonGroup = new ButtonGroup();
		fontButtonGroup.add(proportionalSizeButton);
		fontButtonGroup.add(constantSizeButton);

		JPanel fontSizePanel = new JPanel(new GridLayout(2, 1));
		JPanel constantSizePanel = new JPanel();
		constantSizePanel.add(constantSizeButton);
		constantSizePanel.add(fontSizeTextField);
		fontSizePanel.add(proportionalSizeButton);
		fontSizePanel.add(constantSizePanel);
		return fontSizePanel;
	}
	
	
	private JPanel createShowAnnotationsCheckBoxPanel() {
		// By default show ellipses around clusters
		showEllipsesCheckBox = new JCheckBox("Show clusters");
		showEllipsesCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (selectedAnnotationSet != null) {
					selectedAnnotationSet.setShowEllipses(showEllipsesCheckBox.isSelected());
					if (selectedAnnotationSet.isShowEllipses()) {
						selectedAnnotationSet.updateCoordinates();
						//create iterator - to avoid concurrent modifications on the network create iterator for all tasks
						TaskIterator currentIterator = new TaskIterator();
						for (Cluster cluster : selectedAnnotationSet.getClusterMap().values()) {
							DrawClusterEllipseTask drawellipse = new DrawClusterEllipseTask(cluster);
							currentIterator.append(drawellipse);								
						}
						AutoAnnotationManager.getInstance().getDialogTaskManager().execute(currentIterator);
												
					} else {
						for (Cluster cluster : selectedAnnotationSet.getClusterMap().values()) {
							cluster.eraseEllipse();
						}
					}
				}
			}
		});
		showEllipsesCheckBox.setSelected(true);
		// By default show text above clusters
		JCheckBox showTextCheckBox = new JCheckBox("Show text labels");
		showTextCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (selectedAnnotationSet != null) {
					JCheckBox showTextCheckBox = (JCheckBox) e.getSource();
					selectedAnnotationSet.setShowLabel(showTextCheckBox.isSelected());
					if (selectedAnnotationSet.isShowLabel()) {
						selectedAnnotationSet.updateCoordinates();
						TaskIterator currentTasks = new TaskIterator();
						for (Cluster cluster : selectedAnnotationSet.getClusterMap().values()) {
							
							DrawClusterLabelTask drawlabel = new DrawClusterLabelTask(cluster);
							currentTasks.append(drawlabel);
						}
						AutoAnnotationManager.getInstance().getDialogTaskManager().execute(currentTasks);
					} else {
						for (Cluster cluster : selectedAnnotationSet.getClusterMap().values()) {
							cluster.eraseText();
						}
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
	
	private JPanel createLabelOptionsButtonPanel() {
		JPanel labelOptionsButtonPanel = new JPanel();
		JButton labelOptionsButton = new JButton("Adjust Label Options");
		labelOptionsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (selectedAnnotationSet != null) {
					LabelOptionsEditor labelOptionsEditor = new LabelOptionsEditor(selectedAnnotationSet);
					LabelOptions labelOptions = labelOptionsEditor.showDialog();
					selectedAnnotationSet.setLabelOptions(labelOptions);
					// Redraw labels (position may have changed)
					AutoAnnotationActions.updateAction(selectedAnnotationSet);
				}
			}
		});
		labelOptionsButtonPanel.add(labelOptionsButton);
		return labelOptionsButtonPanel;
	}
	
	private JPanel createShapeTypePanel() {
		JPanel shapeTypePanel = new JPanel();

		JLabel label = new JLabel("Shape type:");

		JRadioButton ellipseButton = new JRadioButton("Ellipse");
		JRadioButton rectangleButton = new JRadioButton("Rectangle");
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(ellipseButton);
		buttonGroup.add(rectangleButton);

		ellipseButton.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				AutoAnnotationManager autoAnnotationManager = AutoAnnotationManager.getInstance();
				JRadioButton ellipseButton = (JRadioButton) e.getSource();
				String shapeType = ellipseButton.isSelected() ? "ELLIPSE" : "ROUNDEDRECTANGLE";
				for (AutoAnnotationParameters params : autoAnnotationManager.getNetworkViewToAutoAnnotationParameters().values()) {
					for (AnnotationSet annotationSet : params.getAnnotationSets().values()) {
						annotationSet.setShapeType(shapeType);
						// Redraw if annotation set is currently selected
						if (annotationSet.equals(selectedAnnotationSet)) {
							//create iterator - to avoid concurrent modifications on the network create iterator for all tasks
							TaskIterator currentIterator = new TaskIterator();
							for (Cluster cluster : selectedAnnotationSet.getClusterMap().values()) {
								cluster.eraseEllipse();
								DrawClusterEllipseTask drawellipse = new DrawClusterEllipseTask(cluster);
								currentIterator.append(drawellipse);								
							}
							AutoAnnotationManager.getInstance().getDialogTaskManager().execute(currentIterator);
						}
					}
				}
				if (selectedAnnotationSet != null) {
					selectedAnnotationSet.setShapeType(ellipseButton.isSelected() ? "ELLIPSE" : "ROUNDEDRECTANGLE");
					//create iterator - to avoid concurrent modifications on the network create iterator for all tasks
					TaskIterator currentIterator = new TaskIterator();
					for (Cluster cluster : selectedAnnotationSet.getClusterMap().values()) {
						cluster.eraseEllipse();
						DrawClusterEllipseTask drawellipse = new DrawClusterEllipseTask(cluster);
						currentIterator.append(drawellipse);
					}
					AutoAnnotationManager.getInstance().getDialogTaskManager().execute(currentIterator);
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
	
	private JPanel createSelectionPanel() {
		JPanel selectionPanel = new JPanel();

		JLabel label = new JLabel("Show on selection:");

		heatmapButton = new JRadioButton("Heat Map");
		JRadioButton wordCloudButton = new JRadioButton("WordCloud");
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(wordCloudButton);
		buttonGroup.add(heatmapButton);
		
		wordCloudButton.setSelected(true);
		wordCloudButton.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				AutoAnnotationManager autoAnnotationManager = AutoAnnotationManager.getInstance();
				CytoPanel southPanel = autoAnnotationManager.getSouthPanel();
				if (e.getStateChange() == ItemEvent.SELECTED) {
					// Switch to WordCloud panel
					for (int index = 0; index < southPanel.getCytoPanelComponentCount(); index++) {
						try {
							CytoPanelComponent panel = (CytoPanelComponent) southPanel.getComponentAt(index);
							if ((panel.getTitle().equals(WORDCLOUD_PANEL_NAME))) {
								southPanel.setSelectedIndex(index);
							}
						} catch (Exception ex) {
							// If the panel doesn't implement CytoPanel
							continue;
						}
					}
				} else {
					// Switch to heatmap panel
					int index = southPanel.indexOfComponent(autoAnnotationManager.getHeatmapPanel());
					if (index != -1) {
						southPanel.setSelectedIndex(southPanel.indexOfComponent(autoAnnotationManager.getHeatmapPanel()));
					}					
				}
			}
		});

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
}
