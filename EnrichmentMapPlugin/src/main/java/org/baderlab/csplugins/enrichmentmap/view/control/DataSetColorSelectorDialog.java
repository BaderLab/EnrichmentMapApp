package org.baderlab.csplugins.enrichmentmap.view.control;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.commons.math3.util.Pair;
import org.baderlab.csplugins.enrichmentmap.AfterInjection;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.view.util.GBCFactory;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.cytoscape.util.color.BrewerType;
import org.cytoscape.util.color.Palette;
import org.cytoscape.util.swing.ColorButton;
import org.cytoscape.util.swing.CyColorPaletteChooser;
import org.cytoscape.util.swing.CyColorPaletteChooserFactory;
import org.cytoscape.util.swing.LookAndFeelUtil;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

@SuppressWarnings("serial")
public class DataSetColorSelectorDialog extends JDialog {
	
	@Inject private CyColorPaletteChooserFactory paletteChooserFactory;
	
	private final List<EMDataSet> dataSets;
	private final EnrichmentMap map;
	
	private final Map<EMDataSet,Color> newColors = new IdentityHashMap<>();
	private Color newCompoundColor = null;
	
	private final List<Pair<EMDataSet,ColorButton>> buttons  = new ArrayList<>();
	
	private boolean colorsChanged = false;
	
	public interface Factory {
		DataSetColorSelectorDialog create(List<EMDataSet> dataSets);
	}
	
	@Inject
	public DataSetColorSelectorDialog(@Assisted List<EMDataSet> dataSets) {
		setTitle("EnrichmentMap: Data Set Colors");
		if(dataSets == null || dataSets.isEmpty())
			throw new IllegalArgumentException("There must be at least one dataset to show dialog");
		this.dataSets = dataSets;
		this.map = dataSets.get(0).getMap();
	}
	
	@AfterInjection
	private void createContents() {
		JPanel compoundEdgePanel = createCompoundEdgeColorPanel();
		JPanel dataSetPanel = createDataSetColorsPanel();
		JPanel buttonPanel  = createButtonPanel();
		
		JScrollPane scrollPane = new JScrollPane(dataSetPanel);
		scrollPane.setAlignmentX(TOP_ALIGNMENT);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		JPanel panel = new JPanel(new BorderLayout());
		if(compoundEdgePanel != null)
			panel.add(compoundEdgePanel, BorderLayout.NORTH);
		panel.add(scrollPane, BorderLayout.CENTER);
		panel.add(buttonPanel, BorderLayout.SOUTH);
		setContentPane(panel);
	}
	
	private JPanel createCompoundEdgeColorPanel() {
		if(!map.useCompoundEdgeColor())
			return null;
		
		JPanel panel = new JPanel(new GridBagLayout());
		JLabel label = new JLabel("Edges   ");
		
		var color = map.getCompoundEdgeColor();
		if(color == null)
			color = dataSets.get(0).getColor();
		
		ColorButton colorButton = new ColorButton(color);
		
		colorButton.addPropertyChangeListener("color", pce -> {
			Color newColor = (Color) pce.getNewValue();
			newCompoundColor = newColor;
		});
		
		panel.add(label,       GBCFactory.grid(0,0).get());
		panel.add(colorButton, GBCFactory.grid(1,0).get());
		
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		return panel;
	}
	
	private JPanel createDataSetColorsPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		
		int y = 0;
		for(EMDataSet dataSet : dataSets) {
			JLabel label = new JLabel(dataSet.getName());
			ColorButton colorButton = new ColorButton(dataSet.getColor());
			colorButton.addPropertyChangeListener("color", pce -> {
				Color newColor = (Color) pce.getNewValue();
				// don't set the color on the data set until the "ok" button is pressed, in case user cancels
				newColors.put(dataSet, newColor); 
			});
			buttons.add(new Pair<>(dataSet,colorButton));
			
			panel.add(label,       GBCFactory.grid(0,y).get());
			panel.add(colorButton, GBCFactory.grid(1,y).get());
			y += 1;
		}
		
		JButton paletteButton = new JButton("Color Palettes");
		SwingUtil.makeSmall(paletteButton);
		
		panel.add(paletteButton, GBCFactory.grid(3, 0).gridheight(y).anchor(GridBagConstraints.NORTH).get());
		paletteButton.addActionListener(e -> showPaletteDialog());
		
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		return panel;
	}
	
	private JPanel createButtonPanel() {
		JButton okButton = new JButton("Ok");
		okButton.addActionListener(e -> {
			applyColors();
			dispose();
		});
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(e -> dispose());
		
		return LookAndFeelUtil.createOkCancelPanel(okButton, cancelButton);
	}
	
	private void showPaletteDialog() {
		CyColorPaletteChooser chooser = paletteChooserFactory.getColorPaletteChooser(BrewerType.ANY, true);
		Palette palette = chooser.showDialog(DataSetColorSelectorDialog.this, "Color Palettes", null, dataSets.size());
		if(palette != null) {
			Color[] colors = palette.getColors();
			for(int i = 0; i < colors.length; i++) {
				EMDataSet dataSet  = buttons.get(i).getFirst();
				ColorButton button = buttons.get(i).getSecond();
				Color color = colors[i];
				// don't set the color on the data set until the "ok" button is pressed, in case user cancels
				newColors.put(dataSet, color);
				button.setColor(color);
			}
		}
	}
	
	private void applyColors() {
		if(newColors.isEmpty() && newCompoundColor == null)
			return;
		
		colorsChanged = true;
		newColors.forEach(EMDataSet::setColor);
		map.setCompoundEdgeColor(newCompoundColor);
	}
	
	public boolean colorsChanged() {
		return colorsChanged;
	}
}
