package org.baderlab.csplugins.enrichmentmap.view.mastermap;

import java.util.ArrayList;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JPanel;

import org.baderlab.csplugins.enrichmentmap.AfterInjection;
import org.baderlab.csplugins.enrichmentmap.view.util.FileBrowser;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

import com.google.inject.Inject;

@SuppressWarnings("serial")
public class EditCommonPanel extends JPanel implements DetailPanel {

	@Inject private PathTextField.Factory pathTextFactory;
	
	private PathTextField gmtText;
	private PathTextField expressionsText;
	
	
	@Override
	public String getIcon() {
		return IconManager.ICON_FILE_O;
	}

	@Override
	public String getDisplayName() {
		return "Common Files";
	}

	@Override
	public JPanel getPanel() {
		return this;
	}
	
	@AfterInjection
	private void createContents() {
		gmtText = pathTextFactory.create("GMT File:", FileBrowser.Filter.GMT);
		expressionsText = pathTextFactory.create("Expressions:", FileBrowser.Filter.EXPRESSION);
		
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
	   		
		layout.setHorizontalGroup(
			layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.TRAILING)
					.addComponent(gmtText.getLabel())
					.addComponent(expressionsText.getLabel())
				)
				.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
					.addComponent(gmtText.getTextField())
					.addComponent(expressionsText.getTextField())
				)
				.addGroup(layout.createParallelGroup()
					.addComponent(gmtText.getBrowseButton())
					.addComponent(expressionsText.getBrowseButton())
				)
		);
		
		layout.setVerticalGroup(
			layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
					.addComponent(gmtText.getLabel())
					.addComponent(gmtText.getTextField())
					.addComponent(gmtText.getBrowseButton())
				)
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
					.addComponent(expressionsText.getLabel())
					.addComponent(expressionsText.getTextField())
					.addComponent(expressionsText.getBrowseButton())
				)
		);
		
   		if(LookAndFeelUtil.isAquaLAF())
   			setOpaque(false);
	}
	

	@Override
	public List<String> validateInput() {
		gmtText.hideError();
		expressionsText.hideError();
		
		List<String> err = new ArrayList<>(2);
		if(!gmtText.emptyOrReadable())
			err.add(gmtText.showError("Enrichments file path is not valid."));
		if(!expressionsText.emptyOrReadable())
			err.add(expressionsText.showError("Enrichments 2 file path is not valid."));
		return err;
	}
	
	
	public String getGmtFile() {
		return gmtText.getText();
	}
	
	public String getExpressionFile() {
		return expressionsText.getText();
	}
	
}
