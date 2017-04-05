package org.baderlab.csplugins.enrichmentmap.view.mastermap;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil.makeSmall;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.baderlab.csplugins.enrichmentmap.AfterInjection;
import org.baderlab.csplugins.enrichmentmap.resolver.DataSetParameters;
import org.baderlab.csplugins.enrichmentmap.view.util.FileBrowser;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

@SuppressWarnings("serial")
public class EditCommonPropertiesPanel extends JPanel implements DetailPanel {

	@Inject private FileUtil fileUtil;
	@Inject private IconManager iconManager;
	@Inject private Provider<JFrame> jframe;
	
	private JTextField gmtText;
	private JTextField expressionsText;
	
	private final DataSetParameters initDataSet;
	
	public interface Factory {
		EditCommonPropertiesPanel create(@Nullable DataSetParameters initDataSet);
	}
	
	@Inject
	public EditCommonPropertiesPanel(@Assisted @Nullable DataSetParameters initDataSet) {
		this.initDataSet = initDataSet;
	}
	
	
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
		JLabel gmtLabel = new JLabel("GMT File:");
		gmtText = new JTextField();
		gmtText.setText(initDataSet != null ? initDataSet.getFiles().getGMTFileName() : null);
		JButton gmtBrowse = EditDataSetPanel.createBrowseButton(iconManager);
		gmtBrowse.addActionListener(e -> browse(gmtText, FileBrowser.Filter.GMT));
		makeSmall(gmtLabel, gmtText);
		
		JLabel expressionsLabel = new JLabel("Expressions:");
		expressionsText = new JTextField();
		expressionsText.setText(initDataSet != null ? initDataSet.getFiles().getExpressionFileName() : null);
		JButton expressionsBrowse = EditDataSetPanel.createBrowseButton(iconManager);
		expressionsBrowse.addActionListener(e -> browse(expressionsText, FileBrowser.Filter.EXPRESSION));
		makeSmall(expressionsLabel, expressionsText);
		
		expressionsLabel.setEnabled(false);
		expressionsText.setEnabled(false);
		
		
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
	   		
		layout.setHorizontalGroup(
			layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.TRAILING)
					.addComponent(gmtLabel)
					.addComponent(expressionsLabel)
				)
				.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
					.addComponent(gmtText, 0, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(expressionsText, 0, DEFAULT_SIZE, Short.MAX_VALUE)
				)
				.addGroup(layout.createParallelGroup()
					.addComponent(gmtBrowse)
					.addComponent(expressionsBrowse)
				)
		);
		
		layout.setVerticalGroup(
			layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
					.addComponent(gmtLabel)
					.addComponent(gmtText)
					.addComponent(gmtBrowse)
				)
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
					.addComponent(expressionsLabel)
					.addComponent(expressionsText)
					.addComponent(expressionsBrowse)
				)
		);
		
   		if(LookAndFeelUtil.isAquaLAF())
   			setOpaque(false);
	}
	
	
	private void browse(JTextField textField, FileBrowser.Filter filter) {
		Optional<Path> path = FileBrowser.browse(fileUtil, jframe.get(), filter);
		path.map(Path::toString).ifPresent(textField::setText);
	}

	@Override
	public List<String> validateInput() {
		List<String> err = new ArrayList<>(2);
		if(!EditDataSetPanel.emptyOrReadable(gmtText)) {
			err.add("Enrichments file path is not valid.");
		}
		if(!EditDataSetPanel.emptyOrReadable(expressionsText)) {
			err.add("Enrichments 2 file path is not valid.");
		}
		return err;
	}
	
	
	public String getGmtFile() {
		return gmtText.getText().trim();
	}
	
}
