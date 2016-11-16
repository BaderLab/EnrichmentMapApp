package org.baderlab.csplugins.enrichmentmap.view.util;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.swing.JFileChooser;

import org.cytoscape.util.swing.FileChooserFilter;
import org.cytoscape.util.swing.FileUtil;

/**
 * Because the Cytoscape FileUtil service doesn't let you browse for directories :(
 */
public class FileBrowser {

	public static Optional<Path> browseGMT(FileUtil fileUtil, Component parent) {
		String title = "GMT Files";
		List<FileChooserFilter> filters = Arrays.asList(new FileChooserFilter("gmt Files", "gmt")); 
		File file = fileUtil.getFile(parent, title, FileUtil.LOAD, filters);
		return Optional.ofNullable(file).map(File::toPath);
	}
	
	
	public static Optional<Path> browseExpression(FileUtil fileUtil, Component parent) {
		String title= "Expression Files";
		FileChooserFilter gct = new FileChooserFilter("gct Files", "gct");          
        FileChooserFilter rnk = new FileChooserFilter("rnk Files", "rnk");
        FileChooserFilter txt = new FileChooserFilter("txt Files", "txt");
        List<FileChooserFilter> filters = Arrays.asList(gct, rnk, txt);
        File file = fileUtil.getFile(parent, title, FileUtil.LOAD, filters);
		return Optional.ofNullable(file).map(File::toPath);
	}
	
	
//	private List<File> browseEnrichments() {
//		FileChooserFilter xls = new FileChooserFilter("gct Files", "xls");          
//        FileChooserFilter bgo = new FileChooserFilter("rnk Files", "bgo");
//        FileChooserFilter txt = new FileChooserFilter("txt Files", "txt");
//        FileChooserFilter tsv = new FileChooserFilter("tsv Files", "tsv");
//        List<FileChooserFilter> filters = Arrays.asList(xls, bgo, txt, tsv);
//        File[] files = fileUtil.getFiles(callback.getDialogFrame(), "Enrichment Files", FileUtil.LOAD, filters);
//        return files == null ? Collections.emptyList() : Arrays.asList(files);
//	}
	
	
	public static Optional<File> browseForRootFolder(Dialog parent) {
		final String osName = System.getProperty("os.name");
		if(osName.startsWith("Mac"))
			return browseForRootFolderMac(parent);
		else
			return browseForRootFolderSwing(parent);
	}
	
	
	private static Optional<File> browseForRootFolderMac(Dialog parent) {
		final String property = System.getProperty("apple.awt.fileDialogForDirectories");
		System.setProperty("apple.awt.fileDialogForDirectories", "true");
		try {
			FileDialog chooser = new FileDialog(parent, "Choose Root Folder", FileDialog.LOAD);
			chooser.setModal(true);
			chooser.setLocationRelativeTo(parent);
			chooser.setVisible(true);
			
			String file = chooser.getFile();
			String dir = chooser.getDirectory();
			
			if(file == null || dir == null) {
				return Optional.empty();
			}
			return Optional.of(new File(dir + File.separator + file));
		} finally {
			if(property != null) {
				System.setProperty("apple.awt.fileDialogForDirectories", property);
			}
		}
	}
	
	private static Optional<File> browseForRootFolderSwing(Dialog parent) {
		JFileChooser chooser = new JFileChooser(); 
	    chooser.setDialogTitle("Select Root Folder");
	    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	    chooser.setAcceptAllFileFilterUsed(false);
	    if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) { 
	    	return Optional.of(chooser.getSelectedFile());
	    }
	    return Optional.empty();
	}
	
}
