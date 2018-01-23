package org.baderlab.csplugins.enrichmentmap.view.postanalysis.web;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import javax.swing.JPanel;

public interface FileChooserPanel {

	JPanel getPanel();
	
	void setFiles(List<GmtFile> gmtFiles);
	
	Optional<String> getSelectedFilePath();
	
	void setSelectionListener(Consumer<Optional<String>> listener);
	
}
