package org.baderlab.csplugins.enrichmentmap.autoannotate.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationManager;
import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationUtils;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.AnnotationSet;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.Cluster;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.util.swing.BasicCollapsiblePanel;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;

public class DisplayOptionsPanel extends JPanel implements CytoPanelComponent {

	private static final long serialVersionUID = -7883203344022939503L;
	private AnnotationSet selectedAnnotationSet;
	private JTextField fontSizeTextField;
	private JCheckBox showEllipsesCheckBox;
	private AutoAnnotationManager autoAnnotationManager;
	private JRadioButton heatmapButton;

	private static final String proportionalSizeButtonString = "Font size by # of nodes";
	private static final String constantSizeButtonString = "Constant font size";
	
	public DisplayOptionsPanel() {
		this.autoAnnotationManager = AutoAnnotationManager.getInstance();
		
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setPreferredSize(new Dimension(300, 300));
		
		add(createFontSizePanel());
		add(createShowEllipsesCheckBoxPanel());
		add(createSelectionPanel());
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
		    		AutoAnnotationUtils.updateFontSizes(fontSize, fontSizeTextField.isEnabled());
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
				AutoAnnotationUtils.updateFontSizes(fontSize, fontSizeTextField.isEnabled());
				updateUI();
			}
		});

		
		// Group buttons together to make them mutually exclusive
		ButtonGroup fontButtonGroup = new ButtonGroup();
		fontButtonGroup.add(proportionalSizeButton);
		fontButtonGroup.add(constantSizeButton);

		JPanel fontSizePanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		fontSizePanel.setBorder(BorderFactory.createTitledBorder("Font Size Options"));
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
		showEllipsesCheckBox = new JCheckBox("Draw ellipses around clusters");
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
								shapeFactory, annotationManager, showEllipses);
					}
				} else {
					for (Cluster cluster : selectedAnnotationSet.getClusterMap().values()) {
						cluster.eraseEllipse();
					}
				}
			}
		}); // Checks if checkbox is selected
		showEllipsesCheckBox.setSelected(true);
		JPanel checkBoxPanel = new JPanel();
		checkBoxPanel.add(showEllipsesCheckBox);
		return checkBoxPanel;
	}	
	
	public JCheckBox getShowEllipsesCheckBox() {
		return showEllipsesCheckBox;
	}
	
	public void setShowEllipsesCheckBox(JCheckBox showEllipsesCheckBox) {
		this.showEllipsesCheckBox = showEllipsesCheckBox;
	}

	private JPanel createSelectionPanel() {
		JPanel selectionPanel = new JPanel(new BorderLayout());
		selectionPanel.setBorder(BorderFactory.createTitledBorder("Autofocus Preferences"));

		JPanel labelPanel = new JPanel();
		JLabel label = new JLabel("Show on selection:");
		labelPanel.add(label);

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

		selectionPanel.add(labelPanel, BorderLayout.WEST);
		selectionPanel.add(radioButtonPanel, BorderLayout.EAST);
		
		return selectionPanel;
	}
	
	public JRadioButton getHeatmapButton() {
		return heatmapButton;
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
