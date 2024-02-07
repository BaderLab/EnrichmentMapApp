package org.baderlab.csplugins.enrichmentmap.view.creation;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil.makeSmall;
import static org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil.simpleDocumentListener;

import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.baderlab.csplugins.enrichmentmap.AfterInjection;
import org.baderlab.csplugins.enrichmentmap.view.util.ComboItem;
import org.baderlab.csplugins.enrichmentmap.view.util.OpenBrowser;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

@SuppressWarnings("serial")
public class NameAndLayoutPanel extends JPanel {

	@Inject Provider<OpenBrowser> browserProvider;
	@Inject Provider<DependencyChecker> dependencyCheckerProvider;
	@Inject CyLayoutAlgorithmManager layoutManager;
	
	private JCheckBox useAutomaticCheck;
	private JTextField nameText;
	
	private JComboBox<ComboItem<CyLayoutAlgorithm>> layoutComboBox;
	
	private JLabel yFilesLink;
	private JCheckBox autoAnnotateCheck;
	
	private String automaticValue;
	private String manualValue;
	
	private Runnable closeRunnable;
	
	
	@AfterInjection
	private void createContents() {
		setBorder(LookAndFeelUtil.createPanelBorder());
		
		JLabel networkLabel = new JLabel("Network Name:");
		useAutomaticCheck = new JCheckBox("Use Default");
		nameText = new JTextField();
		
		JLabel layoutLabel = new JLabel("Layout:");
		layoutComboBox = new JComboBox<>();
		fillLayoutCombo();
		
		yFilesLink = createInstallLink();
		
		autoAnnotateCheck = new JCheckBox("Highlight Significant Nodes with AutoAnnotate");

		makeSmall(networkLabel, useAutomaticCheck, nameText);
		makeSmall(layoutLabel, layoutComboBox, yFilesLink, autoAnnotateCheck);
		
		useAutomaticCheck.setSelected(true);
		nameText.setEnabled(false);
		
		useAutomaticCheck.addActionListener(e -> {
			nameText.setText(isAutomatic() ? automaticValue : manualValue);
			nameText.setEnabled(!isAutomatic());
		});
		
		nameText.getDocument().addDocumentListener(simpleDocumentListener(() -> {
			if(!isAutomatic()) {
				manualValue = nameText.getText();
			}
		}));
		
		
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addGroup(layout.createSequentialGroup()
				.addComponent(networkLabel)
				.addComponent(useAutomaticCheck)
				.addComponent(nameText)
			)
			.addGroup(layout.createSequentialGroup()
				.addComponent(layoutLabel)
				.addComponent(layoutComboBox, 0, 400, 400)
				.addComponent(yFilesLink)
			)
			.addGroup(layout.createSequentialGroup()
				.addComponent(autoAnnotateCheck)
			)
		);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(networkLabel)
				.addComponent(useAutomaticCheck)
				.addComponent(nameText, 0, DEFAULT_SIZE, Short.MAX_VALUE)
			)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(layoutLabel)
				.addComponent(layoutComboBox)
				.addComponent(yFilesLink)
			)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(autoAnnotateCheck)
			)
		);
		
		opened();
	}
	
	
	public void opened() {
		boolean aaInstalled = dependencyCheckerProvider.get().isCommandAvailable("autoannotate", "eminit");
		boolean yfInstalled = dependencyCheckerProvider.get().isYFilesInstalled();
		autoAnnotateCheck.setEnabled(aaInstalled);
		autoAnnotateCheck.setSelected(aaInstalled);
		yFilesLink.setVisible(!yfInstalled);
		fillLayoutCombo();
	}
	
	
	public void setCloseCallback(Runnable onClose) {
		this.closeRunnable = onClose;
	}
	
	private JLabel createInstallLink() {
		return SwingUtil.createLinkLabel(
				"Install yFiles Organic Layout (recommended)", 
				browserProvider.get(), 
				"https://apps.cytoscape.org/apps/yfileslayoutalgorithms", 
				() -> {
					if(closeRunnable != null) {
						closeRunnable.run();
					}
				});
	}
	
	
	private void fillLayoutCombo() {
		Vector<ComboItem<CyLayoutAlgorithm>> layouts = new Vector<>();
		CyLayoutAlgorithm layout;
		CyLayoutAlgorithm layoutToSelect = null;
		
		if((layout = layoutManager.getLayout("force-directed")) != null) { // This layout should always be available
			layouts.add(ComboItem.of(layout));
			layoutToSelect = layout;
		}
		if((layout = layoutManager.getLayout("force-directed-cl")) != null) {
			layouts.add(ComboItem.of(layout));
		}
		if((layout = layoutManager.getLayout("yfiles.OrganicLayout")) != null) {
			layouts.add(ComboItem.of(layout));
			layoutToSelect = layout;
		}
		
		layoutComboBox.setModel(new DefaultComboBoxModel<>(layouts));
		if(layoutToSelect != null)
			layoutComboBox.setSelectedItem(ComboItem.of(layoutToSelect));
	}
	
	
	public boolean isAutomatic() {
		return useAutomaticCheck.isSelected();
	}

	public String getNameText() {
		return isAutomatic() ? automaticValue : manualValue;
	}
	
	public void setAutomaticName(String networkName) {
		this.automaticValue = networkName;
		if(isAutomatic()	) {
			nameText.setText(automaticValue);
		}
	}
	
	public CyLayoutAlgorithm getLayoutAlgorithm() {
		return layoutComboBox.getItemAt(layoutComboBox.getSelectedIndex()).getValue();
	}
	
	public boolean isRunAutoAnnotate() {
		return autoAnnotateCheck.isSelected();
	}

}
