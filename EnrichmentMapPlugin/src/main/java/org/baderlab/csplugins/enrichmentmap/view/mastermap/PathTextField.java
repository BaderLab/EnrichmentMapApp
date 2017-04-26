package org.baderlab.csplugins.enrichmentmap.view.mastermap;

import static org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil.makeSmall;

import java.awt.Color;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.baderlab.csplugins.enrichmentmap.view.util.FileBrowser;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

public class PathTextField {
	
	@Inject private FileUtil fileUtil;
	@Inject private Provider<JFrame> jframeProvider;
	
	private JLabel label;
	private JTextField textField;
	private JButton browseButton;
	
	private Color labelForeground;
	private Color textForeground;
	
	public static interface Factory {
		PathTextField create(String labelText, @Nullable FileBrowser.Filter fileType);
	}
	
	@Inject
	public PathTextField(@Assisted String labelText, @Assisted @Nullable FileBrowser.Filter fileType, IconManager iconManager) {
		label = new JLabel(labelText);
		textField = new JTextField();
		browseButton = createBrowseButton(iconManager);
		browseButton.addActionListener(e -> browse(fileType));
		makeSmall(label, textField);
		
		labelForeground = label.getForeground();
		textForeground = textField.getForeground();
	}
	
	
	public JLabel getLabel() {
		return label;
	}
	
	public JTextField getTextField() {
		return textField; 
	}
	
	public JButton getBrowseButton() {
		return browseButton;
	}
	
	public String getText() {
		return textField.getText().trim();
	}
	
	public void setText(String textValue) {
		if(textValue != null) {
			textField.setText(textValue);
			textField.setCaretPosition(textValue.length());
		}
	}
	
	public void hideError() {
		label.setForeground(labelForeground);
		textField.setForeground(textForeground);
		label.setToolTipText("");
	}
	
	public String showError(String tooltip) {
		label.setForeground(Color.RED);
		textField.setForeground(Color.RED);
		label.setToolTipText(tooltip);
		textField.setToolTipText(tooltip);
		return tooltip;
	}
	
	public void addValidationListener(Runnable runnable) {
		textField.addFocusListener(SwingUtil.simpleFocusListener(runnable));
	}
	
	public boolean emptyOrReadable() {
		return emptyOrReadable(textField.getText());
	}
	
	private static boolean emptyOrReadable(String text) {
		return Strings.isNullOrEmpty(text) || Files.isReadable(Paths.get(text));
	}
	
	public boolean isEmpty() {
		return Strings.isNullOrEmpty(getText());
	}
	
	private void browse(FileBrowser.Filter filter) {
		if(filter == null)
			return;
		Optional<Path> path = FileBrowser.browse(fileUtil, jframeProvider.get(), filter);
		path.map(Path::toString).ifPresent(textField::setText);
	}
	
	private static JButton createBrowseButton(IconManager iconManager) {
		JButton button = new JButton(IconManager.ICON_ELLIPSIS_H);
		button.setFont(iconManager.getIconFont(10.0f));
		button.setToolTipText("Browse...");
		if(LookAndFeelUtil.isAquaLAF()) {
			button.putClientProperty("JButton.buttonType", "gradient");
			button.putClientProperty("JComponent.sizeVariant", "small");
		}
		return button;
	}

	private static JLabel createErrorIcon(IconManager iconManager) {
		JLabel icon = new JLabel(IconManager.ICON_TIMES_CIRCLE);
		icon.setFont(iconManager.getIconFont(13f));
		icon.setForeground(Color.RED.darker());
		icon.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
		return icon;
	}
}
